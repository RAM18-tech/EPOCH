package com.dtv.dcp.epoch.common.httpclient.config;


import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RestApiProperties {
  private String username;
  
  private String password;
  
  private String basicAuth;
  
  private String userTypesBasicAuth;
  
  private Map<String, UserType> userTypes;
  
  private Map<String, String> defaultHeaders;

  
  private int connectTimeout;
  
  private int readTimeout;
  
  private boolean contextAsCookie;
  
  private String excludeHeaders;
  
  private String excludeHeaderRegex;
  
  private List<String> interceptors;
  
  private int maxConnectionsPerRoute;
  
  private int maxConnectionsTotal;
  
  private String proxyRequired;
  
  private String proxyHost;
  
  private int proxyPort;
  
  private String keyStorePath;
  
  private String keyStorePassword;
  
  private String keyStoreProtocol;
  
  private Boolean keyStoreEnabled;
  
  public String isProxyRequired() {
    return this.proxyRequired;
  }
  
  public void setProxyRequired(String proxyRequired) {
    this.proxyRequired = proxyRequired;
  }
  
  public String getProxyHost() {
    return this.proxyHost;
  }
  
  public void setProxyHost(String proxyHost) {
    this.proxyHost = proxyHost;
  }
  
  public int getProxyPort() {
    return this.proxyPort;
  }
  
  public void setProxyPort(int proxyPort) {
    this.proxyPort = proxyPort;
  }
  
  public RestApiProperties() {
    this.defaultHeaders = new HashMap<>();
    this.userTypes = new HashMap<>();
  }
  

  
  public String getUsername() {
    return this.username;
  }
  
  public void setUsername(String username) {
    this.username = username;
  }
  
  public String getPassword() {
    return this.password;
  }
  
  public void setPassword(String password) {
    this.password = password;
  }
  
  public String getBasicAuth() {
    return this.basicAuth;
  }
  
  public void setBasicAuth(String basicAuth) {
    this.basicAuth = basicAuth;
  }
  
  public String getUserTypesBasicAuth() {
    return this.userTypesBasicAuth;
  }
  
  public void setUserTypesBasicAuth(String userTypesBasicAuth) {
    this.userTypesBasicAuth = userTypesBasicAuth;
  }
  
  public Map<String, String> getDefaultHeaders() {
    return this.defaultHeaders;
  }
  
  public Map<String, UserType> getUserTypes() {
    return this.userTypes;
  }
  
  public int getConnectTimeout() {
    return this.connectTimeout;
  }
  
  public void setConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
  }
  
  public int getReadTimeout() {
    return this.readTimeout;
  }
  
  public void setReadTimeout(int readTimeout) {
    this.readTimeout = readTimeout;
  }
  
  public boolean isContextAsCookie() {
    return this.contextAsCookie;
  }
  
  public void setContextAsCookie(boolean contextAsCookie) {
    this.contextAsCookie = contextAsCookie;
  }
  
  public String getExcludeHeaders() {
    return this.excludeHeaders;
  }
  
  public String getExcludeHeaderRegex() {
    return this.excludeHeaderRegex;
  }
  
  public void setExcludeHeaderRegex(String excludeHeaderRegex) {
    this.excludeHeaderRegex = excludeHeaderRegex;
  }
  
  public void setExcludeHeaders(String excludeHeaders) {
    this.excludeHeaders = excludeHeaders;
  }
  
  public List<String> getInterceptors() {
    return this.interceptors;
  }
  
  public void setInterceptors(List<String> interceptors) {
    this.interceptors = interceptors;
  }
  
  public int getMaxConnectionsPerRoute() {
    return this.maxConnectionsPerRoute;
  }
  
  public void setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
    this.maxConnectionsPerRoute = maxConnectionsPerRoute;
  }
  
  public int getMaxConnectionsTotal() {
    return this.maxConnectionsTotal;
  }
  
  public void setMaxConnectionsTotal(int maxConnectionsTotal) {
    this.maxConnectionsTotal = maxConnectionsTotal;
  }
  
  public String getKeyStorePath() {
    return this.keyStorePath;
  }
  
  public void setKeyStorePath(String keyStorePath) {
    this.keyStorePath = keyStorePath;
  }
  
  public String getKeyStorePassword() {
    return this.keyStorePassword;
  }
  
  public void setKeyStorePassword(String keyStorePassword) {
    this.keyStorePassword = keyStorePassword;
  }
  
  public String getKeyStoreProtocol() {
    return this.keyStoreProtocol;
  }
  
  public void setKeyStoreProtocol(String keyStoreProtocol) {
    this.keyStoreProtocol = keyStoreProtocol;
  }
  
  public Boolean isKeyStoreEnabled() {
    return this.keyStoreEnabled;
  }
  
  public void setKeyStoreEnabled(Boolean keyStoreEnabled) {
    this.keyStoreEnabled = keyStoreEnabled;
  }
}
 
 