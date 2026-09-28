package com.dtv.dcp.epoch.service.satellite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.model.common.request.CouponsRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTCouponResponse;
import com.dtv.dcp.epoch.processor.satellite.services.SatelliteServicesCouponsProcessor;

@Service
public class SatelliteCouponsServicesImpl implements SatelliteCouponsService {

    @Autowired
    SatelliteServicesCouponsProcessor satelliteServicesCouponsProcessor;
    
	private static Logger log = LoggerFactory.getLogger(SatelliteCouponsServicesImpl.class);

    
	@Override
	public CTCouponResponse getCoupons(CouponsRequestWrapper couponsRequestWrapper) throws ServiceException {
		log.debug("Start of CouponServiceImpl.getResults() method..");

		CTCouponResponse cTCouponResponse = null;
		cTCouponResponse = satelliteServicesCouponsProcessor.getCoupons(couponsRequestWrapper);

		log.debug("End of CouponServiceImpl.getResults() method..");
		return cTCouponResponse;
	}

}
