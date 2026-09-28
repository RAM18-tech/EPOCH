package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Value implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The centAmount. */
	private Double  centAmount;
	
	/** The currencyCode. */
	private String currencyCode;
	/** The currencyCode. */
	private Integer percentage;
	
	/** The currencyCode. */
	private int fractionDigits;
	
	/** The dollarAmount. */
	private Double dollarAmount;
	
	


	/**
	 * @return the fractionDigits
	 */
	public int getFractionDigits() {
		return fractionDigits;
	}

	/**
	 * @param fractionDigits the fractionDigits to set
	 */
	public void setFractionDigits(int fractionDigits) {
		this.fractionDigits = fractionDigits;
	}

	public Integer getPercentage() {
		return percentage;
	}

	public void setPercentage(Integer percentage) {
		this.percentage = percentage;
	}

	public Double getCentAmount() {
		return centAmount;
	}

	public void setCentAmount(Double centAmount) {
		this.centAmount = centAmount;
	}

	public Double getDollarAmount() {
		return dollarAmount;
	}

	public void setDollarAmount(Double dollarAmount) {
		this.dollarAmount = dollarAmount;
	}

	/**
	 * @return the currencyCode
	 */
	public String getCurrencyCode() {
		return currencyCode;
	}

	/**
	 * @param currencyCode the currencyCode to set
	 */
	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Value [centAmount=");
		builder.append(centAmount);
		builder.append(", currencyCode=");
		builder.append(currencyCode);
		builder.append("]");
		return builder.toString();
	}
	
	

}
