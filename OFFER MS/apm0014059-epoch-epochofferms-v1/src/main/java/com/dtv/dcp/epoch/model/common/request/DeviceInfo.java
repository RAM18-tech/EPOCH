package com.dtv.dcp.epoch.model.common.request;


import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceInfo  implements Serializable {

    private static final long serialVersionUID = 1L;
    private String make;
    private String model;
    private String deviceType;
    private String deviceName;
    private String thirdPartyOtt;
    private String deviceCategory;
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

    public String getThirdPartyOtt() {
        return thirdPartyOtt;
    }

    public void setThirdPartyOtt(String thirdPartyOtt) {
        this.thirdPartyOtt = thirdPartyOtt;
    }

    public String getDeviceCategory() {
        return deviceCategory;
    }

    public void setDeviceCategory(String deviceCategory) {
        this.deviceCategory = deviceCategory;
    }

    public String getCatalogProductName() {
        return catalogProductName;
    }

    public void setCatalogProductName(String catalogProductName) {
        this.catalogProductName = catalogProductName;
    }
}
