package com.dtv.dcp.epoch.service.satellite;

import java.util.List;

import org.springframework.http.HttpHeaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.model.common.request.ProductRequest;
import com.dtv.dcp.epoch.model.common.request.ProductRequestWrapper;
import com.dtv.dcp.epoch.model.ct.generic.GenericByKey;
import com.dtv.dcp.epoch.model.ct.product.ProductDescriptionsByKey;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.model.ct.product.Variant;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.processor.satellite.SatelliteProductsProcessor;
import com.dtv.dcp.epoch.util.OffersUtils;

@Component
public class SatelliteProductServiceImpl implements SatelliteProductService {

	/** The log. */
	private static Logger log = LoggerFactory.getLogger(SatelliteProductServiceImpl.class);

	@Autowired
	SatelliteProductsProcessor productProcessor;

	@Override
	public CTProductResponse getProducts(HttpHeaders headers, ProductRequest productRequest) throws ServiceException {
		log.info("Start of SatelliteProductsServiceImpl.getOffers() method..");

		ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
		productRequestWrapper.setLoggedInId(headers.getFirst(Constants.IDPCTX_LOGGEDINID));
		productRequestWrapper.setAuthAccountsWireless(OffersUtils.getFirstAccount(headers.getFirst(Constants.IDPCTX_AUTHORIZEDACCOUNTS_WIRELESS)));
		productRequestWrapper.setDtvnAccount(OffersUtils.getFirstAccount(headers.getFirst(Constants.IDPCTX_DTVN_ACCOUNT)));
		productRequestWrapper.setSessionId(headers.getFirst(Constants.IDPCTX_SESSION_ID));
		productRequestWrapper.setLinkedUverseAccountNums(OffersUtils.getFirstAccount(headers.getFirst(Constants.IDPCTX_LINKEDUVERSEACCNUMS)));
		productRequestWrapper.setProductRequest(productRequest);

		CTProductResponse productsResponse = null;
		productsResponse = productProcessor.getProducts(productRequestWrapper);

		List<String> salesChannel = productRequest.getSalesChannel();
		if (null != salesChannel && !salesChannel.isEmpty()) {
			updateSalesChannelSpecificKeys(salesChannel, productsResponse);
		}
		log.debug("End of Satellite ProductsServiceImpl.getProducts() method..");
		log.debug("EPOCH_GETPRODUCTS_SUCCESS");
		return productsResponse;

	}

	/**
	 * This method sends only relavant displayNamesByKey & descriptionByKey
	 * attributes in the response based on sales channel attribute in the request.
	 * if no sales channel available then returns all the keys available.
	 * 
	 * @param salesChannel2
	 * @param productsResponse
	 */
	private void updateSalesChannelSpecificKeys(List<String> salesChannel, CTProductResponse productsResponse) {

		if(null!=productsResponse.getProducts()) {
			for (ProductObj prod : productsResponse.getProducts()) {
				for (Variant variant : prod.getVariants()) {
					GenericByKey displayNameByKey = variant.getAttributes().getDisplayNamesByKey();
					ProductDescriptionsByKey descriptionByKey = variant.getAttributes().getDescriptionsByKey();
					if (null != displayNameByKey) {
						if (!salesChannel.contains("onlineSales")) {
							displayNameByKey.setAttdotcom(null);
							descriptionByKey.setShortDescMyatt(null);
							descriptionByKey.setLongDescription(null);
						}

						if (!salesChannel.contains("onlineServices")) {
							displayNameByKey.setOnlineService(null);
							descriptionByKey.setShortDescService(null);
							descriptionByKey.setLongSescServices(null);
						}

						if (!salesChannel.contains("opus")) {
							displayNameByKey.setOpus(null);
							descriptionByKey.setShortDescOpus(null);
						}

					}
				}
			}
		}
	}

}
