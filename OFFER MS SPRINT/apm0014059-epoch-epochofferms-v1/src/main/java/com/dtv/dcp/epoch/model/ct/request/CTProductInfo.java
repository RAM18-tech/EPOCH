package com.dtv.dcp.epoch.model.ct.request;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CTProductInfo implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
    private static final long serialVersionUID = 1L;
    @JsonProperty("billingProductCode")
    private String billingProductCode;

    @JsonProperty("productId")
    private String productId;

    @JsonProperty("productType")
    private String productType;

    @JsonProperty("streamType")
    private String streamType;

    public String getBillingProductCode() {
		return billingProductCode;
	}

	public void setBillingProductCode(String billingProductCode) {
		this.billingProductCode = billingProductCode;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getStreamType() {
		return streamType;
	}

	public void setStreamType(String streamType) {
		this.streamType = streamType;
	}

	public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

	@Override
	public String toString() {
		return "products: {" + (billingProductCode != null ? "billingProductCode=" + billingProductCode + ", " : "")
				+ (productId != null ? "productId=" + productId + ", " : "")
				+ (productType != null ? "productType=" + productType + ", " : "")
				+ (streamType != null ? "streamType=" + streamType : "") + "}";
	}

    
}
