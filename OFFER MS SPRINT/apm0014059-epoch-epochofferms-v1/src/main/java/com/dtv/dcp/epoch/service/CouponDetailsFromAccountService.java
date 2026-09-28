package com.dtv.dcp.epoch.service;

public interface CouponDetailsFromAccountService {
	<T> T getCustomerCoupons(String accountNum);
}