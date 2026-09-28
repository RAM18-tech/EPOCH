package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ActionRule implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private List<ActionRuleAttributes> actionRule;

	public List<ActionRuleAttributes> getActionRule() {
		return actionRule;
	}

	public void setActionRule(List<ActionRuleAttributes> actionRule) {
		this.actionRule = actionRule;
	}
	
}
