package com.dtv.dcp.epoch.model.common.validatecoupons;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.ct.product.Image;
import com.dtv.dcp.epoch.model.ct.product.Price;

public class Variant implements Serializable {



	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The id. */
	private String  id;
	
	/** The sku. */
	private String sku;
	
	/** The key. */
	private String key ;
	
	/** The prices. */
	private List<Price> prices ;
	
	/** The attributes. */
	private CouponProductAttributes attributes ;
	
	/** The contentImages. */
	private List<Image>contentImages ;
	
	/** The productImages. */
	private List<Image> productImages ;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the sku
	 */
	public String getSku() {
		return sku;
	}

	/**
	 * @param sku the sku to set
	 */
	public void setSku(String sku) {
		this.sku = sku;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the prices
	 */
	public List<Price> getPrices() {
		return prices;
	}

	/**
	 * @param prices the prices to set
	 */
	public void setPrices(List<Price> prices) {
		this.prices = prices;
	}

	/**
	 * @return the attributes
	 */
	public CouponProductAttributes getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(CouponProductAttributes attributes) {
		this.attributes = attributes;
	}

	/**
	 * @return the contentImages
	 */
	public List<Image> getContentImages() {
		return contentImages;
	}

	/**
	 * @param contentImages the contentImages to set
	 */
	public void setContentImages(List<Image> contentImages) {
		this.contentImages = contentImages;
	}

	/**
	 * @return the productImages
	 */
	public List<Image> getProductImages() {
		return productImages;
	}

	/**
	 * @param productImages the productImages to set
	 */
	public void setProductImages(List<Image> productImages) {
		this.productImages = productImages;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Variant [id=");
		builder.append(id);
		builder.append(", sku=");
		builder.append(sku);
		builder.append(", key=");
		builder.append(key);
		builder.append(", prices=");
		builder.append(prices);
		builder.append(", attributes=");
		builder.append(attributes);
		builder.append(", contentImages=");
		builder.append(contentImages);
		builder.append(", productImages=");
		builder.append(productImages);
		builder.append("]");
		return builder.toString();
	}

	
}
