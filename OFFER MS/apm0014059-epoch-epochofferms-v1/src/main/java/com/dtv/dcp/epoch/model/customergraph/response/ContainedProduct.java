package com.dtv.dcp.epoch.model.customergraph.response;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ContainedProduct implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String typeCode;
	private String code;
	private String name;
	private String description;
	private List<ContainedProduct> containedProducts;

	public String getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public List<ContainedProduct> getContainedProducts() {
		return containedProducts;
	}

	public void setContainedProducts(List<ContainedProduct> containedProducts) {
		this.containedProducts = containedProducts;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}