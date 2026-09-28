package com.dtv.dcp.epoch.model.ct.offer;

import java.io.Serializable;
import java.util.List;

public class GlobalEligibilityRule implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String predicate;
	
	private List<GlobalEligibilityAttribute> eligibilityAtributes;

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	public List<GlobalEligibilityAttribute> getEligibilityAtributes() {
		return eligibilityAtributes;
	}

	public void setEligibilityAtributes(List<GlobalEligibilityAttribute> eligibilityAtributes) {
		this.eligibilityAtributes = eligibilityAtributes;
	}

	@Override
	public String toString() {
		return "GlobalEligibilityRule [predicate=" + predicate + ", eligibilityAtributes=" + eligibilityAtributes + "]";
	}
	
}
