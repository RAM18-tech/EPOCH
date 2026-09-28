package com.dtv.dcp.epoch.service;

import com.dtv.dcp.epoch.repository.CustomerCouponRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CouponDetailsFromAccountServiceImpl implements CouponDetailsFromAccountService {
	private static final Logger log = LoggerFactory.getLogger(CouponDetailsFromAccountServiceImpl.class);
	@Autowired
	private CustomerCouponRepository couponDetailsFromAccountNumber;

	@Override
	public <T> T getCustomerCoupons(String accountNum) {
		log.debug("Inside CouponDetailsFromAccountServiceImpl.getCustomerCoupons() method..");
		return couponDetailsFromAccountNumber.getCustomerCoupons(accountNum);
	}
}