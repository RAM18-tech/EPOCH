package com.dtv.dcp.epoch.service.watchtv;


import org.springframework.http.HttpHeaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.model.common.request.ProductRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.processor.helper.CPOPDevicesHelper;
import com.dtv.dcp.epoch.processor.watchtv.WatchTVProductsProcessor;
import com.dtv.dcp.epoch.util.CTOfferRequestHelper;

/**
 * The Class BaseOffersServiceImpl.
 */
@Component
public class WatchTVProductsServiceImpl implements WatchTVProductsService {


	@Autowired
	CPOPDevicesHelper cpopDevicesHelper;

	@Autowired
	WatchTVProductsProcessor watchTvProductsProcessor;

	@Autowired
	CTOfferRequestHelper ctOfferRequestHelper;

	/** The log. */
	private static Logger log = LoggerFactory.getLogger(WatchTVProductsServiceImpl.class);

	@Override
	public CTProductResponse getProducts(HttpHeaders headers, ProductRequestWrapper productRequestWrapper) throws ServiceException {
		log.debug("Start of OttProductsServiceImpl.getProducts() method..");

		CTProductResponse productsResponse = null;

		productsResponse = watchTvProductsProcessor.getProducts(productRequestWrapper);
		//Getting Devices Response
		productsResponse.setDevices(cpopDevicesHelper.getDevices(productRequestWrapper.getProductRequest()));
		log.info("EPOCH_GETPRODUCTS_WATCHTV_SUCCESS");
		log.debug("End of OttProductsServiceImpl.getProducts() method..");
		return productsResponse;

	}
}