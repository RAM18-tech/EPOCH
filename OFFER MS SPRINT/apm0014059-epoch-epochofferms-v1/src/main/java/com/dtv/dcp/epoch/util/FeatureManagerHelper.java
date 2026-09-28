package com.dtv.dcp.epoch.util;

import com.dtv.dcp.epoch.common.Constants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.integration.common.FeatureManagerRestClient;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The Class FeatureManagerHelper.
 */
@Component
public class FeatureManagerHelper {

    private static final Logger log = LoggerFactory.getLogger(FeatureManagerHelper.class);

    @Value("${dcp.config.env}")
    private String idpConfigEnv;

    public static final String PREFIX_IDPCTX = "idpctx-feature-";

    public static final String PREFIX_FEATURE = "feature-";

    public static final String LOCAL = "-local";

    public static HttpHeaders httpHeaders = null;

    @Autowired
    FeatureManagerRestClient featureManagerRestClient;

    @Autowired
    PropertiesUtil propUtil;



    public boolean isEnabledWithInvokingIXP(String flag) {
        log.debug("inside isEnabledWithInvokingIXP flag: {}", flag);
        long startTimeInMillies = System.currentTimeMillis();
        String headerValue = getHttpHeaderValue(flag);
        String featureHeaderValue = getHttpHeaderValue(PREFIX_FEATURE + flag);
        String idpctxHeaderValue = getHttpHeaderValue(PREFIX_IDPCTX + flag);
        if(StringUtils.isNotEmpty(headerValue)) {
            log.debug("Inside getHttpHeaderValue without Prefix flag: {}", flag);
            return Boolean.valueOf(headerValue);
        }else if(StringUtils.isNotEmpty(featureHeaderValue)) {
            log.debug("Inside getHttpHeaderValue with feature Prefix flag: {}", flag);
            return Boolean.valueOf(featureHeaderValue);
        }else if (StringUtils.isNotEmpty(idpctxHeaderValue) || StringUtils.isNotEmpty(flag)) {
            log.debug("Execution time for checking value from header with prefix idpctx-feature- {}", System.currentTimeMillis() - startTimeInMillies);
            if(StringUtils.isNotEmpty(idpctxHeaderValue)) {
                return Boolean.valueOf(idpctxHeaderValue);
            }else if(StringUtils.isNotEmpty(headerValue)) {
                return Boolean.valueOf(headerValue);
            }
        }

        // If svc-epoch-ixp-fs-issue-flag-enabled is enabled, only header values are considered and the IXP endpoint is not invoked.
        String ixpFsIssueToggleHeader = getHttpHeaderValue(PREFIX_FEATURE + Constants.FEATURE_TOGGLE_IXP_FS_ISSUE);
        if (StringUtils.isBlank(ixpFsIssueToggleHeader)){
            ixpFsIssueToggleHeader = getHttpHeaderValue(PREFIX_IDPCTX + Constants.FEATURE_TOGGLE_IXP_FS_ISSUE);
            log.debug("isEnabledWithInvokingIXP Checking header with prefix idpctx-feature- for IXP FS issue toggle: {}", ixpFsIssueToggleHeader);
        }
        if (StringUtils.isNotEmpty(ixpFsIssueToggleHeader) && Boolean.parseBoolean(ixpFsIssueToggleHeader)) {
            log.debug("isEnabledWithInvokingIXP Defaulting to false: {}", flag);
            return false;
        }


        log.debug("isEnabledWithInvokingIXP Calling IXP Endpoint: {}", flag);
        String ixpresponse = featureManagerRestClient.invokeIxpEndpoint(idpConfigEnv);
        return isIxpEnabled(ixpresponse, "allocations", flag);
    }

    /**
     * Checks if is enabled.
     *
     * @param feature
     *            the feature
     * @return true, if is enabled
     */
    public boolean isEnabled(String feature) {
		/*Feature iptvMigrationFeature = new Feature(feature);
		return FeatureManager.isEnabled(iptvMigrationFeature);*/
        return isEnabledWithInvokingIXP(feature);
    }

    public boolean isIxpEnabled(String result, String key, String flag) {

        JsonParser parser = new JsonParser();

        JsonElement element = parser.parse(result);

        JsonObject obj = element.getAsJsonObject();
        if (obj.get(key) != null) {

            if (obj.get(key).getAsJsonObject().get(flag) != null) {
                String value = obj.get(key).getAsJsonObject().get(flag).getAsJsonObject().get("value").toString();
                if (value.equalsIgnoreCase("\"true\"")) {
                    return true;
                }
            }else if(null != propUtil.getFeatures().get(flag)) {
                return Boolean.valueOf(propUtil.getFeatures().get(flag));
            }
        }
        return false;
    }

    private String getHttpHeaderValue(String headerKey) {
        if (httpHeaders == null) {
            return "";
        }
        // Check direct header first
        String directValue = httpHeaders.getFirst(headerKey);
        if (directValue != null) {
            log.debug("getHttpHeaderValue: found direct header key={} value={}", headerKey, directValue);
            return directValue;
        }
        // Fall back to parsing the 'baggage' header (comma-separated key=value pairs)
        String baggage = httpHeaders.getFirst("baggage");
        if (StringUtils.isNotEmpty(baggage)) {
            for (String entry : baggage.split(",")) {
                int idx = entry.indexOf('=');
                if (idx > 0) {
                    String k = entry.substring(0, idx).trim();
                    String v = entry.substring(idx + 1).trim();
                    if (k.equalsIgnoreCase(headerKey)) {
                        log.debug("getHttpHeaderValue: found in baggage key={} value={}", headerKey, v);
                        return v;
                    }
                }
            }
        }
        return "";
    }
}
