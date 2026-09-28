package com.dtv.dcp.epoch.util;


import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * The Class JsonFilterService.
 */
public final class JsonFilterService {

    private static final Logger log = LoggerFactory.getLogger(JsonFilterService.class);

    /**
     * The Constant MAPPER.
     */
    private static final ObjectMapper MAPPER;

    private JsonFilterService() {
        //added to remove sonar issues
    }

    static {

        log.debug("Start of JsonFilterService static block ");

        MAPPER = new ObjectMapper();
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        MAPPER.setSerializationInclusion(Include.NON_NULL);

        log.debug("End of JsonFilterService static block ");
    }

    public static <T> T filterAttributesAndGetObjectFromJson(final Object input, final Class<T> valueType, String[] ignorableFieldNames) {
        if (input == null) return null;
        try {
            Set<String> toRemove = Set.of(ignorableFieldNames);
            JsonNode rootNode = MAPPER.valueToTree(input);
            removeFieldsDeep(rootNode, toRemove); // works on any JsonNode type
            return MAPPER.treeToValue(rootNode, valueType);
        } catch (IOException io) {
            log.error("Got IOException while converting from Json to Object..{}", io);
            throw new ServiceException(ErrorMessages.JSON_SERIALIZATION_ERROR, io.getCause());
        }
    }


    private static void removeFieldsDeep(JsonNode node, Set<String> fieldsToRemove) {
        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            obj.remove(fieldsToRemove); // bulk remove at this level
            node.fields().forEachRemaining(entry -> removeFieldsDeep(entry.getValue(), fieldsToRemove));
        } else if (node.isArray()) {
            node.forEach(child -> removeFieldsDeep(child, fieldsToRemove));
        }
    }
}