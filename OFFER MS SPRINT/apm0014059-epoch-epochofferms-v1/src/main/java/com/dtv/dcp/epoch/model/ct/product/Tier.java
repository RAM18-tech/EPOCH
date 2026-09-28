package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Tier implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The minimumQuantity. */
	private Integer minimumQuantity;
	
	/** The value. */
	private Value value;

	/**
	 * @return the minimumQuantity
	 */
	public Integer getMinimumQuantity() {
		return minimumQuantity;
	}

	/**
	 * @param minimumQuantity the minimumQuantity to set
	 */
	public void setMinimumQuantity(Integer minimumQuantity) {
		this.minimumQuantity = minimumQuantity;
	}

	/**
	 * @return the value
	 */
	public Value getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Value value) {
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Tier [minimumQuantity=" + minimumQuantity + ", value=" + value + "]";
	}

}
