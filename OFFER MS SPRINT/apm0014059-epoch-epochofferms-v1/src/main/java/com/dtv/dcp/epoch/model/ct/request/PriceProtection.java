package com.dtv.dcp.epoch.model.ct.request;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceProtection implements Serializable {


    /**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The startDate. */
    private String startDate;

    /** The endDate. */
    private String endDate;

    private String nextBillingDate;

    private String currentDate;
    
    private List<String> contractIndicator;

    private Boolean isHogWindowMatch;

    /**
     * @return the startDate
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the endDate
     */
    public String getEndDate() {
        return endDate;
    }

    /**
     * @param endDate the startDate to set
     */
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getNextBillingDate() {return nextBillingDate; }

    public void setNextBillingDate(String nextBillingDate) {this.nextBillingDate = nextBillingDate; }

    public String getCurrentDate() {return currentDate;}

    public void setCurrentDate(String currentDate) {this.currentDate =currentDate;}

    public List<String> getContractIndicator() {
		return contractIndicator;
	}

	public void setContractIndicator(List<String> contractIndicator) {
		this.contractIndicator = contractIndicator;
	}

    public Boolean getIsHogWindowMatch() { return isHogWindowMatch; }

    public void setIsHogWindowMatch(Boolean isHogWindowMatch) { this.isHogWindowMatch = isHogWindowMatch; }

	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PriceProtection [value=");
        builder.append(", startDate=");
        builder.append(startDate);
        builder.append(", endDate=");
        builder.append(endDate);
        builder.append(", nextBillingDate=");
        builder.append(nextBillingDate);
        builder.append(", currentDate");
        builder.append(currentDate);
        builder.append(", isHogWindowMatch=");
        builder.append(isHogWindowMatch);
        builder.append("]");
        return builder.toString();
    }

}
