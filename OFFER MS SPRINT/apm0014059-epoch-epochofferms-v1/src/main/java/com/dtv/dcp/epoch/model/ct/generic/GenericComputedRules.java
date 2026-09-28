package com.dtv.dcp.epoch.model.ct.generic;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericComputedRules implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The predicate. */
	private String predicate;
	
	/** The groupingOperator. */
	private String groupingOperator;
	
	/** The rules. */
	private List<GenericRule> rules;

	/**
	 * @return the predicate
	 */
	public String getPredicate() {
		return predicate;
	}

	/**
	 * @param predicate the predicate to set
	 */
	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	/**
	 * @return the groupingOperator
	 */
	public String getGroupingOperator() {
		return groupingOperator;
	}

	/**
	 * @param groupingOperator the groupingOperator to set
	 */
	public void setGroupingOperator(String groupingOperator) {
		this.groupingOperator = groupingOperator;
	}

	/**
	 * @return the rules
	 */
	public List<GenericRule> getRules() {
		return rules;
	}

	/**
	 * @param rules the rules to set
	 */
	public void setRules(List<GenericRule> rules) {
		this.rules = rules;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GenericComputedRules [predicate=");
		builder.append(predicate);
		builder.append(", groupingOperator=");
		builder.append(groupingOperator);
		builder.append(", rules=");
		builder.append(rules);
		builder.append("]");
		return builder.toString();
	}
	

}
