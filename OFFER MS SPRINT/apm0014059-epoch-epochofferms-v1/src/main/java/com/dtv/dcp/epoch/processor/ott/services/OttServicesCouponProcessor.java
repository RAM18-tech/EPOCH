package com.dtv.dcp.epoch.processor.ott.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.integration.CpopUCCClient;
import com.dtv.dcp.epoch.model.common.request.CouponsRequestWrapper;
import com.dtv.dcp.epoch.model.ct.request.CTCouponsRequest;
import com.dtv.dcp.epoch.model.ct.response.CTCouponResponse;

@Component
public class OttServicesCouponProcessor {

	/** The log. */
	private static Logger log = LoggerFactory.getLogger(OttServicesCouponProcessor.class);

	@Autowired
	CpopUCCClient cpopUccClient;

	public CTCouponResponse getCoupons(CouponsRequestWrapper couponsRequestWrapper) {

		CTCouponsRequest cTCouponsRequest = new CTCouponsRequest();
		cTCouponsRequest.setCode(couponsRequestWrapper.getCode());
		log.debug("CTRequest: {}", cTCouponsRequest);
		return cpopUccClient.getCoupons(cTCouponsRequest);
	}

}