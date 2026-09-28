package com.dtv.dcp.epoch.processor.ott.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.common.request.ProductInfo;
import com.dtv.dcp.epoch.model.common.request.ReplacementDevices;
import com.dtv.dcp.epoch.model.ct.benefit.Benefit;
import com.dtv.dcp.epoch.model.ct.generic.GenericTypeIdBase;
import com.dtv.dcp.epoch.model.ct.offer.AssociatedProduct;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.offer.OfferPrice;
import com.dtv.dcp.epoch.model.ct.product.Constraints;
import com.dtv.dcp.epoch.model.ct.product.Product;
import com.dtv.dcp.epoch.model.ct.product.ProductWrapper;
import com.dtv.dcp.epoch.model.ct.request.CTCustomerContext;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.helper.CPOPProductsHelper;
import com.dtv.dcp.epoch.processor.ott.OttCTOffersProcessor;
import com.dtv.dcp.epoch.processor.ott.OttMobilityOffersProcessor;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


@Component
public class OttServicesOffersProcessorHelper {
	
	@Autowired
	OffersUtils offersUtils;

	private static final Logger log = LoggerFactory.getLogger(OttServicesOffersProcessorHelper.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT);

	/** The ott mobility offers processor. */
	@Autowired
	OttMobilityOffersProcessor ottMobilityOffersProcessor;
	
	@Autowired
	OttCTOffersProcessor ottCTOffersProcessor;

	@Autowired
	CPOPProductsHelper cpopProductsHelper;
	
	
	/**
	 * Find accountType.
	 *
	 * @param customer segment
	 *            the customer segment
	 * @return true, if successful
	 */
	public String findAccountType(String customersegment) {

		String accountType = null;
		if (customersegment != null && !customersegment.isEmpty()) {
            if (customersegment.toLowerCase().contains((Constants.MOBILITY).toLowerCase())) {
				accountType = Constants.MOBILITY;
			} else if (customersegment.contains(Constants.RESIDENTIAL_ACCOUNT_TYPE)) {
				accountType = Constants.RESIDENTIAL_ACCOUNT_TYPE;

			} else if (customersegment.contains(Constants.EMPLOYEE)) {
				accountType = Constants.EMPLOYEE;
			}else if (customersegment.contains(Constants.DEMO)) {
				accountType = Constants.DEMO;
			}else if (customersegment.contains(Constants.DECA)) {
				accountType = Constants.DECA;
			}else if (customersegment.contains(Constants.SHOWROOM)) {
				accountType = Constants.SHOWROOM;
			}else if (customersegment.contains(Constants.COURTESY)) {
				accountType = Constants.COURTESY;
			}else if (customersegment.contains(Constants.BCOMP)) {
				accountType = Constants.BCOMP;
			}else if (customersegment.contains(Constants.MDUTENANT)) {
				accountType = Constants.MDUTENANT;
			}
		}else{
			throw new ServiceException(ErrorMessages.CTLG_DTVN_OFFER_NOT_EXISTS).addDetail(ErrorMessages.CTLG_DTVN_OFFER_NOT_EXISTS_DETAILS001);
		}
		return accountType;
	}
	
	/**
	 * Find sales channel.
	 *
	 * @param salesChannel
	 *            the sales channel
	 * @return true, if successful
	 */
	public String findSalesChannel(List<String> salesChannel,String offerActionType) {
		
	String dtvnowSalesChannel=null;
	if (salesChannel != null && !salesChannel.isEmpty() ){
		if (salesChannel.stream().anyMatch(s -> OffersUtils.checkOnlineRelatedChannel(s)) && (salesChannel.contains(Constants.OPUS) || salesChannel.contains(Constants.ASSISTED_SALES)) ){
			dtvnowSalesChannel = Constants.ALL;
		} else if (salesChannel.stream().anyMatch(s -> OffersUtils.checkOnlineRelatedChannel(s)) &&  offerActionType != null && !offerActionType.equalsIgnoreCase(Constants.ACQUISITION)) {
			dtvnowSalesChannel = Constants.SELF_SERVICE_EXISTING;

		} else if ((salesChannel.contains(Constants.OPUS) || salesChannel.contains(Constants.ASSISTED_SALES)) && offerActionType != null && !offerActionType.equalsIgnoreCase(Constants.ACQUISITION)) {
			dtvnowSalesChannel = Constants.AGENT_EXISTING;
		}else if (salesChannel.stream().anyMatch(s -> OffersUtils.checkOnlineRelatedChannel(s)) && offerActionType != null && offerActionType.equalsIgnoreCase(Constants.ACQUISITION)) {
			dtvnowSalesChannel = Constants.SELF_SERVICE_NEW;
		}
		else if ((salesChannel.contains(Constants.OPUS)|| salesChannel.contains(Constants.ASSISTED_SALES)) && offerActionType != null && offerActionType.equalsIgnoreCase(Constants.ACQUISITION)) {
			dtvnowSalesChannel = Constants.AGENT_NEW;
		}
	}
		return dtvnowSalesChannel;
	}
	
	/**
	 * Checks if is sales channel valid for customer.
	 *
	 * @param salesChannel the sales channel
	 * @return boolean
	 */
	public boolean isSalesChannelValidForCustomer(String salesChannel) {
		List<String> salesChannelForCustomer = salesChannel != null ? Arrays.asList(salesChannel.split(Constants.PIPE))
				: new ArrayList<>();
		return salesChannelForCustomer.stream()
				.anyMatch(s -> s.equalsIgnoreCase(Constants.SELF_SERVICE_EXISTING) || s.equalsIgnoreCase(Constants.SALES_CHANNEL_ALL));
	}
	
	/**
	 * Checks if is sales channel valid for agent.
	 *
	 * @param salesChannel the sales channel
	 * @return boolean
	 */
	public boolean isSalesChannelValidForAgent(String salesChannel) {
		List<String> salesChannelForAgent = salesChannel != null ? Arrays.asList(salesChannel.split(Constants.PIPE))
				: new ArrayList<>();
		return salesChannelForAgent.stream()
				.anyMatch(s -> s.equalsIgnoreCase(Constants.AGENT_EXISTING) || s.equalsIgnoreCase(Constants.SALES_CHANNEL_ALL));
	}	
	
	
	
	/**
	 * @param offerList
	 */
	public void calculateBestPrice(List<CTOffer> offerList) {

		offerList.stream().filter(Objects::nonNull).forEach(offer -> {

			OfferPrice offerPrice = new OfferPrice();

			List<String> treatmentCode= Objects.nonNull(offer.getAttributes().getTreatmentCode()) ? offer.getAttributes().getTreatmentCode() :null;
            List<String> creditRisk= Objects.nonNull(offer.getAttributes().getCreditRisk()) && Objects.nonNull(treatmentCode) ?
            							offer.getAttributes().getCreditRisk() :null;
            
			
			// Fetch the baseprice and set the offerprice at offer level with the base price. If there is no benfit to subtract, this is the best price 
			List<ProductWrapper> bundleProducts = null;
			AssociatedProduct associatedProduct = offer.getAttributes().getAssociatedProducts().get(0);
			if (Optional.ofNullable(associatedProduct).isPresent()) {
				bundleProducts = associatedProduct.getBundleProducts();
				if (Optional.ofNullable(bundleProducts).isPresent() && !bundleProducts.isEmpty()) {
					bundleProducts.stream().filter(Objects::nonNull).forEach(bundleProduct -> {
						List<Product> products = bundleProduct.getProducts();
						products = cpopProductsHelper.removeExpiredPricesFromList(products, offer.getAttributes().getContractIndicator(),treatmentCode,creditRisk);
						if (Optional.ofNullable(products).isPresent() && !products.isEmpty()) {
							products.stream().filter(Objects::nonNull).forEach(prod -> {
								if (Optional.ofNullable(prod).isPresent() && Optional.ofNullable(prod.getObj()).isPresent()) {
									if (Optional.ofNullable(prod.getObj().getVariants()).isPresent() && !prod.getObj().getVariants().isEmpty()) {
										if (Optional.ofNullable(prod.getObj().getVariants().get(0).getPrices()).isPresent()
												&& !prod.getObj().getVariants().get(0).getPrices().isEmpty()) {
											prod.getObj().getVariants().get(0).getPrices().stream().filter(Objects::nonNull).forEach(price -> {
												if (Optional.ofNullable(price.getValue()).isPresent() && Optional.ofNullable(price.getValue().getDollarAmount()).isPresent()) {
													offerPrice.setDollarAmount(offerPrice.getDollarAmount() + price.getValue().getDollarAmount());
												}
											});
										}
									}
								}
							});
						}
					});
				}
			}

			//Now if there is a relevant benefit value, subtract it from the base price to arrive at BEST PRICE!
			offer.getAttributes().getBenefits().stream().filter(Objects::nonNull).forEach(benefit -> {
				if (Optional.ofNullable(benefit).isPresent() && Optional.ofNullable(benefit.getValue()).isPresent()
						&& Optional.ofNullable(benefit.getBenefitType()).isPresent()
						&& benefit.getBenefitType().equalsIgnoreCase("flat-off")
						&& !benefit.isIoOffer()
						&& !(benefit.isAgentOffer())) {
					offerPrice.setDollarAmount(offerPrice.getDollarAmount() - benefit.getValue().getDollarAmount());
				} else if (Optional.ofNullable(benefit).isPresent()
						&& Optional.ofNullable(benefit.getValue()).isPresent()
						&& Optional.ofNullable(benefit.getBenefitType()).isPresent()
						&& benefit.getBenefitType().equalsIgnoreCase("percent-off")
						&& benefit.getValue().getPercentage() != null && benefit.getValue().getPercentage() != 0
						&& !benefit.isIoOffer() && !(benefit.isAgentOffer())) {
					Double percentageOff = ((offerPrice.getDollarAmount())* (Double.valueOf(benefit.getValue().getPercentage()) / 100));
					offerPrice.setDollarAmount(offerPrice.getDollarAmount() - percentageOff);
				}
			});
			BigDecimal bd = new BigDecimal(Double.toString(offerPrice.getDollarAmount()));
			bd = bd.setScale(2, RoundingMode.HALF_UP);
			offerPrice.setDollarAmount(bd.doubleValue());
			offer.getAttributes().setOfferPrice(offerPrice);
		});
	}
	
	/**
	 * Gets the filtered offers.
	 *
	 * @param offerRequestWrapper the offer request wrapper
	 * @param offerType the offer type
	 * @return the filtered offers
	 */
	public CTOfferResponse getFilteredOffers(OfferRequestWrapper offerRequestWrapper , String offerType) {
		CTOfferResponse offerResponse = new CTOfferResponse();
		CTOfferResponse ctOfferResponse = null;
		List<String> customerSegments = null;

		try {
			customerSegments = offerRequestWrapper.isMobility() ? Stream.of("Mobility").collect(Collectors.toList()) :
				(Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerSegments()).isPresent() ? 
				offerRequestWrapper.getOfferRequest().getCustomerSegments() : Stream.of("Residential").collect(Collectors.toList()));
			if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerSegments()) 
					&& offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.EMPLOYEE)) {
				customerSegments = Stream.of(Constants.EMPLOYEE).collect(Collectors.toList());
			}
			if (isCustomerSegmentPresent(customerSegments, Constants.MOBILITY)) {

				// if request for base
				if (offerType.equalsIgnoreCase(Constants.BASE)) {
					ctOfferResponse = ottCTOffersProcessor.getVideoPlanOffers(offerRequestWrapper, true);
				}
				// if request for standalone 
				if (offerType.equalsIgnoreCase(Constants.STANDALONE)) {
					ctOfferResponse = ottCTOffersProcessor.getVideoAddonStandalonOffers(offerRequestWrapper, true);
				}
				offerResponse = ctOfferResponse;

			} else {
				// NON mobility case for base offers so sending the non mobility offers
				if (isCustomerSegmentPresent(customerSegments, Constants.RESIDENTIAL) || isCustomerSegmentPresent(customerSegments, Constants.EMPLOYEE)
				|| isCustomerSegmentPresent(customerSegments, Constants.DECA) || isCustomerSegmentPresent(customerSegments, Constants.DEMO)
				|| isCustomerSegmentPresent(customerSegments, Constants.SHOWROOM) || isCustomerSegmentPresent(customerSegments, Constants.COURTESY)
				|| isCustomerSegmentPresent(customerSegments, Constants.BCOMP) || isCustomerSegmentPresent(customerSegments, Constants.MDUTENANT )) {
					offerResponse = getNonMobilityOffers(offerRequestWrapper, offerType);
				}
			}

		} catch (ServiceException se) {
			log.error("Got ServiceException getFilteredOffers .... ", se);
			throw se;
		} catch (Exception e) {
			log.error("Services getFilteredOffers::" + Constants.GOT_EXCEPTION, e);
			throw new ServiceException(ErrorMessages.CPOPOFFERMS_INTERNALSERVER_ERROR)
					.addDetail(ErrorMessages.CPOPOFFERMS_INTERNALSERVER_ERROR_DETAILS001);
		}

		return offerResponse;
	}
	
	/**
	 * getNonMobilityOffers.
	 *
	 * @param offerRequestWrapper the offer request wrapper
	 * @param offerType the offer type
	 * @return the non mobility offers
	 */
	
	private CTOfferResponse getNonMobilityOffers(OfferRequestWrapper offerRequestWrapper,String offerType ) {
		
		if(offerType.equalsIgnoreCase(Constants.BASE)) {
			excludeCustomerContextWireless(offerRequestWrapper);
			return  ottCTOffersProcessor.getVideoPlanOffers(offerRequestWrapper, false);
		}
		if(offerType.equalsIgnoreCase(Constants.STANDALONE)) {
			return ottCTOffersProcessor.getVideoAddonStandalonOffers(offerRequestWrapper, false);
		}
		return null;
	}
	

		/**
	 * Exclude customer context wireless.
	 *
	 * @param offerRequestWrapper the offer request wrapper
	 */
	private void excludeCustomerContextWireless(OfferRequestWrapper offerRequestWrapper) {
		if (Objects.nonNull(offerRequestWrapper.getCtOfferRequest())) {
			List<CTCustomerContext> customerContext = offerRequestWrapper.getCtOfferRequest().getCustomerContext();
			if (Objects.nonNull(customerContext)) {
				List<CTCustomerContext> filteredCustomerContext = customerContext.stream().filter(Objects::nonNull)
																  .filter(existProduct -> (!"wireless".equalsIgnoreCase(existProduct.getProductFamily())))
																  .collect(Collectors.toList());
				
				if(CollectionUtils.isNotEmpty(filteredCustomerContext)) {
					offerRequestWrapper.getCtOfferRequest().setCustomerContext(filteredCustomerContext);
				}else {
					offerRequestWrapper.getCtOfferRequest().setCustomerContext(null);
				}
				
			}
		}
	}

	/**
	 * Checks if is customer segment present.
	 *
	 * @param custSegments the cust segments
	 * @param matchCustSegment the match cust segment
	 * @return true, if is customer segment present
	 */
	private boolean isCustomerSegmentPresent(List<String> custSegments, String matchCustSegment) {
		return custSegments != null ? custSegments.stream().anyMatch(custSegment -> custSegment.equals(matchCustSegment)): false;
	}

	/**
	 *
	 * @param processedCTOffers
	 * @return
	 */
	public List<CTOffer> combineOffers(List<CTOffer> processedCTOffers) {
		List<CTOffer> filteredOffers = Collections.synchronizedList(new ArrayList<>());
		if(!processedCTOffers.isEmpty()) {
			Map<String, List<CTOffer>> groupedOffers = processedCTOffers.stream()
					.collect(Collectors.groupingBy(offer -> offer.getAttributes().getBillingCode(), Collectors.toList()));
			groupedOffers.entrySet().parallelStream().forEach(offerMap -> {
				List<CTOffer> value = offerMap.getValue();
				List<Benefit> benefits = new ArrayList<>();
				List<String> ctOfferIds = new ArrayList<>();
				value.stream().filter(Objects::nonNull).forEach(offer -> {
					if (offer.getAttributes().getBenefits() != null) {
						benefits.addAll(offer.getAttributes().getBenefits());
					}
					ctOfferIds.add(offer.getId());
				});
				value.get(0).getAttributes().setBenefits(filterDuplicateBenefits(benefits));
				filteredOffers.add(value.get(0));
			});
		}
		return filteredOffers;
	}

		public List<CTOffer> combineSwimLaneOffers(List<CTOffer> processedCTOffers,
			OfferRequestWrapper offerRequestWrapper) {

		List<CTOffer> filteredOffers = Collections.synchronizedList(new ArrayList<>());
		List<String> prodCodes = new ArrayList<>();
		getProductCodesFromRequest(offerRequestWrapper, prodCodes);
		if (!processedCTOffers.isEmpty()) {
			Map<String, List<CTOffer>> groupedOffers = processedCTOffers.stream().collect(
					Collectors.groupingBy(offer -> offer.getAttributes().getBillingCode(), Collectors.toList()));

			groupedOffers.entrySet().parallelStream().forEach(offerMap -> {
				List<CTOffer> value = offerMap.getValue();
				List<Benefit> benefits = new ArrayList<>();
				List<String> ctOfferIds = new ArrayList<>();
				value.stream().filter(Objects::nonNull).forEach(offer -> {
					if (offer.getAttributes().getBenefits() != null) {
						benefits.addAll(offer.getAttributes().getBenefits());
					}
					if (offer.getAttributes().getContractIndicator().equalsIgnoreCase("EDSP")) {
						offer.getAttributes().setBenefits(filterDuplicateBenefits(benefits));
						setRecommendedFlag(offer, offerRequestWrapper, prodCodes);
						filteredOffers.add(offer);
					}

					ctOfferIds.add(offer.getId());

				});
				value.get(0).getAttributes().setBenefits(filterDuplicateBenefits(benefits));
				

			});
		}
		return filteredOffers;
	}

	public static List<Benefit> filterDuplicateBenefits(List<Benefit> benefits) {

		List<String> benefit_codes = new ArrayList<>();
		List<Benefit> final_benefits = new ArrayList<>();

		benefits.stream().forEach(benefit -> {
			if (Optional.ofNullable(benefit.getBillingBenefitCode()).isPresent()
					&& !benefit_codes.contains(benefit.getBillingBenefitCode())) {
				benefit_codes.add(benefit.getBillingBenefitCode());
				final_benefits.add(benefit);
			}
		});

		return final_benefits;

	}
	private void getProductCodesFromRequest(OfferRequestWrapper offerRequestWrapper, List<String> prodCodes) {
		if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext()).isPresent()
				&& Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()).isPresent()
				&& offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getProducts().size() > 0) {

			offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getProducts().forEach(prod -> {
				if (prod.getProductType().equalsIgnoreCase("video-plan"))
					prodCodes.add(prod.getProductCode());

			});
		}
	}
	
	private CTOffer setRecommendedFlag(CTOffer offer, OfferRequestWrapper offerRequestWrapper, List<String> prodCodes) {

		log.info("{} swimlaneSwitchEligible:{} swimlan flow - STARTS");

		String basePlantobeChecked = offer.getAttributes().getBillingCode();

		// if basePlantobeChecked to be checked is XTRA or Optimomas, We need to return
		// Choice and Entertainment
		for (String prodCode : prodCodes) {
			if (basePlantobeChecked.contains(prodCode)) {
				offer.getAttributes().setRecommendedEDSP("Y");
			} else {
				List<GenericTypeIdBase> typeIdBase = offer.getAttributes().getSwimlaneSwitchEligiblePack();
				Optional.ofNullable(typeIdBase).orElseGet(Collections::emptyList).stream().filter(Objects::nonNull)
				.forEach(type -> {
					if (type.getKey().contains(prodCode)) {
						offer.getAttributes().setRecommendedEDSP("Y");
					}
				});
			}
		}

		log.info("{} swimlaneSwitchEligible:{} swimlan flow - ENDS");

		return offer;
	}

	public void supressOfferLevelAttributes(List<CTOffer> offers){
		if(offers.size() > 0){
			offers.forEach(offer -> {
				if (Optional.ofNullable(offer).isPresent() && Optional.ofNullable(offer.getAttributes()).isPresent()
						&& Optional.ofNullable(offer.getAttributes().getMinimumCustomerTenureInMonths()).isPresent()) {
					offer.getAttributes().setMinimumCustomerTenureInMonths(null);
				}
				if (Optional.ofNullable(offer).isPresent() && Optional.ofNullable(offer.getAttributes()).isPresent()
						&& Optional.ofNullable(offer.getAttributes().getProductsOnAccountToSuppressOffer()).isPresent()) {
					offer.getAttributes().setProductsOnAccountToSuppressOffer(null);
				}
			});
		}
	}
	/**
	 * This method is used to filter offers based on qualifying products with effective dates
	 * @param ctOfferResponse
	 * @param offerRequestWrapper
	 */
	public void filterOffersBasedOnQualifyingProducts(CTOfferResponse ctOfferResponse, OfferRequestWrapper offerRequestWrapper) {
		try {
			HashMap<String, ProductInfo> requestProductInfo = new HashMap<String, ProductInfo>();
			List<CTOffer> eligibleOffers = new ArrayList<>();
			if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext()).isPresent() && Optional
					.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()).isPresent()
					&& Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getProducts()).isPresent()) {
				offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getProducts().forEach(productInfo -> {
					requestProductInfo.put(productInfo.getProductCode(), productInfo);
				});
			}
			if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext()).isPresent() && Optional
					.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()).isPresent()
					&& Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getReplacementDevices()).isPresent()) {
				List<ReplacementDevices> replacementDevices = MAPPER.convertValue(
						offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getReplacementDevices(),
							new TypeReference<List<ReplacementDevices>>() {
							});
				if(CollectionUtils.isNotEmpty(replacementDevices)) {
					replacementDevices.forEach(replacementDevice -> {
						requestProductInfo.put(replacementDevice.getProductCode(), new ProductInfo());
					});
				}
			}
			if (Objects.nonNull(ctOfferResponse) && Objects.nonNull(ctOfferResponse.getOffers())) {
				ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {
					if (Objects.nonNull(ctOffer.getAttributes())
							&& (Objects.isNull(ctOffer.getAttributes().isQualifyingProductCheckNOTRequired()) || !ctOffer.getAttributes().isQualifyingProductCheckNOTRequired())
							&& Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts())) {
						List<AssociatedProduct> associatedProductList = ctOffer.getAttributes().getAssociatedProducts();
						if (Objects.nonNull(associatedProductList) && !associatedProductList.isEmpty()) {
							associatedProductList.stream().filter(Objects::nonNull).forEach(
									associatedProduct -> {
										List<ProductWrapper> qualifyingProductsList = associatedProduct.getQualifyingProducts();
										if (Objects.nonNull(qualifyingProductsList) && !qualifyingProductsList.isEmpty()) {
											qualifyingProductsList.stream().filter(Objects::nonNull).forEach(
													qualifyingProduct -> {
														List<Product> products = qualifyingProduct.getProducts();
														Constraints constraints = qualifyingProduct.getConstraints();
														final LocalDate[] preEffectiveDate = new LocalDate[1];
														final LocalDate[] postEffectiveDate = new LocalDate[1];
														try {
															if (Objects.nonNull(constraints) && StringUtils.isNotEmpty(constraints.getPreEffectiveDate())) {
																preEffectiveDate[0] = LocalDate.parse(constraints.getPreEffectiveDate(), formatter);
															}
															if (Objects.nonNull(constraints) && StringUtils.isNotEmpty(constraints.getPostEffectiveDate())) {
																postEffectiveDate[0] = LocalDate.parse(constraints.getPostEffectiveDate(), formatter);
															}
														} catch (Exception e) {
															log.error("Error in parsing date in the method -> filterOffersBasedOnQualifyingProducts ", e);
														}

														if (Objects.nonNull(products) && !products.isEmpty()) {
															products.stream().filter(Objects::nonNull).forEach(
																	product -> {
																		if (requestProductInfo.containsKey(product.getKey())) {
																			if (Objects.nonNull(preEffectiveDate[0]) || Objects.nonNull(postEffectiveDate[0])) {
																				ProductInfo productInfo = requestProductInfo.get(product.getKey());
																				LocalDate productStartDate = null;
																				try {
																					if (Objects.nonNull(productInfo) && StringUtils.isNotEmpty(productInfo.getProductStartDate())) {
																						productStartDate = LocalDate.parse(productInfo.getProductStartDate(), formatter);
																					}
																				} catch (Exception e) {
																					log.error("Error in parsing date in the method -> filterOffersBasedOnQualifyingProducts ", e);
																				}
																				if ((Objects.nonNull(productStartDate) && Objects.nonNull(preEffectiveDate[0]) && isProductStartDateBeforeOrEqual(productStartDate, preEffectiveDate[0])) ||
																						(Objects.nonNull(productStartDate) && Objects.nonNull(postEffectiveDate[0]) && isProductStartDateAfterOrEqual(productStartDate, postEffectiveDate[0]))) {
																					eligibleOffers.add(ctOffer);
																				}
																			} else {
																				eligibleOffers.add(ctOffer);
																			}
																		}
																	});
														} else { // to add offers which is not having products under qualifying products
															eligibleOffers.add(ctOffer);
														}
													});
										} else {// end of qualifyingProductsList empty check
											eligibleOffers.add(ctOffer);
										}
									});
						} else {// end of associatedProductList empty check
							eligibleOffers.add(ctOffer);
						}
					} else { // to add offers which is not having associated products itself
						eligibleOffers.add(ctOffer);
					}
				});
			}
			List<CTOffer> finalEligibleOffers = eligibleOffers.stream().distinct().collect(Collectors.toList());
			ctOfferResponse.setOffers(finalEligibleOffers);
			ctOfferResponse.setTotal(finalEligibleOffers.size());
			ctOfferResponse.setCount(finalEligibleOffers.size());
		}catch(Exception e) {
			log.error("Error in the method -> filterOffersBasedOnQualifyingProducts ", e);
		}
	}

	private Boolean isProductStartDateBeforeOrEqual(LocalDate productStartDate, LocalDate preEffectiveDate) {
		return productStartDate.isBefore(preEffectiveDate) || productStartDate.isEqual(preEffectiveDate);
	}

	private Boolean isProductStartDateAfterOrEqual(LocalDate productStartDate, LocalDate postEffectiveDate) {
		return productStartDate.isAfter(postEffectiveDate) || productStartDate.isEqual(postEffectiveDate);
	}
	
}