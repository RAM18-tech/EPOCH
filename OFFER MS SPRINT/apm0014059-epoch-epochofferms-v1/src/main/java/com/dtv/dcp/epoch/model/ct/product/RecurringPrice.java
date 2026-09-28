package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;

public class RecurringPrice implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer sequence;
	private Double price;
	private String frequencyOfCharge;
	private Integer numberOfCharges;
	
	public Integer getSequence() {
		return sequence;
	}
	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public String getFrequencyOfCharge() {
		return frequencyOfCharge;
	}
	public void setFrequencyOfCharge(String frequencyOfCharge) {
		this.frequencyOfCharge = frequencyOfCharge;
	}
	public Integer getNumberOfCharges() {
		return numberOfCharges;
	}
	public void setNumberOfCharges(Integer numberOfCharges) {
		this.numberOfCharges = numberOfCharges;
	}
	
	@Override
	public String toString() {
		return "RecurringPrice [sequence=" + sequence + ", price=" + price + ", frequencyOfCharge=" + frequencyOfCharge
				+ ", numberOfCharges=" + numberOfCharges + "]";
	}
	
}