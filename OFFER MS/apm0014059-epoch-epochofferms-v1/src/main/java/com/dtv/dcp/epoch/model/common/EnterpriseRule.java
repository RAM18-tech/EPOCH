package com.dtv.dcp.epoch.model.common;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration 
@PropertySource("classpath:EnterpriseRules.properties")
@ConfigurationProperties(prefix = "rule")
public class EnterpriseRule {

	private String dtvnow;
	private String zipCode;
	private String ctZipCodes;
	/**
	 * @return the ctZipCodes
	 */
	public String getCtZipCodes() {
		return ctZipCodes;
	}
	/**
	 * @param ctZipCodes the ctZipCodes to set
	 */
	public void setCtZipCodes(String ctZipCodes) {
		this.ctZipCodes = ctZipCodes;
	}
	public String getDtvnow() {
		return dtvnow;
	}
	public void setDtvnow(String dtvnow) {
		this.dtvnow = dtvnow;
	}
	public String getZipCode() {
		return zipCode;
	}
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	
}
