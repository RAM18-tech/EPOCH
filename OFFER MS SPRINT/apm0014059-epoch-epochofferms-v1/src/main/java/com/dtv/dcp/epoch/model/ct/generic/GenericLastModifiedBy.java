package com.dtv.dcp.epoch.model.ct.generic;

import java.io.Serializable;

import com.dtv.dcp.epoch.model.common.UserType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericLastModifiedBy implements Serializable {
	
	private String isPlatformClient;
	
	private UserType user;
	
	private String clientId;


	public String getIsPlatformClient() {
		return isPlatformClient;
	}

	public void setIsPlatformClient(String isPlatformClient) {
		this.isPlatformClient = isPlatformClient;
	}

	public UserType getUser() {
		return user;
	}

	public void setUser(UserType user) {
		this.user = user;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

}