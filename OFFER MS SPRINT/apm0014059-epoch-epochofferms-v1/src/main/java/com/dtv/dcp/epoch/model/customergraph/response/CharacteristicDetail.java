package com.dtv.dcp.epoch.model.customergraph.response;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CharacteristicDetail implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String value;
	String valueForDisplay;
	ProductSpecificationCharacteristic productSpecificationCharacteristic;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValueForDisplay() {
		return valueForDisplay;
	}

	public void setValueForDisplay(String valueForDisplay) {
		this.valueForDisplay = valueForDisplay;
	}

	public ProductSpecificationCharacteristic getProductSpecificationCharacteristic() {
		return productSpecificationCharacteristic;
	}

	public void setProductSpecificationCharacteristic(
			ProductSpecificationCharacteristic productSpecificationCharacteristic) {
		this.productSpecificationCharacteristic = productSpecificationCharacteristic;
	}
}