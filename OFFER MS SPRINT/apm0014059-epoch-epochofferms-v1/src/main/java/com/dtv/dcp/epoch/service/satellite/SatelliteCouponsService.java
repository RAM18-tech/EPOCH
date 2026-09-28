package com.dtv.dcp.epoch.service.satellite;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.model.common.request.CouponsRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTCouponResponse;

public interface SatelliteCouponsService {

	/**
	 *
	 * @param headers
	 * @param offerRequestWrapper
	 * @return
	 * @throws ServiceException
	 */
   public CTCouponResponse getCoupons(CouponsRequestWrapper couponsRequestWrapper) throws ServiceException;

}
