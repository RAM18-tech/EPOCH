package com.dtv.dcp.epoch.service.customergraph;

import java.security.cert.X509Certificate;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.RestTemplate;

import com.dtv.dcp.epoch.common.httpclient.config.RestTemplateBeanFactory;


public abstract class CustomerGraphClient {

	@Autowired
	private RestTemplateBeanFactory restTemplateFactory;
	
	@Value("${apiclient.rest.default.connectTimeout}")
	String connTimeout;
	
	@Value("${apiclient.rest.default.readTimeout}")
	String readTimeout;

	private RestTemplate restTemplate;
	
	/** The proxy auth host. */
	@Value("${idp.com.att.proxyAuthHost}")
	private String proxyHost;
	
	/** The proxy port. */
	@Value("${idp.com.att.proxyAuthPort}")
	private int proxyPort;
	
	@Value("${apiclient.rest.default.userTypes.registered.username}")
	private String username;
	
	@Value("${apiclient.rest.default.userTypes.registered.password}")
	private String password;
	
	/** The proxy port. */
	@Value("${dcp.customergraph.proxyEnabled:false}")
	private String isProxyRequired;

	@PostConstruct
	protected void init() throws Exception {
		restTemplate = getRestTemplate(proxyRequired(), getApiKey(), true, getConnTimeout(), getReadTimeout());
	}

	public <T, R> R makePostCall(T serviceRequest, Class<R> clazz, String baseUrl, String path) {

		HttpEntity<T> request = new HttpEntity<>(serviceRequest);
		R serviceResponse = null;
		String url = formatHttpUrl(baseUrl, path);
		ResponseEntity<R> responseEntity = getRestTemplate().postForEntity(url, request, clazz);
		serviceResponse = responseEntity.getBody();

		return serviceResponse;
	}

	public <R> R makeGetCall(Class<R> clazz, String baseUrl, String path) {

		R serviceResponse = null;
		String url = formatHttpUrl(baseUrl, path);
		ResponseEntity<R> responseEntity = getRestTemplate().getForEntity(url, clazz);
		serviceResponse = responseEntity.getBody();

		return serviceResponse;
	}

	protected abstract String getApiKey();
	
	protected boolean proxyRequired() {
		return "enabled".equalsIgnoreCase(isProxyRequired);
	}
	
	protected int getConnTimeout() {
		return Integer.valueOf(connTimeout);
	}
	
	protected int getReadTimeout() {
		return Integer.valueOf(readTimeout);
	}

	protected RestTemplate getRestTemplate() {
		return restTemplate;
	}
	
	private RestTemplate getRestTemplate(boolean requireProxy, String apiName, boolean sslOff, int connTimeout,
			int readTimeout) throws Exception {
		restTemplate = restTemplateFactory.getObject(apiName);
		HttpClientBuilder httpClientBuilder = HttpClients.custom();
		if(requireProxy) {
			httpClientBuilder.setProxy(new HttpHost(proxyHost, proxyPort));
		}
		if (sslOff) {
			TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
			SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
					.loadTrustMaterial(null, acceptingTrustStrategy).build();
			SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
			httpClientBuilder.setSSLSocketFactory(csf);
		}
		CloseableHttpClient httpClient = httpClientBuilder.build();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setConnectTimeout(connTimeout);
		requestFactory.setReadTimeout(readTimeout);
		requestFactory.setHttpClient(httpClient);
		restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(requestFactory));
		restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor(username, password));
		return restTemplate;
	}
	
	public static String formatHttpUrl(String baseUrl,String path) {
		if (StringUtils.isBlank(path)) { 
			return baseUrl;
		} else if (path.startsWith("http")) {
			return path;
		}
		return baseUrl+path;
	}
	
}
