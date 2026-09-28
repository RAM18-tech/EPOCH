package com.dtv.dcp.epoch.model.ct.request;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CTCartProduct implements Serializable {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;
    @JsonProperty("products")
    private List<CTProductInfo> products;
    @JsonProperty("accountNumber")
    private String accountNumber;

    // IPTV/DTVS migration Attr
    private String sunsetDate;
    private boolean isActive;

    // OTT Specific Attributes
    private Boolean retentionPromotionIndicator;
    private String segmentDescription;
    private String coolOffPeriod;
    private Boolean freeTrialEligible;
    private Boolean isDefaultPromo;
    private String accountType;
    private String nextBillingDate;
    @JsonProperty("productFamily")
    private String productFamily;
    @JsonProperty("isContracted")
    private Boolean isContracted;
    @JsonProperty("benefits")
    private List<CTBenefit> benefits;
    @JsonProperty("accountStatus")
    private String accountStatus;
    

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getNextBillingDate() {
        return nextBillingDate;
    }

    public void setNextBillingDate(String nextBillingDate) {
        this.nextBillingDate = nextBillingDate;
    }


    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public Boolean getIsActive() {
        return isActive();
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsDefaultPromo() {
        return isDefaultPromo;
    }

    public void setIsDefaultPromo(Boolean isDefaultPromo) {
        this.isDefaultPromo = isDefaultPromo;
    }

    public List<CTProductInfo> getProducts() {
        return products;
    }

    public void setProducts(List<CTProductInfo> products) {
        this.products = products;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getSunsetDate() {
        return sunsetDate;
    }

    public void setSunsetDate(String sunsetDate) {
        this.sunsetDate = sunsetDate;
    }

    public Boolean getRetentionPromotionIndicator() {
        return retentionPromotionIndicator;
    }

    public void setRetentionPromotionIndicator(Boolean retentionPromotionIndicator) {
        this.retentionPromotionIndicator = retentionPromotionIndicator;
    }

    public String getSegmentDescription() {
        return segmentDescription;
    }

    public void setSegmentDescription(String segmentDescription) {
        this.segmentDescription = segmentDescription;
    }

    public String getCoolOffPeriod() {
        return coolOffPeriod;
    }

    public void setCoolOffPeriod(String coolOffPeriod) {
        this.coolOffPeriod = coolOffPeriod;
    }

    public Boolean getFreeTrialEligible() {
        return freeTrialEligible;
    }

    public void setFreeTrialEligible(Boolean freeTrialEligible) {
        this.freeTrialEligible = freeTrialEligible;
    }

    public Boolean getDefaultPromo() {
        return getIsDefaultPromo();
    }

    public void setDefaultPromo(Boolean defaultPromo) {
        isDefaultPromo = defaultPromo;
    }

    public String getProductFamily() {
		return productFamily;
	}

	public void setProductFamily(String productFamily) {
		this.productFamily = productFamily;
	}

	public Boolean getIsContracted() {
		return isContracted;
	}

	public void setIsContracted(Boolean isContracted) {
		this.isContracted = isContracted;
	}

	public List<CTBenefit> getBenefits() {
		return benefits;
	}

	public void setBenefits(List<CTBenefit> benefits) {
		this.benefits = benefits;
	}

	public String getAccountStatus() {
		return accountStatus;
	}

	public void setAccountStatus(String accountStatus) {
		this.accountStatus = accountStatus;
	}

	@Override
    public String toString() {
        return "CartProduct{" +
                "products:" + products +
                ", accountNumber:'" + accountNumber + '\'' +
                ", sunsetDate:'" + sunsetDate + '\'' +
                ", isActive:" + isActive +
                ", retentionPromotionIndicator:" + retentionPromotionIndicator +
                ", segmentDescription:'" + segmentDescription + '\'' +
                ", coolOffPeriod:'" + coolOffPeriod + '\'' +
                ", freeTrialEligible:" + freeTrialEligible +
                ", isDefaultPromo:" + isDefaultPromo +
                ", accountType:'" + accountType + '\'' +
                ", nextBillingDate:'" + nextBillingDate + '\'' +
                ", productFamily:'" + productFamily + '\'' +
                ", isContracted:'" + isContracted + '\'' +
                ", benefits:'" + benefits + '\'' +
                ", accountStatus:'" + accountStatus + '\'' +
                '}';
    }
}
