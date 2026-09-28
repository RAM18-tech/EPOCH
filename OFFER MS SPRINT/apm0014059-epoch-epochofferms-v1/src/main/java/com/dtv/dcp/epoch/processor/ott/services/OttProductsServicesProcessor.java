package com.dtv.dcp.epoch.processor.ott.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.model.common.request.BenefitRequest;
import com.dtv.dcp.epoch.model.common.request.BenefitRequestWrapper;
import com.dtv.dcp.epoch.model.common.request.CustomerPromotion;
import com.dtv.dcp.epoch.model.common.request.ProductInfo;
import com.dtv.dcp.epoch.model.common.request.ProductRequestWrapper;
import com.dtv.dcp.epoch.model.ct.benefit.Benefit;
import com.dtv.dcp.epoch.model.ct.product.Attributes;
import com.dtv.dcp.epoch.model.ct.product.Product;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.model.ct.product.ProductWrapper;
import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.model.customergraph.response.CGPromotion;
import com.dtv.dcp.epoch.model.customergraph.response.CGResponse;
import com.dtv.dcp.epoch.model.customergraph.response.CGServiceInfo;
import com.dtv.dcp.epoch.processor.helper.CPOPProductsHelper;
import com.dtv.dcp.epoch.processor.ott.services.model.CtProductInfo;
import com.dtv.dcp.epoch.processor.ott.services.model.CustomerServiceDetail;
import com.dtv.dcp.epoch.service.customergraph.CustomerGraphServiceImpl;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.JsonService;
import com.dtv.dcp.epoch.util.OffersUtils;

@Component
public class OttProductsServicesProcessor {

    private static final Logger log = LoggerFactory.getLogger(OttProductsServicesProcessor.class);

    List<ProductObj> fiteredCTProducts = null;
    List<ProductObj> processedCTProducts = null;
    CustomerSubscriptionDetail customerSubscriptionDetail = null;

    Map<String, String> currentServiceInfo = new HashMap<>();
    Map<String, String> availablePackageGroup;

    CTProductResponse finalCTProductResponse = new CTProductResponse();

    private static final String FREE_PROMO = "free-promo";

    private static final String BASIC_SERVICE = "Basic Service";


    private static final String SPECIAL = "SPECIAL";

    private static final String FREE = "Free";

    CGResponse cGResponse = null;

    @Autowired
    CpopClientHelper cpopClientHelper;

    @Autowired
    @Qualifier("CustomerGraph")
    CustomerGraphServiceImpl customerGraphServiceImpl;

    @Autowired
    OttProductsServicesProcessorHelper ottProductsServicesProcessorHelper;

    @Autowired
    OttAvaialbleOffersProcessor ottAvaialbleOffersProcessor;


    @Autowired
    private FeatureManagerHelper featureManagerHelper;

    @Autowired
    OttServicesOffersProcessorHelper ottServicesOffersProcessorHelper;

    @Autowired
    OttServicesBenefitsProcessor ottServicesBenefitsProcessor;

    @Autowired
    CPOPProductsHelper cpopProductsHelper;



    public CTProductResponse processProductsServices(CTProductRequest ctProductRequest, ProductRequestWrapper productRequestWrapper) {
        CTProductResponse ctProductResponse = null;
        List<String> basePackageCompatibleList = new ArrayList<>();
        processedCTProducts = new ArrayList<>();


        try {
            customerSubscriptionDetail = populateCustomerSubscriptionDetail(productRequestWrapper);

        } catch (ParseException e) {
            log.error("Error in method populateCustomerSubscriptionDetail() method", e);
        }
        if (productRequestWrapper.getProductRequest().getProductTypes().contains(Constants.VIDEO_PLAN)) {
            ctProductResponse = ottProductsServicesProcessorHelper.getFilteredProducts(productRequestWrapper, Constants.BASE);

            fiteredCTProducts = filterCTProductResponse(ctProductResponse, customerSubscriptionDetail, productRequestWrapper);

            if (log.isDebugEnabled()) {
                log.info("Filtered products :- ", JsonService.getJsonFromObject(fiteredCTProducts));
            }

            availablePackageGroup = ottAvaialbleOffersProcessor.retrieveAvailablePackageGroup(customerSubscriptionDetail, currentServiceInfo);

            List<ProductObj> availableCtOfferList = new ArrayList<>();
            for (ProductObj filteredCtProduct : fiteredCTProducts) {
                if (customerSubscriptionDetail != null) {
                    ProductObj availableCtOffer = createAvailableBasePackageForDTVNow(filteredCtProduct, customerSubscriptionDetail, availablePackageGroup);
                    if (availableCtOffer != null) {
                        availableCtOfferList.add(availableCtOffer);
                    }
                }
            }
            //fiteredCTProducts.addAll(availableCtOfferList);
            processedCTProducts.addAll(availableCtOfferList);

           // List<CTOffer> ottOffers = dtvnServicesFeeOffersProcessor.retrieveFeeOffers(offerRequestWrapper);

            // Populating RSN Fee details for Available VideoPlan Offers
            // dtvnServicesRSNFeeOffersProcessor.populateRSNFeeDetails(offerRequestWrapper,customerSubscriptionDetail,processedCTOffers, ottOffers);
//            ctProductRequest.setProductTypes(java.util.Arrays.asList(Constants.VIDEO_PLAN));
//            cpopClientHelper.getProducts(ctProductRequest);
        }
        if(productRequestWrapper.getProductRequest().getProductTypes().contains(Constants.VIDEO_DEVICE) || productRequestWrapper.getProductRequest().getProductTypes().contains(Constants.VIDEO_ADDON)) {

            final List<ProductWrapper>[] basePackageCompatibleProds = new List[]{null};
            if (Optional.ofNullable(productRequestWrapper.getProductRequest().getCustomerContext()).isPresent() &&
                    Optional.ofNullable(productRequestWrapper.getProductRequest().getCustomerContext().getOtt()).isPresent()) {
                productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getProducts().forEach(prod -> {
                    if (prod.getProductType().equalsIgnoreCase(Constants.VIDEO_PLAN)) {
                        basePackageCompatibleProds[0] = getBasePackageCompatibleListFromCT(prod.getProductCode(), productRequestWrapper.isMobility(),productRequestWrapper.isEmployeeAccount(),Objects.nonNull(productRequestWrapper.getProductRequest().getBusinessSegment()) ?
                        		productRequestWrapper.getProductRequest().getBusinessSegment() : Arrays.asList("CONS"));
                    }
                });
            } else if ((StringUtils.isNotBlank(productRequestWrapper.getDtvnAccount()))) {
                cGResponse = customerGraphServiceImpl.getActiveSubscriptions(productRequestWrapper.getDtvnAccount(), featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP) ? "backup" : "primary");
                boolean isEmployeeAccount = Constants.EMPLOYEE.equalsIgnoreCase(OffersUtils.getUverseCustomerAccountType(cGResponse));
                for (CGServiceInfo serviceInfo : Arrays.asList(cGResponse.getServiceInfo())) {
                    if (BASIC_SERVICE.equals(serviceInfo.getTypeOfPlan())) {
                        basePackageCompatibleProds[0] = getBasePackageCompatibleListFromCT(serviceInfo.getServiceID(), productRequestWrapper.isMobility(),isEmployeeAccount,Objects.nonNull(productRequestWrapper.getProductRequest().getBusinessSegment()) ?
                        		productRequestWrapper.getProductRequest().getBusinessSegment() : Arrays.asList("CONS"));
                    }
                }
            }
            if (Objects.nonNull(basePackageCompatibleProds[0]) && basePackageCompatibleProds[0].size() > 0) {
                basePackageCompatibleProds[0].forEach(prods -> {
                    if (Objects.nonNull(prods.getProducts())) {
                        prods.getProducts().forEach(prod -> {
                            basePackageCompatibleList.add(prod.getKey());
                        });
                    }
                });
            }
        }
        if (productRequestWrapper.getProductRequest().getProductTypes().contains(Constants.VIDEO_ADDON)) {

            List<ProductObj> productObjList = retrieveAvailableAddOns(productRequestWrapper);

            String accountType = productRequestWrapper.isMobility() ? "Mobility" :
    			(Optional.ofNullable(productRequestWrapper.getProductRequest().getAccountTypes().get(0)).isPresent() ? 
    					productRequestWrapper.getProductRequest().getAccountTypes().get(0) : "Residential");
            // List<CTOffer> standAloneOffers = offersUtils.filterStandAloneOffersByAddOnTypePlanSubType(ottOffers, Constants.STANDALONE,Constants.INTERNATIONAL, accountType);
            // Filter compatible addon based on current base package
            if (!basePackageCompatibleList.isEmpty() && productObjList != null) {
                productObjList = filterCompatibleProductsForCurrentBasePackage(basePackageCompatibleList, productObjList);
            }
            if (productObjList != null) {
                processedCTProducts.addAll(productObjList);
            }
//            if(standAloneOffers != null) {
//                processedCTProducts.addAll(standAloneOffers);
//            }
        }
        
		if (productRequestWrapper.getProductRequest().getProductTypes().contains(Constants.PROTECTION_PLAN)) {
			List<ProductObj> productObjList = retrieveProtectionPlanProduct(productRequestWrapper);
			if (!basePackageCompatibleList.isEmpty() && productObjList != null) {
				productObjList = filterCompatibleProductsForCurrentBasePackage(basePackageCompatibleList,
						productObjList);
			}
			if (productObjList != null) {
				processedCTProducts.addAll(productObjList);
			}
		}

        if (Optional.ofNullable(productRequestWrapper).isPresent()
                && Optional.ofNullable(productRequestWrapper.getProductRequest()).isPresent()
                && (((productRequestWrapper.getProductRequest().getProductTypes()).contains(Constants.VIDEO_DEVICE))
                || productRequestWrapper.getProductRequest().getProductTypes()
                .contains(Constants.VIDEO_ACCESSORY))) {

            List<ProductObj> ottProducts = retrieveEquipmentProducts(productRequestWrapper);

           // ottSalesOffersProcessorHelper.filterExpiredInstallmentOptions(ottOffers);
            // Filter compatible devices based on current base package
            if (!basePackageCompatibleList.isEmpty()  && ottProducts != null) {
                ottProducts = filterCompatibleProductsForCurrentBasePackage(basePackageCompatibleList, ottProducts);
            }
            if (ottProducts != null) {
                //ottOffers = filterForOfferClassificationType(ottOffers, offerRequestWrapper);
                processedCTProducts.addAll(ottProducts);
            }
        }
        finalCTProductResponse.setProducts(processedCTProducts);
       return finalCTProductResponse;
        //Filter products base
    }

    /**
     *
     * @param basePackageCompatibleList
     * @param processedCTProducts
     * @return
     */
    private List<ProductObj> filterCompatibleProductsForCurrentBasePackage(List<String> basePackageCompatibleList, List<ProductObj> processedCTProducts) {
        List<ProductObj> productObjs = new ArrayList<>();
        if (Objects.nonNull(processedCTProducts)) {
            processedCTProducts.stream().filter(Objects::nonNull).forEach(productObj -> {
                if (Objects.nonNull(productObj.getVariants()) && Objects.nonNull(productObj.getVariants().get(0))
                        && Objects.nonNull(productObj.getVariants().get(0).getAttributes())) {
                    if (basePackageCompatibleList.contains(productObj.getVariants().get(0).getAttributes().getBillingProductCode())) {
                        productObjs.add(productObj);
                    }

                }
            });
        }
        return productObjs;
    }

    /**
     *
     * @param ctProductResponse
     * @param subscriptionsContext
     * @param productRequestWrapper
     * @return
     */
    private List<ProductObj> filterCTProductResponse(CTProductResponse ctProductResponse, CustomerSubscriptionDetail subscriptionsContext,
                                                ProductRequestWrapper productRequestWrapper) {

        List<ProductObj> ottProducts = ctProductResponse.getProducts();

        return ottProducts
                .stream().filter(Objects::nonNull).filter(productObj -> Optional.ofNullable(productRequestWrapper).isPresent()
                        && (Optional.ofNullable(productObj).isPresent() &&
                        (Optional.ofNullable(productObj.getVariants()).isPresent() &&
                        !productObj.getVariants().isEmpty()
                        && isBaseProductNotOnCustomerAccount(productObj, subscriptionsContext))))
                .collect(Collectors.toList());

    }

    public boolean isBaseProductNotOnCustomerAccount(ProductObj productObj, CustomerSubscriptionDetail subscriptionsContext) {

        boolean isProductNotOnCustomerAccount = false;
        List<String> bundleProductCodes = new ArrayList<>();
        subscriptionsContext.getBasePackageServiceIdList()
                .stream()
                .forEach(serviceId -> bundleProductCodes.add(serviceId));
        if (!bundleProductCodes.contains(productObj.getVariants().get(0).getAttributes().getBillingProductCode())) {
            isProductNotOnCustomerAccount = true;
        } else {
           populateCurrentServiceInfo(productObj);
//            activePromoCatgory(offer, subscriptionsContext);
        }
        return isProductNotOnCustomerAccount;
    }
    private void populateCurrentServiceInfo(ProductObj productObj) {
        currentServiceInfo.put("productStatus", productObj.getVariants().get(0).getAttributes().getProductStatus());
        currentServiceInfo.put("packageGroup", productObj.getVariants().get(0).getAttributes().getPackageGroup());
    }
    private CustomerSubscriptionDetail populateCustomerSubscriptionDetail(ProductRequestWrapper productRequestWrapper) throws ParseException {


        CustomerSubscriptionDetail customerAccountDetail = new CustomerSubscriptionDetail();
        List<String> addOnServiceIdList = new ArrayList<>();
        List<String> basePackageServiceIdList = new ArrayList<>();
        List<CustomerServiceDetail> listAddOnServiceDetail = new ArrayList<>();
        List<CustomerServiceDetail> listBaseBackageServiceDetail = new ArrayList<>();
        String agentType = null;
        String isDefaultPromo = "false";
        CGResponse cGResponse = null;

        String agentHeader = productRequestWrapper.getAgentType();

        // For Products we don't have offerActionType
        customerAccountDetail.setUserType(ottServicesOffersProcessorHelper.findSalesChannel(productRequestWrapper.getProductRequest().getSalesChannel(), ""));

        if (Optional.ofNullable(agentHeader).isPresent() && !agentHeader.isEmpty()) {
            List<String> agentTypes = Arrays.asList(agentHeader.trim().split(","));
            for (String type : agentTypes)
                if (type.equalsIgnoreCase(SPECIAL)) {//could be VIEW,MODIFY,SPECIAL,ADJUST
                    agentType = type;
                }
        }

        customerAccountDetail.setAgentType(agentType);

        if (Optional.ofNullable(productRequestWrapper.getProductRequest().getCustomerContext()).isPresent() &&
                Optional.ofNullable(productRequestWrapper.getProductRequest().getCustomerContext().getOtt()).isPresent()) {

            isDefaultPromo = (productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getFreeTrialEligible() != null && productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getFreeTrialEligible() == true) ? "true" : "false";


            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT);
            if (Optional.ofNullable(productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getNextBillingDate()).isPresent()) {
                LocalDate nextBillDate = LocalDate.parse(productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getNextBillingDate(), formatter);
                Date nextBillingDate = java.util.Date.from(nextBillDate.atStartOfDay()
                        .atZone(ZoneId.systemDefault())
                        .toInstant());
                customerAccountDetail.setNextBillingDate(nextBillingDate);
            }
            String contractIndicator = null;
            if (Optional.ofNullable(productRequestWrapper.getProductRequest()).isPresent() &&
                   Optional.ofNullable(productRequestWrapper.getProductRequest()).isPresent()) {
                contractIndicator = productRequestWrapper.getProductRequest().getContractApplicable().get(0);
            }
            customerAccountDetail.setContractIndicator(contractIndicator);
            customerAccountDetail.setIsDefaultPromo(productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getIsDefaultPromo() != null &&
                    productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getIsDefaultPromo() ? "true" : "false");
            customerAccountDetail.setFreeTrialEligble(isDefaultPromo);

            String accountType = "";
            if(productRequestWrapper.isMobility())
            {
                accountType = Constants.MOBILITY_WITH_CAPITAL_M;
            }else{
                String customerAccountType = productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getAccountType();
                if(StringUtils.isNotBlank(customerAccountType))
                {
                    accountType = customerAccountType;
                }else{
                    accountType = Constants.RESIDENTIAL_ACCOUNT_TYPE;
                }
            }
            customerAccountDetail.setAccountType(accountType);

            for (ProductInfo product : productRequestWrapper.getProductRequest().getCustomerContext().getOtt().getProducts()) {
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
            }

        } else if ((StringUtils.isNotBlank(productRequestWrapper.getDtvnAccount()))) {

            cGResponse = customerGraphServiceImpl.getActiveSubscriptions(productRequestWrapper.getDtvnAccount(), featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP) ? "backup" : "primary");
            //Getting the list from eligible products
            CGServiceInfo[] cgServiceInfos = cGResponse.getServiceInfo();
            if (cgServiceInfos != null) {

                Date nextBillingDate = new Date(Long.parseLong(cGResponse.getBillingInfo().getNextBillingDateTime()));

                customerAccountDetail.setNextBillingDate(nextBillingDate);
                customerAccountDetail.setContractIndicator(cGResponse.getAccountInfo().isIsPremiumCustomer()? Constants.CONTRACT : Constants.NONCONTRACT);
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

                                promoMap.put(Constants.PROMO_START_DATE, OffersUtils.convertLongDateToStringDate(cGpromo.getStartDate()));
                                promoMap.put(Constants.PROMO_END_DATE, OffersUtils.convertLongDateToStringDate(cGpromo.getEndDate()));
                                promoMap.put(Constants.IS_DEFAULT_PROMO, cGpromo.getIsDefaultPromo());
                                if ("true".equalsIgnoreCase(cGpromo.getIsDefaultPromo())) {
                                    isDefaultPromo = "true";
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

                                promoMap.put(Constants.PROMO_START_DATE, OffersUtils.convertLongDateToStringDate(cGpromo.getStartDate()));
                                promoMap.put(Constants.PROMO_END_DATE, OffersUtils.convertLongDateToStringDate(cGpromo.getEndDate()));
                                promoMap.put(Constants.IS_DEFAULT_PROMO, cGpromo.getIsDefaultPromo());
                                if ("true".equalsIgnoreCase(cGpromo.getIsDefaultPromo())) {
                                    isDefaultPromo = "true";
                                }
                                customerServiceDetail.getAccountPromoStartEndDate().put(cGpromo.getPromotionId(), promoMap);
                            }
                        }
                        customerServiceDetail.setPromosId(customerServiceDetail.getAccountPromoStartEndDate().keySet().stream().collect(Collectors.toList()));
                        listAddOnServiceDetail.add(customerServiceDetail);
                    }
                }
            }
        }

        customerAccountDetail.setIsDefaultPromo(isDefaultPromo);
        customerAccountDetail.setAddOnServiceIdList(addOnServiceIdList);
        customerAccountDetail.setListAddOnServiceDetail(listAddOnServiceDetail);
        customerAccountDetail.setBasePackageServiceIdList(basePackageServiceIdList);
        customerAccountDetail.setListBaseBackageServiceDetail(listBaseBackageServiceDetail);

        return customerAccountDetail;
    }

    private ProductObj createAvailableBasePackageForDTVNow(ProductObj productObj,
                                                        CustomerSubscriptionDetail customerSubscriptionDetail, Map<String, String> availablePackageGroup) {
        ProductObj processedProductObj = null;
        //TODO check valid accountType as products are not having that value, customer Segments
        List<String> accountType = productObj.getVariants().get(0).getAttributes().getCustomerSegments() !=null ?
                                            productObj.getVariants().get(0).getAttributes().getCustomerSegments() :Arrays.asList(customerSubscriptionDetail.getAccountType());
        String salesChannel = ottServicesOffersProcessorHelper.findSalesChannel(productObj.getVariants().get(0).getAttributes().getSalesChannel() != null ?
                                                                    productObj.getVariants().get(0).getAttributes().getSalesChannel() : Arrays.asList(Constants.ALL),
                //TODO offer Acttion Type is not available for products so for now defaulting to ACQUISITION
                Constants.ACQUISITION_ACTION_TYPE);
        List<ProductWrapper> cpopProducts = null;

                processedProductObj = filterProductBaseOnPackageGroup(productObj, customerSubscriptionDetail, availablePackageGroup,
                        salesChannel, null);

        return processedProductObj;
    }
    private ProductObj filterProductBaseOnPackageGroup(ProductObj productObj, CustomerSubscriptionDetail customerSubscriptionDetail,
                                                    Map<String, String> availablePackageGroup, String salesChannel,
                                                    ProductWrapper product) {
            Long packageGroup = null;
            Attributes attributes = productObj.getVariants().get(0) !=null && productObj.getVariants().get(0).getAttributes() !=null ? productObj.getVariants().get(0).getAttributes() : null;//;prod.getObj().getVariants().get(0).getAttributes();
            if (attributes.getPackageGroup() != null) {
                packageGroup = (Long.parseLong(attributes.getPackageGroup()));
            }

            String availableGroup = null;
            if (availablePackageGroup != null && availablePackageGroup.size() > 0
                    && availablePackageGroup.get(Constants.AVAILABE_PACKAGE_GROUP) != null) {
                availableGroup = availablePackageGroup.get(Constants.AVAILABE_PACKAGE_GROUP);
            }
            if ((packageGroup != null
                    && availableGroup != null
                    && availableGroup.contains(String.valueOf(packageGroup))
            ) &&
                    // && (accountType != null
                    (CollectionUtils.isEmpty(customerSubscriptionDetail.getBasePackageServiceIdList())
                    || !customerSubscriptionDetail.getBasePackageServiceIdList()
                    .contains(attributes.getBillingProductCode()))
                    // && customerSubscriptionDetail.getAccountType().equalsIgnoreCase(accountType)

                    //should we validate channel
                   // && (salesChannel != null
//                    && (customerSubscriptionDetail.isAgent()
//                    && isSalesChannelValidForAgent(salesChannel))
//                    || isSalesChannelValidForCustomer(salesChannel))
            ) {

                setBenefitContinuation(productObj, customerSubscriptionDetail, null, attributes, salesChannel);
                return productObj;
            }
       // }
        return null;
    }
//    public boolean isSalesChannelValidForCustomer(String salesChannel) {
//        List<String> salesChannelForCustomer = salesChannel != null ? Arrays.asList(salesChannel.split(Constants.PIPE))
//                : new ArrayList<>();
//        return salesChannelForCustomer.stream()
//                .anyMatch(s -> s.equalsIgnoreCase(Constants.SELF_SERVICE_EXISTING) || s.equalsIgnoreCase(Constants.SALES_CHANNEL_ALL));
//    }
//
//
//    public boolean isSalesChannelValidForAgent(String salesChannel) {
//        List<String> salesChannelForAgent = salesChannel != null ? Arrays.asList(salesChannel.split(Constants.PIPE))
//                : new ArrayList<>();
//        return salesChannelForAgent.stream()
//                .anyMatch(s -> s.equalsIgnoreCase(Constants.AGENT_EXISTING) || s.equalsIgnoreCase(Constants.SALES_CHANNEL_ALL));
//    }
    private void setBenefitContinuation(ProductObj productObj, CustomerSubscriptionDetail customerSubscriptionDetail,
                                        Product prod, Attributes attributes, String salesChannel) {

        String billingProductCode = getBillingProductCodeFromOffer(productObj);
        CtProductInfo productInfo;
        productInfo = new CtProductInfo();
        if (attributes.getServiceFlag() != null && attributes.getServiceFlag()) {
            productInfo.setPackageType(BASIC_SERVICE);
        }

        Map<String, Boolean> freePromoMap = new HashMap<>();
        if (
                // Optional.ofNullable(prod).isPresent() &&
                        Optional.ofNullable(productObj).isPresent()
        ) {
            productInfo.setStatus(attributes.getProductStatus());
            // setBaseAndContractPrice(prod, productInfo);
            List<Benefit> eligbleBenefitList = eligiblePromosList(productObj, customerSubscriptionDetail, productInfo, salesChannel, freePromoMap,billingProductCode);

            List<String> eligbleBenefitcode = new ArrayList<>();
            if(!CollectionUtils.isEmpty(eligbleBenefitList)) {
                eligbleBenefitList.forEach(benefit -> {
                    eligbleBenefitcode.add(benefit.getBillingBenefitCode());
                });
            }
            String eligbleBenefitStr = String.join(",",  eligbleBenefitcode);
            productObj.setBenefitContinuation(eligbleBenefitStr);

        }

    }

    private String getBillingProductCodeFromOffer(ProductObj productObj) {
        String billingProductCode = "";
        if ( Optional.ofNullable(productObj).isPresent() && Optional.ofNullable(productObj.getVariants()).isPresent() &&
                Optional.ofNullable(productObj.getVariants().get(0)).isPresent() &&
                Optional.ofNullable(productObj.getVariants().get(0).getAttributes()).isPresent() &&
                Optional.ofNullable(productObj.getVariants().get(0).getAttributes().getBillingProductCode()).isPresent()) {

            billingProductCode  = productObj.getVariants().get(0).getAttributes().getBillingProductCode();
        }

        return billingProductCode;
    }
    private List<Benefit> eligiblePromosList(ProductObj productObj, CustomerSubscriptionDetail subscriptionDetail,
                                             com.dtv.dcp.epoch.processor.ott.services.model.CtProductInfo productInfo, String salesChannel, Map<String, Boolean> freePromoMap, String billingProductCode) {
        Set<Benefit> applicablePromosSet = new HashSet<>();
        List<String> activePromoIdsInCT = new ArrayList<>();

        List<CustomerServiceDetail> listBaseBackageServiceDetail = subscriptionDetail.getListBaseBackageServiceDetail();
        Map<String, Map<String, String>> accountPromoStartEndDate = !listBaseBackageServiceDetail.isEmpty()
                ? listBaseBackageServiceDetail.get(0).getAccountPromoStartEndDate()
                : new HashMap<>(); // this map contains customer's account promoId and their start and date date

        Set<String> activePromos = activePromoList(accountPromoStartEndDate);// return a set of all active promotions of
        List<Benefit> activeBenefit = new ArrayList<>();


        activePromos.stream().filter(Objects::nonNull).forEach(promo -> {
            try {
                promoContinuationUpgradeDowngrade(subscriptionDetail, promo, applicablePromosSet, productInfo, billingProductCode);
            } catch (ParseException e) {
                log.error("Error in method eligiblePromosList() method", e);
            }
        });


        if ("true".equalsIgnoreCase(customerSubscriptionDetail.getIsDefaultPromo()) || "true".equalsIgnoreCase(customerSubscriptionDetail.getFreeTrialEligble())) {
//            activePromoIdsInCT.stream().filter(Objects::nonNull).forEach(activePromoIdInCT -> {
//                addEligiblePromos(activeBenefit, applicablePromosSet, salesChannel, freePromoMap, billingProductCode);
//            });
        }


        if (customerSubscriptionDetail.isAgent()) {
            activeBenefit.stream().filter(Objects::nonNull).forEach(benefit -> {
                if ((benefit.isAgentOffer() || benefit.isIoOffer()) && ottServicesOffersProcessorHelper.isSalesChannelValidForAgent(salesChannel)) {
                    applicablePromosSet.add(benefit);
                }
            });
        }

        return new ArrayList<>(applicablePromosSet);
    }

    private void promoContinuationUpgradeDowngrade(CustomerSubscriptionDetail context, String promo,
                                                   Set<Benefit> applicablePromosSet, CtProductInfo productInfo,String billingProductCode) throws ParseException {
        // Getting Benefit details of the Benefit Continuation Promo
        Benefit benefit =  getBenefitByBillingBenfitCode(promo);
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

    private Benefit getBenefitByBillingBenfitCode(String benefitCode) {
        Benefit benefit = null;

        if(StringUtils.isNotEmpty(benefitCode)) {
            BenefitRequestWrapper benefitRequestWrapper = new BenefitRequestWrapper();
            BenefitRequest benefitRequest = new BenefitRequest();
            benefitRequest.setBenefitCodes(Stream.of(benefitCode).collect(Collectors.toList()));
            benefitRequestWrapper.setBenefitRequest(benefitRequest);

            CTBenefitsResponse ctBenefitsResponse = ottServicesBenefitsProcessor.getBenefits(benefitRequestWrapper);

            if(Objects.nonNull(ctBenefitsResponse) && org.apache.commons.collections.CollectionUtils.isNotEmpty(ctBenefitsResponse.getBenefits())) {
                benefit = ctBenefitsResponse.getBenefits().get(0);
            }
        }
        return benefit;
    }

    /**
     * It's only taking from the request
     * @param accountPromoStartEndDate
     * @return
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
                	Date startDate = startAndEdnDate.get(Constants.PROMO_START_DATE);
                    if(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_PST_TIME_LOGIC_ENABLED)) {
                    	startDate = new SimpleDateFormat(Constants.DATE_TIME_FORMAT)
                                .parse(sdf.format(startAndEdnDate.get(Constants.PROMO_START_DATE)));
                    }
                    Date date = new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(new Date()));

                    if (date != null && startDate.before(date)) {
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
                    log.error("Invalid date present in the request - activePromoList " + e);
                }
            } else {
                acivePromos.add(promo);
            }
        });

        return acivePromos;
    }

//    public void addEligiblePromos(List<Benefit> benefits,Set<Benefit> applicablePromosSet, String salesChannel, Map<String, Boolean> freePromoMap, String billingProductCode) {
//
//        if (!CollectionUtils.isEmpty(benefits)) {
//            for (Benefit benefit : benefits) {
//                String promoType = benefit.getBenefitType();
//                String promotionCategory = benefit.getBenefitCategory();
//                String selfPromo = "PRODUCT".equalsIgnoreCase(benefit.getBenefitCategory()) ? "T" : "F";
//                boolean freeTrialPromo = benefit.isFreeTrialPromo();
//                String promoRateType = "";
//                if (null != benefit.getContractType()) {
//                    promoRateType = benefit.getContractType();
//                }
//                if (promotionCategory.equalsIgnoreCase("product") && selfPromo.equalsIgnoreCase(Constants.TRUE) && freeTrialPromo) {
//                    if (customerSubscriptionDetail.isAgent()) {
//                        if (salesChannel != null && ottServicesOffersProcessorHelper.isSalesChannelValidForAgent(salesChannel) && validatePromoContractIndicator(promoRateType, customerSubscriptionDetail.isContractIndicator())) {
//
//                            applicablePromosSet.add(benefit);
//                            if (promoType.equalsIgnoreCase("free")) {
//                                freePromoMap.put(billingProductCode, Boolean.TRUE);
//                            }
//                        }
//                    } else if (ottServicesOffersProcessorHelper.isSalesChannelValidForCustomer(salesChannel)
//                            && validatePromoContractIndicator(promoRateType, customerSubscriptionDetail.isContractIndicator())) {
//                        applicablePromosSet.add(benefit);
//                        if (promoRateType.equalsIgnoreCase("free")) {
//                            freePromoMap.put(billingProductCode, Boolean.TRUE);
//                        }
//                    }
//                }
//            }
//        }
//    }

//    public boolean validatePromoContractIndicator(String promoRateType, boolean contractIndicator) {
//        boolean result = false;
//        if ((contractIndicator && (promoRateType.equalsIgnoreCase("All") || promoRateType.equalsIgnoreCase("Contract")))
//                || (!contractIndicator
//                && (promoRateType.equalsIgnoreCase("All") || promoRateType.equalsIgnoreCase("Retail")))) {
//            result = true;
//
//        }
//        return result;
//    }

    private List<ProductWrapper> getBasePackageCompatibleListFromCT(String billingProductCode, boolean isMobility, boolean isEmployeeAccount,List<String> businessSegment) {
        List<ProductWrapper> productWrappers = null;
        CTProductRequest ctProductRequest = new CTProductRequest();
        ctProductRequest.setBusinessSegment(businessSegment);
        ctProductRequest.setProductCodes(Stream.of(billingProductCode).collect(Collectors.toList()));

        CTProductResponse ctProductResponse = cpopClientHelper.getProducts(ctProductRequest);

        if (Objects.nonNull(ctProductResponse) && Objects.nonNull(ctProductResponse.getProducts()) && ctProductResponse.getProducts().size() > 0)
            if (isMobility) {
                productWrappers = ctProductResponse.getProducts().get(0).getVariants().get(0).getAttributes().getCompatibleMobilityProducts();
            }else if(isEmployeeAccount){
                productWrappers = ctProductResponse.getProducts().get(0).getVariants().get(0).getAttributes().getCompatibleEmployeeProducts();
            }else {
                productWrappers = ctProductResponse.getProducts().get(0).getVariants().get(0).getAttributes().getCompatibleProducts();
            }

        return productWrappers;
    }

    public List<ProductObj> retrieveAvailableAddOns(ProductRequestWrapper productRequestWrapper) {

        CTProductResponse ctProductResponse = cpopProductsHelper.getVideoAddonProducts(productRequestWrapper);
        // filterActiveOffers(offerResponseAddOn);
        return ctProductResponse.getProducts();
    }
    public List<ProductObj> retrieveEquipmentProducts(ProductRequestWrapper productRequestWrapper) {

        CTProductResponse ctProductResponse = cpopProductsHelper.getEquipmentsProducts(productRequestWrapper);
        // filterActiveOffers(offerResponseAddOn);
        return ctProductResponse.getProducts();
    }
    public List<ProductObj> retrieveProtectionPlanProduct(ProductRequestWrapper productRequestWrapper) {

        CTProductResponse ctProductResponse = cpopProductsHelper.getProtecionPlanProducts(productRequestWrapper);
        return ctProductResponse.getProducts();
    }
}
