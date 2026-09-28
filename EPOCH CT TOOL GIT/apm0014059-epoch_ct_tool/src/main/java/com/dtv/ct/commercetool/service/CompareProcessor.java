package com.dtv.ct.commercetool.service;

import com.commercetools.api.models.cart_discount.CartDiscount;
import com.commercetools.api.models.channel.Channel;
import com.commercetools.api.models.common.CentPrecisionMoneyImpl;
import com.commercetools.api.models.common.LocalizedString;
import com.commercetools.api.models.common.Price;
import com.commercetools.api.models.custom_object.CustomObjectReferenceImpl;
import com.commercetools.api.models.product.*;
import com.commercetools.api.models.product_type.*;
import com.commercetools.api.models.type.CustomFieldSetTypeImpl;
import com.commercetools.api.models.type.FieldContainer;
import com.commercetools.api.models.type.Type;
import com.dtv.ct.commercetool.config.ProjectRootConfiguration;
import com.dtv.ct.commercetool.model.ProductUpdateRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;

@Component
public class CompareProcessor {

    @Autowired
    ProjectRootConfiguration projectRootConfiguration;

    public static String fileName = null;

    List<String> skipList = Arrays.asList("compliance", "offerPromos", "dynamicUpgradeValues", "eligibilityDynamicattributesbykey", "epochGlobalConfigurations");

    public static String keyIsAnyOf(List<String> offerKeys) {
        if (offerKeys != null && !offerKeys.isEmpty()) {
            StringJoiner joiner = new StringJoiner("\",\"", "\"", "\"");
            offerKeys.forEach(joiner::add);
            return "id in (" + joiner.toString() + ")";
        }
        return null;
    }

    public static void appendToFile(String data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(data);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void clearFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            // This will clear the file content if it exists
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> compare(ProductUpdateRequest request) {
        fileName = "compareDetails.txt";
        clearFile();
        ProductType productType = projectRootConfiguration.getEnvironment(request.getSource()).productTypes().withKey("offer").get().executeBlocking().getBody();
        Map<String, String> attrToFieldMapNonSet = attrToFieldMaps(productType.getAttributes(), false);
        Map<String, String> attrToFieldMapSet = attrToFieldMaps(productType.getAttributes(), true);
        //  Map<String, String> attrToFieldMapSettemp = attrToFieldMaps(productType.getAttributes(), true);
//        attrToFieldMapSet.clear();
//        attrToFieldMapSet.put("descriptionsByKey", attrToFieldMapSettemp.get("descriptionsByKey"));
//        attrToFieldMapSet.put("additionalEligibility", attrToFieldMapSettemp.get("additionalEligibility"));
        request.getOfferCodes().forEach(offerCode -> {
            System.out.println(offerCode);
            appendToFile("OFFER_NAME->" + offerCode);
            Product sourceProduct = null;
            Product targetProduct = null;
            try {
                sourceProduct = projectRootConfiguration.getEnvironment(request.getSource()).products().withKey(offerCode).get().executeBlocking().getBody();
                targetProduct = projectRootConfiguration.getEnvironment(request.getTarget()).products().withKey(offerCode).get().executeBlocking().getBody();
            } catch (Exception exception) {
                appendToFile("OFFER NOT FOUND ->" + offerCode);
                appendToFile("-----------------------------------------------------------------------------------");
            }
            if (ObjectUtils.allNotNull(sourceProduct, targetProduct)) {
                compareCommon(sourceProduct, targetProduct);
                compareAllNonSet(sourceProduct, targetProduct, attrToFieldMapNonSet, productType, request);
                compareAllSet(sourceProduct, targetProduct, attrToFieldMapSet, productType, request);
                compareComplexNested(sourceProduct, targetProduct, "offerPromos");
                appendToFile("-----------------------------------------------------------------------------------");
            }
        });

        return Collections.singletonMap("Products", fileName);
    }

    public Map<String, String> compareProducts(ProductUpdateRequest request) {
        fileName = "compareDetails_" + request.getUserId() + ".txt";
        clearFile();
        List<Type> types = projectRootConfiguration.getEnvironment(request.getSource()).types().get().withLimit(100).executeBlocking().getBody().getResults();
        List<Channel> sourceChannels = projectRootConfiguration.getEnvironment(request.getSource()).channels().get().withLimit(500).executeBlocking().getBody().getResults();
        List<Channel> targetChannels = projectRootConfiguration.getEnvironment(request.getTarget()).channels().get().withLimit(500).executeBlocking().getBody().getResults();
        Type typeObj = types.stream().filter(t -> t.getKey().equalsIgnoreCase("custom-price")).findFirst().orElse(null);
        Map<String, ProductType> productTypeMap = getAllProductType(request.getSource());
        Map<String, Map<String, String>> attrToFieldMapNonSet = getMapNonSets(productTypeMap);
        Map<String, Map<String, String>> attrToFieldMapSet = getMapSets(productTypeMap);
        request.getOfferCodes().forEach(offerCode -> {
            System.out.println(offerCode);
            appendToFile("PRODUCT/OFFFER>" + offerCode);
            Product sourceProduct = null;
            Product targetProduct = null;
            String productType = null;
            try {
                sourceProduct = projectRootConfiguration.getEnvironment(request.getSource()).products().withKey(offerCode).get().executeBlocking().getBody();
                targetProduct = projectRootConfiguration.getEnvironment(request.getTarget()).products().withKey(offerCode).get().executeBlocking().getBody();
            } catch (Exception exception) {
                appendToFile("PRODUCT/OFFER NOT FOUND ->" + offerCode);
                appendToFile("-----------------------------------------------------------------------------------");
            }
            if (ObjectUtils.allNotNull(sourceProduct, targetProduct)) {
                productType = sourceProduct.getProductType().getId();
                compareCommon(sourceProduct, targetProduct);
                compareAllNonSet(sourceProduct, targetProduct, attrToFieldMapNonSet.get(productType), productTypeMap.get(productType), request);
                compareAllSet(sourceProduct, targetProduct, attrToFieldMapSet.get(productType), productTypeMap.get(productType), request);
                compareComplexNested(sourceProduct, targetProduct, "offerPromos");
                priceCompare(sourceProduct, targetProduct, typeObj, sourceChannels, targetChannels, request);
                variantsChecks(sourceProduct, targetProduct, attrToFieldMapNonSet.get(productType), attrToFieldMapSet.get(productType));
                appendToFile("-----------------------------------------------------------------------------------");
            }
        });
        return Collections.singletonMap("Products", fileName);
    }

    private Map<String, ProductType> getAllProductType(String environment) {
        Map<String, ProductType> productTypeMap = new HashMap<>();
        List<ProductType> productTypes = projectRootConfiguration.getEnvironment(environment).productTypes().get().withLimit(500).executeBlocking().getBody().getResults();
        productTypes.forEach(productType -> {
            if (productType.getKey().equalsIgnoreCase("offer") || productType.getKey().equalsIgnoreCase("video-plan") ||
                    productType.getKey().equalsIgnoreCase("video-addon") || productType.getKey().equalsIgnoreCase("video-device") ||
                    productType.getKey().equalsIgnoreCase("fee") || productType.getKey().equalsIgnoreCase("reward") ||
                    productType.getKey().equalsIgnoreCase("protection-plan") || productType.getKey().equalsIgnoreCase("global-epoch-configurations")){
                productTypeMap.put(productType.getId(), productType);
            }
        });
        return productTypeMap;
    }

    private Map<String, Map<String, String>> getMapNonSets(Map<String, ProductType> productTypeMap) {
        Map<String, Map<String, String>> map = new HashMap<>();
        productTypeMap.entrySet().forEach(key -> {
            map.put(key.getKey(), attrToFieldMaps(key.getValue().getAttributes(), false));
        });
        return map;
    }

    private Map<String, Map<String, String>> getMapSets(Map<String, ProductType> productTypeMap) {
        Map<String, Map<String, String>> map = new HashMap<>();
        productTypeMap.entrySet().forEach(key -> {
            map.put(key.getKey(), attrToFieldMaps(key.getValue().getAttributes(), true));
        });
        return map;
    }

    private void compareCommon(Product source, Product target) {
        LocalizedString sourceName = source.getMasterData().getCurrent().getName();
        LocalizedString targetName = target.getMasterData().getCurrent().getName();
        String nameEnSource = sourceName.get("en");
        String nameEnTarget = targetName.get("en");
        if (!nameEnSource.equals(nameEnTarget)) {
            appendToFile("Name: " + nameEnSource + " Target: " + nameEnTarget);
        }

        LocalizedString sourceDescription = source.getMasterData().getCurrent().getDescription();
        LocalizedString targetDescription = target.getMasterData().getCurrent().getDescription();
        String descriptionEnSource = sourceDescription.get("en");
        String descriptionEnTarget = targetDescription.get("en");
        if (!descriptionEnSource.equals(descriptionEnTarget)) {
            appendToFile("Description: " + descriptionEnSource + " Target: " + descriptionEnTarget);
        }
    }

    public void compareAllNonSet(Product sourceProduct, Product targetProduct, Map<String, String> attrToFieldMap, ProductType productType, ProductUpdateRequest request) {
        attrToFieldMap.forEach((attr, type) -> {
            if ("text".equals(type) || "ltext".equals(type)) {
                textCompare(sourceProduct, targetProduct, attr);
            }

            if ("enum".equalsIgnoreCase(type)) {
                enumCompare(sourceProduct, targetProduct, attr);
            }

            if ("number".equalsIgnoreCase(type)) {
                numberCompare(sourceProduct, targetProduct, attr);
            }

            if ("boolean".equalsIgnoreCase(type)) {
                booleanCompare(sourceProduct, targetProduct, attr);
            }

            if ("dateTime".equalsIgnoreCase(type)) {
                dateTimeCompare(sourceProduct, targetProduct, attr);
            }

            if ("nested".equalsIgnoreCase(type)) {
                String id = AttributeNestedTypeImplID(productType, attr);
                ProductType nestedProductType = projectRootConfiguration.getEnvironment(request.getSource()).productTypes().withId(id).get().executeBlocking().getBody();
                Map<String, String> attrToFieldMapNested = attrToFieldMaps(nestedProductType.getAttributes(), false);
                Map<String, String> attrToFieldMapNestedSet = attrToFieldMaps(nestedProductType.getAttributes(), true);
                attrToFieldMapNested.putAll(attrToFieldMapNestedSet);
                Attribute sourceValue = sourceProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
                Attribute targetValue = targetProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
                List<AttributeImpl> sourceNestedAttributes = sourceValue != null ? (List<AttributeImpl>) sourceValue.getValue() : null;
                List<AttributeImpl> targetNestedAttributes = targetValue != null ? (List<AttributeImpl>) targetValue.getValue() : null;
                if (sourceNestedAttributes != null && targetNestedAttributes != null) {
                    attrToFieldMapNested.forEach((nestedAttr, nestedType) -> {
                        if ("text".equals(nestedType) || "ltext".equals(nestedType)) {
                            textCompareNested(sourceNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttr)).findFirst().orElse(null), targetNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttr)).findFirst().orElse(null), nestedAttr, attr);
                        }

                        if ("reference".equals(nestedType)) {
                            productReferenceCompareNest(sourceNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttr)).findFirst().orElse(null), targetNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttr)).findFirst().orElse(null), nestedAttr, request, attr);
                        }

                        if ("enum".equalsIgnoreCase(nestedType)) {
                            enumCompareNestedSet(sourceNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttr)).findFirst().orElse(null), targetNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttr)).findFirst().orElse(null), nestedAttr, attr);
                        }
                    });
                } else if (sourceNestedAttributes != null && targetNestedAttributes == null) {
                    sourceNestedAttributes.forEach(attrImpl -> {
                        System.out.println("Attribute: " + attr + " Nested Attribute: " + attrImpl.getName() + " Source: " + attrImpl.getValue() + " Target: " + "null");
                        appendToFile("Attribute: " + attr + " Nested Attribute: " + attrImpl.getName() + " Source: " + attrImpl.getValue() + " Target: " + "null");
                    });
                }
            }
        });
    }

    public void compareAllSet(Product sourceProduct, Product targetProduct, Map<String, String> attrToFieldMap, ProductType productType, ProductUpdateRequest request) {
        attrToFieldMap.forEach((attr, type) -> {
            System.out.println("Attribute: " + attr + " Type: " + type);
            if ("reference".equals(type)) {
                productReferenceCompare(sourceProduct, targetProduct, attr, request);
            }

            if ("enum".equalsIgnoreCase(type)) {
                enumSetCompare(sourceProduct, targetProduct, attr);
            }

            if ("text".equals(type) || "ltext".equals(type)) {
                textSetCompare(sourceProduct, targetProduct, attr);
            }

            if ("number".equalsIgnoreCase(type)) {
                numberSetCompare(sourceProduct, targetProduct, attr);
            }

            if ("nested".equalsIgnoreCase(type) && !skipList.contains(attr)) {
                nestedCompareForNonSet(sourceProduct, targetProduct, attr, request, productType);
                nestedCompareForSet(sourceProduct, targetProduct, attr, request, productType);
            }

//            if ("epochGlobalConfigurations".equalsIgnoreCase(attr)) {
//                compareGlobalConfig(sourceProduct, targetProduct, attr);
//            }

        });
    }
//
//    private void compareGlobalConfig(Product sourceProduct, Product targetProduct, String attr) {
//        Attribute sourceValue = sourceProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
//        Attribute targetValue = targetProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
//        List<ArrayList> sourceNestedAttributesAll = sourceValue != null ? (List<ArrayList>) sourceValue.getValue() : null;
//        List<ArrayList> targetNestedAttributesAll = targetValue != null ? (List<ArrayList>) targetValue.getValue() : null;
//
//       for (ArrayList attribute :  sourceNestedAttributesAll){
//           List<AttributeImpl> srcAttributes = (List<AttributeImpl>) attribute;
//           srcAttributes.forEach(srcAttribut -> {
//               AttributeImpl sourceValue1 = srcAttribut;
//               String namedAttr = sourceValue1.getName();
//               String value = null;
//               if(sourceValue1.getValue() instanceof  String){
//                   value = (String) sourceValue1.getValue();
//               }
//               if (sourceValue1.getValue() instanceof  List<?>) {
//                   value = ((List<String>) sourceValue1.getValue()).toString();
//               }
//
//
//
//           });
//       }
//    }

        private void enumCompare(AttributeImpl sourceValue, AttributeImpl targetValue, String nestedAttr) {
        if (sourceValue != null && targetValue != null && !sourceValue.getValue().equals(targetValue.getValue())) {
            AttributePlainEnumValue sourceEnumValues = (AttributePlainEnumValue) sourceValue.getValue();
            AttributePlainEnumValue targetEnumValues = (AttributePlainEnumValue) targetValue.getValue();
            if (!sourceEnumValues.getKey().equals(targetEnumValues.getKey())) {
                System.out.println("Attribute: " + nestedAttr + " Source: " + sourceEnumValues.getKey() + " Target: " + targetEnumValues.getKey());
                appendToFile("Attribute: " + nestedAttr + " Source: " + sourceEnumValues.getKey() + " Target: " + targetEnumValues.getKey());
            }
        } else if (sourceValue != null && targetValue == null) {
            AttributePlainEnumValue sourceEnumValues = (AttributePlainEnumValue) sourceValue.getValue();
            System.out.println("Attribute: " + nestedAttr + " Source: " + sourceEnumValues.getKey() + " Target: " + "null");
            appendToFile("Attribute: " + nestedAttr + " Source: " + sourceEnumValues.getKey() + " Target: " + "null");
        }
    }

    private void enumCompareNestedSet(AttributeImpl sourceValue, AttributeImpl targetValue, String nestedAttr, String mainAttribute) {
        if (sourceValue != null && targetValue != null && sourceValue.getValue() instanceof List<?> && targetValue.getValue() instanceof List<?>) {
            List<AttributePlainEnumValue> sourceEnumValues = (List<AttributePlainEnumValue>) sourceValue.getValue();
            List<AttributePlainEnumValue> targetEnumValues = (List<AttributePlainEnumValue>) targetValue.getValue();
            List<String> sourceKey = sourceEnumValues.stream().map(AttributePlainEnumValue::getKey).toList();
            List<String> targetKey = targetEnumValues.stream().map(AttributePlainEnumValue::getKey).toList();
            sourceKey.forEach(sourceEnumValue -> {
                if (!targetKey.contains(sourceEnumValue)) {
                    System.out.println("Attribute: " + mainAttribute + "->" + nestedAttr + " Source: " + sourceEnumValue + " Not in Target: ");
                    appendToFile("Attribute: " + mainAttribute + "->" + nestedAttr + " Source: " + sourceEnumValue + " Not in Target: ");
                }
            });
        } else if (sourceValue != null && targetValue == null && sourceValue.getValue() instanceof List<?>) {
            List<AttributePlainEnumValue> sourceEnumValues = (List<AttributePlainEnumValue>) sourceValue.getValue();
            sourceEnumValues.forEach(sourceEnumValue -> {
                System.out.println("Attribute: " + mainAttribute + "->" + nestedAttr + " Source: " + sourceEnumValue.getKey() + " Target:EMPTY ");
                appendToFile("Attribute: " + mainAttribute + "->" + nestedAttr + " Source: " + sourceEnumValue.getKey() + " Target:EMPTY ");
            });
        } else if (sourceValue != null && targetValue != null) {
            AttributePlainEnumValue sourceEnumValues = (AttributePlainEnumValue) sourceValue.getValue();
            AttributePlainEnumValue targetEnumValues = (AttributePlainEnumValue) targetValue.getValue();
            if (!sourceEnumValues.getKey().equals(targetEnumValues.getKey())) {
                System.out.println("Attribute: " + mainAttribute + "->" + nestedAttr + " Source: " + sourceEnumValues.getKey() + " Target:EMPTY ");
                appendToFile("Attribute: " + mainAttribute + "->" + nestedAttr + " Source: " + sourceEnumValues.getKey() + " Target:EMPTY ");
            }
        }
    }

    private void productReferenceCompareNest(AttributeImpl sourceValue, AttributeImpl targetValue, String nestedAttr, ProductUpdateRequest request, String mainAttribute) {
        if (sourceValue != null && targetValue != null) {
            List<ProductReferenceImpl> sourceReference = (List<ProductReferenceImpl>) sourceValue.getValue();
            List<ProductReferenceImpl> targetReference = (List<ProductReferenceImpl>) targetValue.getValue();

            List<String> sourceIds = sourceReference.stream().map(ProductReferenceImpl::getId).toList();
            List<String> targetIds = targetReference.stream().map(ProductReferenceImpl::getId).toList();

            List<String> sourceKeys = List.of();
            List<String> targetKeys;
            if(CollectionUtils.isNotEmpty(sourceIds)) {
                List<Product> sourceIdsProduct = projectRootConfiguration.getEnvironment(request.getSource()).products().get().withWhere(keyIsAnyOf(sourceIds)).withLimit(500).executeBlocking().getBody().getResults();
                sourceKeys= sourceIdsProduct.stream().map(Product::getKey).toList();
            }
            if(CollectionUtils.isNotEmpty(targetIds)) {
                List<Product> targetIdsProduct = projectRootConfiguration.getEnvironment(request.getTarget()).products().get().withWhere(keyIsAnyOf(targetIds)).withLimit(500).executeBlocking().getBody().getResults();
                targetKeys = targetIdsProduct.stream().map(Product::getKey).toList();
            } else {
                targetKeys = null;
            }

            if (CollectionUtils.isNotEmpty(sourceKeys) && CollectionUtils.isNotEmpty(targetKeys)) {
                sourceKeys.forEach(sourceKey -> {
                    if (!targetKeys.contains(sourceKey)) {
                        System.out.println("Attribute: " + mainAttribute + "->" + nestedAttr + " Source: " + sourceKey + " Not in Target: ");
                        appendToFile("Attribute: " + mainAttribute + "->" + nestedAttr + " Source: " + sourceKey + " Not in Target: ");
                    }
                });
            } else if (CollectionUtils.isNotEmpty(sourceKeys) && CollectionUtils.isEmpty(targetKeys)) {
                sourceKeys.forEach(sourceKey -> {
                    System.out.println("Attribute: " + mainAttribute + "->" + nestedAttr + " Source: " + sourceKey + " Target:EMPTY ");
                    appendToFile("Attribute: " + mainAttribute + "->" + nestedAttr + " Source: " + sourceKey + " Target:EMPTY ");
                });
            } else if (CollectionUtils.isEmpty(sourceKeys) && CollectionUtils.isNotEmpty(targetKeys)) {
                targetKeys.forEach(targetKey -> {
                    System.out.println("Attribute: " + mainAttribute + "->" + nestedAttr + " Source:EMPTY Target: " + targetKey);
                    appendToFile("Attribute: " + mainAttribute + "->" + nestedAttr + " Source:EMPTY Target: " + targetKey);
                });
            }

        } else if (sourceValue != null && targetValue == null) {
            List<ProductReferenceImpl> sourceReference = (List<ProductReferenceImpl>) sourceValue.getValue();
            List<String> sourceIds = sourceReference.stream().map(ProductReferenceImpl::getId).toList();
            List<Product> sourceIdsProduct = projectRootConfiguration.getEnvironment(request.getSource()).products().get().withWhere(keyIsAnyOf(sourceIds)).withLimit(500).executeBlocking().getBody().getResults();
            List<String> sourceKeys = sourceIdsProduct.stream().map(Product::getKey).toList();
            sourceKeys.forEach(sourceKey -> {
                System.out.println("Attribute: " + mainAttribute + "->" + nestedAttr + " Source: " + sourceKey + " Target: ");
                appendToFile("Attribute: " + mainAttribute + "->" + nestedAttr + " Source: " + sourceKey + " Target: ");
            });
        }
    }

    private void textCompareNested(AttributeImpl sourceValue, AttributeImpl targetValue, String nestedAttr, String mainAttribute) {
        if (sourceValue != null && targetValue != null) {
            if (!sourceValue.getValue().equals(targetValue.getValue())) {
                System.out.println("Attribute: " + mainAttribute + "->" + nestedAttr + " Source: " + sourceValue.getValue() + " Target: " + targetValue.getValue());
                appendToFile("Attribute: " + mainAttribute + "->" + nestedAttr + " Source: " + sourceValue.getValue() + " Target: " + targetValue.getValue());
            }
        } else if (sourceValue != null && targetValue == null) {
            System.out.println("Attribute: " + mainAttribute + "->" + nestedAttr + " Source: " + sourceValue.getValue() + " Target: " + "null");
            appendToFile("Attribute: " + mainAttribute + "->" + nestedAttr + " Source: " + sourceValue.getValue() + " Target: " + "null");
        }
    }
    
    private boolean isAttributePresent(Product product, String attr) {
        return product.getMasterData().getCurrent().getMasterVariant().getAttribute(attr) != null;
    }

    private void textCompare(Product sourceProduct, Product targetProduct, String attr) {
        if (isAttributePresent(sourceProduct, attr) && isAttributePresent(targetProduct, attr) ) {
            Attribute sourceValue = sourceProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
            Attribute targetValue = targetProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
            if (!sourceValue.getValue().equals(targetValue.getValue())) {
                System.out.println("Attribute: " + attr + " Source: " + sourceValue.getValue() + " Target: " + targetValue.getValue());
                appendToFile("Attribute: " + attr + " Source: " + sourceValue.getValue() + " Target: " + targetValue.getValue());
            }
        } else if (isAttributePresent(sourceProduct, attr)  && !isAttributePresent(targetProduct, attr) ) {
            Attribute sourceValue = sourceProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
            System.out.println("Attribute: " + attr + " Source: " + sourceValue.getValue() + " Target:EMPTY ");
            appendToFile("Attribute: " + attr + " Source: " + sourceValue.getValue() + " Target:EMPTY ");
        }
    }

    private void enumCompare(Product sourceProduct, Product targetProduct, String attr) {
        if (isAttributePresent(sourceProduct, attr)  && isAttributePresent(targetProduct, attr) ) {
            Attribute sourceValue = sourceProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
            Attribute targetValue = targetProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
            if (sourceValue != null && targetValue != null && !sourceValue.getValue().equals(targetValue.getValue())) {
                AttributePlainEnumValue sourceEnumValues = (AttributePlainEnumValue) sourceValue.getValue();
                AttributePlainEnumValue targetEnumValues = (AttributePlainEnumValue) targetValue.getValue();
                if (!sourceEnumValues.getKey().equals(targetEnumValues.getKey())) {
                    System.out.println("Attribute: " + attr + " Source: " + sourceEnumValues.getKey() + " Target: " + targetEnumValues.getKey());
                    appendToFile("Attribute: " + attr + " Source: " + sourceEnumValues.getKey() + " Target: " + targetEnumValues.getKey());
                }
            }
        } else if (isAttributePresent(sourceProduct, attr)  && !isAttributePresent(targetProduct, attr) ) {
            Attribute sourceValue = sourceProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
            AttributePlainEnumValue sourceEnumValues = (AttributePlainEnumValue) sourceValue.getValue();
            System.out.println("Attribute: " + attr + " Source: " + sourceEnumValues.getKey() + " Target:EMPTY ");
            appendToFile("Attribute: " + attr + " Source: " + sourceEnumValues.getKey() + " Target:EMPTY ");
        }
    }

    private void enumSetCompare(Product sourceProduct, Product targetProduct, String attr) {
        if (isAttributePresent(sourceProduct, attr)  && isAttributePresent(targetProduct, attr) ) {
            Attribute sourceValue = sourceProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
            Attribute targetValue = targetProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
            List<AttributePlainEnumValue> sourceEnumValues = (List<AttributePlainEnumValue>) sourceValue.getValue();
            List<AttributePlainEnumValue> targetEnumValues = (List<AttributePlainEnumValue>) targetValue.getValue();
            List<String> sourceKey = sourceEnumValues.stream().map(AttributePlainEnumValue::getKey).toList();
            List<String> targetKey = targetEnumValues.stream().map(AttributePlainEnumValue::getKey).toList();
            sourceKey.forEach(sourceEnumValue -> {
                if (!targetKey.contains(sourceEnumValue)) {
                    System.out.println("Attribute: " + attr + " Source: " + sourceEnumValue + " Not in Target: ");
                    appendToFile("Attribute: " + attr + " Source: " + sourceEnumValue + " Not in Target: ");
                }
            });
        } else if (isAttributePresent(sourceProduct, attr)  && !isAttributePresent(targetProduct, attr) ) {
            Attribute sourceValue = sourceProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
            List<AttributePlainEnumValue> sourceEnumValues = (List<AttributePlainEnumValue>) sourceValue.getValue();
            sourceEnumValues.forEach(sourceEnumValue -> {
                System.out.println("Attribute: " + attr + " Source: " + sourceEnumValue.getKey() + " Target:EMPTY ");
                appendToFile("Attribute: " + attr + " Source: " + sourceEnumValue.getKey() + " Target:EMPTY ");
            });
        }
    }

    private void textSetCompare(Product sourceProduct, Product targetProduct, String attr) {
        if (isAttributePresent(sourceProduct, attr)  && isAttributePresent(targetProduct, attr) ) {
            Attribute sourceValue = sourceProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
            Attribute targetValue = targetProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
            if (sourceValue != null && targetValue != null) {
                List<String> sourceEnumValues = (List<String>) sourceValue.getValue();
                List<String> targetEnumValues = (List<String>) targetValue.getValue();
                sourceEnumValues.forEach(sourceEnumValue -> {
                    if (!targetEnumValues.contains(sourceEnumValue)) {
                        System.out.println("Attribute: " + attr + " Source: " + sourceEnumValue + " Not in Target: ");
                        appendToFile("Attribute: " + attr + " Source: " + sourceEnumValue + " Not in Target: ");
                    }
                });
            }
        } else if (isAttributePresent(sourceProduct, attr)  && !isAttributePresent(targetProduct, attr) ) {
            Attribute sourceValue1 = sourceProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
            List<String> sourceEnumValues = (List<String>) sourceValue1.getValue();
            sourceEnumValues.forEach(sourceEnumValue -> {
                System.out.println("Attribute: " + attr + " Source: " + sourceEnumValue + " Target:EMPTY ");
                appendToFile("Attribute: " + attr + " Source: " + sourceEnumValue + " Target:EMPTY ");
            });
        }
    }

    private void numberCompare(Product sourceProduct, Product targetProduct, String attr) {
        if (isAttributePresent(sourceProduct, attr)  && isAttributePresent(targetProduct, attr) ) {
            Attribute sourceValue = sourceProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
            Attribute targetValue = targetProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
            Long sourceNumber = (Long) sourceValue.getValue();
            Long targetNumber = (Long) targetValue.getValue();
            if (!sourceNumber.equals(targetNumber)) {
                System.out.println("Attribute: " + attr + " Source: " + sourceNumber + " Target: " + targetNumber);
                appendToFile("Attribute: " + attr + " Source: " + sourceNumber + " Target: " + targetNumber);
            }
        } else if (isAttributePresent(sourceProduct, attr)  && !isAttributePresent(targetProduct, attr) ) {
            Attribute sourceValue = sourceProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
            Long sourceNumber = (Long) sourceValue.getValue();
            System.out.println("Attribute: " + attr + " Source: " + sourceNumber + " Target: " + " Target:EMPTY ");
            appendToFile("Attribute: " + attr + " Source: " + sourceNumber + " Target: " + " Target:EMPTY ");
        }
    }

    private void numberSetCompare(Product sourceProduct, Product targetProduct, String attr) {
        if (isAttributePresent(sourceProduct, attr)  && isAttributePresent(targetProduct, attr) ) {
            Attribute sourceValue = sourceProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
            Attribute targetValue = targetProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
            List<Long> sourceNumber = (List<Long>) sourceValue.getValue();
            List<Long> targetNumber = (List<Long>) targetValue.getValue();
            sourceNumber.forEach(source -> {
                if (!targetNumber.contains(source)) {
                    System.out.println("Attribute: " + attr + " Source: " + source + " Not in Target: ");
                    appendToFile("Attribute: " + attr + " Source: " + source + " Not in Target: ");
                }
            });
        } else if (isAttributePresent(sourceProduct, attr)  && !isAttributePresent(targetProduct, attr) ) {
            Attribute sourceValue = sourceProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
            List<Long> sourceNumber = (List<Long>) sourceValue.getValue();
            sourceNumber.forEach(source -> {
                System.out.println("Attribute: " + attr + " Source: " + source + " Target:EMPTY ");
                appendToFile("Attribute: " + attr + " Source: " + source + " Target:EMPTY ");
            });
        }
    }

    private void booleanCompare(Product sourceProduct, Product targetProduct, String attr) {
        if (isAttributePresent(sourceProduct, attr)  && isAttributePresent(targetProduct, attr) ) {
            Attribute sourceValue = sourceProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
            Attribute targetValue = targetProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
            Boolean sourceBoolean = (Boolean) sourceValue.getValue();
            Boolean targetBoolean = (Boolean) targetValue.getValue();
            if (!sourceBoolean.equals(targetBoolean)) {
                System.out.println("Attribute: " + attr + " Source: " + sourceBoolean + " Target: " + targetBoolean);
                appendToFile("Attribute: " + attr + " Source: " + sourceBoolean + " Target: " + targetBoolean);
            }
        } else if (isAttributePresent(sourceProduct, attr)  && !isAttributePresent(targetProduct, attr) ) {
            Attribute sourceValue = sourceProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
            Boolean sourceBoolean = (Boolean) sourceValue.getValue();
            System.out.println("Attribute: " + attr + " Source: " + sourceBoolean + " Target: " + " Target:EMPTY ");
            appendToFile("Attribute: " + attr + " Source: " + sourceBoolean + " Target: " + " Target:EMPTY ");
        }
    }

    private void dateTimeCompare(Product sourceProduct, Product targetProduct, String attr) {
        if (isAttributePresent(sourceProduct, attr)  && isAttributePresent(targetProduct, attr) ) {
            Attribute sourceValue = sourceProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
            Attribute targetValue = targetProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
            ZonedDateTime sourceDateTime = (ZonedDateTime) sourceValue.getValue();
            ZonedDateTime targetDateTime = (ZonedDateTime) targetValue.getValue();
            if (!sourceDateTime.equals(targetDateTime)) {
                System.out.println("Attribute: " + attr + " Source: " + sourceDateTime + " Target: " + targetDateTime);
                appendToFile("Attribute: " + attr + " Source: " + sourceDateTime + " Target: " + targetDateTime);
            }
        } else if (isAttributePresent(sourceProduct, attr)  && !isAttributePresent(targetProduct, attr) ) {
            Attribute sourceValue = sourceProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
            ZonedDateTime sourceDateTime = (ZonedDateTime) sourceValue.getValue();
            System.out.println("Attribute: " + attr + " Source: " + sourceDateTime + " Target: " + " Target:EMPTY ");
            appendToFile("Attribute: " + attr + " Source: " + sourceDateTime + " Target: " + " Target:EMPTY ");
        }
    }

    private void productReferenceCompare(Product sourceProduct, Product targetProduct, String attr, ProductUpdateRequest request) {
        if (isAttributePresent(sourceProduct, attr)  && isAttributePresent(targetProduct, attr) ) {
            Attribute sourceValue = sourceProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
            Attribute targetValue = targetProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
            List<ProductReferenceImpl> sourceReference = (List<ProductReferenceImpl>) sourceValue.getValue();
            List<ProductReferenceImpl> targetReference = (List<ProductReferenceImpl>) targetValue.getValue();

            List<String> sourceIds = sourceReference.stream().map(ProductReferenceImpl::getId).toList();
            List<String> targetIds = targetReference.stream().map(ProductReferenceImpl::getId).toList();

            List<String> sourceKeys = List.of();
            List<String> targetKeys;
            if (CollectionUtils.isNotEmpty(sourceIds)) {
                List<Product> sourceIdsProduct = projectRootConfiguration.getEnvironment(request.getSource()).products().get().withWhere(keyIsAnyOf(sourceIds)).withLimit(500).executeBlocking().getBody().getResults();
                sourceKeys = sourceIdsProduct.stream().map(Product::getKey).toList();
            }
            if (CollectionUtils.isNotEmpty(targetIds)) {
                List<Product> targetIdsProduct = projectRootConfiguration.getEnvironment(request.getTarget()).products().get().withWhere(keyIsAnyOf(targetIds)).withLimit(500).executeBlocking().getBody().getResults();
                targetKeys = targetIdsProduct.stream().map(Product::getKey).toList();
            } else {
                targetKeys = null;
            }

            if (CollectionUtils.isNotEmpty(sourceKeys) && CollectionUtils.isNotEmpty(targetKeys)) {
                sourceKeys.forEach(sourceKey -> {
                    if (!targetKeys.contains(sourceKey)) {
                        System.out.println("Attribute: " + attr + " Source: " + sourceKey + " Not in Target: ");
                        appendToFile("Attribute: " + attr + " Source: " + sourceKey + " Not in Target: ");
                    }
                });
            } else if (CollectionUtils.isNotEmpty(sourceKeys) && CollectionUtils.isEmpty(targetKeys)) {
                sourceKeys.forEach(sourceKey -> {
                    System.out.println("Attribute: " + attr + " Source: " + sourceKey + " Target:EMPTY ");
                    appendToFile("Attribute: " + attr + " Source: " + sourceKey + " Target:EMPTY ");
                });
            } else if (CollectionUtils.isEmpty(sourceKeys) && CollectionUtils.isNotEmpty(targetKeys)) {
                targetKeys.forEach(targetKey -> {
                    System.out.println("Attribute: " + attr + " Source:EMPTY Target: " + targetKey);
                    appendToFile("Attribute: " + attr + " Source:EMPTY Target: " + targetKey);
                });
            }
        } else if (isAttributePresent(sourceProduct, attr)  && !isAttributePresent(targetProduct, attr) ) {
            Attribute sourceValue = sourceProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
            List<ProductReferenceImpl> sourceReference = (List<ProductReferenceImpl>) sourceValue.getValue();
            if (CollectionUtils.isNotEmpty(sourceReference)) {
                List<String> sourceIds = sourceReference.stream().map(ProductReferenceImpl::getId).toList();
                List<Product> sourceIdsProduct = projectRootConfiguration.getEnvironment(request.getSource()).products().get().withWhere(keyIsAnyOf(sourceIds)).withLimit(500).executeBlocking().getBody().getResults();
                List<String> sourceKeys = sourceIdsProduct.stream().map(Product::getKey).toList();
                sourceKeys.forEach(sourceKey -> {
                    System.out.println("Attribute: " + attr + " Source: " + sourceKey + " Target NOT FOUND");
                    appendToFile("Attribute: " + attr + " Source: " + sourceKey + " Target NOT FOUND");
                });
            }
        }
    }

    private String AttributeNestedTypeImplID(ProductType productType, String attributeName) {
        AttributeDefinition attributeDefinition = productType.getAttributes().stream().filter(attr -> attr.getName().equals(attributeName)).findFirst().get();
        AttributeNestedTypeImpl attributeNestedType = (AttributeNestedTypeImpl) attributeDefinition.getType();
        return attributeNestedType.getTypeReference().getId();
    }

    private String AttributeNestedTypeImplIDForSet(ProductType productType, String attributeName) {
        AttributeDefinition attributeDefinition = productType.getAttributes().stream().filter(attr -> attr.getName().equals(attributeName)).findFirst().get();
        AttributeNestedTypeImpl attributeNestedType = (AttributeNestedTypeImpl) ((AttributeSetTypeImpl) attributeDefinition.getType()).getElementType();
        return attributeNestedType.getTypeReference().getId();
    }

    public Map<String, String> attrToFieldMaps(List<AttributeDefinition> attributes, boolean isSet) {
        Map<String, String> attrToFieldMap = new HashMap<>();
        for (AttributeDefinition attributeDefinition : attributes) {
            if (attributeDefinition.getType() != null && attributeDefinition.getType().getName() != null) {
                String typeName = attributeDefinition.getType().getName();
                if ("set".equals(typeName) && isSet) {
                    String elementTypeName = ((AttributeSetTypeImpl) attributeDefinition.getType()).getElementType().getName();
                    attrToFieldMap.put(attributeDefinition.getName(), elementTypeName);
                } else if (!"set".equals(typeName) && !isSet) {
                    attrToFieldMap.put(attributeDefinition.getName(), typeName);
                }
            }
        }
        attrToFieldMap.keySet().removeAll(attrToFieldMap.keySet().stream().filter(attr -> attr.contains("approval")).toList());
        return attrToFieldMap;
    }

    private void nestedCompareForSet(Product sourceProduct, Product targetProduct, String attr, ProductUpdateRequest request, ProductType productType) {
        String id = AttributeNestedTypeImplIDForSet(productType, attr);
        ProductType nestedProductType = projectRootConfiguration.getEnvironment(request.getSource()).productTypes().withId(id).get().executeBlocking().getBody();
        Map<String, String> attrToFieldMapNestedSet = attrToFieldMaps(nestedProductType.getAttributes(), true);
        Attribute sourceValue = sourceProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
        Attribute targetValue = targetProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
        List<AttributeImpl> sourceNestedAttributesAll = sourceValue != null ? (List<AttributeImpl>) sourceValue.getValue() : null;
        List<AttributeImpl> targetNestedAttributesAll = targetValue != null ? (List<AttributeImpl>) targetValue.getValue() : null;
        int srcSize = sourceNestedAttributesAll != null ? sourceNestedAttributesAll.size() : 0;
        int tarSize = targetNestedAttributesAll != null ? targetNestedAttributesAll.size() : 0;
        if ("descriptionByKey".equalsIgnoreCase(attr) || "displayNamesByKey".equalsIgnoreCase(attr) || "disclosureMessageByKey".equalsIgnoreCase(attr)) {
            validateKeyValuesNested(sourceNestedAttributesAll, targetNestedAttributesAll, attr);
        } else {
            for (int i = 0; i < srcSize; i++) {
                //appendToFile("-----------------------------------------------------------------------------------");
                appendToFile("Attribute: " + attr + " Nested Attribute: " + i);
                List<AttributeImpl> sourceNestedAttributes = sourceNestedAttributesAll != null && sourceNestedAttributesAll.get(i) != null ? (List<AttributeImpl>) sourceNestedAttributesAll.get(i) : null;
                List<AttributeImpl> targetNestedAttributes = targetNestedAttributesAll != null && targetNestedAttributesAll.size() > i && targetNestedAttributesAll.get(i) != null ? (List<AttributeImpl>) targetNestedAttributesAll.get(i) : null;

                if (sourceNestedAttributes != null && targetNestedAttributes != null) {
                    attrToFieldMapNestedSet.forEach((nestedAttr, nestedType) -> {
                        if ("text".equals(nestedType) || "ltext".equals(nestedType)) {
                            textCompareNested(sourceNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttr)).findFirst().orElse(null), targetNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttr)).findFirst().orElse(null), nestedAttr, attr);
                        }

                        if ("reference".equals(nestedType)) {
                            productReferenceCompareNest(sourceNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttr)).findFirst().orElse(null), targetNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttr)).findFirst().orElse(null), nestedAttr, request, attr);
                        }

                        if ("enum".equalsIgnoreCase(nestedType)) {
                            enumCompareNestedSet(sourceNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttr)).findFirst().orElse(null), targetNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttr)).findFirst().orElse(null), nestedAttr, attr);
                        }

                        if ("nested".equalsIgnoreCase(nestedType)) {
                            nestedInsideNested(sourceNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttr)).findFirst().orElse(null), targetNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttr)).findFirst().orElse(null), nestedAttr, nestedProductType, request);
                        }
                    });
                } else if (sourceNestedAttributes != null && targetNestedAttributes == null) {
                    sourceNestedAttributes.forEach(attrImpl -> {
                        if (attrToFieldMapNestedSet.containsKey(attrImpl.getName())) {
                            System.out.println("Attribute: " + attr + " Nested Attribute: " + attrImpl.getName() + " Source: " + attrImpl.getValue() + " Target: " + "null");
                            appendToFile("Attribute: " + attr + " Nested Attribute: " + attrImpl.getName() + " Source: " + attrImpl.getValue() + " Target: " + "null");
                        }
                    });
                }
            }
        }
    }

    private void nestedInsideNested(AttributeImpl sourceValue, AttributeImpl targetValue, String nestedAttr, ProductType productType, ProductUpdateRequest request) {
        String id = AttributeNestedTypeImplIDForSet(productType, nestedAttr);
        ProductType nestedProductType = projectRootConfiguration.getEnvironment(request.getSource()).productTypes().withId(id).get().executeBlocking().getBody();
        Map<String, String> attrToFieldMapNestedSet = attrToFieldMaps(nestedProductType.getAttributes(), true);
        Map<String, String> attrToFieldMapNestedNonSet = attrToFieldMaps(nestedProductType.getAttributes(), false);
        List<AttributeImpl> sourceNestedAttributesAll = sourceValue != null ? (List<AttributeImpl>) sourceValue.getValue() : null;
        List<AttributeImpl> targetNestedAttributesAll = targetValue != null ? (List<AttributeImpl>) targetValue.getValue() : null;
        int srcSize = sourceNestedAttributesAll != null ? sourceNestedAttributesAll.size() : 0;
        int tarSize = targetNestedAttributesAll != null ? targetNestedAttributesAll.size() : 0;
        if ("descriptionByKey".equalsIgnoreCase(nestedAttr) || "displayNamesByKey".equalsIgnoreCase(nestedAttr) || "disclosureMessageByKey".equalsIgnoreCase(nestedAttr)) {
            validateKeyValuesNested(sourceNestedAttributesAll, targetNestedAttributesAll, nestedAttr);
        } else {
            for (int i = 0; i < srcSize; i++) {
                //appendToFile("-----------------------------------------------------------------------------------");
                // appendToFile("Attribute: " + nestedAttr + " Nested Attribute: " + i);
                List<AttributeImpl> sourceNestedAttributes = sourceNestedAttributesAll != null && sourceNestedAttributesAll.get(i) != null ? (List<AttributeImpl>) sourceNestedAttributesAll.get(i) : null;
                List<AttributeImpl> targetNestedAttributes = targetNestedAttributesAll != null && targetNestedAttributesAll.size() > i && targetNestedAttributesAll.get(i) != null ? (List<AttributeImpl>) targetNestedAttributesAll.get(i) : null;

                if (sourceNestedAttributes != null && targetNestedAttributes != null) {
                    attrToFieldMapNestedSet.forEach((nestedAttrSet, nestedTypeSet) -> {
                        if ("text".equals(nestedTypeSet) || "ltext".equals(nestedTypeSet)) {
                            textCompareNested(sourceNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttrSet)).findFirst().orElse(null), targetNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttrSet)).findFirst().orElse(null), nestedAttrSet, nestedAttr);
                        }

                        if ("reference".equals(nestedTypeSet)) {
                            productReferenceCompareNest(sourceNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttrSet)).findFirst().orElse(null), targetNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttrSet)).findFirst().orElse(null), nestedAttrSet, request, nestedAttr);
                        }

                        if ("enum".equalsIgnoreCase(nestedTypeSet)) {
                            enumCompareNestedSet(sourceNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttrSet)).findFirst().orElse(null), targetNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttrSet)).findFirst().orElse(null), nestedAttrSet, nestedAttr);
                        }

                    });

                    attrToFieldMapNestedNonSet.forEach((nestedAttrNonSet, nestedTypeNonSet) -> {
                        if ("text".equals(nestedTypeNonSet) || "ltext".equals(nestedTypeNonSet)) {
                            textCompareNested(sourceNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttrNonSet)).findFirst().orElse(null), targetNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttrNonSet)).findFirst().orElse(null), nestedAttrNonSet, nestedAttr);
                        }

                        if ("enum".equalsIgnoreCase(nestedTypeNonSet)) {
                            enumCompare(sourceNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttrNonSet)).findFirst().orElse(null), targetNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttrNonSet)).findFirst().orElse(null), nestedAttrNonSet);
                        }
                    });
                }
            }
        }
    }

    private void validateKeyValuesNested(List<AttributeImpl> sourceNestedAttributesAll , List<AttributeImpl> targetNestedAttributesAll, String attr) {
        HashMap<String, String> sourceMap = new HashMap<>();
        HashMap<String, String> targetMap = new HashMap<>();

        // Use this (correct):
        if (CollectionUtils.isNotEmpty(sourceNestedAttributesAll)) {
            for (Object nest : sourceNestedAttributesAll) {
                if (nest instanceof List<?>) {
                    List<AttributeImpl> sourceNestedAttributes = ((List<?>) nest).stream()
                            .filter(AttributeImpl.class::isInstance)
                            .map(AttributeImpl.class::cast)
                            .toList();
                    AttributePlainEnumValue attributePlainEnumValue = (AttributePlainEnumValue) sourceNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals("key")).findFirst().get().getValue();
                    String value = sourceNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals("value")).findFirst().get().getValue().toString();
                    sourceMap.put(attributePlainEnumValue.getKey(), value);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(targetNestedAttributesAll)) {
            for (Object nest : targetNestedAttributesAll) {
                if (nest instanceof List<?>) {
                    List<AttributeImpl> targetNestedAttributes = ((List<?>) nest).stream()
                            .filter(AttributeImpl.class::isInstance)
                            .map(AttributeImpl.class::cast)
                            .toList();
                    AttributePlainEnumValue attributePlainEnumValue = (AttributePlainEnumValue) targetNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals("key")).findFirst().get().getValue();
                    String value = targetNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals("value")).findFirst().get().getValue().toString();
                    targetMap.put(attributePlainEnumValue.getKey(), value);
                }
            }
        }


        //compare sourceMap and targetMap
        sourceMap.forEach((key, value) -> {
            if (targetMap.containsKey(key)) {
                if (!value.equals(targetMap.get(key))) {
                    System.out.println("Attribute: " + attr + " Nested Attribute Key: " + key + " Source Value: " + value + " Target Value: " + targetMap.get(key));
                    appendToFile("Attribute: " + attr + " Nested Attribute Key: " + key + " Source Value: " + value + " Target Value: " + targetMap.get(key));
                }
            } else {
                System.out.println("Attribute: " + attr + " Nested Attribute Key: " + key + " Source Value: " + value + " Target Value: null");
                appendToFile("Attribute: " + attr + " Nested Attribute Key: " + key + " Source Value: " + value + " Target Value: null");
            }
        });
    }

    private void nestedCompareForNonSet(Product sourceProduct, Product targetProduct, String attr, ProductUpdateRequest request, ProductType productType) {
        String id = AttributeNestedTypeImplIDForSet(productType, attr);
        ProductType nestedProductType = projectRootConfiguration.getEnvironment(request.getSource()).productTypes().withId(id).get().executeBlocking().getBody();
        Map<String, String> attrToFieldMapNested = attrToFieldMaps(nestedProductType.getAttributes(), false);
        Attribute sourceValue = sourceProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
        Attribute targetValue = targetProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(attr);
        List<AttributeImpl> sourceNestedAttributesAll = sourceValue != null ? (List<AttributeImpl>) sourceValue.getValue() : null;
        List<AttributeImpl> targetNestedAttributesAll = targetValue != null ? (List<AttributeImpl>) targetValue.getValue() : null;
        int srcSize = sourceNestedAttributesAll != null ? sourceNestedAttributesAll.size() : 0;
        int tarSize = targetNestedAttributesAll != null ? targetNestedAttributesAll.size() : 0;

        if ("descriptionByKey".equalsIgnoreCase(attr) || "displayNamesByKey".equalsIgnoreCase(attr) || "disclosureMessageByKey".equalsIgnoreCase(attr)) {
            validateKeyValuesNested(sourceNestedAttributesAll, targetNestedAttributesAll, attr);
        } else {

            for (int i = 0; i < srcSize; i++) {
                //appendToFile("-----------------------------------------------------------------------------------");
                //appendToFile("Attribute: " + attr + " Nested Attribute: " + i);
                System.out.println("Attribute: " + attr + " Nested Attribute: " + i);
                List<AttributeImpl> sourceNestedAttributes = sourceNestedAttributesAll != null && sourceNestedAttributesAll.get(i) != null ? (List<AttributeImpl>) sourceNestedAttributesAll.get(i) : null;
                List<AttributeImpl> targetNestedAttributes = targetNestedAttributesAll != null && targetNestedAttributesAll.size() > i && targetNestedAttributesAll.get(i) != null ? (List<AttributeImpl>) targetNestedAttributesAll.get(i) : null;
                if (sourceNestedAttributes != null && targetNestedAttributes != null) {
                    attrToFieldMapNested.forEach((nestedAttr, nestedType) -> {
                        if ("text".equals(nestedType) || "ltext".equals(nestedType)) {
                            textCompareNested(sourceNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttr)).findFirst().orElse(null), targetNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttr)).findFirst().orElse(null), nestedAttr, attr);
                        }

                        if ("reference".equals(nestedType)) {
                            productReferenceCompareNest(sourceNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttr)).findFirst().orElse(null), targetNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttr)).findFirst().orElse(null), nestedAttr, request, attr);
                        }

                        if ("enum".equalsIgnoreCase(nestedType)) {
                            enumCompareNestedSet(sourceNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttr)).findFirst().orElse(null), targetNestedAttributes.stream().filter(attrImpl -> attrImpl.getName().equals(nestedAttr)).findFirst().orElse(null), nestedAttr, attr);
                        }

                    });
                } else if (sourceNestedAttributes != null && targetNestedAttributes == null) {
                    sourceNestedAttributes.forEach(attrImpl -> {
                        if (attrToFieldMapNested.containsKey(attrImpl.getName())) {
                            System.out.println("Attribute: " + attr + " Nested Attribute: " + attrImpl.getName() + " Source: " + attrImpl.getValue() + " Target: " + "null");
                            appendToFile("Attribute: " + attr + " Nested Attribute: " + attrImpl.getName() + " Source: " + attrImpl.getValue() + " Target: " + "null");
                        }
                    });
                }
            }
        }
    }

    private void compareComplexNested(Product sourceProduct, Product targetProduct, String typeName) {
        Attribute sourceValue = sourceProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(typeName);
        Attribute targetValue = targetProduct.getMasterData().getCurrent().getMasterVariant().getAttribute(typeName);

        List<List<Attribute>> sourceNestedAttributes = sourceValue != null ? (List<List<Attribute>>) sourceValue.getValue() : null;
        List<List<Attribute>> targetNestedAttributes = targetValue != null ? (List<List<Attribute>>) targetValue.getValue() : null;
        Map<String, String> promoMapSource = new HashMap<>();
        if (CollectionUtils.isNotEmpty(sourceNestedAttributes)) {
            sourceNestedAttributes.forEach(attribute -> {
                List<Attribute> attributeList = (List<Attribute>) attribute;
                if (CollectionUtils.isEmpty(attributeList)){
                    return;
                }
                String promoId = attributeList.stream().filter(attr -> attr.getName().equals("promoId")).findFirst().get().getValue().toString();
                String startDate = attributeList.stream().filter(attr -> attr.getName().equals("startDateStr")).findFirst().get().getValue().toString();
                String endDate = attributeList.stream().filter(attr -> attr.getName().equals("endDateStr")).findFirst().get().getValue().toString();
                List<Attribute> targetAttributeList = null;
                //check if promoId present in targetNestedAttributes
                if (CollectionUtils.isNotEmpty(targetNestedAttributes)) {
                    targetAttributeList = targetNestedAttributes.stream().filter(attr -> attr.stream().filter(a -> a.getName().equals("promoId")).findFirst().get().getValue().equals(promoId)).findAny().orElse(null);
                }
                if (CollectionUtils.isEmpty(targetAttributeList)) {
                    System.out.println(typeName + " -PromoId: " + promoId + " Not found in Target");
                    appendToFile(typeName + " -PromoId: " + promoId + " Not found in Target");
                } else {
                    String targetStartDate = targetAttributeList.stream().filter(attr -> attr.getName().equals("startDateStr")).findFirst().get().getValue().toString();
                    String targetEndDate = targetAttributeList.stream().filter(attr -> attr.getName().equals("endDateStr")).findFirst().get().getValue().toString();
                    if (!startDate.equals(targetStartDate)) {
                        System.out.println(typeName + "-PromoId: " + promoId + " StartDate Source: " + startDate + " Target: " + targetStartDate);
                        appendToFile(typeName + "-PromoId: " + promoId + " StartDate Source: " + startDate + " Target: " + targetStartDate);
                    }
                    if (!endDate.equals(targetEndDate)) {
                        System.out.println(typeName + "-PromoId: " + promoId + " EndDate Source: " + endDate + " Target: " + targetEndDate);
                        appendToFile(typeName + "-PromoId: " + promoId + " EndDate Source: " + endDate + " Target: " + targetEndDate);
                    }
                }
            });
        } else if (CollectionUtils.isNotEmpty(targetNestedAttributes)) {
            targetNestedAttributes.forEach(attribute -> {
                List<Attribute> attributeList = (List<Attribute>) attribute;
                String promoId = attributeList.stream().filter(attr -> attr.getName().equals("promoId")).findFirst().get().getValue().toString();
                System.out.println(typeName + " -PromoId: " + promoId + " Not found in Source");
                appendToFile(typeName + " -PromoId: " + promoId + " Not found in Source");
            });

        }
    }

    private void priceCompare(Product sourceProduct, Product targetProduct, Type type, List<Channel> srcChannels, List<Channel> targetChannels, ProductUpdateRequest request) {
        List<Price> sourcePrices = sourceProduct.getMasterData().getCurrent().getMasterVariant().getPrices();
        List<Price> targetPrices = targetProduct.getMasterData().getCurrent().getMasterVariant().getPrices();

        Map<String, String> typeAttributeSet = getTypeAttributeSet(type);
        sourcePrices.forEach(sourcePrice -> {
            Long centAm = sourcePrice.getValue().getCentAmount();
            Channel channel = srcChannels.stream().filter(c -> c.getId().equalsIgnoreCase(sourcePrice.getChannel().getId())).findAny().orElse(null);
            Channel targetChannelKey = targetChannels.stream().filter(c -> c.getKey().equalsIgnoreCase(channel.getKey())).findAny().orElse(null);
            Price targetPrice = targetPrices.stream()
                    .filter(price -> price.getValue().getCentAmount().equals(centAm))
                    .filter(price -> price.getChannel() != null && targetChannelKey != null && price.getChannel().getId().equalsIgnoreCase(targetChannelKey.getId()))
                    .findFirst()
                    .orElse(null);
            if (targetPrice != null) {
                appendToFile("VALIDATING PRICE FOR: " + sourcePrice.getValue().getCurrencyCode() + " " + centAm + "  Against target price" + targetPrice.getValue().getCurrencyCode() + " " + targetPrice.getValue().getCentAmount());
                if (!sourcePrice.getValidFrom().equals(targetPrice.getValidFrom())) {
                    appendToFile("Price Valid From Mismatch: Source: " + sourcePrice.getValidFrom() + " Target: " + targetPrice.getValidFrom());
                }

                if (!sourcePrice.getValidUntil().equals(targetPrice.getValidUntil())) {
                    appendToFile("Price Valid Until Mismatch: Source: " + sourcePrice.getValidUntil() + " Target: " + targetPrice.getValidUntil());
                }

                if (sourcePrice.getCustom() != null && sourcePrice.getCustom().getFields() != null) {
                    FieldContainer fieldContainer = sourcePrice.getCustom().getFields();
                    FieldContainer fieldTargetContainer = targetPrice.getCustom().getFields();
                    compareFieldContainers(fieldContainer, fieldTargetContainer, typeAttributeSet, request);
                }
            } else {
                appendToFile("Price " + centAm + "Not in target");
            }
            appendToFile("-----------------------------------------------------------------------------------");
        });
    }

    private Map<String, String> getTypeAttributeSet(Type type) {
        Map<String, String> typeIdMap = new HashMap<>();
        type.getFieldDefinitions().forEach(fieldDefinition -> {
            String fieldName = fieldDefinition.getName();
            String typeName = fieldDefinition.getType().getName();
            if ("set".equalsIgnoreCase(typeName)) {
                String elementTypeName = ((CustomFieldSetTypeImpl) fieldDefinition.getType()).getElementType().getName();
                typeIdMap.put(fieldName, elementTypeName);
            } else if (!"set".equalsIgnoreCase(typeName)) {
                typeIdMap.put(fieldName, typeName);
            }
        });
        return typeIdMap;
    }

    private void compareFieldContainers(FieldContainer sourceContainer, FieldContainer targetContainer, Map<String, String> attrToFieldMap, ProductUpdateRequest request) {
        if (sourceContainer != null && targetContainer != null) {
            Map<String, Object> sourceFields = sourceContainer.values();
            Map<String, Object> targetFields = targetContainer.values();
            sourceFields.forEach((key, value) -> {
                if (!targetFields.containsKey(key)) {
                    System.out.println("Field: " + key + " in Source: " + value + " Not in Target");
                    appendToFile("Field: " + key + " in Source: " + value + " Not in Target");
                } else {
                    String type = attrToFieldMap.get(key);
                    if (type != null) {
                        if (("Enum".equals(type) || "String".equals(type)) && value instanceof String) {
                            String sourceValue = (String) value;
                            String targetValue = (String) targetFields.get(key);
                            if (!sourceValue.equals(targetValue)) {
                                System.out.println("Field: " + key + " Source: " + sourceValue + " Target: " + targetValue);
                                appendToFile("Field: " + key + " Source: " + sourceValue + " Target: " + targetValue);
                            }
                        } else if ("Number".equals(type)) {
                            Long sourceNumber = (Long) value;
                            Long targetNumber = (Long) targetFields.get(key);
                            if (!sourceNumber.equals(targetNumber)) {
                                System.out.println("Field: " + key + " Source: " + sourceNumber + " Target: " + targetNumber);
                                appendToFile("Field: " + key + " Source: " + sourceNumber + " Target: " + targetNumber);
                            }
                        } else if ("Boolean".equals(type)) {
                            Boolean sourceBoolean = (Boolean) value;
                            Boolean targetBoolean = (Boolean) targetFields.get(key);
                            if (!sourceBoolean.equals(targetBoolean)) {
                                System.out.println("Field: " + key + " Source: " + sourceBoolean + " Target: " + targetBoolean);
                                appendToFile("Field: " + key + " Source: " + sourceBoolean + " Target: " + targetBoolean);
                            }
                        } else if ("Money".equals(type)) {
                            CentPrecisionMoneyImpl sourceMoney = (CentPrecisionMoneyImpl) value;
                            Long srcMoney = sourceMoney.getCentAmount();
                            CentPrecisionMoneyImpl targetMoney = (CentPrecisionMoneyImpl) targetFields.get(key);
                            Long targetMoneyValue = targetMoney.getCentAmount();
                            if (!srcMoney.equals(targetMoneyValue)) {
                                System.out.println("Field: " + key + " Source: " + srcMoney + " Target: " + targetMoneyValue);
                                appendToFile("Field: " + key + " Source: " + srcMoney + " Target: " + targetMoneyValue);
                            }
                        } else if ("Enum".equals(type) && value instanceof List) {
                            List<String> sourceEnum = (List<String>) value;
                            List<String> targetEnum = (List<String>) targetFields.get(key);
                            sourceEnum.forEach(source -> {
                                if (!targetEnum.contains(source)) {
                                    System.out.println("Field: " + key + " Source: " + source + " Not in Target");
                                    appendToFile("Field: " + key + " Source: " + source + " Not in Target");
                                }
                            });
                        } else if (value instanceof LocalizedString) {
                            LocalizedString sourceLocalizedString = (LocalizedString) value;
                            LocalizedString targetLocalizedString = (LocalizedString) targetFields.get(key);

                            String source = sourceLocalizedString.get(Locale.ENGLISH);
                            String target = targetLocalizedString.get(Locale.ENGLISH);

                            if (!source.equalsIgnoreCase(target)) {
                                System.out.println("Field: " + key + " Source: " + source + " Target: " + target);
                                appendToFile("Field: " + key + " Source: " + source + " Target: " + target);
                            }
                        } else if ("Reference".equals(type) && value instanceof List) {
                            List<ProductReferenceImpl> sourceReferences = (List<ProductReferenceImpl>) value;
                            List<ProductReferenceImpl> targetReferences = (List<ProductReferenceImpl>) targetFields.get(key);
                            if (CollectionUtils.isEmpty(targetReferences)) {
                                sourceReferences.forEach(sourceReference -> {
                                    System.out.println("Field: " + key + " Source: " + sourceReference.getId() + " Not in Target");
                                    appendToFile("Field: " + key + " Source: " + sourceReference.getId() + " Not in Target");
                                });
                                return;
                            }
                            List<String> sourceIds = sourceReferences.stream().map(ProductReferenceImpl::getId).toList();
                            List<String> targetIds = targetReferences.stream().map(ProductReferenceImpl::getId).toList();

                            List<Product> sourceIdsProduct = projectRootConfiguration.getEnvironment(request.getSource()).products().get().withWhere(keyIsAnyOf(sourceIds)).withLimit(500).executeBlocking().getBody().getResults();
                            List<Product> targetIdsProduct = projectRootConfiguration.getEnvironment(request.getTarget()).products().get().withWhere(keyIsAnyOf(targetIds)).withLimit(500).executeBlocking().getBody().getResults();

                            List<String> sourceKeys = sourceIdsProduct.stream().map(Product::getKey).toList();
                            List<String> targetKeys = targetIdsProduct.stream().map(Product::getKey).toList();

                            sourceKeys.forEach(sourceKey -> {
                                if (!targetKeys.contains(sourceKey)) {
                                    appendToFile("Field: " + key + " Source: " + sourceKey + " Not in Target");
                                }
                            });
                        } else if (value instanceof CustomObjectReferenceImpl) {

                            //TODO
//                            CustomerReferenceImpl srcCustomRef = (CustomerReferenceImpl) value;
//                            CustomerReferenceImpl targetCustomRef = (CustomerReferenceImpl) targetFields.get(key);
//                            CustomObjectPagedQueryResponse srcCustomObject = projectRootConfiguration.getEnvironment(request.getSource()).customObjects().get().addWhere(srcCustomRef.getId()).executeBlocking().getBody();
//                            CustomObjectPagedQueryResponse targetCustomObject = projectRootConfiguration.getEnvironment(request.getTarget()).customObjects().get().addWhere(targetCustomRef.getId()).executeBlocking().getBody();

                        }
                    }
                }
            });
        }
    }

    public Map<String, String> compareBenefits(ProductUpdateRequest request) {
        fileName = "benefitCompare_" + request.getUserId() + ".txt";
        clearFile();
        List<Type> types = projectRootConfiguration.getEnvironment(request.getSource()).types().get().withLimit(100).executeBlocking().getBody().getResults();
        Type typeObj = types.stream().filter(t -> t.getKey().equalsIgnoreCase("offer-benefit-promotion")).findFirst().orElse(null);
        Map<String, String> typeAttributeSet = getTypeAttributeSet(typeObj);
        request.getBenefitCodes().forEach(benefitCode -> {
            CartDiscount srcCartDiscount = projectRootConfiguration.getEnvironment(request.getSource()).cartDiscounts().withKey(benefitCode).get().executeBlocking().getBody();
            CartDiscount targetCartDiscount = projectRootConfiguration.getEnvironment(request.getTarget()).cartDiscounts().withKey(benefitCode).get().executeBlocking().getBody();
            appendToFile("Comparing Benefit Code: " + benefitCode);
            if (srcCartDiscount != null && targetCartDiscount != null) {
                if (srcCartDiscount.getName() != null && targetCartDiscount.getName() != null) {
                    if (!srcCartDiscount.getName().equals(targetCartDiscount.getName())) {
                        appendToFile("Name Mismatch: Source: " + srcCartDiscount.getName() + " Target: " + targetCartDiscount.getName());
                    }
                } else {
                    appendToFile("Name Mismatch: Source or Target Name is null");
                }
                if (srcCartDiscount.getDescription() != null && targetCartDiscount.getDescription() != null) {
                    if (!srcCartDiscount.getDescription().equals(targetCartDiscount.getDescription())) {
                        appendToFile("Description Mismatch: Source: " + srcCartDiscount.getDescription() + " Target: " + targetCartDiscount.getDescription());
                    }
                } else {
                    appendToFile("Description Mismatch: Source or Target Description is null");
                }
                if (srcCartDiscount.getIsActive() && !targetCartDiscount.getIsActive()) {
                    appendToFile("IsActive Mismatch: Source: " + srcCartDiscount.getIsActive() + " Target: " + targetCartDiscount.getIsActive());
                } else if (!srcCartDiscount.getIsActive() && targetCartDiscount.getIsActive()) {
                    appendToFile("IsActive Mismatch: Source: " + srcCartDiscount.getIsActive() + " Target: " + targetCartDiscount.getIsActive());
                }

                if (srcCartDiscount.getValidFrom() != null && targetCartDiscount.getValidFrom() != null) {
                    if (!srcCartDiscount.getValidFrom().equals(targetCartDiscount.getValidFrom())) {
                        appendToFile("Valid From Mismatch: Source: " + srcCartDiscount.getValidFrom() + " Target: " + targetCartDiscount.getValidFrom());
                    }
                } else {
                    appendToFile("Valid From Mismatch: Source or Target Valid From is null");
                }

                if (srcCartDiscount.getValidUntil() != null && targetCartDiscount.getValidUntil() != null) {
                    if (!srcCartDiscount.getValidUntil().equals(targetCartDiscount.getValidUntil())) {
                        appendToFile("Valid Until Mismatch: Source: " + srcCartDiscount.getValidUntil() + " Target: " + targetCartDiscount.getValidUntil());
                    }
                } else {
                    appendToFile("Valid Until Mismatch: Source or Target Valid Until is null");
                }
                if (srcCartDiscount.getSortOrder() != null && targetCartDiscount.getSortOrder() != null) {
                    if (!srcCartDiscount.getSortOrder().equals(targetCartDiscount.getSortOrder())) {
                        appendToFile("Sort Order Mismatch: Source: " + srcCartDiscount.getSortOrder() + " Target: " + targetCartDiscount.getSortOrder());
                    }
                } else {
                    appendToFile("Sort Order Mismatch: Source or Target Sort Order is null");
                }
                if (srcCartDiscount.getCustom() != null && targetCartDiscount.getCustom() != null) {
                    compareFieldContainers(srcCartDiscount.getCustom().getFields(), targetCartDiscount.getCustom().getFields(), typeAttributeSet, request);
                } else {
                    appendToFile("Custom Fields Mismatch: Source or Target Custom Fields are null");
                }
            } else {
                appendToFile("Source or Target Cart Discount not found for key: " + benefitCode);
            }

            appendToFile("-----------------------------------------------------------------------------------");
        });
        return Collections.singletonMap("Benefit", fileName);
    }

    private void variantsChecks(Product sourceProduct, Product targetProduct, Map<String, String> attrToFieldMap, Map<String, String> attrToFieldSetMap) {
        List<ProductVariant> sourceVariants = sourceProduct.getMasterData().getCurrent().getVariants();
        List<ProductVariant> targetVariants = targetProduct.getMasterData().getCurrent().getVariants();

        if (CollectionUtils.isEmpty(sourceVariants) && CollectionUtils.isEmpty(targetVariants)) {
            appendToFile("Both Source and Target have no variants for MDU");
            return;
        }

        if (CollectionUtils.isEmpty(sourceVariants)) {
            appendToFile("Source has no variants for MDU");
            return;
        }

        if (CollectionUtils.isEmpty(targetVariants)) {
            appendToFile("Target has no variants for MDU");
            return;
        }

        sourceVariants.forEach(sourceVariant -> {
            ProductVariant targetVariant = targetVariants.stream()
                    .filter(variant -> variant.getSku().equals(sourceVariant.getSku()))
                    .findFirst()
                    .orElse(null);

            if (targetVariant == null) {
                appendToFile("Source Variant SKU: " + sourceVariant.getSku() + " not found in Target");
            } else {
                if (!sourceVariant.getKey().equals(targetVariant.getKey())) {
                    appendToFile("Variant Key Mismatch for SKU: " + sourceVariant.getSku() + " Source: " + sourceVariant.getKey() + " Target: " + targetVariant.getKey());
                } else {
                    appendToFile("Variant Key Match for SKU: " + sourceVariant.getSku() + " Key: " + sourceVariant.getKey());
                }

                List<Attribute> sourceAttributes = sourceVariant.getAttributes();
                List<Attribute> targetAttributes = targetVariant.getAttributes();
                compareAllNonSet(sourceAttributes, targetAttributes, attrToFieldMap);
                compareAllSet(sourceAttributes, targetAttributes, attrToFieldSetMap);

            }
        });
    }


    public void compareAllNonSet(List<Attribute> sourceAttributes, List<Attribute> targetAttributes, Map<String, String> attrToFieldMap) {
        attrToFieldMap.forEach((attr, type) -> {
            if ("text".equals(type) || "ltext".equals(type)) {
                textAttributeCompare(sourceAttributes, targetAttributes, attr, false);
            }

            if ("enum".equalsIgnoreCase(type)) {
                enumAttributeCompare(sourceAttributes, targetAttributes, attr, false);
            }

            if ("boolean".equalsIgnoreCase(type)) {
                booleanAttributeCompare(sourceAttributes, targetAttributes, attr);
            }

            if ("number".equalsIgnoreCase(type)) {
                numberAttributeCompare(sourceAttributes, targetAttributes, attr);
            }
        });
    }

    public void compareAllSet(List<Attribute> sourceAttributes, List<Attribute> targetAttributes, Map<String, String> attrToFieldMap) {
        attrToFieldMap.forEach((attr, type) -> {
            if ("text".equals(type) || "ltext".equals(type)) {
                textAttributeCompare(sourceAttributes, targetAttributes, attr, true);
            }

            if ("enum".equalsIgnoreCase(type)) {
                enumAttributeCompare(sourceAttributes, targetAttributes, attr, true);
            }

        });
    }

    private void textAttributeCompare(List<Attribute> sourceAttributes, List<Attribute> targetAttributes, String attr, boolean isSet) {
        if (!isSet) {
            if (isAttributePresent(targetAttributes, attr) && isAttributePresent(sourceAttributes, attr)) {
                Attribute sourceValue = sourceAttributes.stream().filter(attribute -> attribute.getName().equals(attr)).findFirst().orElse(null);
                Attribute targetValue = targetAttributes.stream().filter(attribute -> attribute.getName().equals(attr)).findFirst().orElse(null);
                if (!sourceValue.getValue().equals(targetValue.getValue())) {
                    System.out.println("Attribute: " + attr + " Source: " + sourceValue.getValue() + " Target: " + targetValue.getValue());
                    appendToFile("Attribute: " + attr + " Source: " + sourceValue.getValue() + " Target: " + targetValue.getValue());
                }
            } else if (!isAttributePresent(targetAttributes, attr) && isAttributePresent(sourceAttributes, attr)) {
                Attribute sourceValue = sourceAttributes.stream().filter(attribute -> attribute.getName().equals(attr)).findFirst().orElse(null);
                System.out.println("Attribute: " + attr + " Source: " + sourceValue.getValue() + " Target:EMPTY ");
                appendToFile("Attribute: " + attr + " Source: " + sourceValue.getValue() + " Target:EMPTY ");
            }
        } else {
            if (isAttributePresent(targetAttributes, attr) && isAttributePresent(sourceAttributes, attr)) {
                Attribute sourceValue = sourceAttributes.stream().filter(attribute -> attribute.getName().equals(attr)).findFirst().orElse(null);
                Attribute targetValue = targetAttributes.stream().filter(attribute -> attribute.getName().equals(attr)).findFirst().orElse(null);
                if (sourceValue != null && targetValue != null) {
                    List<String> sourceEnumValues = (List<String>) sourceValue.getValue();
                    List<String> targetEnumValues = (List<String>) targetValue.getValue();
                    sourceEnumValues.forEach(sourceEnumValue -> {
                        if (!targetEnumValues.contains(sourceEnumValue)) {
                            System.out.println("Attribute: " + attr + " Source: " + sourceEnumValue + " Not in Target: ");
                            appendToFile("Attribute: " + attr + " Source: " + sourceEnumValue + " Not in Target: ");
                        }
                    });
                }
            } else if (!isAttributePresent(targetAttributes, attr) && isAttributePresent(sourceAttributes, attr)) {
                Attribute sourceValue1 = sourceAttributes.stream().filter(attribute -> attribute.getName().equals(attr)).findFirst().orElse(null);
                List<String> sourceEnumValues = (List<String>) sourceValue1.getValue();
                sourceEnumValues.forEach(sourceEnumValue -> {
                    System.out.println("Attribute: " + attr + " Source: " + sourceEnumValue + " Target:EMPTY ");
                    appendToFile("Attribute: " + attr + " Source: " + sourceEnumValue + " Target:EMPTY ");
                });
            }
        }
    }

    private void enumAttributeCompare(List<Attribute> sourceAttributes, List<Attribute> targetAttributes, String attr, boolean isSet) {
        if (!isSet) {
            if (isAttributePresent(targetAttributes, attr) && isAttributePresent(sourceAttributes, attr)) {
                Attribute sourceValue = sourceAttributes.stream().filter(attribute -> attribute.getName().equals(attr)).findFirst().orElse(null);
                Attribute targetValue = targetAttributes.stream().filter(attribute -> attribute.getName().equals(attr)).findFirst().orElse(null);
                AttributePlainEnumValue sourceEnumValues = (AttributePlainEnumValue) sourceValue.getValue();
                AttributePlainEnumValue targetEnumValues = (AttributePlainEnumValue) targetValue.getValue();
                if (!sourceEnumValues.getKey().equals(targetEnumValues.getKey())) {
                    System.out.println("Attribute: " + attr + " Source: " + sourceEnumValues.getKey() + " Target: " + targetEnumValues.getKey());
                    appendToFile("Attribute: " + attr + " Source: " + sourceEnumValues.getKey() + " Target: " + targetEnumValues.getKey());
                }
            } else if (!isAttributePresent(targetAttributes, attr) && isAttributePresent(sourceAttributes, attr)) {
                Attribute sourceValue = sourceAttributes.stream().filter(attribute -> attribute.getName().equals(attr)).findFirst().orElse(null);
                AttributePlainEnumValue sourceEnumValues = (AttributePlainEnumValue) sourceValue.getValue();
                System.out.println("Attribute: " + attr + " Source: " + sourceEnumValues.getKey() + " Target: " + "null");
                appendToFile("Attribute: " + attr + " Source: " + sourceEnumValues.getKey() + " Target: " + "null");
            }
        } else {
            // Handle set enum attributes
            if (isAttributePresent(targetAttributes, attr) && isAttributePresent(sourceAttributes, attr)) {
                Attribute sourceValue = sourceAttributes.stream().filter(attribute -> attribute.getName().equals(attr)).findFirst().orElse(null);
                Attribute targetValue = targetAttributes.stream().filter(attribute -> attribute.getName().equals(attr)).findFirst().orElse(null);
                List<AttributePlainEnumValue> sourceEnumValues = (List<AttributePlainEnumValue>) sourceValue.getValue();
                List<AttributePlainEnumValue> targetEnumValues = (List<AttributePlainEnumValue>) targetValue.getValue();
                List<String> sourceKey = sourceEnumValues.stream().map(AttributePlainEnumValue::getKey).toList();
                List<String> targetKey = targetEnumValues.stream().map(AttributePlainEnumValue::getKey).toList();
                sourceKey.forEach(sourceEnumValue -> {
                    if (!targetKey.contains(sourceEnumValue)) {
                        System.out.println("Attribute: " + attr + " Source: " + sourceEnumValue + " Not in Target: ");
                        appendToFile("Attribute: " + attr + " Source: " + sourceEnumValue + " Not in Target: ");
                    }
                });

            } else if (!isAttributePresent(targetAttributes, attr) && isAttributePresent(sourceAttributes, attr)) {
                Attribute sourceValue = sourceAttributes.stream().filter(attribute -> attribute.getName().equals(attr)).findFirst().orElse(null);
                List<AttributePlainEnumValue> sourceEnumValues = (List<AttributePlainEnumValue>) sourceValue.getValue();
                sourceEnumValues.forEach(sourceEnumValue -> {
                    System.out.println("Attribute: " + attr + " Source: " + sourceEnumValue.getKey() + " Target:EMPTY ");
                    appendToFile("Attribute: " + attr + " Source: " + sourceEnumValue.getKey() + " Target:EMPTY ");
                });
            }
        }
    }

    private boolean isAttributePresent(List<Attribute> attributes, String attr) {
        return attributes.stream().anyMatch(attribute -> attribute.getName().equals(attr));
    }

    private void booleanAttributeCompare(List<Attribute> sourceAttributes, List<Attribute> targetAttributes, String attr) {
        if (isAttributePresent(targetAttributes, attr) && isAttributePresent(sourceAttributes, attr)) {
            Attribute sourceValue = sourceAttributes.stream().filter(attribute -> attribute.getName().equals(attr)).findFirst().orElse(null);
            Attribute targetValue = targetAttributes.stream().filter(attribute -> attribute.getName().equals(attr)).findFirst().orElse(null);
            if (!sourceValue.getValue().equals(targetValue.getValue())) {
                System.out.println("Attribute: " + attr + " Source: " + sourceValue.getValue() + " Target: " + targetValue.getValue());
                appendToFile("Attribute: " + attr + " Source: " + sourceValue.getValue() + " Target: " + targetValue.getValue());
            }
        } else if (!isAttributePresent(targetAttributes, attr) && isAttributePresent(sourceAttributes, attr)) {
            Attribute sourceValue = sourceAttributes.stream().filter(attribute -> attribute.getName().equals(attr)).findFirst().orElse(null);
            System.out.println("Attribute: " + attr + " Source: " + sourceValue.getValue() + " Target:EMPTY ");
            appendToFile("Attribute: " + attr + " Source: " + sourceValue.getValue() + " Target:EMPTY ");
        }
    }

    private void numberAttributeCompare(List<Attribute> sourceAttributes, List<Attribute> targetAttributes, String attr) {
        if (isAttributePresent(targetAttributes, attr) && isAttributePresent(sourceAttributes, attr)) {
            Attribute sourceValue = sourceAttributes.stream().filter(attribute -> attribute.getName().equals(attr)).findFirst().orElse(null);
            Attribute targetValue = targetAttributes.stream().filter(attribute -> attribute.getName().equals(attr)).findFirst().orElse(null);
            if (!sourceValue.getValue().equals(targetValue.getValue())) {
                System.out.println("Attribute: " + attr + " Source: " + sourceValue.getValue() + " Target: " + targetValue.getValue());
                appendToFile("Attribute: " + attr + " Source: " + sourceValue.getValue() + " Target: " + targetValue.getValue());
            }
        } else if (!isAttributePresent(targetAttributes, attr) && isAttributePresent(sourceAttributes, attr)) {
            Attribute sourceValue = sourceAttributes.stream().filter(attribute -> attribute.getName().equals(attr)).findFirst().orElse(null);
            System.out.println("Attribute: " + attr + " Source: " + sourceValue.getValue() + " Target:EMPTY ");
            appendToFile("Attribute: " + attr + " Source: " + sourceValue.getValue() + " Target:EMPTY ");
        }
    }

    public Map<String, String> epochGlobalConfigCompare(ProductUpdateRequest request) {
        fileName = "epochGlobalConfigurations_" + request.getUserId() + ".txt";
        clearFile();
        Product srcProduct = projectRootConfiguration.getEnvironment(request.getSource()).products().withKey("epochGlobalConfigurations").get().executeBlocking().getBody();
        Product targetProduct = projectRootConfiguration.getEnvironment(request.getTarget()).products().withKey("epochGlobalConfigurations").get().executeBlocking().getBody();

        List<Attribute> srcAttributes = srcProduct.getMasterData().getCurrent().getMasterVariant().getAttributes();
        List<Attribute> targetAttributes = targetProduct.getMasterData().getCurrent().getMasterVariant().getAttributes();

        List<AttributeImpl> epochSrcAttributes = srcAttributes.stream().filter(attribute -> attribute.getName().equalsIgnoreCase("epochGlobalConfigurations")).map(attribute -> (AttributeImpl) attribute).toList();
        List<AttributeImpl> epochTargetAttributes = targetAttributes.stream().filter(attribute -> attribute.getName().equalsIgnoreCase("epochGlobalConfigurations")).map(attribute -> (AttributeImpl) attribute).toList();

        List<List<AttributeImpl>> srcNestedAttributes = epochSrcAttributes.stream().map(attribute -> (List<AttributeImpl>) attribute.getValue()).toList();
        List<List<AttributeImpl>> targetNestedAttributes = epochTargetAttributes.stream().map(attribute -> (List<AttributeImpl>) attribute.getValue()).toList();

        List<AttributeImpl> flatSrcAttributes = srcNestedAttributes.stream().flatMap(List::stream).toList();
        List<AttributeImpl> flatTargetAttributes = targetNestedAttributes.stream().flatMap(List::stream).toList();

        HashMap<String, List<String>> srcMap = getEpochConfigMap(flatSrcAttributes);
        HashMap<String, List<String>> targetMap = getEpochConfigMap(flatTargetAttributes);

        srcMap.forEach((key, value) -> {
            if (targetMap.containsKey(key)) {
                List<String> targetValue = targetMap.get(key);
                value.forEach(srcVal -> {
                    if (!targetValue.contains(srcVal)) {
                        System.out.println("Config Key: " + key + " Source Value: " + srcVal + " Not Matching with" + targetValue);
                        appendToFile("Config Key: " + key + " Source Value: " + srcVal + " Not Matching with" + targetValue);
                    }
                });
            } else {
                value.forEach(srcVal -> {
                    System.out.println("Config Key: " + key + " Source Value: " + srcVal + " Not in Target");
                    appendToFile("Config Key: " + key + " Source Value: " + srcVal + " Not in Target");
                });
            }
        });

        return Collections.singletonMap("epochGlobalConfigurations", fileName);
    }

    private HashMap<String, List<String>> getEpochConfigMap(List<AttributeImpl> configAttributes) {
        HashMap<String, List<String>> targetMap = new HashMap<>();
        if (configAttributes instanceof List<AttributeImpl>) {
            List<AttributeImpl> list = (List<AttributeImpl>) configAttributes;
            for (Object item : list) {
                if (item instanceof ArrayList<?>) {
                    String configKey = "";
                    List<String> configValue = new ArrayList<>();
                    ArrayList arrayList = (ArrayList) item;
                    for (Object obj : arrayList) {
                        if (obj instanceof AttributeImpl) {
                            AttributeImpl attribute = (AttributeImpl) obj;
                            if (attribute.getName().equals("globalConfigKey")) {
                                configKey = (String) attribute.getValue();
                            } else if (attribute.getName().equals("globalConfigValue")) {
                                configValue = (List<String>) attribute.getValue();
                            }
                        }
                    }
                    targetMap.put(configKey, configValue);
                }
            }
        }
        return targetMap;
    }
}
