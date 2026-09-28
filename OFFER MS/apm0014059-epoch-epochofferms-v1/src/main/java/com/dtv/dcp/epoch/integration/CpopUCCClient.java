package com.dtv.dcp.epoch.integration;


import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.integration.common.CpopUCCBaseClient;
import com.dtv.dcp.epoch.integration.common.oauthtoken.OAuthUCCTokenService;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.model.ct.request.CTCouponsRequest;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.request.Pagination;
import com.dtv.dcp.epoch.model.ct.response.CTCouponResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.util.JsonService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by nk3077 on 03/21/2019.
 */

@Component("CpopUCCClient")
public class CpopUCCClient extends CpopUCCBaseClient {

	/**
	 * The log.
	 */
	private static final Logger log = LoggerFactory.getLogger(CpopUCCClient.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();

	/**
	 * The rest baseUrl.
	 */
	@Value("${apiclient.rest.ucc.baseUrl}")
	private String baseUrl;
	
	/**
	 * CT API base URL
	 */
	@Value("${apiclient.rest.ucc.ctoffersms.ct.baseUrl}")
	private String cTBaseUrl;

	/**
	 * The  proxyEnabled.
	 */
	@Value("${apiclient.rest.ucc.proxyEnabled}")
	private String proxyEnabled;

	/**
	 * The  sslEnabled.
	 */
	@Value("${apiclient.rest.ucc.sslEnabled:false}")
	private boolean sslEnabled;

	/**
	 * The connectTimeout.
	 */
	@Value("${apiclient.rest.ucc.connectTimeout:4000}")
	private int connectTimeout;

	/**
	 * The rest readTimeout.
	 */
	@Value("${apiclient.rest.ucc.readTimeout:4000}")
	private int readTimeout;

	/**
	 * The Constant epoch.
	 */
	private static final String EPOCH = "epoch";
	/**
	 * The pageLimit.
	 */
	@Value("${pageLimit}")
	private int pageLimit;
	/**
	 * The rest noOfConnections.
	 */
	@Value("${apiclient.rest.ucc.noOfConnections}")
	private int noOfConnections;
	
	
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
				ErrorMessages.CPOP_OFFER_EXTERNAL_API_CALL_TIME_OUT, "Offers ", "CpopUCCClient.getOffers");
	}


	public CTOfferResponse asyncApiExecutor(CTOfferResponse cpopOffersResponse, CTOfferRequest cpopOffersRequest, String api) {
		log.debug("Start of CpopUCCClient.asyncApiExecutor method..");
		List<CompletableFuture<CTOfferResponse>> cfList = new ArrayList<>();
		int pages = cpopOffersResponse.getTotal() / pageLimit + 1;

		for (int i = 2; i <= pages; i++) {
			CTOfferRequest asyncCpopOffersRequest = new CTOfferRequest();
			if (Objects.nonNull(cpopOffersRequest)) {
				BeanUtils.copyProperties(cpopOffersRequest, asyncCpopOffersRequest);
			}

			asyncCpopOffersRequest.setState(Objects.nonNull(cpopOffersRequest) ? cpopOffersRequest.getState():null);
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
		log.debug("End of CpopUCCClient.asyncApiExecutor method..");
		return finalCpopOffersResponse;
	}

	@Override
	public CTOfferResponse getOffers(CTOfferRequest ctOfferRequest) {
		log.info("Start of CpopUCCClient.getOffers method..[{}]", baseUrl);
		log.info("EPOCH_GETOFFERS_CT_REQUEST-[{}]", JsonService.getJsonFromObject(ctOfferRequest));
		log.debug("End of CpopUCCClient.getOffers method..");
		final String apiPath = "getoffers";

		CTOfferResponse cpopOffersResponse=makePostCall(ctOfferRequest, CTOfferResponse.class, baseUrl, apiPath);
		if(cpopOffersResponse!=null && cpopOffersResponse.getTotal()>pageLimit){
			log.debug("Calling asyncApiExecutor");
			cpopOffersResponse=asyncApiExecutor(cpopOffersResponse, ctOfferRequest,apiPath);
		}
		log.info("EPOCH_OFFERS_CALL_SUCCESS");
		return cpopOffersResponse;

	}

	@Override
	public CTCouponResponse getCoupons(CTCouponsRequest cTCouponsRequest) {
		log.info("Start of CpopUCCClient.getCoupons method with baseUrl..[{}]", baseUrl);
		log.info("EPOCH_COUPON_CALL_CT_REQUEST: [{}]", JsonService.getJsonFromObject(cTCouponsRequest));
		final String apiPath = "discount-codes";
		CTCouponResponse cTCouponResponse = null;
		log.debug("End of CpopUCCClient.getCoupons method..");

		String couponCode = cTCouponsRequest.getCode();

		JsonNode jsonNode = null;
		try {
			jsonNode = makeGetCall(JsonNode.class, cTBaseUrl, apiPath + "/?where=code=\"" + couponCode + "\"");
		} catch (Exception excep) {
			log.error("EPOCH_PRIMARY_CLIENT_MET_EXCEPTION_AS_URL_COMPROPISED" + excep);
			throw new ServiceException(ErrorMessages.EXTERNAL_PROCESSING_ERROR, excep);
		}

		cTCouponResponse = MAPPER.convertValue(jsonNode, new TypeReference<CTCouponResponse>() {
		});
		log.info("EPOCH_COUPON_CALL_SUCCESS");
		return cTCouponResponse;

	}
}
