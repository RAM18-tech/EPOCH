package com.dtv.dcp.epoch.common.httpclient;

import java.io.IOException;
import java.nio.charset.Charset;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.AsyncClientHttpRequestExecution;
import org.springframework.http.client.AsyncClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Base64Utils;
import org.springframework.util.concurrent.ListenableFuture;

import com.dtv.dcp.epoch.common.httpclient.config.RestApiProperties;
import com.dtv.dcp.epoch.common.httpclient.config.UserType;



public class DynamicBasicAuthInterceptor implements ClientHttpRequestInterceptor, AsyncClientHttpRequestInterceptor {
  private static final String userTypeHeaderKey = "idpctx-user-type";
  
  private static final Charset UTF_8 = Charset.forName("UTF-8");
  
  private RestApiProperties apiProps;
  
  public DynamicBasicAuthInterceptor(RestApiProperties apiProps) {
    this.apiProps = apiProps;
  }
  
  public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    addBasicAuth(request);
    return execution.execute(request, body);
  }
  
  public ListenableFuture<ClientHttpResponse> intercept(HttpRequest request, byte[] body, AsyncClientHttpRequestExecution execution) throws IOException {
    addBasicAuth(request);
    return execution.executeAsync(request, body);
  }
  
  private void addBasicAuth(HttpRequest request) {
    if ("true".equalsIgnoreCase(this.apiProps.getBasicAuth())) {
      String username = this.apiProps.getUsername();
      String password = this.apiProps.getPassword();
      if (username != null && !username.isEmpty()) {
        String token = Base64Utils.encodeToString((username + ":" + password).getBytes(UTF_8));
        request.getHeaders().set("Authorization", "Basic " + token);
      } 
    } 
    if ("true".equalsIgnoreCase(this.apiProps.getUserTypesBasicAuth())) {
      String role = request.getHeaders().getFirst("idpctx-user-type");
      if (this.apiProps.getUserTypes().get(role) != null) {
        String username = ((UserType)this.apiProps.getUserTypes().get(role)).getUsername();
        String password = ((UserType)this.apiProps.getUserTypes().get(role)).getPassword();
        String token = Base64Utils.encodeToString((username + ":" + password).getBytes(UTF_8));
        request.getHeaders().set("Authorization", "Basic " + token);
      } 
    } 
  }
}
