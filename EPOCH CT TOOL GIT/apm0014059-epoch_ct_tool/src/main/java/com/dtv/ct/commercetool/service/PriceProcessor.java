package com.dtv.ct.commercetool.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.commercetools.api.models.channel.Channel;
import com.commercetools.api.models.channel.ChannelResourceIdentifier;
import com.commercetools.api.models.common.CentPrecisionMoney;
import com.commercetools.api.models.common.Price;
import com.commercetools.api.models.common.PriceDraft;
import com.commercetools.api.models.common.PriceDraftBuilder;
import com.commercetools.api.models.customer_group.CustomerGroup;
import com.commercetools.api.models.customer_group.CustomerGroupResourceIdentifier;
import com.commercetools.api.models.product.Product;
import com.commercetools.api.models.product.ProductAddPriceAction;
import com.commercetools.api.models.product.ProductChangePriceAction;
import com.commercetools.api.models.product.ProductPublishAction;
import com.commercetools.api.models.product.ProductUpdateAction;
import com.commercetools.api.models.product.ProductUpdateActionBuilder;
import com.commercetools.api.models.product.ProductUpdateBuilder;
import com.commercetools.api.models.type.CustomFieldsDraft;
import com.commercetools.api.models.type.FieldContainer;
import com.commercetools.api.models.type.Type;
import com.dtv.ct.commercetool.config.ProjectRootConfiguration;
import com.dtv.ct.commercetool.model.PriceRequest;
import com.dtv.ct.commercetool.model.ProductUpdateRequest;

@Component
public class PriceProcessor {

    @Autowired
    ProjectRootConfiguration projectRootConfiguration;


    public List<String> productUpdatePriceDates(ProductUpdateRequest productUpdateRequest) {
        List<String> exceptions = new ArrayList<>();
        List<Channel> channels = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment()).channels().get().withLimit(500).executeBlocking().getBody().getResults();
        List<CustomerGroup> customerGroups = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment()).customerGroups().get().withLimit(500).executeBlocking().getBody().getResults();
        List<Type> types = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment()).types().get().executeBlocking().getBody().getResults();

        productUpdateRequest.getPrices().forEach(price -> {
            try {
                Product product = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment()).products().withKey(price.getProductCode()).get().executeBlocking().getBody();
                //get the price from the product
                Channel channel = channels.stream().filter(c -> c.getKey().equalsIgnoreCase(price.getChannel())).findAny().orElse(null);

                List<Price> prices = product.getMasterData().getCurrent().getMasterVariant().getPrices().stream().filter(p -> Objects.nonNull(p.getChannel())).filter(p -> p.getChannel().getId().equalsIgnoreCase(channel.getId())).collect(Collectors.toList());

                if (StringUtils.isNotEmpty(price.getCustomerGroup())) {
                    CustomerGroup customerGroup = customerGroups.stream().filter(c -> StringUtils.isNotEmpty(c.getKey())).filter(c -> c.getKey().equalsIgnoreCase(price.getCustomerGroup())).findAny().orElse(null);
                    prices = prices.stream().filter(p -> Objects.nonNull(p.getCustomerGroup()))
                            .filter(p -> StringUtils.isNotEmpty(p.getCustomerGroup().getId()))
                            .filter(p -> p.getCustomerGroup().getId().equalsIgnoreCase(customerGroup.getId()))
                            .collect(Collectors.toList());
                } else {
                    prices = prices.stream().filter(p -> Objects.isNull(p.getCustomerGroup())).collect(Collectors.toList());
                }
                if (!CollectionUtils.isEmpty(prices)) {

                    prices.forEach(p -> {
                        CustomerGroup customerGroup = null;
                        if (Objects.nonNull(p.getCustomerGroup()) && StringUtils.isNotEmpty(price.getCustomerGroup())) {
                            customerGroup = customerGroups.stream().filter(c -> StringUtils.isNotEmpty(c.getKey())).filter(c -> c.getKey().equalsIgnoreCase(price.getCustomerGroup())).findAny().orElse(null);
                        }
                        Long centAm = p.getValue().getCentAmount();
                        if (centAm.longValue() == price.getPrice() && (p.getChannel().getId().equalsIgnoreCase(channel.getId())
                                && price.getChannel().equalsIgnoreCase(channel.getKey()))) {

                            ChannelResourceIdentifier channelResourceIdentifier = null;
                            CustomerGroupResourceIdentifier customerGroupResourceIdentifier = null;

                            if (channel != null) {
                                channelResourceIdentifier = ChannelResourceIdentifier.builder().id(channel.getId()).build();
                            }
                            if (customerGroup != null) {
                                customerGroupResourceIdentifier = CustomerGroupResourceIdentifier.builder().id(customerGroup.getId()).build();
                            }


                            Type typeObj = types.stream().filter(t -> t.getKey().equalsIgnoreCase("custom-price")).findFirst().orElse(null);
                            CustomFieldsDraft customFieldsDraft = null;
                            if (price.getFieldContainer() == null) {
                                customFieldsDraft = CustomFieldsDraft.builder()
                                        .type(typeResourceIdentifierBuilder -> typeResourceIdentifierBuilder.id(typeObj.getId()))
                                        .fields(p.getCustom().getFields()).build();
                            } else {
                                //FieldContainer fieldContainer = p.getCustom().getFields();
                                customFieldsDraft = CustomFieldsDraft.builder().type(typeResourceIdentifierBuilder -> typeResourceIdentifierBuilder.id(typeObj.getId()))
                                        .fields(fieldContainerBuilder -> fieldContainerBuilder.values(fieldContainerBuilder(price, p.getCustom().getFields()))).build();
                            }

                            ZonedDateTime validFrom = AddHoursToZonedDateTime(ZonedDateTime.parse(price.getValidFrom()));
                            ZonedDateTime validTo = AddHoursToZonedDateTime(ZonedDateTime.parse(price.getValidUntil()));

                            PriceDraft priceDraft = PriceDraftBuilder.of().custom(CustomFieldsDraft.of(p.getCustom()))
                                    .channel(channelResourceIdentifier)
                                    .customerGroup(customerGroupResourceIdentifier)
                                    .custom(customFieldsDraft)
                                    .value(p.getValue())
                                    .validFrom(validFrom)
                                    .validUntil(validTo)
                                    .build();


                            ProductChangePriceAction productChangePriceAction = ProductChangePriceAction.of();
                            productChangePriceAction.setPrice(priceDraft);
                            productChangePriceAction.setPriceId(p.getId());

                            ProductPublishAction productPublishAction = ProductUpdateActionBuilder.of().publishBuilder().build();

                            Product updatedProduct = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment())
                                    .products()
                                    .withKey(price.getProductCode())
                                    .post(ProductUpdateBuilder.of().actions(productChangePriceAction,productPublishAction).version(product.getVersion()).build())
                                    .executeBlocking()
                                    .getBody();
                            System.out.println("Update Success for : " + updatedProduct.getKey() + " For Price " + price.getPrice() + "and Channel" + price.getChannel());
                            exceptions.add("Update Success for : " + updatedProduct.getKey() + " For Price " + price.getPrice() + " and Channel " + price.getChannel());
                        }else if (price.getOldPrice() != null && centAm.longValue() == price.getOldPrice() && (p.getChannel().getId().equalsIgnoreCase(channel.getId())
                                && price.getChannel().equalsIgnoreCase(channel.getKey())))
                        {

                            ChannelResourceIdentifier channelResourceIdentifier = null;
                            CustomerGroupResourceIdentifier customerGroupResourceIdentifier = null;

                            if (channel != null) {
                                channelResourceIdentifier = ChannelResourceIdentifier.builder().id(channel.getId()).build();
                            }
                            if (customerGroup != null) {
                                customerGroupResourceIdentifier = CustomerGroupResourceIdentifier.builder().id(customerGroup.getId()).build();
                            }

                            CentPrecisionMoney centPrecisionMoney = CentPrecisionMoney.builder().centAmount(Long.valueOf(price.getPrice())).currencyCode("USD").fractionDigits(2).build();


                            Type typeObj = types.stream().filter(t -> t.getKey().equalsIgnoreCase("custom-price")).findFirst().orElse(null);
                            CustomFieldsDraft customFieldsDraft = null;
                            if (price.getFieldContainer() == null) {
                                customFieldsDraft = CustomFieldsDraft.builder()
                                        .type(typeResourceIdentifierBuilder -> typeResourceIdentifierBuilder.id(typeObj.getId()))
                                        .fields(p.getCustom().getFields()).build();
                            } else {
                                customFieldsDraft = CustomFieldsDraft.builder().type(typeResourceIdentifierBuilder -> typeResourceIdentifierBuilder.id(typeObj.getId()))
                                        .fields(fieldContainerBuilder -> fieldContainerBuilder.values(fieldContainerBuilder(price,p.getCustom().getFields()))).build();
                            }

                            ZonedDateTime validFrom = AddHoursToZonedDateTime(ZonedDateTime.parse(price.getValidFrom()));
                            ZonedDateTime validTo = AddHoursToZonedDateTime(ZonedDateTime.parse(price.getValidUntil()));

                            PriceDraft priceDraft = PriceDraftBuilder.of().custom(CustomFieldsDraft.of(p.getCustom()))
                                    .channel(channelResourceIdentifier)
                                    .customerGroup(customerGroupResourceIdentifier)
                                    .custom(customFieldsDraft)
                                    .value(centPrecisionMoney)
                                    .validFrom(validFrom)
                                    .validUntil(validTo)
                                    .build();


                            ProductChangePriceAction productChangePriceAction = ProductChangePriceAction.of();
                            productChangePriceAction.setPrice(priceDraft);
                            productChangePriceAction.setPriceId(p.getId());

                            ProductPublishAction productPublishAction = ProductUpdateActionBuilder.of().publishBuilder().build();

                            Product updatedProduct = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment())
                                    .products()
                                    .withKey(price.getProductCode())
                                    .post(ProductUpdateBuilder.of().actions(productChangePriceAction,productPublishAction).version(product.getVersion()).build())
                                    .executeBlocking()
                                    .getBody();
                            System.out.println("Update Success for : " + updatedProduct.getKey() +"For Old Price" + price.getOldPrice() +" And For New Price " + price.getPrice());
                            exceptions.add("Update Success for : " + updatedProduct.getKey() +"For Old Price" + price.getOldPrice() +" And For New Price " + price.getPrice());
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                exceptions.add("Update Failed for Product: " + price.getProductCode() + " For Price " + price.getPrice() + " and Channel " + price.getChannel());
                System.out.println("Update Failed for Product: " + price.getProductCode() + " For Price " + price.getPrice() + " and Channel " + price.getChannel());
                //throw e;
            }
        });
        return exceptions;
    }

    public List<String> priceTierUpdate(ProductUpdateRequest productUpdateRequest) {
        List<String> response = new ArrayList<>();

        List<Channel> channels = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment())
                .channels().get().withLimit(500).executeBlocking().getBody().getResults();
        List<CustomerGroup> customerGroups = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment())
                .customerGroups().get().withLimit(500).executeBlocking().getBody().getResults();

        productUpdateRequest.getPrices().forEach(priceReq -> {
            try {
                if (StringUtils.isBlank(priceReq.getProductCode()) || StringUtils.isBlank(priceReq.getChannel()) || priceReq.getPrice() == null) {
                    response.add("productCode, channel and price are mandatory for each price entry");
                    return;
                }
                if (priceReq.getFieldContainer() == null || CollectionUtils.isEmpty(priceReq.getFieldContainer().getPriceTier())) {
                    response.add("fieldContainer.priceTier is mandatory for: " + priceReq.getProductCode());
                    return;
                }

                String contractIndicator = priceReq.getFieldContainer().getContractIndicator();

                Product product = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment())
                        .products().withKey(priceReq.getProductCode()).get().executeBlocking().getBody();

                Channel channel = channels.stream()
                        .filter(c -> StringUtils.isNotBlank(c.getKey()))
                        .filter(c -> c.getKey().equalsIgnoreCase(priceReq.getChannel()))
                        .findFirst().orElse(null);

                if (channel == null) {
                    response.add("No channel found for key: " + priceReq.getChannel());
                    return;
                }

                CustomerGroup customerGroup = null;
                if (StringUtils.isNotBlank(priceReq.getCustomerGroup())) {
                    customerGroup = customerGroups.stream()
                            .filter(cg -> StringUtils.isNotBlank(cg.getKey()))
                            .filter(cg -> cg.getKey().equalsIgnoreCase(priceReq.getCustomerGroup()))
                            .findFirst().orElse(null);
                    if (customerGroup == null) {
                        response.add("No customerGroup found for key: " + priceReq.getCustomerGroup());
                        return;
                    }
                }

                List<Price> allPrices = product.getMasterData().getCurrent().getMasterVariant().getPrices();
                final Channel finalChannel = channel;
                final CustomerGroup finalCustomerGroup = customerGroup;

                List<Price> matchedPrices = allPrices.stream()
                        .filter(p -> Objects.nonNull(p.getChannel()) && StringUtils.equalsIgnoreCase(p.getChannel().getId(), finalChannel.getId()))
                        .filter(p -> Objects.nonNull(p.getValue()) && Objects.equals(p.getValue().getCentAmount(), priceReq.getPrice()))
                        .filter(p -> {
                            if (finalCustomerGroup == null) return true;
                            return p.getCustomerGroup() != null
                                    && StringUtils.isNotBlank(p.getCustomerGroup().getId())
                                    && StringUtils.equalsIgnoreCase(p.getCustomerGroup().getId(), finalCustomerGroup.getId());
                        })
                        .filter(p -> {
                            if (StringUtils.isBlank(contractIndicator)) return true;
                            if (p.getCustom() == null || p.getCustom().getFields() == null) return false;
                            Object ci = p.getCustom().getFields().values().get("contractIndicator");
                            return Objects.nonNull(ci) && StringUtils.equalsIgnoreCase(String.valueOf(ci), contractIndicator);
                        })
                        .filter(this::isPriceActiveNow)
                        .collect(Collectors.toList());

                if (CollectionUtils.isEmpty(matchedPrices)) {
                    response.add("No matching prices found for: " + priceReq.getProductCode() + " channel=" + priceReq.getChannel() + " price=" + priceReq.getPrice());
                    return;
                }

                List<ProductUpdateAction> actions = new ArrayList<>();

                for (Price matchedPrice : matchedPrices) {
                    if (matchedPrice.getCustom() == null || matchedPrice.getCustom().getType() == null) continue;

                    Map<String, Object> updatedFields = new HashMap<>(matchedPrice.getCustom().getFields().values());
                    updatedFields.put("priceTier", priceReq.getFieldContainer().getPriceTier());

                    CustomFieldsDraft customFieldsDraft = CustomFieldsDraft.builder()
                            .type(t -> t.id(matchedPrice.getCustom().getType().getId()))
                            .fields(f -> f.values(updatedFields))
                            .build();

                    ChannelResourceIdentifier channelResourceIdentifier = ChannelResourceIdentifier.builder()
                            .id(matchedPrice.getChannel().getId()).build();

                    CustomerGroupResourceIdentifier customerGroupResourceIdentifier = null;
                    if (matchedPrice.getCustomerGroup() != null && StringUtils.isNotBlank(matchedPrice.getCustomerGroup().getId())) {
                        customerGroupResourceIdentifier = CustomerGroupResourceIdentifier.builder()
                                .id(matchedPrice.getCustomerGroup().getId()).build();
                    }

                    PriceDraft priceDraft = PriceDraftBuilder.of()
                            .channel(channelResourceIdentifier)
                            .customerGroup(customerGroupResourceIdentifier)
                            .custom(customFieldsDraft)
                            .value(matchedPrice.getValue())
                            .validFrom(matchedPrice.getValidFrom())
                            .validUntil(matchedPrice.getValidUntil())
                            .build();

                    ProductChangePriceAction changePriceAction = ProductChangePriceAction.of();
                    changePriceAction.setPriceId(matchedPrice.getId());
                    changePriceAction.setPrice(priceDraft);
                    actions.add(changePriceAction);
                }

                if (CollectionUtils.isEmpty(actions)) {
                    response.add("No eligible prices to update for: " + priceReq.getProductCode());
                    return;
                }

                if (productUpdateRequest.isPublish() || "true".equalsIgnoreCase(priceReq.getPublish())) {
                    actions.add(ProductUpdateActionBuilder.of().publishBuilder().build());
                }

                Product updatedProduct = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment())
                        .products().withKey(priceReq.getProductCode())
                        .post(ProductUpdateBuilder.of().version(product.getVersion()).actions(actions).build())
                        .executeBlocking().getBody();

                response.add("priceTier update success for: " + updatedProduct.getKey() + " matched=" + matchedPrices.size());
            } catch (Exception e) {
                e.printStackTrace();
                response.add("priceTier update failed for: " + priceReq.getProductCode() + " reason: " + e.getMessage());
            }
        });
        return response;
    }


    public List<String> pricesAddNewPrices(ProductUpdateRequest productUpdateRequest) {
        List<String> exceptions = new ArrayList<>();
        //get all channel list
        List<Channel> channels = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment()).channels().get().withLimit(500).executeBlocking().getBody().getResults();
        //get all customer group
        List<CustomerGroup> customerGroups = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment()).customerGroups().get().withLimit(500).executeBlocking().getBody().getResults();
        //get all TypeResoucres
        List<Type> types = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment()).types().get().executeBlocking().getBody().getResults();

        productUpdateRequest.getPrices().forEach(price -> {
            try {
                CustomerGroup customerGroup = null;
                if (Objects.nonNull(price.getCustomerGroup()) && StringUtils.isNotEmpty(price.getCustomerGroup())) {
                    customerGroup = customerGroups.stream().filter(c -> StringUtils.isNotEmpty(c.getKey())).filter(c -> c.getKey().equalsIgnoreCase(price.getCustomerGroup())).findAny().orElse(null);
                }
                Product product = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment()).products().withKey(price.getProductCode()).get().executeBlocking().getBody();

                ChannelResourceIdentifier channelResourceIdentifier = null;
                CustomerGroupResourceIdentifier customerGroupResourceIdentifier = null;
                CentPrecisionMoney centPrecisionMoney = CentPrecisionMoney.builder().centAmount(Long.valueOf(price.getPrice())).currencyCode("USD").fractionDigits(2).build();

                Channel channel = channels.stream().filter(c -> StringUtils.isNotEmpty(c.getKey())).filter(c -> c.getKey().equalsIgnoreCase(price.getChannel())).findAny().orElse(null);
                if (channel != null) {
                    channelResourceIdentifier = ChannelResourceIdentifier.builder().id(channel.getId()).build();
                }
              //  CustomerGroup customerGroup = customerGroups.stream().filter(c -> c.getKey().equalsIgnoreCase(price.getCustomerGroup())).findFirst().orElse(null);
                if (customerGroup != null) {
                    customerGroupResourceIdentifier = CustomerGroupResourceIdentifier.builder().id(customerGroup.getId()).build();
                }

                Type typeObj = types.stream().filter(t -> t.getKey().equalsIgnoreCase("custom-price")).findFirst().orElse(null);

                CustomFieldsDraft customFieldsDraft = CustomFieldsDraft.builder().type(typeResourceIdentifierBuilder -> typeResourceIdentifierBuilder.id(typeObj.getId())).fields(fieldContainerBuilder -> fieldContainerBuilder.values(fieldContainerBuilder(price))).build();

                ZonedDateTime validFrom = AddHoursToZonedDateTime(ZonedDateTime.parse(price.getValidFrom()));
                ZonedDateTime validTo = AddHoursToZonedDateTime(ZonedDateTime.parse(price.getValidUntil()));

                PriceDraft priceDraft = PriceDraftBuilder.of().validFrom(validFrom).validUntil(validTo).channel(channelResourceIdentifier).customerGroup(customerGroupResourceIdentifier).value(centPrecisionMoney).custom(customFieldsDraft).build();

                ProductAddPriceAction productAddPriceAction = ProductUpdateActionBuilder.of().addPriceBuilder().price(priceDraft).variantId(1L).build();
                ProductPublishAction productPublishAction = ProductUpdateActionBuilder.of().publishBuilder().build();

                Product priceAdding = projectRootConfiguration.getEnvironment(productUpdateRequest.getEnvironment()).products().withKey(price.getProductCode()).post(ProductUpdateBuilder.of().actions(productAddPriceAction, productPublishAction).version(product.getVersion()).build()).executeBlocking().getBody();
                System.out.println("Price added for " + priceAdding.getKey() + " For Price " + price.getPrice() + " and Channel" + price.getChannel());

            } catch (Exception e) {
                e.printStackTrace();
                exceptions.add("Price adding Failed for Product: " + price.getProductCode() + " For Price " + price.getPrice() + " and Channel " + price.getChannel());
                System.out.println("Price adding Failed for Product: " + price.getProductCode() + " For Price " + price.getPrice() + " and Channel " + price.getChannel());
            }
        });
        return exceptions;
    }

    private ZonedDateTime AddHoursToZonedDateTime(ZonedDateTime zonedDateTime) {
        // Check if validFrom is between March 3 and November 3
        if ((zonedDateTime.getMonthValue() > 3 || (zonedDateTime.getMonthValue() == 3 && zonedDateTime.getDayOfMonth() >= 3)) &&
                (zonedDateTime.getMonthValue() < 11 || (zonedDateTime.getMonthValue() == 11 && zonedDateTime.getDayOfMonth() <= 3))) {
            zonedDateTime = zonedDateTime.plusHours(7);
        } else {
            zonedDateTime = zonedDateTime.plusHours(8);
        }
        return zonedDateTime;
    }

    private boolean isPriceActiveNow(Price price) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime validFrom = price.getValidFrom();
        ZonedDateTime validUntil = price.getValidUntil();

        if (validFrom != null && validUntil != null && validUntil.isBefore(validFrom)) {
            return false;
        }

        boolean startsBeforeOrNow = validFrom == null || !now.isBefore(validFrom);
        boolean endsAfterOrNow = validUntil == null || !now.isAfter(validUntil);

        return startsBeforeOrNow && endsAfterOrNow;
    }

    private Map<String, Object>  fieldContainerBuilder (PriceRequest priceRequest) {

        Map<String, Object> fieldMap = new HashMap<>();
        if (StringUtils.isNotEmpty(priceRequest.getFieldContainer().getContractIndicator()))
            fieldMap.put("contractIndicator", priceRequest.getFieldContainer().getContractIndicator());
        if (StringUtils.isNotEmpty(priceRequest.getFieldContainer().getBillingReferenceId()))
            fieldMap.put("billingReferenceId", priceRequest.getFieldContainer().getBillingReferenceId());
        if (StringUtils.isNotEmpty(priceRequest.getFieldContainer().getPeriod()))
            fieldMap.put("period", priceRequest.getFieldContainer().getPeriod());
        if (StringUtils.isNotEmpty(priceRequest.getFieldContainer().getProRateFrequency()))
            fieldMap.put("proRateFrequency", priceRequest.getFieldContainer().getProRateFrequency());
        if (priceRequest.getFieldContainer().getRecurrenceIndicator() != null)
            fieldMap.put("recurrenceIndicator", priceRequest.getFieldContainer().getRecurrenceIndicator());
        if (priceRequest.getFieldContainer().getGrandfathered() != null)
            fieldMap.put("isGrandfathered", priceRequest.getFieldContainer().getGrandfathered());
        if (!CollectionUtils.isEmpty(priceRequest.getFieldContainer().getCreditRisk()))
            fieldMap.put("creditRisk", priceRequest.getFieldContainer().getCreditRisk());
        if (!CollectionUtils.isEmpty(priceRequest.getFieldContainer().getTreatmentCode()))
            fieldMap.put("treatmentCode", priceRequest.getFieldContainer().getTreatmentCode());
        if (!CollectionUtils.isEmpty(priceRequest.getFieldContainer().getPriceTier()))
            fieldMap.put("priceTier", priceRequest.getFieldContainer().getPriceTier());
        if (!CollectionUtils.isEmpty(priceRequest.getFieldContainer().getServiceSubscriptionType()))
            fieldMap.put("serviceSubscriptionType", priceRequest.getFieldContainer().getServiceSubscriptionType());
        if (!CollectionUtils.isEmpty(priceRequest.getFieldContainer().getConflictingPriceTier()))
            fieldMap.put("conflictingPriceTiers", priceRequest.getFieldContainer().getConflictingPriceTier());
        if (!CollectionUtils.isEmpty(priceRequest.getFieldContainer().getAttributePricingCriteria()))
            fieldMap.put("attributePricingCriteria", priceRequest.getFieldContainer().getAttributePricingCriteria());
        return fieldMap;

    }


    private Map<String, Object>  fieldContainerBuilder (PriceRequest priceRequest, FieldContainer fieldContainer) {

        com.dtv.ct.commercetool.model.FieldContainer requestFieldContainer = priceRequest.getFieldContainer();
        Map<String, Object> fieldContainerMap = fieldContainer.values();
        if (!fieldContainerMap.isEmpty()) {
            fieldContainerMap.entrySet().forEach(field -> {
                if (StringUtils.isEmpty(requestFieldContainer.getContractIndicator()) && field.getKey().equalsIgnoreCase("contractIndicator")) {
                    requestFieldContainer.setContractIndicator((String) field.getValue());
                }
                if (StringUtils.isEmpty(requestFieldContainer.getBillingReferenceId()) && field.getKey().equalsIgnoreCase("billingReferenceId")) {
                    requestFieldContainer.setBillingReferenceId((String) field.getValue());
                }
                if (StringUtils.isEmpty(requestFieldContainer.getPeriod()) && field.getKey().equalsIgnoreCase("period")) {
                    requestFieldContainer.setPeriod((String) field.getValue());
                }
                if (StringUtils.isEmpty(requestFieldContainer.getProRateFrequency()) && field.getKey().equalsIgnoreCase("proRateFrequency")) {
                    requestFieldContainer.setProRateFrequency((String) field.getValue());
                }
                if (requestFieldContainer.getRecurrenceIndicator() == null && field.getKey().equalsIgnoreCase("recurrenceIndicator")) {
                    requestFieldContainer.setRecurrenceIndicator((Boolean) field.getValue());
                }
                if (requestFieldContainer.getGrandfathered() == null && field.getKey().equalsIgnoreCase("isGrandfathered")) {
                    requestFieldContainer.setGrandfathered((Boolean) field.getValue());
                }
                if (CollectionUtils.isEmpty(requestFieldContainer.getCreditRisk()) && field.getKey().equalsIgnoreCase("creditRisk")) {
                    requestFieldContainer.setCreditRisk((List<String>) field.getValue());
                }
                if (CollectionUtils.isEmpty(requestFieldContainer.getTreatmentCode()) && field.getKey().equalsIgnoreCase("treatmentCode")) {
                    requestFieldContainer.setTreatmentCode((List<String>) field.getValue());
                }
                if (CollectionUtils.isEmpty(requestFieldContainer.getPriceTier()) && field.getKey().equalsIgnoreCase("priceTier")) {
                    requestFieldContainer.setPriceTier((List<String>) field.getValue());
                }
                if (CollectionUtils.isEmpty(requestFieldContainer.getServiceSubscriptionType()) && field.getKey().equalsIgnoreCase("serviceSubscriptionType")) {
                    requestFieldContainer.setServiceSubscriptionType((List<String>) field.getValue());
                }
                if (CollectionUtils.isEmpty(requestFieldContainer.getConflictingPriceTier()) && field.getKey().equalsIgnoreCase("conflictingPriceTiers")) {
                    requestFieldContainer.setConflictingPriceTier((List<String>) field.getValue());
                }
                if (CollectionUtils.isEmpty(requestFieldContainer.getAttributePricingCriteria()) && field.getKey().equalsIgnoreCase("attributePricingCriteria")) {
                    requestFieldContainer.setAttributePricingCriteria((List<String>) field.getValue());
                }
            });
        }

        Map<String, Object> fieldMap = new HashMap<>();
        if (StringUtils.isNotEmpty(requestFieldContainer.getContractIndicator()))
            fieldMap.put("contractIndicator", requestFieldContainer.getContractIndicator());
        if (StringUtils.isNotEmpty(requestFieldContainer.getBillingReferenceId()))
            fieldMap.put("billingReferenceId", requestFieldContainer.getBillingReferenceId());
        if (StringUtils.isNotEmpty(requestFieldContainer.getPeriod()))
            fieldMap.put("period", requestFieldContainer.getPeriod());
        if (StringUtils.isNotEmpty(requestFieldContainer.getProRateFrequency()))
            fieldMap.put("proRateFrequency", requestFieldContainer.getProRateFrequency());
        if (requestFieldContainer.getRecurrenceIndicator() != null)
            fieldMap.put("recurrenceIndicator", requestFieldContainer.getRecurrenceIndicator());
        if (requestFieldContainer.getGrandfathered() != null)
            fieldMap.put("isGrandfathered", requestFieldContainer.getGrandfathered());
        if (!CollectionUtils.isEmpty(requestFieldContainer.getCreditRisk()))
            fieldMap.put("creditRisk", requestFieldContainer.getCreditRisk());
        if (!CollectionUtils.isEmpty(requestFieldContainer.getTreatmentCode()))
            fieldMap.put("treatmentCode", requestFieldContainer.getTreatmentCode());
        if (!CollectionUtils.isEmpty(requestFieldContainer.getPriceTier()))
            fieldMap.put("priceTier", requestFieldContainer.getPriceTier());
        if (!CollectionUtils.isEmpty(priceRequest.getFieldContainer().getServiceSubscriptionType()))
            fieldMap.put("serviceSubscriptionType", priceRequest.getFieldContainer().getServiceSubscriptionType());
        if (!CollectionUtils.isEmpty(priceRequest.getFieldContainer().getConflictingPriceTier()))
            fieldMap.put("conflictingPriceTiers", priceRequest.getFieldContainer().getConflictingPriceTier());
        if (!CollectionUtils.isEmpty(priceRequest.getFieldContainer().getAttributePricingCriteria()))
            fieldMap.put("attributePricingCriteria", priceRequest.getFieldContainer().getAttributePricingCriteria());

        if (!fieldMap.isEmpty() && !fieldContainerMap.isEmpty()) {
            fieldContainerMap.entrySet().forEach(field -> {
                if (!fieldMap.containsKey(field.getKey())) {
                    fieldMap.put(field.getKey(), field.getValue());
                }
            });
        }
        return fieldMap;

    }
}
