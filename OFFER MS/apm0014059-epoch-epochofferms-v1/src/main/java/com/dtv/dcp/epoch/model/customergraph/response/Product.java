package com.dtv.dcp.epoch.model.customergraph.response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author dr000y
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	@JsonProperty("productBillingCode")
	private String productBillingCode;

	@JsonProperty("productBillingId")
	private String productBillingId;

	@JsonProperty("productName")
	private String productName;

	@JsonProperty("productType")
	private String productType;

	@JsonProperty("productFamily")
	private String productFamily;

	@JsonProperty("promotions")
	private List<Promotion> promotions;

	@JsonProperty("linesOfBusiness")
	private String linesOfBusiness;

	@JsonProperty("status")
	private String status;

	@JsonProperty("productDescription")
	private String productDescription;

	@JsonProperty("startDate")
	private String startDate;

	@JsonProperty("endDate")
	private String endDate;

	/**
	 * @return the productBillingCode
	 */
	public String getProductBillingCode() {
		return productBillingCode;
	}

	/**
	 * @param productBillingCode
	 *            the productBillingCode to set
	 */
	public void setProductBillingCode(String productBillingCode) {
		this.productBillingCode = productBillingCode;
	}

	/**
	 * @return the productBillingId
	 */
	public String getProductBillingId() {
		return productBillingId;
	}

	/**
	 * @param productBillingId
	 *            the productBillingId to set
	 */
	public void setProductBillingId(String productBillingId) {
		this.productBillingId = productBillingId;
	}

	/**
	 * @return the productName
	 */
	public String getProductName() {
		return productName;
	}

	/**
	 * @param productName
	 *            the productName to set
	 */
	public void setProductName(String productName) {
		this.productName = productName;
	}

	/**
	 * @return the productType
	 */
	public String getProductType() {
		return productType;
	}

	/**
	 * @param productType
	 *            the productType to set
	 */
	public void setProductType(String productType) {
		this.productType = productType;
	}

	/**
	 * @return the productFamily
	 */
	public String getProductFamily() {
		return productFamily;
	}

	/**
	 * @param productFamily
	 *            the productFamily to set
	 */
	public void setProductFamily(String productFamily) {
		this.productFamily = productFamily;
	}

	public List<Promotion> getPromotions() {
		promotions = Optional.ofNullable(promotions).orElse(new ArrayList<>());
		return promotions;
	}

	public void setPromotions(List<Promotion> promotions) {
		this.promotions = promotions;
	}

	/**
	 * @return the linesOfBusiness
	 */
	public String getLinesOfBusiness() {
		return linesOfBusiness;
	}

	/**
	 * @param linesOfBusiness
	 *            the linesOfBusiness to set
	 */
	public void setLinesOfBusiness(String linesOfBusiness) {
		this.linesOfBusiness = linesOfBusiness;
	}

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
	 * @return the productDescription
	 */
	public String getProductDescription() {
		return productDescription;
	}

	/**
	 * @param productDescription
	 *            the productDescription to set
	 */
	public void setProductDescription(String productDescription) {
		this.productDescription = productDescription;
	}

	/**
	 * @return the startDate
	 */
	public String getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate
	 *            the startDate to set
	 */
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the endDate
	 */
	public String getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate
	 *            the endDate to set
	 */
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	
	public enum ProductType {

		BASE_PACKAGE("Base Package"), ADDON("ADDON"), PICK_SERVICES("Pick Services");

		private String type;

		private ProductType(String type) {
			this.type = type;
		}

		@Override
		public String toString() {
			return type;
		}
	}
}
