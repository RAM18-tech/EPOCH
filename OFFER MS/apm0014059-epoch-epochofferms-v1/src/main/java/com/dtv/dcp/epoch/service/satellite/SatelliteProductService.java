package com.dtv.dcp.epoch.service.satellite;

import org.springframework.http.HttpHeaders;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.model.common.request.ProductRequest;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;

public interface SatelliteProductService {

	CTProductResponse getProducts(HttpHeaders headers, ProductRequest productRequest) throws ServiceException;
	
}
