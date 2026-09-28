package com.dtv.dcp.epoch.processor.watchtv;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.util.OffersUtils;

/**
 * 
 * @author sn611j
 *
 */
@Component
public class WatchTVCTOffersProcessor {

    private static final Logger log = LoggerFactory.getLogger(WatchTVCTOffersProcessor.class);

    @Autowired
    CpopClientHelper cpopClientHelper;
    
	@Autowired
	OffersUtils offersUtils;

	public CTOfferResponse getVideoAddonOffers(OfferRequestWrapper offerRequestWrapper) {
		
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		ctOfferRequest.setOfferProductFamily(offerRequestWrapper.getOfferRequest().getOfferProductFamily());
		ctOfferRequest.setOfferProductType(offerRequestWrapper.getOfferRequest().getOfferProductType());
		ctOfferRequest.setOfferActionType(offerRequestWrapper.getOfferRequest().getOfferActionType());
        ctOfferRequest.setSalesChannel(offerRequestWrapper.getOfferRequest().getSalesChannel());
		ctOfferRequest.setOfferCodes(offerRequestWrapper.getOfferRequest().getOfferCodes());
		ctOfferRequest.setExpiredOffers(offerRequestWrapper.getOfferRequest().isExpiredOffers());
		log.debug("\n WatchTVCTOffersProcessor getVideoAddonOffers payLoad >>>>> {}", ctOfferRequest);
		CTOfferResponse ctOfferResponse = getWatchTVOffersFromCT(ctOfferRequest);
		return ctOfferResponse;

	}

    /**
     *
     * @param offerRequestWrapper
     * @return
     */
    public CTOfferResponse getOfferByIds(OfferRequestWrapper offerRequestWrapper) {
        CTOfferResponse ctOfferResponse = null;

        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        if (Objects.nonNull(offerRequestWrapper.getCtOfferRequest())) {
            BeanUtils.copyProperties(offerRequestWrapper.getCtOfferRequest(), ctOfferRequest);
        }

        ctOfferRequest.setOfferIds(offerRequestWrapper.getOfferRequest().getOfferIds());
        ctOfferRequest.setOfferCodes(offerRequestWrapper.getOfferRequest().getOfferCodes());
        ctOfferRequest.setBundleProducts(offerRequestWrapper.getOfferRequest().getBundleProductIds());
        ctOfferRequest.setSalesChannel(offerRequestWrapper.getOfferRequest().getSalesChannel());
        ctOfferRequest.setPagination(offerRequestWrapper.getOfferRequest().getPagination());

        ctOfferResponse = getWatchTVOffersFromCT(ctOfferRequest);

        return ctOfferResponse;

    }

    /**
     * @param ctOfferRequest
     * @return
     * @throws ServiceException
     */


    public CTOfferResponse getWatchTVOffersFromCT(CTOfferRequest ctOfferRequest) {
        CTOfferResponse watchTVOfferResponse = cpopClientHelper.getOffers(ctOfferRequest);
        if (watchTVOfferResponse == null) {
            log.info("In Method getOffersFromCT.OttCTOffersProcessor() :: Getting the null response from the CTMS");
            throw new ServiceException(ErrorMessages.CT_ERROR)
                    .addDetail(ErrorMessages.CT_ERROR_DETAILS);
        } else {
        	filterActiveWatchTVOffers(watchTVOfferResponse);
        }
        log.info("In Method getOffersFromCT.OttCTOffersProcessor() :: Getting response from the CTMS");
        return watchTVOfferResponse;
    }

    /**
     * Filter active offers.
     *
     * @param watchTVOfferResponse the offer response
     */
    public void filterActiveWatchTVOffers(CTOfferResponse watchTVOfferResponse) {
        List<CTOffer> watchTVOffers = new ArrayList<>();
        if (Optional.ofNullable(watchTVOfferResponse).isPresent() && Optional.ofNullable(watchTVOfferResponse.getOffers()).isPresent()) {
            watchTVOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {
                if (ctOffer != null && ctOffer.getStartDate() != null && ctOffer.getEndDate() != null
                        && OffersUtils.validateActiveDates(OffersUtils.getFormattedDate(ctOffer.getStartDate()),
                        OffersUtils.getFormattedDate(ctOffer.getEndDate()))) {
                	watchTVOffers.add(ctOffer);
                }
            });
            watchTVOfferResponse.setOffers(watchTVOffers);
        }
    }	
}
