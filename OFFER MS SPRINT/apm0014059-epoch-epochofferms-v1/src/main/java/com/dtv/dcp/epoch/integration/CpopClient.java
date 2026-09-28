package com.dtv.dcp.epoch.integration;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.model.common.response.CpopIXPResponse;
import com.dtv.dcp.epoch.model.ct.burn.CommerceBurnResponse;
import com.dtv.dcp.epoch.model.ct.burn.OfferBurnRequest;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.offer.GlobalEligibilityRule;
import com.dtv.dcp.epoch.model.ct.request.CTBenefitsRequest;
import com.dtv.dcp.epoch.model.ct.request.CTCouponsRequest;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.dtv.dcp.epoch.model.ct.request.CTShoppingCartRequest;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.model.ct.response.CTCouponResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.model.ct.response.CTShoppingCartResponse;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.JsonService;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by nk3077 on 03/21/2019.
 */

@Component("CpopClient")
public class CpopClient {
	
	  /**
     * The log.
     */
    private static final Logger log = LoggerFactory.getLogger(CpopClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);

    @Autowired
	CpopBackUpClient cpopBackUpClient;
	
	@Autowired
	CpopPrimaryClient cpopPrimaryClient;	
	
	@Autowired
	EpochDataloadClient epochDataloadClient;
	
	@Autowired
	CpopPVTClient cpopPVTClient;
	
	@Autowired
	CpopClientHelper cpopClientHelper;
	
	@Autowired
	BuildMiddlewareCTFilters middlewareFilters;
	
	@Value("${dcp.config.env}")
	private String idpConfigEnv;
	
	@Autowired
	private FeatureManagerHelper featureManagerHelper;
	
	/** The  benefitsCodesToSuppressOffer. */
	@Value("${benefitsCodesToSuppressOffer}")
	private String benefitsCodesToSuppressOffer;
	
	@Autowired
    private CpopIxpClient ixpClient;
	
	@Autowired
	OffersUtils offersUtils;
	

	
	public CTOfferResponse getOffers(CTOfferRequest ctOfferRequest) {
		CTOfferResponse ctResponse = null;
		if (isEligibleToCallCache(ctOfferRequest)) {
			log.info("Call getOffersFromCache");
			ctResponse= getOffersFromCache(ctOfferRequest);
		} else {
			log.info("Call getOffersFromCT");
			ctResponse= getOffersFromCT(ctOfferRequest);
		}
		if(Objects.nonNull(ctResponse) && Objects.nonNull(ctResponse.getOffers())) {
			filterEmployeeOffers(ctResponse, ctOfferRequest);
		}
		return ctResponse;
	}
	

	public CTOfferResponse getOffersFromCT(CTOfferRequest ctOfferRequest) {
		CTOfferResponse ctResponse = null;
		if (cpopClientHelper.isPVTEnabled()) {
			log.info("PVT getOffers Call ");
			CTOfferRequest newCTOfferRequest=cpopClientHelper.cleanCTOfferRequest(ctOfferRequest);
			return cpopPVTClient.getOffers(newCTOfferRequest);
		} else {
			if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)) {
				CTOfferRequest newCTOfferRequest=cpopClientHelper.cleanCTOfferRequest(ctOfferRequest);
				log.info("BackUp getOffers Call ");
				return cpopBackUpClient.getOffers(newCTOfferRequest);
			} else {
				log.info("Primary getOffers Call ");
				long startTimeMillis = System.currentTimeMillis();
				CTOfferRequest newCTOfferRequest=cpopClientHelper.cleanCTOfferRequest(ctOfferRequest);
				ctResponse =  cpopPrimaryClient.getOffers(newCTOfferRequest);
				log.info("TOTAL_TIME_TAKEN_FROM_CTMS-[{}]", System.currentTimeMillis()-startTimeMillis);
				return ctResponse;
			}
		}
	}
	
	public CTOfferResponse getOffersFromCache(CTOfferRequest ctOfferRequest) {
		
        CTOfferRequest newCTtOfferRequest = new CTOfferRequest();
        BeanUtils.copyProperties(ctOfferRequest, newCTtOfferRequest);
        
        boolean isGetOffersSatellite = false;
        if(CollectionUtils.isNotEmpty(ctOfferRequest.getOfferActionType())
				&& CollectionUtils.isNotEmpty(ctOfferRequest.getOfferProductType())
				&& CollectionUtils.isNotEmpty(ctOfferRequest.getOfferProductFamily())
				&& ctOfferRequest.getOfferProductFamily().contains(Constants.SATELLITE_PRODUCT_FAMILY)) {
        	isGetOffersSatellite = true;
        }

		boolean isRewardRequestWithZipNDMA =false;

		if (CollectionUtils.isNotEmpty(ctOfferRequest.getOfferActionType())
				&& CollectionUtils.isNotEmpty(ctOfferRequest.getOfferProductType())
				&& ctOfferRequest.getOfferProductType().contains(Constants.REWARD)
				&& CollectionUtils.isNotEmpty(ctOfferRequest.getOfferProductFamily())
				&& ctOfferRequest.getOfferProductFamily().contains(Constants.OTT_PRODUCT_FAMILY)
				&& Objects.nonNull(ctOfferRequest.getCustomerEligibility())
				&& (CollectionUtils.isNotEmpty(ctOfferRequest.getCustomerEligibility().getZipCode())
				|| CollectionUtils.isNotEmpty(ctOfferRequest.getCustomerEligibility().getDma()))) {
			isRewardRequestWithZipNDMA = true;
		}
        
        if(!isZipCodeAvailable(ctOfferRequest) 
        		&& !isGetOffersSatellite && !isRewardRequestWithZipNDMA){
    		ctOfferRequest.setCustomerEligibility(null);
        }
        
		ctOfferRequest.setBenefitsCodesToSuppressOffer(null);
		ctOfferRequest.setBenefitCodesToSuppressTheOffer(null);
//		ctOfferRequest.setProductsOnAccountToSuppressOffer(null);
		String ctReqString=JsonService.getJsonFromObject(ctOfferRequest);
		CTOfferResponse ctResponse = null;
		if(cpopClientHelper.isPVTEnabled()) {
			log.info("PVT getOffersFromCache Call ");
			long startTimeMillis = System.currentTimeMillis();
            String json;
			try {
				json = MAPPER.writeValueAsString(ctOfferRequest);
				CTOfferRequest newCTOfferRequest=cpopClientHelper.cleanCTOfferRequest(ctOfferRequest);
				ctReqString=JsonService.getJsonFromObject(newCTOfferRequest);
				log.info("CT-JSON-REQUEST {}",json);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ctResponse=cpopPVTClient.getOffers(ctReqString,idpConfigEnv);
			log.info("TOTAL_TIME_TAKEN_FROM_REDIS_PVT_CACHE-[{}]", System.currentTimeMillis()-startTimeMillis);
		}else {		
			if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)) {
				log.info("BackUp getOffersFromCache Call ");
				long startTimeMillis = System.currentTimeMillis();
				CTOfferRequest newCTOfferRequest=cpopClientHelper.cleanCTOfferRequest(ctOfferRequest);
				ctReqString=JsonService.getJsonFromObject(newCTOfferRequest);
				ctResponse=cpopBackUpClient.getOffers(ctReqString,idpConfigEnv);
				log.info("TOTAL_TIME_TAKEN_FROM_REDIS_BACKUP_CACHE-[{}]", System.currentTimeMillis()-startTimeMillis);
			} else {
				log.info("Primary getOffersFromCache Call ");
				long startTimeMillis = System.currentTimeMillis();
				CTOfferRequest newCTOfferRequest=cpopClientHelper.cleanCTOfferRequest(ctOfferRequest);
				ctReqString=JsonService.getJsonFromObject(newCTOfferRequest);
				ctResponse = cpopPrimaryClient.getOffers(ctReqString,idpConfigEnv);
				log.info("TOTAL_TIME_TAKEN_FROM_REDIS_PRIMARY_CACHE-[{}]", System.currentTimeMillis()-startTimeMillis);
			}
		}
		if (ctResponse != null) {
			ctResponse = filterBenefitCodeSuppressOffer(ctResponse, newCTtOfferRequest);
//			if(!cpopClientHelper.isPVTEnabled()) {
//				middlewareFilters.evaluateBenefitsCodestoSuppressTheOffer(ctResponse, newCTtOfferRequest);
//			}
			//ctResponse = cpopClientHelper.filterProductsOnAccountToSuppressOffer(ctResponse, newCTtOfferRequest);
		}
		return ctResponse;
	}
	
	public CTProductResponse getProductsFromCache(CTProductRequest ctProductRequest) {
		String ctReqString = JsonService.getJsonFromObject(ctProductRequest);
		CTProductResponse ctProductResponse = null;
		if (cpopClientHelper.isPVTEnabled()) {
			log.info("PVT getProductsFromCache Call ");
			long startTimeMillis = System.currentTimeMillis();
			ctProductResponse = cpopPVTClient.getProducts(ctReqString, idpConfigEnv);
			log.info("TOTAL_TIME_TAKEN_FROM_REDIS_PVT_CACHE-[{}]", System.currentTimeMillis() - startTimeMillis);
		} else {
			if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)) {
				log.info("BackUp getProductsFromCache Call ");
				long startTimeMillis = System.currentTimeMillis();
				ctProductResponse = cpopBackUpClient.getProducts(ctReqString, idpConfigEnv);
				log.info("TOTAL_TIME_TAKEN_FROM_REDIS_BACKUP_CACHE-[{}]", System.currentTimeMillis() - startTimeMillis);
			} else {
				log.info("Primary getProductsFromCache Call ");
				long startTimeMillis = System.currentTimeMillis();
				ctProductResponse = cpopPrimaryClient.getProducts(ctReqString, idpConfigEnv);
				log.info("TOTAL_TIME_TAKEN_FROM_REDIS_PRIMARY_CACHE-[{}]", System.currentTimeMillis() - startTimeMillis);
			}
		}
		return ctProductResponse;

	}
	
	public CTProductResponse getProductsByTypeFromCache(String ctReqString) {
		CTProductResponse ctProductResponse = null;
		if (cpopClientHelper.isPVTEnabled()) {
			log.info("PVT getProductsByTypeFromCache Call ");
			long startTimeMillis = System.currentTimeMillis();
			ctProductResponse = cpopPVTClient.getProductsByType(ctReqString, idpConfigEnv);
			log.info("TOTAL_TIME_TAKEN_FROM_REDIS_PVT_CACHE-[{}]", System.currentTimeMillis() - startTimeMillis);
		} else {
			if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)) {
				log.info("BackUp getProductsByTypeFromCache Call ");
				long startTimeMillis = System.currentTimeMillis();
				ctProductResponse = cpopBackUpClient.getProductsByType(ctReqString, idpConfigEnv);
				log.info("TOTAL_TIME_TAKEN_FROM_REDIS_BACKUP_CACHE-[{}]", System.currentTimeMillis() - startTimeMillis);
			} else {
				log.info("Primary getProductsByTypeFromCache Call ");
				long startTimeMillis = System.currentTimeMillis();
				ctProductResponse = cpopPrimaryClient.getProductsByType(ctReqString, idpConfigEnv);
				log.info("TOTAL_TIME_TAKEN_FROM_REDIS_PRIMARY_CACHE-[{}]", System.currentTimeMillis() - startTimeMillis);
			}
		}
		return ctProductResponse;

	}
	
	public CTBenefitsResponse getBenefits(CTBenefitsRequest ctBenefitsRequest) {
		CTBenefitsResponse ctResponse = null;
		if (featureManagerHelper.isEnabled(Constants.SVC_BENEFITS_CACHE_FLAG)) {
			log.info("Call getBenefitsFromCache");
			ctResponse = getBenefitsFromCache(ctBenefitsRequest);
		} else {
			log.info("Call getBenefitsFromCT");
			ctResponse = getBenefitsFromCT(ctBenefitsRequest);
		}
		return ctResponse;

	}
	
	public CTBenefitsResponse getBenefitsFromCT(CTBenefitsRequest ctBenefitsRequest) {
		if(cpopClientHelper.isPVTEnabled()) {
			log.info("PVT getBenefits Call ");
			return cpopPVTClient.getBenefits(ctBenefitsRequest);
		}else {
			if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)) {
				log.info("BackUp getBenefits Call ");
				return cpopBackUpClient.getBenefits(ctBenefitsRequest);
			} else {
				log.info("Primary getBenefits Call ");
				return cpopPrimaryClient.getBenefits(ctBenefitsRequest);
			}
		}
	}

    public CTBenefitsResponse getBenefitsFromGraphlQL(CTBenefitsRequest ctBenefitsRequest, List<String> attributeName) {
        String ctBenefitsRequestStr = JsonService.getJsonFromObject(ctBenefitsRequest);
        if (cpopClientHelper.isPVTEnabled()) {
            log.info("PVT getBenefitsFromGraphlQL Call ");
            return cpopPVTClient.executeGraphQLQueryForCartDiscountRequestedAttributes(ctBenefitsRequestStr, attributeName);
        } else {
            if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)) {
                log.info("BackUp getBenefitsFromGraphlQL Call ");
                return cpopBackUpClient.executeGraphQLQueryForCartDiscountRequestedAttributes(ctBenefitsRequestStr, attributeName);
            } else {
                log.info("Primary getBenefitsFromGraphlQL Call ");
                return cpopPrimaryClient.executeGraphQLQueryForCartDiscountRequestedAttributes(ctBenefitsRequestStr, attributeName);
            }
        }
    }
	
	public CTBenefitsResponse getBenefitsFromCache(CTBenefitsRequest ctBenefitsRequest) {
		String ctReqString = JsonService.getJsonFromObject(ctBenefitsRequest);
		CTBenefitsResponse ctBenefitsResponse = null;
		if (cpopClientHelper.isPVTEnabled()) {
			log.info("PVT getBenefitsFromCache Call ");
			long startTimeMillis = System.currentTimeMillis();
			ctBenefitsResponse = cpopPVTClient.getBenefits(ctReqString, idpConfigEnv);
			log.info("TOTAL_TIME_TAKEN_FROM_REDIS_PVT_CACHE-[{}]", System.currentTimeMillis() - startTimeMillis);
		} else {
			if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)) {
				log.info("BackUp getBenefitsFromCache Call ");
				long startTimeMillis = System.currentTimeMillis();
				ctBenefitsResponse = cpopBackUpClient.getBenefits(ctReqString, idpConfigEnv);
				log.info("TOTAL_TIME_TAKEN_FROM_REDIS_BACKUP_CACHE-[{}]", System.currentTimeMillis() - startTimeMillis);
			} else {
				log.info("Primary getBenefitsFromCache Call ");
				long startTimeMillis = System.currentTimeMillis();
				ctBenefitsResponse = cpopPrimaryClient.getBenefits(ctReqString, idpConfigEnv);
				log.info("TOTAL_TIME_TAKEN_FROM_REDIS_PRIMARY_CACHE-[{}]", System.currentTimeMillis() - startTimeMillis);
			}
		}
		return ctBenefitsResponse;

	}
	public CTBenefitsResponse getSatelliteBenefits(CTBenefitsRequest ctBenefitsRequest) {
		log.info("Dataload getSatelliteBenefits Call ");
		return epochDataloadClient.getSatelliteBenefits(ctBenefitsRequest);
	}
	
	public CTCouponResponse getCoupons(CTCouponsRequest cTCouponsRequest) {
		if(cpopClientHelper.isPVTEnabled()) {
			log.info("PVT getBenefits Call ");
			return cpopPVTClient.getCoupons(cTCouponsRequest);
		}else {
			if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)) {
				log.info("BackUp getBenefits Call ");
				return cpopBackUpClient.getCoupons(cTCouponsRequest);
			} else {
				log.info("Primary getBenefits Call ");
				return cpopPrimaryClient.getCoupons(cTCouponsRequest);
			}
		}
	}

	public CTProductResponse getProductsCT(CTProductRequest ctProductRequest) {
		if(cpopClientHelper.isPVTEnabled()) {
			log.info("PVT getBenefits Call ");
			return cpopPVTClient.getProducts(ctProductRequest);
		}else {
			if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)) {
				log.info("BackUp getProducts Call ");
				return cpopBackUpClient.getProducts(ctProductRequest);
			} else {
				log.info("Primary getProducts Call ");
				return cpopPrimaryClient.getProducts(ctProductRequest);
			}	
		}
	}
	
	public CTProductResponse getProducts(CTProductRequest ctProductRequest) {
		CTProductResponse ctResponse = null;
		if (featureManagerHelper.isEnabled(Constants.SVC_PRODUCTS_CACHE_FLAG)) {
			log.info("Call getProductsFromCache");
			ctResponse = getProductsFromCache(ctProductRequest);
		} else {
			log.info("Call getProductsCT");
			ctResponse = getProductsCT(ctProductRequest);
		}
		return ctResponse;

	}
	
	public CTProductResponse getProductsByTypeFromCT(String productType) {
		if (cpopClientHelper.isPVTEnabled()) {
			log.info("PVT getProductsByType Call ");
			return cpopPVTClient.getProductsByType(productType);
		} else {
			if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)) {
				log.info("BackUp getProductsByType Call ");
				return cpopBackUpClient.getProductsByType(productType);
			} else {
				log.info("Primary getProductsByType Call ");
				return cpopPrimaryClient.getProductsByType(productType);
			}
		}
	}
	
	public CTProductResponse getProductsByType(String productType) {
		CTProductResponse ctResponse = null;
		if (featureManagerHelper.isEnabled(Constants.SVC_PRODUCTS_CACHE_FLAG)) {
			log.info("Call getProductsByTypeFromCache");
			ctResponse = getProductsByTypeFromCache(productType);
		} else {
			log.info("Call getProductsByTypeFromCT");
			ctResponse = getProductsByTypeFromCT(productType);
		}
		return ctResponse;

	}
	
	public CTShoppingCartResponse validateShoppingCart(CTShoppingCartRequest ctShoppingCartRequest, boolean validateShoppingCartFlag) {
		if(cpopClientHelper.isPVTEnabled()) {
			log.info("PVT validateShoppingCart Call ");
			//Updated
			return cpopPVTClient.validateShoppingCart(ctShoppingCartRequest, validateShoppingCartFlag);
		}else {
			if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)) {
				log.info("BackUp validateShoppingCart Call ");
				return cpopBackUpClient.validateShoppingCart(ctShoppingCartRequest, validateShoppingCartFlag);
			} else {
				log.info("Primary validateShoppingCart Call ");
				return cpopPrimaryClient.validateShoppingCart(ctShoppingCartRequest, validateShoppingCartFlag);
			}
		}
	}


	public CommerceBurnResponse burnQuotaBasedPromotion(OfferBurnRequest request) {
		if(cpopClientHelper.isPVTEnabled()) {
			log.info("PVT burnQuotaBasedPromotion Call ");
			return cpopPVTClient.burnQuotaBasedPromotion(request);
		}else {
		 if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)) {
				log.info("BackUp burnQuotaBasedPromotion Call ");
				return cpopBackUpClient.burnQuotaBasedPromotion(request);
			} else {
				log.info("Primary burnQuotaBasedPromotion Call ");
				return cpopPrimaryClient.burnQuotaBasedPromotion(request);
			}
		}
	}
	
	/**
	 * @param ctOfferRequest
	 * @return
	 */
	private boolean isEligibleToCallCacheV2(CTOfferRequest ctOfferRequest) {
		boolean isEligible = true;
		if (CollectionUtils.isNotEmpty(ctOfferRequest.getOfferIds()) || CollectionUtils.isNotEmpty(ctOfferRequest.getOfferCodes())) {
			isEligible = false;
		}
		return isEligible;
	}
	
	/**
	 * @param ctOfferRequest
	 * @return
	 */
	private boolean isEligibleToCallCache(CTOfferRequest ctOfferRequest) {
		if (featureManagerHelper.isEnabled(Constants.FEATURE_SVC_MIDDLEWARECACHE_ENABLED)) {
			return isEligibleToCallCacheV2(ctOfferRequest);
		}else {
			boolean isEligible = false;
			if ((CollectionUtils.isEmpty(ctOfferRequest.getOfferIds()) && CollectionUtils.isEmpty(ctOfferRequest.getOfferCodes())) && CollectionUtils.isNotEmpty(ctOfferRequest.getOfferActionType())
					&& ctOfferRequest.getOfferActionType().contains(Constants.ACQUISITION_ACTION_TYPE)
					&& CollectionUtils.isNotEmpty(ctOfferRequest.getSalesChannel()) 
					&& ctOfferRequest.getSalesChannel().stream().anyMatch(s -> OffersUtils.checkOnlineRelatedChannel(s) 
							|| OffersUtils.checkAgentIndirectChannels(s, ctOfferRequest.getOfferProductFamily()))
					&& featureManagerHelper.isEnabled(Constants.SVC_EPOCH_INTERNAL)) {
				isEligible = true;
			} else if (CollectionUtils.isEmpty(ctOfferRequest.getOfferCodes()) && CollectionUtils.isNotEmpty(ctOfferRequest.getOfferActionType())
					&& ctOfferRequest.getOfferActionType().contains(Constants.ACQUISITION_ACTION_TYPE)
					&& CollectionUtils.isNotEmpty(ctOfferRequest.getSalesChannel()) && (ctOfferRequest.getSalesChannel().contains(Constants.OPUS) || ctOfferRequest.getSalesChannel().contains(Constants.DTV360) || ctOfferRequest.getSalesChannel().contains(Constants.UVC))
					&& featureManagerHelper.isEnabled(Constants.SVC_EPOCH_OPUS_INTERNAL)) {
				isEligible = true;
			} else if (CollectionUtils.isEmpty(ctOfferRequest.getOfferCodes()) && CollectionUtils.isNotEmpty(ctOfferRequest.getOfferActionType())
					&& !ctOfferRequest.getOfferActionType().contains(Constants.ACQUISITION_ACTION_TYPE)
					&& featureManagerHelper.isEnabled(Constants.SVC_EPOCH_SERVICES_INTERNAL)) {
				isEligible = true;
			}
			if(offersUtils.isPerformanceUpdatesEnabled(ctOfferRequest.getSalesChannel()) && featureManagerHelper.isEnabled(Constants.SVC_EPOCH_INTERNAL)) {
				isEligible = true;
			}
			if (CollectionUtils.isNotEmpty(ctOfferRequest.getOfferActionType())
					&& ctOfferRequest.getOfferActionType().contains(Constants.RETENTION_ACTION_TYPE) && CollectionUtils.isNotEmpty(ctOfferRequest.getBenefitCodesToSuppressTheOffer())){
				isEligible = false;
			}
			if (CollectionUtils.isNotEmpty(ctOfferRequest.getBenefitCodesToSuppressTheOffer())){
				isEligible = false;
			}
			if (CollectionUtils.isNotEmpty(ctOfferRequest.getOfferType()) && ctOfferRequest.getOfferType().contains(Constants.COUPON) 
					&& featureManagerHelper.isEnabled(Constants.SVC_EPOCH_UCC_ENABLED)){
				isEligible = false;
			}
			if(isZipCodeAvailable(ctOfferRequest) && featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_ZIP_CACHING_ENABLED)){
				isEligible = true;
			}else if(isZipCodeAvailable(ctOfferRequest) && !featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_ZIP_CACHING_ENABLED)){
				isEligible = false;
			}
			return isEligible;
		}
	}

	/**
	 * @param ctResponse
	 * @param ctOfferRequest
	 */
	public CTOfferResponse filterBenefitCodeSuppressOffer(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {
		List<CTOffer> removeOffers = new ArrayList<>();
		List<CTOffer> offers = ctResponse.getOffers();
		if (Optional.ofNullable(offers).isPresent()) {
			offers.stream().filter(Objects::nonNull).forEach(offer -> {
				if (CollectionUtils.isNotEmpty(ctOfferRequest.getBenefitsCodesToSuppressOffer()) && offer != null
						&& CollectionUtils.isNotEmpty(offer.getAttributes().getBenefitCodesToSuppressOffer())) {
					for (String inputSupressBenefitCode : ctOfferRequest.getBenefitsCodesToSuppressOffer()) {
						for (String outputSuppressCode : offer.getAttributes().getBenefitCodesToSuppressOffer()) {
							if (inputSupressBenefitCode.equalsIgnoreCase(outputSuppressCode)) {
								removeOffers.add(offer);
							}
						}
					}
				}
			});

		}
		if (CollectionUtils.isNotEmpty(removeOffers)) {
			offers.removeAll(removeOffers);
			ctResponse.setOffers(offers);
			return ctResponse;
		} else {
			return ctResponse;
		}
	}

	/**
	 * @param ctResponse
	 * @param ctOfferRequest
	 */
	public CTOfferResponse filterEmployeeOffers(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {
		List<CTOffer> removeOffers = new ArrayList<>();
		List<CTOffer> offers = ctResponse.getOffers();
		if (CollectionUtils.isEmpty(ctOfferRequest.getCustomerSegments())) {
			offers.stream().filter(Objects::nonNull).forEach(offer -> {
				if (offer.getAttributes().getOfferProductFamily()!=null && offer.getAttributes().getOfferProductFamily().equalsIgnoreCase("OTT") && Optional.ofNullable(offer.getAttributes().getEligibility()).isPresent()
						&& Optional.ofNullable(offer.getAttributes().getEligibility().getConstraints()).isPresent()
						&& CollectionUtils.isNotEmpty(offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments())
						&& offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments().contains(Constants.EMPLOYEE)) {
					removeOffers.add(offer);
				}

			});

		}
		if (CollectionUtils.isNotEmpty(removeOffers)) {
			offers.removeAll(removeOffers);
			ctResponse.setOffers(offers);
			return ctResponse;
		} else {
			return ctResponse;
		}
	}
	/**
	 * @param ctOfferRequest
	 * @return
	 */
	private boolean isZipCodeAvailable(CTOfferRequest ctOfferRequest) {
		boolean isEligible = false;
		List<String> zipCodeList = offersUtils.fetchCTZipcodes();

		if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_ZIP_SALES_OFFER) && CollectionUtils.isNotEmpty(ctOfferRequest.getOfferActionType())
				&& ctOfferRequest.getOfferActionType().contains(Constants.ACQUISITION_ACTION_TYPE)
				&& CollectionUtils.isNotEmpty(ctOfferRequest.getSalesChannel()) && ctOfferRequest.getSalesChannel().stream().anyMatch(s -> OffersUtils.checkOnlineRelatedChannel(s))
				&& CollectionUtils.isNotEmpty(ctOfferRequest.getOfferProductType()) && ctOfferRequest.getOfferProductType().contains(Constants.VIDEO_PLAN)
				&& Optional.ofNullable(ctOfferRequest.getCustomerEligibility()).isPresent()
				&& CollectionUtils.isNotEmpty(ctOfferRequest.getCustomerEligibility().getZipCode()) && CollectionUtils.isNotEmpty(zipCodeList)
				&& zipCodeList.containsAll(ctOfferRequest.getCustomerEligibility().getZipCode())) {
			isEligible = true;
		}
		return isEligible;
	}

	public Map<String, List<String>> loadGlobalConfigurations(String productFamily) {
		CTProductRequest ctProductRequest = new CTProductRequest();
		if(productFamily.equalsIgnoreCase(Constants.OTT)) {
			List<String> ctProductFamily = new ArrayList<>();
			ctProductFamily.add("OTT");
			ctProductRequest.setProductFamily(ctProductFamily);
			List<String> productCodes = new ArrayList<>();
			productCodes.add("epochGlobalConfigurations");
			ctProductRequest.setProductCodes(productCodes);
		} else if(Constants.SATELLITE_PRODUCT_FAMILY.equalsIgnoreCase(productFamily)){
			List<String> ctProductFamily = new ArrayList<>();
			ctProductFamily.add(productFamily);
			ctProductRequest.setProductFamily(ctProductFamily);
			List<String> productCodes = new ArrayList<>();
			productCodes.add("epochGlobalConfigurations_satellite");
			ctProductRequest.setProductCodes(productCodes);
		}
		String ctReqString=JsonService.getJsonFromObject(ctProductRequest);

		Map<String, List<String>> globalConfigurationMap;
		if (cpopClientHelper.isPVTEnabled()) {
			log.info("started PVT cpopPVTClient.loadGlobalConfigurations...");
			return cpopPVTClient.loadGlobalConfigurations(ctReqString, idpConfigEnv);
		} else {
			if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)) {
				log.info("started CpopBackupClient.loadGlobalConfigurations...");
				globalConfigurationMap = cpopBackUpClient.loadGlobalConfigurations(ctReqString, idpConfigEnv);
			} else {
				log.info("started CpopPrimaryClient.loadGlobalConfigurations...");
				globalConfigurationMap = cpopPrimaryClient.loadGlobalConfigurations(ctReqString, idpConfigEnv);
			}
		}
		return globalConfigurationMap;

	}
	
	public List<GlobalEligibilityRule> loadGlobalEligibilityRules(String productFamily) {
		CTProductRequest ctProductRequest = new CTProductRequest();
		if(productFamily.equalsIgnoreCase(Constants.OTT)) {
			List<String> ctProductFamily = new ArrayList<>();
			ctProductFamily.add("OTT");
			ctProductRequest.setProductFamily(ctProductFamily);
			List<String> productTypes=new ArrayList<String>();
			productTypes.add(Constants.EPOCH_GLOBAL_CONFIG);
			ctProductRequest.setProductTypes(productTypes);
			List<String> productCodes = new ArrayList<>();
			productCodes.add("epochGlobalConfigurations");
			productCodes.add("OTT-ELIGIBITYCHECK_OPUSSTORE");
			productCodes.add("OTT-ELIGIBITYCHECK_ONLINEPARTNERDETAILS");
//			ctProductRequest.setProductCodes(productCodes);
		} else if(Constants.SATELLITE_PRODUCT_FAMILY.equalsIgnoreCase(productFamily)){
			List<String> ctProductFamily = new ArrayList<>();
			ctProductFamily.add(productFamily);
			ctProductRequest.setProductFamily(ctProductFamily);
			List<String> productCodes = new ArrayList<>();
			productCodes.add("epochGlobalConfigurations_satellite");
			ctProductRequest.setProductCodes(productCodes);
		}
		String ctReqString=JsonService.getJsonFromObject(ctProductRequest);

		List<GlobalEligibilityRule> globalEligibilityRules;
		if (cpopClientHelper.isPVTEnabled()) {
			log.info("started PVT cpopPVTClient.loadGlobalEligibilityRules...");
			return cpopPVTClient.loadGlobalEligibilityRules(ctReqString, idpConfigEnv);
		} else {
			if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)) {
				log.info("started CpopBackupClient.loadGlobalEligibilityRules...");
				globalEligibilityRules = cpopBackUpClient.loadGlobalEligibilityRules(ctReqString, idpConfigEnv);
			} else {
				log.info("started CpopPrimaryClient.loadGlobalEligibilityRules...");
				globalEligibilityRules = cpopPrimaryClient.loadGlobalEligibilityRules(ctReqString, idpConfigEnv);
			}
		}
		return globalEligibilityRules;

	}

	public Map<String, List<String>> loadValidateCartRules(String productFamily) {
		CTProductRequest ctProductRequest = new CTProductRequest();
		if(productFamily.equalsIgnoreCase(Constants.OTT)) {
			List<String> ctProductFamily = new ArrayList<>();
			ctProductFamily.add("OTT");
			ctProductRequest.setProductFamily(ctProductFamily);
			List<String> productCodes = new ArrayList<>();
			productCodes.add("epochGlobalConfigurations");
			ctProductRequest.setProductCodes(productCodes);
		} else if(Constants.SATELLITE_PRODUCT_FAMILY.equalsIgnoreCase(productFamily)){
			List<String> ctProductFamily = new ArrayList<>();
			ctProductFamily.add(productFamily);
			ctProductRequest.setProductFamily(ctProductFamily);
			List<String> productCodes = new ArrayList<>();
			productCodes.add("epochGlobalConfigurations_satellite");
			ctProductRequest.setProductCodes(productCodes);
		}
		String ctReqString=JsonService.getJsonFromObject(ctProductRequest);
		Map<String, List<String>> globalConfigurationMap;
		if (cpopClientHelper.isPVTEnabled()) {
			log.info("started PVT cpopPVTClient.loadValidateCartRules...");
			return cpopPVTClient.loadValidateCartRules(ctReqString, idpConfigEnv);
		} else {
			if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)) {
				log.info("started CpopBackupClient.loadValidateCartRules...");
				globalConfigurationMap = cpopBackUpClient.loadValidateCartRules(ctReqString, idpConfigEnv);
			} else {
				log.info("started CpopPrimaryClient.loadValidateCartRules...");
				globalConfigurationMap = cpopPrimaryClient.loadValidateCartRules(ctReqString, idpConfigEnv);
			}
		}
		return globalConfigurationMap;
	}

    public Map<String, List<String>> loadSwimlaneRules(String productFamily) {
        CTProductRequest ctProductRequest = new CTProductRequest();
        if(productFamily.equalsIgnoreCase(Constants.OTT)) {
            List<String> ctProductFamily = new ArrayList<>();
            ctProductFamily.add("OTT");
            ctProductRequest.setProductFamily(ctProductFamily);
            List<String> productCodes = new ArrayList<>();
            productCodes.add("epochGlobalConfigurations");
            ctProductRequest.setProductCodes(productCodes);
        } else if(Constants.SATELLITE_PRODUCT_FAMILY.equalsIgnoreCase(productFamily)){
            List<String> ctProductFamily = new ArrayList<>();
            ctProductFamily.add(productFamily);
            ctProductRequest.setProductFamily(ctProductFamily);
            List<String> productCodes = new ArrayList<>();
            productCodes.add("epochGlobalConfigurations_satellite");
            ctProductRequest.setProductCodes(productCodes);
        }
        String ctReqString=JsonService.getJsonFromObject(ctProductRequest);
        Map<String, List<String>> globalConfigurationMap;
        if (cpopClientHelper.isPVTEnabled()) {
            log.info("started PVT cpopPVTClient.loadSwimlaneRules...");
            return cpopPVTClient.loadSwimlaneRules(ctReqString, idpConfigEnv);
        } else {
            if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)) {
                log.info("started CpopBackupClient.loadSwimlaneRules...");
                globalConfigurationMap = cpopBackUpClient.loadSwimlaneRules(ctReqString, idpConfigEnv);
            } else {
                log.info("started CpopPrimaryClient.loadSwimlaneRules...");
                globalConfigurationMap = cpopPrimaryClient.loadSwimlaneRules(ctReqString, idpConfigEnv);
            }
        }
        return globalConfigurationMap;
    }

}
