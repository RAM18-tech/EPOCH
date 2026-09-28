package com.dtv.dcp.epoch.model.common.response;

import java.util.List;

import com.dtv.dcp.epoch.model.common.Promotion;
import com.dtv.dcp.epoch.model.common.response.ucc.Offer;

public class CartItems {

    private String productCode;

    private  String action;

    private List<Promotion> promotions;

    private List<Offer> offers;

    private String validationMessage;
    private Boolean delayProvisioning;

	public Boolean getDelayProvisioning() {
		return delayProvisioning;
	}

	public void setDelayProvisioning(Boolean delayProvisioning) {
		this.delayProvisioning = delayProvisioning;
	}


    public List<Offer> getOffers() {
        return offers;
    }

    public void setOffers(List<Offer> offers) {
        this.offers = offers;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<Promotion> getPromotions() {
        return promotions;
    }

    public void setPromotions(List<Promotion> promotions) {
        this.promotions = promotions;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }
}
