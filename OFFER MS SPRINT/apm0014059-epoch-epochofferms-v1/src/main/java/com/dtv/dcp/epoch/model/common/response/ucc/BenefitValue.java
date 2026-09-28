package com.dtv.dcp.epoch.model.common.response.ucc;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BenefitValue implements Serializable {

	private static final long serialVersionUID = 1L;

	private Double dollarAmount;
	private String percentage;

	public Double getDollarAmount() {
		return dollarAmount;
	}

	public void setDollarAmount(Double dollarAmount) {
		this.dollarAmount = dollarAmount;
	}

	public String getPercentage() {
		return percentage;
	}

	public void setPercentage(String percentage) {
		this.percentage = percentage;
	}

	@Override
	public String toString() {
		return "BenefitValue [dollarAmount=" + dollarAmount + ", percentage=" + percentage + "]";
	}

}