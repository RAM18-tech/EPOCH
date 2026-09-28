package com.dtv.dcp.epoch.model.common;

import java.util.List;
import java.util.Map;

import com.dtv.dcp.epoch.model.common.response.ucc.ValidateCoupon;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;

public class ValidateCartAsyncResponses {

    private List<ValidateCoupon> coupons;
    private CTOfferResponse ctCartCallOfferResponse;
    private CTProductResponse ctProductResponse;
    private CTBenefitsResponse ctBenefitsResponse;
    private Map<String, String> productCodeToProductIdMap;
    private Map<String, String> productCodeToProductTypeMap;

    public Map<String, String> getProductCodeToProductTypeMap() {
        return productCodeToProductTypeMap;
    }

    public void setProductCodeToProductTypeMap(Map<String, String> productCodeToProductTypeMap) {
        this.productCodeToProductTypeMap = productCodeToProductTypeMap;
    }

    public CTBenefitsResponse getCtBenefitsResponse() {
        return ctBenefitsResponse;
    }

    public void setCtBenefitsResponse(CTBenefitsResponse ctBenefitsResponse) {
        this.ctBenefitsResponse = ctBenefitsResponse;
    }


    public Map<String, String> getProductCodeToProductIdMap() {
        return productCodeToProductIdMap;
    }

    public void setProductCodeToProductIdMap(Map<String, String> productCodeToProductIdMap) {
        this.productCodeToProductIdMap = productCodeToProductIdMap;
    }

    public CTProductResponse getCtProductResponse() {
        return ctProductResponse;
    }

    public void setCtProductResponse(CTProductResponse ctProductResponse) {
        this.ctProductResponse = ctProductResponse;
    }

    public List<ValidateCoupon> getCoupons() {
        return coupons;
    }

    public void setCoupons(List<ValidateCoupon> coupons) {
        this.coupons = coupons;
    }

    public CTOfferResponse getCtCartCallOfferResponse() {
        return ctCartCallOfferResponse;
    }

    public void setCtCartCallOfferResponse(CTOfferResponse ctCartCallOfferResponse) {
        this.ctCartCallOfferResponse = ctCartCallOfferResponse;
    }

}
