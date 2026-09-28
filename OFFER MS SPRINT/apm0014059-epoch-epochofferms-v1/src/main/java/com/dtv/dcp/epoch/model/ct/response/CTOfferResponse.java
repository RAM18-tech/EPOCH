package com.dtv.dcp.epoch.model.ct.response;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.common.Additonals;
import com.dtv.dcp.epoch.model.common.validatecoupons.Coupon;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.offer.IneligibleOffer;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CTOfferResponse implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	

	/** The minimumPurchaseAmount. */
	private int limit;
	
	/** The offset. */
	private int offset;
	
	/** The count. */
	private int count;
	
	/** The total. */
	private int total;
	
	/** The additonals. */
	@JsonProperty("additionals")
	private Additonals additionals;
	
	/** The offers. */
	private List<CTOffer> offers;
	
	/** The includedProducts.	 */
	private List<ProductObj> includedProducts;

	/** The offers. */
	private List<CTOffer> ioOffers;
	
	private List<CTOfferQualification> offerQualification;
	
	/** The products. */
	private List<ProductObj> products;
	
	/** The coupons. */
	private List<Coupon> coupons;
	
	/** The ineligibleOffers. */
	private List<IneligibleOffer> ineligibleOffers;
	
	/** The ineligibleOffers. */
	private List<String> specialOfferCartModeCodes;
	
	/**
	 * @return the ineligibleOffers
	 */
	public List<IneligibleOffer> getIneligibleOffers() {
		return ineligibleOffers;
	}

	/**
	 * @param ineligibleOffers the ineligibleOffers to set
	 */
	public void setIneligibleOffers(List<IneligibleOffer> ineligibleOffers) {
		this.ineligibleOffers = ineligibleOffers;
	}

	public List<ProductObj> getIncludedProducts() {
		return includedProducts;
	}

	public void setIncludedProducts(List<ProductObj> includedProducts) {
		this.includedProducts = includedProducts;
	}

	public List<Coupon> getCoupons() {
		return coupons;
	}

	public void setCoupons(List<Coupon> coupons) {
		this.coupons = coupons;
	}

	public List<CTOffer> getIoOffers() {
		return ioOffers;
	}

	public void setIoOffers(List<CTOffer> ioOffers) {
		this.ioOffers = ioOffers;
	}

	/**
	 * @return the limit
	 */
	public int getLimit() {
		return limit;
	}

	/**
	 * @param limit the limit to set
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}

	/**
	 * @return the offset
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * @param offset the offset to set
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * @return the total
	 */
	public int getTotal() {
		return total;
	}

	/**
	 * @param total the total to set
	 */
	public void setTotal(int total) {
		this.total = total;
	}

	/**
	 * @return the offers
	 */
	public List<CTOffer> getOffers() {
		return offers;
	}

	/**
	 * @return the additionals
	 */
	public Additonals getAdditionals() {
		return additionals;
	}

	/**
	 * @param additionals the additionals to set
	 */
	public void setAdditionals(Additonals additionals) {
		this.additionals = additionals;
	}

	/**
	 * @param offers the offers to set
	 */
	public void setOffers(List<CTOffer> offers) {
		this.offers = offers;
	}

	/**
	 * @return the products
	 */
	public List<ProductObj> getProducts() {
		return products;
	}

	/**
	 * @param products the products to set
	 */
	public void setProducts(List<ProductObj> products) {
		this.products = products;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CTOfferResponse [limit=");
		builder.append(limit);
		builder.append(", offset=");
		builder.append(offset);
		builder.append(", count=");
		builder.append(count);
		builder.append(", total=");
		builder.append(total);
		builder.append(", additionals=");
		builder.append(additionals);
		builder.append(", offers=");
		builder.append(offers);
		builder.append(", offerQualification=");
		builder.append(offerQualification);
		builder.append(", products=");
		builder.append(products);
		builder.append(", coupons=");
		builder.append(coupons);
		builder.append(", includedProducts=");
		builder.append(includedProducts);
		builder.append(", ineligibleOffers=");
		builder.append(ineligibleOffers);
		builder.append("]");
		return builder.toString();
	}

	public List<CTOfferQualification> getOfferQualification() {
		return offerQualification;
	}

	public void setOfferQualification(List<CTOfferQualification> offerQualification) {
		this.offerQualification = offerQualification;
	}

	public List<String> getSpecialOfferCartModeCodes() {
		return specialOfferCartModeCodes;
	}

	public void setSpecialOfferCartModeCodes(List<String> specialOfferCartModeCodes) {
		this.specialOfferCartModeCodes = specialOfferCartModeCodes;
	}
}
