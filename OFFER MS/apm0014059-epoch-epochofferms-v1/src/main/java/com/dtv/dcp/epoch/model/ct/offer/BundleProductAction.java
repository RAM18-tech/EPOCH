package com.dtv.dcp.epoch.model.ct.offer;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.ct.product.Product;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BundleProductAction implements Serializable
{
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    private String action;

    private List<Product> productIdOnAccount;

    public List<Product> getProductIdOnAccount() {
        return productIdOnAccount;
    }

    public void setProductIdOnAccount(List<Product> productIdOnAccount) {
        this.productIdOnAccount = productIdOnAccount;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}