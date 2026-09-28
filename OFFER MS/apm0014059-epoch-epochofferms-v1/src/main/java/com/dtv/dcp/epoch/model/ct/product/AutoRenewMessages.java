package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class AutoRenewMessages implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	
	/** The optInDisclosure. */
	@JsonProperty("optInDisclosure")
	private String optInDisclosure;

	/** The optOutDisclosure. */
	@JsonProperty("optOutDisclosure")
	private String optOutDisclosure;
	
	/** The offSeasonMessage. */
	@JsonProperty("offSeasonMessage")
	private String offSeasonMessage;
	public String getOptInDisclosure() {
		return optInDisclosure;
	}

	public void setOptInDisclosure(String optInDisclosure) {
		this.optInDisclosure = optInDisclosure;
	}

	public String getOptOutDisclosure() {
		return optOutDisclosure;
	}

	public void setOptOutDisclosure(String optOutDisclosure) {
		this.optOutDisclosure = optOutDisclosure;
	}

	public String getOffSeasonMessage() {
		return offSeasonMessage;
	}

	public void setOffSeasonMessage(String offSeasonMessage) {
		this.offSeasonMessage = offSeasonMessage;
	}
	
}
