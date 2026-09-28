package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.common.Constants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerContext implements Serializable {

    /**
     * The Constant serialVersionUID.
     */
	private static final long serialVersionUID = 1L;

    @JsonProperty("existingProductFamily")
    private List<String> existingProductFamily;
    
	@JsonProperty("existingAccounts")
    private List<String> existingAccounts;


    @JsonProperty(Constants.WIRELESS_PRODUCT_FAMILY)
    private CartProduct wireless;


    @JsonProperty(Constants.BB_PRODUCT_FAMILY)
    private CartProduct broadband;

    @JsonProperty(Constants.IPTV_PRODUCT_FAMILY)
    private CartProduct iptv;

    @JsonProperty(Constants.SATELLITE_PRODUCT_FAMILY)
    private CartProduct satellite;

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

	/**
     * @return the existingAccounts
     */
    public List<String> getExistingProductFamily() {
        return existingProductFamily;
    }

    /**
     * @param existingProductFamily the existingAccounts to set
     */
    public void setExistingProductFamily(List<String> existingProductFamily) {
        this.existingProductFamily = existingProductFamily;
    }

    /**
     * @return the ott
     */
    public CartProduct getOtt() {
        return ott;
    }

    /**
     * @param ott the ott to set
     */
    public void setOtt(CartProduct ott) {
        this.ott = ott;
    }

    /**
     * @return the broadband
     */
    public CartProduct getBroadband() {
        return broadband;
    }

    /**
     * @param broadband the broadband to set
     */
    public void setBroadband(CartProduct broadband) {
        this.broadband = broadband;
    }

    /**
     * @return the iptv
     */
    public CartProduct getIptv() {
        return iptv;
    }

    /**
     * @param iptv the iptv to set
     */
    public void setIptv(CartProduct iptv) {
        this.iptv = iptv;
    }

    /**
     * @return the satellite
     */
    public CartProduct getSatellite() {
        return satellite;
    }

    /**
     * @param satellite the satellite to set
     */
    public void setSatellite(CartProduct satellite) {
        this.satellite = satellite;
    }

    /**
     * @return the wireless
     */
    public CartProduct getWireless() {
        return wireless;
    }

    /**
     * @param wireless the wireless to set
     */
    public void setWireless(CartProduct wireless) {
        this.wireless = wireless;
    }

    /**
	 * @return the existingAccounts
	 */
	public List<String> getExistingAccounts() {
		return existingAccounts;
	}

	/**
	 * @param existingAccounts the existingAccounts to set
	 */
	public void setExistingAccounts(List<String> existingAccounts) {
		this.existingAccounts = existingAccounts;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "customerContext: {"
				+ (existingProductFamily != null ? "existingProductFamily=" + existingProductFamily + ", " : "")
				+ (existingAccounts != null ? "existingAccounts=" + existingAccounts + ", " : "")
				+ (wireless != null ? "wireless=" + wireless + ", " : "")
				+ (broadband != null ? "broadband=" + broadband + ", " : "")
				+ (iptv != null ? "iptv=" + iptv + ", " : "")
				+ (satellite != null ? "satellite=" + satellite + ", " : "") 
				+ (ott != null ? "ott=" + ott : "")
				+ (additionalAccountDetails != null ? "additionalAccountDetails=" + additionalAccountDetails : "") + "}";
	}

	

}
