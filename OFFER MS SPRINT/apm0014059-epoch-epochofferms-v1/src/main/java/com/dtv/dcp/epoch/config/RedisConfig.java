package com.dtv.dcp.epoch.config;


import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.retry.annotation.EnableRetry;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;

import io.micrometer.core.instrument.MeterRegistry;

@Configuration
@EnableCaching
@ConditionalOnClass({ RedisOperations.class })
@AutoConfigureBefore({ RedisAutoConfiguration.class })
@EnableRetry
public class RedisConfig {

	@Value("true")
	private boolean cluster;

	@Autowired
	private RedisPropertyManager redisPropertyManager;
	
	  
  @Autowired
  private MeterRegistry meterRegistry;
  
  /** The log. */
  private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

	@Bean
	public RedisConnectionFactory connectionFactory() {
		String msArtifactId = "EPOCHOfferMs";
		log.info("inside connection Factory");
		if (this.cluster) {
			log.info("inside cluster true");
			GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
			genericObjectPoolConfig.setMaxTotal(this.redisPropertyManager.getJedisPoolMaxActive());
			log.info("inside cluster true max active ::"+ this.redisPropertyManager.getJedisPoolMaxActive());
			genericObjectPoolConfig.setMaxIdle(this.redisPropertyManager.getJedisPoolMaxIdle());
			genericObjectPoolConfig.setMinIdle(this.redisPropertyManager.getJedisPoolMinIdle());
			genericObjectPoolConfig.setMaxWaitMillis(this.redisPropertyManager.getJedisPoolMaxWait());
			JedisClientConfiguration jedisClientConfiguration = null;
			log.info("is SSL enabled ::"+ this.redisPropertyManager.isSslEnabled());
			
			log.info("Cluster Nodes ::"+ this.redisPropertyManager.getClusterNodes());
			
			log.info("Cluster Nodes no SSL  ::"+ this.redisPropertyManager.getClusterNodesNossl());
			RedisClusterConfiguration redisClusterConfig = new RedisClusterConfiguration(
					this.redisPropertyManager.isSslEnabled() ? this.redisPropertyManager.getClusterNodes()
							: this.redisPropertyManager.getClusterNodesNossl());
			jedisClientConfiguration = JedisClientConfiguration.builder().clientName(msArtifactId)
					.connectTimeout(Duration.ofMillis(this.redisPropertyManager.getConnectionTimeout()))
					.readTimeout(Duration.ofMillis(this.redisPropertyManager.getReadTimeout())).usePooling()
					.poolConfig(genericObjectPoolConfig).build();
			redisClusterConfig.setPassword(RedisPassword.of(this.redisPropertyManager.getPassword()));
			redisClusterConfig.setMaxRedirects(this.redisPropertyManager.getMaxRedirects());
			JedisConnectionFactory jedisConnectionFactory1 = new JedisConnectionFactory(redisClusterConfig,
					jedisClientConfiguration);
			StringSerializer.setRedisPropertyManager(this.redisPropertyManager);
			RedisGenericJacksonSerializer.setRedisPropertyManager(this.redisPropertyManager);
			RedisDistributedLock.setRedisPropertyManager(this.redisPropertyManager);
			return (RedisConnectionFactory) jedisConnectionFactory1;
		}

		GenericObjectPoolConfig jedisPoolConfig = new GenericObjectPoolConfig();
		jedisPoolConfig.setMaxTotal(this.redisPropertyManager.getJedisPoolMaxActive());
		jedisPoolConfig.setMaxIdle(this.redisPropertyManager.getJedisPoolMaxIdle());
		jedisPoolConfig.setMinIdle(this.redisPropertyManager.getJedisPoolMinIdle());
		jedisPoolConfig.setMaxWaitMillis(this.redisPropertyManager.getJedisPoolMaxWait());
		JedisClientConfiguration.JedisClientConfigurationBuilder clientConfig = JedisClientConfiguration.builder();
		clientConfig.clientName(msArtifactId);
		clientConfig.connectTimeout(Duration.ofMillis(this.redisPropertyManager.getConnectionTimeout()));
		clientConfig.readTimeout(Duration.ofMillis(this.redisPropertyManager.getReadTimeout()));
		clientConfig.usePooling().poolConfig(jedisPoolConfig);
		clientConfig.useSsl();
		log.info("Before Redis Cluster config");
		RedisClusterConfiguration redisStdConfig = new RedisClusterConfiguration();
		redisStdConfig.clusterNode(this.redisPropertyManager.getHost(),
				Integer.valueOf(this.redisPropertyManager.getPort()));
		redisStdConfig.setPassword(RedisPassword.of(this.redisPropertyManager.getCloudPassword()));
		redisStdConfig.setMaxRedirects(this.redisPropertyManager.getMaxRedirects());
		log.info("After Redis Cluster config");
		JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisStdConfig,
				clientConfig.build());
		StringSerializer.setRedisPropertyManager(this.redisPropertyManager);
		RedisGenericJacksonSerializer.setRedisPropertyManager(this.redisPropertyManager);
		RedisDistributedLock.setRedisPropertyManager(this.redisPropertyManager);
		RedisDistributedLock.setCluster(this.cluster);
		log.info("Before final Return");
		return (RedisConnectionFactory) jedisConnectionFactory;
		// return jedisConnectionFactory;
	}

	@Primary
	  @Bean
	  @ConditionalOnMissingBean({RedisCacheManager.class})
	  public RedisCacheManager getRedisCacheManager(RedisConnectionFactory connectionFactory) {
	    
	    return buildRedisCacheManager(connectionFactory, this.redisPropertyManager.getDefaultTimeToLive());
	  }
	  
	  @Bean(name = {"redisCacheManagerFirstHolder"})
	  @ConditionalOnProperty(prefix = "redis.cluster", name = {"time-to-live-firstholder"})
	  public RedisCacheManager getredisCacheManagerFirstHolder(RedisConnectionFactory connectionFactory) {
	    
	    return buildRedisCacheManager(connectionFactory, this.redisPropertyManager.getTimeToLiveFirstholder());
	  }
	  
	  @Bean(name = {"redisCacheManagerSecondHolder"})
	  @ConditionalOnProperty(prefix = "redis.cluster", name = {"time-to-live-secondholder"})
	  public RedisCacheManager getredisCacheManagerSecondHolder(RedisConnectionFactory connectionFactory) {
	   
	    return buildRedisCacheManager(connectionFactory, this.redisPropertyManager.getTimeToLiveSecondholder());
	  }
	  
	  @Bean(name = {"redisCacheManagerThirdHolder"})
	  @ConditionalOnProperty(prefix = "redis.cluster", name = {"time-to-live-thirdholder"})
	  public RedisCacheManager getredisCacheManagerThirdHolder(RedisConnectionFactory connectionFactory) {
	  
	    return buildRedisCacheManager(connectionFactory, this.redisPropertyManager.getTimeToLiveThirdholder());
	  }
	  
	  @Bean(name = {"redisCacheManagerFourthHolder"})
	  @ConditionalOnProperty(prefix = "redis.cluster", name = {"time-to-live-fourthholder"})
	  public RedisCacheManager getredisCacheManagerFourthHolder(RedisConnectionFactory connectionFactory) {
	    
	    return buildRedisCacheManager(connectionFactory, this.redisPropertyManager.getTimeToLiveFourthholder());
	  }
	  
	  @Bean(name = {"redisCacheManagerFifthHolder"})
	  @ConditionalOnProperty(prefix = "redis.cluster", name = {"time-to-live-fifthholder"})
	  public RedisCacheManager getredisCacheManagerFifthHolder(RedisConnectionFactory connectionFactory) {
	   
	    return buildRedisCacheManager(connectionFactory, this.redisPropertyManager.getTimeToLiveFifthholder());
	  }
	  
	  @Bean(name = {"redisCacheManagerSixthHolder"})
	  @ConditionalOnProperty(prefix = "redis.cluster", name = {"time-to-live-sixthholder"})
	  public RedisCacheManager getredisCacheManagerSixthHolder(RedisConnectionFactory connectionFactory) {
	   
	    return buildRedisCacheManager(connectionFactory, this.redisPropertyManager.getTimeToLiveSixthholder());
	  }
	  
	  @Bean(name = {"redisCacheManagerSeventhHolder"})
	  @ConditionalOnProperty(prefix = "redis.cluster", name = {"time-to-live-seventhholder"})
	  public RedisCacheManager getredisCacheManagerSeventhHolder(RedisConnectionFactory connectionFactory) {
	  
	    return buildRedisCacheManager(connectionFactory, this.redisPropertyManager.getTimeToLiveSeventhholder());
	  }
	  
	  @Bean(name = {"redisCacheManagerEightHolder"})
	  @ConditionalOnProperty(prefix = "redis.cluster", name = {"time-to-live-eighthholder"})
	  public RedisCacheManager getRedisCacheManagerEighthHolder(RedisConnectionFactory connectionFactory) {
	   
	    return buildRedisCacheManager(connectionFactory, this.redisPropertyManager.getTimeToLiveEighthholder());
	  }
	  
	  @Bean(name = {"redisCacheManagerNinthHolder"})
	  @ConditionalOnProperty(prefix = "redis.cluster", name = {"time-to-live-ninthholder"})
	  public RedisCacheManager getredisCacheManagerNinthHolder(RedisConnectionFactory connectionFactory) {
	   
	    return buildRedisCacheManager(connectionFactory, this.redisPropertyManager.getTimeToLiveNinthholder());
	  }
	  
	  @Bean(name = {"caffeineCacheManager"})
	  @ConditionalOnProperty(name = {"spring.cache.caffeine"}, havingValue = "true")
	  @ConditionalOnMissingBean({CaffeineCacheManager.class})
	  public CacheManager caffeineCacheManager() {
	    
	    CaffeineCacheManager cacheManager = new CaffeineCacheManager();
	    cacheManager.setCaffeine(caffeineCacheBuilder());
	    return (CacheManager)cacheManager;
	  }
	  
	  Caffeine<Object, Object> caffeineCacheBuilder() {
	    
	    return Caffeine.newBuilder()
	      .initialCapacity(Integer.parseInt(System.getProperty("caffeine.initialCapacity")))
	      .maximumSize(Long.parseLong(System.getProperty("caffeine.maximumSize")))
	      .expireAfterAccess(Long.parseLong(System.getProperty("caffeine.expirationTime")), TimeUnit.SECONDS)
	      .removalListener(new CustomRemovalListener())
	      .recordStats();
	  }
	  
	  private RedisCacheManager buildRedisCacheManager(RedisConnectionFactory connectionFactory, Long timeToLive) {
		log.info("inside build Redis cache manager");
	    StringSerializer<String> stringSerializer = new StringSerializer(String.class);
	    stringSerializer.setExceptionMappings(this.redisPropertyManager.getAuthExceptionMappings());
	    RedisGenericJacksonSerializer redisGenericJacksonSerializer = new RedisGenericJacksonSerializer();
	    RedisSerializationContext.SerializationPair<String> pairKey = RedisSerializationContext.SerializationPair.fromSerializer((RedisSerializer)stringSerializer);
	    RedisSerializationContext.SerializationPair<Object> pairValue = RedisSerializationContext.SerializationPair.fromSerializer((RedisSerializer)redisGenericJacksonSerializer);
	    RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig().serializeValuesWith(pairValue);
	    cacheConfig = cacheConfig.serializeKeysWith(pairKey);
	    if (null == timeToLive || timeToLive.longValue() == 0L)
	      cacheConfig = cacheConfig.entryTtl(Duration.ofSeconds(10800L)); 
	    cacheConfig = cacheConfig.entryTtl(Duration.ofSeconds(timeToLive.longValue()));
	    cacheConfig = cacheConfig.computePrefixWith(CacheKeyPrefix.simple());
	    log.info("inside build Redis cache manager 1");
	    RedisCacheManager redisCacheManager = RedisCacheManager.RedisCacheManagerBuilder.fromCacheWriter(getCacheWriter(connectionFactory())).cacheDefaults(cacheConfig).build();
	    log.info("inside build Redis cache manager 2");
	    
	    log.info("This.cluser value agan :::"+this.cluster);
	    log.info("connectionFactory"+connectionFactory);
	    log.info("connectionFactory 1"+connectionFactory
		          .getClusterConnection());
		/*
		 * log.info("connectionFactory 2"+connectionFactory
		 * .getClusterConnection().clusterGetClusterInfo());
		 */
	    if (this.cluster) {
			/*
			 * log.info("Redis Master Cluster Size : {} ", connectionFactory
			 * .getClusterConnection().clusterGetClusterInfo().getClusterSize());
			 * log.info("Redis Cluster State : {} ", connectionFactory
			 * .getClusterConnection().clusterGetClusterInfo().getState());
			 * log.info("Redis Cluster OK Slots : {}", connectionFactory
			 * .getClusterConnection().clusterGetClusterInfo().getSlotsOk());
			 * log.info("Redis Cluster Failed Slots : {} ", connectionFactory
			 * .getClusterConnection().clusterGetClusterInfo().getSlotsFail()); log.info(
			 * "----------------------------------------------------------------------------------------------------------"
			 * ); log.info("Redis Connection Details:", connectionFactory.getConnection());
			 */
	    } else {
	      log.info("Redis Host Name:{} ", this.redisPropertyManager.getHost());
	    } 
	    return redisCacheManager;
	  }
	  
	  @Bean
	  public RedisCacheWriter getCacheWriter(RedisConnectionFactory connectionFactory) {
	    IdpRedisCacheWriter cacheWriter = new IdpRedisCacheWriter(connectionFactory(), Duration.ofMillis(this.redisPropertyManager.getWritelockduration()));
	    IdpRedisCacheWriter.setMeterRegistry(this.meterRegistry);
	    IdpRedisCacheWriter.setRedisPropertyManager(this.redisPropertyManager);
	    return (RedisCacheWriter)cacheWriter;
	  }
	  
	  class CustomRemovalListener implements RemovalListener<Object, Object> {
	    public void onRemoval(Object key, Object value, RemovalCause cause) {
	      System.out.format("removal listener called with key [%s], cause [%s], evicted [%S]\n", new Object[] { key, cause
	            .toString(), Boolean.valueOf(cause.wasEvicted()) });
	    }
	  }
}