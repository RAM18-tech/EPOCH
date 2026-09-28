package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import com.dtv.dcp.epoch.model.common.AdditionalDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AdditionalAccountDetails implements Serializable {

	private static final long serialVersionUID = 1L;

	private String status;
	private String startDate;
	private String endDate;

	private List<AdditionalDetails> additonalInfo;

	public List<AdditionalDetails> getAdditonalInfo() {
		return additonalInfo;
	}

	public void setAdditonalInfo(List<AdditionalDetails> additonalInfo) {
		this.additonalInfo = additonalInfo;
	}

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
		AdditionalAccountDetails other = (AdditionalAccountDetails) obj;
		return Objects.equals(endDate, other.endDate) && Objects.equals(startDate, other.startDate)
				&& Objects.equals(status, other.status);
	}

	@Override
	public String toString() {
		return "AdditionalAccountDetails [status=" + status + ", startDate=" + startDate + ", endDate=" + endDate + "]";
	}

}