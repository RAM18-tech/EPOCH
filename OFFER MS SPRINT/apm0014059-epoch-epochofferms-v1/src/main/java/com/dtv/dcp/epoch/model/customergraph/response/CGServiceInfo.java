package com.dtv.dcp.epoch.model.customergraph.response;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CGServiceInfo implements Serializable {
    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    private String typeOfPlan;
    private List<String> supportedLanguages;
    private String serviceName;
    private String serviceID;
    private String startDate;
    private String retailPrice;
    private String description;
    private String status;
    private boolean basicService;
    private boolean cancellable;
    private boolean hasAction;
    private String ordProdId;
    private String rateType;
    private String contractPrice;
    private String quantity;
    private String allowedRefundDate;
    private List<CGPromotion> promotion;

    /**
	 * @return the promotion
	 */
	public List<CGPromotion> getPromotion() {
		return promotion;
	}

	/**
	 * @param promotion the promotion to set
	 */
	public void setPromotion(List<CGPromotion> promotion) {
		this.promotion = promotion;
	}

	public String getTypeOfPlan() {
        return typeOfPlan;
    }

    public void setTypeOfPlan(String typeOfPlan) {
        this.typeOfPlan = typeOfPlan;
    }

    public List<String> getSupportedLanguages() {
        return supportedLanguages;
    }

    public void setSupportedLanguages(List<String> supportedLanguages) {
        this.supportedLanguages = supportedLanguages;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceID() {
        return serviceID;
    }

    public void setServiceID(String serviceID) {
        this.serviceID = serviceID;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getRetailPrice() {
        return retailPrice;
    }

    public void setRetailPrice(String retailPrice) {
        this.retailPrice = retailPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isBasicService() {
        return basicService;
    }

    public void setBasicService(boolean basicService) {
        this.basicService = basicService;
    }

    public boolean isCancellable() {
        return cancellable;
    }

    public void setCancellable(boolean cancellable) {
        this.cancellable = cancellable;
    }

    public boolean isHasAction() {
        return hasAction;
    }

    public void setHasAction(boolean hasAction) {
        this.hasAction = hasAction;
    }

    public String getOrdProdId() {
        return ordProdId;
    }

    public void setOrdProdId(String ordProdId) {
        this.ordProdId = ordProdId;
    }

    public String getRateType() {
        return rateType;
    }

    public void setRateType(String rateType) {
        this.rateType = rateType;
    }

    public String getContractPrice() {
        return contractPrice;
    }

    public void setContractPrice(String contractPrice) {
        this.contractPrice = contractPrice;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getAllowedRefundDate() {
        return allowedRefundDate;
    }

    public void setAllowedRefundDate(String allowedRefundDate) {
        this.allowedRefundDate = allowedRefundDate;
    }
}
