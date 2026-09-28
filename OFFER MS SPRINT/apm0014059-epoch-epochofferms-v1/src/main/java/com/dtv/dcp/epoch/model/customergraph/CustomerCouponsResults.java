package com.dtv.dcp.epoch.model.customergraph;

import java.io.Serializable;
import java.util.List;

public class CustomerCouponsResults implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4488683241579559588L;
	
	private List<CustomerCoupons> coupons;

	public List<CustomerCoupons> getCoupons() {
		return coupons;
	}

	public void setCoupons(List<CustomerCoupons> coupons) {
		this.coupons = coupons;
	}

}
