package com.dtv.dcp.epoch.model.ct.request;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CTProductNextBillingDate implements Serializable {


    /**
	 *
	 */
	private static final long serialVersionUID = 1L;


    private String nextBillingDate;

    private List<String> contractIndicator;

    public String getNextBillingDate() {return nextBillingDate; }

    public void setNextBillingDate(String nextBillingDate) {this.nextBillingDate = nextBillingDate; }

    public List<String> getContractIndicator() {
		return contractIndicator;
	}

	public void setContractIndicator(List<String> contractIndicator) {
		this.contractIndicator = contractIndicator;
	}

	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PriceProtection [value=");
        builder.append(nextBillingDate);
        builder.append(", contractIndicator");
        builder.append(contractIndicator);
        builder.append("]");
        return builder.toString();
    }

}
