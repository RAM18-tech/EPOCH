package com.dtv.dcp.epoch.integration.rsn;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;

/**
 * The Class RSNRequest.
 *
 * @author ks5810
 */
@ApiModel(value = "BasePackage")
@JsonInclude(Include.NON_NULL)
public class BasePackage implements Serializable {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;	
	/** The sku. */
	private String sku;
	/** The accountType. */
	private  boolean contracted;
	/**
	 * @return the sku
	 */
	public String getSku() {
		return sku;
	}
	/**
	 * @param sku the sku to set
	 */
	public void setSku(String sku) {
		this.sku = sku;
	}
	/**
	 * @return the contracted
	 */
	public boolean isContracted() {
		return contracted;
	}
	/**
	 * @param contracted the contracted to set
	 */
	public void setContracted(boolean contracted) {
		this.contracted = contracted;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BasePackage [sku=" + sku + ", contracted=" + contracted + "]";
	}


}
