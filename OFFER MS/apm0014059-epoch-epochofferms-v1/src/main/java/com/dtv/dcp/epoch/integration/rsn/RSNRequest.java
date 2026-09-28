package com.dtv.dcp.epoch.integration.rsn;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;

/**
 * The Class RSNRequest.
 *
 * @author vc402t
 */
@ApiModel(value = "RSNRequest")
@JsonInclude(Include.NON_NULL)
public class RSNRequest implements Serializable {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;	
	/** The zipCode. */
	private String zipCode;
	/** The basePackageSku. */
	private BasePackage basePackage;
	/** The accountType. */
	private String accountType;	
	/** The customer profile. */
	private CustomerProfile customerProfile;
	
	private String subscriberType;
	
	public String getSubscriberType() {
		return subscriberType;
	}
	public void setSubscriberType(String subscriberType) {
		this.subscriberType = subscriberType;
	}
	/**
	 * @return the zipCode
	 */
	public String getZipCode() {
		return zipCode;
	}
	/**
	 * @param zipCode the zipCode to set
	 */
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	/**
	 * @return the basePackage
	 */
	public BasePackage getBasePackage() {
		return basePackage;
	}
	/**
	 * @param basePackage the basePackage to set
	 */
	public void setBasePackage(BasePackage basePackage) {
		this.basePackage = basePackage;
	}
	/**
	 * @return the accountType
	 */
	public String getAccountType() {
		return accountType;
	}
	/**
	 * @param accountType the accountType to set
	 */
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
	public CustomerProfile getCustomerProfile() {
		return customerProfile;
	}
	public void setCustomerProfile(CustomerProfile customerProfile) {
		this.customerProfile = customerProfile;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RSNRequest [zipCode=");
		builder.append(zipCode);
		builder.append(", basePackage=");
		builder.append(basePackage);
		builder.append(", accountType=");
		builder.append(accountType);
		builder.append(", customerProfile=");
		builder.append(customerProfile);
		builder.append("]");
		return builder.toString();
	}
}
