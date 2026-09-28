package com.dtv.dcp.epoch.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * The Class JsonService.
 */
public final class JsonService {
	
	/** The Constant got LOG. */
	private static final Logger log = LoggerFactory.getLogger(JsonService.class);

	/** The Constant for error log string. */
	private static final String ERROR_LOG_STR = "Got IOException while converting from Json to Object..{}";
	
	/** The Constant MAPPER. */
	private static final ObjectMapper MAPPER;

	/**
	 * Instantiates a new json service.
	 */
	private JsonService() {
		// Added to remove sonar issues.
	}

	static {
		
		log.debug("Start of JsonService static block ");
		
		MAPPER = new ObjectMapper();
		MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		MAPPER.setSerializationInclusion(Include.NON_NULL);
		
		log.debug("End of JsonService static block ");
	}

	/**
	 * Gets the object from json.
	 *
	 * @param <T> the generic type
	 * @param jsonString the json string
	 * @param valueType the value type
	 * @return the object from json
	 */
	public static <T> T getObjectFromJson(final Object jsonString, final Class<T> valueType) {
		
		T object = null;
		if (jsonString != null) {
			try {
				object = MAPPER.readValue(jsonString.toString(), valueType);

			} catch (IOException io) {
				log.error(ERROR_LOG_STR +" in method getObjectFromJson(). Exception Message={}, Exception Stack ={}", io.getMessage(),io);
				throw new ServiceException(ErrorMessages.CPOP_OFFER_MOCK_JSON_SERIALIZATION_ERROR, io.getCause());
			}
		}
	
		return object;
	}
	
	public static <T> T getObjectFromJson(final Object jsonString, final TypeReference<T> type) {

		T object = null;
		if (jsonString != null) {
			try {
				object = MAPPER.readValue(jsonString.toString(), type);

			} catch (IOException io) {

				log.error(ErrorMessages.CPOP_OFFER_MOCK_JSON_SERIALIZATION_ERROR +" in method getObjectFromJson(final Object jsonString, final TypeReference<T> type). Exception Message={}, Exception Stack ={}", io.getCause(),io);
				throw new ServiceException(ErrorMessages.CPOP_OFFER_MOCK_JSON_SERIALIZATION_ERROR, io.getCause());
			}
		}

		
		return object;
	}

	/**
	 * Gets the json from object.
	 *
	 * @param object the object
	 * @return the json from object
	 * @throws Exception the exception
	 */
	public static String getJsonFromObject(final Object object) {
		
		log.debug("Start of JsonService.getJsonFromObject() method.. {}");
		
		String jsonString = null;
		try {
			jsonString = MAPPER.writeValueAsString(object);
		
		} catch (JsonProcessingException e) {
			log.error("Got IOException while converting from Object to Json in method getJsonFromObject(). Exception Message={}, Exception Stack ={}", e.getMessage(),e);

		}
		return jsonString;
	}
	
	public static <T> T getObjectFromJsonTree(final Object jsonTree,String element, final Class<T> valueType) {
		
		T object = null;
		if (jsonTree != null) {
			try {
				JsonNode jsonTreeNode = MAPPER.readTree(jsonTree.toString());
				JsonNode parent=jsonTreeNode.findValue(element);
				if(parent==null)
					return null;
				object = MAPPER.readValue(parent.toString(), valueType);
			} catch (IOException io) {
				log.error(ERROR_LOG_STR +" in method getObjectFromJsonTree(). Exception Message={}, Exception Stack ={}", io.getMessage(),io);

				throw new ServiceException(ErrorMessages.CPOP_OFFER_MOCK_JSON_SERIALIZATION_ERROR, io.getCause());
			}
		}

		return object;
	}
	
	/**
	 * Gets the list object from json.
	 *
	 * @param <T> the generic type
	 * @param jsonString the json string
	 * @param valueType the value type
	 * @return the list object from json
	 */
	public static <T> List<T> getListObjectFromJsonTree(final Object jsonTree, String element, final Class<T> valueType) {		
		
		List<T> object = null;
		if (jsonTree != null) {
			try {
				JsonNode jsonTreeNode = MAPPER.readTree(jsonTree.toString());
				JsonNode parent=jsonTreeNode.findValue(element);
				CollectionLikeType typeref = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, valueType);
				object = MAPPER.readValue(parent.toString(), typeref);

			} catch (IOException io) {

				log.error(ERROR_LOG_STR +" in method getListObjectFromJsonTree(). Exception Message={}, Exception Stack ={}", io.getMessage(),io);
				throw new ServiceException(ErrorMessages.CPOP_OFFER_MOCK_JSON_SERIALIZATION_ERROR, io.getCause());
			}
		}

		
		return object;
	}
	
	public static <T> List<T> getListObjectFromJsonTreeWithNoRootElement(final Object jsonTree, final Class<T> valueType) {		

		
		List<T> object = new ArrayList<>();
		if (jsonTree != null) {
			try {
				JsonNode jsonTreeNode = MAPPER.readTree(jsonTree.toString());
			
				if(jsonTreeNode!=null) {
					jsonTreeNode.forEach(treeNode -> {
						T obj = getObjectFromJson(treeNode, valueType);
						object.add(obj);
					});
				}

			} catch (IOException io) {
				log.error(ERROR_LOG_STR +" in method getListObjectFromJsonTreeWithNoRootElement(). Exception Message={}, Exception Stack ={}", io.getMessage(),io);

				throw new ServiceException(ErrorMessages.CPOP_OFFER_MOCK_JSON_SERIALIZATION_ERROR, io.getCause());
			}
		}

		return object;
	}
}
