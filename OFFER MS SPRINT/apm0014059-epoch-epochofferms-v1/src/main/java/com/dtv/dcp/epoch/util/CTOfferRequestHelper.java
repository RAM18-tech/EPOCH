package com.dtv.dcp.epoch.util;


import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.CpopConstants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.model.common.request.*;
import com.dtv.dcp.epoch.model.ct.request.*;
import com.dtv.dcp.epoch.model.customergraph.response.CGAccountProducts;
import com.dtv.dcp.epoch.model.customergraph.response.CGResponse;
import com.dtv.dcp.epoch.model.customergraph.response.CGServiceInfo;
import com.dtv.dcp.epoch.model.customergraph.response.UVAccountProductsResponse;
import com.dtv.dcp.epoch.service.DMALookUpService;
import com.dtv.dcp.epoch.service.customergraph.CustomerGraphService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@Component
public class CTOfferRequestHelper {

    private static final Logger log = LoggerFactory.getLogger(CTOfferRequestHelper.class);

    @Autowired
    private CustomerGraphService customerGraphService;

    @Autowired
    private FeatureManagerHelper featureHelper;

    @Autowired
    CustomerGraphProductsHelper customerGraphHelper;

    @Autowired
    OffersUtils offersUtils;

    @Autowired
    private RedisCacheHelper redisCacheHelper;

    @Autowired
    private DMALookUpService dmaLookUpService;

    /**
     * Pre-processes the offer request and headers to build an OfferRequestWrapper.
     *
     * @param headers       HTTP headers containing request context
     * @param offersRequest the offer request object
     * @return OfferRequestWrapper containing processed request data
     */
    public OfferRequestWrapper preProcessOfferRequest(HttpHeaders headers, OfferRequest offersRequest) {
        // SLS-IXP-FLAG changes
        if((offersUtils.isSlsGetOfferSalesEnabled() || offersUtils.isSlsGetOfferServicesEnabled()) && (Optional.ofNullable(offersRequest.getOfferProductFamily()).isPresent() && !offersRequest.getOfferProductFamily().isEmpty() && !offersRequest.getOfferProductFamily().contains(null) && (offersRequest.getOfferProductFamily().stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))))
                && ((offersUtils.getIapSalesChannels(offersRequest) !=  null && "Y".equalsIgnoreCase(offersUtils.getIapSalesChannels(offersRequest)))
                || (offersRequest.getCustomerContext() != null && offersRequest.getCustomerContext().getOtt() != null && offersRequest.getCustomerContext().getOtt().getIapPartnerAccountType() != null))
                && (Constants.GENRE).equalsIgnoreCase(offersRequest.getContractIndicator().get(0))){
            offersRequest.setContractIndicator(Arrays.asList(Constants.ROAD_RUNNER));
            offersRequest.setOriginalContractIndicator(Constants.GENRE);
            offersRequest.setCustomerSubscriptionType(Constants.GENRE);
            offersRequest.setIapSalesChannelIsPresent("Y");
        }
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        PartnerDealerDetails partnerDealerDetails = new PartnerDealerDetails();
        AtomicBoolean isExistingSatelliteNonCouponFlow = new AtomicBoolean(false);

        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        //setting Flow as getOffers
        offerRequestWrapper.setFlow(Constants.FLOW_TYPE_GET_OFFERS);
        offerRequestWrapper.setLoggedInId(headers.getFirst(Constants.IDPCTX_LOGGEDINID));
        offerRequestWrapper.setSessionId(headers.getFirst(Constants.IDPCTX_SESSION_ID));
        offerRequestWrapper.setAgentType(headers.getFirst(Constants.IDPCTX_AGENT_TYPE));
        offerRequestWrapper.setPartnerType(headers.getFirst(Constants.IDPCTX_PARTNER_TYPE));
        offerRequestWrapper.setCurrentDate(headers.getFirst(Constants.CURRENT_DATE));

        if (StringUtils.isNotEmpty(offersRequest.getMode())){
            offerRequestWrapper.setFlow(offersRequest.getMode());
        }

        if (Objects.isNull(offersRequest.getServerDate()) && Objects.nonNull(headers.getFirst(Constants.SERVER_DATE))) {
            log.info("server Date::::{}", headers.getFirst(Constants.SERVER_DATE));
            offersRequest.setServerDate(headers.getFirst(Constants.SERVER_DATE));
        }

        List<String> serverDateGlobalConfig = redisCacheHelper.getValues(Constants.SERVER_DATE, Constants.SATELLITE_PRODUCT_FAMILY);
        if (Objects.isNull(offersRequest.getServerDate()) && CollectionUtils.isNotEmpty(serverDateGlobalConfig)) {
            offersRequest.setServerDate(serverDateGlobalConfig.get(0));
        }

        if (offersRequest.getSalesChannel().contains(Constants.OPUS)
                && (Objects.nonNull(offersRequest.getContractIndicator())
                && !offersRequest.getContractIndicator().contains(Constants.EDSP_STRING)
                && offersRequest.getContractIndicator().contains(Constants.TAZBYOD_STRING))
                && offersRequest.isReconnectCustomer()
                && !offersUtils.isDateWithinEligibilityWindow(offersRequest.getServiceEndDate(),
                offersRequest.getSalesChannel(), offersRequest.getOfferProductType(),
                offersRequest.getOfferCodes())) {
            if (offersRequest.getContractIndicator().contains(Constants.TAZ_STRING)) {
                offersRequest.setContractIndicator(Arrays.asList(Constants.TAZ_STRING));
            }
        }


        //setting contract Indicator for TAZ and TAZBYOD when contract indicator is null
        //  offersUtils.setContractorIndicatorForTAZ(offersRequest);

        // Added for the US# 158528 (Longhorn Long Term) - started
        partnerDealerDetails.setPartnerDealerCode1(headers.getFirst(Constants.IDPCTX_PARTNER_DEALER_CODE1));
        partnerDealerDetails.setPartnerDealerCode2(headers.getFirst(Constants.IDPCTX_PARTNER_DEALER_CODE2));
        partnerDealerDetails.setPartnerLocationId(headers.getFirst(Constants.IDPCTX_PARTNER_LOCATION_ID));

        if (Optional.ofNullable(offersRequest.getOfferProductFamily()).isPresent() && !offersRequest.getOfferProductFamily().isEmpty() && !offersRequest.getOfferProductFamily().contains(null)) {
            List<String> offerProductFamily = offersRequest.getOfferProductFamily();

            if (offerProductFamily.stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
                if ((StringUtils.equalsIgnoreCase(Constants.SALES_CRM, offersRequest.getSalesChannel().get(0)))
                        || (StringUtils.equalsIgnoreCase(Constants.CCAP, offersRequest.getSalesChannel().get(0)))) {

                    offersRequest.getSalesChannel().add(0, Constants.OPUS);

                    offersRequest.setChannelEligibility(null);
                }
            }
        }

        if (offersUtils.isPerformanceUpdatesEnabled(offersRequest.getSalesChannel())) {
            offersRequest.setPartnerDealerDetails(partnerDealerDetails);
            if (StringUtils.isBlank(partnerDealerDetails.getPartnerDealerCode1())
                    && StringUtils.isBlank(partnerDealerDetails.getPartnerDealerCode2())) {
                ctOfferRequest.setPartnerDealerDetails(new PartnerDealerDetails());
            }
        } else {
            ctOfferRequest.setPartnerDealerDetails(partnerDealerDetails);
        }
        if (Optional.ofNullable(offersRequest.getAgentDealerDetails()).isPresent()
                && Objects.nonNull(offersRequest.getAgentDealerDetails())) {
            ctOfferRequest.setAgentDealerDetails(offersRequest.getAgentDealerDetails());
        }
        // Added for the US# 158528 (Longhorn Long Term) - End

        if (Optional.ofNullable(offersRequest.getOnlinePartnerDetails()).isPresent()
                && Objects.nonNull(offersRequest.getOnlinePartnerDetails())) {
            ctOfferRequest.setOnlinePartnerDetails(offersRequest.getOnlinePartnerDetails());
        }

        if (Objects.nonNull(headers.getFirst(Constants.IDPCTX_AUTHORIZEDACCOUNTS_WIRELESS))) {
            offerRequestWrapper.setCartModeMobility(true);
        }
        List<CTCustomerContext> customerContext = new ArrayList<>();
        List<String> existingProdIntentList = null;

        if (Optional.ofNullable(offersRequest.getSalesChannel()).isPresent() && !offersRequest.getSalesChannel().isEmpty() && StringUtils.equalsIgnoreCase(Constants.INDIRECT_PARTNER, offersRequest.getSalesChannel().get(0))) {
            offerRequestWrapper.setPartner(true);
        }

        if (Optional.ofNullable(offersRequest.getSalesChannel()).isPresent()
                && !offersRequest.getSalesChannel().isEmpty()
                && StringUtils.equalsIgnoreCase(Constants.DIRECTV_ONLINE, offersRequest.getSalesChannel().get(0))) {
            offerRequestWrapper.setDirectvOnline(true);
        }

        // Check for any specific existing customer intent
        if (Objects.nonNull(offersRequest.getCustomerIntent()) && !offersRequest.getCustomerIntent().isEmpty()) {
            List<String> existingProductIntentList = new ArrayList<>();
            offersRequest.getCustomerIntent().stream().filter(Objects::nonNull)
                    .filter(existProduct -> ("existing".equalsIgnoreCase(existProduct.getProductStatus())))
                    .filter(existProduct -> (existingProductIntentList.add(existProduct.getProductFamily().toLowerCase())))
                    .collect(Collectors.toList());
            existingProdIntentList = existingProductIntentList;
        }

        // Check CustomerContext for Existing Services
        if (Objects.nonNull(offersRequest) && Objects.nonNull(offersRequest.getCustomerContext()) &&
                Objects.nonNull(offersRequest.getCustomerContext().getExistingProductFamily()) && !offersRequest.getCustomerContext().getExistingProductFamily().isEmpty()) {

            Map<String, CartProduct> customerContextMap = getCustomerContextMap(offersRequest.getCustomerContext());
            List<String> existingProductIntentList = existingProdIntentList;

            if (Objects.nonNull(offersRequest.getCustomerContext().getAdditionalAccountDetails()) && Objects.nonNull(offersRequest.getCustomerContext().getAdditionalAccountDetails().getStatus())
                    && offersRequest.getCustomerContext().getAdditionalAccountDetails().getStatus().equalsIgnoreCase(Constants.SUSPEND)
                    && Objects.nonNull(offersRequest.getCustomerContext().getOtt())) {
                offersRequest.getCustomerContext().getOtt().setNextBillingDate(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            }

            offersRequest.getCustomerContext().getExistingProductFamily().stream().filter(StringUtils::isNotBlank).forEach(existingProdFamily -> {

                // If Specific Customer Intent is coming, then only add those product families which are part of existing customer intent
                if (CollectionUtils.isEmpty(existingProductIntentList) || existingProductIntentList.contains(existingProdFamily.toLowerCase())) {

                    CartProduct existingProduct = null;
                    if (Objects.nonNull(customerContextMap.get(existingProdFamily.toLowerCase())) && customerContextMap.get(existingProdFamily.toLowerCase()).getIsActive()) {
                        existingProduct = customerContextMap.get(existingProdFamily.toLowerCase());
                    }

                    if (Objects.nonNull(existingProduct)) {
                        if (existingProdFamily.equalsIgnoreCase(Constants.OTT_PRODUCT_FAMILY)) {
                            offerRequestWrapper.setDtvnAccount(existingProduct.getAccountNumber());
                        } else if (existingProdFamily.equalsIgnoreCase(Constants.WIRELESS_PRODUCT_FAMILY)) {
                            offerRequestWrapper.setMobility(true);
                            offerRequestWrapper.setAuthAccountsWireless(existingProduct.getAccountNumber());
                        } else {
                            offerRequestWrapper.setLinkedUverseAccountNums(existingProduct.getAccountNumber());
                        }

                        CTCustomerContext ctCustomerContext = new CTCustomerContext();
                        ctCustomerContext.setProductFamily(existingProdFamily);
                        populateProducts(ctCustomerContext, existingProduct);

                        if (null != offersRequest.getCustomerContext() && null != offersRequest.getCustomerContext().getSatellite() &&
                                existingProdFamily.equalsIgnoreCase(Constants.SATELLITE_PRODUCT_FAMILY)) {
                            ctCustomerContext.setBusinessSegment(offersRequest.getCustomerContext().getSatellite().getBusinessSegment());
                            ctCustomerContext.setEmployeeSegment(offersRequest.getCustomerContext().getSatellite().getEmployeeSegment());
                            ctCustomerContext.setNftvFlag(offersRequest.getCustomerContext().getSatellite().getNftvFlag());
                            ctCustomerContext.setFtvCount(offersRequest.getCustomerContext().getSatellite().getFtvCount());
                            ctCustomerContext.setPolicy(offersRequest.getCustomerContext().getSatellite().getPolicy());
                            ctCustomerContext.setDtvSwimlane(offersRequest.getCustomerContext().getSatellite().getDtvSwimlane());
                            ctCustomerContext.setServiceOrderDate(offersRequest.getCustomerContext().getSatellite().getServiceOrderDate());
                            ctCustomerContext.setRemoteReplacementType(offersRequest.getCustomerContext().getSatellite().getRemoteReplacementType());
                            ctCustomerContext.setPartnerName(offersRequest.getCustomerContext().getSatellite().getPartnerName());

                            isExistingSatelliteNonCouponFlow.set(!Optional.ofNullable(offersRequest.getOfferTypes()).orElse(Collections.emptyList()).contains("coupon"));
                        }

                        customerContext.add(ctCustomerContext);
                    }
                }
            });
            if (offersRequest.getCustomerContext().getOtt() != null
                    && Constants.EMPLOYEE.equalsIgnoreCase(offersRequest.getCustomerContext().getOtt().getAccountType())) {
                offerRequestWrapper.setEmployeeAccount(true);
            }
        }


        // If ATTTV account info not coming in customer context , but header  has account number
        if (StringUtils.isEmpty(offerRequestWrapper.getDtvnAccount()) &&
                Objects.nonNull(headers.getFirst(Constants.IDPCTX_DTVN_ACCOUNT)) &&
                !offersRequest.getOfferProductFamily().stream()
                        .allMatch(Predicate.isEqual(Constants.WIRELESS_PRODUCT_FAMILY))

        ) {

            // If Specific Customer Intent is coming, then only add those product families which are part of existing customer intent
            if (Objects.isNull(existingProdIntentList) || existingProdIntentList.contains(Constants.OTT_PRODUCT_FAMILY.toLowerCase())) {

                offerRequestWrapper.setDtvnAccount(OffersUtils.getFirstAccount(headers.getFirst(Constants.IDPCTX_DTVN_ACCOUNT)));

                // Make CG call here and set customerContext in CT Offer Request
                CGResponse cgOTTResponse = getCGOTTProductDetailsByAccountId(offerRequestWrapper.getDtvnAccount());
                List<CGServiceInfo> cgOttProductInfos;
                List<Product> products = new ArrayList<>();
                CTCustomerContext ctCustomerContext = new CTCustomerContext();
                if (Optional.ofNullable(cgOTTResponse).isPresent()) {
                    cgOttProductInfos = Arrays.asList(cgOTTResponse.getServiceInfo());
                    cgOttProductInfos.stream().filter(Objects::nonNull).collect(Collectors.toList());
                    cgOttProductInfos.forEach(product -> {
                        Product ctProduct = new Product();
                        ctProduct.setProductCode(product.getServiceID());
                        ctProduct.setProductType(product.getTypeOfPlan());
                        products.add(ctProduct);
                    });
                    ctCustomerContext.setProductFamily(CpopConstants.OTT);
                    ctCustomerContext.setProducts(products);
                    customerContext.add(ctCustomerContext);
                    offerRequestWrapper.setEmployeeAccount(Constants.EMPLOYEE.equalsIgnoreCase(OffersUtils.getUverseCustomerAccountType(cgOTTResponse)));
                }
            }
        }

        // If Uverse account info not coming in customer context , but header has account number
        // this block is not required for OTT services flow or sattelite
        if (StringUtils.isEmpty(offerRequestWrapper.getLinkedUverseAccountNums()) &&
                StringUtils.isNotEmpty(headers.getFirst(Constants.IDPCTX_LINKEDUVERSEACCNUMS)) &&
                !offersUtils.isOTTService(offersRequest) &&
                !(offersRequest.getOfferProductFamily().contains(Constants.SATELLITE_PRODUCT_FAMILY) &&
                        Optional.ofNullable(offersRequest.getOfferActionType()).isPresent() &&
                        offersRequest.getOfferActionType().contains(Constants.RETENTION_ACTION_TYPE)) &&
                !offersRequest.getOfferProductFamily().stream()
                        .allMatch(Predicate.isEqual(Constants.WIRELESS_PRODUCT_FAMILY))
        ) {
            // If Specific Customer Intent is coming, then only add those
            // product families which are part of existing customer intent
            if (Objects.isNull(existingProdIntentList)
                    || existingProdIntentList.contains(Constants.IPTV_PRODUCT_FAMILY.toLowerCase())
                    || existingProdIntentList.contains(Constants.BB_PRODUCT_FAMILY)
                    || existingProdIntentList.contains(Constants.SATELLITE_PRODUCT_FAMILY)) {
                offerRequestWrapper
                        .setLinkedUverseAccountNums(OffersUtils.getFirstAccount(headers.getFirst(Constants.IDPCTX_LINKEDUVERSEACCNUMS)));

                // Make Profile/CG call here and set customerContext in CT Offer Request
                Map<String, List<com.dtv.dcp.epoch.model.customergraph.response.Product>> uverseProducts = getUverseProductDetalsByAccountId(
                        offerRequestWrapper.getLinkedUverseAccountNums());
                List<List<com.dtv.dcp.epoch.model.customergraph.response.Product>> hsiaProducts = uverseProducts.entrySet().stream().filter(x -> x.getKey().equals("HSIA")).map(x -> x.getValue())
                        .collect(Collectors.toList());
                List<List<com.dtv.dcp.epoch.model.customergraph.response.Product>> iptvProducts = uverseProducts.entrySet().stream().filter(x -> x.getKey().equals("IPTV")).map(x -> x.getValue())
                        .collect(Collectors.toList());
                List<List<com.dtv.dcp.epoch.model.customergraph.response.Product>> dtvsProducts = uverseProducts.entrySet().stream().filter(x -> x.getKey().equals("DTVS")).map(x -> x.getValue())
                        .collect(Collectors.toList());

                hsiaProducts.forEach(hsiaProduct -> {
                    CTCustomerContext ctCustomerContext = new CTCustomerContext();
                    List<Product> products = new ArrayList<>();
                    List<com.dtv.dcp.epoch.model.customergraph.response.Product> prods = hsiaProduct;
                    prods.forEach(prod -> {
                        Product product = new Product();
                        product.setProductCode(prod.getProductBillingCode());
                        product.setProductType(prod.getProductType());
                        products.add(product);
                    });
                    ctCustomerContext.setProducts(products);
                    ctCustomerContext.setProductFamily("broadband");
                    customerContext.add(ctCustomerContext);
                });
                iptvProducts.forEach(iptvProduct -> {
                    CTCustomerContext ctCustomerContext = new CTCustomerContext();
                    List<Product> products = new ArrayList<>();
                    List<com.dtv.dcp.epoch.model.customergraph.response.Product> prods = iptvProduct;
                    prods.forEach(prod -> {
                        Product product = new Product();
                        product.setProductCode(prod.getProductBillingCode());
                        product.setProductType(prod.getProductType());
                        products.add(product);
                    });
                    ctCustomerContext.setProducts(products);
                    ctCustomerContext.setProductFamily("IPTV");
                    customerContext.add(ctCustomerContext);
                });
                dtvsProducts.forEach(dtvsProduct -> {
                    CTCustomerContext ctCustomerContext = new CTCustomerContext();
                    List<Product> products = new ArrayList<>();
                    List<com.dtv.dcp.epoch.model.customergraph.response.Product> prods = dtvsProduct;
                    prods.forEach(prod -> {
                        Product product = new Product();
                        product.setProductCode(prod.getProductBillingCode());
                        product.setProductType(prod.getProductType());
                        products.add(product);
                    });
                    ctCustomerContext.setProducts(products);
                    ctCustomerContext.setProductFamily("satellite");
                    customerContext.add(ctCustomerContext);
                });
            }
        }

        if (!featureHelper.isEnabled(CpopConstants.EPOCH_HBO_MAX_PERM_SOLUTION)) {
            // Set customerContext only for Acquisition flows.
            if (CollectionUtils.isNotEmpty(customerContext) &&
                    ((
                            Objects.nonNull(offersRequest.getOfferActionType())
                                    && offersRequest.getOfferActionType().contains(Constants.ACQUISITION)
                    )
                            || isExistingSatelliteNonCouponFlow.get())) {
                ctOfferRequest.setCustomerContext(customerContext);
            }
        } else {
            // Set customerContext for all flows
            if (CollectionUtils.isNotEmpty(customerContext) && Objects.nonNull(offersRequest.getOfferActionType())) {
                ctOfferRequest.setCustomerContext(customerContext);
            }
        }
        // Set Customer Eligibility
        if (Objects.nonNull(offersRequest.getCustomerEligibility())) {
            ctOfferRequest.setCustomerEligibility(getCustomerEligibilityRequest(offersRequest.getCustomerEligibility()));
        }

        // Set cart Context
        if (Optional.ofNullable(offersRequest.getCartContext()).isPresent()) {
            ctOfferRequest.setCartContext(getCTCartContextRequest(offersRequest.getCartContext()));
        }

        if (Optional.ofNullable(offersRequest.getCustomerSegments()).isPresent()
                && !offersRequest.getCustomerSegments().isEmpty()) {
            ctOfferRequest.setCustomerSegments(offersRequest.getCustomerSegments());
        }

        if (Optional.ofNullable(offersRequest.getBusinessSegments()).isPresent()
                && !offersRequest.getBusinessSegments().isEmpty()) {
            ctOfferRequest.setBusinessSegments(offersRequest.getBusinessSegments());
        }

        // Set Exclusion List
//        ctOfferRequest.setExclusions(getExclusionByOfferType(offersRequest));

        if (checkChannelEligibility(offersRequest, offerRequestWrapper)) {
            ctOfferRequest.setOpusChannel(offersRequest.getChannelEligibility().getOpusChannel());
            ctOfferRequest.setOpusSubChannel(offersRequest.getChannelEligibility().getOpusSubChannel());
            ctOfferRequest.setOpusStoreId(offersRequest.getChannelEligibility().getOpusStoreId());
            offerRequestWrapper.setChannelEligiblity(true);
        }

        // SLS-IXP-FLAG changes
        if (featureHelper.isEnabled(Constants.FEATURE_FLAG_SLS_GETOFFERS_SERVICES_ENABLED)
                && Objects.nonNull(offersRequest.getCustomerContext()) && Objects.nonNull(offersRequest.getCustomerContext().getOtt()) &&
                Optional.ofNullable(offersRequest.getCustomerContext().getOtt().getPendingSwimlaneSwitch()).isPresent() &&
                offersRequest.getCustomerContext().getOtt().getPendingSwimlaneSwitch()) {
            if (Objects.isNull(ctOfferRequest.getAdditionalAccountDetails())) {
                ctOfferRequest.setAdditionalAccountDetails(new AdditionalAccountDetails());
            }
            ctOfferRequest.getAdditionalAccountDetails().setStatus(Constants.PENDING_SWIMLANE);
        }
        if (Objects.nonNull(offersRequest.getChannelEligibility())) {
            setCTCheckEligibilty(offersRequest, ctOfferRequest);
        }

        ctOfferRequest.setReconnectOfferFlag(true);

        if (Optional.ofNullable(offersRequest.getCustomerContext()).isPresent()
                && Objects.nonNull(offersRequest.getCustomerContext())
                && Optional.ofNullable(offersRequest.getCustomerContext().getAdditionalAccountDetails()).isPresent()
                && Objects.nonNull(offersRequest.getCustomerContext().getAdditionalAccountDetails())) {
            ctOfferRequest.setAdditionalAccountDetails(offersRequest.getCustomerContext().getAdditionalAccountDetails());
        }

        if (offersRequest.isDecisioningFlow()) {
            ctOfferRequest.setDecisioningFlow(true);
            ctOfferRequest.setCustomerContext(customerContext);
        }

        boolean localsEnabledFlag = false;
        boolean isServerdMarketFlag = false;
        boolean isReqNotContainZipOrFip = false;
        List<String> zipCode = null;
        List<String> fipsCode = null;
        List<String> localsEnabledValues = redisCacheHelper.getValues(Constants.REDIS_CACHE_LOCALS_ENABLED_FLAG, Constants.OTT);
        if (CollectionUtils.isNotEmpty(localsEnabledValues)) {
            localsEnabledFlag = Boolean.parseBoolean(localsEnabledValues.get(0));
        }
        List<String> servedMarketValues = redisCacheHelper.getValues(Constants.REDIS_CACHE_SERVED_MARKET_FLAG, Constants.OTT);
        if (CollectionUtils.isNotEmpty(servedMarketValues)) {
            isServerdMarketFlag = Boolean.parseBoolean(servedMarketValues.get(0));
        }
        if (localsEnabledFlag && (Optional.ofNullable(offersRequest.getBusinessSegment()).isEmpty() ||
                (Optional.ofNullable(offersRequest.getBusinessSegment()).isPresent() && !offersRequest.getBusinessSegment().isEmpty() && offersRequest.getBusinessSegment().stream().noneMatch(s -> s.equalsIgnoreCase(Constants.MDU))))) {
            if (Objects.isNull(offersRequest.getCustomerEligibility()) ||
                    (Objects.nonNull(offersRequest.getCustomerEligibility()) && CollectionUtils.isEmpty(offersRequest.getCustomerEligibility().getZipCode()))) {
                isReqNotContainZipOrFip = true;
            } else {
                zipCode = offersRequest.getCustomerEligibility().getZipCode();
            }
            if (Objects.isNull(offersRequest.getCustomerEligibility()) ||
                    (Objects.nonNull(offersRequest.getCustomerEligibility()) && CollectionUtils.isEmpty(offersRequest.getCustomerEligibility().getFipsCode()))) {
                isReqNotContainZipOrFip = true;
            } else {
                fipsCode = offersRequest.getCustomerEligibility().getFipsCode();
            }
            if (isReqNotContainZipOrFip) {
                offersRequest.setHasOTTLocalChannels(isServerdMarketFlag);
            } else {
                if (CollectionUtils.isNotEmpty(zipCode) && CollectionUtils.isNotEmpty(fipsCode)) {
                    Boolean isLocalsServed = dmaLookUpService.hasLocalChannels(zipCode.get(0), fipsCode.get(0));
                    if (null != isLocalsServed) {
                        offersRequest.setHasOTTLocalChannels(isLocalsServed);
                    } else {
                        offersRequest.setHasOTTLocalChannels(false);
                    }
                }
            }
        } else if (localsEnabledFlag && (Optional.ofNullable(offersRequest.getBusinessSegment()).isPresent() && !offersRequest.getBusinessSegment().isEmpty() && offersRequest.getBusinessSegment().stream().anyMatch(s -> s.equalsIgnoreCase(Constants.MDU)))) {
            offersRequest.setHasOTTLocalChannels(true);
        }
        Optional.ofNullable(offersRequest).map(OfferRequest::isReconnectCustomer).ifPresent(ctOfferRequest::setReconnectCustomer);
        Optional.ofNullable(offersRequest).map(OfferRequest::getServiceEndDate).ifPresent(ctOfferRequest::setServiceEndDate);
        Optional.ofNullable(offersRequest).map(OfferRequest::getExistingAccountSubscriberType).ifPresent(ctOfferRequest::setExistingAccountSubscriberType);
        Optional.ofNullable(offersRequest)
                .map(OfferRequest::getCustomerSubType)
                .ifPresent(subType -> ctOfferRequest.setCustomerSubType(Collections.singletonList(subType)));
        if (offersRequest.isReconnectCustomer() && CollectionUtils.isNotEmpty(offersRequest.getSalesChannel()) && StringUtils.isEmpty(offersRequest.getExistingAccountSubscriberType())) {
            boolean isOemSalesChannel = offersRequest.getSalesChannel().stream()
                    .anyMatch(salesChannel -> salesChannel.equalsIgnoreCase(Constants.OEM_IAPFIRETV)
                            || salesChannel.equalsIgnoreCase(Constants.OEM_IAPROKUTV)
                            || salesChannel.equalsIgnoreCase(Constants.OEM_IAP_GOOGLE));
            if (isOemSalesChannel) {
                ctOfferRequest.setExistingAccountSubscriberType(Constants.EDSP_STRING);
                offersRequest.setExistingAccountSubscriberType(Constants.EDSP_STRING);
            }
        }
        if (CollectionUtils.isEmpty(offersRequest.getAddOnType())) {
            offersRequest.setAddOnType(null);
        }
        ctOfferRequest.setOfferRequest(offersRequest);
        offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
        offerRequestWrapper.setOfferRequest(offersRequest);
		/*
		 * if(OffersUtils.isOTTServiceFlow(offerRequestWrapper.getOfferRequest())){
		 * List<String> swimlaneSubscriptionType =
		 * redisCacheHelper.getValues(Constants.SWIMLANESUBSCRIPTIONTYPE,
		 * Constants.OTT_PRODUCT_FAMILY); Map<String, String>
		 * swimlaneSubscriptionTypeMap = swimlaneSubscriptionType.stream().map(a ->
		 * a.split(":")) .collect(Collectors.toMap(a -> a[0], a -> a[1]));
		 * offerRequestWrapper.setSwimlaneSubscriptionTypeMap(
		 * swimlaneSubscriptionTypeMap); }
		 */
        return offerRequestWrapper;
    }

    public void setCTCheckEligibilty(OfferRequest offersRequest, CTOfferRequest ctOfferRequest) {
        boolean channelEligibility = false;

		CTChannelEligibility ctChannelEligibility = new CTChannelEligibility();
		if (Optional.ofNullable(offersRequest.getChannelEligibility().getSalesChannel()).isPresent()
				&& StringUtils.isNotBlank(offersRequest.getChannelEligibility().getSalesChannel())) {
			channelEligibility = true;
			ctChannelEligibility.setSalesChannel(offersRequest.getChannelEligibility().getSalesChannel());
		}
		if (Optional.ofNullable(offersRequest.getChannelEligibility().getSalesSubChannel()).isPresent()
				&& StringUtils.isNotBlank(offersRequest.getChannelEligibility().getSalesSubChannel())) {
			channelEligibility = true;
			ctChannelEligibility.setSalesSubChannel(offersRequest.getChannelEligibility().getSalesSubChannel());
		}
		if (Optional.ofNullable(offersRequest.getChannelEligibility().getLocationId()).isPresent()
				&& StringUtils.isNotBlank(offersRequest.getChannelEligibility().getLocationId())) {
			channelEligibility = true;
			ctChannelEligibility.setLocationId(offersRequest.getChannelEligibility().getLocationId());
		}
		if (Optional.ofNullable(offersRequest.getChannelEligibility().getLocationTypeId()).isPresent()
				&& StringUtils.isNotBlank(offersRequest.getChannelEligibility().getLocationTypeId())) {
			channelEligibility = true;
			ctChannelEligibility.setLocationTypeId(offersRequest.getChannelEligibility().getLocationTypeId());
		}
		if (Optional.ofNullable(offersRequest.getChannelEligibility().getDealerCode()).isPresent()
				&& StringUtils.isNotBlank(offersRequest.getChannelEligibility().getDealerCode())) {
			channelEligibility = true;
			ctChannelEligibility.setDealerCode(offersRequest.getChannelEligibility().getDealerCode());
		}
		if (Optional.ofNullable(offersRequest.getChannelEligibility().getDealerId()).isPresent()
				&& StringUtils.isNotBlank(offersRequest.getChannelEligibility().getDealerId())) {
			channelEligibility = true;
			ctChannelEligibility.setDealerId(offersRequest.getChannelEligibility().getDealerId());
		}
		if (Optional.ofNullable(offersRequest.getChannelEligibility().getDirectIntegrationPartnerName()).isPresent()
				&& StringUtils.isNotBlank(offersRequest.getChannelEligibility().getDirectIntegrationPartnerName())) {
			channelEligibility = true;
			ctChannelEligibility.setDirectIntegrationPartnerName(
					offersRequest.getChannelEligibility().getDirectIntegrationPartnerName());
		}
		if (Optional.ofNullable(offersRequest.getChannelEligibility().getMasterDealerId()).isPresent()
				&& StringUtils.isNotBlank(offersRequest.getChannelEligibility().getMasterDealerId())) {
			channelEligibility = true;
			ctChannelEligibility.setMasterDealerId(offersRequest.getChannelEligibility().getMasterDealerId());
		}
        if (Optional.ofNullable(offersRequest.getChannelEligibility().getSalesSiteId()).isPresent()
                && StringUtils.isNotBlank(offersRequest.getChannelEligibility().getSalesSiteId())) {
            channelEligibility = true;
            ctChannelEligibility.setSalesSiteId(offersRequest.getChannelEligibility().getSalesSiteId());
        }
		if (channelEligibility) {
			ctOfferRequest.setChannelEligibility(ctChannelEligibility);
		}
    }

    public boolean checkChannelEligibility(OfferRequest offersRequest, OfferRequestWrapper offerRequestWrapper) {
        if (Optional.ofNullable(offersRequest.getChannelEligibility()).isPresent()
                && Optional.ofNullable(offersRequest.getChannelEligibility().getOpusChannel()).isPresent()
                && !offersRequest.getChannelEligibility().getOpusChannel().isEmpty()
                && Optional.ofNullable(offersRequest.getChannelEligibility().getOpusSubChannel()).isPresent()
                && !offersRequest.getChannelEligibility().getOpusSubChannel().isEmpty()
                && Optional.ofNullable(offersRequest.getChannelEligibility().getOpusStoreId()).isPresent()
                && !offersRequest.getChannelEligibility().getOpusStoreId().isEmpty()
                && (offersRequest.getSalesChannel().contains(Constants.OPUS))) {
            return true;
        }
        return false;
    }

    /**
     * Pre-processes the product request and headers to build a ProductRequestWrapper.
     *
     * @param headers        HTTP headers containing request context
     * @param productRequest the product request object
     * @return ProductRequestWrapper containing processed product request data
     */
    public ProductRequestWrapper preProcessProductRequest(HttpHeaders headers, ProductRequest productRequest) {
        CTProductRequest ctProductRequest = new CTProductRequest();
        ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
        productRequestWrapper.setLoggedInId(headers.getFirst(Constants.IDPCTX_LOGGEDINID));
        productRequestWrapper.setSessionId(headers.getFirst(Constants.IDPCTX_SESSION_ID));

        log.info("EPOCH_GETPRODUCT_REQUEST [{}]", JsonService.getJsonFromObject(productRequest));

        // Check CustomerContext for Existing Services
        if (Objects.nonNull(productRequest) && Objects.nonNull(productRequest.getCustomerContext()) &&
                Objects.nonNull(productRequest.getCustomerContext().getExistingProductFamily())) {
            Map<String, CartProduct> customerContextMap = getCustomerContextMap(productRequest.getCustomerContext());

            productRequest.getCustomerContext().getExistingProductFamily().stream().filter(StringUtils::isNotBlank).forEach(existingProdFamily -> {
                CartProduct existingProduct = null;
                if (Objects.nonNull(customerContextMap.get(existingProdFamily.toLowerCase())) && customerContextMap.get(existingProdFamily.toLowerCase()).getIsActive()) {
                    existingProduct = customerContextMap.get(existingProdFamily.toLowerCase());
                }

                if (Objects.nonNull(existingProduct)) {
                    if (existingProdFamily.equalsIgnoreCase(Constants.OTT_PRODUCT_FAMILY)) {
                        productRequestWrapper.setDtvnAccount(existingProduct.getAccountNumber());
                    } else if (existingProdFamily.equalsIgnoreCase(Constants.WIRELESS_PRODUCT_FAMILY)) {
                        productRequestWrapper.setMobility(true);
                        productRequestWrapper.setAuthAccountsWireless(existingProduct.getAccountNumber());
                    } else {
                        productRequestWrapper.setLinkedUverseAccountNums(existingProduct.getAccountNumber());
                    }
                }
            });
            if (productRequest.getCustomerContext().getOtt() != null
                    && Constants.EMPLOYEE.equalsIgnoreCase(productRequest.getCustomerContext().getOtt().getAccountType())) {
                productRequestWrapper.setEmployeeAccount(true);
            }
        }

        // If ATTTV account info not coming in customer context , but header  has account number
        if (StringUtils.isEmpty(productRequestWrapper.getDtvnAccount()) &&
                Objects.nonNull(headers.getFirst(Constants.IDPCTX_DTVN_ACCOUNT))) {

            productRequestWrapper.setDtvnAccount(OffersUtils.getFirstAccount(headers.getFirst(Constants.IDPCTX_DTVN_ACCOUNT)));
            // Make CG call here and set customerContext in CT Offer Request
            //CGResponse cgOTTResponse = getCGOTTProductDetailsByAccountId(productRequestWrapper.getDtvnAccount());
        }

        // If Wireless account info not coming in customer context , but header  has account number
        if (StringUtils.isEmpty(productRequestWrapper.getAuthAccountsWireless()) &&
                Objects.nonNull(headers.getFirst(Constants.IDPCTX_AUTHORIZEDACCOUNTS_WIRELESS))) {
            productRequestWrapper.setMobility(true);
            productRequestWrapper.setAuthAccountsWireless(OffersUtils.getFirstAccount(headers.getFirst(Constants.IDPCTX_AUTHORIZEDACCOUNTS_WIRELESS)));

            // Make Profile call here and set customerContext in CT Offer Request
            log.info("Calling EPOCH getWirelessProductDetailsByAccountId");
            log.debug("Calling EPOCH getWirelessProductDetailsByAccountId");
            //WirelessResponse cgWirelessResponse = getWirelessProductDetailsByAccountId(productRequestWrapper.getAuthAccountsWireless());
        }

        // If Uverse account info not coming in customer context , but header has account number
        if (StringUtils.isEmpty(productRequestWrapper.getLinkedUverseAccountNums()) &&
                Objects.nonNull(headers.getFirst(Constants.IDPCTX_LINKEDUVERSEACCNUMS))) {
            // product families which are part of existing customer intent
            productRequestWrapper.setLinkedUverseAccountNums(OffersUtils.getFirstAccount(headers.getFirst(Constants.IDPCTX_LINKEDUVERSEACCNUMS)));
        }

        if (Optional.ofNullable(productRequest.getCartContext()).isPresent() &&
                Optional.ofNullable(productRequest.getCartContext().getCpopProductIds()).isPresent()) {
            CartContext cartContext = new CartContext();
            cartContext.setCpopProductIds(productRequest.getCartContext().getCpopProductIds());
            ctProductRequest.setCartContext(cartContext);
        }

        if (Optional.ofNullable(productRequest.getProductFamily()).isPresent() &&
                !productRequest.getProductFamily().isEmpty()) {

            ctProductRequest.setProductFamily(productRequest.getProductFamily());
        }

        if (Optional.ofNullable(productRequest.getProductTypes()).isPresent() &&
                !productRequest.getProductTypes().isEmpty()) {

            ctProductRequest.setProductTypes(productRequest.getProductTypes());
        }

        if (Optional.ofNullable(productRequest.getContractIndicator()).isPresent() &&
                !productRequest.getContractIndicator().isEmpty()) {

            ctProductRequest.setContractIndicator(productRequest.getContractIndicator());
            //ctProductRequest.setContractApplicable(productRequest.getContractIndicator());
            productRequest.setContractApplicable(productRequest.getContractIndicator());
        }
        if (Optional.ofNullable(productRequest.getContractApplicable()).isPresent() &&
                !productRequest.getContractApplicable().isEmpty()) {

            ctProductRequest.setContractApplicable(productRequest.getContractApplicable());
        }
        if (Optional.ofNullable(productRequest.getAddOnType()).isPresent() &&
                !productRequest.getAddOnType().isEmpty()) {

            ctProductRequest.setAddOnType(productRequest.getAddOnType());
        }
        if (Optional.ofNullable(productRequest.getPlanSubType()).isPresent() &&
                !productRequest.getPlanSubType().isEmpty()) {

            ctProductRequest.setPlanSubType(productRequest.getPlanSubType());
        }
        if (Optional.ofNullable(productRequest.getSalesChannel()).isPresent() &&
                !productRequest.getSalesChannel().isEmpty()) {

            ctProductRequest.setSalesChannel(productRequest.getSalesChannel());
        }
        if (Optional.ofNullable(productRequest.getCustomerSegments()).isPresent() &&
                !productRequest.getCustomerSegments().isEmpty()) {

            ctProductRequest.setCustomerSegments(productRequest.getCustomerSegments());
        }
        if (Optional.ofNullable(productRequest.getCustomerContext()).isPresent()
                && Objects.nonNull(productRequest.getCustomerContext())
                && Optional.ofNullable(productRequest.getCustomerContext().getAdditionalAccountDetails()).isPresent()
                && Objects.nonNull(productRequest.getCustomerContext().getAdditionalAccountDetails())) {
            ctProductRequest.setAdditionalAccountDetails(productRequest.getCustomerContext().getAdditionalAccountDetails());
        }
        if (Optional.ofNullable(productRequest.getOfferActionType()).isPresent() &&
                !productRequest.getOfferActionType().isEmpty()) {
            ctProductRequest.setOfferActionType(productRequest.getOfferActionType());
        }
        // ctProductRequest.setProductStatus(Arrays.asList(Constants.ACTIVE));
        productRequestWrapper.setCtProductRequest(ctProductRequest);
        productRequestWrapper.setProductRequest(productRequest);
        return productRequestWrapper;
    }

    private Map<String, List<com.dtv.dcp.epoch.model.customergraph.response.Product>> getUverseProductDetalsByAccountId(String linkedUverseAccountNums) {
        CGAccountProducts cgAccountProducts;
        Map<String, List<com.dtv.dcp.epoch.model.customergraph.response.Product>> productsByLosgMap = new HashMap<>();
        UVAccountProductsResponse accountProductsResponse = getUverseAccountProducts(linkedUverseAccountNums, "uverse",
                UVAccountProductsResponse.class);
        cgAccountProducts = customerGraphHelper.createUVerseAccountProducts(accountProductsResponse);
        if (Optional.ofNullable(cgAccountProducts.getProducts()).isPresent()) {
            List<com.dtv.dcp.epoch.model.customergraph.response.Product> products = cgAccountProducts.getProducts()
                    .stream().filter(product -> product.getLinesOfBusiness() != null).collect(Collectors.toList());
            products.forEach(product -> {
                List<com.dtv.dcp.epoch.model.customergraph.response.Product> temp = Optional
                        .ofNullable(productsByLosgMap.get(product.getLinesOfBusiness())).orElse(new ArrayList<>());
                temp.add(product);
                productsByLosgMap.put(product.getLinesOfBusiness(), temp);
            });

        }
        return productsByLosgMap;
    }

    private <R> R getUverseAccountProducts(String accountId, String accountType, Class<R> responseClass) {
        try {
            return customerGraphService.getUverseAccountProducts(accountId, accountId, accountType, Boolean.TRUE,
                    responseClass, featureHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP) ? "backup" : "primary");
        } catch (ServiceException e) {
            log.error("Exception while getUverseAccountProducts", e.getMessage());
        }
        return null;
    }

    /**
     * @param ctCustomerContext
     * @param prod
     */
    private void populateProducts(CTCustomerContext ctCustomerContext, CartProduct prod) {

        try {
            List<Product> products = new ArrayList<>();
            if (Objects.nonNull(prod) && Objects.nonNull(prod.getProducts())) {
                prod.getProducts().stream().filter(Objects::nonNull).forEach(product -> {
                    Product ctProduct = new Product();
                    ctProduct.setProductCode(product.getProductCode());
                    ctProduct.setProductType(product.getProductType());
                    ctProduct.setBillingProductCode(product.getBillingProductCode());
                    ctProduct.setBillingReferenceID(product.getBillingReferenceID());
                    ctProduct.setPricePlanCode(product.getPricePlanCode());
                    ctProduct.setPickCode(product.getPickCode());
                    ctProduct.setPriceCode(product.getPriceCode());
                    ctProduct.setComponentCode(product.getComponentCode());
                    ctProduct.setModelNumber(product.getModelNumber());
                    ctProduct.setManufacturer(product.getManufacturer());
                    ctProduct.setAccessCardId(product.getAccessCardId());
                    ctProduct.setAccessCardStatus(product.getAccessCardStatus());
                    ctProduct.setEquipmentOwnership(product.getEquipmentOwnership());
                    products.add(ctProduct);
                });
            }
            ctCustomerContext.setProducts(products);
        } catch (Exception e) {
            log.error("Exception in OffersResourceImpl::populateProducts : {} ", e.getMessage());
        }
    }

    /**
     * Builds a map of product family to CartProduct from the customer context.
     *
     * @param customerContext the customer context object
     * @return Map of product family to CartProduct
     */
    private Map<String, CartProduct> getCustomerContextMap(CustomerContext customerContext) {
        Map<String, CartProduct> customerContextMap = new HashMap<>();
        if (Optional.ofNullable(customerContext.getBroadband()).isPresent()) {
            customerContextMap.put(Constants.BB_PRODUCT_FAMILY, customerContext.getBroadband());
        }
        if (Optional.ofNullable(customerContext.getSatellite()).isPresent()) {
            customerContextMap.put(Constants.SATELLITE_PRODUCT_FAMILY, customerContext.getSatellite());
        }
        if (Optional.ofNullable(customerContext.getIptv()).isPresent()) {
            customerContextMap.put(Constants.IPTV_PRODUCT_FAMILY.toLowerCase(), customerContext.getIptv());
        }
        if (Optional.ofNullable(customerContext.getWireless()).isPresent()) {
            customerContextMap.put(Constants.WIRELESS_PRODUCT_FAMILY, customerContext.getWireless());
        }
        if (Optional.ofNullable(customerContext.getOtt()).isPresent()) {
            customerContextMap.put(Constants.OTT_PRODUCT_FAMILY.toLowerCase(), customerContext.getOtt());
        }
        return customerContextMap;
    }

    /**
     * Gets OTT product details by account ID from the customer graph service.
     *
     * @param accountId the account ID
     * @return CGResponse containing OTT product details
     */
    private CGResponse getCGOTTProductDetailsByAccountId(String accountId) {
        if (!org.springframework.util.StringUtils.isEmpty(accountId) && Optional.ofNullable(accountId).isPresent()) {
            try {
                return customerGraphService.getActiveSubscriptions(accountId, featureHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP) ? "backup" : "primary");
            } catch (Exception e) {
                log.error("Exception while getCGOTTProductDetailsByAccountId: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * Copies customer eligibility fields into a new CustomerEligibility object.
     *
     * @param customereligibility the source customer eligibility
     * @return CustomerEligibility with copied fields
     */
    private CustomerEligibility getCustomerEligibilityRequest(CustomerEligibility customereligibility) {
        CustomerEligibility customerEligibility = new CustomerEligibility();
        customerEligibility.setDma(customereligibility.getDma());
        customerEligibility.setZipCode(customereligibility.getZipCode());
        customerEligibility.setCity(customereligibility.getCity());
        customerEligibility.setState(customereligibility.getState());
        customerEligibility.setRegion(customereligibility.getRegion());
        customerEligibility.setCounty(customereligibility.getCounty());
        customerEligibility.setFipsCode(customereligibility.getFipsCode());
        return customerEligibility;
    }


    /**
     * Converts a CartContext to a CTCartContext for CT requests.
     *
     * @param cartContext the cart context object
     * @return CTCartContext containing line items and offer details
     */
    private CTCartContext getCTCartContextRequest(CartContext cartContext) {
        CTCartContext ctCartContext = new CTCartContext();
        List<CTLineItems> listOfLineItems = new ArrayList<>();


        if (Optional.ofNullable(cartContext.getBroadband()).isPresent()) {
            CTLineItems ctLineItem = new CTLineItems();
            ctLineItem.setProductFamily(Constants.BB_PRODUCT_FAMILY);
            List<Product> broadBandProducts = new ArrayList<>();
            cartContext.getBroadband().getProducts().forEach(products -> {
                Product productDetails = new Product();
                productDetails.setProductId(products.getCpopProductId());
                productDetails.setBillingCode(products.getPricePlanCode());
                productDetails.setBillingProductCode(products.getProductCode());
                productDetails.setProductType(products.getProductType());
                broadBandProducts.add(productDetails);
            });
            ctLineItem.setProducts(broadBandProducts);
            listOfLineItems.add(ctLineItem);
        }

        if (Optional.ofNullable(cartContext.getSatellite()).isPresent()) {
            CTLineItems ctLineItem = new CTLineItems();
            ctLineItem.setProductFamily(Constants.SATELLITE_PRODUCT_FAMILY);
            List<Product> satelliteProducts = new ArrayList<>();
            cartContext.getSatellite().getProducts().forEach(products -> {
                Product productDetails = new Product();
                productDetails.setProductId(products.getCpopProductId());
                productDetails.setBillingCode(products.getPricePlanCode());
                productDetails.setBillingProductCode(products.getProductCode());
                productDetails.setProductType(products.getProductType());
                satelliteProducts.add(productDetails);
            });
            ctLineItem.setProducts(satelliteProducts);
            listOfLineItems.add(ctLineItem);
        }

        if (Optional.ofNullable(cartContext.getIptv()).isPresent()) {
            CTLineItems ctLineItem = new CTLineItems();
            ctLineItem.setProductFamily(Constants.IPTV_PRODUCT_FAMILY);
            List<Product> iptvProducts = new ArrayList<>();
            cartContext.getIptv().getProducts().forEach(products -> {
                Product productDetails = new Product();
                productDetails.setProductId(products.getCpopProductId());
                productDetails.setBillingCode(products.getPricePlanCode());
                productDetails.setBillingProductCode(products.getProductCode());
                productDetails.setProductType(products.getProductType());
                iptvProducts.add(productDetails);
            });
            ctLineItem.setProducts(iptvProducts);
            listOfLineItems.add(ctLineItem);
        }

        if (Optional.ofNullable(cartContext.getOtt()).isPresent()) {
            CTLineItems ctLineItem = new CTLineItems();
            ctLineItem.setProductFamily(Constants.OTT_PRODUCT_FAMILY);
            List<Product> ottProducts = new ArrayList<>();
            cartContext.getOtt().getProducts().forEach(products -> {
                Product productDetails = new Product();
                productDetails.setProductId(products.getCpopProductId());
                productDetails.setBillingCode(products.getPricePlanCode());
                productDetails.setBillingProductCode(products.getProductCode());
                productDetails.setProductType(products.getProductType());
                ottProducts.add(productDetails);
            });
            ctLineItem.setProducts(ottProducts);
            listOfLineItems.add(ctLineItem);
        }

        if (CollectionUtils.isNotEmpty(listOfLineItems)) {
            ctCartContext.setLineItems(listOfLineItems);
        }

        if (CollectionUtils.isNotEmpty(cartContext.getCpopOfferIds())) {
            ctCartContext.setOfferIds(cartContext.getCpopOfferIds());
        }

        if (CollectionUtils.isNotEmpty(cartContext.getCpopOfferCodes())) {
            ctCartContext.setOfferCodes(cartContext.getCpopOfferCodes());
        }

        return ctCartContext;
    }

    public List<String> getPricePlanCodesByProductTypeFromCustomerContext(OfferRequestWrapper offerRequestWrapper, String productType) {
        log.info("CTOfferRequestHelper.getPricePlanCodesByProductTypeFromCustomerContext::::Start");
        List<String> pricePlanCodesByProductType = new ArrayList<>();
        offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite().getProducts().forEach(product -> {
            if (productType.equals(product.getProductType())) {
                pricePlanCodesByProductType.add(product.getPricePlanCode());
            }
        });
        log.debug("CTOfferRequestHelper.getPricePlanCodesByProductTypeFromCustomerContext::::End");
        return pricePlanCodesByProductType;
    }
}