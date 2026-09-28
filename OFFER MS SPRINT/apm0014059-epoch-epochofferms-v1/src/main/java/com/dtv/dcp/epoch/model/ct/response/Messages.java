package com.dtv.dcp.epoch.model.ct.response;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Messages implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	@JsonProperty("messageCode")
	private String messageCode;
	
	@JsonProperty("messageType")
	private String messageType;
	
	@JsonProperty("messageDescription")
	private String messageDescription;
	
	@JsonProperty("offerCode")
	private String offerCode;

	@JsonProperty("billingProductCode")
	private String billingProductCode;
	
	@JsonProperty("status")
	private String status;

	@JsonProperty("statusCode")
	private String statusCode;

	@JsonProperty("statusDescription")
	private String statusDescription;

	@JsonProperty("type")
	private String type;

	@JsonProperty("id")
	private String id;

	@JsonProperty("errorCode")
	private String errorCode;

	@JsonProperty("errorMessage")
	private String errorMessage;

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the statusCode
	 */
	public String getStatusCode() {
		return statusCode;
	}

	/**
	 * @param statusCode
	 *            the statusCode to set
	 */
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	/**
	 * @return the statusDescription
	 */
	public String getStatusDescription() {
		return statusDescription;
	}

	/**
	 * @param statusDescription
	 *            the statusDescription to set
	 */
	public void setStatusDescription(String statusDescription) {
		this.statusDescription = statusDescription;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the errorCode
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * @param errorCode
	 *            the errorCode to set
	 */
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @param errorMessage
	 *            the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * @return the messageCode
	 */
	public String getMessageCode() {
		return messageCode;
	}

	/**
	 * @param messageCode the messageCode to set
	 */
	public void setMessageCode(String messageCode) {
		this.messageCode = messageCode;
	}

	/**
	 * @return the messageType
	 */
	public String getMessageType() {
		return messageType;
	}

	/**
	 * @param messageType the messageType to set
	 */
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	/**
	 * @return the messageDescription
	 */
	public String getMessageDescription() {
		return messageDescription;
	}

	/**
	 * @param messageDescription the messageDescription to set
	 */
	public void setMessageDescription(String messageDescription) {
		this.messageDescription = messageDescription;
	}

	/**
	 * @return the offerCode
	 */
	public String getOfferCode() {
		return offerCode;
	}

	/**
	 * @param offerCode the offerCode to set
	 */
	public void setOfferCode(String offerCode) {
		this.offerCode = offerCode;
	}

	/**
	 * @return the billingProductCode
	 */
	public String getBillingProductCode() {
		return billingProductCode;
	}

	/**
	 * @param billingProductCode the billingProductCode to set
	 */
	public void setBillingProductCode(String billingProductCode) {
		this.billingProductCode = billingProductCode;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Messages [" + (messageCode != null ? "messageCode=" + messageCode + ", " : "")
				+ (messageType != null ? "messageType=" + messageType + ", " : "")
				+ (messageDescription != null ? "messageDescription=" + messageDescription + ", " : "")
				+ (offerCode != null ? "offerCode=" + offerCode + ", " : "")
				+ (billingProductCode != null ? "billingProductCode=" + billingProductCode + ", " : "")
				+ (status != null ? "status=" + status + ", " : "")
				+ (statusCode != null ? "statusCode=" + statusCode + ", " : "")
				+ (statusDescription != null ? "statusDescription=" + statusDescription + ", " : "")
				+ (type != null ? "type=" + type + ", " : "") + (id != null ? "id=" + id + ", " : "")
				+ (errorCode != null ? "errorCode=" + errorCode + ", " : "")
				+ (errorMessage != null ? "errorMessage=" + errorMessage : "") + "]";
	}

}
