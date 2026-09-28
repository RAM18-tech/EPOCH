package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;

public class RecurringPriceValue implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private RecurringPrice recurringPrice;

	public RecurringPrice getRecurringPrice() {
		return recurringPrice;
	}

	public void setRecurringPrice(RecurringPrice recurringPrice) {
		this.recurringPrice = recurringPrice;
	}

	@Override
	public String toString() {
		return "RecurringPriceValue [recurringPrice=" + recurringPrice + "]";
	}
	
}
