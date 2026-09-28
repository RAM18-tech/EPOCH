package com.dtv.dcp.epoch.processor.ott;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.CpopConstants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.MinMaxQuantity;
import com.dtv.dcp.epoch.model.common.request.*;
import com.dtv.dcp.epoch.model.ct.generic.GenericTypeIdBase;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.offer.OfferChoiceGroup;
import com.dtv.dcp.epoch.model.ct.product.InstallmentInfo;
import com.dtv.dcp.epoch.model.ct.product.Product;
import com.dtv.dcp.epoch.model.ct.product.ProductWrapper;
import com.dtv.dcp.epoch.model.ct.request.*;
import com.dtv.dcp.epoch.model.ct.request.PriceProtection;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.model.ct.response.EvergentContract;
import com.dtv.dcp.epoch.processor.ott.sales.OttSalesOffersProcessorHelper;
import com.dtv.dcp.epoch.processor.ott.sales.SalesATTTVMigrationProcessor;
import com.dtv.dcp.epoch.processor.ott.services.CustomerSubscriptionDetail;
import com.dtv.dcp.epoch.processor.ott.services.model.CustomerServiceDetail;
import com.dtv.dcp.epoch.repository.AccountDataRepositoryImpl;
import com.dtv.dcp.epoch.service.AccountLookupService;
import com.dtv.dcp.epoch.service.DMALookUpService;
import com.dtv.dcp.epoch.util.CTOfferRequestHelper;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.dtv.dcp.epoch.util.RedisCacheHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * The Class OttCTOffersProcessor.
 */
@Component
public class OttCTOffersProcessor {

	private static final Logger log = LoggerFactory.getLogger(OttCTOffersProcessor.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final String ACTIVATION_FEE = "ActivationFee";

	private static final String CPOP_MIGRATION_OFFER_ENABLED = "cpop-migration-offer-enabled";

	@Autowired
	CpopClientHelper cpopClientHelper;

	@Autowired
	AccountDataRepositoryImpl accountData;

	@Autowired
	SalesATTTVMigrationProcessor salesATTTVMigrationProcessor;

	@Autowired
	OffersUtils offersUtils;

	@Autowired
	CTOfferRequestHelper cTOfferRequestHelper;

	@Autowired
	private AccountLookupService accountLookupService;

	@Autowired
	OttSalesOffersProcessorHelper ottSalesOffersProcessorHelper;
	@Autowired
	private DMALookUpService dmaLookUpService;

	@Autowired
	private RedisCacheHelper redisCacheHelper;

	@Autowired
	private FeatureManagerHelper featureManagerHelper;

	public CTOfferResponse getVideoPlanOffers(OfferRequestWrapper offerRequestWrapper, boolean mobility) {
		CTOfferResponse ctOfferResponse = null;
		boolean isReconnectFlag = false;
		String eligibleOfferFlavour = "";
		List<CTOffer> processedCTOffers = null;
		processedCTOffers = new ArrayList<>();
		EvergentContract propertyOwnerData=new EvergentContract();

		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		if (Objects.nonNull(offerRequestWrapper.getCtOfferRequest())) {
			BeanUtils.copyProperties(offerRequestWrapper.getCtOfferRequest(), ctOfferRequest);
		}

		if (StringUtils.isNotEmpty(offerRequestWrapper.getFlow()) && offerRequestWrapper.getFlow().equalsIgnoreCase("validateCart")) {
			ctOfferRequest.setMigrationIndicator(offerRequestWrapper.getOfferRequest().isMigrationIndicator());
		}

		List<String> offerActionTypeCopy = new ArrayList<>(offerRequestWrapper.getOfferRequest().getOfferActionType());
		ctOfferRequest.setOfferActionType(offerActionTypeCopy);

		// ctOfferRequest.setOfferActionType(offerRequestWrapper.getOfferRequest().getOfferActionType());
		if (offerRequestWrapper.getOfferRequest().isDecisioningFlow()
				&& ctOfferRequest.getOfferActionType().contains(Constants.RETENTION_ACTION_TYPE)
				&& ctOfferRequest.getOfferProductType().contains(Constants.VIDEO_PLAN)
				&& offersUtils.isService(offerRequestWrapper.getOfferRequest())) {
			ctOfferRequest.getOfferActionType().remove(Constants.RETENTION_ACTION_TYPE);
		}
		log.info("API_NAME:EPOCH_GETOFFERS OFFERACTIONTYPE:{}",OffersUtils.sanitizeData(ctOfferRequest.getOfferActionType()));
		ctOfferRequest.setOfferProductType(Stream.of(Constants.VIDEO_PLAN).collect(Collectors.toList()));
//        if (offerRequestWrapper.isDirectvOnline()) {
//            ctOfferRequest.setSalesChannel(Stream.of(Constants.ONLINE, Constants.DIRECTV_ONLINE).collect(Collectors.toList()));
//
//        }else{
//            ctOfferRequest.setSalesChannel(offerRequestWrapper.getOfferRequest().getSalesChannel());
//        }
		ctOfferRequest.setSalesChannel(offerRequestWrapper.getOfferRequest().getSalesChannel());
		ctOfferRequest.setCustomerSegments(offerRequestWrapper.isMobility() ? Stream.of("Mobility").collect(Collectors.toList()) :
				Stream.of("Residential").collect(Collectors.toList()));

		if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerSegments())
				&& (offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.DEMO)
				|| offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.DECA)
				|| offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.SHOWROOM)
				|| offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.COURTESY)
				|| offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.BCOMP)
				|| offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.MDUTENANT))){

			ctOfferRequest.setCustomerSegments(offerRequestWrapper.getOfferRequest().getCustomerSegments());
		}

		//if TAZCONTRACT, check for price Protection range
        //for PG, it should be 24 months; HOG is the non-24-month case
		boolean isValidTAZCONTRACTPGDates = false;
        int monthsForTazContract = 24;
        if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext()) &&
                Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()) &&
                Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getPriceProtection())
                &&offersUtils.containsTAZCONTRACT(offerRequestWrapper.getOfferRequest())){
            PriceProtection priceProtection = offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getPriceProtection();
            if(priceProtection.getStartDate()!=null && priceProtection.getEndDate()!=null){
                isValidTAZCONTRACTPGDates = offersUtils.validateMonthsBetweenDates(priceProtection.getStartDate(), priceProtection.getEndDate(), monthsForTazContract);
            }
        }

		//Not TAZCONTRACT/NOT RR FLOW
		if(!offersUtils.containsTAZCONTRACT(offerRequestWrapper.getOfferRequest()) &&
				!offersUtils.containsROADRUNNER(offerRequestWrapper.getOfferRequest()) &&
				Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext()) &&
				Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()) &&
				Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getPriceProtection())) {
			PriceProtection priceProtection = offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getPriceProtection();
			priceProtection.setNextBillingDate(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getNextBillingDate());
			if(offerRequestWrapper.getCurrentDate()!=null){
				priceProtection.setCurrentDate(offerRequestWrapper.getCurrentDate());
			}else{
				priceProtection.setCurrentDate(new SimpleDateFormat("MM/dd/yyyy").format(new Date()));
			}

			ctOfferRequest.setPriceProtection(priceProtection);
		} 
		//TAZCONTRACT FLOW - CHECK if price protection range is not 24months
        else if(offersUtils.containsTAZCONTRACT(offerRequestWrapper.getOfferRequest()) &&
                !isValidTAZCONTRACTPGDates &&
                Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext()) &&
                Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()) &&
                Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getPriceProtection())) {
            PriceProtection priceProtection = offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getPriceProtection();
            priceProtection.setNextBillingDate(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getNextBillingDate());
            if(offerRequestWrapper.getCurrentDate()!=null){
                priceProtection.setCurrentDate(offerRequestWrapper.getCurrentDate());
            }else{
                priceProtection.setCurrentDate(new SimpleDateFormat("MM/dd/yyyy").format(new Date()));
            }
            ctOfferRequest.setPriceProtection(priceProtection);
        }		
		//TAZCONTRACT or RR FLOW - PG-PNP Flow
		else if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext()) &&
				Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()) &&
				offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getNextBillingDate()!=null) {
			ctOfferRequest.setNextBillingDate(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getNextBillingDate());
		}

		if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerSegments())
				&& offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.EMPLOYEE))
		{
			if (isReconnectFlow(offerRequestWrapper)) {
				ctOfferRequest.setCustomerSegments(Stream.of(Constants.RESIDENTIAL).collect(Collectors.toList()));
			} else {
				ctOfferRequest.setCustomerSegments(Stream.of(Constants.EMPLOYEE).collect(Collectors.toList()));
			}
		} else
		if (!offersUtils.containsEDSP(offerRequestWrapper.getOfferRequest()) && isReconnectFlow(offerRequestWrapper) &&
				(isValidSubscribeType(offerRequestWrapper) || offersUtils.containsTAZCONTRACT(offerRequestWrapper.getOfferRequest())
						|| offersUtils.containsROADRUNNER(offerRequestWrapper.getOfferRequest())
						|| offersUtils.containsGENRE(offerRequestWrapper.getOfferRequest())) &&
				Optional.ofNullable(offerRequestWrapper.getOfferRequest().getServiceEndDate()).isPresent() &&
				isEligibleToReconnect(offerRequestWrapper, ctOfferRequest)) {

			// customer type reconnect
			isReconnectFlag = true;
			ctOfferRequest.setCustomerType(Stream.of("reconnect").collect(Collectors.toList()));
		}

		ctOfferRequest.setBusinessSegment(Objects.nonNull(offerRequestWrapper.getOfferRequest().getBusinessSegment())?
				offerRequestWrapper.getOfferRequest().getBusinessSegment() : Arrays.asList(Constants.CONS));

		if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext())
				&&	Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt())
				&&	Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getPropertyOwnerActNumber()))
		{
			propertyOwnerData =accountData.fetchTenantData(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getPropertyOwnerActNumber()).get(0);

		}

		log.info("offer request {}",ctOfferRequest);


		log.info("API_NAME:EPOCH_GETOFFERS CUSTOMERSEGMENT:{}",OffersUtils.sanitizeData(ctOfferRequest.getCustomerSegments()));
		ctOfferRequest.setPagination(offerRequestWrapper.getOfferRequest().getPagination());
		ctOfferRequest.setContractIndicator(offerRequestWrapper.getOfferRequest().getContractIndicator());
		ctOfferRequest.setBenefitsCodesToSuppressOffer(offerRequestWrapper.getOfferRequest().getBenefitsCodesToSuppressOffer());
		if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getBenefitsCustomerHasReceived())) {
			List<BenefitsCustomerHasReceived> benefitsCustomerHasReceived = new ArrayList<>();
			benefitsCustomerHasReceived = offerRequestWrapper.getOfferRequest().getBenefitsCustomerHasReceived();
			List<BenefitCodesToSuppressTheOffer> benefitCodesToSuppressTheOffer = convertToBenefitCodesToSuppressTheOffer(benefitsCustomerHasReceived);
			ctOfferRequest.setBenefitCodesToSuppressTheOffer(benefitCodesToSuppressTheOffer);
		}
		if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext()) &&
				Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()) &&
				Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getIapPartnerAccountType()) &&
				offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getIapPartnerAccountType() != null) {
			ctOfferRequest.setIapPartnerAccountType(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getIapPartnerAccountType());
		}
		ctOfferRequest.setExpiredOffers(offerRequestWrapper.getOfferRequest().isExpiredOffers());
		if (!offersUtils.isService(offerRequestWrapper.getOfferRequest())) {
			if (offerRequestWrapper.getOfferRequest().isMigrationIndicator() || featureManagerHelper.isEnabled(CPOP_MIGRATION_OFFER_ENABLED)) {
				log.info("EPOCH_GETOFFERS_ACQUISITIONFLOW_MIGRATION_OFFERSREQUEST ");
				salesATTTVMigrationProcessor.populateMigrationRequest(offerRequestWrapper, ctOfferRequest);
			}else {
				if(Objects.nonNull(ctOfferRequest.getCustomerContext())) {
					// Non-IPTV Migration flow, Excluding Customer Context entries for IPTV, DTVS & BB
					List<CTCustomerContext> customerContext = excludeUverseCustomerContext(ctOfferRequest);
					ctOfferRequest.setCustomerContext(customerContext);
				}
			}
			ctOfferResponse = getOffersFromCT(ctOfferRequest,offerRequestWrapper);
			if ((offersUtils.containsEDSP(offerRequestWrapper.getOfferRequest())
					|| offersUtils.containsTAZ(offerRequestWrapper.getOfferRequest())
					|| offersUtils.containsTAZBYOD(offerRequestWrapper.getOfferRequest())
					|| offersUtils.containsROADRUNNER(offerRequestWrapper.getOfferRequest())
					|| offersUtils.containsGENRE(offerRequestWrapper.getOfferRequest())
					|| offersUtils.containsTAZCONTRACT(offerRequestWrapper.getOfferRequest())) && !isReconnectFlag 	&& (null != ctOfferResponse)) {
				processedCTOffers = filterReconnectOffer(ctOfferResponse.getOffers());
				ctOfferResponse.setOffers(processedCTOffers);
				ctOfferResponse.setCount(processedCTOffers.size());
				ctOfferResponse.setTotal(processedCTOffers.size());
			}
			if (offerRequestWrapper.getOfferRequest().isMigrationIndicator() || featureManagerHelper.isEnabled(CPOP_MIGRATION_OFFER_ENABLED)) {
				ctOfferResponse = salesATTTVMigrationProcessor.filterMigrationEligibleOffers(offerRequestWrapper,ctOfferRequest, ctOfferResponse);
			}
		} else {
			ctOfferResponse = getOffersFromCT(ctOfferRequest,offerRequestWrapper);
		}

		if (Optional.ofNullable(offerRequestWrapper.getOfferRequest()).isPresent()
				&& !offersUtils.isService(offerRequestWrapper.getOfferRequest())
				&& (offersUtils.containsEDSP(offerRequestWrapper.getOfferRequest())
				|| offersUtils.containsTAZ(offerRequestWrapper.getOfferRequest()))) {
			if (offersUtils.checkIsOemChannel(offerRequestWrapper.getOfferRequest())) {
				if (isReconnectFlow(offerRequestWrapper)) {
					//Checking which Flavor of Offer is to be returned for OEM
					eligibleOfferFlavour = getEligibleOfferFlavour(offerRequestWrapper,ctOfferRequest);
					ctOfferResponse = getOemOffer(ctOfferResponse, eligibleOfferFlavour);
				} else {
					ctOfferResponse = getOemOffer(ctOfferResponse, "FT");
				}
			} else if(offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.OPUS)
					&& !offerRequestWrapper.isChannelEligiblity()) {
				ctOfferResponse = offersUtils.filterInvalidOffer(ctOfferResponse);
			}
		}

		if(Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerSegments()).isPresent()
				&& (offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.DEMO)
				|| offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.DECA)
				|| offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.SHOWROOM)
				|| offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.BCOMP)
				|| offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.COURTESY))
				&& Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferActionType()).isPresent()
				&& offerRequestWrapper.getOfferRequest().getOfferActionType().contains(Constants.ACQUISITION)) {
			ctOfferResponse.getOffers().forEach(offer -> {
				setDisplayTypeForCDVR(offer);
			});
		}

		if(Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerSegments()).isPresent()
				&& offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.MDUTENANT)
				&& Optional.ofNullable(propertyOwnerData).isPresent()) {

			List<String> existingBasePkg= propertyOwnerData.getBulked_pkg().isEmpty() ?
					new ArrayList<String>() :Arrays.asList(propertyOwnerData.getBulked_pkg().split(";"));

			List<String> existingAddons= propertyOwnerData.getProg_addons().isEmpty() ?
					new ArrayList<String>() : Arrays.asList(propertyOwnerData.getProg_addons().split(";"));

			CTProductRequest ctProductRequest = new CTProductRequest();
			ctProductRequest.setProductCodes(existingAddons);
			ctProductRequest.setBusinessSegment(Arrays.asList(Constants.MDU));
			CTProductResponse productResponse=cpopClientHelper.getProducts(ctProductRequest);

			AtomicInteger addonsCount= new AtomicInteger();
			addonsCount.set(propertyOwnerData.getProg_addons().isEmpty() ?
					0 : Arrays.asList(propertyOwnerData.getProg_addons().split(";")).size());

			productResponse.getProducts().forEach(product ->{
				if(Objects.nonNull(product.getCode()) && existingAddons.contains(product.getCode())){
					if(Objects.nonNull(product.getVariants()) && Objects.nonNull(product.getVariants().get(0))
							&& Objects.nonNull(product.getVariants().get(0).getAttributes()) && Objects.nonNull(product.getVariants().get(0).getAttributes().getAddonCount())
							&& product.getVariants().get(0).getAttributes().getAddonCount()!=0) {

						addonsCount.set(addonsCount.get()+product.getVariants().get(0).getAttributes().getAddonCount()-1);
					}
				}
			});

			List<String> basePackageCompatibleProds = new ArrayList<String>();
			if (offersUtils.isService(offerRequestWrapper.getOfferRequest())
					&&Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext()).isPresent()
					&&Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()).isPresent()
					&&Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getProducts()).isPresent()) {
				offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getProducts().forEach(prod -> {
					if (prod.getProductType().equalsIgnoreCase(Constants.VIDEO_PLAN)) {
						basePackageCompatibleProds.add(prod.getProductCode());
					}
				});
			}else {
				basePackageCompatibleProds.addAll(existingBasePkg);
			}

			ctOfferResponse.setOffers(filterTenantOffers(ctOfferResponse,addonsCount.get(),existingBasePkg,basePackageCompatibleProds));

		}

		return ctOfferResponse;
	}

	/**
	 * This method filters reward offers based on customer type acquisition.
	 *
	 * @param ctOfferResponse     The CTOfferResponse object containing the offers
	 *                            to be filtered.
	 * @param offerRequestWrapper The OfferRequestWrapper object containing the
	 *                            offer request details.
	 */
	public void filterRewardOffersBasedOnCustomerTypeAcquisition(CTOfferResponse ctOfferResponse,
																 OfferRequestWrapper offerRequestWrapper) {

		// Check if the CTOfferResponse object is not null and it contains offers
		if (ctOfferResponse != null && !ctOfferResponse.getOffers().isEmpty()) {

			// Remove the offers from the CTOfferResponse object that are not customer type
			// acquisition offers
			ctOfferResponse.getOffers()
					.removeIf(offer -> offersUtils.isCustomerTypeAcquisitionOffers(offerRequestWrapper, offer));

			// Calculate the new number of offers
			int offersCount = ctOfferResponse.getOffers().size();

			// Update the count and total fields of the CTOfferResponse object
			ctOfferResponse.setCount(offersCount);
			ctOfferResponse.setTotal(offersCount);
		}
	}

	private CTOfferResponse getPendentTargetedOffers(CTOfferResponse ctOfferResponse,
													 OfferRequestWrapper offerRequestWrapper) {
		List<String> targetedOffers = fetchTargetedOffers(ctOfferResponse);
		if (CollectionUtils.isNotEmpty(targetedOffers)) {
			// fetch eligible targeted offers for the account
			List<String> offers = accountLookupService.fetchEligibleOffersForOTTAccount(
					offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getAccountNumber(),
					targetedOffers);
			// when STMS and Enabler has different offer codes,
			// OFFER_ID in table will have comma separated offer codes
			if (Objects.nonNull(offers) && !offers.isEmpty()) {
				List<String> eligibleOffers = new ArrayList<>();
				offers.stream().filter(Objects::nonNull).forEach(offer -> eligibleOffers.addAll(getListFromString(offer)));
				ctOfferResponse.getOffers().removeIf(ctOffer -> isNotValidTargetedOffer(eligibleOffers, ctOffer));
			}
		}

		return ctOfferResponse;
	}

	public CTOfferResponse filterProductsOnDisallowReplacementForPromotion(CTOfferResponse ctResponse,
																		   OfferRequestWrapper offerRequestWrapper, List<CustomerServiceDetail> productPromolist) {
		log.info("Method_Name:filterProductsOnDisallowReplacementForPromotion:{} : Product and Promotions list :::::::::::",productPromolist);
		if (offerRequestWrapper.getOfferRequest().getOfferClassificationType() != null && offerRequestWrapper
				.getOfferRequest().getOfferClassificationType().contains(Constants.REPLACEMENT_CLASSIFICATION_TYPE)) {
			Set<CTOffer> filterOffers = new HashSet<>();
			List<CTOffer> offers = ctResponse.getOffers();
			log.info("Method_Name:filterProductsOnDisallowReplacementForPromotion:{} : Request is :::::::::",Constants.REPLACEMENT_CLASSIFICATION_TYPE);
			productPromolist.forEach(customerDetails -> {
				log.info("Method_Name:filterProductsOnDisallowReplacementForPromotion:{} : customerDetails.getPlanCount() :::::::::"
						+ customerDetails.getPlanCount() + "customerDetails.getPromosId()"+ customerDetails.getPromosId().size());
				Integer productCount = customerDetails.getPlanCount();
				Integer promoCount = customerDetails.getPromosId().size();
				ctResponse.getOffers().forEach(ctOffer -> {
					if (ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts() != null
							&& ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0) != null
							&& ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts() != null
							&& ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0) != null
							&& ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getKey() != null) {
						if (ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0)
								.getProducts().get(0).getKey().contains(customerDetails.getPlanCode())) {
							if (ctOffer.getAttributes().getDisallowReplacementForPromotion() != null) {
								if (productCount <= promoCount) {
									filterOffers.add(ctOffer);
								}
							}
						}
					}
				});
			});
			if (CollectionUtils.isNotEmpty(filterOffers)) {
				offers.removeAll(filterOffers);
				ctResponse.setOffers(offers);
			}
		}

		return ctResponse;
	}

	/**
	 * Fetch all targeted offers
	 *
	 * @param ctOfferResponse
	 * @return
	 */
	private List<String> fetchTargetedOffers(CTOfferResponse ctOfferResponse) {
		return ctOfferResponse.getOffers().stream().filter(Objects::nonNull)
				.filter(ctOffer -> Objects.nonNull(ctOffer.getAttributes().getTargetedOffer())
						&& Boolean.TRUE.equals(ctOffer.getAttributes().getTargetedOffer()))
				.map(CTOffer::getCode).collect(Collectors.toList());
	}

	/**
	 * Filter targetedOffer based on account eligibility
	 *
	 * @param eligibleOffers
	 * @param ctOffer
	 */
	private boolean isNotValidTargetedOffer(List<String> eligibleOffers, CTOffer ctOffer) {
		if (Objects.nonNull(ctOffer.getAttributes())
				&& Boolean.TRUE.equals(ctOffer.getAttributes().getTargetedOffer())) {
			// If the offer is a targetedOffer, return it only for eligible accounts
			return eligibleOffers.stream().noneMatch(ctOffer.getCode()::equalsIgnoreCase);
		}
		return false;
	}

	public List<String> getListFromString(String inputString) {
		List<String> convertedList = new ArrayList<>();

		if (StringUtils.isNotBlank(inputString)) {
			if (inputString.indexOf(",") != -1) {
				convertedList = Stream.of(inputString.split(",", -1)).map(String::trim).collect(Collectors.toList());
			} else {
				convertedList.add(inputString.trim());
			}
		}
		return convertedList;
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

	private CTOfferResponse getOemOffer(CTOfferResponse ctOfferResponse, String eligibleOfferFlavour) {
		if(eligibleOfferFlavour == "FT") {
			ctOfferResponse = offersUtils.getDirectvOffers(ctOfferResponse);
		} else if(eligibleOfferFlavour == "EDSP") {
			ctOfferResponse = offersUtils.filterFreeTrailOffer(ctOfferResponse);
		}
		return ctOfferResponse;
	}

	private String getEligibleOfferFlavour(OfferRequestWrapper offerRequestWrapper, CTOfferRequest ctOfferRequest) {
		if (!isValidSubscribeType(offerRequestWrapper)) {
			return "EDSP";
		} else if (isValidSubscribeType(offerRequestWrapper)) {
			//If Employee then always return FT
			if (offerRequestWrapper.getOfferRequest().getCustomerSegments()!=null && offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.EMPLOYEE)) {
				return "FT";
			} else if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getServiceEndDate()).isPresent()
					&& isEligibleToReconnect(offerRequestWrapper,ctOfferRequest)) {
				return "EDSP";
			} else {
				return "FT";
			}
		}
		return "";
	}

	public List<CTOffer> filterReconnectOffer(List<CTOffer> processedCTOffers) {

		List<CTOffer> filteredOffers = Collections.synchronizedList(new ArrayList<>());
		if (!processedCTOffers.isEmpty()) {
			Map<String, List<CTOffer>> groupedOffers = processedCTOffers.stream().collect(
					Collectors.groupingBy(offer -> offer.getAttributes().getBillingCode(), Collectors.toList()));

			groupedOffers.entrySet().parallelStream().forEach(offerMap -> {
				List<CTOffer> value = offerMap.getValue();
				value.stream().filter(Objects::nonNull).forEach(offer -> {
					if (Optional.ofNullable(offer.getAttributes().getCustomerTypes()).isEmpty() ||
							!offersUtils.isCustomerTypeReconnect(offer)) {
						filteredOffers.add(offer);
					}
					if(offer.getAttributes().getReconnectEligibleOffer()!=null && !offer.getAttributes().getReconnectEligibleOffer().isEmpty()) {
						offer.getAttributes().setReconnectEligibleOffer(null);
					}
				});

			});
		}
		return filteredOffers;
	}


	public CTOfferResponse getVideoAddonStandalonOffers(OfferRequestWrapper offerRequestWrapper, boolean mobility) {
		CTOfferResponse ctOfferResponse = null;
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		if (Objects.nonNull(offerRequestWrapper.getCtOfferRequest())) {
			BeanUtils.copyProperties(offerRequestWrapper.getCtOfferRequest(), ctOfferRequest);
		}
		ctOfferRequest.setOfferActionType(offerRequestWrapper.getOfferRequest().getOfferActionType());
		log.info("API_NAME:EPOCH_GETOFFERS OFFERACTIONTYPE:{}",OffersUtils.sanitizeData(ctOfferRequest.getOfferActionType()));
		ctOfferRequest.setOfferProductType(Stream.of(Constants.VIDEO_ADDON).collect(Collectors.toList()));
		ctOfferRequest.setAddOnType(Stream.of("Standalone").collect(Collectors.toList()));
		ctOfferRequest.setPlanSubType(Stream.of("International").collect(Collectors.toList()));
		ctOfferRequest.setPagination(offerRequestWrapper.getOfferRequest().getPagination());
		ctOfferRequest.setSalesChannel(offerRequestWrapper.getOfferRequest().getSalesChannel());
		ctOfferRequest.setExpiredOffers(offerRequestWrapper.getOfferRequest().isExpiredOffers());
		ctOfferRequest.setCustomerSegments(offerRequestWrapper.isMobility() ? Stream.of("Mobility").collect(Collectors.toList()) :
				Stream.of("Residential").collect(Collectors.toList()));


		if(	Optional.ofNullable(offerRequestWrapper.getOfferRequest()).isPresent()
				&& 	Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerSegments()).isPresent()
				&& offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.EMPLOYEE)) {
			ctOfferRequest.setCustomerSegments(Stream.of(Constants.EMPLOYEE).collect(Collectors.toList()));
		}else
		if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerSegments())
				&& (offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.DEMO)
				|| offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.DECA)
				|| offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.SHOWROOM)
				|| offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.COURTESY)
				|| offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.BCOMP)
				|| offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.MDUTENANT))){

			ctOfferRequest.setCustomerSegments(offerRequestWrapper.getOfferRequest().getCustomerSegments());
		}

		ctOfferRequest.setBusinessSegment(Objects.nonNull(offerRequestWrapper.getOfferRequest().getBusinessSegment())?
				offerRequestWrapper.getOfferRequest().getBusinessSegment() : Arrays.asList(Constants.CONS));

		//set price protection and NBD for video addons standalone
		setPriceProtectionNBDCTOffReq(offerRequestWrapper, ctOfferRequest);

		log.info("API_NAME:EPOCH_GETOFFERS CUSTOMERSEGMENT:{}",OffersUtils.sanitizeData(ctOfferRequest.getCustomerSegments()));
		ctOfferRequest.setBenefitsCodesToSuppressOffer(offerRequestWrapper.getOfferRequest().getBenefitsCodesToSuppressOffer());
		if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getBenefitsCustomerHasReceived())) {
			List<BenefitsCustomerHasReceived> benefitsCustomerHasReceived = new ArrayList<>();
			benefitsCustomerHasReceived = offerRequestWrapper.getOfferRequest().getBenefitsCustomerHasReceived();
			List<BenefitCodesToSuppressTheOffer> benefitCodesToSuppressTheOffer = convertToBenefitCodesToSuppressTheOffer(benefitsCustomerHasReceived);
			ctOfferRequest.setBenefitCodesToSuppressTheOffer(benefitCodesToSuppressTheOffer);
		}
		if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext())
				&& (Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt())) && (Objects
				.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getProducts()))) {
			List<String> productsOnAccountToSuppress = new ArrayList<>();
			offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getProducts().stream().filter(Objects::nonNull).forEach(prod->{
				if(prod !=null && prod.getProductCode()!=null){
					productsOnAccountToSuppress.add(prod.getProductCode());
				}
			});
			ctOfferRequest.setProductsOnAccountToSuppressOffer(productsOnAccountToSuppress);
		}
		if (offerRequestWrapper != null && offerRequestWrapper.getOfferRequest() != null
				&& offerRequestWrapper.getOfferRequest().isMigrationIndicator()) {
			log.info("EPOCH_GET_OFFERS_ACQUISITION_FLOW_MIGRATION_OFFERS_REQUEST_VIDEO_ADDON_STANDALONE");
			ctOfferRequest.setMigrationIndicator(offerRequestWrapper.getOfferRequest().isMigrationIndicator());
		}
		ctOfferResponse = getOffersFromCT(ctOfferRequest,offerRequestWrapper);
		return ctOfferResponse;
	}

	public CTOfferResponse getVideoAddonOffers(OfferRequestWrapper offerRequestWrapper) {
		CTOfferResponse ctOfferResponse = null;
		EvergentContract propertyOwnerData=new EvergentContract();

		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		if (Objects.nonNull(offerRequestWrapper.getCtOfferRequest())) {
			BeanUtils.copyProperties(offerRequestWrapper.getCtOfferRequest(), ctOfferRequest);
		}
		ctOfferRequest.setOfferActionType(offerRequestWrapper.getOfferRequest().getOfferActionType());
		log.info("API_NAME:EPOCH_GETOFFERS OFFERACTIONTYPE:{}",OffersUtils.sanitizeData(ctOfferRequest.getOfferActionType()));
		ctOfferRequest.setOfferProductType(Stream.of(Constants.VIDEO_ADDON).collect(Collectors.toList()));
		ctOfferRequest.setAddOnType(offerRequestWrapper.getOfferRequest().getAddOnType());
		ctOfferRequest.setPlanSubType(offerRequestWrapper.getOfferRequest().getPlanSubType());
		ctOfferRequest.setPagination(offerRequestWrapper.getOfferRequest().getPagination());
//        if(offerRequestWrapper.isDirectvOnline()) {
//            ctOfferRequest.setSalesChannel(Stream.of(Constants.ONLINE, Constants.DIRECTV_ONLINE).collect(Collectors.toList()));
//
//        }else{
//            ctOfferRequest.setSalesChannel(offerRequestWrapper.getOfferRequest().getSalesChannel());
//        }
		ctOfferRequest.setSalesChannel(offerRequestWrapper.getOfferRequest().getSalesChannel());
		ctOfferRequest.setContractIndicator(offerRequestWrapper.getOfferRequest().getContractIndicator());

		ctOfferRequest.setBusinessSegment(Objects.nonNull(offerRequestWrapper.getOfferRequest().getBusinessSegment())?
				offerRequestWrapper.getOfferRequest().getBusinessSegment() : Arrays.asList(Constants.CONS));

		//set price protection and NBD for video addons
		setPriceProtectionNBDCTOffReq(offerRequestWrapper, ctOfferRequest);

		List<String> existingAddons=new ArrayList<String>();
		if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext())
				&&	Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt())
				&&	Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getPropertyOwnerActNumber()))
		{
			propertyOwnerData =accountData.fetchTenantData(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getPropertyOwnerActNumber()).get(0);

			existingAddons= propertyOwnerData.getProg_addons().isEmpty() ?
					new ArrayList<String>() : Arrays.asList(propertyOwnerData.getProg_addons().split(";"));
		}

		ctOfferRequest.setBenefitsCodesToSuppressOffer(offerRequestWrapper.getOfferRequest().getBenefitsCodesToSuppressOffer());
		if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext())
				&& (Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt())) && (Objects
				.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getProducts()))) {
			List<String> productsOnAccountToSuppress = new ArrayList<>();
			offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getProducts().stream().filter(Objects::nonNull).forEach(prod->{
				if(prod !=null && prod.getProductCode()!=null){
					productsOnAccountToSuppress.add(prod.getProductCode());
				}
			});
			ctOfferRequest.setProductsOnAccountToSuppressOffer(productsOnAccountToSuppress);
		}
		if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getBenefitsCustomerHasReceived())) {
			List<BenefitsCustomerHasReceived> benefitsCustomerHasReceived = new ArrayList<>();
			benefitsCustomerHasReceived = offerRequestWrapper.getOfferRequest().getBenefitsCustomerHasReceived();
			List<BenefitCodesToSuppressTheOffer> benefitCodesToSuppressTheOffer = convertToBenefitCodesToSuppressTheOffer(benefitsCustomerHasReceived);
			ctOfferRequest.setBenefitCodesToSuppressTheOffer(benefitCodesToSuppressTheOffer);


		}
		if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext()) &&
				Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()) &&
				Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getIapPartnerAccountType()) &&
				offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getIapPartnerAccountType() != null) {
			ctOfferRequest.setIapPartnerAccountType(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getIapPartnerAccountType());
		}

		//Add offer intent
//		if (featureHelper.isEnabled(Constants.FEATURE_IDP_FEATURES_SVC_PREMIUM_SWEETNER_ENABLED)) {
//			if (!offersUtils.isService(offerRequestWrapper.getOfferRequest())) {
//				if (offerRequestWrapper.getOfferRequest().isMigrationIndicator() || featureHelper.isEnabled(CPOP_MIGRATION_OFFER_ENABLED)) {
//					log.info("EPOCH_GETOFFERS_ACQUISITIONFLOW_MIGRATION_OFFERSREQUEST VIDEO ADDON");
//					salesATTTVMigrationProcessor.populateMigrationRequest(offerRequestWrapper, ctOfferRequest);
//				}
//			}
//
//
//		}

		if (offerRequestWrapper.getOfferRequest().isMigrationIndicator()) {
			OfferRequest offerRequest = offerRequestWrapper.getOfferRequest();
			log.info("EPOCH_GETOFFERS_ACQUISITIONFLOW_MIGRATION_OFFERSREQUEST VIDEO ADDON");
			ctOfferRequest.setMigrationIndicator(offerRequest.isMigrationIndicator());
		}

		// No need to set Customer Segments as it is not defined for addon offers.
		if (!featureManagerHelper.isEnabled(CpopConstants.EPOCH_HBO_MAX_PERM_SOLUTION)) {
			ctOfferRequest.setCustomerContext(null);
		}
		// For Reconnect flow to fetch video addone offers, setting Customer Segments as null as Customer Segments is not defined for addon offers
		if(isReconnectFlow(offerRequestWrapper))
		{
			ctOfferRequest.setCustomerSegments(null);
		}
		ctOfferResponse = getOffersFromCT(ctOfferRequest,offerRequestWrapper);
		if (ctOfferResponse != null && ctOfferRequest!=null && ctOfferRequest.getProductsOnAccountToSuppressOffer()!=null ) {
			ctOfferResponse = cpopClientHelper.filterProductsOnAccountToSuppressOffer(ctOfferResponse, ctOfferRequest);
		}


		//if reconnect flow, filter out special offers when service end date is less than 12 months.
		//special offer are those having  true as its value for isVirtual attribute
		// if svc-reconnectoffer-flag-enabled flag is enabled, special offers should be returned
		// if svc-reconnectoffer-flag-enabled flag is disabled, special offers should not be returned
/*
  	Commented out as it was previously used only for online sales channel

   if (isReconnectFlow(offerRequestWrapper) &&
    		  isValidSubscribeType(offerRequestWrapper) &&
      		Optional.ofNullable(offerRequestWrapper.getOfferRequest().getServiceEndDate()).isPresent() &&
				isEligibleToReconnectLessThanEligibleMonths(offerRequestWrapper,ctOfferRequest) && isNotEmployeeFlowOrNoCustomerSegement(offerRequestWrapper)) {
    	  ctOfferResponse = JsonFilterService.filterAttributesAndGetObjectFromJson(ctOfferResponse,
    			  CTOfferResponse.class, getIgnoreAttributesList());
			processedCTOffers = filterSpecialOffer(ctOfferResponse.getOffers());
			ctOfferResponse.setOffers(processedCTOffers);
			ctOfferResponse.setCount(processedCTOffers.size());
			ctOfferResponse.setTotal(processedCTOffers.size());
		}
*/
		if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getSalesChannel()).isPresent()
				&& Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferActionType()).isPresent()
				&& (offerRequestWrapper.getOfferRequest().getOfferActionType().contains(Constants.ACQUISITION))
				|| offerRequestWrapper.getOfferRequest().getOfferActionType().contains(Constants.UPSELL_ACTION_TYPE)) {

			if (offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.OPUS)) {
				if(offerRequestWrapper.getOfferRequest().getChannelEligibility()!=null) {
					ctOfferResponse = supressCorrectCDVR(true,ctOfferResponse);
				} else {
					ctOfferResponse = supressCorrectCDVR(false,ctOfferResponse);
				}

			}

			//to set display-type for CDVR offers for DEMO,DECA & COMP accounts
			if (ctOfferRequest.getBusinessSegment().contains(Constants.MDU)
					&&(offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.DIRECTV_ONLINE) ||
					offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.EVERGENT_CRM))
					&& !(Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerSegments()).isPresent()
					&& offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.MDUTENANT))) {
				ctOfferResponse = supressCorrectCDVR(true,ctOfferResponse);
			}else if ((offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.DIRECTV_ONLINE)
					&& ctOfferRequest.getBusinessSegment().contains(Constants.CONS))
					|| (offerRequestWrapper.getOfferRequest().getSalesChannel()
					.contains(Constants.DIRECTV_STREAM_ONLINE)
					&& ctOfferRequest.getBusinessSegment().contains(Constants.CONS))) {
				ctOfferResponse = supressCorrectCDVR(true,ctOfferResponse);
			}
			if (offersUtils.isPerformanceUpdatesEnabled(offerRequestWrapper.getOfferRequest().getSalesChannel())
					|| (Optional.ofNullable(ctOfferRequest.getPartnerDealerDetails()).isPresent()
					&& Optional.ofNullable(ctOfferRequest.getPartnerDealerDetails().getPartnerDealerCode1()).isPresent())) {
				ctOfferResponse = supressCorrectCDVR(true,ctOfferResponse);
			} else if (offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.INDIRECT_PARTNER)
					||offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.DIRECT_INTEGRATION_PARTNER)) {
				if (ctOfferRequest.getContractIndicator().contains(Constants.TAZ_STRING) ||
						ctOfferRequest.getContractIndicator().contains(Constants.TAZBYOD_STRING) ||
						ctOfferRequest.getContractIndicator().contains(Constants.TAZCONTRACT_STRING) ||
						ctOfferRequest.getContractIndicator().contains(Constants.ROAD_RUNNER) ||
						ctOfferRequest.getContractIndicator().contains(Constants.GENRE)
				) {
					ctOfferResponse = supressCorrectCDVR(true, ctOfferResponse);
				} else {
					ctOfferResponse = supressCorrectCDVR(false, ctOfferResponse);
				}
			}
		}
		if (ctOfferRequest.getBusinessSegment().contains(Constants.CONS)
				&& Optional.ofNullable(offerRequestWrapper.getOfferRequest().getSalesChannel()).isPresent()
				&& Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferActionType()).isPresent()
				&& offerRequestWrapper.getOfferRequest().getOfferActionType().contains(Constants.OTHER_ACTION_TYPE)
				&& (offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.OPUS)
				|| offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.DIRECTV_ONLINE))) {
			ctOfferResponse = supressCorrectCDVR(true,ctOfferResponse);
		}

		if(offerRequestWrapper.getOfferRequest().getAccountTypes()!=null &&
				offerRequestWrapper.getOfferRequest().getAccountTypes().contains(Constants.EMPLOYEE)){
			List<CTOffer> processedCTEmployeeOffers = new ArrayList<>();
			processedCTEmployeeOffers = removeConflictingEmployeeOffers(ctOfferResponse.getOffers());
			ctOfferResponse.setOffers(processedCTEmployeeOffers);
			ctOfferResponse.setCount(processedCTEmployeeOffers.size());
			ctOfferResponse.setTotal(processedCTEmployeeOffers.size());

		}


		//To filter CDVR offers for MDUTenant
		if(Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerSegments()).isPresent()
				&& offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.MDUTENANT)
				&& Optional.ofNullable(propertyOwnerData).isPresent()) {

			//unlimited_cdvr --> no/null   &&  free_unlimited_cdvr --> no/null
			if((!Optional.ofNullable(propertyOwnerData.isUnlimited_cdvr()).isPresent() || !propertyOwnerData.isUnlimited_cdvr())
					&&(!Optional.ofNullable(propertyOwnerData.isFree_unlimited_cdvr()).isPresent() || !propertyOwnerData.isFree_unlimited_cdvr())) {
				ctOfferResponse = supressCorrectCDVR(false,ctOfferResponse);
			}
/*        	else {
        		//unlimited_cdvr --> yes   &&  free_unlimited_cdvr --> no/null
        		if((Optional.ofNullable(propertyOwnerData.isUnlimited_cdvr()).isPresent() || propertyOwnerData.isUnlimited_cdvr())
            			&&(!Optional.ofNullable(propertyOwnerData.isFree_unlimited_cdvr()).isPresent() || !propertyOwnerData.isFree_unlimited_cdvr())) {
        			List<String> conflictingList = new ArrayList<String>();
        			List<CTOffer> finalOfferList = ctOfferResponse.getOffers();
        	        if (Objects.nonNull(finalOfferList)
        	      			&& !finalOfferList.isEmpty()) {
        	            finalOfferList.forEach(offer -> {
        	        		if(isFreePromoNotSpecialOffer(offer) && Objects.nonNull(offer.getAttributes())
        	        				&& Objects.nonNull(offer.getAttributes().getBenefits())
        	        				&& offer.getAttributes().getBenefits().isEmpty()){
        	        				if(Optional.ofNullable(offer.getAttributes().getConflictingOffers()).isPresent()
        	        				&& !offer.getAttributes().getConflictingOffers().isEmpty()) {
        	        			offer.getAttributes().getConflictingOffers().forEach(conflictingOffer -> conflictingList.add(conflictingOffer.getKey()));
        	            		offer.getAttributes().setConflictingOffers(null);
        					}
        	                    setDisplayTypeForCDVR(offer);
        	        		}
        	            });
        	        }
        	        if (!conflictingList.isEmpty()) {
        	        	finalOfferList = finalOfferList.stream().filter(offer -> (!conflictingList.contains(offer.getCode()))).collect(Collectors.toList());
        	        }

        	        ctOfferResponse.setOffers(finalOfferList);
        		}*/
			//unlimited_cdvr --> yes/no   &&  free_unlimited_cdvr --> yes/no but not both no/null
			else {
				List<String> conflictingList = new ArrayList<String>();
				List<CTOffer> finalOfferList = ctOfferResponse.getOffers();
                if (featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_CONFLICTING_OFFER_NEW_LOGIC) && CollectionUtils.isNotEmpty(finalOfferList)) {
                    finalOfferList.forEach(ctOffer -> {
                        if (isFreePromoNotSpecialOffer(ctOffer)) {
                            setDisplayTypeForCDVR(ctOffer);
                        }
                    });
                }else  if (Objects.nonNull(finalOfferList) && !finalOfferList.isEmpty()) {
					finalOfferList.forEach(offer -> {
						if(isFreePromoNotSpecialOffer(offer) && Objects.nonNull(offer.getAttributes())
								&& Objects.nonNull(offer.getAttributes().getBenefits())
								&& !offer.getAttributes().getBenefits().isEmpty()){
							if(Optional.ofNullable(offer.getAttributes().getConflictingOffers()).isPresent()
									&& !offer.getAttributes().getConflictingOffers().isEmpty()) {
								offer.getAttributes().getConflictingOffers().forEach(conflictingOffer -> conflictingList.add(conflictingOffer.getKey()));
								offer.getAttributes().setConflictingOffers(null);
							}
							setDisplayTypeForCDVR(offer);
						}
					});
				}
				if (!conflictingList.isEmpty()) {
					finalOfferList = finalOfferList.stream().filter(offer -> (!conflictingList.contains(offer.getCode()))).collect(Collectors.toList());
				}

				ctOfferResponse.setOffers(finalOfferList);
			}

//        	}

			if (ctOfferResponse != null && ctOfferRequest!=null && ctOfferRequest.getProductsOnAccountToSuppressOffer()!=null ) {
				ctOfferResponse = cpopClientHelper.filterProductsOnAccountToSuppressOffer(ctOfferResponse, ctOfferRequest);
			}

			//to filter bulk offers based on existing addons
			ctOfferResponse.setOffers(filterTenantOffers(ctOfferResponse,0,existingAddons,null));
		}

		boolean localsEnabledFlag = false;
		List<String> values = redisCacheHelper.getValues(Constants.REDIS_CACHE_LOCALS_ENABLED_FLAG, Constants.OTT);
		if (values != null && !values.isEmpty()) {
			localsEnabledFlag = Boolean.parseBoolean(values.get(0));
		}
		List<CTOffer> filteredOTTLocalsOffers = offersUtils.filterOffersByProductsHavingSubCategoryLocals(ctOfferResponse.getOffers());
		if ((!localsEnabledFlag || !offerRequestWrapper.getOfferRequest().getHasOTTLocalChannels())
				&& !filteredOTTLocalsOffers.isEmpty()
				&& Objects.nonNull(ctOfferResponse.getOffers())
				&& !ctOfferResponse.getOffers().isEmpty()) {
			ctOfferResponse.getOffers().removeAll(filteredOTTLocalsOffers);
		}
		return ctOfferResponse;
	}

	private CTOfferResponse supressCorrectCDVR(boolean ixpFlag, CTOfferResponse ctOfferResponse) {

		if (ixpFlag) {
			ctOfferResponse = suppressOffers(ctOfferResponse, Constants.SUPPRESS_UMLIMITED_RACKRATE_OFFER);
		} else {
			ctOfferResponse = suppressOffers(ctOfferResponse, Constants.SUPPRESS_UMLIMITED_OFFER);
		}
		return ctOfferResponse;
	}

	public CTOfferResponse getEquipmentOffers(OfferRequestWrapper offerRequestWrapper,CustomerSubscriptionDetail customerSubscriptionDetail) {
		CTOfferResponse ctOfferResponse = null;
		List<String> replacementDeviceList = new ArrayList<>();
		List<CTOffer> processedCTOffers = new ArrayList<>();
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		if (Objects.nonNull(offerRequestWrapper.getCtOfferRequest())) {
			BeanUtils.copyProperties(offerRequestWrapper.getCtOfferRequest(), ctOfferRequest);
		}
		ctOfferRequest.setOfferActionType(offerRequestWrapper.getOfferRequest().getOfferActionType());
		log.info("API_NAME:EPOCH_GETOFFERS OFFERACTIONTYPE:{}",OffersUtils.sanitizeData(ctOfferRequest.getOfferActionType()));
		ctOfferRequest.setOfferProductType(Stream.of(Constants.VIDEO_DEVICE).collect(Collectors.toList()));
		ctOfferRequest.setPagination(offerRequestWrapper.getOfferRequest().getPagination());
		ctOfferRequest.setContractIndicator(offerRequestWrapper.getOfferRequest().getContractIndicator());
		ctOfferRequest.setSalesChannel(offerRequestWrapper.getOfferRequest().getSalesChannel());

		if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerSegments())
				&& (offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.DEMO)
				|| offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.DECA)
				|| offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.SHOWROOM)
				|| offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.COURTESY)
				|| offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.BCOMP)
				|| offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.MDUTENANT))){

			ctOfferRequest.setCustomerSegments(offerRequestWrapper.getOfferRequest().getCustomerSegments());
		}

		//set price protection and NBD for equipment offers
		setPriceProtectionNBDCTOffReq(offerRequestWrapper, ctOfferRequest);


		if(offerRequestWrapper.getOfferRequest().getSalesChannel() != null
				&& offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.INDIRECT_PARTNER)
				&& StringUtils.isNotBlank(offerRequestWrapper.getPartnerType()))
		{
			ctOfferRequest.setPartnerType(Stream.of(offerRequestWrapper.getPartnerType()).collect(Collectors.toList()));
		}
		if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext()) &&
				Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()) &&
				Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getIapPartnerAccountType()) &&
				offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getIapPartnerAccountType() != null) {
			ctOfferRequest.setIapPartnerAccountType(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getIapPartnerAccountType());
		}
		ctOfferRequest.setBusinessSegment(Objects.nonNull(offerRequestWrapper.getOfferRequest().getBusinessSegment())?
				offerRequestWrapper.getOfferRequest().getBusinessSegment() : Arrays.asList(Constants.CONS));


	/*	if (offerRequestWrapper.getOfferRequest().getOfferClassificationType() != null
				&& !offerRequestWrapper.getOfferRequest().getOfferClassificationType().isEmpty()) {
        	ctOfferRequest.setOfferClassificationType(offerRequestWrapper.getOfferRequest().getOfferClassificationType());
        }*/
		if (offersUtils.isService(offerRequestWrapper.getOfferRequest())) {
			if (offerRequestWrapper.getOfferRequest().getOfferClassificationType() != null
					&& !offerRequestWrapper.getOfferRequest().getOfferClassificationType().isEmpty()) {
				ctOfferRequest
						.setOfferClassificationType(offerRequestWrapper.getOfferRequest().getOfferClassificationType());

			} else {
				replacementDeviceList.add("non-replacement-device");
				ctOfferRequest.setOfferClassificationType(replacementDeviceList);
			}
		}
		ctOfferRequest.setBenefitsCodesToSuppressOffer(offerRequestWrapper.getOfferRequest().getBenefitsCodesToSuppressOffer());
		if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getBenefitsCustomerHasReceived())) {
			List<BenefitsCustomerHasReceived> benefitsCustomerHasReceived = new ArrayList<>();
			benefitsCustomerHasReceived = offerRequestWrapper.getOfferRequest().getBenefitsCustomerHasReceived();
			List<BenefitCodesToSuppressTheOffer> benefitCodesToSuppressTheOffer = convertToBenefitCodesToSuppressTheOffer(benefitsCustomerHasReceived);
			ctOfferRequest.setBenefitCodesToSuppressTheOffer(benefitCodesToSuppressTheOffer);
		}

		if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext())
				&& (Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt())) && (Objects
				.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getProducts()))) {
			List<String> productsOnAccountToSuppress = new ArrayList<>();
			offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getProducts().stream()
					.filter(Objects::nonNull).forEach(prod -> {
						if (prod != null && prod.getProductCode() != null) {
							productsOnAccountToSuppress.add(prod.getProductCode());
						}
					});
			ctOfferRequest.setProductsOnAccountToSuppressOffer(productsOnAccountToSuppress);
		}

		// No need to set Customer Segments as it is not defined for device offers.
		// Also removing customerContext
		ctOfferRequest.setCustomerContext(null);

		// Set Credit Risk for sales flow
		if (!offersUtils.isService(offerRequestWrapper.getOfferRequest())) {
			String creditRisk = Constants.LOW_CREDIT_RISK;
			if(StringUtils.isNotBlank(offerRequestWrapper.getOfferRequest().getCreditRisk()))
			{
				creditRisk = offerRequestWrapper.getOfferRequest().getCreditRisk();
				if(creditRisk.equalsIgnoreCase(Constants.UNKNOWN_CREDIT_RISK)){
					creditRisk = Constants.HIGH_CREDIT_RISK;
				}
			}
			ctOfferRequest.setCreditRisk(creditRisk);
		}

		// Set Credit Risk for services flow if request has credit risk
		if (offersUtils.isService(offerRequestWrapper.getOfferRequest()) && StringUtils.isNotBlank(offerRequestWrapper.getOfferRequest().getCreditRisk())) {
			String creditRisk = offerRequestWrapper.getOfferRequest().getCreditRisk();
			if(creditRisk.equalsIgnoreCase(Constants.UNKNOWN_CREDIT_RISK)){
				creditRisk = Constants.HIGH_CREDIT_RISK;
			}

			ctOfferRequest.setCreditRisk(creditRisk);
		}


		// Fix to resolve Sales Blocker (Temporary fix to be removed once CT Fix is in place)
		if (offerRequestWrapper.getOfferRequest().isMigrationIndicator()) {
			OfferRequest offerRequest = offerRequestWrapper.getOfferRequest();
			log.info("EPOCH_GETOFFERS_ACQUISITIONFLOW_MIGRATION_OFFERSREQUEST VIDEO DEVICE");
			ctOfferRequest.setMigrationIndicator(offerRequest.isMigrationIndicator());
		}
		// For Reconnect flow to fetch video device offers, setting Customer Segments as null as Customer Segments is not defined for device offers
		if(isReconnectFlow(offerRequestWrapper))
		{
			ctOfferRequest.setCustomerSegments(null);
		}
		ctOfferResponse = getOffersFromCT(ctOfferRequest,offerRequestWrapper);
		ctOfferResponse = getDeviceOffersBasedOnQualifyingProductsNew(ctOfferResponse, ctOfferRequest, offerRequestWrapper);
		// Check to filter Device offers based on Qualifying Products
		//ctOfferResponse = getDeviceOffersBasedOnQualifyingProducts(ctOfferResponse, ctOfferRequest, offerRequestWrapper);

		// Check to filter  offers based on ProductsOnAccountToSupress offer
		if (ctOfferResponse != null && ctOfferRequest != null&& ctOfferRequest.getProductsOnAccountToSuppressOffer() != null) {
			ctOfferResponse = cpopClientHelper.filterProductsOnAccountToSuppressOffer(ctOfferResponse, ctOfferRequest);
		}
		ctOfferResponse = getPendentTargetedOffers(ctOfferResponse,offerRequestWrapper);
		ctOfferResponse = Objects.nonNull(customerSubscriptionDetail) ?
				filterProductsOnDisallowReplacementForPromotion(ctOfferResponse,offerRequestWrapper,customerSubscriptionDetail.getListDeviceServiceDetail())
				: ctOfferResponse ;

		if(offerRequestWrapper.getOfferRequest().getAccountTypes()!=null &&
				offerRequestWrapper.getOfferRequest().getAccountTypes().contains(Constants.EMPLOYEE)){
			processedCTOffers = removeConflictingEmployeeOffers(ctOfferResponse.getOffers());
			ctOfferResponse.setOffers(processedCTOffers);
			ctOfferResponse.setCount(processedCTOffers.size());
			ctOfferResponse.setTotal(processedCTOffers.size());

		}

		if (offersUtils.isService(offerRequestWrapper.getOfferRequest())
				&& Objects.nonNull(OffersUtils.getIapPartnerAccountType(offerRequestWrapper.getOfferRequest()))) {
			processedCTOffers = checkExcludedParter(ctOfferResponse.getOffers(), OffersUtils.getIapPartnerAccountType(offerRequestWrapper.getOfferRequest()));
			ctOfferResponse.setOffers(processedCTOffers);
			ctOfferResponse.setCount(processedCTOffers.size());
			ctOfferResponse.setTotal(processedCTOffers.size());
		}

		if (offersUtils.isService(offerRequestWrapper.getOfferRequest()) && Objects.nonNull(ctOfferResponse.getOffers())
				&& !ctOfferResponse.getOffers().isEmpty()) {
			ctOfferResponse.getOffers().forEach(offer -> {
				if(Optional.ofNullable(offer.getAttributes().getExcludedPartners()).isPresent()) {
					offer.getAttributes().setExcludedPartners(null);
				}
			});
		}

		if (Objects.nonNull(ctOfferResponse.getOffers()) && !ctOfferResponse.getOffers().isEmpty()
		) {
			processedCTOffers = removeConflictingDeviceOffers(ctOfferResponse.getOffers());
			ctOfferResponse.setOffers(processedCTOffers);
			ctOfferResponse.setCount(processedCTOffers.size());
			ctOfferResponse.setTotal(processedCTOffers.size());
		}

		//to set maxoccurance, minmaxquantity and freeDeviceCount in device-offers for COMP and Tenant accounts
		if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getBusinessSegment())
				&& offerRequestWrapper.getOfferRequest().getBusinessSegment().contains(Constants.MDU)
				&& Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerSegments()))
		{
			AtomicInteger freeDeviceCount= new AtomicInteger(-1);
			AtomicInteger bcompDeviceCount= new AtomicInteger(-1);
			AtomicInteger courtesyDeviceCount= new AtomicInteger(-1);
			AtomicInteger showroomDeviceCount= new AtomicInteger(-1);
			EvergentContract propertyOwnerData=new EvergentContract();
			if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext())
					&&	Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt())
					&&	Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getPropertyOwnerActNumber()))
			{
				propertyOwnerData = accountData.fetchTenantData(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getPropertyOwnerActNumber()).get(0);

				if(offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.MDUTENANT)) {
					freeDeviceCount.set(propertyOwnerData.getAddtl_devices()+propertyOwnerData.getAmt_of_addtl_free_devices()+1);
				}
				bcompDeviceCount.set(propertyOwnerData.getTotal_devices_per_bcomp_acct());
				courtesyDeviceCount.set(propertyOwnerData.getTotal_devices_per_courtesy_acct());
				showroomDeviceCount.set(propertyOwnerData.getTotal_devices_per_showroom_acct());
			}
			ctOfferResponse.getOffers().forEach(offer -> {
				if(Optional.ofNullable(offer.getAttributes().getBenefits()).isPresent()
						&& !offer.getAttributes().getBenefits().isEmpty()) {
					offer.getAttributes().getBenefits().forEach(benefit -> {
						if(offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.MDUTENANT)) {
							offersUtils.setMaxOccurance(offerRequestWrapper.getOfferRequest().getCustomerSegments(),benefit , offer, offerRequestWrapper,freeDeviceCount.get());
						}else {
							if(offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.BCOMP) && bcompDeviceCount.get()!=-1 ) {
								benefit.setMaxOccurrence_bcomp(bcompDeviceCount.get());
								freeDeviceCount.set(bcompDeviceCount.get());
							}if(offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.SHOWROOM) && showroomDeviceCount.get()!=-1) {
								benefit.setMaxOccurrence_showroom(showroomDeviceCount.get());
								freeDeviceCount.set(showroomDeviceCount.get());
							}if(offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.COURTESY) && courtesyDeviceCount.get()!=-1){
								benefit.setMaxOccurrence_courtesy(courtesyDeviceCount.get());
								freeDeviceCount.set(courtesyDeviceCount.get());
							}
							if(freeDeviceCount.get()==-1) {
								if(offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.BCOMP)) {
									freeDeviceCount.set(benefit.getMaxOccurrence_bcomp());
								}if(offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.SHOWROOM)) {
									freeDeviceCount.set(benefit.getMaxOccurrence_showroom());
								}if(offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.COURTESY)){
									freeDeviceCount.set(benefit.getMaxOccurrence_courtesy());
								}
							}
							offersUtils.setMaxOccurance(offerRequestWrapper.getOfferRequest().getCustomerSegments(),benefit , offer, offerRequestWrapper,freeDeviceCount.get());
						}
					});
				}
				else {
					if(offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.MDUTENANT)) {
						offersUtils.setMaxOccurance(offerRequestWrapper.getOfferRequest().getCustomerSegments(),null , offer, offerRequestWrapper,freeDeviceCount.get());
					}else {
						offersUtils.setMaxOccurance(offerRequestWrapper.getOfferRequest().getCustomerSegments(),null , offer, offerRequestWrapper,0);
					}
				}
			});
		}
		return ctOfferResponse;

	}

	public List<CTOffer> checkExcludedParter(List<CTOffer> offers, String iapPartnerAccountType) {
		if (Objects.nonNull(offers) && !offers.isEmpty()) {
			offers = offers.stream()
					.filter(offer -> ((!Optional.ofNullable(offer.getAttributes().getExcludedPartners()).isPresent())
							|| (Optional.ofNullable(offer.getAttributes().getExcludedPartners()).isPresent()
							&& !offer.getAttributes().getExcludedPartners().contains(iapPartnerAccountType))))
					.collect(Collectors.toList());
		}
		return offers;
	}

	public CTOfferResponse getFeeOffers(OfferRequestWrapper offerRequestWrapper) {
		CTOfferResponse ctOfferResponse = null;
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		if (Objects.nonNull(offerRequestWrapper.getCtOfferRequest())) {
			BeanUtils.copyProperties(offerRequestWrapper.getCtOfferRequest(), ctOfferRequest);
		}
		ctOfferRequest.setSalesChannel(offerRequestWrapper.getOfferRequest().getSalesChannel());
		ctOfferRequest.setOfferActionType(offerRequestWrapper.getOfferRequest().getOfferActionType());
		log.info("API_NAME:EPOCH_GETOFFERS OFFERACTIONTYPE:{}",OffersUtils.sanitizeData(ctOfferRequest.getOfferActionType()));
		ctOfferRequest.setOfferProductType(Stream.of(Constants.FEE).collect(Collectors.toList()));
		ctOfferRequest.setPagination(offerRequestWrapper.getOfferRequest().getPagination());
		ctOfferRequest.setContractIndicator(offerRequestWrapper.getOfferRequest().getContractIndicator());

		// No need to set Customer Segments as it is not defined for fee offers.
		// Also removing customerContext
		ctOfferRequest.setCustomerContext(null);
		ctOfferRequest.setCartContext(null);
		ctOfferRequest.setBenefitsCodesToSuppressOffer(offerRequestWrapper.getOfferRequest().getBenefitsCodesToSuppressOffer());
		if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getBenefitsCustomerHasReceived())) {
			List<BenefitsCustomerHasReceived> benefitsCustomerHasReceived = new ArrayList<>();
			benefitsCustomerHasReceived = offerRequestWrapper.getOfferRequest().getBenefitsCustomerHasReceived();
			List<BenefitCodesToSuppressTheOffer> benefitCodesToSuppressTheOffer = convertToBenefitCodesToSuppressTheOffer(benefitsCustomerHasReceived);
			ctOfferRequest.setBenefitCodesToSuppressTheOffer(benefitCodesToSuppressTheOffer);
		}
		// Fix to resolve Sales Blocker (Temporary fix to be removed once CT Fix is in place)
		if (offerRequestWrapper.getOfferRequest().isMigrationIndicator()) {
			OfferRequest offerRequest = offerRequestWrapper.getOfferRequest();
			log.info("EPOCH_GETOFFERS_ACQUISITIONFLOW_MIGRATION_OFFERSREQUEST VIDEO FEE");
			ctOfferRequest.setMigrationIndicator(offerRequest.isMigrationIndicator());
		}
		ctOfferResponse = getOffersFromCT(ctOfferRequest,offerRequestWrapper);
		//filter do fee offer based on credit risk/ treatment code
		ctOfferResponse.setOffers(offersUtils.filterDOFeeOffer(ctOfferResponse, offerRequestWrapper));
		if( offerRequestWrapper.getOfferRequest().isMigrationIndicator() || featureManagerHelper.isEnabled(CPOP_MIGRATION_OFFER_ENABLED)) {
			filterNonActivationFeeOffers(ctOfferResponse);
		}
		return ctOfferResponse;
	}

	/**
	 *
	 * @param offerRequestWrapper
	 * @return
	 */
	public CTOfferResponse getOfferByIds(OfferRequestWrapper offerRequestWrapper) {
		CTOfferResponse ctOfferResponse = null;
		CTOfferResponse ctOfferResponse1 = null;
		List<String> reconnectOffer = new ArrayList<>();

		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		if (Objects.nonNull(offerRequestWrapper.getCtOfferRequest())) {
			BeanUtils.copyProperties(offerRequestWrapper.getCtOfferRequest(), ctOfferRequest);
		}
		ctOfferRequest.setOfferRequest(offerRequestWrapper.getOfferRequest());
		ctOfferRequest.setBusinessSegment(Objects.nonNull(offerRequestWrapper.getOfferRequest().getBusinessSegment())?
				offerRequestWrapper.getOfferRequest().getBusinessSegment() : Arrays.asList(Constants.CONS));
		ctOfferRequest.setOfferIds(offerRequestWrapper.getOfferRequest().getOfferIds());
		ctOfferRequest.setOfferCodes(offerRequestWrapper.getOfferRequest().getOfferCodes());
		ctOfferRequest.setBundleProducts(offerRequestWrapper.getOfferRequest().getBundleProductIds());
		ctOfferRequest.setPagination(offerRequestWrapper.getOfferRequest().getPagination());
		ctOfferRequest.setCustomerSegments(offerRequestWrapper.getOfferRequest().getCustomerSegments());
		ctOfferRequest.setBusinessSegments(offerRequestWrapper.getOfferRequest().getBusinessSegments());
		ctOfferRequest.setBenefitsCodesToSuppressOffer(offerRequestWrapper.getOfferRequest().getBenefitsCodesToSuppressOffer());
		ctOfferRequest.setContractIndicator(offerRequestWrapper.getOfferRequest().getContractIndicator());
		if(StringUtils.isNotBlank(offerRequestWrapper.getOfferRequest().getCreditRisk()))
		{
			ctOfferRequest.setCreditRisk(offerRequestWrapper.getOfferRequest().getCreditRisk());
		}
		if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getOnlinePartnerDetails())){
			ctOfferRequest.setOnlinePartnerDetails(offerRequestWrapper.getOfferRequest().getOnlinePartnerDetails());
		}
		ctOfferRequest.setSalesChannel(offerRequestWrapper.getOfferRequest().getSalesChannel());
		if(offerRequestWrapper.getOfferRequest().getSalesChannel() != null
				&& offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.INDIRECT_PARTNER)
				&& StringUtils.isNotBlank(offerRequestWrapper.getPartnerType()))
		{
			ctOfferRequest.setPartnerType(Stream.of(offerRequestWrapper.getPartnerType()).collect(Collectors.toList()));
		}

		if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getBenefitsCustomerHasReceived())) {
			List<BenefitsCustomerHasReceived> benefitsCustomerHasReceived = new ArrayList<>();
			benefitsCustomerHasReceived = offerRequestWrapper.getOfferRequest().getBenefitsCustomerHasReceived();
			List<BenefitCodesToSuppressTheOffer> benefitCodesToSuppressTheOffer = convertToBenefitCodesToSuppressTheOffer(
					benefitsCustomerHasReceived);
			ctOfferRequest.setBenefitCodesToSuppressTheOffer(benefitCodesToSuppressTheOffer);
		}
		if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getChannelEligibility())) {
			cTOfferRequestHelper.setCTCheckEligibilty(offerRequestWrapper.getOfferRequest(), ctOfferRequest);
			ctOfferRequest.setOpusChannel(offerRequestWrapper.getOfferRequest().getChannelEligibility().getOpusChannel());
			ctOfferRequest.setOpusSubChannel(offerRequestWrapper.getOfferRequest().getChannelEligibility().getOpusSubChannel());
			ctOfferRequest.setOpusStoreId(offerRequestWrapper.getOfferRequest().getChannelEligibility().getOpusStoreId());

		}
		// only in reconnect flow, below are executed to fetch reconnect offers if
		// within the reconnect window period
		if (Optional.ofNullable(offerRequestWrapper.getOfferRequest()).isPresent() &&
				Optional.ofNullable(offerRequestWrapper.getOfferRequest().isReconnectCustomer()).isPresent() &&
				offerRequestWrapper.getOfferRequest().isReconnectCustomer() == true &&
				Optional.ofNullable(offerRequestWrapper.getOfferRequest().getServiceEndDate()).isPresent()) {
			if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerSegments())
					&& offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.EMPLOYEE)) {
				ctOfferRequest.setCustomerSegments(null);
			}
			if (isEligibleToReconnect(offerRequestWrapper, ctOfferRequest)) {
				ctOfferResponse = fetchReconnectFlowOffers(offerRequestWrapper, ctOfferRequest, true);
			} else {
				ctOfferResponse = fetchReconnectFlowOffers(offerRequestWrapper, ctOfferRequest, false);
			}
		} else {
			ctOfferResponse = getOffersFromCT(ctOfferRequest, offerRequestWrapper);
		}
		//offersUtils.filterOffersBasedOnSalesChannel(ctOfferResponse, offerRequestWrapper);
		if(CollectionUtils.isNotEmpty(ctOfferResponse.getOffers())) {
			ctOfferResponse.getOffers().forEach(offer -> {
				if (offer.getAttributes().getOfferProductType().equalsIgnoreCase(Constants.VIDEO_ADDON) && isFreePromoNotSpecialOffer(offer)) {
					setDisplayTypeForCDVR(offer);
				}
			});
		}
		offersUtils.filterOfferBasedOnZipOrDMA(ctOfferResponse.getOffers(), offerRequestWrapper);
		//NonStackable Functionality in Acquisition flow with ixp flag
        if (featureManagerHelper.isEnabled(Constants.FEATURE_NON_STACKABLE_RULE_ENABLED)) {
            offersUtils.filterOffersBasedOnNonStackableCartOffers(ctOfferResponse.getOffers(), offerRequestWrapper);
        }
		ctOfferResponse.setCount(ctOfferResponse.getOffers().size());
		ctOfferResponse.setTotal(ctOfferResponse.getOffers().size());
		if (ctOfferResponse.getOffers().isEmpty()) {
			ctOfferResponse.setProducts(null);
		}


		//to set maxoccurance, minmaxquantity and freeDeviceCount in device-offers for COMP and Tenant accounts only for video devices
		if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getBusinessSegment())
				&& offerRequestWrapper.getOfferRequest().getBusinessSegment().contains(Constants.MDU)
				&& Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerSegments()))
		{
			AtomicInteger freeDeviceCount= new AtomicInteger(-1);
			AtomicInteger bcompDeviceCount= new AtomicInteger(-1);
			AtomicInteger courtesyDeviceCount= new AtomicInteger(-1);
			AtomicInteger showroomDeviceCount= new AtomicInteger(-1);
			EvergentContract propertyOwnerData=new EvergentContract();
			if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext())
					&&	Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt())
					&&	Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getPropertyOwnerActNumber()))
			{
				propertyOwnerData = accountData.fetchTenantData(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getPropertyOwnerActNumber()).get(0);

				if(offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.MDUTENANT)) {
					freeDeviceCount.set(propertyOwnerData.getAddtl_devices()+propertyOwnerData.getAmt_of_addtl_free_devices()+1);
				}
				bcompDeviceCount.set(propertyOwnerData.getTotal_devices_per_bcomp_acct());
				courtesyDeviceCount.set(propertyOwnerData.getTotal_devices_per_courtesy_acct());
				showroomDeviceCount.set(propertyOwnerData.getTotal_devices_per_showroom_acct());
			}
			ctOfferResponse.getOffers().forEach(offer -> {
				if(Objects.nonNull(offer.getAttributes()) && Objects.nonNull(offer.getAttributes().getOfferProductTypes()) && offer.getAttributes().getOfferProductTypes().contains(Constants.VIDEO_DEVICE)
						&& Optional.ofNullable(offer.getAttributes().getBenefits()).isPresent()
						&& !offer.getAttributes().getBenefits().isEmpty()) {
					offer.getAttributes().getBenefits().forEach(benefit -> {
						if(offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.MDUTENANT)) {
							offersUtils.setMaxOccurance(offerRequestWrapper.getOfferRequest().getCustomerSegments(),benefit , offer, offerRequestWrapper,freeDeviceCount.get());
						}else {
							if(offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.BCOMP) && bcompDeviceCount.get()!=-1 ) {
								benefit.setMaxOccurrence_bcomp(bcompDeviceCount.get());
								freeDeviceCount.set(bcompDeviceCount.get());
							}if(offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.SHOWROOM) && showroomDeviceCount.get()!=-1) {
								benefit.setMaxOccurrence_showroom(showroomDeviceCount.get());
								freeDeviceCount.set(showroomDeviceCount.get());
							}if(offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.COURTESY) && courtesyDeviceCount.get()!=-1){
								benefit.setMaxOccurrence_courtesy(courtesyDeviceCount.get());
								freeDeviceCount.set(courtesyDeviceCount.get());
							}
							if(freeDeviceCount.get()==-1) {
								if(offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.BCOMP)) {
									freeDeviceCount.set(benefit.getMaxOccurrence_bcomp());
								}if(offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.SHOWROOM)) {
									freeDeviceCount.set(benefit.getMaxOccurrence_showroom());
								}if(offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.COURTESY)){
									freeDeviceCount.set(benefit.getMaxOccurrence_courtesy());
								}
							}
							offersUtils.setMaxOccurance(offerRequestWrapper.getOfferRequest().getCustomerSegments(),benefit , offer, offerRequestWrapper,freeDeviceCount.get());
						}
					});
				}
				else {
					if(offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.MDUTENANT)) {
						offersUtils.setMaxOccurance(offerRequestWrapper.getOfferRequest().getCustomerSegments(),null , offer, offerRequestWrapper,freeDeviceCount.get());
					}else {
						offersUtils.setMaxOccurance(offerRequestWrapper.getOfferRequest().getCustomerSegments(),null , offer, offerRequestWrapper,0);
					}
				}
			});
		}

		return ctOfferResponse;

	}

	/**
	 * @param ctOfferRequest
	 * @return
	 * @throws ServiceException
	 */


	public CTOfferResponse getOffersFromCT(CTOfferRequest ctOfferRequest, OfferRequestWrapper offerRequestWrapper) {
		CTOfferResponse offerResponse = null;
		ctOfferRequest.setOfferProductFamily(Stream.of("OTT").collect(Collectors.toList()));

		ctOfferRequest.setFlow(offerRequestWrapper.getFlow());
		if(CollectionUtils.isNotEmpty(ctOfferRequest.getOfferProductType()) && !ctOfferRequest.getOfferProductType().stream().anyMatch(Arrays.asList(Constants.FEE, Constants.CREDIT)::contains)
				&& featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_VALIDATE_GRAPHQL) && null !=  offerRequestWrapper.getFlow() && offerRequestWrapper.getFlow().equalsIgnoreCase("validateCart") && CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getBundleProductIds()) ) {
			ctOfferRequest.setBundleProducts(offerRequestWrapper.getOfferRequest().getBundleProductIds());
		}
		long startTimeInMillis = System.currentTimeMillis();
		// log.info("CT Offers request employee offers {}",ctOfferRequest);
		offerResponse = cpopClientHelper.getOffers(ctOfferRequest);
		log.info("EPOCH_CT_OFFERS_EXECUTION_TIME_DETAILS-[{}, {}, {}, {}]", (System.currentTimeMillis() - startTimeInMillis),OffersUtils.sanitizeData(ctOfferRequest.getSalesChannel()),OffersUtils.sanitizeData(ctOfferRequest.getOfferProductType()), OffersUtils.sanitizeData(ctOfferRequest.getOfferActionType()));
		if (offerResponse == null) {
			log.info("In Method getOffersFromCT.OttCTOffersProcessor() :: Getting the null response from the CTMS");
			if (Objects.nonNull(ctOfferRequest.getOfferProductFamily())
					&& ctOfferRequest.getOfferProductFamily().stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
				throw new ServiceException(ErrorMessages.OTT_CT_ERROR)
						.addDetail(ErrorMessages.OTT_CT_ERROR_DETAILS);
			} else {
				throw new ServiceException(ErrorMessages.CT_ERROR)
						.addDetail(ErrorMessages.CT_ERROR_DETAILS);
			}
		} else {
			if(!ctOfferRequest.isExpiredOffers()) {
				filterActiveOffers(offerResponse, ctOfferRequest);
			}
			if(offersUtils.isPerformanceUpdatesEnabled(offerRequestWrapper.getOfferRequest().getSalesChannel())) {
				long startPostProcessingTimes = System.currentTimeMillis();
				PartnerDealerDetails partnerDetails = offerRequestWrapper.getOfferRequest().getPartnerDealerDetails();
				offersUtils.filterOfferByAttribute(offerResponse,partnerDetails);
				log.info("TOTAL_POST_PROCESSING-[{}]",System.currentTimeMillis()- startPostProcessingTimes );
			}
		}
		//Filter InEligibleStoreIds that matches the store ids from request.channelEligibility.opusStoreId
		filterInEligibleStoreIds(offerResponse, offerRequestWrapper);
		//acquisition flow and not reconnect
		log.info("In Method getOffersFromCT.OttCTOffersProcessor() :: Getting response from the CTMS");
		return offerResponse;
	}

	/**
	 * Filter active offers.
	 *
	 * @param offerResponse the offer response
	 */
	public void filterActiveOffers(CTOfferResponse offerResponse, CTOfferRequest ctOfferRequest) {
		List<CTOffer> offers = new ArrayList<>();
		List<CTOffer> ioOffers = new ArrayList<>();
		if (Optional.ofNullable(offerResponse).isPresent() && Optional.ofNullable(offerResponse.getOffers()).isPresent()) {
			offerResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {
				if (ctOffer != null && ctOffer.getStartDate() != null && ctOffer.getEndDate() != null
						&& OffersUtils.validateActiveDates(OffersUtils.getFormattedDate(ctOffer.getStartDate()),
						OffersUtils.getFormattedDate(ctOffer.getEndDate()))) {
					offers.add(ctOffer);
				} else if ( Objects.nonNull(ctOfferRequest.getSalesChannel()) && StringUtils.equalsIgnoreCase(Constants.OPUS, ctOfferRequest.getSalesChannel().get(0)) &&
						offersUtils.isServiceCT(ctOfferRequest) && ctOffer.getAttributes() != null && org.apache.commons.collections.CollectionUtils.isNotEmpty(ctOffer.getAttributes().getBenefits()) && ctOffer.getAttributes().getBenefits().get(0) != null &&
						(ctOffer.getAttributes().getBenefits().get(0).isIoOffer())) {
					// Separating IOOffers only for OPUS
					log.debug(":::::::::IOOFFER: " + OffersUtils.sanitizeData(ctOffer.getCode()));
					ioOffers.add(ctOffer);
				} else
				{
					log.debug("This is the filtered offer ::: "+ctOffer.getCode());
				}
			});
			offerResponse.setOffers(offers);
			offerResponse.setCount(offers.size());
			offerResponse.setTotal(offers.size());
			offerResponse.setIoOffers(ioOffers);
		}
	}

	/**
     * set price protection and next billing date in CTOfferRequest if the offer request does not contain TAZCONTRACT
     * or ROADRUNNER offer and contains price protection details in customer context.
     * This is required as part of HOG implementation
     * This would be used for Video Addons, Video Addons Standalone, equipment offers and protection plan offers
     * User Story 4531, 4533 (26.1.5)
     * @param offerRequestWrapper
     * @param ctOfferRequest
     */

    private void setPriceProtectionNBDCTOffReq(OfferRequestWrapper offerRequestWrapper,
                                               CTOfferRequest ctOfferRequest){
        if(!offersUtils.containsTAZCONTRACT(offerRequestWrapper.getOfferRequest()) &&
                !offersUtils.containsROADRUNNER(offerRequestWrapper.getOfferRequest()) &&
                Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext()) &&
                Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()) &&
                Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getPriceProtection())) {
            PriceProtection priceProtection = offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getPriceProtection();
            priceProtection.setNextBillingDate(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getNextBillingDate());
            if(offerRequestWrapper.getCurrentDate()!=null){
                priceProtection.setCurrentDate(offerRequestWrapper.getCurrentDate());
            }else{
                priceProtection.setCurrentDate(new SimpleDateFormat("MM/dd/yyyy").format(new Date()));
            }

            ctOfferRequest.setPriceProtection(priceProtection);
        } else if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext()) &&
                Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()) &&
                offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getNextBillingDate()!=null) {
            ctOfferRequest.setNextBillingDate(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getNextBillingDate());
        }
    }

	/**
	 * Filter non activation fee offers.
	 *
	 * @param ctOfferResponse the ct offer response
	 */
	public void filterNonActivationFeeOffers(CTOfferResponse ctOfferResponse) {
		if (Optional.ofNullable(ctOfferResponse).isPresent() && Optional.ofNullable(ctOfferResponse.getOffers()).isPresent()) {
			List<CTOffer> otherFeeOffers = ctOfferResponse.getOffers().stream().filter(Objects::nonNull)
					.filter(offer ->
							offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts() != null &&
									offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0) != null &&
									offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts() != null &&
									offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0) != null &&
									offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj() != null &&
									offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants() != null &&
									offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0) != null &&
									offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes() != null &&
									!ACTIVATION_FEE.equalsIgnoreCase(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().getFeeType()))
					.collect(Collectors.toList());
			ctOfferResponse.setOffers(otherFeeOffers);
		}
	}

	public List<CTCustomerContext> excludeUverseCustomerContext(CTOfferRequest ctOfferRequest) {
		return ctOfferRequest.getCustomerContext().stream().filter(Objects::nonNull)
				.filter(custContext ->
						!Constants.IPTV_PRODUCT_FAMILY.equalsIgnoreCase(custContext.getProductFamily()) &&
								!Constants.DTVS_PRODUCT_FAMILY.equalsIgnoreCase(custContext.getProductFamily()) &&
								!Constants.BB_PRODUCT_FAMILY.equalsIgnoreCase(custContext.getProductFamily()))
				.collect(Collectors.toList());
	}

	public List<BenefitCodesToSuppressTheOffer> convertToBenefitCodesToSuppressTheOffer(List<BenefitsCustomerHasReceived> benefitsCustomerHasReceivedList){
		List<BenefitCodesToSuppressTheOffer> result = new ArrayList<>();
		benefitsCustomerHasReceivedList.forEach(benefitsCustomerHasReceived -> {
			BenefitCodesToSuppressTheOffer obj = new BenefitCodesToSuppressTheOffer();
			obj.setBenefitCode(benefitsCustomerHasReceived.getBenefitCode());
			obj.setStartDate(benefitsCustomerHasReceived.getStartDate());
			obj.setEndDate(benefitsCustomerHasReceived.getEndDate());
			result.add(obj);
		});
		return result;
	}

	private boolean isCustomerSegmentPresent(List<String> custSegments, String matchCustSegment) {
		return custSegments != null ? custSegments.stream().anyMatch(custSegment -> custSegment.equals(matchCustSegment)): false;
	}
	public boolean isEligibleOffers(CTOffer offer, List<String> basePackageProd) {

		List<CTOffer> offers = new ArrayList<>();

		if (Objects.nonNull(offer.getAttributes())
				&& Objects.nonNull(offer.getAttributes().getAssociatedProducts())
				&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getQualifyingProducts())) {
			List<ProductWrapper> qualifyingProducts = offer.getAttributes().getAssociatedProducts().get(0).getQualifyingProducts();
			qualifyingProducts.stream().filter(Objects::nonNull).forEach(qualifyingProduct -> {
				List<Product> products = qualifyingProduct.getProducts();
				if (Objects.nonNull(products) && !products.isEmpty()) {
					products.stream().filter(Objects::nonNull).forEach(product -> {
						if (basePackageProd.contains(product.getKey())) {
							offers.add(offer);
						}
					});
				}
			});
		}

		return offers.size()>0? true: false;
	}

	private boolean isReconnectFlow(OfferRequestWrapper offerRequestWrapper) {
		return (Optional.ofNullable(offerRequestWrapper.getOfferRequest()).isPresent()
				&& (offersUtils.containsEDSP(offerRequestWrapper.getOfferRequest())
				|| offersUtils.containsTAZ(offerRequestWrapper.getOfferRequest())
				|| offersUtils.containsTAZBYOD(offerRequestWrapper.getOfferRequest())
				|| offersUtils.containsGENRE(offerRequestWrapper.getOfferRequest())
				|| offersUtils.containsROADRUNNER(offerRequestWrapper.getOfferRequest())
				|| offersUtils.containsTAZCONTRACT(offerRequestWrapper.getOfferRequest()))
				&& Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferActionType()).isPresent()
				&& (offerRequestWrapper.getOfferRequest().getOfferActionType().contains(Constants.ACQUISITION)
				|| offerRequestWrapper.getOfferRequest().getOfferActionType().contains(Constants.UPSELL_ACTION_TYPE)
				|| offerRequestWrapper.getOfferRequest().getOfferActionType().contains(Constants.CLOSING_ACTION_TYPE))
				&& Optional.ofNullable(offerRequestWrapper.getOfferRequest().isReconnectCustomer()).isPresent()
				&& offerRequestWrapper.getOfferRequest().isReconnectCustomer() == true) ? true : false;
	}

	private boolean isValidSubscribeType(OfferRequestWrapper offerRequestWrapper) {
		List<String> notValidSubscribeType = Arrays.asList(Constants.BYOD, Constants.DTVN);
		return (!Optional.ofNullable(offerRequestWrapper.getOfferRequest().getExistingAccountSubscriberType()).isPresent() ||
				(Optional.ofNullable(offerRequestWrapper.getOfferRequest().getExistingAccountSubscriberType()).isPresent() &&
						!notValidSubscribeType.contains(offerRequestWrapper.getOfferRequest().getExistingAccountSubscriberType())));
	}

	private boolean isValidSubscribeTypeForOtherCohorts(OfferRequestWrapper offerRequestWrapper, CTOffer offer) {
		if(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_OFFER_CODE_VALID_SUBSCRIBER_ENABLED)) {
			if (Optional.ofNullable(offerRequestWrapper.getOfferRequest()).isPresent()
					&& (offersUtils.containsEDSP(offerRequestWrapper.getOfferRequest())
					|| offersUtils.containsTAZ(offerRequestWrapper.getOfferRequest()))) {
				return offersUtils.checkIsOemChannel(offerRequestWrapper.getOfferRequest())
						? isValidSubscribeType(offerRequestWrapper)
						: offersUtils.isCustomerTypeReconnectLessThanEligibleMonthsOffers(offerRequestWrapper, offer);

			}
			return offersUtils.isCustomerTypeReconnectLessThanEligibleMonthsOffers(offerRequestWrapper, offer);
		} else
		{
			return isValidSubscribeType(offerRequestWrapper);
		}
	}

	public boolean isEligibleToReconnect(OfferRequestWrapper offerRequestWrapper, CTOfferRequest ctOfferRequest) {
		boolean eligible = false;
		List<String> offerCodes= Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferCodes()).isPresent() ?
				offerRequestWrapper.getOfferRequest().getOfferCodes() : new ArrayList<String>();
		eligible = offersUtils.isDateWithinEligibilityWindow(offerRequestWrapper.getOfferRequest().getServiceEndDate(),
				offerRequestWrapper.getOfferRequest().getSalesChannel(),
				ctOfferRequest.getOfferProductType(),offerCodes);
		log.info("==================eligibleToReconnect {}",eligible);
		return eligible;
	}

	private void updateChannelEligibilityToNull(CTOfferRequest ctOfferRequest) {
		ctOfferRequest.setOpusChannel(null);
		ctOfferRequest.setOpusSubChannel(null);
		ctOfferRequest.setOpusStoreId(null);
	}

	private CTOfferResponse fetchReconnectFlowOffers(OfferRequestWrapper offerRequestWrapper, CTOfferRequest ctOfferRequest, Boolean isReconnnectOffer) {
		CTOfferResponse finalCTOfferResponse = null;
		CTOfferResponse ctOfferResponse = null;
		List<String> reconnectOffer = new ArrayList<>();
		List<CTOffer> filteredOffersWithOfferProductTypeNotVideoPlan = new ArrayList<>();
		ctOfferResponse = getOffersFromCT(ctOfferRequest,offerRequestWrapper);
		if(ctOfferResponse != null
				&& CollectionUtils.isNotEmpty(ctOfferResponse.getOffers())
				&& ctOfferResponse.getOffers().size()>1){
			ctOfferResponse = offersUtils.updateCTOfferResponseIndexToVideoPlan(ctOfferResponse);
			filteredOffersWithOfferProductTypeNotVideoPlan = offersUtils.filterOffersWithOfferProductTypeNotVideoPlan(ctOfferResponse);
		}
		Boolean noChange = false;
		if (ctOfferResponse != null && CollectionUtils.isNotEmpty(ctOfferResponse.getOffers())) {
			for (CTOffer offer : ctOfferResponse.getOffers()) {
				if (decideNoSecondCTCall(offerRequestWrapper, offer, isReconnnectOffer)) {
					noChange = true;
					break;
				}
				if (Objects.nonNull(offer.getAttributes().getBillingCode())) {
					List<GenericTypeIdBase> typeIdBase = offer.getAttributes().getReconnectEligibleOffer();
					if (Objects.nonNull(typeIdBase)) {
						for (GenericTypeIdBase type : typeIdBase) {
							if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_VALIDATE_GRAPHQL)
									&& null != ctOfferRequest.getFlow()
									&& ctOfferRequest.getFlow().equalsIgnoreCase("validateCart")) {
								if (Optional.ofNullable(type.getId()).isPresent()) {
									reconnectOffer.add(type.getId());
								}
							} else {
								if (Optional.ofNullable(type.getKey()).isPresent()) {
									reconnectOffer.add(type.getKey());
								}
							}
						}
					}
				}

				if(CollectionUtils.isNotEmpty(reconnectOffer)){
					break;
				}
			}
		}
		if (noChange) {
			finalCTOfferResponse = ctOfferResponse;
		} else
		if (reconnectOffer != null && !reconnectOffer.isEmpty()) {
			if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_VALIDATE_GRAPHQL)
					&& null != ctOfferRequest.getFlow()
					&& ctOfferRequest.getFlow().equalsIgnoreCase("validateCart")) {
				ctOfferRequest.setOfferIds(reconnectOffer);
				ctOfferRequest.setOfferCodes(null);
			} else {
				ctOfferRequest.setOfferCodes(reconnectOffer);
			}
			finalCTOfferResponse = getOffersFromCT(ctOfferRequest,offerRequestWrapper);
			if (offersUtils.containsTAZCONTRACT(offerRequestWrapper.getOfferRequest()) && StringUtils
					.isNotEmpty(offerRequestWrapper.getOfferRequest().getExistingAccountSubscriberType()) && isReconnnectOffer) {
				offersUtils.filterOffersBasedOnCustomerTypeReconnect(finalCTOfferResponse, offerRequestWrapper);
			}
		}
		if (!noChange) {
			if (finalCTOfferResponse == null) {
				finalCTOfferResponse = new CTOfferResponse();
			}
			if (CollectionUtils.isNotEmpty(filteredOffersWithOfferProductTypeNotVideoPlan)) {
				if (finalCTOfferResponse.getOffers() == null) {
					finalCTOfferResponse.setOffers(filteredOffersWithOfferProductTypeNotVideoPlan);
				} else {
					finalCTOfferResponse.getOffers().addAll(filteredOffersWithOfferProductTypeNotVideoPlan);
				}
			} else if (finalCTOfferResponse.getOffers() == null) {
				finalCTOfferResponse.setOffers(new ArrayList<>());
			}
		}

		if (finalCTOfferResponse == null) {
			finalCTOfferResponse = new CTOfferResponse();
			finalCTOfferResponse.setOffers(new ArrayList<>());
		}
		if (featureManagerHelper.isEnabled(CpopConstants.EPOCH_MY_FREE_UPSELL_RECONNECT_DIP_FLAG)) {
			finalCTOfferResponse = filteredOffersBasedOnSalesChannels(offerRequestWrapper, finalCTOfferResponse);
			finalCTOfferResponse = removeConflictingOffersInOfferCodeCall(finalCTOfferResponse);
		}
		return finalCTOfferResponse;
	}

	private Boolean decideNoSecondCTCall(OfferRequestWrapper offerRequestWrapper, CTOffer offer, Boolean isReconnnectOffer) {
		if (isReconnnectOffer) {
			return (offer.getAttributes().getOfferProductType().contains(Constants.VIDEO_ADDON) ||
					offer.getAttributes().getOfferProductType().contains(Constants.VIDEO_DEVICE) ||
					// reconnect offer in cart context, if employee flow then no second call
					(offersUtils.isCustomerTypeReconnect(offer)
							&& isNotEmployeeFlowOrNoCustomerSegement(offerRequestWrapper)
							&& isValidSubscribeTypeForOtherCohorts(offerRequestWrapper,offer)) ||
					// EDSP offer in cart context, if employee flow then no second call
					(Optional.ofNullable(offer.getAttributes().getCustomerTypes()).isEmpty() && (isEmployeeFlowOrNoCustomerSegement(offerRequestWrapper) ||
							!isValidSubscribeTypeForOtherCohorts(offerRequestWrapper,offer))));
		} else {
			return (offer.getAttributes().getOfferProductType().contains(Constants.VIDEO_ADDON) ||
					offer.getAttributes().getOfferProductType().contains(Constants.VIDEO_DEVICE) ||
					Optional.ofNullable(offer.getAttributes().getCustomerTypes()).isEmpty());
		}

	}

	public Boolean isEmployeeFlowOrNoCustomerSegement(OfferRequestWrapper offerRequestWrapper) {
		return (Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerSegments())
				&& offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.EMPLOYEE));
	}

	public Boolean isNotEmployeeFlowOrNoCustomerSegement(OfferRequestWrapper offerRequestWrapper) {
		return (!Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerSegments())  ||
				(Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerSegments())
						&& !offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.EMPLOYEE)));
	}

	//	to filter out special offers in reconnect flow
	public List<CTOffer> filterSpecialOffer(List<CTOffer> processedCTOffers) {

		List<CTOffer> filteredOffers = new ArrayList<>();
		if (!processedCTOffers.isEmpty()) {
			Map<String, List<CTOffer>> groupedOffers = processedCTOffers.stream().collect(
					Collectors.groupingBy(offer -> offer.getAttributes().getBillingCode(), Collectors.toList()));

			groupedOffers.entrySet().parallelStream().forEach(offerMap -> {
				List<CTOffer> value = offerMap.getValue();
				value.stream().filter(Objects::nonNull).forEach(offer -> {
					if (!offer.getAttributes().isSpecialOffer()) {
						filteredOffers.add(offer);
					}
				});

			});
		}
		return filteredOffers;
	}

	public String[] getIgnoreAttributesList(){
		String[] ignoredAttr = offersUtils.getIgnoreAttributes().split(",");
		return ignoredAttr;
	}

	private List<CTOffer> removeConflictingOffers(List<CTOffer> finalOfferList) {
		List<String> conflictingList = new ArrayList<String>();
		if (Objects.nonNull(finalOfferList)
				&& !finalOfferList.isEmpty()) {
			finalOfferList.forEach(offer -> {
				if(isFreePromoNotSpecialOffer(offer)){
					if(Optional.ofNullable(offer.getAttributes().getConflictingOffers()).isPresent()
							&& !offer.getAttributes().getConflictingOffers().isEmpty()) {
						offer.getAttributes().getConflictingOffers().forEach(conflictingOffer -> conflictingList.add(conflictingOffer.getKey()));
						offer.getAttributes().setConflictingOffers(null);
					}
					setDisplayTypeForCDVR(offer);
				}
			});
		}
		if (!conflictingList.isEmpty()) {
			finalOfferList = finalOfferList.stream().filter(offer -> (!conflictingList.contains(offer.getCode()))).collect(Collectors.toList());
		}

		return finalOfferList;
	}

	public boolean isFreePromoNotSpecialOffer(CTOffer offer) {
		return Optional.ofNullable(offer.getAttributes()).isPresent()
				&& Optional.ofNullable(offer.getAttributes().getContractIndicator()).isPresent()
				&& (Constants.EDSP_STRING.equalsIgnoreCase(offer.getAttributes().getContractIndicator())
				||Constants.TAZ_STRING.equalsIgnoreCase(offer.getAttributes().getContractIndicator())
				||Constants.TAZBYOD_STRING.equalsIgnoreCase(offer.getAttributes().getContractIndicator())
				||Constants.ROAD_RUNNER.equalsIgnoreCase(offer.getAttributes().getContractIndicator())
				||Constants.TAZCONTRACT_STRING.equalsIgnoreCase(offer.getAttributes().getContractIndicator())
				||Constants.GENRE.equalsIgnoreCase(offer.getAttributes().getContractIndicator()))
				&& Optional.ofNullable(offer.getAttributes().isSpecialOffer()).isPresent()
				&& !offer.getAttributes().isSpecialOffer()
				&& Optional.ofNullable(offer.getAttributes().getOfferType()).isPresent()
				&& offer.getAttributes().getOfferType().equalsIgnoreCase(Constants.FREE_PROMO);
	}


	private List<CTOffer> removeConflictingEmployeeOffers(List<CTOffer> finalOfferList) {
		List<String> conflictingList = new ArrayList<String>();
		if (Objects.nonNull(finalOfferList)
				&& !finalOfferList.isEmpty()) {
			finalOfferList.forEach(offer -> {
				if(Optional.ofNullable(offer.getAttributes().getEligibility()).isPresent()
						&& Optional.ofNullable(offer.getAttributes().getEligibility().getConstraints()).isPresent()
						&& CollectionUtils.isNotEmpty(offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments())
						&& offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments().contains(Constants.EMPLOYEE)
						&& Optional.ofNullable(offer.getAttributes().getConflictingOffers()).isPresent()
						&& !offer.getAttributes().getConflictingOffers().isEmpty()) {
					offer.getAttributes().getConflictingOffers().forEach(conflictingOffer -> conflictingList.add(conflictingOffer.getKey()));
					offer.getAttributes().setConflictingOffers(null);
				}
			});
		}
		if (!conflictingList.isEmpty()) {
			finalOfferList = finalOfferList.stream().filter(offer -> (!conflictingList.contains(offer.getCode()))).collect(Collectors.toList());
		}

		return finalOfferList;
	}

	private List<CTOffer> removeConflictingDeviceOffers(List<CTOffer> finalOfferList) {
		List<String> conflictingList = new ArrayList<String>();
		if (Objects.nonNull(finalOfferList) && !finalOfferList.isEmpty()) {
			finalOfferList.forEach(offer -> {
				if (Optional.ofNullable(offer.getAttributes().getConflictingOffers()).isPresent()
						&& !offer.getAttributes().getConflictingOffers().isEmpty()) {
					offer.getAttributes().getConflictingOffers()
							.forEach(conflictingOffer -> conflictingList.add(conflictingOffer.getKey()));
					offer.getAttributes().setConflictingOffers(null);
				}
			});
		}
		if (!conflictingList.isEmpty()) {
			finalOfferList = finalOfferList.stream().filter(offer -> (!conflictingList.contains(offer.getCode())))
					.collect(Collectors.toList());
		}
		return finalOfferList;
	}

	public List<CTOffer> filterTenantOffers(CTOfferResponse ctOfferResponse, int addonsCount,List<String> existingBasePkg, List<String> RequestBasePkg) {
		List<String> conflictingOffers= new ArrayList<String>();
		final List<CTOffer> finalOffers= new ArrayList<CTOffer>();
		ctOfferResponse.getOffers().forEach(offer -> {
			String bundleProductCode="";
			if( Objects.nonNull(offer.getAttributes().getAssociatedProducts())
					&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts())
					&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts())
					&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0))
					&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj())
					&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getCode())) {
				bundleProductCode=offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getCode();
			}

			//filter 20/10/no off for premium upgrade offers
			if(Objects.nonNull(offer.getAttributes().getOfferProductType())
					&& offer.getAttributes().getOfferProductType().equals(Constants.VIDEO_PLAN)
					&& isEligibleOffers(offer,RequestBasePkg)
					&& (Objects.isNull(offer.getAttributes().isBulkOffer()) || !offer.getAttributes().isBulkOffer())) {
				if(Objects.nonNull(offer.getAttributes().getMinAddonCount()) && Objects.nonNull(offer.getAttributes().getMaxAddonCount())
						&& addonsCount>=offer.getAttributes().getMinAddonCount()
						&& (Objects.isNull(offer.getAttributes().getMaxAddonCount()) || addonsCount<=offer.getAttributes().getMaxAddonCount())) {
					finalOffers.add(offer);
				}else if(Objects.isNull(offer.getAttributes().getMinAddonCount()) && Objects.isNull(offer.getAttributes().getMaxAddonCount())){
					finalOffers.add(offer);
				}

			}
			//filter bulk video-addon offers
			if(Objects.nonNull(offer.getAttributes().isBulkOffer()) && offer.getAttributes().isBulkOffer()){
				if(existingBasePkg.contains(bundleProductCode)) {
					setDisplayTypeForCDVR(offer);
					finalOffers.add(offer);
					if(Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants())
							&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0))
							&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes())
							&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().getAddonCount())
							&& offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().getAddonCount()>1 ) {
						offer.getAttributes().getConflictingOffers().forEach(conflictingOffer -> conflictingOffers.add(conflictingOffer.getKey()));
						offer.getAttributes().setConflictingOffers(null);
					}
				}if(isFreePromoNotSpecialOffer(offer)) {
					setDisplayTypeForCDVR(offer);
					finalOffers.add(offer);
				}else if(Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts()
						.get(0).getObj().getVariants().get(0).getAttributes().getPlanSubType())
						&& offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts()
						.get(0).getObj().getVariants().get(0).getAttributes().getPlanSubType().contains(Constants.CDVR)
						&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts()
						.get(0).getObj().getVariants().get(0).getAttributes().getDisplayType())
						&&offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts()
						.get(0).getObj().getVariants().get(0).getAttributes().getDisplayType().contains(Constants.INCLUDED)){
					finalOffers.add(offer);
				}
			}else if(!existingBasePkg.contains(bundleProductCode) && offer.getAttributes().getOfferProductType().equals(Constants.VIDEO_ADDON)) {
				finalOffers.add(offer);
			}
			//to add existing bulk offer------> if the account and given request have different baseplans then to add both video-plan offers
			if(RequestBasePkg!=null &&RequestBasePkg.contains(bundleProductCode) && !existingBasePkg.contains(bundleProductCode)) {
				setDisplayTypeForCDVR(offer);
				finalOffers.add(offer);
			}
		});

		List<CTOffer> finalOfferList = new ArrayList<CTOffer>();

		if (!conflictingOffers.isEmpty()) {
			finalOfferList=finalOffers.stream().filter(offer -> (!conflictingOffers.contains(offer.getCode()))).collect(Collectors.toList());
		}else {
			finalOfferList=finalOffers;
		}

		return finalOfferList;
    }
	private  boolean isLocalBulkOffer(CTOffer ctOffer){
		return offersUtils.isProductsHavingSubCategoryLocals(ctOffer) && (Objects.nonNull(ctOffer.getAttributes().isBulkOffer()) && ctOffer.getAttributes().isBulkOffer());
	}

	public void setDisplayTypeForCDVR(CTOffer ctOffer) {
		long startTimeInMillis = System.currentTimeMillis();
		try {
                if (!isLocalBulkOffer(ctOffer) && ctOffer.getAttributes() != null && ctOffer.getAttributes().getAssociatedProducts() != null) {
				ctOffer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull).forEach(associatedProduct -> {
					if (associatedProduct.getBundleProducts() != null) {
						associatedProduct.getBundleProducts().stream().filter(Objects::nonNull).forEach(bundleProduct -> {
							if (Objects.nonNull(bundleProduct.getProducts())) {
								bundleProduct.getProducts().stream().filter(Objects::nonNull).forEach(product -> {
									if(product.getObj()!=null && product.getObj().getVariants()!=null) {
										product.getObj().getVariants().stream().filter(Objects::nonNull).forEach(variant -> {
											if (Optional.ofNullable(variant.getAttributes().ismDUBridePackage())
													.isPresent() && !variant.getAttributes().ismDUBridePackage()) {
												variant.getAttributes().setDisplayType(Constants.INCLUDED);
											}
										});
									}
								});
							}

						});
					}
				});
			}

		} catch (Exception ex) {
			log.error("EPOCH setDisplayTypeForCDVR::" + ex);

		}
		long endTimeMillis = System.currentTimeMillis() - startTimeInMillis;
		log.info("EPOCH_CT_OFFERS_setDisplayTypeForCDVR-[{}]", (endTimeMillis));
	}

	private CTOfferResponse suppressOffers(CTOfferResponse ctOfferResponse, String suppressOffer) {
		List<CTOffer> processedCTOffers = new ArrayList<>();
		if (Objects.nonNull(ctOfferResponse) && Objects.nonNull(ctOfferResponse.getOffers()) && !ctOfferResponse.getOffers().isEmpty()) {
			if (suppressOffer == Constants.SUPPRESS_UMLIMITED_RACKRATE_OFFER) {
                if (featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_CONFLICTING_OFFER_NEW_LOGIC)) {
                    ctOfferResponse.getOffers().forEach(ctOffer -> {
                        if (isFreePromoNotSpecialOffer(ctOffer)) {
                            setDisplayTypeForCDVR(ctOffer);
                        }
                    });
                }else {
                    processedCTOffers = removeConflictingOffers(ctOfferResponse.getOffers());
                    ctOfferResponse.setOffers(processedCTOffers);
                    ctOfferResponse.setCount(processedCTOffers.size());
                    ctOfferResponse.setTotal(processedCTOffers.size());
                }
			} else if (suppressOffer == Constants.SUPPRESS_UMLIMITED_OFFER) {
				processedCTOffers = ctOfferResponse.getOffers().stream()
						.filter(offer -> (!isFreePromoNotSpecialOffer(offer))).collect(Collectors.toList());
				ctOfferResponse.setOffers(processedCTOffers);
				ctOfferResponse.setCount(processedCTOffers.size());
				ctOfferResponse.setTotal(processedCTOffers.size());
			}
		}
		return ctOfferResponse;
	}


	public CTOfferResponse filterInstallmentProvider(CTOfferResponse ctOfferResponse) {
		ctOfferResponse.getOffers().forEach(offer -> {
			String installmentBiller;
			if (Optional.ofNullable(offer.getAttributes().getInstallmentBiller()).isPresent()){
				installmentBiller=offer.getAttributes().getInstallmentBiller();
				if (Optional.ofNullable(offer.getAttributes().getAssociatedProducts()).isPresent() &&
						Optional.ofNullable(offer.getAttributes().getAssociatedProducts().get(0)).isPresent() &&
						Optional.ofNullable(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts()).isPresent() &&
						Optional.ofNullable(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0)).isPresent() &&
						Optional.ofNullable(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts()).isPresent()) {
					offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).
							getProducts().forEach(product -> {
								if (Optional.ofNullable(product.getObj().getVariants().get(0).getAttributes()).isPresent() &&
										Optional.ofNullable(product.getObj().getVariants().get(0).getAttributes().getInstallmentList()).isPresent()) {

									List<InstallmentInfo> installmentInfo = product.getObj().getVariants().get(0).getAttributes().getInstallmentList();
									if (Objects.nonNull(installmentInfo)) {
										installmentInfo = installmentInfo.stream().filter(installment ->
												(installment.getInstallmentProvider()!=null
														&& installment.getInstallmentProvider().equalsIgnoreCase(installmentBiller) )).collect(Collectors.toList());
										product.getObj().getVariants().get(0).getAttributes().setInstallmentList(installmentInfo);
									}
								}
							});
				}
				offer.getAttributes().setInstallmentBiller(null);
			}
		});

		return ctOfferResponse;
	}

	/**
	 * Generating CT request for protection plan and invoking CT
	 * @param offerRequestWrapper
	 * @return
	 */
	public CTOfferResponse getClosingOffers(OfferRequestWrapper offerRequestWrapper) {

		CTOfferResponse ctOfferResponse = null;
		boolean isReconnectFlag = false;
		String eligibleOfferFlavour = "";
		List<CTOffer> processedCTOffers = null;
		processedCTOffers = new ArrayList<>();
		EvergentContract propertyOwnerData=new EvergentContract();

		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		if (Objects.nonNull(offerRequestWrapper.getCtOfferRequest())) {
			BeanUtils.copyProperties(offerRequestWrapper.getCtOfferRequest(), ctOfferRequest);
		}
		//for OTT Offers Bundle Prooduct is not configure in CT so setting cartContext to null
		if(CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getOfferProductFamily()) && offerRequestWrapper.getOfferRequest().getOfferProductFamily().contains(Constants.OTT_PRODUCT_FAMILY)){
			ctOfferRequest.setCartContext(null);
		}
		ctOfferRequest.setOfferActionType(offerRequestWrapper.getOfferRequest().getOfferActionType());
		log.info("API_NAME:EPOCH_GETOFFERS OFFERACTIONTYPE:{}",OffersUtils.sanitizeData(ctOfferRequest.getOfferActionType()));
		ctOfferRequest.setOfferProductType(Stream.of(Constants.CREDIT).collect(Collectors.toList()));
		ctOfferRequest.setSalesChannel(offerRequestWrapper.getOfferRequest().getSalesChannel());
		ctOfferRequest.setCustomerSegments(offerRequestWrapper.isMobility() ? Stream.of("Mobility").collect(Collectors.toList()) :
				Stream.of("Residential").collect(Collectors.toList()));

		if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerSegments())
				&& (offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.DEMO)
				|| offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.DECA)
				|| offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.SHOWROOM)
				|| offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.COURTESY)
				|| offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.BCOMP)
				|| offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.MDUTENANT))){

			ctOfferRequest.setCustomerSegments(offerRequestWrapper.getOfferRequest().getCustomerSegments());
		}

		if(!offersUtils.containsTAZCONTRACT(offerRequestWrapper.getOfferRequest()) &&
				!offersUtils.containsROADRUNNER(offerRequestWrapper.getOfferRequest()) &&
				Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext()) &&
				Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()) &&
				Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getPriceProtection())) {
			PriceProtection priceProtection = offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getPriceProtection();
			priceProtection.setNextBillingDate(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getNextBillingDate());
			if(offerRequestWrapper.getCurrentDate()!=null){
				priceProtection.setCurrentDate(offerRequestWrapper.getCurrentDate());
			}else{
				priceProtection.setCurrentDate(new SimpleDateFormat("MM/dd/yyyy").format(new Date()));
			}

			ctOfferRequest.setPriceProtection(priceProtection);
		} else if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext()) &&
				Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()) &&
				offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getNextBillingDate()!=null) {
			ctOfferRequest.setNextBillingDate(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getNextBillingDate());
		}

		if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerSegments())
				&& offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.EMPLOYEE))
		{
			if (isReconnectFlow(offerRequestWrapper)) {
				ctOfferRequest.setCustomerSegments(Stream.of(Constants.RESIDENTIAL).collect(Collectors.toList()));
			} else {
				ctOfferRequest.setCustomerSegments(Stream.of(Constants.EMPLOYEE).collect(Collectors.toList()));
			}
		}/* else
		if (!offersUtils.checkIsOemChannel(offerRequestWrapper.getOfferRequest()) && isReconnectFlow(offerRequestWrapper) &&
				(isValidSubscribeType(offerRequestWrapper) || offersUtils.containsTAZCONTRACT(offerRequestWrapper.getOfferRequest()) || offersUtils.containsROADRUNNER(offerRequestWrapper.getOfferRequest())) &&
				Optional.ofNullable(offerRequestWrapper.getOfferRequest().getServiceEndDate()).isPresent() &&
				isEligibleToReconnect(offerRequestWrapper,ctOfferRequest)) {

			// customer type reconnect
			isReconnectFlag = true;
			ctOfferRequest.setCustomerType(Stream.of("reconnect").collect(Collectors.toList()));
		}*/

		ctOfferRequest.setBusinessSegment(Objects.nonNull(offerRequestWrapper.getOfferRequest().getBusinessSegment())?
				offerRequestWrapper.getOfferRequest().getBusinessSegment() : Arrays.asList(Constants.CONS));

		if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext())
				&&	Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt())
				&&	Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getPropertyOwnerActNumber()))
		{
			propertyOwnerData =accountData.fetchTenantData(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getPropertyOwnerActNumber()).get(0);

		}

		log.info("offer request {}",ctOfferRequest);


		log.info("API_NAME:EPOCH_GETOFFERS CUSTOMERSEGMENT:{}",OffersUtils.sanitizeData(ctOfferRequest.getCustomerSegments()));
		ctOfferRequest.setPagination(offerRequestWrapper.getOfferRequest().getPagination());
		ctOfferRequest.setContractIndicator(offerRequestWrapper.getOfferRequest().getContractIndicator());
		ctOfferRequest.setBenefitsCodesToSuppressOffer(offerRequestWrapper.getOfferRequest().getBenefitsCodesToSuppressOffer());
		if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getBenefitsCustomerHasReceived())) {
			List<BenefitsCustomerHasReceived> benefitsCustomerHasReceived = new ArrayList<>();
			benefitsCustomerHasReceived = offerRequestWrapper.getOfferRequest().getBenefitsCustomerHasReceived();
			List<BenefitCodesToSuppressTheOffer> benefitCodesToSuppressTheOffer = convertToBenefitCodesToSuppressTheOffer(benefitsCustomerHasReceived);
			ctOfferRequest.setBenefitCodesToSuppressTheOffer(benefitCodesToSuppressTheOffer);
		}
		if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext()) &&
				Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()) &&
				Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getIapPartnerAccountType()) &&
				offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getIapPartnerAccountType() != null) {
			ctOfferRequest.setIapPartnerAccountType(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getIapPartnerAccountType());
		}
		ctOfferRequest.setExpiredOffers(offerRequestWrapper.getOfferRequest().isExpiredOffers());
 		if (!offersUtils.isService(offerRequestWrapper.getOfferRequest())) {
			if (offerRequestWrapper.getOfferRequest().isMigrationIndicator() || featureManagerHelper.isEnabled(CPOP_MIGRATION_OFFER_ENABLED)) {
				log.info("EPOCH_GETOFFERS_ACQUISITIONFLOW_MIGRATION_OFFERSREQUEST ");
				salesATTTVMigrationProcessor.populateMigrationRequest(offerRequestWrapper, ctOfferRequest);
				ctOfferRequest.setMigrationIndicator(true);
			}else {
				if(Objects.nonNull(ctOfferRequest.getCustomerContext())) {
					// Non-IPTV Migration flow, Excluding Customer Context entries for IPTV, DTVS & BB
					List<CTCustomerContext> customerContext = excludeUverseCustomerContext(ctOfferRequest);
					ctOfferRequest.setCustomerContext(customerContext);
				}
			}

			//before ct
			ctOfferResponse = getOffersFromCT(ctOfferRequest,offerRequestWrapper);
			//after ct
			if ((offersUtils.containsEDSP(offerRequestWrapper.getOfferRequest())
					|| offersUtils.containsTAZ(offerRequestWrapper.getOfferRequest())
					|| offersUtils.containsTAZBYOD(offerRequestWrapper.getOfferRequest())
					|| offersUtils.containsROADRUNNER(offerRequestWrapper.getOfferRequest())
					|| offersUtils.containsTAZCONTRACT(offerRequestWrapper.getOfferRequest())) && !isReconnectFlag 	&& (null != ctOfferResponse)) {
				processedCTOffers = filterReconnectOffer(ctOfferResponse.getOffers());
				ctOfferResponse.setOffers(processedCTOffers);
				ctOfferResponse.setCount(processedCTOffers.size());
				ctOfferResponse.setTotal(processedCTOffers.size());
			}
			if (offerRequestWrapper.getOfferRequest().isMigrationIndicator() || featureManagerHelper.isEnabled(CPOP_MIGRATION_OFFER_ENABLED)) {
				ctOfferResponse = salesATTTVMigrationProcessor.filterMigrationEligibleOffers(offerRequestWrapper,ctOfferRequest, ctOfferResponse);
			}
		} else {
			ctOfferResponse = getOffersFromCT(ctOfferRequest,offerRequestWrapper);
		}

		if (Optional.ofNullable(offerRequestWrapper.getOfferRequest()).isPresent()
				&& !offersUtils.isService(offerRequestWrapper.getOfferRequest())
				&& (offersUtils.containsEDSP(offerRequestWrapper.getOfferRequest())
				|| offersUtils.containsTAZ(offerRequestWrapper.getOfferRequest()))) {
			if (offersUtils.checkIsOemChannel(offerRequestWrapper.getOfferRequest())) {
				if (isReconnectFlow(offerRequestWrapper)) {
					//Checking which Flavor of Offer is to be returned for OEM
					eligibleOfferFlavour = getEligibleOfferFlavour(offerRequestWrapper,ctOfferRequest);
					ctOfferResponse = getOemOffer(ctOfferResponse, eligibleOfferFlavour);
				} else {
					ctOfferResponse = getOemOffer(ctOfferResponse, "FT");
				}
			} else if(offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.OPUS)
					&& !offerRequestWrapper.isChannelEligiblity()) {
				ctOfferResponse = offersUtils.filterInvalidOffer(ctOfferResponse);
			}
		}
		if(Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerSegments()).isPresent()
				&& (offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.DEMO)
				|| offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.DECA)
				|| offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.SHOWROOM)
				|| offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.BCOMP)
				|| offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.COURTESY))
				&& Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferActionType()).isPresent()
				&& offerRequestWrapper.getOfferRequest().getOfferActionType().contains(Constants.ACQUISITION)) {
			ctOfferResponse.getOffers().forEach(offer -> {
				setDisplayTypeForCDVR(offer);
			});
		}

		if(Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerSegments()).isPresent()
				&& offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.MDUTENANT)
				&& Optional.ofNullable(propertyOwnerData).isPresent()) {

			List<String> existingBasePkg= propertyOwnerData.getBulked_pkg().isEmpty() ?
					new ArrayList<String>() :Arrays.asList(propertyOwnerData.getBulked_pkg().split(";"));

			List<String> existingAddons= propertyOwnerData.getProg_addons().isEmpty() ?
					new ArrayList<String>() : Arrays.asList(propertyOwnerData.getProg_addons().split(";"));

			CTProductRequest ctProductRequest = new CTProductRequest();
			ctProductRequest.setProductCodes(existingAddons);
			ctProductRequest.setBusinessSegment(Arrays.asList(Constants.MDU));
			CTProductResponse productResponse=cpopClientHelper.getProducts(ctProductRequest);

			AtomicInteger addonsCount= new AtomicInteger();
			addonsCount.set(propertyOwnerData.getProg_addons().isEmpty() ?
					0 : Arrays.asList(propertyOwnerData.getProg_addons().split(";")).size());

			productResponse.getProducts().forEach(product ->{
				if(Objects.nonNull(product.getCode()) && existingAddons.contains(product.getCode())){
					if(Objects.nonNull(product.getVariants()) && Objects.nonNull(product.getVariants().get(0))
							&& Objects.nonNull(product.getVariants().get(0).getAttributes()) && Objects.nonNull(product.getVariants().get(0).getAttributes().getAddonCount())
							&& product.getVariants().get(0).getAttributes().getAddonCount()!=0) {

						addonsCount.set(addonsCount.get()+product.getVariants().get(0).getAttributes().getAddonCount()-1);
					}
				}
			});

			List<String> basePackageCompatibleProds = new ArrayList<String>();
			if (offersUtils.isService(offerRequestWrapper.getOfferRequest())
					&&Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext()).isPresent()
					&&Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()).isPresent()
					&&Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getProducts()).isPresent()) {
				offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getProducts().forEach(prod -> {
					if (prod.getProductType().equalsIgnoreCase(Constants.VIDEO_PLAN)) {
						basePackageCompatibleProds.add(prod.getProductCode());
					}
				});
			}else {
				basePackageCompatibleProds.addAll(existingBasePkg);
			}

			ctOfferResponse.setOffers(filterTenantOffers(ctOfferResponse,addonsCount.get(),existingBasePkg,basePackageCompatibleProds));

		}

		return ctOfferResponse;
	}

	public CTOfferResponse getProtectionPlanOffers(OfferRequestWrapper offerRequestWrapper) {
		CTOfferResponse ctOfferResponse = null;

		List<CTOffer> processedCTOffers = null;
		processedCTOffers = new ArrayList<>();

		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		if (Objects.nonNull(offerRequestWrapper.getCtOfferRequest())) {
			BeanUtils.copyProperties(offerRequestWrapper.getCtOfferRequest(), ctOfferRequest);
		}
		ctOfferRequest.setSalesChannel(offerRequestWrapper.getOfferRequest().getSalesChannel());
		ctOfferRequest.setOfferActionType(offerRequestWrapper.getOfferRequest().getOfferActionType());
		log.info("API_NAME:EPOCH_GETOFFERS OFFERACTIONTYPE:{}",
				OffersUtils.sanitizeData(ctOfferRequest.getOfferActionType()));
		ctOfferRequest.setOfferProductType(Stream.of(Constants.PROTECTION_PLAN).collect(Collectors.toList()));
		ctOfferRequest.setPagination(offerRequestWrapper.getOfferRequest().getPagination());
		ctOfferRequest.setContractIndicator(offerRequestWrapper.getOfferRequest().getContractIndicator());

		//set price protection and NBD for protection plan offers
		setPriceProtectionNBDCTOffReq(offerRequestWrapper, ctOfferRequest);

		ctOfferRequest.setBenefitsCodesToSuppressOffer(offerRequestWrapper.getOfferRequest().getBenefitsCodesToSuppressOffer());
		if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext())
				&& (Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt())) && (Objects
				.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getProducts()))) {
			List<String> productsOnAccountToSuppress = new ArrayList<>();
			offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getProducts().stream()
					.filter(Objects::nonNull).forEach(prod -> {
						if (prod != null && prod.getProductCode() != null) {
							productsOnAccountToSuppress.add(prod.getProductCode());
						}
					});
			ctOfferRequest.setProductsOnAccountToSuppressOffer(productsOnAccountToSuppress);
		}
		if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getBenefitsCustomerHasReceived())) {
			List<BenefitsCustomerHasReceived> benefitsCustomerHasReceived = new ArrayList<>();
			benefitsCustomerHasReceived = offerRequestWrapper.getOfferRequest().getBenefitsCustomerHasReceived();
			List<BenefitCodesToSuppressTheOffer> benefitCodesToSuppressTheOffer = convertToBenefitCodesToSuppressTheOffer(
					benefitsCustomerHasReceived);
			ctOfferRequest.setBenefitCodesToSuppressTheOffer(benefitCodesToSuppressTheOffer);

		}
		if (offerRequestWrapper.getOfferRequest().isMigrationIndicator()) {
			ctOfferRequest.setMigrationIndicator(offerRequestWrapper.getOfferRequest().isMigrationIndicator());
		}

		ctOfferResponse = getOffersFromCT(ctOfferRequest,offerRequestWrapper);

		if (ctOfferResponse != null && ctOfferRequest!=null && ctOfferRequest.getProductsOnAccountToSuppressOffer()!=null ) {
			ctOfferResponse = cpopClientHelper.filterProductsOnAccountToSuppressOffer(ctOfferResponse, ctOfferRequest);
		}

		if (offersUtils.isService(offerRequestWrapper.getOfferRequest())
				&& Objects.nonNull(OffersUtils.getIapPartnerAccountType(offerRequestWrapper.getOfferRequest()))) {
			processedCTOffers = checkExcludedParter(ctOfferResponse.getOffers(),
					OffersUtils.getIapPartnerAccountType(offerRequestWrapper.getOfferRequest()));
			ctOfferResponse.setOffers(processedCTOffers);
			ctOfferResponse.setCount(processedCTOffers.size());
			ctOfferResponse.setTotal(processedCTOffers.size());
		}

		if (offersUtils.isService(offerRequestWrapper.getOfferRequest()) && Objects.nonNull(ctOfferResponse.getOffers())
				&& !ctOfferResponse.getOffers().isEmpty()) {
			ctOfferResponse.getOffers().forEach(offer -> {
				if (Optional.ofNullable(offer.getAttributes().getExcludedPartners()).isPresent()) {
					offer.getAttributes().setExcludedPartners(null);
				}
			});
		}
		return ctOfferResponse;

	}


	/**
	 * This method is used to get reward offers. It first creates a CTOfferRequest object and sets its properties
	 * using the provided OfferRequestWrapper object. It then calls the getOffersFromCT method to get a CTOfferResponse.
	 * If the sales channel of the offer request contains DIRECTV_ONLINE, it removes conflicting reward offers from the
	 * CTOfferResponse. Finally, it returns the CTOfferResponse.
	 *
	 * @param offerRequestWrapper The OfferRequestWrapper object containing the offer request details.
	 * @return CTOfferResponse The CTOfferResponse object containing the reward offers.
	 */
	public CTOfferResponse getRewardOffers(OfferRequestWrapper offerRequestWrapper) {
		// Initialize CTOfferResponse and processedCTOffers
		CTOfferResponse ctOfferResponse = null;
		// Create a new CTOfferRequest and set its properties using the provided OfferRequestWrapper
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		if (Objects.nonNull(offerRequestWrapper.getCtOfferRequest())) {
			BeanUtils.copyProperties(offerRequestWrapper.getCtOfferRequest(), ctOfferRequest);
		}
		ctOfferRequest.setSalesChannel(offerRequestWrapper.getOfferRequest().getSalesChannel());
		ctOfferRequest.setOfferActionType(offerRequestWrapper.getOfferRequest().getOfferActionType());
		ctOfferRequest.setMarketingSourceCode(offerRequestWrapper.getOfferRequest().getMarketingSourceCode());
		log.info("API_NAME:EPOCH_GETOFFERS OFFERACTIONTYPE:{}",
				OffersUtils.sanitizeData(ctOfferRequest.getOfferActionType()));
		ctOfferRequest.setOfferProductType(Stream.of(Constants.REWARD).collect(Collectors.toList()));
		ctOfferRequest.setPagination(offerRequestWrapper.getOfferRequest().getPagination());
		ctOfferRequest.setContractIndicator(offerRequestWrapper.getOfferRequest().getContractIndicator());
		ctOfferRequest.setBenefitsCodesToSuppressOffer(
				offerRequestWrapper.getOfferRequest().getBenefitsCodesToSuppressOffer());
		if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerEligibility())) {
			CustomerEligibility custEligibility = offerRequestWrapper.getOfferRequest().getCustomerEligibility();
			List<String> zipCode = custEligibility.getZipCode();
			List<String> fipsCode = custEligibility.getFipsCode();

			if (CollectionUtils.isNotEmpty(zipCode) && CollectionUtils.isNotEmpty(fipsCode)) {
				List<String> dmaValueList = dmaLookUpService.getDMAValue(zipCode.get(0), fipsCode.get(0));
				if (!dmaValueList.isEmpty()) {
					custEligibility.setDma(dmaValueList);
				}
				ctOfferRequest.setCustomerEligibility(custEligibility);
			} else if (CollectionUtils.isNotEmpty(zipCode)) {
				custEligibility.setZipCode(zipCode);
				ctOfferRequest.setCustomerEligibility(custEligibility);
			}
		}
		if (offerRequestWrapper != null && offerRequestWrapper.getOfferRequest() != null
				&& offerRequestWrapper.getOfferRequest().isMigrationIndicator()) {
			log.info("EPOCH_GET_OFFERS_ACQUISITION_FLOW_MIGRATION_OFFERS_REQUEST_REWARD");
			ctOfferRequest.setMigrationIndicator(offerRequestWrapper.getOfferRequest().isMigrationIndicator());
		}
		// Get the CTOfferResponse by calling the getOffersFromCT method
		ctOfferResponse = getOffersFromCT(ctOfferRequest, offerRequestWrapper);

		// Check if the cart context and CPOP offer codes in the offer request are not null
		if (offerRequestWrapper.getOfferRequest().getCartContext() != null
				&& offerRequestWrapper.getOfferRequest().getCartContext().getCpopOfferCodes() != null) {
			List<CTOffer> offers = ctOfferResponse.getOffers();
			if (!offers.isEmpty()) {
				offers = ottSalesOffersProcessorHelper.filterInEligibleOffers(offers, offerRequestWrapper);
				ctOfferResponse.setOffers(offers);
				ctOfferResponse.setCount(offers.size());
				ctOfferResponse.setTotal(offers.size());
			}
		}
		return ctOfferResponse;
	}

	public  CTOfferResponse filterChoiceGroupBasedOnSalesChannel(CTOfferResponse ctOfferResponse,
																 OfferRequestWrapper offerRequestWrapper) {
		log.info("OttCtOffersProcesser class filterChoiceGroupBasedOnSalesChannel method execution started");
		if (ctOfferResponse != null) {
			ctOfferResponse.getOffers().forEach(ctOffer -> {
				if (Objects.nonNull(ctOffer.getAttributes())
						&& Objects.nonNull(ctOffer.getAttributes().getOfferChoiceGroup())) {
					List<OfferChoiceGroup> offerChoiceGroup = ctOffer.getAttributes().getOfferChoiceGroup();
					List<OfferChoiceGroup> offerChoiceGroupResult = new ArrayList<>();
					offerChoiceGroup.stream().filter(Objects::nonNull).forEach(choiceGroup -> {
						// Checking offer choice group have sales channel or not
						if (CollectionUtils.isNotEmpty(choiceGroup.getChoiceGroupSalesChannel()) && choiceGroup.getChoiceGroupSalesChannel()
								.contains(offerRequestWrapper.getOfferRequest().getSalesChannel().get(0))) {
							offerChoiceGroupResult.add(choiceGroup);
						}
						if (choiceGroup.getDisclosureMessagesByKey() != null
								&& choiceGroup.getDisclosureMessagesByKey().isEmpty()){
							choiceGroup.setDisclosureMessagesByKey(null);
						}
					});
					// if offer having choice group sales channel and IDP requests sales channel is
					// then return the choice group other
					// wise choice group is empty
					if (!offerChoiceGroupResult.isEmpty()) {
						ctOffer.getAttributes().setOfferChoiceGroup(offerChoiceGroupResult);
					} else {
						ctOffer.getAttributes().setOfferChoiceGroup(null);
					}

				}
			});
		}
		log.info("OttCtOffersProcesser class filterChoiceGroupBasedOnSalesChannel method execution end");
		return ctOfferResponse;
	}

	public  CTOfferResponse replaceProductMinMaxValueWithOfferValue(CTOfferResponse ctOfferResponse,
																	OfferRequestWrapper offerRequestWrapper) {
		log.info("OttCtOffersProcesser class replaceProductMinMaxValueWithOfferValue method execution started");
		if (ctOfferResponse != null) {
			ctOfferResponse.getOffers().forEach(ctOffer -> {
				if (Objects.nonNull(ctOffer.getAttributes().getOfferMinMaxQuantity())
						&& Objects.nonNull(ctOffer.getAttributes().getOfferMinMaxQuantity().get(0).getMaxQuantity())
						&& Objects.nonNull(ctOffer.getAttributes().getOfferMinMaxQuantity().get(0).getMinQuantity())
						&& Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts())
						&& Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts())
						&& Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts())
						&& Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0))
						&& Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj())
						&& Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants())
						&& Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0))
						&& Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes())
						&& Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().getMinMaxQuantity())) {
					List<MinMaxQuantity> minmax=ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().getMinMaxQuantity();

					minmax.forEach(obj -> {
						if (StringUtils.isNotEmpty(obj.getMinQuantity()) && StringUtils.isNotEmpty(obj.getMaxQuantity())
								&& CollectionUtils.isNotEmpty(ctOffer.getAttributes().getOfferMinMaxQuantity())
								&& StringUtils.isNotEmpty(ctOffer.getAttributes().getOfferMinMaxQuantity().get(0).getMaxQuantity())
								&& StringUtils.isNotEmpty(ctOffer.getAttributes().getOfferMinMaxQuantity().get(0).getMinQuantity())) {
							obj.setMinQuantity(ctOffer.getAttributes().getOfferMinMaxQuantity().get(0).getMinQuantity());
							obj.setMaxQuantity(ctOffer.getAttributes().getOfferMinMaxQuantity().get(0).getMaxQuantity());
							Optional.ofNullable(obj.getMinQuantity())
									.ifPresent(minQuantity -> {
										if (ctOffer.getAttributes().getAssociatedProducts().get(0)
												.getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0)
												.getAttributes().getMinQuantity() != null) {
											// Set the minQuantity attribute to the value from MinMaxQuantity.getMinQuantity()
											ctOffer.getAttributes().getAssociatedProducts().get(0)
													.getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0)
													.getAttributes().setMinQuantity(minQuantity);
										}
									});

							Optional.ofNullable(obj.getMaxQuantity())
									.ifPresent(maxQuantity -> {
										if (ctOffer.getAttributes().getAssociatedProducts().get(0)
												.getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0)
												.getAttributes().getMaxQuantity() != null) {
											// Set the maxQuantity attribute to the value from MinMaxQuantity.getMaxQuantity()
											ctOffer.getAttributes().getAssociatedProducts().get(0)
													.getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0)
													.getAttributes().setMaxQuantity(maxQuantity);
										}
									});
						}

					});
				}

			});
		}
		log.info("OttCtOffersProcesser class replaceProductMinMaxValueWithOfferValue method execution end");
		return ctOfferResponse;
	}

	/**
	 * This method filters the InEligibleStoreIds that matches the ChannelEligiblity Opus StoreId from the request
	 * @param ctOfferResponse
	 * @param offerRequestWrapper
	 * @return
	 */
	public void filterInEligibleStoreIds(CTOfferResponse ctOfferResponse,
										 OfferRequestWrapper offerRequestWrapper) {
		log.debug("OttCTOffersProcessor :: filterInEligibleStoreIds method execution start");

		if (null != ctOfferResponse && null != offerRequestWrapper.getOfferRequest().getChannelEligibility()
				&& null != offerRequestWrapper.getOfferRequest().getChannelEligibility().getOpusStoreId()) {
			ctOfferResponse.getOffers().removeIf(ctOffer -> inEligibleStoreIdCheck(
					offerRequestWrapper.getOfferRequest().getChannelEligibility().getOpusStoreId(), ctOffer));
			ctOfferResponse.setCount(ctOfferResponse.getOffers().size());
			ctOfferResponse.setTotal(ctOfferResponse.getOffers().size());
		}
		log.debug("OttCTOffersProcessor :: filterInEligibleStoreIds method execution end");
	}

	private boolean inEligibleStoreIdCheck(String opusStoreId, CTOffer ctOffer) {
		if (Objects.nonNull(ctOffer.getAttributes())
				&& Objects.nonNull(ctOffer.getAttributes().getIneligibleStoreIds())) {
			return ctOffer.getAttributes().getIneligibleStoreIds().contains(opusStoreId.toUpperCase());
		}
		return false;
	}
	/**
	private CTOfferResponse getDeviceOffersBasedOnQualifyingProducts(CTOfferResponse ctOfferResponse,
																	 CTOfferRequest ctOfferRequest, OfferRequestWrapper offerRequestWrapper) {
		log.debug("getDeviceOffersBasedOnQualifyingProducts method execution start");
		if (ctOfferResponse != null && ctOfferRequest != null) {
			List<String> qualifyingProductIds = new ArrayList<>();
			List<CTOffer> filterOffers = new ArrayList<>();
			List<String> custProducts = new ArrayList<>();
			ctOfferResponse.getOffers().forEach(ctOffer -> {
				if (Objects.nonNull(ctOffer.getAttributes())
						&& Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts())
						&& Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0))
						&& Objects
						.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getQualifyingProducts())) {
					List<ProductWrapper> qualifyingProducts = ctOffer.getAttributes().getAssociatedProducts().get(0)
							.getQualifyingProducts();
					if (Objects.nonNull(qualifyingProducts) && !qualifyingProducts.isEmpty()) {
						qualifyingProducts.stream().filter(Objects::nonNull).forEach(qualifyingProduct -> {
							List<Product> products = qualifyingProduct.getProducts();
							if (Objects.nonNull(products) && !products.isEmpty()) {
								products.stream().filter(Objects::nonNull).forEach(product -> {
									qualifyingProductIds.add(product.getKey());
									if (!qualifyingProductIds.isEmpty()) {
										offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getProducts()
												.forEach(prod -> {
													custProducts.add(prod.getProductCode());
												});
										if (!custProducts.isEmpty() && !qualifyingProductIds.isEmpty()) {
											custProducts.stream().filter(Objects::nonNull).forEach(custProduct -> {
												// filter the offer based on qualifying product and send it back
												if (qualifyingProductIds.contains(custProduct)) {
													filterOffers.add(ctOffer);
												}
											});
										}
									}
								});
							}
						});
					}
				}
			});
			if (!filterOffers.isEmpty()) {
				ctOfferResponse.setOffers(filterOffers);
				ctOfferResponse.setCount(filterOffers.size());
				ctOfferResponse.setTotal(filterOffers.size());
			}
		}
		log.debug("getDeviceOffersBasedOnQualifyingProducts method execution end");
		return ctOfferResponse;
	}**/

	private CTOfferResponse getDeviceOffersBasedOnQualifyingProductsNew(CTOfferResponse ctOfferResponse,
																		CTOfferRequest ctOfferRequest, OfferRequestWrapper offerRequestWrapper) {
		log.debug("getDeviceOffersBasedOnQualifyingProducts New method execution start");
		if (ctOfferResponse != null && ctOfferRequest != null &&
				offerRequestWrapper.getOfferRequest().getCustomerContext()!=null &&
				offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()!=null &&
				offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getProducts()!=null) {
			List<CTOffer> filterOffers = new ArrayList<>();
			List<String> custProducts = new ArrayList<>();
			offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getProducts()
					.forEach(prod -> {
						custProducts.add(prod.getProductCode());
					});
			ctOfferResponse.getOffers().forEach(ctOffer -> {
				List<String> qualifyingProductIds = new ArrayList<>();
				if (Objects.nonNull(ctOffer.getAttributes())
						&& Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts())
						&& Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0))
						&& Objects
						.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getQualifyingProducts())) {
					List<ProductWrapper> qualifyingProducts = ctOffer.getAttributes().getAssociatedProducts().get(0)
							.getQualifyingProducts();
					if(Objects.nonNull(qualifyingProducts) && !qualifyingProducts.isEmpty()) {
						qualifyingProducts.stream().filter(Objects::nonNull).forEach(qualifyingProduct -> {
							List<Product> products = qualifyingProduct.getProducts();
							if(Objects.nonNull(products) && !products.isEmpty()) {
								products.stream().filter(Objects::nonNull).forEach(product -> {
									qualifyingProductIds.add(product.getKey());
								});
							}
						});
					}
					if (CollectionUtils.isNotEmpty(qualifyingProductIds) && CollectionUtils.isNotEmpty(custProducts)) {
						custProducts.stream().filter(Objects::nonNull).forEach(products -> {
							// filter the offer based on qualifying product and send it back
							if (qualifyingProductIds.contains(products)) {
								filterOffers.add(ctOffer);
							}
						});
					}
				}
			});
			if (!filterOffers.isEmpty()) {
				ctOfferResponse.setOffers(filterOffers);
				ctOfferResponse.setCount(filterOffers.size());
				ctOfferResponse.setTotal(filterOffers.size());
			}
		}
		log.debug("getDeviceOffersBasedOnQualifyingProducts New method execution end");
		return ctOfferResponse;
	}

	private CTOfferResponse filteredOffersBasedOnSalesChannels(OfferRequestWrapper offerRequestWrapper, CTOfferResponse ctOfferResponse) {
		List<CTOffer> filteredOffersList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(ctOfferResponse.getOffers())) {
			ctOfferResponse.getOffers().forEach(offer -> {
				if (offer != null
						&& offer.getAttributes() != null
						&& offer.getAttributes().getEligibility() != null
						&& CollectionUtils.isNotEmpty(offer.getAttributes().getEligibility().getConstraints())
						&& offer.getAttributes().getEligibility().getConstraints().get(0).getSalesChannel() != null
						&& CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getSalesChannel())
						&& offerRequestWrapper.getOfferRequest().getSalesChannel().get(0) != null) {
					boolean isSalesChannelMatch = offer.getAttributes().getEligibility().getConstraints().get(0).getSalesChannel().stream()
							.anyMatch(salesChannel -> salesChannel.equalsIgnoreCase(offerRequestWrapper.getOfferRequest().getSalesChannel().get(0)));
					if (isSalesChannelMatch) {
						filteredOffersList.add(offer);
					}
				}
			});
			ctOfferResponse.setOffers(filteredOffersList);
		}
		return ctOfferResponse;
	}

	private CTOfferResponse removeConflictingOffersInOfferCodeCall(CTOfferResponse ctOfferResponse) {
		List<CTOffer> finalOfferList = ctOfferResponse.getOffers();
		List<String> conflictingList = new ArrayList<>();
		if (Objects.nonNull(finalOfferList) && !finalOfferList.isEmpty()) {
			finalOfferList.forEach(offer -> {
				if (Optional.ofNullable(offer.getAttributes().getConflictingOffers()).isPresent()
						&& !offer.getAttributes().getConflictingOffers().isEmpty()) {
					offer.getAttributes().getConflictingOffers()
							.forEach(conflictingOffer -> conflictingList.add(conflictingOffer.getKey()));
					offer.getAttributes().setConflictingOffers(null);
				}
			});
		}
		if (!conflictingList.isEmpty()) {
			finalOfferList = finalOfferList.stream().filter(offer -> (!conflictingList.contains(offer.getCode())))
					.collect(Collectors.toList());
		}
		ctOfferResponse.setOffers(finalOfferList);
		return ctOfferResponse;
	}

	public CTOfferResponse getCreditOffers(OfferRequestWrapper offerRequestWrapper) {
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		if (offerRequestWrapper.getCtOfferRequest() != null) {
			BeanUtils.copyProperties(offerRequestWrapper.getCtOfferRequest(), ctOfferRequest);
		}

		OfferRequest offerRequest = offerRequestWrapper.getOfferRequest();
		ctOfferRequest.setSalesChannel(offerRequest.getSalesChannel());
		ctOfferRequest.setOfferActionType(offerRequest.getOfferActionType());
		ctOfferRequest.setOfferProductType(Collections.singletonList(Constants.CREDIT));
		ctOfferRequest.setPagination(offerRequest.getPagination());
		ctOfferRequest.setContractIndicator(offerRequest.getContractIndicator());
		ctOfferRequest.setBenefitsCodesToSuppressOffer(offerRequest.getBenefitsCodesToSuppressOffer());

		if (offerRequest.getCustomerContext() != null && offerRequest.getCustomerContext().getOtt() != null) {
			List<String> productsToSuppress = offerRequest.getCustomerContext().getOtt().getProducts().stream()
					.filter(Objects::nonNull)
					.map(ProductInfo::getProductCode)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			ctOfferRequest.setProductsOnAccountToSuppressOffer(productsToSuppress);
		}

		if (offerRequest.getBenefitsCustomerHasReceived() != null) {
			List<BenefitCodesToSuppressTheOffer> benefitCodes = convertToBenefitCodesToSuppressTheOffer(
					offerRequest.getBenefitsCustomerHasReceived());
			ctOfferRequest.setBenefitCodesToSuppressTheOffer(benefitCodes);
		}

		if (offerRequest.isMigrationIndicator()) {
			ctOfferRequest.setMigrationIndicator(true);
		}

		CTOfferResponse ctOfferResponse = getOffersFromCT(ctOfferRequest, offerRequestWrapper);

		if (ctOfferResponse != null && ctOfferRequest.getProductsOnAccountToSuppressOffer() != null) {
			ctOfferResponse = cpopClientHelper.filterProductsOnAccountToSuppressOffer(ctOfferResponse, ctOfferRequest);
		}

		if (offersUtils.isService(offerRequest)) {
			String partnerAccountType = OffersUtils.getIapPartnerAccountType(offerRequest);
			if (partnerAccountType != null) {
				List<CTOffer> filteredOffers = checkExcludedParter(ctOfferResponse.getOffers(), partnerAccountType);
				ctOfferResponse.setOffers(filteredOffers);
				ctOfferResponse.setCount(filteredOffers.size());
				ctOfferResponse.setTotal(filteredOffers.size());
			}

			if (ctOfferResponse.getOffers() != null) {
				ctOfferResponse.getOffers().forEach(offer -> {
					if (offer.getAttributes().getExcludedPartners() != null) {
						offer.getAttributes().setExcludedPartners(null);
					}
				});
			}
		}

		return ctOfferResponse;
	}

}
