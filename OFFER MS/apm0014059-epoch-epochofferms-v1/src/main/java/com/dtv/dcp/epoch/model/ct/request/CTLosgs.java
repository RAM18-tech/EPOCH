package com.dtv.dcp.epoch.model.ct.request;


import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.common.LineItem;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *This Bean class is for lineitems  of the cart for offer validation
 *
 */

/**
 * Created by nk3077 on 05/08/2018.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class CTLosgs implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;


    /** The id  */
    @JsonProperty("losgId")
    private String id;
    
    /** ThelosgType. */
    @JsonProperty("losgType")
    private String losgType;
    
    
    /** The lineItems. */
    @JsonProperty("lineItems")
    private List<LineItem> lineItems;


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
	public List<LineItem> getLineItems() {
		return lineItems;
	}


	/**
	 * @param lineItems the lineItems to set
	 */
	public void setLineItems(List<LineItem> lineItems) {
		this.lineItems = lineItems;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "losgs: {" + (id != null ? "id=" + id + ", " : "")
				+ (losgType != null ? "losgType=" + losgType + ", " : "")
				+ (lineItems != null ? "lineItems=" + lineItems : "") + "}";
	}

	

}
