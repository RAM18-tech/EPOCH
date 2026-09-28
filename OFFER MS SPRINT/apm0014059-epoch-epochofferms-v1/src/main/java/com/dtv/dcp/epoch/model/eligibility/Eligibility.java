package com.dtv.dcp.epoch.model.eligibility;
 
import java.util.List;
import java.util.Map;
 
 
public class Eligibility {
 
    private Map<String,List<String>> customerEligibility;
    private Map<String,List<String>> channelDetails;
 
    private List<Map<String,List<String>>> agentEligibility;
 
    private Map<String,List<String>> keyEvaluation;
 
    public Map<String, List<String>> getCustomerEligibility() {
        return customerEligibility;
    }
 
    public void setCustomerEligibility(Map<String, List<String>> customerEligibility) {
        this.customerEligibility = customerEligibility;
    }
 
    public Map<String, List<String>> getChannelDetails() {
        return channelDetails;
    }
 
    public void setChannelDetails(Map<String, List<String>> channelDetails) {
        this.channelDetails = channelDetails;
    }
 
    public List<Map<String, List<String>>> getAgentEligibility() {
		return agentEligibility;
	}
 
	public void setAgentEligibility(List<Map<String, List<String>>> agentEligibility) {
		this.agentEligibility = agentEligibility;
	}
 
	public Map<String, List<String>> getKeyEvaluation() {
        return keyEvaluation;
    }
 
    public void setKeyEvaluation(Map<String, List<String>> keyEvaluation) {
        this.keyEvaluation = keyEvaluation;
    }
}
 