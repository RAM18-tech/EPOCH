package com.dtv.dcp.epoch.service.watchtv;

import org.springframework.http.HttpHeaders;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.model.common.request.ProductRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;

/**
 * The Interface IntegratedOffersService for IO Offers.
 */
public interface WatchTVProductsService {

	/**
	 *
	 * @param headers
	 * @param productRequestWrapper
	 * @return
	 * @throws ServiceException
	 */
	 CTProductResponse getProducts(HttpHeaders headers, ProductRequestWrapper productRequestWrapper) throws ServiceException;

}
