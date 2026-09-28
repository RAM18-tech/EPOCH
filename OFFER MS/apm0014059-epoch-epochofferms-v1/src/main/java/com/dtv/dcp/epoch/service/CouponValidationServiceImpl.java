package com.dtv.dcp.epoch.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.model.common.EligibilityContext;
import com.dtv.dcp.epoch.model.common.PurchaseContext;
import com.dtv.dcp.epoch.model.common.request.CouponValidationRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.common.request.ProductRequest;
import com.dtv.dcp.epoch.model.common.request.ProductRequestWrapper;
import com.dtv.dcp.epoch.model.ct.request.CTCustomerContext;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.request.Product;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.processor.bundle.BundleOfferProcessor;
import com.dtv.dcp.epoch.processor.satellite.SatelliteProductsProcessor;
import com.dtv.dcp.epoch.processor.satellite.services.SatelliteServicesOffersProcessor;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.OffersUtils;

@Service
public class CouponValidationServiceImpl implements CouponValidationService {



	@Autowired
	BundleOfferProcessor bundleOfferProcessor;

	@Autowired
	SatelliteProductsProcessor productProcessor;

	@Autowired
	SatelliteServicesOffersProcessor offerProcessor;

	@Autowired
	FeatureManagerHelper featureManagerHelper;

	    /**
	     * The log.
	     */
	    private static Logger log = LoggerFactory.getLogger(CouponValidationServiceImpl.class);

	    @Override
	    public CTOfferResponse getBundleOffers(OfferRequestWrapper offerRequestWrapper) throws ServiceException {
	        log.debug("Start of BundleOfferServiceImpl.getBundleOffers() method..");

	        OfferRequest offerRequest = offerRequestWrapper.getOfferRequest();

	        CTOfferResponse offersResponse = null;
	        /**
	         * VBB Offers are for Acquisition only.
	         */
			if (Optional.ofNullable(offerRequest).isPresent()
					&& Optional.ofNullable(offerRequest.getOfferActionType()).isPresent()) {
				offersResponse = bundleOfferProcessor.getBundleOffers(offerRequestWrapper);
				log.debug("End of BundleOfferServiceImpl.getBundleOffers() method..");
			}
	        return offersResponse;

	    }
	    
	    
	    @Override
	    public CTProductResponse getProducts(ProductRequestWrapper productRequestWrapper) throws ServiceException {
	        log.debug("Start of CouponValidation.getproducts method..");

	        ProductRequest productRequest = productRequestWrapper.getProductRequest();

	        CTProductResponse productResponse = null;
	        /**
	         * VBB Offers are for Acquisition only.
	         */
//			if (Optional.ofNullable(offerRequest).isPresent()
//					&& Optional.ofNullable(offerRequest.getOfferActionType()).isPresent()) {
	        productResponse = productProcessor.getProducts(productRequestWrapper);
				log.debug("End of CouponValidation.getproducts method..");
			//}
	        return productResponse;

	    }
	    
	    public CTOfferResponse getOffers(OfferRequestWrapper offerRequestWrapper) throws ServiceException {
	        log.debug("Start of CouponValidationServiceImpl.getOffers() method..");

	        CTOfferRequest cTOfferRequest = offerRequestWrapper.getCtOfferRequest();
	        
	        CTOfferResponse offersResponse = null;

	        offersResponse = offerProcessor.getOffersFromCT(cTOfferRequest);
	        log.debug("End of CouponValidationServiceImpl.getOffers() method..");

	        return offersResponse;

	    }
	    
		public OfferRequestWrapper generateOfferRequest(String campaignCode)
		{
			OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
			CTOfferRequest cTOfferRequest = new CTOfferRequest();
			CTCustomerContext customerContext  = new CTCustomerContext();
			Product products = new Product();
			List<Product> productList =  new ArrayList<Product>();
			products.setProductCode(campaignCode);
			productList.add(products);
			customerContext.setProducts(productList);
			List<CTCustomerContext> customerContextList = new ArrayList<CTCustomerContext>();
			List<String> offerType =  new ArrayList<String>();
			offerType.add(Constants.COUPON);
			customerContextList.add(customerContext);
			cTOfferRequest.setCustomerContext(customerContextList);
			cTOfferRequest.setOfferType(offerType);
			cTOfferRequest.setState(null);
			offerRequestWrapper.setCtOfferRequest(cTOfferRequest);
			
			return offerRequestWrapper;
		}
		
		public Boolean ValidateCampaignEligibility(CTOfferResponse offerResponse , CouponValidationRequest couponValidationRequest)
		{
			try {
				EligibilityContext eligibility = new EligibilityContext();
				String benefitType = null;
				BigDecimal minPurchaseAmount = null;
				
				if (Objects.nonNull(offerResponse.getProducts()) && offerResponse.getProducts().size() > 0
						&& Objects.nonNull(offerResponse.getProducts().get(0).getVariants())
						&& Objects.nonNull(offerResponse.getProducts().get(0).getVariants().get(0).getAttributes())
						&& Objects.nonNull(offerResponse.getProducts().get(0).getVariants().get(0).getAttributes()
								.getCampaignEligibility())
						&& offerResponse.getProducts().get(0).getVariants().get(0).getAttributes()
								.getCampaignEligibility().size() > 0) {
					eligibility = offerResponse.getProducts().get(0).getVariants().get(0).getAttributes()
							.getCampaignEligibility().get(0);
				}

				if (Objects.nonNull(offerResponse.getOffers()) && (offerResponse.getOffers().size() > 0)
						&& Objects.nonNull(offerResponse.getOffers().get(0).getAttributes())
						&& (offerResponse.getOffers().get(0).getAttributes().getBenefits().size() > 0) && offerResponse
								.getOffers().get(0).getAttributes().getBenefits().get(0).getBenefitType() != null) {
					benefitType = offerResponse.getOffers().get(0).getAttributes().getBenefits().get(0)
							.getBenefitType();
					log.info("Benefit type value {}", benefitType);
				}
				if (Objects.nonNull(offerResponse.getOffers()) && (offerResponse.getOffers().size() > 0) 
						&& Objects.nonNull(offerResponse.getOffers().get(0).getAttributes())
						&& Objects.nonNull(offerResponse.getOffers().get(0).getAttributes().getEligibility())
						&& Objects.nonNull(offerResponse.getOffers().get(0).getAttributes().getEligibility().getConstraints())
						&& offerResponse.getOffers().get(0).getAttributes().getEligibility().getConstraints().size() > 0
						&& Objects.nonNull(offerResponse.getOffers().get(0).getAttributes().getEligibility().getConstraints().get(0).getMinimumPurchaseAmount())
						) {
					minPurchaseAmount = offerResponse.getOffers().get(0).getAttributes().getEligibility().getConstraints().get(0).getMinimumPurchaseAmount();
					log.info("minPurchaseAmount value {}", minPurchaseAmount);
				}

				log.info("End-ValidateCampaignEligibility");

				return this.matchEligibility(eligibility, benefitType, couponValidationRequest, minPurchaseAmount);
			} catch (Exception exc) {
				log.error("Exception caught while validating campaign eligibility {}", exc.getMessage());
			}
			return false;
		}
		
		public Boolean matchEligibility(EligibilityContext eligibility , String benefitType, CouponValidationRequest couponValidationRequest, BigDecimal minPurchaseAmount)
		{
			log.info("Start-matchEligibility");
			log.info("couponValidationRequest.getPurchaseContext().getNewRelease() {}",couponValidationRequest.getPurchaseContext().getNewRelease());
			Object titleData=null;
			if(null==eligibility.getTitles()){
				titleData=eligibility.getTitle();
			}else{
				titleData=eligibility.getTitles();
			}

			if (eligibility.getPurchaseType() != null
					&& Optional.ofNullable(couponValidationRequest.getPurchaseContext().getPurchaseType()).isPresent()
					&& !couponValidationRequest.getPurchaseContext().getPurchaseType().isEmpty()
				    && !(this.validatePurchaseType(couponValidationRequest.getPurchaseContext().getPurchaseType(),
						eligibility.getPurchaseType()))) {
							log.info("PurchaseType-matchEligibility-failure");
				return false;
			} 
			else if(eligibility.getContentCategory() != null 
					&& Optional.ofNullable(couponValidationRequest.getPurchaseContext().getContentCategory()).isPresent()
					&& !couponValidationRequest.getPurchaseContext().getContentCategory().isEmpty() 
					&& !(this.validateContentCategory(couponValidationRequest.getPurchaseContext().getContentCategory(),
							eligibility.getContentCategory())))

			{
				log.info("ContentCategory-matchEligibility");
				return false;
			}
			else if(eligibility.getFormat() != null 
					&& Optional.ofNullable(couponValidationRequest.getPurchaseContext().getFormat()).isPresent()
					&& !couponValidationRequest.getPurchaseContext().getFormat().isEmpty() 
					&& !(this.validateFormat(couponValidationRequest.getPurchaseContext().getFormat(),
					eligibility.getFormat())))
			{
				log.info("Format-matchEligibility");
				return false;
			}

			else if(eligibility.getGenre() != null 
					&& Optional.ofNullable(couponValidationRequest.getPurchaseContext().getGenre()).isPresent()
					&& !couponValidationRequest.getPurchaseContext().getGenre().isEmpty() 
					&& !eligibility.getGenre().equalsIgnoreCase(couponValidationRequest.getPurchaseContext().getGenre()))
			{
				log.info("Genre-matchEligibility");
				return false;
			}
			else if(eligibility.getStudio() != null 
					&& Optional.ofNullable(couponValidationRequest.getPurchaseContext().getStudio()).isPresent()
					&& !couponValidationRequest.getPurchaseContext().getStudio().isEmpty() 
					&& !eligibility.getStudio().equalsIgnoreCase(couponValidationRequest.getPurchaseContext().getStudio()))
			{
				log.info("Studio-matchEligibility");
				return false;
			}
			else if(eligibility.getNewRelease() != null
					&& Optional.ofNullable(couponValidationRequest.getPurchaseContext().getNewRelease()).isPresent()
					&& couponValidationRequest.getPurchaseContext().getNewRelease() != null 
					&& !eligibility.getNewRelease().equals(couponValidationRequest.getPurchaseContext().getNewRelease()))
			{
				log.info("newReleases-matchEligibility");
				return false;
			}
			else if(featureManagerHelper.isEnabled(Constants.SVC_EPOCH_UCC_B_ENABLED)
					&& minPurchaseAmount != null 
					&& Optional.ofNullable(couponValidationRequest.getPurchaseContext().getPurchaseAmount()).isPresent()
					&& !couponValidationRequest.getPurchaseContext().getPurchaseAmount().isEmpty() 
					&& !this.validatePurchaseAmount(minPurchaseAmount.toString() , couponValidationRequest.getPurchaseContext().getPurchaseAmount()))
			{
				log.info("purchaseAmount-matchEligibility");
				return false;
			}
			else if(null!=titleData
					&& Optional.ofNullable(couponValidationRequest.getPurchaseContext().getEventCode()).isPresent()
					&& !couponValidationRequest.getPurchaseContext().getEventCode().isEmpty()){
				Boolean result = titleValidation(titleData, couponValidationRequest.getPurchaseContext());
				if (!result) {
					return result;
				}
			}
			return true;

		}


		private boolean titleValidation(Object titleData, PurchaseContext purchaseContext) {
			if (titleData instanceof Collection) {
				List<String> eligibilityTitles = (List<String>) titleData;
				List<List<String>> matchedTmsIdList = matchedTmsIds(eligibilityTitles,purchaseContext);
				List<List<String>> matchedTmsIdListWithYear = new ArrayList<>();
				if(matchedTmsIdList.size() != 0 && !matchedTmsIdList.isEmpty()){
					for (List<String> sublist : matchedTmsIdList) {
						if(sublist.size()>6){
							matchedTmsIdListWithYear.add(sublist);
						}
					}
				}
				if(matchedTmsIdListWithYear.size() !=0) {
					return matchedTmsIdListWithYear.stream().filter(title -> isTitleMathched(title, purchaseContext)).count() > 0;
				}else{
					return matchedTmsIdList.stream().filter(title -> isTitleMathched(title, purchaseContext)).count() > 0;
				}
			} else {
				String[] titleDetails = ((String) titleData).split("\\|");
				return titleDetails[0].trim().equalsIgnoreCase(StringEscapeUtils.unescapeHtml(purchaseContext.getTitle()));
			}
		}

		private List<List<String>> matchedTmsIds(List<String> titles, PurchaseContext purchaseContext){
			List<List<String>> matchedTmsIdlist = new ArrayList<>();
			if(!titles.isEmpty() && titles.size()!=0) {
				for (String title : titles) {
					List<String> titleDetails = Arrays.asList(title.split("\\|"));
					if (titleDetails.get(0).trim().equalsIgnoreCase(purchaseContext.getTmsProgramID())) {
						matchedTmsIdlist.add(titleDetails);
					}
				}
			}
	    	return matchedTmsIdlist;
		}
	private boolean isTitleMathched(List<String> title, PurchaseContext purchaseContext) {
		boolean result = false;
		if (!title.isEmpty() && null != title) {
			if (title.size() > 0 && title.get(0).trim().equalsIgnoreCase(StringEscapeUtils.unescapeHtml(purchaseContext.getTmsProgramID()))) {
				result = true;
				if (title.size() < 4) {
					return result;
				}
					if (title.size() > 6 && null != purchaseContext.getReleaseDate()) {
						String year = purchaseContext.getReleaseDate().split("/")[2];
						if (title.get(6).trim().equalsIgnoreCase(year)) {
							result = true;
							return result;
						} else {
							log.info("Release date match failed for the request");
							result = false;
						}
					}
				 else {
					log.info("Format match failed for the request");
				}
			}
		}
		return result;
	}

		private boolean validateContentCategory(String requestContentCategory, String campaignContentCategory) {
			/*
			 * log.
			 * info("Start validateContentCategory :: Parameters are requestContentCategory [{}] , campaignContentCategory [{}] "
			 * , ESAPI.encoder().encodeForHTML(requestContentCategory),
			 * ESAPI.encoder().encodeForHTML(campaignContentCategory));
			 */
			boolean isValid = false;
			if (null != campaignContentCategory 
					&& (Constants.CONTENT_CATEGORY_YES).equalsIgnoreCase(campaignContentCategory)) {
				campaignContentCategory = Constants.CONTENT_CATEGORY_NONADULT;
			} else if (null != campaignContentCategory 
					&& (Constants.CONTENT_CATEGORY_NO).equalsIgnoreCase(campaignContentCategory)) {
				campaignContentCategory = Constants.CONTENT_CATEGORY_ADULT;
			}
			if (requestContentCategory != null && campaignContentCategory != null) {
				List<String> validContentCategory = Arrays.asList(campaignContentCategory.split(","));
				isValid = validContentCategory.stream()
						.anyMatch(contentCategory -> contentCategory.equalsIgnoreCase(requestContentCategory.trim()));
				return isValid;
			} else {
				log.error("Value of requestContentCategory or campaignContentCategory is not valid");
				return isValid;
			}
		}


		public Boolean validatePurchaseAmount(String PurchaseAmount, String PurchaseAmountToBeValidated)
		{
			//log.info("Start Validate PurchaseAmount");
			//log.info("Purchase Amount {} ",ESAPI.encoder().encodeForHTML(PurchaseAmount));
			//log.info("Purchase validated {} ",ESAPI.encoder().encodeForHTML(PurchaseAmountToBeValidated));
			Double convertedPurchaseAmount = Double.parseDouble(PurchaseAmount.trim());
			Double convertedPurchaseAmountToBeValidated = Double.parseDouble(PurchaseAmountToBeValidated.trim());
			if(convertedPurchaseAmountToBeValidated >= convertedPurchaseAmount) {
				log.info("Valid PurchaseAmount");
				return true;
			} else {
				log.info("End Validate PurchaseAmount");
			   return false;
			}
			
		}
		
		private boolean validatePurchaseType(String requestPurchaseType, String campaignPurchaseType) {
			log.info("Start validatePurchaseType :: Parameters are [{}] [{}] ", OffersUtils.sanitizeData(requestPurchaseType),
					OffersUtils.sanitizeData(campaignPurchaseType));
			boolean isValid = false;
			if (requestPurchaseType != null && campaignPurchaseType != null) {

				List<String> validPurchaseTypes = Arrays
						.asList(campaignPurchaseType.split(Constants.PURCHASE_TYPE_SEPARATOR));
				isValid = validPurchaseTypes.stream()
						.anyMatch(purchaseType -> purchaseType.equalsIgnoreCase(requestPurchaseType.trim()));
				return isValid;
			} else {
				log.error("Value of requestPurchaseType or campaignPurchaseType is not valid");
				return isValid;
			}
		}

	private boolean validateFormat(String requestFormat, String campaignFormat) {
		log.info("Start validateFormat :: Parameters are [{}] [{}] ", OffersUtils.sanitizeData(requestFormat),
				OffersUtils.sanitizeData(campaignFormat));
		boolean isValid = false;
		if (requestFormat != null && campaignFormat != null) {

			List<String> validPurchaseTypes = Arrays
					.asList(campaignFormat.split(Constants.PURCHASE_TYPE_SEPARATOR));
			isValid = validPurchaseTypes.stream()
					.anyMatch(purchaseType -> purchaseType.equalsIgnoreCase(requestFormat.trim()));
			return isValid;
		} else {
			log.error("Value of requestFormat or campaignFormat is not valid");
			return isValid;
		}
	}
}
