package com.dtv.dcp.epoch.model.ct.burn;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OfferBurnRequest implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;
    private String code;
    private boolean coupon;

    public OfferBurnRequest(String id, String code, boolean coupon) {
        this.id = id;
        this.code = code;
        this.coupon = coupon;
    }

    public OfferBurnRequest() {
        // Intentionally Left Blank For Jackson
    }

    @Override
    public String toString() {
        return "OfferBurnRequest{" +
                "id='" + id + '\'' +
                ", code='" + code + '\'' +
                ", coupon=" + coupon +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isCoupon() {
        return coupon;
    }

    public void setCoupon(boolean coupon) {
        this.coupon = coupon;
    }
}