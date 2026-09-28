package com.dtv.dcp.epoch.model.common.response.ucc;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Benefit implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private String name;
	private String benefitBillingCode;
	private String description;
	private String benefitType;
	private String duration;
	private String period;
	private BenefitValue benefitValue;
	private List<ApplicableProduct> applicableProducts;
	private boolean rtpIndicator;
	private DisclosureMessagesByKey disclosureMessagesByKey;
	private DescriptionByKey descriptionsByKey;
	private DisplayNamesByKey displayNamesByKey;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBenefitBillingCode() {
		return benefitBillingCode;
	}

	public void setBenefitBillingCode(String benefitBillingCode) {
		this.benefitBillingCode = benefitBillingCode;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getBenefitType() {
		return benefitType;
	}

	public void setBenefitType(String benefitType) {
		this.benefitType = benefitType;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public BenefitValue getBenefitValue() {
		return benefitValue;
	}

	public void setBenefitValue(BenefitValue benefitValue) {
		this.benefitValue = benefitValue;
	}

	public List<ApplicableProduct> getApplicableProducts() {
		return applicableProducts;
	}

	public void setApplicableProducts(List<ApplicableProduct> applicableProducts) {
		this.applicableProducts = applicableProducts;
	}

	public boolean isRtpIndicator() {
		return rtpIndicator;
	}

	public void setRtpIndicator(boolean rtpIndicator) {
		this.rtpIndicator = rtpIndicator;
	}

	public DisclosureMessagesByKey getDisclosureMessagesByKey() {
		return disclosureMessagesByKey;
	}

	public void setDisclosureMessagesByKey(DisclosureMessagesByKey disclosureMessagesByKey) {
		this.disclosureMessagesByKey = disclosureMessagesByKey;
	}

	public DescriptionByKey getDescriptionsByKey() {
		return descriptionsByKey;
	}

	public void setDescriptionsByKey(DescriptionByKey descriptionsByKey) {
		this.descriptionsByKey = descriptionsByKey;
	}

	public DisplayNamesByKey getDisplayNamesByKey() {
		return displayNamesByKey;
	}

	public void setDisplayNamesByKey(DisplayNamesByKey displayNamesByKey) {
		this.displayNamesByKey = displayNamesByKey;
	}

	@Override
	public String toString() {
		return "Benefit [id=" + id + ", name=" + name + ", benefitBillingCode=" + benefitBillingCode + ", description="
				+ description + ", benefitType=" + benefitType + ", duration=" + duration + ", period=" + period
				+ ", benefitValue=" + benefitValue + ", applicableProducts=" + applicableProducts + ", rtpIndicator="
				+ rtpIndicator + ", disclosureMessagesByKey=" + disclosureMessagesByKey + ", descriptionsByKey="
				+ descriptionsByKey + ", displayNamesByKey=" + displayNamesByKey + "]";
	}

}