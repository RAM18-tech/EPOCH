package com.dtv.dcp.epoch.config;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.params.SetParams;

public class RedisDistributedLock {
  private static final Logger log = LoggerFactory.getLogger(RedisDistributedLock.class);
  
  private static final String LOCK_SUCCESS = "OK";
  
  private static final Long RELEASE_SUCCESS = Long.valueOf(1L);
  
  private static RedisPropertyManager redisPropertyManager;
  
  JedisCluster jedisCluster = null;
  
  Jedis jedis = null;
  
  private static boolean cluster;
  
  public static void setCluster(boolean cluster) {
    RedisDistributedLock.cluster = cluster;
  }
  
  public static void setRedisPropertyManager(RedisPropertyManager redisPropertyManager) {
    RedisDistributedLock.redisPropertyManager = redisPropertyManager;
  }
  
  public boolean tryGetDistributedLock(String lockKey, String requestId, long expireTime) {
    log.info("Redis lock LockKey ::= " + lockKey);
    log.info("Redis lock RequestID ::= " + requestId);
    SetParams setParams = SetParams.setParams();
    setParams.nx().ex((int)expireTime);
    if (cluster) {
      log.info("------------------------------Inside Cluster Call----------------------------");
      JedisCluster jedisCluster = createJedisCluster();
      String str = jedisCluster.set(lockKey, requestId, setParams);
      if ("OK".equals(str)) {
        log.debug("Lock acquired for lockKey = " + lockKey);
        return true;
      } 
      log.debug("Lock failed to acquire for lockKey = " + lockKey);
      return false;
    } 
    log.info("------------------------------Inside Standalone Call----------------------------");
    JedisCluster jedisClusterShards = createJedisClusterShards();
    String result = jedisClusterShards.set(lockKey, requestId, setParams);
    if ("OK".equals(result)) {
      log.debug("Lock acquired for lockKey = " + lockKey);
      return true;
    } 
    log.debug("Lock failed to acquire for lockKey = " + lockKey);
    return false;
  }
  
  public boolean releaseDistributedLock(String lockKey, String requestId) {
    if (cluster) {
      log.info("------------------------------Inside Cluster Call----------------------------");
      JedisCluster jedisCluster = createJedisCluster();
      String str = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
      Object object = jedisCluster.eval(str, Collections.singletonList(lockKey), Collections.singletonList(requestId));
      if (RELEASE_SUCCESS.equals(object)) {
        log.debug("Lock released for lockKey = " + lockKey);
        return true;
      } 
      log.debug("Lock failed to release for lockKey = " + lockKey);
      return false;
    } 
    log.info("------------------------------Inside Standalone Call----------------------------");
    Jedis jedis = createJedisConnection();
    String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
    Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));
    if (RELEASE_SUCCESS.equals(result)) {
      log.debug("Lock released for lockKey = " + lockKey);
      return true;
    } 
    log.debug("Lock failed to release for lockKey = " + lockKey);
    return false;
  }
  
  private JedisCluster createJedisCluster() {
    log.debug("Redis getConnectionTimeout = " + redisPropertyManager.getConnectionTimeout());
    log.debug("Redis getReadTimeout = " + redisPropertyManager.getReadTimeout());
    log.debug("Redis getConnectionTimeout = " + redisPropertyManager.getConnectionfactoryMaxRetryAttempts());
    if (isNullOrBlank(redisPropertyManager.getPassword()))
      redisPropertyManager.setPassword(null); 
    if (this.jedisCluster == null)
      this
        
        .jedisCluster = new JedisCluster(getHostAndPortSet(), redisPropertyManager.getConnectionTimeout(), redisPropertyManager.getReadTimeout(), redisPropertyManager.getConnectionfactoryMaxRetryAttempts(), redisPropertyManager.getPassword(), buildPoolConfig()); 
    return this.jedisCluster;
  }
  
  private Jedis createJedisConnection() {
    log.info("Redis connected to host :" + redisPropertyManager.getHost());
    log.debug("Redis getConnectionTimeout = " + redisPropertyManager.getConnectionTimeout());
    log.debug("Redis getReadTimeout = " + redisPropertyManager.getReadTimeout());
    log.debug("Redis getConnectionTimeout = " + redisPropertyManager.getConnectionfactoryMaxRetryAttempts());
    if (isNullOrBlank(redisPropertyManager.getCloudPassword()))
      redisPropertyManager.setCloudPassword(null); 
    if (this.jedis == null) {
      this
        .jedis = new Jedis(redisPropertyManager.getHost(), redisPropertyManager.getPort(), redisPropertyManager.getConnectionTimeout(), redisPropertyManager.getReadTimeout(), true);
      this.jedis.auth(redisPropertyManager.getCloudPassword());
    } 
    return this.jedis;
  }
  
  private JedisCluster createJedisClusterShards() {
    log.debug("Redis getConnectionTimeout = " + redisPropertyManager.getConnectionTimeout());
    log.debug("Redis getReadTimeout = " + redisPropertyManager.getReadTimeout());
    log.debug("Redis getConnectionTimeout = " + redisPropertyManager.getConnectionfactoryMaxRetryAttempts());
    String msArtifactId = System.getProperty("info.build.artifact");
    Set<HostAndPort> jedisClusterNode = new HashSet<>();
    jedisClusterNode.add(new HostAndPort(redisPropertyManager.getHost(), redisPropertyManager.getPort()));
    if (this.jedisCluster == null)
      this
        
        .jedisCluster = new JedisCluster(jedisClusterNode, redisPropertyManager.getConnectionTimeout(), redisPropertyManager.getReadTimeout(), redisPropertyManager.getConnectionfactoryMaxRetryAttempts(), redisPropertyManager.getCloudPassword(), msArtifactId, buildPoolConfig(), true); 
    return this.jedisCluster;
  }
  
  private GenericObjectPoolConfig buildPoolConfig() {
    GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
    poolConfig.setMaxTotal(redisPropertyManager.getJedisPoolMaxActive());
    poolConfig.setMaxIdle(redisPropertyManager.getJedisPoolMaxIdle());
    poolConfig.setMinIdle(redisPropertyManager.getJedisPoolMinIdle());
    poolConfig.setMaxWaitMillis(redisPropertyManager.getJedisPoolMaxWait());
    JedisClientConfiguration.JedisClientConfigurationBuilder clientConfig = JedisClientConfiguration.builder();
    clientConfig.connectTimeout(Duration.ofMillis(redisPropertyManager.getConnectionTimeout()));
    clientConfig.readTimeout(Duration.ofMillis(redisPropertyManager.getReadTimeout()));
    clientConfig.usePooling().poolConfig(poolConfig);
    clientConfig.useSsl();
    return poolConfig;
  }
  
  private Set<HostAndPort> getHostAndPortSet() {
    List<String> clusterList;
    if (redisPropertyManager.isSslEnabled()) {
      clusterList = redisPropertyManager.getClusterNodes();
    } else {
      clusterList = redisPropertyManager.getClusterNodesNossl();
    } 
    Set<HostAndPort> set = new HashSet<>();
    for (String sCluster : clusterList) {
      String[] clusterValue = sCluster.split(":");
      set.add(new HostAndPort(clusterValue[0], (new Integer(clusterValue[1])).intValue()));
    } 
    return set;
  }
  
  public static boolean isNullOrBlank(String param) {
    return (param == null || param.trim().length() == 0);
  }
}
