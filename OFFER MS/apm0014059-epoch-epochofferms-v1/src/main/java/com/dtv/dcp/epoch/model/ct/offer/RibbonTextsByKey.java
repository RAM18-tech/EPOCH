package com.dtv.dcp.epoch.model.ct.offer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RibbonTextsByKey implements Serializable
{
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;
	private String ribbonText;

	public String getRibbonText() {
		return ribbonText;
	}

	public void setRibbonText(String ribbonText) {
		this.ribbonText = ribbonText;
	}

	@JsonIgnore
	public boolean isEmpty() {
		return StringUtils.isBlank(ribbonText);
	}
}