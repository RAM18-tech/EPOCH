package com.dtv.dcp.epoch.model.common.wireless;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class AdditionalOffering
 *
 */
public class AdditionalOffering {

	@JsonProperty("code")
	private String code;

	@JsonProperty("shareDataGrpInd")
	private String shareDataGrpInd;

	@JsonProperty("effectiveDate")
	private String effectiveDate;

	@JsonProperty("expirationDate")
	private String expirationDate;

	@JsonProperty("mrc")
	private float mrc;

	@JsonProperty("netMRC")
	private float netMRC;

	@JsonProperty("netDiscount")
	private float netDiscount;

	@JsonProperty("socDiscountInfo")
	private List<SocDiscountInfo> socDiscountInfo = null;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getShareDataGrpInd() {
		return shareDataGrpInd;
	}

	public void setShareDataGrpInd(String shareDataGrpInd) {
		this.shareDataGrpInd = shareDataGrpInd;
	}

	public String getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(String effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public String getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(String expirationDate) {
		this.expirationDate = expirationDate;
	}

	public float getMrc() {
		return mrc;
	}

	public void setMrc(float mrc) {
		this.mrc = mrc;
	}

	public float getNetMRC() {
		return netMRC;
	}

	public void setNetMRC(float netMRC) {
		this.netMRC = netMRC;
	}

	public float getNetDiscount() {
		return netDiscount;
	}

	public void setNetDiscount(float netDiscount) {
		this.netDiscount = netDiscount;
	}

	public List<SocDiscountInfo> getSocDiscountInfo() {
		return socDiscountInfo;
	}

	public void setSocDiscountInfo(List<SocDiscountInfo> socDiscountInfo) {
		this.socDiscountInfo = socDiscountInfo;
	}

}
