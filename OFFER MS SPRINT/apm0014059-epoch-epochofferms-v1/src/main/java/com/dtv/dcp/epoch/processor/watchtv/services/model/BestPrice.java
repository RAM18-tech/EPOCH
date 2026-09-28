package com.dtv.dcp.epoch.processor.watchtv.services.model;

public class BestPrice {
	Double bestPrices = null;
	/**
	 * @return the bestPrice
	 */
	public Double getBestPrice() {
		return bestPrices;
	}
	/**
	 * @param bestPrice the bestPrice to set
	 */
	public void setBestPrice(Double bestPrice) {
		this.bestPrices = bestPrice;
	}
	/**
	 * @return the basePrice
	 */
	public Double getBasePrice() {
		return basePrice;
	}
	/**
	 * @param basePrice the basePrice to set
	 */
	public void setBasePrice(Double basePrice) {
		this.basePrice = basePrice;
	}
	Double basePrice = null;

}
