package com.dtv.dcp.epoch.processor.watchtv.sales;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.AssociatedProduct;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.offer.OfferPrice;
import com.dtv.dcp.epoch.model.ct.product.Product;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.model.ct.product.ProductWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.helper.CPOPBenefitsHelper;
import com.dtv.dcp.epoch.processor.helper.CPOPProductsHelper;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.dtv.dcp.epoch.util.SalesVideoAddonProcessor;

/**
 * 
 * @author sn611j
 *
 */
@Component
public class WatchTVSalesOffersProcessor {

    @Autowired
    CpopClient cpopClient;

    @Autowired
    SalesVideoAddonProcessor salesVideoAddonProcessor;

    @Autowired
    CPOPProductsHelper cpopProductsHelper;

    @Autowired
    CPOPBenefitsHelper cpopBenefitsHelper;

    public CTOfferResponse getOffers(OfferRequestWrapper offerRequestWrapper) throws ServiceException {
        CTOfferResponse videoAddonOffer = null;
        Integer count = 0;
        Integer total = 0;
        List<CTOffer> finalOfferList = new ArrayList<>();
        List<ProductObj> finalProductList = new ArrayList<>();
        CTOfferResponse finalOfferResponse = new CTOfferResponse();
        videoAddonOffer = salesVideoAddonProcessor.getWatchTVVideoAddonOffers(offerRequestWrapper);

        populateOffersProducts(videoAddonOffer, finalOfferList, finalProductList);
		
		if (!finalOfferList.isEmpty()) {
			finalOfferResponse.setOffers(finalOfferList);
			finalOfferResponse.setProducts(finalProductList);
		}

        count = count + finalOfferList.size();

        finalOfferResponse.setOffers(finalOfferList);
        finalOfferResponse.setCount(count);
        finalOfferResponse.setTotal(total);

        finalOfferResponse = cpopProductsHelper.filterInvalidPrices(finalOfferResponse);
        finalOfferResponse = cpopBenefitsHelper.filterInvalidBenefits(finalOfferResponse, offerRequestWrapper);
        if(offerRequestWrapper.getOfferRequest().getSalesChannel().stream().anyMatch(s -> OffersUtils.checkOnlineRelatedChannel(s)))
        {
            calculateBestPrice(finalOfferList);
        }
        
        return finalOfferResponse;
    }

    /**
     * Temp Solution for the FEs to use the Outside Products as it was published for watchTV Flow
     * @param videoAddonOffer
     * @param finalOfferList
     * @param finalProductList
     */
	private void populateOffersProducts(CTOfferResponse videoAddonOffer, List<CTOffer> finalOfferList,
			List<ProductObj> finalProductList) {
		Map<String ,ProductObj> productsMaps = new HashMap<>();
        if (videoAddonOffer != null && CollectionUtils.isNotEmpty(videoAddonOffer.getOffers())) {
			finalOfferList.addAll(videoAddonOffer.getOffers());
			finalOfferList.stream().filter(Objects::nonNull).forEach(offer -> {
				AssociatedProduct associatedProduct = offer.getAttributes().getAssociatedProducts().get(0);
				if (Optional.ofNullable(associatedProduct).isPresent()) {
					if (Optional.ofNullable(associatedProduct.getBundleProducts()).isPresent() && !associatedProduct.getBundleProducts().isEmpty()) {
						associatedProduct.getBundleProducts().stream().filter(Objects::nonNull).forEach(bundleProduct -> {
							List<Product> products = bundleProduct.getProducts();
							if (Optional.ofNullable(products).isPresent() && !products.isEmpty()) {
								products.stream().filter(Objects::nonNull).forEach(prod -> 
									productsMaps.put(prod.getId(),prod.getObj())
								);
							}
						});
					}
				}
			});
		}		
		if(productsMaps != null && !productsMaps.isEmpty()) {
			finalProductList.addAll(productsMaps.values());
		}
	}
   
	public void calculateBestPrice(List<CTOffer> offerList) {
		offerList.stream().filter(Objects::nonNull).forEach(offer -> {
			OfferPrice offerPrice = new OfferPrice();
			// Fetch the baseprice and set the offerprice at offer level with the base
			// price. If there is no benfit to subtract, this is the best price
			List<ProductWrapper> bundleProducts = null;
			AssociatedProduct associatedProduct = offer.getAttributes().getAssociatedProducts().get(0);
			if (Optional.ofNullable(associatedProduct).isPresent()) {
				bundleProducts = associatedProduct.getBundleProducts();
				if (Optional.ofNullable(bundleProducts).isPresent() && !bundleProducts.isEmpty()) {
					bundleProducts.stream().filter(Objects::nonNull).forEach(bundleProduct -> {
						List<Product> products = bundleProduct.getProducts();
						if (Optional.ofNullable(products).isPresent() && !products.isEmpty()) {
							products.stream().filter(Objects::nonNull).forEach(prod -> {
								if (Optional.ofNullable(prod).isPresent()
										&& Optional.ofNullable(prod.getObj()).isPresent()) {
									if (Optional.ofNullable(prod.getObj().getVariants()).isPresent()
											&& !prod.getObj().getVariants().isEmpty()) {
										if (Optional.ofNullable(prod.getObj().getVariants().get(0).getPrices())
												.isPresent()
												&& !prod.getObj().getVariants().get(0).getPrices().isEmpty()) {
											prod.getObj().getVariants().get(0).getPrices().stream()
													.filter(Objects::nonNull).forEach(price -> {
														if (Optional.ofNullable(price.getValue()).isPresent()
																&& Optional
																		.ofNullable(price.getValue().getDollarAmount())
																		.isPresent()) {
															offerPrice.setDollarAmount(offerPrice.getDollarAmount()
																	+ price.getValue().getDollarAmount());
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

			// Now if there is a relevant benefit value, subtract it from the base price to
			// arrive at BEST PRICE!
			offer.getAttributes().getBenefits().stream().filter(Objects::nonNull).forEach(benefit -> {
				if (Optional.ofNullable(benefit).isPresent() && Optional.ofNullable(benefit.getValue()).isPresent()
						&& Optional.ofNullable(benefit.getBenefitType()).isPresent() && !(benefit.isAgentOffer())) {
					if (benefit.getBenefitType().equalsIgnoreCase("flat-off")) {
						offerPrice.setDollarAmount(offerPrice.getDollarAmount() - benefit.getValue().getDollarAmount());
					} else if (benefit.getBenefitType().equalsIgnoreCase("percent-off")) {
						Double percentageOff = ((offerPrice.getDollarAmount()) * (benefit.getValue().getPercentage()))
								/ 100;
						offerPrice.setDollarAmount(offerPrice.getDollarAmount() - percentageOff);
					}
				}
			});
			BigDecimal bd = new BigDecimal(Double.toString(offerPrice.getDollarAmount()));
			bd = bd.setScale(2, RoundingMode.HALF_UP);
			offerPrice.setDollarAmount(bd.doubleValue());
			offer.getAttributes().setOfferPrice(offerPrice);
		});
	}

    /**
     *
     * @param offers
     * @param offerRequestWrapper
     */
	public void processOffersFetchedBySearchIds(CTOfferResponse offers, OfferRequestWrapper offerRequestWrapper) {
		cpopProductsHelper.filterInvalidPrices(offers);
		cpopBenefitsHelper.filterInvalidBenefits(offers, offerRequestWrapper);
		if (offerRequestWrapper.getOfferRequest().getSalesChannel().stream()
				.anyMatch(s -> OffersUtils.checkOnlineRelatedChannel(s))) {
			calculateBestPrice(offers.getOffers());
		}
		
		  List<CTOffer> finalOfferList = new ArrayList<>();
	      List<ProductObj> finalProductList = new ArrayList<>();
	      CTOfferResponse finalOfferResponse = new CTOfferResponse();	        
	      populateOffersProducts(offers, finalOfferList, finalProductList);
	      finalOfferResponse.setOffers(finalOfferList);
		  finalOfferResponse.setProducts(finalProductList);
	}
    
}
