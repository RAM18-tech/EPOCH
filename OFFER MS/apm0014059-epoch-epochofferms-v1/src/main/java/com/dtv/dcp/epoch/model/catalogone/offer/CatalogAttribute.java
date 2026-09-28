/**
 * 
 */
package com.dtv.dcp.epoch.model.catalogone.offer;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author dr000y
 *
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CatalogAttribute implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7123026433392938339L;

	private String name;
	private String displayName;
	private List<AttributeValue> attributeValues;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return the attributeValues
	 */
	public List<AttributeValue> getAttributeValues() {
		return attributeValues;
	}

	/**
	 * @param attributeValues the attributeValues to set
	 */
	public void setAttributeValues(List<AttributeValue> attributeValues) {
		this.attributeValues = attributeValues;
	}

	@Override
	public String toString() {
		return "CatalogAttribute [name=" + name + ", displayName=" + displayName + ", attributeValues="
				+ attributeValues + "]";
	}

}
