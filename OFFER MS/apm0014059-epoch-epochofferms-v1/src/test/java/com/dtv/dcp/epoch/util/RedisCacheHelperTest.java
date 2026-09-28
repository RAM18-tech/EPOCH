package com.dtv.dcp.epoch.util;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.model.ct.offer.GlobalEligibilityAttribute;
import com.dtv.dcp.epoch.model.ct.offer.GlobalEligibilityRule;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class RedisCacheHelperTest {

    @InjectMocks
    RedisCacheHelper redisCacheHelper;

    @Mock
    CpopClient cpopClient;

    @Test
    void getValuesTest() {
        Map<String, List<String>> epochGloablConfigMap = new LinkedHashMap<>();
        epochGloablConfigMap.put(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_ELIGIBLITY_CHECK_MONTHS, Arrays.asList("2"));
        Mockito.when(cpopClient.loadGlobalConfigurations(any())).thenReturn(epochGloablConfigMap);
        List<String> response = redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_ELIGIBLITY_CHECK_MONTHS, Constants.OTT);
        Assertions.assertNotNull(response, "Successfully covered the getValues test");
        Assertions.assertEquals(response.size(), 1);
        Assertions.assertEquals(response.get(0), "2");
    }

    @Test
    void getValuesReturnWhenExceptionTest() {
        Map<String, List<String>> epochGloablConfigMap = new LinkedHashMap<>();
        epochGloablConfigMap.put(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_ELIGIBLITY_CHECK_MONTHS, Arrays.asList("2"));
        Mockito.when(cpopClient.loadGlobalConfigurations(any())).thenThrow(NullPointerException.class);
        List<String> response = redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_ELIGIBLITY_CHECK_MONTHS, Constants.OTT);
        Assertions.assertNull(response);
    }

    @Test
    void getGlobalEligibilityRulesTest() {
        List<GlobalEligibilityRule> epochGloablEligibilityRules = new ArrayList<GlobalEligibilityRule>();
        epochGloablEligibilityRules.add(getGlobalEligibilityRule());
        Mockito.when(cpopClient.loadGlobalEligibilityRules(any())).thenReturn(epochGloablEligibilityRules);
        List<GlobalEligibilityRule> response = redisCacheHelper.getGlobalEligibilityRules(Constants.OTT);
        Assertions.assertNotNull(response, "Successfully covered the getGlobalEligibilityRules test");
        Assertions.assertEquals(response.size(), 1);
        Assertions.assertEquals(response.get(0).getPredicate(), "!salesChannel");
    }
    
    @Test
    void getGlobalEligibilityRulesReturnWhenExceptionTest() {
        List<GlobalEligibilityRule> epochGloablEligibilityRules = new ArrayList<GlobalEligibilityRule>();     
        epochGloablEligibilityRules.add(getGlobalEligibilityRule());
        Mockito.when(cpopClient.loadGlobalEligibilityRules(any())).thenThrow(NullPointerException.class);
        List<GlobalEligibilityRule> response = redisCacheHelper.getGlobalEligibilityRules(Constants.OTT);
        Assertions.assertNull(response);
    }
    
    private GlobalEligibilityRule getGlobalEligibilityRule() {
    	GlobalEligibilityRule rule=new GlobalEligibilityRule();
        rule.setPredicate("!salesChannel");
        GlobalEligibilityAttribute atr = new GlobalEligibilityAttribute();
        atr.setAttributeName("salesChannel");
        atr.setRequestAttributePath("salesChannel");
        atr.setStaticValue("request.contains(\"opus\")");
        rule.setEligibilityAtributes(List.of(atr));
        return rule;
    }
}
