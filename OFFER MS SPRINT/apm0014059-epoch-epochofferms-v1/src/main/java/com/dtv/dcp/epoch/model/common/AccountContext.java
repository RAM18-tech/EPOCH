package com.dtv.dcp.epoch.model.common;


import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class Context.
 * @author dr000y
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountContext implements Serializable {


    @JsonProperty("BAN")
    private String BAN;
    
 
	public String getBAN() {
		return BAN;
	}


	public void setBAN(String bAN) {
		BAN = bAN;
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AccountContext [BAN=");
		builder.append(BAN);
		builder.append("]");
		return builder.toString();
	}

}
