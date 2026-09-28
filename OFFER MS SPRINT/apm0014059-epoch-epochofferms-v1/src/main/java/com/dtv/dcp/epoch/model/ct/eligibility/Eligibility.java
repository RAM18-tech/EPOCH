package com.dtv.dcp.epoch.model.ct.eligibility;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.ct.generic.GenericComputedRules;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Eligibility implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The computedRules. */
	private GenericComputedRules computedRules;
	
	/** The constraints. */
	private List<Constraint> constraints;

	/**
	 * @return the computedRules
	 */
	public GenericComputedRules getComputedRules() {
		return computedRules;
	}

	/**
	 * @param computedRules the computedRules to set
	 */
	public void setComputedRules(GenericComputedRules computedRules) {
		this.computedRules = computedRules;
	}


	/**
	 * @return the constraints
	 */
	public List<Constraint> getConstraints() {
		return constraints;
	}

	/**
	 * @param constraints the constraints to set
	 */
	public void setConstraints(List<Constraint> constraints) {
		this.constraints = constraints;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Eligibility [computedRules=");
		builder.append(computedRules);
		builder.append(", constraints=");
		builder.append(constraints);
		builder.append("]");
		return builder.toString();
	}
	
	

	
}
