package com.dtv.dcp.epoch.common.logging.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;


import ch.qos.logback.classic.LoggerContext;


public class RestApiClientLogger {
  private Logger log = null;
  
  private static final String HTTP_STATUS_FAILURE = "rac-http-status-failure";
  
  public void logRequest(HttpRequest request, byte[] requestBody, Map<String, Object> urlTemplateMap) {
    long logStartTime = System.nanoTime();
    Map<String, Object> logMap = new HashMap<>();
    try {
      logRequestInfo(logMap, request, urlTemplateMap);
      logRequestHeaders(logMap, request);
      logRequestPayload(logMap, requestBody);
    } catch (Exception ex) {
      this.log.error("Exception occurred while logging the Rest API Client - Request logging", ex);
    } finally {
      log("racReq", logMap);
      long logEndTime = System.nanoTime();
      if (this.log.isTraceEnabled())
        this.log.trace("Rest API Client - Request logging overhead - {} nanoseconds", Long.valueOf(logEndTime - logStartTime)); 
    } 
  }
  
  public void logResponse(HttpRequest request, byte[] requestBody, ClientHttpResponse response, long duration, Map<String, Object> urlTemplateMap) {
    long logStartTime = System.nanoTime();
    Map<String, Object> logMap = new HashMap<>();
    try {
      logRequestInfo(logMap, request, urlTemplateMap);
      logResponseInfo(logMap, response);
      logResponseHeaders(logMap, response);
      if (isRequestFailed(response)) {
        logRequestHeaders(logMap, request);
        logRequestPayload(logMap, requestBody);
      } 
      logResponsePayload(logMap, response);
      if (duration > 0L)
        logMap.put("duration", Long.valueOf(duration)); 
    } catch (Exception ex) {
      this.log.error("Exception occurred while logging the Rest API Client - Response logging", ex);
    } finally {
      log("racRes", logMap);
      long logEndTime = System.nanoTime();
      if (this.log.isTraceEnabled())
        this.log.trace("Rest API Client - Response logging overhead - {} nanoseconds", Long.valueOf(logEndTime - logStartTime)); 
    } 
  }
  
  private void logRequestInfo(Map<String, Object> logMap, HttpRequest request, Map<String, Object> urlTemplateMap) {
    logMap.put("method", request.getMethodValue());
    logMap.put("uri", request.getURI().toASCIIString());
    if (!CollectionUtils.isEmpty(urlTemplateMap)) {
      logMap.put("uriTemplate", urlTemplateMap.get("url"));
      if (!ObjectUtils.isEmpty(urlTemplateMap.get("params")))
        logMap.put("uriParams", urlTemplateMap.get("params")); 
    } 
  }
  
  private void logResponseInfo(Map<String, Object> logMap, ClientHttpResponse response) throws IOException {
    logMap.put("statusCode", Integer.valueOf(response.getStatusCode().value()));
    logMap.put("rac-http-status-failure", Boolean.valueOf(isRequestFailed(response)));
  }
  
  private void logRequestHeaders(Map<String, Object> logMap, HttpRequest request) {
    logHeaders("reqHeaders", logMap, request.getHeaders());
  }
  
  private void logResponseHeaders(Map<String, Object> logMap, ClientHttpResponse response) {
    logHeaders("resHeaders", logMap, response.getHeaders());
  }
  
  private void logHeaders(String headerType, Map<String, Object> logMap, HttpHeaders headers) {
    Map<String, String> sortedHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    headers.forEach((key, value) -> sortedHeaders.put(key.toLowerCase(), String.join(",", value)));
    //logMap.put(headerType, LoggerUtil.maskAuthorizationHeader(sortedHeaders));
  }
  
  private void logRequestPayload(Map<String, Object> logMap, byte[] requestBody) {
    if (requestBody != null && requestBody.length > 0)
      logMap.put("reqPayload", new String(requestBody)); 
  }
  
  private void logResponsePayload(Map<String, Object> logMap, ClientHttpResponse response) throws IOException {
    if (response.getBody() != null)
      logMap.put("resPayload", inputStreamToString(response.getBody())); 
  }
  
  private String inputStreamToString(InputStream inputStream) throws IOException {
    StringBuilder builder = new StringBuilder();
    BufferedReader bufferedReader = null;
    try {
      bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
      String line = bufferedReader.readLine();
      while (line != null) {
        builder.append(line);
        line = bufferedReader.readLine();
      } 
    } finally {
      if (bufferedReader != null)
        bufferedReader.close(); 
    } 
    return builder.toString();
  }
  
  private boolean isRequestFailed(ClientHttpResponse response) throws IOException {
    return (response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError());
  }
  
  private void log(String logType, Map<String, Object> logMap) {
    Map<String, Object> logMapTBL = new HashMap<>(logMap);
    if ("racReq".equals(logType)) {
      if (!this.log.isDebugEnabled())
        if (this.log.isInfoEnabled()) {
          logMapTBL.keySet().removeAll(Arrays.asList((Object[])new String[] { "reqPayload" }));
        } else if (this.log.isErrorEnabled()) {
          return;
        }  
    } else if ("racRes".equals(logType)) {
      Object statusObj = logMapTBL.get("rac-http-status-failure");
      if (statusObj != null && ((Boolean)statusObj).booleanValue()) {
        logMapTBL.keySet().remove("rac-http-status-failure");
        //this.log.error(SpiMarker.getInstance(), logType, StructuredArguments.entries(logMapTBL));
        return;
      } 
      if (!this.log.isDebugEnabled())
        if (this.log.isInfoEnabled()) {
          logMapTBL.keySet().removeAll(Arrays.asList((Object[])new String[] { "resPayload" }));
        } else if (this.log.isErrorEnabled()) {
          return;
        }  
      logMapTBL.keySet().remove("rac-http-status-failure");
    } 
    if (this.log.isDebugEnabled()) {
     // this.log.debug(SpiMarker.getInstance(), logType, StructuredArguments.entries(logMapTBL));
    } else if (this.log.isInfoEnabled()) {
      //this.log.info(SpiMarker.getInstance(), logType, StructuredArguments.entries(logMapTBL));
    } else if (this.log.isErrorEnabled()) {
      //this.log.error(SpiMarker.getInstance(), logType, StructuredArguments.entries(logMapTBL));
    } 
  }
  
  public RestApiClientLogger(String name) {
    String loggerName = "com.att.idp.logging." + RestApiClientLogger.class.getSimpleName() + "." + name + "." + hashCode();
    this.log = LoggerFactory.getLogger(loggerName);
    Logger logger = ((LoggerContext)LoggerFactory.getILoggerFactory()).getLogger(loggerName);
    this.log.info("Setting the [LogLevel]: {} to the [Logger]: {}", logger.getName(), name);
  }
}
