package com.dtv.dcp.epoch.integration.rsn;

import java.io.Serializable;

import com.dtv.dcp.epoch.model.ct.request.PriceProtection;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;

/**
 * The Class RSNRequest.
 *
 * @author vc402t
 */
@ApiModel(value = "CustomerProfile")
@JsonInclude(Include.NON_NULL)
public class CustomerProfile implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The accountType. */
	private String rsnSku;
	/** The zipCode. */
	private String zipCode;
	/** The basePackage */
	private BasePackage basePackage;
	
	private PriceProtection priceProtection;

	private String nextBillCycleDate;

	public String getZipCode() {
		return zipCode;
	}
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	public BasePackage getBasePackage() {
		return basePackage;
	}
	public void setBasePackage(BasePackage basePackage) {
		this.basePackage = basePackage;
	}
	public String getRsnSku() {
		return rsnSku;
	}
	public void setRsnSku(String rsnSku) {
		this.rsnSku = rsnSku;
	}

	public PriceProtection getPriceProtection() {
		return priceProtection;
	}
	public void setPriceProtection(PriceProtection priceProtection) {
		this.priceProtection = priceProtection;
	}
	public String getNextBillCycleDate() {
		return nextBillCycleDate;
	}
	public void setNextBillCycleDate(String nextBillCycleDate) {
		this.nextBillCycleDate = nextBillCycleDate;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CustomerProfile [zipCode=");
		builder.append(zipCode);
		builder.append(", basePackage=");
		builder.append(basePackage);
		builder.append(", rsnSku=");
		builder.append(rsnSku);
		builder.append(", priceProtection=");
		builder.append(priceProtection);
		builder.append("]");
		return builder.toString();
	}
}
