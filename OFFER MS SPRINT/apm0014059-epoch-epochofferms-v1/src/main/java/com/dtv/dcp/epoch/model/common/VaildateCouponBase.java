package com.dtv.dcp.epoch.model.common;

import java.io.Serializable;

import com.dtv.dcp.epoch.model.ct.response.Messages;

public class VaildateCouponBase implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6877076186806497786L;
	
	public VaildateCouponBase(String couponCode, CouponResponseData data, Messages result) {
		this.couponCode = couponCode;
		this.data = data;
		this.results = result;
	}
	
	public VaildateCouponBase() {
		// Intentionally Left Blank For Jackson
	}
	private String couponCode;
	
	private Messages results;
	
	private CouponResponseData data;

	public String getCouponCode() {
		return couponCode;
	}

	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}

	public Messages getResults() {
		return results;
	}

	public void setResults(Messages results) {
		this.results = results;
	}

	public CouponResponseData getData() {
		return data;
	}

	public void setData(CouponResponseData data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "VaildateCouponBase [couponCode=" + couponCode + ", results=" + results + ", data=" + data + "]";
	}
	
	

}
