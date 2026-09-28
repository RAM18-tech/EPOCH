package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductInfo implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
    private static final long serialVersionUID = 1L;
    @JsonProperty("cpopProductId")
    private String cpopProductId;

    @JsonProperty("productCode")
    private String productCode;

    @JsonProperty("productType")
    private String productType;

    @JsonProperty("pricePlanCode")
    private String pricePlanCode;
    
    @JsonProperty("pickCode")
    private String pickCode;
    
    @JsonProperty("priceCode")
    private String priceCode;
    
    @JsonProperty("billingProductCode")
    private String billingProductCode;
    
    @JsonProperty("productId")
    private String productId;
    
    @JsonProperty("streamType")
    private String streamType;

    @JsonProperty("promotions")
    private List<CustomerPromotion> promotions;

    @JsonProperty
    private String basePrice;
    
    @JsonProperty("billingReferenceID")
    private String billingReferenceID;
    
    private String componentCode;
    
   	private String modelNumber;
   	
   	private String manufacturer;
	private String accessCardId;
	private String accessCardStatus;
	private String equipmentOwnership;
	
    private String productStatus;
	private Integer quantity;
	private String action;

	private String productStartDate;
	private String priceTier;
	private String serviceSubscriptionType;
	private String status;
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getServiceSubscriptionType() {
		return serviceSubscriptionType;
	}
	public void setServiceSubscriptionType(String serviceSubscriptionType) {
		this.serviceSubscriptionType = serviceSubscriptionType;
	}
	public String getPriceTier() {
		return priceTier;
	}
	public void setPriceTier(String priceTier) {
		this.priceTier = priceTier;
	}

	public String getProductStartDate() {
		return productStartDate;
	}

	public void setProductStartDate(String productStartDate) {

		this.productStartDate = productStartDate;
	}

	private String productEndDate;

	public String getProductEndDate() {

		return productEndDate;
	}

	public void setProductEndDate(String productEndDate) {

		this.productEndDate = productEndDate;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public String getProductStatus() {
		return productStatus;
	}
	public void setProductStatus(String productStatus) {
		this.productStatus = productStatus;
	}
   	
   	public String getManufacturer() {
		return manufacturer;
	}
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}
	public String getAccessCardId() {
		return accessCardId;
	}
	public void setAccessCardId(String accessCardId) {
		this.accessCardId = accessCardId;
	}
	public String getAccessCardStatus() {
		return accessCardStatus;
	}
	public void setAccessCardStatus(String accessCardStatus) {
		this.accessCardStatus = accessCardStatus;
	}
	public String getEquipmentOwnership() {
		return equipmentOwnership;
	}
	public void setEquipmentOwnership(String equipmentOwnership) {
		this.equipmentOwnership = equipmentOwnership;
	}
   	
   	public String getPriceCode() {
		return priceCode;
	}
	public void setPriceCode(String priceCode) {
		this.priceCode = priceCode;
	}
	public String getComponentCode() {
   		return componentCode;
   	}
   	public void setComponentCode(String componentCode) {
   		this.componentCode = componentCode;
   	}
   	
    public String getModelNumber() {
		return modelNumber;
	}
	public void setModelNumber(String modelNumber) {
		this.modelNumber = modelNumber;
	}
	public String getBillingReferenceID() {
		return billingReferenceID;
	}

	public void setBillingReferenceID(String billingReferenceID) {
		this.billingReferenceID = billingReferenceID;
	}

    public String getCpopProductId() {
        return cpopProductId;
    }

    public void setCpopProductId(String cpopProductId) {
        this.cpopProductId = cpopProductId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getPricePlanCode() {
        return pricePlanCode;
    }

    public void setPricePlanCode(String pricePlanCode) {
        this.pricePlanCode = pricePlanCode;
    }

    public String getPickCode() {
		return pickCode;
	}

	public void setPickCode(String pickCode) {
		this.pickCode = pickCode;
	}

	public List<CustomerPromotion> getPromotions() {
        return promotions;
    }

    public void setPromotions(List<CustomerPromotion> promotions) {
        this.promotions = promotions;
    }

    public String getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(String basePrice) {
        this.basePrice = basePrice;
    }

    public String getBillingProductCode() {
		return billingProductCode;
	}

	public void setBillingProductCode(String billingProductCode) {
		this.billingProductCode = billingProductCode;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getStreamType() {
		return streamType;
	}

	public void setStreamType(String streamType) {
		this.streamType = streamType;
	}

	@Override
	public String toString() {
		return "ProductInfo [cpopProductId=" + cpopProductId + ", productCode=" + productCode + ", productType="
				+ productType + ", pricePlanCode=" + pricePlanCode + ", pickCode=" + pickCode + ", billingProductCode="
				+ billingProductCode + ", productId=" + productId + ", streamType=" + streamType + ", promotions="
				+ promotions + ", basePrice=" + basePrice + ", billingReferenceID=" + billingReferenceID
				+ ", componentCode=" + componentCode + ", modelNumber=" + modelNumber
				+ ", manufacturer=" + manufacturer + ", accessCardId=" + accessCardId
				+ ", accessCardStatus=" + accessCardStatus + ", equipmentOwnership=" + equipmentOwnership
				+ ", productStartDate =" + productStartDate
				+ ", productEndDate =" + productEndDate +"]";
		
	}

	@JsonProperty("delayProvisioningTillTime")
	private String delayProvisioningTillTime;

	public String getDelayProvisioningTillTime() {
		return delayProvisioningTillTime;
	}

	public void setDelayProvisioningTillTime(String delayProvisioningTillTime) {
		this.delayProvisioningTillTime = delayProvisioningTillTime;
	}

}
