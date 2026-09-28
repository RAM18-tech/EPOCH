package com.dtv.dcp.epoch.util;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.model.ct.offer.GlobalEligibilityRule;

@Component
public class RedisCacheHelper {

	@Autowired
	CpopClient cpopClient;

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

}
