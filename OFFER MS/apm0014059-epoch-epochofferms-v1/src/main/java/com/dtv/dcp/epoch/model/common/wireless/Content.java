package com.dtv.dcp.epoch.model.common.wireless;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class Content
 *
 */
public class Content {

	@JsonProperty("accountNumber")
	private String accountNumber;

	@JsonProperty("status")
	private String status;

	@JsonProperty("type")
	private String type;

	@JsonProperty("billingFirstName")
	private String billingFirstName;

	@JsonProperty("billingMiddleInitial")
	private String billingMiddleInitial;

	@JsonProperty("billingLastName")
	private String billingLastName;

	@JsonProperty("subType")
	private String subType;

	@JsonProperty("emailAddress")
	private String emailAddress;

	@JsonProperty("bmgIndicator")
	private boolean bmgIndicator;

	@JsonProperty("addresses")
	private List<Address> addresses = null;

	@JsonProperty("subscribers")
	private List<Subscriber> subscribers = null;

	@JsonProperty("billingMarketInfo")
	private BillingMarket billingMarket;

	@JsonProperty("customerType")
	private String customerType;

	@JsonProperty("fanDetails")
	private FanDetails fanDetails;

	@JsonProperty("billLanguage")
	private String billLanguage;

	@JsonProperty("billingSystemIndicator")
	private String billingSystemIndicator;

	@JsonProperty("titanBan")
	private String titanBan;

	@JsonProperty("primaryAccountTelephoneNumber")
	private String primaryAccountTelephoneNumber;

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBillingFirstName() {
		return billingFirstName;
	}

	public void setBillingFirstName(String billingFirstName) {
		this.billingFirstName = billingFirstName;
	}

	public String getBillingMiddleInitial() {
		return billingMiddleInitial;
	}

	public void setBillingMiddleInitial(String billingMiddleInitial) {
		this.billingMiddleInitial = billingMiddleInitial;
	}

	public String getBillingLastName() {
		return billingLastName;
	}

	public void setBillingLastName(String billingLastName) {
		this.billingLastName = billingLastName;
	}

	public String getSubType() {
		return subType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public boolean isBmgIndicator() {
		return bmgIndicator;
	}

	public void setBmgIndicator(boolean bmgIndicator) {
		this.bmgIndicator = bmgIndicator;
	}

	public List<Address> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<Address> addresses) {
		this.addresses = addresses;
	}

	public List<Subscriber> getSubscribers() {
		return subscribers;
	}

	public void setSubscribers(List<Subscriber> subscribers) {
		this.subscribers = subscribers;
	}

	public BillingMarket getBillingMarket() {
		return billingMarket;
	}

	public void setBillingMarket(BillingMarket billingMarket) {
		this.billingMarket = billingMarket;
	}

	public String getCustomerType() {
		return customerType;
	}

	public void setCustomerType(String customerType) {
		this.customerType = customerType;
	}

	public FanDetails getFanDetails() {
		return fanDetails;
	}

	public void setFanDetails(FanDetails fanDetails) {
		this.fanDetails = fanDetails;
	}

	public String getBillLanguage() {
		return billLanguage;
	}

	public void setBillLanguage(String billLanguage) {
		this.billLanguage = billLanguage;
	}

	public String getBillingSystemIndicator() {
		return billingSystemIndicator;
	}

	public void setBillingSystemIndicator(String billingSystemIndicator) {
		this.billingSystemIndicator = billingSystemIndicator;
	}

	public String getTitanBan() {
		return titanBan;
	}

	public void setTitanBan(String titanBan) {
		this.titanBan = titanBan;
	}

	public String getPrimaryAccountTelephoneNumber() {
		return primaryAccountTelephoneNumber;
	}

	public void setPrimaryAccountTelephoneNumber(String primaryAccountTelephoneNumber) {
		this.primaryAccountTelephoneNumber = primaryAccountTelephoneNumber;
	}

}
