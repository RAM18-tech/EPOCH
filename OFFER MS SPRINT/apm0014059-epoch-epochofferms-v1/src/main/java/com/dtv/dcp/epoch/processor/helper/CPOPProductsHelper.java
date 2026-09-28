package com.dtv.dcp.epoch.processor.helper;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.CpopConstants;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.common.request.ProductRequest;
import com.dtv.dcp.epoch.model.common.request.ProductRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.product.*;
import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.OffersUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CPOPProductsHelper {
    private static final Logger log = LoggerFactory.getLogger(CPOPProductsHelper.class);
    private static final String PRODUCTS="Products::  %s";

    @Autowired
    CpopClientHelper cpopClientHelper;

    @Autowired
    FeatureManagerHelper featureManagerHelper;

    @Autowired
    OffersUtils offersUtils;

    /**
     * @param products
     * @param contractIndicator
     * @return
     */
    public List<ProductObj> filterPriceByContractIndicator(List<ProductObj> products, String contractIndicator,
    		String treatmentCode, String creditRisk) {
        //final String contractIndicatorText = contractIndicator ? Constants.CONTRACT : Constants.NONCONTRACT;

        products.stream().filter(Objects::nonNull).forEach(productObj ->
            productObj.getVariants().stream().filter(Objects::nonNull).forEach(variant -> {
                List<Price> newList = variant.getPrices().stream().filter(Objects::nonNull).filter(price ->
                        price.getContractIndicator() != null && OffersUtils.validateActiveDates(OffersUtils.getFormattedDate(price.getStartDate()), OffersUtils.getFormattedDate(price.getEndDate())) 
                        &&price.getContractIndicator().equalsIgnoreCase(contractIndicator)
                        && Objects.isNull(price.getCustomerGroup())
                        &&(
                       			(  (Objects.nonNull(price.getTreatmentCode()) && Objects.nonNull(treatmentCode) && price.getTreatmentCode().contains(treatmentCode))  ||  Objects.isNull(price.getTreatmentCode()))
                       		||
                       			(  (Objects.nonNull(price.getCreditRisk()) && Objects.nonNull(creditRisk) && price.getCreditRisk().contains(creditRisk)) || Objects.isNull(price.getCreditRisk()) )
                       	  )
                		).collect(Collectors.toList());
                variant.setPrices(newList);
            })
        );

        return products;
    }
    
	/**
	 * @param products
	 * @param contractIndicator
	 * @return
	 */
	public List<ProductObj> filterNBCDPriceByContractIndicator(List<ProductObj> products, String contractIndicator,
			String nbcd, String treatmentCode, String creditRisk , Boolean isPNP, String PnpCustomerGroup) {
		// final String contractIndicatorText = contractIndicator ? Constants.CONTRACT :
		// Constants.NONCONTRACT;
		products.stream().filter(Objects::nonNull)
				.forEach(productObj -> productObj.getVariants().stream().filter(Objects::nonNull).forEach(variant -> {
					
					if(!isPNP || PnpCustomerGroup.isEmpty() || !productObj.getProductType().getKey().equalsIgnoreCase(Constants.VIDEO_PLAN)) {
							List<Price> newList = variant.getPrices().stream().filter(Objects::nonNull)
									.filter(price -> price.getContractIndicator() != null
											&& OffersUtils.validateActiveNBCDDates(
													OffersUtils.getFormattedDate(price.getStartDate()),
													OffersUtils.getFormattedDate(price.getEndDate()), nbcd)
											&& price.getContractIndicator().equalsIgnoreCase(contractIndicator)
											&& Objects.isNull(price.getCustomerGroup())
											&&(
					                       			((Objects.nonNull(price.getTreatmentCode()) && Objects.nonNull(treatmentCode) && price.getTreatmentCode().contains(treatmentCode))  ||  Objects.isNull(price.getTreatmentCode()))
					                       		||
					                       			((Objects.nonNull(price.getCreditRisk()) && Objects.nonNull(creditRisk) && price.getCreditRisk().contains(creditRisk)) || Objects.isNull(price.getCreditRisk()))
					                       	  )
											).collect(Collectors.toList());
							variant.setPrices(newList);
					}
					if(isPNP && !PnpCustomerGroup.isEmpty() && productObj.getProductType().getKey().equalsIgnoreCase(Constants.VIDEO_PLAN)) {
						List<Price> newList = variant.getPrices().stream().filter(Objects::nonNull).filter(price ->
                        !PnpCustomerGroup.isEmpty()&& price != null && Optional.ofNullable(price.getCustomerGroup()).isPresent()
						&& price.getCustomerGroup().equalsIgnoreCase(PnpCustomerGroup) && price.getEndDate() != null &&
                        OffersUtils.validateActiveNBCDDates(OffersUtils.getFormattedDate(price.getStartDate()),
                        OffersUtils.getFormattedDate(price.getEndDate()),nbcd)
                        && price.getContractIndicator().equalsIgnoreCase(contractIndicator)).collect(Collectors.toList());
                    variant.setPrices(newList);
					}
				}));

		return products;
	}

    /**
     * Setting CompatibleProducts always, if it's Mobility it will put the MobilityProducts into compatibleProducts and vice versa.
     *
     * @param products
     * @param isEmployeeAccount 
     * @param mobility
     * @return
     */
    public List<ProductObj> filterByCompatibleProducts(List<ProductObj> products, boolean mobility, boolean isEmployeeAccount) {
        products.stream().filter(Objects::nonNull).forEach(productObj ->
            productObj.getVariants().stream().filter(Objects::nonNull).forEach(variant -> {
                if (mobility) {
                    // then remove others

                    variant.getAttributes().setCompatibleProducts(variant.getAttributes().getCompatibleMobilityProducts());
                    variant.getAttributes().setCompatibleMobilityProducts(null);
                    variant.getAttributes().setCompatibleEmployeeProducts(null);
                    variant.getAttributes().setIncludedProducts(variant.getAttributes().getIncludedMobilityProducts());
                    variant.getAttributes().setIncludedMobilityProducts(null);
                } else if(isEmployeeAccount) {
                	 variant.getAttributes().setCompatibleProducts(variant.getAttributes().getCompatibleEmployeeProducts());
                	 variant.getAttributes().setCompatibleEmployeeProducts(null);
                	 variant.getAttributes().setCompatibleMobilityProducts(null);
                     variant.getAttributes().setIncludedMobilityProducts(null);
                }else {
                    variant.getAttributes().setCompatibleMobilityProducts(null);
                    // compatibleProducts should remain unchanged
                    variant.getAttributes().setCompatibleEmployeeProducts(null);
                    variant.getAttributes().setIncludedMobilityProducts(null);
                }
            })
        );
        return products;
    }
    

    public List<ProductObj> filterByProductsObj(List<ProductObj> products, List<String> listOfProducts) throws ServiceException {
        try {
            products.stream().filter(Objects::nonNull).forEach(productObj ->
            productObj.getVariants().stream().filter(Objects::nonNull).filter(variant ->
                        listOfProducts != null && listOfProducts.contains(variant.getAttributes().getBillingProductCode())).collect(Collectors.toList())
            );

        } catch (Exception ex) {
            log.error(String.format("Error when filterByProductsObj: %s", ex.getMessage()));
            throw new ServiceException(ErrorMessages.CPOPOFFERMS_INTERNALSERVER_ERROR)
                    .addDetail(ErrorMessages.CPOPOFFERMS_INTERNALSERVER_ERROR);
        }


        return products;
    }

    /**
     * If product is in listOfProducts then it will return it
     *
     * @param products
     * @param listOfProducts
     * @return
     */
    public List<Product> filterByProducts(List<Product> products, List<String> listOfProducts) {
        List<Product> list = null;
        try {
            list = products.stream().filter(Objects::nonNull).filter(product ->
                    listOfProducts != null && product.getKey() != null && listOfProducts.contains(product.getKey())).collect(Collectors.toList());
        } catch (Exception e) {
            log.error(String.format("filterByProducts: %s", e.getMessage()));
            log.error(String.format(PRODUCTS, products));
            log.error(String.format("listOfProducts:: %s", listOfProducts));
        }

        return list;
    }

    /**
     * @param products
     * @param listOfProducts
     * @return
     */
    public List<Product> filterByProductsNotInList(List<Product> products, List<String> listOfProducts) {
        List<Product> list = null;
        try {
            if(CollectionUtils.isNotEmpty(listOfProducts)) {
                list = products.stream().filter(Objects::nonNull).filter(product ->
                        listOfProducts != null && product.getKey() != null &&  !listOfProducts.contains(product.getKey())).collect(Collectors.toList());
            }

        } catch (Exception e) {
            log.error(String.format("filterByProductsNotInList: %s", e.getMessage()));
            log.error(String.format(PRODUCTS, "%s", products));
            log.error(String.format("listOfProducts:: %s", listOfProducts));
        }

        return list;
    }
    public List<Product> filterByProductsInList(List<Product> products, List<String> listOfProducts) {
        List<Product> list = null;
        try {
            if(CollectionUtils.isNotEmpty(listOfProducts)) {
                list = products.stream().filter(Objects::nonNull).filter(product ->
                        listOfProducts != null && product.getKey() != null &&  listOfProducts.contains(product.getKey())).collect(Collectors.toList());
            }

        } catch (Exception e) {
            log.error(String.format("filterByProductsInList: %s", e.getMessage()));
            log.error(String.format(PRODUCTS, "%s", products));
            log.error(String.format("listOfProducts:: %s", listOfProducts));
        }

        return list;
    }

    public List<Product> filterProductsByCustomerSegment(List<Product> products, String customerSegment) {
        List<Product> listP = new ArrayList<>();
        try {
            products.stream().filter(Objects::nonNull).forEach(product -> {
                List<Variant> list = product.getObj().getVariants().stream().filter(Objects::nonNull).filter(variant ->
                        variant != null && variant.getAttributes().getCustomerSegments().contains(customerSegment)).collect(Collectors.toList());
                if (list != null && !list.isEmpty()) {
                    listP.add(product);
                }
            });


        } catch (Exception e) {
            log.error(String.format("filterProductsByCustomerSegment: %s",e.getMessage()));
            log.debug(OffersUtils.sanitizeData(String.format(PRODUCTS, products)));
            log.debug((String.format("customerSegment:: %s",customerSegment)));
        }

        return listP;
    }

    /**
     *
     * @param segmentDescriptions
     * @return
     */
    public List<String> getSegmentDescription(String segmentDescriptions) {
        List<String> segmentDesc = new ArrayList<>();
        if (Optional.ofNullable(segmentDescriptions).isPresent()) {
            segmentDesc = Arrays.asList(segmentDescriptions.split(Constants.PIPE));
            segmentDesc = segmentDesc.stream().map(String::trim).collect(Collectors.toList());
        }
        return segmentDesc;
    }

    /**
     *
     * @param ctOfferResponse
     * @param listHeartValue
     * @return
     */
    public CTOfferResponse filterOffersByHeartValue(CTOfferResponse ctOfferResponse, List<String> listHeartValue, Boolean retentionPromoIndicator, String coolOffPeriod, String salesChannel, String contractIndicator) {

        List<CTOffer> listOfOffers = new ArrayList<>();

        if (ctOfferResponse.getOffers() != null) {
            ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {
                List<String> segmentDescriptionFromCT = null;
                if (ctOffer.getAttributes() != null && ctOffer.getAttributes().getEligibility() != null && ctOffer.getAttributes().getEligibility().getConstraints() != null &&
                        !ctOffer.getAttributes().isMigratedOffer()) {
                    segmentDescriptionFromCT = ctOffer.getAttributes().getEligibility().getConstraints().get(0).getHeartValue();
                } else {
                    if(ctOffer.getAttributes().getBenefits() != null && !ctOffer.getAttributes().getBenefits().isEmpty())
                        segmentDescriptionFromCT = getSegmentDescription(ctOffer.getAttributes().getBenefits().get(0).getSegmentDescription());
                }

                if ((salesChannel.equalsIgnoreCase(Constants.OPUS) || salesChannel.equalsIgnoreCase(Constants.DTV360) || salesChannel.equalsIgnoreCase(Constants.UVC)) && isValidPromoBasedOnSegementDescriptionForAgent(segmentDescriptionFromCT, listHeartValue, retentionPromoIndicator) ) {
                    if (ctOffer.getAttributes().getBenefits() != null && !ctOffer.getAttributes().getBenefits().isEmpty())
                    {
                        if (ctOffer.getAttributes().getBenefits().get(0) != null &&
                                (ctOffer.getAttributes().getBenefits().get(0).isAgentOffer() || ctOffer.getAttributes().getBenefits().get(0).isIoOffer()))
                        {
                            ctOffer.getAttributes().getBenefits().get(0).setAgentOffer(true);
                            listOfOffers.add(ctOffer);
                        }
                    }
                }

				if ((OffersUtils.checkOnlineRelatedChannel(salesChannel)) && isValidPromoBasedOnSegementDescriptionForCustomer(segmentDescriptionFromCT, listHeartValue,
								retentionPromoIndicator, coolOffPeriod)) {
                    if(ctOffer.getAttributes().getBenefits() != null && !ctOffer.getAttributes().getBenefits().isEmpty()){
                        ctOffer.getAttributes().getBenefits().get(0).setAgentOffer(false);
                        boolean validCustomerClassification = false;
                        validCustomerClassification = ( ctOffer.getAttributes().getBenefits().get(0).getCustomerClassification() != null &&
                                ctOffer.getAttributes().getBenefits().get(0).getCustomerClassification().equalsIgnoreCase(Constants.EXISTING_CUSTOMER)) ? false : true;
                        if (ctOffer.getAttributes().getBenefits().get(0) != null && ctOffer.getAttributes().getBenefits().get(0).getCustomerClassification() != null
                                && validCustomerClassification) {
                            listOfOffers.add(ctOffer);
                        }
                    }
                }

            });
        }

        ctOfferResponse.setOffers(listOfOffers);
        ctOfferResponse.setTotal(listOfOffers.size());
        ctOfferResponse.setCount(listOfOffers.size());

        return ctOfferResponse;
    }

    public boolean isValidPromoBasedOnSegementDescriptionForAgent(List<String> segmentDescriptionFromCT, List<String> listHeartValue,Boolean retentionPromoIndicator) {

        if (Boolean.FALSE.equals(retentionPromoIndicator)){
            return true;
        } else if (Boolean.TRUE.equals(CollectionUtils.isNotEmpty(listHeartValue) && retentionPromoIndicator && CollectionUtils.isNotEmpty(segmentDescriptionFromCT)
                && CollectionUtils.containsAny(segmentDescriptionFromCT, listHeartValue))) {
            return true;
        }
        return false;
    }



    public boolean isValidPromoBasedOnSegementDescriptionForCustomer(List<String> segmentDescriptionFromCT, List<String> listHeartValue, Boolean retentionPromoIndicator, String coolOffPeriod) {

        if (Boolean.FALSE.equals(retentionPromoIndicator)  && !CollectionUtils.isNotEmpty(segmentDescriptionFromCT)) {
            return true;
        } else if (Boolean.TRUE.equals(CollectionUtils.isNotEmpty(listHeartValue) && retentionPromoIndicator && StringUtils.isEmpty(coolOffPeriod)
                && CollectionUtils.isNotEmpty(segmentDescriptionFromCT) && CollectionUtils.containsAny(segmentDescriptionFromCT, listHeartValue))) {
            return true;
        }
        return false;
    }

    /**
     * @param ctOfferResponse
     * @param accountType Mobility, Residential, Employee
     * @return
     */
    public CTOfferResponse filterProductsByAccountType(CTOfferResponse ctOfferResponse, String accountType) throws ServiceException {
        // Filtering by Account Type/Customer Segment ( Residential, Mobility, Employee )
        try {
            if (accountType != null) {
                ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {

                    if (ctOffer.getAttributes() != null && ctOffer.getAttributes().getAssociatedProducts() != null) {
                        ctOffer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull).forEach(associatedProduct -> {
                            if (associatedProduct.getQualifyingProducts() != null ) {
                                associatedProduct.getQualifyingProducts().stream().filter(Objects::nonNull).forEach(qualifyingProduct -> {
                                    if (ctOffer.getAttributes().isMigratedOffer()) {
                                        // This is applicable for Migrated Offers , if product is in the conflicting list then it will not return
                                        if (accountType.equalsIgnoreCase(CpopConstants.MOBILITY_WITH_CAPITAL_M) && CollectionUtils.isNotEmpty(ctOffer.getAttributes().getMobilityConflictingProducts())) {
                                            qualifyingProduct.setProducts(this.filterByProductsNotInList(qualifyingProduct.getProducts(), ctOffer.getAttributes().getMobilityConflictingProducts()));
                                        } else if (accountType.equalsIgnoreCase(CpopConstants.RESIDENTIAL) && CollectionUtils.isNotEmpty(ctOffer.getAttributes().getResidentialConflictingProducts())) {
                                            qualifyingProduct.setProducts(this.filterByProductsNotInList(qualifyingProduct.getProducts(), ctOffer.getAttributes().getResidentialConflictingProducts()));
                                        } else if (accountType.equalsIgnoreCase(CpopConstants.EMPLOYEE) && CollectionUtils.isNotEmpty(ctOffer.getAttributes().getEmployeeConflictingProducts())) {
                                            qualifyingProduct.setProducts(this.filterByProductsNotInList(qualifyingProduct.getProducts(), ctOffer.getAttributes().getEmployeeConflictingProducts()));
                                        }
                                    }

                                });
                            }
                        });
                    }
                    if(ctOffer.getAttributes() != null && CollectionUtils.isNotEmpty(ctOffer.getAttributes().getBenefits())) {
                        ctOffer.getAttributes().getBenefits().stream().filter(Objects::nonNull).forEach(benefit -> {
                            if (CollectionUtils.isNotEmpty(benefit.getApplicableProducts())) {
                                benefit.getApplicableProducts().stream().filter(Objects::nonNull).forEach(productWrapper -> {
                                    if (ctOffer.getAttributes().isMigratedOffer()) {
                                        // This is applicable for Migrated Offers , if product is in the conflicting list then it will not return
                                        if (accountType.equalsIgnoreCase(CpopConstants.MOBILITY_WITH_CAPITAL_M) && CollectionUtils.isNotEmpty(ctOffer.getAttributes().getMobilityConflictingProducts())) {
                                            productWrapper.setProducts(this.filterByProductsNotInList(productWrapper.getProducts(), ctOffer.getAttributes().getMobilityConflictingProducts()));
                                        } else if (accountType.equalsIgnoreCase(CpopConstants.RESIDENTIAL) && CollectionUtils.isNotEmpty(ctOffer.getAttributes().getResidentialConflictingProducts())) {
                                            productWrapper.setProducts(this.filterByProductsNotInList(productWrapper.getProducts(), ctOffer.getAttributes().getResidentialConflictingProducts()));
                                        } else if (accountType.equalsIgnoreCase(CpopConstants.EMPLOYEE) && CollectionUtils.isNotEmpty(ctOffer.getAttributes().getEmployeeConflictingProducts())) {
                                            productWrapper.setProducts(this.filterByProductsNotInList(productWrapper.getProducts(), ctOffer.getAttributes().getEmployeeConflictingProducts()));
                                        }
                                    }

                                });
                            }
                        });
                    }
                });
            }
        } catch (Exception ex) {
        	log.error("filterProductsByAccountType:: %s", ex.getMessage());
            throw ((new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001, ex))
                    .addDetail(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10002, "CPOPProductsHelper" + ".filterProductsByAccountType()"));
        }

        return ctOfferResponse;
    }
    public CTOfferResponse filterProductsByRetIOApplicableProducts(CTOfferResponse ctOfferResponse) throws ServiceException {
        // Filtering by Account Type/Customer Segment ( Residential, Mobility, Employee )
        try {

                ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {

                    if (ctOffer.getAttributes() != null && ctOffer.getAttributes().getAssociatedProducts() != null) {
                        ctOffer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull).forEach(associatedProduct -> {
                            if (associatedProduct.getQualifyingProducts() != null ) {
                                associatedProduct.getQualifyingProducts().stream().filter(Objects::nonNull).forEach(qualifyingProduct -> {
                                    if (ctOffer.getAttributes().isMigratedOffer()) {
                                        // This is applicable for Migrated Offers , if product is in the conflicting list then it will not return
                                        if ( CollectionUtils.isNotEmpty(ctOffer.getAttributes().getRetIoApplicableProducts())) {
                                            qualifyingProduct.setProducts(this.filterByProductsInList(qualifyingProduct.getProducts(), ctOffer.getAttributes().getRetIoApplicableProducts()));
                                        }
                                    }

                                });
                            }
                        });
                    }
                    if(ctOffer.getAttributes() != null && CollectionUtils.isNotEmpty(ctOffer.getAttributes().getBenefits())) {
                        ctOffer.getAttributes().getBenefits().stream().filter(Objects::nonNull).forEach(benefit -> {
                            if (CollectionUtils.isNotEmpty(benefit.getApplicableProducts())) {
                                benefit.getApplicableProducts().stream().filter(Objects::nonNull).forEach(productWrapper -> {
                                    if (ctOffer.getAttributes().isMigratedOffer()) {
                                        // This is applicable for Migrated Offers , if product is in the conflicting list then it will not return
                                        if ( CollectionUtils.isNotEmpty(ctOffer.getAttributes().getRetIoApplicableProducts())) {
                                            productWrapper.setProducts(this.filterByProductsInList(productWrapper.getProducts(), ctOffer.getAttributes().getRetIoApplicableProducts()));
                                        }
                                    }

                                });
                            }
                        });
                    }
                });

        } catch (Exception ex) {
            log.error("filterProductsByRetIOApplicableProducts:: %s", ex.getMessage());
            throw ((new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001, ex))
                    .addDetail(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10002, "CPOPProductsHelper" + ".filterProductsByRetIOApplicableProducts()"));
        }

        return ctOfferResponse;
    }

    /**
     * @param ctOfferResponse
     * @throws ServiceException
     */
    public CTOfferResponse filterInvalidPrices(CTOfferResponse ctOfferResponse) throws ServiceException {
        try {

            ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {

                boolean isBulkOffer = ctOffer.getAttributes().isBulkOffer();
                List<String> treatmentCode= (Objects.nonNull(ctOffer.getAttributes().getTreatmentCode()) && !ctOffer.getAttributes().getTreatmentCode().isEmpty()) ? ctOffer.getAttributes().getTreatmentCode() :null;
                List<String> creditRisk= Objects.nonNull(ctOffer.getAttributes().getCreditRisk()) && Objects.nonNull(treatmentCode) ?
                							ctOffer.getAttributes().getCreditRisk() :null;
                
                

                if (ctOffer.getAttributes() != null && ctOffer.getAttributes().getAssociatedProducts() != null) {
                    ctOffer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull).forEach(associatedProduct -> {
                        if (associatedProduct.getQualifyingProducts() != null) {
                            associatedProduct.getQualifyingProducts().stream().filter(Objects::nonNull).forEach(qualifyingProduct -> {
                            	
                                if (Objects.nonNull(qualifyingProduct.getProducts())  && !isBulkOffer) {
                                    qualifyingProduct.setProducts(removeExpiredPricesFromList(qualifyingProduct.getProducts(), ctOffer.getAttributes().getContractIndicator(),treatmentCode,creditRisk));
                                }
                                else if (Objects.nonNull(qualifyingProduct.getProducts()) && isBulkOffer) {
                                	qualifyingProduct.getProducts().stream().filter(Objects::nonNull).forEach(product ->
                                    product.getObj().getVariants().stream().filter(Objects::nonNull).forEach(variant -> {
                                        List<Price> newList = variant.getPrices().stream().filter(Objects::nonNull).filter(price ->
                                        price != null && price.getStartDate() != null && price.getEndDate()!=null && price.getContractIndicator()!=null &&
                                                        OffersUtils.validateActiveDates(OffersUtils.getFormattedDate(price.getStartDate()), OffersUtils.getFormattedDate(price.getEndDate())) 
                                                        && price.getContractIndicator().equalsIgnoreCase(Constants.BULK_PRICE)
                                                        && price.getChannel().contains(Constants.DISPLAY_PRICE)).collect(Collectors.toList());
                                        variant.setPrices(newList);
                                    })
                                    );
                                }

                            });
                        }
                        if (associatedProduct.getBundleProducts() != null) {
                            associatedProduct.getBundleProducts().stream().filter(Objects::nonNull).forEach(bundleProduct -> {
                                if (Objects.nonNull(bundleProduct.getProducts())  && !isBulkOffer) {
                                    bundleProduct.setProducts(removeExpiredPricesFromList(bundleProduct.getProducts(), ctOffer.getAttributes().getContractIndicator(),treatmentCode,creditRisk));
                                }else if (Objects.nonNull(bundleProduct.getProducts()) && isBulkOffer) {
                                	bundleProduct.getProducts().stream().filter(Objects::nonNull).forEach(product ->
                                    product.getObj().getVariants().stream().filter(Objects::nonNull).forEach(variant -> {
                                        List<Price> newList = variant.getPrices().stream().filter(Objects::nonNull).filter(price ->
                                        price != null && price.getStartDate() != null && price.getEndDate()!=null && price.getContractIndicator()!=null &&
                                                        OffersUtils.validateActiveDates(OffersUtils.getFormattedDate(price.getStartDate()), OffersUtils.getFormattedDate(price.getEndDate())) 
                                                        && price.getContractIndicator().equalsIgnoreCase(Constants.BULK_PRICE)
                                                        && price.getChannel().contains(Constants.DISPLAY_PRICE)).collect(Collectors.toList());
                                        variant.setPrices(newList);
                                    })
                                    );
                                }

                            });
                        }
                    });
                }
            });
        } catch (Exception e) {
        	log.error(String.format("removeExpiredPricesByDate:: %s", e));
        }
        return ctOfferResponse;

    }

    /**
     *
     * @param ctOfferResponse
     * @param offerRequestWrapper
     * @return
     * @throws ServiceException
     */
    public CTOfferResponse filterInvalidPrices(CTOfferResponse ctOfferResponse, OfferRequestWrapper offerRequestWrapper) throws ServiceException {
        try {
            ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {
                boolean isBulkOffer = ctOffer.getAttributes().isBulkOffer();
                List<String> treatmentCode= (Objects.nonNull(ctOffer.getAttributes().getTreatmentCode()) &&  !ctOffer.getAttributes().getTreatmentCode().isEmpty()) ? ctOffer.getAttributes().getTreatmentCode() :null;
                List<String> creditRisk= Objects.nonNull(ctOffer.getAttributes().getCreditRisk()) && Objects.nonNull(treatmentCode) ?
                        ctOffer.getAttributes().getCreditRisk() :null;
                if (ctOffer.getAttributes() != null && ctOffer.getAttributes().getAssociatedProducts() != null) {
                    ctOffer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull).forEach(associatedProduct -> {
                        if (associatedProduct.getQualifyingProducts() != null) {
                            associatedProduct.getQualifyingProducts().stream().filter(Objects::nonNull).forEach(qualifyingProduct -> {
                                if (Objects.nonNull(qualifyingProduct.getProducts()) && !isBulkOffer) {
                                    qualifyingProduct.setProducts(removeExpiredPricesFromList(qualifyingProduct.getProducts(), ctOffer.getAttributes().getContractIndicator(), offerRequestWrapper, treatmentCode, creditRisk));
                                } else if (Objects.nonNull(qualifyingProduct.getProducts()) && isBulkOffer) {
                                    qualifyingProduct.getProducts().stream().filter(Objects::nonNull).forEach(product ->
                                            product.getObj().getVariants().stream().filter(Objects::nonNull).forEach(variant -> {
                                                List<Price> newList = variant.getPrices().stream().filter(Objects::nonNull).filter(price ->
                                                        price != null && price.getStartDate() != null && price.getEndDate() != null && price.getContractIndicator() != null &&
                                                                OffersUtils.validateActiveDates(OffersUtils.getFormattedDate(price.getStartDate()), OffersUtils.getFormattedDate(price.getEndDate()))
                                                                && price.getContractIndicator().equalsIgnoreCase(Constants.BULK_PRICE)
                                                                && price.getChannel().contains(Constants.DISPLAY_PRICE)).collect(Collectors.toList());
                                                variant.setPrices(newList);
                                            })
                                    );
                                }
                            });
                        }
                        if (associatedProduct.getBundleProducts() != null) {
                            associatedProduct.getBundleProducts().stream().filter(Objects::nonNull).forEach(bundleProduct -> {
                                if (Objects.nonNull(bundleProduct.getProducts()) && !isBulkOffer) {
                                    bundleProduct.setProducts(removeExpiredPricesFromList(bundleProduct.getProducts(), ctOffer.getAttributes().getContractIndicator(), offerRequestWrapper, treatmentCode, creditRisk));
                                } else if (Objects.nonNull(bundleProduct.getProducts()) && isBulkOffer) {
                                    bundleProduct.getProducts().stream().filter(Objects::nonNull).forEach(product ->
                                            product.getObj().getVariants().stream().filter(Objects::nonNull).forEach(variant -> {
                                                List<Price> newList = variant.getPrices().stream().filter(Objects::nonNull).filter(price ->
                                                        price != null && price.getStartDate() != null && price.getEndDate() != null && price.getContractIndicator() != null &&
                                                                OffersUtils.validateActiveDates(OffersUtils.getFormattedDate(price.getStartDate()), OffersUtils.getFormattedDate(price.getEndDate()))
                                                                && price.getContractIndicator().equalsIgnoreCase(Constants.BULK_PRICE)
                                                                && price.getChannel().contains(Constants.DISPLAY_PRICE)).collect(Collectors.toList());
                                                variant.setPrices(newList);
                                            })
                                    );
                                }
                            });
                        }
                    });
                }
            });
        } catch (Exception e) {
            log.error(String.format("Exception occurred in filterInvalidPrices():: %s", e));
        }
        return ctOfferResponse;
    }

    /**
     * @param ctOfferResponse
     * @throws ServiceException
     */
    public CTOfferResponse filterInvalidContractIndicatorPrices(CTOfferResponse ctOfferResponse, OfferRequestWrapper offerRequestWrapper, Boolean isPNP, String PnpCustomerGroup) throws ServiceException {
        try {
            if (ctOfferResponse != null && Objects.nonNull(ctOfferResponse.getOffers())
					&& !ctOfferResponse.getOffers().isEmpty()) {
            	ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {
                    List<String> swimlaneSubTypes = (offerRequestWrapper != null && offerRequestWrapper.getOfferRequest() != null)
                            ? offerRequestWrapper.getOfferRequest().getSlsEligibleSubscriptionTypes() : null;
                    boolean hasExplicitSwimlaneIntent = ctOffer.getAttributes() != null
                            && CollectionUtils.isNotEmpty(ctOffer.getAttributes().getFlowIntents())
                            && ctOffer.getAttributes().getFlowIntents().stream().anyMatch(Constants.SWIMLANE::equalsIgnoreCase);
                    boolean hasExplicitFlowIntents = ctOffer.getAttributes() != null
                            && CollectionUtils.isNotEmpty(ctOffer.getAttributes().getFlowIntents());
                    boolean swimlaneBySubscriptionType = ctOffer.getAttributes() != null
                            && CollectionUtils.isNotEmpty(swimlaneSubTypes)
                            && CollectionUtils.isNotEmpty(ctOffer.getAttributes().getServiceSubscriptionType())
                            && ctOffer.getAttributes().getServiceSubscriptionType().stream()
                            .anyMatch(type -> swimlaneSubTypes.stream().anyMatch(st -> StringUtils.equalsIgnoreCase(st, type)));
                    boolean isSwimlaneSwitchEligible = (offerRequestWrapper != null) && (offerRequestWrapper.getOfferRequest() != null)
                            && Boolean.TRUE.equals(offerRequestWrapper.getOfferRequest().getSwimlaneSwitchEligible());
                    boolean isSwimlaneOffer = isSwimlaneSwitchEligible
                            && (hasExplicitSwimlaneIntent || (!hasExplicitFlowIntents && swimlaneBySubscriptionType));
                    boolean isBulkOffer = ctOffer.getAttributes() != null && ctOffer.getAttributes().isBulkOffer();
                    if (ctOffer.getAttributes() != null && ctOffer.getAttributes().getAssociatedProducts() != null) {
                        ctOffer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull).forEach(associatedProduct -> {
                            if (associatedProduct.getQualifyingProducts() != null) {
                                associatedProduct.getQualifyingProducts().stream().filter(Objects::nonNull).forEach(qualifyingProduct -> {
                                    if (Objects.nonNull(qualifyingProduct.getProducts())) {
                                    	if (isBulkOffer)
                                    	{
                                    		qualifyingProduct.getProducts().stream().filter(Objects::nonNull).forEach(product ->
                                            product.getObj().getVariants().stream().filter(Objects::nonNull).forEach(variant -> {
                                                List<Price> newList = variant.getPrices().stream().filter(Objects::nonNull).filter(price ->
                                                	price != null && price.getStartDate() != null && price.getEndDate()!=null && price.getContractIndicator()!=null &&
                                                		OffersUtils.validateActiveNBCDDates(OffersUtils.getFormattedDate(price.getStartDate()),
        					                            		OffersUtils.getFormattedDate(price.getEndDate()), offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getNextBillingDate())
                                                                && price.getContractIndicator().equalsIgnoreCase(Constants.BULK_PRICE)
                                                                && price.getChannel().contains(Constants.DISPLAY_PRICE)).collect(Collectors.toList());
                                                variant.setPrices(newList);
                                            })
                                            );
                                    	}else if(isPNP && ctOffer.getAttributes().getOfferProductTypes().contains("video-plan") && !isSwimlaneOffer){
                                    		qualifyingProduct.getProducts().stream().filter(Objects::nonNull).forEach(product ->
                                            product.getObj().getVariants().stream().filter(Objects::nonNull).forEach(variant -> {
                                                List<Price> newList = variant.getPrices().stream().filter(Objects::nonNull).filter(price ->
	                                                !PnpCustomerGroup.isEmpty()&& price != null && Optional.ofNullable(price.getCustomerGroup()).isPresent()
													&& price.getCustomerGroup().equalsIgnoreCase(PnpCustomerGroup) && price.getEndDate() != null &&
						                            OffersUtils.validateActiveNBCDDates(OffersUtils.getFormattedDate(price.getStartDate()),
						                            OffersUtils.getFormattedDate(price.getEndDate()), offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getNextBillingDate())
						                            && price.getContractIndicator().equalsIgnoreCase(ctOffer.getAttributes().getContractIndicator())).collect(Collectors.toList());
                                                variant.setPrices(newList);
                                            })
                                            );
                                    	}
                                    	else 
                                    	{
                                    		qualifyingProduct.setProducts(removeInvalidContractIndicatorPricesFromList(qualifyingProduct.getProducts(),
                                            		ctOffer.getAttributes().getContractIndicator(), offerRequestWrapper));
                                    	}
                                    }

                                });
                            }
                            if (associatedProduct.getBundleProducts() != null) {
                                associatedProduct.getBundleProducts().stream().filter(Objects::nonNull).forEach(bundleProduct -> {
                                    if (Objects.nonNull(bundleProduct.getProducts())) {
                                    	if (isBulkOffer)
                                    	{
                                    		bundleProduct.getProducts().stream().filter(Objects::nonNull).forEach(product ->
                                            product.getObj().getVariants().stream().filter(Objects::nonNull).forEach(variant -> {
                                                List<Price> newList = variant.getPrices().stream().filter(Objects::nonNull).filter(price ->
                                                	price != null && price.getStartDate() != null && price.getEndDate()!=null && price.getContractIndicator()!=null
                                                	&& OffersUtils.validateActiveNBCDDates(OffersUtils.getFormattedDate(price.getStartDate()), OffersUtils.getFormattedDate(price.getEndDate()), offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getNextBillingDate())
                                                    && price.getContractIndicator().equalsIgnoreCase(Constants.BULK_PRICE)
                                                    && price.getChannel().contains(Constants.DISPLAY_PRICE)).collect(Collectors.toList());
                                                variant.setPrices(newList);
                                            })
                                            );
                                    	}else if(isPNP && ctOffer.getAttributes().getOfferProductTypes().contains("video-plan") && !isSwimlaneOffer){
                                    		bundleProduct.getProducts().stream().filter(Objects::nonNull).forEach(product ->
                                            product.getObj().getVariants().stream().filter(Objects::nonNull).forEach(variant -> {
                                                List<Price> newList = variant.getPrices().stream().filter(Objects::nonNull).filter(price ->
	                                                !PnpCustomerGroup.isEmpty()&& price != null && Optional.ofNullable(price.getCustomerGroup()).isPresent()
													&& price.getCustomerGroup().equalsIgnoreCase(PnpCustomerGroup) && price.getEndDate() != null &&
						                            OffersUtils.validateActiveNBCDDates(OffersUtils.getFormattedDate(price.getStartDate()),
						                            OffersUtils.getFormattedDate(price.getEndDate()), offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getNextBillingDate())
						                            && price.getContractIndicator().equalsIgnoreCase(ctOffer.getAttributes().getContractIndicator())).collect(Collectors.toList());
                                                variant.setPrices(newList);
                                            })
                                            );
                                    	}
                                    	else 
                                    	{
                                    		bundleProduct.setProducts(removeInvalidContractIndicatorPricesFromList(bundleProduct.getProducts(),
                                            		ctOffer.getAttributes().getContractIndicator(), offerRequestWrapper));
                                    	}
                                    }

                                });
                            }
                        });
                    }
                });
            }
        } catch (Exception e) {
            log.error(String.format("Exception occurred in filterInvalidContractIndicatorPrices():: %s", e));
        }
        return ctOfferResponse;

    }
    /**
     * @param products
     * @param contractIndicator
     * @return
     */
    public List<Product> removeExpiredPricesFromList(List<Product> products, String contractIndicator, List<String> treatmentCode, List<String> creditRisk) {

        products.stream().filter(Objects::nonNull).forEach(product ->
        product.getObj().getVariants().stream().filter(Objects::nonNull).forEach(variant -> {
            List<Price> newList = variant.getPrices().stream().filter(Objects::nonNull).filter(price ->
            price != null && price.getStartDate() != null && price.getEndDate()!=null && price.getContractIndicator()!=null && Objects.isNull(price.getCustomerGroup())
                           && OffersUtils.validateActiveDates(OffersUtils.getFormattedDate(price.getStartDate()), OffersUtils.getFormattedDate(price.getEndDate()))
                           &&  price.getContractIndicator().equalsIgnoreCase(contractIndicator)
                           && (
                           			(  (Objects.isNull(treatmentCode) && Objects.isNull(price.getTreatmentCode()))  ||  (!treatmentCode.isEmpty() && Objects.nonNull(price.getTreatmentCode())) && !Collections.disjoint(price.getTreatmentCode(), treatmentCode)  )
                           		||
                           			(  (Objects.isNull(creditRisk) && Objects.isNull(price.getCreditRisk()))  ||  (!creditRisk.isEmpty() && Objects.nonNull(price.getCreditRisk())) &&  !Collections.disjoint(price.getCreditRisk(), creditRisk) )
                           	  )
                           ).collect(Collectors.toList());
            variant.setPrices(newList);
        })
        );
        return products;
    }

    /**
     * @param products
     * @param contractIndicator
     * @param offerRequestWrapper
     * @return
     */
    // Method to remove expired prices from a list of products
    public List<Product> removeExpiredPricesFromList(List<Product> products, String contractIndicator, OfferRequestWrapper offerRequestWrapper, List<String> treatmentCode, List<String> creditRisk) {
        boolean acapsEnabled = featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_ACAPS_ENABLED);
        String contractIndicatorOverride ;
        // SLS-IXP-FLAG changes
        if((offersUtils.isSlsGetOfferSalesEnabled() || offersUtils.isSlsGetOfferServicesEnabled()) && ((offerRequestWrapper.getOfferRequest() !=  null && "Y".equalsIgnoreCase(offerRequestWrapper.getOfferRequest().getIapSalesChannelIsPresent()))
                || (offerRequestWrapper.getOfferRequest().getCustomerContext() != null && offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt() != null && offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getIapPartnerAccountType() != null))
                && (Constants.GENRE).equalsIgnoreCase(offerRequestWrapper.getOfferRequest().getOriginalContractIndicator())){
            contractIndicatorOverride   = Constants.GENRE;
        }else{
            contractIndicatorOverride  = contractIndicator;
        }
        try {
            // Using Java 8 Stream API to process the list of products
            products.stream()
                    // Filtering out null products
                    .filter(Objects::nonNull)
                    // For each product, process its variants
                    .forEach(product -> product.getObj().getVariants().stream()
                            // Filtering out null variants
                            .filter(Objects::nonNull)
                            // For each variant, process its prices
                            .forEach(variant -> {
                                List<Price> doFeeList;
                                List<Price> nonDoFeeList;
                                // Check if the fee type of the variant is "DO_FEE"
                                if (acapsEnabled
                                        && Optional.ofNullable(variant.getAttributes().getFeeType()).isPresent()
                                        && StringUtils.isNotEmpty(variant.getAttributes().getFeeType())
                                        && variant.getAttributes().getFeeType().equalsIgnoreCase(Constants.DO_FEE)) {
                                    // If yes, filter the prices based on several conditions
                                    doFeeList = variant.getPrices().stream()
                                            .filter(price -> price != null
                                                    && price.getStartDate() != null
                                                    && price.getEndDate() != null
                                                    && price.getContractIndicator() != null
                                                    && Objects.isNull(price.getCustomerGroup())
                                                    && OffersUtils.validateActiveDates(OffersUtils.getFormattedDate(price.getStartDate()), OffersUtils.getFormattedDate(price.getEndDate()))
                                                    && price.getContractIndicator().equalsIgnoreCase(contractIndicatorOverride)
                                                    && (isValidCreditRisk(price, offerRequestWrapper) || isValidTreatmentCode(price, offerRequestWrapper)))
                                            // Collect the valid prices into a new list
                                            .collect(Collectors.toList());
                                    // Set the new list of prices to the variant
                                    variant.setPrices(doFeeList);
                                } else {
                                    // If the fee type of the variant is not "DO_FEE", filter the prices based on a different set of conditions
                                    nonDoFeeList = variant.getPrices().stream()
                                            .filter(price -> price != null
                                                    && price.getStartDate() != null
                                                    && price.getEndDate() != null
                                                    && price.getContractIndicator() != null
                                                    && Objects.isNull(price.getCustomerGroup())
                                                    && OffersUtils.validateActiveDates(OffersUtils.getFormattedDate(price.getStartDate()), OffersUtils.getFormattedDate(price.getEndDate()))
                                                    && price.getContractIndicator().equalsIgnoreCase(contractIndicatorOverride)
                                                    && (((Objects.isNull(treatmentCode) && Objects.isNull(price.getTreatmentCode())) || (!treatmentCode.isEmpty() && Objects.nonNull(price.getTreatmentCode())) && !Collections.disjoint(price.getTreatmentCode(), treatmentCode))
                                                    ||
                                                    ((Objects.isNull(creditRisk) && Objects.isNull(price.getCreditRisk())) || (!creditRisk.isEmpty() && Objects.nonNull(price.getCreditRisk())) && !Collections.disjoint(price.getCreditRisk(), creditRisk))
                                            ))
                                            // Collect the valid prices into a new list
                                            .collect(Collectors.toList());
                                    // Set the new list of prices to the variant
                                    variant.setPrices(nonDoFeeList);
                                }
                            })
                    );
            // Return the modified list of products
        }catch (Exception e){
            log.error(String.format("Exception occurred in removeExpiredPricesFromList():: %s", e));
        }
        return products;
    }

    /**
     * @param price
     * @param offerRequestWrapper
     * @return
     */
    public boolean isValidCreditRisk(Price price, OfferRequestWrapper offerRequestWrapper) {
        return (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCreditRisk()).isPresent() && Optional.ofNullable(price.getCreditRisk()).isPresent() && price.getCreditRisk().contains(offerRequestWrapper.getOfferRequest().getCreditRisk()));
    }

    /**
     * @param price
     * @param offerRequestWrapper
     * @return
     */
    public boolean isValidTreatmentCode(Price price, OfferRequestWrapper offerRequestWrapper) {
        return (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getTreatmentCode()).isPresent() && Optional.ofNullable(price.getTreatmentCode()).isPresent() && price.getTreatmentCode().contains(offerRequestWrapper.getOfferRequest().getTreatmentCode()));
    }

    // Method to remove invalid contract indicator prices from a list of products
    public List<Product> removeInvalidContractIndicatorPricesFromList(List<Product> products, String contractIndicator, OfferRequestWrapper offerRequestWrapper) {
        String contractIndicatorOverride ;
        // SLS-IXP-FLAG changes
        if((offersUtils.isSlsGetOfferSalesEnabled() || offersUtils.isSlsGetOfferServicesEnabled()) && ((offerRequestWrapper.getOfferRequest() !=  null && "Y".equalsIgnoreCase(offerRequestWrapper.getOfferRequest().getIapSalesChannelIsPresent()))
                || (offerRequestWrapper.getOfferRequest().getCustomerContext() != null && offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt() != null && offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getIapPartnerAccountType() != null))
                && (Constants.GENRE).equalsIgnoreCase(offerRequestWrapper.getOfferRequest().getOriginalContractIndicator())){
            contractIndicatorOverride   = Constants.GENRE;
        }else{
            contractIndicatorOverride  = contractIndicator;
        }
        try {
            // Using Java 8 Stream API to process the list of products
            products.stream()
                    // Filtering out null products
                    .filter(Objects::nonNull)
                    // For each product, process its variants
                    .forEach(product -> {
                        // Check if the product and its variants are not null
                        if (Objects.nonNull(product.getObj()) && Objects.nonNull(product.getObj().getVariants())) {
                            // Using Java 8 Stream API to process the variants of the product
                            product.getObj().getVariants().stream()
                                    // Filtering out null variants
                                    .filter(Objects::nonNull)
                                    // For each variant, process its prices
                                    .forEach(variant -> {
                                        List<Price> doFeeList;
                                        List<Price> nonDoFeeList;
                                        // Check if the fee type of the variant is "DO_FEE"
                                        if (featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_ACAPS_ENABLED)
                                                && Optional.ofNullable(variant.getAttributes().getFeeType()).isPresent()
                                                && StringUtils.isNotEmpty(variant.getAttributes().getFeeType())
                                                && variant.getAttributes().getFeeType().equalsIgnoreCase(Constants.DO_FEE)) {
                                            // Check if the prices of the variant are not null
                                            if (Objects.nonNull(variant.getPrices())) {
                                                // Using Java 8 Stream API to process the prices of the variant
                                                doFeeList = variant.getPrices().stream()
                                                        // Filtering out prices that meet certain conditions
                                                        .filter(price -> price != null && price.getEndDate() != null
                                                                && OffersUtils.validateActiveNBCDDates(OffersUtils.getFormattedDate(price.getStartDate()),
                                                                OffersUtils.getFormattedDate(price.getEndDate()), offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getNextBillingDate())
                                                                && price.getContractIndicator().equalsIgnoreCase(contractIndicatorOverride)
                                                                && Objects.isNull(price.getCustomerGroup())
                                                                && (isValidCreditRisk(price, offerRequestWrapper) || isValidTreatmentCode(price, offerRequestWrapper)))
                                                        // Collecting the valid prices into a new list
                                                        .collect(Collectors.toList());
                                                // Setting the new list of prices to the variant
                                                variant.setPrices(doFeeList);
                                            }
                                        } else {
                                            // If the fee type of the variant is not "DO_FEE"
                                            // Check if the prices of the variant are not null
                                            if (Objects.nonNull(variant.getPrices())) {
                                                // Using Java 8 Stream API to process the prices of the variant
                                                nonDoFeeList = variant.getPrices().stream()
                                                        // Filtering out prices that meet certain conditions
                                                        .filter(price -> price != null && price.getEndDate() != null
                                                                && OffersUtils.validateActiveNBCDDates(OffersUtils.getFormattedDate(price.getStartDate()),
                                                                OffersUtils.getFormattedDate(price.getEndDate()), offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getNextBillingDate())
                                                                && price.getContractIndicator().equalsIgnoreCase(contractIndicatorOverride)
                                                                && Objects.isNull(price.getCustomerGroup()))
                                                        // Collecting the valid prices into a new list
                                                        .collect(Collectors.toList());
                                                // Setting the new list of prices to the variant
                                                variant.setPrices(nonDoFeeList);
                                            }
                                        }
                                    });
                        }
                    });
            // Return the modified list of products
        }catch (Exception e){
            log.error(String.format("Exception occurred in removeInvalidContractIndicatorPricesFromList():: %s", e));
        }
        return products;
    }

    /**
     *
     * @param products
     */
    public void filterExpiredInstallmentOptions(List<ProductObj> products) {
        if (Optional.ofNullable(products).isPresent()) {
            products.stream().filter(Objects::nonNull).forEach(productObj -> {
                if (Objects.nonNull(productObj.getVariants())
                        && Objects.nonNull(productObj.getVariants().get(0))) {
                    Variant variant1 = productObj.getVariants().get(0);
                    if (Objects.nonNull(variant1)) {
                        List<InstallmentInfo> installmentInfo = variant1.getAttributes().getInstallmentList();
                        if (Objects.nonNull(installmentInfo)) {
                            installmentInfo = installmentInfo.stream().filter(list ->
                                    (list.getInstallmentStartDate() != null && list.getInstallmentEndDate() != null
                                            && OffersUtils.validateActiveDates(OffersUtils.getFormattedDate(list.getInstallmentStartDate()),
                                            OffersUtils.getFormattedDate(list.getInstallmentEndDate())))).collect(Collectors.toList());
                            variant1.getAttributes().setInstallmentList(installmentInfo);
                        }
                    }

                }
            });
        }
    }

    public CTProductResponse getVideoAddonProducts(ProductRequestWrapper productRequestWrapper) {
        CTProductResponse ctProductResponse = null;

        CTProductRequest ctProductRequest = new CTProductRequest();
        if (Objects.nonNull(productRequestWrapper.getCtProductRequest())) {
            BeanUtils.copyProperties(productRequestWrapper.getCtProductRequest(), ctProductRequest);
        }

        ctProductRequest.setProductTypes(Stream.of(Constants.VIDEO_ADDON).collect(Collectors.toList()));
        ctProductRequest.setPagination(productRequestWrapper.getProductRequest().getPagination());
        ctProductRequest.setContractIndicator(productRequestWrapper.getProductRequest().getContractIndicator());


        // No need to set Customer Segments as it is not defined for addon products.
        // Also removing customerContext
        ctProductRequest.setCustomerContext(null);
        ctProductRequest.setProductStatus(null);

        ctProductResponse = cpopClientHelper.getProducts (ctProductRequest);

        return ctProductResponse;

    }
    
    public CTProductResponse getProtecionPlanProducts(ProductRequestWrapper productRequestWrapper) {
        CTProductResponse ctProductResponse = null;

        CTProductRequest ctProductRequest = new CTProductRequest();
        if (Objects.nonNull(productRequestWrapper.getCtProductRequest())) {
            BeanUtils.copyProperties(productRequestWrapper.getCtProductRequest(), ctProductRequest);
        }

        ctProductRequest.setProductTypes(Stream.of(Constants.PROTECTION_PLAN).collect(Collectors.toList()));
        ctProductRequest.setPagination(productRequestWrapper.getProductRequest().getPagination());
        ctProductRequest.setContractIndicator(productRequestWrapper.getProductRequest().getContractIndicator());


        // No need to set Customer Segments as it is not defined for addon products.
        // Also removing customerContext
        ctProductRequest.setCustomerContext(null);
        ctProductRequest.setProductStatus(null);

        ctProductResponse = cpopClientHelper.getProducts (ctProductRequest);

        return ctProductResponse;

    }

    public CTProductResponse getEquipmentsProducts(ProductRequestWrapper productRequestWrapper) {
        CTProductResponse ctProductResponse = null;

        CTProductRequest ctProductRequest = new CTProductRequest();
        if (Objects.nonNull(productRequestWrapper.getCtProductRequest())) {
            BeanUtils.copyProperties(productRequestWrapper.getCtProductRequest(), ctProductRequest);
        }
        List<String> listProductTypes = new ArrayList();
        if(productRequestWrapper.getProductRequest().getProductTypes().contains(Constants.VIDEO_ACCESSORY) ){
            listProductTypes.add(Constants.VIDEO_ACCESSORY);
        }
        if(productRequestWrapper.getProductRequest().getProductTypes().contains(Constants.VIDEO_ACCESSORY) ){
            listProductTypes.add(Constants.VIDEO_DEVICE);
        }
        ctProductRequest.setProductTypes(listProductTypes);
//        ctProductRequest.setAddOnType(productRequestWrapper.getProductRequest().getAddOnType());
//        ctProductRequest.setPlanSubType(productRequestWrapper.getProductRequest().getPlanSubType());
        ctProductRequest.setPagination(productRequestWrapper.getProductRequest().getPagination());
        // ctProductRequest.setSalesChannel(productRequestWrapper.getProductRequest().getSalesChannel());
        ctProductRequest.setContractIndicator(productRequestWrapper.getProductRequest().getContractIndicator());

        // No need to set Customer Segments as it is not defined for addon products.
        // Also removing customerContext
        ctProductRequest.setCustomerContext(null);
        ctProductRequest.setProductStatus(null);
        ctProductResponse = cpopClientHelper.getProducts (ctProductRequest);

        return ctProductResponse;

    }

    public void filterRemovalRuleBySalesChannel(List<ProductObj> products, String iapAccountType, String customerSegment, ProductRequest productRequest) {
        if (Optional.ofNullable(products).isPresent()) {
            products.stream().filter(Objects::nonNull).forEach(productObj -> {
                if (Objects.nonNull(productObj.getVariants())
                        && Objects.nonNull(productObj.getVariants().get(0))) {
                    Variant variant1 = productObj.getVariants().get(0);
                    if (Objects.nonNull(variant1)) {
                        if(Objects.nonNull(variant1.getAttributes().getRemovalRuleByChannel()) && Objects.nonNull(variant1.getAttributes().getRemovalRuleByChannel().getActionRule())
                        && variant1.getAttributes().getRemovalRuleByChannel().getActionRule().size()>0) {
                            List<ActionRuleAttributes> actionRules = variant1.getAttributes().getRemovalRuleByChannel().getActionRule();
                            if (Objects.nonNull(actionRules)) {
                                actionRules = actionRules.stream().filter(rule ->
                                        (rule.getSalesChannel() != null && rule.getCustomerSegments() != null && rule.getIapPartnerAccountType() !=null
                                                && rule.getIapPartnerAccountType().equalsIgnoreCase(iapAccountType) && productRequest.getSalesChannel().contains(rule.getSalesChannel())
                                                && rule.getCustomerSegments().equalsIgnoreCase(customerSegment) && filterActionRuleForOPUS(rule, productRequest))).collect(Collectors.toList());
                                if(Objects.nonNull(actionRules) && actionRules.size() > 0) {
                                    variant1.getAttributes().getRemovalRuleByChannel().setActionRule(actionRules);
                                }else{
                                    variant1.getAttributes().setRemovalRuleByChannel(null);
                                }
                            }
                        }
                    }

                }
            });
        }
    }

	public void filterNonIAPRemovalRules(List<ProductObj> products, String accountType, String iapAccountType, ProductRequest productRequest) {
		String salesChannel = CollectionUtils.isNotEmpty(productRequest.getSalesChannel())
				&& productRequest.getSalesChannel().get(0) != null ? productRequest.getSalesChannel().get(0) : null;
		if (Optional.ofNullable(products).isPresent()) {
			products.stream().filter(Objects::nonNull).forEach(productObj -> {
				if (CollectionUtils.isNotEmpty(productObj.getVariants())
						&& Objects.nonNull(productObj.getVariants().get(0))) {
					Variant variant1 = productObj.getVariants().get(0);
					if (Objects.nonNull(variant1)) {
						if (Objects.nonNull(variant1.getAttributes().getRemovalRuleByChannel())
								&& Objects.nonNull(variant1.getAttributes().getRemovalRuleByChannel().getActionRule())
								&& CollectionUtils.isNotEmpty(
										variant1.getAttributes().getRemovalRuleByChannel().getActionRule())) {
							List<ActionRuleAttributes> actionRules = variant1.getAttributes().getRemovalRuleByChannel()
									.getActionRule();
							if (CollectionUtils.isNotEmpty(actionRules)) {
								if (accountType.equalsIgnoreCase(Constants.EMPLOYEE)) {
									List<ActionRuleAttributes> actionRulesWithAccType = actionRules.stream()
											.filter(rule -> rule.getCustomerSegments().equalsIgnoreCase(accountType))
											.collect(Collectors.toList());
									if (CollectionUtils.isNotEmpty(actionRulesWithAccType)) {
										variant1.getAttributes().getRemovalRuleByChannel().setActionRule(actionRulesWithAccType);
									} else {
										variant1.getAttributes().setRemovalRuleByChannel(null);
									}
								} else if (!accountType.equalsIgnoreCase(Constants.EMPLOYEE)) {
									List<ActionRuleAttributes> actionRulesWithNoIAPAndAccStatus = actionRules.stream()
											.filter(rule -> rule.getSalesChannel().equalsIgnoreCase(salesChannel))
											.filter(rule -> CollectionUtils
													.isEmpty(rule.getIneligibleAccountStatuses()))
											.filter(rule -> StringUtils.isEmpty(rule.getIapPartnerAccountType()))
											.filter(rule -> filterActionRuleForOPUS(rule, productRequest))
											.collect(Collectors.toList());
									if (CollectionUtils.isNotEmpty(actionRulesWithNoIAPAndAccStatus)) {
										variant1.getAttributes().getRemovalRuleByChannel().setActionRule(actionRulesWithNoIAPAndAccStatus);
									} else {
										variant1.getAttributes().setRemovalRuleByChannel(null);
									}
								} else if (iapAccountType == null) {
									variant1.getAttributes().setRemovalRuleByChannel(null);
								}
							}
						}
					}

				}
			});
		}
	}
	
	public void filterActionRuleByIneligibleAccountStatuses(List<ProductObj> products, ProductRequest productRequest,
			String iapPartnerAccountType, String accountType, boolean checkIapPartnerAccountTypeExist,
			boolean checkAccountTypeEmployeeExist,boolean pendingSwimlaneSwitch) {
		if (Optional.ofNullable(products).isPresent()) {
			products.stream().filter(Objects::nonNull).forEach(productObj -> {
				if (Objects.nonNull(productObj.getVariants()) && Objects.nonNull(productObj.getVariants().get(0))) {
					Variant variant1 = productObj.getVariants().get(0);
					if (Objects.nonNull(variant1)) {
						if (Objects.nonNull(variant1.getAttributes().getRemovalRuleByChannel())
								&& Objects.nonNull(variant1.getAttributes().getRemovalRuleByChannel().getActionRule())
								&& variant1.getAttributes().getRemovalRuleByChannel().getActionRule().size() > 0) {
							List<ActionRuleAttributes> actionRules = variant1.getAttributes().getRemovalRuleByChannel()
									.getActionRule();
							List<ActionRuleAttributes> filteredActionRuleAttributes = new ArrayList<>();
							if (Objects.nonNull(actionRules)) {
								Map<String, List<ActionRuleAttributes>> ActionRuleAttributesMapBySalesChannel = actionRules
										.stream().filter(Objects::nonNull)
										.collect(Collectors.groupingBy(ActionRuleAttributes::getSalesChannel));
								if (Objects.nonNull(ActionRuleAttributesMapBySalesChannel)
										&& ActionRuleAttributesMapBySalesChannel.size() > 0) {
									ActionRuleAttributesMapBySalesChannel.entrySet().stream().filter(Objects::nonNull)
											.forEach(actionRule -> {
												List<ActionRuleAttributes> actionRuleAttributesList = actionRule
														.getValue();
												// check anyone of the actionRuleAttributesList elements having
												// getIneligibleAccountStatuses
												boolean checkIneligibleAccountStatusesExist = actionRuleAttributesList
														.stream().filter(Objects::nonNull)
														.anyMatch(rule -> (Objects
																.nonNull(rule.getIneligibleAccountStatuses())
																&& rule.getIneligibleAccountStatuses().size() > 0));
												if (checkIneligibleAccountStatusesExist) {
													// one of the actionRuleAttributesList elements having
													// getIneligibleAccountStatuses
													actionRuleAttributesList.forEach(rule -> {
														if (Objects.nonNull(rule.getIneligibleAccountStatuses())
																&& rule.getIneligibleAccountStatuses().size() > 0) {
															// add only actionRule having ineligibleAccountStatuses
                                                           if(rule.getIneligibleAccountStatuses().stream()
                                                                    .anyMatch(status -> status.equalsIgnoreCase("Pending Swimlane")) && pendingSwimlaneSwitch) {
                                                               filteredActionRuleAttributes.add(rule);
                                                           } else if(!pendingSwimlaneSwitch)
                                                           {
                                                               filteredActionRuleAttributes.add(rule);
                                                           }
														}
													});
												} else {
													// add other actionRule's not having ineligibleAccountStatuses
													filteredActionRuleAttributes.addAll(actionRuleAttributesList);
												}

											});
									if (!ObjectUtils.isEmpty(filteredActionRuleAttributes)
											&& filteredActionRuleAttributes.size() > 0) {
										List<ActionRuleAttributes> filter1 = new ArrayList<>();
										List<ActionRuleAttributes> filter2 = new ArrayList<>();
										List<ActionRuleAttributes> filter3 = new ArrayList<>();
										List<ActionRuleAttributes> filterActionRuleAttributesByIapPartnerAccountType = new ArrayList<>();
										List<ActionRuleAttributes> filterActionRuleAttributesByEmpAccountType = new ArrayList<>();
										List<ActionRuleAttributes> filterActionRuleAttributesByAccountType = new ArrayList<>();

										// filter by IapPartnerAccountType
										if (checkIapPartnerAccountTypeExist && !checkAccountTypeEmployeeExist) {
											boolean checkIneligibleAccountStatusesForIapPartner = filteredActionRuleAttributes
													.stream().filter(Objects::nonNull)
													.anyMatch(rule -> (!StringUtils.isEmpty(rule.getSalesChannel())
															&& CollectionUtils.isNotEmpty(productRequest.getSalesChannel())
															&& productRequest.getSalesChannel().size() > 0
															&& productRequest.getSalesChannel().contains(rule.getSalesChannel())
															&& !StringUtils.isEmpty(rule.getCustomerSegments())
															&& !StringUtils.isEmpty(accountType)
															&& rule.getCustomerSegments().equalsIgnoreCase(accountType)
															&& Objects.nonNull(rule.getIneligibleAccountStatuses())
															&& rule.getIneligibleAccountStatuses().size() > 0));
											if (checkIneligibleAccountStatusesForIapPartner) {
												filteredActionRuleAttributes.stream().filter(Objects::nonNull)
														.forEach(rule -> {
															if (!StringUtils.isEmpty(rule.getSalesChannel())
																	&& CollectionUtils.isNotEmpty(productRequest.getSalesChannel())
																	&& productRequest.getSalesChannel().size() > 0
																	&& productRequest.getSalesChannel().contains(rule.getSalesChannel())
																	&& !StringUtils.isEmpty(rule.getCustomerSegments())
																	&& !StringUtils.isEmpty(accountType)
																	&& rule.getCustomerSegments()
																			.equalsIgnoreCase(accountType)
																	&& (Objects.nonNull(
																			rule.getIneligibleAccountStatuses())
																			&& rule.getIneligibleAccountStatuses()
																					.size() > 0)
																	&& filterActionRuleForOPUS(rule, productRequest)) {
																filterActionRuleAttributesByIapPartnerAccountType
																		.add(rule);
															}
														});
											} else {
												filteredActionRuleAttributes.stream().filter(Objects::nonNull)
														.forEach(rule -> {
															if (!StringUtils.isEmpty(rule.getSalesChannel())
																	&& CollectionUtils.isNotEmpty(productRequest.getSalesChannel())
																	&& productRequest.getSalesChannel().size() > 0
																	&& productRequest.getSalesChannel().contains(rule.getSalesChannel())
																	&& !StringUtils.isEmpty(rule.getCustomerSegments())
																	&& !StringUtils.isEmpty(accountType)
																	&& rule.getCustomerSegments()
																			.equalsIgnoreCase(accountType)
																	&& Objects
																			.isNull(rule.getIneligibleAccountStatuses())
																	&& !StringUtils
																			.isEmpty(rule.getIapPartnerAccountType())
																	&& !StringUtils.isEmpty(iapPartnerAccountType)
																	&& rule.getIapPartnerAccountType()
																			.equalsIgnoreCase(iapPartnerAccountType)
																	&& filterActionRuleForOPUS(rule, productRequest)) {
																filterActionRuleAttributesByIapPartnerAccountType
																		.add(rule);
															}
														});
											}
											filter1.addAll(filterActionRuleAttributesByIapPartnerAccountType);
										}

										// filter by AccountType
										if (!checkIapPartnerAccountTypeExist && checkAccountTypeEmployeeExist) {
											boolean checkIneligibleAccountStatusesForAccountType = filteredActionRuleAttributes
													.stream().filter(Objects::nonNull)
													.anyMatch(rule -> ((Objects
															.nonNull(rule.getIneligibleAccountStatuses())
															&& rule.getIneligibleAccountStatuses().size() > 0)));
											if (checkIneligibleAccountStatusesForAccountType) {
												filteredActionRuleAttributes.stream().filter(Objects::nonNull)
														.forEach(rule -> {
															if (!StringUtils.isEmpty(rule.getSalesChannel())
																	&& CollectionUtils.isNotEmpty(productRequest.getSalesChannel())
																	&& productRequest.getSalesChannel().size() > 0
																	&& productRequest.getSalesChannel().contains(rule.getSalesChannel())
																	&& (Objects.nonNull(
																			rule.getIneligibleAccountStatuses())
																			&& rule.getIneligibleAccountStatuses()
																					.size() > 0)
																	&& filterActionRuleForOPUS(rule, productRequest)) {
																filterActionRuleAttributesByEmpAccountType.add(rule);
															}
														});
											} else {
												filteredActionRuleAttributes.stream().filter(Objects::nonNull)
														.forEach(rule -> {
															if (Objects.isNull(rule.getIneligibleAccountStatuses())
																	&& !StringUtils.isEmpty(rule.getCustomerSegments())
																	&& rule.getCustomerSegments()
																			.equalsIgnoreCase(Constants.EMPLOYEE)) {
																filterActionRuleAttributesByEmpAccountType.add(rule);
															}
														});
											}
											filter2.addAll(filterActionRuleAttributesByEmpAccountType);
										}

										if (!checkIapPartnerAccountTypeExist && !checkAccountTypeEmployeeExist) {
											filteredActionRuleAttributes.stream().filter(Objects::nonNull)
													.forEach(rule -> {
														if (!StringUtils.isEmpty(rule.getCustomerSegments())
																&& rule.getCustomerSegments()
																		.equalsIgnoreCase(accountType)
																&& rule.getCustomerSegments()
																		.equalsIgnoreCase(Constants.RESIDENTIAL)
																&& Objects.nonNull(rule.getIneligibleAccountStatuses())
																&& rule.getIneligibleAccountStatuses().size() > 0
																&& !StringUtils.isEmpty(rule.getSalesChannel())
																&& CollectionUtils.isNotEmpty(productRequest.getSalesChannel())
																&& productRequest.getSalesChannel().size() > 0
																&& productRequest.getSalesChannel().contains(rule.getSalesChannel())
																&& filterActionRuleForOPUS(rule, productRequest)) {
															// check accountType is Residential
															filterActionRuleAttributesByAccountType.add(rule);
														} else if (!StringUtils.isEmpty(rule.getCustomerSegments())
																&& rule.getCustomerSegments()
																		.equalsIgnoreCase(accountType)
																&& rule.getCustomerSegments()
																		.equalsIgnoreCase(Constants.EMPLOYEE)) {
															// check accountType is Employee
															filterActionRuleAttributesByAccountType.add(rule);
														}
													});
											filter3.addAll(filterActionRuleAttributesByAccountType);
										}

										if (checkIapPartnerAccountTypeExist) {
											if ((Objects.nonNull(filter1) && filter1.size() > 0)) {
												variant1.getAttributes().getRemovalRuleByChannel()
														.setActionRule(filter1);
											} else {
												variant1.getAttributes().setRemovalRuleByChannel(null);
											}
										} else if (checkAccountTypeEmployeeExist) {
											if ((Objects.nonNull(filter2) && filter2.size() > 0)) {
												variant1.getAttributes().getRemovalRuleByChannel()
														.setActionRule(filter2);
											} else {
												variant1.getAttributes().setRemovalRuleByChannel(null);
											}
										} else if (!checkIapPartnerAccountTypeExist && !checkAccountTypeEmployeeExist) {
											if ((Objects.nonNull(filter3) && filter3.size() > 0)) {
												variant1.getAttributes().getRemovalRuleByChannel()
														.setActionRule(filter3);
											} else {
												variant1.getAttributes().setRemovalRuleByChannel(null);
											}
										}

									} else {
										variant1.getAttributes().setRemovalRuleByChannel(null);
									}
								}
							}
						}else {
							variant1.getAttributes().setRemovalRuleByChannel(null);
						}
					}

				}
			});
		}
	}
	
	public boolean filterActionRuleForOPUS(ActionRuleAttributes actionRuleAttributes, ProductRequest prdReq) {
		if (StringUtils.equalsIgnoreCase(actionRuleAttributes.getSalesChannel(), Constants.OPUS)
				&& prdReq.getSalesChannel().contains(Constants.OPUS) && Objects.nonNull(prdReq.getChannelEligibility())) {
			if (StringUtils.isNotEmpty(prdReq.getChannelEligibility().getOpusStoreId())
					&& CollectionUtils.isNotEmpty(actionRuleAttributes.getOpusStoreIds())) {
				return actionRuleAttributes.getOpusStoreIds().stream().filter(Objects::nonNull)
						.anyMatch(c -> c.contains(prdReq.getChannelEligibility().getOpusStoreId()));
			} else if (StringUtils.isNotEmpty(prdReq.getChannelEligibility().getOpusSubChannel())
					&& CollectionUtils.isNotEmpty(actionRuleAttributes.getOpusSubChannels())) {
				return actionRuleAttributes.getOpusSubChannels().stream().filter(Objects::nonNull)
						.anyMatch(c -> c.contains(prdReq.getChannelEligibility().getOpusSubChannel()));
			} else if (StringUtils.isNotEmpty(prdReq.getChannelEligibility().getOpusChannel())
					&& CollectionUtils.isNotEmpty(actionRuleAttributes.getOpusChannels())) {
				return actionRuleAttributes.getOpusChannels().stream().filter(Objects::nonNull)
						.anyMatch(c -> c.contains(prdReq.getChannelEligibility().getOpusChannel()));
			}
		}
		return true;
	}

    public Map<String, List<String>> priceTierMapFromRequest(ProductRequest productRequest) {
        Map<String, List<String>> priceTierMap = new HashMap<>();
        if (Objects.isNull(productRequest.getCustomerContext())
                || Objects.isNull(productRequest.getCustomerContext().getOtt())
                || Objects.isNull(productRequest.getCustomerContext().getOtt().getProducts())) {
            return priceTierMap;
        }
        productRequest.getCustomerContext().getOtt().getProducts().forEach(productInfo -> {
            String priceTier = productInfo.getPriceTier();
            if (priceTier == null) {
                return; // Skip if no price tier
            }
            String productCode = productInfo.getProductCode();
            if (priceTierMap.containsKey(productCode)) {
                List<String> existingTiers = priceTierMap.get(productCode);
                if (!existingTiers.contains(priceTier)) {
                    existingTiers.add(priceTier);
                    priceTierMap.put(productCode, existingTiers);
                }
            } else {
                priceTierMap.put(productCode, new ArrayList<>(List.of(priceTier)));
            }
        });
        return priceTierMap;
    }

    public Set<String> getProductCodeWithNoPriceTier(ProductRequest productRequest) {
        Set<String> productCodes = new HashSet<>();
        if (Objects.isNull(productRequest.getCustomerContext())
                || Objects.isNull(productRequest.getCustomerContext().getOtt())
                || Objects.isNull(productRequest.getCustomerContext().getOtt().getProducts())) {
            return productCodes;
        }
        productRequest.getCustomerContext().getOtt().getProducts().forEach(productInfo -> {
            String priceTier = productInfo.getPriceTier();
            if (priceTier == null) {
                productCodes.add(productInfo.getProductCode());
            }
        });
        return productCodes;
    }

    public void filterPricesByPriceTier(List<ProductObj>  products, ProductRequest productsRequest) {
        if (CollectionUtils.isNotEmpty(products)) {
            Map<String, List<String>> productToPriceTierMap = priceTierMapFromRequest(productsRequest);
            Set<String> productCodesWithNoPriceTier = getProductCodeWithNoPriceTier(productsRequest);
            products.stream().filter(Objects::nonNull).forEach(productObj -> {
                if (Optional.ofNullable(productObj.getVariants()).isPresent() && !productObj.getVariants().isEmpty()) {
                    if (Optional.ofNullable(productObj.getVariants().get(0).getPrices()).isPresent() && !productObj.getVariants().get(0).getPrices().isEmpty() && productObj.getVariants().get(0).getPrices().size() > 1) {
                        List<String> priceTier;
                        if (productToPriceTierMap.containsKey(productObj.getCode())) {
                            priceTier = productToPriceTierMap.get(productObj.getCode());
                        } else {
                            priceTier = null;
                        }
                        List<Price> priceWithPriceTier = productObj.getVariants().get(0).getPrices().stream().filter(Objects::nonNull).filter(price -> price.getPriceTier() != null).collect(Collectors.toList());
                        List<Price> pricesWithOutPriceTier = productObj.getVariants().get(0).getPrices().stream().filter(Objects::nonNull).filter(price -> price.getPriceTier() == null).collect(Collectors.toList());
                        if (priceWithPriceTier.size() > 0 && priceTier != null && priceWithPriceTier.stream().anyMatch(price -> price.getPriceTier().stream().anyMatch(priceTier::contains))) {
                            List<String> newPriceTier = new ArrayList<>();
                            newPriceTier.addAll(priceTier);
                            priceWithPriceTier = priceWithPriceTier.stream().filter(price -> price.getPriceTier().stream().anyMatch(priceTier::contains)).collect(Collectors.toList());
                            List<String> filteredPriceTiers = priceWithPriceTier.stream().filter(Objects::nonNull).map(Price::getPriceTier).filter(Objects::nonNull).flatMap(List::stream).distinct().collect(Collectors.toList());
                            priceWithPriceTier = priceWithPriceTier.stream()
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.collectingAndThen(
                                            Collectors.toMap(
                                                    price -> price.getValue().getCentAmount(),
                                                    price -> price,
                                                    (price1, price2) -> price1 // In case of duplicates, keep the first one
                                            ),
                                            map -> new ArrayList<>(map.values())
                                    ));
/*                            if (priceWithPriceTier.size() > 0 && filteredPriceTiers.size() > 1) {
                                priceWithPriceTier.stream().forEach(price -> price.setPriceTier(filteredPriceTiers));
                            }*/
                            productObj.getVariants().get(0).setPrices(priceWithPriceTier);
                        } else {
                            priceWithPriceTier.clear();
                        }
                        if (priceWithPriceTier.isEmpty()) {
                            productObj.getVariants().get(0).setPrices(pricesWithOutPriceTier);
                        } else if (productCodesWithNoPriceTier.contains(productObj.getCode())) {
                            priceWithPriceTier.addAll(pricesWithOutPriceTier);
                            productObj.getVariants().get(0).setPrices(priceWithPriceTier);
                        }
                    }
                }
            });
        }
    }

    /**
     * Applies attribute pricing criteria and conflicting priceTier resolution
     * to every variant in the product list, using the criteria context built from the ProductRequest.
     *
     *
     * @param products       the product list to process (already date/contract-filtered)
     * @param productRequest the incoming product request carrying criteria values
     */
    public void applyAttributePricingCriteriaToProducts(List<ProductObj> products, ProductRequest productRequest) {
        if (CollectionUtils.isEmpty(products) || productRequest == null) return;

        Map<String, String> criteriaContext = offersUtils.buildProductPricingCriteriaContext(productRequest);
        log.debug("PriceTier Framework: getProducts criteriaContext={}", criteriaContext);

        products.stream().filter(Objects::nonNull).forEach(productObj -> {
            if (Optional.ofNullable(productObj.getVariants()).isPresent() && !productObj.getVariants().isEmpty()) {
                productObj.getVariants().stream().filter(Objects::nonNull).forEach(variant -> {
                    List<Price> prices = variant.getPrices();
                    if (CollectionUtils.isEmpty(prices)) return;
                    List<Price> selected = offersUtils.evaluateAndSelectPrices(prices, criteriaContext);
                    log.debug("PriceTier Framework: getProducts product [{}] prices before={} after={}",
                            productObj.getCode(), prices.size(), selected != null ? selected.size() : 0);
                    variant.setPrices(selected);
                });
            }
        });
    }
}

