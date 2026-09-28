package com.dtv.dcp.epoch.service.ott;
import org.springframework.http.HttpHeaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.model.common.request.ProductRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.processor.helper.CPOPDevicesHelper;
import com.dtv.dcp.epoch.processor.ott.OttProductsProcessor;
import com.dtv.dcp.epoch.util.CTOfferRequestHelper;

/**
 * The Class BaseOffersServiceImpl.
 */
@Component
public class OttProductsImpl implements OttProducts {


	@Autowired
	CPOPDevicesHelper cpopDevicesHelper;

	@Autowired
	OttProductsProcessor ottProductsProcessor;


	@Autowired
	CTOfferRequestHelper ctOfferRequestHelper;

	/** The log. */
	private static Logger log = LoggerFactory.getLogger(OttProductsImpl.class);

	@Override
	public CTProductResponse getProducts(HttpHeaders headers, ProductRequestWrapper productRequestWrapper) throws ServiceException {
		log.debug("Start of OttProductsServiceImpl.getProducts() method..");

		CTProductResponse productsResponse = new CTProductResponse();

		if (ottProductsProcessor.isValidProductRequest(productRequestWrapper.getProductRequest())) {
			productsResponse = ottProductsProcessor.getProducts(productRequestWrapper);
		}

		if ( ottProductsProcessor.isValidDevicesRequest(productRequestWrapper.getProductRequest())) {
			//Getting Devices Response
			productsResponse.setDevices(ottProductsProcessor.getDevices(productRequestWrapper));
		}


		log.debug("End of OttProductsServiceImpl.getProducts() method..");
		log.info("EPOCH_GETPRODUCTS_OTT_SUCCESS");
		return productsResponse;

	}
}