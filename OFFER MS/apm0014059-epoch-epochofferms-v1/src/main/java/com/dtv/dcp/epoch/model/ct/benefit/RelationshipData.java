package com.dtv.dcp.epoch.model.ct.benefit;

import java.io.Serializable;
import java.util.List;

public class RelationshipData implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<RelationSet> relationSet;
	
	public List<RelationSet> getRelationSet() {
		return relationSet;
	}
	public void setRelationSet(List<RelationSet> relationSet) {
		this.relationSet = relationSet;
	}
	@Override
	public String toString() {
		return "RelationshipData [relationSet=" + relationSet + "]";
	}
	
}

