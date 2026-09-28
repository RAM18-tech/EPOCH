//package com.dtv.dcp.epoch.model.ct.request;
//
//import java.io.Serializable;
//import java.util.List;
//import java.util.Optional;
//
//public class OfferInfo implements Serializable {
//    private String productCode;
//    List<String> benefitCodes;
//
//    public String getProductCode() {
//        return productCode;
//    }
//
//    public void setProductCode(String productCode) {
//        this.productCode = productCode;
//    }
//
//    public List<String> getBenefitCodes() {
//        return benefitCodes;
//    }
//
//    public void setBenefitCodes(List<String> benefitCodes) {
//        this.benefitCodes = benefitCodes;
//    }
//
//    @Override
//    public String toString() {
//        StringBuilder builder = new StringBuilder();
//        builder.append("OfferInfo [productCode=");
//        builder.append(this.productCode);
//        builder.append(", benefitCodes=");
//        this.benefitCodes.stream().map(item -> builder.append(item));
//        builder.append(benefitCodes);
//        builder.append("]");
//        return super.toString();
//    }
//}
