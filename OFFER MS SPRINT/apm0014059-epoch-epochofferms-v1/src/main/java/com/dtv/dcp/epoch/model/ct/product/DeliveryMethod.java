package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DeliveryMethod implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private List<DeliveryMethodAttributes> deliveryMethod;

	public List<DeliveryMethodAttributes> getDeliveryMethod() {
		return deliveryMethod;
	}

	public void setDeliveryMethod(List<DeliveryMethodAttributes> deliveryMethod) {
		this.deliveryMethod = deliveryMethod;
	}

		
}
