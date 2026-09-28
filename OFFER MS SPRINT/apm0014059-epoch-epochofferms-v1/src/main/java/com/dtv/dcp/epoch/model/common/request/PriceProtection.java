package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;
import java.util.Objects;

public class PriceProtection implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	private String startDate;
	private String enddate;

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEnddate() {
		return enddate;
	}

	public void setEnddate(String enddate) {
		this.enddate = enddate;
	}

	@Override
	public int hashCode() {
		return Objects.hash(enddate, startDate);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PriceProtection other = (PriceProtection) obj;
		return Objects.equals(enddate, other.enddate) && Objects.equals(startDate, other.startDate);
	}

	@Override
	public String toString() {
		return "PriceProtection [startDate=" + startDate + ", enddate=" + enddate + "]";
	}

}