package com.dtv.dcp.epoch.model.ct.product;

public class Specification {

	private String id;
	private String salesChannel;
	private String flowActionType;
	private String category;
	private String subCategory;
	private String text;
	private String displayRank ;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSalesChannel() {
		return salesChannel;
	}
	public void setSalesChannel(String salesChannel) {
		this.salesChannel = salesChannel;
	}
	public String getFlowActionType() {
		return flowActionType;
	}
	public void setFlowActionType(String flowActionType) {
		this.flowActionType = flowActionType;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getSubCategory() {
		return subCategory;
	}
	public void setSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getDisplayRank() {
		return displayRank;
	}
	public void setDisplayRank(String displayRank) {
		this.displayRank = displayRank;
	}
	
}
