package com.dtv.dcp.epoch.model.common;

import com.dtv.dcp.epoch.model.common.request.BenefitsCustomerHasReceived;
import com.dtv.dcp.epoch.model.common.request.PriceProtection;
import com.dtv.dcp.epoch.model.common.validatecoupons.Products;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExistingProductFamily implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	private String productFamily;
	private String accountNumber;
	private boolean isActive;
	private String segmentDescription;
	private String iapPartnerAccountType;
	private String nextBillingDate;
	private String parentSubscriptionDate;
	private PriceProtection priceProtection;
	private List<BenefitsCustomerHasReceived> benefitsCustomerHasReceived;
	private List<Products> products;
	private String gracePeriodEndDate;
	private String accountStatus;
	private String currentEmployeeStatus;
	private String accountCreationDate;
	private boolean installmentsAvailed;
	private boolean scheduledDisconnection;
	private boolean isDCBEnabled;
	private String currentDCBTxnStatus;
	private String lastReconnectDate;
	private List<AdditionalDetails> additionalDetails;
	private boolean hasActiveInstallments;
	private List<String> installmentProvider;
	private List<VolPauseDetails> volPauseDetails;



	public String getGracePeriodEndDate() {
		return gracePeriodEndDate;
	}

	public void setGracePeriodEndDate(String gracePeriodEndDate) {
		this.gracePeriodEndDate = gracePeriodEndDate;
	}

	public String getAccountStatus() {
		return accountStatus;
	}

	public void setAccountStatus(String accountStatus) {
		this.accountStatus = accountStatus;
	}

	public String getCurrentEmployeeStatus() {
		return currentEmployeeStatus;
	}

	public void setCurrentEmployeeStatus(String currentEmployeeStatus) {
		this.currentEmployeeStatus = currentEmployeeStatus;
	}

	public String getAccountCreationDate() {
		return accountCreationDate;
	}

	public void setAccountCreationDate(String accountCreationDate) {
		this.accountCreationDate = accountCreationDate;
	}

	public boolean isInstallmentsAvailed() {
		return installmentsAvailed;
	}

	public void setInstallmentsAvailed(boolean installmentsAvailed) {
		this.installmentsAvailed = installmentsAvailed;
	}

	public boolean isScheduledDisconnection() {
		return scheduledDisconnection;
	}

	public void setScheduledDisconnection(boolean scheduledDisconnection) {
		this.scheduledDisconnection = scheduledDisconnection;
	}

	public boolean isDCBEnabled() {
		return isDCBEnabled;
	}

	public void setDCBEnabled(boolean isDCBEnabled) {
		this.isDCBEnabled = isDCBEnabled;
	}

	public String getCurrentDCBTxnStatus() {
		return currentDCBTxnStatus;
	}

	public void setCurrentDCBTxnStatus(String currentDCBTxnStatus) {
		this.currentDCBTxnStatus = currentDCBTxnStatus;
	}

	public String getLastReconnectDate() {
		return lastReconnectDate;
	}

	public void setLastReconnectDate(String lastReconnectDate) {
		this.lastReconnectDate = lastReconnectDate;
	}

	public List<AdditionalDetails> getAdditionalDetails() {
		return additionalDetails;
	}

	public void setAdditionalDetails(List<AdditionalDetails> additionalDetails) {
		this.additionalDetails = additionalDetails;
	}

	public boolean isHasActiveInstallments() {
		return hasActiveInstallments;
	}

	public void setHasActiveInstallments(boolean hasActiveInstallments) {
		this.hasActiveInstallments = hasActiveInstallments;
	}

	public List<String> getInstallmentProvider() {
		return installmentProvider;
	}

	public void setInstallmentProvider(List<String> installmentProvider) {
		this.installmentProvider = installmentProvider;
	}

	public List<VolPauseDetails> getVolPauseDetails() {
		return volPauseDetails;
	}

	public void setVolPauseDetails(List<VolPauseDetails> volPauseDetails) {
		this.volPauseDetails = volPauseDetails;
	}

	public String getProductFamily() {
		return productFamily;
	}

	public void setProductFamily(String productFamily) {
		this.productFamily = productFamily;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean active) {
		isActive = active;
	}

	public String getSegmentDescription() {
		return segmentDescription;
	}

	public void setSegmentDescription(String segmentDescription) {
		this.segmentDescription = segmentDescription;
	}

	public String getIapPartnerAccountType() {
		return iapPartnerAccountType;
	}

	public void setIapPartnerAccountType(String iapPartnerAccountType) {
		this.iapPartnerAccountType = iapPartnerAccountType;
	}

	public String getNextBillingDate() {
		return nextBillingDate;
	}

	public void setNextBillingDate(String nextBillingDate) {
		this.nextBillingDate = nextBillingDate;
	}

	public String getParentSubscriptionDate() {
		return parentSubscriptionDate;
	}

	public void setParentSubscriptionDate(String parentSubscriptionDate) {
		this.parentSubscriptionDate = parentSubscriptionDate;
	}

	public PriceProtection getPriceProtection() {
		return priceProtection;
	}

	public void setPriceProtection(PriceProtection priceProtection) {
		this.priceProtection = priceProtection;
	}

	public List<BenefitsCustomerHasReceived> getBenefitsCustomerHasReceived() {
		return benefitsCustomerHasReceived;
	}

	public void setBenefitsCustomerHasReceived(List<BenefitsCustomerHasReceived> benefitsCustomerHasReceived) {
		this.benefitsCustomerHasReceived = benefitsCustomerHasReceived;
	}

	public List<Products> getProducts() {
		return products;
	}

	public void setProducts(List<Products> products) {
		this.products = products;
	}

	@Override
	public int hashCode() {
		return Objects.hash(accountNumber, benefitsCustomerHasReceived, iapPartnerAccountType, isActive,
				nextBillingDate, parentSubscriptionDate, priceProtection, productFamily, products, segmentDescription);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExistingProductFamily other = (ExistingProductFamily) obj;
		return Objects.equals(accountNumber, other.accountNumber)
				&& Objects.equals(benefitsCustomerHasReceived, other.benefitsCustomerHasReceived)
				&& Objects.equals(iapPartnerAccountType, other.iapPartnerAccountType) && isActive == other.isActive
				&& Objects.equals(nextBillingDate, other.nextBillingDate)
				&& Objects.equals(parentSubscriptionDate, other.parentSubscriptionDate)
				&& Objects.equals(priceProtection, other.priceProtection)
				&& Objects.equals(productFamily, other.productFamily) && Objects.equals(products, other.products)
				&& Objects.equals(segmentDescription, other.segmentDescription);
	}

	@Override
	public String toString() {
		return "ExistingProductFamily [productFamily=" + productFamily + ", accountNumber=" + accountNumber
				+ ", isActive=" + isActive + ", segmentDescription=" + segmentDescription + ", iapPartnerAccountType="
				+ iapPartnerAccountType + ", nextBillingDate=" + nextBillingDate + ", parentSubscriptionDate="
				+ parentSubscriptionDate + ", priceProtection=" + priceProtection + ", benefitsCustomerHasReceived="
				+ benefitsCustomerHasReceived + ", products=" + products + "]";
	}

	private boolean isProjectedBillDate;

	private boolean pendingSwimlaneSwitch;
	private String subscriptionStatus;

	private String tenureType;

	public boolean getProjectedBillDate() {
		return isProjectedBillDate;
	}

	public void setProjectedBillDate(boolean projectedBillDate) {
		isProjectedBillDate = projectedBillDate;
	}

	public boolean getPendingSwimlaneSwitch() {
		return pendingSwimlaneSwitch;
	}

	public void setPendingSwimlaneSwitch(boolean pendingSwimlaneSwitch) {
		this.pendingSwimlaneSwitch = pendingSwimlaneSwitch;
	}

	public String getSubscriptionStatus() {
		return subscriptionStatus;
	}

	public void setSubscriptionStatus(String subscriptionStatus) {
		this.subscriptionStatus = subscriptionStatus;
	}

	public String getTenureType() {
		return tenureType;
	}

	public void setTenureType(String tenureType) {
		this.tenureType = tenureType;
	}
}