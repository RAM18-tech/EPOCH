package com.dtv.dcp.epoch.processor.satellite.sales;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.satellite.SatelliteCTOffersProcessor;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.OffersUtils;

@Component
public class SatelliteSalesFeeProcessor {


    @Autowired
    SatelliteCTOffersProcessor satelliteCTOffersProcessor;

    @Autowired
    OffersUtils offersUtils;

    @Autowired
    FeatureManagerHelper featureManagerHelper;


    public CTOfferResponse getSatelliteFeeOffers(OfferRequestWrapper offerRequestWrapper, List<CTOffer> ctSelectedOffer
            , Map<Map<String, String>, String> actionData) {

        CTOfferResponse ctOfferResponse = satelliteCTOffersProcessor.getSatelliteFeeOffers(offerRequestWrapper, ctSelectedOffer);
        if (offerRequestWrapper.getOfferRequest().getOfferActionType().contains(Constants.ACQUISITION_ACTION_TYPE)) {
            satelliteCTOffersProcessor.filterOffersBasedOnBillingSystem(offerRequestWrapper, ctOfferResponse);
        } else {
            ctOfferResponse.getOffers().removeIf(ctOffer -> offersUtils.isNotValidBasedOnBillingSystem(ctOffer, offerRequestWrapper));
        }
        filterFees(ctOfferResponse.getOffers(), ctSelectedOffer, offerRequestWrapper, actionData);

        return ctOfferResponse;
    }

    private void filterFees(List<CTOffer> offers, List<CTOffer> ctSelectedOffer,
                            OfferRequestWrapper offerRequestWrapper, Map<Map<String, String>, String> actionData) {
        List<CTOffer> activationFeeOffers = null;
        if (Objects.nonNull(offers)) {
            if (Objects.nonNull(offerRequestWrapper) && Objects.nonNull(offerRequestWrapper.getOfferRequest())
                    && Objects.nonNull(offerRequestWrapper.getOfferRequest().getOfferProductType())
                    && !offerRequestWrapper.getOfferRequest().getOfferProductType().contains(Constants.VIDEO_DEVICE)) {
                activationFeeOffers = offersUtils.filterFeeOffersByFeeType(offers, Constants.ACTIVATION_FEE);
            }
            List<CTOffer> hdAccessFee = offersUtils.filterFeeOffersByFeeType(offers, Constants.HD_ACCESS_FEE);
            if (featureManagerHelper.isEnabled(Constants.FEATURE_EPOCH_SERVICE_ACTIVATION_FEE_ENABLED)) {
                activationFeeOffers = offersUtils.filterFeeOffersByFeeType(offers, Constants.ACTIVATION_FEE);
                satelliteCTOffersProcessor.applyFilterOnPriceforBillRefId(activationFeeOffers, null, offerRequestWrapper);
            }
            offers.clear();
            if (CollectionUtils.isNotEmpty(activationFeeOffers)) {
                offers.addAll(activationFeeOffers);
            }
            if (CollectionUtils.isNotEmpty(hdAccessFee)) {
                offers.addAll(hdAccessFee);
            }
        }
    }

}
