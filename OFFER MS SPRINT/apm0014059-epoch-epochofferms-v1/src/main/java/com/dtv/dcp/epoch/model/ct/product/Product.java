package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Product implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The id. */
	private String  id;
	
	/** The typeId. */
	private String typeId;
	
	/** The obj. */
	private ProductObj obj;

	private String productType;

	private String key;
	
	
	private Attributes attributes;

	public Attributes getAttributes() {
		return attributes;
	}

	public void setAttributes(Attributes attributes) {
		this.attributes = attributes;
	}
	 

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the typeId
	 */
	public String getTypeId() {
		return typeId;
	}

	/**
	 * @param typeId the typeId to set
	 */
	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	/**
	 * @return the obj
	 */
	public ProductObj getObj() {
		return obj;
	}

	/**
	 * @param obj the obj to set
	 */
	public void setObj(ProductObj obj) {
		this.obj = obj;
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Product [id=");
		builder.append(id);
		builder.append(", typeId=");
		builder.append(typeId);
		builder.append(", obj=");
		builder.append(obj);
		builder.append(", productType=");
		builder.append(productType);
		builder.append(", key=");
		builder.append(key);
		builder.append(", attributes=");
		builder.append(attributes);
		builder.append("]");
		return builder.toString();
	}
}
 