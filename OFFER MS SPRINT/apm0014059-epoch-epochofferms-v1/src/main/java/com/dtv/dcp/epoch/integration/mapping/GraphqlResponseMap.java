package com.dtv.dcp.epoch.integration.mapping;

import com.commercetools.graphql.api.types.*;
import com.commercetools.graphql.api.types.Product;
import com.dtv.dcp.epoch.model.ct.benefit.Benefit;
import com.dtv.dcp.epoch.model.ct.eligibility.Constraint;
import com.dtv.dcp.epoch.model.ct.eligibility.Eligibility;
import com.dtv.dcp.epoch.model.ct.generic.GenericLocaleBase;
import com.dtv.dcp.epoch.model.ct.generic.GenericTypeIdBase;
import com.dtv.dcp.epoch.model.ct.offer.*;
import com.dtv.dcp.epoch.model.ct.product.*;
import com.dtv.dcp.epoch.model.ct.productType.ProductTypeAttribute;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.eligibility.EligibilityErrorMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
public class GraphqlResponseMap {

    private static final Logger log = LoggerFactory.getLogger(GraphqlResponseMap.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, String> attrToFieldMaps(List<ProductTypeAttribute> attributes) {
        Map<String, String> attrToFieldMap = new HashMap<>();
        Map<String, String> setTypeMappings = new HashMap<>();
        setTypeMappings.put("enum", "set_enum");
        setTypeMappings.put("text", "set_text");
        setTypeMappings.put("reference", "set_reference");
        setTypeMappings.put("number", "set_number");
        setTypeMappings.put("money", "set_money");
        setTypeMappings.put("ltext", "set_ltext");
        setTypeMappings.put("boolean", "set_boolean");
        setTypeMappings.put("lenum", "set_lenum");
        setTypeMappings.put("date", "set_date");
        setTypeMappings.put("datetime", "set_datetime");
        setTypeMappings.put("time", "set_time");

        for (ProductTypeAttribute attr : attributes) {
            if (attr.getType() != null && attr.getType().getName() != null) {
                String typeName = attr.getType().getName();
                if ("set".equals(typeName)) {
                    String elementTypeName = attr.getType().getElementType().getName();
                    attrToFieldMap.put(attr.getName(), setTypeMappings.getOrDefault(elementTypeName, ""));
                } else {
                    attrToFieldMap.put(attr.getName(), typeName);
                }
            }
        }
        return attrToFieldMap;
    }


    public CTOfferResponse buildCTOfferResponse(List<Product> graphqlProduct) {
        CTOfferResponse response = new CTOfferResponse();
        Set<String> offerCodes = new HashSet<>();
        List<CTOffer> offers = new ArrayList<>();
        graphqlProduct = graphqlProduct.stream().filter(Objects::nonNull).filter(product -> product.getMasterData() != null && product.getMasterData().getPublished()).collect(Collectors.toList());
        for (Product prod : graphqlProduct) {
            if (offerCodes.contains(prod.getKey())) {
                continue;
            }
            offerCodes.add(prod.getKey());

            CTOffer offer = new CTOffer();
            OfferAttributes attrs = new OfferAttributes();
            offer.setCode(prod.getKey());
            offer.setId(prod.getId());
            GenericLocaleBase name = new GenericLocaleBase();
            name.setEn(getGraphQlOfferAllLocaleAttrs(prod));
            offer.setName(name);
            attrs.setOfferPreselectDesignation(getGraphQlOfferKeyAttrs(prod, "offerPreselectDesignation"));
            attrs.setCreditRisk(getGraphQlOfferListAttrs(prod, "eligibilityCreditRisk"));
            attrs.setTreatmentCode(getGraphQlOfferListAttrs(prod, "treatmentCode"));
            attrs.setContractIndicator(getGraphQlOfferKeyAttrs(prod, "contractIndicator"));
            attrs.setBillingCode(getGraphQlOfferAttrs(prod, "billingCode"));
            List<String> customerTypes = getGraphQlOfferListAttrs(prod, "customerTypes");
            if (CollectionUtils.isNotEmpty(customerTypes)) {
                attrs.setCustomerTypes(customerTypes);
            }
            List<String> salesChannel = getGraphQlOfferListAttrs(prod, "salesChannel");
            if (CollectionUtils.isNotEmpty(salesChannel)) {
                attrs.setSalesChannel(salesChannel);
            }
            List<String> directIntegrationPartnerName = getGraphQlOfferListAttrs(prod, "directIntegrationPartnerName");
            if (CollectionUtils.isNotEmpty(directIntegrationPartnerName)) {
                attrs.setDirectIntegrationPartnerName(directIntegrationPartnerName);
            }
            List<String> opusChannels = getGraphQlOfferListAttrs(prod, "opusChannels");
            if (CollectionUtils.isNotEmpty(opusChannels)) {
                attrs.setOpusChannels(opusChannels);
            }
            List<String> opusSubChannels = getGraphQlOfferListAttrs(prod, "opusSubChannels");
            if (CollectionUtils.isNotEmpty(opusSubChannels)) {
                attrs.setOpusSubChannels(opusSubChannels);
            }
            List<String> opusStoreIds = getGraphQlOfferListAttrs(prod, "opusStoreIds");
            if (CollectionUtils.isNotEmpty(opusStoreIds)) {
                attrs.setOpusStoreIds(opusStoreIds);
            }
            List<String> salesSubChannel = getGraphQlOfferListAttrs(prod, "salesSubChannel");
            if (CollectionUtils.isNotEmpty(salesSubChannel)) {
                attrs.setSalesSubChannel(salesSubChannel);
            }

            List<String> ineligiblePartners = getGraphQlOfferListAttrs(prod, "ineligiblePartners");
            if (CollectionUtils.isNotEmpty(ineligiblePartners)) {
                attrs.setIneligiblePartners(ineligiblePartners);
            }
            String locationId = getGraphQlOfferAttrs(prod, "locationId");
            if (StringUtils.isNotEmpty(locationId)) {
                attrs.setLocationId(locationId);
            }
            String locationTypeId = getGraphQlOfferAttrs(prod, "locationTypeId");
            if (StringUtils.isNotEmpty(locationTypeId)) {
                attrs.setLocationTypeId(locationTypeId);
            }
            String specialPage = getGraphQlOfferAttrs(prod, "specialPage");
            if (StringUtils.isNotEmpty(specialPage)) {
                attrs.setSpecialPage(specialPage);
            }
            List<String> dealerCode = getGraphQlOfferListAttrsValue(prod, "dealerCode");
            if (CollectionUtils.isNotEmpty(dealerCode)) {
                attrs.setDealerCode(dealerCode);
            }
            List<String> dealerIds = getGraphQlOfferListAttrsValue(prod, "dealerIds");
            if (CollectionUtils.isNotEmpty(dealerIds)) {
                attrs.setDealerIds(dealerIds);
            }
            List<String> masterDealerId = getGraphQlOfferListAttrs(prod, "masterDealerId");
            if (CollectionUtils.isNotEmpty(masterDealerId)) {
                attrs.setMasterDealerId(masterDealerId);
            }
            List<String> marketingSrcCode = getGraphQlOfferListAttrsValue(prod, "marketingSrcCode");
            if (CollectionUtils.isNotEmpty(marketingSrcCode)) {
                attrs.setMarketingSrcCode(marketingSrcCode);
            }
            attrs.setOfferProductTypes(getGraphQlOfferListAttrs(prod, "offerProductType"));
            attrs.setOfferIntents(getGraphQlOfferListAttrs(prod, "offerIntent"));
            attrs.setMigrationServiceType(getGraphQlOfferListAttrs(prod, "migrationServiceType"));
            setEligibilityInOffer(prod, attrs);
            
            attrs.setDelayProvisioning("".equals(getGraphQlOfferAttrs(prod, "delayProvisioning")) ? null : Boolean.valueOf(getGraphQlOfferAttrs(prod, "delayProvisioning")));
            attrs.setDelayProvisioningReasons(getGraphQlOfferListAttrs(prod, "delayProvisioningReasons"));
            attrs.setServiceSubscriptionType(getGraphQlOfferListAttrs(prod, "serviceSubscriptionType"));
            /** SVOD Only Upsell - SLS */
            attrs.setActiveSubscriptionType(getGraphQlOfferListAttrs(prod, "activeSubscriptionType"));

            ArrayNode additionalEligibilityResponse = getGraphQlOffeNestedrListAttrs(prod, "additionalEligibility");
            if (additionalEligibilityResponse != null && additionalEligibilityResponse.size() > 0) {
                List<AdditionalEligibility> additionalEligibilities = new ArrayList<>();
                additionalEligibilityResponse.forEach(jsonNode -> {
                    if (jsonNode != null) {
                        AdditionalEligibility additionalEligibility = new AdditionalEligibility();
                        additionalEligibility.setEligibilityType(getValues((ArrayNode) jsonNode, "eligibilityType"));
                        additionalEligibility.setEligibleProductType(getValues((ArrayNode) jsonNode, "eligibleProductType"));
                        additionalEligibility.setMaxCount(getValuesTxt((ArrayNode) jsonNode, "maxCount"));
                        additionalEligibility.setMinCount(getValuesTxt((ArrayNode) jsonNode, "minCount"));
                        additionalEligibility.setOfferIds(getListValues((ArrayNode) jsonNode, "offerIds"));
                        additionalEligibility.setPriorityOffers(getListValues((ArrayNode) jsonNode, "priorityOffers"));
                        additionalEligibilities.add(additionalEligibility);
                    }
                });
                attrs.setAdditionalEligibility(additionalEligibilities);
            }

            ArrayNode onlinePartnerDetailsResponse = getGraphQlOffeNestedrListAttrs(prod, "onlinePartnerDetails");
            if (onlinePartnerDetailsResponse != null && onlinePartnerDetailsResponse.size() > 0) {
                List<OnlinePartnerDetails> onlinePartnerDetails = new ArrayList<>();
                onlinePartnerDetailsResponse.forEach(jsonNode -> {
                    if (jsonNode != null) {
                        OnlinePartnerDetails onlinePartnerDetail = new OnlinePartnerDetails();
                        onlinePartnerDetail.setPartnerName(getValues((ArrayNode) jsonNode, "onlinepartnerName"));
                        onlinePartnerDetail.setAgentId(getValues((ArrayNode) jsonNode, "agentId"));
                        onlinePartnerDetail.setDealerCode1(getListValuesFromEnum((ArrayNode) jsonNode, "dealerCode1"));
                        onlinePartnerDetail.setDealerCode2(getListValuesFromEnum((ArrayNode) jsonNode, "dealerCode2"));
                        onlinePartnerDetails.add(onlinePartnerDetail);
                    }
                });
                attrs.setOnlinePartnerDetails(onlinePartnerDetails);
            }

            if (CollectionUtils.isNotEmpty(attrs.getOfferProductTypes())) {
                attrs.setOfferProductType(attrs.getOfferProductTypes().get(0));
            }
            offer.setStartDate(getGraphQlOfferAttrs(prod, "startDate"));
            offer.setEndDate(getGraphQlOfferAttrs(prod, "endDate"));

            List<AssociatedProduct> associatedProducts = new ArrayList<>();
            AssociatedProduct associatedProduct = new AssociatedProduct();
            List<ProductWrapper> bundleProductsWrapper = new ArrayList<>();
            ProductWrapper bundleProductWrapper = new ProductWrapper();
            List<com.dtv.dcp.epoch.model.ct.product.Product> bundleProducts = new ArrayList<>();
            com.dtv.dcp.epoch.model.ct.product.Product bundleProduct = new com.dtv.dcp.epoch.model.ct.product.Product();
            List<ProductWrapper>  qualifyingProductsWrapper = new ArrayList<>();
            ProductWrapper qualifyingProductWrapper = new ProductWrapper();
            List<com.dtv.dcp.epoch.model.ct.product.Product> qualifyingProducts = new ArrayList<>();
            List<String> bundleProductIds = getProductTypeId(prod, "bundleProductIds");
            List<String> qualifyingProductIds = getProductTypeId(prod, "qualifyingProductIds");
            if (CollectionUtils.isNotEmpty(bundleProductIds)) {
                bundleProduct.setId(bundleProductIds.get(0));
                bundleProduct.setKey(getGraphQlRefAllVariantsProductKey(prod));
            }
            if (CollectionUtils.isNotEmpty(qualifyingProductIds)) {
                for (String qualifyingProductId : qualifyingProductIds) {
                    com.dtv.dcp.epoch.model.ct.product.Product qualifyingProduct = new com.dtv.dcp.epoch.model.ct.product.Product();
                    qualifyingProduct.setId(qualifyingProductId);
                    qualifyingProduct.setKey(getGraphQlRefQualifyingProductKey(prod, qualifyingProductId));
                    qualifyingProducts.add(qualifyingProduct);
                }
            }
            List<GenericTypeIdBase> genericTypeIdBasesReconnectOffers = new ArrayList<>();
            Map<String, String> reconnectEligibileOfferMap = getProductTypeIdMap(prod, "reconnectEligibleOffer");
            if (!reconnectEligibileOfferMap.isEmpty()){
                reconnectEligibileOfferMap.entrySet().forEach(entry -> {
                    GenericTypeIdBase reconnectEligibileOfferIdsObj = new GenericTypeIdBase();
                    reconnectEligibileOfferIdsObj.setId(entry.getKey());
                    reconnectEligibileOfferIdsObj.setKey(entry.getValue());
                    genericTypeIdBasesReconnectOffers.add(reconnectEligibileOfferIdsObj);
                });
                attrs.setReconnectEligibleOffer(genericTypeIdBasesReconnectOffers);
            }

            List<GenericTypeIdBase> genericTypeIdBasesConflictingOffer = new ArrayList<>();
            Map<String, String> conflictingOfferMap = getProductTypeIdMap(prod, "conflictingOffers");
            if (!conflictingOfferMap.isEmpty()){
                conflictingOfferMap.entrySet().forEach(entry -> {
                    GenericTypeIdBase conflictingOfferIdsObj = new GenericTypeIdBase();
                    conflictingOfferIdsObj.setId(entry.getKey());
                    conflictingOfferIdsObj.setKey(entry.getValue());
                    genericTypeIdBasesConflictingOffer.add(conflictingOfferIdsObj);
                });
                attrs.setConflictingOffers(genericTypeIdBasesConflictingOffer);
            }

            List<GenericTypeIdBase> genericTypeIdBasesAllowConflictingOffer = new ArrayList<>();
            Map<String, String> allowConflictingOfferMap = getProductTypeIdMap(prod, "allowConflictingOffers");
            if (!allowConflictingOfferMap.isEmpty()){
                allowConflictingOfferMap.entrySet().forEach(entry -> {
                    GenericTypeIdBase allowConflictingOfferIdsObj = new GenericTypeIdBase();
                    allowConflictingOfferIdsObj.setId(entry.getKey());
                    allowConflictingOfferIdsObj.setKey(entry.getValue());
                    genericTypeIdBasesAllowConflictingOffer.add(allowConflictingOfferIdsObj);
                });
                attrs.setAllowConflictingOffers(genericTypeIdBasesAllowConflictingOffer);
            }

            ProductObj productObj = new ProductObj();
            productObj.setCode(getGraphQlRefAllVariantsProductKey(prod));
            List<Variant> variants = new ArrayList<>();
            Variant variant = new Variant();
            Attributes variantAttr = new Attributes();
            variantAttr.setDelayProvisioning("".equals(getGraphQlRefBundleProductAttrs(prod, "delayProvisioning")) ? null : Boolean.valueOf(getGraphQlRefBundleProductAttrs(prod, "delayProvisioning")));
            variantAttr.setDelayProvisioningReasons(getGraphQlRefProductListAttrs(prod, "delayProvisioningReasons"));
            variantAttr.setDisplayType(getGraphQlRefProductAttrs(prod, "displayType"));
            variantAttr.setSubCategory(getGraphQlRefProductAttrs(prod, "subCategory"));
            variantAttr.setFeeType(getGraphQlOfferKeyReferenceAttrs(prod, "feeType"));
            variantAttr.setContractIndicator(getGraphQlRefProductListAttrs(prod, "contractIndicator1"));
            variantAttr.setCategory(getGraphQlRefAllVariantsProductAttrs(prod, "categoryNew"));
            variantAttr.setIncludedProducts(setIncludedProductWrappersArray(prod, "includedProducts"));
            variantAttr.setIncludedEmployeeProducts(setIncludedProductWrappersArray(prod, "includedEmployeeProducts"));
            variantAttr.setCompatibleEmployeeProducts(setProductWrapperArray(prod, "compatibleEmployeeProducts"));
            variantAttr.setCompatibleProducts(setProductWrapperArray(prod, "compatibleProducts"));
            variantAttr.setConflictingProductsReference(setIncludedProductWrappersArray(prod, "conflictingProducts"));

            variant.setAttributes(variantAttr);
            variants.add(variant);
            productObj.setVariants(variants);
            bundleProduct.setObj(productObj);
            bundleProducts.add(bundleProduct);
            bundleProductWrapper.setProducts(bundleProducts);
            bundleProductsWrapper.add(bundleProductWrapper);
            qualifyingProductWrapper.setProducts(qualifyingProducts);
            qualifyingProductsWrapper.add(qualifyingProductWrapper);
            associatedProduct.setQualifyingProducts(qualifyingProductsWrapper);
            associatedProduct.setBundleProducts(bundleProductsWrapper);
            associatedProducts.add(associatedProduct);
            attrs.setAssociatedProducts(associatedProducts);
            offer.setAttributes(attrs);
            offers.add(offer);
        }
        response.setTotal(offers.size());
        response.setCount(offers.size());
        response.setOffers(offers);
        return response;
    }

    private List<String> getProductIdsFromJsonNode(JsonNode jsonNode, String name) {
        if (jsonNode == null) {
            return Collections.emptyList();
        }
        return StreamSupport.stream(jsonNode.spliterator(), false)
                .filter(node -> {
                    JsonNode nameNode = node.get("name");
                    return nameNode != null && name.equalsIgnoreCase(nameNode.asText());
                })
                .flatMap(node -> {
                    JsonNode valueNode = node.get("value");
                    if (valueNode != null && valueNode.isArray()) {
                        return StreamSupport.stream(valueNode.spliterator(), false);
                    }
                    return Stream.empty();
                })
                .map(item -> item.get("id"))
                .filter(idNode -> idNode != null && !idNode.isNull())
                .map(JsonNode::asText)
                .collect(Collectors.toList());
    }

    private String getGraphQlRefBundleProductAttrs(Product prod, String queryAttr) {
        List<RawProductAttribute> attrsRaw = prod.getMasterData().getCurrent().getMasterVariant().getAttributesRaw();
        for (RawProductAttribute attrRaw : attrsRaw) {
            if (attrRaw.getName().equalsIgnoreCase("bundleProductIds")) {
                if (CollectionUtils.isNotEmpty(attrRaw.getReferencedResourceSet())) {
                    for (ReferenceExpandable attrResrSet : attrRaw.getReferencedResourceSet()) {
                        Product x = (Product) attrResrSet;
                        return getGraphQlOfferAttrs(x, queryAttr);
                    }
                }
            }
        }
        return null;
    }
    
    private List<ProductWrapper> setProductWrapperArray(Product prod, String attrs) {
        if (checkIfAttributeExistForNestedProducts(prod, attrs)) {
            List<ProductWrapper> productsWrapper = new ArrayList<ProductWrapper>();
            ProductWrapper compatibleProductWrapper = new ProductWrapper();
            List<com.dtv.dcp.epoch.model.ct.product.Product> products = new ArrayList<com.dtv.dcp.epoch.model.ct.product.Product>();
            for (String compatibleProduct : getGraphQlRefListAttrs(prod, attrs)) {
                com.dtv.dcp.epoch.model.ct.product.Product compatibleProductObj = new com.dtv.dcp.epoch.model.ct.product.Product();
                compatibleProductObj.setId(compatibleProduct);
                products.add(compatibleProductObj);
            }
            compatibleProductWrapper.setProducts(products);
            productsWrapper.add(compatibleProductWrapper);
            return productsWrapper;
        }
        return null;
    }

    private List<IncludeProductWrapper> setIncludedProductWrappersArray(Product prod, String attrs) {
        if (checkIfAttributeExistForNestedProducts(prod, attrs)) {
            List<IncludeProductWrapper> productsWrapper = new ArrayList<>();
            ArrayNode arrayNode = getGraphQlRefArrayNode(prod, attrs);
            if (arrayNode !=null) {
                arrayNode.forEach(jsonNode -> {
                    if (jsonNode != null && jsonNode.isArray()) {
                        List<String> productIds = getProductIdsFromJsonNode(jsonNode, "products");
                        List<String> billingConflictingProductIds = getProductIdsFromJsonNode(jsonNode, "billingConflictingProducts");
                        IncludeProductWrapper includeProductWrapper = new IncludeProductWrapper();
                        includeProductWrapper.setProducts(buildIncludedPrd(productIds));
                        includeProductWrapper.setBillingConflictingProducts(buildIncludedPrd(billingConflictingProductIds));
                        includeProductWrapper.setConstraints(new Constraints());
                        includeProductWrapper.getConstraints().setEligibleSalesChannels(getListValuesFromEnum((ArrayNode) jsonNode, "eligibleSalesChannels"));
                        includeProductWrapper.getConstraints().setExcludedPartners(getListValuesFromEnum((ArrayNode) jsonNode, "excludedPartners"));
                        productsWrapper.add(includeProductWrapper);
                    }
                });
            }
            return productsWrapper;
        }
        return null;
    }

    private List<IncludedProduct> buildIncludedPrd(List<String> productIds) {
        if (CollectionUtils.isNotEmpty(productIds)) {
            List<IncludedProduct> includedProducts = new ArrayList<>();
            for (String includedProduct : productIds) {
                IncludedProduct includedProductObj = new IncludedProduct();
                includedProductObj.setId(includedProduct);
                includedProducts.add(includedProductObj);
            }
            return includedProducts;
        }
        return null;
    }

    private void setEligibilityInOffer(Product product, OfferAttributes attrs) {
        Eligibility eligibility = new Eligibility();
        List<Constraint> constraints = new ArrayList<>();
        Constraint constraint = new Constraint();
        constraint.setSalesChannel(getGraphQlOfferListAttrs(product, "eligibilitySalesChannels"));
        constraint.setCustomerSegments(getGraphQlOfferListAttrs(product, "eligibilityCustomerSegments"));
        if (CollectionUtils.isNotEmpty(constraint.getCustomerSegments())) {
            constraint.setCustomerSegment(constraint.getCustomerSegments().get(0));
        }
        constraints.add(constraint);
        eligibility.setConstraints(constraints);
        attrs.setEligibility(eligibility);
    }

    private String getGraphQlRefAllVariantsProductKey(Product prod) {
        for (RawProductAttribute attrRaw : prod.getMasterData().getCurrent().getMasterVariant().getAttributesRaw()) {
            if (attrRaw.getName().equalsIgnoreCase("bundleProductIds")) {
                if (CollectionUtils.isNotEmpty(attrRaw.getReferencedResourceSet())) {
                    for (ReferenceExpandable attrResrSet : attrRaw.getReferencedResourceSet()) {
                        Product x = (Product) attrResrSet;
                        return x.getKey();
                    }
                }
            }
        }
        return "";
    }

    private String getGraphQlRefQualifyingProductKey(Product prod, String id) {
        for (RawProductAttribute attrRaw : prod.getMasterData().getCurrent().getMasterVariant().getAttributesRaw()) {
            if (attrRaw.getName().equalsIgnoreCase("qualifyingProductIds")) {
                if (CollectionUtils.isNotEmpty(attrRaw.getReferencedResourceSet())) {
                    for (ReferenceExpandable attrResrSet : attrRaw.getReferencedResourceSet()) {
                        Product x = (Product) attrResrSet;
                        if (x.getId().equalsIgnoreCase(id)) {
                            return x.getKey();
                        }
                    }
                }
            }
        }
        return "";
    }

    private String getGraphQlOfferKeyReferenceAttrs(Product prod, String queryAttr) {
        List<RawProductAttribute> attrsRaw = prod.getMasterData().getCurrent().getMasterVariant().getAttributesRaw();
        for (RawProductAttribute attrRaw : attrsRaw) {
            if (CollectionUtils.isNotEmpty(attrRaw.getReferencedResourceSet())) {
                for (ReferenceExpandable attrResrSet : attrRaw.getReferencedResourceSet()) {
                    Product x = (Product) attrResrSet;
                    return getGraphQlKeyFromRefAttrs(x, queryAttr);
                }
            }
        }
        return "";
    }

    private String getGraphQlKeyFromRefAttrs(Product prod, String queryAttr) {
        List<RawProductAttribute> attrsRaw = prod.getMasterData().getCurrent().getMasterVariant().getAttributesRaw();
        for (RawProductAttribute attrRaw : attrsRaw) {
            if (attrRaw.getName().equalsIgnoreCase(queryAttr)) {
                return attrRaw.getValue().findValue("key").asText();
            }
        }
        return "";
    }

    private String getValues(ArrayNode additionalEligibilityResponse, String attrName) {
        for (JsonNode additionalEligibility : additionalEligibilityResponse) {
            if (additionalEligibility.get("name").asText().equalsIgnoreCase(attrName)) {
                return additionalEligibility.get("value").get("key").asText();
            }
        }
        return null;
    }

    private List<String> getListValues(ArrayNode additionalEligibilityResponse, String attrName) {
        List<String> values = new ArrayList<>();
        for (JsonNode additionalEligibility : additionalEligibilityResponse) {
            if (additionalEligibility.get("name").asText().equalsIgnoreCase(attrName)) {
                if (additionalEligibility.get("value").isArray()) {
                    for (JsonNode value : additionalEligibility.get("value")) {
                        values.add(value.asText());
                    }
                    return values;
                }
            }

        }
        return null;
    }

    private List<String> getListValuesFromEnum(ArrayNode arrayNode, String attrName) {
        List<String> values = new ArrayList<>();
        for (JsonNode jsonNode : arrayNode) {
            if (jsonNode.get("name").asText().equalsIgnoreCase(attrName)) {
                if (jsonNode.get("value").isArray()) {
                    for (JsonNode value : jsonNode.get("value")) {
                        values.add(value.get("key").asText());
                    }
                    return values;
                }
            }
        }
        return null;
    }

    private List<String> getGraphQlRefListAttrs(Product prod, String queryAttr) {
        List<RawProductAttribute> attrsRaw = prod.getMasterData().getCurrent().getMasterVariant().getAttributesRaw();
        for (RawProductAttribute attrRaw : attrsRaw) {
            if (attrRaw.getName().equalsIgnoreCase("bundleProductIds")) {
                if (CollectionUtils.isNotEmpty(attrRaw.getReferencedResourceSet())) {
                    for (ReferenceExpandable attrResrSet : attrRaw.getReferencedResourceSet()) {
                        Product prodRef = (Product) attrResrSet;
                        List<RawProductAttribute> attrsRawRef = prodRef.getMasterData().getCurrent().getMasterVariant().getAttributesRaw();
                        for (RawProductAttribute attrRawRef : attrsRawRef) {
                            if (attrRawRef.getName().equalsIgnoreCase(queryAttr)) {
                                return StreamSupport.stream(attrRawRef.getValue().spliterator(), false)
                                        .flatMap(valueArray -> StreamSupport.stream(valueArray.spliterator(), false))
                                        .filter(productGroup -> "products".equals(productGroup.path("name").asText()))
                                        .flatMap(productGroup -> StreamSupport.stream(productGroup.path("value").spliterator(), false))
                                        .map(product -> product.path("id").asText())
                                        .collect(Collectors.toList());

                            }
                        }
                    }
                }
            }
        }
        return new ArrayList<>();
    }

    private ArrayNode getGraphQlRefArrayNode(Product prod, String queryAttr) {
        List<RawProductAttribute> attrsRaw = prod.getMasterData().getCurrent().getMasterVariant().getAttributesRaw();
        for (RawProductAttribute attrRaw : attrsRaw) {
            if (attrRaw.getName().equalsIgnoreCase("bundleProductIds")) {
                if (CollectionUtils.isNotEmpty(attrRaw.getReferencedResourceSet())) {
                    for (ReferenceExpandable attrResrSet : attrRaw.getReferencedResourceSet()) {
                        Product prodRef = (Product) attrResrSet;
                        List<RawProductAttribute> attrsRawRef = prodRef.getMasterData().getCurrent().getMasterVariant().getAttributesRaw();
                        for (RawProductAttribute attrRawRef : attrsRawRef) {
                            if (attrRawRef.getName().equalsIgnoreCase(queryAttr)) {
                                if (attrRawRef.getValue() != null) {
                                    return (ArrayNode) attrRawRef.getValue();
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }





    private String getGraphQlRefAllVariantsProductAttrs(Product prod, String queryAttr) {

        for (RawProductAttribute attrRaw : prod.getMasterData().getCurrent().getMasterVariant().getAttributesRaw()) {
            if (attrRaw.getName().equalsIgnoreCase("bundleProductIds")) {
                if(CollectionUtils.isNotEmpty(attrRaw.getReferencedResourceSet())) {
                    for (ReferenceExpandable attrResrSet : attrRaw.getReferencedResourceSet()) {
                        Product x = (Product) attrResrSet;
                        if (null != x.getMasterData().getCurrent().getAllVariants()) {
                            List<RawProductAttribute> attrsRaw = x.getMasterData().getCurrent().getAllVariants().get(0).getAttributesRaw();
                            for (RawProductAttribute attrRawAttr : attrsRaw) {
                                if (attrRawAttr.getName().equalsIgnoreCase(queryAttr)) {
                                    return attrRawAttr.getValue().findValue("key").asText();
                                }
                            }
                        }
                    }
                }
            }
        }
        return "";
    }

    private String getGraphQlRefProductAttrs(Product prod, String queryAttr) {
        List<RawProductAttribute> attrsRaw = prod.getMasterData().getCurrent().getMasterVariant().getAttributesRaw();
        for (RawProductAttribute attrRaw : attrsRaw) {
            if(CollectionUtils.isNotEmpty(attrRaw.getReferencedResourceSet())) {
                for (ReferenceExpandable attrResrSet : attrRaw.getReferencedResourceSet()) {
                    Product x = (Product) attrResrSet;
                    return getGraphQlOfferAttrs(x, queryAttr);
                }
            }
        }
        return "";
    }

    private String getGraphQlOfferAttrs(Product prod, String queryAttr) {
        List<RawProductAttribute> attrsRaw = prod.getMasterData().getCurrent().getMasterVariant().getAttributesRaw();
        for (RawProductAttribute attrRaw : attrsRaw) {
            if (attrRaw.getName().equalsIgnoreCase(queryAttr)) {
                return attrRaw.getValue().asText();
            }
        }
        return "";
    }

//    private String getGraphQlOfferLocaleAttrs(Product prod, String queryAttr) {
//        List<RawProductAttribute> attrsRaw = prod.getMasterData().getCurrent().getMasterVariant().getAttributesRaw();
//        for (RawProductAttribute attrRaw : attrsRaw) {
//            if (attrRaw.getName().equalsIgnoreCase(queryAttr)) {
//                return attrRaw.getValue().findValue("en").asText();
//            }
//        }
//        return "";
//    }

    private String getGraphQlOfferAllLocaleAttrs(Product prod) {
        List<LocalizedString> attrsRaw = prod.getMasterData().getCurrent().getNameAllLocales();
        for (LocalizedString attrRaw : attrsRaw) {
            if (attrRaw.getLocale().equalsIgnoreCase("en")) {
                return attrRaw.getValue();
            }
        }
        return null;
    }

    private ArrayNode getGraphQlOffeNestedrListAttrs(Product prod, String queryAttr) {
        try {
            List<RawProductAttribute> attrsRaw = prod.getMasterData().getCurrent().getMasterVariant().getAttributesRaw();
            for (RawProductAttribute attrRaw : attrsRaw) {
                if (attrRaw.getName().equalsIgnoreCase(queryAttr)) {
                    if (attrRaw.getValue() != null) {
                        return (ArrayNode) attrRaw.getValue();
                    }

                }
            }
        } catch (Exception ex) {
            log.error("Exception occured in getGraphQlOffeNestedrListAttrs::", ex.getMessage());
        }
        return null;
    }

    private List<String> getGraphQlOfferListAttrs(Product prod, String queryAttr) {
        try {
            List<RawProductAttribute> attrsRaw = prod.getMasterData().getCurrent().getMasterVariant().getAttributesRaw();
            for (RawProductAttribute attrRaw : attrsRaw) {
                if (attrRaw.getName().equalsIgnoreCase(queryAttr)) {
                    if (attrRaw.getValue() != null) {
                        return StreamSupport.stream(attrRaw.getValue().spliterator(), false)
                                .map(node -> node.path("key").asText())
                                .collect(Collectors.toList());

                    }
                    return null;
                }
            }
        } catch (Exception ex) {
            log.error("Exception occured in getGraphQlOfferListAttrs::", ex.getMessage());
        }
        return null;
    }

    private String getGraphQlOfferKeyAttrs(Product prod, String queryAttr) {
        List<RawProductAttribute> attrsRaw = prod.getMasterData().getCurrent().getMasterVariant().getAttributesRaw();
        for (RawProductAttribute attrRaw : attrsRaw) {
            if (attrRaw.getName().equalsIgnoreCase(queryAttr)) {
                return attrRaw.getValue().findValue("key").asText();
            }
        }
        return "";
    }

    private String getValuesTxt(ArrayNode additionalEligibilityResponse, String attrName) {
        for (JsonNode additionalEligibility : additionalEligibilityResponse) {
            if (additionalEligibility.get("name").asText().equalsIgnoreCase(attrName)) {
                return additionalEligibility.get("value").asText();
            }
        }
        return null;
    }

    private List<String> getProductTypeId(Product product, String attrName) {
        List<String> productTypeIds = new ArrayList<>();
        List<RawProductAttribute> attrsRaw = product.getMasterData().getCurrent().getMasterVariant().getAttributesRaw();
        for (RawProductAttribute attrRaw : attrsRaw) {
            if (attrRaw.getName().equalsIgnoreCase(attrName)) {
                if (attrRaw.getValue() != null) {
                    if (attrRaw.getValue().isArray()) {
                        for (JsonNode value : attrRaw.getValue()) {
                            productTypeIds.add(value.get("id").asText());
                        }
                        return productTypeIds;
                    }
                }
            }
        }
        return null;

    }

    private  Map<String, String> getProductTypeIdMap(Product product, String attrName) {
        Map<String, String> productTypeIdMap = new HashMap<>();
        List<RawProductAttribute> attrsRaw = product.getMasterData().getCurrent().getMasterVariant().getAttributesRaw();
        for (RawProductAttribute attrRaw : attrsRaw) {
            if (attrRaw.getName().equalsIgnoreCase(attrName)) {
                if (CollectionUtils.isNotEmpty(attrRaw.getReferencedResourceSet())) {
                    for (ReferenceExpandable attrResrSet : attrRaw.getReferencedResourceSet()) {
                        Product prd = (Product) attrResrSet;
                        productTypeIdMap.put(prd.getId(), prd.getKey());
                    }
                }
            }
        }
        return productTypeIdMap;
    }

    private Boolean checkIfAttributeExistForNestedProducts(Product product, String queryAttr) {
        return Optional.ofNullable(product.getMasterData())
                .map(ProductCatalogData::getCurrent)
                .map(ProductData::getMasterVariant)
                .map(ProductVariant::getAttributesRaw)
                .orElse(Collections.emptyList())
                .stream()
                .filter(attrRaw -> Objects.nonNull(attrRaw.getReferencedResourceSet()))
                .flatMap(attrRaw -> attrRaw.getReferencedResourceSet().stream())
                .filter(Objects::nonNull)
                .map(attrResrSet -> (Product) attrResrSet)
                .flatMap(prodRef -> Optional.ofNullable(prodRef.getMasterData())
                        .map(ProductCatalogData::getCurrent)
                        .map(ProductData::getMasterVariant)
                        .map(ProductVariant::getAttributesRaw)
                        .orElse(Collections.emptyList())
                        .stream())
                .anyMatch(attrRawRef -> queryAttr.equalsIgnoreCase(attrRawRef.getName()));
    }


    private List<String> getGraphQlOfferListAttrsValue(Product prod, String queryAttr) {
        List<String> nodeText = new ArrayList<>();
        try {
            List<RawProductAttribute> attrsRaw = prod.getMasterData().getCurrent().getMasterVariant().getAttributesRaw();
            for (RawProductAttribute attrRaw : attrsRaw) {
                if (attrRaw.getName().equalsIgnoreCase(queryAttr) && attrRaw.getValue() != null) {
                    ArrayNode arrayNode = (ArrayNode) attrRaw.getValue();
                    arrayNode.forEach(node -> nodeText.add(node.asText()));
                    return nodeText;
                }
            }
        } catch (Exception ex) {
            log.error("Exception occurred in getGraphQlOfferListAttrsValue: {}", ex.getMessage());
        }
        return null;
    }

    private List<String> getGraphQlRefProductListAttrs(Product prod, String queryAttr) {
        List<RawProductAttribute> attrsRaw = prod.getMasterData().getCurrent().getMasterVariant().getAttributesRaw();
        for (RawProductAttribute attrRaw : attrsRaw) {
            if (attrRaw.getName().equalsIgnoreCase("bundleProductIds")) {
                if (CollectionUtils.isNotEmpty(attrRaw.getReferencedResourceSet())) {
                    for (ReferenceExpandable attrResrSet : attrRaw.getReferencedResourceSet()) {
                        Product x = (Product) attrResrSet;
                        return getGraphQlOfferListAttrs(x, queryAttr);
                    }
                }
            }
        }
        return null;
    }


    public Map<String, Object> buildEligibilityGraphQLResponse(Product graphqlProduct) {
        Map<String, Object> eligibilityResponse = new HashMap<>();

        if (graphqlProduct != null && graphqlProduct.getMasterData() != null
                && graphqlProduct.getMasterData().getCurrent() != null
                && graphqlProduct.getMasterData().getCurrent().getMasterVariant() != null) {
            // Extract attributes from the master variant of the product
            List<RawProductAttribute> attributesRaw = graphqlProduct.getMasterData()
                    .getCurrent()
                    .getMasterVariant()
                    .getAttributesRaw();

            if (attributesRaw != null) {
                // Iterate through attributes to parse eligibilityDesign and eligibilityErrorMessage
                for (RawProductAttribute attribute : attributesRaw) {
                    if (attribute != null && "eligibilityDesign".equals(attribute.getName())) {
                        // Parse eligibilityDesign into POJO
                        JsonNode valueNode = attribute.getValue();
                        if (valueNode != null) {
                            List<List<Map<String, Object>>> eligibilityDesignRaw = parseJsonNode(valueNode);
                            List<com.dtv.dcp.epoch.model.eligibility.Eligibility> eligibilityDesignList = parseEligibilityDesign(eligibilityDesignRaw);
                            eligibilityResponse.put("eligibilityDesign", eligibilityDesignList);
                        }
                    } else if (attribute != null && "eligibilityErrorMessages".equals(attribute.getName())) {
                        // Parse eligibilityErrorMessage into POJO
                        JsonNode valueNode = attribute.getValue();
                        if (valueNode != null) {
                            List<List<Map<String, Object>>> errorMessageDesignRaw = parseJsonNode(valueNode);
                            List<EligibilityErrorMessage> errorMessageDesignList = parseEligibilityErrorMessage(errorMessageDesignRaw);
                            eligibilityResponse.put("eligibilityErrorMessages", errorMessageDesignList);
                        }
                    }
                }
            }
        }
        return eligibilityResponse;
    }

    private List<List<Map<String, Object>>> parseJsonNode(JsonNode jsonNode) {
        try {
            if (jsonNode != null) {
                return objectMapper.convertValue(jsonNode, new TypeReference<List<List<Map<String, Object>>>>() {
                });
            }
        } catch (IllegalArgumentException e) {
            log.error("Error parsing JsonNode to List<List<Map<String, Object>>>: {}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    private List<com.dtv.dcp.epoch.model.eligibility.Eligibility> parseEligibilityDesign(List<List<Map<String, Object>>> rawData) {
        List<com.dtv.dcp.epoch.model.eligibility.Eligibility> eligibilityDesignList = new ArrayList<>();
        if (rawData != null) {
            for (List<Map<String, Object>> designGroup : rawData) {
                if (designGroup != null) {
                    com.dtv.dcp.epoch.model.eligibility.Eligibility eligibilityDesign = new com.dtv.dcp.epoch.model.eligibility.Eligibility();
                    for (Map<String, Object> design : designGroup) {
                        if (design != null) {
                            String name = (String) design.get("name");
                            List<String> values = (List<String>) design.get("value");
                            if (name != null && values != null) {
                                switch (name) {
                                    case "eligibilityKeyEvaluation":
                                        eligibilityDesign.setKeyEvaluation(parseKeyValueMap(values));
                                        break;
                                    case "customerEligibility":
                                        eligibilityDesign.setCustomerEligibility(parseKeyValueMap(values));
                                        break;
                                    case "channelEligibilityDetails":
                                        eligibilityDesign.setChannelDetails(parseKeyValueMap(values));
                                        break;
                                    case "agentEligibility":
                                        List<Map<String, List<String>>> agentEligibility = parseGlobalKeyEligibility((List<List<Map<String, Object>>>) design.get("value"));
                                        eligibilityDesign.setAgentEligibility(agentEligibility);
                                        break;
                                }
                            }
                        }
                    }
                    eligibilityDesignList.add(eligibilityDesign);
                }
            }
        }
        return eligibilityDesignList;
    }

    private List<EligibilityErrorMessage> parseEligibilityErrorMessage(List<List<Map<String, Object>>> rawData) {
        List<EligibilityErrorMessage> errorMessageDesignList = new ArrayList<>();
        if (rawData != null) {
            for (List<Map<String, Object>> errorGroup : rawData) {
                if (errorGroup != null) {
                    for (Map<String, Object> error : errorGroup) {
                        if (error != null) {
                            EligibilityErrorMessage errorMessageDesign = new EligibilityErrorMessage();
                            List<String> values = (List<String>) error.get("value");
                            if (values != null) {
                                Map<String, List<String>> errorKeyValue = parseKeyValueMap(values);
                                errorMessageDesign.setEligibilityErrorMessage(Collections.singletonList(errorKeyValue));
                                errorMessageDesignList.add(errorMessageDesign);
                            }
                        }
                    }
                }
            }
        }
        return errorMessageDesignList;
    }

    private Map<String, List<String>> parseKeyValueMap(List<String> values) {
        Map<String, List<String>> keyValueMap = new HashMap<>();
        if (values != null) {
            for (String value : values) {
                if (value != null && value.contains(":")) {
                    String[] keyValue = value.split(":", 2); // Split into key and value
                    if (keyValue.length == 2) {
                        String key = keyValue[0];
                        String[] valueParts = keyValue[1].split(","); // Split values by comma
                        for (String part : valueParts) {
                            keyValueMap.computeIfAbsent(key, k -> new ArrayList<>()).add(part.trim());
                        }
                    }
                }
            }
        }
        return keyValueMap;
    }

    private List<Map<String, List<String>>> parseGlobalKeyEligibility(List<List<Map<String, Object>>> rawData) {
        List<Map<String, List<String>>> globalKeyEligibilityList = new ArrayList<>();
        if (rawData != null) {
            for (List<Map<String, Object>> group : rawData) {
                if (group != null) {
                    for (Map<String, Object> item : group) {
                        if (item != null) {
                            String name = (String) item.get("name");
                            Object value = item.get("value");
                            if (name != null && value instanceof List) {
                                List<String> values = extractFlatValues((List<?>) value);
                                if (values != null) {
                                    Map<String, List<String>> keyValueMap = parseKeyValueMap(values);
                                    // Flatten the inner map to match the required type
                                    Map<String, List<String>> flattenedMap = new HashMap<>();
                                    keyValueMap.forEach((key, val) -> flattenedMap.put(key, val));
                                    globalKeyEligibilityList.add(flattenedMap);
                                }
                            }
                        }
                    }
                }
            }
        }
        return globalKeyEligibilityList;
    }

    private List<String> extractFlatValues(List<?> nestedValues) {
        List<String> flatValues = new ArrayList<>();
        for (Object value : nestedValues) {
            if (value instanceof String) {
                flatValues.add((String) value);
            } else if (value instanceof Map) {
                Object innerValue = ((Map<?, ?>) value).get("value");
                if (innerValue instanceof List) {
                    flatValues.addAll(extractFlatValues((List<?>) innerValue));
                }
            }
        }
        return flatValues;
    }


    public CTBenefitsResponse buildCTBenefitsResponse(List<CartDiscount> cartDiscounts, List<String> requestedAttributes) {
        CTBenefitsResponse response = new CTBenefitsResponse();
        List<Benefit> benefits = new ArrayList<>();
        for (CartDiscount cartDiscount : cartDiscounts) {
            Benefit benefit = new Benefit();
            benefit.setCode(cartDiscount.getKey());
            benefit.setId(cartDiscount.getId());
            for (String requestedAttribute : requestedAttributes) {
            	if ("benefitType".equalsIgnoreCase(requestedAttribute)) {
					benefit.setBenefitType(getGraphQlDirectValueCD(cartDiscount, requestedAttribute));
				} else if ("extPromoType".equalsIgnoreCase(requestedAttribute)) {
					benefit.setExtPromoType(getGraphQlDirectValueCD(cartDiscount, requestedAttribute));
				} else if ("saleschannels".equalsIgnoreCase(requestedAttribute)) {
					benefit.setSalesChannels(getGraphQlDirectValuesCD(cartDiscount, requestedAttribute));
				} else if ("eligibleIAPPartners".equalsIgnoreCase(requestedAttribute)) {
					benefit.setEligibleIAPPartners(getGraphQlDirectValuesCD(cartDiscount, requestedAttribute));
				} else if ("swimlaneContractIndicators".equalsIgnoreCase(requestedAttribute)) {
					benefit.setSwimlaneContractIndicators(getGraphQlDirectValuesCD(cartDiscount, requestedAttribute));
				}
			}
			benefits.add(benefit);
		}
        response.setTotal(benefits.size());
        response.setCount(benefits.size());
        response.setBenefits(benefits);
        return response;
    }
    
	private List<String> getGraphQlDirectValuesCD(CartDiscount cartDiscount, String fieldName) {
		if (Objects.isNull(cartDiscount) || Objects.isNull(cartDiscount.getCustom())
				|| org.apache.commons.collections4.CollectionUtils
						.isEmpty(cartDiscount.getCustom().getCustomFieldsRaw())) {
			return Collections.emptyList();
		}

		return cartDiscount.getCustom().getCustomFieldsRaw().stream()
				.filter(field -> Objects.nonNull(field) && Objects.nonNull(field.getName())
						&& field.getName().equalsIgnoreCase(fieldName))
				.filter(field -> Objects.nonNull(field.getValue())).flatMap(field -> {
					JsonNode value = field.getValue();
					if (value.isArray()) {
						return StreamSupport.stream(value.spliterator(), false).map(JsonNode::asText);
					}
					return StreamSupport.stream(Collections.singletonList(value).spliterator(), false)
							.map(JsonNode::asText);
				}).filter(StringUtils::isNotBlank).collect(Collectors.toList());
	}

    private String getGraphQlDirectValueCD(CartDiscount cartDiscount, String fieldName) {
        if (Objects.isNull(cartDiscount) || Objects.isNull(cartDiscount.getCustom()) || org.apache.commons.collections4.CollectionUtils.isEmpty(cartDiscount.getCustom().getCustomFieldsRaw())) {
            return "";
        }

        return cartDiscount.getCustom().getCustomFieldsRaw().stream()
                .filter(field -> Objects.nonNull(field) && Objects.nonNull(field.getName()) && field.getName().equalsIgnoreCase(fieldName))
                .map(field -> Objects.nonNull(field.getValue()) ? field.getValue().asText() : "")
                .findFirst()
                .orElse("");
    }

}
