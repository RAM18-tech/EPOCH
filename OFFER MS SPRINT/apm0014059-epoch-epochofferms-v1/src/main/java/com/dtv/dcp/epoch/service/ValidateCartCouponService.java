package com.dtv.dcp.epoch.service;

import com.dtv.dcp.epoch.model.common.request.CouponOffersRequest;
import com.dtv.dcp.epoch.model.common.response.ucc.ValidateCoupon;
import com.dtv.dcp.epoch.model.ct.coupon.Coupon;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;

import java.util.List;

public interface ValidateCartCouponService {

	public Boolean validateRequestAttributes(CouponOffersRequest couponOffersRequest, Coupon cTCouponResponse,
												String couponCode, List<ValidateCoupon> coupons);

	public Boolean validateCoupon(CouponOffersRequest couponOffersRequest, Coupon cTCouponResponse,
									 String couponCode, List<ValidateCoupon> coupons, String couponType, Boolean isCouponValid
			, CTOfferResponse cartContextOfferResponse, CTOfferResponse offerResponse);
}