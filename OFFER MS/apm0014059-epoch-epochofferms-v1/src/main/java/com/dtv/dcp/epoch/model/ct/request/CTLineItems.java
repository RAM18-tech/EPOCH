package com.dtv.dcp.epoch.model.ct.request;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CTLineItems implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	private String productFamily;
	
	private List<Product> products;

	
	
	@Override
	public String toString() {
		return "CTLineItems{" +
				"ProductFamily=" + productFamily +
				", products=" + products +

				'}';
	}



	public String getProductFamily() {
		return productFamily;
	}



	public void setProductFamily(String productFamily) {
		this.productFamily = productFamily;
	}



	public List<Product> getProducts() {
		return products;
	}



	public void setProducts(List<Product> products) {
		this.products = products;
	}

}
