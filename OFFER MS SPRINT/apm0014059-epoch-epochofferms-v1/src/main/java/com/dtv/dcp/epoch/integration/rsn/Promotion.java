package com.dtv.dcp.epoch.integration.rsn;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


/**
 * The Class Promotion.
 */
@ApiModel(value = "Promotion")
@JsonInclude(Include.NON_NULL)
public class Promotion implements Serializable { 

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The promotion code. */
	@ApiModelProperty(value = "Promotion ID.")
	@JsonProperty("promotionCode")
	private String promotionCode;

	/** The promo type. */
	@ApiModelProperty(value = "Promo Type can take values:" + " - Flat" + " - Free" + " - Flat Off" + " - Percentage"
			+ " - Percentage Off.")
	@JsonProperty("promoType")
	private String promoType;

	/** The promotion category. */
	@ApiModelProperty(value = "It takes one of the value listed below:" + "- PayInAdvance" + "- Dependent"
			+ "- Product.")
	@JsonProperty("promotionCategory")
	private String promotionCategory;

	/** The display name. */
	@ApiModelProperty(value = "Display Name of the Promotion.")
	@JsonProperty("displayName")
	private String displayName;

	/** The duration. */
	@ApiModelProperty(value = "duraiton of the Promotion.")
	@JsonProperty("duration")
	private String duration;

	/** The duration period. */
	@ApiModelProperty(value = "Will take one of the values listed below:" + " - No.of Days" + " - Months" + " - years.")
	@JsonProperty("durationPeriod")
	private String durationPeriod;

	/** The billing product code. */
	@ApiModelProperty(value = "Billing Product Code.")
	@JsonProperty("billingProductCode")
	private String billingProductCode;

	/** The billing product id. */
	@ApiModelProperty(value = "Billing Product ID.")
	@JsonProperty("billingProductId")
	private String billingProductId;

	/** The free trial period. */
	@ApiModelProperty(value = "Number of free Trial Days. If promoType = "
			+ "‘Free’ then based on durationPeriod field, value in this field varis. Ex:"
			+ "if durationPeriod =’Months’, PromotType =’Free’ and freeTrialPeriod=’10’ "
			+ "then it indicates that Promotion is free for 10 Months.")
	@JsonProperty("freeTrialPeriod")
	private Integer freeTrialPeriod;

	/** The provision code. */
	@ApiModelProperty(value = "provision code of Offer")
	@JsonProperty("provisioningCode")
	private String provisioningCode;

	/** The amount. */
	@ApiModelProperty(value = "Promotion amount")
	@JsonProperty("amount")
	private Double amount;
	
	/** The parent promotion id. */
	@ApiModelProperty(value = "parent Promotion id of the promo")
	@JsonProperty("parentPromotionId")
	private String parentPromotionId;
	
	/** The double Percentage. */
	@ApiModelProperty(value = "percentage of the promotion")
	@JsonProperty("percentage")
	private Double percentage;
	
	/** The String prepayPeriod. */
	@ApiModelProperty(value = "PrePay period of the promo")
	@JsonProperty("prepayPeriod")
	private String prepayPeriod;
	
	/** The String prepyDuration. */
	@ApiModelProperty(value = "Pre Pay duration of the promo")
	@JsonProperty("prepayDuration")
	private String prepayDuration;
	
	/** The description code. */
	@ApiModelProperty(value = "description code of offers")
	@JsonProperty("description")
	private String description;
	
	 /**   relationType. */
	@ApiModelProperty(value = "String to store name of the promo")
    @JsonProperty("name")
    private String name;
    
    /**   String  displayOrder. */
    @ApiModelProperty(value = "String to store display Order of the promo")
    @JsonProperty("displayOrder")
    private Integer displayOrder;
    
    /** The agent offer. */
    @ApiModelProperty(value = "String to store whether offer is for agent or not ")
    @JsonProperty("agentOffer")
    private String agentOffer;
    
    
    /** The ext promo type. */
    private String extPromoType;
    
    /** The io offer. */
    @JsonProperty("ioOffer")
    private Boolean ioOffer;
    
    /** The contract indicator. */
    @ApiModelProperty(value = "ContractIndicator of the promotion")
    @JsonProperty("contractIndicator")
    private boolean contractIndicator;
    
    
    /** The contract version. */
    @ApiModelProperty(value = "Represents contract version ")
    @JsonProperty("contractVersion")
    private String contractVersion;
    
    /** The promo priority. */
    @ApiModelProperty(value = "Represents promo priority")
    @JsonProperty("promoPriority")
    private String promoPriority;
    
    /** The free trial promo. */
    @ApiModelProperty(value = "Represents promo is freeTrial eligible or not")
    @JsonProperty("freeTrialPromo")
    private boolean freeTrialPromo;
    
    /** The max occurrence. */
    @ApiModelProperty(value = "This will tell how many times this promotion can be applied in sales flow.")
    @JsonProperty("maxOccurrence")
    private Integer maxOccurrence;
    
    /** The segment description. */
    private String segmentDescription;
    
    /**The String myATTDisclosureMsg*/
	@JsonProperty("myATTDisclosureMsg")
	private String myATTDisclosureMsg;
	
	/**The String oPUSDisclosureMsg*/
	@JsonProperty("oPUSDisclosureMsg")
	private String oPUSDisclosureMsg;
	
	
	/** The long description for sales. */
	@JsonProperty("longDescriptionForSales")
	private String longDescriptionForSales;


	/** The display name for sales. */
	@JsonProperty("displayNameForSales")
	private String displayNameForSales;

	
	/** The short description for sales. */
	@JsonProperty("shortDescriptionForSales")
	private List<ShortDescription> shortDescriptionForSales;


	/** The description for sales. */
	@JsonProperty("descriptionForSales")
	private String descriptionForSales;
	
	
	/** The long description for services. */
	@JsonProperty("longDescriptionForServices")
	private String longDescriptionForServices;


	/** The display name for sales. */
	@JsonProperty("displayNameForServices")
	private String displayNameForServices;

	
	/** The short description for sales. */
	@JsonProperty("shortDescriptionForServices")
	private String shortDescriptionForServices;


	/** The description for sales. */
	@JsonProperty("descriptionForServices")
	private String descriptionForServices;
	
	/** The validityEndDate. */
	  private String validityEndDate;

		/** The enableServiceDisconnect. */
		private Boolean enableServiceDisconnect;

		/**
		 * @return the enableServiceDisconnect
		 */
		public Boolean isEnableServiceDisconnect() {
			return enableServiceDisconnect;
		}

		/**
		 * @param enableServiceDisconnect the enableServiceDisconnect to set
		 */
		public void setEnableServiceDisconnect(Boolean enableServiceDisconnect) {
			this.enableServiceDisconnect = enableServiceDisconnect;
		}

		/**
		 * @return the validityEndDate
		 */
		public String getValidityEndDate() {
			return validityEndDate;
		}

		/**
		 * @param validityEndDate the validityEndDate to set
		 */
		public void setValidityEndDate(String validityEndDate) {
			this.validityEndDate = validityEndDate;
		}
	
	/**
	 * Gets the long description for services.
	 *
	 * @return the long description for services
	 */
	public String getLongDescriptionForServices() {
		return longDescriptionForServices;
	}

	/**
	 * Sets the long description for services.
	 *
	 * @param longDescriptionForServices the new long description for services
	 */
	public void setLongDescriptionForServices(String longDescriptionForServices) {
		this.longDescriptionForServices = longDescriptionForServices;
	}

	/**
	 * Gets the display name for services.
	 *
	 * @return the display name for services
	 */
	public String getDisplayNameForServices() {
		return displayNameForServices;
	}

	/**
	 * Sets the display name for services.
	 *
	 * @param displayNameForServices the new display name for services
	 */
	public void setDisplayNameForServices(String displayNameForServices) {
		this.displayNameForServices = displayNameForServices;
	}

	/**
	 * Gets the short description for services.
	 *
	 * @return the short description for services
	 */
	public String getShortDescriptionForServices() {
		return shortDescriptionForServices;
	}

	/**
	 * Sets the short description for services.
	 *
	 * @param shortDescriptionForServices the new short description for services
	 */
	public void setShortDescriptionForServices(String shortDescriptionForServices) {
		this.shortDescriptionForServices = shortDescriptionForServices;
	}

	/**
	 * Gets the description for services.
	 *
	 * @return the description for services
	 */
	public String getDescriptionForServices() {
		return descriptionForServices;
	}

	/**
	 * Sets the description for services.
	 *
	 * @param descriptionForServices the new description for services
	 */
	public void setDescriptionForServices(String descriptionForServices) {
		this.descriptionForServices = descriptionForServices;
	}

	/**
	 * Gets the long description for sales.
	 *
	 * @return the long description for sales
	 */
	public String getLongDescriptionForSales() {
		return longDescriptionForSales;
	}

	/**
	 * Sets the long description for sales.
	 *
	 * @param longDescriptionForSales the new long description for sales
	 */
	public void setLongDescriptionForSales(String longDescriptionForSales) {
		this.longDescriptionForSales = longDescriptionForSales;
	}

	/**
	 * Gets the display name for sales.
	 *
	 * @return the display name for sales
	 */
	public String getDisplayNameForSales() {
		return displayNameForSales;
	}

	/**
	 * Sets the display name for sales.
	 *
	 * @param displayNameForSales the new display name for sales
	 */
	public void setDisplayNameForSales(String displayNameForSales) {
		this.displayNameForSales = displayNameForSales;
	}




	/**
	 * Gets the description for sales.
	 *
	 * @return the description for sales
	 */
	public String getDescriptionForSales() {
		return descriptionForSales;
	}

	/**
	 * Sets the description for sales.
	 *
	 * @param descriptionForSales the new description for sales
	 */
	public void setDescriptionForSales(String descriptionForSales) {
		this.descriptionForSales = descriptionForSales;
	}

	
	/**
	 * Gets the short description for sales.
	 *
	 * @return the short description for sales
	 */
	public List<ShortDescription> getShortDescriptionForSales() {
		return shortDescriptionForSales;
	}

	/**
	 * Sets the short description for sales.
	 *
	 * @param shortDescriptionForSales the new short description for sales
	 */
	public void setShortDescriptionForSales(List<ShortDescription> shortDescriptionForSales) {
		this.shortDescriptionForSales = shortDescriptionForSales;
	}
	
	/**
	 * @return String
	 */
	public String getoPUSDisclosureMsg() {
		return oPUSDisclosureMsg;
	}

	/**
	 * @param oPUSDisclosureMsg
	 */
	public void setoPUSDisclosureMsg(String oPUSDisclosureMsg) {
		this.oPUSDisclosureMsg = oPUSDisclosureMsg;
	}

	/**
	 * @return String
	 */
	public String getMyATTDisclosureMsg() {
		return myATTDisclosureMsg;
	}

	/**
	 * @param myATTDisclosureMsg
	 */
	public void setMyATTDisclosureMsg(String myATTDisclosureMsg) {
		this.myATTDisclosureMsg = myATTDisclosureMsg;
	}
    
 
	/**
	 * Gets the segment description.
	 *
	 * @return the segment description
	 */
	public String getSegmentDescription() {
		return segmentDescription;
	}

	/**
	 * Sets the segment description.
	 *
	 * @param segmentDescription the new segment description
	 */
	public void setSegmentDescription(String segmentDescription) {
		this.segmentDescription = segmentDescription;
	}

	/**
	 * Gets the max occurrence.
	 *
	 * @return the maxOccurrence
	 */
	public Integer getMaxOccurrence() {
		return maxOccurrence;
	}

	/**
	 * Sets the max occurrence.
	 *
	 * @param maxOccurrence the maxOccurrence to set
	 */
	public void setMaxOccurrence(Integer maxOccurrence) {
		this.maxOccurrence = maxOccurrence;
	}

    
	/**
	 * Checks if is free trial promo.
	 *
	 * @return the freeTrialPromo
	 */
	public boolean isFreeTrialPromo() {
		return freeTrialPromo;
	}

	/**
	 * Sets the free trial promo.
	 *
	 * @param freeTrialPromo the freeTrialPromo to set
	 */
	public void setFreeTrialPromo(boolean freeTrialPromo) {
		this.freeTrialPromo = freeTrialPromo;
	}

	/**
	 * Gets the contract version.
	 *
	 * @return the contractVersion
	 */
	public String getContractVersion() {
		return contractVersion;
	}

	/**
	 * Sets the contract version.
	 *
	 * @param contractVersion the contractVersion to set
	 */
	public void setContractVersion(String contractVersion) {
		this.contractVersion = contractVersion;
	}

	/**
	 * Checks if is contract indicator.
	 *
	 * @return boolean
	 */
	public boolean isContractIndicator() {
		return contractIndicator;
	}

	/**
	 * Sets the contract indicator.
	 *
	 * @param contractIndicator the new contract indicator
	 */
	public void setContractIndicator(boolean contractIndicator) {
		this.contractIndicator = contractIndicator;
	}


	/**
	 * Gets the io offer.
	 *
	 * @return the io offer
	 */
	public Boolean getIoOffer() {
		return ioOffer;
	}

	/**
	 * Sets the io offer.
	 *
	 * @param ioOffer the new io offer
	 */
	public void setIoOffer(Boolean ioOffer) {
		this.ioOffer = ioOffer;
	}
	
	/**
	 * Gets the ext promo type.
	 *
	 * @return the ext promo type
	 */
	public String getExtPromoType() {
		return extPromoType;
	}

	/**
	 * Sets the ext promo type.
	 *
	 * @param extPromoType the new ext promo type
	 */
	public void setExtPromoType(String extPromoType) {
		this.extPromoType = extPromoType;
	}

	/**
	 * Gets the agent offer.
	 *
	 * @return String
	 */
	public String getAgentOffer() {
		return agentOffer;
	}

	/**
	 * Sets the agent offer.
	 *
	 * @param agentOffer the new agent offer
	 */
	public void setAgentOffer(String agentOffer) {
		this.agentOffer = agentOffer;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	

	/** The expDate code. */
	@ApiModelProperty(value = "expDate code of offers")
	@JsonProperty("expDate")
	private String expDate;
	
	/**
	 * Gets the exp date.
	 *
	 * @return the exp date
	 */
	public String getExpDate() {
		return expDate;
	}

	/**
	 * Sets the exp date.
	 *
	 * @param expDate the new exp date
	 */
	public void setExpDate(String expDate) {
		this.expDate = expDate;
	}

	/**
	 * Gets the prepay period.
	 *
	 * @return String
	 */
	public String getPrepayPeriod() {
		return prepayPeriod;
	}

	/**
	 * Sets the prepay period.
	 *
	 * @param prepayPeriod the new prepay period
	 */
	public void setPrepayPeriod(String prepayPeriod) {
		this.prepayPeriod = prepayPeriod;
	}

	/**
	 * Gets the prepay duration.
	 *
	 * @return String
	 */
	public String getPrepayDuration() {
		return prepayDuration;
	}

	/**
	 * Sets the prepay duration.
	 *
	 * @param prepayDuration the new prepay duration
	 */
	public void setPrepayDuration(String prepayDuration) {
		this.prepayDuration = prepayDuration;
	}

	/**
	 * Gets the percentage.
	 *
	 * @return Double
	 */
	public Double getPercentage() {
		return percentage;
	}

	/**
	 * Sets the percentage.
	 *
	 * @param percentage the new percentage
	 */
	public void setPercentage(Double percentage) {
		this.percentage = percentage;
	}

	/**
	 * Gets the parent promotion id.
	 *
	 * @return the parent promotion id
	 */
	public String getParentPromotionId() {
		return parentPromotionId;
	}

	/**
	 * Sets the parent promotion id.
	 *
	 * @param parentPromotionId the new parent promotion id
	 */
	public void setParentPromotionId(String parentPromotionId) {
		this.parentPromotionId = parentPromotionId;
	}

	/**
	 * Gets the amount.
	 *
	 * @return the amount
	 */
	public Double getAmount() {
		return amount;
	}

	/**
	 * Sets the amount.
	 *
	 * @param amount
	 *            the new amount
	 */
	public void setAmount(Double amount) {
		this.amount = amount;
	}

	/**
	 * Gets the promotion code.
	 *
	 * @return the promotion code
	 */
	public String getPromotionCode() {
		return promotionCode;
	}

	/**
	 * Sets the promotion code.
	 *
	 * @param promotionCode
	 *            the new promotion code
	 */
	public void setPromotionCode(String promotionCode) {
		this.promotionCode = promotionCode;
	}

	/**
	 * Gets the promo type.
	 *
	 * @return the promo type
	 */
	public String getPromoType() {
		return promoType;
	}

	/**
	 * Sets the promo type.
	 *
	 * @param promoType
	 *            the new promo type
	 */
	public void setPromoType(String promoType) {
		this.promoType = promoType;
	}

	/**
	 * Gets the promotion category.
	 *
	 * @return the promotion category
	 */
	public String getPromotionCategory() {
		return promotionCategory;
	}

	/**
	 * Sets the promotion category.
	 *
	 * @param promotionCategory
	 *            the new promotion category
	 */
	public void setPromotionCategory(String promotionCategory) {
		this.promotionCategory = promotionCategory;
	}

	/**
	 * Gets the display name.
	 *
	 * @return the display name
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Sets the display name.
	 *
	 * @param displayName
	 *            the new display name
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Gets the duration.
	 *
	 * @return the duration
	 */
	public String getDuration() {
		return duration;
	}

	/**
	 * Sets the duration.
	 *
	 * @param duration
	 *            the new duration
	 */
	public void setDuration(String duration) {
		this.duration = duration;
	}

	/**
	 * Gets the duration period.
	 *
	 * @return the duration period
	 */
	public String getDurationPeriod() {
		return durationPeriod;
	}

	/**
	 * Sets the duration period.
	 *
	 * @param durationPeriod
	 *            the new duration period
	 */
	public void setDurationPeriod(String durationPeriod) {
		this.durationPeriod = durationPeriod;
	}

	/**
	 * Gets the billing product code.
	 *
	 * @return the billing product code
	 */
	public String getBillingProductCode() {
		return billingProductCode;
	}

	/**
	 * Sets the billing product code.
	 *
	 * @param billingProductCode
	 *            the new billing product code
	 */
	public void setBillingProductCode(String billingProductCode) {
		this.billingProductCode = billingProductCode;
	}

	/**
	 * Gets the billing product id.
	 *
	 * @return the billing product id
	 */
	public String getBillingProductId() {
		return billingProductId;
	}

	/**
	 * Sets the billing product id.
	 *
	 * @param billingProductId
	 *            the new billing product id
	 */
	public void setBillingProductId(String billingProductId) {
		this.billingProductId = billingProductId;
	}

	/**
	 * Gets the free trial period.
	 *
	 * @return the free trial period
	 */
	public Integer getFreeTrialPeriod() {
		return freeTrialPeriod;
	}

	/**
	 * Sets the free trial period.
	 *
	 * @param freeTrialPeriod
	 *            the new free trial period
	 */
	public void setFreeTrialPeriod(Integer freeTrialPeriod) {
		this.freeTrialPeriod = freeTrialPeriod;
	}

	/**
	 * Gets the provisioning code.
	 *
	 * @return the provisioning code
	 */
	public String getProvisioningCode() {
		return provisioningCode;
	}

	/**
	 * Sets the provisioning code.
	 *
	 * @param provisioningCode
	 *            the new provisioning code
	 */
	public void setProvisioningCode(String provisioningCode) {
		this.provisioningCode = provisioningCode;
	}



	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the display order.
	 *
	 * @return the display order
	 */
	public Integer getDisplayOrder() {
		return displayOrder;
	}

	/**
	 * Sets the display order.
	 *
	 * @param displayOrder the new display order
	 */
	public void setDisplayOrder(Integer displayOrder) {
		this.displayOrder = displayOrder;
	}
	
	

	/**
	 * Gets the promo priority.
	 *
	 * @return the promoPriority
	 */
	public String getPromoPriority() {
		return promoPriority;
	}

	/**
	 * Sets the promo priority.
	 *
	 * @param promoPriority the promoPriority to set
	 */
	public void setPromoPriority(String promoPriority) {
		this.promoPriority = promoPriority;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Promotion [promotionCode=");
		builder.append(promotionCode);
		builder.append(", promoType=");
		builder.append(promoType);
		builder.append(", promotionCategory=");
		builder.append(promotionCategory);
		builder.append(", displayName=");
		builder.append(displayName);
		builder.append(", duration=");
		builder.append(duration);
		builder.append(", durationPeriod=");
		builder.append(durationPeriod);
		builder.append(", billingProductCode=");
		builder.append(billingProductCode);
		builder.append(", billingProductId=");
		builder.append(billingProductId);
		builder.append(", freeTrialPeriod=");
		builder.append(freeTrialPeriod);
		builder.append(", provisioningCode=");
		builder.append(provisioningCode);
		builder.append(", amount=");
		builder.append(amount);
		builder.append(", parentPromotionId=");
		builder.append(parentPromotionId);
		builder.append(", percentage=");
		builder.append(percentage);
		builder.append(", prepayPeriod=");
		builder.append(prepayPeriod);
		builder.append(", prepayDuration=");
		builder.append(prepayDuration);
		builder.append(", description=");
		builder.append(description);
		builder.append(", name=");
		builder.append(name);
		builder.append(", displayOrder=");
		builder.append(displayOrder);
		builder.append(", agentOffer=");
		builder.append(agentOffer);
		builder.append(", extPromoType=");
		builder.append(extPromoType);
		builder.append(", ioOffer=");
		builder.append(ioOffer);
		builder.append(", contractIndicator=");
		builder.append(contractIndicator);
		builder.append(", contractVersion=");
		builder.append(contractVersion);
		builder.append(", promoPriority=");
		builder.append(promoPriority);
		builder.append(", freeTrialPromo=");
		builder.append(freeTrialPromo);
		builder.append(", maxOccurrence=");
		builder.append(maxOccurrence);
		builder.append(", segmentDescription=");
		builder.append(segmentDescription);
		builder.append(", myATTDisclosureMsg=");
		builder.append(myATTDisclosureMsg);
		builder.append(", oPUSDisclosureMsg=");
		builder.append(oPUSDisclosureMsg);
		builder.append(", expDate=");
		builder.append(expDate);
		builder.append("]");
		return builder.toString();
	}
	
}