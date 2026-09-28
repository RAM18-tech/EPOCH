package com.dtv.dcp.epoch.common.httpclient.config;


import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.Properties;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriTemplateHandler;

import com.dtv.dcp.epoch.common.httpclient.ApiLoggingInterceptor;
import com.dtv.dcp.epoch.common.httpclient.DefaultHeadersInterceptor;
import com.dtv.dcp.epoch.common.httpclient.DynamicBasicAuthInterceptor;
import com.dtv.dcp.epoch.common.httpclient.context.IdpContextInterceptor;
import com.dtv.dcp.epoch.common.logging.client.RestApiClientLogger;

import io.opentracing.Tracer;
import io.opentracing.contrib.spring.web.client.TracingRestTemplateInterceptor;


@Component
public class RestTemplateBeanFactory implements FactoryBean<RestTemplate> {
  private static final Logger logger = LoggerFactory.getLogger(RestTemplateBeanFactory.class);
  
  
  @Autowired
  private Tracer tracer;
  
  @Autowired
  private RestTemplateBuilder builder;
  
  @Autowired
  private RestClientConfig restClientProps;
  
  public RestTemplate getObject() throws Exception {
    return this.builder.build();
  }
  
  public RestTemplate getObject(String apiName) throws Exception {
    if (apiName == null)
      throw new NullPointerException("apiName"); 
    RestApiProperties apiProps = this.restClientProps.getProperties(apiName);
    RestTemplateBuilder bldr = tracingInterceptor(this.builder, apiProps);
    bldr = basicAuthInterceptor(bldr, apiProps);
    bldr = defaultHeadersInterceptor(bldr, apiProps);
    bldr = idpContextInterceptor(bldr, apiProps);
    bldr = customInterceptor(bldr, apiProps);
    bldr = loggingInterceptor(bldr, apiName, apiProps);
    
    RestTemplate restTemplate = bldr.build();
    PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = null;
    if (apiProps.isKeyStoreEnabled().booleanValue()) {
      SSLContextBuilder sslBuilder = new SSLContextBuilder();
      KeyStore cks = KeyStore.getInstance(KeyStore.getDefaultType());
      cks.load(new FileInputStream(new File(apiProps.getKeyStorePath())), apiProps
          .getKeyStorePassword().toCharArray());
      sslBuilder.loadKeyMaterial(cks, apiProps.getKeyStorePassword().toCharArray());
      sslBuilder.setProtocol(apiProps.getKeyStoreProtocol());
      SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslBuilder.build());
      
      Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
              .<ConnectionSocketFactory>create().register("https", sslsf)
              .register("http", sslsf)
              .build();
      
      
      poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
    } else {
      poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
    } 
    poolingHttpClientConnectionManager.setDefaultMaxPerRoute(apiProps.getMaxConnectionsPerRoute());
    poolingHttpClientConnectionManager.setMaxTotal(apiProps.getMaxConnectionsTotal());
    HttpClientBuilder httpBuilder = HttpClients.custom().useSystemProperties().setConnectionManager((HttpClientConnectionManager)poolingHttpClientConnectionManager);
    if ("enabled".equalsIgnoreCase(apiProps.isProxyRequired()))
      httpBuilder.setRoutePlanner((HttpRoutePlanner)new DefaultProxyRoutePlanner(new HttpHost(apiProps
              .getProxyHost(), apiProps.getProxyPort()))); 
    CloseableHttpClient httpClient = httpBuilder.build();
    HttpComponentsClientHttpRequestFactory httpClientFactory = new HttpComponentsClientHttpRequestFactory((HttpClient)httpClient);
    httpClientFactory.setConnectTimeout(apiProps.getConnectTimeout());
    httpClientFactory.setReadTimeout(apiProps.getReadTimeout());
    restTemplate.setRequestFactory((ClientHttpRequestFactory)new BufferingClientHttpRequestFactory((ClientHttpRequestFactory)httpClientFactory));
    restTemplate.setErrorHandler((ResponseErrorHandler)new DefaultResponseErrorHandler());
    return restTemplate;
  }
  
	private RestTemplateBuilder tracingInterceptor(RestTemplateBuilder bldr, RestApiProperties apiProps) {
		return bldr.additionalInterceptors(new ClientHttpRequestInterceptor[] {
				(ClientHttpRequestInterceptor) new TracingRestTemplateInterceptor(this.tracer) });
	}
  
  private RestTemplateBuilder basicAuthInterceptor(RestTemplateBuilder bldr, RestApiProperties apiProps) {
    if (apiProps != null && ("true".equalsIgnoreCase(apiProps.getBasicAuth()) || "true"
      .equalsIgnoreCase(apiProps.getUserTypesBasicAuth())))
      return bldr.additionalInterceptors(new ClientHttpRequestInterceptor[] { (ClientHttpRequestInterceptor)new DynamicBasicAuthInterceptor(apiProps) }); 
    return bldr;
  }
  
  private RestTemplateBuilder defaultHeadersInterceptor(RestTemplateBuilder bldr, RestApiProperties apiProps) {
    return bldr.additionalInterceptors(new ClientHttpRequestInterceptor[] { (ClientHttpRequestInterceptor)new DefaultHeadersInterceptor(apiProps) });
  }
  
  private RestTemplateBuilder loggingInterceptor(RestTemplateBuilder builder, String name, RestApiProperties apiProps) {
	    ApiLoggingInterceptor interceptor = new ApiLoggingInterceptor(new RestApiClientLogger(name));
	    return builder.uriTemplateHandler(interceptor.createUriTemplateHandler((UriTemplateHandler)new DefaultUriBuilderFactory()))
	      .additionalInterceptors(new ClientHttpRequestInterceptor[] { (ClientHttpRequestInterceptor)interceptor });
	  }
 
  private RestTemplateBuilder idpContextInterceptor(RestTemplateBuilder builder, RestApiProperties apiProps) {
    if (apiProps == null)
      return builder; 
    Properties props = new Properties();
    props.setProperty("contextAsCookie", Boolean.toString(apiProps.isContextAsCookie()));
    return builder.additionalInterceptors(new ClientHttpRequestInterceptor[] { (ClientHttpRequestInterceptor)new IdpContextInterceptor(props) });
  }
  
  private RestTemplateBuilder customInterceptor(RestTemplateBuilder bldr, RestApiProperties apiProps) {
    if (apiProps != null && null != apiProps.getInterceptors() && !apiProps.getInterceptors().isEmpty())
      for (String interceptor : apiProps.getInterceptors()) {
        try {
          bldr = bldr.additionalInterceptors(new ClientHttpRequestInterceptor[] { (ClientHttpRequestInterceptor)Class.forName(interceptor).newInstance() });
        } catch (InstantiationException|IllegalAccessException|ClassNotFoundException e) {
          logger.warn("Exception while trying to instantiate and load custom Interceptor: {}", interceptor, e);
        } 
      }  
    return bldr;
  }
  
  public Class<?> getObjectType() {
    return RestTemplate.class;
  }
  
  public boolean isSingleton() {
    return false;
  }
}
 
 