package com.dtv.dcp.epoch.processor.watchtv.sales;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.model.ct.benefit.Benefit;
import com.dtv.dcp.epoch.model.ct.offer.AssociatedProduct;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.offer.OfferPrice;
import com.dtv.dcp.epoch.model.ct.product.Product;
import com.dtv.dcp.epoch.model.ct.product.ProductWrapper;


/**
 * 
 * @author sn611j
 *
 */
@Component
public class WatchTVSalesOffersProcessorHelper {

	/**
	 * Calculate best price.
	 *
	 * @param offerList the offer list
	 */
	public void calculateBestPrice(List<CTOffer> offerList) {
		offerList.stream().filter(Objects::nonNull).forEach(offer -> {
			OfferPrice offerPrice = new OfferPrice();
			// Fetch the baseprice and set the offerprice at offer level with the base price. If there is no benfit to subtract, this is the best price 
			List<ProductWrapper> bundleProducts = null;
			AssociatedProduct associatedProduct = offer.getAttributes().getAssociatedProducts().get(0);
			if (Optional.ofNullable(associatedProduct).isPresent()) {
				bundleProducts = associatedProduct.getBundleProducts();
				if (Optional.ofNullable(bundleProducts).isPresent() && !bundleProducts.isEmpty()) {
					updateOfferPrice(offerPrice, bundleProducts);
				}
			}

			//Now if there is a relevant benefit value, subtract it from the base price to arrive at BEST PRICE!
			offer.getAttributes().getBenefits().stream().filter(Objects::nonNull).forEach(benefit -> 
				extractBenefits(offerPrice, benefit)
			);
			BigDecimal bd = new BigDecimal(Double.toString(offerPrice.getDollarAmount()));
			bd = bd.setScale(2, RoundingMode.HALF_UP);
			offerPrice.setDollarAmount(bd.doubleValue());
			offer.getAttributes().setOfferPrice(offerPrice);
		});
	}

	private void extractBenefits(OfferPrice offerPrice, Benefit benefit) {
		if (Optional.ofNullable(benefit).isPresent() && Optional.ofNullable(benefit.getValue()).isPresent()
				&& Optional.ofNullable(benefit.getBenefitType()).isPresent()
				&& benefit.getBenefitType().equalsIgnoreCase("flat-off")
				&& !benefit.isIoOffer()
				&& !(benefit.isAgentOffer())) {
			offerPrice.setDollarAmount(offerPrice.getDollarAmount() - benefit.getValue().getDollarAmount());
		}
	}

	private void updateOfferPrice(OfferPrice offerPrice, List<ProductWrapper> bundleProducts) {
		bundleProducts.stream().filter(Objects::nonNull).forEach(bundleProduct -> {
			List<Product> products = bundleProduct.getProducts();
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
	