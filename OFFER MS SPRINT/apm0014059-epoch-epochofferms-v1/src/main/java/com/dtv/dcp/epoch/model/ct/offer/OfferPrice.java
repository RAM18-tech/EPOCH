package com.dtv.dcp.epoch.model.ct.offer;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OfferPrice implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The dollarAmount. */
	private double  dollarAmount;
	

	private Double  priceOnCGSelection;


	private Double priceOnCGDeselection;



	public Double getPriceOnCGSelection() {
		return priceOnCGSelection;
	}

	public void setPriceOnCGSelection(Double priceOnCGSelection) {
		this.priceOnCGSelection = priceOnCGSelection;
	}

	public Double getPriceOnCGDeselection() {
		return priceOnCGDeselection;
	}

	public void setPriceOnCGDeselection(Double priceOnCGDeselection) {
		this.priceOnCGDeselection = priceOnCGDeselection;
	}

	/**
	 * @return the dollarAmount
	 */
	public double getDollarAmount() {
		return dollarAmount;
	}

	/**
	 * @param dollarAmount the contractIndicator to set
	 */
	public void setDollarAmount(double dollarAmount) {
		this.dollarAmount = dollarAmount;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OfferPrice [");
		builder.append(", dollarAmount=");
		builder.append(dollarAmount);
		builder.append("]");
		return builder.toString();
	}
	
	
}
