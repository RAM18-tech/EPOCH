package com.dtv.dcp.epoch.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.CartContext;
import com.dtv.dcp.epoch.model.common.request.ChannelEligibility;
import com.dtv.dcp.epoch.model.common.request.CouponOffersRequest;
import com.dtv.dcp.epoch.model.common.request.CustomerContext;
import com.dtv.dcp.epoch.model.common.request.CustomerEligibility;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.request.CartOffer;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.service.DMALookUpService;
import com.dtv.dcp.epoch.service.ott.OttOffersService;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class Util {
	@Autowired
	CTOfferRequestHelper ctOfferRequestHelper;
	@Autowired
	OttOffersService ottOffersService;
	@Autowired
	RedisCacheHelper redisCacheHelper;
	@Autowired
	private DMALookUpService dmaLookUpService;
	private static Logger log = LoggerFactory.getLogger(Util.class);
	private static final String INTERNAL_SERVICE_EXCEPTION = "Internal Service Exception";
	private static final String DATE_TIME_FORMAT = "MM/dd/yyyy HH:mm:ss";
	private static final String ALT_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	private static final String PST = "PST";

	public static String formatHttpUrl(String baseUrl, String path) {
		if (StringUtils.isBlank(path)) {
			return baseUrl;
		} else if (path.startsWith("http")) {
			return path;
		}
		//log.info("URL={}{}", ESAPI.encoder().encodeForHTML(baseUrl), ESAPI.encoder().encodeForHTML(path));
		return baseUrl + path;
	}

	public static String createQueryString(final Map<String, Object> uriVariables) {
		final String format = "%s=%s&";
		final StringBuilder queryParamBuilder = new StringBuilder();

		uriVariables.entrySet().stream()
				.forEach(s -> queryParamBuilder.append(String.format(format, s.getKey(), s.getValue().toString())));

		return queryParamBuilder.toString();
	}

	public static boolean nonEmptyArray(JsonNode arrayNode) {
		return arrayNode != null && !arrayNode.isMissingNode() && arrayNode.size() > 0;
	}

	public static ServiceException createInternalServiceException(Exception e) {
		log.error(INTERNAL_SERVICE_EXCEPTION, e);

		String message = e.getMessage();

		if (message == null && e.getStackTrace().length > 0) {
			message = e.getStackTrace()[0].toString();
		}

		ServiceException ex = new ServiceException(ErrorMessages.INTERNAL_SERVER_ERROR);
		ex.addDetail(ErrorMessages.INTERNAL_SERVER_ERROR, message);

		return ex;
	}

	public static boolean checkValidityBaseOnDate(String startDate, String endDate) {
		boolean result = false;

		SimpleDateFormat sdf = new SimpleDateFormat(ALT_DATE_TIME_FORMAT);
		sdf.setTimeZone(TimeZone.getTimeZone(PST));

		if (startDate != null && endDate != null) {
			try {
				Date currentDate = sdf.parse(sdf.format(new Date()));
				Date start = sdf.parse(startDate);
				Date end = sdf.parse(endDate);
				if (start.before(currentDate) && (end.after(currentDate) || (end.equals(currentDate)))) {
					result = true;
				}
			} catch (ParseException e) {
				log.error("Invalid date present in the request - CatalogUtility.validateActiveFeedPromos() " + e);
			}

		}
		return result;
	}

	public static String convertDateToString(Date date) {

		SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
		sdf.setTimeZone(TimeZone.getTimeZone(PST));
		String strDate = sdf.format(date);
		return strDate;
	}

	public static String convertDateToUTCString(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat(ALT_DATE_TIME_FORMAT);
		sdf.setTimeZone(TimeZone.getTimeZone(PST));
		return sdf.format(date);
	}

	public static void createOrCleanDirectory(String fileDirectory) throws IOException {

		try {
			if (Files.exists(Paths.get(fileDirectory))) {
				FileUtils.cleanDirectory(new File(fileDirectory));
				if (log.isDebugEnabled())
					log.debug("Directory cleared - " + fileDirectory);
			} else {
				Files.createDirectories(Paths.get(fileDirectory));
				if (log.isDebugEnabled())
					log.debug("Directory created successfully - " + fileDirectory);
			}
		} catch (IOException e) {
			log.error("Error occurred while creating or cleaning the directory - " + fileDirectory + "- with - "
					+ e.getMessage());
			throw new IOException("Error occurred while creating or cleaning the directory", e);
		}

	}
	
	public static boolean isValidDateFormat(String date, String dateFormat) {
		try {
			DateFormat df = new SimpleDateFormat(dateFormat);
			df.setLenient(false);
			df.parse(date);
			return true;
		} catch (Exception e) {
			log.debug("Unable to Validate date {} with format {} : {}", date, dateFormat, e);
			return false;
		}
	}

	/**
	 * Checking given date is before Current Date
	 * @param date
	 * @return
	 */
	public static boolean isDateBeforeCurrentDate(String date) {
		boolean result = false;

		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_MM_DD_YYYY_SLASH);
		if (date != null) {
			try {
				Date currentDate = sdf.parse(sdf.format(new Date()));
				Date start = sdf.parse(date);
				if (start.before(currentDate)) {
					result = true;
				}
			} catch (ParseException e) {
				log.error("Invalid date present in the request - UTIl.isDateBeforeCurrentDate() " + e);
			}

		}
		return result;
	}

	public static String readFileAsString(String fileName, String path) {
		String fileContent = readFileContent(fileName);
		Gson gson = new Gson();
		JsonObject jsonObject = gson.fromJson(fileContent, JsonObject.class);
		return jsonObject.get(path).getAsString();
	}

	private static String readFileContent(String fileName) {
		InputStream inputStream = Util.class.getResourceAsStream(fileName);
		return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines()
				.collect(Collectors.joining("\n"));
	}

    public static String queryBuilder(List<String> keys, String fieldName) {
        if (keys != null && !keys.isEmpty()) {
            StringJoiner joiner = new StringJoiner("\",\"", "\"", "\"");
            keys.forEach(joiner::add);
            return fieldName + " in (" + joiner.toString() + ")";
        }
        return null;
    }

}
