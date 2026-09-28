package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.model.common.Location;
import com.dtv.dcp.epoch.model.ct.request.CartOffer;
import com.dtv.dcp.epoch.model.ct.request.Product;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by nk3077 on 07/30/2019.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CartContext implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	@JsonProperty("salesChannel")
	private String salesChannel;
	@JsonProperty("validateOnlyReward")
	private Boolean validateOnlyReward;
	@JsonProperty("validateAvailableQuota")
	private Boolean validateAvailableQuota;
	@JsonProperty("lobDetails")
	private List<LOBDetails> lobDetails;
	@JsonProperty("location")
	private Location location;

	/**
	 * The cpopOfferId.
	 */
	@JsonProperty(Constants.CPOPOFFERIDS)
	private List<String> cpopOfferIds;

	@JsonProperty(Constants.CPOPOFFERCODES)
	private List<String> cpopOfferCodes;
	
	private List<String> epochOfferCodes;

	private List<String> cpopProductIds;

	@JsonProperty(Constants.BB_PRODUCT_FAMILY)
	private CartProduct broadband;

	@JsonProperty(Constants.IPTV_PRODUCT_FAMILY)
	private CartProduct iptv;

	@JsonProperty(Constants.SATELLITE_PRODUCT_FAMILY)
	private CartProduct satellite;

	@JsonProperty(Constants.OTT_PRODUCT_FAMILY)
	private CartProduct ott;
	
    @JsonProperty("cartOffers")
	private List<CartOffer> cartOffers;
    
    @JsonProperty("cartProducts")
    private List<Product> cartProducts;

	@JsonProperty(Constants.WIRELESS_PRODUCT_FAMILY)
	private CartProduct wirelesss;

	public List<String> getEpochOfferCodes() {
		return epochOfferCodes;
	}

	public void setEpochOfferCodes(List<String> epochOfferCodes) {
		this.epochOfferCodes = epochOfferCodes;
	}

	public String getSalesChannel() {
		return salesChannel;
	}

	public void setSalesChannel(String salesChannel) {
		this.salesChannel = salesChannel;
	}

	public Boolean getValidateOnlyReward() {
		return validateOnlyReward;
	}

	public void setValidateOnlyReward(Boolean validateOnlyReward) {
		this.validateOnlyReward = validateOnlyReward;
	}

	public Boolean getValidateAvailableQuota() {
		return validateAvailableQuota;
	}

	public void setValidateAvailableQuota(Boolean validateAvailableQuota) {
		this.validateAvailableQuota = validateAvailableQuota;
	}

	public List<LOBDetails> getLobDetails() {
		return lobDetails;
	}

	public void setLobDetails(List<LOBDetails> lobDetails) {
		this.lobDetails = lobDetails;
	}

	public List<String> getCpopOfferCodes() {
		return cpopOfferCodes;
	}

	public void setCpopOfferCodes(List<String> cpopOfferCodes) {
		this.cpopOfferCodes = cpopOfferCodes;
	}

	public List<String> getCpopOfferIds() {
		return cpopOfferIds;
	}

	public void setCpopOfferIds(List<String> cpopOfferIds) {
		this.cpopOfferIds = cpopOfferIds;
	}

	public CartProduct getBroadband() {
		return broadband;
	}

	public void setBroadband(CartProduct broadband) {
		this.broadband = broadband;
	}

	public CartProduct getIptv() {
		return iptv;
	}

	public void setIptv(CartProduct iptv) {
		this.iptv = iptv;
	}

	public CartProduct getSatellite() {
		return satellite;
	}

	public void setSatellite(CartProduct satellite) {
		this.satellite = satellite;
	}

	public CartProduct getOtt() {
		return ott;
	}

	public void setOtt(CartProduct ott) {
		this.ott = ott;
	}

	public CartProduct getWirelesss() {
		return wirelesss;
	}

	public void setWirelesss(CartProduct wirelesss) {
		this.wirelesss = wirelesss;
	}

	public List<String> getCpopProductIds() {
		return cpopProductIds;
	}

	public void setCpopProductIds(List<String> cpopProductIds) {
		this.cpopProductIds = cpopProductIds;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
	public List<CartOffer> getCartOffers() {
		return cartOffers;
	}

	public void setCartOffers(List<CartOffer> cartOffers) {
		this.cartOffers = cartOffers;
	}
	
	public List<Product> getCartProducts() {
		return cartProducts;
	}

	public void setCartProducts(List<Product> cartProducts) {
		this.cartProducts = cartProducts;
	}

	@Override
	public String toString() {
		return "CartContext: {" + (salesChannel != null ? "salesChannel=" + salesChannel + ", " : "")
				+ (validateOnlyReward != null ? "validateOnlyReward=" + validateOnlyReward + ", " : "")
				+ (validateAvailableQuota != null ? "validateAvailableQuota=" + validateAvailableQuota + ", " : "")
				+ (lobDetails != null ? "lobDetails=" + lobDetails + ", " : "")
				+ (location != null ? "location=" + location + ", " : "")
				+ (cpopOfferIds != null ? "cpopOfferIds=" + cpopOfferIds + ", " : "")
				+ (cpopOfferCodes != null ? "cpopOfferCodes=" + cpopOfferCodes + ", " : "")
				+ (epochOfferCodes != null ? "epochOfferCodes=" + epochOfferCodes + ", " : "")
				+ (cpopProductIds != null ? "cpopProductIds=" + cpopProductIds + ", " : "")
				+ (broadband != null ? "broadband=" + broadband + ", " : "")
				+ (iptv != null ? "iptv=" + iptv + ", " : "")
				+ (satellite != null ? "satellite=" + satellite + ", " : "") + (ott != null ? "ott=" + ott : "")
				+ (cartOffers != null ? "cartOffers=" + cartOffers + ", " : "")
				+ (cartProducts != null ? "cartProducts=" + cartProducts + ", " : "")
				+ (wirelesss != null ? "wirelesss=" + wirelesss + ", " : "") + "}";
	}

}
