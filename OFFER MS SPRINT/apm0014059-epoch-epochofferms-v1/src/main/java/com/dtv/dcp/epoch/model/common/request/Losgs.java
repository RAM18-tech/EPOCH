/**
 * 
 */
package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.common.LineItem;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author pradiptaa.paul
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Losgs implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;
	
	private List<LineItem> lineItems;
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
		return "Losgs [" + (lineItems != null ? "lineItems=" + lineItems : "") + "]";
	}
	
	

}
