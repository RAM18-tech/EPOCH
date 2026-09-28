package com.dtv.dcp.epoch.model.ct.burn;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommerceBurnRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<BurnRequest> requests;

    public CommerceBurnRequest(List<BurnRequest> requests) {
        this.requests = requests;
    }

    public CommerceBurnRequest() {
        // Intentionally Left Blank For Jackson
    }

    @Override
    public String toString() {
        return "CommerceBurnRequest{" +
                "requests=" + requests +
                '}';
    }

    public List<BurnRequest> getRequests() {
        return requests;
    }

    public void setRequests(List<BurnRequest> requests) {
        this.requests = requests;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BurnRequest implements Serializable {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String billingBenefitCode;
        private int count;

        public BurnRequest(String billingBenefitCode, int count) {
            this.billingBenefitCode = billingBenefitCode;
            this.count = count;
        }

        public BurnRequest() {
            // Intentionally Left Blank For Jackson
        }

        @Override
        public String toString() {
            return "CommerceBurnRequest{" +
                    "billingBenefitCode='" + billingBenefitCode + '\'' +
                    ", count=" + count +
                    '}';
        }

        public String getBillingBenefitCode() {
            return billingBenefitCode;
        }

        public void setBillingBenefitCode(String billingBenefitCode) {
            this.billingBenefitCode = billingBenefitCode;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
}
