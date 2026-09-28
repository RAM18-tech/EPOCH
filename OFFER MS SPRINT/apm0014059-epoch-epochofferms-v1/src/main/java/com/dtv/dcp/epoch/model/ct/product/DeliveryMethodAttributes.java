package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliveryMethodAttributes implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String salesChannel;
	
	private String customerSegments;
	
	private String deliveryMethod;
    
	private String partnerType;

	public String getPartnerType() {
		return partnerType;
	}

	public void setPartnerType(String partnerType) {
		this.partnerType = partnerType;
	}
	public String getSalesChannel() {
		return salesChannel;
	}

	public void setSalesChannel(String salesChannel) {
		this.salesChannel = salesChannel;
	}

	public String getDeliveryMethod() {
		return deliveryMethod;
	}

	public void setDeliveryMethod(String deliveryMethod) {
		this.deliveryMethod = deliveryMethod;
	}

	public String getCustomerSegments() {
		return customerSegments;
	}

	public void setCustomerSegments(String customerSegments) {
		this.customerSegments = customerSegments;
	}
	
}
