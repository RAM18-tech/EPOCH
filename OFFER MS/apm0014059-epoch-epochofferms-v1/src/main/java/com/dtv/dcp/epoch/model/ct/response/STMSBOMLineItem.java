package com.dtv.dcp.epoch.model.ct.response;

import java.io.Serializable;

public class STMSBOMLineItem implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private STMSLineItem lineItem;

	public STMSLineItem getLineItem() {
		return lineItem;
	}

	public void setLineItem(STMSLineItem lineItem) {
		this.lineItem = lineItem;
	}

	@Override
	public String toString() {
		return "STMSBOMLineItem [lineItem=" + lineItem + "]";
	}
	
}
