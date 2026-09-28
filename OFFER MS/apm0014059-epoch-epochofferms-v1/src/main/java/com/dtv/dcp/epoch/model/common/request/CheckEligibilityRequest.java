package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.common.CustomerContext;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckEligibilityRequest implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	@JsonProperty("businessSegment")
	private String businessSegment;

	@JsonProperty("customerSegments")
	private String customerSegments;

	private List<String> contractIndicator;

	/**
	 * The offerActionType.
	 */
	private List<String> offerActionType;

	private String offerProductFamily;

	private String salesChannel;

	private boolean decisioningFlow;

	private boolean reconnectCustomer;
	
	private boolean swimlaneSwitchEligible;

	private String serviceEndDate;
	
	private String existingAccountSubscriberType;
	
	private List<String>  migrationServiceType;
	
	private boolean migrationIndicator;
	
	private String creditRisk;
	
	private CustomerAddress customerAddress;
	
	private CustomerContext customerContext;

	/**
	 * @return the businessSegment
	 */
	public String getBusinessSegment() {
		return businessSegment;
	}

	/**
	 * @param businessSegment the businessSegment to set
	 */
	public void setBusinessSegment(String businessSegment) {
		this.businessSegment = businessSegment;
	}

	/**
	 * @return the customerSegments
	 */
	public String getCustomerSegments() {
		return customerSegments;
	}

	/**
	 * @param customerSegments the customerSegments to set
	 */
	public void setCustomerSegments(String customerSegments) {
		this.customerSegments = customerSegments;
	}

	/**
	 * @return the contractIndicator
	 */
	public List<String> getContractIndicator() {
		return contractIndicator;
	}

	/**
	 * @param contractIndicator the contractIndicator to set
	 */
	public void setContractIndicator(List<String> contractIndicator) {
		this.contractIndicator = contractIndicator;
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
	 * @return the offerProductFamily
	 */
	public String getOfferProductFamily() {
		return offerProductFamily;
	}

	/**
	 * @param offerProductFamily the offerProductFamily to set
	 */
	public void setOfferProductFamily(String offerProductFamily) {
		this.offerProductFamily = offerProductFamily;
	}

	/**
	 * @return the salesChannel
	 */
	public String getSalesChannel() {
		return salesChannel;
	}

	/**
	 * @param salesChannel the salesChannel to set
	 */
	public void setSalesChannel(String salesChannel) {
		this.salesChannel = salesChannel;
	}

	/**
	 * @return the decisioningFlow
	 */
	public boolean isDecisioningFlow() {
		return decisioningFlow;
	}

	/**
	 * @param decisioningFlow the decisioningFlow to set
	 */
	public void setDecisioningFlow(boolean decisioningFlow) {
		this.decisioningFlow = decisioningFlow;
	}

	/**
	 * @return the reconnectCustomer
	 */
	public boolean isReconnectCustomer() {
		return reconnectCustomer;
	}

	/**
	 * @param reconnectCustomer the reconnectCustomer to set
	 */
	public void setReconnectCustomer(boolean reconnectCustomer) {
		this.reconnectCustomer = reconnectCustomer;
	}

	/**
	 * @return the swimlaneSwitchEligible
	 */
	public boolean isSwimlaneSwitchEligible() {
		return swimlaneSwitchEligible;
	}

	/**
	 * @param swimlaneSwitchEligible the swimlaneSwitchEligible to set
	 */
	public void setSwimlaneSwitchEligible(boolean swimlaneSwitchEligible) {
		this.swimlaneSwitchEligible = swimlaneSwitchEligible;
	}

	/**
	 * @return the serviceEndDate
	 */
	public String getServiceEndDate() {
		return serviceEndDate;
	}

	/**
	 * @param serviceEndDate the serviceEndDate to set
	 */
	public void setServiceEndDate(String serviceEndDate) {
		this.serviceEndDate = serviceEndDate;
	}

	/**
	 * @return the existingAccountSubscriberType
	 */
	public String getExistingAccountSubscriberType() {
		return existingAccountSubscriberType;
	}

	/**
	 * @param existingAccountSubscriberType the existingAccountSubscriberType to set
	 */
	public void setExistingAccountSubscriberType(String existingAccountSubscriberType) {
		this.existingAccountSubscriberType = existingAccountSubscriberType;
	}

	/**
	 * @return the migrationServiceType
	 */
	public List<String> getMigrationServiceType() {
		return migrationServiceType;
	}

	/**
	 * @param migrationServiceType the migrationServiceType to set
	 */
	public void setMigrationServiceType(List<String> migrationServiceType) {
		this.migrationServiceType = migrationServiceType;
	}

	/**
	 * @return the migrationIndicator
	 */
	public boolean isMigrationIndicator() {
		return migrationIndicator;
	}

	/**
	 * @param migrationIndicator the migrationIndicator to set
	 */
	public void setMigrationIndicator(boolean migrationIndicator) {
		this.migrationIndicator = migrationIndicator;
	}

	/**
	 * @return the creditRisk
	 */
	public String getCreditRisk() {
		return creditRisk;
	}

	/**
	 * @param creditRisk the creditRisk to set
	 */
	public void setCreditRisk(String creditRisk) {
		this.creditRisk = creditRisk;
	}

	/**
	 * @return the customerAddress
	 */
	public CustomerAddress getCustomerAddress() {
		return customerAddress;
	}

	/**
	 * @param customerAddress the customerAddress to set
	 */
	public void setCustomerAddress(CustomerAddress customerAddress) {
		this.customerAddress = customerAddress;
	}

	/**
	 * @return the customerContext
	 */
	public CustomerContext getCustomerContext() {
		return customerContext;
	}

	/**
	 * @param customerContext the customerContext to set
	 */
	public void setCustomerContext(CustomerContext customerContext) {
		this.customerContext = customerContext;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CheckEligibilityRequest [businessSegment=" + businessSegment + ", customerSegments=" + customerSegments
				+ ", contractIndicator=" + contractIndicator + ", offerActionType=" + offerActionType
				+ ", offerProductFamily=" + offerProductFamily + ", salesChannel=" + salesChannel + ", decisioningFlow="
				+ decisioningFlow + ", reconnectCustomer=" + reconnectCustomer + ", swimlaneSwitchEligible="
				+ swimlaneSwitchEligible + ", serviceEndDate=" + serviceEndDate + ", existingAccountSubscriberType="
				+ existingAccountSubscriberType + ", migrationServiceType=" + migrationServiceType
				+ ", migrationIndicator=" + migrationIndicator + ", creditRisk=" + creditRisk + ", customerAddress="
				+ customerAddress + ", customerContext=" + customerContext + "]";
	}
	
	
}
