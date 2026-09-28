package com.dtv.dcp.epoch.model.catalogone.offer;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author dr000y
 *
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductSpecification implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String id;

	private String name;

	private String type;

	private String productSpecificationTypeId;

	private String productSpecificationTypeName;

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return this.id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}

	public void setProductSpecificationTypeId(String productSpecificationTypeId) {
		this.productSpecificationTypeId = productSpecificationTypeId;
	}

	public String getProductSpecificationTypeId() {
		return this.productSpecificationTypeId;
	}

	public void setProductSpecificationTypeName(String productSpecificationTypeName) {
		this.productSpecificationTypeName = productSpecificationTypeName;
	}

	public String getProductSpecificationTypeName() {
		return this.productSpecificationTypeName;
	}

	@Override
	public String toString() {
		return "ProductSpecification [id=" + id + ", name=" + name + ", type=" + type + ", productSpecificationTypeId="
				+ productSpecificationTypeId + ", productSpecificationTypeName=" + productSpecificationTypeName + "]";
	}

}
