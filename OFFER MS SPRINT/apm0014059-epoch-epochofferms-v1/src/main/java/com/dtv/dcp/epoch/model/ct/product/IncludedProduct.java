package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;

import com.dtv.dcp.epoch.model.ct.generic.GenericLocaleBase;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class IncludedProduct implements Serializable{
    
	/**
	 * The Class Attributes
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private String typeId;
	private String productType;
	private String key;
	private ProductObj obj;
	private GenericLocaleBase name;
	private GenericLocaleBase description;
	
	/**
	 * @return the name
	 */
	public GenericLocaleBase getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(GenericLocaleBase name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public GenericLocaleBase getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(GenericLocaleBase description) {
		this.description = description;
	}
	
	public ProductObj getObj() {
		return obj;
	}
	public void setObj(ProductObj obj) {
		this.obj = obj;
	}	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTypeId() {
		return typeId;
	}
	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}
	public String getProductType() {
		return productType;
	}
	public void setProductType(String productType) {
		this.productType = productType;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
}
