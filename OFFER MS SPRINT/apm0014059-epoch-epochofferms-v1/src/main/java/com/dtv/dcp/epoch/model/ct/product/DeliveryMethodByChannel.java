package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DeliveryMethodByChannel implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private List<String> salesChannel;
	
	private List<String> customerSegments;
	
	private List<String> deliveryMethod;

	private List<String> partnerType;

	public List<String> getSalesChannel() {
		return salesChannel;
	}

	public void setSalesChannel(List<String> salesChannel) {
		this.salesChannel = salesChannel;
	}

	public List<String> getCustomerSegments() {
		return customerSegments;
	}

	public void setCustomerSegments(List<String> customerSegments) {
		this.customerSegments = customerSegments;
	}

	public List<String> getDeliveryMethod() {
		return deliveryMethod;
	}

	public void setDeliveryMethod(List<String> deliveryMethod) {
		this.deliveryMethod = deliveryMethod;
	}

	public List<String> getPartnerType() {
		return partnerType;
	}

	public void setPartnerType(List<String> partnerType) {
		this.partnerType = partnerType;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DeliveryMethodInfo [salesChannel=");
		builder.append(salesChannel);
		builder.append(", customerSegments=");
		builder.append(customerSegments);
		builder.append(", deliveryMethod=");
		builder.append(deliveryMethod);
		builder.append(", partnerType=");
		builder.append(partnerType);
		builder.append("]");
		return builder.toString();
	}
}
