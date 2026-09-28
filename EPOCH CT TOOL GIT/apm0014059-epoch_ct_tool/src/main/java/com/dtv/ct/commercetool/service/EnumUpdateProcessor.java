package com.dtv.ct.commercetool.service;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.models.product.*;
import com.commercetools.api.models.product_type.*;
import com.dtv.ct.commercetool.config.ProjectRootConfiguration;
import com.dtv.ct.commercetool.model.ProductUpdateRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Component
public class EnumUpdateProcessor {

    @Autowired
    ProjectRootConfiguration projectRootConfiguration;


    private List<String> storeIdsToAdd(ProductType productType, List<String> storeIds, String attributeKey){

        AttributeDefinition attributeDefinition = productType.getAttribute(attributeKey);

        AttributeSetTypeImpl attributeSetType = (AttributeSetTypeImpl) attributeDefinition.getType();

        AttributeEnumType attributeEnumType = (AttributeEnumType) attributeSetType.getElementType();

        List<AttributePlainEnumValue> plainEnumValues11 = attributeEnumType.getValues();

        List<String> storeIdNotConfigured = new ArrayList<>();
        //find storeIds which is not present in plainEnumValues
        storeIds.forEach(storeId -> {
            if (plainEnumValues11.stream().noneMatch(attr -> attr.getKey().equals(storeId))) {
                storeIdNotConfigured.add(storeId);
            }
        });

        return storeIdNotConfigured;
    }
    public void storeIDUpdate(ProductUpdateRequest request) {
        request.getStoreIdUpdatesList().forEach(sid -> {
            try {
                ProductType productType = projectRootConfiguration.getEnvironment(request.getEnvironment()).productTypes().withKey("offer").get().executeBlocking().getBody();

                List<String> enumToAddIfMissing = storeIdsToAdd(productType, sid.getStoreIds(), sid.getAttributeName());

                if (enumToAddIfMissing.size() > 0) {
                    addPlainEnum(enumToAddIfMissing, sid.getAttributeName(), productType, request.getEnvironment());
                }

                Product product = projectRootConfiguration.getEnvironment(request.getEnvironment()).products().withKey(sid.getOfferId()).get().executeBlocking().getBody();

                List<Attribute> attributes = product.getMasterData().getCurrent().getMasterVariant().getAttributes();

                Attribute attribute = attributes.stream().filter(attr -> attr.getName().equals(sid.getAttributeName())).findFirst().orElse(null);
                Object value = null;
                if (attribute!=null && attribute.getValue()!=null) {
                    value = attribute.getValue();
                }
                List<ProductUpdateAction> updateActions = new ArrayList<>();

                AtomicReference<List<AttributePlainEnumValue>> plainEnumValues = new AtomicReference<>((List<AttributePlainEnumValue>) value);

                //if storeId already present in the offer then removing from request
                //sid.getStoreIds().removeIf(storeId -> plainEnumValues.stream().anyMatch(attr -> attr.getKey().equals(storeId)));

                List<String> updatedStoreIds = sid.getStoreIds().stream().filter(s -> plainEnumValues.get() != null && plainEnumValues.get().stream().noneMatch(attributePlainEnumValue -> attributePlainEnumValue.getKey().equalsIgnoreCase(s))).collect(Collectors.toList());
                if (plainEnumValues.get() == null) {
                    updatedStoreIds = sid.getStoreIds().stream().filter(s -> !s.trim().isEmpty()).collect(Collectors.toList());
                }

                updatedStoreIds = updatedStoreIds.stream().distinct().collect(Collectors.toList());

                if (CollectionUtils.isNotEmpty(sid.getRemoveIds())) {
                    sid.getRemoveIds().forEach(removeId -> {
                        plainEnumValues.get().removeIf(attr -> attr.getKey().equals(removeId));
                    });
                }

                updatedStoreIds.forEach(storeId -> {
                    if(plainEnumValues.get() == null) {
                        plainEnumValues.set(new ArrayList<>());
                    }
                    AttributePlainEnumValueBuilder attributePlainEnumValueBuilder = AttributePlainEnumValueBuilder.of().key(storeId).label(storeId);
                    plainEnumValues.get().add(attributePlainEnumValueBuilder.build());
                });
                updateActions.add(ProductSetAttributeActionBuilder.of().variantId(Long.valueOf(1)).name(sid.getAttributeName()).value(plainEnumValues).build());
                updateActions.add(ProductPublishActionBuilder.of().build());

                Product updatedProduct = projectRootConfiguration.getEnvironment(request.getEnvironment())
                        .products()
                        .withKey(product.getKey())
                        .post(ProductUpdateBuilder.of().actions(updateActions).version(product.getVersion()).build())
                        .executeBlocking()
                        .getBody();

                System.out.println("Update done for " + updatedProduct.getKey());
            } catch (Exception e) {
                System.out.println("Exception in StoreIDUpdate");
                e.printStackTrace();
            }
        });
    }


        public void addPlainEnum(List<String> storeIds, String attributeKey, ProductType productType, String environment) {
            try {
                // first 500 storeIds to pick up and then next using for loop
                List<String> storeIdsToAdd = storeIds.stream().limit(500).collect(Collectors.toList());
                //next 500 storeIds to pick up
                //List<String> storeIds2 = storeIds.stream().skip(500).collect(Collectors.toList());
                List<ProductTypeUpdateAction> productTypeUpdateActions = new ArrayList<>();

                storeIdsToAdd.forEach(storeId -> {
                    AttributePlainEnumValue attributePlainEnumValue = AttributePlainEnumValueBuilder.of().key(storeId).label(storeId).build();
                    ProductTypeUpdateAction productTypeUpdateAction = ProductTypeUpdateAction.addPlainEnumValueBuilder().attributeName(attributeKey).value(attributePlainEnumValue).build();
                    productTypeUpdateActions.add(productTypeUpdateAction);
                });


                ProductType updatedProductType = projectRootConfiguration.getEnvironment(environment)
                        .productTypes()
                        .withKey(productType.getKey())
                        .post(ProductTypeUpdateBuilder.of().actions(productTypeUpdateActions).version(productType.getVersion()).build())
                        .executeBlocking()
                        .getBody();

//                List<ProductTypeUpdateAction> productTypeUpdateActions2 = new ArrayList<>();
//                storeIds2.forEach(storeId -> {
//                    AttributePlainEnumValue attributePlainEnumValue = AttributePlainEnumValueBuilder.of().key(storeId).label(storeId).build();
//                    ProductTypeUpdateAction productTypeUpdateAction = ProductTypeUpdateAction.addPlainEnumValueBuilder().attributeName(attributeKey).value(attributePlainEnumValue).build();
//                    productTypeUpdateActions2.add(productTypeUpdateAction);
//                });
//
//
//                ProductType updatedProductType2 = getEnvironment(environment)
//                        .productTypes()
//                        .withKey(productType.getKey())
//                        .post(ProductTypeUpdateBuilder.of().actions(productTypeUpdateActions2).version(productType.getVersion()).build())
//                        .executeBlocking()
//                        .getBody();

            } catch (Exception e) {
                System.out.println("Exception in Adding Plain Enum");
                e.printStackTrace();
            }
        }

    public void updateMultiText(ProductUpdateRequest request){
        request.getStoreIdUpdatesList().forEach(sid -> {
            try {
                Product product = projectRootConfiguration.getEnvironment(request.getEnvironment()).products().withKey(sid.getOfferId()).get().executeBlocking().getBody();

                List<String> productReferences = (List<String>) product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals(sid.getAttributeName())).findFirst().get().getValue();

                if (CollectionUtils.isNotEmpty(productReferences)) {
                    List<String> objects = new ArrayList<>();

                    objects.addAll(sid.getStoreIds());
                    objects.addAll(productReferences);

                    //remove duplicate from object
                    objects = objects.stream().distinct().collect(Collectors.toList());


                    List<ProductUpdateAction> updateActions = new ArrayList<>();
                    updateActions.add(ProductSetAttributeActionBuilder.of().variantId(Long.valueOf(1)).name(sid.getAttributeName()).value(objects).build());
                    if (sid.isPublish()) {
                        updateActions.add(ProductPublishActionBuilder.of().build());
                    }

                    Product updatedProduct = projectRootConfiguration.getEnvironment(request.getEnvironment())
                            .products()
                            .withKey(product.getKey())
                            .post(ProductUpdateBuilder.of().actions(updateActions).version(product.getVersion()).build())
                            .executeBlocking()
                            .getBody();
                    System.out.println("Product updated " + updatedProduct.getKey());
                }
            } catch (Exception e) {
                System.out.println("Error in product " + sid.getOfferId());
            }
        });
    }

    public void empty(ProductUpdateRequest request){
        request.getOfferCodes().forEach(sid -> {
            try {
                Product product = projectRootConfiguration.getEnvironment(request.getEnvironment()).products().withKey(sid).get().executeBlocking().getBody();

                //List<String> productReferences = (List<String>) product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals("contractIndicator")).findFirst().get().getValue();


                    List<ProductUpdateAction> updateActions = new ArrayList<>();
                    updateActions.add(ProductSetAttributeActionBuilder.of().variantId(Long.valueOf(1)).name("contractIndicator").build());
                    if (product.getMasterData().getPublished()) {
                        updateActions.add(ProductPublishActionBuilder.of().build());
                    }

                    Product updatedProduct = projectRootConfiguration.getEnvironment(request.getEnvironment())
                            .products()
                            .withKey(product.getKey())
                            .post(ProductUpdateBuilder.of().actions(updateActions).version(product.getVersion()).build())
                            .executeBlocking()
                            .getBody();
                    System.out.println("Product updated " + updatedProduct.getKey());

            } catch (Exception e) {
                //System.out.println("Error in product " + sid.getOfferId());
            }
        });
    }

    public void dealerCodesUpdate(ProductUpdateRequest request) {
        request.getDealerCodeUpdateList().forEach(sid -> {
            try {
                Product product = projectRootConfiguration.getEnvironment(request.getEnvironment()).products().withKey(sid.getOfferId()).get().executeBlocking().getBody();

                List<Attribute> attributes = product.getMasterData().getCurrent().getMasterVariant().getAttributes();

                Attribute attribute = attributes.stream().filter(attr -> attr.getName().equals(sid.getAttributeName())).findFirst().orElse(null);
                List<String> plainTextValues = new ArrayList<>();
                if (attribute != null && attribute.getValue() != null) {
                    List<String> plainTextValuesExisisting = (List<String>) attribute.getValue();
                    plainTextValues.addAll(plainTextValuesExisisting);
                }
                List<ProductUpdateAction> updateActions = new ArrayList<>();

                if (CollectionUtils.isNotEmpty(sid.getRemoveDealerCodes())) {
                    sid.getRemoveDealerCodes().forEach(removeId -> {
                        plainTextValues.removeIf(attr -> attr.equals(removeId));
                    });
                }
                if (CollectionUtils.isNotEmpty(sid.getAddDealerCodes())) {
                    sid.getAddDealerCodes().forEach(dealerId -> {
                        plainTextValues.add(String.valueOf(dealerId));
                    });
                }
                List<String> finalPlanTestValues=new ArrayList<>();
                // Remove duplicates from plainTextValues
                finalPlanTestValues = plainTextValues.stream().distinct().collect(Collectors.toList());

                updateActions.add(ProductSetAttributeActionBuilder.of().variantId(Long.valueOf(1)).name(sid.getAttributeName()).value(finalPlanTestValues).build());
                updateActions.add(ProductPublishActionBuilder.of().build());

                Product updatedProduct = projectRootConfiguration.getEnvironment(request.getEnvironment())
                        .products()
                        .withKey(product.getKey())
                        .post(ProductUpdateBuilder.of().actions(updateActions).version(product.getVersion()).build())
                        .executeBlocking()
                        .getBody();

                System.out.println("Update done for " + updatedProduct.getKey());
            } catch (Exception e) {
                System.out.println("Exception in StoreIDUpdate");
                e.printStackTrace();
            }
        });

    }

}
