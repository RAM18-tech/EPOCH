package com.dtv.dcp.epoch.model.ct.response;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.dtv.dcp.epoch.model.ct.request.Product;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CTOfferQualification implements Serializable {

	private static final long serialVersionUID = 1L;

	public CTOfferQualification() {
		qualifiedOfferCodes = new HashSet<>();
	}

	@JsonProperty("qualifiedOfferCodes")
	private Set<String> qualifiedOfferCodes;

	@JsonProperty("benificiaryProductSkus")
	private Map<String, Set<BeneficiarySku>> benificiaryProductSkus;

	@JsonProperty("products")
	private List<Product> products;

	public Set<String> getQualifiedOfferCodes() {
		if(Objects.isNull(qualifiedOfferCodes)) {
			return new HashSet<>();
		}
		return qualifiedOfferCodes;
	}

	public void setQualifiedOfferCodes(Set<String> qualifiedOfferCodes) {
		this.qualifiedOfferCodes = qualifiedOfferCodes;
	}

	
	public List<Product> getProducts() {
		return products;
	}

	public void setProducts(List<Product> products) {
		this.products = products;
	}

	public Map<String, Set<BeneficiarySku>> getBenificiaryProductSkus() {
		return benificiaryProductSkus;
	}

	public void setBenificiaryProductSkus(Map<String, Set<BeneficiarySku>> benificiaryProductSkus) {
		this.benificiaryProductSkus = benificiaryProductSkus;
	}
}
