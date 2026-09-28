package com.dtv.dcp.epoch.model.common.response.ucc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.dtv.dcp.epoch.model.ct.coupon.Coupon;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Coupons implements Serializable {
	private Coupon coupon;
	private List<Offer> offers;

	public Coupon getCoupon() {
		return coupon;
	}

	public void setCoupon(Coupon coupon) {
		this.coupon = coupon;
	}

	public List<Offer> getOffers() {
		return offers;
	}

	public void setOffers(List<Offer> offers) {
		this.offers = offers;
	}
}