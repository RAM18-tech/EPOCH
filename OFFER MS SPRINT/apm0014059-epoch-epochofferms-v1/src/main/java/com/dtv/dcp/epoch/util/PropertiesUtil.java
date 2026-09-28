package com.dtv.dcp.epoch.util;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "idp")
public class PropertiesUtil {
  private Map<String, String> features = new HashMap<>();
  
  public Map<String, String> getFeatures() {
    return this.features;
  }
  
  public void setFeatures(Map<String, String> feats) {
    this.features = feats;
  }
}
