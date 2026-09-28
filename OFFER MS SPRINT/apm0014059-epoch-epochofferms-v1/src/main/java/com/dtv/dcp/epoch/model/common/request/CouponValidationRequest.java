package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;

import com.dtv.dcp.epoch.model.common.AccountContext;
import com.dtv.dcp.epoch.model.common.PurchaseContext;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CouponValidationRequest implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Coupon Code.
     */
    private String couponcode;
    
    /**
     * Purchase Context.
     */
	
	@JsonProperty("purchaseContext")
    private PurchaseContext purchaseContext;
    
    /**
     * Account Context.
     */
	@JsonProperty("accountContext")
    private AccountContext accountContext;

   
	public String getCouponcode() {
		return couponcode;
	}

	public void setCouponcode(String couponcode) {
		this.couponcode = couponcode;
	}

	public PurchaseContext getPurchaseContext() {
		return purchaseContext;
	}

	public void setPurchaseContext(PurchaseContext purchaseContext) {
		this.purchaseContext = purchaseContext;
	}

	public AccountContext getAccountContext() {
		return accountContext;
	}

	public void setAccountContext(AccountContext accountContext) {
		this.accountContext = accountContext;
	}

}