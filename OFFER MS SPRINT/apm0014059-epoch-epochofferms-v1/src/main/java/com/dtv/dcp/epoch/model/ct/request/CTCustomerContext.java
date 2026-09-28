package com.dtv.dcp.epoch.model.ct.request;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.common.Constants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CTCustomerContext implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@JsonProperty("existingAccounts")
	private List<String> existingAccounts;

	@JsonProperty("productFamily")
	private String productFamily;

	@JsonProperty("products")
	private List<Product> products;
	
	@JsonProperty(Constants.OTT_PRODUCT_FAMILY)
    private CTCartProduct ott;
	
	@JsonProperty("businessSegment")
	private String businessSegment;
	
	@JsonProperty("employeeSegment")
	private String employeeSegment;
	
	@JsonProperty("nftvFlag")
	private String nftvFlag;
	
	@JsonProperty("ftvCount")
	private String ftvCount;
	
	@JsonProperty("policy")
    private String policy;
    
    @JsonProperty("serviceOrderDate")
    private String serviceOrderDate;
    
    private String remoteReplacementType;
    
    private String partnerName;
    
    private String dtvSwimlane;
    
    public String getDtvSwimlane() {
		return dtvSwimlane;
	}

	public void setDtvSwimlane(String dtvSwimlane) {
		this.dtvSwimlane = dtvSwimlane;
	}
    
	public String getPartnerName() {
		return partnerName;
	}

	public void setPartnerName(String partnerName) {
		this.partnerName = partnerName;
	}
	
	public String getRemoteReplacementType() {
		return remoteReplacementType;
	}

	public void setRemoteReplacementType(String remoteReplacementType) {
		this.remoteReplacementType = remoteReplacementType;
	}
	
	public String getPolicy() {
		return policy;
	}

	public void setPolicy(String policy) {
		this.policy = policy;
	}

	public String getServiceOrderDate() {
		return serviceOrderDate;
	}

	public void setServiceOrderDate(String serviceOrderDate) {
		this.serviceOrderDate = serviceOrderDate;
	}

	public String getBusinessSegment() {
		return businessSegment;
	}

	public void setBusinessSegment(String businessSegment) {
		this.businessSegment = businessSegment;
	}

	public String getEmployeeSegment() {
		return employeeSegment;
	}

	public void setEmployeeSegment(String employeeSegment) {
		this.employeeSegment = employeeSegment;
	}

	public String getNftvFlag() {
		return nftvFlag;
	}

	public void setNftvFlag(String nftvFlag) {
		this.nftvFlag = nftvFlag;
	}

	public String getFtvCount() {
		return ftvCount;
	}

	public void setFtvCount(String ftvCount) {
		this.ftvCount = ftvCount;
	}

	/**
	 * @return the productFamily
	 */
	public String getProductFamily() {
		return productFamily;
	}

	/**
	 * @param productFamily
	 *            the productFamily to set
	 */
	public void setProductFamily(String productFamily) {
		this.productFamily = productFamily;
	}

	/**
	 * @return the products
	 */
	public List<Product> getProducts() {
		return products;
	}

	/**
	 * @param products
	 *            the products to set
	 */
	public void setProducts(List<Product> products) {
		this.products = products;
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

	/**
	 * @return the ott
	 */
	public CTCartProduct getOtt() {
		return ott;
	}

	/**
	 * @param ott the ott to set
	 */
	public void setOtt(CTCartProduct ott) {
		this.ott = ott;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "customerContext: {" + (existingAccounts != null ? "existingAccounts=" + existingAccounts + ", " : "")
				+ (productFamily != null ? "productFamily=" + productFamily + ", " : "")
				+ (products != null ? "products=" + products + ", " : "") + (ott != null ? "ott=" + ott : "") + "}";
	}

}
