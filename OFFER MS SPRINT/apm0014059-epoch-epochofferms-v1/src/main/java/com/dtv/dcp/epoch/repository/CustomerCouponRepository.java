package com.dtv.dcp.epoch.repository;

import com.dtv.dcp.epoch.model.customergraph.CustomerCouponsResults;

public interface CustomerCouponRepository {
	<T> T getCustomerCoupons(String accountNum);
}