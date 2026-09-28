package com.dtv.dcp.epoch.processor.ott.sales;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.model.common.request.CartProduct;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.request.CTCustomerContext;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.customergraph.response.UVCustomerAccountResponse;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.OffersUtils;

/**
 * The Class SalesIPTVMigrationProcessor.
 *
 * @author vc402t
 */
/**
 * @author vc402t
 *
 */
@Component
public class SalesATTTVMigrationProcessor {
	
	private static final Logger log = LoggerFactory.getLogger(SalesATTTVMigrationProcessor.class);

	/** The Constant MIGRATION. */
	private static final String MIGRATION = "migration";
	
	@Autowired
	private OttSalesOffersProcessorHelper ottSalesOffersProcessorHelper;
	
	@Autowired
	private FeatureManagerHelper featureHelper;
	
	private static final List<String> productList = Arrays.asList(Constants.IPTV_PRODUCT_FAMILY,Constants.SATELLITE_PRODUCT_FAMILY);
	/**
	 * Populate migration request.
	 *
	 * @param offerRequestWrapper the offer request wrapper
	 * @param ctOfferRequest the ct offer request
	 */
	public void populateMigrationRequest(OfferRequestWrapper offerRequestWrapper, CTOfferRequest ctOfferRequest) {
		OfferRequest offerRequest = offerRequestWrapper.getOfferRequest();
		CartProduct iptv = null;
		CartProduct dtvs = null;
		if (Objects.nonNull(offerRequest) && Objects.nonNull(offerRequest.getCustomerContext())
				&& Objects.nonNull(offerRequest.getCustomerContext().getExistingProductFamily())
				&& (isMatchFound(offerRequest.getCustomerContext().getExistingProductFamily(),
						Constants.IPTV_PRODUCT_FAMILY)
						|| isMatchFound(offerRequest.getCustomerContext().getExistingProductFamily(),
								Constants.SATELLITE_PRODUCT_FAMILY))) {
			List<String> existingProdFamilies = offerRequest.getCustomerContext().getExistingProductFamily();
			iptv = offerRequest.getCustomerContext().getIptv();
			dtvs = offerRequest.getCustomerContext().getSatellite();
			if (Boolean.TRUE.equals(Objects.nonNull(iptv) && iptv.getIsActive())
					&& isMatchFound(existingProdFamilies, Constants.IPTV_PRODUCT_FAMILY)) {
				ctOfferRequest.setOfferIntent(Stream.of(MIGRATION).collect(Collectors.toList()));
				
				getMigrationRequestByFeatures(offerRequest, ctOfferRequest, iptv);
			}

			if (Boolean.TRUE.equals(Objects.nonNull(dtvs) && dtvs.getIsActive())
					&& isMatchFound(existingProdFamilies, Constants.SATELLITE_PRODUCT_FAMILY)) {
				ctOfferRequest.setOfferIntent(Stream.of(MIGRATION).collect(Collectors.toList()));
			}
		} else if (!StringUtils.isEmpty(offerRequestWrapper.getLinkedUverseAccountNums())
				&& (productFamilyExistsInCtOfferRequest(ctOfferRequest, Constants.IPTV_PRODUCT_FAMILY)
						|| productFamilyExistsInCtOfferRequest(ctOfferRequest, Constants.SATELLITE_PRODUCT_FAMILY))) {
			ctOfferRequest.setOfferIntent(Stream.of(MIGRATION).collect(Collectors.toList()));
			
			getMigrationRequestByToggles(offerRequestWrapper, ctOfferRequest, offerRequest);			
		}
	}

	/**
	 * @param offerRequestWrapper
	 * @param ctOfferRequest
	 * @param offerRequest
	 */
	private void getMigrationRequestByToggles(OfferRequestWrapper offerRequestWrapper, CTOfferRequest ctOfferRequest,
			OfferRequest offerRequest) {
		long currentStartTime = System.currentTimeMillis();
		List<String> migrationCohort = new ArrayList<>();
		log.info("CG_CUSTOMER_ACCOUNT_API_EXECUTION_START_TIME:[{}]", currentStartTime);
		UVCustomerAccountResponse response = ottSalesOffersProcessorHelper.getUverseCustomerAccounts(
				offerRequestWrapper.getLinkedUverseAccountNums(), "uverse", UVCustomerAccountResponse.class);
		log.info("CG_CUSTOMER_ACCOUNT_API_EXECUTION_END_TIME:[{}]", System.currentTimeMillis() - currentStartTime);
		if (Objects.nonNull(response) && Objects.nonNull(response.getCustomer())) {
			String iptvSunsetDate = response.getCustomer().getAccounts().get(0).getIptvSunsetDate();
			log.info("CUSTOMER_GRAPH_SUNSET_DATE :[{}]", iptvSunsetDate);
			getIPTVSunsetDateOrCohort(iptvSunsetDate, migrationCohort, true);
		} 

		if(CollectionUtils.isNotEmpty(migrationCohort)) {
			ctOfferRequest.setMigrationCustomerCohort(migrationCohort);
		}
		ctOfferRequest.setMigrationServiceType(
				setMigrationServiceTypeFromCTOfferRequest(ctOfferRequest, offerRequest.getMigrationServiceType()));
	}
	
	/**
	 * @param offerRequest
	 * @param ctOfferRequest
	 * @param iptv
	 */
	private void getMigrationRequestByFeatures(OfferRequest offerRequest, CTOfferRequest ctOfferRequest,
			CartProduct iptv) {
		List<String> migrationCohort = new ArrayList<>();
		if (!StringUtils.isEmpty(iptv.getSunsetDate())) {
			getIPTVSunsetDateOrCohort(iptv.getSunsetDate(), migrationCohort, false);
		}
		if(CollectionUtils.isNotEmpty(migrationCohort)) {
			ctOfferRequest.setMigrationCustomerCohort(migrationCohort);
		}
		ctOfferRequest.setMigrationServiceType(setMigrationServiceTypeFromOfferRequest(offerRequest));
	}
	
	/**
	 * @param sunsetDate
	 * @param migrationCohort
	 * @return
	 */
	private List<String> getIPTVSunsetDateOrCohort(String sunsetDate, List<String> migrationCohort,
			boolean isCGSunsetDate) {
		if (!StringUtils.isEmpty(sunsetDate)) {
			if (isCGSunsetDate) {
				sunsetDate = buildSunsetDateFromCG(sunsetDate);
			}
			migrationCohort.addAll(Stream.of(sunsetDate).collect(Collectors.toList()));
			return migrationCohort;
		}
		return Collections.emptyList();
	}
	private String buildSunsetDateFromCG(String sunsetDateCG) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");
		LocalDate local_date;
		StringBuffer sunsetDate = new StringBuffer();
		try {
			if (StringUtils.isNotBlank(sunsetDateCG) && sunsetDateCG.length() >= 10 ) {
				sunsetDateCG = sunsetDateCG.substring(0,10);
				local_date = LocalDate.parse(sunsetDateCG, formatter);
				sunsetDate.append(OffersUtils.formatTwoDigits(local_date.getMonthValue()))
						  .append(OffersUtils.formatTwoDigits(local_date.getDayOfMonth()))
						  .append(local_date.getYear());
			}
		} catch (Exception e) {
		}
		return sunsetDate.toString();
	}
	

	/**
	 * @param offerRequest
	 * @return
	 */
	private List<String> setMigrationServiceTypeFromOfferRequest(OfferRequest offerRequest) {
		List<String> migrationServiceTypes = null;
		if(Objects.nonNull(offerRequest.getMigrationServiceType()) && Objects.nonNull(offerRequest.getCustomerContext())
				&& Objects.nonNull(offerRequest.getCustomerContext().getExistingProductFamily())){
			migrationServiceTypes = new ArrayList<>();
			List<String> productFamilies = offerRequest.getCustomerContext().getExistingProductFamily();
			if(productFamilies.containsAll(productList)) {
				migrationServiceTypes.addAll(Stream.of(Constants.IPTV_PRODUCT_FAMILY).collect(Collectors.toList()));	
			}else {
				migrationServiceTypes.addAll(offerRequest.getMigrationServiceType());	
			}
		}
		return migrationServiceTypes;
	}
	/**
	 * @param ctOfferRequest
	 * @param migServiceTypes
	 * @return
	 */
	private List<String> setMigrationServiceTypeFromCTOfferRequest(CTOfferRequest ctOfferRequest,List<String> migServiceTypes) {
		List<String> migrationServiceTypes = null;
		if (Objects.nonNull(migServiceTypes) && Objects.nonNull(ctOfferRequest) && Objects.nonNull(ctOfferRequest.getCustomerContext())) {
			migrationServiceTypes = new ArrayList<>();
			List<String> productFamilies = ctOfferRequest.getCustomerContext().stream().filter(Objects::nonNull)
					.map(context -> context.getProductFamily()).collect(Collectors.toList());
		  if(productFamilies.containsAll(productList)) {
			  migrationServiceTypes.addAll(Stream.of(Constants.IPTV_PRODUCT_FAMILY).collect(Collectors.toList()));
		  }else {
			  migrationServiceTypes.addAll(migServiceTypes);
		  }
		
		
		}
		
		return migrationServiceTypes;
	}
	
	/**
	 * Product family exists in ct offer request.
	 *
	 * @param ctOfferRequest the ct offer request
	 * @param productFamily the product family
	 * @return true, if successful
	 */
	private boolean productFamilyExistsInCtOfferRequest(CTOfferRequest ctOfferRequest,String productFamily) {
		boolean exists = false;
		if (Objects.nonNull(ctOfferRequest) && Objects.nonNull(ctOfferRequest.getCustomerContext())) {
			List<CTCustomerContext> customerContext = ctOfferRequest.getCustomerContext();
				exists = customerContext.stream().filter(Objects::nonNull).anyMatch(existProduct -> productFamily.equalsIgnoreCase(existProduct.getProductFamily()));
		}
		return exists;
	}
	
	/**
	 * Filter migration eligible offers.
	 *
	 * @param offerRequestWrapper the offer request wrapper
	 * @param ctOfferResponse the ct offer response
	 * @return the CT offer response
	 */
	public CTOfferResponse filterMigrationEligibleOffers(OfferRequestWrapper offerRequestWrapper,CTOfferRequest ctOfferRequest,CTOfferResponse ctOfferResponse) {
		OfferRequest offerRequest = offerRequestWrapper.getOfferRequest();
		if (Objects.nonNull(ctOfferResponse) && Objects.nonNull(ctOfferResponse.getOffers()) ) {
			
			List<CTOffer> ctOffers = ctOfferResponse.getOffers();
			List<CTOffer> migrationOffers = new ArrayList<>();
			ctOffers.stream().filter(Objects::nonNull).forEach(ctOffer -> {
				
				//if(MIGRATION.equalsIgnoreCase(ctOffer.getAttributes().getOfferIntent()) ) {
					if(CollectionUtils.isNotEmpty(ctOffer.getAttributes().getOfferIntents()) && ctOffer.getAttributes().getOfferIntents().contains(MIGRATION)) {
					if (Objects.nonNull(offerRequest) && Objects.nonNull(offerRequest.getCustomerContext()) &&
						Objects.nonNull(offerRequest.getCustomerContext().getExistingProductFamily()) && 
						(isMatchFound(offerRequest.getCustomerContext().getExistingProductFamily(), Constants.IPTV_PRODUCT_FAMILY) || 
						 isMatchFound(offerRequest.getCustomerContext().getExistingProductFamily(), Constants.SATELLITE_PRODUCT_FAMILY))) {
						List<String> existingProdFamilies = offerRequest.getCustomerContext().getExistingProductFamily();
						// MigrationServiceType: IPTV
						if (isMatchFound(existingProdFamilies, Constants.IPTV_PRODUCT_FAMILY)
								&& isMatchFound(ctOffer.getAttributes().getMigrationServiceType(),Constants.IPTV_PRODUCT_FAMILY)) {
							if (Objects.nonNull(offerRequestWrapper)
									&& Objects.nonNull(offerRequestWrapper.getOfferRequest())
									&& Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext())
									&& Objects.nonNull(
											offerRequestWrapper.getOfferRequest().getCustomerContext().getIptv())) {

								if (isMatchFound(offerRequestWrapper.getOfferRequest().getSalesChannel(),Constants.OPUS) || 
									isMatchFound(offerRequestWrapper.getOfferRequest().getSalesChannel(),Constants.ONLINE) ||
									isMatchFound(offerRequestWrapper.getOfferRequest().getSalesChannel(),Constants.DIRECTV_ONLINE) ||
									isMatchFound(offerRequestWrapper.getOfferRequest().getSalesChannel(),Constants.INDIRECT_PARTNER)) {
									migrationOffers.add(ctOffer);
								}
							}
							// MigrationServiceType: Satellite
						} else if (isMatchFound(existingProdFamilies, Constants.SATELLITE_PRODUCT_FAMILY)
								&& isMatchFound(ctOffer.getAttributes().getMigrationServiceType(),Constants.SATELLITE_PRODUCT_FAMILY) 
								&& isMatchFound(offerRequestWrapper.getOfferRequest().getSalesChannel(),Constants.OPUS)) {
							migrationOffers.add(ctOffer);
							if ((Objects.nonNull(offerRequest.getMigrationServiceType())
									&& offerRequest.getMigrationServiceType().contains(Constants.LEGACY_SATELLITE_PRODUCT_FAMILY))
									&& !isMatchFound(ctOffer.getAttributes().getMigrationServiceType(),Constants.LEGACY_SATELLITE_PRODUCT_FAMILY)) {
								migrationOffers.remove(ctOffer);
							}
						}
					
				}else if(!StringUtils.isEmpty(offerRequestWrapper.getLinkedUverseAccountNums())) {
					if (Objects.nonNull(ctOfferRequest) && Objects.nonNull(ctOfferRequest.getCustomerContext()) 
							&& productFamilyExistsInCtOfferRequest(ctOfferRequest, Constants.IPTV_PRODUCT_FAMILY)
							&& isMatchFound(ctOffer.getAttributes().getMigrationServiceType(),Constants.IPTV_PRODUCT_FAMILY)) {
							
							if (isMatchFound(offerRequestWrapper.getOfferRequest().getSalesChannel(),Constants.OPUS) || 
								isMatchFound(offerRequestWrapper.getOfferRequest().getSalesChannel(),Constants.ONLINE) ||
								isMatchFound(offerRequestWrapper.getOfferRequest().getSalesChannel(),Constants.DIRECTV_ONLINE) ||
								isMatchFound(offerRequestWrapper.getOfferRequest().getSalesChannel(),Constants.INDIRECT_PARTNER)) {
								migrationOffers.add(ctOffer);
							}
						// MigrationServiceType: Satellite
					} else if (productFamilyExistsInCtOfferRequest(ctOfferRequest, Constants.SATELLITE_PRODUCT_FAMILY) 
							&& isMatchFound(ctOffer.getAttributes().getMigrationServiceType(),Constants.SATELLITE_PRODUCT_FAMILY)
							&& isMatchFound(offerRequestWrapper.getOfferRequest().getSalesChannel(),Constants.OPUS)) {
							migrationOffers.add(ctOffer);
							if ((Objects.nonNull(offerRequest.getMigrationServiceType())
									&& offerRequest.getMigrationServiceType().contains(Constants.LEGACY_SATELLITE_PRODUCT_FAMILY))
									&& !isMatchFound(ctOffer.getAttributes().getMigrationServiceType(),Constants.LEGACY_SATELLITE_PRODUCT_FAMILY)) {
								migrationOffers.remove(ctOffer);
							}
					}
				}
			}
			});
			
			ctOfferResponse.setOffers(migrationOffers);
			ctOfferResponse.setCount(migrationOffers.size());
			ctOfferResponse.setTotal(migrationOffers.size());
		  }
		return ctOfferResponse;
	}	
	
	/**
	 * Checks if is match found.
	 *
	 * @param types the types
	 * @param matchType the match type
	 * @return true, if is match found
	 */
	private boolean isMatchFound(List<String> types, String matchType) {
		return types != null ? types.stream().anyMatch(type -> type.equalsIgnoreCase(matchType)): false;
	}
}