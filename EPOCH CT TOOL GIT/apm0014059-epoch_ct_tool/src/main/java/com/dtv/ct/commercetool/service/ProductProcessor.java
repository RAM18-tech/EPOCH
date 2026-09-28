package com.dtv.ct.commercetool.service;

import com.commercetools.api.models.cart_discount.*;
import com.commercetools.api.models.common.CentPrecisionMoneyImpl;
import com.commercetools.api.models.common.LocalizedString;
import com.commercetools.api.models.product.*;
import com.commercetools.api.models.product_type.AttributeDefinition;
import com.commercetools.api.models.product_type.AttributePlainEnumValue;
import com.commercetools.api.models.product_type.AttributePlainEnumValueBuilder;
import com.commercetools.api.models.product_type.ProductType;
import com.commercetools.api.models.type.CustomFields;
import com.commercetools.api.models.type.FieldContainer;
import com.dtv.ct.commercetool.config.ProjectRootConfiguration;
import com.dtv.ct.commercetool.model.ProductUpdateRequest;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Component
public class ProductProcessor {
    @Autowired
    ProjectRootConfiguration projectRootConfiguration;

    private static final Logger logger = LoggerFactory.getLogger(ProductProcessor.class);


    public Map<String, String> findMissingReferences(ProductUpdateRequest request) {
        Map<String, String> missingReferences = new HashMap<>();
        request.getOfferCodes().forEach(a -> {
            Product product = null;
            try {
                product = projectRootConfiguration.getEnvironment(request.getEnvironment()).products().withKey(a).get().executeBlocking().getBody();
            } catch (Exception e) {
                System.out.println("Product not found for offer code: " + a);
            }
            if (product != null) {
                missingReferences.putAll(findMissingReferences(product, request));
            }
        });
        return missingReferences;
    }

    public Map<String, String> findMissingReferences(Product product, ProductUpdateRequest request) {
        System.out.println("Finding missing references");

        Map<String, String> missingReferences = new HashMap<>();
        List<String> referencePrd = Arrays.asList("qualifyingProductIds", "bundleProductIds", "includedProductIds", "qualifierIncompatibleProductIds", "reconnectEligibleOffer", "conflictingOffers", "associatedOffers");

        for (String refPrd : referencePrd) {
            System.out.println("Processing product: " + refPrd);
            List<ProductReferenceImpl> productReferencesIds = null;

            if (product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals(refPrd)).findAny().isPresent()) {
                productReferencesIds = (List<ProductReferenceImpl>) product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals(refPrd)).findFirst().get().getValue();
                List<String> productIds = productReferencesIds.stream().filter(Objects::nonNull).map(ProductReferenceImpl::getId).collect(Collectors.toList());
                List<Product> productList = projectRootConfiguration.getEnvironment(request.getEnvironment()).products().get().withLimit(500).withWhere("id in (\"" + String.join("\",\"", productIds) + "\")").executeBlocking().getBody().getResults();
                if (CollectionUtils.isNotEmpty(productList)) {
                    List<String> missingRef = productIds.stream().filter(id -> productList.stream().noneMatch(p -> p.getId().equals(id))).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(missingRef)) {
                        missingReferences.put(product.getKey(), String.join(",", refPrd));
                    }
                }
            }

        }
        return missingReferences;

    }

    public  void updateALlowConflictingFromConflicting(ProductUpdateRequest productUpdateRequest){

        List<Product> productList = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment()).products().get().withWhere(keyIsAnyOf(productUpdateRequest.getOfferCodes())).withLimit(500).executeBlocking().getBody().getResults();
        for (Product product : productList){
            if (product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("conflictingOffers")).findAny().isEmpty()){
                System.out.println(product.getKey());
                continue;
            }
            List<ProductReferenceImpl> conflictingOffers = (List<ProductReferenceImpl>) product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("conflictingOffers")).findFirst().get().getValue();
            if (CollectionUtils.isEmpty(conflictingOffers)){
                System.out.println(product.getKey());
                continue;
            }
            if (product.getMasterData().getPublished()) {
                Product updatedProduct = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment())
                        .products()
                        .update(product)
                        .with(builder -> builder
                                .plus(actionBuilder -> actionBuilder.setAttributeInAllVariantsBuilder()
                                        .name("conflictingOffers").value(null))
                                .plus(actionBuilder -> actionBuilder.publishBuilder()))
                        .executeBlocking()
                        .getBody();
                System.out.println("Product updated: " + updatedProduct.getKey());
            } else {


                Product updatedProduct = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment())
                        .products()
                        .update(product)
                        .with(builder -> builder
                                .plus(actionBuilder -> actionBuilder.setAttributeInAllVariantsBuilder()
                                        .name("conflictingOffers").value(null)))
                        .executeBlocking()
                        .getBody();
                System.out.println("Product updated: " + updatedProduct.getKey());
            }
        }
    }

    public Map<String, String> findExpiredEnDateOfProduct(ProductUpdateRequest request) {
        Map<String, String> expiredEnDate = new HashMap<>();
        Integer limit = 500;
        Integer offset = 0;
        for (int i = 0; i < 10; i++) {
            try {
                // with where
                List<Product> products = projectRootConfiguration.getEnvironment(request.getEnvironment()).products().get().
                        withWhere("masterData(current(masterVariant(attributes(name=\"offerProductFamily\" and value(key in (\"OTT\")))))) or masterData(current(masterVariant(attributes(name=\"productFamily\" and value(key in (\"OTT\"))))))").
                        withLimit(limit).withOffset(offset).executeBlocking().getBody().getResults();

                if (CollectionUtils.isNotEmpty(products)) {
                    products.forEach(product -> {
                        try {
//                            if (findExpiredEnDateOfProduct(product, request) != null) {
//                                expiredEnDate.putAll(findExpiredEnDateOfProduct(product, request));
//                            }
                            expiredEnDate.putAll(findMissingReferences(product, request));
                        } catch (Exception e) {
                            System.out.println("Error in product " + product.getKey());
                        }

                    });
                    offset += limit;
                }
            } catch (Exception e) {
                System.out.println("Error in product " + e.getMessage());
            }
        }
        return expiredEnDate;
    }

    public Map<String, String> findEmptyProductRef(ProductUpdateRequest request) {
        Map<String, String> expiredEnDate = new HashMap<>();
        Integer limit = 500;
        Integer offset = 0;
        for (int i = 0; i < 10; i++) {
            try {
                // with where
                List<Product> products = projectRootConfiguration.getEnvironment(request.getEnvironment()).products().get().
                        withWhere("masterData(current(masterVariant(attributes(name=\"productFamily\" and value(key in (\"OTT\"))))))").
                        withLimit(limit).withOffset(offset).executeBlocking().getBody().getResults();

                if (CollectionUtils.isNotEmpty(products)) {
                    products.forEach(product -> {
                        try {
                            expiredEnDate.putAll(findProdRef(product, request));
                        } catch (Exception e) {
                            System.out.println("Error in product " + product.getKey());
                        }

                    });
                    offset += limit;
                }
            } catch (Exception e) {
                System.out.println("Error in product " + e.getMessage());
            }
        }
        return expiredEnDate;
    }


//    public Map<String, String> getSalesChannel(Product product, ProductUpdateRequest request) {
//
//        List<Attribute> attributes = product.getMasterData().getCurrent().getMasterVariant().getAttributes();
//        Attribute eligibilitySalesChannel = attributes.stream().filter(attr -> attr.getName().equals("eligibilitySalesChannels")).findFirst().orElse(null);
//        List<Attribute> offerEligibility = attributes.stream().filter(attr -> attr.getName().equals("eligibility")).collect(Collectors.toList());
//        List<Attribute> offerEligibilityReferences = (List<Attribute>) offerEligibility.get(0).getValue();
//        List<Attribute> offerEligibilityRe = (List<Attribute>) offerEligibilityReferences.get(0);
//        List<Attribute> salesChannel = offerEligibilityRe.stream().filter(attr -> attr.getName().equalsIgnoreCase("salesChannelEligibility")).collect(Collectors.toList());
//        List<AttributePlainEnumValue> attributePlainEnumValues = (List<AttributePlainEnumValue>) salesChannel.get(0).getValue();
//        if (eligibilitySalesChannel != null) {
//            List<String> allSalesChannel = new ArrayList<>();
//            List<AttributePlainEnumValue> plainEnumValues = (List<AttributePlainEnumValue>) eligibilitySalesChannel.getValue();
//            plainEnumValues.forEach(pv -> {
//                allSalesChannel.add(pv.getKey());
//            });
//            if (attributePlainEnumValues != null) {
//                allSalesChannel.add("INSIDEEEE");
//                attributePlainEnumValues.forEach(pv -> {
//                    allSalesChannel.add(pv.getKey());
//                });
//            }
//            return Collections.singletonMap(product.getKey(), String.join(",", allSalesChannel));
//        }
//        return null;
//
//    }

    public Map<String, String> findExpiredEnDateOfProduct(Product product, ProductUpdateRequest request) {

        if (product.getMasterData().getPublished()) {
            if (product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("endDate")).findAny().isEmpty()
                    || product.getKey().contains("satellite") || product.getKey().contains("Satellite")) {
                return null;
            }
            ZonedDateTime endDate = (ZonedDateTime) product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("endDate")).findAny().get().getValue();
            // check end date less than or equal to current date using LocalDate.now()
            if (endDate.isBefore(ZonedDateTime.now())) {
                return Collections.singletonMap(product.getKey(), endDate.toString());
            }


        }
        return null;
    }


    public Map<String, String> findProdRef(Product product, ProductUpdateRequest request) {
        Map<String, String> missingReferences = new HashMap<>();
        if (product.getMasterData().getPublished()) {
            List<String> fieldNames = Arrays.asList("compatibleProducts", "incompatibleProducts", "compatibleMobilityProducts", "conflictingProducts"
                    , "includedProducts", "compatibleEmployeeProducts", "includedMobilityProducts", "includedEmployeeProducts", "associatedOffers");
            fieldNames.forEach(fieldName -> {
                if (product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals(fieldName)).findAny().isPresent()) {
                    List<ProductReferenceImpl> productReferenceList = (List<ProductReferenceImpl>) product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals(fieldName)).findFirst().get().getValue();
                    List<AttributeImpl> productsList = productReferenceList.size() > 0 ? (List<AttributeImpl>) productReferenceList.get(0) : null;
                    if (CollectionUtils.isNotEmpty(productsList)) {
                        productsList.forEach(pl -> {
                            System.out.println(pl.getName());
                            if (pl.getName().equalsIgnoreCase("products")) {
                                List<ProductReferenceImpl> productReferences = (List<ProductReferenceImpl>) pl.getValue();

                                List<String> productIds = productReferences.stream().filter(Objects::nonNull).map(ProductReferenceImpl::getId).collect(Collectors.toList());
                                List<Product> productList = projectRootConfiguration.getEnvironment(request.getEnvironment()).products().get().withLimit(500).withWhere("id in (\"" + String.join("\",\"", productIds) + "\")").executeBlocking().getBody().getResults();

                                if (CollectionUtils.isNotEmpty(productList)) {
                                    List<String> missingRef = productIds.stream().filter(id -> productList.stream().noneMatch(p -> p.getId().equals(id))).collect(Collectors.toList());
                                    if (CollectionUtils.isNotEmpty(missingRef)) {
                                        missingReferences.put(product.getKey(), String.join(",", fieldName));
                                    }
                                } else {
                                    missingReferences.put(product.getKey(), String.join(",", fieldName));
                                }
                            }
                        });
                    }
                }
            });

        }
        return missingReferences;
    }


    public List<String> story(ProductUpdateRequest request) {
        ProductType productType = projectRootConfiguration.getEnvironment(request.getEnvironment()).productTypes().withKey("offer").get().executeBlocking().getBody();
        String typeId = productType.getId();
        List<Product> productsStag2 = projectRootConfiguration.getEnvironment(request.getEnvironment()).products().get()
                .withWhere("productType(id=\"" + typeId + "\")" + "and masterData(current(masterVariant(attributes(name=\"offerProductFamily\" and value(key in (\"OTT\")))))) and masterData(current(masterVariant(attributes(name=\"contractIndicator\" and value(key in (\"TAZCONTRACT\", \"TAZBYOD\")))))) and masterData(current(masterVariant(attributes(name=\"offerActionType\" and value(key in (\"Acquisition\")))))) and masterData(current(masterVariant(attributes(name=\"offerProductType\" and value(key in (\"video-plan\")))))) and masterData(current(masterVariant(attributes(name=\"eligibilitySalesChannels\" and value(key in (\"opus\", \"directvOnline\"))))))").
                withLimit(500).executeBlocking().getBody().getResults();
        List<String> promoIds = new ArrayList<>();
        List<String> codes = new ArrayList<>();
        productsStag2.forEach(product -> {

            Object value = product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("offerPromos")).findFirst().get().getValue();
            List<List<Attribute>> lists = new ArrayList<>((Collection) value);
            if (CollectionUtils.isNotEmpty(lists)) {
                lists.forEach(list -> {
                    List<Attribute> attributes = list;
                    attributes.forEach(attr -> {
                        if (attr.getName().equals("promoId")) {
                            promoIds.add(attr.getValue().toString());
                            codes.add(product.getId());
                        }
                    });
                });
            }
        });
        List<String> distnct = codes.stream().distinct().collect(Collectors.toList());
        request.setOfferCodes(distnct);
        return promoIds.stream().distinct().collect(Collectors.toList());
    }


    public List<StringBuilder> story2(List<String> codes, ProductUpdateRequest request) {
        List<StringBuilder> builders = new ArrayList<>();
        codes.forEach(code -> {

            CartDiscount cartDiscount = projectRootConfiguration.getEnvironment(request.getEnvironment()).cartDiscounts().withKey(code).get().executeBlocking().getBody();

            StringBuilder sb = new StringBuilder();
            sb.append(code + "||");
            sb.append(cartDiscount.getName().get("en") + "||");
            sb.append(cartDiscount.getDescription().get("en") + "||");
            CustomFields customFields = cartDiscount.getCustom();
            FieldContainer fieldContainer = customFields.getFields();

            Map<String, Object> values = fieldContainer.values();

            List<ProductReferenceImpl> productReferences = (List<ProductReferenceImpl>) values.get("parentOffers");
            List<String> offers = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(productReferences)) {

                productReferences.forEach(productReference -> {
                    try {
                        if (request.getOfferCodes().contains(productReference.getId())) {
                            Product productStg2 = projectRootConfiguration.getEnvironment(request.getEnvironment()).products().withId(productReference.getId()).get().executeBlocking().getBody();
                            List<ProductReferenceImpl> bundleProductIds = (List<ProductReferenceImpl>) productStg2.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("bundleProductIds")).findFirst().get().getValue();
                            bundleProductIds.forEach(bprd -> {
                                try {
                                    Product product1 = projectRootConfiguration.getEnvironment(request.getEnvironment()).products().withId(bprd.getId()).get().executeBlocking().getBody();
                                    offers.add(product1.getKey());
                                } catch (Exception e) {
                                    System.out.println("Error in product inside prodList " + bprd.getId());

                                }
                            });


                        }
                    } catch (Exception e) {
                        System.out.println("Error in product " + productReference.getId());
                    }
                });
            }

            sb.append(String.join(",", offers.stream().distinct().collect(Collectors.toList())) + "||");

            String period = (String) values.get("period");
            sb.append(period + "||");

            String duration = (String) values.get("duration");
            sb.append(duration + "||");

            String benefitType = (String) values.get("benefitType");
            sb.append(benefitType + "||");

            CartDiscountValue cartDiscountValue = cartDiscount.getValue();
            sb.append(cartDiscountValue.getType() + "||");

            if (cartDiscountValue instanceof CartDiscountValueRelativeImpl) {
                CartDiscountValueRelativeImpl cartDiscountValueRelative = (CartDiscountValueRelativeImpl) cartDiscountValue;
                sb.append(cartDiscountValueRelative.getPermyriad() + "||");
            } else if (cartDiscountValue instanceof CartDiscountValueAbsoluteImpl) {
                CartDiscountValueAbsoluteImpl cartDiscountValueAbsolute = (CartDiscountValueAbsoluteImpl) cartDiscountValue;
                CentPrecisionMoneyImpl money = (CentPrecisionMoneyImpl) cartDiscountValueAbsolute.getMoney().get(0);
                sb.append(money.getCentAmount() + "||");
            } else if (cartDiscountValue instanceof CartDiscountValueFixedImpl) {
                CartDiscountValueFixedImpl cartDiscountValueFixed = (CartDiscountValueFixedImpl) cartDiscountValue;
                CentPrecisionMoneyImpl money = (CentPrecisionMoneyImpl) cartDiscountValueFixed.getMoney().get(0);
                sb.append(money.getCentAmount() + "||");
            }

            builders.add(sb);
        });

        return builders;
    }

    public void updateZipInsideOfferEligiblity(ProductUpdateRequest productUpdateRequest) {
        productUpdateRequest.getOfferCodes().forEach(prd -> {
            Product product = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment()).products().withKey(prd).get().executeBlocking().getBody();
            List<Attribute> attributes = product.getMasterData().getCurrent().getMasterVariant().getAttributes();
            List<Attribute> offerEligibility = attributes.stream().filter(attr -> attr.getName().equals("eligibility")).collect(Collectors.toList());

            List<Attribute> offerEligibilityReferences = (List<Attribute>) offerEligibility.get(0).getValue();
            List<Attribute> offerEligibilityRe = (List<Attribute>) offerEligibilityReferences.get(0);

            Attribute dma = offerEligibilityRe.stream().filter(attr -> attr.getName().equalsIgnoreCase("dma")).findFirst().orElse(null);
            if (dma != null) {
                List<String> dmaL = (List<String>) dma.getValue();
                productUpdateRequest.getZipCodes().addAll(dmaL);
            }
            //offerEligibilityRe.removeIf(attr -> attr.getName().equalsIgnoreCase("zip"));
            offerEligibilityRe.removeIf(attr -> attr.getName().equalsIgnoreCase("dma"));

            Attribute attribute = AttributeBuilder.of().name("dma").value(productUpdateRequest.getZipCodes()).build();
            offerEligibilityRe.add(attribute);

            Product updatedProduct = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment())
                    .products()
                    .update(product)
                    .with(builder -> builder
                            .plus(actionBuilder -> actionBuilder.setAttributeInAllVariantsBuilder()
                                    .name("eligibility").value(offerEligibilityReferences))
                            .plus(actionBuilder -> actionBuilder.publishBuilder()))
                    .executeBlocking()
                    .getBody();


        });
    }

    public void getActiveProducts(ProductUpdateRequest productUpdateRequest) {
        List<String> typpes = Arrays.asList("video-plan", "video-addon", "video-device", "fee", "protection-plan", "reward", "campaign");
        AtomicInteger count = new AtomicInteger();
        typpes.forEach(type -> {
            ProductType productType = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment()).productTypes().withKey(type).get().executeBlocking().getBody();
            List<Product> products = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment()).products().get()
                    .withWhere("productType(id=\"" + productType.getId() + "\")" + "and masterData(current(masterVariant(attributes(name=\"productFamily\" and value(key in (\"OTT\"))))))").
                    withLimit(500).executeBlocking().getBody().getResults();
            List<Product> activeProducts = products.stream().filter(product -> product.getMasterData().getPublished()).collect(Collectors.toList());
            count.addAndGet(activeProducts.size());
        });
        System.out.println("Active products: " + count);
    }

    public void getActiveBenefits(ProductUpdateRequest request) {
        AtomicInteger count = new AtomicInteger();
        for (Integer i = 0; i <= 15000; i += 500) {
            List<CartDiscount> cartDiscounts = projectRootConfiguration.getEnvironment(request.getEnvironment()).cartDiscounts().get().withOffset(i).withLimit(500).executeBlocking().getBody().getResults();
            List<CartDiscount> activeCartDiscounts = cartDiscounts.stream().filter(cartDiscount -> cartDiscount.getIsActive()).collect(Collectors.toList());
            activeCartDiscounts.forEach(cartDiscount -> {
                CustomFields customFields = cartDiscount.getCustom();
                FieldContainer fieldContainer = customFields.getFields();

                Map<String, Object> values = fieldContainer.values();
                String family = (String) values.get("beneficiaryProductFamily");
                if ("OTT".equalsIgnoreCase(family)) {
                    count.getAndIncrement();
                }
            });
        }

        System.out.println("Active benefits: " + count);
    }


    public void getAllOttBenfits(ProductUpdateRequest request) {
        AtomicInteger count = new AtomicInteger();
        Long total = projectRootConfiguration.getEnvironment(request.getEnvironment()).cartDiscounts().get().withWhere("custom(fields(beneficiaryProductFamily in (\"OTT\")))").executeBlocking().getBody().getTotal();
        for (Integer i = 0; i <= total; i += 500) {
            List<CartDiscount> cartDiscounts = projectRootConfiguration.getEnvironment(request.getEnvironment()).cartDiscounts().get().withWhere("custom(fields(beneficiaryProductFamily in (\"OTT\")))").withOffset(i).withLimit(500).executeBlocking().getBody().getResults();
            List<CartDiscount> activeCartDiscounts = cartDiscounts.stream().filter(cartDiscount -> cartDiscount.getIsActive()).collect(Collectors.toList());
            activeCartDiscounts.forEach(cartDiscount -> {
                count.getAndIncrement();
                if(cartDiscount.getKey()!=null) {
                    updateBenefit(cartDiscount, request.getEnvironment(), cartDiscount.getKey());
                }
                //updateBenefitEmptyString(cartDiscount, request.getEnvironment(), cartDiscount.getKey());
            });
        }
        System.out.println("Active benefits: " + count);
    }

    public void updateBenefitEmptyString(CartDiscount cartDiscount, String env, String code) {
        // CartDiscount cartDiscount = projectRootConfiguration.getEnvironment(env).cartDiscounts().withKey(code).get().executeBlocking().getBody();
        CustomFields customFields = cartDiscount.getCustom();
        FieldContainer fieldContainer = customFields.getFields();
        Map<String, Object> values = fieldContainer.values();
        AtomicInteger count = new AtomicInteger();
        Map<String, List<String>> listMap = new HashMap<>();
        List<String> benefitUpdated = new ArrayList<>();

        for (Map.Entry<String, Object> customvlues : values.entrySet()) {

            if (customvlues.getValue() instanceof String) {
                String emptyStringValues = (String) customvlues.getValue();
                String emptyStringKeys = (String) customvlues.getKey();
                if(emptyStringKeys.equalsIgnoreCase("directOnlineSalesDisclosure")){
                    System.out.println( cartDiscount.getKey() + "----" + customvlues.getKey() +"---"+code);
                }
                if (emptyStringValues.equalsIgnoreCase(""))
                    System.out.println( cartDiscount.getKey() + "----" + customvlues.getKey());

            }
        }
    }
    public void getAllOffer(ProductUpdateRequest request) {
        AtomicInteger count = new AtomicInteger();
        List<String> offerCode = new ArrayList<>();
        Long total = projectRootConfiguration.getEnvironment(request.getEnvironment()).products().get().
                withWhere("masterData(current(masterVariant(attributes(name=\"offerProductFamily\" and value(key in (\"OTT\"))))))").executeBlocking().getBody().getTotal();
        for (Integer i = 0; i <= total; i += 500) {

            List<Product> products = projectRootConfiguration.getEnvironment(request.getEnvironment()).products().get().
                    withWhere("masterData(current(masterVariant(attributes(name=\"offerProductFamily\" and value(key in (\"OTT\"))))))").withOffset(i).withLimit(500).executeBlocking().getBody().getResults();
            products.forEach(product -> {
                if (product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("offerPromos")).findFirst().isPresent()) {
                    Object value = product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("offerPromos")).findFirst().get().getValue();
                    List<List<Attribute>> lists = new ArrayList<>((Collection) value);
                    if (CollectionUtils.isNotEmpty(lists)) {
                        offerCode.add(product.getKey());
                    }
                }
            });
        }

        System.out.println(offerCode);
    }

    public void updateBenefit(CartDiscount cartDiscount1, String env, String code) {
        CartDiscount cartDiscount = projectRootConfiguration.getEnvironment(env).cartDiscounts().withKey(code).get().executeBlocking().getBody();
        CustomFields customFields = cartDiscount.getCustom();
        FieldContainer fieldContainer = customFields.getFields();
        Map<String, Object> values = fieldContainer.values();
        AtomicInteger count = new AtomicInteger();
        Map<String, List<String>> listMap = new HashMap<>();
        List<String> benefitUpdated = new ArrayList<>();
        //24.1.4
        listMap.put("opusServicesShortDescription", Arrays.asList("dotcomAgentServicesShortDescription"));
        listMap.put("opusServicesLongDescription", Arrays.asList("dotcomAgentServicesLongDescription"));
        listMap.put("opusServicesShortDisplayName", Arrays.asList("dotcomAgentServicesShortDisplayName"));
        listMap.put("opusServicesLongDisplayName", Arrays.asList("dotcomAgentServicesLongDisplayName"));
        listMap.put("opusServicesOptIn", Arrays.asList("dotcomAgentServicesOptIn"));
        listMap.put("opusServicesOptOut", Arrays.asList("dotcomAgentServicesOptOut"));
        listMap.put("opusServicesAutoRenew", Arrays.asList("dotcomAgentServicesAutoRenew"));
        listMap.put("opusServicesDisclosure", Arrays.asList("dotcomAgentServicesDisclosure"));
        listMap.put("opusServicesShortDisclosure", Arrays.asList("dotcomAgentServicesShortDisclosure"));
//        listMap.put("shortDescriptionforOpus", Arrays.asList("opusSalesShortDesc"));
//        listMap.put("longDescription", Arrays.asList("opusSalesLongDesc"));
//        listMap.put("shortDescriptionforServices", Arrays.asList("opusServicesShortDesc","directvOnlineServicesShortDesc"));
//        listMap.put("longDescriptionforServices", Arrays.asList("opusServicesLongDesc","directvOnlineServicesLongDesc"));
//        listMap.put("benefitDisplayName", Arrays.asList("opusSalesShortDisplayName", "opusSalesLongDisplayName"));
//        listMap.put("displayNameforServices", Arrays.asList("opusServicesShortDisplayName", "opusServicesLongDisplayName","directvOnlineServicesShortDisplayName","directvOnlineServicesLongDisplayName"));
//        //24.1.5 (old , new)
//        listMap.put("opusSalesDisclosure", Arrays.asList("evCRMSalesDisclosure"));
//        listMap.put("opusServicesDisclosure", Arrays.asList("evCRMServicesDisclosure"));
//        listMap.put("opusSalesShortDisclosure", Arrays.asList("evCRMSalesShortDisclosure"));
//        listMap.put("opusServicesShortDisclosure", Arrays.asList("evCRMServicesShortDisclosure"));
//
//        listMap.put("opusSalesShortDesc", Arrays.asList("evCRMSalesShortDesc"));
//        listMap.put("opusSalesLongDesc", Arrays.asList("evCRMSalesLongDesc"));
//        listMap.put("opusServicesShortDesc", Arrays.asList("evCRMServicesShortDesc"));
//        listMap.put("opusServicesLongDesc", Arrays.asList("evCRMServicesLongDesc"));
//        listMap.put("opusSalesShortDisplayName", Arrays.asList("evCRMSalesShortDisplayName"));
//        listMap.put("opusServicesShortDisplayName", Arrays.asList("evCRMServicesShortDisplayName"));
//        listMap.put("opusSalesLongDisplayName", Arrays.asList("evCRMSalesLongDisplayName"));
//        listMap.put("opusServicesLongDisplayName", Arrays.asList("evCRMServicesLongDisplayName"));

        List<CartDiscountUpdateAction> cartDiscountUpdateActions = new ArrayList<>();
        listMap.forEach((key, value) -> {
            if (values.containsKey(key)) {
                if ("benefitDisplayName".equalsIgnoreCase(key)) {
                    LocalizedString localizedString = (LocalizedString) values.get(key);
                    String localString = localizedString.get("en").toString();
                    if (localString != null && localString != "" && !localString.isEmpty()) {
                        value.forEach(val -> {
                            CartDiscountUpdateAction cartDiscountUpdateAction = CartDiscountSetCustomFieldActionBuilder.of().name(val).value(localString).build();
                            cartDiscountUpdateActions.add(cartDiscountUpdateAction);
                        });
                    }
                } else {
                    String desc = (String) values.get(key);
                    if (desc != null && desc != ""&& !desc.isEmpty()) {
                        value.forEach(val -> {
                            CartDiscountUpdateAction cartDiscountUpdateAction = CartDiscountSetCustomFieldActionBuilder.of().name(val).value(desc).build();
                            cartDiscountUpdateActions.add(cartDiscountUpdateAction);
                        });
                    }
                }
            }
        });
        if (cartDiscountUpdateActions.size() > 0) {
            CartDiscount updatedCartDiscount = projectRootConfiguration.getEnvironment(env).cartDiscounts().update(cartDiscount).with(builder -> builder.plus(cartDiscountUpdateActions)).executeBlocking().getBody();
            count.getAndIncrement();
            System.out.println("Cart discount updated: " + updatedCartDiscount.getKey());
            benefitUpdated.add(updatedCartDiscount.getKey());
        }
        System.out.println("Total benefits updated Name: " + benefitUpdated);
        System.out.println("Total benefits updated: " + count);
    }
    public void updateBenefit(CartDiscount cartDiscount1, String env, List<String> codes) {
        codes.stream().forEach(code -> {
            CartDiscount cartDiscount = projectRootConfiguration.getEnvironment(env).cartDiscounts().withKey(code).get().executeBlocking().getBody();
            CustomFields customFields = cartDiscount.getCustom();
            FieldContainer fieldContainer = customFields.getFields();
            Map<String, Object> values = fieldContainer.values();
            AtomicInteger count = new AtomicInteger();
            Map<String, List<String>> listMap = new HashMap<>();
            List<String> benefitUpdated = new ArrayList<>();
            //24.1.4
            listMap.put("opusServicesShortDesc", Arrays.asList("dotcomAgentServicesShortDesc"));
            listMap.put("opusServicesLongDesc", Arrays.asList("dotcomAgentServicesLongDesc"));
            listMap.put("opusServicesShortDisplayName", Arrays.asList("dotcomAgentServicesShortDisplayName"));
            listMap.put("opusServicesLongDisplayName", Arrays.asList("dotcomAgentServicesLongDisplayName"));
            listMap.put("opusServicesOptIn", Arrays.asList("dotcomAgentServicesOptIn"));
            listMap.put("opusServicesOptOut", Arrays.asList("dotcomAgentServicesOptOut"));
            listMap.put("opusServicesAutoRenew", Arrays.asList("dotcomAgentServicesAutoRenew"));
            listMap.put("opusServicesDisclosure", Arrays.asList("dotcomAgentServicesDisclosure"));
            listMap.put("opusServicesShortDisclosure", Arrays.asList("dotcomAgentServicesShortDisclosure"));
//        listMap.put("shortDescriptionforOpus", Arrays.asList("opusSalesShortDesc"));
//        listMap.put("longDescription", Arrays.asList("opusSalesLongDesc"));
//        listMap.put("shortDescriptionforServices", Arrays.asList("opusServicesShortDesc","directvOnlineServicesShortDesc"));
//        listMap.put("longDescriptionforServices", Arrays.asList("opusServicesLongDesc","directvOnlineServicesLongDesc"));
//        listMap.put("benefitDisplayName", Arrays.asList("opusSalesShortDisplayName", "opusSalesLongDisplayName"));
//        listMap.put("displayNameforServices", Arrays.asList("opusServicesShortDisplayName", "opusServicesLongDisplayName","directvOnlineServicesShortDisplayName","directvOnlineServicesLongDisplayName"));
//        //24.1.5 (old , new)
//        listMap.put("opusSalesDisclosure", Arrays.asList("evCRMSalesDisclosure"));
//        listMap.put("opusServicesDisclosure", Arrays.asList("evCRMServicesDisclosure"));
//        listMap.put("opusSalesShortDisclosure", Arrays.asList("evCRMSalesShortDisclosure"));
//        listMap.put("opusServicesShortDisclosure", Arrays.asList("evCRMServicesShortDisclosure"));
//
//        listMap.put("opusSalesShortDesc", Arrays.asList("evCRMSalesShortDesc"));
//        listMap.put("opusSalesLongDesc", Arrays.asList("evCRMSalesLongDesc"));
//        listMap.put("opusServicesShortDesc", Arrays.asList("evCRMServicesShortDesc"));
//        listMap.put("opusServicesLongDesc", Arrays.asList("evCRMServicesLongDesc"));
//        listMap.put("opusSalesShortDisplayName", Arrays.asList("evCRMSalesShortDisplayName"));
//        listMap.put("opusServicesShortDisplayName", Arrays.asList("evCRMServicesShortDisplayName"));
//        listMap.put("opusSalesLongDisplayName", Arrays.asList("evCRMSalesLongDisplayName"));
//        listMap.put("opusServicesLongDisplayName", Arrays.asList("evCRMServicesLongDisplayName"));

            List<CartDiscountUpdateAction> cartDiscountUpdateActions = new ArrayList<>();
            listMap.forEach((key, value) -> {
                if (values.containsKey(key)) {
                    if ("benefitDisplayName".equalsIgnoreCase(key)) {
                        LocalizedString localizedString = (LocalizedString) values.get(key);
                        String localString = localizedString.get("en").toString();
                        if (localString != null && localString != "" && !localString.isEmpty()) {
                            value.forEach(val -> {
                                CartDiscountUpdateAction cartDiscountUpdateAction = CartDiscountSetCustomFieldActionBuilder.of().name(val).value(localString).build();
                                cartDiscountUpdateActions.add(cartDiscountUpdateAction);
                            });
                        }
                    } else {
                        String desc = (String) values.get(key);
                        if (desc != null && desc != "" && !desc.isEmpty()) {
                            value.forEach(val -> {
                                CartDiscountUpdateAction cartDiscountUpdateAction = CartDiscountSetCustomFieldActionBuilder.of().name(val).value(desc).build();
                                cartDiscountUpdateActions.add(cartDiscountUpdateAction);
                            });
                        }
                    }
                }
            });
            if (cartDiscountUpdateActions.size() > 0) {
                CartDiscount updatedCartDiscount = projectRootConfiguration.getEnvironment(env).cartDiscounts().update(cartDiscount).with(builder -> builder.plus(cartDiscountUpdateActions)).executeBlocking().getBody();
                count.getAndIncrement();
                System.out.println("Cart discount updated: " + updatedCartDiscount.getKey());
                benefitUpdated.add(updatedCartDiscount.getKey());
            }
            System.out.println("Total benefits updated Name: " + benefitUpdated);
            System.out.println("Total benefits updated: " + count);
        });
    }

    public List<String> checkCardDiscountMappingForAssocitedOffers(ProductUpdateRequest request) {
        List<String> msgs = new ArrayList<>();
        request.getOfferCodes().forEach(code -> {
            List<String> promoIds = new ArrayList<>();
            Product product = projectRootConfiguration.getEnvironment(request.getEnvironment()).products().withKey(code).get().executeBlocking().getBody();
            if (product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("offerPromos")).findFirst().isPresent()) {
                Object value = product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("offerPromos")).findFirst().get().getValue();
                List<List<Attribute>> lists = new ArrayList<>((Collection) value);
                if (CollectionUtils.isNotEmpty(lists)) {
                    lists.forEach(list -> {
                        List<Attribute> attributes = list;
                        attributes.forEach(attr -> {
                            if (attr.getName().equals("promoId")) {
                                promoIds.add(attr.getValue().toString());
                            }
                        });
                    });
                }
            }
            if (CollectionUtils.isNotEmpty(promoIds)) {
                String productId = product.getId();
                List<CartDiscount> cartDiscounts = projectRootConfiguration.getEnvironment(request.getEnvironment()).cartDiscounts().get().withWhere(keyIsAnyOf(promoIds)).executeBlocking().getBody().getResults();
                cartDiscounts.forEach(cartDiscount -> {
                    CustomFields customFields = cartDiscount.getCustom();
                    FieldContainer fieldContainer = customFields.getFields();
                    Map<String, Object> values = fieldContainer.values();
                    List<ProductReference> productReferences = (List<ProductReference>) values.get("parentOffers");
                    if (CollectionUtils.isNotEmpty(productReferences)) {
                        boolean checkPresent = productReferences.stream().anyMatch(pr -> productId.equalsIgnoreCase(pr.getId()));
                        if (!checkPresent) {
                            System.out.println("Product not found in associated offers for cart discount: " + cartDiscount.getKey());
                            msgs.add(cartDiscount.getKey() + " does not have" + product.getKey() + " in associated offers");
//                            ProductReference productReferenceBuilder = ProductReferenceBuilder.of().id(productId).build();
//                            productReferences.add(productReferenceBuilder);
//                            CartDiscount updatedCartDiscount = projectRootConfiguration.getEnvironment(request.getEnvironment())
//                                    .cartDiscounts()
//                                    .update(cartDiscount)
//                                    .with(builder -> builder.plus(actionBuilder -> actionBuilder.setCustomFieldBuilder()
//                                            .name("parentOffers").value(productReferences)))
//                                    .executeBlocking()
//                                    .getBody();
//                            System.out.println("Cart discount updated: " + updatedCartDiscount.getKey());
                        }

                    }
                });

            }
        });
        return msgs;
    }

    public static String keyIsAnyOf(List<String> offerKeys) {
        if (offerKeys != null && !offerKeys.isEmpty()) {
            StringJoiner joiner = new StringJoiner("\",\"", "\"", "\"");
            offerKeys.forEach(joiner::add);
            return "key in (" + joiner.toString() + ")";
        }
        return null;
    }

    public static String IdsAnyOf(List<String> offerKeys) {
        if (offerKeys != null && !offerKeys.isEmpty()) {
            StringJoiner joiner = new StringJoiner("\",\"", "\"", "\"");
            offerKeys.forEach(joiner::add);
            return "id in (" + joiner.toString() + ")";
        }
        return null;
    }

    public List<String> getBundlePrd(ProductUpdateRequest productUpdateRequest) {
        List<String> bundlePrd = new ArrayList<>();
        List<Product> productsWithKey = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment()).products().get().withWhere(keyIsAnyOf(productUpdateRequest.getOfferCodes())).withLimit(500).executeBlocking().getBody().getResults();
        productsWithKey.forEach(product -> {
            if (product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("bundleProductIds")).findFirst().isPresent()) {
                Object value = product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("bundleProductIds")).findFirst().get().getValue();
                List<ProductReferenceImpl> list = (List<ProductReferenceImpl>) value;
                list.forEach(l -> {
                    bundlePrd.add(l.getId());
                });
            }
        });
        List<String> bundlePrdDisT = bundlePrd.stream().distinct().collect(Collectors.toList());
        String Ids = IdsAnyOf(bundlePrdDisT);

        List<Product> products = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment()).products().get().withWhere(Ids).withLimit(500).executeBlocking().getBody().getResults();

        return products.stream().map(Product::getKey).collect(Collectors.toList());
    }


    public List<String> getOfferid(ProductUpdateRequest productUpdateRequest) {
        List<String> bundlePrd = new ArrayList<>();
        List<Product> productsWithKey = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment()).products().get().withWhere(keyIsAnyOf(productUpdateRequest.getOfferCodes())).withLimit(500).executeBlocking().getBody().getResults();
        productsWithKey.forEach(product -> {
            if (product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("choiceGroup")).findFirst().isPresent()) {
                Object value = product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("bundleProductIds")).findFirst().get().getValue();
                List<ProductReferenceImpl> list = (List<ProductReferenceImpl>) value;
                list.forEach(l -> {
                    bundlePrd.add(l.getId());
                });
            }
        });
        List<String> bundlePrdDisT = bundlePrd.stream().distinct().collect(Collectors.toList());
        String Ids = IdsAnyOf(bundlePrdDisT);

        List<Product> products = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment()).products().get().withWhere(Ids).withLimit(500).executeBlocking().getBody().getResults();

        return products.stream().map(Product::getKey).collect(Collectors.toList());
    }


    public void getProductsDetails(ProductUpdateRequest productUpdateRequest) {
        JsonArray dataArray = new JsonArray();
        List<Product> products = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment()).products().get().
                withWhere("masterData(current(masterVariant(attributes(name=\"productFamily\" and value(key in (\"OTT\"))))))").
                withLimit(10).executeBlocking().getBody().getResults();

        List<ProductType> productTypes = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment()).productTypes().get().executeBlocking().getBody().getResults();

        products.forEach(product -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("productId", product.getId());
            jsonObject.addProperty("productKey", product.getKey());
            if (product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("businessSegment")).findFirst().isPresent()) {
                List<AttributePlainEnumValue> businessSegment = (List<AttributePlainEnumValue>) product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("businessSegment")).findFirst().get().getValue();
                List<String> allBs = new ArrayList<>();
                businessSegment.forEach(b -> {
                    allBs.add(b.getKey());
                });
                jsonObject.addProperty("businessSegment", String.join(",", allBs));
            }


                if (product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("displayNameByKey")).findFirst().isPresent()) {
                List<Attribute> displayNameByKey = (List<Attribute>) product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("displayNameByKey")).findFirst().get().getValue();
                JsonObject displayNameByKeyObject = new JsonObject();
                ProductType productType = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment()).productTypes().withKey("display-names-by-key-new").get().executeBlocking().getBody();
                displayNameByKey.forEach(displayKey -> {
                    AttributeDefinition attributeDefinition = productType.getAttributes().stream().filter(attr -> attr.getName().equals(displayKey)).findFirst().get();
                    displayNameByKeyObject.addProperty(attributeDefinition.getLabel().get("en"), displayKey.getValue().toString());
                });
                jsonObject.add("displayNameByKey", displayNameByKeyObject);
            }

            if (product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("descriptionByKey")).findFirst().isPresent()) {
                List<Attribute> descriptionByKey = (List<Attribute>) product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("displayNameByKey")).findFirst().get().getValue();
                JsonObject descriptionByKeyObject = new JsonObject();
                descriptionByKey.forEach(attr -> {
                    descriptionByKeyObject.addProperty(attr.getName(), attr.getValue().toString());
                });
                jsonObject.add("descriptionByKey", descriptionByKeyObject);
            }

            //disclosureMessagesByKey
            if (product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("disclosureMessagesByKey")).findFirst().isPresent()) {
                List<Attribute> disclosureMessagesByKey = (List<Attribute>) product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("displayNameByKey")).findFirst().get().getValue();
                JsonObject disclosureMessagesByKeyObject = new JsonObject();
                disclosureMessagesByKey.forEach(attr -> {
                    disclosureMessagesByKeyObject.addProperty(attr.getName(), attr.getValue().toString());
                });
                jsonObject.add("disclosureMessagesByKey", disclosureMessagesByKeyObject);
            }

            dataArray.add(jsonObject);
        });

        System.out.println(dataArray);
    }

    //this will get the sales channel from outside and update same inside
    public void updateSalesChannelInsideOfferEligibilty(ProductUpdateRequest productUpdateRequest) {
        List<String> errorOffer = new ArrayList<>();
        productUpdateRequest.getOfferCodes().forEach(prd -> {
            try {
                Product product = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment()).products().withKey(prd).get().executeBlocking().getBody();
                List<Attribute> attributes = product.getMasterData().getCurrent().getMasterVariant().getAttributes();
                List<Attribute> eligibilitySalesChannel = attributes.stream().filter(attr -> attr.getName().equals("eligibilitySalesChannels")).collect(Collectors.toList());
                List<Attribute> offerEligibility = attributes.stream().filter(attr -> attr.getName().equals("eligibility")).collect(Collectors.toList());
                List<AttributePlainEnumValue> eligibilitySalesChannelEnumValues = (List<AttributePlainEnumValue>) eligibilitySalesChannel.get(0).getValue();
                List<Attribute> offerEligibilityReferences = (List<Attribute>) offerEligibility.get(0).getValue();
                List<Attribute> offerEligibilityRe = (List<Attribute>) offerEligibilityReferences.get(0);
                List<Attribute> salesChannel = offerEligibilityRe.stream().filter(attr -> attr.getName().equalsIgnoreCase("salesChannelEligibility")).collect(Collectors.toList());
                salesChannel.get(0).setValue(eligibilitySalesChannelEnumValues);

                if (product.getMasterData().getPublished()) {
                    Product updatedProduct = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment())
                            .products()
                            .update(product)
                            .with(builder -> builder
                                    .plus(actionBuilder -> actionBuilder.setAttributeInAllVariantsBuilder()
                                            .name("eligibility").value(offerEligibilityReferences))
                                    .plus(actionBuilder -> actionBuilder.publishBuilder()))
                            .executeBlocking()
                            .getBody();
                } else {


                    Product updatedProduct = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment())
                            .products()
                            .update(product)
                            .with(builder -> builder
                                    .plus(actionBuilder -> actionBuilder.setAttributeInAllVariantsBuilder()
                                            .name("eligibility").value(offerEligibilityReferences))
                                    .plus(actionBuilder -> actionBuilder.unpublishBuilder()))
                            .executeBlocking()
                            .getBody();
                }
            } catch (Exception e) {
                System.out.println("Error in product " + prd);
                errorOffer.add(prd);
                System.out.println(errorOffer.toString());
            }
        });
    }


    public void updateSalesChannelBothInsideOutside(ProductUpdateRequest productUpdateRequest) {
        productUpdateRequest.getOfferCodes().forEach(prd -> {
            try {
                Product product = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment()).products().withKey(prd).get().executeBlocking().getBody();
                List<Attribute> attributes = product.getMasterData().getCurrent().getMasterVariant().getAttributes();
                List<Attribute> eligibilitySalesChannel = attributes.stream().filter(attr -> attr.getName().equals("eligibilitySalesChannels")).collect(Collectors.toList());
                List<Attribute> offerEligibility = attributes.stream().filter(attr -> attr.getName().equals("eligibility")).collect(Collectors.toList());
                List<AttributePlainEnumValue> eligibilitySalesChannelEnumValues = (List<AttributePlainEnumValue>) eligibilitySalesChannel.get(0).getValue();
                List<Attribute> offerEligibilityReferences = (List<Attribute>) offerEligibility.get(0).getValue();
                List<Attribute> offerEligibilityRe = (List<Attribute>) offerEligibilityReferences.get(0);
                List<Attribute> salesChannel = offerEligibilityRe.stream().filter(attr -> attr.getName().equalsIgnoreCase("salesChannelEligibility")).collect(Collectors.toList());
                productUpdateRequest.getSalesChannelToAdd().forEach(salesChnl -> {
                    String label = null;
                    if (salesChnl.equalsIgnoreCase("directvOnline")) {
                        label = "Directv Online";
                    } else if (salesChnl.equalsIgnoreCase("directIntegrationPartner")) {
                        label = "Direct Integration Partner";
                    } else if (salesChnl.equalsIgnoreCase("all")) {
                        label = "all";
                    }else if (salesChnl.equalsIgnoreCase("osprey")) {
                        label = "Osprey";
                    }
                    AttributePlainEnumValue attributePlainEnumValue = AttributePlainEnumValueBuilder.of().key(salesChnl).label(label).build();
                    eligibilitySalesChannelEnumValues.add(attributePlainEnumValue);
                });
                salesChannel.get(0).setValue(eligibilitySalesChannelEnumValues);

                if (product.getMasterData().getPublished()) {
                    Product updatedProduct = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment())
                            .products()
                            .update(product)
                            .with(builder -> builder
                                    .plus(actionBuilder -> actionBuilder.setAttributeInAllVariantsBuilder()
                                            .name("eligibility").value(offerEligibilityReferences))
                                    .plus(actionBuilder -> actionBuilder.setAttributeInAllVariantsBuilder()
                                            .name("eligibilitySalesChannels").value(eligibilitySalesChannelEnumValues))
                                    .plus(actionBuilder -> actionBuilder.publishBuilder()))
                            .executeBlocking()
                            .getBody();
                    System.out.println("Product updated: " + updatedProduct.getKey());
                } else {


                    Product updatedProduct = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment())
                            .products()
                            .update(product)
                            .with(builder -> builder
                                    .plus(actionBuilder -> actionBuilder.setAttributeInAllVariantsBuilder()
                                            .name("eligibility").value(offerEligibilityReferences))
                                    .plus(actionBuilder -> actionBuilder.setAttributeInAllVariantsBuilder()
                                            .name("eligibilitySalesChannels").value(eligibilitySalesChannelEnumValues)))
                            .executeBlocking()
                            .getBody();
                    System.out.println("Product updated: " + updatedProduct.getKey());
                }
            } catch (Exception e) {
                System.out.println("Error in product " + e);
                throw e;
            }
        });
    }


    public void updateAssociatedProduct(ProductUpdateRequest productUpdateRequest) {
        AtomicInteger counter = new AtomicInteger(0);
        productUpdateRequest.getOfferCodes().forEach(offer -> {
            List<ProductUpdateAction> updateActions = new ArrayList<>();
            Product product =  projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment()).products().withKey(offer).get().executeBlocking().getBody();

            List<ProductReferenceImpl> productReferences = null;
            List<String> productOnAccountToSuprres = null;
            if (product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("associatedProducts")).findAny().isPresent()) {
                productReferences = (List<ProductReferenceImpl>) product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("associatedProducts")).findFirst().get().getValue();
            }
            List<ProductReferenceImpl> qualifyingProductIds = null;
            if (product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("qualifyingProductIds")).findAny().isPresent()) {
                qualifyingProductIds = (List<ProductReferenceImpl>) product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("qualifyingProductIds")).findFirst().get().getValue();
            }

            if (product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("productsOnAccountToSuppressOffer")).findAny().isPresent()) {
                productOnAccountToSuprres = (List<String>) product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("productsOnAccountToSuppressOffer")).findFirst().get().getValue();
            } else {
                productOnAccountToSuprres = new ArrayList<>();
            }
//            if (CollectionUtils.isNotEmpty(productOnAccountToSuprres)){
//                productOnAccountToSuprres.removeIf(productId -> productId.equalsIgnoreCase("BASE-SPORTSMVP-202408"));
//                updateActions.add(ProductSetAttributeActionBuilder.of().variantId(Long.valueOf(1)).name("productsOnAccountToSuppressOffer").value(productOnAccountToSuprres).build());
//            }
//            if (!productOnAccountToSuprres.contains("BASE-SPORTSMVP-202408")) {
//                productOnAccountToSuprres.add("BASE-SPORTSMVP-202408");
//                updateActions.add(ProductSetAttributeActionBuilder.of().variantId(Long.valueOf(1)).name("productsOnAccountToSuppressOffer").value(productOnAccountToSuprres).build());
//            }

            if(CollectionUtils.isNotEmpty(qualifyingProductIds)) {
                if (!qualifyingProductIds.stream().filter(productReference -> productReference.getId().equalsIgnoreCase("c27e80ca-a5c4-4558-b07a-8f0bb8909a5b")).findAny().isPresent()) {
                    ProductReferenceImpl productReferenceImpl = new ProductReferenceImpl();
                    productReferenceImpl.setId("c27e80ca-a5c4-4558-b07a-8f0bb8909a5b");
                    qualifyingProductIds.add(productReferenceImpl);
                    updateActions.add(ProductSetAttributeActionBuilder.of().variantId(Long.valueOf(1)).name("qualifyingProductIds").value(qualifyingProductIds).build());
                }
                //qualifyingProductIds.removeIf(productReference -> productReference.getId().equals("c27e80ca-a5c4-4558-b07a-8f0bb8909a5b"));
                //updateActions.add(ProductSetAttributeActionBuilder.of().variantId(Long.valueOf(1)).name("qualifyingProductIds").value(qualifyingProductIds).build());
            }
            if (CollectionUtils.isNotEmpty(productReferences)) {
                List<Object> objects = new ArrayList<>(productReferences);

                objects.forEach(o -> {
                    List<Attribute> productsList = (List<Attribute>) o;

                    productsList.forEach(prd -> {
                        //prd.getName().equalsIgnoreCase("bundleProducts") ||
                        if ( prd.getName().equalsIgnoreCase("qualifyingProducts")) {
                            List<Attribute> bp = (List<Attribute>) prd.getValue();
                            List<Attribute> bp1 = (List<Attribute>) bp.get(0);
                            bp1.forEach(b -> {
                                if (b.getName().equalsIgnoreCase("products")) {
                                    List<ProductReferenceImpl> productReferencesToAdd = new ArrayList<>();

                                    List<ProductReferenceImpl> productReferences1 = (List<ProductReferenceImpl>) b.getValue();
                                    //productReferences1.removeIf(productReference -> productReference.getId().equals("c27e80ca-a5c4-4558-b07a-8f0bb8909a5b"));
                                    if (!productReferences1.stream().filter(productReference -> productReference.getId().equalsIgnoreCase("c27e80ca-a5c4-4558-b07a-8f0bb8909a5b")).findAny().isPresent()) {
                                        ProductReferenceImpl productReferenceImpl = new ProductReferenceImpl();
                                        productReferenceImpl.setId("c27e80ca-a5c4-4558-b07a-8f0bb8909a5b");
                                        productReferences1.add(productReferenceImpl);
                                    }
//                                    productReferences1.forEach(productReference -> {
//                                        try {
//                                       /* Product productStage2 = projectRootConfiguration.createApiClientStaging2().products().withId(productReference.getId()).get().executeBlocking().getBody();
//                                        Product productStage3 = projectRootConfiguration.createApiClientStaging3().products().withKey(productStage2.getKey()).get().executeBlocking().getBody();
//*/
//                                            ProductReferenceImpl productReferenceImpl = new ProductReferenceImpl();
//                                            productReferenceImpl.setId(productReference.getId());
//                                            productReferencesToAdd.add(productReferenceImpl);
//                                        } catch (Exception e) {
//                                            System.out.println("Error in product inside bundle " + productReference.getId());
//                                        }
//                                    });
//
//                                    if (prd.getName().equalsIgnoreCase("qualifyingProducts")) {
//                                        List<ProductReferenceImpl> temp = (List<ProductReferenceImpl>) b.getValue();
//                                        if (!temp.stream().filter(p -> p.getId().equals("c27e80ca-a5c4-4558-b07a-8f0bb8909a5b")).findAny().isPresent()) {
//                                            ProductReferenceImpl productReferenceImpl = new ProductReferenceImpl();
//                                            productReferenceImpl.setId("c27e80ca-a5c4-4558-b07a-8f0bb8909a5b");
//                                            productReferencesToAdd.add(productReferenceImpl);
//                                        }
//                                    }

                                    //if (productReferencesToAdd.size() > 0) {
                                    b.setValue(productReferences1);
                                    // }
                                }

                                if (b.getName().equalsIgnoreCase("productFamily")) {
                                    AttributePlainEnumValue attributePlainEnumValue = (AttributePlainEnumValue) b.getValue();
                                    if (attributePlainEnumValue.getKey().equalsIgnoreCase("OTT")) {
                                        attributePlainEnumValue.setLabel("AT&T TV");
                                    }
                                }
                            });
                        }
                    });

                });

                updateActions.add(ProductSetAttributeActionBuilder.of().variantId(Long.valueOf(1)).name("associatedProducts").value(objects).build());
            }

            if (product.getMasterData().getPublished()) {
                updateActions.add(ProductPublishActionBuilder.of().build());
            }else {
                updateActions.add(ProductUnpublishActionBuilder.of().build());
            }

            Product updatedProduct =  projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment())
                    .products()
                    .withKey(product.getKey())
                    .post(ProductUpdateBuilder.of().actions(updateActions).version(product.getVersion()).build())
                    .executeBlocking()
                    .getBody();
            System.out.println("Product updated " + updatedProduct.getKey() + " " + counter.incrementAndGet());

        });
    }


    public static String readFileAsString(String fileName, String path) {
        String fileContent = readFileContent(fileName);
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(fileContent, JsonObject.class);
        return jsonObject.get(path).getAsString();
    }

    private static String readFileContent(String fileName) {
        InputStream inputStream = ProductProcessor.class.getResourceAsStream(fileName);
        return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines()
                .collect(Collectors.joining("\n"));
    }

    //"removeOffers": true -> remove offers
    //`"removeOffers": false-> add offers
    public List<StringBuilder> addParentOffers(ProductUpdateRequest request) {
        request.getBenefitsUpdateList().forEach(benefitCodes -> {
                    try {
                        AtomicInteger count = new AtomicInteger();
                        List<String> benefitUpdated = new ArrayList<>();
                        List<String> productId = new ArrayList<>();

                        CartDiscount cartDiscount = projectRootConfiguration.getEnvironment(request.getEnvironment()).cartDiscounts().withKey(benefitCodes.getBenefitCode()).get().executeBlocking().getBody();

                        benefitCodes.getOfferCodes().forEach(offercode ->
                                {
                                    Product product = projectRootConfiguration.getEnvironment(request.getEnvironment()).products().withKey(offercode).get().executeBlocking().getBody();
                                    productId.add(product.getId());
                                }
                        );

                        StringBuilder sb = new StringBuilder();
                        sb.append(benefitCodes.getBenefitCode() + "||");
                        sb.append(cartDiscount.getName().get("en") + "||");
                        sb.append(cartDiscount.getDescription().get("en") + "||");
                        CustomFields customFields = cartDiscount.getCustom();
                        FieldContainer fieldContainer = customFields.getFields();

                        Map<String, Object> values = fieldContainer.values();
                        List<ProductReferenceImpl> parentOffersList = (List<ProductReferenceImpl>) values.get(benefitCodes.getAttributeName());
                        List<String> offers = new ArrayList<>();

                        if (CollectionUtils.isNotEmpty(parentOffersList)) {
                            if (request.isRemoveOffers()) {
                                // Remove parent offers that match the condition
                                System.out.println("Removing offers from parent offers list");
                                parentOffersList.removeIf(parentOffer -> {

                                    try {

                                        // Check if the parentOffer ID exists in benefitCodes.getOfferCodes()
                                        return productId.contains(parentOffer.getId());
                                    } catch (Exception e) {
                                        System.out.println("Error in product " + parentOffer.getId());
                                        return false; // Keep the parentOffer in case of an exception
                                    }
                                });
                            } else {
                                // Add parent offers that don't already exist
                                System.out.println("Adding offers to parent offers list");
                                for (String id : productId) {
                                    boolean exists = parentOffersList.stream()
                                            .anyMatch(parentOffer -> {
                                                try {
                                                    return id.equals(parentOffer.getId());
                                                } catch (Exception e) {
                                                    System.out.println("Error in product " + parentOffer.getId());
                                                    return false;
                                                }
                                            });
                                    if (!exists) {
                                        ProductReferenceImpl newOffer = new ProductReferenceImpl();
                                        newOffer.setId(id);
                                        parentOffersList.add(newOffer);
                                        System.out.println("Added offer: " + id);
                                    }
                                }
                            }

                        }
                        List<CartDiscountUpdateAction> cartDiscountUpdateActions = new ArrayList<>();

                        //    CartDiscountUpdateAction cartDiscountUpdateAction = CartDiscountSetCustomFieldActionBuilder.of().name("parentOffers").value(parentOffersList).build();

                        CartDiscountUpdateAction cartDiscountUpdateAction = CartDiscountSetCustomFieldActionBuilder.of().name(benefitCodes.getAttributeName()).value(parentOffersList).build();
                        cartDiscountUpdateActions.add(cartDiscountUpdateAction);

                        if (cartDiscountUpdateActions.size() > 0) {
                            CartDiscount updatedCartDiscount = projectRootConfiguration.getEnvironment(request.getEnvironment()).cartDiscounts().update(cartDiscount).with(builder -> builder.plus(cartDiscountUpdateActions)).executeBlocking().getBody();
                            count.getAndIncrement();
                            System.out.println("Cart discount updated: " + updatedCartDiscount.getKey());
                            benefitUpdated.add(updatedCartDiscount.getKey());
                        }
                        System.out.println("Total benefits updated Name: " + benefitUpdated);
                        System.out.println("Total benefits updated: " + count);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );

        return null;
    }


    public void updateAssociatedProductInsideOutSide(ProductUpdateRequest productUpdateRequest) {
        AtomicInteger counter = new AtomicInteger(0);
        productUpdateRequest.getAssociatedProducts().forEach(associatedProducts -> {
            List<ProductUpdateAction> updateActions = new ArrayList<>();
            List<String> qualifyingToAdd = null;
            List<String> bundleProductToAdd = null;
            List<ProductReferenceImpl> associatedProductReferences = null;
            List<ProductReferenceImpl> qualifyingProductIds = null;
            List<ProductReferenceImpl> bundleProductIds = null;
            try {
                Product product = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment()).products().withKey(associatedProducts.getOfferId()).get().executeBlocking().getBody();

                if (CollectionUtils.isNotEmpty(associatedProducts.getQualifyingProductIds())) {
                    qualifyingToAdd = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment()).products().get().withWhere(keyIsAnyOf(associatedProducts.getQualifyingProductIds())).executeBlocking().getBody().getResults().stream().map(Product::getId).collect(Collectors.toList());
                }
                if (CollectionUtils.isNotEmpty(associatedProducts.getBundledProductIds())) {
                    bundleProductToAdd = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment()).products().get().withWhere(keyIsAnyOf(associatedProducts.getBundledProductIds())).executeBlocking().getBody().getResults().stream().map(Product::getId).collect(Collectors.toList());
                }

                if (product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().anyMatch(attr -> attr.getName().equals("associatedProducts"))) {
                    @SuppressWarnings("unchecked")
                    List<ProductReferenceImpl> refs = (List<ProductReferenceImpl>) product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("associatedProducts")).findFirst().orElseThrow().getValue();
                    associatedProductReferences = refs;
                }

                if (product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().anyMatch(attr -> attr.getName().equals("qualifyingProductIds"))) {
                    @SuppressWarnings("unchecked")
                    List<ProductReferenceImpl> refs = (List<ProductReferenceImpl>) product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("qualifyingProductIds")).findFirst().orElseThrow().getValue();
                    qualifyingProductIds = refs;
                }
                if (product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().anyMatch(attr -> attr.getName().equals("bundleProductIds"))) {
                    @SuppressWarnings("unchecked")
                    List<ProductReferenceImpl> refs = (List<ProductReferenceImpl>) product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("bundleProductIds")).findFirst().orElseThrow().getValue();
                    bundleProductIds = refs;
                }

                if (!associatedProducts.isKeepExisting()) {
                    if (qualifyingProductIds != null) qualifyingProductIds.clear();
                    if (bundleProductIds != null) bundleProductIds.clear();
                }

                if (CollectionUtils.isNotEmpty(qualifyingToAdd)) {
                    if (qualifyingProductIds == null) qualifyingProductIds = new ArrayList<>();
                    for (String id : qualifyingToAdd) {
                        if (qualifyingProductIds.stream().noneMatch(productReference -> productReference.getId().equalsIgnoreCase(id))) {
                            ProductReferenceImpl productReferenceImpl = new ProductReferenceImpl();
                            productReferenceImpl.setId(id);
                            qualifyingProductIds.add(productReferenceImpl);
                        }
                    }
                    updateActions.add(ProductSetAttributeActionBuilder.of().variantId(1L).name("qualifyingProductIds").value(qualifyingProductIds).build());
                }

                if (CollectionUtils.isNotEmpty(bundleProductToAdd)) {
                    if (bundleProductIds == null) bundleProductIds = new ArrayList<>();
                    for (String id : bundleProductToAdd) {
                        if (bundleProductIds.stream().noneMatch(productReference -> productReference.getId().equalsIgnoreCase(id))) {
                            ProductReferenceImpl productReferenceImpl = new ProductReferenceImpl();
                            productReferenceImpl.setId(id);
                            bundleProductIds.add(productReferenceImpl);
                        }
                    }
                    updateActions.add(ProductSetAttributeActionBuilder.of().variantId(1L).name("bundleProductIds").value(bundleProductIds).build());
                }

                if (CollectionUtils.isNotEmpty(associatedProductReferences)) {
                    List<Object> assocObjReference = new ArrayList<>(associatedProductReferences);
                    List<String> finalQualifyingToAdd = qualifyingToAdd;
                    List<String> finalBundleProductToAdd = bundleProductToAdd;
                    assocObjReference.forEach(refObject -> {
                        @SuppressWarnings("unchecked")
                        List<Attribute> productsList = (List<Attribute>) refObject;
                        productsList.forEach(prd -> {
                            if (prd.getName().equalsIgnoreCase("qualifyingProducts") && CollectionUtils.isNotEmpty(finalQualifyingToAdd)) {
                                @SuppressWarnings("unchecked")
                                List<Attribute> qualifyingProductValue = (List<Attribute>) prd.getValue();
                                @SuppressWarnings("unchecked")
                                List<Attribute> qpValues = (List<Attribute>) qualifyingProductValue.get(0);
                                qpValues.forEach(qpValue -> {
                                    if (qpValue.getName().equalsIgnoreCase("products")) {
                                        @SuppressWarnings("unchecked")
                                        List<ProductReferenceImpl> qualifyingPrdRefInside = (List<ProductReferenceImpl>) qpValue.getValue();
                                        if (!associatedProducts.isKeepExisting()) {
                                            qualifyingPrdRefInside.clear();
                                        }
                                        for (String id : finalQualifyingToAdd) {
                                            if (qualifyingPrdRefInside.stream().noneMatch(productReference -> productReference.getId().equalsIgnoreCase(id))) {
                                                ProductReferenceImpl productReferenceImpl = new ProductReferenceImpl();
                                                productReferenceImpl.setId(id);
                                                qualifyingPrdRefInside.add(productReferenceImpl);
                                            }
                                        }
                                        qpValue.setValue(qualifyingPrdRefInside);
                                    }
                                    if (qpValue.getName().equalsIgnoreCase("productFamily")) {
                                        AttributePlainEnumValue attributePlainEnumValue = (AttributePlainEnumValue) qpValue.getValue();
                                        if (attributePlainEnumValue.getKey().equalsIgnoreCase("OTT")) {
                                            attributePlainEnumValue.setLabel("AT&T TV");
                                        }
                                    }
                                });

                                if (qpValues.stream().noneMatch(attr -> attr.getName().equalsIgnoreCase("products"))) {
                                    List<ProductReferenceImpl> qualifyingPrdRefInside = new ArrayList<>();
                                    for (String id : finalQualifyingToAdd) {
                                        ProductReferenceImpl productReferenceImpl = new ProductReferenceImpl();
                                        productReferenceImpl.setId(id);
                                        qualifyingPrdRefInside.add(productReferenceImpl);
                                    }
                                    Attribute productsAttribute = AttributeBuilder.of().name("products").value(qualifyingPrdRefInside).build();
                                    qpValues.add(productsAttribute);
                                }

                                if (associatedProducts.isClearInside() && qpValues.stream().anyMatch(attr -> attr.getName().equalsIgnoreCase("products"))) {
                                    Attribute productsAttribute = AttributeBuilder.of().name("products").value(null).build();
                                    qpValues.removeIf(attribute -> attribute.getName().equalsIgnoreCase("products"));
                                    qpValues.add(productsAttribute);
                                }

                            }

                            if (prd.getName().equalsIgnoreCase("bundleProducts") && CollectionUtils.isNotEmpty(finalBundleProductToAdd)) {
                                @SuppressWarnings("unchecked")
                                List<Attribute> bundleProductValue = (List<Attribute>) prd.getValue();
                                @SuppressWarnings("unchecked")
                                List<Attribute> bpValues = (List<Attribute>) bundleProductValue.get(0);
                                bpValues.forEach(bpValue -> {
                                    if (bpValue.getName().equalsIgnoreCase("products")) {
                                        @SuppressWarnings("unchecked")
                                        List<ProductReferenceImpl> bundlePrdRefInside = (List<ProductReferenceImpl>) bpValue.getValue();
                                        if (!associatedProducts.isKeepExisting()) {
                                            bundlePrdRefInside.clear();
                                        }
                                        for (String id : finalBundleProductToAdd) {
                                            if (bundlePrdRefInside.stream().noneMatch(productReference -> productReference.getId().equalsIgnoreCase(id))) {
                                                ProductReferenceImpl productReferenceImpl = new ProductReferenceImpl();
                                                productReferenceImpl.setId(id);
                                                bundlePrdRefInside.add(productReferenceImpl);
                                            }
                                        }
                                        bpValue.setValue(bundlePrdRefInside);
                                    }
                                    if (bpValue.getName().equalsIgnoreCase("productFamily")) {
                                        AttributePlainEnumValue attributePlainEnumValue = (AttributePlainEnumValue) bpValue.getValue();
                                        if (attributePlainEnumValue.getKey().equalsIgnoreCase("OTT")) {
                                            attributePlainEnumValue.setLabel("AT&T TV");
                                        }
                                    }
                                });

                                if (bpValues.stream().noneMatch(attr -> attr.getName().equalsIgnoreCase("products"))) {
                                    List<ProductReferenceImpl> bundlePrdRefInside = new ArrayList<>();
                                    for (String id : finalBundleProductToAdd) {
                                        ProductReferenceImpl productReferenceImpl = new ProductReferenceImpl();
                                        productReferenceImpl.setId(id);
                                        bundlePrdRefInside.add(productReferenceImpl);
                                    }
                                    Attribute productsAttribute = AttributeBuilder.of().name("products").value(bundlePrdRefInside).build();
                                    bpValues.add(productsAttribute);
                                }
                            }
                        });

                        if(productsList.stream().noneMatch(attr -> attr.getName().equalsIgnoreCase("qualifyingProducts")) && CollectionUtils.isNotEmpty(finalQualifyingToAdd)) {
                            List<ProductReferenceImpl> qualifyingPrdRefInside = new ArrayList<>();
                            for (String id : finalQualifyingToAdd) {
                                ProductReferenceImpl productReferenceImpl = new ProductReferenceImpl();
                                productReferenceImpl.setId(id);
                                qualifyingPrdRefInside.add(productReferenceImpl);
                            }
                            Attribute productsAttribute = AttributeBuilder.of().name("products").value(qualifyingPrdRefInside).build();
                            List<Attribute> qualifyingProductValue = new ArrayList<>();
                            qualifyingProductValue.add(productsAttribute);
                            List<List<Attribute>> qualifyingProductsAttributeValue = new ArrayList<>();
                            qualifyingProductsAttributeValue.add(qualifyingProductValue);
                            Attribute qualifyingProductsAttribute = AttributeBuilder.of().name("qualifyingProducts").value(qualifyingProductsAttributeValue).build();
                            productsList.add(qualifyingProductsAttribute);
                        }

                        if(productsList.stream().noneMatch(attr -> attr.getName().equalsIgnoreCase("bundleProducts")) && CollectionUtils.isNotEmpty(finalBundleProductToAdd)) {
                            List<ProductReferenceImpl> bundlePrdRefInside = new ArrayList<>();
                            for (String id : finalBundleProductToAdd) {
                                ProductReferenceImpl productReferenceImpl = new ProductReferenceImpl();
                                productReferenceImpl.setId(id);
                                bundlePrdRefInside.add(productReferenceImpl);
                            }
                            Attribute productsAttribute = AttributeBuilder.of().name("products").value(bundlePrdRefInside).build();
                            List<Attribute> bundleProductsValue = new ArrayList<>();
                            bundleProductsValue.add(productsAttribute);
                            List<List<Attribute>> bundleProductsAttributeValue = new ArrayList<>();
                            bundleProductsAttributeValue.add(bundleProductsValue);
                            Attribute bundleProductsAttribute = AttributeBuilder.of().name("bundleProducts").value(bundleProductsAttributeValue).build();
                            productsList.add(bundleProductsAttribute);
                        }
                    });
                    updateActions.add(ProductSetAttributeActionBuilder.of().variantId(1L).name("associatedProducts").value(assocObjReference).build());
                }

                if (product.getMasterData().getPublished()) {
                    updateActions.add(ProductPublishActionBuilder.of().build());
                } else {
                    updateActions.add(ProductUnpublishActionBuilder.of().build());
                }

                Product updatedProduct = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment())
                        .products()
                        .withKey(product.getKey())
                        .post(ProductUpdateBuilder.of().actions(updateActions).version(product.getVersion()).build())
                        .executeBlocking()
                        .getBody();
                System.out.println("Product updated " + updatedProduct.getKey() + " " + counter.incrementAndGet());

            } catch (Exception e) {
                logger.error("Error in product " + associatedProducts.getOfferId(), e);
                throw new RuntimeException(e);
            }
        });
    }


    public void checkIfqualityProductMatchInsideOutside(ProductUpdateRequest request) {
        Map<String, String> nonMatch = new HashMap<>();
        Integer limit = 50;
        Integer offset = 0;
        int total = Math.toIntExact(projectRootConfiguration.getEnvironment(request.getEnvironment()).products().get().withWhere("masterData(current(masterVariant(attributes(name=\"offerProductFamily\" and value(key in (\"OTT\"))))))").executeBlocking().getBody().getTotal());
        for (int i = 0; offset < total; i++) {
            try {
                // with where
                List<Product> products = projectRootConfiguration.getEnvironment(request.getEnvironment()).products().get().
                        withWhere("masterData(current(masterVariant(attributes(name=\"offerProductFamily\" and value(key in (\"OTT\"))))))").
                        withLimit(limit).withOffset(offset).executeBlocking().getBody().getResults();

                if (CollectionUtils.isNotEmpty(products)) {
                    products.forEach(product -> {
                        try {
                            AtomicReference<List<String>> IDFromAssociatedProducts = new AtomicReference<>(new ArrayList<>());
                            List<String> IDFromQualifyingProductIds = new ArrayList<>();
                            if (product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().anyMatch(attr -> attr.getName().equals("associatedProducts"))) {
                                @SuppressWarnings("unchecked")
                                List<ProductReferenceImpl> refs = (List<ProductReferenceImpl>) product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("associatedProducts")).findFirst().orElseThrow().getValue();
                                if (CollectionUtils.isNotEmpty(refs)) {
                                    List<Object> assocObjReference = new ArrayList<>(refs);
                                    assocObjReference.forEach(refObject -> {
                                        @SuppressWarnings("unchecked")
                                        List<Attribute> productsList = (List<Attribute>) refObject;
                                        productsList.forEach(prd -> {
                                            if (prd.getName().equalsIgnoreCase("qualifyingProducts")) {
                                                @SuppressWarnings("unchecked")
                                                List<Attribute> qualifyingProductValue = (List<Attribute>) prd.getValue();
                                                @SuppressWarnings("unchecked")
                                                List<Attribute> qpValues = (List<Attribute>) qualifyingProductValue.get(0);
                                                qpValues.forEach(qpValue -> {
                                                    if (qpValue.getName().equalsIgnoreCase("products")) {
                                                        @SuppressWarnings("unchecked")
                                                        List<ProductReferenceImpl> qualifyingPrdRefInside = (List<ProductReferenceImpl>) qpValue.getValue();
                                                        if (CollectionUtils.isNotEmpty(qualifyingPrdRefInside)) {
                                                            IDFromAssociatedProducts.set(qualifyingPrdRefInside.stream().map(ProductReferenceImpl::getId).collect(Collectors.toList()));
                                                        }
                                                    }
                                                });
                                            }

                                        });
                                    });
                                }
                            }

                            if (product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().anyMatch(attr -> attr.getName().equals("qualifyingProductIds"))) {
                                @SuppressWarnings("unchecked")
                                List<ProductReferenceImpl> refs = (List<ProductReferenceImpl>) product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("qualifyingProductIds")).findFirst().orElseThrow().getValue();
                                IDFromQualifyingProductIds = refs.stream().map(ProductReferenceImpl::getId).collect(Collectors.toList());
                            }

                            List<String> finalIDFromQualifyingProductIds = IDFromQualifyingProductIds;
                            if (CollectionUtils.isNotEmpty(finalIDFromQualifyingProductIds) && CollectionUtils.isNotEmpty(IDFromAssociatedProducts.get())) {
                                List<String> nonMatchingInQualifying = IDFromAssociatedProducts.get().stream()
                                        .filter(id -> !finalIDFromQualifyingProductIds.contains(id))
                                        .collect(Collectors.toList());

                                List<String> nonMatchingInAssociated = finalIDFromQualifyingProductIds.stream()
                                        .filter(id -> !IDFromAssociatedProducts.get().contains(id))
                                        .collect(Collectors.toList());

                                List<String> nonMatching = new ArrayList<>();
                                nonMatching.addAll(nonMatchingInQualifying);
                                nonMatching.addAll(nonMatchingInAssociated);
                                nonMatching = nonMatching.stream().distinct().collect(Collectors.toList());
                                if (CollectionUtils.isNotEmpty(nonMatching)) {
                                    List<Product> nonMatchingKey = projectRootConfiguration.getEnvironment(request.getEnvironment()).products().get().withWhere(IdsAnyOf(nonMatching)).executeBlocking().getBody().getResults();
                                    nonMatch.put(product.getKey(), "Non matching qualifying product ids in associatedProducts are " + nonMatchingKey.stream().map(Product::getKey).collect(Collectors.toList()));
                                }
                            }else if (CollectionUtils.isNotEmpty(IDFromAssociatedProducts.get()) && CollectionUtils.isEmpty(finalIDFromQualifyingProductIds)) {
                                nonMatch.put(product.getKey(), "qualifyingProductIds is empty but associatedProducts has qualifying products");
                            }else if (CollectionUtils.isEmpty(IDFromAssociatedProducts.get()) && CollectionUtils.isNotEmpty(finalIDFromQualifyingProductIds)) {
                                nonMatch.put(product.getKey(), "qualifyingProductIds has values but associatedProducts has no qualifying products");
                            }
                        } catch (Exception e) {
                            System.out.println("Error in product " + product.getKey());
                        }

                    });
                    offset += limit;
                }
            } catch (Exception e) {
                System.out.println("Error in product " + e.getMessage());
            }
        }

        System.out.println(nonMatch);

    }

    public Map<String, Map<String, List<String>>> compareSearchAbleAttributes(ProductUpdateRequest request) {
        List<String> offerCodes = request.getOfferCodes();
        List<String> attributesToCompareWithAP = Arrays.asList("qualifyingProductIds", "bundleProductIds");
        List<String> attributesToCompareWithOE = Arrays.asList("eligibilitySalesChannels", "eligibilityBusinessSegment", "eligibilityCustomerSegments");
        HashMap<String, String> innerOuterAttributeMap = innerOuterMap();
        Map<String, Map<String, List<String>>> returnMap = new HashMap<>();
        offerCodes.forEach(offerCode -> {
            try {
                Map<String, List<String>> diiferenceMap = new HashMap<>();
                Product product = projectRootConfiguration.getEnvironment(request.getEnvironment()).products().withKey(offerCode).get().executeBlocking().getBody();
                List<ProductReferenceImpl> associatedProductsRefs = (List<ProductReferenceImpl>) product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("associatedProducts")).findFirst().orElseThrow().getValue();
                List<Attribute> attributes = product.getMasterData().getCurrent().getMasterVariant().getAttributes();
                List<Attribute> offerEligibility = attributes.stream().filter(attr -> attr.getName().equals("eligibility")).collect(Collectors.toList());
                List<Attribute> offerEligibilityReferences = (List<Attribute>) offerEligibility.get(0).getValue();
                List<Attribute> offerEligibilityFirstElement = (List<Attribute>) offerEligibilityReferences.get(0);
                attributesToCompareWithAP.forEach(at -> {
                    List<ProductReferenceImpl> outerRefs = attributes.stream().filter(attr -> attr.getName().equals(at)).findFirst().map(attr -> (List<ProductReferenceImpl>) attr.getValue()).orElse(new ArrayList<>());
                    List<String> innerIds = getInnerIDs(innerOuterAttributeMap.get(at), associatedProductsRefs);
                    List<String> outerIds = outerRefs.stream().filter(Objects::nonNull).map(ProductReferenceImpl::getId).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(outerIds) && CollectionUtils.isNotEmpty(innerIds)) {
                        List<String> nonMatchingInOuter = outerIds.stream().filter(id -> !innerIds.contains(id)).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(nonMatchingInOuter)) {
                            diiferenceMap.put(at + " Outside check with Inside", getKeyFromID(nonMatchingInOuter, request.getEnvironment()));
                        }

                        List<String> nonMatchingInInner = innerIds.stream().filter(id -> !outerIds.contains(id)).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(nonMatchingInOuter)) {
                            diiferenceMap.put(at + " Inside check with Outside", getKeyFromID(nonMatchingInInner, request.getEnvironment()));
                        }
                    } else if (CollectionUtils.isEmpty(outerIds) && CollectionUtils.isNotEmpty(innerIds)) {
                        diiferenceMap.put(at + " Outside is empty with Inside having ", getKeyFromID(innerIds, request.getEnvironment()));

                    } else if (CollectionUtils.isNotEmpty(outerIds) && CollectionUtils.isEmpty(innerIds)) {
                        diiferenceMap.put(at + " Inside is empty with Outside having ", getKeyFromID(outerIds, request.getEnvironment()));
                    }
                });

                attributesToCompareWithOE.forEach(at -> {
                    Attribute outerAttr = product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals(at)).findFirst().orElse(null);
                    List<AttributePlainEnumValue> outerEnums = null;
                    if (outerAttr != null) {
                        outerEnums = (List<AttributePlainEnumValue>) outerAttr.getValue();
                    }
                    List<AttributePlainEnumValue> innerEnums = getInnerEnums(innerOuterAttributeMap.get(at), offerEligibilityFirstElement);
                    if (CollectionUtils.isNotEmpty(outerEnums) && CollectionUtils.isNotEmpty(innerEnums)) {
                        List<String> outerEnumKeys = outerEnums.stream().filter(Objects::nonNull).map(AttributePlainEnumValue::getKey).collect(Collectors.toList());
                        List<String> innerEnumKeys = innerEnums.stream().filter(Objects::nonNull).map(AttributePlainEnumValue::getKey).collect(Collectors.toList());
                        List<String> nonMatchingInOuter = outerEnumKeys.stream().filter(id -> !innerEnumKeys.contains(id)).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(nonMatchingInOuter)) {
                            diiferenceMap.put(at + " Outside check with Inside", nonMatchingInOuter);
                        }
                        List<String> nonMatchingInInner = innerEnumKeys.stream().filter(id -> !outerEnumKeys.contains(id)).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(nonMatchingInInner)) {
                            diiferenceMap.put(at + " Inside check with Outside", nonMatchingInInner);
                        }
                    } else if (CollectionUtils.isEmpty(outerEnums) && CollectionUtils.isNotEmpty(innerEnums)) {
                        diiferenceMap.put(at + " Outside is empty with Inside having ", innerEnums.stream().filter(Objects::nonNull).map(AttributePlainEnumValue::getKey).collect(Collectors.toList()));

                    } else if (CollectionUtils.isNotEmpty(outerEnums) && CollectionUtils.isEmpty(innerEnums)) {
                        diiferenceMap.put(at + " Inside is empty with Outside having ", outerEnums.stream().filter(Objects::nonNull).map(AttributePlainEnumValue::getKey).collect(Collectors.toList()));
                    }
                });

                attributes.forEach(attribute -> {
                    if (attribute.getValue() == null){
                        diiferenceMap.put(attribute.getName() + " is null", Collections.singletonList(product.getKey()));
                    }
                    if (diiferenceMap.isEmpty()) {
                        returnMap.put(product.getKey(), diiferenceMap);
                    }
                });
            } catch (Exception e) {
                System.out.println("Error in product " + offerCode);
            }
        });
        return returnMap;
    }

    public List<String> getInnerIDs(String insideAttribute, List<ProductReferenceImpl> associatedProductsRefs) {
        List<String> insideIds = new ArrayList<>();
        if (associatedProductsRefs != null) {
            if (CollectionUtils.isNotEmpty(associatedProductsRefs)) {
                List<Object> assocObjReference = new ArrayList<>(associatedProductsRefs);
                assocObjReference.forEach(refObject -> {
                    List<Attribute> productsList = (List<Attribute>) refObject;
                    productsList.forEach(prd -> {
                        if (prd.getName().equalsIgnoreCase(insideAttribute)) {
                            List<Attribute> qualifyingProductValue = (List<Attribute>) prd.getValue();
                            List<Attribute> qpValues = (List<Attribute>) qualifyingProductValue.get(0);
                            qpValues.forEach(qpValue -> {
                                if (qpValue.getName().equalsIgnoreCase("products")) {
                                    List<ProductReferenceImpl> qualifyingPrdRefInside = (List<ProductReferenceImpl>) qpValue.getValue();
                                    qualifyingPrdRefInside.forEach(productReference -> {
                                        insideIds.add(productReference.getId());
                                    });
                                }
                            });
                        }
                    });
                });
            }
        }
        return insideIds;
    }

    public List<AttributePlainEnumValue> getInnerEnums(String insideAttribute, List<Attribute> offerEligibilityReferences) {
        List<AttributePlainEnumValue> insideEnums = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(offerEligibilityReferences)) {
            AttributeImpl attImpl = (AttributeImpl) offerEligibilityReferences.stream().filter(ref -> ref.getName().equalsIgnoreCase(insideAttribute)).findFirst().orElse(null);
            if (attImpl != null && attImpl.getValue() instanceof List) {
                List<AttributePlainEnumValue> attributePlainEnumValues = (List<AttributePlainEnumValue>) attImpl.getValue();
                attributePlainEnumValues.forEach(enumValue -> {
                    insideEnums.add(enumValue);
                });
            }
        }
        return insideEnums;
    }

    private List<String> getKeyFromID(List<String> ids, String environment) {
        List<String> keys = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            List<Product> products = projectRootConfiguration.getEnvironment(environment).products().get().withWhere(IdsAnyOf(ids)).executeBlocking().getBody().getResults();
            keys = products.stream().map(Product::getKey).collect(Collectors.toList());
        }
        return keys;
    }

    private HashMap<String, String> innerOuterMap(){
        HashMap<String, String> map = new HashMap<>();
        map.put("qualifyingProductIds", "qualifyingProducts");
        map.put("bundleProductIds", "bundleProducts");
        map.put("eligibilitySalesChannels", "salesChannelEligibility");
        map.put("eligibilityBusinessSegment", "businessSegment");
        map.put("eligibilityCustomerSegments", "customerSegment");
        return map;
    }

}
