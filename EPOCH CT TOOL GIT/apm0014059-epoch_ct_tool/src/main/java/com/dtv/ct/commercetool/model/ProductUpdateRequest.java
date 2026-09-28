package com.dtv.ct.commercetool.model;

import java.util.List;

public class ProductUpdateRequest {

    List<PriceRequest> prices;
    List<String> offerCodes;
    List<String> benefitCodes;
    String productName;
    AddAndRemove promoId;
    List<String> reconnectEligibileOffer;
    List<String> zipCodes;
    List<String> salesChannelToAdd;
    private String userId;
    private List<AssociatedProducts> associatedProducts;

    public List<AssociatedProducts> getAssociatedProducts() {
        return associatedProducts;
    }

    public String getUserId() {
        return userId;
    }

    public List<String> getSalesChannelToAdd() {
        return salesChannelToAdd;
    }

    List<StoreIdUpdates> storeIdUpdatesList;

    public List<String> getZipCodes() {
        return zipCodes;
    }

    public void setZipCodes(List<String> zipCodes) {
        this.zipCodes = zipCodes;
    }

    boolean keepExisting;

    boolean publish;

    boolean removeOffers;

    String environment;

    List<DealerCodeUpdates> dealerCodeUpdateList;
    List<BenefitUpdateRequest> benefitsUpdateList;

    public List<BenefitUpdateRequest> getBenefitsUpdateList() {
        return benefitsUpdateList;
    }

    public void setBenefitsUpdateList(List<BenefitUpdateRequest> benefitsUpdateList) {
        this.benefitsUpdateList = benefitsUpdateList;
    }

    public List<DealerCodeUpdates> getDealerCodeUpdateList() {
        return dealerCodeUpdateList;
    }

    public void setDealerCodeUpdates(List<DealerCodeUpdates> dealerCodeUpdateList) {
        this.dealerCodeUpdateList = dealerCodeUpdateList;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setStoreIdUpdatesList(List<StoreIdUpdates> storeIdUpdatesList) {
        this.storeIdUpdatesList = storeIdUpdatesList;
    }

    public List<StoreIdUpdates> getStoreIdUpdatesList() {
        return storeIdUpdatesList;
    }

    public List<PriceRequest> getPrices() {
        return prices;
    }

    public void setOfferCodes(List<String> offerCodes) {
        this.offerCodes = offerCodes;
    }

    public List<String> getOfferCodes() {
        return offerCodes;
    }

    public boolean isPublish() {
        return publish;
    }

    public boolean isKeepExisting() {
        return keepExisting;
    }

    public boolean isRemoveOffers() {
        return removeOffers;
    }

    public void setRemoveOffers(boolean removeOffers) {
        this.removeOffers = removeOffers;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public AddAndRemove getPromoId() {
        return promoId;
    }

    public List<String> getReconnectEligibileOffer() {
        return reconnectEligibileOffer;
    }

    public void setReconnectEligibileOffer(List<String> reconnectEligibileOffer) {
        this.reconnectEligibileOffer = reconnectEligibileOffer;
    }

    private String source;
    private String target;

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public List<String> getBenefitCodes() {
        return benefitCodes;
    }

    public void setBenefitCodes(List<String> benefitCodes) {
        this.benefitCodes = benefitCodes;
    }
}
