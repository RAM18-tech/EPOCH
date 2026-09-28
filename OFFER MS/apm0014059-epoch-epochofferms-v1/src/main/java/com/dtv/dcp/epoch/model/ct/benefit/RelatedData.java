package com.dtv.dcp.epoch.model.ct.benefit;

import java.io.Serializable;

public class RelatedData implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer lineItemId;
	
	private String relationshipType;

	public Integer getLineItemId() {
		return lineItemId;
	}

	public void setLineItemId(Integer lineItemId) {
		this.lineItemId = lineItemId;
	}

	public String getRelationshipType() {
		return relationshipType;
	}

	public void setRelationshipType(String relationshipType) {
		this.relationshipType = relationshipType;
	}

	@Override
	public String toString() {
		return "RelatedData [lineItemId=" + lineItemId + ", relationshipType=" + relationshipType + "]";
	}
	
}
