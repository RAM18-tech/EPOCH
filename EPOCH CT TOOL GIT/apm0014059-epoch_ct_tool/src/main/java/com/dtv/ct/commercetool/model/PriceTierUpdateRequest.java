package com.dtv.ct.commercetool.model;

public class PriceTierUpdateRequest {

    private String environment;
    private String productCode;
    private Boolean publish;
    private String channel;
    private Long price;
    private FieldContainer fieldContainer;

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public Boolean getPublish() {
        return publish;
    }

    public void setPublish(Boolean publish) {
        this.publish = publish;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public FieldContainer getFieldContainer() {
        return fieldContainer;
    }

    public void setFieldContainer(FieldContainer fieldContainer) {
        this.fieldContainer = fieldContainer;
    }
}
