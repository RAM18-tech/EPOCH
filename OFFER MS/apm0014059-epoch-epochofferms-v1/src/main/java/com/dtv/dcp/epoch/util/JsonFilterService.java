package com.dtv.dcp.epoch.util;


import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

/**
 * The Class JsonFilterService.
 */
public final class JsonFilterService {
	
	private static final Logger log = LoggerFactory.getLogger(JsonFilterService.class);
	
	public static final String FILTER_PROPERTIES_BY_NAME = "filter properties by name";
	
	/** The Constant MAPPER. */
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

	/**
	 * Filter attributes.
	 *
	 * @param input
	 * @param ignorableFieldNames
	 * @return json String
	 * @throws JsonProcessingException 
	 * @throws Exception
	 *             the exception
	 */
	public static String filterAttributes(Object input, String[] ignorableFieldNames) throws JsonProcessingException {

		ObjectMapper mapper = new ObjectMapper();
		mapper.addMixIn(Object.class, PropertyFilterMixIn.class);

		FilterProvider filters = new SimpleFilterProvider().addFilter(FILTER_PROPERTIES_BY_NAME,
				SimpleBeanPropertyFilter.serializeAllExcept(ignorableFieldNames));
		ObjectWriter writer = mapper.writer(filters);

		return writer.writeValueAsString(input);
	}
	
	public static <T> T filterAttributesAndGetObjectFromJson(final Object input, final Class<T> valueType, String[] ignorableFieldNames) {
		
		log.debug("Start of JsonFilterService.getObjectFromJson() method.. {}");
		
		T object = null;

		if (input != null) {
			try {
				MAPPER.addMixIn(Object.class, PropertyFilterMixIn.class);
				FilterProvider filters = new SimpleFilterProvider().addFilter(FILTER_PROPERTIES_BY_NAME,
						SimpleBeanPropertyFilter.serializeAllExcept(ignorableFieldNames));
				ObjectWriter writer = MAPPER.writer(filters);
				object = MAPPER.readValue(writer.writeValueAsString(input), valueType);
				//log.debug("After conversion from Json, Object is..{}" + object);
			} catch (IOException io) {
				log.error("Got IOException while converting from Json to Object..{}",io);
					throw new ServiceException(ErrorMessages.JSON_SERIALIZATION_ERROR, io.getCause());
			}
		}
		log.debug("End of JsonService.getObjectFromJson() method.. {}");
		
		return object;
	}
}