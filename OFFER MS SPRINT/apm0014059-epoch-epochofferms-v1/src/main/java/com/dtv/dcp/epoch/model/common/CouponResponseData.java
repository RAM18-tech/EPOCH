package com.dtv.dcp.epoch.model.common;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.common.validatecoupons.Coupon;
import com.dtv.dcp.epoch.model.common.validatecoupons.Offers;
import com.dtv.dcp.epoch.model.common.validatecoupons.Products;

public class CouponResponseData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2087628307921923269L;
	
	/** The offers. */
	private List<Offers> offers;
	
	/** The products. */
	private List<Products> products;
	
	private List<Coupon> coupons;

	public List<Offers> getOffers() {
		return offers;
	}

	public void setOffers(List<Offers> offers) {
		this.offers = offers;
	}


	public List<Products> getProducts() {
		return products;
	}

	public void setProducts(List<Products> products) {
		this.products = products;
	}

	public List<Coupon> getCoupons() {
		return coupons;
	}

	public void setCoupons(List<Coupon> coupons) {
		this.coupons = coupons;
	}

	@Override
	public String toString() {
		return "CouponResponseData [offers=" + offers + ", products=" + products + ", coupons=" + coupons + "]";
	}
	
	
	
	

}
