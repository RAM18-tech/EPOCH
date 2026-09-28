package com.dtv.dcp.epoch.integration;


import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.commercetools.graphql.api.types.CartDiscount;
import com.dtv.dcp.epoch.integration.mapping.GlobalConfigMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.defaultconfig.ApiRootBuilder;
import com.commercetools.api.defaultconfig.ServiceRegion;
import com.commercetools.api.models.graph_ql.GraphQLRequest;
import com.commercetools.api.models.graph_ql.GraphQLResponse;
import com.commercetools.graphql.api.GraphQLDataImpl;
import com.commercetools.graphql.api.types.Product;
import com.commercetools.http.okhttp4.CtOkHttp4Client;
import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.common.CpopBackupBaseClient;
import com.dtv.dcp.epoch.integration.mapping.GraphqlResponseMap;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.ct.burn.CommerceBurnRequest;
import com.dtv.dcp.epoch.model.ct.burn.CommerceBurnResponse;
import com.dtv.dcp.epoch.model.ct.burn.OfferBurnRequest;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.offer.GlobalEligibilityRule;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.model.ct.productType.ProductType;
import com.dtv.dcp.epoch.model.ct.request.CTBenefitsRequest;
import com.dtv.dcp.epoch.model.ct.request.CTCouponsRequest;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.dtv.dcp.epoch.model.ct.request.CTShoppingCartRequest;
import com.dtv.dcp.epoch.model.ct.request.Pagination;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.model.ct.response.CTCouponResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.model.ct.response.CTShoppingCartResponse;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.JsonService;
import com.dtv.dcp.epoch.util.Util;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.vrap.rmf.base.client.ApiHttpResponse;
import io.vrap.rmf.base.client.VrapHttpClient;
import io.vrap.rmf.base.client.oauth2.ClientCredentials;

import static com.dtv.dcp.epoch.util.Util.queryBuilder;

/**
 * Created by nk3077 on 03/21/2019.
 */

@Component("CpopBackUpClient")
public class CpopBackUpClient extends CpopBackupBaseClient {

    /**
     * The log.
     */
    private static final Logger log = LoggerFactory.getLogger(CpopBackUpClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    /**
     * The Constant epoch.
     */
    private static final String EPOCH = "epoch";

    @Value("${apiclient.graphql.backup.env}")
    private String graphQlEnv;

    @Value("${apiclient.graphql.backup.username}")
    private String graphQLUserName;
    
    @Value("${apiclient.graphql.backup.password}")
    private String graphQLPassword;
    
	@Autowired
	GraphqlResponseMap graphqlResponseMapping;

    @Autowired
    GlobalConfigMapper globalConfigMapper;
    /**
     * The rest baseUrl.
     */
    @Value("${apiclient.rest.backupcpop.baseUrl}")
    private String baseUrl;
    /**
     * CT API base URL
     */
    @Value("${apiclient.rest.ctoffersms.backupct.baseUrl}")
    private String cTBaseUrl;
    /**
     * The  proxyEnabled.
     */
    @Value("${apiclient.rest.backupcpop.proxyEnabled}")
    private String proxyEnabled;
    /**
     * The  sslEnabled.
     */
    @Value("${apiclient.rest.backupcpop.sslEnabled:false}")
    private boolean sslEnabled;
    /**
     * The connectTimeout.
     */
    @Value("${apiclient.rest.backupcpop.connectTimeout:4000}")
    private int connectTimeout;
    /**
     * The rest readTimeout.
     */
    @Value("${apiclient.rest.backupcpop.readTimeout:4000}")
    private int readTimeout;
    /**
     * The pageLimit.
     */
    @Value("${pageLimit}")
    private int pageLimit;
    /**
     * The rest noOfConnections.
     */
    @Value("${apiclient.rest.backupcpop.noOfConnections}")
    private int noOfConnections;
    /**
     * The burn endpoint presented through CT.
     */
    @Value("${apiclient.rest.epoch.burnUrl}")
    private String burnEndpoint;

    @Value("${apiclient.rest.epoch.cartValidationUrl}")
    private String cartValidationUrl;

    @Value("${apiclient.rest.epoch.v2.cartValidationUrl}")
    private String v2CartValidationUrl;
    
	/** The proxy auth host. */
	@Value("${idp.com.att.proxyAuthHost}")
	private String proxyHost;
	
	/** The proxy port. */
	@Value("${idp.com.att.proxyAuthPort}")
	private int proxyPort;
	
	/** The proxy enabled. */
	@Value("${dcp.graphql.backup.proxyEnabled:false}")
	private String isProxyRequired;
	
	private ProjectApiRoot projectApiRoot;	

    @Autowired
    private FeatureManagerHelper featureManagerHelper;

    /**
     * The the abstract method  getting value of APIKEY.
     */
    @Override
    protected String getApiKey() {
        return EPOCH;
    }

    /**
     * The the abstract method  getting value of isProxyEnabled.
     */
    @Override
    protected boolean isProxyEnabled() {
        return "enabled".equalsIgnoreCase(proxyEnabled);

    }

    /**
     * The the abstract method  getting value of isSSLEnabled.
     */
    @Override
    protected boolean isSSLEnabled() {
        return sslEnabled;

    }

    /**
     * The the abstract method  getting value of connectTimeout.
     */
    protected int connectTimeout() {
        return connectTimeout;
    }

    /**
     * The the abstract method  getting value of readTimeout.
     */
    protected int readTimeout() {
        return readTimeout;
    }

    /**
     * The the abstract method getting value of connections.
     */
    protected int noOfConnections() {
        return noOfConnections;
    }

	private ProjectApiRoot getProjectApiRootClient() {
		if (projectApiRoot == null) {
			
			VrapHttpClient httpClient;
			if("enabled".equalsIgnoreCase(isProxyRequired)) {
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
				httpClient = new CtOkHttp4Client(builder -> builder.proxy(proxy));
			} else {
				httpClient = new CtOkHttp4Client();
			}
			projectApiRoot = ApiRootBuilder.of(httpClient)
					.defaultClient(
							ClientCredentials.of().withClientId(graphQLUserName)
									.withClientSecret(graphQLPassword).build(),
							ServiceRegion.AWS_US_EAST_2)
					.build(graphQlEnv);
		}
		return projectApiRoot;
	}

    /**
     * Recover get Offers
     *
     * @param re the re
     * @return the response entity
     */
    @Recover
    public CTOfferResponse recoverGetOffers(RestClientException re) {
        log.info("EPOCH_OFFERS_CALL_FAILED");
        if (re != null && re.getRootCause() != null && re.getRootCause() instanceof SocketTimeoutException) {
            throw new ServiceException(ErrorMessages.CPOP_OFFER_TIMEOUT_ERROR_UNKNOWN, re)
                    .addDetail(ErrorMessages.CPOP_OFFER_TIMEOUT_ERROR_UNKNOWN_DETAILS, EPOCH, Integer.toString(readTimeout));
        }

        throw new ServiceException(ErrorMessages.CPOP_OFFER_EXTERNAL_PROCESSING_ERROR, re).addDetail(
                ErrorMessages.CPOP_OFFER_EXTERNAL_API_CALL_TIME_OUT, "Offers ", "CpopClient.getOffers");
    }

    @Override
    @Cacheable(value = Constants.EPOCHOFFERSMS_CT_BACKUP_CACHE_MAP_NAME, key = "{ #ctOfferRequest,#idpConfigEnv,'epochOffers' }", unless = "#result==null or #result.offers.isEmpty()", cacheManager = "redisCacheManagerSecondHolder")
    public CTOfferResponse getOffers(String ctOfferRequest, String idpConfigEnv) {
        log.info("CT_Cacheput epochOffersMsCTCache2 Start of CpopClient.getOffers method..[{}]", baseUrl);
        log.info("EPOCH_GETOFFERS_CT_REQUEST-[{}]", JsonService.getJsonFromObject(ctOfferRequest));
        log.debug("End of CpopClient.getOffers method..");
        final String apiPath = "getoffers";
        CTOfferResponse cpopOffersResponse = null;
        CTOfferRequest ctreq = JsonService.getObjectFromJson(ctOfferRequest, CTOfferRequest.class);
        cpopOffersResponse = makePostCall(ctreq, CTOfferResponse.class, baseUrl, apiPath);


        if (featureManagerHelper.isEnabled("svc-epoch-ade-perf-change")
                && ((Optional.ofNullable(ctreq.getOfferActionType()).isPresent()
                && !ctreq.getOfferActionType().contains(Constants.ACQUISITION_ACTION_TYPE))
                && (Optional.ofNullable(ctreq.getSalesChannel()).isPresent()
                && ctreq.getSalesChannel().contains("online"))
                && (Optional.ofNullable(ctreq.getOfferProductFamily()).isPresent()
                && ctreq.getOfferProductFamily().contains("satellite")))) {
            if (cpopOffersResponse != null && cpopOffersResponse.getTotal() > ctreq.getPagination().getLimit()) {
                log.debug("Calling ADE asyncApiExecutor");
                cpopOffersResponse = asyncApiExecutorAdeFlow(cpopOffersResponse, ctreq, apiPath);
            }
        } else {
            if (cpopOffersResponse != null && cpopOffersResponse.getTotal() > pageLimit) {
                log.debug("Calling asyncApiExecutor");
                cpopOffersResponse = asyncApiExecutor(cpopOffersResponse, ctreq, apiPath);
            }
        }
        /*
         * if(cpopOffersResponse!=null && cpopOffersResponse.getTotal()>pageLimit){
         * log.debug("Calling asyncApiExecutor");
         * cpopOffersResponse=asyncApiExecutor(cpopOffersResponse, ctreq,apiPath); }
         */
        log.info("EPOCH_OFFERS_CALL_SUCCESS");
        return cpopOffersResponse;

    }

    public CTOfferResponse asyncApiExecutorAdeFlow(CTOfferResponse cpopOffersResponse, CTOfferRequest cpopOffersRequest, String api) {
        log.debug("Start of ADE CpopClient.asyncApiExecutor method..");
        List<CompletableFuture<CTOfferResponse>> cfList = new ArrayList<>();
        int pages = cpopOffersResponse.getTotal() / cpopOffersRequest.getPagination().getLimit() + 1;

        for (int i = 2; i <= pages; i++) {
            CTOfferRequest asyncCpopOffersRequest = new CTOfferRequest();
            if (Objects.nonNull(cpopOffersRequest)) {
                BeanUtils.copyProperties(cpopOffersRequest, asyncCpopOffersRequest);
            }

            asyncCpopOffersRequest.setState(Objects.nonNull(cpopOffersRequest) ? cpopOffersRequest.getState() : null);
            Pagination pagination = new Pagination();
            pagination.setPage(i);
            pagination.setLimit(cpopOffersRequest.getPagination().getLimit());
            asyncCpopOffersRequest.setPagination(pagination);

            CompletableFuture<CTOfferResponse> cpopresponse = CompletableFuture
                    .supplyAsync(() -> makePostCall(asyncCpopOffersRequest, CTOfferResponse.class, baseUrl, api));
            cfList.add(cpopresponse);
        }
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(cfList.toArray(new CompletableFuture[cfList.size()]));

        // When all the Futures are completed, call `future.join()` to get their
        // results and collect the results in a list -
        CompletableFuture<List<CTOfferResponse>> allPageContentsFuture = allFutures.thenApply(v -> {
            return cfList.stream().map(pageContentFuture -> pageContentFuture.join()).collect(Collectors.toList());
        });

        List<CTOffer> offers = new ArrayList<>();
        List<ProductObj> productObjs = new ArrayList<>();
        try {
            List<CTOfferResponse> allPageResponses = allPageContentsFuture.get();
            if (allPageResponses != null && !allPageResponses.isEmpty()) {
                allPageResponses.add(cpopOffersResponse);
                allPageResponses.stream().filter(Objects::nonNull).forEach(page ->
                        {
                            offers.addAll(page.getOffers());
                            productObjs.addAll(page.getProducts());
                        }

                );
            }
        } catch (Exception e) {
            throw new ServiceException(ErrorMessages.EXTERNAL_PROCESSING_ERROR, e);
        }
        CTOfferResponse finalCpopOffersResponse = new CTOfferResponse();
        finalCpopOffersResponse.setOffers(offers);
        finalCpopOffersResponse.setProducts(productObjs);
        log.debug("End of ADE CpopClient.asyncApiExecutor method..");
        return finalCpopOffersResponse;
    }

    @Override
    @Cacheable(value = Constants.EPOCHOFFERSMS_CT_PRODUCTS_BACKUP_CACHE_MAP_NAME, key = "{ #ctProductRequest,#idpConfigEnv,'epochProducts' }", unless = "#result==null or #result.products.isEmpty()", cacheManager = "redisCacheManagerSecondHolder")
    public CTProductResponse getProducts(String ctProductRequest, String idpConfigEnv) {
        log.info("CT_Cacheput epochOffersMsProductsCache2 -Start of CpopClient.getProducts  method with baseUrl..[{}]", baseUrl);
        log.info("EPOCH_PRODUCTS_CALL_CT_REQUEST:[{}]", JsonService.getJsonFromObject(ctProductRequest));
        log.debug("End of CpopClient.getProducts method..");
        final String apiPath = "getproducts";
        CTProductRequest ctreq = JsonService.getObjectFromJson(ctProductRequest, CTProductRequest.class);
        CTProductResponse ctProductResponse = makePostCall(ctreq, CTProductResponse.class, baseUrl, apiPath);
        if (ctProductResponse != null && ctProductResponse.getTotal() > pageLimit) {
            log.debug("Calling asyncApiExecutor");
            ctProductResponse = asyncApiProductsExecutor(ctProductResponse, ctreq, apiPath);
        }
        log.info("EPOCH_PRODUCTS_CALL_SUCCESS");
        return ctProductResponse;

    }

    @Cacheable(value = Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CACHE_MAP_NAME_BACKUP, key = "{#ctProductRequest,#idpConfigEnv,'epochGlobalConfigs' }", unless = "#result==null or #result.isEmpty()", cacheManager = "redisCacheManagerSecondHolder")
    public Map<String, List<String>> loadGlobalConfigurations(String ctProductRequest, String idpConfigEnv) {
        log.info("Start of CpopBackupClient.loadGlobalConfigurations method with baseUrl..[{}]", baseUrl);
        log.info("EPOCH_PRODUCTS_CALL_CT_REQUEST:[{}]", JsonService.getJsonFromObject(ctProductRequest));
        final String apiPath = "getproducts";
        CTProductRequest ctreq = JsonService.getObjectFromJson(ctProductRequest, CTProductRequest.class);
        JsonNode ctProductResponse = makePostCall(ctreq, JsonNode.class, baseUrl, apiPath);
        Map<String, List<String>> epochGlobalConfigurations = globalConfigMapper.extractDataFromJsonNode(ctProductResponse);
        log.debug("End of CpopBackupClient.loadGlobalConfigurations method..");
        log.info("EPOCH_PRODUCTS_CALL_SUCCESS");
        return epochGlobalConfigurations;
    }

    @Cacheable(value = Constants.EPOCHOFFERMS_GLOBAL_ELIGIBILITYRULES_CACHE_MAP_NAME, key = "{#ctProductRequest,#idpConfigEnv,'epochGlobalEligibilityRules' }", unless = "#result==null or #result.isEmpty()", cacheManager = "redisCacheManagerSecondHolder")
    public List<GlobalEligibilityRule> loadGlobalEligibilityRules(String ctProductRequest, String idpConfigEnv) {
        log.info("Start of CpopPrimaryClient.loadGlobalEligibilityRules method with baseUrl..[{}]", baseUrl);
        log.info("EPOCH_PRODUCTS_CALL_CT_REQUEST:[{}]", JsonService.getJsonFromObject(ctProductRequest));
        final String apiPath = "getproducts";
        CTProductRequest ctreq = JsonService.getObjectFromJson(ctProductRequest, CTProductRequest.class);
        JsonNode ctProductResponse = makePostCall(ctreq, JsonNode.class, baseUrl, apiPath);
        List<GlobalEligibilityRule> epochGlobalEligibilityRules = globalConfigMapper.extractGlobalEligibilityRulesDataFromJsonNode(ctProductResponse);
        log.debug("End of CpopPrimaryClient.loadGlobalEligibilityRules method..");
        log.info("EPOCH_PRODUCTS_CALL_SUCCESS");
        return epochGlobalEligibilityRules;

    }
    
    @Cacheable(value = Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CACHE_MAP_NAME_BACKUP, key = "{#ctProductRequest,#idpConfigEnv,'epochGlobalValidateCartRules' }", unless = "#result==null or #result.isEmpty()", cacheManager = "redisCacheManagerSecondHolder")
    public Map<String, List<String>> loadValidateCartRules(String ctProductRequest, String idpConfigEnv) {
        log.info("Start of CpopBackupClient.loadValidateCartRules method with baseUrl..[{}]", baseUrl);
        log.info("EPOCH_PRODUCTS_CALL_CT_REQUEST:[{}]", JsonService.getJsonFromObject(ctProductRequest));
        final String apiPath = "getproducts";
        CTProductRequest ctreq = JsonService.getObjectFromJson(ctProductRequest, CTProductRequest.class);
        JsonNode ctProductResponse = makePostCall(ctreq, JsonNode.class, baseUrl, apiPath);
        Map<String, List<String>> epochGlobalConfigurations = globalConfigMapper.extractValidateCartRulesDataFromJsonNode(ctProductResponse);
        log.debug("End of CpopBackupClient.loadValidateCartRules method..");
        log.info("EPOCH_PRODUCTS_CALL_SUCCESS");
        return epochGlobalConfigurations;
    }

    public CTOfferResponse asyncApiExecutor(CTOfferResponse cpopOffersResponse, CTOfferRequest cpopOffersRequest, String api) {
        log.debug("Start of CpopClient.asyncApiExecutor method..");
        List<CompletableFuture<CTOfferResponse>> cfList = new ArrayList<>();
        int pages = cpopOffersResponse.getTotal() / pageLimit + 1;

        for (int i = 2; i <= pages; i++) {
            CTOfferRequest asyncCpopOffersRequest = new CTOfferRequest();
            if (Objects.nonNull(cpopOffersRequest)) {
                BeanUtils.copyProperties(cpopOffersRequest, asyncCpopOffersRequest);
            }

            asyncCpopOffersRequest.setState(Objects.nonNull(cpopOffersRequest) ? cpopOffersRequest.getState() : null);
            Pagination pagination = new Pagination();
            pagination.setPage(i);
            pagination.setLimit(pageLimit);
            asyncCpopOffersRequest.setPagination(pagination);

            CompletableFuture<CTOfferResponse> cpopresponse = CompletableFuture
                    .supplyAsync(() -> makePostCall(asyncCpopOffersRequest, CTOfferResponse.class, baseUrl, api));
            cfList.add(cpopresponse);
        }
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(cfList.toArray(new CompletableFuture[cfList.size()]));

        // When all the Futures are completed, call `future.join()` to get their
        // results and collect the results in a list -
        CompletableFuture<List<CTOfferResponse>> allPageContentsFuture = allFutures.thenApply(v -> {
            return cfList.stream().map(pageContentFuture -> pageContentFuture.join()).collect(Collectors.toList());
        });

        List<CTOffer> offers = new ArrayList<>();
        List<ProductObj> productObjs = new ArrayList<>();
        try {
            List<CTOfferResponse> allPageResponses = allPageContentsFuture.get();
            if (allPageResponses != null && !allPageResponses.isEmpty()) {
                allPageResponses.add(cpopOffersResponse);
                allPageResponses.stream().filter(Objects::nonNull).forEach(page ->
                        {
                            offers.addAll(page.getOffers());
                            productObjs.addAll(page.getProducts());
                        }

                );
            }
        } catch (Exception e) {
            throw new ServiceException(ErrorMessages.EXTERNAL_PROCESSING_ERROR, e);
        }
        CTOfferResponse finalCpopOffersResponse = new CTOfferResponse();
        finalCpopOffersResponse.setOffers(offers);
        finalCpopOffersResponse.setProducts(productObjs);
        log.debug("End of CpopClient.asyncApiExecutor method..");
        return finalCpopOffersResponse;
    }

    public CTProductResponse asyncApiProductsExecutor(CTProductResponse cpopProductsResponse, CTProductRequest cpopProductsRequest, String api) {
        log.debug("Start of CpopClient.asyncApiExecutor method..");
        List<CompletableFuture<CTProductResponse>> cfList = new ArrayList<>();
        int pages = cpopProductsResponse.getTotal() / pageLimit + 1;

        for (int i = 2; i <= pages; i++) {
            CTProductRequest asyncCpopProductsRequest = new CTProductRequest();
            if (Objects.nonNull(cpopProductsRequest)) {
                BeanUtils.copyProperties(cpopProductsRequest, asyncCpopProductsRequest);
            }

            asyncCpopProductsRequest.setState(Objects.nonNull(cpopProductsRequest) ? cpopProductsRequest.getState() : null);
            Pagination pagination = new Pagination();
            pagination.setPage(i);
            pagination.setLimit(pageLimit);
            asyncCpopProductsRequest.setPagination(pagination);

            CompletableFuture<CTProductResponse> cpopresponse = CompletableFuture
                    .supplyAsync(() -> makePostCall(asyncCpopProductsRequest, CTProductResponse.class, baseUrl, api));
            cfList.add(cpopresponse);
        }
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(cfList.toArray(new CompletableFuture[cfList.size()]));

        // When all the Futures are completed, call `future.join()` to get their
        // results and collect the results in a list -
        CompletableFuture<List<CTProductResponse>> allPageContentsFuture = allFutures.thenApply(v -> {
            return cfList.stream().map(pageContentFuture -> pageContentFuture.join()).collect(Collectors.toList());
        });

        List<ProductObj> products = new ArrayList<>();
        try {
            List<CTProductResponse> allPageResponses = allPageContentsFuture.get();
            if (allPageResponses != null && !allPageResponses.isEmpty()) {
                allPageResponses.add(cpopProductsResponse);
                allPageResponses.stream().filter(Objects::nonNull).forEach(page ->
                        products.addAll(page.getProducts())
                );
            }
        } catch (Exception e) {
            throw new ServiceException(ErrorMessages.EXTERNAL_PROCESSING_ERROR, e);
        }
        CTProductResponse finalCpopProductsResponse = new CTProductResponse();
        finalCpopProductsResponse.setProducts(products);
        log.debug("End of CpopClient.asyncApiExecutor method..");
        return finalCpopProductsResponse;
    }

    @Override
    public CTBenefitsResponse getBenefits(CTBenefitsRequest ctBenefitsRequest) {
        log.info("Start of CpopClient.getBenefits method with baseUrl..[{}]", baseUrl);
        log.info("EPOCH_BENEFITS_CALL_CT_REQUEST: [{}]", JsonService.getJsonFromObject(ctBenefitsRequest));
        final String apiPath = "getbenefits";
        CTBenefitsResponse benefitsResponse = null;
        log.debug("End of CpopClient.getBenefits method..");

        List<String> benefitIdsList = ctBenefitsRequest.getBenefitIds() == null ? new ArrayList<>() : ctBenefitsRequest.getBenefitIds();
        List<String> benefitCodeList = ctBenefitsRequest.getBenefitCodes() == null ? new ArrayList<>() : ctBenefitsRequest.getBenefitCodes();
        List<String> salesChannelList = ctBenefitsRequest.getSalesChannel() == null ? new ArrayList<>() : ctBenefitsRequest.getSalesChannel();

        JsonNode jsonNode = null;
        try {
            jsonNode = makeGetCall(JsonNode.class, baseUrl,
					apiPath + "?benefitIds=" + String.join(",", benefitIdsList) +
							"&billingBenefitCodes=" + String.join(",", benefitCodeList) +
                            "&salesChannel=" + String.join(",", salesChannelList) +
							"&state=" + ctBenefitsRequest.getState());
        } catch (Exception excep) {
            log.error("EPOCH_BACKUP_CLIENT_MET_EXCEPTION_AS_URL_COMPROPISED" + excep);
        }

        ObjectMapper mapper = MAPPER;

        benefitsResponse = mapper.convertValue(jsonNode, new TypeReference<CTBenefitsResponse>() {
        });
        log.info("EPOCH_BENEFITS_CALL_SUCCESS");
        return benefitsResponse;

    }

    @Override
    @Cacheable(value = Constants.EPOCHOFFERSMS_CT_BENEFITS_BACKUP_CACHE_MAP_NAME, key = "{ #ctBenefitsRequestStr,#idpConfigEnv,'epochBenefits' }", unless = "#result==null or #result.benefits.isEmpty()", cacheManager = "redisCacheManagerSecondHolder")
    public CTBenefitsResponse getBenefits(String ctBenefitsRequestStr, String idpConfigEnv) {
        log.info("CT_Cacheput EPOCHOFFERSMS_CT_BENEFITS_BACKUP_CACHE_MAP_NAME -Start of EPOCHDCPClient.getBenefits method with baseUrl..[{}]", baseUrl);
        log.info("EPOCH_BENEFITS_CALL_CT_REQUEST: [{}]", JsonService.getJsonFromObject(ctBenefitsRequestStr));
        final String apiPath = "getbenefits";
        CTBenefitsResponse benefitsResponse = null;

        CTBenefitsRequest ctBenefitsRequest = JsonService.getObjectFromJson(ctBenefitsRequestStr, CTBenefitsRequest.class);
        List<String> benefitIdsList = ctBenefitsRequest.getBenefitIds() == null ? new ArrayList<>() : ctBenefitsRequest.getBenefitIds();
        List<String> benefitCodeList = ctBenefitsRequest.getBenefitCodes() == null ? new ArrayList<>() : ctBenefitsRequest.getBenefitCodes();
        List<String> salesChannelList = ctBenefitsRequest.getSalesChannel() == null ? new ArrayList<>() : ctBenefitsRequest.getSalesChannel();
        JsonNode jsonNode = null;
        try {
            jsonNode = makeGetCall(JsonNode.class, baseUrl,
					apiPath + "?benefitIds=" + String.join(",", benefitIdsList) +
							"&billingBenefitCodes=" + String.join(",", benefitCodeList) +
                            "&salesChannel=" + String.join(",", salesChannelList) +
							"&state=" + ctBenefitsRequest.getState());
        } catch (Exception excep) {
            log.error("EPOCH_PRIMARY_CLIENT_MET_EXCEPTION_AS_URL_COMPROPISED" + excep);
        }

        ObjectMapper mapper = MAPPER;

        benefitsResponse = mapper.convertValue(jsonNode, new TypeReference<CTBenefitsResponse>() {
        });
        log.info("EPOCH_BENEFITS_CALL_SUCCESS");
        return benefitsResponse;
    }

    @Override
    public CTProductResponse getProducts(CTProductRequest ctProductRequest) {
        log.info("Start of CpopClient.getProducts method with baseUrl..[{}]", baseUrl);
        log.info("EPOCH_PRODUCTS_CALL_CT_REQUEST:[{}]", JsonService.getJsonFromObject(ctProductRequest));
        log.debug("End of CpopClient.getProducts method..");
        final String apiPath = "getproducts";
        CTProductResponse ctProductResponse = makePostCall(ctProductRequest, CTProductResponse.class, baseUrl, apiPath);
        if (ctProductResponse != null && ctProductResponse.getTotal() > pageLimit) {
            log.debug("Calling asyncApiExecutor");
            ctProductResponse = asyncApiProductsExecutor(ctProductResponse, ctProductRequest, apiPath);
        }
        log.info("EPOCH_PRODUCTS_CALL_SUCCESS");
        return ctProductResponse;

    }

    @Override
    public CTProductResponse getProductsByType(String productType) {
        log.info("Start of CpopClient.getProductsByType method with baseUrl..[{}]", baseUrl);
        log.info("EPOCH_PRODUCTS_CALL_CT_REQUEST:[{}]", productType);
        log.debug("End of CpopClient.getProductsByType method..");
        final String apiPath = "getproducts";
        CTProductResponse ctProductResponse = makeGetCall(CTProductResponse.class, baseUrl, apiPath.concat("/").concat(productType));

        log.info("EPOCH_PRODUCTS_CALL_SUCCESS");
        return ctProductResponse;

    }

    @Override
    @Cacheable(value = Constants.EPOCHOFFERSMS_CT_PRODUCTS_BACKUP_CACHE_MAP_NAME, key = "{ #productType,#idpConfigEnv,'epochProductsByType' }", unless = "#result==null or #result.products.isEmpty()", cacheManager = "redisCacheManagerSecondHolder")
    public CTProductResponse getProductsByType(String productType, String idpConfigEnv) {
        log.info("CT_Cacheput epochOffersMsProductsCache2 -Start of CpopBackUpClient.getProductsByType  method with baseUrl..[{}]", baseUrl);
        log.info("EPOCH_PRODUCTS_BY_TYPE_CT_REQUEST:[{}]", productType);
        final String apiPath = "getproducts";
        CTProductResponse ctProductResponse = makeGetCall(CTProductResponse.class, baseUrl, apiPath.concat("/").concat(productType));
        log.info("EPOCH_PRODUCTS_CALL_SUCCESS");
        return ctProductResponse;
    }

    /**
     * @param ctShoppingCartRequest
     * @return
     */
    @Retryable(maxAttemptsExpression = "${apiclient.rest.backupcpop.maxAttempts}", value = {RestClientException.class})
    public CTShoppingCartResponse validateShoppingCart(CTShoppingCartRequest ctShoppingCartRequest, boolean validateShoppingCartFlag) {
        log.info("Start of CpopClient.validateShoppingCart method with baseUrl..[{}]", baseUrl);
        log.info("EPOCH_VALIDATE_SHOPPINGCART_CT_REQUEST:[{}]", JsonService.getJsonFromObject(ctShoppingCartRequest));
        if (validateShoppingCartFlag) {
            CTShoppingCartResponse cpopRewardResponse = makePostCall(ctShoppingCartRequest, CTShoppingCartResponse.class, baseUrl, v2CartValidationUrl);
            log.info("EPOCH_REWARD_CALL_SUCCESS");
            log.debug("End of CpopClient.validateShoppingCart method..");
            return cpopRewardResponse;
        } else {
            CTShoppingCartResponse cpopRewardResponse = makePostCall(ctShoppingCartRequest, CTShoppingCartResponse.class, baseUrl, cartValidationUrl);
            log.info("EPOCH_REWARD_CALL_SUCCESS");
            log.debug("End of CpopClient.validateShoppingCart method..");
            return cpopRewardResponse;
        }
    }

    /**
     * Recover get validateShoppingCart
     *
     * @param excp
     * @return the response entity
     */
    @Recover
    public CTShoppingCartResponse recoverValidateShoppingCart(RestClientException excp) {
        log.info("EPOCH_REWARD_CALL_FAILED");
        if (excp != null && excp.getRootCause() != null && excp.getRootCause() instanceof SocketTimeoutException) {
            throw new ServiceException(ErrorMessages.CTLG_TIMEOUT_ERROR_UNKNOWN, excp)
                    .addDetail(ErrorMessages.CTLG_TIMEOUT_ERROR_UNKNOWN_DETAILS, EPOCH, Integer.toString(readTimeout));
        }

        throw new ServiceException(ErrorMessages.EXTERNAL_PROCESSING_ERROR, excp).addDetail(
                ErrorMessages.CTLG_WIRELESS_UNHANDLED_EXCEPTION_DETAILS, "Offers ", "CpopClient.recoverValidateShoppingCart");
    }

    /**
     * burnQuotaBasedPromotion through CT.
     *
     * @param request Burn offer request
     * @return Commerce tools response object.
     */
    @Retryable(maxAttemptsExpression = "${apiclient.rest.backupcpop.maxAttempts}", value = {RestClientException.class})
    public CommerceBurnResponse burnQuotaBasedPromotion(OfferBurnRequest request) {
        CommerceBurnRequest.BurnRequest burn = new CommerceBurnRequest.BurnRequest(request.getCode(), 1);
        log.info("Sending burn request to CT: [{}]", burn);
        return makePostCall(burn, CommerceBurnResponse.class, baseUrl, burnEndpoint);
    }

    @Override
    public CTOfferResponse getOffers(CTOfferRequest ctOfferRequest) {
        log.info("Start of CpopClient.getOffers method..[{}]", baseUrl);
        log.info("EPOCH_GETOFFERS_CT_REQUEST-[{}]", JsonService.getJsonFromObject(ctOfferRequest));
        log.debug("End of CpopClient.getOffers method..");
        final String apiPath = "getoffers";

        CTOfferResponse cpopOffersResponse = makePostCall(ctOfferRequest, CTOfferResponse.class, baseUrl, apiPath);
        if (featureManagerHelper.isEnabled("svc-epoch-ade-perf-change")
                && ((Optional.ofNullable(ctOfferRequest.getOfferActionType()).isPresent()
                && !ctOfferRequest.getOfferActionType().contains(Constants.ACQUISITION_ACTION_TYPE))
                && (Optional.ofNullable(ctOfferRequest.getSalesChannel()).isPresent()
                && ctOfferRequest.getSalesChannel().contains("online"))
                && (Optional.ofNullable(ctOfferRequest.getOfferProductFamily()).isPresent()
                && ctOfferRequest.getOfferProductFamily().contains("satellite")))) {
            if (cpopOffersResponse != null && cpopOffersResponse.getTotal() > ctOfferRequest.getPagination().getLimit()) {
                log.debug("Calling ADE asyncApiExecutor");
                cpopOffersResponse = asyncApiExecutorAdeFlow(cpopOffersResponse, ctOfferRequest, apiPath);
            }
        } else {
            if (cpopOffersResponse != null && cpopOffersResponse.getTotal() > pageLimit) {
                log.debug("Calling asyncApiExecutor");
                cpopOffersResponse = asyncApiExecutor(cpopOffersResponse, ctOfferRequest, apiPath);
            }
        }

        /*
         * if(cpopOffersResponse!=null && cpopOffersResponse.getTotal()>pageLimit){
         * log.debug("Calling asyncApiExecutor");
         * cpopOffersResponse=asyncApiExecutor(cpopOffersResponse,
         * ctOfferRequest,apiPath); }
         */
        log.info("EPOCH_OFFERS_CALL_SUCCESS");
        return cpopOffersResponse;

    }

    @Override
    public CTCouponResponse getCoupons(CTCouponsRequest cTCouponsRequest) {
        log.info("Start of CpopClient.getCoupons method with baseUrl..[{}]", baseUrl);
        log.info("EPOCH_COUPON_CALL_CT_REQUEST: [{}]", JsonService.getJsonFromObject(cTCouponsRequest));
        final String apiPath = "discount-codes";
        CTCouponResponse cTCouponResponse = null;
        log.debug("End of CpopClient.getCoupons method..");

        String CouponCode = cTCouponsRequest.getCode();

        JsonNode jsonNode = null;
        try {
            jsonNode = makeGetCall(JsonNode.class, cTBaseUrl,
                    apiPath + "/?where=code=\"" + CouponCode + "\""
            );
        } catch (Exception excep) {
            log.error("EPOCH_PRIMARY_CLIENT_MET_EXCEPTION_AS_URL_COMPROPISED" + excep);
        }

        ObjectMapper mapper = MAPPER;

        cTCouponResponse = mapper.convertValue(jsonNode, new TypeReference<CTCouponResponse>() {
        });
        log.info("EPOCH_COUPON_CALL_SUCCESS");
        return cTCouponResponse;

    }

    @Override
    @Cacheable(value = Constants.EPOCHOFFERSMS_CT_BACKUP_CACHE_MAP_NAME, key = "{ #ctOfferRequest,#idpConfigEnv,'epochgraphqlOffers' }", unless = "#result==null or #result.offers.isEmpty()", cacheManager = "redisCacheManagerSecondHolder")
    public CTOfferResponse executeGraphQLQuery(String ctOfferRequest, String idpConfigEnv) {
        log.info("CT_Cacheput epochOffersMsCTCache3 Start of CpopBACKUPClient.executeGraphQLQuery method..[{}]", baseUrl);
        log.info("EPOCH_GETOFFERS_CT_REQUEST-[{}]", JsonService.getJsonFromObject(ctOfferRequest));
		log.debug("End of CpopBACKUPClient.executeGraphQLQuery method..");
        long startTimeInMillies = System.currentTimeMillis();
		String query = Util.readFileAsString("/ctGraphQLQuery.json", "query");
        //GraphQLResponse serviceResponse = epochGraphQlClient.makeGraphQLCall(ctOfferRequest, query, graphQLUserName, graphQLPassword, graphQlEnv);
        
		GraphQLRequest request = GraphQLRequest.builder().query(query).variables(builder -> builder.addValue("productFilter", ctOfferRequest)).build();
		ApiHttpResponse<GraphQLResponse> response = getProjectApiRootClient().graphql().post(request).executeBlocking();
		GraphQLResponse serviceResponse = response.getBody();
        
        GraphQLDataImpl graphQlImpl = (GraphQLDataImpl) serviceResponse.getData();
        List<Product> products = graphQlImpl.getProducts().getResults();
        log.info("The time taken for getOffers to retrieve GraphQL IDS is ::::" + (System.currentTimeMillis() - startTimeInMillies));
		return graphqlResponseMapping.buildCTOfferResponse(products);
    }

    @Override
    @Cacheable(value = Constants.EPOCH_PRODUCT_TYPES_ATTR_BACKUP_CACHE_MAP_NAME, key = "{#productAttrsType, #idpConfigEnv,'epochProductAttrsTypeConfigs' }", unless = "#result==null", cacheManager = "redisCacheManagerSecondHolder")
    public Map<String, String> loadProductTypeAttrs(String productType, String idpConfigEnv) {
        log.info("Start of CPOP BackUp Client .loadProductTypes method with cTBaseUrl..[{}]", cTBaseUrl);

        final String apiPath = "product-types";
        ProductType productTypeResponse = null;

        JsonNode jsonNode = null;
        try {
            jsonNode = makeCTCustomGetCall(JsonNode.class, cTBaseUrl,
                    apiPath + "/key=" + productType);
        } catch (Exception excep) {
            log.error("EPOCH_BACKUP_CLIENT_MET_EXCEPTION_AS_URL_COMPROPISED" + excep);
        }

        ObjectMapper mapper = MAPPER;

        productTypeResponse = mapper.convertValue(jsonNode, new TypeReference<ProductType>() {
        });
        log.info("EPOCH_LOAD_PRODUCT_TYPES");
		Map<String, String> attrToFieldMap = graphqlResponseMapping.attrToFieldMaps(productTypeResponse.getAttributes());
        return attrToFieldMap;

    }

    public CTOfferResponse executeGraphQLQueryForRequestedAttributes(String ctOfferRequest, List<String> requestedAttributes) {
        log.info("Start of CpopBACKUPClient.executeGraphQLQueryForRequestedAttributes method..[{}]", baseUrl);
        log.info("EPOCH_GETOFFERS_CT_REQUEST-[{}]", JsonService.getJsonFromObject(ctOfferRequest));
        long startTimeInMillies = System.currentTimeMillis();
        String query = Util.readFileAsString("/ctGraphQLQuery.json", "query2");
        query = query.replace("requestedAttributes", requestedAttributes.stream()
                .map(attr -> "\"" + attr + "\"")
                .collect(Collectors.joining(",")));
        GraphQLRequest request = GraphQLRequest.builder().query(query).variables(builder -> builder.addValue("productFilter", ctOfferRequest)).build();
        ApiHttpResponse<GraphQLResponse> response = getProjectApiRootClient().graphql().post(request).executeBlocking();
        GraphQLResponse serviceResponse = response.getBody();
        GraphQLDataImpl graphQlImpl = (GraphQLDataImpl) serviceResponse.getData();
        List<Product> products = graphQlImpl.getProducts().getResults();
        log.info("The time taken for getOffers to retrieve GraphQL IDS is ::::" + (System.currentTimeMillis() - startTimeInMillies));
        log.debug("End of CpopBACKUPClient.executeGraphQLQueryForRequestedAttributes method..");
        return graphqlResponseMapping.buildCTOfferResponse(products);
    }

    @Cacheable(value = Constants.EPOCHOFFERSMS_CT_BENEFITS_BACKUP_CACHE_MAP_NAME, key = "{ #ctBenefitsRequestStr,#idpConfigEnv,'cartDiscounts' }", unless = "#result==null", cacheManager = "redisCacheManagerSecondHolder")
    public CTBenefitsResponse executeGraphQLQueryForCartDiscountRequestedAttributes(String ctBenefitsRequestStr, List<String> requestedAttributes) {
        log.info("Start of CartDiscount CpopBACKUPClient.executeGraphQLQueryForRequestedAttributes method..[{}]", baseUrl);
        CTBenefitsRequest ctBenefitsRequest = JsonService.getObjectFromJson(ctBenefitsRequestStr, CTBenefitsRequest.class);
        List<String> benefitIdsList =  ctBenefitsRequest.getBenefitIds() == null ? new ArrayList<>() : ctBenefitsRequest.getBenefitIds();
        List<String> benefitCodeList = ctBenefitsRequest.getBenefitCodes() == null ? new ArrayList<>() : ctBenefitsRequest.getBenefitCodes();
        long startTimeInMillies = System.currentTimeMillis();
        String benefitsFilter = null;
        if(CollectionUtils.isNotEmpty(benefitCodeList)){
            benefitsFilter = queryBuilder(benefitCodeList, "key");
        }else {
            benefitsFilter = queryBuilder(benefitIdsList, "id");
        }
        String query = Util.readFileAsString("/ctGraphQLQuery.json", "benefitQuery");
        query = query.replace("requestedAttributes", requestedAttributes.stream()
                .map(attr -> "\"" + attr + "\"")
                .collect(Collectors.joining(",")));
        String finalBenefitsFilter = benefitsFilter;
        log.info("Final Benefits Filter for GraphQL Query : {}", finalBenefitsFilter);
        GraphQLRequest request = GraphQLRequest.builder().query(query).variables(builder -> builder.addValue("benefitFilter", finalBenefitsFilter)).build();
        ApiHttpResponse<GraphQLResponse> response = getProjectApiRootClient().graphql().post(request).executeBlocking();
        GraphQLResponse serviceResponse = response.getBody();
        GraphQLDataImpl graphQlImpl = (GraphQLDataImpl) serviceResponse.getData();
        List<CartDiscount> cartDiscounts = graphQlImpl.getCartDiscounts().getResults();
        log.info("The time taken for getBenefits to retrieve GraphQL IDS is ::::" + (System.currentTimeMillis() - startTimeInMillies));
        log.debug("End of CartDiscount CpopBACKUPClient.executeGraphQLQueryForRequestedAttributes method..");
        return graphqlResponseMapping.buildCTBenefitsResponse(cartDiscounts,requestedAttributes);
    }

}


