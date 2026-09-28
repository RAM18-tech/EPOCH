package com.dtv.dcp.epoch.model.ct.burn;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OfferBurnRequestWrapper implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<OfferBurnRequest> promotions;
	
	private String orderNumber;

    public OfferBurnRequestWrapper(List<OfferBurnRequest> requests) {
        this.promotions = requests;
    }

    public OfferBurnRequestWrapper() {
        // Intentionally Left Blank For Jackson
    }

    /**
	 * @return the orderNumber
	 */
	public String getOrderNumber() {
		return orderNumber;
	}

	/**
	 * @param orderNumber the orderNumber to set
	 */
	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	@Override
	public String toString() {
		return "OfferBurnRequestWrapper{" + "requests=" + "orderNumber" + ":" + orderNumber + "," + promotions + '}';
	}

    public List<OfferBurnRequest> getPromotions() {
        return promotions;
    }

    public void setPromotions(List<OfferBurnRequest> promotions) {
        this.promotions = promotions;
    }
}
