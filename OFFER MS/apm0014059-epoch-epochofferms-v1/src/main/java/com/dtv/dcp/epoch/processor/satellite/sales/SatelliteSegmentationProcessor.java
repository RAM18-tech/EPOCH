/**
 * 
 */
package com.dtv.dcp.epoch.processor.satellite.sales;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.benefit.Benefit;
import com.dtv.dcp.epoch.model.ct.generic.GenericTypeIdBase;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.product.*;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.processor.satellite.SatelliteCTOffersProcessor;
import com.dtv.dcp.epoch.processor.satellite.SatelliteProductsProcessor;
import com.dtv.dcp.epoch.processor.satellite.services.SatelliteServicesOffersProcessor;
import com.dtv.dcp.epoch.util.OffersUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author sx4928
 *
 */
@Component
public class SatelliteSegmentationProcessor {

	@Autowired
	SatelliteProductsProcessor satelliteProductsProcessor;

	@Autowired
	SatelliteCTOffersProcessor satelliteCTOffersProcessor;

	@Autowired
	SatelliteServicesOffersProcessor satelliteServicesOffersProcessor;
	
	@Autowired
	OffersUtils offersUtils;

	private static final Logger logger = LoggerFactory.getLogger(SatelliteSegmentationProcessor.class);

	/**
	 * Performs segmentation processing for satellite offers.
	 * Filters eligible segments and segment offers, and associates benefits based on segment eligibility.
	 *
	 * @param offerRequestWrapper the wrapper containing offer request details
	 * @param finalOfferList the list of final CTOffer objects to process
	 * @param selectedOffer the list of selected CTOffer objects
	 * @param bomFlag flag indicating BOM compatibility checks
	 * @param conditions a map to store associated conditions for offers
	 * @throws ServiceException if an error occurs during segmentation processing
	 */
	public void performSegmentation(OfferRequestWrapper offerRequestWrapper, List<CTOffer> finalOfferList, List<CTOffer> selectedOffer,boolean bomFlag
			,Map<String,List<ProductWrapper>> conditions) throws ServiceException {
		logger.info("SatelliteSegmentationProcessor.performSegmentation::::Start");

		if (CollectionUtils.isNotEmpty(finalOfferList)) {
			// fetch all segments
			CTProductResponse ctProductResponse = satelliteProductsProcessor.getProductsByType(Constants.SEGMENT);

			// filter out segment based on eligibility
			performSegmentEligibility(offerRequestWrapper, ctProductResponse);

			// fetch all segment offers
			CTOfferResponse segmentOffers = satelliteCTOffersProcessor.getSatelliteOffers(offerRequestWrapper,
					new ArrayList<>(List.of(Constants.SEGMENT)));
			
			// filter out segment offers based on eligibility
			performSegmentOfferEligibility(selectedOffer, segmentOffers, bomFlag, offerRequestWrapper);

			// filter benefits based on segments eligible
			filterBenefits(finalOfferList, ctProductResponse, segmentOffers, conditions);
		}

		logger.info("SatelliteSegmentationProcessor.performSegmentation::::Ends");

	}

	/**
	 * Remove ineligible segments
	 *
	 * @param offerRequestWrapper the wrapper containing offer request details
	 * @param ctProductResponse the response containing segment products to filter
	 */
	private void performSegmentEligibility(OfferRequestWrapper offerRequestWrapper,
			CTProductResponse ctProductResponse) {
		logger.info("SatelliteSegmentationProcessor.performSegmentEligibility::::Start");

		ctProductResponse.getProducts().removeIf(segment -> isNotValidSegment(segment, offerRequestWrapper));
		
		if (CollectionUtils.isNotEmpty(ctProductResponse.getProducts()) && ctProductResponse.getProducts().size() > 1) {
			ctProductResponse.getProducts()
					.sort(Comparator.comparing(prod -> prod.getVariants().get(0).getAttributes().getSegmentLevel()));
		
			Collections.reverse(ctProductResponse.getProducts());
			ProductObj obj = ctProductResponse.getProducts().stream().findFirst().orElse(null);
			ctProductResponse.getProducts().clear();
			ctProductResponse.getProducts().add(obj);
		}
		
		logger.debug("SatelliteSegmentationProcessor.performSegmentEligibility::::Ends");
	}

	/**
	 * Returns true if the segment is ineligible
	 *
	 * @param segment the segment product to validate
	 * @param offerRequestWrapper the wrapper containing offer request details
	 * @return true if the segment is ineligible, false otherwise
	 */
	private boolean isNotValidSegment(ProductObj segment, OfferRequestWrapper offerRequestWrapper) {
		if (Objects.nonNull(segment) && CollectionUtils.isNotEmpty(segment.getVariants())
				&& Objects.nonNull(segment.getVariants().get(0))
				&& Objects.nonNull(segment.getVariants().get(0).getAttributes())) {

			Attributes segmentAttributes = segment.getVariants().get(0).getAttributes();

			return isNotValidBasedOnDate(segmentAttributes,offerRequestWrapper) || isNotValidBasedOnStatus(segmentAttributes)
					|| isNotValidBasedOnSalesChannel(segmentAttributes, offerRequestWrapper)
					|| isNotValidBasedOnAccountType(segmentAttributes, offerRequestWrapper)
					|| isNotValidBasedOnAccountStatus(segmentAttributes, offerRequestWrapper)
					|| isNotValidBasedOnSalesSubChannel(segmentAttributes, offerRequestWrapper);

		}
		return true;
	}

	/**
	 * Returns true if the segment is ineligible based on start and end dates
	 *
	 * @param segmentAttributes the attributes of the segment containing start and end dates
	 * @param offerRequestWrapper the wrapper containing the server date information
	 * @return true if the segment is ineligible based on dates, false otherwise
	 */
	private boolean isNotValidBasedOnDate(Attributes segmentAttributes,OfferRequestWrapper offerRequestWrapper) {
		String startDate = segmentAttributes.getStartDate();
		String endDate = segmentAttributes.getEndDate();
        Date serverDate = OffersUtils.getServerDateValue(offerRequestWrapper);
		return !OffersUtils.validateActiveDates(OffersUtils.getFormattedDate(startDate),
				OffersUtils.getFormattedDate(endDate),serverDate);
	}

	/**
	 * Returns true if the segment is ineligible based on status
	 *
	 * @param segmentAttributes the attributes of the segment containing status information
	 * @return true if the segment is ineligible based on status, false otherwise
	 */
	private boolean isNotValidBasedOnStatus(Attributes segmentAttributes) {
		return !Constants.ACTIVE.equalsIgnoreCase(segmentAttributes.getSegmentStatus());
	}
	
	/**
	 * Returns true if the segment is ineligible based on saleschannel from
	 * request
	 *
	 * @param segmentAttributes the attributes of the segment containing sales channel information
	 * @param offerRequestWrapper the wrapper containing offer request details
	 * @return true if the segment is ineligible based on sales channel, false otherwise
	 */
	private boolean isNotValidBasedOnSalesChannel(Attributes segmentAttributes,
			OfferRequestWrapper offerRequestWrapper) {
		if (Objects.nonNull(segmentAttributes.getSalesSystem())) {
			List<String> salesChannels = satelliteServicesOffersProcessor
					.getListFromString(segmentAttributes.getSalesSystem());
			if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getSalesChannel())) {
				return salesChannels.stream().noneMatch(offerRequestWrapper.getOfferRequest()
						.getSalesChannel().get(0)::equalsIgnoreCase);
			} else {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns true if the segment is ineligible based on business segment from
	 * request
	 *
	 * @param segmentAttributes the attributes of the segment containing eligible account type information
	 * @param offerRequestWrapper the wrapper containing offer request details
	 * @return true if the segment is ineligible based on account type, false otherwise
	 */
	private boolean isNotValidBasedOnAccountType(Attributes segmentAttributes,
			OfferRequestWrapper offerRequestWrapper) {
		if (Objects.nonNull(segmentAttributes.getEligibleAccountTypeList())) {
			List<String> eligibleAccountTypes = satelliteServicesOffersProcessor
					.getListFromString(segmentAttributes.getEligibleAccountTypeList());
			if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext())
					&& Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite())
					&& Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite()
							.getBusinessSegment())) {
				return eligibleAccountTypes.stream().noneMatch(offerRequestWrapper.getOfferRequest()
						.getCustomerContext().getSatellite().getBusinessSegment()::equalsIgnoreCase);
			} else {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns true if the segment is ineligible based on account status from
	 * request
	 *
	 * @param segmentAttributes the attributes of the segment containing eligible account status information
	 * @param offerRequestWrapper the wrapper containing offer request details
	 * @return true if the segment is ineligible based on account status, false otherwise
	 */
	private boolean isNotValidBasedOnAccountStatus(Attributes segmentAttributes,
			OfferRequestWrapper offerRequestWrapper) {
		if (Objects.nonNull(segmentAttributes.getEligibleAccountStatus())) {
			List<String> eligibleAccountStatus = satelliteServicesOffersProcessor
					.getListFromString(segmentAttributes.getEligibleAccountStatus());
			if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext())
					&& Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite())
					&& Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite()
							.getAccountStatus())) {
				return eligibleAccountStatus.stream().noneMatch(offerRequestWrapper.getOfferRequest()
						.getCustomerContext().getSatellite().getAccountStatus()::equalsIgnoreCase);
			} else {
				return true;
			}

		}

		return false;
	}

	/**
	 * Returns true if the segment is ineligible based on sales sub channel from
	 * request
	 * 
	 * @param segmentAttributes the attributes of the segment containing sales sub channel information
	 * @param offerRequestWrapper the wrapper containing offer request details
	 * @return true if the segment is ineligible based on sales sub channel, false otherwise
	 */
	private boolean isNotValidBasedOnSalesSubChannel(Attributes segmentAttributes,
			OfferRequestWrapper offerRequestWrapper) {
		if (CollectionUtils.isNotEmpty(segmentAttributes.getSalesSubChannel())) {
			List<String> salesSubChannel = segmentAttributes.getSalesSubChannel();
			if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getChannelEligibility()) && Objects
					.nonNull(offerRequestWrapper.getOfferRequest().getChannelEligibility().getSalesSubChannel())) {
				return salesSubChannel.stream().noneMatch(offerRequestWrapper.getOfferRequest().getChannelEligibility()
						.getSalesSubChannel()::equalsIgnoreCase);
			} else {
				return true;
			}

		}

		return false;
	}

	/**
	 * Filter the benefits based on segments eligible
	 *
	 * @param finalOfferList the list of final CTOffer objects to process
	 * @param ctProductResponse the response containing eligible segment products
	 * @param segmentOffers the response containing eligible segment offers
	 * @param conditions a map to store associated conditions for each offer code
	 */
	private void filterBenefits(List<CTOffer> finalOfferList, CTProductResponse ctProductResponse,
			CTOfferResponse segmentOffers, Map<String,List<ProductWrapper>> conditions) {
		logger.info("SatelliteSegmentationProcessor.filterBenefits::::Start");
		
		// fetch all eligible segment offer codes from the segment
		logger.debug("Eligible Segments List");
		List<String> segmentOfferCodes = new ArrayList<>();
		ctProductResponse.getProducts().stream().filter(Objects::nonNull).forEach(segment -> {
			logger.debug("Segment is HIT [{}] : ", segment.getCode());
			List<SegmentOffer> segOffers = segment.getVariants().get(0).getAttributes().getSegmentOffers();
			if (CollectionUtils.isNotEmpty(segOffers)) {
				List<String> offerCodes = segOffers.stream().filter(Objects::nonNull)
						.map(SegmentOffer::getSegmentOfferId).collect(Collectors.toList());
				segmentOfferCodes.addAll(offerCodes);
			}
		});

		// remove ineligible segment offers
		segmentOffers.getOffers()
				.removeIf(offer -> segmentOfferCodes.stream().noneMatch(offer.getCode()::equalsIgnoreCase));

		// remove benefit association from the actual offer
		finalOfferList.stream().filter(Objects::nonNull).forEach(offer -> {
			offer.getAttributes().getBenefits().removeIf(benefit -> true);
		});

		// associate benefit based segment eligiblity
		finalOfferList.stream().filter(Objects::nonNull).forEach(offer -> {
			if (CollectionUtils.isNotEmpty(offer.getAttributes().getAssociatedOffers())) {
				List<String> associatedOffers = offer.getAttributes().getAssociatedOffers().stream()
						.filter(Objects::nonNull).map(GenericTypeIdBase::getKey).collect(Collectors.toList());
				associatedOffers.stream().forEach(offerCd -> {
					List<Benefit> benefits = getBenefitsFromSegmentOffer(segmentOffers, offerCd, conditions);
					if (CollectionUtils.isNotEmpty(benefits)) {
						if (Objects.nonNull(offer.getAttributes().getBenefits())) {
							offer.getAttributes().getBenefits().addAll(benefits);
						} else {
							offer.getAttributes().setBenefits(benefits);
						}
					}
				});
			}
		});
		logger.debug("SatelliteSegmentationProcessor.filterBenefits::::Ends");
	}

	/**
	 * Get the benefits from segment offer
	 *
	 * @param segmentOffers the response containing eligible segment offers
	 * @param offerCode the code of the offer to retrieve benefits for
	 * @param conditions a map to store associated conditions for each offer code
	 * @return a list of benefits associated with the segment offer, or an empty list if not found
	 */
	private List<Benefit> getBenefitsFromSegmentOffer(CTOfferResponse segmentOffers, String offerCode, Map<String,List<ProductWrapper>> conditions) {
		Optional<CTOffer> matchingObject = segmentOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> offerCode.equalsIgnoreCase(offer.getCode())).findFirst();
		if (matchingObject.isPresent()) {
			List<ProductWrapper> conditionsList = getAssociatedConditionsFromSegmentOffer(segmentOffers, offerCode);
			conditions.put(offerCode, conditionsList);
			return matchingObject.get().getAttributes().getBenefits();
		}
		return Collections.emptyList();
	}
	
	
	/**
	 * Remove ineligible segment offers
	 *
	 * @param selectedOffer the list of selected CTOffer objects
	 * @param segmentOffers the response containing segment offers to filter
	 * @param bomFlag flag indicating BOM compatibility checks
	 * @param offerRequestWrapper the wrapper containing offer request details
	 */
	private void performSegmentOfferEligibility(List<CTOffer> selectedOffer, CTOfferResponse segmentOffers, boolean bomFlag, OfferRequestWrapper offerRequestWrapper) {
		logger.info("SatelliteSegmentationProcessor.performSegmentOfferEligibility::::Start");
		
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		ctOfferResponse.setOffers(selectedOffer);
		
		List<String> existingProducts = satelliteServicesOffersProcessor.populateExistingProducts(offerRequestWrapper, ctOfferResponse);

		segmentOffers.getOffers().removeIf(segmentOffer -> isNotValidSegmentOffer(segmentOffer, selectedOffer, bomFlag, existingProducts, offerRequestWrapper));
		
		logger.debug("SatelliteSegmentationProcessor.performSegmentOfferEligibility::::Ends");
	}

	/**
	 * Returns true if the segment offer is ineligible
	 *
	 * @param segmentOffer the segment offer to validate
	 * @param selectedOffer the list of selected CTOffer objects
	 * @param bomFlag flag indicating BOM compatibility checks
	 * @param existingProducts the list of existing product codes
	 * @param offerRequestWrapper the wrapper containing offer request details
	 * @return true if the segment offer is ineligible, false otherwise
	 */
	private boolean isNotValidSegmentOffer(CTOffer segmentOffer, List<CTOffer> selectedOffer, boolean bomFlag, List<String> existingProducts, OfferRequestWrapper offerRequestWrapper) {
		
		if (Objects.nonNull(segmentOffer)) {
			return !satelliteCTOffersProcessor.isQualifyingOffer(segmentOffer, selectedOffer) 
					|| (bomFlag && satelliteCTOffersProcessor.isInCompatibleOffer(segmentOffer, selectedOffer))
					|| satelliteServicesOffersProcessor.isNotValidBasedOnRequiredProduct(segmentOffer,existingProducts)
					|| offersUtils.isNotValidBasedOnCommitmentDuration(segmentOffer, selectedOffer, offerRequestWrapper);
		}
		return true;
	}
	
	private List<ProductWrapper> getAssociatedConditionsFromSegmentOffer(CTOfferResponse segmentOffers, String offerCode) {
		
		List<ProductWrapper> associatedConditions=new ArrayList<>();
		Optional<CTOffer> matchingObject = segmentOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> offerCode.equalsIgnoreCase(offer.getCode())).findFirst();
		if (matchingObject.isPresent() && Objects.nonNull(matchingObject.get().getAttributes().getAssociatedProducts())) {
			 matchingObject.get().getAttributes().getAssociatedProducts().forEach(associatedProd->{
				 if(CollectionUtils.isNotEmpty(associatedProd.getAssociatedConditions())) {
					 associatedConditions.addAll(associatedProd.getAssociatedConditions());
				 }
			 });
		}
		return associatedConditions;
	}
	
}