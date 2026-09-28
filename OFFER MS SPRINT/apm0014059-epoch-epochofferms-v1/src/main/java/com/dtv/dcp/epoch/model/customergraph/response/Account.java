package com.dtv.dcp.epoch.model.customergraph.response;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Account implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String accountStatus;
	private String startServiceDate;
	private String accountType;
	private String iptvSunsetDate;
	private String ban;
	private List<Address> addresses;

	public String getAccountStatus() {
		return accountStatus;
	}

	public void setAccountStatus(String accountStatus) {
		this.accountStatus = accountStatus;
	}

	public String getStartServiceDate() {
		return startServiceDate;
	}

	public void setStartServiceDate(String startServiceDate) {
		this.startServiceDate = startServiceDate;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public String getIptvSunsetDate() {
		return iptvSunsetDate;
	}

	public void setIptvSunsetDate(String iptvSunsetDate) {
		this.iptvSunsetDate = iptvSunsetDate;
	}

	public String getBan() {
		return ban;
	}

	public void setBan(String ban) {
		this.ban = ban;
	}

	public List<Address> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<Address> addresses) {
		this.addresses = addresses;
	}

}
