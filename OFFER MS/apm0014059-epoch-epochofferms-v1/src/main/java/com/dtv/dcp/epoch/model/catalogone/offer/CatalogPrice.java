package com.dtv.dcp.epoch.model.catalogone.offer;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author dr000y
 *
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CatalogPrice implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String priceType;

	private String recurringChargePeriod;

	private Integer recurringChargePeriodLength;

	private String taxIncluded;

	private String period;

	private Price price;

	private Price finalPrice;

	private Price standalonePrice;

	private List<PriceAdjustment> priceAdjustment;

	/**
	 * @return the priceType
	 */
	public String getPriceType() {
		return priceType;
	}

	/**
	 * @param priceType the priceType to set
	 */
	public void setPriceType(String priceType) {
		this.priceType = priceType;
	}

	/**
	 * @return the recurringChargePeriod
	 */
	public String getRecurringChargePeriod() {
		return recurringChargePeriod;
	}

	/**
	 * @param recurringChargePeriod the recurringChargePeriod to set
	 */
	public void setRecurringChargePeriod(String recurringChargePeriod) {
		this.recurringChargePeriod = recurringChargePeriod;
	}

	/**
	 * @return the recurringChargePeriodLength
	 */
	public Integer getRecurringChargePeriodLength() {
		return recurringChargePeriodLength;
	}

	/**
	 * @param recurringChargePeriodLength the recurringChargePeriodLength to set
	 */
	public void setRecurringChargePeriodLength(Integer recurringChargePeriodLength) {
		this.recurringChargePeriodLength = recurringChargePeriodLength;
	}

	/**
	 * @return the taxIncluded
	 */
	public String getTaxIncluded() {
		return taxIncluded;
	}

	/**
	 * @param taxIncluded the taxIncluded to set
	 */
	public void setTaxIncluded(String taxIncluded) {
		this.taxIncluded = taxIncluded;
	}

	/**
	 * @return the period
	 */
	public String getPeriod() {
		return period;
	}

	/**
	 * @param period the period to set
	 */
	public void setPeriod(String period) {
		this.period = period;
	}

	/**
	 * @return the price
	 */
	public Price getPrice() {
		return price;
	}

	/**
	 * @param price the price to set
	 */
	public void setPrice(Price price) {
		this.price = price;
	}

	/**
	 * @return the finalPrice
	 */
	public Price getFinalPrice() {
		return finalPrice;
	}

	/**
	 * @param finalPrice the finalPrice to set
	 */
	public void setFinalPrice(Price finalPrice) {
		this.finalPrice = finalPrice;
	}

	/**
	 * @return the standalonePrice
	 */
	public Price getStandalonePrice() {
		return standalonePrice;
	}

	/**
	 * @param standalonePrice the standalonePrice to set
	 */
	public void setStandalonePrice(Price standalonePrice) {
		this.standalonePrice = standalonePrice;
	}

	/**
	 * @return the priceAdjustment
	 */
	public List<PriceAdjustment> getPriceAdjustment() {
		return priceAdjustment;
	}

	/**
	 * @param priceAdjustment the priceAdjustment to set
	 */
	public void setPriceAdjustment(List<PriceAdjustment> priceAdjustment) {
		this.priceAdjustment = priceAdjustment;
	}

	@Override
	public String toString() {
		return "CatalogPrice [priceType=" + priceType + ", recurringChargePeriod=" + recurringChargePeriod
				+ ", recurringChargePeriodLength=" + recurringChargePeriodLength + ", taxIncluded=" + taxIncluded
				+ ", period=" + period + ", price=" + price + ", finalPrice=" + finalPrice + ", standalonePrice="
				+ standalonePrice + ", priceAdjustment=" + priceAdjustment + "]";
	}

}
