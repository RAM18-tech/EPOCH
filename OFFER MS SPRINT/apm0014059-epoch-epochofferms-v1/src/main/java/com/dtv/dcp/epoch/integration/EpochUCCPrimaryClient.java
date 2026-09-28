package com.dtv.dcp.epoch.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.dtv.dcp.epoch.model.common.request.ProductRequest;
import com.dtv.dcp.epoch.model.common.response.CampaignProducts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.common.EpochUCCPrimaryBaseClient;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.model.ct.request.CTBenefitsRequest;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.request.Pagination;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.util.JsonService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component("EpochUCCPrimaryClient")
public class EpochUCCPrimaryClient extends EpochUCCPrimaryBaseClient {
	/**
	 * The log.
	 */
	private static final Logger log = LoggerFactory.getLogger(EpochUCCPrimaryClient.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();
	/**
	 * The rest baseUrl.
	 */
	@Value("${apiclient.rest.uccprimary.baseUrl}")
	private String baseUrl;

	@Value("${apiclient.rest.uccprimary.ctoffersms.ct.baseUrl}")
	private String cTBaseUrl;

	/**
	 * The proxyEnabled.
	 */
	@Value("${apiclient.rest.uccprimary.proxyEnabled}")
	private String proxyEnabled;

	/**
	 * The sslEnabled.
	 */
	@Value("${apiclient.rest.uccprimary.sslEnabled:false}")
	private boolean sslEnabled;

	/**
	 * The connectTimeout.
	 */
	@Value("${apiclient.rest.uccprimary.connectTimeout:4000}")
	private int connectTimeout;

	/**
	 * The rest readTimeout.
	 */
	@Value("${apiclient.rest.uccprimary.readTimeout:4000}")
	private int readTimeout;

	/**
	 * The Constant uccprimary.
	 */
	private static final String UCCPRIMARY = "uccprimary";
	/**
	 * The pageLimit.
	 */
	@Value("${pageLimit}")
	private int pageLimit;
	/**
	 * The rest noOfConnections.
	 */
	@Value("${apiclient.rest.uccprimary.noOfConnections}")
	private int noOfConnections;

	/**
	 * The the abstract method getting value of APIKEY.
	 */
	@Override
	protected String getApiKey() {
		return UCCPRIMARY;
	}

	/**
	 * The the abstract method getting value of isProxyEnabled.
	 */
	@Override
	protected boolean isProxyEnabled() {
		return "enabled".equalsIgnoreCase(proxyEnabled);

	}

	/**
	 * The the abstract method getting value of isSSLEnabled.
	 */
	@Override
	protected boolean isSSLEnabled() {
		return sslEnabled;

	}

	/**
	 * The the abstract method getting value of connectTimeout.
	 */
	protected int connectTimeout() {
		return connectTimeout;
	}

	/**
	 * The the abstract method getting value of readTimeout.
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
	
	@Override
	public CTOfferResponse getOffers(CTOfferRequest ctOfferRequest) {
		log.info("Start of EpochUCCPrimaryClient.getOffers method with baseUrl..[{}]", baseUrl);
		log.info("EPOCH_GETOFFERS_CT_REQUEST-[{}]", JsonService.getJsonFromObject(ctOfferRequest));
		final String apiPath = "getoffers";
		CTOfferResponse cpopOffersResponse = makePostCall(ctOfferRequest, CTOfferResponse.class, baseUrl, apiPath);
		if (cpopOffersResponse != null && cpopOffersResponse.getTotal() > pageLimit) {
			log.debug("Calling asyncApiExecutor");
			cpopOffersResponse = asyncApiExecutor(cpopOffersResponse, ctOfferRequest, apiPath);
		}
		log.debug("End of EpochUCCPrimaryClient.getOffers method..");
		log.info("EPOCH_OFFERS_CALL_SUCCESS");
		return cpopOffersResponse;

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
			
			asyncCpopOffersRequest.setState(Objects.nonNull(cpopOffersRequest) ? cpopOffersRequest.getState(): null);
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


	@Override
	public CTBenefitsResponse getBenefits(CTBenefitsRequest ctBenefitsRequest) {
		log.info("Start of EpochUCCPrimaryClient.getBenefits method with baseUrl..[{}]", baseUrl);
		log.info("EPOCH_BENEFITS_CALL_CT_REQUEST: [{}]", JsonService.getJsonFromObject(ctBenefitsRequest));
		final String apiPath = "getbenefits";
		CTBenefitsResponse benefitsResponse = null;
		List<String> benefitIdsList = ctBenefitsRequest.getBenefitIds() == null ? new ArrayList<>()
				: ctBenefitsRequest.getBenefitIds();
		List<String> benefitCodeList = ctBenefitsRequest.getBenefitCodes() == null ? new ArrayList<>()
				: ctBenefitsRequest.getBenefitCodes();

		JsonNode jsonNode = null;
		try {
			jsonNode = makeGetCall(JsonNode.class, baseUrl,
					apiPath + "?benefitIds=".concat(String.join(",", benefitIdsList)).concat("&billingBenefitCodes=")
							.concat(String.join(",", benefitCodeList)).concat("&state=")
							.concat(ctBenefitsRequest.getState()));
		} catch (Exception excep) {
			log.error("EPOCH_PRIMARY_CLIENT_MET_EXCEPTION_AS_URL_COMPROPISED" + excep);
		}

		benefitsResponse = MAPPER.convertValue(jsonNode, new TypeReference<CTBenefitsResponse>() {
		});
		log.info("End of EpochUCCPrimaryClient.getBenefits method..");
		return benefitsResponse;

	}

	public JsonNode getCouponDetails(String couponCode) {
		log.info("Start of EpochUCCPrimaryClient.getCouponDetails method with cTBaseUrl..[{}]", cTBaseUrl);
		log.info("EPOCH_CPOP_PRIMARY_COUPON_CALL_CT_REQUEST: [{}]", JsonService.getJsonFromObject(couponCode));
		final String apiPath = "discount-codes";
		JsonNode jsonNode = null;
		try {
			jsonNode = makeGetCall(JsonNode.class, cTBaseUrl, apiPath + "/?where=code=\"" + couponCode + "\"");
		} catch (Exception excep) {
			log.error("EPOCH_CPOP_PRIMARY_CLIENT_MET_EXCEPTION_AS_URL_COMPROPISED" + excep);
			throw new ServiceException(ErrorMessages.EXTERNAL_PROCESSING_ERROR, excep);
		}
		log.info("End of EpochUCCPrimaryClient.getCouponDetails method..");
		return jsonNode;
	}
	
	public JsonNode getCampaignDetails(JsonNode campaignRequest) {
		log.info("Start of EpochUCCPrimaryClient.getCampaignDetails method with cTBaseUrl..[{}]", baseUrl);
		log.info("EPOCH_CPOP_PRIMARY_CAMPAIGN_CALL_CT_REQUEST: [{}]", JsonService.getJsonFromObject(campaignRequest));
		final String apiPath = "getproducts";
		JsonNode jsonNode = null;
		try {
			jsonNode = makePostCall(campaignRequest, JsonNode.class, baseUrl, apiPath);
		} catch (Exception excep) {
			log.error("EPOCH_CPOP_PRIMARY_CLIENT_MET_EXCEPTION_AS_URL_COMPROPISED" + excep);
		}
		log.info("End of EpochUCCPrimaryClient.getCampaignDetails method..");

		return jsonNode;
	}
	public JsonNode getCouponDetailsWithMultipleCoupons(List<String> couponCode) {
		log.info("Start of EpochUCCPrimaryClient.getCouponDetailsWithMultipleCoupons method with cTBaseUrl..[{}]", cTBaseUrl);
		log.info("EPOCH_CPOP_PRIMARY_COUPON_CALL_CT_REQUEST: [{}]", JsonService.getJsonFromObject(couponCode));
		final String apiPath = "discount-codes";
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("/?where=code in (");
		for (String code : couponCode) {
			stringBuilder.append("\"").append(code).append("\",");
		}
		stringBuilder.deleteCharAt(stringBuilder.length() - 1);
		stringBuilder.append(")");
		JsonNode jsonNode = null;
		try {
			jsonNode = makeGetCall(JsonNode.class, cTBaseUrl, apiPath + stringBuilder.toString());
		} catch (Exception excep) {
			log.error("EPOCH_CPOP_PRIMARY_CLIENT_MET_EXCEPTION_AS_URL_COMPROPISED" + excep);
			throw new ServiceException(ErrorMessages.EXTERNAL_PROCESSING_ERROR, excep);
		}
		log.info("End of EpochUCCPrimaryClient.getCouponDetailsWithMultipleCoupons method..");
		return jsonNode;
	}

	public CampaignProducts getCampaignDetailsList(ProductRequest productRequest) {
		log.info("Start of EpochUCCPrimaryClient.getCampaignDetailsList method with cTBaseUrl..[{}]", baseUrl);
		log.info("EPOCH_CPOP_PRIMARY_CAMPAIGN_CALL_CT_REQUEST: [{}]", JsonService.getJsonFromObject(productRequest));
		final String apiPath = "getproducts";
		CampaignProducts campaignProducts = null;
		try {
			campaignProducts = makePostCall(productRequest, CampaignProducts.class, baseUrl, apiPath);
		} catch (Exception excep) {
			log.error("EPOCH_CPOP_PRIMARY_CLIENT_MET_EXCEPTION_AS_URL_COMPROPISED" + excep);
		}
		log.info("End of EpochUCCPrimaryClient.getCampaignDetailsList method..");

		return campaignProducts;
	}

}
