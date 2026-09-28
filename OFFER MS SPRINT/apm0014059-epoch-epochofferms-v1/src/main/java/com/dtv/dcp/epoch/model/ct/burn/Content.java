package com.dtv.dcp.epoch.model.ct.burn;

import java.io.Serializable;
import java.util.List;

/**
 * The class Content
 * @author vd7621
 */

public class Content implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private List<BurnPromotionResponse> promotions;
	
	public Content(List<BurnPromotionResponse> promotions) {
        this.promotions = promotions;
    }

    public Content() {
        // Intentionally Left Blank For Jackson
    }

    @Override
    public String toString() {
        return "Content{" +
                "promotions=" + promotions +
                '}';
    }

	public List<BurnPromotionResponse> getPromotions() {
			return promotions;
	}

    public void setPromotions(List<BurnPromotionResponse> promotions) {
        this.promotions = promotions;
    }

}
