package com.dtv.dcp.epoch.processor.ott.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.dtv.dcp.epoch.model.ct.generic.GenericTypeIdBase;
import com.dtv.dcp.epoch.model.ct.offer.OfferAttributes;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.CpopConstants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.DtvnMidasRule;
import com.dtv.dcp.epoch.model.common.request.BenefitRequest;
import com.dtv.dcp.epoch.model.common.request.BenefitRequestWrapper;
import com.dtv.dcp.epoch.model.common.request.ChannelEligibility;
import com.dtv.dcp.epoch.model.common.request.CustomerContext;
import com.dtv.dcp.epoch.model.common.request.CustomerPromotion;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.common.request.ProductInfo;
import com.dtv.dcp.epoch.model.ct.benefit.Benefit;
import com.dtv.dcp.epoch.model.ct.generic.GenericNameValueBase;
import com.dtv.dcp.epoch.model.ct.offer.AssociatedProduct;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.offer.OfferPrice;
import com.dtv.dcp.epoch.model.ct.product.Attributes;
import com.dtv.dcp.epoch.model.ct.product.IncludeProductWrapper;
import com.dtv.dcp.epoch.model.ct.product.Price;
import com.dtv.dcp.epoch.model.ct.product.Product;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.model.ct.product.ProductWrapper;
import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.dtv.dcp.epoch.model.ct.request.PriceProtection;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.model.customergraph.response.CGPromotion;
import com.dtv.dcp.epoch.model.customergraph.response.CGResponse;
import com.dtv.dcp.epoch.model.customergraph.response.CGServiceInfo;
import com.dtv.dcp.epoch.processor.helper.CPOPProductsHelper;
import com.dtv.dcp.epoch.processor.ott.OttCTOffersProcessor;
import com.dtv.dcp.epoch.processor.ott.sales.OttSalesOffersProcessorHelper;
import com.dtv.dcp.epoch.processor.ott.services.model.BestPrice;
import com.dtv.dcp.epoch.processor.ott.services.model.CtProductInfo;
import com.dtv.dcp.epoch.processor.ott.services.model.CustomerServiceDetail;
import com.dtv.dcp.epoch.service.AccountLookupService;
import com.dtv.dcp.epoch.service.customergraph.CustomerGraphServiceImpl;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.dtv.dcp.epoch.util.PnpGroupUtils;
import com.dtv.dcp.epoch.util.RedisCacheHelper;
import com.dtv.dcp.epoch.util.RxJavaHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component("ottPromoContinuationProcessor")
public class OttAvaialbleOffersProcessor {

    private static final String FREE_PROMO = "free-promo";

	private static final String BASIC_SERVICE = "Basic Service";
	private static final String ADDON = "addon";
	private static final String FALSE = "false";
	private static final String TRUE ="true";

    @Autowired
    OttCTOffersProcessor ottCTOffersProcessor;

    @Autowired
    OffersUtils offersUtils;

    @Autowired
    OttServicesOffersProcessorHelper ottServicesOffersProcessorHelper;

    @Autowired
    @Qualifier("CustomerGraph")
    CustomerGraphServiceImpl customerGraphServiceImpl;
    
    @Autowired
    private FeatureManagerHelper featureHelper;

    @Autowired
    private DtvnServicesAddonOffersProcessor dtvnServicesAddonOffersProcessor;

    @Autowired
    private DtvnServicesFeeOffersProcessor dtvnServicesFeeOffersProcessor;

    @Autowired
    private DtvnServicesEquipmentOffersProcessor dtvnServicesEquipmentOffersProcessor;

    private Set<String> activePromoTypes = new HashSet<>();

    @Autowired
    private CpopClientHelper cpopClientHelper;

    @Autowired
    private OttSalesOffersProcessorHelper ottSalesOffersProcessorHelper;

    @Autowired
    DtvnServicesRSNFeeOffersProcessor dtvnServicesRSNFeeOffersProcessor;
    
    @Autowired
    OttServicesBenefitsProcessor ottServicesBenefitsProcessor;

    @Autowired
    private OttServicesIoOffersProcessor ottServicesIoOffersProcessor;
    
    @Autowired
    DtvnServicesProtectionPlanProcessor dtvnServicesProtectionPlanProcessor;
	
	@Autowired
	PnpGroupUtils pnpGroupUtils;

    @Autowired
    private AccountLookupService accountLookupService;

    /**
     * @return the activePromoTypes
     */
    public Set<String> getActivePromoTypes() {
        return activePromoTypes;
    }

 
    /**
     * @param activePromoTypes the activePromoTypes to set
     */
    public void setActivePromoTypes(Set<String> activePromoTypes) {
        this.activePromoTypes = activePromoTypes;
    }

    @Autowired
    private FeatureManagerHelper featureManagerHelper;


    private static final Logger log = LoggerFactory.getLogger(OttAvaialbleOffersProcessor.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();



    @Autowired
    DtvnServicesStandAloneProcessor dtvnServicesStandAloneProcessor;


    private static final String SPECIAL = "SPECIAL";

    private static final String FREE = "Free";

    @Autowired
    CPOPProductsHelper cpopProductsHelper;

    @Autowired
    private RedisCacheHelper redisCacheHelper;

    @Autowired
    DtvnServicesCreditOffersProcessor dtvnServicesCreditOffersProcessor;

    public CTOfferResponse getAvailableCTOffers(OfferRequestWrapper offerRequestWrapper) {
        List<CTOffer> processedCTOffers = null;
        CustomerSubscriptionDetail customerSubscriptionDetail = null;
       CTOfferResponse finalCTOfferResponse = new CTOfferResponse();
       Map<String, String> currentServiceInfo = new HashMap<>();

        processedCTOffers = new ArrayList<>();
        CGResponse cGResponse = null;
        List<String> basePackageCompatibleList = new ArrayList<>();
        try {
            customerSubscriptionDetail = populateCustomerSubscriptionDetail(offerRequestWrapper);

        } catch (ParseException e) {
            log.error("Error in method populateCustomerSubscriptionDetail() method", e);
        }
        
        if(offerRequestWrapper.getOfferRequest().getOfferProductType().contains(Constants.VIDEO_DEVICE) || offerRequestWrapper.getOfferRequest().getOfferProductType().contains(Constants.VIDEO_PLAN) || offerRequestWrapper.getOfferRequest().getOfferProductType().contains(Constants.VIDEO_ADDON)) {

            final List<ProductWrapper>[] basePackageCompatibleProds = new List[]{null};
            if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext()).isPresent() &&
                    Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()).isPresent()) {
                CustomerSubscriptionDetail finalCustomerSubscriptionDetail = customerSubscriptionDetail;
                offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getProducts().forEach(prod -> {
                    if (prod.getProductType().equalsIgnoreCase(Constants.VIDEO_PLAN)) {
                        basePackageCompatibleProds[0] = getBasePackageCompatibleListFromCT(prod.getProductCode(), offerRequestWrapper.isMobility() , offerRequestWrapper.isEmployeeAccount(),offerRequestWrapper.getOfferRequest().getBusinessSegment(),finalCustomerSubscriptionDetail);
                    }
                });
            } else if ((StringUtils.isNotBlank(offerRequestWrapper.getDtvnAccount()))) {
                cGResponse = customerGraphServiceImpl.getActiveSubscriptions(offerRequestWrapper.getDtvnAccount(), featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP) ? "backup" : "primary");
                for (CGServiceInfo serviceInfo : Arrays.asList(cGResponse.getServiceInfo())) {
                    if (BASIC_SERVICE.equals(serviceInfo.getTypeOfPlan())) {
                        basePackageCompatibleProds[0] = getBasePackageCompatibleListFromCT(serviceInfo.getServiceID(), offerRequestWrapper.isMobility() , offerRequestWrapper.isEmployeeAccount(),Objects.nonNull(offerRequestWrapper.getOfferRequest().getBusinessSegment())?
                        offerRequestWrapper.getOfferRequest().getBusinessSegment() : Arrays.asList(Constants.CONS),customerSubscriptionDetail);
                    }
                }
            } 

            if (Objects.nonNull(basePackageCompatibleProds[0]) &&  !basePackageCompatibleProds[0].isEmpty()) {
                basePackageCompatibleProds[0].forEach(prods -> {
                    if (Objects.nonNull(prods.getProducts())) {
                        prods.getProducts().forEach(prod -> 
                            basePackageCompatibleList.add(prod.getKey())
                        );
                    }
                });
            }
        }
        // STANDALONE Scenario : POPULATE Package Group and ProductStatus and CompatibleList
        dtvnServicesStandAloneProcessor.populatePackageGroupStandAlone(offerRequestWrapper,customerSubscriptionDetail, currentServiceInfo, basePackageCompatibleList);

        // Making Async calls
        log.info("Making Async Calls Services Flow");
        log.info("Getting Video Addon, Video Device and Fee Offers");
        processedCTOffers.addAll(getOffersAsynchronously(offerRequestWrapper, basePackageCompatibleList, customerSubscriptionDetail, finalCTOfferResponse,currentServiceInfo));
        log.info("End Async Calls Services Flow");
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(processedCTOffers)){
            offersUtils.filterOffersBasedOnEligibleIapPartners(processedCTOffers, offerRequestWrapper);
        }
        // SLS-IXP-FLAG changes
        if (offersUtils.isSlsGetOfferServicesEnabled()
                && Optional.ofNullable(offerRequestWrapper.getOfferRequest().getSwimlaneSwitchEligible()).isPresent()
                && offerRequestWrapper.getOfferRequest().getSwimlaneSwitchEligible()
                && !offersUtils.isUniversalCohort(offerRequestWrapper.getOfferRequest().getContractIndicator())) {
            processedCTOffers = ottServicesOffersProcessorHelper.combineSwimLaneOffers(processedCTOffers, offerRequestWrapper);
        } else {
            processedCTOffers = ottServicesOffersProcessorHelper.combineOffers(processedCTOffers);
        }
        
        List<String> existingPromos = new ArrayList<String>();
        customerSubscriptionDetail.getListAddOnServiceDetail().forEach(product -> {
        	existingPromos.addAll(product.getPromosId());
        });
        
		customerSubscriptionDetail.getListBaseBackageServiceDetail().forEach(product -> {
            existingPromos.addAll(product.getPromosId());
		});
		
		customerSubscriptionDetail.getListDeviceServiceDetail().forEach(product -> {
            existingPromos.addAll(product.getPromosId());
		});
       
        List<CTOffer> removeList = new ArrayList<CTOffer>();
        processedCTOffers.forEach(offer -> {
        	if (Optional.ofNullable(offer).isPresent() && Optional.ofNullable(offer.getAttributes()).isPresent()
        			&& Optional.ofNullable(offer.getAttributes().getBenefitCodesToSuppressOffer()).isPresent()
        			&& !offer.getAttributes().getBenefitCodesToSuppressOffer().isEmpty()) {
        		offer.getAttributes().getBenefitCodesToSuppressOffer().forEach(bolt -> {
        			if (existingPromos.contains(bolt)) {
        				removeList.add(offer);
        			}
        		});
        	}
        });
        
        if (!removeList.isEmpty()) {
        	processedCTOffers.removeAll(removeList);
        }
        
        
		AtomicInteger devicesCustomerHad = new AtomicInteger();
		if (Optional.ofNullable(customerSubscriptionDetail.getListDeviceServiceDetail()).isPresent()
				&& !customerSubscriptionDetail.getListDeviceServiceDetail().isEmpty()) {
            List<String> freeDevicePromoList = redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_FREEDEVICEPROMOS, Constants.OTT_PRODUCT_FAMILY);
			customerSubscriptionDetail.getListDeviceServiceDetail().forEach(deviceServiceDetail -> {
				CustomerServiceDetail promosList = deviceServiceDetail;
				promosList.getPromosId().forEach(promo ->{
					if (Optional.ofNullable(promo).isPresent() && freeDevicePromoList.contains(promo)) {
						devicesCustomerHad.getAndIncrement();
					}
				});
			});

		}

        AtomicInteger maxOccurance = new AtomicInteger();

		processedCTOffers.forEach(offer -> {

			if (Optional.ofNullable(offer).isPresent() && Optional.ofNullable(offer.getAttributes()).isPresent()
					&& Optional.ofNullable(offer.getAttributes().getOfferProductType()).isPresent()
					&& offer.getAttributes().getOfferProductType().equalsIgnoreCase(Constants.VIDEO_DEVICE)
					&& Optional.ofNullable(offer.getAttributes().getEligibility()).isPresent()
					&& Optional.ofNullable(offer.getAttributes().getEligibility().getConstraints()).isPresent()
					&& Optional.ofNullable(
							offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments())
							.isPresent()
                     && (offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments().contains(Constants.EMPLOYEE) ||
                            offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments().contains(Constants.DEMO)     ||
                            offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments().contains(Constants.DECA)     ||
                            offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments().contains(Constants.SHOWROOM) ||
                            offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments().contains(Constants.BCOMP)    ||
                            offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments().contains(Constants.COURTESY) ||
                            offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments().contains(Constants.MDUTENANT))
					&& Optional.ofNullable(offer.getAttributes().getBenefits()).isPresent()
					&& !offer.getAttributes().getBenefits().isEmpty()) {

				offer.getAttributes().getBenefits().forEach(benefit -> {

					if (Optional.ofNullable(benefit.getMaxOccurrence()).isPresent() && devicesCustomerHad != null) {

						benefit.setMaxOccurrence(Math.max(0, benefit.getMaxOccurrence() - devicesCustomerHad.get()));
                        maxOccurance.set(benefit.getMaxOccurrence());
					}

				});
                if( Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerSegments()).isPresent() 
						&& (offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.DEMO)
								|| (offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.MDUTENANT) && Objects.nonNull(offer.getAttributes().isBulkOffer()) && offer.getAttributes().isBulkOffer()))) {
					
					offersUtils.setMaxOccurance(null, null, offer, offerRequestWrapper,0);
				}
			}
		});
        
//		if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerSegments())
//				&& offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.MDUTENANT)
//                && !featureHelper.isEnabled(Constants.FEATURE_TOGGLE_IXP_PAIDDEVICES_MDUTENANT_SERVICES_ENABLED)) {
//			processedCTOffers=offersUtils.removeBulkOffer(processedCTOffers,Constants.VIDEO_DEVICE,maxOccurance.get());
//		} else if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerSegments())
//				&& offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.MDUTENANT)
//				&& featureHelper.isEnabled(Constants.FEATURE_TOGGLE_IXP_PAIDDEVICES_MDUTENANT_SERVICES_ENABLED)) {
			removeFreeDeviceBulkOfferIfCountMatch(processedCTOffers, devicesCustomerHad.get());
//		}
        //suppress offerlevel attributes in response
        ottServicesOffersProcessorHelper.supressOfferLevelAttributes(processedCTOffers);
            offersUtils.filterOfferBasedOnZipOrDMA(processedCTOffers, offerRequestWrapper);

        finalCTOfferResponse.setOffers(processedCTOffers);
        finalCTOfferResponse.setCount(processedCTOffers.size());
        finalCTOfferResponse.setTotal(processedCTOffers.size());

          boolean hasSpecialPage = Optional.ofNullable(offerRequestWrapper)
                    .map(OfferRequestWrapper::getOfferRequest)
                    .map(OfferRequest::getChannelEligibility)
                    .map(ChannelEligibility::getSpecialPage)
                    .filter(StringUtils::isNotBlank)
                    .isPresent();

            if (hasSpecialPage) {
                filterOffersBasedOnSpecialPage(offerRequestWrapper, finalCTOfferResponse);
            } else {
                //remove special page offers from finalCTOfferResponse if request doesn't have special page
                removeSpecialPageOffers(finalCTOfferResponse);
            }
            filterOffersBasedOnBANLookup(offerRequestWrapper, finalCTOfferResponse);

        if (offerRequestWrapper != null && offerRequestWrapper.getOfferRequest() != null
                && offerRequestWrapper.getOfferRequest().getCustomerContext()!=null
                && offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()!=null
                && offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getNextBillingDate()!=null) {
        	
        	Boolean isPNP=false;
        	String PnpCustomerGroup="";

            //Added HOG global config logic for RR and TAZCONTRACT
            if((offersUtils.containsTAZCONTRACT(offerRequestWrapper.getOfferRequest()) ||
                    offersUtils.containsROADRUNNER(offerRequestWrapper.getOfferRequest()))
                            && Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext()) &&
                            Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()) &&
                            Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getNextBillingDate()) &&
                            Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getPriceProtection())){

                PriceProtection priceProtection = offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getPriceProtection();

                String nbcd = offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getNextBillingDate();

                PnpCustomerGroup = pnpGroupUtils.getPnpGroup(priceProtection, nbcd,Constants.OTT);
                isPNP= (Objects.isNull(PnpCustomerGroup) ||PnpCustomerGroup.isEmpty())? isPNP : true;

                //if hog window match then consider the customer as non pnp
                if (Boolean.TRUE.equals(priceProtection.getIsHogWindowMatch())) {
                    isPNP = false;
                    PnpCustomerGroup = "";
                }

            }

    		finalCTOfferResponse = cpopProductsHelper.filterInvalidContractIndicatorPrices(finalCTOfferResponse, offerRequestWrapper, isPNP, PnpCustomerGroup);
        	calculateOfferPriceIXPFlagEnabled(offerRequestWrapper, processedCTOffers);
            return finalCTOfferResponse;
        }
        // Filter the offer price based the current date.
        finalCTOfferResponse = cpopProductsHelper.filterInvalidPrices(finalCTOfferResponse, offerRequestWrapper);
    	//Set the offer price which is filtered based on the current date.
        calculateOfferPriceIXPFlagEnabled(offerRequestWrapper, processedCTOffers);
        
        return finalCTOfferResponse;

    }
    
	public void removeFreeDeviceBulkOfferIfCountMatch(List<CTOffer> processedCTOffers, int deviceCustomerhas) {
		List<CTOffer> removeList = new ArrayList<CTOffer>();
		processedCTOffers.forEach(pc -> {
			if (Optional.ofNullable(pc).isPresent() && Optional.ofNullable(pc.getAttributes()).isPresent()
					&& Optional.ofNullable(pc.getAttributes().getOfferProductType()).isPresent()
					&& pc.getAttributes().getOfferProductType().equalsIgnoreCase(Constants.VIDEO_DEVICE)
                	&& pc.getAttributes().isBulkOffer()
					&& freeDeviceCountDoneForBulkOffer(pc, deviceCustomerhas)) {
				removeList.add(pc);
			}
		});
		if (!removeList.isEmpty()) {
			processedCTOffers.removeAll(removeList);
		}
	}
    
    public boolean freeDeviceCountDoneForBulkOffer(CTOffer offer,int deviceCustomerhas) {
    	int freeDeviceCount = 0;
    	if( Objects.nonNull(offer.getAttributes().getAssociatedProducts()) 
    			&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts())
				&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts())
				&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0))
				&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj())
				&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants())
				&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0))
				&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes())
				&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().getFreeDeviceCount())) 
    		{
    		freeDeviceCount = offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().getFreeDeviceCount();
    			if(freeDeviceCount == deviceCustomerhas) {
    				return true;
    			}
    		}
		return false;
    }
    
	private void calculateOfferPriceIXPFlagEnabled(OfferRequestWrapper offerRequestWrapper,
			List<CTOffer> processedCTOffers) {
//		if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_IXP_OFFERPRICE_ENABLED)) {
//			if (offerRequestWrapper.getOfferRequest().getSalesChannel().stream().anyMatch(
//					s -> Constants.OEM_IAPFIRETV.equalsIgnoreCase(s) || Constants.OEM_IAPROKUTV.equalsIgnoreCase(s))) {
//				log.info("Get offers Based on the IXP flag is TRUE and sales channel is Fire TV or Roku TV");
//				OffersUtils.calculateBestPrice(offerRequestWrapper, processedCTOffers);
//			} else {
//				log.info("Get offers Based on the IXP flag is TRUE For Old Offer Price logic");
//				ottServicesOffersProcessorHelper.calculateBestPrice(processedCTOffers);
//			}
//		} else {
		log.info("Get offers Based on the IXP flag is FALSE Which returns new Offer prices");
		offersUtils.calculateBestPrice(offerRequestWrapper, processedCTOffers);
           // offersUtils.setBasePriceBasedOnCustomerSubscriptionType(offerRequestWrapper, processedCTOffers);
//		}
	}
    /**
     *
     * @param offerRequestWrapper
     * @param basePackageCompatibleList
     * @return
     */
    public List<CTOffer> getOffersAsynchronously(OfferRequestWrapper offerRequestWrapper, List<String> basePackageCompatibleList,  CustomerSubscriptionDetail customerSubscriptionDetail, CTOfferResponse ctOfferResponse, Map<String, String> currentServiceInfo) {
        List<Callable<?>> callableObjList = new ArrayList<>();
        // Capture MDC context
        Map<String, String> capturedMdcContext = MDC.getCopyOfContextMap();
        OfferRequestWrapper asyncCpopOffersRequestVideoPlan = new OfferRequestWrapper();
        OfferRequestWrapper asyncCpopOffersRequestVideoAddon = new OfferRequestWrapper();
        OfferRequestWrapper asyncCpopOffersRequestVideoDevice = new OfferRequestWrapper();
        OfferRequestWrapper asyncCpopOffersRequestFee = new OfferRequestWrapper();
        OfferRequestWrapper asyncCpopOffersRequestCredit = new OfferRequestWrapper();


        if (Objects.nonNull(offerRequestWrapper)) {
            BeanUtils.copyProperties(offerRequestWrapper, asyncCpopOffersRequestVideoPlan);
            BeanUtils.copyProperties(offerRequestWrapper, asyncCpopOffersRequestVideoAddon);
            BeanUtils.copyProperties(offerRequestWrapper, asyncCpopOffersRequestVideoDevice);
            BeanUtils.copyProperties(offerRequestWrapper, asyncCpopOffersRequestFee);
            BeanUtils.copyProperties(offerRequestWrapper, asyncCpopOffersRequestCredit);
        }
        callableObjList.add(() -> OffersUtils.executeWithMdcContext(capturedMdcContext, 
                () -> getVideoPlanOffers(asyncCpopOffersRequestVideoPlan, customerSubscriptionDetail, currentServiceInfo)));
        callableObjList.add(() -> OffersUtils.executeWithMdcContext(capturedMdcContext, 
                () -> getVideoAddonOffers(asyncCpopOffersRequestVideoAddon,customerSubscriptionDetail, basePackageCompatibleList)));
        callableObjList.add(() -> OffersUtils.executeWithMdcContext(capturedMdcContext, 
                () -> getVideoDeviceOffers(asyncCpopOffersRequestVideoDevice, basePackageCompatibleList,customerSubscriptionDetail)));
        callableObjList.add(() -> OffersUtils.executeWithMdcContext(capturedMdcContext, 
                () -> getFeeOffers(asyncCpopOffersRequestFee, customerSubscriptionDetail)));
        callableObjList.add(() -> OffersUtils.executeWithMdcContext(capturedMdcContext, 
                () -> getProtectionPlanOffers(offerRequestWrapper)));
        callableObjList.add(() -> OffersUtils.executeWithMdcContext(capturedMdcContext,
                () -> getCreditOffers(offerRequestWrapper)));
        
        //callableObjList.add(() ->  getVideoPlanOffers(asyncCpopOffersRequestVideoPlan, customerSubscriptionDetail, currentServiceInfo));
        //callableObjList.add(() ->  getVideoAddonOffers(asyncCpopOffersRequestVideoAddon,customerSubscriptionDetail, basePackageCompatibleList));
        //callableObjList.add(() ->  getVideoDeviceOffers(asyncCpopOffersRequestVideoDevice, basePackageCompatibleList,customerSubscriptionDetail));
        //callableObjList.add(() ->  getFeeOffers(asyncCpopOffersRequestFee, customerSubscriptionDetail));
        //callableObjList.add(() ->  getProtectionPlanOffers(offerRequestWrapper));
        
        Object[] responses = null;
        long startTimeInMillis = System.currentTimeMillis();
        responses = RxJavaHelper.callConcurrentlyGetResult(callableObjList.toArray(new Callable[callableObjList.size()]));
        long endTimeMillis = System.currentTimeMillis() - startTimeInMillis;
        log.info("EPOCH_OTT_SERVICES_GET_OFFERS_ASYNC-[{}]", (endTimeMillis));
        // log.info("Getting Async response: {}", responses);

        log.info("End of getOffersAsynchronously Services method..");
        return handleAsyncResponsesServices(responses, offerRequestWrapper, customerSubscriptionDetail, ctOfferResponse);
    }

    /**
     *
     * @param responses
     * @return
     * @throws ServiceException
     */
    public List<CTOffer> handleAsyncResponsesServices(Object[] responses, OfferRequestWrapper offerRequestWrapper, CustomerSubscriptionDetail customerSubscriptionDetail, CTOfferResponse ctOfferResponse) throws ServiceException {
        log.info("Start handleAsyncResponsesServices");
        List<CTOffer> localProcessedCTOffers = new ArrayList<>();

        ServiceException serviceException = null;
        boolean anyFailure = false;
        try {
            // VIDEO PLAN
            if( responses[0] instanceof List) {

                if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getSalesChannel()) &&
                        org.apache.commons.lang.StringUtils.equalsIgnoreCase(Constants.OPUS, offerRequestWrapper.getOfferRequest().getSalesChannel().get(0))){
                    // Don't change the order of this execution, extracting non ioOffers should come first
                    localProcessedCTOffers.addAll( (ottServicesIoOffersProcessor.extractNonIOOffers(new ArrayList<>((List<CTOffer>) responses[0]))));
                    // extract ioOffers
                    List<CTOffer> ioOffers = ottServicesIoOffersProcessor.processIoOffers(offerRequestWrapper, new ArrayList<>((List<CTOffer>) responses[0]));
                    ctOfferResponse.setIoOffers(ioOffers);

                } else {
                    localProcessedCTOffers.addAll((List<CTOffer>) responses[0]);
                }

                if( responses[3] instanceof List) {
                    List<CTOffer> feeOffers = (List<CTOffer>) responses[3];

                    // Populating RSN Fee details for Available VideoPlan Offers
                    if (Objects.nonNull(customerSubscriptionDetail) && (Constants.CONTRACT.equalsIgnoreCase(customerSubscriptionDetail.getContractIndicator())
                    		|| Constants.TAZ_STRING.equalsIgnoreCase(customerSubscriptionDetail.getContractIndicator())
                            || Constants.TAZCONTRACT_STRING.equalsIgnoreCase(customerSubscriptionDetail.getContractIndicator())
                            || Constants.ROAD_RUNNER.equalsIgnoreCase(customerSubscriptionDetail.getContractIndicator()))
                    ) {
                        dtvnServicesRSNFeeOffersProcessor.populateRSNFeeDetails(offerRequestWrapper, customerSubscriptionDetail, localProcessedCTOffers, feeOffers);
                    }
                }


            } else {
                serviceException = (ServiceException) responses[0];
                log.error("handleAsyncResponse: Scenario {} Failure Description {} {}", Constants.VIDEO_PLAN, serviceException.getMessage(), serviceException.getError());
                anyFailure = true;
            }
            // VIDEO ADDON
            if( responses[1] instanceof List) {
                localProcessedCTOffers.addAll( (List<CTOffer>) responses[1]);
            } else {
                serviceException = (ServiceException) responses[1];
                log.error("handleAsyncResponse: Scenario {} Failure Description {} {}", Constants.VIDEO_ADDON, serviceException.getMessage(), serviceException.getError());
                anyFailure = true;
            }
            // VIDEO DEVICE
            if( responses[2] instanceof List) {
                localProcessedCTOffers.addAll( (List<CTOffer>) responses[2]);
            } else {
                serviceException = (ServiceException) responses[2];
                log.error("handleAsyncResponse: Scenario {} Failure Description {} {}", Constants.VIDEO_DEVICE, serviceException.getMessage(), serviceException.getError());
                anyFailure = true;
            }
            // FEE
            if( responses[3] instanceof List) {
            	if(offerRequestWrapper.getOfferRequest().getOfferProductType().contains(Constants.FEE) && !offersUtils.isEDSPOnlyRequest(offerRequestWrapper.getOfferRequest())) {
	                List<CTOffer> feeOffers = (List<CTOffer>) responses[3];
                    if (localProcessedCTOffers != null && !localProcessedCTOffers.isEmpty() && feeOffers != null && !feeOffers.isEmpty()){
                        dtvnServicesRSNFeeOffersProcessor.filterRsnFeeOfferBasedOnRsnFeeDetails(localProcessedCTOffers, feeOffers);
                    }else{
                        dtvnServicesRSNFeeOffersProcessor.filterRSNFeeOffers(offerRequestWrapper,customerSubscriptionDetail, feeOffers);
                    }
	                localProcessedCTOffers.addAll(feeOffers);
            	}
            } else {
                serviceException = (ServiceException) responses[3];
                log.error("serviceException {}", serviceException);
                log.error("handleAsyncResponse: Scenario {} Failure Description {} {}", Constants.FEE, serviceException.getMessage(), serviceException.getError());
                anyFailure = true;
            }
            // PROTECTION PLAN
            if( responses[4] instanceof List) {
                localProcessedCTOffers.addAll( (List<CTOffer>) responses[4]);
            } else {
                serviceException = (ServiceException) responses[4];
                log.error("handleAsyncResponse: Scenario {} Failure Description {} {}", Constants.PROTECTION_PLAN, serviceException.getMessage(), serviceException.getError());
                anyFailure = true;
            }
            // CREDIT
            if( responses[5] instanceof List) {
                localProcessedCTOffers.addAll( (List<CTOffer>) responses[5]);
            } else {
                serviceException = (ServiceException) responses[5];
                log.error("handleAsyncResponse: Scenario {} Failure Description {} {}", Constants.CREDIT, serviceException.getMessage(), serviceException.getError());
                anyFailure = true;
            }
            if(anyFailure) {
                log.error("Some method failed handleAsyncResponsesServices::::::");
                if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferProductFamily()).isPresent()
    					&& !offerRequestWrapper.getOfferRequest().getOfferProductFamily().isEmpty()
    					&& !offerRequestWrapper.getOfferRequest().getOfferProductFamily().contains(null)
    					&& offerRequestWrapper.getOfferRequest().getOfferProductFamily().stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
        			throw new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_OTT_GETEOFFER_10001)
        					.addDetail(ErrorMessages.CPOP_OFFER_ERROR_ON_OTT_GETEOFFER_10002,  "handleAsyncResponse()");
                } else {
                    throw ((new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001))
                            .addDetail(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10002,  "handleAsyncResponse()"));
                }
            }

        } catch (ServiceException ex) {
            log.error("Exception handleAsyncResponsesServices...{}", ex);
            if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferProductFamily()).isPresent()
					&& !offerRequestWrapper.getOfferRequest().getOfferProductFamily().isEmpty()
					&& !offerRequestWrapper.getOfferRequest().getOfferProductFamily().contains(null)
					&& offerRequestWrapper.getOfferRequest().getOfferProductFamily().stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
    			throw new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_OTT_GETEOFFER_10001, ex)
    					.addDetail(ErrorMessages.CPOP_OFFER_ERROR_ON_OTT_GETEOFFER_10002,  "handleAsyncResponse()");
            } else {
                throw ((new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001, ex))
                        .addDetail(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10002,  "handleAsyncResponse()"));
            }
        }
        log.info("End handleAsyncResponsesServices");
        return localProcessedCTOffers;
    }
    public List<CTOffer> getVideoPlanOffers(OfferRequestWrapper offerRequestWrapper, CustomerSubscriptionDetail customerSubscriptionDetail,Map<String, String> currentServiceInfo) {
        log.info("Start Services getVideoPlanOffers");
        CTOfferResponse ctOfferResponse = null;
        List<CTOffer> fiteredCToffers = null;
        List<CTOffer> ioOffers = null;
        Map<String, String> availablePackageGroup;

        boolean mockGCflag = true;


        List<CTOffer> localProcessedCTOffers = new ArrayList<>();
        if (offerRequestWrapper.getOfferRequest().getOfferProductType().contains(Constants.VIDEO_PLAN)) {

            ctOfferResponse = ottServicesOffersProcessorHelper.getFilteredOffers(offerRequestWrapper, Constants.BASE);
            // it will come only for salesChannel = OPUS
            ioOffers = ctOfferResponse.getIoOffers();

            // ctOfferResponse.setIoOffers(null);

            // BYODOPC-5029:
            // For reconnect services flow, bypass subscription-based filtering since the customer
            // has no active subscription — return CT offers directly after reconnect rule filtering
            boolean isReconnectServicesFlow = offersUtils.isService(offerRequestWrapper.getOfferRequest())
                    && Optional.ofNullable(offerRequestWrapper.getOfferRequest().isReconnectCustomer()).orElse(false)
                    && StringUtils.isNotBlank(offerRequestWrapper.getOfferRequest().getServiceEndDate());

            if (isReconnectServicesFlow) {
                log.info("Reconnect services flow: bypassing subscription-based filterCTOfferResponse, returning CT offers directly. Offer codes: {}",
                        ctOfferResponse.getOffers().stream().map(CTOffer::getCode).collect(Collectors.toList()));
                localProcessedCTOffers.addAll(ctOfferResponse.getOffers());
                if (org.apache.commons.collections.CollectionUtils.isNotEmpty(ioOffers)) {
                    localProcessedCTOffers.addAll(ioOffers);
                    log.info("End Services getVideoPlanOffers");
                    return  localProcessedCTOffers;
                }
            } else {
                fiteredCToffers = filterCTOfferResponse(ctOfferResponse, customerSubscriptionDetail, offerRequestWrapper, currentServiceInfo);

                if (currentServiceInfo.get("packageGroup") == null) {
                    currentServiceInfo.put("productStatus", customerSubscriptionDetail.getVideoPlanProductStatus());
                    currentServiceInfo.put("packageGroup", customerSubscriptionDetail.getVideoPlanPackageCode());
                }

                if (offerRequestWrapper.isMobility()) {
                    updateMobilityProductsAsIncluded(fiteredCToffers);
                } else {
                    removeMobilityProductsIncluded(fiteredCToffers);
                }

                if (log.isDebugEnabled()) {
                    log.debug(String.format("Filtered offers :- %s", convertObjectToJson(fiteredCToffers)));
                }

                availablePackageGroup = retrieveAvailablePackageGroup(customerSubscriptionDetail, currentServiceInfo);

                Set<String> activePromos = getSubscriptionActivePromos(customerSubscriptionDetail);

                Map<String, Benefit> activeBenefits = getBenefitsByBillingBenfitCodes(activePromos);

                List<CTOffer> availableCtOfferList = new ArrayList<>();
                for (CTOffer filteredCtOffer : fiteredCToffers) {
                    if (customerSubscriptionDetail != null) {
                        CTOffer availableCtOffer = createAvailableBasePackageForDTVNow(offerRequestWrapper, filteredCtOffer, customerSubscriptionDetail, activePromos, activeBenefits, availablePackageGroup, currentServiceInfo);
                        //mark the lead offer check

                        if (availableCtOffer != null) {
                            setLeadOffers(offerRequestWrapper, availableCtOffer, availablePackageGroup);
                            availableCtOfferList.add(availableCtOffer);
                        }
                    }
                }
                localProcessedCTOffers.addAll(availableCtOfferList);
                if (org.apache.commons.collections.CollectionUtils.isNotEmpty(ioOffers)) {
                    localProcessedCTOffers.addAll(ioOffers);
                }
            }
        }
        log.info("End Services getVideoPlanOffers");
        return  localProcessedCTOffers;
    }
    /**
     *
     * @param offerRequestWrapper
     * @param basePackageCompatibleList
     * @return
     */
    public List<CTOffer> getVideoAddonOffers(OfferRequestWrapper offerRequestWrapper,CustomerSubscriptionDetail customerSubscriptionDetail, List<String> basePackageCompatibleList ) {
        log.info("Start Services getVideoAddonOffers");

        List<CTOffer> localProcessedCTOffers = new ArrayList<>();
        if (offerRequestWrapper.getOfferRequest().getOfferProductType().contains(Constants.VIDEO_ADDON)) {

            List<CTOffer> ottOffers = dtvnServicesAddonOffersProcessor.retrieveAvailableAddOns(offerRequestWrapper);

            String accountType = offerRequestWrapper.isMobility() ?"Mobility" 
            		: (Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerSegments()) ? 
            				offerRequestWrapper.getOfferRequest().getCustomerSegments().get(0):"Residential");

            if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext()).isPresent() 
            		&& Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext()) 
            		&& Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()).isPresent() 
            		&& Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()))
    		{
            	String parentSubscriptionDate = offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getParentSubscriptionDate();
            	log.info("API_NAME:EPOCH_GETOFFERS ParentSubscriptionDate:{} "+OffersUtils.sanitizeData(parentSubscriptionDate));
    			ottOffers = filterSubscriptionTenureOffers(ottOffers, parentSubscriptionDate);
    		    //Feature 176015 changes - 2nd Chance Premium Acquisition Offers
    			ottOffers = filterSecondChanceOffers (ottOffers, parentSubscriptionDate);
    		}

            if (offersUtils.isService(offerRequestWrapper.getOfferRequest())
            		&& Objects.nonNull(OffersUtils.getIapPartnerAccountType(offerRequestWrapper.getOfferRequest()))) {
            	ottOffers = ottCTOffersProcessor.checkExcludedParter(ottOffers, OffersUtils.getIapPartnerAccountType(offerRequestWrapper.getOfferRequest()));       	
            }
            
            if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerSegments())
            		&&  offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.EMPLOYEE))
            {
            	accountType = "Employee";
            }

            // Feature-163205 Changes
            if (offersUtils.isService(offerRequestWrapper.getOfferRequest()))
            {
            	updateProductActionBundle(ottOffers, offerRequestWrapper.getOfferRequest());
            }

            List<CTOffer> standAloneOffers = offersUtils.filterStandAloneOffersByAddOnTypePlanSubType(ottOffers, Constants.STANDALONE,Constants.INTERNATIONAL, accountType);
            // Filter compatible addon based on current base package
            if (Objects.nonNull(basePackageCompatibleList) && basePackageCompatibleList.size() > 0 && ottOffers != null) {
                ottOffers = filterCompatibleOfferForCurrentBasePackage(basePackageCompatibleList, ottOffers);
            }


            if (!featureHelper.isEnabled(CpopConstants.EPOCH_HBO_MAX_PERM_SOLUTION)) {
                if (Objects.nonNull(basePackageCompatibleList) && ottOffers != null) {
                    ottOffers = filterQualifyingProducts(basePackageCompatibleList, ottOffers, offerRequestWrapper);
                }
                // SLS-IXP-FLAG changes
                if (offersUtils.isSlsGetOfferServicesEnabled()
                        && offersUtils.isService(offerRequestWrapper.getOfferRequest())
                        && offersUtils.isUniversalCohort(offerRequestWrapper.getOfferRequest().getContractIndicator())
                        && Objects.nonNull(basePackageCompatibleList) && ottOffers != null) {
                    ottOffers = filterConflictingSLSOffers(ottOffers);
                } else if(Objects.nonNull(basePackageCompatibleList) && ottOffers != null){
                    ottOffers = filterOffersWhenEligible(basePackageCompatibleList, ottOffers, offerRequestWrapper);
                }
            }
            boolean localsEnabledFlag = false;
            List<String> values = redisCacheHelper.getValues(Constants.REDIS_CACHE_LOCALS_ENABLED_FLAG, Constants.OTT);
            if (values != null && !values.isEmpty()) {
                localsEnabledFlag = Boolean.parseBoolean(values.get(0));
            }
            List<CTOffer> filteredOTTLocalsOffers = offersUtils.filterOffersByProductsHavingSubCategoryLocals(ottOffers);
            if ((!localsEnabledFlag || !offerRequestWrapper.getOfferRequest().getHasOTTLocalChannels())
                    && !filteredOTTLocalsOffers.isEmpty()
                    && Objects.nonNull(ottOffers)
                    && !ottOffers.isEmpty()) {
                ottOffers.removeAll(filteredOTTLocalsOffers);
            }
            if (ottOffers != null) {
                localProcessedCTOffers.addAll(ottOffers);
            }
            if(standAloneOffers != null) {
            	List<CTOffer> filteredStandAloneOffers = new ArrayList<>();
            	// StandAlone in Subscription Scenario
				offersUtils.excludeStandaloneInSubscription(customerSubscriptionDetail, standAloneOffers, filteredStandAloneOffers);
                localProcessedCTOffers.addAll(filteredStandAloneOffers);
            }
            if (featureHelper.isEnabled(Constants.FEATURE_FLAG_CONFLICTING_OFFER_NEW_LOGIC)) {
                localProcessedCTOffers=  offersUtils.filterConflictingOfferWhenAllowConflictingOffersPresent(localProcessedCTOffers, offerRequestWrapper);
            }
        }
        log.info("End Services getVideoAddonOffers");
        return  localProcessedCTOffers;
    }

    /**
     * Filters the offers based on the maxcustomerEligibilityTenure value in CT
     * @param processedCTOffers List<CTOffer>
     * @param parentSubscriptionDate String
     * @return List<CTOffer>
     */
    public List<CTOffer> filterSecondChanceOffers(List<CTOffer> processedCTOffers, String parentSubscriptionDate)
    {
    	List<CTOffer> filteredOffers = new ArrayList<>();
        if (Objects.nonNull(processedCTOffers) && !processedCTOffers.isEmpty()) 
        {
        	processedCTOffers.forEach(offer -> {
        		String eligibilityTenure = offer.getAttributes().getMaxCustomerEligibilityTenureInDays();
        		if(null == eligibilityTenure || (StringUtils.isNumeric(eligibilityTenure) 
        				&& isValidSecondChanceOffer (parentSubscriptionDate, eligibilityTenure))) {
        			log.debug("Either no eligibilityTenure or offer is a valid second chance offer");
        			filteredOffers.add(offer);
        		}
            });
        }
    	return filteredOffers;
    }

    public List<CTOffer> filterSubscriptionTenureOffers(List<CTOffer> processedCTOffers, String parentSubscriptionDate)
    {
    	List<CTOffer> filteredOffers = new ArrayList<>();
        if (Objects.nonNull(processedCTOffers) && !processedCTOffers.isEmpty()) 
        {
        	processedCTOffers.forEach(offer -> {
        		String offerTenure = offer.getAttributes().getMinimumCustomerTenureInMonths();
        		if(null == offerTenure || (StringUtils.isNumeric(offerTenure) && isValidTenureOffer (parentSubscriptionDate, offerTenure))) {
        			log.debug("Either no tenure or offer is valid for given parentSubscriptionDate");
        			filteredOffers.add(offer);
        		}
            });
        }
    	return filteredOffers;
    }

    public boolean isValidTenureOffer(String parentSubscriptionDate, String offerTenure)
    {
    	boolean isValidTenureOffer = false;

    	if (StringUtils.isNotBlank(parentSubscriptionDate))
    	{
    		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    		LocalDate bday = LocalDate.parse(parentSubscriptionDate, formatter);
    		LocalDate today = LocalDate.now();
    		Period age = Period.between(bday, today);

    		int totalMonths = age.getYears() * 12 + age.getMonths();
    		isValidTenureOffer = (totalMonths >= Integer.parseInt(offerTenure))? true : false;
    	}
    	return isValidTenureOffer;
    }
    
    public boolean isValidSecondChanceOffer(String parentSubscriptionDate, String eligibilityTenure)
    {
    	boolean isValidSecondChanceOffer = false;

    	if (StringUtils.isNotBlank(parentSubscriptionDate))
    	{
    		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    		LocalDate bday = LocalDate.parse(parentSubscriptionDate, formatter);
    		LocalDate today = LocalDate.now();
    		long totalDays = ChronoUnit.DAYS.between(bday, today);

    		isValidSecondChanceOffer = (totalDays <= Integer.parseInt(eligibilityTenure))? true : false;
    	}
    	return isValidSecondChanceOffer;
    }


    /**
     *
     * @param ottOffers
     * @param offerRequest
     */

    private void updateProductActionBundle(List<CTOffer> ottOffers, OfferRequest offerRequest)
    {
    	if(Objects.nonNull(ottOffers)) {
    		ottOffers.stream().filter(Objects::nonNull).forEach(offer -> {
    			List<String> productCodesfromCustomerCtxt = new ArrayList<String>();
    			Map<String,String> bundleProductMap = new HashMap<String, String>();
    			if (Objects.nonNull(offerRequest.getCustomerContext())
    					&& Objects.nonNull(offerRequest.getCustomerContext().getOtt()))
    			{
    					offerRequest.getCustomerContext().getOtt().getProducts().forEach(prod -> {
    						productCodesfromCustomerCtxt.add(prod.getProductCode());
    				});
    			}

    			if (Objects.nonNull(offer.getAttributes().getBundleProductAction()))
    			{
    				offer.getAttributes().getBundleProductAction().forEach(bundleProd -> {
    					String action = bundleProd.getAction();
    					bundleProd.getProductIdOnAccount().forEach(prod -> {
    						bundleProductMap.put(prod.getKey(), action);
    					});
    				});
    			}

    			if (null != productCodesfromCustomerCtxt && productCodesfromCustomerCtxt.size() > 0
    					&& !MapUtils.isEmpty(bundleProductMap))
    			{
    				for (Map.Entry<String, String> entry : bundleProductMap.entrySet())
    				{
    					if (productCodesfromCustomerCtxt.contains(entry.getKey()))
    					{
    						log.info("Key :: "+entry.getKey());
    						updateProductAction(entry.getKey(), entry.getValue(), offer);
    						offer.getAttributes().setBundleProductAction(null);
    					}
    				}
    			}
    		});
    	}

	}

	private void updateProductAction(String key, String value, CTOffer offer) {
		if (Objects.nonNull(offer.getAttributes().getAssociatedProducts()))
			offer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull)
					.forEach(associatedProduct -> {
						if (Objects.nonNull(associatedProduct.getBundleProducts()))
						associatedProduct.getBundleProducts().stream().filter(Objects::nonNull)
								.forEach(bundleProduct -> {
									if (Objects.nonNull(bundleProduct.getProducts()))
									bundleProduct.getProducts().stream().filter(Objects::nonNull).forEach(prod -> {
										if (Objects.nonNull(prod.getObj().getVariants()))
											prod.getObj().getVariants().stream().filter(Objects::nonNull)
													.forEach(variants -> {
														if (Objects.nonNull(variants.getAttributes()))
															variants.getAttributes().setProductAction(value);
													});
									});
								});
					});
	}


    /**
	 * 
	 * @param processedCTOffers
	 */
	private void removeMobilityProductsIncluded(List<CTOffer> processedCTOffers) {
		if (Objects.nonNull(processedCTOffers)) {
			processedCTOffers.stream().filter(Objects::nonNull).forEach(offer -> {
				if (Objects.nonNull(offer.getAttributes().getAssociatedProducts()))
				offer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull)
						.forEach(associatedProduct -> {
							if (Objects.nonNull(associatedProduct.getBundleProducts()))
							associatedProduct.getBundleProducts().stream().filter(Objects::nonNull)
									.forEach(bundleProduct -> {
										if (Objects.nonNull(bundleProduct.getProducts()))
										bundleProduct.getProducts().stream().filter(Objects::nonNull).forEach(prod -> {
											if (Objects.nonNull(prod.getObj().getVariants()))
											prod.getObj().getVariants().stream().filter(Objects::nonNull)
													.forEach(variants -> {
														if (Objects.nonNull(variants.getAttributes()))														
														variants.getAttributes().setIncludedMobilityProducts(null);
													});
										});
									});

						});
			});
		}		
	}
    
    /**
     * 
     * @param processedCTOffers
     * @return
     */
	private void updateMobilityProductsAsIncluded(List<CTOffer> processedCTOffers) {
		
		if (Objects.nonNull(processedCTOffers)) {
			processedCTOffers.stream().filter(Objects::nonNull).forEach(offer -> {
				if (Objects.nonNull(offer.getAttributes().getAssociatedProducts()))
				offer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull)
						.forEach(associatedProduct -> {
							if (Objects.nonNull(associatedProduct.getBundleProducts()))
							associatedProduct.getBundleProducts().stream().filter(Objects::nonNull)
									.forEach(bundleProduct -> {
										if (Objects.nonNull(bundleProduct.getProducts()))
										bundleProduct.getProducts().stream().filter(Objects::nonNull).forEach(prod -> {
											if (Objects.nonNull(prod.getObj().getVariants()))
											prod.getObj().getVariants().stream().filter(Objects::nonNull)
													.forEach(variants -> {
														if (Objects.nonNull(variants.getAttributes()) && Objects.nonNull(variants.getAttributes().getIncludedMobilityProducts()))
														{
															List<IncludeProductWrapper> includedMobilityProducts = new ArrayList<IncludeProductWrapper>(
																		variants.getAttributes().getIncludedMobilityProducts());															
															variants.getAttributes().setIncludedProducts(includedMobilityProducts);
															variants.getAttributes().setIncludedMobilityProducts(null);
														}
													});
										});
									});

						});
			});
		}		
	}


    /**
     *
     * @param offerRequestWrapper
     * @param basePackageCompatibleList
     * @return
     */
    public List<CTOffer> getVideoDeviceOffers(OfferRequestWrapper offerRequestWrapper, List<String> basePackageCompatibleList, CustomerSubscriptionDetail customerSubscriptionDetail ) {
        log.info("Start Services getVideoDeviceOffers");
        List<CTOffer> localProcessedCTOffers = new ArrayList<>();
        if (Optional.ofNullable(offerRequestWrapper).isPresent()
                && Optional.ofNullable(offerRequestWrapper.getOfferRequest()).isPresent()
                && (((offerRequestWrapper.getOfferRequest().getOfferProductType()).contains(Constants.VIDEO_DEVICE))
                || offerRequestWrapper.getOfferRequest().getOfferProductType()
                .contains(Constants.VIDEO_ACCESSORY))) {

            List<CTOffer> ottOffers = dtvnServicesEquipmentOffersProcessor.retrieveEquipmentOffers(offerRequestWrapper,customerSubscriptionDetail);

            ottSalesOffersProcessorHelper.filterExpiredInstallmentOptions(ottOffers);
            // Filter compatible devices based on current base package
            if ( (Objects.nonNull(basePackageCompatibleList) && !basePackageCompatibleList.isEmpty())  && ottOffers != null) {
                ottOffers = filterCompatibleOfferForCurrentBasePackage(basePackageCompatibleList, ottOffers);
            }
            if (ottOffers != null) {
                //ottOffers = filterForOfferClassificationType(ottOffers, offerRequestWrapper);
                localProcessedCTOffers.addAll(ottOffers);
            }
        }
        log.info("End Services getVideoDeviceOffers");
        return localProcessedCTOffers;
    }

    /**
     *
     * @param offerRequestWrapper
     * @return
     */
    public List<CTOffer> getFeeOffers(OfferRequestWrapper offerRequestWrapper, CustomerSubscriptionDetail customerSubscriptionDetail) {
        log.info("Start Services getFeeOffers");
        List<CTOffer> localProcessedCTOffers = new ArrayList<>();
        if (offerRequestWrapper.getOfferRequest().getOfferProductType().contains(Constants.FEE) || 
        	offerRequestWrapper.getOfferRequest().getOfferProductType().contains(Constants.VIDEO_PLAN)) {

            List<CTOffer> feeOffers = dtvnServicesFeeOffersProcessor.retrieveFeeOffers(offerRequestWrapper);

            if (feeOffers != null) {
                localProcessedCTOffers.addAll(feeOffers);
            }
        }
        log.info("End Services getFeeOffers");
        return localProcessedCTOffers;
    }
    
    /**
     * Returns Protection Plan Offers 
     * @param offerRequestWrapper
     * @return
     */
	public List<CTOffer> getProtectionPlanOffers(OfferRequestWrapper offerRequestWrapper) {
		log.info("Start Services getProtectionPlanOffers");
		List<CTOffer> localProcessedCTOffers = new ArrayList<>();
		if (offerRequestWrapper.getOfferRequest().getOfferProductType().contains(Constants.PROTECTION_PLAN)) {

			List<CTOffer> protecionPlanOffers = dtvnServicesProtectionPlanProcessor.retrieveProtectionPlanOffers(offerRequestWrapper);

			if (protecionPlanOffers != null) {
				localProcessedCTOffers.addAll(protecionPlanOffers);
			}
		}
		log.info("End Services getProtectionPlanOffers");
		return localProcessedCTOffers;
	}
    
    public void setLeadOffers(OfferRequestWrapper offerRequestWrapper, CTOffer offer, Map<String, String> availablePackageGroup) {

        log.info("Start of OttAvaialbleOffersProcessor.setLeadOffers() method..");

        try {

            if (offer != null && offer.getAttributes() != null && offer.getAttributes().getEligibility() != null && offer.getAttributes().getEligibility().getConstraints() != null
                    && offer.getAttributes().getEligibility().getConstraints().get(0).getSalesChannel() != null) {
       
                    Long packageGroup = 0L;
                    AssociatedProduct associatedProduct = offer.getAttributes().getAssociatedProducts().get(0);
                    if (Optional.ofNullable(associatedProduct).isPresent()) {
                        List<ProductWrapper> bundleProducts = associatedProduct.getBundleProducts();
                        if (Optional.ofNullable(bundleProducts).isPresent() && !bundleProducts.isEmpty()) {
                            ProductWrapper bundleProduct = bundleProducts.get(0);
                            List<Product> products = bundleProduct.getProducts();
                            if (Optional.ofNullable(products).isPresent() && !products.isEmpty()) {
                                Product prod = products.get(0);
                                if (Optional.ofNullable(prod).isPresent() && Optional.ofNullable(prod.getObj()).isPresent()) {
                                    Attributes attributes = prod.getObj().getVariants().get(0).getAttributes();
                                    if (Optional.ofNullable(attributes).isPresent()) {
                                        if (Optional.ofNullable(attributes.getPackageGroup()).isPresent() && !attributes.getPackageGroup().isEmpty()) {
                                            packageGroup = Long.valueOf(attributes.getPackageGroup());
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (Objects.nonNull(availablePackageGroup) &&  Objects.nonNull(availablePackageGroup.get(Constants.LEAD)) &&
                            Long.parseLong(availablePackageGroup.get(Constants.LEAD)) == packageGroup ) {
                        offer.getAttributes().setLeadOffer(true);
                    } else {
                        offer.getAttributes().setLeadOffer(false);
                    }
         
            }



        } catch (Exception e) {
            log.error(" Exception Occured OttAvaialbleOffersProcessor.setLeadOffers:", e);
            if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferProductFamily()).isPresent()
					&& !offerRequestWrapper.getOfferRequest().getOfferProductFamily().isEmpty()
					&& !offerRequestWrapper.getOfferRequest().getOfferProductFamily().contains(null)
					&& offerRequestWrapper.getOfferRequest().getOfferProductFamily().stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
    			throw new ServiceException(ErrorMessages.OTT_CATALOGMS_INTERNALSERVER_ERROR)
    					.addDetail(ErrorMessages.OTT_CATALOGMS_INTERNALSERVER_ERROR_DETAILS001);
            } else {
    			throw new ServiceException(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR)
				.addDetail(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR_DETAILS001);
            }
        }
        log.info("End of OttAvaialbleOffersProcessor.setLeadOffers() method..");
    }


	private List<CTOffer> filterForOfferClassificationType(List<CTOffer> retrievedEquipmentOffers,
			OfferRequestWrapper offerRequestWrapper) {
		List<CTOffer> freeReplacementOffers = new ArrayList<>();

		try {

			for (CTOffer deviceReplacementOffer : retrievedEquipmentOffers) {
				if (offerRequestWrapper.getOfferRequest().getOfferClassificationType() != null
						&& deviceReplacementOffer.getAttributes().getOfferClassificationType() != null
						&& deviceReplacementOffer.getAttributes().getOfferClassificationType()
								.equalsIgnoreCase("replacement-device")) {
					freeReplacementOffers.add(deviceReplacementOffer);
				}

				if (offerRequestWrapper.getOfferRequest().getOfferClassificationType() == null
						&& deviceReplacementOffer.getAttributes().getOfferClassificationType() == null) {
					freeReplacementOffers.add(deviceReplacementOffer);

				}
			}

		} catch (Exception e) {
			log.error(String.format("filterForOfferClassificationType: %s", e.getMessage()));
		}

		return freeReplacementOffers;
	}

    /**
     * @param basePackageCompatibleList
     * @param processedCTOffers
     * @return
     */
	private List<CTOffer> filterQualifyingProducts(List<String> basePackageCompatibleList,
			List<CTOffer> processedCTOffers, OfferRequestWrapper offerRequestWrapper) {
		
		final List<String> basePackageProd = new ArrayList<String>();
        if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext()).isPresent() && Optional
                .ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()).isPresent()) {
            offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getProducts().forEach(prod -> {
                if (featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_QUALIFYINGPRODUCT_CHECKON_PRODUCTCODES)) {
                    basePackageProd.add(prod.getProductCode());
                }else {
                    if (prod.getProductType().equalsIgnoreCase(Constants.VIDEO_PLAN)) {
                        basePackageProd.add(prod.getProductCode());
                    }
                }
            });
        }
		List<CTOffer> offers = new ArrayList<>();
		if (Objects.nonNull(processedCTOffers)) {
			processedCTOffers.stream().filter(Objects::nonNull).forEach(offer -> {
				if (Objects.nonNull(offer.getAttributes())
						&& Objects.nonNull(offer.getAttributes().getAssociatedProducts())
						&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getQualifyingProducts())) {
					if (Optional.ofNullable(offer.getAttributes().isQualifyingProductCheckNOTRequired()).isPresent()
							&& offer.getAttributes().isQualifyingProductCheckNOTRequired()) {
						offers.add(offer);
					} else {
                        //code for checking multiple qualifying products
                        List<ProductWrapper> qualifyingProductsList = offer.getAttributes().getAssociatedProducts().get(0)
                                .getQualifyingProducts();
                        qualifyingProductsList.stream().filter(Objects::nonNull).forEach(
                                qualifyingProduct -> {
                                    List<Product> productList = qualifyingProduct.getProducts();
                                    if (Objects.nonNull(productList) && !productList.isEmpty()) {
                                        productList.stream().filter(Objects::nonNull).forEach(product -> {
                                            if (basePackageProd.contains(product.getKey())) {
                                                offers.add(offer);
                                            }
                                        });
                                    } else {
                                        offers.add(offer);
                                    }
                                }
                        );
                    }
				} else {
					offers.add(offer);
				}
			});
		}
		return offers;
	}
	
	 /**
     * @param basePackageCompatibleList
     * @param processedCTOffers
     * @return
     */
	private List<CTOffer> filterOffersWhenEligible(List<String> basePackageCompatibleList,
			List<CTOffer> processedCTOffers, OfferRequestWrapper offerRequestWrapper) {

		List<String> conflictingOffers = new ArrayList<>();
		List<CTOffer> filteredOffers = new ArrayList<>();
		if (Objects.nonNull(processedCTOffers)) {
			processedCTOffers.stream().filter(Objects::nonNull).forEach(offer -> {
				if (Objects.nonNull(offer.getAttributes())
						&& Objects.nonNull(offer.getAttributes().getConflictingOffers())) {
					if (offer.getAttributes().isSpecialOffer()) {
						offer.getAttributes().getConflictingOffers().stream().filter(Objects::nonNull)
								.forEach(conflictingOfferCode -> {
									conflictingOffers.add(conflictingOfferCode.getKey());
								});
					}
				}				
			});

			if (conflictingOffers.size() > 0) {
				processedCTOffers.stream().filter(Objects::nonNull).forEach(offer -> {
					if (!conflictingOffers.contains(offer.getCode())) {
						filteredOffers.add(offer);
					}
				});
			}
		}
		if(conflictingOffers.size() > 0) {
			return filteredOffers;	
		}else {
			return processedCTOffers;
		}
		
	}

    /**
     * @param basePackageCompatibleList
     * @param processedCTOffers
     * @return
     */
    private List<CTOffer> filterCompatibleOfferForCurrentBasePackage(List<String> basePackageCompatibleList, List<CTOffer> processedCTOffers) {
        List<CTOffer> offers = new ArrayList<>();
        List<CTOffer> filteredOutOffers = new ArrayList<>();
        List<CTOffer> filteredOutOffersInlane = new ArrayList<>();
        List<CTOffer> filteredOutOffersSwimlane = new ArrayList<>();
        if (Objects.nonNull(processedCTOffers)) {
            processedCTOffers.stream().filter(Objects::nonNull).forEach(offer -> {
                if (Objects.nonNull(offer.getAttributes())
                        && Objects.nonNull(offer.getAttributes().getAssociatedProducts())
                        && Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts())
                        && Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0))
                        && Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0))) {
                    boolean isFiltered = false;
                    if (!isFiltered && offer.getAttributes().isVirtualOffer() && basePackageCompatibleList.contains(offer.getAttributes().getBillingCode())) {
                        offers.add(offer);
                        isFiltered = true;
                    }

                    if (!isFiltered && !offer.getAttributes().isVirtualOffer() && basePackageCompatibleList.contains(offer.getAttributes().getAssociatedProducts().get(0)
                            .getBundleProducts().get(0).getProducts().get(0).getKey())) {
                        offers.add(offer);
                        isFiltered = true;
                    }

                    if (!isFiltered && Optional.ofNullable(offer.getAttributes().isQualifyingProductCheckNOTRequired()).isPresent()
                            && offer.getAttributes().isQualifyingProductCheckNOTRequired()) {
                        offers.add(offer);
                        isFiltered = true;
                    }
                    if (!isFiltered) {
                        filteredOutOffers.add(offer);
                    }

                }else {
                    filteredOutOffers.add(offer);
                }
            });
        }

        if(filteredOutOffers!=null){
            for (CTOffer ctOffer : filteredOutOffers) {
                if (ctOffer.getAttributes() != null) {
                    if (org.apache.commons.collections.CollectionUtils.isEmpty(ctOffer.getAttributes().getFlowIntents()) ||
                            (org.apache.commons.collections.CollectionUtils.isNotEmpty(ctOffer.getAttributes().getFlowIntents())
                                    && ctOffer.getAttributes().getFlowIntents().contains(Constants.INLANE)) ){
                        filteredOutOffersInlane =  filterCompatibleOfferForCurrentBasePackageBAU(basePackageCompatibleList,ctOffer);
                    } else if(org.apache.commons.collections.CollectionUtils.isEmpty(ctOffer.getAttributes().getFlowIntents()) ||
                            org.apache.commons.collections.CollectionUtils.isNotEmpty(ctOffer.getAttributes().getFlowIntents())
                                    && ctOffer.getAttributes().getFlowIntents().contains(Constants.SWIMLANE)) {
                        filteredOutOffersSwimlane.add(ctOffer);

                    }else {
                        filteredOutOffersInlane =  filterCompatibleOfferForCurrentBasePackageBAU(basePackageCompatibleList,ctOffer);
                    }
                }
            }
        }
        offers.addAll(filteredOutOffersInlane);
        offers.addAll(filteredOutOffersSwimlane);
        return offers;
    }

    private List<ProductWrapper> getBasePackageCompatibleListFromCT(String billingProductCode, boolean isMobility, boolean isEmployeeAccount, List<String> businessSegment,CustomerSubscriptionDetail customerSubscriptionDetail ) {
        List<ProductWrapper> productWrappers = null;
        CTProductRequest ctProductRequest = new CTProductRequest();
        ctProductRequest.setBusinessSegment(businessSegment);
        ctProductRequest.setProductCodes(Stream.of(billingProductCode).collect(Collectors.toList()));
        CTProductResponse ctProductResponse = cpopClientHelper.getProducts(ctProductRequest);

        if (Objects.nonNull(ctProductResponse) && Objects.nonNull(ctProductResponse.getProducts()) && !ctProductResponse.getProducts().isEmpty())
        {
        	if (isMobility) {
                productWrappers = ctProductResponse.getProducts().get(0).getVariants().get(0).getAttributes().getCompatibleMobilityProducts();
            }else if (isEmployeeAccount) {
				productWrappers = ctProductResponse.getProducts().get(0).getVariants().get(0).getAttributes().getCompatibleEmployeeProducts();
            }else {
                productWrappers = ctProductResponse.getProducts().get(0).getVariants().get(0).getAttributes().getCompatibleProducts();
            }
            customerSubscriptionDetail.setVideoPlanProductStatus(ctProductResponse.getProducts().get(0).getVariants().get(0).getAttributes().getProductStatus());
            customerSubscriptionDetail.setVideoPlanPackageCode(ctProductResponse.getProducts().get(0).getVariants().get(0).getAttributes().getPackageGroup());

        }
        return productWrappers;
    }


    /**
     * The method to retrieve the Lead offer and available Package group from dtvnRules
     *
     * @param subscriptionsContext
     * @param currentServiceInfo
     * @return Map<String, String>
     */
    Map<String, String> retrieveAvailablePackageGroup(CustomerSubscriptionDetail subscriptionsContext, Map<String, String> currentServiceInfo ) {

        log.info("START retrieveAvailablePackageGroup ");
        Map<String, String> leadAndAvailableGroup = new HashMap<>();
        List<DtvnMidasRule> dtvnRules = offersUtils.fetchDtvnMidasRules();
        if (dtvnRules != null) {
            log.info("In first if block subscriptionsContext.getDtvnRules() != null. ruleId: ");
            dtvnRules.forEach(dtvRule -> {
                if (dtvRule.getSalesChannel() != null
                        && dtvRule.getSalesChannel().contains(subscriptionsContext.getUserType())) {

                    if (checkValidContractIntent(subscriptionsContext, dtvRule)) {

                        if (subscriptionsContext.getAgentType() != null
                                && subscriptionsContext.getAgentType().equalsIgnoreCase(dtvRule.getAgentType())
                                && dtvRule.getAgentType().equalsIgnoreCase(Constants.SPECIAL)) {
                            leadAndAvailableGroup.put(Constants.LEAD, String.valueOf(dtvRule.getLeadGroup()));
                            leadAndAvailableGroup.put(Constants.AVAILABE_PACKAGE_GROUP, dtvRule.getAvailableGroup());
                        }

                    }
                }
            });
        }


        final boolean[] dtvRuleExists = new boolean[1];
        if (leadAndAvailableGroup.size() == 0 && dtvnRules != null) {
            dtvnRules.forEach(dtvnRule -> {
                if (!OffersUtils.checkExistingGroupNumberValue(dtvnRule.getExistingGroup())
                        && currentServiceInfo != null && currentServiceInfo.get(Constants.PACKAGE) != null
                        && !OffersUtils.checkExistingGroupNumberValue(currentServiceInfo.get(Constants.PACKAGE))) {
                    if (Integer.valueOf(currentServiceInfo.get(Constants.PACKAGE))
                            .equals(Integer.parseInt(dtvnRule.getExistingGroup()))) {
                        dtvRuleExists[0] = true;
                        if (dtvnRule.getSalesChannel() != null
                                && dtvnRule.getSalesChannel().contains(subscriptionsContext.getUserType())
                                && checkValidContractIntent(subscriptionsContext, dtvnRule)) {
                            leadAndAvailableGroup.put(Constants.LEAD, String.valueOf(dtvnRule.getLeadGroup()));
                            leadAndAvailableGroup.put(Constants.AVAILABE_PACKAGE_GROUP, dtvnRule.getAvailableGroup());
                        }
                    }
                }
                log.debug("leadAndAvailableGroup is null 2");
            });

            if (!dtvRuleExists[0]) {
                log.error("retrieveAvailablePackageGroup Error {} ", ErrorMessages.CTLG_DTVNOW_DTVRULE_NOT_EXIST); 
                //throw ((new ServiceException(ErrorMessages.CATALOG_VALIDATION_FAILED)).addDetail(ErrorMessages.CTLG_DTVNOW_DTVRULE_NOT_EXIST));
            }
        }

        log.info("END retrieveAvailablePackageGroup ");
        return leadAndAvailableGroup;
    }

    public boolean checkValidContractIntent(CustomerSubscriptionDetail subscriptionsContext, DtvnMidasRule dtvnRule) {
        if (Constants.CONTRACT.equalsIgnoreCase(subscriptionsContext.getContractIndicator()) ) {
            if (OffersUtils.getContractIntentForContract(dtvnRule)) {
                log.debug(
                        "In checkValidContractIntent(), subscriptionsContext.getContractIndicator is true & " +
                                "contractIntent is 'Contract'");
                return true;
            }
        } else if (Constants.NONCONTRACT.equalsIgnoreCase(subscriptionsContext.getContractIndicator())) {
            if (OffersUtils.getContractIntentForNonContract(dtvnRule)) {
                log.debug(
                        "In checkValidContractIntent(), subscriptionsContext.getContractIndicator is false & " +
                                "contractIntent is 'Retail'");
                return true;
            }
        } else if (Constants.EDSP_STRING.equalsIgnoreCase(subscriptionsContext.getContractIndicator())) {
            if (OffersUtils.getContractIntentForEdsp(dtvnRule)) {
                log.debug("In checkValidContractIntent(), subscriptionsContext.getContractIndicator is false & " +
                        "contractIntent is 'EDSP'");
                return true;
            }
		} else if (Constants.TAZ_STRING.equalsIgnoreCase(subscriptionsContext.getContractIndicator())) {
			if (OffersUtils.getContractIntentForTAZ(dtvnRule)) {
				log.debug("In checkValidContractIntent(), subscriptionsContext.getContractIndicator is false & " +
                        "contractIntent is 'TAZ'");
				return true;
			}
		} else if (Constants.TAZBYOD_STRING.equalsIgnoreCase(subscriptionsContext.getContractIndicator())) {
			if (OffersUtils.getContractIntentForTAZBYOD(dtvnRule)) {
				log.debug("In checkValidContractIntent(), subscriptionsContext.getContractIndicator is false & " +
                        "contractIntent is 'TAZBYOD'");
				return true;
			}
		} else if (Constants.TAZCONTRACT_STRING.equalsIgnoreCase(subscriptionsContext.getContractIndicator())) {
			if (OffersUtils.getContractIntentForTAZCONTRACT(dtvnRule)) {
				log.debug("In checkValidContractIntent(), subscriptionsContext.getContractIndicator is false & " +
                        "contractIntent is 'TAZCONTRACT'");
				return true;
			}
        } else if (Constants.FRONTPORCH_STRING.equalsIgnoreCase(subscriptionsContext.getContractIndicator())) {
            if (OffersUtils.getContractIntentForFRONTPORCH(dtvnRule)) {
                log.debug("In checkValidContractIntent(), subscriptionsContext.getContractIndicator is false & " +
                        "contractIntent is 'FRONTPORCH'");
                return true;
            }
        } else if (Constants.ROAD_RUNNER.equalsIgnoreCase(subscriptionsContext.getContractIndicator())) {
            if (OffersUtils.getContractIntentForRR(dtvnRule)) {
                log.debug("In checkValidContractIntent(), subscriptionsContext.getContractIndicator is false & " +
                        "contractIntent is 'ROAD_RUNNER'");
                return true;
            }
        }else if (Constants.GENRE.equalsIgnoreCase(subscriptionsContext.getContractIndicator())) {
            if (OffersUtils.getContractIntentForGENRE(dtvnRule)) {
                log.debug("In checkValidContractIntent(), subscriptionsContext.getContractIndicator is false & " +
                        "contractIntent is 'GENRE'");
                return true;
            }
        }
        return false;

    }


    private List<CTOffer> filterCTOfferResponse(CTOfferResponse cTOfferResponse, CustomerSubscriptionDetail subscriptionsContext,
                                                OfferRequestWrapper offerRequestWrapper, Map<String, String> currentServiceInfo) {

        List<CTOffer> ottOffers = cTOfferResponse.getOffers();

        return ottOffers
                .stream().filter(Objects::nonNull).filter(offer -> Optional.ofNullable(offerRequestWrapper).isPresent()
                        && (Optional.ofNullable(offer).isPresent() && Optional
                        .ofNullable(offer.getAttributes()).isPresent()
                        && Optional.ofNullable(offer.getAttributes().getAssociatedProducts()).isPresent()
                        && Optional.ofNullable(
                        offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts())
                        .isPresent()
                        && Optional.ofNullable(
                        offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0))
                        .isPresent()
                        && Optional.ofNullable(offer.getAttributes().getAssociatedProducts().get(0)
                        .getBundleProducts().get(0).getProducts().get(0)).isPresent())
                        && isBaseProductNotOnCustomerAccount(offer, subscriptionsContext, currentServiceInfo, offerRequestWrapper))
                .collect(Collectors.toList());

    }

    public boolean isBaseProductNotOnCustomerAccount(CTOffer offer, CustomerSubscriptionDetail subscriptionsContext,  Map<String, String> currentServiceInfo, OfferRequestWrapper offerRequestWrapper) {
        ProductObj productObj = offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj();
        boolean isProductNotOnCustomerAccount = false;
        List<String> bundleProductCodes = new ArrayList<>();
        subscriptionsContext.getBasePackageServiceIdList()
                .stream()
                .forEach(serviceId -> bundleProductCodes.add(serviceId));
        if(Optional.ofNullable(offerRequestWrapper.getOfferRequest().getSwimlaneSwitchEligible()).isPresent() &&
                offerRequestWrapper.getOfferRequest().getSwimlaneSwitchEligible() &&
                bundleProductCodes.contains(productObj.getVariants().get(0).getAttributes().getBillingProductCode()) && 
        		offer.getAttributes().getContractIndicator().equalsIgnoreCase("EDSP")) {
        	isProductNotOnCustomerAccount = true;
        }else if (!bundleProductCodes.contains(productObj.getVariants().get(0).getAttributes().getBillingProductCode())) {
            isProductNotOnCustomerAccount = true;
        } else {
            pupulateCurrentServiceInfo(productObj, currentServiceInfo);
            activePromoCatgory(offer, subscriptionsContext);
        }
        if(bundleProductCodes.contains(productObj.getVariants().get(0).getAttributes().getBillingProductCode()) && Objects.nonNull(productObj.getVariants().get(0).getAttributes().getDisplayType())
        		&& productObj.getVariants().get(0).getAttributes().getDisplayType().equalsIgnoreCase(Constants.INCLUDED)) {
        	isProductNotOnCustomerAccount = true;
        }
        return isProductNotOnCustomerAccount;
    }

    private void pupulateCurrentServiceInfo(ProductObj productObj, Map<String, String>  currentServiceInfo) {
        currentServiceInfo.put("productStatus", productObj.getVariants().get(0).getAttributes().getProductStatus());
        currentServiceInfo.put("packageGroup", productObj.getVariants().get(0).getAttributes().getPackageGroup());
    }

    /**
     * @param offer
     * @param subscriptionDetail
     * @param productInfo
     * @param salesChannel
     * @param freePromoMap
     * @param billingProductCode 
     * @return
     */
    private List<Benefit> eligiblePromosList(CTOffer offer, CustomerSubscriptionDetail subscriptionDetail,Set<String> activePromos, Map<String,Benefit> activeBenefits,
                                            com.dtv.dcp.epoch.processor.ott.services.model.CtProductInfo productInfo, String salesChannel, Map<String, Boolean> freePromoMap, String billingProductCode, Map<String, String> currentServiceInfo) {
        Set<Benefit> applicablePromosSet = new HashSet<>();
        List<String> activePromoIdsInCT = new ArrayList<>();

        List<Map<String, String>> offerPromosList = prepareCTOfferPromoMap(offer.getAttributes().getOfferPromos());


        List<Benefit> activeBenefit = new ArrayList<>();


        offerPromosList.forEach(promo -> {
            if (promo != null && OffersUtils.validateActiveDates(promo.get(Constants.CT_PROMO_START_DATE), promo.get(Constants.CT_PROMO_END_DATE))) {
                activePromoIdsInCT.add(promo.get(Constants.CT_PROMO_ID));
                for (Benefit ben : offer.getAttributes().getBenefits()) {
                    if (ben.getBillingBenefitCode() != null && ben.getBillingBenefitCode().equalsIgnoreCase(promo.get(Constants.CT_PROMO_ID))) {
                        activeBenefit.add(ben);
                    }
                }
            }
        });
        
        activePromos.stream().filter(Objects::nonNull).forEach(promo -> {
            try {
                promoContinuationUpgradeDowngrade(subscriptionDetail, promo, applicablePromosSet, productInfo, billingProductCode,activeBenefits, currentServiceInfo);
            } catch (ParseException e) {
                log.error("Error in method eligiblePromosList() method", e);
            }
        });


        if ("true".equalsIgnoreCase(subscriptionDetail.getIsDefaultPromo()) || "true".equalsIgnoreCase(subscriptionDetail.getFreeTrialEligble())) {
            activePromoIdsInCT.stream().filter(Objects::nonNull).forEach(activePromoIdInCT -> 
                addEligiblePromos(activeBenefit, applicablePromosSet, salesChannel, freePromoMap, billingProductCode, subscriptionDetail)
            );
        }


        if (subscriptionDetail.isAgent()) {
            activeBenefit.stream().filter(Objects::nonNull).forEach(benefit -> {
                if ((benefit.isAgentOffer() || benefit.isIoOffer()) && ottServicesOffersProcessorHelper.isSalesChannelValidForAgent(salesChannel)) {
                    applicablePromosSet.add(benefit);
                }
            });
        }

        return new ArrayList<>(applicablePromosSet);
    }


	private Set<String> getSubscriptionActivePromos(CustomerSubscriptionDetail subscriptionDetail) {
		List<CustomerServiceDetail> listBaseBackageServiceDetail = subscriptionDetail.getListBaseBackageServiceDetail();
        Map<String, Map<String, String>> accountPromoStartEndDate = !listBaseBackageServiceDetail.isEmpty()
                ? listBaseBackageServiceDetail.get(0).getAccountPromoStartEndDate()
                : new HashMap<>(); // this map contains customer's account promoId and their start and date date

        Set<String> activePromos = activePromoList(accountPromoStartEndDate);// return a set of all active promotions of
        // a customer's account.
		return activePromos;
	}

	private String getBillingProductCodeFromOffer(CTOffer offer) {
		String billingProductCode = "";
		AssociatedProduct associatedProduct = offer.getAttributes().getAssociatedProducts().get(0);
		List<ProductWrapper> cpopProducts = null;
		
		
		if(FREE_PROMO.equalsIgnoreCase(offer.getAttributes().getOfferType())) {
			cpopProducts = associatedProduct.getBundleProducts();
		}else {
			cpopProducts = associatedProduct.getQualifyingProducts();
		}
		
		if (Optional.ofNullable(cpopProducts).isPresent() && !cpopProducts.isEmpty()) {
			for (ProductWrapper product : cpopProducts) {
				List<Product> products = product.getProducts();
				if (Optional.ofNullable(products).isPresent() && !products.isEmpty()) {
					for (Product prod : products) {
						billingProductCode = prod.getObj().getVariants().get(0).getAttributes().getBillingProductCode();
					}
				}
			}
		}
      return billingProductCode;
	}

	private void addEligiblePromos(List<Benefit> benefits,Set<Benefit> applicablePromosSet, String salesChannel, Map<String, Boolean> freePromoMap, String billingProductCode, CustomerSubscriptionDetail customerSubscriptionDetail) {

        if (!CollectionUtils.isEmpty(benefits)) {
            for (Benefit benefit : benefits) {
                String promoType = benefit.getBenefitType();
                String promotionCategory = benefit.getBenefitCategory();
                String selfPromo = "PRODUCT".equalsIgnoreCase(benefit.getBenefitCategory()) ? "T" : "F";
                boolean freeTrialPromo = benefit.isFreeTrialPromo();
                String promoRateType = "";
                if (null != benefit.getContractType()) {
                    promoRateType = benefit.getContractType();
                }
                if (promotionCategory.equalsIgnoreCase("product") && selfPromo.equalsIgnoreCase(Constants.TRUE) && freeTrialPromo) {
                    if (customerSubscriptionDetail.isAgent()) {
                        if (salesChannel != null && ottServicesOffersProcessorHelper.isSalesChannelValidForAgent(salesChannel) && validatePromoContractIndicator(promoRateType, customerSubscriptionDetail.getContractIndicator())) {

                            applicablePromosSet.add(benefit);
                            if (promoType.equalsIgnoreCase("free")) {
                                freePromoMap.put(billingProductCode, Boolean.TRUE);
                            }
                        }
                    } else if (ottServicesOffersProcessorHelper.isSalesChannelValidForCustomer(salesChannel)
                            && validatePromoContractIndicator(promoRateType, customerSubscriptionDetail.getContractIndicator())) {
                        applicablePromosSet.add(benefit);
                        if (promoRateType.equalsIgnoreCase("free")) {
                            freePromoMap.put(billingProductCode, Boolean.TRUE);
                        }
                    }
                }
            }
        }
    }


    /**
     * @param accountPromoStartEndDate
     * @return return a set of all active promotions of a customer's account. active
     * promotions means , promoStartDate < today'ss date <= promoEndDate
     */
    private Set<String> activePromoList(Map<String, Map<String, String>> accountPromoStartEndDate) {
        Set<String> acivePromos = new HashSet<>();

        accountPromoStartEndDate.forEach((promo, startEndDate) -> {
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);
            if(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_PST_TIME_LOGIC_ENABLED)) {
            	sdf.setTimeZone(TimeZone.getTimeZone(Constants.PST));
            } else {
                sdf.setTimeZone(TimeZone.getTimeZone(Constants.GMT));
            }
            Map<String, Date> startAndEdnDate = OffersUtils.validateDateFormat(startEndDate);
            if (promo != null && startEndDate != null && startEndDate.get(Constants.PROMO_START_DATE) != null) {
                try {
                    Date date = new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(new Date()));

                    if (date != null ) {
                        if (startAndEdnDate.get(Constants.PROMO_END_DATE) != null) {
                        	Date endDate = startAndEdnDate.get(Constants.PROMO_END_DATE);
                        	if(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_PST_TIME_LOGIC_ENABLED)) {
                        		endDate = new SimpleDateFormat(Constants.DATE_TIME_FORMAT)
                                        .parse(sdf.format(startAndEdnDate.get(Constants.PROMO_END_DATE)));
                        	}
                            if (endDate.after(date) || endDate.equals(date)) {
                                acivePromos.add(promo);
                            }
                        } else {
                            acivePromos.add(promo);
                        }
                    }
                } catch (ParseException e) {
                    log.error(String.format("Invalid date present in the request - activePromoList %s", e));
                }
            } else {
                acivePromos.add(promo);
            }
        });

        return acivePromos;
    }

    private List<Map<String, String>> prepareCTOfferPromoMap(List<List<GenericNameValueBase>> offerPromoList) {

        List<Map<String, String>> offerPromosList = new ArrayList<>();
        if (offerPromoList != null) {
            offerPromoList.stream().filter(Objects::nonNull).forEach(benefit -> {
                Map<String, String> mapOfferPromoDeatils = new HashMap<>();
                if (Optional.ofNullable(benefit).isPresent() && !benefit.isEmpty()) {
                    for (GenericNameValueBase offerPromo : benefit) {
                        if (Optional.ofNullable(offerPromo.getName()).isPresent()
                                && offerPromo.getName().equalsIgnoreCase(Constants.CT_PROMO_ID)) {
                            mapOfferPromoDeatils.put(Constants.CT_PROMO_ID, offerPromo.getValue());
                        }
                        if (Optional.ofNullable(offerPromo.getName()).isPresent()
                                && offerPromo.getName().equalsIgnoreCase(Constants.CT_PROMO_START_DATE)) {
                            mapOfferPromoDeatils.put(Constants.CT_PROMO_START_DATE, offerPromo.getValue());
                        }
                        if (Optional.ofNullable(offerPromo.getName()).isPresent()
                                && offerPromo.getName().equalsIgnoreCase(Constants.CT_PROMO_END_DATE)) {
                            mapOfferPromoDeatils.put(Constants.CT_PROMO_END_DATE, offerPromo.getValue());
                        }
                        offerPromosList.add(mapOfferPromoDeatils);
                    }
                }
            });
        }
        return offerPromosList;
    }

    private CTOffer createAvailableBasePackageForDTVNow(OfferRequestWrapper offerRequestWrapper,CTOffer offer,
                                                        CustomerSubscriptionDetail customerSubscriptionDetail,Set<String> activePromos, Map<String,Benefit> activeBenefits, Map<String, String> availablePackageGroup, Map<String, String> currentServiceInfo) {
        CTOffer processedOffer = null;
        List<String> accountType = Optional.ofNullable(offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments()).isPresent() ?
        offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments() : null;
        String salesChannel = ottServicesOffersProcessorHelper.findSalesChannel(offer.getAttributes().getEligibility().getConstraints().get(0).getSalesChannel(),
                    offer.getAttributes().getOfferActionTypes().toString());
        List<ProductWrapper> cpopProducts = null;
        AssociatedProduct associatedProduct = offer.getAttributes().getAssociatedProducts().get(0);

        if (Optional.ofNullable(associatedProduct).isPresent()) {
            cpopProducts = associatedProduct.getBundleProducts();
        }

        if (cpopProducts != null && !cpopProducts.isEmpty()) {
            for (ProductWrapper product : cpopProducts) {
                processedOffer = filterProductBaseOnPackageGroup(offerRequestWrapper,offer, customerSubscriptionDetail,activePromos, activeBenefits, availablePackageGroup, accountType,
                        salesChannel, product, currentServiceInfo);
            }
        }

        return processedOffer;
    }

    /**
     * @param offer
     * @param customerSubscriptionDetail
     * @param availablePackageGroup
     * @param accountType
     * @param salesChannel
     * @param product
     */
    private CTOffer filterProductBaseOnPackageGroup(OfferRequestWrapper offerRequestWrapper,CTOffer offer, CustomerSubscriptionDetail customerSubscriptionDetail,Set<String> activePromos, Map<String,Benefit> activeBenefits,
                                                    Map<String, String> availablePackageGroup, List<String> accountType, String salesChannel,
                                                    ProductWrapper product, Map<String, String> currentServiceInfo) {
        List<Product> products = product.getProducts();
        if (Optional.ofNullable(products).isPresent() && products.isEmpty()) {
            return null;
        }        
        for (Product prod : products) {
            Long packageGroup = null;
            Attributes attributes = prod.getObj().getVariants().get(0).getAttributes();
            if (attributes.getPackageGroup() != null) {
                packageGroup = (Long.parseLong(attributes.getPackageGroup()));
            }
            String availableGroup = null;
            if (availablePackageGroup != null && availablePackageGroup.size() > 0
                    && availablePackageGroup.get(Constants.AVAILABE_PACKAGE_GROUP) != null) {
                availableGroup = availablePackageGroup.get(Constants.AVAILABE_PACKAGE_GROUP);
            }          
            if ((packageGroup != null && availableGroup != null
                    && availableGroup.contains(String.valueOf(packageGroup)))
                    && (accountType != null
                    && (CollectionUtils.isEmpty(customerSubscriptionDetail.getBasePackageServiceIdList())
                    || (Optional.ofNullable(offer.getAttributes().getSwimlaneSwitchEligiblePack()).isPresent()
                    		&& !offer.getAttributes().getSwimlaneSwitchEligiblePack().isEmpty()))
                    && accountType.contains(customerSubscriptionDetail.getAccountType()))
                    && (salesChannel != null
                    && (customerSubscriptionDetail.isAgent()
                    && ottServicesOffersProcessorHelper.isSalesChannelValidForAgent(salesChannel))
                    || ottServicesOffersProcessorHelper.isSalesChannelValidForCustomer(salesChannel))) {
            	
	            	setBenefitContinuation(offerRequestWrapper,offer, customerSubscriptionDetail,activePromos, activeBenefits, prod, attributes, salesChannel, currentServiceInfo);
	            	return offer;
            }
            else if ((packageGroup != null && availableGroup != null
                    && availableGroup.contains(String.valueOf(packageGroup)))
                    && (accountType != null
                    && (CollectionUtils.isEmpty(customerSubscriptionDetail.getBasePackageServiceIdList())
                    || !customerSubscriptionDetail.getBasePackageServiceIdList()
                    .contains(attributes.getBillingProductCode()))
                    && accountType.contains(customerSubscriptionDetail.getAccountType()))
                    && (salesChannel != null
                    && (
                    //       customerSubscriptionDetail.isAgent() &&
                    ottServicesOffersProcessorHelper.isSalesChannelValidForAgent(salesChannel))
                    || ottServicesOffersProcessorHelper.isSalesChannelValidForCustomer(salesChannel))) {

                setBenefitContinuation(offerRequestWrapper,offer, customerSubscriptionDetail,activePromos, activeBenefits, prod, attributes, salesChannel, currentServiceInfo);
                return offer;
            }
            else if ((packageGroup != null && availableGroup != null
                    && availableGroup.contains(String.valueOf(packageGroup)))
                    && (accountType != null
                    && (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerSegments()).isPresent() 
                    && offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.MDUTENANT))
                    && accountType.contains(customerSubscriptionDetail.getAccountType()))
                    && (salesChannel != null
                    && (customerSubscriptionDetail.isAgent()
                    && ottServicesOffersProcessorHelper.isSalesChannelValidForAgent(salesChannel))
                    || ottServicesOffersProcessorHelper.isSalesChannelValidForCustomer(salesChannel))) {

                setBenefitContinuation(offerRequestWrapper,offer, customerSubscriptionDetail,activePromos, activeBenefits, prod, attributes, salesChannel, currentServiceInfo);
                return offer;
            }
        }
        return null;
    }

    private void setBenefitContinuation(OfferRequestWrapper offerRequestWrapper,CTOffer offer, CustomerSubscriptionDetail customerSubscriptionDetail,Set<String> activePromos, Map<String,Benefit> activeBenefits,
                                        Product prod, Attributes attributes, String salesChannel, Map<String, String> currentServiceInfo) {

        String billingProductCode = getBillingProductCodeFromOffer(offer);
        CtProductInfo productInfo;
        productInfo = new CtProductInfo();
        if (attributes.getServiceFlag() != null && attributes.getServiceFlag()) {
            productInfo.setPackageType(BASIC_SERVICE);
        }

        Map<String, Boolean> freePromoMap = new HashMap<>();
        if (Optional.ofNullable(prod).isPresent() && Optional.ofNullable(prod.getObj()).isPresent()) {
            productInfo.setStatus(attributes.getProductStatus());
            if (offerRequestWrapper != null && offerRequestWrapper.getOfferRequest() != null
                    && offerRequestWrapper.getOfferRequest().getCustomerContext()!=null
                    && offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()!=null
                    && offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getNextBillingDate()!=null) {
            	setBaseAndContractNBCDPrice(prod, productInfo,
            			offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getNextBillingDate());
            } else {
            setBaseAndContractPrice(prod, productInfo);
            }
            List<Benefit> eligbleBenefitList = eligiblePromosList(offer, customerSubscriptionDetail,activePromos, activeBenefits, productInfo, salesChannel, freePromoMap,billingProductCode, currentServiceInfo);
            OfferPrice offerPrice = new OfferPrice();
            Double bestPrice = calculateBestPriceBasedOnPromoPriority(offerRequestWrapper,offer,productInfo, eligbleBenefitList, customerSubscriptionDetail, freePromoMap, "base",billingProductCode);
            if (bestPrice != null) {
                offerPrice.setDollarAmount(bestPrice);
            }
            offer.getAttributes().setOfferPrice(offerPrice);
            List<String> eligbleBenefitcode = new ArrayList<>();
            if(!CollectionUtils.isEmpty(eligbleBenefitList)) {
            	eligbleBenefitList.forEach(benefit -> 
	            	eligbleBenefitcode.add(benefit.getBillingBenefitCode())
	            );
            }
           String eligbleBenefitStr = String.join(",",  eligbleBenefitcode);
            prod.getObj().setBenefitContinuation(eligbleBenefitStr);

        }

    }


    private boolean validatePromoContractIndicator(String promoRateType, String contractIndicator) {
        boolean result = false;
		if ((Constants.CONTRACT.equalsIgnoreCase(contractIndicator)
				&& (promoRateType.equalsIgnoreCase("All") || promoRateType.equalsIgnoreCase("Contract")))
				|| (Constants.NONCONTRACT.equalsIgnoreCase(contractIndicator)
						&& (promoRateType.equalsIgnoreCase("All") || promoRateType.equalsIgnoreCase("Retail")))
				|| (Constants.EDSP_STRING.equalsIgnoreCase(contractIndicator)
						&& (promoRateType.equalsIgnoreCase("All") || promoRateType.equalsIgnoreCase("EDSP")))
				|| (Constants.TAZ_STRING.equalsIgnoreCase(contractIndicator)
						&& (promoRateType.equalsIgnoreCase("All") || promoRateType.equalsIgnoreCase("TAZ")))
				|| (Constants.TAZBYOD_STRING.equalsIgnoreCase(contractIndicator)
						&& (promoRateType.equalsIgnoreCase("All") || promoRateType.equalsIgnoreCase("TAZBYOD")))
				|| (Constants.TAZCONTRACT_STRING.equalsIgnoreCase(contractIndicator)
						&& (promoRateType.equalsIgnoreCase("All") || promoRateType.equalsIgnoreCase("TAZCONTRACT")))) {
            result = true;

        }
        return result;
    }

    /*
     * if , Price of the product is active based on start date and end date) if,
     * price.getContractIndicator() == "contract" set contractPrice/BasePrice =
     * price.getValue().getDollarAmount() else set nonContractPrice =
     * price.getValue().getDollarAmount()
     */
    private void setBaseAndContractPrice(Product prod, CtProductInfo productInfo) {
        String contractPrice = null;
        String nonContractPrice = null;
        String edspPrice = null;
        List<Price> prices = prod.getObj().getVariants().get(0).getPrices();
        if (Optional.ofNullable(prices).isPresent() && !prices.isEmpty()) {
            for (Price price : prices) {
                if (OffersUtils.validateActiveDates(OffersUtils.getFormattedDate(price.getStartDate()),
                        OffersUtils.getFormattedDate(price.getEndDate()))) {
                    if (Optional.ofNullable(price.getContractIndicator()).isPresent()
                            && price.getContractIndicator().equalsIgnoreCase(Constants.CONTRACT)
                            && Optional.ofNullable(price.getValue()).isPresent()
                            && Optional.ofNullable(price.getValue().getDollarAmount()).isPresent()) {
                        contractPrice = String.valueOf(price.getValue().getDollarAmount());
                    }else if (Optional.ofNullable(price.getContractIndicator()).isPresent()
                            && price.getContractIndicator().equalsIgnoreCase(Constants.EDSP_STRING)
                            && Optional.ofNullable(price.getValue()).isPresent()
                            && Optional.ofNullable(price.getValue().getDollarAmount()).isPresent()) {
                    	edspPrice = String.valueOf(price.getValue().getDollarAmount());
                    }else if (Optional.ofNullable(price.getValue()).isPresent()
                            && Optional.ofNullable(price.getValue().getDollarAmount()).isPresent()) {
                        nonContractPrice = String.valueOf(price.getValue().getDollarAmount());
                    }
                }
            }

            productInfo.setBasePrice(nonContractPrice);
            productInfo.setContractPrice(contractPrice);
            productInfo.setEdspPrice(edspPrice);
        }

    }
    
    private void setBaseAndContractNBCDPrice(Product prod, CtProductInfo productInfo, String nbcDate) {
        String contractPrice = null;
        String nonContractPrice = null;
        String edspPrice = null;
        List<Price> prices = prod.getObj().getVariants().get(0).getPrices();
        if (Optional.ofNullable(prices).isPresent() && !prices.isEmpty()) {
            for (Price price : prices) {
                if (OffersUtils.validateActiveNBCDDates(OffersUtils.getFormattedDate(price.getStartDate()),
                        OffersUtils.getFormattedDate(price.getEndDate()),nbcDate )) {
                    if (Optional.ofNullable(price.getContractIndicator()).isPresent()
                            && price.getContractIndicator().equalsIgnoreCase(Constants.CONTRACT)
                            && Optional.ofNullable(price.getValue()).isPresent()
                            && Optional.ofNullable(price.getValue().getDollarAmount()).isPresent()) {
                        contractPrice = String.valueOf(price.getValue().getDollarAmount());
                    }else if (Optional.ofNullable(price.getContractIndicator()).isPresent()
                            && price.getContractIndicator().equalsIgnoreCase(Constants.EDSP_STRING)
                            && Optional.ofNullable(price.getValue()).isPresent()
                            && Optional.ofNullable(price.getValue().getDollarAmount()).isPresent()) {
                    	edspPrice = String.valueOf(price.getValue().getDollarAmount());
                    }else if (Optional.ofNullable(price.getValue()).isPresent()
                            && Optional.ofNullable(price.getValue().getDollarAmount()).isPresent()) {
                        nonContractPrice = String.valueOf(price.getValue().getDollarAmount());
                    }
                }
            }

            productInfo.setBasePrice(nonContractPrice);
            productInfo.setContractPrice(contractPrice);
            productInfo.setEdspPrice(edspPrice);
        }

    }

    /**
     * @param context
     * @param promo
     * @param applicablePromosSet
     * @param productInfo
     * @throws ParseException 1st Condition : if CT tells that product status for product on
     *                        customer's account is "Grandfather" and status of offered product
     *                        is active && accountPromoEndDate >= next billing date then set
     *                        offered product promo in applicablePromosSet 2nd Condition : if
     *                        not : CT tells that product status for product on customer's
     *                        account is "Grandfather" and price of available product > price
     *                        of the product on Customer's account , then set offered product
     *                        promo in applicablePromosSet 3rd Condition : if not : CT tells
     *                        that product status for product on customer's account is
     *                        "Grandfather" and if not : price of available product > price of
     *                        the product on Customer's account and and if accountPromoEndDate
     *                        => nextBillingDate then set offered product promo in
     *                        applicablePromosSet
     */
    private void promoContinuationUpgradeDowngrade(CustomerSubscriptionDetail context, String promo,
                                                   Set<Benefit> applicablePromosSet, CtProductInfo productInfo,String billingProductCode,Map<String,Benefit> activeBenefits, Map<String, String> currentServiceInfo) throws ParseException {
    	// Getting Benefit details of the Benefit Continuation Promo
    	Benefit benefit =  !CollectionUtils.isEmpty(activeBenefits) ? activeBenefits.get(promo) : null;
    	Date nextBillingDate = context.getNextBillingDate();
        Double availableBasePackagePrice = productInfo.getBasePrice() != null ? Double.parseDouble(productInfo.getBasePrice()) : null;
        List<CustomerServiceDetail> listBaseBackageServiceDetail = context.getListBaseBackageServiceDetail();
        Map<String, Map<String, String>> accountPromoStartEndDate = !listBaseBackageServiceDetail.isEmpty()
                ? listBaseBackageServiceDetail.get(0).getAccountPromoStartEndDate()
                : new HashMap<>();

        Double accountPrice = !listBaseBackageServiceDetail.isEmpty()
                ? listBaseBackageServiceDetail.get(0).getAccountPrice()
                : 0.0;

        if(isBenefitEligibleForProduct(billingProductCode, benefit)) {
	        if (accountPromoStartEndDate != null && accountPromoStartEndDate.get(promo) != null
	                && accountPromoStartEndDate.get(promo).get(Constants.PROMO_END_DATE) != null && accountPrice != null
	                && nextBillingDate != null && productInfo.getBasePrice() != null) {
	
	            Date accountPromoEndDate = new SimpleDateFormat(Constants.DATE_FORMAT)
	                    .parse(accountPromoStartEndDate.get(promo).get(Constants.PROMO_END_DATE));
	            if (currentServiceInfo != null && currentServiceInfo.get(Constants.STATUS) != null
	                    && currentServiceInfo.get(Constants.STATUS).equalsIgnoreCase(Constants.GRANDFATHER)
	                    && ("active").equalsIgnoreCase(productInfo.getStatus())) {
	                if (accountPromoEndDate != null && (accountPromoEndDate.after(nextBillingDate)
	                        || accountPromoEndDate.equals(nextBillingDate))) {
	                    applicablePromosSet.add(benefit);
	                }
	            } else {
	                if (availableBasePackagePrice >= accountPrice) {
	                    applicablePromosSet.add(benefit);
	                } else {
	                    if (accountPromoEndDate != null && (accountPromoEndDate.after(nextBillingDate)
	                            || accountPromoEndDate.equals(nextBillingDate))) {
	                        applicablePromosSet.add(benefit);
	                    }
	                }
	            }
	        } else {
	            applicablePromosSet.add(benefit);
	        }
        }
    }

    private boolean isBenefitEligibleForProduct(String billingProductCode, Benefit benefit) {
    	final boolean[] eligible = new boolean[1];
    	eligible[0] = false;
        if(Objects.nonNull(benefit) && !CollectionUtils.isEmpty(benefit.getApplicableProducts())) {
            benefit.getApplicableProducts().stream().filter(Objects::nonNull).forEach(productWrapper -> {
            	if(Objects.nonNull(productWrapper) && !CollectionUtils.isEmpty(productWrapper.getProducts())) {
                    List<String> productCodes = productWrapper.getProducts().stream().filter(Objects::nonNull).map(product -> product.getKey()).collect(Collectors.toList());
                    if(OffersUtils.isMatchFound(productCodes, billingProductCode)){
                    	eligible[0] = true;
                    } 
            	}
            });
        }
       return eligible[0];
    }
    private Map<String,Benefit> getBenefitsByBillingBenfitCodes(Set<String> benefitCodes) {
    	Map<String,Benefit> benefits = null;
        log.info("START getBenefitsByBillingBenfitCodes");
    	if(!CollectionUtils.isEmpty(benefitCodes)) {
	    	BenefitRequestWrapper benefitRequestWrapper = new BenefitRequestWrapper();
	    	BenefitRequest benefitRequest = new BenefitRequest();
	    	benefitRequest.setBenefitCodes(benefitCodes.stream().collect(Collectors.toList()));
	    	benefitRequestWrapper.setBenefitRequest(benefitRequest);
	    
	    	CTBenefitsResponse ctBenefitsResponse = ottServicesBenefitsProcessor.getBenefits(benefitRequestWrapper);
	    	
	    	if(Objects.nonNull(ctBenefitsResponse) && !CollectionUtils.isEmpty(ctBenefitsResponse.getBenefits())) {
	    		try {
					benefits = ctBenefitsResponse.getBenefits().stream().collect(Collectors.toMap(Benefit::getBillingBenefitCode, benefit -> benefit));
				} catch (Exception e) {
					log.error("Error in getBenefitsByBillingBenfitCodes()", e);
				}
	    	}
    	}
    	log.info("END getBenefitsByBillingBenfitCodes");
		return benefits;
	}

	private CustomerSubscriptionDetail populateCustomerSubscriptionDetail(OfferRequestWrapper offerRequestWrapper) throws ParseException {


        CustomerSubscriptionDetail customerAccountDetail = new CustomerSubscriptionDetail();
        List<String> addOnServiceIdList = new ArrayList<>();
        List<String> deviceServiceIdList = new ArrayList<>();
        List<String> basePackageServiceIdList = new ArrayList<>();
        List<CustomerServiceDetail> listAddOnServiceDetail = new ArrayList<>();
        List<CustomerServiceDetail> listDeviceServiceDetail = new ArrayList<>();
        List<CustomerServiceDetail> listBaseBackageServiceDetail = new ArrayList<>();
        String agentType = null;
        String isDefaultPromo = FALSE;
        CGResponse cGResponse = null;

        String agentHeader = offerRequestWrapper.getAgentType();

        customerAccountDetail.setUserType(ottServicesOffersProcessorHelper.findSalesChannel(offerRequestWrapper.getOfferRequest().getSalesChannel(), offerRequestWrapper.getOfferRequest().getOfferActionType().get(0)));

        if (Optional.ofNullable(agentHeader).isPresent() && !agentHeader.isEmpty()) {
            List<String> agentTypes = Arrays.asList(agentHeader.trim().split(","));
            for (String type : agentTypes)
                if (type.equalsIgnoreCase(SPECIAL)) {//could be VIEW,MODIFY,SPECIAL,ADJUST
                    agentType = type;
                }
        }

        customerAccountDetail.setAgentType(agentType);

        if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext()).isPresent() &&
                Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()).isPresent()) {

            isDefaultPromo = (offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getFreeTrialEligible() != null && offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getFreeTrialEligible() == true) ? TRUE : FALSE;


            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT);
            if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getNextBillingDate()).isPresent()) {
                LocalDate nextBillDate = LocalDate.parse(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getNextBillingDate(), formatter);
                Date nextBillingDate = java.util.Date.from(nextBillDate.atStartOfDay()
                        .atZone(ZoneId.systemDefault())
                        .toInstant());
                customerAccountDetail.setNextBillingDate(nextBillingDate);
            }

            customerAccountDetail.setContractIndicator(offerRequestWrapper.getOfferRequest().getContractIndicator().get(0));
            customerAccountDetail.setIsDefaultPromo(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getIsDefaultPromo() != null &&
                    offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getIsDefaultPromo() ? TRUE : FALSE);
            customerAccountDetail.setFreeTrialEligble(isDefaultPromo);

            String accountType = "";
            if(offerRequestWrapper.isMobility())
            {
                accountType = Constants.MOBILITY_WITH_CAPITAL_M;
            }else if(offerRequestWrapper.isEmployeeAccount()){
                accountType = Constants.EMPLOYEE;
            }else{
                String customerAccountType = offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getAccountType();
                if(StringUtils.isNotBlank(customerAccountType))
                {
                    accountType = customerAccountType;
                    if(Constants.MOBILITY_WITH_CAPITAL_M.equalsIgnoreCase(accountType)) {
                    	offerRequestWrapper.setMobility(true);
                    }else if(Constants.EMPLOYEE.equalsIgnoreCase(accountType)) {
                    	offerRequestWrapper.setEmployeeAccount(true);
                    }                    
                }else{
                    accountType = Constants.RESIDENTIAL_ACCOUNT_TYPE;
                }
            }
            customerAccountDetail.setAccountType(accountType);

            for (ProductInfo product : offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getProducts()) {
                if (Constants.VIDEO_PLAN.equals(product.getProductType())) {
                    basePackageServiceIdList.add(product.getProductCode());
                    CustomerServiceDetail customerServiceDetail = new CustomerServiceDetail();
                    customerServiceDetail.setAccountPrice(Double.parseDouble(Objects.nonNull(product.getBasePrice()) ? product.getBasePrice() : "0.0"));
                    customerServiceDetail.setTypeOfPlan(BASIC_SERVICE);
                    if (product.getPromotions() != null) {
                        for (CustomerPromotion cPromo : product.getPromotions()) {
                            Map<String, String> promoMap = new HashMap<>();

                            promoMap.put(Constants.PROMO_START_DATE, cPromo.getPromotionStartDate());
                            promoMap.put(Constants.PROMO_END_DATE, cPromo.getPromotionEndDate());
                            promoMap.put(Constants.IS_DEFAULT_PROMO, isDefaultPromo);
                            customerServiceDetail.getAccountPromoStartEndDate().put(cPromo.getPromotionId(), promoMap);
                        }
                    }
                    customerServiceDetail.setPromosId(customerServiceDetail.getAccountPromoStartEndDate().keySet().stream().collect(Collectors.toList()));
                    listBaseBackageServiceDetail.add(customerServiceDetail);
                }

                if (Constants.VIDEO_ADDON.equals(product.getProductType())) {
                    addOnServiceIdList.add(product.getProductCode());
                    CustomerServiceDetail customerServiceDetail = new CustomerServiceDetail();
                    customerServiceDetail.setAccountPrice(Double.parseDouble(Objects.nonNull(product.getBasePrice()) ? product.getBasePrice() : "0.0"));
                    customerServiceDetail.setTypeOfPlan("Add-on");
                    if (product.getPromotions() != null) {
                        for (CustomerPromotion cPromo : product.getPromotions()) {
                            Map<String, String> promoMap = new HashMap<>();

                            promoMap.put(Constants.PROMO_START_DATE, cPromo.getPromotionStartDate());
                            promoMap.put(Constants.PROMO_END_DATE, cPromo.getPromotionEndDate());
                            promoMap.put(Constants.IS_DEFAULT_PROMO, isDefaultPromo);
                            customerServiceDetail.getAccountPromoStartEndDate().put(cPromo.getPromotionId(), promoMap);
                        }
                    }
                    customerServiceDetail.setPromosId(customerServiceDetail.getAccountPromoStartEndDate().keySet().stream().collect(Collectors.toList()));
                    listAddOnServiceDetail.add(customerServiceDetail);
                }
                
                
                if (Constants.VIDEO_DEVICE.equals(product.getProductType()) 
                		&& Optional.ofNullable(product.getProductCode()).isPresent()
                		&& !product.getProductCode().contains("REFURB")) {
                    CustomerServiceDetail customerServiceDetail = new CustomerServiceDetail();
                    customerServiceDetail.setPlanCode(product.getProductCode());
                    customerServiceDetail.setPlanCount(1);
                    customerServiceDetail.setAccountPrice(Double.parseDouble(Objects.nonNull(product.getBasePrice()) ? product.getBasePrice() : "0.0"));
                    customerServiceDetail.setTypeOfPlan("Device");
                    if (product.getPromotions() != null) {
                        for (CustomerPromotion cPromo : product.getPromotions()) {
                            Map<String, String> promoMap = new HashMap<>();

                            promoMap.put(Constants.PROMO_START_DATE, cPromo.getPromotionStartDate());
                            promoMap.put(Constants.PROMO_END_DATE, cPromo.getPromotionEndDate());
                            promoMap.put(Constants.IS_DEFAULT_PROMO, isDefaultPromo);
                            customerServiceDetail.getAccountPromoStartEndDate().put(cPromo.getPromotionId(), promoMap);
                        }
                    }
                    customerServiceDetail.setPromosId(customerServiceDetail.getAccountPromoStartEndDate().keySet().stream().collect(Collectors.toList()));

                	
                	if(deviceServiceIdList.contains(product.getProductCode())) {
                		listDeviceServiceDetail.forEach(detail ->{
                			if(detail.getPlanCode().equalsIgnoreCase(product.getProductCode())) {
                				detail.setPlanCount(detail.getPlanCount()+1);
                				detail.getPromosId().addAll(customerServiceDetail.getPromosId());
                			}
                		});
                	}else {
                	deviceServiceIdList.add(product.getProductCode());
                    listDeviceServiceDetail.add(customerServiceDetail);
                	}
                }
                
            }
            
            

        } else if ((StringUtils.isNotBlank(offerRequestWrapper.getDtvnAccount()))) {

            cGResponse = customerGraphServiceImpl.getActiveSubscriptions(offerRequestWrapper.getDtvnAccount(), featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP) ? "backup" : "primary");
            //Getting the list from eligible products
            CGServiceInfo[] cgServiceInfos = cGResponse.getServiceInfo();
            if (cgServiceInfos != null) {

                Date nextBillingDate = new Date(Long.parseLong(cGResponse.getBillingInfo().getNextBillingDateTime()));

                customerAccountDetail.setNextBillingDate(nextBillingDate);
                customerAccountDetail.setContractIndicator(cGResponse.getAccountInfo().isIsPremiumCustomer() ? Constants.CONTRACT : Constants.NONCONTRACT);
                customerAccountDetail.setAccountType(cGResponse.getAccountInfo().getAccountType());
                customerAccountDetail.setFreeTrialEligble(cGResponse.getAccountInfo().getIsEligibleForFreeTrial());

                for (CGServiceInfo serviceInfo : Arrays.asList(cGResponse.getServiceInfo())) {
                    if (BASIC_SERVICE.equals(serviceInfo.getTypeOfPlan())) {
                        basePackageServiceIdList.add(serviceInfo.getServiceID());
                        CustomerServiceDetail customerServiceDetail = new CustomerServiceDetail();
                        customerServiceDetail.setAccountPrice(Double.parseDouble(serviceInfo.getRetailPrice()));
                        customerServiceDetail.setTypeOfPlan(BASIC_SERVICE);
                        if (serviceInfo.getPromotion() != null) {
                            for (CGPromotion cGpromo : serviceInfo.getPromotion()) {
                                Map<String, String> promoMap = new HashMap<>();

                                promoMap.put(Constants.PROMO_START_DATE, convertLongDateToStringDate(cGpromo.getStartDate()));
                                promoMap.put(Constants.PROMO_END_DATE, convertLongDateToStringDate(cGpromo.getEndDate()));
                                promoMap.put(Constants.IS_DEFAULT_PROMO, cGpromo.getIsDefaultPromo());
                                if (TRUE.equalsIgnoreCase(cGpromo.getIsDefaultPromo())) {
                                    isDefaultPromo = TRUE;
                                }
                                customerServiceDetail.getAccountPromoStartEndDate().put(cGpromo.getPromotionId(), promoMap);
                            }
                        }
                        customerServiceDetail.setPromosId(customerServiceDetail.getAccountPromoStartEndDate().keySet().stream().collect(Collectors.toList()));
                        listBaseBackageServiceDetail.add(customerServiceDetail);
                    }

                    if ("Bolt-on".equals(serviceInfo.getTypeOfPlan())) {
                        addOnServiceIdList.add(serviceInfo.getServiceID());
                        CustomerServiceDetail customerServiceDetail = new CustomerServiceDetail();
                        customerServiceDetail.setAccountPrice(Double.parseDouble(serviceInfo.getRetailPrice()));
                        customerServiceDetail.setTypeOfPlan("Add-on");
                        if (serviceInfo.getPromotion() != null) {
                            for (CGPromotion cGpromo : serviceInfo.getPromotion()) {
                                Map<String, String> promoMap = new HashMap<>();

                                promoMap.put(Constants.PROMO_START_DATE, convertLongDateToStringDate(cGpromo.getStartDate()));
                                promoMap.put(Constants.PROMO_END_DATE, convertLongDateToStringDate(cGpromo.getEndDate()));
                                promoMap.put(Constants.IS_DEFAULT_PROMO, cGpromo.getIsDefaultPromo());
                                if (TRUE.equalsIgnoreCase(cGpromo.getIsDefaultPromo())) {
                                    isDefaultPromo = TRUE;
                                }
                                customerServiceDetail.getAccountPromoStartEndDate().put(cGpromo.getPromotionId(), promoMap);
                            }
                        }
                        customerServiceDetail.setPromosId(customerServiceDetail.getAccountPromoStartEndDate().keySet().stream().collect(Collectors.toList()));
                        listAddOnServiceDetail.add(customerServiceDetail);
                    }
                    
                    if (Optional.ofNullable(serviceInfo.getServiceID()).isPresent()
                    	&&	serviceInfo.getServiceID().equalsIgnoreCase("HARD-OSPREY-042019")) {
                        deviceServiceIdList.add(serviceInfo.getServiceID());
                        CustomerServiceDetail customerServiceDetail = new CustomerServiceDetail();
                        customerServiceDetail.setAccountPrice(Double.parseDouble(serviceInfo.getRetailPrice()));
                        customerServiceDetail.setTypeOfPlan("Device");
                        if (serviceInfo.getPromotion() != null) {
                            for (CGPromotion cGpromo : serviceInfo.getPromotion()) {
                                Map<String, String> promoMap = new HashMap<>();

                                promoMap.put(Constants.PROMO_START_DATE, convertLongDateToStringDate(cGpromo.getStartDate()));
                                promoMap.put(Constants.PROMO_END_DATE, convertLongDateToStringDate(cGpromo.getEndDate()));
                                promoMap.put(Constants.IS_DEFAULT_PROMO, cGpromo.getIsDefaultPromo());
                                if (TRUE.equalsIgnoreCase(cGpromo.getIsDefaultPromo())) {
                                    isDefaultPromo = TRUE;
                                }
                                customerServiceDetail.getAccountPromoStartEndDate().put(cGpromo.getPromotionId(), promoMap);
                            }
                        }
                        customerServiceDetail.setPromosId(customerServiceDetail.getAccountPromoStartEndDate().keySet().stream().collect(Collectors.toList()));
                        listDeviceServiceDetail.add(customerServiceDetail);
                    }
                    
                }
            }
        }

        customerAccountDetail.setIsDefaultPromo(isDefaultPromo);
        customerAccountDetail.setAddOnServiceIdList(addOnServiceIdList);
        customerAccountDetail.setListAddOnServiceDetail(listAddOnServiceDetail);
        customerAccountDetail.setDeviceServiceIdList(deviceServiceIdList);
        customerAccountDetail.setListDeviceServiceDetail(listDeviceServiceDetail);
        customerAccountDetail.setBasePackageServiceIdList(basePackageServiceIdList);
        customerAccountDetail.setListBaseBackageServiceDetail(listBaseBackageServiceDetail);
        log.info("END populateCustomerSubscriptionDetail");
        return customerAccountDetail;
    }


    private String convertLongDateToStringDate(String date) {
        if (date != null) {
            return new SimpleDateFormat("MM/dd/yyyy").format(new Date(Long.parseLong(date)));
        }
        return null;
    }

    public static String convertObjectToJson(Object obj) {
        String str = null;
        try {
            str = MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Error in method convertObjectToJson method", e);
        }
        return str;

    }


    public Double calculateBestPriceBasedOnPromoPriority(OfferRequestWrapper offerRequestWrapper,CTOffer offer,CtProductInfo productInfo, List<Benefit> eligbleBenefitList, CustomerSubscriptionDetail customerSubscriptionDetail2, Map<String, Boolean> freePromoMap, String productType, String billingProductCode) {

        BestPrice price = new BestPrice();

        if ((isFreePromoCategories(freePromoMap, billingProductCode) || isFreePromoType(eligbleBenefitList))
                && productType != null && !productType.equalsIgnoreCase(ADDON)) {
            price.setBestPrice(0.00);
        } else if (!CollectionUtils.isEmpty(eligbleBenefitList)) {
            if (Objects.nonNull(productInfo) ) {
            	if(Constants.CONTRACT.equalsIgnoreCase(offer.getAttributes().getContractIndicator()) ) {
	                if(Objects.nonNull(productInfo.getContractPrice())) {
	                	price.setBasePrice(Double.parseDouble(productInfo.getContractPrice()));
	                }
            	}else if(Constants.EDSP_STRING.equalsIgnoreCase(offer.getAttributes().getContractIndicator()) ) {
	                if(Objects.nonNull(productInfo.getEdspPrice())) {
	                	price.setBasePrice(Double.parseDouble(productInfo.getEdspPrice()));
	                }
            	}else if (Objects.nonNull(productInfo.getBasePrice())) {
            		price.setBasePrice(Double.parseDouble(productInfo.getBasePrice()));
            	}
            }
        	eligbleBenefitList.sort(Comparator.comparing(Benefit::getPromoPriority));
        	eligbleBenefitList.stream().filter(Objects::nonNull).forEach(benefit -> {

                if (null != benefit) {
                	 if(Optional.ofNullable(offerRequestWrapper.getOfferRequest().getSwimlaneSwitchEligible()).isPresent() &&
                             !offerRequestWrapper.getOfferRequest().getSwimlaneSwitchEligible()){
	                    if (Optional.ofNullable(benefit.getValue()).isPresent() && 
	                    	Optional.ofNullable(benefit.getValue().getPercentage()).isPresent() && 
	                    	benefit.getValue().getPercentage() != 0) {
	                        setBestPriceForPercentageOff(price, customerSubscriptionDetail2, productType,
	                                benefit, benefit.getBillingBenefitCode());
	
	                    } else if (String.valueOf(benefit.getValue().getDollarAmount()) != null) {
	                        setBestPriceForFlatOff(price, customerSubscriptionDetail2, productType,
	                                benefit, benefit.getBillingBenefitCode());
	                    } else if (price.getBestPrice() == null || (price.getBasePrice() != null && price.getBasePrice() < 0)) {
	                        price.setBestPrice(price.getBasePrice());
	                    }
                	 }   
                }
            });
        } else {
            if (Objects.nonNull(productInfo)) {
            	if(Constants.CONTRACT.equalsIgnoreCase(offer.getAttributes().getContractIndicator())) {
	                if(Objects.nonNull(productInfo.getContractPrice())) {
	            		price.setBasePrice(Double.parseDouble(productInfo.getContractPrice()));
		                price.setBestPrice(Double.parseDouble(productInfo.getContractPrice()));
	                }
            	}else if(Constants.EDSP_STRING.equalsIgnoreCase(offer.getAttributes().getContractIndicator()) ) {
	                if(Objects.nonNull(productInfo.getEdspPrice())) {
	                	price.setBasePrice(Double.parseDouble(productInfo.getEdspPrice()));
	                	price.setBestPrice(Double.parseDouble(productInfo.getEdspPrice()));
	                }
            	}else if(Objects.nonNull(productInfo.getBasePrice())) {
                    price.setBasePrice(Double.parseDouble(productInfo.getBasePrice()));
                    price.setBestPrice(Double.parseDouble(productInfo.getBasePrice()));
                }
            }

        }

        if (price.getBestPrice() == null || (price.getBasePrice() != null && price.getBasePrice() < 0)) {
            price.setBestPrice(price.getBasePrice());
        }

        if (null != price.getBestPrice() && price.getBestPrice() > 0) {
            Double d = price.getBestPrice();
            double roundedDoubleBestPrice = Math.round(d * 100.0) / 100.0;
            price.setBestPrice(roundedDoubleBestPrice);
        }
        log.info("END calculateBestPriceBasedOnPromoPriority");
        return price.getBestPrice();
    }


    public static boolean isFreePromoType(List<Benefit> benefitList) {
        boolean result = false;
        if (!CollectionUtils.isEmpty(benefitList)) {
            for (Benefit benefit : benefitList) {
                if (benefit.getBenefitType() != null && benefit.getBenefitType().equalsIgnoreCase(FREE)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }


    private boolean isFreePromoCategories(Map<String, Boolean> freePromoMap, String billingProductCode) {
        return freePromoMap != null
                && getActivePromoTypes().contains(FREE) &&
                !MapUtils.isEmpty(freePromoMap) && isPackageAndPromoCategoryValueTrue(freePromoMap, billingProductCode);
    }


    private boolean isPackageAndPromoCategoryValueTrue(Map<String, Boolean> freePromoMap, String billingProductCode) {
        return freePromoMap.get(billingProductCode) != null
                && freePromoMap.get(billingProductCode).booleanValue();
    }


    private static void setBestPriceForFlatOff(BestPrice price, CustomerSubscriptionDetail customerSubscriptionDetail, String apiType, Benefit benefit, String billingProductCode) {
        
    	if (String.valueOf(benefit.getValue().getDollarAmount()) != null) {
            List<CustomerServiceDetail> listBaseBackageServiceDetail = customerSubscriptionDetail.getListBaseBackageServiceDetail();
			if ((!CollectionUtils.isEmpty(listBaseBackageServiceDetail) &&
            		Objects.nonNull(listBaseBackageServiceDetail.get(0))&&
            		Objects.nonNull(listBaseBackageServiceDetail.get(0).getPromosId()) && 
            		listBaseBackageServiceDetail.get(0).getPromosId()
                    .contains(billingProductCode))
                    || apiType.equalsIgnoreCase(ADDON)) {
                if (benefit.getBenefitType() != null && !benefit.getBenefitType().equalsIgnoreCase(FREE)
                        && !benefit.isAgentOffer()) {
                    Double totalAmount = price.getBestPrice() != null ? price.getBestPrice() : price.getBasePrice();
                	if(Objects.nonNull(benefit.getValue()) && Objects.nonNull(benefit.getValue().getDollarAmount()) && 
                	   Objects.nonNull(totalAmount)) {
						price.setBestPrice(totalAmount - benefit.getValue().getDollarAmount());
                	}
                }
            }

        }
    }

    private void setBestPriceForPercentageOff(BestPrice price, CustomerSubscriptionDetail customerSubscriptionDetail2, String apiType,
                                              Benefit benefit, String billingProductCode) {
        List<CustomerServiceDetail> listBaseBackageServiceDetail = customerSubscriptionDetail2.getListBaseBackageServiceDetail();
		if ((!CollectionUtils.isEmpty(listBaseBackageServiceDetail) && 
           		Objects.nonNull(listBaseBackageServiceDetail.get(0))&&
        		Objects.nonNull(listBaseBackageServiceDetail.get(0).getPromosId()) && 
				listBaseBackageServiceDetail.get(0).getPromosId().contains(billingProductCode))
                || apiType.equalsIgnoreCase(ADDON)) {

            if (benefit.getBenefitType() != null && !benefit.getBenefitType().equalsIgnoreCase(FREE)
                    && benefit.getValue().getPercentage() != null && benefit.isAgentOffer()) {
                Double totalAmount = price.getBestPrice() != null ? price.getBestPrice() : price.getBasePrice();
                if (totalAmount != null && benefit.getValue().getPercentage() != null) {
                    Double percentageOff = ((totalAmount) * (benefit.getValue().getPercentage())) / 100;
                    if (benefit.getBenefitType().equalsIgnoreCase("percentage")) {
                        price.setBestPrice(percentageOff);
                    } else {
                        Double finalPrice = totalAmount - percentageOff;
                        price.setBestPrice(finalPrice);
                    }

                }
            }
        }
    }

    private void activePromoCatgory(CTOffer offer, CustomerSubscriptionDetail subscriptionsContext) {
        Set<String> promoTypes = new HashSet<>();
        List<String> activePromoIds = subscriptionsContext.getBasePackageServiceIdList();

        if (TRUE.equalsIgnoreCase(subscriptionsContext.getIsDefaultPromo())) {
            offer.getAttributes().getBenefits().stream().filter(Objects::nonNull).forEach(benefit -> {
                if (activePromoIds.contains(benefit.getBillingBenefitCode())) {
                    promoTypes.add(benefit.getBenefitType());
                }
            });
        }

        if (TRUE.equalsIgnoreCase(subscriptionsContext.getFreeTrialEligble())) {
            promoTypes.add("Free");
        }
        setActivePromoTypes(promoTypes);
    }

    // Method to filter offers based on BAN lookup
    public void filterOffersBasedOnBANLookup(OfferRequestWrapper offerRequestWrapper, CTOfferResponse finalCTOfferResponse) {
        try {
            if (offerRequestWrapper == null || finalCTOfferResponse == null) {
                return;
            }
            List<CTOffer> finalOffers = new ArrayList<>();
            CustomerContext customerContext = offerRequestWrapper.getOfferRequest().getCustomerContext();
            String accountNumber;
            if (customerContext != null && customerContext.getOtt() != null && StringUtils.isNotEmpty(customerContext.getOtt().getAccountNumber())) {
                accountNumber = customerContext.getOtt().getAccountNumber();
            } else {
                accountNumber = null;
            }
            finalCTOfferResponse.getOffers().forEach(ctOffer -> {
                if (Optional.ofNullable(ctOffer.getAttributes()).isPresent() && Optional.ofNullable(ctOffer.getAttributes().getTargetedOffer()).isPresent()
                        && ctOffer.getAttributes().getTargetedOffer()
                        && StringUtils.isNotEmpty(ctOffer.getAttributes().getBanLookupTable())) {
                    if (accountNumber != null && accountLookupService.isAccountNumberExistsInBanLookupTable(accountNumber, ctOffer.getAttributes().getBanLookupTable().trim())) {
                        finalOffers.add(ctOffer);
                    }
                } else {
                    finalOffers.add(ctOffer);
                }
            });
            finalCTOfferResponse.setOffers(finalOffers);
            finalCTOfferResponse.setTotal(finalOffers.size());
            finalCTOfferResponse.setCount(finalOffers.size());
        } catch (Exception exception) {
            log.error("Exception occurred in the method filterOffersBasedOnBANLookup", exception);
        }
    }
    // Method to filter offers for a given special page in the request
    private void filterOffersBasedOnSpecialPage(OfferRequestWrapper offerRequestWrapper, CTOfferResponse finalCTOfferResponse) {
        try {
            if (offerRequestWrapper == null || finalCTOfferResponse == null) {
                return;
            }
            String specialPage = offerRequestWrapper.getOfferRequest().getChannelEligibility().getSpecialPage();

            List<CTOffer> specialPageOffers = finalCTOfferResponse.getOffers().stream()
                    .filter(Objects::nonNull)
                    .filter(ctOffer -> Objects.nonNull(ctOffer.getAttributes())
                            && StringUtils.isNotEmpty(ctOffer.getAttributes().getSpecialPage())
                            && specialPage.equalsIgnoreCase(ctOffer.getAttributes().getSpecialPage().trim()))
                    .collect(Collectors.toList());

            finalCTOfferResponse.setOffers(specialPageOffers);
            finalCTOfferResponse.setTotal(specialPageOffers.size());
            finalCTOfferResponse.setCount(specialPageOffers.size());
        } catch (Exception exception) {
            log.error("Exception occurred in the method filterOffersBasedOnSpecialPage", exception);
        }
    }

    // Method to filter specialPage offers
    private void removeSpecialPageOffers(CTOfferResponse finalCTOfferResponse) {
        try {

            List<CTOffer> eligibleOffers = finalCTOfferResponse.getOffers().stream()
                    .filter(Objects::nonNull)
                    .filter(ctOffer -> Optional.ofNullable(ctOffer.getAttributes()).isPresent()
                            && !StringUtils.isNotEmpty(ctOffer.getAttributes().getSpecialPage()))
                    .collect(Collectors.toList());
                finalCTOfferResponse.setOffers(eligibleOffers);
                finalCTOfferResponse.setTotal(eligibleOffers.size());
                finalCTOfferResponse.setCount(eligibleOffers.size());

        } catch (Exception exception) {
            log.error("Exception occurred in the method removeSpecialPageOffers", exception);
        }
    }


    private List<CTOffer> filterCompatibleOfferForCurrentBasePackageBAU(List<String> basePackageCompatibleList, CTOffer processedCTOffers) {
        List<CTOffer> offers = new ArrayList<>();
        if (Objects.nonNull(processedCTOffers)) {
            if (Objects.nonNull(processedCTOffers.getAttributes())
                    && Objects.nonNull(processedCTOffers.getAttributes().getAssociatedProducts())
                    && Objects.nonNull(processedCTOffers.getAttributes().getAssociatedProducts().get(0).getBundleProducts())
                    && Objects.nonNull(processedCTOffers.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0))
                    && Objects.nonNull(processedCTOffers.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0))) {
                if (processedCTOffers.getAttributes().isVirtualOffer() && basePackageCompatibleList.contains(processedCTOffers.getAttributes().getBillingCode())) {
                    offers.add(processedCTOffers);
                }

                if (!processedCTOffers.getAttributes().isVirtualOffer() && basePackageCompatibleList.contains(processedCTOffers.getAttributes().getAssociatedProducts().get(0)
                        .getBundleProducts().get(0).getProducts().get(0).getKey())) {
                    offers.add(processedCTOffers);
                }

                if (Optional.ofNullable(processedCTOffers.getAttributes().isQualifyingProductCheckNOTRequired()).isPresent()
                        && processedCTOffers.getAttributes().isQualifyingProductCheckNOTRequired()) {
                    offers.add(processedCTOffers);
                }

            }

        }

        return offers;
    }

    private List<CTOffer> filterConflictingSLSOffers(List<CTOffer> processedCTOffers) {
        if (CollectionUtils.isEmpty(processedCTOffers)) {
            return processedCTOffers;
        }
        Set<CTOffer> suppressedOffers = new HashSet<>();
        for (CTOffer offer : processedCTOffers) {
            if (offer == null || offer.getAttributes() == null || !offer.getAttributes().isSpecialOffer()) {
                continue;
            }
            List<GenericTypeIdBase> conflicting = offer.getAttributes().getConflictingOffers();
            if (CollectionUtils.isEmpty(conflicting)) {
                continue;
            }
            Set<String> conflictingKeys = conflicting.stream()
                    .map(GenericTypeIdBase::getKey)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            for (CTOffer conflictingOffer : processedCTOffers) {
                if (conflictingOffer == null || !conflictingKeys.contains(conflictingOffer.getCode())) {
                    continue;
                }
                OfferAttributes conflictingAttributes = conflictingOffer.getAttributes();
                if (conflictingAttributes != null && org.apache.commons.collections.CollectionUtils.isNotEmpty(conflictingAttributes.getFlowIntents())) {
                    if (offer.getAttributes().getFlowIntents().containsAll(conflictingAttributes.getFlowIntents())) {
                        suppressedOffers.add(conflictingOffer);
                    } else {
                        conflictingAttributes.getFlowIntents()
                                .removeAll(offer.getAttributes().getFlowIntents());
                        conflictingAttributes.getServiceSubscriptionType()
                                .removeAll(offer.getAttributes().getServiceSubscriptionType());
                    }
                }
            }
        }
        processedCTOffers.removeAll(suppressedOffers);
        return processedCTOffers;
    }


    public List<CTOffer> getCreditOffers(OfferRequestWrapper offerRequestWrapper) {
        log.info("Start Services getCreditOffers");
        List<CTOffer> localProcessedCTOffers = new ArrayList<>();
        if (offerRequestWrapper.getOfferRequest().getOfferProductType().contains(Constants.CREDIT)) {

            List<CTOffer> creditOffers = dtvnServicesCreditOffersProcessor.retrieveCreditOffers(offerRequestWrapper);

            if (creditOffers != null) {
                localProcessedCTOffers.addAll(creditOffers);
            }
        }
        log.info("End Services getCreditOffers");
        return localProcessedCTOffers;
    }

}
