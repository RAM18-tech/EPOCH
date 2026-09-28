package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Variant implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String id;
	
	private CampaignWrapper attributes;
	
	private List<String> prices;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	
	public CampaignWrapper getAttributes() {
		return attributes;
	}

	public void setAttributes(CampaignWrapper attributes) {
		this.attributes = attributes;
	}

	public List<String> getPrices() {
		return prices;
	}

	public void setPrices(List<String> prices) {
		this.prices = prices;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Variant [id=");
		builder.append(id);
		builder.append(", attributes=");
		builder.append(attributes);
		builder.append(", prices=");
		builder.append(prices);
		builder.append("]");
		return builder.toString();
	}
	
	
	
}
