package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;

import com.dtv.dcp.epoch.model.ct.generic.GenericLocaleBase;

public class Category implements Serializable{
	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private String categoryType;
	private int maxOrderLimitCategory;
	private GenericLocaleBase displayName;
	private GenericLocaleBase description;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCategoryType() {
		return categoryType;
	}
	public void setCategoryType(String categoryType) {
		this.categoryType = categoryType;
	}
	public int getMaxOrderLimitCategory() {
		return maxOrderLimitCategory;
	}
	public void setMaxOrderLimitCategory(int maxOrderLimitCategory) {
		this.maxOrderLimitCategory = maxOrderLimitCategory;
	}
	public GenericLocaleBase getDisplayName() {
		return displayName;
	}
	public void setDisplayName(GenericLocaleBase displayName) {
		this.displayName = displayName;
	}
	public GenericLocaleBase getDescription() {
		return description;
	}
	public void setDescription(GenericLocaleBase description) {
		this.description = description;
	}
}
