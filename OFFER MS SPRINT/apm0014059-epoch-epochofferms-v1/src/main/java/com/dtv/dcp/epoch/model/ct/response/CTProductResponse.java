package com.dtv.dcp.epoch.model.ct.response;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.common.Additonals;
import com.dtv.dcp.epoch.model.common.request.DeviceInfo;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class CTProductResponse implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private String dummy;


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

	/** The products. */
	// private List<CTOffer> offers;
	private List<ProductObj> products;

	private List<DeviceInfo> devices;

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
	public List<ProductObj> getProducts() {
		return products;
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
	 * @param products the offers to set
	 */
	public void setProducts(List<ProductObj> products) {
		this.products = products;
	}

	public List<DeviceInfo> getDevices() {
		return devices;
	}

	public void setDevices(List<DeviceInfo> devices) {
		this.devices = devices;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CTProductResponse [limit=");
		builder.append(limit);
		builder.append(", dummy=");
		builder.append(dummy);
		builder.append(", offset=");
		builder.append(offset);
		builder.append(", count=");
		builder.append(count);
		builder.append(", total=");
		builder.append(total);
		builder.append(", additionals=");
		builder.append(additionals);
		builder.append(", products=");
		builder.append(products);
		builder.append(", devices=");
		builder.append(devices);
		builder.append("]");
		return builder.toString();
	}
}
