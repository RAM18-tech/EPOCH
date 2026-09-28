package com.dtv.dcp.epoch.model.catalogone.offer;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author dr000y
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SelectionOption implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String minLimit;

	private String maxLimit;

	private String defaultLimit;

	private List<CatalogPrice> totalPrice;

	private List<CatalogPrice> productPrice;

	public void setMinLimit(String minLimit) {
		this.minLimit = minLimit;
	}

	public String getMinLimit() {
		return this.minLimit;
	}

	public void setMaxLimit(String maxLimit) {
		this.maxLimit = maxLimit;
	}

	public String getMaxLimit() {
		return this.maxLimit;
	}

	public void setDefaultLimit(String defaultLimit) {
		this.defaultLimit = defaultLimit;
	}

	public String getDefaultLimit() {
		return this.defaultLimit;
	}

	public void setTotalPrice(List<CatalogPrice> totalPrice) {
		this.totalPrice = totalPrice;
	}

	public List<CatalogPrice> getTotalPrice() {
		return this.totalPrice;
	}

	public void setProductPrice(List<CatalogPrice> productPrice) {
		this.productPrice = productPrice;
	}

	public List<CatalogPrice> getProductPrice() {
		return this.productPrice;
	}

	@Override
	public String toString() {
		return "SelectionOptions [minLimit=" + minLimit + ", maxLimit=" + maxLimit + ", defaultLimit=" + defaultLimit
				+ ", totalPrice=" + totalPrice + ", productPrice=" + productPrice + "]";
	}
}
