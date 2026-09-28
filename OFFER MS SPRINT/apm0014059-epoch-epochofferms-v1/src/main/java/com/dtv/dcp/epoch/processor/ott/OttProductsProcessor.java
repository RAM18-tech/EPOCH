package com.dtv.dcp.epoch.processor.ott;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import com.dtv.dcp.epoch.model.common.request.CartProduct;
import com.dtv.dcp.epoch.model.ct.product.IncludeProductWrapper;
import com.dtv.dcp.epoch.model.ct.product.IncludedProduct;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.model.common.request.DeviceInfo;
import com.dtv.dcp.epoch.model.common.request.ProductRequest;
import com.dtv.dcp.epoch.model.common.request.ProductRequestWrapper;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.model.ct.product.Variant;
import com.dtv.dcp.epoch.model.ct.request.CTDeviceDetailsRequest;
import com.dtv.dcp.epoch.model.ct.request.CTProductNextBillingDate;
import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.dtv.dcp.epoch.model.ct.request.PriceProtection;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.model.customergraph.response.CGResponse;
import com.dtv.dcp.epoch.processor.helper.CPOPDevicesHelper;
import com.dtv.dcp.epoch.processor.helper.CPOPProductsHelper;
import com.dtv.dcp.epoch.processor.ott.services.OttLeadProductsProcessor;
import com.dtv.dcp.epoch.processor.ott.services.OttProductsServicesProcessor;
import com.dtv.dcp.epoch.service.customergraph.CustomerGraphService;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.dtv.dcp.epoch.util.PnpGroupUtils;


@Component
public class OttProductsProcessor {

    /** The log. */
    private static Logger log = LoggerFactory.getLogger(OttProductsProcessor.class);




    @Autowired
    CpopClientHelper cpopClientHelper;
    
    @Autowired
    CPOPDevicesHelper cpopDevicesHelper;

    @Autowired
    CPOPProductsHelper cpopProductsHelper;

    @Autowired
    OffersUtils offersUtils;
    
	@Autowired
	PnpGroupUtils pnpGroupUtils;



	@Autowired
	CustomerGraphService customerGraphService;

	@Autowired
	private FeatureManagerHelper featureManagerHelper;

	@Autowired
    OttProductsServicesProcessor ottProductsServicesProcessor;

	@Autowired
	OttLeadProductsProcessor ottLeadProductsProcessor;

    @Value("${apiclient.rest.cpopofferms.ctstate}")
    private String ctstate;

    /**
     *
     * @param productRequestWrapper
     * @return
     */
    public CTProductResponse getProducts(ProductRequestWrapper productRequestWrapper) {

        CTProductRequest ctProductRequest = new CTProductRequest();
        CGResponse cgResponse = null;

        try {
            BeanUtils.copyProperties(ctProductRequest, productRequestWrapper.getCtProductRequest());
            ctProductRequest.setSalesChannel(null);
        } catch (Exception e) {
            log.error("Error in BeanUtils.copyProperties: " + e.getMessage());
        }

        if (productRequestWrapper.getProductRequest() != null) {
            ctProductRequest.setProductCodes(productRequestWrapper.getProductRequest().getProductCodes());
            ctProductRequest.setProductSalesChannel(productRequestWrapper.getProductRequest().getSalesChannel());
            ctProductRequest.setBusinessSegment(productRequestWrapper.getProductRequest().getBusinessSegment());
        }
        log.info("CTRequest: {}",ctProductRequest);


        CTProductResponse ctProductResponse = null;

        boolean isEmployeeAccount = false;
        boolean isContracted = false;
        String contractIndicator = null;
        boolean isMobility = productRequestWrapper.isMobility();
		String customerSegment = null;
		String iapAccountType = null;
		boolean filteredRemovalRuleByChannel = false;
		ctProductRequest.setBusinessSegment(productRequestWrapper.getProductRequest().getBusinessSegment());
//        if (Optional.ofNullable(productRequestWrapper.getProductRequest().getContractApplicable()).isPresent() &&
//                !productRequestWrapper.getProductRequest().getContractApplicable().isEmpty()) {
//            isContracted = Constants.CONTRACT.equalsIgnoreCase(productRequestWrapper.getProductRequest().getContractApplicable().get(0)) ? true : false;
//        }
        
        if (Optional.ofNullable(productRequestWrapper.getProductRequest().getContractApplicable()).isPresent() &&
             !productRequestWrapper.getProductRequest().getContractApplicable().isEmpty()) {
        	contractIndicator = productRequestWrapper.getProductRequest().getContractApplicable().get(0);
        }
        List<String> salesChannelsList = null;
        if(productRequestWrapper.getProductRequest() != null 
        		&& Optional.ofNullable(productRequestWrapper.getProductRequest().getSalesChannel()).isPresent() 
        		&& !productRequestWrapper.getProductRequest().getSalesChannel().isEmpty()) {
        	salesChannelsList = productRequestWrapper.getProductRequest().getSalesChannel();
        }
        //SERVICES
        if (!StringUtils.isBlank(productRequestWrapper.getDtvnAccount())) {
        	
        	 // Set Credit Risk for video-devices services flow
            if (Objects.nonNull(productRequestWrapper.getProductRequest().getProductTypes())
                    && productRequestWrapper.getProductRequest().getProductTypes().contains(Constants.VIDEO_DEVICE)) {
                String creditRisk = Constants.LOW_CREDIT_RISK;
                if (org.apache.commons.lang.StringUtils.isNotBlank(productRequestWrapper.getProductRequest().getCreditRisk())) {
                    creditRisk = productRequestWrapper.getProductRequest().getCreditRisk();
                    if (creditRisk.equalsIgnoreCase(Constants.UNKNOWN_CREDIT_RISK)) {
                        creditRisk = Constants.HIGH_CREDIT_RISK;
                    }
                }
                ctProductRequest.setCreditRiskEligibility(creditRisk);
            }
            
            if(Objects.nonNull(productRequestWrapper.getProductRequest().getCustomerContext()) &&
            		Objects.nonNull(productRequestWrapper.getProductRequest().getCustomerContext().getOtt()) &&
            		Objects.nonNull(productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getPriceProtection())) {
                PriceProtection priceProtection = productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getPriceProtection();
                if(productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getNextBillingDate()!=null) {
                	priceProtection.setNextBillingDate(productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getNextBillingDate());
                }
                ctProductRequest.setPriceProtection(priceProtection);
                if(null != contractIndicator) {
                	priceProtection.setContractIndicator(Stream.of(contractIndicator).collect(Collectors.toList()));
                }
                priceProtection.setIsHogWindowMatch(offersUtils.isConfiguredHogPriceProtectionWindow(priceProtection));
            } else if(Objects.nonNull(productRequestWrapper.getProductRequest().getCustomerContext()) &&
            		Objects.nonNull(productRequestWrapper.getProductRequest().getCustomerContext().getOtt()) &&
            		productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getNextBillingDate()!=null) {
            	CTProductNextBillingDate nextBillingDate = new CTProductNextBillingDate();
            	nextBillingDate.setNextBillingDate(productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getNextBillingDate());
            	if(null != contractIndicator) {
            		nextBillingDate.setContractIndicator(Stream.of(contractIndicator).collect(Collectors.toList()));
                }
            	ctProductRequest.setNextBillingDate(nextBillingDate);
            }
        	

            if ((Optional.ofNullable(productRequestWrapper.getProductRequest().getCustomerContext()).isPresent() &&
                    Optional.ofNullable(productRequestWrapper.getProductRequest().getCustomerContext().getOtt()).isPresent()) &&
                    !Optional.ofNullable(productRequestWrapper.getProductRequest().getProductCodes()).isPresent() &&
                    !Optional.ofNullable(productRequestWrapper.getProductRequest().getProductIds()).isPresent()) {
            	
            	isEmployeeAccount = Constants.EMPLOYEE.equalsIgnoreCase(productRequestWrapper.getProductRequest().getCustomerContext().getOtt()
						.getAccountType());
				 //Filter existing base package
                //Apply promo continuation logic only for video-plan
                ctProductResponse = ottProductsServicesProcessor.processProductsServices(ctProductRequest, productRequestWrapper);
            } else {
                ctProductRequest.setContractApplicable(null);
                ctProductResponse = cpopClientHelper.getProducts(ctProductRequest);
            }



            //Get information from CG if not available in customerContext
            if (!Optional.ofNullable(productRequestWrapper.getProductRequest().getCustomerContext()).isPresent()
                    || (Optional.ofNullable(productRequestWrapper.getProductRequest().getCustomerContext()).isPresent() &&
                    !Optional.ofNullable(productRequestWrapper.getProductRequest().getCustomerContext().getOtt()).isPresent())) {

                cgResponse = getCustomerAccount(productRequestWrapper.getDtvnAccount());
                if (Optional.ofNullable(cgResponse).isPresent() && Optional.ofNullable(cgResponse.getAccountInfo()).isPresent() &&
                        Optional.ofNullable(cgResponse.getAccountInfo().isIsPremiumCustomer()).isPresent()) {
                    isContracted = true;
                }
                if (Optional.ofNullable(cgResponse).isPresent() && Optional.ofNullable(cgResponse.getAccountInfo()).isPresent() &&
                        Optional.ofNullable(cgResponse.getAccountInfo().getAccountType()).isPresent()) {
                    isMobility = cgResponse.getAccountInfo().getAccountType().equalsIgnoreCase(Constants.MOBILITY) ? true : false;
                    isEmployeeAccount = cgResponse.getAccountInfo().getAccountType().equalsIgnoreCase(Constants.EMPLOYEE) ? true : false;
                }
            }
            if (featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_INCLUDE_PRODUCT_FILTERS)) {
                OffersUtils.filterIncludedProductsBasedOnSalesChannelOrExcludedPartner(ctProductResponse, salesChannelsList, getIapPartnerAccountTypeDetails(productRequestWrapper));
            }
			if (featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_CONFLICTING_PRODUCT_FILTERS)) {
				OffersUtils.filterConflictingProductsBasedOnSalesChannelOrExcludedPartner(ctProductResponse, salesChannelsList, getIapPartnerAccountTypeDetails(productRequestWrapper));
			}
        } else {
            // SALES
            if(Objects.nonNull(ctProductRequest)) {


                // Set Credit Risk for video-devices sales flow
                if (Objects.nonNull(productRequestWrapper.getProductRequest().getProductTypes())
                        && productRequestWrapper.getProductRequest().getProductTypes().contains(Constants.VIDEO_DEVICE)) {
                    String creditRisk = Constants.LOW_CREDIT_RISK;
                    if (org.apache.commons.lang.StringUtils.isNotBlank(productRequestWrapper.getProductRequest().getCreditRisk())) {
                        creditRisk = productRequestWrapper.getProductRequest().getCreditRisk();
                        if (creditRisk.equalsIgnoreCase(Constants.UNKNOWN_CREDIT_RISK)) {
                            creditRisk = Constants.HIGH_CREDIT_RISK;
                        }
                    }
                    ctProductRequest.setCreditRiskEligibility(creditRisk);
                }


                ctProductResponse = cpopClientHelper.getProducts(ctProductRequest);
            }

        }

		List<ProductObj> filteredList = ctProductResponse.getProducts();	
		
		String treatmentCode=productRequestWrapper.getProductRequest().getTreatmentCode();
		
		String creditRisk=ctProductRequest.getCreditRiskEligibility();

		if (Objects.nonNull(productRequestWrapper.getProductRequest().getCustomerContext())
				&& Objects.nonNull(productRequestWrapper.getProductRequest().getCustomerContext().getOtt())
				&& productRequestWrapper.getProductRequest().getCustomerContext().getOtt()
						.getNextBillingDate() != null) {
			
			Boolean isPNP=false;
        	String PnpCustomerGroup="";

    		//Added HOG global config logic for RR and TAZCONTRACT
            if(Objects.nonNull(productRequestWrapper.getProductRequest().getContractApplicable())
                    && (productRequestWrapper.getProductRequest().getContractApplicable().contains(Constants.TAZCONTRACT_STRING)
                    || productRequestWrapper.getProductRequest().getContractApplicable().contains(Constants.ROAD_RUNNER))
                    && Objects.nonNull(productRequestWrapper.getProductRequest().getCustomerContext()) &&
                    Objects.nonNull(productRequestWrapper.getProductRequest().getCustomerContext().getOtt()) &&
                    Objects.nonNull(productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getNextBillingDate()) &&
                    Objects.nonNull(productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getPriceProtection())){

                PriceProtection priceProtection = productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getPriceProtection();

                String nbcd = productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getNextBillingDate();

                PnpCustomerGroup = pnpGroupUtils.getPnpGroup(priceProtection, nbcd,Constants.OTT);
                isPNP= (Objects.isNull(PnpCustomerGroup) ||PnpCustomerGroup.isEmpty())? isPNP : true;

                //if hog window match then consider the customer as non pnp
                if (Boolean.TRUE.equals(priceProtection.getIsHogWindowMatch())) {
                    isPNP = false;
                    PnpCustomerGroup = "";
                }

            }
    		
			filteredList = cpopProductsHelper.filterNBCDPriceByContractIndicator(ctProductResponse.getProducts(),
					contractIndicator,
					productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getNextBillingDate(),
					treatmentCode, creditRisk, isPNP, PnpCustomerGroup);
			// SLS-IXP-FLAG: Run attribute pricing criteria AFTER contract-indicator filter.
			// Single OR check so it executes exactly once even if both flags are enabled.
			// Uses getProducts flags. BAU fallback is inside applyAttributePricingCriteriaToProducts.
			// SLS-IXP-FLAG changes
			if (offersUtils.isSlsGetProductSalesEnabled() || offersUtils.isSlsGetProductServicesEnabled()) {
				cpopProductsHelper.applyAttributePricingCriteriaToProducts(ctProductResponse.getProducts(), productRequestWrapper.getProductRequest());
			}
			cpopProductsHelper.filterPricesByPriceTier(ctProductResponse.getProducts(), productRequestWrapper.getProductRequest());
			ctProductResponse.setProducts(filteredList);
		} else {
			filteredList = cpopProductsHelper.filterPriceByContractIndicator(ctProductResponse.getProducts(),
					contractIndicator, treatmentCode, creditRisk);
			// SLS-IXP-FLAG: Run attribute pricing criteria AFTER contract-indicator filter.
			// Single OR check so it executes exactly once even if both flags are enabled.
			// Uses getProducts flags. BAU fallback is inside applyAttributePricingCriteriaToProducts.
			// SLS-IXP-FLAG changes
			if (offersUtils.isSlsGetProductSalesEnabled() || offersUtils.isSlsGetProductServicesEnabled()) {
				cpopProductsHelper.applyAttributePricingCriteriaToProducts(ctProductResponse.getProducts(), productRequestWrapper.getProductRequest());
			}
			cpopProductsHelper.filterPricesByPriceTier(ctProductResponse.getProducts(), productRequestWrapper.getProductRequest());
			ctProductResponse.setProducts(filteredList);
		}
        ctProductResponse.setProducts(filteredList);
        // Apply 3D GlobalMessages after prices are resolved by filterPricesByPriceTier.
		// SLS-IXP-FLAG changes
        if ((offersUtils.isSlsGetProductSalesEnabled() || offersUtils.isSlsGetProductServicesEnabled())
                && Objects.nonNull(ctProductResponse)) {
            String salesChannel = CollectionUtils.isNotEmpty(salesChannelsList) ? salesChannelsList.get(0) : Constants.DEFAULT;
            String flowType = offersUtils.getProductsFlowType(productRequestWrapper.getProductRequest());
            offersUtils.applyGlobalMessages3DForProducts(ctProductResponse, salesChannel, flowType);
        }
        List<ProductObj> filteredListP = cpopProductsHelper.filterByCompatibleProducts(ctProductResponse.getProducts(), isMobility, isEmployeeAccount);
        cpopProductsHelper.filterExpiredInstallmentOptions(ctProductResponse.getProducts());
		// SLS-IXP-FLAG changes
		boolean pendingSwimLaneSwitch=false;
		if(offersUtils.isSlsGetProductServicesEnabled()) {
			pendingSwimLaneSwitch = Optional.ofNullable(productRequestWrapper.getProductRequest())
					.map(ProductRequest::getCustomerContext)
					.map(com.dtv.dcp.epoch.model.common.request.CustomerContext::getOtt)
					.map(CartProduct::getPendingSwimlaneSwitch)
					.orElse(false);
		}
		//Case 1: filter removalRulesByChannel ActionRule By IneligibleAccountStatuses
		if (Optional.ofNullable(productRequestWrapper.getProductRequest().getCustomerContext()).isPresent()
				&& Objects.nonNull(productRequestWrapper.getProductRequest().getCustomerContext())
				&& ((Optional.ofNullable(
						productRequestWrapper.getProductRequest().getCustomerContext().getAdditionalAccountDetails())
						.isPresent()
				&& Objects.nonNull(
						productRequestWrapper.getProductRequest().getCustomerContext().getAdditionalAccountDetails())) || pendingSwimLaneSwitch)
		) {
			String iapPartnerAccountType = getIapPartnerAccountTypeDetails(productRequestWrapper);
			String accountType = getAccountTypeDetails(productRequestWrapper);
			cpopProductsHelper.filterActionRuleByIneligibleAccountStatuses(ctProductResponse.getProducts(),
					productRequestWrapper.getProductRequest(), iapPartnerAccountType, accountType,
					checkIapPartnerAccountTypeExist(iapPartnerAccountType, accountType), checkAccountTypeEmployeeExist(accountType),pendingSwimLaneSwitch);
			filteredRemovalRuleByChannel = true;
		}
		
		//Case 2:  filter removalRulesByChannel ActionRule for IAPPartnerAccountType as ROKUTV or FIRETV or GOOGLE, and AccountType not EMPLOYEE
		if (Objects.nonNull(productRequestWrapper.getProductRequest().getCustomerContext()) &&
				Objects.nonNull(productRequestWrapper.getProductRequest().getCustomerContext().getOtt()) &&
				(!Objects.nonNull(productRequestWrapper.getProductRequest().getCustomerContext().getAdditionalAccountDetails()) && !pendingSwimLaneSwitch) &&
				Objects.nonNull(productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getIapPartnerAccountType()) &&
				productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getIapPartnerAccountType() != null) {
			iapAccountType = productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getIapPartnerAccountType();

			if (Objects.nonNull(productRequestWrapper.getProductRequest().getCustomerContext()) &&
					Objects.nonNull(productRequestWrapper.getProductRequest().getCustomerContext().getOtt()) &&
					Objects.nonNull(productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getAccountType()) &&
					productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getAccountType() != null) {
				customerSegment = productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getAccountType();
			}
			if((iapAccountType.equalsIgnoreCase(Constants.ROKUTV) || iapAccountType.equalsIgnoreCase(Constants.FIRETV)
					|| iapAccountType.equalsIgnoreCase(Constants.GOOGLE)) && customerSegment !=null
					&& !customerSegment.equalsIgnoreCase(Constants.EMPLOYEE)) {
				cpopProductsHelper.filterRemovalRuleBySalesChannel(ctProductResponse.getProducts(), iapAccountType,
						customerSegment, productRequestWrapper.getProductRequest());
				filteredRemovalRuleByChannel = true;
			}
		}
		
		//Case 3:  filter removalRulesByChannel ActionRule for AccountType is EMPLOYEE, and IAPPartnerAccountType not as ROKUTV or FIRETV or GOOGLE
		if (Objects.nonNull(productRequestWrapper.getProductRequest().getCustomerContext()) &&
				Objects.nonNull(productRequestWrapper.getProductRequest().getCustomerContext().getOtt()) &&
				(!Objects.nonNull(productRequestWrapper.getProductRequest().getCustomerContext().getAdditionalAccountDetails()) && !pendingSwimLaneSwitch) &&
				Objects.nonNull(productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getAccountType()) &&
				productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getAccountType() != null &&
				StringUtils.isEmpty(iapAccountType)) {
			String accntType = productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getAccountType();
			cpopProductsHelper.filterNonIAPRemovalRules(ctProductResponse.getProducts(), accntType,iapAccountType, productRequestWrapper.getProductRequest());
			filteredRemovalRuleByChannel = true;
		}
		
		//Setting removalRulesByChannel as null when not filtered in above cases
		if (!filteredRemovalRuleByChannel) {
			ctProductResponse.getProducts().stream().filter(Objects::nonNull).forEach(productObj -> {
				productObj.getVariants().get(0).getAttributes().setRemovalRuleByChannel(null);
			});
		}

        if(Objects.nonNull(ctProductResponse) && ctProductResponse.getProducts() !=null ) {
        	//setting leadoffer attribute
	       	if(CollectionUtils.isNotEmpty(salesChannelsList)) {
	       	  ottLeadProductsProcessor.populateLeadAttribute(filteredListP, productRequestWrapper);
	       	}
            ctProductResponse.setCount(ctProductResponse.getProducts().size());
            ctProductResponse.setTotal(ctProductResponse.getProducts().size());
        }
        long startTimeInMillis = System.currentTimeMillis();
        
        updateDisplayTypeForProduct(ctProductResponse,productRequestWrapper);
        log.info("::::::::Time taken for Processing::::::"+
        (System.currentTimeMillis() - startTimeInMillis));
        ctProductResponse.setProducts(filteredListP);


        return ctProductResponse;
    }

    public List<DeviceInfo> getDevices(ProductRequestWrapper productRequestWrapper) {
    	
    	log.info("Start of get device details from CT");
    	
    	CTProductRequest ctDevicedetailsRequest = new CTProductRequest();
    	
    	CTDeviceDetailsRequest Details= new CTDeviceDetailsRequest();
    	
    	List<String> ProductType = new ArrayList<String>();
    	ProductType.add("deviceDetails");    	
    	ctDevicedetailsRequest.setProductTypes(ProductType);
    	
    	 List<String> make= new ArrayList<String>();
    	
    	 List<String> model= new ArrayList<String>();
    	
    	 List<String> deviceType= new ArrayList<String>();
    	 
    	List<DeviceInfo> DeviceDetails;
    	
    	DeviceDetails=productRequestWrapper.getProductRequest().getDevices();
    	
    	DeviceDetails.stream().filter(Objects::nonNull).forEach(deviceObj ->
    	{
    		if(deviceObj.getMake()!=null && !deviceObj.getMake().equalsIgnoreCase("UNKNOWN")) {
    			make.add(deviceObj.getMake());
    			}
    		else {
    			make.add("UNKNOWN");
    		}
    		if(deviceObj.getModel()!=null && !deviceObj.getModel().equalsIgnoreCase("UNKNOWN")) {
    			model.add(deviceObj.getModel());
    			}
    		else {
    			model.add("UNKNOWN");
    		}
    		if(deviceObj.getDeviceType()!=null && !deviceObj.getDeviceType().equalsIgnoreCase("UNKNOWN")){
    			deviceType.add(deviceObj.getDeviceType());
    			}
    		else {
    			deviceType.add("UNKNOWN");
    		}
    	});
    	
    	make.add("UNKNOWN");
    	model.add("UNKNOWN");
    	deviceType.add("UNKNOWN");
    	
    	Details.setMake(make);
    	Details.setDeviceType(deviceType);
    	Details.setModel(model);
    	ctDevicedetailsRequest.setDevices(Details);
    	
    	
    	CTProductResponse ctProductResponse = cpopClientHelper.getProducts(ctDevicedetailsRequest);;
    	
    	List<DeviceInfo> DeviceDetailsResponse = new ArrayList<DeviceInfo>();
    	Map<String, DeviceInfo> devicesMap = new HashMap<String, DeviceInfo>();
    	
    	
    	ctProductResponse.getProducts().stream().filter(Objects::nonNull).forEach(productObj -> {
    		
    		DeviceInfo currentDevice=new DeviceInfo();
    		
    		
    		currentDevice.setMake(productObj.getVariants().get(0).getAttributes().getMake());
    		currentDevice.setModel(productObj.getVariants().get(0).getAttributes().getModel());
    		currentDevice.setThirdPartyOtt(String.valueOf(productObj.getVariants().get(0).getAttributes().getThirdPartyOTT()));
    		currentDevice.setCatalogProductName(productObj.getVariants().get(0).getAttributes().getCatalogProductName());
    		currentDevice.setDeviceName(productObj.getVariants().get(0).getAttributes().getDeviceName());
    		currentDevice.setDeviceType(productObj.getVariants().get(0).getAttributes().getDeviceType());
    		String DeviceCategory=productObj.getVariants().get(0).getAttributes().getDeviceCategory();
    		if(DeviceCategory!=null) {
    		currentDevice.setDeviceCategory(DeviceCategory);}
    		else {
    			currentDevice.setDeviceCategory("");
    		}

    		
    		
    		devicesMap.put(currentDevice.getMake()+currentDevice.getModel()+currentDevice.getDeviceType(), currentDevice);
    		
    		
    	});
    	
    	log.info("CT device details request:{}",ctDevicedetailsRequest);
    	
    	CPOPDevicesHelper deviceObj=new CPOPDevicesHelper();
    	
    	DeviceDetailsResponse=deviceObj.returnDeviceInfoResponse(productRequestWrapper.getProductRequest(), devicesMap);
    	
		return DeviceDetailsResponse;
    	
    	
    }
    
    
    public void updateDisplayTypeForProduct(CTProductResponse ctProductResponse,
    		ProductRequestWrapper productRequestWrapper) {
    	Map<String, List<String>> productBenfitMapping = new HashMap<>();

    	getProductCodesFromRequest(productRequestWrapper, productBenfitMapping);

    	if (Optional.ofNullable(ctProductResponse.getProducts()).isPresent()) {
    		ctProductResponse.getProducts().stream().filter(Objects::nonNull).forEach(productObj -> {
    			productBenfitMapping.entrySet().parallelStream().forEach(promoMap -> {
    				if(promoMap.getKey().equalsIgnoreCase(productObj.getCode())) {
    					if (Objects.nonNull(productObj.getVariants())
    							&& Objects.nonNull(productObj.getVariants().get(0))) {
    						Variant variant1 = productObj.getVariants().get(0);
    						if (Objects.nonNull(variant1)) {
    						    if(Optional.ofNullable(variant1.getAttributes().getBenefitsOnCustomersAccount()).isPresent()
    									&& Optional.ofNullable(promoMap.getValue()).isPresent()
    									&&(!Collections.disjoint(promoMap.getValue(), variant1.getAttributes().getBenefitsOnCustomersAccount()))) {
    						    	variant1.getAttributes().setDisplayType("included");
    							}
    						}

    					}
    				}
    			});
    			if (Objects.nonNull(productObj.getVariants())
						&& Objects.nonNull(productObj.getVariants().get(0))
						&& Optional.ofNullable(productObj.getVariants().get(0).getAttributes().getBenefitsOnCustomersAccount()).isPresent()) {
        			productObj.getVariants().get(0).getAttributes().setBenefitsOnCustomersAccount(null);
    			}
    		});
    	}
    }
    
    private  void getProductCodesFromRequest(ProductRequestWrapper productRequestWrapper, Map<String, List<String>> productBenfitMapping) {
		if (Optional.ofNullable(productRequestWrapper.getProductRequest().getCustomerContext()).isPresent()
				&& Optional.ofNullable(productRequestWrapper.getProductRequest().getCustomerContext().getOtt()).isPresent()
				&& Optional.ofNullable(productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getProducts()).isPresent()
				&& productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getProducts().size() > 0) {
			productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getProducts().forEach(prod -> {
				if (prod.getProductType().equalsIgnoreCase("video-addon")) {
					if(Optional.ofNullable(prod.getPromotions()).isPresent()) {
						List<String> promoCodes = new ArrayList<String>();
						prod.getPromotions().stream().filter(Objects::nonNull).forEach(promo -> {
							promoCodes.add(promo.getPromotionId());
						});
						productBenfitMapping.put(prod.getProductCode(), promoCodes);
					}
				}
			});
		}
	}

    public CGResponse getCustomerAccount(String accountId) throws ServiceException {
        CGResponse cgResponse = null;
        try {
            cgResponse = customerGraphService.getActiveSubscriptions(accountId,featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)?"backup":"primary");
        }catch (ServiceException ex) {
           // log.error("Failed to getCustomerAccount::: accountId: {}", ESAPI.encoder().encodeForHTML(accountId));            
            throw  ex;
        }
        return cgResponse;
    }

    public boolean isValidProductRequest(@NotNull ProductRequest productRequest) {
        if (Optional.ofNullable(productRequest.getCustomerContext()).isPresent()
                || Optional.ofNullable(productRequest.getProductCodes()).isPresent()
                || Optional.ofNullable(productRequest.getProductTypes()).isPresent()
        ) {
            return true;
        }
        log.error("Required request payload doesn't met  ...");
        return false;
    }
    public boolean isValidDevicesRequest(@NotNull ProductRequest productRequest) {
        if (Optional.ofNullable(productRequest.getDevices()).isPresent()) {
            return true;
        }
        log.error("Required request payload doesn't met  ...");
        return false;
    }
	
	private String getAccountTypeDetails(ProductRequestWrapper productRequestWrapper) {
		String accountType = null;
		if (Objects.nonNull(productRequestWrapper.getProductRequest().getCustomerContext())
				&& Objects.nonNull(productRequestWrapper.getProductRequest().getCustomerContext().getOtt())
				&& Objects.nonNull(
						productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getAccountType())
				&& productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getAccountType() != null) {
			accountType = productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getAccountType();
		}
		return accountType;
	}

	private String getIapPartnerAccountTypeDetails(ProductRequestWrapper productRequestWrapper) {
		String iapPartnerAccountType = null;
		if (Objects.nonNull(productRequestWrapper.getProductRequest().getCustomerContext())
				&& Objects.nonNull(productRequestWrapper.getProductRequest().getCustomerContext().getOtt())
				&& Objects.nonNull(productRequestWrapper.getProductRequest().getCustomerContext().getOtt()
						.getIapPartnerAccountType())
				&& productRequestWrapper.getProductRequest().getCustomerContext().getOtt()
						.getIapPartnerAccountType() != null) {
			iapPartnerAccountType = productRequestWrapper.getProductRequest().getCustomerContext().getOtt()
					.getIapPartnerAccountType();
		}
		return iapPartnerAccountType;
	}
	
	private boolean checkIapPartnerAccountTypeExist(String iapPartnerAccountType, String accountType) {
		boolean flag = false;
		if (!StringUtils.isEmpty(iapPartnerAccountType)
				&& (iapPartnerAccountType.equalsIgnoreCase(Constants.ROKUTV)
						|| iapPartnerAccountType.equalsIgnoreCase(Constants.FIRETV)
						|| iapPartnerAccountType.equalsIgnoreCase(Constants.GOOGLE))
				&& !StringUtils.isEmpty(accountType) && !accountType.equalsIgnoreCase(Constants.EMPLOYEE)) {
			flag = true;
		}
		return flag;
	}

	private boolean checkAccountTypeEmployeeExist(String accountType) {
		boolean flag = false;
		if (!StringUtils.isEmpty(accountType) && accountType.equalsIgnoreCase(Constants.EMPLOYEE)) {
			flag = true;
		}
		return flag;
	}

}

