package com.dtv.dcp.epoch.processor.helper;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.model.common.request.DeviceInfo;
import com.dtv.dcp.epoch.model.common.request.Devices;
import com.dtv.dcp.epoch.model.common.request.ProductRequest;
import com.dtv.dcp.epoch.util.OffersUtils;


@Component
public class CPOPDevicesHelper {
    private static final Logger log = LoggerFactory.getLogger(CPOPDevicesHelper.class);

    private static final String UNKNOWN = "UNKNOWN";

    @Autowired
    OffersUtils offersUtils;


    public List<DeviceInfo> getDevices(ProductRequest productRequest) {
        Map<String, DeviceInfo> devicesMap = null;
        List<DeviceInfo> list = null;
        try {
            //devicesMap = offersUtils.fetchDevices();
            list = returnDeviceInfoResponse(productRequest, devicesMap);
        } catch (Exception e) {
            log.error(String.format("Error getDevices not able to fetchDevices: %s ",e));
        }


        return list;
    }
    public List<DeviceInfo> returnDeviceInfoResponse(ProductRequest request,
                                                     Map<String, DeviceInfo> devicesMap) {
        List<DeviceInfo> listDeviceInfo = new ArrayList<>();
        if (Objects.nonNull(request)
                && Objects.nonNull(request.getDevices())
                && !request.getDevices().isEmpty()
                && Objects.nonNull(devicesMap) && !devicesMap.isEmpty()) {
            request.getDevices().stream().filter(Objects::nonNull).forEach(temp -> {
                if (temp.getMake() != null && temp.getModel() != null && temp.getDeviceType() != null) {
                    boolean isMatched = false;
                    for (Entry<String, DeviceInfo> entry : devicesMap.entrySet()) {
                        if (entry.getValue() != null && entry.getValue().getMake() != null
                                && entry.getValue().getModel() != null && entry.getValue().getDeviceType() != null) {
                            if (temp.getMake() != null && temp.getModel() != null && temp.getDeviceType() != null
                                    && temp.getMake().equals(entry.getValue().getMake())
                                    && temp.getModel().equals(entry.getValue().getModel())
                                    && temp.getDeviceType().equals(entry.getValue().getDeviceType())) {
                                isMatched = true;
                                DeviceInfo deviceInfo = new DeviceInfo();
                                deviceInfobuilder(entry.getValue(), deviceInfo, listDeviceInfo);

                            }
                        }
                    }
                    if (!isMatched) {
                        unKnownDeviceBuilder(devicesMap, listDeviceInfo);
                    }
                } else {
                    unKnownDeviceBuilder(devicesMap, listDeviceInfo);
                }
            });
        }
        return listDeviceInfo;

    }
    public void deviceInfobuilder(DeviceInfo deviceInfo2, DeviceInfo deviceInfo, List<DeviceInfo> listDeviceInfo) {
    	deviceInfo.setModel(deviceInfo2.getModel());
        deviceInfo.setMake(deviceInfo2.getMake());
        deviceInfo.setDeviceCategory(deviceInfo2.getDeviceCategory());
        deviceInfo.setDeviceName(deviceInfo2.getDeviceName());
        deviceInfo.setDeviceType(deviceInfo2.getDeviceType());
        deviceInfo.setThirdPartyOtt(deviceInfo2.getThirdPartyOtt());
        deviceInfo.setCatalogProductName(deviceInfo2.getCatalogProductName());
        listDeviceInfo.add(deviceInfo);
    }
    private void unKnownDeviceBuilder(Map<String, DeviceInfo> devicesMap, List<DeviceInfo> listDeviceInfo) {
        for (Entry<String, DeviceInfo> entry : devicesMap.entrySet()) {
            if (entry.getValue() != null && entry.getValue().getMake() != null
                    && entry.getValue().getModel() != null && entry.getValue().getDeviceType() != null
                    && entry.getValue().getMake().equalsIgnoreCase(UNKNOWN)
                    && entry.getValue().getModel().equalsIgnoreCase(UNKNOWN)
                    && entry.getValue().getDeviceType().equalsIgnoreCase(UNKNOWN)) {
                DeviceInfo deviceInfo = new DeviceInfo();
                deviceInfobuilder(entry.getValue(), deviceInfo, listDeviceInfo);
            }

        }
    }

}

