package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.ct.product.Code;
import com.dtv.dcp.epoch.model.ct.request.Pagination;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductRequest implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private Pagination pagination;
	
	private List<Code> codes;

	private List<String> productCodes;

	/** The offerActionType. */
	private List<String> offerActionType;

	/** The salesChannel. */
	private List<String> salesChannel;

	/** The heartValue. */
	private String heartValue;

	/** The accountTypes. */
	private List<String> accountTypes;

	/** The addOnType. */
	private List<String> addOnType;

	/** The customerSegments. */
	private List<String> customerSegments;

	/** The customerSegments. */
	private List<String> businessSegment;
	
	/** The offerTypes. */
	private List<String> offerTypes;

	/** The offerProductTypes. */
	private List<String> productTypes;

	/** The offerProductFamily. */
	private List<String> productFamily;

	/** The offerStatus. */
	private List<String> offerStatus;

	/** The bundleProductCodes. */
	private List<String> bundleProductCodes;

	/** The bundleProductIds. */
	private List<String> bundleProductIds;

	private List<String> productIds;

	/** The contractApplicable. */
	private List<String> contractApplicable;

	/** The benefitCodes. */
	private List<String> benefitCodes;

	/** The customerEligibility. */
	private CustomerEligibility customerEligibility;

	/** The channelEligibility. */
	private ChannelEligibility channelEligibility;

	/** The cartContext. */
	private CartContext cartContext;

	private List<String> contractIndicator;

	private String userType;

	private String productStatus;

	private CustomerContext customerContext;

	private List<DeviceInfo> devices;

	private String creditRisk;
	
	private String treatmentCode;

	private List<String> planSubType;
	
	private String serviceOrderDate;

	private String policy;
	
	private AdditionalAccountDetails additionalAccountDetails;
	
	public String getTreatmentCode() {
		return treatmentCode;
	}

	public void setTreatmentCode(String treatmentCode) {
		this.treatmentCode = treatmentCode;
	}

	public AdditionalAccountDetails getAdditionalAccountDetails() {
		return additionalAccountDetails;
	}

	public void setAdditionalAccountDetails(AdditionalAccountDetails additionalAccountDetails) {
		this.additionalAccountDetails = additionalAccountDetails;
	}

	public String getServiceOrderDate() {
		return serviceOrderDate;
	}

	public void setServiceOrderDate(String serviceOrderDate) {
		this.serviceOrderDate = serviceOrderDate;
	}

	public String getPolicy() {
		return policy;
	}

	public void setPolicy(String policy) {
		this.policy = policy;
	}


	public List<String> getProductCodes() {
		return productCodes;
	}

	public List<String> getPlanSubType() {
		return planSubType;
	}

	public void setPlanSubType(List<String> planSubType) {
		this.planSubType = planSubType;
	}

	public void setProductCodes(List<String> productCodes) {
		this.productCodes = productCodes;
	}

	public Pagination getPagination() {
		return pagination;
	}

	public void setPagination(Pagination pagination) {
		this.pagination = pagination;
	}

	public List<String> getBusinessSegment() {
		return businessSegment;
	}

	public void setBusinessSegment(List<String> businessSegment) {
		this.businessSegment = businessSegment;
	}

	public List<Code> getCodes() {
		return codes;
	}

	public void setCodes(List<Code> codes) {
		this.codes = codes;
	}

	public String getProductStatus() {
		return productStatus;
	}

	public void setProductStatus(String productStatus) {
		this.productStatus = productStatus;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public List<String> getContractIndicator() {
		return contractIndicator;
	}

	public void setContractIndicator(List<String> contractIndicator) {
		this.contractIndicator = contractIndicator;
	}

	public List<String> getProductIds() {
		return productIds;
	}

	public void setProductIds(List<String> productIds) {
		this.productIds = productIds;
	}

	/**
	 * @return the offerActionType
	 */
	public List<String> getOfferActionType() {
		return offerActionType;
	}

	/**
	 * @param offerActionType the offerActionType to set
	 */
	public void setOfferActionType(List<String> offerActionType) {
		this.offerActionType = offerActionType;
	}

	/**
	 * @return the salesChannel
	 */
	public List<String> getSalesChannel() {
		return salesChannel;
	}

	/**
	 * @param salesChannel the salesChannel to set
	 */
	public void setSalesChannel(List<String> salesChannel) {
		this.salesChannel = salesChannel;
	}

	/**
	 * @return the heartValue
	 */
	public String getHeartValue() {
		return heartValue;
	}

	/**
	 * @param heartValue the heartValue to set
	 */
	public void setHeartValue(String heartValue) {
		this.heartValue = heartValue;
	}

	/**
	 * @return the accountTypes
	 */
	public List<String> getAccountTypes() {
		return accountTypes;
	}

	/**
	 * @param accountTypes the accountTypes to set
	 */
	public void setAccountTypes(List<String> accountTypes) {
		this.accountTypes = accountTypes;
	}

	/**
	 * @return the addOnType
	 */
	public List<String> getAddOnType() {
		return addOnType;
	}

	/**
	 * @param addOnType the addOnType to set
	 */
	public void setAddOnType(List<String> addOnType) {
		this.addOnType = addOnType;
	}

	/**
	 * @return the customerSegments
	 */
	public List<String> getCustomerSegments() {
		return customerSegments;
	}

	/**
	 * @param customerSegments the customerSegments to set
	 */
	public void setCustomerSegments(List<String> customerSegments) {
		this.customerSegments = customerSegments;
	}

	/**
	 * @return the offerTypes
	 */
	public List<String> getOfferTypes() {
		return offerTypes;
	}

	/**
	 * @param offerTypes the offerTypes to set
	 */
	public void setOfferTypes(List<String> offerTypes) {
		this.offerTypes = offerTypes;
	}

	/**
	 * @return the offerProductTypes
	 */
	public List<String> getProductTypes() {
		return productTypes;
	}

	/**
	 * @param offerProductTypes the offerProductTypes to set
	 */
	public void setProductTypes(List<String> offerProductTypes) {
		this.productTypes = offerProductTypes;
	}

	/**
	 * @return the offerProductFamily
	 */
	public List<String> getProductFamily() {
		return productFamily;
	}

	/**
	 *
	 * @param productFamily
	 */
	public void setProductFamily(List<String> productFamily) {
		this.productFamily = productFamily;
	}

	/**
	 * @return the offerStatus
	 */
	public List<String> getOfferStatus() {
		return offerStatus;
	}

	/**
	 * @param offerStatus the offerStatus to set
	 */
	public void setOfferStatus(List<String> offerStatus) {
		this.offerStatus = offerStatus;
	}

	/**
	 * @return the bundleProductCodes
	 */
	public List<String> getBundleProductCodes() {
		return bundleProductCodes;
	}

	/**
	 * @param bundleProductCodes the bundleProductCodes to set
	 */
	public void setBundleProductCodes(List<String> bundleProductCodes) {
		this.bundleProductCodes = bundleProductCodes;
	}

	/**
	 * @return the bundleProductIds
	 */
	public List<String> getBundleProductIds() {
		return bundleProductIds;
	}

	/**
	 * @param bundleProductIds the bundleProductIds to set
	 */
	public void setBundleProductIds(List<String> bundleProductIds) {
		this.bundleProductIds = bundleProductIds;
	}

	/**
	 * @return the contractApplicable
	 */
	public List<String> getContractApplicable() {
		return contractApplicable;
	}

	/**
	 * @param contractApplicable the contractApplicable to set
	 */
	public void setContractApplicable(List<String> contractApplicable) {
		this.contractApplicable = contractApplicable;
	}

	/**
	 * @return the benefitCodes
	 */
	public List<String> getBenefitCodes() {
		return benefitCodes;
	}

	/**
	 * @param benefitCodes the benefitCodes to set
	 */
	public void setBenefitCodes(List<String> benefitCodes) {
		this.benefitCodes = benefitCodes;
	}

	/**
	 * @return the customerEligibility
	 */
	public CustomerEligibility getCustomerEligibility() {
		return customerEligibility;
	}

	/**
	 * @param customerEligibility the customerEligibility to set
	 */
	public void setCustomerEligibility(CustomerEligibility customerEligibility) {
		this.customerEligibility = customerEligibility;
	}

	/**
	 * @return the channelEligibility
	 */
	public ChannelEligibility getChannelEligibility() {
		return channelEligibility;
	}

	/**
	 * @param channelEligibility the channelEligibility to set
	 */
	public void setChannelEligibility(ChannelEligibility channelEligibility) {
		this.channelEligibility = channelEligibility;
	}

	/**
	 * @return the cartContext
	 */
	public CartContext getCartContext() {
		return cartContext;
	}

	/**
	 * @param cartContext the cartContext to set
	 */
	public void setCartContext(CartContext cartContext) {
		this.cartContext = cartContext;
	}

	public CustomerContext getCustomerContext() {
		return customerContext;
	}

	public void setCustomerContext(CustomerContext customerContext) {
		this.customerContext = customerContext;
	}

	public List<DeviceInfo> getDevices() {
		return devices;
	}

	public void setDevices(List<DeviceInfo> devices) {
		this.devices = devices;
	}

	public String getCreditRisk() {
		return creditRisk;
	}

	public void setCreditRisk(String creditRisk) {
		this.creditRisk = creditRisk;
	}

	@Override
	public String toString() {
		return "ProductRequest{" +
				"pagination=" + pagination +
				", codes=" + codes +
				", productCodes=" + productCodes +
				", offerActionType=" + offerActionType +
				", salesChannel=" + salesChannel +
				", heartValue='" + heartValue + '\'' +
				", accountTypes=" + accountTypes +
				", addOnType=" + addOnType +
				", customerSegments=" + customerSegments +
				", businessSegment=" + businessSegment +
				", offerTypes=" + offerTypes +
				", productTypes=" + productTypes +
				", productFamily=" + productFamily +
				", offerStatus=" + offerStatus +
				", bundleProductCodes=" + bundleProductCodes +
				", bundleProductIds=" + bundleProductIds +
				", productIds=" + productIds +
				", contractApplicable=" + contractApplicable +
				", benefitCodes=" + benefitCodes +
				", customerEligibility=" + customerEligibility +
				", channelEligibility=" + channelEligibility +
				", cartContext=" + cartContext +
				", contractIndicator=" + contractIndicator +
				", userType='" + userType + '\'' +
				", productStatus='" + productStatus + '\'' +
				", customerContext=" + customerContext +
				", devices=" + devices +
				", creditRisk='" + creditRisk + '\'' +
				", serviceOrderDate=" + serviceOrderDate +
				", policy='" + policy + '\'' +
				'}';
	}
}
