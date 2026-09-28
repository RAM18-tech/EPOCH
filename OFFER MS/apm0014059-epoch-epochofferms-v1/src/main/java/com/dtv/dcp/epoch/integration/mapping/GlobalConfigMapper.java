package com.dtv.dcp.epoch.integration.mapping;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.model.ct.offer.GlobalEligibilityAttribute;
import com.dtv.dcp.epoch.model.ct.offer.GlobalEligibilityRule;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GlobalConfigMapper {
    public Map<String, List<String>> extractDataFromJsonNode(JsonNode rootNode) {
        Map<String, List<String>> epochMap = new HashMap<String, List<String>>();
        String key = null;
        JsonNode productsNode = rootNode.path("products");
        if (productsNode.isArray()) {
            for (JsonNode product : productsNode) {
                JsonNode variantsNode = product.path("variants");
                if (variantsNode.isArray()) {
                    for (JsonNode variant : variantsNode) {
                        JsonNode attributeNode = variant.path("attributes");
                        JsonNode epochNode = attributeNode.path("epochGlobalConfigurations");
                        if (epochNode.isArray()) {
                            extractKeyValueFromNode(epochNode, epochMap);
                        }
                        JsonNode deviceRulesNode = attributeNode.path("deviceRules");
                        if (deviceRulesNode.isArray()) {
                            extractKeyValueFromNode(deviceRulesNode, epochMap);
                        }
                    }
                }
            }
        }
        return epochMap;
    }

    private void extractKeyValueFromNode(JsonNode epochNode, Map<String, List<String>> epochMap) {
        if (epochNode.isArray()) {
            for (JsonNode node : epochNode) {
                if (node.isArray()) {
                    String key = null;
                    List<String> list = null;
                    for (JsonNode nodeArray : node) {
                        if (Constants.GLOBAL_CONFIGURATIONS_KEY
                                .equalsIgnoreCase(nodeArray.path("name").asText())) {
                            key = nodeArray.path("value").asText();
                        }
                        if (Constants.GLOBAL_CONFIGURATIONS_VALUE
                                .equalsIgnoreCase(nodeArray.path("name").asText())) {
                            JsonNode valueNode = nodeArray.path("value");
                            list = new ArrayList<>();
                            if (valueNode.isArray()) {
                                for (JsonNode value : valueNode) {
                                    list.add(value.asText());
                                }
                            }
                        }
                        if (key != null && list != null) {
                            epochMap.put(key, list);
                            key = null;
                        }
                    }
                }
            }
        }
    }

    public List<GlobalEligibilityRule> extractGlobalEligibilityRulesDataFromJsonNode(JsonNode rootNode) {
        List<GlobalEligibilityRule> epochEligibilityRules = new ArrayList<GlobalEligibilityRule>();

        JsonNode productsNode = rootNode.path("products");
        if (productsNode.isArray()) {
            for (JsonNode product : productsNode) {
                JsonNode variantsNode = product.path("variants");
                if (variantsNode.isArray()) {
                    for (JsonNode variant : variantsNode) {
                        JsonNode attributeNode = variant.path("attributes");
                        JsonNode eligibilityRulesNode = attributeNode.path("epochEligibilityRulesCheck");
                        for (JsonNode node : eligibilityRulesNode) {
                            if (node.isArray()) {
                                GlobalEligibilityRule rule = new GlobalEligibilityRule();
                                for (JsonNode nodeArray : node) {
                                    if (Constants.GLOBAL_ELIGIBILITYRULE_PREDICATEEVAL
                                            .equalsIgnoreCase(nodeArray.path("name").asText())) {
                                        rule.setPredicate(nodeArray.path("value").asText());
                                    }

                                    if (Constants.GLOBAL_ELIGIBILITYRULE_PREDICATES
                                            .equalsIgnoreCase(nodeArray.path("name").asText())) {
                                        JsonNode valueNode = nodeArray.path("value");
                                        List<GlobalEligibilityAttribute> attributes = new ArrayList<GlobalEligibilityAttribute>();
                                        if (valueNode.isArray()) {
                                            for (JsonNode value : valueNode) {
                                                if (value.isArray()) {
                                                    GlobalEligibilityAttribute attr = new GlobalEligibilityAttribute();
                                                    for (JsonNode attribute : value) {
                                                        if (Constants.GLOBAL_ELIGIBILITYRULE_ATTRIBUTE_NAME
                                                                .equalsIgnoreCase(attribute.path("name").asText())) {
                                                            attr.setAttributeName(attribute.path("value").asText());
                                                        }

                                                        if (Constants.GLOBAL_ELIGIBILITYRULE_ATTRIBUTE_REQUESTPATH
                                                                .equalsIgnoreCase(attribute.path("name").asText())) {
                                                            attr.setRequestAttributePath(attribute.path("value").asText());
                                                        }

                                                        if (Constants.GLOBAL_ELIGIBILITYRULE_ATTRIBUTE_RESPONSEPATH
                                                                .equalsIgnoreCase(attribute.path("name").asText())) {
                                                            attr.setResponseAttributePath(attribute.path("value").asText());
                                                        }

                                                        if (Constants.GLOBAL_ELIGIBILITYRULE_ATTRIBUTE_STATICVALUE
                                                                .equalsIgnoreCase(attribute.path("name").asText())) {
                                                            attr.setStaticValue(attribute.path("value").asText());
                                                        }
                                                    }
                                                    attributes.add(attr);
                                                }
                                            }
                                        }
                                        rule.setEligibilityAtributes(attributes);
                                    }
                                }
                                epochEligibilityRules.add(rule);
                            }
                        }
                    }
                }
            }

        }

        return epochEligibilityRules;
    }

    public Map<String, List<String>> extractValidateCartRulesDataFromJsonNode(JsonNode rootNode) {
        Map<String, List<String>> epochMap = new HashMap<String, List<String>>();
        String key = null;
        JsonNode productsNode = rootNode.path("products");
        if (productsNode.isArray()) {
            for (JsonNode product : productsNode) {
                JsonNode variantsNode = product.path("variants");
                if (variantsNode.isArray()) {
                    for (JsonNode variant : variantsNode) {
                        JsonNode attributeNode = variant.path("attributes");
                        JsonNode epcohNode = attributeNode.path("validateCartRules");
                        if (epcohNode.isArray()) {
                            for (JsonNode node : epcohNode) {
                                if (node.isArray()) {
                                    List<String> list = null;
                                    for (JsonNode nodeArray : node) {
                                        if (Constants.GLOBAL_CONFIGURATIONS_KEY
                                                .equalsIgnoreCase(nodeArray.path("name").asText())) {
                                            key = nodeArray.path("value").asText();
                                        }
                                        if (Constants.GLOBAL_CONFIGURATIONS_VALUE
                                                .equalsIgnoreCase(nodeArray.path("name").asText())) {
                                            JsonNode valueNode = nodeArray.path("value");
                                            list = new ArrayList<>();
                                            if (valueNode.isArray()) {
                                                for (JsonNode value : valueNode) {
                                                    list.add(value.asText());
                                                }
                                            }

                                        }
                                        if (key != null && list != null) {
                                            epochMap.put(key, list);
                                            key = null;
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
        return epochMap;
    }

}

