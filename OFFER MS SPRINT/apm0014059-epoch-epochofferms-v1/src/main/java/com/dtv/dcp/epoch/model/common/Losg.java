package com.dtv.dcp.epoch.model.common;


import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *This Bean class is for lineitems  of the cart for offer validation
 *
 */

/**
 * Created by nk3077 on 05/08/2018.
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Losg implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;


    /** The id  */
    @JsonProperty("id")
    private String id;
    
    /** ThelosgType. */
    @JsonProperty("losgType")
    private String losgType;
    
    
    /** The lineItems. */
    @JsonProperty("lineItems")
    private Map<String, LineItem> lineItems;


	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}


	/**
	 * @return the losgType
	 */
	public String getLosgType() {
		return losgType;
	}


	/**
	 * @param losgType the losgType to set
	 */
	public void setLosgType(String losgType) {
		this.losgType = losgType;
	}


	/**
	 * @return the lineItems
	 */
	public Map<String, LineItem> getLineItems() {
		return lineItems;
	}


	/**
	 * @param lineItems the lineItems to set
	 */
	public void setLineItems(Map<String, LineItem> lineItems) {
		this.lineItems = lineItems;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Losg [id=");
		builder.append(id);
		builder.append(", losgType=");
		builder.append(losgType);
		builder.append(", lineItems=");
		builder.append(lineItems);
		builder.append("]");
		return builder.toString();
	}

}
