package com.dtv.dcp.epoch.common.httpclient.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties(prefix = "apiclient")
public class RestClientConfig {
  public static final String OPENTRACING_TRACE_ID = "open-trace-id";
  
  private static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 25;
  
  private static final int DEFAULT_MAX_CONNECTIONS_TOTAL = 50;
  
  private static final int DEFAULT_CONNECT_TIMEOUT = 10000;
  
  private static final int DEFAULT_READ_TIMEOUT = 30000;
  
  private static final String DEFAULT_PROXY_HOST = "pxyapp.proxy.att.com";
  
  private static final int DEFAULT_PROXY_PORT = 8080;
  
  private static final String DEFAULT_KEYSTORE_PATH = System.getProperty("javax.net.ssl.keyStore");
  
  private static final String DEFAULT_KEYSTORE_PASSWORD = System.getProperty("javax.net.ssl.keyStorePassword");
  
  private static final String DEFAULT_KEYSTORE_PROTOCOL = System.getProperty("https.protocols");
  
  private Map<String, RestApiProperties> rest;
  
  public RestApiProperties getProperties(String api) {
    if (api == null)
      throw new IllegalArgumentException("api can not be null"); 
    RestApiProperties apiProps = null;
    RestApiProperties defaultProps = null;
    if (this.rest != null) {
      apiProps = this.rest.get(api);
      defaultProps = this.rest.get("default");
    } 
    if (apiProps == null && defaultProps == null)
      return null; 
    if (defaultProps == null)
      return apiProps; 
    apiProps = (apiProps != null) ? apiProps : new RestApiProperties();
    apiProps.setUsername((apiProps.getUsername() == null) ? defaultProps.getUsername() : apiProps.getUsername());
    apiProps.setPassword((apiProps.getPassword() == null) ? defaultProps.getPassword() : apiProps.getPassword());
    apiProps.setConnectTimeout((apiProps.getConnectTimeout() == 0) ? (
        (defaultProps.getConnectTimeout() == 0) ? 10000 : defaultProps.getConnectTimeout()) : 
        apiProps.getConnectTimeout());
    apiProps.setReadTimeout((apiProps.getReadTimeout() == 0) ? (
        (defaultProps.getReadTimeout() == 0) ? 30000 : defaultProps.getReadTimeout()) : 
        apiProps.getReadTimeout());
		/*
		 * defaultProps.setLogLevel((defaultProps.getLogLevel() != null) ?
		 * defaultProps.getLogLevel() : LogLevel.DEBUG);
		 * apiProps.setLogLevel((apiProps.getLogLevel() == null) ?
		 * defaultProps.getLogLevel() : apiProps.getLogLevel());
		 */
    Map<String, String> defaultHeaders = apiProps.getDefaultHeaders();
    for (String key : defaultProps.getDefaultHeaders().keySet())
      defaultHeaders.putIfAbsent(key, defaultProps.getDefaultHeaders().get(key)); 
    Map<String, UserType> apiRoles = apiProps.getUserTypes();
    for (String key : defaultProps.getUserTypes().keySet())
      apiRoles.putIfAbsent(key, defaultProps.getUserTypes().get(key)); 
    apiProps.setExcludeHeaders((apiProps.getExcludeHeaders() == null) ? defaultProps.getExcludeHeaders() : 
        apiProps.getExcludeHeaders());
    apiProps.setExcludeHeaderRegex((apiProps.getExcludeHeaderRegex() == null) ? defaultProps.getExcludeHeaderRegex() : 
        apiProps.getExcludeHeaderRegex());
    apiProps.setMaxConnectionsPerRoute(
        (apiProps.getMaxConnectionsPerRoute() == 0) ? (
        (defaultProps.getMaxConnectionsPerRoute() == 0) ? 25 : 
        defaultProps.getMaxConnectionsPerRoute()) : 
        apiProps.getMaxConnectionsPerRoute());
    apiProps.setMaxConnectionsTotal(
        (apiProps.getMaxConnectionsTotal() == 0) ? (
        (defaultProps.getMaxConnectionsTotal() == 0) ? 50 : 
        defaultProps.getMaxConnectionsTotal()) : 
        apiProps.getMaxConnectionsTotal());
    apiProps.setProxyRequired((apiProps.isProxyRequired() == null) ? (
    	(defaultProps.isProxyRequired() == null) ? "disabled" : defaultProps.isProxyRequired()) : 
        apiProps.isProxyRequired());
    apiProps.setProxyHost((apiProps.getProxyHost() == null || apiProps.getProxyHost().isEmpty()) ? (
        (defaultProps.getProxyHost() == null || defaultProps.getProxyHost().isEmpty()) ? "pxyapp.proxy.att.com" : 
        defaultProps.getProxyHost()) : 
        apiProps.getProxyHost());
    apiProps.setProxyPort((apiProps.getProxyPort() == 0) ? (
        (defaultProps.getProxyPort() == 0) ? 8080 : defaultProps.getProxyPort()) : 
        apiProps.getProxyPort());
    apiProps.setKeyStorePath((apiProps.getKeyStorePath() == null) ? (
        (defaultProps.getKeyStorePath() == null) ? DEFAULT_KEYSTORE_PATH : defaultProps.getKeyStorePath()) : 
        apiProps.getKeyStorePath());
    apiProps.setKeyStorePassword((apiProps.getKeyStorePassword() == null) ? (
        (defaultProps.getKeyStorePassword() == null) ? DEFAULT_KEYSTORE_PASSWORD : 
        defaultProps.getKeyStorePassword()) : 
        apiProps.getKeyStorePassword());
    apiProps.setKeyStoreProtocol((apiProps.getKeyStoreProtocol() == null) ? (
        (defaultProps.getKeyStoreProtocol() == null) ? DEFAULT_KEYSTORE_PROTOCOL : 
        defaultProps.getKeyStoreProtocol()) : 
        apiProps.getKeyStoreProtocol());
    apiProps.setKeyStoreEnabled(Boolean.valueOf((apiProps.isKeyStoreEnabled() == null) ? (
          (defaultProps.isKeyStoreEnabled() == null) ? false : defaultProps.isKeyStoreEnabled().booleanValue()) : 
          apiProps.isKeyStoreEnabled().booleanValue()));
    return apiProps;
  }
  
  public Map<String, RestApiProperties> getRest() {
    return this.rest;
  }
  
  public void setRest(Map<String, RestApiProperties> rest) {
    this.rest = rest;
  }
}
