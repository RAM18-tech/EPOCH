package com.dtv.dcp.epoch.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.redis.cache.CacheStatistics;
import org.springframework.data.redis.cache.CacheStatisticsCollector;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.lang.Nullable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.Assert;

public class IdpRedisCacheWriter implements RedisCacheWriter {
  private static final Logger log = LoggerFactory.getLogger(IdpRedisCacheWriter.class);
  
  private RedisConnectionFactory connectionFactory;
  
  private Duration sleepTime;
  
  private List<String> mapNamesPut;
  
  private List<String> mapNamesGet;
  
  private List<String> mapNamesMiss;
  
  private List<String> mapNamesError;
  
  private static MeterRegistry meterRegistry;
  
  private static RedisPropertyManager redisPropertyManager;
  
  private static final String MAPNAME = "mapname";
  
  private static final String IDPCACHETAGPREFIX = "idp_cache_metrics_";
  
  private static final String NAMELOGVERBOSE = "Name must not be null!";
  
  private static final String VALUELOGVERBOSE = "Value must not be null!";
  
  private static final String KEYLOGVERBOSE = "Key must not be null!";
  
  public IdpRedisCacheWriter() {
    log.info("default constructor");
  }
  
  public IdpRedisCacheWriter(RedisConnectionFactory connectionFactory) {
    this(connectionFactory, Duration.ZERO);
  }
  
  public static void setMeterRegistry(MeterRegistry meterRegistry) {
    IdpRedisCacheWriter.meterRegistry = meterRegistry;
  }
  
  public static void setRedisPropertyManager(RedisPropertyManager redisPropertyManager) {
    IdpRedisCacheWriter.redisPropertyManager = redisPropertyManager;
  }
  
  public IdpRedisCacheWriter(RedisConnectionFactory connectionFactory, Duration sleepTime) {
    Assert.notNull(connectionFactory, "ConnectionFactory must not be null!");
    Assert.notNull(sleepTime, "SleepTime must not be null!");
    this.connectionFactory = connectionFactory;
    this.sleepTime = sleepTime;
    this.mapNamesPut = new ArrayList<>();
    this.mapNamesGet = new ArrayList<>();
    this.mapNamesMiss = new ArrayList<>();
    this.mapNamesError = new ArrayList<>();
  }
  
  @Retryable(value = {Exception.class}, maxAttemptsExpression = "#{${redis.cluster.cache-operation-retry:3}}", backoff = @Backoff(delayExpression = "#{${redis.cluster.cache-operation-delay:10}}"))
  public void put(String name, byte[] key, byte[] value, @Nullable Duration ttl) {
    cacheCounterMeterRegistry(this.mapNamesPut, "cache.puts", "mapname", name, "Number of Cache puts");
    Assert.notNull(name, "Name must not be null!");
    Assert.notNull(key, "Key must not be null!");
    Assert.notNull(value, "Value must not be null!");
    long start = System.currentTimeMillis();
    execute(name, connection -> {
          if (shouldExpireWithin(ttl)) {
            connection.set(key, value, Expiration.from(ttl.toMillis(), TimeUnit.MILLISECONDS), RedisStringCommands.SetOption.upsert());
          } else {
            connection.set(key, value);
          } 
          cacheTimerMeterRegistry(this.mapNamesGet, "cache.timer.puts", "mapname", name, "Time Taken For Cache Puts", Long.valueOf(System.currentTimeMillis() - start));
          return "OK";
        });
  }
  
  @Retryable(value = {Exception.class}, maxAttemptsExpression = "#{${redis.cluster.cache-operation-retry:3}}", backoff = @Backoff(delayExpression = "#{${redis.cluster.cache-operation-delay:10}}"))
  public byte[] get(String name, byte[] key) {
    Assert.notNull(name, "Name must not be null!");
    Assert.notNull(key, "Key must not be null!");
    long start = System.currentTimeMillis();
    byte[] result = execute(name, connection -> connection.get(key));
    if (null == result || result.length == 0) {
      cacheTimerMeterRegistry(this.mapNamesGet, "cache.timer.miss", "mapname", name, "Time Taken For Cache Miss", Long.valueOf(System.currentTimeMillis() - start));
      cacheCounterMeterRegistry(this.mapNamesMiss, "cache.miss", "mapname", name, "Number of Cache Miss");
    } else {
      cacheTimerMeterRegistry(this.mapNamesGet, "cache.timer.gets", "mapname", name, "Time Taken For Cache Gets", Long.valueOf(System.currentTimeMillis() - start));
      cacheCounterMeterRegistry(this.mapNamesGet, "cache.gets", "mapname", name, "Number of Cache Gets");
    } 
    return result;
  }
  
  @Retryable(value = {Exception.class}, maxAttemptsExpression = "#{${redis.cluster.cache-operation-retry:3}}", backoff = @Backoff(delayExpression = "#{${redis.cluster.cache-operation-delay:10}}"))
  public byte[] putIfAbsent(String name, byte[] key, byte[] value, @Nullable Duration ttl) {
    Assert.notNull(name, "Name must not be null!");
    Assert.notNull(key, "Key must not be null!");
    Assert.notNull(value, "Value must not be null!");
    return execute(name, connection -> {
          if (isLockingCacheWriter())
            doLock(name, connection); 
          try {
            if (connection.setNX(key, value).booleanValue()) {
              if (shouldExpireWithin(ttl))
                connection.pExpire(key, ttl.toMillis()); 
              return null;
            } 
            return connection.get(key);
          } finally {
            if (isLockingCacheWriter())
              doUnlock(name, connection); 
          } 
        });
  }
  
  @Retryable(value = {Exception.class}, maxAttemptsExpression = "#{${redis.cluster.cache-operation-retry:3}}", backoff = @Backoff(delayExpression = "#{${redis.cluster.cache-operation-delay:10}}"))
  public void remove(String name, byte[] key) {
    Assert.notNull(name, "Name must not be null!");
    Assert.notNull(key, "Key must not be null!");
    long start = System.currentTimeMillis();
    execute(name, connection -> connection.del(new byte[][] { key }));
    cacheTimerMeterRegistry(this.mapNamesGet, "cache.timer.evict", "mapname", name, "Time Taken For Cache Evicts", Long.valueOf(System.currentTimeMillis() - start));
    cacheCounterMeterRegistry(this.mapNamesGet, "cache.evict", "mapname", name, "Number of Cache Evicts");
  }
  
  @Retryable(value = {Exception.class}, maxAttemptsExpression = "#{${redis.cluster.cache-operation-retry:3}}", backoff = @Backoff(delayExpression = "#{${redis.cluster.cache-operation-delay:10}}"))
  public void clean(String name, byte[] pattern) {
    Assert.notNull(name, "Name must not be null!");
    Assert.notNull(pattern, "Pattern must not be null!");
    execute(name, connection -> {
          boolean wasLocked = false;
          try {
            if (isLockingCacheWriter()) {
              doLock(name, connection);
              wasLocked = true;
            } 
            byte[][] keys = (byte[][])((Set)Optional.<Set>ofNullable(connection.keys(pattern)).orElse(Collections.emptySet())).toArray((Object[])new byte[0][]);
            if (keys.length > 0)
              connection.del(keys); 
          } finally {
            if (wasLocked && isLockingCacheWriter())
              doUnlock(name, connection); 
          } 
          return "OK";
        });
  }
  
  private Boolean doLock(String name, RedisConnection connection) {
    return connection.setNX(createCacheLockKey(name), new byte[0]);
  }
  
  private Long doUnlock(String name, RedisConnection connection) {
    return connection.del(new byte[][] { createCacheLockKey(name) });
  }
  
  boolean doCheckLock(String name, RedisConnection connection) {
    return connection.exists(createCacheLockKey(name)).booleanValue();
  }
  
  private boolean isLockingCacheWriter() {
    return (!this.sleepTime.isZero() && !this.sleepTime.isNegative());
  }
  
  private <T> T execute(String name, Function<RedisConnection, T> callback) {
    RedisConnection connection = null;
    try {
      connection = this.connectionFactory.getConnection();
    } catch (Exception ex) {
      cacheCounterMeterRegistry(this.mapNamesError, "cache.connectionError", "mapname", name, "Number of Redis Connection Errors from Client Services");
      log.error("Exception occccured while getting redis connection " + ex);
    } 
    try {
      checkAndPotentiallyWaitUntilUnlocked(name, connection);
      return callback.apply(connection);
    } finally {
      if (null != connection)
        connection.close(); 
    } 
  }
  
  private void checkAndPotentiallyWaitUntilUnlocked(String name, RedisConnection connection) {
    if (!isLockingCacheWriter())
      return; 
    try {
      while (doCheckLock(name, connection))
        Thread.sleep(this.sleepTime.toMillis()); 
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new PessimisticLockingFailureException(
          String.format("Interrupted while waiting to unlock cache %s", new Object[] { name }), ex);
    } 
  }
  
  private static boolean shouldExpireWithin(@Nullable Duration ttl) {
    return (ttl != null && !ttl.isZero() && !ttl.isNegative());
  }
  
  static byte[] createCacheLockKey(String name) {
    return (name + "~lock").getBytes(StandardCharsets.UTF_8);
  }
  
  private static void cacheCounterMeterRegistry(List<String> mapNamesList, String counterName, String tag, String tagValue, String description) {
    if (redisPropertyManager != null && (!redisPropertyManager.isCacheMetricsDisabled() || 
      Boolean.valueOf(redisPropertyManager.isCacheMetricsDisabled()) == null)) {
      if (!mapNamesList.isEmpty() && !mapNamesList.contains(tagValue)) {
        Counter.builder("idp_cache_metrics_" + counterName).tags(new String[] { tag, tagValue }).description(description).register(meterRegistry);
        mapNamesList.add(tagValue);
      } 
      if (null != meterRegistry) {
        Counter cacheCounter = meterRegistry.counter("idp_cache_metrics_" + counterName, new String[] { tag, tagValue });
        cacheCounter.increment();
      } 
    } 
  }
  
  private static void cacheTimerMeterRegistry(List<String> mapNamesList, String counterName, String tag, String tagValue, String description, Long timeTaken) {
    if (redisPropertyManager != null && (!redisPropertyManager.isCacheMetricsDisabled() || 
      Boolean.valueOf(redisPropertyManager.isCacheMetricsDisabled()) == null)) {
      if (!mapNamesList.isEmpty() && !mapNamesList.contains(tagValue)) {
        Timer.builder("idp_cache_metrics_" + counterName).tags(new String[] { tag, tagValue }).description(description).register(meterRegistry);
        mapNamesList.add(tagValue);
      } 
      if (null != meterRegistry) {
        Timer cacheTimer = meterRegistry.timer("idp_cache_metrics_" + counterName, new String[] { tag, tagValue });
        cacheTimer.record(timeTaken.longValue(), TimeUnit.MILLISECONDS);
      } 
    } 
  }
  
  public CacheStatistics getCacheStatistics(String cacheName) {
    return null;
  }
  
  public void clearStatistics(String name) {}
  
  public RedisCacheWriter withStatisticsCollector(CacheStatisticsCollector cacheStatisticsCollector) {
    return null;
  }
}
