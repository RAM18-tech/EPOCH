package com.dtv.ct.commercetool.model;

public class PriceRequest {

    private String productCode;
    private String validFrom;

    private String validUntil;

    private String channel;

    private String customerGroup;

    private String publish;

    private Long price;

    private Long oldPrice;

    private FieldContainer fieldContainer;

    public Long getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(Long oldPrice) {
        this.oldPrice = oldPrice;
    }

    public FieldContainer getFieldContainer() {
        return fieldContainer;
    }

    public void setFieldContainer(FieldContainer fieldContainer) {
        this.fieldContainer = fieldContainer;
    }

    public void setPrice(Long price) {
        this.price = price;
    }


    public Long getPrice() {
        return price;
    }

    public String getPublish() {
        return publish;
    }

    public void setPublish(String publish) {
        this.publish = publish;
    }

    public PriceRequest() {
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(String validFrom) {
        this.validFrom = validFrom;
    }

    public String getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(String validUntil) {
        this.validUntil = validUntil;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getCustomerGroup() {
        return customerGroup;
    }

    public void setCustomerGroup(String customerGroup) {
        this.customerGroup = customerGroup;
    }
}

