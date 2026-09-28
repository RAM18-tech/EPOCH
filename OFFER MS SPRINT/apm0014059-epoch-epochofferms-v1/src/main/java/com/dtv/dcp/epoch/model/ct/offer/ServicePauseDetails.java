package com.dtv.dcp.epoch.model.ct.offer;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServicePauseDetails implements Serializable {

	private static final long serialVersionUID = 1L;

	private String status;
	private String startDate;
	private String endDate;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	@Override
	public int hashCode() {
		return Objects.hash(endDate, startDate, status);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServicePauseDetails other = (ServicePauseDetails) obj;
		return Objects.equals(endDate, other.endDate) && Objects.equals(startDate, other.startDate)
				&& Objects.equals(status, other.status);
	}

	@Override
	public String toString() {
		return "ServicePauseDetails [status=" + status + ", startDate=" + startDate + ", endDate=" + endDate + "]";
	}

}