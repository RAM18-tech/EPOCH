/**
 *
 */
package com.dtv.dcp.epoch.service.satellite;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.satellite.SatelliteCTOffersProcessor;
import com.dtv.dcp.epoch.processor.satellite.sales.SatelliteSalesOffersProcessor;
import com.dtv.dcp.epoch.processor.satellite.services.SatelliteRetentionOffersProcessor;
import com.dtv.dcp.epoch.processor.satellite.services.SatelliteServicesOffersProcessor;
import com.dtv.dcp.epoch.service.DMALookUpService;

/**
 * @author ap778g
 *
 */
@Service
public class SatelliteOffersServiceImpl implements SatelliteOffersService {

    @Autowired
    SatelliteSalesOffersProcessor satelliteSalesOffersProcessor;

    @Autowired
    private DMALookUpService dmaLookUpService;

    @Autowired
    SatelliteServicesOffersProcessor satelliteServicesOffersProcessor;
    
    @Autowired
    SatelliteRetentionOffersProcessor satelliteRetentionOffersProcessor;
    
    @Autowired
	SatelliteCTOffersProcessor satelliteCTOffersProcessor;

    /** The log. */
    private static Logger log = LoggerFactory.getLogger(SatelliteOffersServiceImpl.class);

    @Override
    public CTOfferResponse getOffers(OfferRequestWrapper offerRequestWrapper) throws ServiceException {
        log.debug("Start of SatelliteOffersServiceImpl.getOffers() method..");

        OfferRequest offerRequest = offerRequestWrapper.getOfferRequest();

        CTOfferResponse offersResponse = null;
        List<String> offerActionType = new ArrayList<>();
        List<String> offerType = new ArrayList<>();
        
        if (Optional.ofNullable(offerRequest.getOfferActionType()).isPresent() && !offerRequest.getOfferActionType().isEmpty() && !offerRequest.getOfferActionType().contains(null))
		{
        	offerActionType = offerRequest.getOfferActionType();
		}
        if (Optional.ofNullable(offerRequest.getOfferTypes()).isPresent() && !offerRequest.getOfferTypes().isEmpty() && !offerRequest.getOfferTypes().contains(null))
        {
            offerType=offerRequest.getOfferTypes();
        }
        if(Objects.nonNull(offerRequest) && Objects.nonNull(offerRequest.getCustomerEligibility())) {
        	
        	List<String> zipCode = offerRequest.getCustomerEligibility().getZipCode();
        	List<String> county = offerRequest.getCustomerEligibility().getCounty();
        	
        	if(Objects.nonNull(zipCode) && !zipCode.isEmpty() 
        			&& Objects.nonNull(county) && !county.isEmpty()) {
        		Boolean hasLocalChannels =  dmaLookUpService.hasLocalChannels(zipCode.get(0),county.get(0));
            	offerRequest.setHasLocalChannels(hasLocalChannels);
            	
        	}
        	
        }
        
        if(!offerActionType.isEmpty()) {
        	if(offerActionType.contains(Constants.ACQUISITION_ACTION_TYPE) || offerActionType.contains(Constants.CLOSING_ACTION_TYPE)
            		|| offerActionType.contains(Constants.SOS_ACTION_TYPE)) {
            	offersResponse = satelliteSalesOffersProcessor.getOffers(offerRequestWrapper);
            } else { // services case for enabler and stms
                offersResponse = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
                redemption(offersResponse,offerType);
            }
        } else {
        	if(Objects.nonNull(offerRequest) && (CollectionUtils.isNotEmpty(offerRequest.getOfferCodes()) || CollectionUtils.isNotEmpty(offerRequest.getOfferIds())
        			|| CollectionUtils.isNotEmpty(offerRequest.getBundleProductIds()))) {
            	//To get Offers based on offerId, offerCode, bundleProductId
            	offersResponse = satelliteCTOffersProcessor.getOfferByIds(offerRequestWrapper,false,false);
        	} else { // services case for coupons
                offersResponse = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
                redemption(offersResponse,offerType);
            }
        }

        log.debug("End of SatelliteOffersServiceImpl.getOffers() method..");

        return offersResponse;

    }

    private void redemption(CTOfferResponse offersResponse, List<String> offerType) {
        if(!offerType.isEmpty() && offerType.contains("coupon")){
            if(null!=offersResponse && CollectionUtils.isNotEmpty(offersResponse.getOffers())) {
                offersResponse.getOffers().forEach(offer -> {
                    offer.getAttributes().getAssociatedProducts().forEach(associatedProduct -> {
                        associatedProduct.getQualifyingProducts().forEach(qualifyingproduct -> {
                            if(null!=qualifyingproduct && CollectionUtils.isNotEmpty(qualifyingproduct.getProducts())) {
                                qualifyingproduct.getProducts().forEach(product -> {
                                    if (null != product.getObj() && CollectionUtils.isNotEmpty(product.getObj().getVariants())) {
                                        product.getObj().getVariants().forEach(variant -> {
                                            if (null != variant.getAttributes() && null == variant.getAttributes().getRedemptionLimit()) {
                                                variant.getAttributes().setRedemptionLimit("");
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    });
                });
            }
        }
    }
}