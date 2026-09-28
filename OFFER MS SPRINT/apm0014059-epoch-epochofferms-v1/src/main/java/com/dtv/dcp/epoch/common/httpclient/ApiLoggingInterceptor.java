package com.dtv.dcp.epoch.common.httpclient;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.NamedThreadLocal;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.AsyncClientHttpRequestExecution;
import org.springframework.http.client.AsyncClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplateHandler;

import com.dtv.dcp.epoch.common.logging.client.RestApiClientLogger;

import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;

public class ApiLoggingInterceptor implements ClientHttpRequestInterceptor, AsyncClientHttpRequestInterceptor {
  private static final ThreadLocal<Map<String, Object>> urlTemplate = (ThreadLocal<Map<String, Object>>)new NamedThreadLocal("Rest Template URL Template");
  
  private static final Logger logger = LoggerFactory.getLogger(ApiLoggingInterceptor.class);
  
  private RestApiClientLogger restApiClientLogger;
  
  public ApiLoggingInterceptor(RestApiClientLogger restApiClientLogger) {
    this.restApiClientLogger = restApiClientLogger;
  }
  
  public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    Map<String, Object> urltemp = urlTemplate.get();
    ClientHttpResponse response = null;
    String currentTraceId = MDC.get("open-trace-id");
    try {
      Span span = GlobalTracer.get().activeSpan();
      if (span != null)
        MDC.put("open-trace-id", GlobalTracer.get().activeSpan().toString()); 
      if (null == urltemp)
        urltemp = createUriMap(request.getURI().toString(), null, new Object[0]); 
      this.restApiClientLogger.logRequest(request, body, urltemp);
      long startTime = System.currentTimeMillis();
      response = execution.execute(request, body);
      long endTime = System.currentTimeMillis();
      this.restApiClientLogger.logResponse(request, body, response, endTime - startTime, urltemp);
    } catch (Exception e) {
      if (response != null)
        response.close(); 
      throw e;
    } finally {
      MDC.put("open-trace-id", currentTraceId);
      urlTemplate.remove();
    } 
    return response;
  }
  
  public UriTemplateHandler createUriTemplateHandler(final UriTemplateHandler delegate) {
    return new UriTemplateHandler() {
        public URI expand(String url, Map<String, ?> arguments) {
          ApiLoggingInterceptor.urlTemplate.set(ApiLoggingInterceptor.this.createUriMap(url, arguments, new Object[0]));
          return delegate.expand(url, arguments);
        }
        
        public URI expand(String url, Object... arguments) {
          ApiLoggingInterceptor.urlTemplate.set(ApiLoggingInterceptor.this.createUriMap(url, null, arguments));
          return delegate.expand(url, arguments);
        }
      };
  }
  
  private Map<String, Object> createUriMap(String url, Map<String, ?> arguments, Object... varArguments) {
    Map<String, Object> urlTemplateMap = new HashMap<>();
    UriComponents uri = UriComponentsBuilder.fromUriString(url).replaceQuery(null).build();
    if (null != uri)
      urlTemplateMap.put("url", uri.getPath()); 
    if (arguments != null) {
      urlTemplateMap.put("params", arguments);
    } else if (varArguments != null) {
      urlTemplateMap.put("params", varArguments);
    } else {
      Map<String, Object> tempMap = new HashMap<>();
      UriComponents uriParams = UriComponentsBuilder.fromUriString(url).build();
      uriParams.getQueryParams().forEach((a, b) -> tempMap.put(a, b.get(0)));
      urlTemplateMap.put("params", tempMap);
    } 
    return urlTemplateMap;
  }
  
  public ListenableFuture<ClientHttpResponse> intercept(final HttpRequest request, final byte[] body, AsyncClientHttpRequestExecution execution) throws IOException {
    ListenableFuture<ClientHttpResponse> response = null;
    Map<String, Object> urltemp = urlTemplate.get();
    String parentTraceId = MDC.get("open-trace-id");
    if (null == urltemp)
      urltemp = createUriMap(request.getURI().toString(), null, new Object[0]); 
    String tempTraceId = null;
    Span span = GlobalTracer.get().activeSpan();
    if (span != null) {
      tempTraceId = GlobalTracer.get().activeSpan().toString();
      try {
        MDC.put("open-trace-id", tempTraceId);
        this.restApiClientLogger.logRequest(request, body, urltemp);
      } finally {
        MDC.put("open-trace-id", parentTraceId);
      } 
    } 
    final String currentTraceId = tempTraceId;
    final long startTime = System.currentTimeMillis();
    try {
      response = execution.executeAsync(request, body);
      final Map<String, Object> urlMap = urltemp;
      response.addCallback(new ListenableFutureCallback<ClientHttpResponse>() {
            public void onSuccess(ClientHttpResponse response) {
              try {
                long endTime = System.currentTimeMillis();
                MDC.put("open-trace-id", currentTraceId);
                ApiLoggingInterceptor.this.restApiClientLogger.logResponse(request, body, response, endTime - startTime, urlMap);
              } finally {
                MDC.remove("open-trace-id");
                ApiLoggingInterceptor.urlTemplate.remove();
              } 
            }
            
            public void onFailure(Throwable t) {
              ApiLoggingInterceptor.logger.error("Error occurred in invoking REST api " + request.getMethod() + " " + request.getURI(), t);
            }
          });
    } catch (Exception e) {
      if (response != null)
        ((ClientHttpResponse)response).close(); 
      throw e;
    } 
    return response;
  }
}
