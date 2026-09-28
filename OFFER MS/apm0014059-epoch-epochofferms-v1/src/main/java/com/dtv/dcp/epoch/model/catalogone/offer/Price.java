package com.dtv.dcp.epoch.model.catalogone.offer;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author dr000y
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Price implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Amount taxIncludedAmount;

	private Amount baseAmount;

	private Amount taxAmount;

	private BigDecimal taxRate;

	/**
	 * @return the taxIncludedAmount
	 */
	public Amount getTaxIncludedAmount() {
		return taxIncludedAmount;
	}

	/**
	 * @param taxIncludedAmount the taxIncludedAmount to set
	 */
	public void setTaxIncludedAmount(Amount taxIncludedAmount) {
		this.taxIncludedAmount = taxIncludedAmount;
	}

	/**
	 * @return the baseAmount
	 */
	public Amount getBaseAmount() {
		return baseAmount;
	}

	/**
	 * @param baseAmount the baseAmount to set
	 */
	public void setBaseAmount(Amount baseAmount) {
		this.baseAmount = baseAmount;
	}

	/**
	 * @return the taxAmount
	 */
	public Amount getTaxAmount() {
		return taxAmount;
	}

	/**
	 * @param taxAmount the taxAmount to set
	 */
	public void setTaxAmount(Amount taxAmount) {
		this.taxAmount = taxAmount;
	}

	/**
	 * @return the taxRate
	 */
	public BigDecimal getTaxRate() {
		return taxRate;
	}

	/**
	 * @param taxRate the taxRate to set
	 */
	public void setTaxRate(BigDecimal taxRate) {
		this.taxRate = taxRate;
	}

	@Override
	public String toString() {
		return "Price [taxIncludedAmount=" + taxIncludedAmount + ", baseAmount=" + baseAmount + ", taxAmount="
				+ taxAmount + ", taxRate=" + taxRate + "]";
	}

}
