package com.dtv.dcp.epoch.model.common;

import java.io.Serializable;

import com.dtv.dcp.epoch.model.ct.generic.GenericTypeIdBase;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomCouponBase implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5315441858084261324L;

	/** The id. */
	private GenericTypeIdBase type;

	/** The code. */
	private CustomCouponFields fields;

	public GenericTypeIdBase getType() {
		return type;
	}

	public void setType(GenericTypeIdBase type) {
		this.type = type;
	}

	public CustomCouponFields getFields() {
		return fields;
	}

	public void setFields(CustomCouponFields fields) {
		this.fields = fields;
	}

	@Override
	public String toString() {
		return "CustomCouponBase [type=" + type + ", fields=" + fields + "]";
	}

}