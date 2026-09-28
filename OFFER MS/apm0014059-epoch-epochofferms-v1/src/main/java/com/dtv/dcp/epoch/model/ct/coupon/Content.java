package com.dtv.dcp.epoch.model.ct.coupon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.dtv.dcp.epoch.model.common.VaildateCouponBase;

public class Content implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private List<VaildateCouponBase> Coupons;
	

    public List<VaildateCouponBase> getCoupons() {
		return Coupons;
	}

	public void setCoupons(List<VaildateCouponBase> coupons) {
		Coupons = coupons;
	}

	public Content(ArrayList arrayList) {
        // Intentionally Left Blank For Jackson
    }
	
	public Content() {
        // Intentionally Left Blank For Jackson
    }

    @Override
    public String toString() {
        return "Content{" +
                "Coupons=" + Coupons +
                '}';
    }

}
