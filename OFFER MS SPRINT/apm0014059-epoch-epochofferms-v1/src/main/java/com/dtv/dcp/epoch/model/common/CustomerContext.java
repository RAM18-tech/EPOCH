package com.dtv.dcp.epoch.model.common;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.model.common.request.AdditionalAccountDetails;
import com.dtv.dcp.epoch.model.common.request.CartProduct;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerContext implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	private List<ExistingProductFamily> existingProductFamily;

	@JsonProperty(Constants.OTT_PRODUCT_FAMILY)
	private CartProduct ott;
	
	@JsonProperty("additionalAccountDetails")
	private AdditionalAccountDetails additionalAccountDetails;

	public AdditionalAccountDetails getAdditionalAccountDetails() {
		return additionalAccountDetails;
	}

	public void setAdditionalAccountDetails(AdditionalAccountDetails additionalAccountDetails) {
		this.additionalAccountDetails = additionalAccountDetails;
	}

	public CartProduct getOtt() {
		return ott;
	}

	public void setOtt(CartProduct ott) {
		this.ott = ott;
	}



	public List<ExistingProductFamily> getExistingProductFamily() {
		return existingProductFamily;
	}

	public void setExistingProductFamily(List<ExistingProductFamily> existingProductFamily) {
		this.existingProductFamily = existingProductFamily;
	}

	@Override
	public String toString() {
		return "CustomerContext [existingProductFamily=" + existingProductFamily + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(existingProductFamily);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CustomerContext other = (CustomerContext) obj;
		return Objects.equals(existingProductFamily, other.existingProductFamily);
	}

}