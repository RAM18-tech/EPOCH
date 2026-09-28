package com.dtv.dcp.epoch.util;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dtv.dcp.epoch.integration.EpochClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.model.ct.offer.GlobalEligibilityRule;

@Component
public class RedisCacheHelper {

	@Autowired
	CpopClient cpopClient;

	@Autowired
	EpochClient epochClient;

	public List<String> getValues(String key, String productFamily) {

		try {
			Map<String, List<String>> epochGloablConfigMap = cpopClient.loadGlobalConfigurations(productFamily);
			if (epochGloablConfigMap != null && !epochGloablConfigMap.isEmpty()) {
				if (epochGloablConfigMap.containsKey(key)) {
					return epochGloablConfigMap.get(key);
				}
			}
		} catch (Exception e) {
           return null;
		}
		return null;

	}
	
	public List<GlobalEligibilityRule> getGlobalEligibilityRules(String productFamily){
		try {
			List<GlobalEligibilityRule> epochGloablEligibilityRules = cpopClient.loadGlobalEligibilityRules(productFamily);
			if (epochGloablEligibilityRules != null && !epochGloablEligibilityRules.isEmpty()) {
				return epochGloablEligibilityRules;
			}
		} catch (Exception e) {
           return null;
		}
		return null;
	}

	public Map<String, Object> getEligibilityGlobalConfig(){
		Map<String, Object> getEligibilityGlobalConfigRules = new HashMap<>();
		try {
			getEligibilityGlobalConfigRules = epochClient.loadEligibilityGlobalConfigRules();
		} catch (Exception e) {
			return getEligibilityGlobalConfigRules;
		}
		return getEligibilityGlobalConfigRules;
	}

	public List<String> getValidateRules(String key, String productFamily) {
		try {
			Map<String, List<String>> epochValidateCartRulesConfigMap = cpopClient.loadValidateCartRules(productFamily);
			if (epochValidateCartRulesConfigMap != null && !epochValidateCartRulesConfigMap.isEmpty()) {
				if (epochValidateCartRulesConfigMap.containsKey(key)) {
					return epochValidateCartRulesConfigMap.get(key);
				}
			}
		} catch (Exception e) {
			return null;
		}
		return null;

	}

    public List<String> getSwimlaneRules(String key, String productFamily) {
        try {
            Map<String, List<String>> epochSwimlaneRulesConfigMap = cpopClient.loadSwimlaneRules(productFamily);
            if (epochSwimlaneRulesConfigMap != null && !epochSwimlaneRulesConfigMap.isEmpty()) {
                if (epochSwimlaneRulesConfigMap.containsKey(key)) {
                    return epochSwimlaneRulesConfigMap.get(key);
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;

    }
}
