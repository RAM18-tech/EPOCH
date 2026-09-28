package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.ct.generic.GenericTypeIdBase;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Constraints implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	
	/** The isAnchor. */
	private Boolean isAnchor;
	
	/** The isDependent. */
	private Boolean isDependent;
	
	
	/** The productFamily. */
	private String productFamily;
	

	/** The productIds. */
	private List<GenericTypeIdBase> productIds;
	
	/** The productCategories. */
	private List<GenericTypeIdBase> productCategories;
	
	/** The productType. */
	private List<GenericTypeIdBase> productType;
	
	/** The productType. */
	private List<String> productTypes;

	private List<String> eligibleSalesChannels;

	private List<String> excludedPartners;

	//Price Policy attributes
	private String startDate;
	private String endDate;
	private Integer maxTVFeesWaivedForWired;
	private Integer maxTVFeesWaivedForWireless;
	private Integer maxTVFeesWaivedForGenieMiniWired;
	private Integer maxTVFeesWaivedForGenieMiniWireless;
	private Integer includedGenieWired;
	private Integer includedGenieWireless;
	private Integer waiveHDDVRNRCCount;
	private Integer includedGenieMiniWireless;
	private Integer includedGenieMiniWired;
	private Integer discountedGenieMiniWired;
	private Integer maxAddlTVsSelectedWired;
	private Integer discountedGenieMiniWireless;
	private Integer maxAddlTVsSelectedWireless;
	private String customerTypes;
	private String productStatus;
	private String policyText;
	
	private String modelNos;
	private String allowedAction;

	public List<String> getEligibleSalesChannels() {
		return eligibleSalesChannels;
	}

	public void setEligibleSalesChannels(List<String> eligibleSalesChannels) {
		this.eligibleSalesChannels = eligibleSalesChannels;
	}

	public List<String> getExcludedPartners() {
		return excludedPartners;
	}

	public void setExcludedPartners(List<String> excludedPartners) {
		this.excludedPartners = excludedPartners;
	}

	private List<Product> qualifierIncompatibleProducts;
	
	public List<Product> getQualifierIncompatibleProducts() {
		return qualifierIncompatibleProducts;
	}

	public void setQualifierIncompatibleProducts(List<Product> qualifierIncompatibleProducts) {
		this.qualifierIncompatibleProducts = qualifierIncompatibleProducts;
	}
	
	public String getAllowedAction() {
		return allowedAction;
	}

	public void setAllowedAction(String allowedAction) {
		this.allowedAction = allowedAction;
	}
	
	public String getModelNos() {
		return modelNos;
	}

	public void setModelNos(String modelNos) {
		this.modelNos = modelNos;
	}
	
	/**
	 * @return the maxTVFeesWaivedForWired
	 */
	public Integer getMaxTVFeesWaivedForWired() {
		return maxTVFeesWaivedForWired;
	}

	/**
	 * @param maxTVFeesWaivedForWired the maxTVFeesWaivedForWired to set
	 */
	public void setMaxTVFeesWaivedForWired(Integer maxTVFeesWaivedForWired) {
		this.maxTVFeesWaivedForWired = maxTVFeesWaivedForWired;
	}

	/**
	 * @return the maxTVFeesWaivedForWireless
	 */
	public Integer getMaxTVFeesWaivedForWireless() {
		return maxTVFeesWaivedForWireless;
	}

	/**
	 * @param maxTVFeesWaivedForWireless the maxTVFeesWaivedForWireless to set
	 */
	public void setMaxTVFeesWaivedForWireless(Integer maxTVFeesWaivedForWireless) {
		this.maxTVFeesWaivedForWireless = maxTVFeesWaivedForWireless;
	}

	/**
	 * @return the maxTVFeesWaivedForGenieMiniWired
	 */
	public Integer getMaxTVFeesWaivedForGenieMiniWired() {
		return maxTVFeesWaivedForGenieMiniWired;
	}

	/**
	 * @param maxTVFeesWaivedForGenieMiniWired the maxTVFeesWaivedForGenieMiniWired to set
	 */
	public void setMaxTVFeesWaivedForGenieMiniWired(Integer maxTVFeesWaivedForGenieMiniWired) {
		this.maxTVFeesWaivedForGenieMiniWired = maxTVFeesWaivedForGenieMiniWired;
	}

	/**
	 * @return the maxTVFeesWaivedForGenieMiniWireless
	 */
	public Integer getMaxTVFeesWaivedForGenieMiniWireless() {
		return maxTVFeesWaivedForGenieMiniWireless;
	}

	/**
	 * @param maxTVFeesWaivedForGenieMiniWireless the maxTVFeesWaivedForGenieMiniWireless to set
	 */
	public void setMaxTVFeesWaivedForGenieMiniWireless(Integer maxTVFeesWaivedForGenieMiniWireless) {
		this.maxTVFeesWaivedForGenieMiniWireless = maxTVFeesWaivedForGenieMiniWireless;
	}

	/**
	 * @return the includedGenieWired
	 */
	public Integer getIncludedGenieWired() {
		return includedGenieWired;
	}

	/**
	 * @param includedGenieWired the includedGenieWired to set
	 */
	public void setIncludedGenieWired(Integer includedGenieWired) {
		this.includedGenieWired = includedGenieWired;
	}

	/**
	 * @return the includedGenieWireless
	 */
	public Integer getIncludedGenieWireless() {
		return includedGenieWireless;
	}

	/**
	 * @param includedGenieWireless the includedGenieWireless to set
	 */
	public void setIncludedGenieWireless(Integer includedGenieWireless) {
		this.includedGenieWireless = includedGenieWireless;
	}

	/**
	 * @return the waiveHDDVRNRCCount
	 */
	public Integer getWaiveHDDVRNRCCount() {
		return waiveHDDVRNRCCount;
	}

	/**
	 * @param waiveHDDVRNRCCount the waiveHDDVRNRCCount to set
	 */
	public void setWaiveHDDVRNRCCount(Integer waiveHDDVRNRCCount) {
		this.waiveHDDVRNRCCount = waiveHDDVRNRCCount;
	}

	/**
	 * @return the includedGenieMiniWireless
	 */
	public Integer getIncludedGenieMiniWireless() {
		return includedGenieMiniWireless;
	}

	/**
	 * @param includedGenieMiniWireless the includedGenieMiniWireless to set
	 */
	public void setIncludedGenieMiniWireless(Integer includedGenieMiniWireless) {
		this.includedGenieMiniWireless = includedGenieMiniWireless;
	}

	/**
	 * @return the includedGenieMiniWired
	 */
	public Integer getIncludedGenieMiniWired() {
		return includedGenieMiniWired;
	}

	/**
	 * @param includedGenieMiniWired the includedGenieMiniWired to set
	 */
	public void setIncludedGenieMiniWired(Integer includedGenieMiniWired) {
		this.includedGenieMiniWired = includedGenieMiniWired;
	}

	/**
	 * @return the discountedGenieMiniWired
	 */
	public Integer getDiscountedGenieMiniWired() {
		return discountedGenieMiniWired;
	}

	/**
	 * @param discountedGenieMiniWired the discountedGenieMiniWired to set
	 */
	public void setDiscountedGenieMiniWired(Integer discountedGenieMiniWired) {
		this.discountedGenieMiniWired = discountedGenieMiniWired;
	}

	/**
	 * @return the maxAddlTVsSelectedWired
	 */
	public Integer getMaxAddlTVsSelectedWired() {
		return maxAddlTVsSelectedWired;
	}

	/**
	 * @param maxAddlTVsSelectedWired the maxAddlTVsSelectedWired to set
	 */
	public void setMaxAddlTVsSelectedWired(Integer maxAddlTVsSelectedWired) {
		this.maxAddlTVsSelectedWired = maxAddlTVsSelectedWired;
	}

	/**
	 * @return the discountedGenieMiniWireless
	 */
	public Integer getDiscountedGenieMiniWireless() {
		return discountedGenieMiniWireless;
	}

	/**
	 * @param discountedGenieMiniWireless the discountedGenieMiniWireless to set
	 */
	public void setDiscountedGenieMiniWireless(Integer discountedGenieMiniWireless) {
		this.discountedGenieMiniWireless = discountedGenieMiniWireless;
	}

	/**
	 * @return the maxAddlTVsSelectedWireless
	 */
	public Integer getMaxAddlTVsSelectedWireless() {
		return maxAddlTVsSelectedWireless;
	}

	/**
	 * @param maxAddlTVsSelectedWireless the maxAddlTVsSelectedWireless to set
	 */
	public void setMaxAddlTVsSelectedWireless(Integer maxAddlTVsSelectedWireless) {
		this.maxAddlTVsSelectedWireless = maxAddlTVsSelectedWireless;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	
	

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getCustomerTypes() {
		return customerTypes;
	}

	public void setCustomerTypes(String customerTypes) {
		this.customerTypes = customerTypes;
	}

	public String getProductStatus() {
		return productStatus;
	}

	public void setProductStatus(String productStatus) {
		this.productStatus = productStatus;
	}

	public String getPolicyText() {
		return policyText;
	}

	public void setPolicyText(String policyText) {
		this.policyText = policyText;
	}



	/** The predicate. */
	private String predicate;
	
	private String predicateTag;

	/** The productStatus. */
	private String productState;
	
	private List<String> beneficiarySku;
	
	private List<String> qualifierSku;
	
	private List<String> deviceSubCategory;
	
	private List<String> deviceManufacturer;
	
	private List<String> deviceModel;
	
	private List<String> deviceCategory;
	
	

	/** The productIds. */
	private List<GenericTypeIdBase> products;

	public String getProductState() {
		return productState;
	}

	public void setProductState(String productState) {
		this.productState = productState;
	}

	/**
	 * @return the isAnchor
	 */
	public Boolean isAnchor() {
		return isAnchor;
	}

	/**
	 * @param isAnchor the isAnchor to set
	 */
	public void setAnchor(Boolean isAnchor) {
		this.isAnchor = isAnchor;
	}

	/**
	 * @return the isDependent
	 */
	public Boolean isDependent() {
		return isDependent;
	}

	/**
	 * @param isDependent the isDependent to set
	 */
	public void setDependent(Boolean isDependent) {
		this.isDependent = isDependent;
	}

	/**
	 * @return the productFamily
	 */
	public String getProductFamily() {
		return productFamily;
	}

	/**
	 * @param productFamily the productFamily to set
	 */
	public void setProductFamily(String productFamily) {
		this.productFamily = productFamily;
	}

	/**
	 * @return the productIds
	 */
	public List<GenericTypeIdBase> getProductIds() {
		return productIds;
	}

	/**
	 * @param productIds the productIds to set
	 */
	public void setProductIds(List<GenericTypeIdBase> productIds) {
		this.productIds = productIds;
	}

	/**
	 * @return the productCategories
	 */
	public List<GenericTypeIdBase> getProductCategories() {
		return productCategories;
	}

	/**
	 * @param productCategories the productCategories to set
	 */
	public void setProductCategories(List<GenericTypeIdBase> productCategories) {
		this.productCategories = productCategories;
	}

	/**
	 * @return the productType
	 */
	public List<GenericTypeIdBase> getProductType() {
		return productType;
	}

	/**
	 * @param productType the productType to set
	 */
	public void setProductType(List<GenericTypeIdBase> productType) {
		this.productType = productType;
	}

	/**
	 * @return the predicate
	 */
	public String getPredicate() {
		return predicate;
	}

	/**
	 * @param predicate the predicate to set
	 */
	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	public List<GenericTypeIdBase> getProducts() {
		return products;
	}

	public void setProducts(List<GenericTypeIdBase> products) {
		this.products = products;
	}

	public String getPredicateTag() {
		return predicateTag;
	}

	public void setPredicateTag(String predicateTag) {
		this.predicateTag = predicateTag;
	}

	public List<String> getProductTypes() {
		return productTypes;
	}

	public void setProductTypes(List<String> productTypes) {
		this.productTypes = productTypes;
	}

	public List<String> getBeneficiarySku() {
		return beneficiarySku;
	}

	public void setBeneficiarySku(List<String> beneficiarySku) {
		this.beneficiarySku = beneficiarySku;
	}

	public List<String> getQualifierSku() {
		return qualifierSku;
	}

	public void setQualifierSku(List<String> qualifierSku) {
		this.qualifierSku = qualifierSku;
	}

	public List<String> getDeviceSubCategory() {
		return deviceSubCategory;
	}

	public void setDeviceSubCategory(List<String> deviceSubCategory) {
		this.deviceSubCategory = deviceSubCategory;
	}

	public List<String> getDeviceManufacturer() {
		return deviceManufacturer;
	}

	public void setDeviceManufacturer(List<String> deviceManufacturer) {
		this.deviceManufacturer = deviceManufacturer;
	}

	public List<String> getDeviceModel() {
		return deviceModel;
	}

	public void setDeviceModel(List<String> deviceModel) {
		this.deviceModel = deviceModel;
	}

	public List<String> getDeviceCategory() {
		return deviceCategory;
	}

	public void setDeviceCategory(List<String> deviceCategory) {
		this.deviceCategory = deviceCategory;
	}

	private String preEffectiveDate;

	public String getPreEffectiveDate() {
		return preEffectiveDate;
	}

	public void setPreEffectiveDate(String preEffectiveDate) {
		this.preEffectiveDate = preEffectiveDate;
	}

	private String postEffectiveDate;

	public String getPostEffectiveDate() {
		return postEffectiveDate;
	}

	public void setPostEffectiveDate(String postEffectiveDate) {
		this.postEffectiveDate = postEffectiveDate;
	}

	@Override
	public String toString() {
		return "Constraints [isAnchor=" + isAnchor + ", isDependent=" + isDependent + ", productFamily=" + productFamily
				+ ", productIds=" + productIds + ", productCategories=" + productCategories + ", productType="
				+ productType + ", productTypes=" + productTypes + ", startDate=" + startDate + ", endDate=" + endDate
				+ ", maxTVFeesWaivedForWired=" + maxTVFeesWaivedForWired + ", maxTVFeesWaivedForWireless="
				+ maxTVFeesWaivedForWireless + ", maxTVFeesWaivedForGenieMiniWired=" + maxTVFeesWaivedForGenieMiniWired
				+ ", maxTVFeesWaivedForGenieMiniWireless=" + maxTVFeesWaivedForGenieMiniWireless
				+ ", includedGenieWired=" + includedGenieWired + ", includedGenieWireless=" + includedGenieWireless
				+ ", waiveHDDVRNRCCount=" + waiveHDDVRNRCCount + ", includedGenieMiniWireless="
				+ includedGenieMiniWireless + ", includedGenieMiniWired=" + includedGenieMiniWired
				+ ", discountedGenieMiniWired=" + discountedGenieMiniWired + ", maxAddlTVsSelectedWired="
				+ maxAddlTVsSelectedWired + ", discountedGenieMiniWireless=" + discountedGenieMiniWireless
				+ ", maxAddlTVsSelectedWireless=" + maxAddlTVsSelectedWireless + ", customerTypes=" + customerTypes
				+ ", productStatus=" + productStatus + ", policyText=" + policyText + ", modelNos=" + modelNos + ", allowedAction=" + allowedAction
				+ ", predicate=" + predicate + ", productState=" + productState + ", beneficiarySku=" + beneficiarySku
				+ ", qualifierSku=" + qualifierSku + ", deviceSubCategory=" + deviceSubCategory
				+ ", deviceManufacturer=" + deviceManufacturer + ", deviceModel=" + deviceModel + ", deviceCategory="
				+ deviceCategory + ", products=" + products + ", preEffectiveDate=" + preEffectiveDate+ ", postEffectiveDate=" + postEffectiveDate +"]";
		
		/*return "Constraints{" +
		"isAnchor=" + isAnchor +
		", isDependent=" + isDependent +
		", productFamily='" + productFamily + '\'' +
		", productIds=" + productIds +
		", productCategories=" + productCategories +
		", productType=" + productType +
		", predicate='" + predicate + '\'' +
		", productState='" + productState + '\'' +
		", products=" + products +
		'}';*/
	}

	
}
