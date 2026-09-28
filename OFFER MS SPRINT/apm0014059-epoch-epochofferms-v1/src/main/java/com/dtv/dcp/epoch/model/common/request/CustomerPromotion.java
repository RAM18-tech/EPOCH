package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerPromotion implements Serializable{

   /**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;
	
    @JsonProperty("promotionId")
    private String promotionId ;

    @JsonProperty("promotionStartDate")
    private String promotionStartDate ;

    @JsonProperty("promotionEndDate")
    private String promotionEndDate;

    public String getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(String promotionId) {
        this.promotionId = promotionId;
    }

    public String getPromotionStartDate() {
        return promotionStartDate;
    }

    public void setPromotionStartDate(String promotionStartDate) {
        this.promotionStartDate = promotionStartDate;
    }

    public String getPromotionEndDate() {
        return promotionEndDate;
    }

    public void setPromotionEndDate(String promotionEndDate) {
        this.promotionEndDate = promotionEndDate;
    }

    @Override
    public String toString() {
        return "CustomerPromotion{" +
                "promotionId='" + promotionId + '\'' +
                ", promotionStartDate='" + promotionStartDate + '\'' +
                ", promotionEndDate='" + promotionEndDate + '\'' +
                '}';
    }
}
