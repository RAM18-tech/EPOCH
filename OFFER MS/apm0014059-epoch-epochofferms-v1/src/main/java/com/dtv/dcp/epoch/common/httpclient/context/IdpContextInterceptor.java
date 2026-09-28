package com.dtv.dcp.epoch.common.httpclient.context;


import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.AsyncClientHttpRequestExecution;
import org.springframework.http.client.AsyncClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

public class IdpContextInterceptor implements ClientHttpRequestInterceptor, AsyncClientHttpRequestInterceptor {
  private static final Logger log = LoggerFactory.getLogger(IdpContextInterceptor.class);
  
  private static final String idpCookie = "idpctx-cookie";
  
  private static final String idpSetCookie = "idpctx-set-cookie";
  
  public static final String contextAsCookie = "contextAsCookie";
  
  private static final String setCookie = "Set-Cookie";
  
  private Properties apiProps;
  
  public IdpContextInterceptor(Properties apiProps) {
    this.apiProps = apiProps;
  }
  
  public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    boolean isContextAsCookie = Boolean.parseBoolean(this.apiProps.getProperty("contextAsCookie", "false"));
    processRequest(request, isContextAsCookie);
    ClientHttpResponse response = null;
    try {
      response = execution.execute(request, body);
      processResponse(response, isContextAsCookie);
    } catch (Exception e) {
      if (response != null)
        response.close(); 
      throw e;
    } 
    return response;
  }
  
  public ListenableFuture<ClientHttpResponse> intercept(final HttpRequest request, byte[] body, AsyncClientHttpRequestExecution execution) throws IOException {
    final boolean isContextAsCookie = Boolean.parseBoolean(this.apiProps.getProperty("contextAsCookie", "false"));
    processRequest(request, isContextAsCookie);
    ListenableFuture<ClientHttpResponse> future = null;
    try {
      future = execution.executeAsync(request, body);
      future.addCallback(new ListenableFutureCallback<ClientHttpResponse>() {
            public void onSuccess(ClientHttpResponse response) {
              IdpContextInterceptor.processResponse(response, isContextAsCookie);
            }
            
            public void onFailure(Throwable ex) {
              IdpContextInterceptor.log.error("Async call failed for" + request.getMethod() + " " + request.getURI(), ex);
            }
          });
    } catch (Exception e) {
      if (future != null)
        ((ClientHttpResponse)future).close(); 
      throw e;
    } 
    return future;
  }
  
  private static void processRequest(HttpRequest request, boolean contextAsCookie) {
    if (!contextAsCookie)
      return; 
    List<String> cookies = request.getHeaders().get("idpctx-cookie");
    if (cookies != null) {
      request.getHeaders().remove("idpctx-cookie");
      for (String cookie : cookies)
        request.getHeaders().set("Cookie", cookie); 
    } 
  }
  
  private static void processResponse(ClientHttpResponse response, boolean contextAsCookie) {
    List<String> setCookies;
    if (contextAsCookie) {
      setCookies = response.getHeaders().get("Set-Cookie");
    } else {
      setCookies = response.getHeaders().get("idpctx-set-cookie");
    } 
    if (setCookies != null)
      for (String setCookie : setCookies)
        Context.add("idpctx-set-cookie", setCookie);  
  }
}
