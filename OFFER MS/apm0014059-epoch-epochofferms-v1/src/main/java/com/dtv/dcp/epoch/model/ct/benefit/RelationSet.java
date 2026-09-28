package com.dtv.dcp.epoch.model.ct.benefit;

import java.io.Serializable;
import java.util.List;

public class RelationSet implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List<RelatedData> relatedTo;

	public List<RelatedData> getRelatedTo() {
		return relatedTo;
	}

	public void setRelatedTo(List<RelatedData> relatedTo) {
		this.relatedTo = relatedTo;
	}

	@Override
	public String toString() {
		return "RelationSet [relatedTo=" + relatedTo + "]";
	}
	
}