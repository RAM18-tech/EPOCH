package com.dtv.ct.commercetool.service;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.models.product.Product;
import com.commercetools.api.models.product.ProductReferenceImpl;
import com.commercetools.api.models.product_type.ProductType;
import com.commercetools.api.models.state.StateResourceIdentifier;
import com.commercetools.api.models.state.StateResourceIdentifierImpl;
import com.dtv.ct.commercetool.config.ProjectRootConfiguration;
import com.dtv.ct.commercetool.model.ProductUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PublishAndUnPublishProcessor {

    @Autowired
    ProjectRootConfiguration projectRootConfiguration;

    public void unPublishAndSetEndDate(ProductUpdateRequest request) {

        StateResourceIdentifier stateResourceIdentifier = new StateResourceIdentifierImpl();
        stateResourceIdentifier.setKey("live");

        if (request.isPublish()) {
            request.getOfferCodes().forEach(a -> {
                Product product = null;
                try {
                     product = projectRootConfiguration.getEnvironment(request.getEnvironment()).products().withKey(a).get().executeBlocking().getBody();
                }catch (Exception e){
                    System.out.println("Product not found for offer code: "+a);
                }
                if (product != null) {
                    Product updatedProduct = projectRootConfiguration.getEnvironment(request.getEnvironment())
                            .products()
                            .update(product)
                            .with(builder -> builder.plus(actionBuilder -> actionBuilder.setAttributeBuilder()
                                            .variantId(Long.valueOf(1)).name("endDate").value("2099-06-20T18:30:00.000Z"))
                                    .plus(actionBuilder -> actionBuilder.transitionStateBuilder().force(true).state(stateResourceIdentifier))
                                    .plus(actionBuilder -> actionBuilder.publishBuilder()))
                            .executeBlocking()
                            .getBody();
                }
            });
        } else {

            request.getOfferCodes().forEach(a -> {
                Product product = null;
                try {
                    product = projectRootConfiguration.getEnvironment(request.getEnvironment()).products().withKey(a).get().executeBlocking().getBody();
                }catch (Exception e){
                    System.out.println("Product not found for offer code: "+a);
                }
                if (product != null) {
                    Product updatedProduct = projectRootConfiguration.getEnvironment(request.getEnvironment())
                            .products()
                            .update(product)
                            .with(builder -> builder
                                    .plus(actionBuilder -> actionBuilder.setAttributeBuilder()
                                            .variantId(Long.valueOf(1)).name("endDate").value("2024-04-20T18:30:00.000Z"))
                                    .plus(actionBuilder -> actionBuilder.transitionStateBuilder().force(true).state(stateResourceIdentifier))
                                    .plus(actionBuilder -> actionBuilder.unpublishBuilder()))
                            .executeBlocking()
                            .getBody();
                }
            });
        }

      /*  //update the productTransitionStateAction to live for product
        ProductTransitionStateAction productTransitionStateAction = ProductTransitionStateAction.builder()
                .state(new StateResourceIdentifierImpl())
                .buildUnchecked();

        //update the productTransitionStateAction to live for product
        ProductTransitionStateAction productTransitionStateAction1 = ProductTransitionStateAction.builder()
                .force(true)
                .buildUnchecked();

*/
    }


    public Map<String, String> checkIfqualifyingProductEmptyInOffer(ProductUpdateRequest request) {
        Map<String, String> emptyQyalifyingProd = new HashMap<>();

        try {

            ProductType productType = projectRootConfiguration.getEnvironment(request.getEnvironment()).productTypes().withKey("offer").get().executeBlocking().getBody();
            String typeId = productType.getId();

            List<Product> productsStag2 =  projectRootConfiguration.getEnvironment(request.getEnvironment()).products().get().withOffset(0)
                    //withWhere(typeIdWhere + otherWhere).
                    .withWhere("productType(id=\"" + typeId + "\")"+"and masterData(current(masterVariant(attributes(name=\"offerProductFamily\" and value(key in (\"OTT\")))))) and masterData(current(masterVariant(attributes(name=\"contractIndicator\" and value(key in (\"contract\")))))) and masterData(current(masterVariant(attributes(name=\"offerProductType\" and value(key in (\"video-addon\")))))) and masterData(current(masterVariant(attributes(name=\"eligibilitySalesChannels\" and value(key in (\"opus\"))))))").
                    withLimit(500).executeBlocking().getBody().getResults();
            productsStag2.forEach(product -> {
                try {

                    List<String> prodList = Arrays.asList("qualifyingProductIds", "bundleProductIds", "includedProductIds", "qualifierIncompatibleProductIds", "reconnectEligibleOffer", "conflictingOffers");
                    Product finalProduct = product;
                    prodList.forEach(p -> {
                        List<ProductReferenceImpl> bundleProductIds = null;
                        List<ProductReferenceImpl> newBundleProductIds = new ArrayList<>();
                        if (finalProduct.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals(p)).findAny().isPresent()) {
                            bundleProductIds = (List<ProductReferenceImpl>) finalProduct.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().filter(attr -> attr.getName().equals(p)).findFirst().get().getValue();
                            bundleProductIds.forEach(bprd -> {

                                try {
                                Product product1 = projectRootConfiguration.getEnvironment(request.getEnvironment()).products().withId(bprd.getId()).get().executeBlocking().getBody();

                                } catch (Exception e) {
                                    System.out.println("Error in product inside prodList " + bprd.getId());
                                    if (!emptyQyalifyingProd.containsKey(finalProduct.getKey())) {
                                        emptyQyalifyingProd.put(finalProduct.getKey(), p);
                                    }else {
                                        String existingValue = emptyQyalifyingProd.get(finalProduct.getKey());
                                        if(!existingValue.contains(p)) {
                                            emptyQyalifyingProd.put(finalProduct.getKey(), existingValue + "," + p);
                                        }
                                    }
                                }
                            });

                        }
                    });
                } catch (Exception e) {
                    System.out.println("Error in product " + product.getKey());
                }
            });


            System.out.println(emptyQyalifyingProd);
        } catch (Exception e) {
            System.out.println("Error in product ");

        }
        return emptyQyalifyingProd;
    }

}
