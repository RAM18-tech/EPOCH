package com.dtv.dcp.epoch.model.common;

import java.io.Serializable;

public class OfferMinMaxQuantity implements Serializable {
	private static final long serialVersionUID = 1L;

	private String maxQuantity;

	private String minQuantity;

	public String getMaxQuantity() {
		return maxQuantity;
	}

	public void setMaxQuantity(String maxQuantity) {
		this.maxQuantity = maxQuantity;
	}

	public String getMinQuantity() {
		return minQuantity;
	}

	public void setMinQuantity(String minQuantity) {
		this.minQuantity = minQuantity;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
