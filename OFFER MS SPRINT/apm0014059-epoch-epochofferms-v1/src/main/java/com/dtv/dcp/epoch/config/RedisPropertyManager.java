package com.dtv.dcp.epoch.config;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@PropertySources({@PropertySource(value = {"classpath:application.properties"}, ignoreResourceNotFound = true)})
@ConfigurationProperties(prefix = "redis.cluster", ignoreUnknownFields = true)
public class RedisPropertyManager {
  private List<String> clusterNodes;
  
  private List<String> clusterNodesNossl;
  
  private List<String> clusterNodesMultiDCSsl;
  
  private List<String> clusterNodesMultiDCNossl;
  
  private String password;
  
  private long writelockduration;
  
  private boolean isSslEnabled;
  
  private boolean isMultiDCEnabled;
  
  private boolean isMultiDCSslEnabled;
  
  private boolean isSerializerKeyLogging;
  
  private boolean isDeserializerKeyLogging;
  
  private boolean isSerializerValueLogging;
  
  private boolean isDeserializerValueLogging;
  
  private boolean isSkipValidation;
  
  private boolean isCacheMetricsDisabled;
  
  private Long defaultTimeToLive;
  
  private Long timeToLiveFirstholder;
  
  private Long timeToLiveSecondholder;
  
  private Long timeToLiveThirdholder;
  
  private Long timeToLiveFourthholder;
  
  private Long timeToLiveFifthholder;
  
  private Long timeToLiveSixthholder;
  
  private Long timeToLiveSeventhholder;
  
  private Long timeToLiveEighthholder;
  
  private Long timeToLiveNinthholder;
  
  private int connectionTimeout;
  
  private int readTimeout;
  
  private int maxRedirects;
  
  private int jedisPoolMaxActive;
  
  private int jedisPoolMaxIdle;
  
  private int jedisPoolMinIdle;
  
  private int jedisPoolMaxWait;
  
  private int connectionfactoryMaxRetryAttempts;
  
  private int connectionfactoryDelay;
  
  private int cacheOperationRetry;
  
  private int cacheOperationDelay;
  
  private Map<String, String> authExceptionMappings;
  
  private String host;
  
  private int port;
  
  private String cloudPassword;
  
  private static Logger log = LoggerFactory.getLogger(RedisPropertyManager.class);
  
  public List<String> getClusterNodes() {
    return this.clusterNodes;
  }
  
  public void setClusterNodes(List<String> clusterNodes) {
    this.clusterNodes = clusterNodes;
  }
  
  public List<String> getClusterNodesNossl() {
    return this.clusterNodesNossl;
  }
  
  public void setClusterNodesNossl(List<String> clusterNodesNossl) {
    this.clusterNodesNossl = clusterNodesNossl;
  }
  
  public List<String> getClusterNodesMultiDCSsl() {
    return this.clusterNodesMultiDCSsl;
  }
  
  public void setClusterNodesMultiDCSsl(List<String> clusterNodesMultiDCSsl) {
    this.clusterNodesMultiDCSsl = clusterNodesMultiDCSsl;
  }
  
  public List<String> getClusterNodesMultiDCNossl() {
    return this.clusterNodesMultiDCNossl;
  }
  
  public void setClusterNodesMultiDCNossl(List<String> clusterNodesMultiDCNossl) {
    this.clusterNodesMultiDCNossl = clusterNodesMultiDCNossl;
  }
  
  public String getPassword() {
    return this.password;
  }
  
  public void setPassword(String password) {
    this.password = password;
  }
  
  public long getWritelockduration() {
    return this.writelockduration;
  }
  
  public void setWritelockduration(long writelockduration) {
    this.writelockduration = writelockduration;
  }
  
  public boolean isSslEnabled() {
    return this.isSslEnabled;
  }
  
  public void setSslEnabled(boolean isSslEnabled) {
    this.isSslEnabled = isSslEnabled;
  }
  
  public boolean isMultiDCEnabled() {
    return this.isMultiDCEnabled;
  }
  
  public void setMultiDCEnabled(boolean multiDCEnabled) {
    this.isMultiDCEnabled = multiDCEnabled;
  }
  
  public boolean isMultiDCSslEnabled() {
    return this.isMultiDCSslEnabled;
  }
  
  public void setMultiDCSslEnabled(boolean multiDCSslEnabled) {
    this.isMultiDCSslEnabled = multiDCSslEnabled;
  }
  
  public boolean isSerializerKeyLogging() {
    return this.isSerializerKeyLogging;
  }
  
  public void setSerializerKeyLogging(boolean isSerializerKeyLogging) {
    this.isSerializerKeyLogging = isSerializerKeyLogging;
  }
  
  public boolean isDeserializerKeyLogging() {
    return this.isDeserializerKeyLogging;
  }
  
  public void setDeserializerKeyLogging(boolean isDeserializerKeyLogging) {
    this.isDeserializerKeyLogging = isDeserializerKeyLogging;
  }
  
  public boolean isSerializerValueLogging() {
    return this.isSerializerValueLogging;
  }
  
  public void setSerializerValueLogging(boolean isSerializerValueLogging) {
    this.isSerializerValueLogging = isSerializerValueLogging;
  }
  
  public boolean isSkipValidation() {
    return this.isSkipValidation;
  }
  
  public void setSkipValidation(boolean isSkipValidation) {
    this.isSkipValidation = isSkipValidation;
  }
  
  public boolean isCacheMetricsDisabled() {
    return this.isCacheMetricsDisabled;
  }
  
  public void setCacheMetricsDisabled(boolean isCacheMetricsDisabled) {
    this.isCacheMetricsDisabled = isCacheMetricsDisabled;
  }
  
  public boolean isDeserializerValueLogging() {
    return this.isDeserializerValueLogging;
  }
  
  public void setDeserializerValueLogging(boolean isDeserializerValueLogging) {
    this.isDeserializerValueLogging = isDeserializerValueLogging;
  }
  
  public Long getDefaultTimeToLive() {
    return this.defaultTimeToLive;
  }
  
  public void setDefaultTimeToLive(Long defaultTimeToLive) {
    this.defaultTimeToLive = defaultTimeToLive;
  }
  
  public Long getTimeToLiveFirstholder() {
    return this.timeToLiveFirstholder;
  }
  
  public void setTimeToLiveFirstholder(Long timeToLiveFirstholder) {
    this.timeToLiveFirstholder = timeToLiveFirstholder;
  }
  
  public Long getTimeToLiveSecondholder() {
    return this.timeToLiveSecondholder;
  }
  
  public void setTimeToLiveSecondholder(Long timeToLiveSecondholder) {
    this.timeToLiveSecondholder = timeToLiveSecondholder;
  }
  
  public Long getTimeToLiveThirdholder() {
    return this.timeToLiveThirdholder;
  }
  
  public void setTimeToLiveThirdholder(Long timeToLiveThirdholder) {
    this.timeToLiveThirdholder = timeToLiveThirdholder;
  }
  
  public Long getTimeToLiveFourthholder() {
    return this.timeToLiveFourthholder;
  }
  
  public void setTimeToLiveFourthholder(Long timeToLiveFourthholder) {
    this.timeToLiveFourthholder = timeToLiveFourthholder;
  }
  
  public Long getTimeToLiveFifthholder() {
    return this.timeToLiveFifthholder;
  }
  
  public void setTimeToLiveFifthholder(Long timeToLiveFifthholder) {
    this.timeToLiveFifthholder = timeToLiveFifthholder;
  }
  
  public Long getTimeToLiveSixthholder() {
    return this.timeToLiveSixthholder;
  }
  
  public void setTimeToLiveSixthholder(Long timeToLiveSixthholder) {
    this.timeToLiveSixthholder = timeToLiveSixthholder;
  }
  
  public Long getTimeToLiveSeventhholder() {
    return this.timeToLiveSeventhholder;
  }
  
  public void setTimeToLiveSeventhholder(Long timeToLiveSeventhholder) {
    this.timeToLiveSeventhholder = timeToLiveSeventhholder;
  }
  
  public Long getTimeToLiveEighthholder() {
    return this.timeToLiveEighthholder;
  }
  
  public void setTimeToLiveEighthholder(Long timeToLiveEighthholder) {
    this.timeToLiveEighthholder = timeToLiveEighthholder;
  }
  
  public Long getTimeToLiveNinthholder() {
    return this.timeToLiveNinthholder;
  }
  
  public void setTimeToLiveNinthholder(Long timeToLiveNinthholder) {
    this.timeToLiveNinthholder = timeToLiveNinthholder;
  }
  
  public int getConnectionTimeout() {
    return this.connectionTimeout;
  }
  
  public void setConnectionTimeout(int connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }
  
  public int getReadTimeout() {
    return this.readTimeout;
  }
  
  public void setReadTimeout(int readTimeout) {
    this.readTimeout = readTimeout;
  }
  
  public int getMaxRedirects() {
    return this.maxRedirects;
  }
  
  public void setMaxRedirects(int maxRedirects) {
    this.maxRedirects = maxRedirects;
  }
  
  public int getJedisPoolMaxActive() {
    return this.jedisPoolMaxActive;
  }
  
  public void setJedisPoolMaxActive(int jedisPoolMaxActive) {
    this.jedisPoolMaxActive = jedisPoolMaxActive;
  }
  
  public int getJedisPoolMaxIdle() {
    return this.jedisPoolMaxIdle;
  }
  
  public void setJedisPoolMaxIdle(int jedisPoolMaxIdle) {
    this.jedisPoolMaxIdle = jedisPoolMaxIdle;
  }
  
  public int getJedisPoolMinIdle() {
    return this.jedisPoolMinIdle;
  }
  
  public void setJedisPoolMinIdle(int jedisPoolMinIdle) {
    this.jedisPoolMinIdle = jedisPoolMinIdle;
  }
  
  public int getJedisPoolMaxWait() {
    return this.jedisPoolMaxWait;
  }
  
  public void setJedisPoolMaxWait(int jedisPoolMaxWait) {
    this.jedisPoolMaxWait = jedisPoolMaxWait;
  }
  
  public int getConnectionfactoryMaxRetryAttempts() {
    return this.connectionfactoryMaxRetryAttempts;
  }
  
  public void setConnectionfactoryMaxRetryAttempts(int connectionfactoryMaxRetryAttempts) {
    this.connectionfactoryMaxRetryAttempts = connectionfactoryMaxRetryAttempts;
  }
  
  public int getConnectionfactoryDelay() {
    return this.connectionfactoryDelay;
  }
  
  public void setConnectionfactoryDelay(int connectionfactoryDelay) {
    this.connectionfactoryDelay = connectionfactoryDelay;
  }
  
  public int getCacheOperationRetry() {
    return this.cacheOperationRetry;
  }
  
  public void setCacheOperationRetry(int cacheOperationRetry) {
    this.cacheOperationRetry = cacheOperationRetry;
  }
  
  public int getCacheOperationDelay() {
    return this.cacheOperationDelay;
  }
  
  public void setCacheOperationDelay(int cacheOperationDelay) {
    this.cacheOperationDelay = cacheOperationDelay;
  }
  
  public Map<String, String> getAuthExceptionMappings() {
    return this.authExceptionMappings;
  }
  
  public void setAuthExceptionMappings(Map<String, String> authExceptionMappings) {
    this.authExceptionMappings = authExceptionMappings;
  }
  
  public String getHost() {
    return this.host;
  }
  
  public void setHost(String host) {
    this.host = host;
  }
  
  public int getPort() {
    return this.port;
  }
  
  public void setPort(int port) {
    this.port = port;
  }
  
  public String getCloudPassword() {
    return this.cloudPassword;
  }
  
  public void setCloudPassword(String cloudPassword) {
    this.cloudPassword = cloudPassword;
  }
}
