package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;

public class Devices implements Serializable {

    /**
     * The make.
     */
    private String make;

    /**
     * The model.
     */
    private String model;

    /**
     * The device type.
     */
    private String deviceType;

    /**
     * The device name.
     */
    private String deviceName;


    /**
     * The device category.
     */
    private String deviceCategory;

    /**
     * The device classification type.
     */
    private String deviceClassificationType;

    /**
     * The step.
     */
    private String step;

    /**
     * The third party ott.
     */
    private String thirdPartyOtt;

    /**
     * The String osName.
     */
    private String osName;

    /**
     * The catalog product name.
     */
    private String catalogProductName;

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceCategory() {
        return deviceCategory;
    }

    public void setDeviceCategory(String deviceCategory) {
        this.deviceCategory = deviceCategory;
    }

    public String getDeviceClassificationType() {
        return deviceClassificationType;
    }

    public void setDeviceClassificationType(String deviceClassificationType) {
        this.deviceClassificationType = deviceClassificationType;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getThirdPartyOtt() {
        return thirdPartyOtt;
    }

    public void setThirdPartyOtt(String thirdPartyOtt) {
        this.thirdPartyOtt = thirdPartyOtt;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getCatalogProductName() {
        return catalogProductName;
    }

    public void setCatalogProductName(String catalogProductName) {
        this.catalogProductName = catalogProductName;
    }
}