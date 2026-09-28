package com.dtv.dcp.epoch.common.httpclient;



import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.AsyncClientHttpRequestExecution;
import org.springframework.http.client.AsyncClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.concurrent.ListenableFuture;

import com.dtv.dcp.epoch.common.httpclient.config.RestApiProperties;

public class DefaultHeadersInterceptor implements ClientHttpRequestInterceptor, AsyncClientHttpRequestInterceptor {
  private static final Logger logger = LoggerFactory.getLogger(DefaultHeadersInterceptor.class);
  
  private static final String FEATURE_FLAGS_ENABLED = "platform_dev_apiclient_featureFlagsPropagation_enabled";
  
  private static final String IXP_FLAGS_PROPAGATION = "idpctx-ixp-propagate";
  
  private static final String IDP_TRACE_ID = "idp-trace-id";
  
  private RestApiProperties apiProps;
  
  private Predicate<String> regexFilter;
  
  private Predicate<String> matchFilter;
  
  public DefaultHeadersInterceptor(RestApiProperties apiProps) {
    this.apiProps = apiProps;
    if (apiProps != null && (StringUtils.isNotEmpty(apiProps.getExcludeHeaderRegex()) || 
      StringUtils.isNotEmpty(apiProps.getExcludeHeaders()))) {
      if (StringUtils.isNotEmpty(apiProps.getExcludeHeaderRegex())) {
        String ctxHeaderRegex = apiProps.getExcludeHeaderRegex();
        logger.trace("setting regex exclude {}", ctxHeaderRegex);
        if (ctxHeaderRegex.contains(","))
          logger.warn("You have used an invalid Regex pattern: {} for exludeHeaderRegex property with a , (comma) in it; filter on this regex might fail", ctxHeaderRegex); 
        setRegexFilter(Pattern.compile(ctxHeaderRegex).asPredicate());
      } else {
        logger.trace("setting regex exclude to filter empty");
        setRegexFilter(String::isEmpty);
      } 
      if (StringUtils.isNotEmpty(apiProps.getExcludeHeaders())) {
        logger.trace("setting header exclude {}", apiProps.getExcludeHeaders());
        setMatchFilter(str -> Arrays.<String>asList(apiProps.getExcludeHeaders().split("\\s*,\\s*")).contains(str));
      } else {
        logger.trace("setting header exclude to filter empty");
        setMatchFilter(String::isEmpty);
      } 
    } 
  }
  
  public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    logger.trace("DefaultHeadersInterceptor called");
    addDefaultHeaders(request);
    removeRequestHeaders(request);
    return execution.execute(request, body);
  }
  
  public ListenableFuture<ClientHttpResponse> intercept(HttpRequest request, byte[] body, AsyncClientHttpRequestExecution execution) throws IOException {
    logger.trace("DefaultHeadersInterceptor called for AsyncRestTemplate");
    addDefaultHeaders(request);
    removeRequestHeaders(request);
    return execution.executeAsync(request, body);
  }
  
  private void addDefaultHeaders(HttpRequest request) {
    HttpHeaders headers = request.getHeaders();
    String traceId = MDC.get("idp-trace-id");
    if (!StringUtils.isBlank(traceId))
      headers.add("idp-trace-id", traceId); 
    if (this.apiProps == null)
      return; 
    for (Map.Entry<String, String> entry : (Iterable<Map.Entry<String, String>>)this.apiProps.getDefaultHeaders().entrySet())
      headers.add(entry.getKey(), entry.getValue()); 
  }
  
  private void removeRequestHeaders(HttpRequest request) {
    HttpHeaders headers = request.getHeaders();
    headers.remove("open-trace-id");
    logger.trace("Headers pre exlusion {}", headers.keySet());
    if (getRegexFilter() != null || getMatchFilter() != null) {
      logger.trace("Excluding Header {} and regex {}", this.apiProps.getExcludeHeaders(), this.apiProps
          .getExcludeHeaderRegex());
      logger.trace("Predicates created for exclude {} and regex {}", getMatchFilter(), getRegexFilter());
      List<String> removeHeaders = (List<String>)headers.keySet().stream().filter(getRegexFilter().or(getMatchFilter())).collect(Collectors.toList());
      logger.debug("Excluding the headers {} for rest api {}", removeHeaders, request.getURI());
      headers.keySet().removeAll(removeHeaders);
      logger.trace("Headers post exlusion {}", headers.keySet());
    } 
    String featureFlagsPropEnabled = System.getenv("platform_dev_apiclient_featureFlagsPropagation_enabled");
    if (featureFlagsPropEnabled == null || Boolean.valueOf(featureFlagsPropEnabled).booleanValue() || 
      !StringUtils.isEmpty(headers.getFirst("idpctx-ixp-propagate")))
      return; 
    Set<String> featureHeaders = new HashSet<>();
    headers.forEach((k, v) -> {
          if (k.contains("idpctx-feature-svc") || k.contains("idpctx-feature-cfg") || k.contains("idpctx-feature-exp"))
            featureHeaders.add(k); 
        });
    logger.trace("Removing the features flags headers {} for rest api {}", featureHeaders, request.getURI());
    for (String header : featureHeaders)
      headers.remove(header); 
  }
  
  public Predicate<String> getRegexFilter() {
    return this.regexFilter;
  }
  
  public void setRegexFilter(Predicate<String> regexFilter) {
    this.regexFilter = regexFilter;
  }
  
  public Predicate<String> getMatchFilter() {
    return this.matchFilter;
  }
  
  public void setMatchFilter(Predicate<String> matchFilter) {
    this.matchFilter = matchFilter;
  }
}
