package com.dtv.dcp.epoch.resource;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.model.ct.burn.BurnPromotionResponse;
import com.dtv.dcp.epoch.model.ct.burn.BurnResult;
import com.dtv.dcp.epoch.model.ct.burn.CommerceBurnResponse;
import com.dtv.dcp.epoch.model.ct.burn.Content;
import com.dtv.dcp.epoch.model.ct.burn.OfferBurnRequest;
import com.dtv.dcp.epoch.model.ct.burn.OfferBurnRequestWrapper;
import com.dtv.dcp.epoch.model.ct.burn.OfferBurnResponse;
import com.dtv.dcp.epoch.processor.helper.CPOPAdditionalOfferHelper;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.OffersUtils;

@Controller
public class BurnOfferResourceImpl implements BurnOfferResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(BurnOfferResourceImpl.class);
    private static final String ERROR_MESSAGE = "Unknown error ocuured in BurnOfferResourceImpl.burnReward() method.";
    private static final String ERROR_CODE_QUOTA_REACHED = "eV8353";
    private static final String ERROR_CODE_INVALID_BENEFIT = "eV8354";
    private static final String ERROR_CODE_BENEFIT_NOT_EXIST = "eV8357";
    private final CpopClient client;
    private final CPOPAdditionalOfferHelper additionalOfferHelper;
	private final FeatureManagerHelper featureManagerHelper;
	private static final String ERROR="error";

    public BurnOfferResourceImpl(CpopClient client, CPOPAdditionalOfferHelper additionalOfferHelper, FeatureManagerHelper featureManagerHelper) {
        this.client = client;
        this.additionalOfferHelper = additionalOfferHelper;
        this.featureManagerHelper = featureManagerHelper;
    }

    @Override
    public OfferBurnResponse burnReward(HttpHeaders headers, @NotNull OfferBurnRequestWrapper wrapper) {
    	MDC.put(Constants.TRACE_ID, null != headers ? headers.getFirst(Constants.TRACE_ID):"");
    	LOGGER.info("Received request: [{}]", wrapper);
        OfferBurnResponse response = new OfferBurnResponse();
        Content content = new Content(new ArrayList<>());
        try {
        FeatureManagerHelper.httpHeaders = headers;
        if (Objects.nonNull(wrapper.getPromotions()) && !wrapper.getPromotions().isEmpty()) {
            wrapper.getPromotions().forEach(promo -> {
                if (invalidRequest(promo)) {
                	content.getPromotions().add(new BurnPromotionResponse(
                            Objects.nonNull(promo.getId())
                                    ? promo.getId()
                                    : "INVALID_ID",
                            Objects.nonNull(promo.getCode())
                                    ? promo.getCode()
                                    : "INVALID_BENEFIT_CODE",
                            new BurnResult(ERROR, "OFFER_ERR_INV_REQUEST", "Invalid Request, Null Request")
                    ));
                } else {
                    String session = headers.getFirst(Constants.IDPCTX_SESSION_ID);
                    String uuId = (headers.getFirst(Constants.IDPCTX_UUID) == null) ? "" : headers.getFirst(Constants.IDPCTX_UUID);
                    String orderNumber = (wrapper.getOrderNumber() == null) ? "" : wrapper.getOrderNumber();
                    Map<String, Boolean> quotaBasedOffers = additionalOfferHelper.getQuotaBaseOfferMapFromCache(session,featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)?"backup":"primary");
                    LOGGER.info("QuotaBase promotion present in Cache: [{}]", Objects.nonNull(quotaBasedOffers) && quotaBasedOffers.containsKey(promo.getCode()));
                        CommerceBurnResponse burnResponse = client.burnQuotaBasedPromotion(promo);                                
                        if (Objects.isNull(burnResponse.getError())) {
                        	content.getPromotions().add(new BurnPromotionResponse(promo.getId(), promo.getCode(),
                                    new BurnResult("success", null, null)));
                            LOGGER.info("Successfully burn Offer for OrderNumber :{} and UUID:{}",OffersUtils.sanitizeData(orderNumber),OffersUtils.sanitizeData(uuId));
                        } else {
                        		String errorCode = "";
                                if (ERROR_CODE_QUOTA_REACHED.equalsIgnoreCase(burnResponse.getError().getErrorId())) {
                                	content.getPromotions().add(new BurnPromotionResponse(promo.getId(), promo.getCode(),
                                            new BurnResult("warning", "OFFER_ERR_QUOTA_REACH_REWARD", "The reward quota has reached")));
                                	errorCode = ERROR_CODE_QUOTA_REACHED;
                                
                                } else if (ERROR_CODE_INVALID_BENEFIT.equalsIgnoreCase(burnResponse.getError().getErrorId())) {
                                	content.getPromotions().add(new BurnPromotionResponse(promo.getId(), promo.getCode(),
                                            new BurnResult(ERROR, "OFFER_ERROR_VALIDATION_FAILED", burnResponse.getError().getMessage())));
                                	errorCode = ERROR_CODE_INVALID_BENEFIT;
                                	
                                } else if (ERROR_CODE_BENEFIT_NOT_EXIST.equalsIgnoreCase(burnResponse.getError().getErrorId())) {
                                	content.getPromotions().add(new BurnPromotionResponse(promo.getId(), promo.getCode(),
                                            new BurnResult(ERROR, "OFFER_ERROR_VALIDATION_FAILED", burnResponse.getError().getMessage())));
                                	errorCode = ERROR_CODE_BENEFIT_NOT_EXIST;
                                	
                                } else {
                                	content.getPromotions().add(new BurnPromotionResponse(promo.getId(), promo.getCode(),
                                            new BurnResult(ERROR, "OFFER_ERROR", "System Error")));
                                	errorCode = ERROR_CODE_BENEFIT_NOT_EXIST;
                                	
                                }
                                LOGGER.info("Failed Burn Offer for OrderNumber :{} and UUID:{} with ERROR_CODE:{}",OffersUtils.sanitizeData(orderNumber),
                                		OffersUtils.sanitizeData(uuId),OffersUtils.sanitizeData(errorCode));
                        }
                }
            });
         response.setContent(content);
        }
        }catch(Exception ex) {
        	LOGGER.error(ERROR_MESSAGE, ex);
        	LOGGER.error("EPOCH_BURNREWARD_FAILED-[{}]", ERROR_MESSAGE);
        	throw ex;     
        }
        return response;

    }

    private boolean invalidRequest(OfferBurnRequest request) {
        if (Objects.isNull(request)) {
            return true;
        }
        return Objects.isNull(request.getCode())
                || Objects.isNull(request.getId())
                || !StringUtils.hasText(request.getCode())
                || !StringUtils.hasText(request.getId());
    }
}