package com.dtv.dcp.epoch.model.common.wireless;

/**
 * The Class SocDiscountInfo
 *
 */
public class SocDiscountInfo {

	private String priority;
	private String category;
	private String type;
	private String code;
	private float amount;
	private float mrc;
	private float netMRC;

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
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
}
