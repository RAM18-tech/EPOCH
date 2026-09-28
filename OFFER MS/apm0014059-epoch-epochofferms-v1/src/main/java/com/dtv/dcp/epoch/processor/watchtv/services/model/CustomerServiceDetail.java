package com.dtv.dcp.epoch.processor.watchtv.services.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerServiceDetail {

	private String typeOfPlan;
	private Map<String,Map<String,String>> accountPromoStartEndDate = new HashMap<>();
	private Double accountPrice;
	private List<String> promosId;
	/**
	 * @return the typeOfPlan
	 */
	public String getTypeOfPlan() {
		return typeOfPlan;
	}
	/**
	 * @param typeOfPlan the typeOfPlan to set
	 */
	public void setTypeOfPlan(String typeOfPlan) {
		this.typeOfPlan = typeOfPlan;
	}
	/**
	 * @return the accountPromoStartEndDate
	 */
	public Map<String, Map<String, String>> getAccountPromoStartEndDate() {
		return accountPromoStartEndDate;
	}
	/**
	 * @param accountPromoStartEndDate the accountPromoStartEndDate to set
	 */
	public void setAccountPromoStartEndDate(Map<String, Map<String, String>> accountPromoStartEndDate) {
		this.accountPromoStartEndDate = accountPromoStartEndDate;
	}
	
	/**
	 * @return the accountPrice
	 */
	public Double getAccountPrice() {
		return accountPrice;
	}
	/**
	 * @param accountPrice the accountPrice to set
	 */
	public void setAccountPrice(Double accountPrice) {
		this.accountPrice = accountPrice;
	}
	/**
	 * @return the promosId
	 */
	public List<String> getPromosId() {
		return promosId;
	}
	/**
	 * @param promosId the promosId to set
	 */
	public void setPromosId(List<String> promosId) {
		this.promosId = promosId;
	}
}
