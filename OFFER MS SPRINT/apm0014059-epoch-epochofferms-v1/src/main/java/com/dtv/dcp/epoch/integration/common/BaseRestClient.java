package com.dtv.dcp.epoch.integration.common;

import java.io.IOException;
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
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.httpclient.config.RestTemplateBeanFactory;


/**
 * Created by nk3077 on 03/21/2019.
 * This is common  rest and can be use for any Ms by overriding the method 
 */

public abstract class BaseRestClient {
	
	/** The log. */
	private static final Logger log = LoggerFactory.getLogger(BaseRestClient.class);
	
	/** The rest template. */
	@Autowired
	private RestTemplateBeanFactory restTemplateFactory;
	
	/** The proxy auth host. */
	@Value("${idp.com.att.proxyAuthHost}")
	private String proxyHost;
	
	/** The proxy port. */
	@Value("${idp.com.att.proxyAuthPort}")
	private int proxyPort;
	
	/** The proxy port. */
	@Value("${dcp.proxyEnabled}")
	private String isProxyRequired;
	
	/** The proxy enabled. */
	@Value("${dcp.base.proxyEnabled:false}")
	private String isProxyEnabled;
	
	/** The Default max Connections For Localhost . */
	@Value("${idp.com.att.defaultMaxPerRoute}")
	private int defaultMaxPerRoute;
	
	public static final String CT_HOST = "api.us-east-2.aws.commercetools.com";
	
	
	/** The rest restTemplate. */
	private RestTemplate restTemplate;
	
	/** The the abstract method  getting value of APIKEY. */
	protected abstract String getApiKey();
	
	/** The the abstract method  getting value of isProxyEnabled. */
	protected abstract boolean  isProxyEnabled();
	
	/** The the abstract method  getting value of isSSLEnabled. */
	protected abstract boolean  isSSLEnabled();
	
	/** The the abstract method  getting value of connectTimeout. */
	protected abstract int  connectTimeout();
	
	/** The the abstract method  getting value of readTimeout. */
	protected abstract int  readTimeout();
	/** The the abstract method  getting value of number of connections. */
	protected abstract int  noOfConnections();
	
	/** The rest restTemplate. */
	private RestTemplate restCustomTemplate;

	/**
	 * Instantiates a new rest client.
	 *
	 */
	protected RestTemplate getRestTemplate() {
		return restTemplate;
	}

	protected RestTemplate getCustomRestTemplate() {
		return restCustomTemplate;
	}
	
	
	/**
	 * Post construction populating values.
	 *
	 */
	@PostConstruct
	protected void init() throws Exception {
		restCustomTemplate = getCustomRestTemplate("enabled".equalsIgnoreCase(isProxyRequired), getApiKey(), isSSLEnabled() , connectTimeout(), readTimeout(),noOfConnections());
		restTemplate = getRestTemplate("enabled".equalsIgnoreCase(isProxyEnabled), getApiKey(), isSSLEnabled() , connectTimeout(), readTimeout(),noOfConnections());
	}
	
	

	/**
	 * make a post call makePostCall() .
	 *
	 * @param  serviceRequest
	 * @param clazz
	 * @param baseUrl 
	 * @param path
	 * @return generic object Class<R>
	 */
	
	
	public <T, R> R makePostCall(T serviceRequest, Class<R> clazz, String baseUrl, String path) {
		log.debug("Start of BaseRestClient.makePostCall method..");
		HttpEntity<T> request = new HttpEntity<>(serviceRequest);
		R serviceResponse = null;
		String url = formatHttpUrl(baseUrl, path);
		ResponseEntity<R> responseEntity = null;
		if (null != url && url.contains("auth")) {
			responseEntity = getRestTemplate().postForEntity(url, request, clazz);
		} else {
			responseEntity = getCustomRestTemplate().postForEntity(url, request, clazz);
		}		
		serviceResponse = responseEntity.getBody();
		log.debug("End of BaseRestClient.makePostCall method..");
		return serviceResponse;
	}
	
	/**
	 * make a Get call makeGetCall() .
	 *
	 * @param clazz
	 * @param baseUrl
	 * @param path
	 * @return generic object Class<R>
	 * @throws Exception 
	 */
	public <R> R makeGetCall(Class<R> clazz, String baseUrl, String path) throws RestClientException {
		log.debug("Start of BaseRestClient.makeGetCall method..");
		R serviceResponse = null;
		String urlWhiteListed = "https://";
		String url = formatHttpUrl(baseUrl, path);
		ResponseEntity<R> responseEntity = null;
		try {
			if (!baseUrl.startsWith(urlWhiteListed)) {
				throw new IOException();
			} else {
				if ( baseUrl.contains("directv.com")) {
					responseEntity = getCustomRestTemplate().getForEntity(url, clazz);						
				} else {
					responseEntity = getRestTemplate().getForEntity(url, clazz);
				}
				serviceResponse = responseEntity.getBody();
			}
		} catch (Exception e) {
			log.error("Exception in BaseRestClient : {}", e.getMessage());
			throw new RestClientException("RestClientException, being compromised !");
		}

		log.debug("END of BaseRestClient.makeGetCall method..");
		return serviceResponse;
	}
	
	/**
	 * make a Get call makeGetCall() .
	 *
	 * @param clazz
	 * @param baseUrl
	 * @param path
	 * @return generic object Class<R>
	 * @throws Exception 
	 */
	public <R> R makeGetCallCoupon(Class<R> clazz, String baseUrl, String path) throws RestClientException {
		log.debug("Start of BaseRestClient.makeGetCallCoupon method..");
		R serviceResponse = null;
		String url = formatHttpUrl(baseUrl, path);
		try {
			if ((baseUrl.startsWith("https") && (baseUrl.contains("att")) || baseUrl.contains("commercetools.com")) 
					|| (baseUrl.startsWith("http") && (baseUrl.contains("customergraphproductms")))
					|| (baseUrl.startsWith("https") && (baseUrl.contains("customergraphproductms")))) {
				ResponseEntity<R> responseEntity = getRestTemplate().getForEntity(url, clazz);
				serviceResponse = responseEntity.getBody();
			}else if(baseUrl.contains("directv.com")) {
				ResponseEntity<R> responseEntity = getCustomRestTemplate().getForEntity(url, clazz);
				serviceResponse = responseEntity.getBody();
			}else {
				throw new IOException("IOException experienced !");
			}
		} catch (Exception e) {
			throw new RestClientException("RestClientException, being compromised !");
		}

		log.debug("END of BaseRestClient.makeGetCallCoupon method..");
		return serviceResponse;
	}
	
	public <R> R makeGetCall(Class<R> clazz, String baseUrl, String path, String smartAttributes)
			throws RestClientException {
		log.debug("Start of BaseRestClient.makeGetCall smartAttributes method..");
		log.info("CG_SMART_ATTRIBUTES :[{}]", smartAttributes);
		R serviceResponse = null;
		String urlWhiteListed = "https://";
		String url = formatHttpUrl(baseUrl, path);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(Constants.SMART_ATTRIBUTES, smartAttributes);

		try {
			if (!baseUrl.startsWith(urlWhiteListed)) {
				throw new IOException();
			} else {
				if (baseUrl.startsWith("https")
						&& (baseUrl.contains("att") || baseUrl.contains("commercetools.com") || baseUrl.contains("customergraphproductms") 
								)) {
					ResponseEntity<R> responseEntity = getRestTemplate().exchange(url, HttpMethod.GET,
							new HttpEntity<Object>(headers), clazz);
					serviceResponse = responseEntity.getBody();
				} else if(baseUrl.contains("directv.com")) {
					ResponseEntity<R> responseEntity = getCustomRestTemplate().exchange(url, HttpMethod.GET,
							new HttpEntity<Object>(headers), clazz);
					serviceResponse = responseEntity.getBody();
				}else {
				
					throw new RestClientException("BaseURL, is being compromised !");
				}
			}
		} catch (Exception e) {
			throw new RestClientException("RestClientException, being compromised !");
		}

		log.debug("END of BaseRestClient.makeGetCall smartAttributes method..");
		return serviceResponse;

	}
	
	/**
	 * make a Get call makeGetCall() .
	 *
	 * @param clazz
	 * @param baseUrl
	 * @param path
	 * @return generic object Class<R>
	 * @throws Exception 
	 */
	public <R> R makeCTCustomGetCall(Class<R> clazz, String baseUrl, String path) throws RestClientException {
		log.debug("Start of BaseRestClient.makeGetCall method..");
		R serviceResponse = null;
		String urlWhiteListed = "https://";
		String url = formatHttpUrl(baseUrl, path);
		ResponseEntity<R> responseEntity = null;
		try {
			if (!baseUrl.startsWith(urlWhiteListed)) {
				throw new IOException();
			} else {
				if (baseUrl.startsWith("https") && (baseUrl.contains("att") || baseUrl.contains("commercetools.com") || baseUrl.contains("directv.com"))) {
					if(baseUrl.contains("directv.com")) {
						responseEntity = getCustomRestTemplate().getForEntity(url, clazz);
					}else {
					 responseEntity = getRestTemplate().getForEntity(url, clazz);
					}
					serviceResponse = responseEntity.getBody();
				} else {
					throw new IOException("IOException experienced !");
				}
			}
		} catch (Exception e) {
			log.error("Exception in BaseRestClient : {}", e.getMessage());
			throw new RestClientException("RestClientException, being compromised !");
		}

		log.debug("END of BaseRestClient.makeGetCall method..");
		return serviceResponse;
	}

	public RestTemplate getRestTemplate(boolean requireProxy,String apiName, boolean isSSLEnabled , int connectTimeout, int readTimeout,int noOfConnections) throws Exception {
		log.info("getRestTemplate " + apiName + ":: requireProxy= " + requireProxy + ":: isSSLEnabled= " + isSSLEnabled
				+ ":: connectTimeout= " + connectTimeout + ":: readTimeout=" + readTimeout + ":: noOfConnections=" + noOfConnections 
				+ ":: defaultMaxPerRoute= " + defaultMaxPerRoute); 
		
		if(restTemplate==null){
			restTemplate=restTemplateFactory.getObject(apiName);
			HttpClientBuilder httpClientBuilder = HttpClients.custom();		
			if(noOfConnections!=0){
				PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(); 
				connManager.setMaxTotal(noOfConnections); 
				connManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
				//HttpHost localhost = new HttpHost("locahost", maxConnectionsForLocalhost);
				//connManager.setMaxPerRoute(new HttpRoute(localhost), maxPerRoute);
				httpClientBuilder.setConnectionManager(connManager);
			}
			if(requireProxy) {
				httpClientBuilder.setProxy(new HttpHost(proxyHost, proxyPort));
			}
			
			if(!isSSLEnabled) {
				TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
				SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
				        .loadTrustMaterial(null, acceptingTrustStrategy)
				        .build();
				SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext,new NoopHostnameVerifier());
				httpClientBuilder.setSSLSocketFactory(csf);
			}
			CloseableHttpClient httpClient =  httpClientBuilder.build();		
			HttpComponentsClientHttpRequestFactory requestFactory= new HttpComponentsClientHttpRequestFactory();
			requestFactory.setConnectTimeout(connectTimeout);
			requestFactory.setReadTimeout(readTimeout);
		    requestFactory.setHttpClient(httpClient);
		    restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(requestFactory));
		}
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
	
	
	public RestTemplate getCustomRestTemplate(boolean requireProxy,String apiName, boolean isSSLEnabled , int connectTimeout, int readTimeout,int noOfConnections) throws Exception {
		log.info("getCustomRestTemplate =" + apiName + ":: requireProxy= " + requireProxy + ":: isSSLEnabled= " + isSSLEnabled
				+ ":: connectTimeout= " + connectTimeout + ":: readTimeout=" + readTimeout + ":: noOfConnections=" + noOfConnections 
				+ ":: defaultMaxPerRoute= " + defaultMaxPerRoute); 
		
		if(restCustomTemplate==null){
			restCustomTemplate=restTemplateFactory.getObject(apiName);
			HttpClientBuilder httpClientBuilder = HttpClients.custom();		
			if(noOfConnections!=0){
				PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(); 
				connManager.setMaxTotal(noOfConnections); 
				connManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
				//HttpHost localhost = new HttpHost("locahost", maxConnectionsForLocalhost);
				//connManager.setMaxPerRoute(new HttpRoute(localhost), maxPerRoute);
				httpClientBuilder.setConnectionManager(connManager);
			}
			if(requireProxy) {
				httpClientBuilder.setProxy(new HttpHost(proxyHost, proxyPort));
			}
			
			if(!isSSLEnabled) {
				TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
				SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
				        .loadTrustMaterial(null, acceptingTrustStrategy)
				        .build();
				SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext,new NoopHostnameVerifier());
				httpClientBuilder.setSSLSocketFactory(csf);
			}
			CloseableHttpClient httpClient =  httpClientBuilder.build();		
			HttpComponentsClientHttpRequestFactory requestFactory= new HttpComponentsClientHttpRequestFactory();
			requestFactory.setConnectTimeout(connectTimeout);
			requestFactory.setReadTimeout(readTimeout);
		    requestFactory.setHttpClient(httpClient);
		    restCustomTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(requestFactory));
		}
		return restCustomTemplate;
	}
}
