package com.dtv.dcp.epoch.util;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.integration.EpochClient;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.DtvnMidasRule;
import com.dtv.dcp.epoch.model.common.EnterpriseRule;
import com.dtv.dcp.epoch.model.common.MinMaxQuantity;
import com.dtv.dcp.epoch.model.common.request.*;
import com.dtv.dcp.epoch.model.ct.benefit.Benefit;
import com.dtv.dcp.epoch.model.ct.eligibility.Constraint;
import com.dtv.dcp.epoch.model.ct.eligibility.Eligibility;
import com.dtv.dcp.epoch.model.ct.generic.GenericTypeIdBase;
import com.dtv.dcp.epoch.model.ct.offer.*;
import com.dtv.dcp.epoch.model.ct.product.*;
import com.dtv.dcp.epoch.model.ct.product.Variant;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.model.customergraph.response.CGResponse;
import com.dtv.dcp.epoch.processor.ott.services.CustomerSubscriptionDetail;
import com.dtv.dcp.epoch.service.DMALookUpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * The Class OffersUtil.
 */
@Component
public class OffersUtils {

    private static final Logger log = LoggerFactory.getLogger(OffersUtils.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String EXCEPTION_OCCURED = " Exception Occured :";

    @Value("${ignoreAttributes}")
    private String ignoreAttributes;

    @Autowired
    EpochClient epochClient;

    @Value("${apiclient.rest.cpopofferms.reconnectEligibilityWindow}")
    private int reconnectEligibilityWindow;

    @Autowired
    CpopClientHelper cpopClientHelper;

    @Autowired
    EnterpriseRule enterpriseRule;

    @Autowired
    private FeatureManagerHelper featureHelper;

    @Autowired
    RedisCacheHelper redisCacheHelper;

    @Autowired
    DMALookUpService dmaLookUpService;

    /**
     * Instantiates a new offers util.
     */

    private OffersUtils() {
        // private constructor to hide the implicit public one
    }

    public boolean isADEFlow(CTOfferRequest ctOfferRequest, String feature) {
        return featureHelper.isEnabled(feature)
                && ((Optional.ofNullable(ctOfferRequest.getOfferActionType()).isPresent()
                && !ctOfferRequest.getOfferActionType().contains(Constants.ACQUISITION_ACTION_TYPE))
                && (Optional.ofNullable(ctOfferRequest.getSalesChannel()).isPresent()
                && ctOfferRequest.getSalesChannel().contains("online"))
                && (Optional.ofNullable(ctOfferRequest.getOfferProductFamily()).isPresent()
                && ctOfferRequest.getOfferProductFamily().contains("satellite")));
    }

    public boolean isPerformanceUpdatesEnabled(List<String> saleschannel) {
        return !CollectionUtils.isEmpty(saleschannel) && saleschannel.contains("partner") && featureHelper.isEnabled(Constants.FEATURE_TOGGLE_PERFORMANCE_UPDATES_ENABLED);
    }

    public List<DtvnMidasRule> fetchDtvnMidasRules() {

        try {
            return JsonService
                    .getListObjectFromJsonTreeWithNoRootElement(enterpriseRule.getDtvnow(), DtvnMidasRule.class);
        } catch (ServiceException se) {
            throw se;
        } catch (Exception e) {
            log.error(EXCEPTION_OCCURED, e);
            throw new ServiceException(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR)
                    .addDetail(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR_DETAILS001);
        }

    }

    public static boolean needsAttributeRemoval(List<String> offerProductFamilies, List<String> offerTypes, List<String> productTypes) {
        return !CollectionUtils.isEmpty(offerProductFamilies) && offerProductFamilies.contains(Constants.SATELLITE_PRODUCT_FAMILY)
                && (CollectionUtils.isEmpty(offerTypes) || (!CollectionUtils.isEmpty(offerTypes) && !offerTypes.contains(Constants.COUPON)))
                && (CollectionUtils.isEmpty(productTypes) || (!CollectionUtils.isEmpty(productTypes) && !productTypes.contains(Constants.CAMPAIGN)));
    }


    /**
     * Checks if is alphanumeric.
     *
     * @param str the str
     * @return true, if is alphanumeric
     */
    public static boolean isAlphanumeric(String str) {
        boolean flag = true;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c < 0x30 || (c >= 0x3a && c <= 0x40)) {
                flag = false;
            }
            if ((c > 0x5a && c <= 0x60) || c > 0x7a) {
                flag = false;
            }
        }
        return flag;
    }

    /**
     * Generates a cache key using the provided primary key.
     *
     * @param primaryKey the primary key
     * @return the generated cache key
     */
    public static String getKey(String primaryKey) {
        return primaryKey + "_" + Constants.CACHE_NAME + "_" + Constants.OBJECT_KEY + "_" + Constants.CACHE_VERSION_ID + "_" + Constants.APPLICATION_ID;
    }

    /**
     * Returns a predicate that filters distinct elements by a key.
     *
     * @param keyExtractor function to extract the key
     * @param <T>          the type of elements
     * @return predicate for distinct elements
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    /**
     * Validates and parses date strings to Date objects.
     *
     * @param strtAndEndDate map of date strings
     * @return map of parsed Date objects
     */
    public static Map<String, Date> validateDateFormat(Map<String, String> strtAndEndDate) {
        Map<String, Date> dateWithTimeList = new HashMap<>();
        SimpleDateFormat sdfWithTime = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);
        SimpleDateFormat sdfWithDate = new SimpleDateFormat(Constants.DATE_FORMAT);
        if (!MapUtils.isEmpty(strtAndEndDate)) {
            strtAndEndDate.forEach((k, v) -> {
                Date dateWithTime = null;
                try {
                    if (isPSTflagEnabled()) {
                        sdfWithTime.setTimeZone(TimeZone.getTimeZone(Constants.PST));
                    }
                    if (v != null) {
                        dateWithTime = sdfWithTime.parse(v);
                    }
                    dateWithTimeList.put(k, dateWithTime);
                } catch (ParseException e) {
                    try {
                        if (isPSTflagEnabled()) {
                            sdfWithDate.setTimeZone(TimeZone.getTimeZone(Constants.PST));
                        }
                        if (v != null) {
                            Calendar cal = Calendar.getInstance();
                            if (k.equals(Constants.PROMO_START_DATE)) {
                                cal = setTimeInDate(sdfWithDate, v);
                                cal.add(Calendar.HOUR_OF_DAY, 0);
                                cal.add(Calendar.MINUTE, 0);
                                cal.add(Calendar.SECOND, 0);
                                dateWithTimeList.put(k, cal.getTime());
                            }
                            if (k.equals(Constants.PROMO_END_DATE)) {
                                cal = setTimeInDate(sdfWithDate, v);
                                cal.add(Calendar.HOUR_OF_DAY, 23);
                                cal.add(Calendar.MINUTE, 59);
                                cal.add(Calendar.SECOND, 59);
                                dateWithTimeList.put(k, cal.getTime());
                            }
                            if (k.equals(Constants.NBCD_DATE)) {
                                cal = setTimeInDate(sdfWithDate, v);
                                cal.add(Calendar.HOUR_OF_DAY, 0);
                                cal.add(Calendar.MINUTE, 0);
                                cal.add(Calendar.SECOND, 0);
                                dateWithTimeList.put(k, cal.getTime());
                            }
                        }


                    } catch (ParseException ex) {
                        log.error(String.format("Invalid date present in the request - validateDateFormat() %s", ex));
                    }
                }
            });
        }
        return dateWithTimeList;
    }

    /**
     * Parses a date string and returns a Calendar object.
     *
     * @param sdfWithDate the date formatter
     * @param v           the date string
     * @return Calendar object
     * @throws ParseException if parsing fails
     */
    private static Calendar setTimeInDate(SimpleDateFormat sdfWithDate, String v) throws ParseException {
        Date date = sdfWithDate.parse(v);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    /**
     * Formats a CPOP date string.
     *
     * @param cpopDate the CPOP date string
     * @return formatted date string
     */
    public static String getFormattedDate(String cpopDate) {

        String formattedDate = null;
        if (isPSTflagEnabled()) {
            if (Optional.ofNullable(cpopDate).isPresent()) {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(Constants.CPOP_DATE_FORMATE);
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT);
                LocalDate date = LocalDate.parse(cpopDate, inputFormatter);
                formattedDate = outputFormatter.format(date);
            }
        } else {
            try {
                if (Optional.ofNullable(cpopDate).isPresent()) {
                    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(Constants.CPOP_DATE_FORMATE);
                    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT);
                    LocalDateTime date = LocalDateTime.parse(cpopDate, inputFormatter);
                    formattedDate = date.format(outputFormatter);
                }
            } catch (DateTimeParseException e) {
                log.error("Invalid date present in the request - isActive() ", e);
            }
        }
        return formattedDate;
    }

    /**
      * method to get number of months between two dates and 
      * check if it's the number given, return true or false
      * allows a tolerance of plus or minus 2 days
      * @param startDate 
      * @param endDate 
      * @param months
      * @return boolean
    */
    public static boolean validateMonthsBetweenDates(String startDate, String endDate, int months) {
        boolean result = false;
        Map<String, String> strtAndEndDateString = new HashMap<>();
        strtAndEndDateString.put(Constants.PROMO_START_DATE, startDate);
        strtAndEndDateString.put(Constants.PROMO_END_DATE, endDate);
        Map<String, Date> startAndEndDate = validateDateFormat(strtAndEndDateString);
        if (startDate != null && endDate != null && startAndEndDate.get(Constants.PROMO_START_DATE) != null
                && startAndEndDate.get(Constants.PROMO_END_DATE) != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(startAndEndDate.get(Constants.PROMO_START_DATE));
            cal.add(Calendar.MONTH, months);

            // Check if startDate + months is within ±2 days of endDate
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(startAndEndDate.get(Constants.PROMO_END_DATE));

            // Calculate the difference in days between expected and actual end date
            long expectedTimeMillis = cal.getTimeInMillis();
            long actualTimeMillis = endCal.getTimeInMillis();
            long diffInMillis = Math.abs(expectedTimeMillis - actualTimeMillis);
            long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);

            // Allow tolerance of ±2 days
            if (diffInDays <= 2) {
                result = true;
            }
        }
        return result;
    }

    /**
     * @param startDate
     * @param endDate
     * @return
     */
    public static boolean validateActiveDates(String startDate, String endDate, Date compareDate) {
        boolean result = false;
        Map<String, String> strtAndEndDateString = new HashMap<>();
        strtAndEndDateString.put(Constants.PROMO_START_DATE, startDate);
        strtAndEndDateString.put(Constants.PROMO_END_DATE, endDate);
        Map<String, Date> startAndEndDate = validateDateFormat(strtAndEndDateString);
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);
        if (isPSTflagEnabled()) {
            sdf.setTimeZone(TimeZone.getTimeZone(Constants.PST));
        } else {
            sdf.setTimeZone(TimeZone.getTimeZone(Constants.GMT));
        }
        boolean isCompareDatePresent = true;
        if (compareDate == null) {
            isCompareDatePresent = false;
            compareDate = new Date();
        }
        if (startDate != null && endDate != null && startAndEndDate.get(Constants.PROMO_START_DATE) != null
                && startAndEndDate.get(Constants.PROMO_END_DATE) != null) {
            try {
                if (isPSTflagEnabled()) {
                    Date currentDate = new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(compareDate));
                    Date start = new SimpleDateFormat(Constants.DATE_TIME_FORMAT)
                            .parse(sdf.format(startAndEndDate.get(Constants.PROMO_START_DATE)));
                    Date end = new SimpleDateFormat(Constants.DATE_TIME_FORMAT)
                            .parse(sdf.format(startAndEndDate.get(Constants.PROMO_END_DATE)));
                    if (isCompareDatePresent) {
                        if ((start.before(currentDate) || start.equals(currentDate)) && (end.after(currentDate) || end.equals(currentDate))) {
                            result = true;
                        }
                    } else {
                        if ((start.before(currentDate)) && (end.after(currentDate) || end.equals(currentDate))) {
                            result = true;
                        }
                    }
                } else {
                    Date currentDate = new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(compareDate));
                    Date start = startAndEndDate.get(Constants.PROMO_START_DATE);
                    Date end = startAndEndDate.get(Constants.PROMO_END_DATE);
                    if (isCompareDatePresent) {
                        currentDate = compareDate;
                        if ((start.before(currentDate) || start.equals(currentDate)) && (end.after(currentDate) || end.equals(currentDate))) {
                            result = true;
                        }
                    } else {
                        if ((start.before(currentDate) || start.equals(currentDate)) && (end.after(currentDate) || end.equals(currentDate))) {
                            result = true;
                        }
                    }
                }
            } catch (ParseException e) {
                log.error("Invalid date present in the request - isActive() ", e);
            }

        }
        return result;
    }

    /**
     * Validates if the NBCD date is within the start and end dates.
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @param nbcdDate  the NBCD date
     * @return true if within range, false otherwise
     */
    public static boolean validateActiveNBCDDates(String startDate, String endDate, String nbcdDate) {
        boolean result = false;
        Map<String, String> strtAndEndDateString = new HashMap<>();
        strtAndEndDateString.put(Constants.PROMO_START_DATE, startDate);
        strtAndEndDateString.put(Constants.PROMO_END_DATE, endDate);
        strtAndEndDateString.put(Constants.NBCD_DATE, nbcdDate);
        Map<String, Date> startAndEdnDate = validateDateFormat(strtAndEndDateString);
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);

        if (isPSTflagEnabled()) {
            sdf.setTimeZone(TimeZone.getTimeZone(Constants.PST));
        } else {
            sdf.setTimeZone(TimeZone.getTimeZone(Constants.GMT));
        }

        if (startDate != null && endDate != null && startAndEdnDate.get(Constants.PROMO_START_DATE) != null
                && startAndEdnDate.get(Constants.PROMO_END_DATE) != null) {
            try {
                if (isPSTflagEnabled()) {
                    if (nbcdDate != null) {
                        Date nbcd = new SimpleDateFormat(Constants.DATE_TIME_FORMAT)
                                .parse(sdf.format(startAndEdnDate.get(Constants.NBCD_DATE)));
                        Date start = new SimpleDateFormat(Constants.DATE_TIME_FORMAT)
                                .parse(sdf.format(startAndEdnDate.get(Constants.PROMO_START_DATE)));
                        Date end = new SimpleDateFormat(Constants.DATE_TIME_FORMAT)
                                .parse(sdf.format(startAndEdnDate.get(Constants.PROMO_END_DATE)));
                        if ((start.before(new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(nbcd)))
                                || start.equals(new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(nbcd))))
                                && (end.after(new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(nbcd)))
                                || (end.equals(new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(nbcd)))))) {
                            result = true;
                        }

                    } else {
                        Date start = new SimpleDateFormat(Constants.DATE_TIME_FORMAT)
                                .parse(sdf.format(startAndEdnDate.get(Constants.PROMO_START_DATE)));
                        Date end = new SimpleDateFormat(Constants.DATE_TIME_FORMAT)
                                .parse(sdf.format(startAndEdnDate.get(Constants.PROMO_END_DATE)));
                        if ((start.before(new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(new Date())))
                                || start.equals(new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(new Date()))))
                                && (end.after(new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(new Date())))
                                || (end.equals(new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(new Date())))))) {
                            result = true;
                        }
                    }
                } else {
                    if (nbcdDate != null) {
                        Date nbcd = startAndEdnDate.get(Constants.NBCD_DATE);
                        Date start = startAndEdnDate.get(Constants.PROMO_START_DATE);
                        Date end = startAndEdnDate.get(Constants.PROMO_END_DATE);
                        if ((start.before(nbcd) || start.equals(nbcd)) && (end.after(nbcd) || (end.equals(nbcd)))) {
                            result = true;
                        }

                    } else {
                        Date start = startAndEdnDate.get(Constants.PROMO_START_DATE);
                        Date end = startAndEdnDate.get(Constants.PROMO_END_DATE);
                        if ((start.before(new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(new Date())))
                                || start.equals(new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(new Date()))))
                                && (end.after(new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(new Date())))
                                || (end.equals(new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(new Date())))))) {
                            result = true;
                        }
                    }
                }

            } catch (ParseException e) {
                log.error("Invalid date present in the request - isActive() ", e);
            }

        }
        return result;
    }

    /**
     * Validates if the current date is within the start and end dates.
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @return true if within range, false otherwise
     */
    public static boolean validateActiveDates(String startDate, String endDate) {
        return validateActiveDates(startDate, endDate, null);
    }

    /**
     * Returns the number of days between the current date and the end date.
     *
     * @param endDate the end date
     * @return number of days
     */
    public static int returnNumberOfDays(String endDate) {
        LocalDate localCurrentDate = LocalDate.parse(LocalDate.now().toString(), DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_YYYY_MM_DD));
        LocalDate localEndDate = LocalDate.parse(endDate, DateTimeFormatter.ofPattern(Constants.DTVN_CPC_DATE_FORMAT));
        return Math.toIntExact(ChronoUnit.DAYS.between(localEndDate, localCurrentDate));
    }


    /**
     * Checks if the start date is greater than the current date.
     *
     * @param startDate the start date
     * @return true if start date is in the future, false otherwise
     */
    public static boolean isEndDateIsGreater(String startDate) {
        boolean result = false;
        Map<String, String> strtAndEndDateString = new HashMap<>();
        strtAndEndDateString.put(Constants.PROMO_START_DATE, startDate);

        Map<String, Date> startAndEdnDate = validateDateFormat(strtAndEndDateString);
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);
        if (isPSTflagEnabled()) {
            sdf.setTimeZone(TimeZone.getTimeZone(Constants.PST));
        } else {
            sdf.setTimeZone(TimeZone.getTimeZone(Constants.GMT));
        }
        if (startDate != null && startAndEdnDate.get(Constants.PROMO_START_DATE) != null) {
            try {
                if (isPSTflagEnabled()) {
                    Date start = new SimpleDateFormat(Constants.DATE_TIME_FORMAT)
                            .parse(sdf.format(startAndEdnDate.get(Constants.PROMO_START_DATE)));

                    if (start.after(new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(new Date())))) {
                        result = true;
                    }
                } else {
                    Date start = startAndEdnDate.get(Constants.PROMO_START_DATE);
                    if (start.after(new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(new Date())))) {
                        result = true;
                    }
                }
            } catch (ParseException e) {
                log.error("Invalid date present in the request - isActive()", e);
            }

        }
        return result;
    }

    /**
     * Checks if the start date is a future date.
     *
     * @param startDate the start date
     * @return true if future date, false otherwise
     */
    public static boolean isFutureDate(String startDate) {
        boolean result = false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_M_D_YYYY);
            Date currentDate = new SimpleDateFormat(Constants.DATE_FORMAT_M_D_YYYY).parse(sdf.format(new Date()));
            Date start = sdf.parse(startDate);
            if (start.after(currentDate)) {
                result = true;
            }
        } catch (ParseException e) {
            if (startDate.contains("/")) {
                startDate = startDate.replaceAll("/", "-");
            }
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_M_D_YYYY);
                Date currentDate = new SimpleDateFormat(Constants.DATE_FORMAT_M_D_YYYY).parse(sdf.format(new Date()));
                Date start = sdf.parse(startDate);
                if (start.after(currentDate)) {
                    result = true;
                }
            } catch (ParseException ex) {
                log.error(String.format("Invalid date2 present in the request - validateDateFormat() %s", ex));
            }
        }
        return result;
    }

    /**
     * Checks if the start date is after the end date.
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @return true if start date is after end date, false otherwise
     */
    public static boolean isDateAfter(String startDate, String endDate) {
        boolean result = false;
        Map<String, String> strtAndEndDateString = new HashMap<>();
        strtAndEndDateString.put(Constants.PROMO_START_DATE, startDate);
        strtAndEndDateString.put(Constants.PROMO_END_DATE, endDate);

        Map<String, Date> startAndEdnDate = validateDateFormat(strtAndEndDateString);
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);
        if (isPSTflagEnabled()) {
            sdf.setTimeZone(TimeZone.getTimeZone(Constants.PST));
        } else {
            sdf.setTimeZone(TimeZone.getTimeZone(Constants.GMT));
        }
        if (startDate != null && startAndEdnDate.get(Constants.PROMO_START_DATE) != null) {
            try {
                if (isPSTflagEnabled()) {
                    Date start = new SimpleDateFormat(Constants.DATE_TIME_FORMAT)
                            .parse(sdf.format(startAndEdnDate.get(Constants.PROMO_START_DATE)));
                    Date end = new SimpleDateFormat(Constants.DATE_TIME_FORMAT)
                            .parse(sdf.format(startAndEdnDate.get(Constants.PROMO_END_DATE)));

                    if (start.after(end)) {
                        result = true;
                    }
                } else {
                    Date start = startAndEdnDate.get(Constants.PROMO_START_DATE);
                    Date end = startAndEdnDate.get(Constants.PROMO_END_DATE);
                    if (start.after(end)) {
                        result = true;
                    }
                }
            } catch (ParseException e) {
                log.error("Invalid date present in the request - isActive() ", e);
            }

        }
        return result;
    }

    /**
     * Checks if the start date is the same or after the end date.
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @return true if same or after, false otherwise
     */
    public static boolean isDateSameOrAfter(String startDate, String endDate) {
        boolean result = false;
        Map<String, String> strtAndEndDateString = new HashMap<>();
        strtAndEndDateString.put(Constants.PROMO_START_DATE, startDate);
        strtAndEndDateString.put(Constants.NBCD_DATE, endDate);

        Map<String, Date> startAndEdnDate = validateDateFormat(strtAndEndDateString);
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);
        if (isPSTflagEnabled()) {
            sdf.setTimeZone(TimeZone.getTimeZone(Constants.PST));
        } else {
            sdf.setTimeZone(TimeZone.getTimeZone(Constants.GMT));
        }
        if (startDate != null && startAndEdnDate.get(Constants.PROMO_START_DATE) != null) {
            try {
                if (isPSTflagEnabled()) {
                    Date start = new SimpleDateFormat(Constants.DATE_TIME_FORMAT)
                            .parse(sdf.format(startAndEdnDate.get(Constants.PROMO_START_DATE)));
                    Date end = new SimpleDateFormat(Constants.DATE_TIME_FORMAT)
                            .parse(sdf.format(startAndEdnDate.get(Constants.NBCD_DATE)));

                    if (start.equals(end) || start.after(end)) {
                        result = true;
                    }
                } else {
                    Date start = startAndEdnDate.get(Constants.PROMO_START_DATE);
                    Date end = startAndEdnDate.get(Constants.NBCD_DATE);
                    if (start.equals(end) || start.after(end)) {
                        result = true;
                    }
                }
            } catch (ParseException e) {
                log.error("Invalid date present in the request - isActive() ", e);
            }

        }
        return result;
    }

    /**
     * Checks if the start date is before the current date.
     *
     * @param startDate the start date
     * @return true if active, false otherwise
     */
    public static boolean isDateActive(String startDate) {
        boolean result = false;
        Map<String, String> strtAndEndDateString = new HashMap<>();
        strtAndEndDateString.put(Constants.PROMO_START_DATE, startDate);

        Map<String, Date> startAndEdnDate = validateDateFormat(strtAndEndDateString);
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);
        if (isPSTflagEnabled()) {
            sdf.setTimeZone(TimeZone.getTimeZone(Constants.PST));
        } else {
            sdf.setTimeZone(TimeZone.getTimeZone(Constants.GMT));
        }
        if (startDate != null && startAndEdnDate.get(Constants.PROMO_START_DATE) != null) {
            try {
                if (isPSTflagEnabled()) {
                    Date start = new SimpleDateFormat(Constants.DATE_TIME_FORMAT)
                            .parse(sdf.format(startAndEdnDate.get(Constants.PROMO_START_DATE)));

                    if (start.before(new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(new Date())))) {
                        result = true;
                    }
                } else {
                    Date start = startAndEdnDate.get(Constants.PROMO_START_DATE);
                    if (start.before(new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(new Date())))) {
                        result = true;
                    }
                }
            } catch (ParseException e) {
                log.error("Invalid date present in the request - isActive() ", e);
            }

        }
        return result;
    }

    public boolean isDateWithinEligibilityWindow(String serviceEndDate, List<String> salesChannel, List<String> offerProductType, List<String> offerCodes) {

        log.info("offerRequestWrapper.getOfferRequest().getServiceEndDate() {}", sanitizeData(serviceEndDate));
        boolean isDateWithinEligibilityWindow = false;
        try {
            Date endDate = new SimpleDateFormat(Constants.DATE_FORMAT).parse(serviceEndDate);
            Calendar currentDateMinue12Months = Calendar.getInstance();
            Integer reconnectEligibilityWindowRedis = null;
            List<String> reconnectWindowValue = redisCacheHelper
                    .getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_RECONNECT_ELIGIBILITY_WINDOW, Constants.OTT);
            if (reconnectWindowValue != null && !reconnectWindowValue.isEmpty()
                    && reconnectWindowValue.get(0) != null) {
                reconnectEligibilityWindowRedis = Integer.parseInt(reconnectWindowValue.get(0));

                currentDateMinue12Months.add(Calendar.MONTH, -(reconnectEligibilityWindowRedis));

            } else {
                currentDateMinue12Months.add(Calendar.MONTH, -(reconnectEligibilityWindow));
            }

            if (Optional.ofNullable(offerCodes).isPresent() && !offerCodes.isEmpty()
                    && endDate.after(currentDateMinue12Months.getTime())) {

                isDateWithinEligibilityWindow = true;
            } else if (endDate.after(currentDateMinue12Months.getTime())) {
                isDateWithinEligibilityWindow = true;
            }

        } catch (ParseException e) {
            e.printStackTrace();
            isDateWithinEligibilityWindow = false;
        }
        return isDateWithinEligibilityWindow;
    }

    /**
     * Checks if the given string can be parsed to a Long.
     *
     * @param existingGroup the string to check
     * @return true if not parsable, false otherwise
     */
    public static boolean checkExistingGroupNumberValue(String existingGroup) {
        boolean result = false;
        try {
            Long.parseLong(existingGroup);
        } catch (Exception e) {
            result = true;
        }
        return result;

    }


    public void setContractorIndicatorForTAZ(OfferRequest offerRequest) {
        if (offerRequest.getSalesChannel().contains(Constants.OPUS)
                && (Objects.nonNull(offerRequest.getContractIndicator())
                && !offerRequest.getContractIndicator().contains(Constants.EDSP_STRING))) {
            if (offerRequest.isReconnectCustomer()
                    && !isDateWithinEligibilityWindow(offerRequest.getServiceEndDate(), offerRequest.getSalesChannel(), offerRequest.getOfferProductType(), offerRequest.getOfferCodes())) {
                offerRequest.setContractIndicator(List.of((Constants.TAZ_STRING)));
            }
        }
        if (offerRequest.getSalesChannel().contains(Constants.OPUS)
                && Objects.isNull(offerRequest.getContractIndicator())) {
            offerRequest.setContractIndicator(List.of((Constants.TAZ_STRING)));
        }
        if (offerRequest.getSalesChannel().contains(Constants.INDIRECT_PARTNER)
                && Objects.isNull(offerRequest.getContractIndicator())) {
            offerRequest.setContractIndicator(List.of((Constants.TAZ_STRING)));
        }

        if (offerRequest.getSalesChannel().contains(Constants.DIRECTV_ONLINE)
                && !Objects.nonNull(offerRequest.getContractIndicator())) {
            offerRequest.setContractIndicator(List.of((Constants.TAZ_STRING)));
        }

        if (offerRequest.getSalesChannel().contains(Constants.DIRECTV_STREAM_ONLINE)
                && !Objects.nonNull(offerRequest.getContractIndicator())) {
            offerRequest.setContractIndicator(List.of((Constants.TAZBYOD_STRING)));
        }
    }


    /**
     * Checks if the contract intent for the given DtvnMidasRule is "Contract".
     *
     * @param dtvnRule the DtvnMidasRule object to check
     * @return true if contract intent is "Contract", false otherwise
     */
    public static boolean getContractIntentForContract(DtvnMidasRule dtvnRule) {
        return Optional.ofNullable(dtvnRule.getContractIntent()).isPresent()
                && ("Contract".equalsIgnoreCase(dtvnRule.getContractIntent()));
    }

    /**
     * Checks if the contract intent for the given DtvnMidasRule is "Retail".
     *
     * @param dtvnRule the DtvnMidasRule object to check
     * @return true if contract intent is "Retail", false otherwise
     */
    public static boolean getContractIntentForNonContract(DtvnMidasRule dtvnRule) {
        return Optional.ofNullable(dtvnRule.getContractIntent()).isPresent()
                && ("Retail".equalsIgnoreCase(dtvnRule.getContractIntent()));
    }

    public static boolean getContractIntentForEdsp(DtvnMidasRule dtvnRule) {
        return Optional.ofNullable(dtvnRule.getContractIntent()).isPresent()
                && ("EDSP".equalsIgnoreCase(dtvnRule.getContractIntent()));
    }

    public static boolean getContractIntentForTAZ(DtvnMidasRule dtvnRule) {
        return Optional.ofNullable(dtvnRule.getContractIntent()).isPresent()
                && ("TAZ".equalsIgnoreCase(dtvnRule.getContractIntent()));
    }

    public static boolean getContractIntentForTAZBYOD(DtvnMidasRule dtvnRule) {
        return Optional.ofNullable(dtvnRule.getContractIntent()).isPresent()
                && ("TAZBYOD".equalsIgnoreCase(dtvnRule.getContractIntent()));
    }

    public static boolean getContractIntentForTAZCONTRACT(DtvnMidasRule dtvnRule) {
        return Optional.ofNullable(dtvnRule.getContractIntent()).isPresent()
                && ("TAZCONTRACT".equalsIgnoreCase(dtvnRule.getContractIntent()));
    }

    public static boolean getContractIntentForRR(DtvnMidasRule dtvnRule) {
        return Optional.ofNullable(dtvnRule.getContractIntent()).isPresent()
                && ("RR".equalsIgnoreCase(dtvnRule.getContractIntent()));
    }

    public static boolean getContractIntentForGENRE(DtvnMidasRule dtvnRule) {
        return Optional.ofNullable(dtvnRule.getContractIntent()).isPresent()
                && ("GENRE".equalsIgnoreCase(dtvnRule.getContractIntent()));
    }

    /**
     * This method checks if the contract intent for a given DtvnMidasRule is "FRONTPORCH".
     *
     * @param dtvnRule The DtvnMidasRule object to check.
     * @return boolean Returns true if the contract intent for the given DtvnMidasRule is "FRONTPORCH", false otherwise.
     */
    public static boolean getContractIntentForFRONTPORCH(DtvnMidasRule dtvnRule) {
        return Optional.ofNullable(dtvnRule.getContractIntent()).isPresent()
                && ("FRONTPORCH".equalsIgnoreCase(dtvnRule.getContractIntent()));
    }

    public static boolean isMatchFound(List<String> types, String matchType) {
        return types != null ? types.stream().anyMatch(type -> type.equalsIgnoreCase(matchType)) : false;
    }

    public static boolean isPSTflagEnabled() {
        return false;
    }

    public boolean isService(OfferRequest offerRequest) {
        return Optional.ofNullable(offerRequest.getOfferActionType()).isPresent()
                && (offerRequest.getOfferActionType().contains(Constants.UPGRADE_ACTION_TYPE)
                || offerRequest.getOfferActionType().contains(Constants.DOWNGRADE_ACTION_TYPE)
                || offerRequest.getOfferActionType().contains(Constants.CROSS_SELL_ACTION_TYPE)
                || offerRequest.getOfferActionType().contains(Constants.OTHER_ACTION_TYPE));
    }

    public boolean isOTTService(OfferRequest offerRequest) {
        return offerRequest.getOfferProductFamily().contains(Constants.OTT_PRODUCT_FAMILY) &&
                Optional.ofNullable(offerRequest.getOfferActionType()).isPresent()
                && (offerRequest.getOfferActionType().contains(Constants.UPGRADE_ACTION_TYPE)
                || offerRequest.getOfferActionType().contains(Constants.DOWNGRADE_ACTION_TYPE)
                || offerRequest.getOfferActionType().contains(Constants.CROSS_SELL_ACTION_TYPE)
                || offerRequest.getOfferActionType().contains(Constants.OTHER_ACTION_TYPE));
    }

    public boolean isServiceCT(CTOfferRequest offerRequest) {
        return Optional.ofNullable(offerRequest.getOfferActionType()).isPresent()
                && (offerRequest.getOfferActionType().contains(Constants.UPGRADE_ACTION_TYPE)
                || offerRequest.getOfferActionType().contains(Constants.DOWNGRADE_ACTION_TYPE)
                || offerRequest.getOfferActionType().contains(Constants.CROSS_SELL_ACTION_TYPE)
                || offerRequest.getOfferActionType().contains(Constants.OTHER_ACTION_TYPE));
    }

    /**
     * Filters the given list of CTOffer objects and returns offers whose fee type does not match the specified feeType.
     *
     * @param ctOffers the list of CTOffer objects to filter
     * @param feeType  the fee type to exclude from the results
     * @return a list of CTOffer objects with a different fee type than the specified feeType
     */
    public List<CTOffer> filterOtherFeeOffersByFeeType(List<CTOffer> ctOffers, String feeType) {
        List<CTOffer> otherFeeOffers = null;
        if (Optional.ofNullable(ctOffers).isPresent()) {
            otherFeeOffers = ctOffers.stream().filter(Objects::nonNull)
                    .filter(offer ->
                            offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts() != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0) != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts() != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0) != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj() != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants() != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0) != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes() != null &&
                                    !feeType.equalsIgnoreCase(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().getFeeType()))
                    .collect(Collectors.toList());
        }
        return otherFeeOffers;
    }

    /**
     * Filters the given list of CTOffer objects and returns offers whose fee type matches the specified feeType.
     *
     * @param ctOffers the list of CTOffer objects to filter
     * @param feeType  the fee type to include in the results
     * @return a list of CTOffer objects with the specified fee type
     */
    public List<CTOffer> filterFeeOffersByFeeType(List<CTOffer> ctOffers, String feeType) {
        List<CTOffer> otherFeeOffers = null;
        if (Optional.ofNullable(ctOffers).isPresent()) {
            otherFeeOffers = ctOffers.stream().filter(Objects::nonNull)
                    .filter(offer ->
                            offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts() != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0) != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts() != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0) != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj() != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants() != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0) != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes() != null &&
                                    feeType.equalsIgnoreCase(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().getFeeType()))
                    .collect(Collectors.toList());
        }
        return otherFeeOffers;
    }

    /**
     * Filters the given list of CTOffer objects and returns offers whose device type matches the specified deviceType.
     *
     * @param ctOffers   the list of CTOffer objects to filter
     * @param deviceType the device type to include in the results
     * @return a list of CTOffer objects with the specified device type
     */
    public List<CTOffer> filterDeviceOffersByDeviceType(List<CTOffer> ctOffers, String deviceType) {
        List<CTOffer> deviceOffers = null;
        if (Optional.ofNullable(ctOffers).isPresent()) {
            deviceOffers = ctOffers.stream().filter(Objects::nonNull)
                    .filter(offer ->
                            offer.getAttributes() != null &&
                                    offer.getAttributes().getAssociatedProducts() != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0) != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts() != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0) != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts() != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0) != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj() != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants() != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0) != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes() != null &&
                                    deviceType.equalsIgnoreCase(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().getDeviceType()))
                    .collect(Collectors.toList());
        }
        return deviceOffers;
    }

    public List<CTOffer> filterStandAloneOffersByAddOnTypePlanSubType(List<CTOffer> ctOffers, String addOnType, String planSubType, String accountType) {
        List<CTOffer> standAloneOffers = null;
        if (Optional.ofNullable(ctOffers).isPresent()) {
            standAloneOffers = ctOffers.stream().filter(Objects::nonNull)
                    .filter(offer ->
                            offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts() != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0) != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts() != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0) != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj() != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants() != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0) != null &&
                                    offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes() != null &&
                                    addOnType.equalsIgnoreCase(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().getAddOnType()) &&
                                    planSubType.equalsIgnoreCase(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().getPlanSubType()) &&
                                    offer.getAttributes().getEligibility() != null &&
                                    offer.getAttributes().getEligibility().getConstraints() != null &&
                                    offer.getAttributes().getEligibility().getConstraints().get(0) != null &&
                                    accountType.equalsIgnoreCase(offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegment()))
                    .collect(Collectors.toList());
        }
        return standAloneOffers;
    }

    public void excludeStandaloneInSubscription(CustomerSubscriptionDetail customerSubscriptionDetail,
                                                List<CTOffer> standAloneOffers, List<CTOffer> filteredStandAloneOffers) {
        standAloneOffers.stream().filter(Objects::nonNull).forEach(offer -> {
            List<AssociatedProduct> associatedProducts = offer.getAttributes().getAssociatedProducts();
            List<String> addOnServiceIdList = customerSubscriptionDetail.getAddOnServiceIdList();
            if (!CollectionUtils.isEmpty(associatedProducts)
                    && Objects.nonNull(associatedProducts.get(0))
                    && !CollectionUtils.isEmpty(associatedProducts.get(0).getBundleProducts())
                    && Objects.nonNull(associatedProducts.get(0).getBundleProducts().get(0))
                    && !CollectionUtils.isEmpty(associatedProducts.get(0).getBundleProducts().get(0).getProducts())
                    && Objects.nonNull(associatedProducts.get(0).getBundleProducts().get(0).getProducts().get(0))
                    && (CollectionUtils.isEmpty(addOnServiceIdList) ||
                    !addOnServiceIdList.contains(associatedProducts.get(0).getBundleProducts().get(0).getProducts().get(0).getKey()))) {
                filteredStandAloneOffers.add(offer);
            }
        });
    }

    public static String convertLongDateToStringDate(String date) {
        if (date != null) {
            return new SimpleDateFormat("MM/dd/yyyy").format(new Date(Long.parseLong(date)));
        }
        return null;
    }

    public static String getFirstAccount(String accountNumber) {
        if (!StringUtils.isEmpty(accountNumber)) {
            String[] pattern = accountNumber.split(",");
            return pattern[0];
        }
        return accountNumber;
    }

    /**
     * Gets the uverse customer account type.
     *
     * @param cgResponse the cg response
     * @return the uverse customer account type
     */
    public static String getUverseCustomerAccountType(CGResponse cgResponse) {
        if (Optional.ofNullable(cgResponse).isPresent()
                && Optional.ofNullable(cgResponse.getAccountInfo()).isPresent()) {
            return cgResponse.getAccountInfo().getAccountType();
        }
        return null;
    }

    public static String formatTwoDigits(int dayOfMonth) {
        String date = Integer.toString(dayOfMonth);
        if (dayOfMonth < 10) {
            NumberFormat f = new DecimalFormat("00");
            date = f.format(dayOfMonth);
        }
        return date;
    }

    public List<String> fetchCTZipcodes() {
        try {
            String zipcode = enterpriseRule.getCtZipCodes();
            return JsonService.getListObjectFromJsonTree(zipcode, "zipCode", String.class);

        } catch (ServiceException se) {
            throw se;
        } catch (Exception e) {
            log.error(" Exception Occured :", e);
            throw new ServiceException(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR)
                    .addDetail(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR_DETAILS001);
        }
    }

    public boolean isEDSPOnlyRequest(OfferRequest offerRequest) {
        return (!CollectionUtils.isEmpty(offerRequest.getContractIndicator())
                && offerRequest.getContractIndicator().size() == 1
                && "EDSP".equalsIgnoreCase(offerRequest.getContractIndicator().get(0)));
    }

    public static String sanitizeData(List inputList) {
        if (inputList != null && !inputList.isEmpty()) {
            return HtmlUtils.htmlEscape(inputList.toString());
        } else {
            return null;
        }
    }

    public static String sanitizeData(String input) {
        if (input != null) {
            return HtmlUtils.htmlEscape(input);
        } else {
            return null;
        }
    }

    public boolean containsEDSP(OfferRequest offerRequest) {
        return (!CollectionUtils.isEmpty(offerRequest.getContractIndicator())
                && offerRequest.getContractIndicator().contains("EDSP"));
    }

    public boolean containsTAZ(OfferRequest offerRequest) {
        return (!CollectionUtils.isEmpty(offerRequest.getContractIndicator())
                && offerRequest.getContractIndicator().contains("TAZ"));
    }

    public boolean containsTAZBYOD(OfferRequest offerRequest) {
        return (!CollectionUtils.isEmpty(offerRequest.getContractIndicator())
                && offerRequest.getContractIndicator().contains(Constants.TAZBYOD_STRING));
    }

    public boolean containsTAZCONTRACT(OfferRequest offerRequest) {
        return (!CollectionUtils.isEmpty(offerRequest.getContractIndicator())
                && offerRequest.getContractIndicator().contains(Constants.TAZCONTRACT_STRING));
    }

    public boolean containsROADRUNNER(OfferRequest offerRequest) {
        return (!CollectionUtils.isEmpty(offerRequest.getContractIndicator())
                && offerRequest.getContractIndicator().contains(Constants.ROAD_RUNNER));
    }

    public boolean containsGENRE(OfferRequest offerRequest) {
        return (!CollectionUtils.isEmpty(offerRequest.getContractIndicator())
                && offerRequest.getContractIndicator().contains(Constants.GENRE));
    }

    public boolean isActiveOffer(CTOffer ctOffer, Date serverDate) {
        return ctOffer != null
                && ctOffer.getStartDate() != null
                && ctOffer.getEndDate() != null
                && OffersUtils.validateActiveDates(
                OffersUtils.getFormattedDate(ctOffer.getStartDate()), OffersUtils.getFormattedDate(ctOffer.getEndDate()), serverDate);
    }

    public String getIgnoreAttributes() {
        return ignoreAttributes;
    }

    public List<CTOffer> filterOffersByProductType(List<CTOffer> ctOffers, String productType) {
        List<CTOffer> deviceOffers = null;
        if (Optional.ofNullable(ctOffers).isPresent()) {
            deviceOffers = ctOffers.stream().filter(Objects::nonNull).filter(offer ->
                            offer.getAttributes() != null && offer.getAttributes().getOfferProductType() != null &&
                                    productType.equalsIgnoreCase(offer.getAttributes().getOfferProductType()))
                    .collect(Collectors.toList());
        }
        return deviceOffers;
    }

    public List<CTOffer> filterOneTimePaymentOffers(List<CTOffer> ctOffers) {
        List<CTOffer> oneTimePaymentOffers = null;
        if (Optional.ofNullable(ctOffers).isPresent()) {
            //Get the billingID from offer
            //Go to the list of variants in all the associated products and select the price with the matching billing id
            //Check if 'NumberOfPayments' attribute is configured to '1'
            List<CTOffer> offersWithBillingId = ctOffers.stream().filter(Objects::nonNull).filter(offer ->
                    Objects.nonNull(offer.getAttributes())
                            && !StringUtils.isEmpty(offer.getAttributes().getBillingId())
                            && !StringUtils.isEmpty(offer.getAttributes().getBillingCode())
            ).collect(Collectors.toList());

            if (Objects.nonNull(offersWithBillingId) && !offersWithBillingId.isEmpty()) {
                oneTimePaymentOffers = offersWithBillingId.stream()
                        .filter(offer -> Objects.nonNull(offer.getAttributes())
                                && !CollectionUtils.isEmpty(offer.getAttributes().getAssociatedProducts())
                                && offer.getAttributes().getAssociatedProducts().stream().filter(associatedProducts ->
                                !CollectionUtils.isEmpty(associatedProducts.getBundleProducts())
                                        && associatedProducts.getBundleProducts().stream().filter(bundleProducts ->
                                        !CollectionUtils.isEmpty(bundleProducts.getProducts())
                                                && bundleProducts.getProducts().stream().filter(products ->
                                                Objects.nonNull(products.getObj())
                                                        && !CollectionUtils.isEmpty(products.getObj().getVariants())
                                                        && products.getObj().getVariants().stream().filter(variant ->
                                                        !CollectionUtils.isEmpty(variant.getPrices())
                                                                && variant.getPrices().stream().filter(price ->
                                                                Objects.nonNull(price.getBillingReferenceId())
                                                                        && offer.getAttributes().getBillingId().equals(price.getBillingReferenceId())
                                                                        && Objects.nonNull(price.getNumberOfPayments())
                                                                        && price.getNumberOfPayments().equals(Integer.valueOf(1))
                                                                        && !Objects.isNull(price.getValue())
                                                        ).count() > 0
                                                ).count() > 0
                                        ).count() > 0
                                ).count() > 0
                        ).count() > 0)
                        .collect(Collectors.toList());
            }
        }
        return oneTimePaymentOffers;
    }


    /**
     * Determines whether the specified CTOffer includes local channels.
     *
     * @param ctOffer the CTOffer object to check
     * @return true if the offer has local channels, false otherwise
     */
    public Boolean hasLocalChannels(CTOffer ctOffer) {
        Boolean hasLocalChannels = false;
        if (Objects.nonNull(ctOffer) && Objects.nonNull(ctOffer.getAttributes()) &&
                !CollectionUtils.isEmpty(ctOffer.getAttributes().getAssociatedProducts()) &&
                Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0)) &&
                !CollectionUtils.isEmpty(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts()) &&
                Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0)) &&
                !CollectionUtils.isEmpty(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts()) &&
                Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0)) &&
                Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj()) &&
                !CollectionUtils.isEmpty(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants()) &&
                Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0)) &&
                Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes()))

            hasLocalChannels = ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().getHasLocalChannels();

        return hasLocalChannels;
    }

    /**
     * Determines whether the specified CTOffer is eligible for the served market.
     *
     * @param ctOffer the CTOffer object to check
     * @return true if the offer is eligible for the served market, false otherwise
     */
    public Boolean isEligibleForServedMarket(CTOffer ctOffer) {
        Boolean isEligibleForServedMarket = false;
        if (Objects.nonNull(ctOffer.getAttributes()) && Objects.nonNull(ctOffer.getAttributes().getEligibleForServedMarket())) {
            isEligibleForServedMarket = ctOffer.getAttributes().getEligibleForServedMarket();
        }
        return isEligibleForServedMarket;
    }

    /**
     * Determines whether the specified CTOffer is a Roadrunner offer.
     *
     * @param ctOffer the CTOffer object to check
     * @return true if the offer is Roadrunner, false otherwise
     */
    public Boolean isRoadrunner(CTOffer ctOffer) {
        Boolean isRoadrunner = false;

        ProductObj productObj = getProductObjFromOffer(ctOffer);

        if (Objects.nonNull(productObj)
                && !CollectionUtils.isEmpty(productObj.getVariants())
                && Objects.nonNull(productObj.getVariants().get(0))
                && Objects.nonNull(productObj.getVariants().get(0).getAttributes())
                && Constants.ROADRUNNER.equalsIgnoreCase(productObj.getVariants().get(0).getAttributes().getBpType())) {
            isRoadrunner = true;
        }

        return isRoadrunner;
    }

    /**
     * This method is used to check whether it is Locals bolton or not
     *
     * @param ctOffer the CTOffer object to check
     * @return true if the offer isLocalsBolton, false otherwise
     */
    public Boolean isLocalsBolton(CTOffer ctOffer) {
        Boolean isLocalsBolton = false;

        ProductObj productObj = getProductObjFromOffer(ctOffer);

        if (Objects.nonNull(productObj)
                && !CollectionUtils.isEmpty(productObj.getVariants())
                && Objects.nonNull(productObj.getVariants().get(0))
                && Objects.nonNull(productObj.getVariants().get(0).getAttributes())
                && Constants.LOCALS_SUBCATEGORY.equalsIgnoreCase(productObj.getVariants().get(0).getAttributes().getSubCategory())) {
            isLocalsBolton = true;
        }

        return isLocalsBolton;
    }

    /**
     * This method is used to check whether the VideoAddon has local channels or not
     *
     * @param ctOffer the CTOffer object to check
     * @return true if the offer isLocalChannelVideoAddon, false otherwise
     */
    public Boolean isLocalChannelVideoAddon(CTOffer ctOffer) {
        Boolean isLocalChannelVideoAddon = null;
        ProductObj productObj = getProductObjFromOffer(ctOffer);

        if (Objects.nonNull(productObj)
                && !CollectionUtils.isEmpty(productObj.getVariants())
                && Objects.nonNull(productObj.getVariants().get(0))
                && Objects.nonNull(productObj.getVariants().get(0).getAttributes())) {
            isLocalChannelVideoAddon = productObj.getVariants().get(0).getAttributes().getHasLocalChannels();
        }

        return isLocalChannelVideoAddon;
    }

    /**
     * This method is used to get the product id from offer
     *
     * @param ctOffer the CTOffer object to check
     * @return productId from offer
     */
    public String getProductIdFromOffer(CTOffer ctOffer) {
        if (Objects.nonNull(ctOffer) && Objects.nonNull(ctOffer.getAttributes()) && !CollectionUtils.isEmpty(ctOffer.getAttributes().getAssociatedProducts())
                && !CollectionUtils.isEmpty(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts())
                && !CollectionUtils.isEmpty(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts())
                && Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getId())) {
            return ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getId();
        }
        return null;
    }

    /**
     * This method is used to get ProductObj from offer
     *
     * @param ctOffer the CTOffer object to check
     * @return ProductObj from offer
     */
    public ProductObj getProductObjFromOffer(CTOffer ctOffer) {
        if (Objects.nonNull(ctOffer) && Objects.nonNull(ctOffer.getAttributes())
                && !CollectionUtils.isEmpty(ctOffer.getAttributes().getAssociatedProducts())
                && Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0))
                && !CollectionUtils.isEmpty(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts())
                && Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0))
                && !CollectionUtils.isEmpty(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts())
                && Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0))
                && Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj())) {
            return ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj();
        }
        return null;
    }

    public CTOfferResponse filterInvalidOffer(CTOfferResponse processctOfferResponse) {

        if ((null != processctOfferResponse) && !processctOfferResponse.getOffers().isEmpty()) {
            List<CTOffer> filteredOffers = new ArrayList<>();
            processctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(offer -> {
                if (!Optional.ofNullable(offer.getAttributes().getOfferType()).isPresent() ||
                        (Optional.ofNullable(offer.getAttributes().getOfferType()).isPresent() &&
                                !offer.getAttributes().getOfferType().equalsIgnoreCase(Constants.FREETRIALWITHHYPHEN) && CollectionUtils.isEmpty(offer.getAttributes().getOpusStoreIds()))) {
                    filteredOffers.add(offer);
                }
            });
            processctOfferResponse.setOffers(filteredOffers);
            processctOfferResponse.setCount(filteredOffers.size());
            processctOfferResponse.setTotal(filteredOffers.size());
        }

        return processctOfferResponse;
    }

    public CTOfferResponse filterFreeTrailOffer(CTOfferResponse processctOfferResponse) {

        if ((null != processctOfferResponse) && !processctOfferResponse.getOffers().isEmpty()) {
            List<CTOffer> filteredOffers = new ArrayList<>();
            processctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(offer -> {
                if (!Optional.ofNullable(offer.getAttributes().getOfferType()).isPresent() ||
                        (Optional.ofNullable(offer.getAttributes().getOfferType()).isPresent() && !offer.getAttributes().getOfferType().equalsIgnoreCase(Constants.FREETRIALWITHHYPHEN))) {
                    filteredOffers.add(offer);
                }
            });
            processctOfferResponse.setOffers(filteredOffers);
            processctOfferResponse.setCount(filteredOffers.size());
            processctOfferResponse.setTotal(filteredOffers.size());
        }

        return processctOfferResponse;
    }

    public CTOfferResponse getDirectvOffers(CTOfferResponse processctOfferResponse) {

        if ((null != processctOfferResponse) && !processctOfferResponse.getOffers().isEmpty()) {
            List<CTOffer> filteredOffers = new ArrayList<>();
            processctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(offer -> {
                //For Free trial offers Setting conflicting offers as null. For the other offers returning the response as is
                if (!Optional.ofNullable(offer.getAttributes().getOfferType()).isPresent() ||
                        (Optional.ofNullable(offer.getAttributes().getOfferType()).isPresent() && offer.getAttributes().getOfferType().equalsIgnoreCase(Constants.FREETRIALWITHHYPHEN))) {
                    offer.getAttributes().setConflictingOffers(null);
                    filteredOffers.add(offer);
                } else if (Optional.ofNullable(offer.getAttributes().getEligibility()).isPresent()
                        && Optional.ofNullable(offer.getAttributes().getEligibility().getConstraints()).isPresent()
                        && !CollectionUtils.isEmpty(offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments())
                        && offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments().contains(Constants.EMPLOYEE)) {
                    filteredOffers.add(offer);
                }
            });
            processctOfferResponse.setOffers(filteredOffers);
            processctOfferResponse.setCount(filteredOffers.size());
            processctOfferResponse.setTotal(filteredOffers.size());
        }

        return processctOfferResponse;
    }

    public boolean isSTMSRequest(OfferRequestWrapper offerRequestWrapper) {
        List<String> billingProductCodes = new ArrayList<>();
        boolean isSTMSRequest = false;

        if (!offerRequestWrapper.getOfferRequest().getOfferActionType().contains(Constants.ACQUISITION)) {
            if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext())) {
                offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite().getProducts().forEach(product -> {
                    if (org.apache.commons.lang.StringUtils.isNotBlank(product.getBillingProductCode())) {
                        billingProductCodes.add(product.getBillingProductCode());
                    }
                });
            }
        }
        if (offerRequestWrapper.getOfferRequest().getOfferActionType().contains(Constants.ACQUISITION)
                && !StringUtils.isEmpty(offerRequestWrapper.getOfferRequest().getBillingSystem())
                && Constants.STMS.equalsIgnoreCase(offerRequestWrapper.getOfferRequest().getBillingSystem())) {
            isSTMSRequest = true;
        }
        if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getIsMigrationRequired())
                && Boolean.TRUE.equals(offerRequestWrapper.getOfferRequest().getIsMigrationRequired())) {
            isSTMSRequest = true;
        }

        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(billingProductCodes)) {
            isSTMSRequest = true;
        }
        return isSTMSRequest;
    }

    public Boolean isNotValidBasedOnBillingSystem(CTOffer ctOffer, OfferRequestWrapper offerRequestWrapper) {
        boolean isSTMSRequest = isSTMSRequest(offerRequestWrapper);
        if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getIsMigrationRequired())
                && offerRequestWrapper.getOfferRequest().getIsMigrationRequired().equals(Boolean.TRUE)) {
            if (!Constants.FEE.equalsIgnoreCase(ctOffer.getAttributes().getOfferProductType())
                    || (Constants.FEE.equalsIgnoreCase(ctOffer.getAttributes().getOfferProductType())
                    && (Constants.STMS.equalsIgnoreCase(ctOffer.getAttributes().getBillingSystem())
                    || StringUtils.isEmpty(ctOffer.getAttributes().getBillingSystem())))) {
                return false;
            } else {
                return true;
            }
        } else if (isSTMSRequest) {
            return Objects.nonNull(ctOffer) && Objects.nonNull(ctOffer.getAttributes()) && Constants.ENABLER.equalsIgnoreCase(ctOffer.getAttributes().getBillingSystem());
        } else {
            return Objects.nonNull(ctOffer) && Objects.nonNull(ctOffer.getAttributes()) && Constants.STMS.equalsIgnoreCase(ctOffer.getAttributes().getBillingSystem());
        }
    }

    /**
     * Return true, if sales sub channel matching.
     *
     * @param ctOffer             the CTOffer object to check
     * @param offerRequestWrapper the offer request wrapper
     * @return boolean value
     */
    public Boolean isValidBasedOnSalesSubChannel(CTOffer ctOffer, OfferRequestWrapper offerRequestWrapper) {
        if (CollectionUtils.isNotEmpty(ctOffer.getAttributes().getSalesSubChannel())) {
            if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getChannelEligibility()) && Objects
                    .nonNull(offerRequestWrapper.getOfferRequest().getChannelEligibility().getSalesSubChannel())) {
                return ctOffer.getAttributes().getSalesSubChannel().stream().anyMatch(offerRequestWrapper
                        .getOfferRequest().getChannelEligibility().getSalesSubChannel()::equalsIgnoreCase);

            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Return true, if commitment duration is not matching.
     *
     * @param ctOffer             the CTOffer object to check
     * @param selectedOffers      the selected offers
     * @param offerRequestWrapper the offer request wrapper
     * @return boolean value
     */
    public Boolean isNotValidBasedOnCommitmentDuration(CTOffer ctOffer, List<CTOffer> selectedOffers,
                                                       OfferRequestWrapper offerRequestWrapper) {
        if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getChannelEligibility()) && Constants.MDU_DTH
                .equalsIgnoreCase(offerRequestWrapper.getOfferRequest().getChannelEligibility().getSalesSubChannel())) {
            if (CollectionUtils.isNotEmpty(ctOffer.getAttributes().getEligibleCommitmentDuration())) {

                if (CollectionUtils.isNotEmpty(selectedOffers)) {

                    CTOffer ctBaseOffer = selectedOffers.stream()
                            .filter(offer -> offer != null && offer.getAttributes() != null
                                    && Constants.VIDEO_PLAN.equals(offer.getAttributes().getOfferProductType()))
                            .findFirst().orElse(null);

                    if (Objects.nonNull(ctBaseOffer)) {
                        String commDuration = ctBaseOffer.getAttributes().getCommitmentDuration();
                        if (Objects.nonNull(commDuration)) {
                            return ctOffer.getAttributes().getEligibleCommitmentDuration().stream()
                                    .noneMatch(commDuration::equalsIgnoreCase);
                        } else {
                            return true;
                        }
                    } else {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Return true, if sales sub channel is not eligible.
     *
     * @param ctOffer             the CTOffer object to check
     * @param offerRequestWrapper the offer request wrapper
     * @return boolean value
     */
    public Boolean isNotValidBasedOnIneligibleSalesSubChannel(CTOffer ctOffer,
                                                              OfferRequestWrapper offerRequestWrapper) {
        if (CollectionUtils.isNotEmpty(ctOffer.getAttributes().getIneligibleSalesSubChannel())) {
            if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getChannelEligibility()) && Objects
                    .nonNull(offerRequestWrapper.getOfferRequest().getChannelEligibility().getSalesSubChannel())) {
                return ctOffer.getAttributes().getIneligibleSalesSubChannel().stream().anyMatch(offerRequestWrapper
                        .getOfferRequest().getChannelEligibility().getSalesSubChannel()::equalsIgnoreCase);
            } else {
                return false;
            }
        }
        return false;
    }


    public static boolean checkOnlineRelatedChannel(String salesChannel) {
        return (salesChannel.equalsIgnoreCase(Constants.ONLINE) || salesChannel.equalsIgnoreCase(Constants.DIRECTV_ONLINE) || salesChannel.equalsIgnoreCase(Constants.INDIRECT_PARTNER)
                || salesChannel.equalsIgnoreCase(Constants.OEM_IAPFIRETV) || salesChannel.equalsIgnoreCase(Constants.OEM_IAPROKUTV) || salesChannel.equalsIgnoreCase(Constants.EVERGENT_CRM) || salesChannel.equalsIgnoreCase(Constants.OEM_IAP_GOOGLE)
                || salesChannel.equalsIgnoreCase(Constants.DIRECT_INTEGRATION_PARTNER) || salesChannel.equalsIgnoreCase(Constants.OSPREY) || salesChannel.equalsIgnoreCase(Constants.DIRECTV_STREAM_ONLINE) || salesChannel.equalsIgnoreCase(Constants.ISUROKUTV)
                || salesChannel.equalsIgnoreCase(Constants.ASSISTED_SALES));
    }

    public static boolean checkAgentIndirectChannels(String salesChannel, List<String> productFamily) {
        return !CollectionUtils.isEmpty(productFamily) && productFamily.contains(Constants.SATELLITE_PRODUCT_FAMILY)
                && (salesChannel.equalsIgnoreCase(Constants.SALES_CRM) || salesChannel.equalsIgnoreCase(Constants.CCAP) || salesChannel.equalsIgnoreCase(Constants.DPP)
                || salesChannel.equalsIgnoreCase(Constants.DIRECT_INTEGRATION_PARTNER) || salesChannel.equalsIgnoreCase(Constants.ASSISTED_SALES));
    }

    public boolean checkIsOemChannel(OfferRequest offerRequest) {
        return Optional.ofNullable(offerRequest).isPresent()
                && Optional.ofNullable(offerRequest.getSalesChannel()).isPresent()
                && !offerRequest.getSalesChannel().isEmpty()
                && Optional.ofNullable(offerRequest.getSalesChannel().get(0)).isPresent()
                && (offerRequest.getSalesChannel().get(0).equalsIgnoreCase(Constants.OEM_IAPFIRETV)
                || offerRequest.getSalesChannel().get(0).equalsIgnoreCase(Constants.OEM_IAPROKUTV)
                || offerRequest.getSalesChannel().get(0).equalsIgnoreCase(Constants.OEM_IAP_GOOGLE));
    }

    public static String getIapPartnerAccountType(OfferRequest offerRequest) {
        if (Objects.nonNull(offerRequest.getCustomerContext())
                && (Objects.nonNull(offerRequest.getCustomerContext().getOtt()))
                && (Objects.nonNull(offerRequest.getCustomerContext().getOtt().getIapPartnerAccountType()))
                && Optional.ofNullable(offerRequest.getCustomerContext().getOtt().getIapPartnerAccountType()).isPresent()) {
            return offerRequest.getCustomerContext().getOtt().getIapPartnerAccountType();
        }
        return null;
    }

    public void setMaxOccurance(List<String> customerSegments, Benefit benefit, CTOffer offer, OfferRequestWrapper offerRequestWrapper, int freeDeviceCount) {
        if (Objects.nonNull(customerSegments) && Objects.nonNull(benefit)) {
            if (customerSegments.contains(Constants.DEMO)) {
                benefit.setMaxOccurrence(Optional.ofNullable(benefit.getMaxOccurrence_demo()).isPresent() ? benefit.getMaxOccurrence_demo() : 0);
            } else if (customerSegments.contains(Constants.DECA)) {
                benefit.setMaxOccurrence(Optional.ofNullable(benefit.getMaxOccurrence_deca()).isPresent() ? benefit.getMaxOccurrence_deca() : 0);
            } else if (customerSegments.contains(Constants.SHOWROOM)) {
                benefit.setMaxOccurrence(Optional.ofNullable(benefit.getMaxOccurrence_showroom()).isPresent() ? benefit.getMaxOccurrence_showroom() : 0);
            } else if (customerSegments.contains(Constants.BCOMP)) {
                benefit.setMaxOccurrence(Optional.ofNullable(benefit.getMaxOccurrence_bcomp()).isPresent() ? benefit.getMaxOccurrence_bcomp() : 0);
            } else if (customerSegments.contains(Constants.COURTESY)) {
                benefit.setMaxOccurrence(Optional.ofNullable(benefit.getMaxOccurrence_courtesy()).isPresent() ? benefit.getMaxOccurrence_courtesy() : 0);
            }
        }
        if (Objects.nonNull(offer.getAttributes().getAssociatedProducts())
                && Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts())
                && Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts())
                && Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0))
                && Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj())
                && Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants())
                && Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0))
                && Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes())
                && Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().getMinMaxQuantity())) {

            if ((offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.MDUTENANT) || offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.SHOWROOM)
                    || offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.COURTESY) || offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.BCOMP))
                    && (Objects.nonNull(freeDeviceCount) && freeDeviceCount != 0 && Objects.nonNull(benefit))) {
                offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().setFreeDeviceCount(freeDeviceCount);
                if (Optional.ofNullable(offer.getAttributes().getBenefits()).isPresent() && !offer.getAttributes().getBenefits().isEmpty()) {
                    offer.getAttributes().getBenefits().forEach(benefitobj -> benefitobj.setMaxOccurrence(freeDeviceCount));
                }
            }
            if ((offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.DEMO) || offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.DECA))
                    && Objects.nonNull(benefit)) {
                offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().setFreeDeviceCount(benefit.getMaxOccurrence());
            }
            if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerSegments()).isPresent()
                    &&
                    (
                            offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.DEMO)
                                    ||
                                    (offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.MDUTENANT) && offer.getAttributes().isBulkOffer())
                    )
            ) {
                List<MinMaxQuantity> minmax = offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().getMinMaxQuantity();
                minmax.forEach(obj -> {
                    if (Objects.nonNull(obj.getContractApplicable())
                            && ((Objects.nonNull(offerRequestWrapper.getOfferRequest().getContractIndicator())
                            && isMatchFound(offerRequestWrapper.getOfferRequest().getContractIndicator(), obj.getContractApplicable().get(0)))
                            ||
                            obj.getContractApplicable().contains(offer.getAttributes().getContractIndicator())
                    )
                    ) {
                        if (Optional.ofNullable(offer.getAttributes().getBenefits()).isPresent() && !offer.getAttributes().getBenefits().isEmpty()) {
                            obj.setMaxQuantity(String.valueOf(offer.getAttributes().getBenefits().get(0).getMaxOccurrence()));
                        } else if (offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.MDUTENANT)) {
                            obj.setMaxQuantity(String.valueOf(freeDeviceCount));
                        }

                    }
                });
            }

        }

    }

    public List<CTOffer> removeBulkOffer(List<CTOffer> offers, String offerProductType, int maxOccurance) {

        List<CTOffer> finalOffers = new ArrayList<>();

        log.info("maxoccurance-------->>>>> {} ", maxOccurance);

        offers.forEach(offer -> {
            log.info("offer-------->>>>> {}", offer.getCode());
            if (Optional.ofNullable(offer).isPresent() && Optional.ofNullable(offer.getAttributes()).isPresent()
                    && Optional.ofNullable(offer.getAttributes().getOfferProductType()).isPresent()
                    && offer.getAttributes().getOfferProductType().equalsIgnoreCase(Constants.VIDEO_DEVICE)
                    && Optional.ofNullable(offer.getAttributes().getEligibility()).isPresent()
                    && Optional.ofNullable(offer.getAttributes().getEligibility().getConstraints()).isPresent()
                    && Optional.ofNullable(offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments()).isPresent()
                    && offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments().contains(Constants.MDUTENANT)) {

                log.info("logic-------->>>>> inside");
                if (maxOccurance > 0) {
                    if (Optional.ofNullable(offer.getAttributes().getBenefits()).isPresent() && !offer.getAttributes().getBenefits().isEmpty()
                            && offer.getAttributes().getBenefits().get(0).getMaxOccurrence() > 0 && Objects.nonNull(offer.getAttributes().isBulkOffer()) && offer.getAttributes().isBulkOffer()) {
                        finalOffers.add(offer);
                    }
                } else if (Objects.isNull(offer.getAttributes().isBulkOffer()) || !offer.getAttributes().isBulkOffer()) {
                    finalOffers.add(offer);
                }

            } else {
                log.info("logic-------->>>>> outside");
                finalOffers.add(offer);
            }
        });
        return finalOffers;

    }

    public static void calculateBestPriceForCredit(CTOffer offer, List<String> choiceGrpOnDeselection, List<String> choiceGrpOnSelection, OfferRequestWrapper offerRequestWrapper, boolean choiceGrpSalesChannelFlag) {
        log.info("API_NAME:EPOCH_GETOFFERS calculateBestPriceForCredit:{} ", offer.getCode());
        OfferPrice offerPrice = new OfferPrice();
        Double totalOfferPrice = 0.0;
        Double totalChoiceOnSelectionPrice = 0.0;
        Double totalChoiceOnDeSelectionPrice = 0.0;

        offer.getAttributes().setOfferPrice(null);
        offer.getAttributes().setBenefits(filterDuplicateBenefits(offer.getAttributes().getBenefits()));
        for (Benefit benefit : offer.getAttributes().getBenefits()) {
            if (benefit.getBenefitType().equalsIgnoreCase("flat-off")) {
                Double benefitPrice = benefit.getValue().getDollarAmount();
                totalOfferPrice = totalOfferPrice + benefitPrice;
            }
            if (offerRequestWrapper.getOfferRequest().getCartOffers() == null && benefit.getBenefitType().equalsIgnoreCase("flat-off") && choiceGrpOnSelection.contains(benefit.getCode())) {
                Double benefitPrice = benefit.getValue().getDollarAmount();
                totalChoiceOnSelectionPrice = totalChoiceOnSelectionPrice + benefitPrice;
            }
            if (offerRequestWrapper.getOfferRequest().getCartOffers() == null && benefit.getBenefitType().equalsIgnoreCase("flat-off") && choiceGrpOnDeselection.contains(benefit.getCode())) {
                Double benefitPrice = benefit.getValue().getDollarAmount();
                totalChoiceOnDeSelectionPrice = totalChoiceOnDeSelectionPrice + benefitPrice;
            }
        }
        BigDecimal bd = new BigDecimal(Double.toString(totalOfferPrice));
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        offerPrice.setDollarAmount(bd.doubleValue());
        if (offerRequestWrapper.getOfferRequest().getCartOffers() == null && choiceGrpSalesChannelFlag
                && (!choiceGrpOnDeselection.isEmpty() || !choiceGrpOnSelection.isEmpty())) {
            BigDecimal bd1 = new BigDecimal(Double.toString(totalChoiceOnSelectionPrice));
            bd1 = bd1.setScale(2, RoundingMode.HALF_UP);
            offerPrice.setPriceOnCGSelection(bd1.doubleValue());
            BigDecimal bd2 = new BigDecimal(Double.toString(totalChoiceOnDeSelectionPrice));
            bd2 = bd2.setScale(2, RoundingMode.HALF_UP);
            offerPrice.setPriceOnCGDeselection(bd2.doubleValue());

        }

        offer.getAttributes().setOfferPrice(offerPrice);
    }

    public static boolean checkOfferCodeInChoiceGrp(List<OfferChoiceGroup> choiceGrpOnSelection, OfferRequestWrapper offerRequestWrapper) {
        AtomicBoolean isOfferCode = new AtomicBoolean(false);
        if (choiceGrpOnSelection != null && !choiceGrpOnSelection.isEmpty()) {
            choiceGrpOnSelection.stream()
                    .filter(choice ->
                            (choice != null && choice.getChoiceGroupSalesChannel() != null && !choice.getChoiceGroupSalesChannel().isEmpty() &&
                                    choice.getChoiceGroupSalesChannel().contains(offerRequestWrapper.getOfferRequest().getSalesChannel().get(0)) && choice.getOfferCodes() != null &&
                                    ((choice.getPromosOnSelection() != null && !choice.getPromosOnSelection().isEmpty()) ||
                                            (choice.getPromosOnDeselection() != null && !choice.getPromosOnDeselection().isEmpty())))).forEach(choice -> {
                        isOfferCode.set(true);

                    });
        }
        return isOfferCode.get();

    }

    public void updateBenefitsBasedOnConditions(OfferRequestWrapper offerRequestWrapper, List<CTOffer> offerList) {
        List<String> filteredGraphQLVideoDeviceOfferCodes = filterVideoDeviceOffer(offerList);
        offerList.stream().filter(Objects::nonNull).forEach(offer -> {
            if (offerRequestWrapper.getOfferRequest() != null
                    && Objects.nonNull(offer.getAttributes())
                    && Objects.nonNull(offer.getAttributes().getOfferChoiceGroup())
                    && checkOfferCodeInChoiceGrp(offer.getAttributes().getOfferChoiceGroup(), offerRequestWrapper)
                    && CollectionUtils.isNotEmpty(filteredGraphQLVideoDeviceOfferCodes)) {
                updateBenefitsBasedOnVideoDeviceOfferCodes(offer, filteredGraphQLVideoDeviceOfferCodes);
            }
        });
        if (CollectionUtils.isNotEmpty(offerList)) {
            removedOffersBasedOnAdditionalEligibilityProductType(offerList);
        }
    }

    public static void calculateBestPrice(OfferRequestWrapper offerRequestWrapper, List<CTOffer> offerList) {
        boolean isBYODFlowStatus = Optional.ofNullable(offerRequestWrapper)
                .map(OfferRequestWrapper::getOfferRequest)
                .map(OfferRequest::getCustomerEligibility)
                .map(CustomerEligibility::isBYODFlow)
                .orElse(false);
        offerList.stream().filter(Objects::nonNull).forEach(offer -> {
            setBasePriceBasedOnPriceTier(offer, offerRequestWrapper);
            if (!(offerRequestWrapper.isNoDeviceFrameworkEnabled() && isBYODFlowStatus)
                    && offerRequestWrapper.getOfferRequest().getCartOffers() != null
                    && checkOfferCodeInChoiceGrp(offer.getAttributes().getOfferChoiceGroup(), offerRequestWrapper)) {
                updateBenefitsBasedOnSelection(offerRequestWrapper, offer);
            }
            AtomicReference<List<String>> choiceGrpOnSelection = new AtomicReference<>(new ArrayList<>());
            AtomicReference<List<String>> choiceGrpOnDeselection = new AtomicReference<>(new ArrayList<>());
            AtomicBoolean isChoicGrpSalesChannelPresentInRequest = new AtomicBoolean(false);
            if (Optional.ofNullable(offer.getAttributes().getOfferChoiceGroup()).isPresent()) {
                if (!offer.getAttributes().getOfferChoiceGroup().isEmpty()) {
                    offer.getAttributes().getOfferChoiceGroup().stream().filter(choice -> (choice != null && choice.getChoiceGroupSalesChannel() != null && !choice.getChoiceGroupSalesChannel().isEmpty() &&

                            (choice.getPromosOnSelection() != null && !choice.getPromosOnSelection().isEmpty()) || (choice.getPromosOnDeselection() != null && !choice.getPromosOnDeselection().isEmpty()))).forEach(choice -> {
                        if (choice.getChoiceGroupSalesChannel().contains(offerRequestWrapper.getOfferRequest().getSalesChannel().get(0))) {
                            if (choice.getPromosOnSelection() != null && !choice.getPromosOnSelection().isEmpty()) {
                                choiceGrpOnSelection.set(choice.getPromosOnSelection());
                            }
                            if (choice.getPromosOnDeselection() != null && !choice.getPromosOnDeselection().isEmpty()) {
                                choiceGrpOnDeselection.set(choice.getPromosOnDeselection());
                            }
                            choice.setOfferCodes(null);
                            if (choice.getPromosOnDeselection() == null && choice.getPromosOnSelection() != null) {
                                choice.setPromosOnDeselection(new ArrayList<>());
                            }
                            if (choice.getPromosOnDeselection() != null && choice.getPromosOnSelection() == null) {
                                choice.setPromosOnSelection(new ArrayList<>());
                            }
                            isChoicGrpSalesChannelPresentInRequest.set(true);
                        }


                    });
                }
            }

            if (Constants.CREDIT.equalsIgnoreCase(offer.getAttributes().getOfferProductType())) {
                calculateBestPriceForCredit(offer, choiceGrpOnDeselection.get(), choiceGrpOnSelection.get(), offerRequestWrapper, isChoicGrpSalesChannelPresentInRequest.get());
            } else {
                OfferPrice offerPrice = new OfferPrice();
                offer.getAttributes().setOfferPrice(offerPrice);
                log.info("API_NAME:EPOCH_GETOFFERS OfferCode:{} ", offer.getCode());
                Map<String, List<Benefit>> benefitData = new HashMap<>();
                Map<String, Product> productData = new HashMap<>();
                Map<String, List<Benefit>> benefitDataOnSelection = new HashMap<>();
                Map<String, List<Benefit>> benefitDataOnDeselection = new HashMap<>();
                offer.getAttributes().setBenefits(filterDuplicateBenefits(offer.getAttributes().getBenefits()));

                if (Optional.ofNullable(offer.getAttributes().getAssociatedProducts()).isPresent()) {
                    AssociatedProduct associatedProduct = offer.getAttributes().getAssociatedProducts().get(0);
                    if (Optional.ofNullable(associatedProduct.getBundleProducts()).isPresent()) {
                        ProductWrapper bundleProduct = associatedProduct.getBundleProducts().get(0);
                        if (Optional.ofNullable(bundleProduct).isPresent()) {
                            List<Product> products = bundleProduct.getProducts();
                            if (Optional.ofNullable(products).isPresent() && !products.isEmpty()) {
                                productData = products.stream().collect(Collectors.toMap(product -> product.getKey(), product -> product));
                                mapProductWithBenefits(offer, benefitData, productData, products);
                                if (offerRequestWrapper.getOfferRequest().getCartOffers() == null && !choiceGrpOnSelection.get().isEmpty()) {
                                    mapProductWithBenefitsInChoiceGrp(offer, benefitDataOnSelection, choiceGrpOnSelection.get(), products);
                                }
                                if (offerRequestWrapper.getOfferRequest().getCartOffers() == null && !choiceGrpOnDeselection.get().isEmpty()) {
                                    mapProductWithBenefitsInChoiceGrp(offer, benefitDataOnDeselection, choiceGrpOnDeselection.get(), products);
                                }
                                if (isChoicGrpSalesChannelPresentInRequest.get()) {
                                    setRackRateForPromoselectionOrDeselectionIsEmpty(offerRequestWrapper, offer, choiceGrpOnDeselection, choiceGrpOnSelection, benefitData, productData);
                                }
                            }
                        }
                    } else if (Optional.ofNullable(associatedProduct.getQualifyingProducts()).isPresent()) {
                        List<Product> products = getQualifyingProductsFromAssociatedProducts(offer.getAttributes().getAssociatedProducts());
                        if (Optional.ofNullable(products).isPresent() && !products.isEmpty()) {
                            productData = products.stream().collect(Collectors.toMap(product -> product.getKey(), product -> product));
                            mapProductWithBenefits(offer, benefitData, productData, products);
                            if (offerRequestWrapper.getOfferRequest().getCartOffers() == null && !choiceGrpOnSelection.get().isEmpty()) {
                                mapProductWithBenefitsInChoiceGrp(offer, benefitDataOnSelection, choiceGrpOnSelection.get(), products);
                            }
                            if (offerRequestWrapper.getOfferRequest().getCartOffers() == null && !choiceGrpOnDeselection.get().isEmpty()) {
                                mapProductWithBenefitsInChoiceGrp(offer, benefitDataOnDeselection, choiceGrpOnDeselection.get(), products);
                            }
                            if (isChoicGrpSalesChannelPresentInRequest.get()) {
                                setRackRateForPromoselectionOrDeselectionIsEmpty(offerRequestWrapper, offer, choiceGrpOnDeselection, choiceGrpOnSelection, benefitData, productData);
                            }
                        }
                    }
                }
                setPromoSelectionOrDeselectionPrice(offerRequestWrapper, offer, benefitData, productData, "noChoice");
                setPromoSelectionOrDeselectionPrice(offerRequestWrapper, offer, benefitDataOnSelection, productData, "promoOnSelect");
                setPromoSelectionOrDeselectionPrice(offerRequestWrapper, offer, benefitDataOnDeselection, productData, "promoOnDeSelect");
            }

        });
    }

    public static void setBasePriceBasedOnPriceTier(CTOffer offer, OfferRequestWrapper offerRequestWrapper) {
        if (Optional.ofNullable(offer.getAttributes().getAssociatedProducts()).isPresent()) {
            AssociatedProduct associatedProduct = offer.getAttributes().getAssociatedProducts().get(0);
            if (Optional.ofNullable(associatedProduct.getBundleProducts()).isPresent()) {
                ProductWrapper bundleProduct = associatedProduct.getBundleProducts().get(0);
                if (Optional.ofNullable(bundleProduct).isPresent()) {
                    List<Product> products = bundleProduct.getProducts();
                    setBasePriceTierPriceForQulifyingOrBundledProducts(offer, offerRequestWrapper, products);
                }
            }
            if (Optional.ofNullable(associatedProduct.getQualifyingProducts()).isPresent()) {
                ProductWrapper bundleProduct = associatedProduct.getQualifyingProducts().get(0);
                if (Optional.ofNullable(bundleProduct).isPresent()) {
                    List<Product> products = bundleProduct.getProducts();
                    setBasePriceTierPriceForQulifyingOrBundledProducts(offer, offerRequestWrapper, products);
                }
            }
        }
    }

    private static void setBasePriceTierPriceForQulifyingOrBundledProducts(CTOffer offer, OfferRequestWrapper offerRequestWrapper, List<Product> products) {
        if (Optional.ofNullable(products).isPresent() && !products.isEmpty()) {
            products.stream().forEach(product -> {
                if (Optional.ofNullable(product).isPresent() && Optional.ofNullable(product.getObj()).isPresent()) {
                    if (Optional.ofNullable(product.getObj().getVariants()).isPresent()
                            && !product.getObj().getVariants().isEmpty()) {
                        if (Optional.ofNullable(product.getObj().getVariants().get(0).getPrices()).isPresent()
                                && !product.getObj().getVariants().get(0).getPrices().isEmpty()
                                && product.getObj().getVariants().get(0).getPrices().size() > 1
                        ) {
                            String priceTier = offer.getAttributes().getPriceTier() != null ? offer.getAttributes().getPriceTier() : null;
                            List<Price> priceWithPriceTier = product.getObj().getVariants().get(0).getPrices().stream().filter(Objects::nonNull).filter(price -> price.getPriceTier() != null).collect(Collectors.toList());
                            List<Price> pricesWithOutPriceTier = product.getObj().getVariants().get(0).getPrices().stream().filter(Objects::nonNull).
                                    filter(price -> price.getPriceTier() == null).collect(Collectors.toList());
                            if (!priceWithPriceTier.isEmpty() && priceWithPriceTier.stream().anyMatch(price -> price.getPriceTier().contains(priceTier))) {
                                List<String> newPriceTier = new ArrayList<>();
                                newPriceTier.add(offer.getAttributes().getPriceTier());
                                priceWithPriceTier = priceWithPriceTier.stream().filter(price -> price.getPriceTier().contains(priceTier)).collect(Collectors.toList());
                                priceWithPriceTier.stream().forEach(price -> price.setPriceTier(newPriceTier));
                                product.getObj().getVariants().get(0).setPrices(priceWithPriceTier);

                            } else {
                                priceWithPriceTier.clear();
                            }
                            if (priceWithPriceTier.isEmpty()) {

                                product.getObj().getVariants().get(0).setPrices(pricesWithOutPriceTier);
                            }
                        }

                    }
                }
            });
        }
    }

    private static void setRackRateForPromoselectionOrDeselectionIsEmpty(OfferRequestWrapper offerRequestWrapper, CTOffer offer, AtomicReference<List<String>> choiceGrpOnDeselection, AtomicReference<List<String>> choiceGrpOnSelection, Map<String, List<Benefit>> benefitData, Map<String, Product> productData) {
        if (offerRequestWrapper.getOfferRequest().getCartOffers() == null && !choiceGrpOnDeselection.get().isEmpty() && choiceGrpOnSelection.get().isEmpty()) {

            Double totalBasePrice = 0.0;
            Set<String> productKeys = benefitData.keySet();
            OfferPrice op = offer.getAttributes().getOfferPrice();
            if (!productKeys.isEmpty()) {
                for (String productKey : productKeys) {
                    Product product = productData.get(productKey);
                    Double price = setBasePriceForProduct(op, product, offer, "promoOnSelect");
                    totalBasePrice = totalBasePrice + price;
                    log.info("API_NAME:EPOCH_GETOFFERS TotalBasePrice:{} ", totalBasePrice);
                }
            }
            BigDecimal bd = new BigDecimal(Double.toString(totalBasePrice));
            bd = bd.setScale(2, RoundingMode.HALF_UP);
            op.setPriceOnCGSelection(bd.doubleValue());
        }

        if (offerRequestWrapper.getOfferRequest().getCartOffers() == null && choiceGrpOnDeselection.get().isEmpty() && !choiceGrpOnSelection.get().isEmpty()) {

            Double totalBasePrice = 0.0;
            Set<String> productKeys = benefitData.keySet();
            OfferPrice op = offer.getAttributes().getOfferPrice();
            if (!productKeys.isEmpty()) {
                for (String productKey : productKeys) {
                    Product product = productData.get(productKey);
                    Double price = setBasePriceForProduct(op, product, offer, "promoOnDeSelect");
                    totalBasePrice = totalBasePrice + price;
                    log.info("API_NAME:EPOCH_GETOFFERS TotalBasePrice:{} ", totalBasePrice);
                }
            }
            BigDecimal bd = new BigDecimal(Double.toString(totalBasePrice));
            bd = bd.setScale(2, RoundingMode.HALF_UP);
            op.setPriceOnCGDeselection(bd.doubleValue());
        }
    }

    private static void mapProductWithBenefitsInChoiceGrp(CTOffer offer, Map<String, List<Benefit>> benefitData,
                                                          List<String> choiceGroupPromoSelectionOrDeselection, List<Product> products) {
        for (Product product : products) {
            List<Benefit> benefits = new ArrayList<>();
            offer.getAttributes().getBenefits().stream().filter(Objects::nonNull).forEach(benefit -> {
                if (Optional.ofNullable(benefit).isPresent() && !benefit.getApplicableProducts().isEmpty() &&
                        choiceGroupPromoSelectionOrDeselection.contains(benefit.getCode())) {
                    benefit.getApplicableProducts().stream().filter(Objects::nonNull).forEach(applicableProduct -> {
                        if (!applicableProduct.getProducts().isEmpty()) {
                            List<Product> applicableProducts = applicableProduct.getProducts().stream()
                                    .filter(Objects::nonNull).collect(Collectors.toList());
                            for (Product benefitProd : applicableProducts) {
                                if (null != benefitProd.getKey()
                                        && benefitProd.getKey().equalsIgnoreCase(product.getKey())) {
                                    benefits.add(benefit);
                                    //log.info("API_NAME:EPOCH_GETOFFERS BenefitProduct Key:{} ",benefitProd.getKey());
                                    //log.info("API_NAME:EPOCH_GETOFFERS Product Key:{} ",product.getKey());
                                }
                            }
                        }
                    });
                }
            });

            benefitData.put(product.getKey(), benefits);

        }
    }

    private static void setPromoSelectionOrDeselectionPrice(OfferRequestWrapper offerRequestWrapper, CTOffer offer, Map<String, List<Benefit>> benefitData, Map<String, Product> productData, String beenefitOnselection) {
        Double totalOfferPrice = 0.0;
        if (!benefitData.isEmpty()) {
            Double totalBasePrice = 0.0;
            Set<String> productKeys = benefitData.keySet();
            OfferPrice offerPrice = offer.getAttributes().getOfferPrice();
            if (!productKeys.isEmpty()) {
                for (String productKey : productKeys) {
                    Product product = productData.get(productKey);
                    Double price = setBasePriceForProduct(offerPrice, product, offer, beenefitOnselection);
                    totalBasePrice = totalBasePrice + price;
                    log.info("API_NAME:EPOCH_GETOFFERS TotalBasePrice:{} ", totalBasePrice);
                }
            }

            for (Map.Entry<String, List<Benefit>> entry : benefitData.entrySet()) {
                Product product = productData.get(entry.getKey());
                setBasePriceForProduct(offerPrice, product, offer, beenefitOnselection);
                Double price = calculateOfferBestPrice(offerRequestWrapper, offer, offerPrice, entry.getValue(), product, beenefitOnselection);
                totalOfferPrice = totalOfferPrice + price;
                log.info("API_NAME:EPOCH_GETOFFERS TotalOfferPrice:{} ", totalOfferPrice);
            }
            if (!(Double.compare(totalBasePrice, totalOfferPrice) == 0)) {
                BigDecimal bd = new BigDecimal(Double.toString(totalOfferPrice));
                bd = bd.setScale(2, RoundingMode.HALF_UP);

                if (beenefitOnselection.equals("promoOnSelect")) {
                    offerPrice.setPriceOnCGSelection(bd.doubleValue());
                } else if (beenefitOnselection.equals("promoOnDeSelect")) {
                    offerPrice.setPriceOnCGDeselection(bd.doubleValue());
                } else {
                    offerPrice.setDollarAmount(bd.doubleValue());
                }
                offer.getAttributes().setOfferPrice(offerPrice);
            } else {
                BigDecimal bd = new BigDecimal(Double.toString(totalBasePrice));
                bd = bd.setScale(2, RoundingMode.HALF_UP);

                if (beenefitOnselection.equals("promoOnSelect")) {
                    offerPrice.setPriceOnCGSelection(bd.doubleValue());
                } else if (beenefitOnselection.equals("promoOnDeSelect")) {
                    offerPrice.setPriceOnCGDeselection(bd.doubleValue());
                } else {
                    offerPrice.setDollarAmount(bd.doubleValue());
                }
                offer.getAttributes().setOfferPrice(offerPrice);
            }

            if (offer.getAttributes().isBulkOffer()) {
                BigDecimal bd = new BigDecimal(Double.toString(0.0));
                bd = bd.setScale(2, RoundingMode.HALF_UP);

                if (beenefitOnselection.equals("promoOnSelect")) {
                    offerPrice.setPriceOnCGSelection(bd.doubleValue());
                } else if (beenefitOnselection.equals("promoOnDeSelect")) {
                    offerPrice.setPriceOnCGDeselection(bd.doubleValue());
                } else {
                    offerPrice.setDollarAmount(bd.doubleValue());
                }
                offer.getAttributes().setOfferPrice(offerPrice);
            }
        }
    }

    public static void updateBenefitsBasedOnSelection(OfferRequestWrapper offerRequestWrapper, CTOffer offer) {
        boolean selectionStatus = checkCGPromosOnSelection(offerRequestWrapper, offer);
        if (selectionStatus) {
            offer.getAttributes().setBenefits(setBenefitsBasedOnOfferCGPromosOnSelection(offer, offer.getAttributes().getBenefits()));
        } else {
            offer.getAttributes().setBenefits(setBenefitsBasedOnOfferCGPromosOnDeSelection(offer, offer.getAttributes().getBenefits()));
        }
    }

    /*
     * To Exclude SAVENOW Benefits from offer price calculation
     */
    public static List<Benefit> filterSaveNowBenefits(OfferRequestWrapper offerRequestWrapper, List<Benefit> benefits) {
        if (offerRequestWrapper.getOfferRequest().getSalesChannel().stream()
                .noneMatch(s -> Constants.DIRECTV_ONLINE.equalsIgnoreCase(s)
                        || Constants.DIRECTV_STREAM_ONLINE.equalsIgnoreCase(s))) {
            benefits.removeIf(benefit -> Optional.ofNullable(benefit.getBillingBenefitCode()).isPresent()
                    && Optional.ofNullable(benefit.getExtPromoType()).isPresent()
                    && Constants.SAVE_NOW.equalsIgnoreCase(benefit.getExtPromoType()));
        }
        return benefits;
    }

    private static boolean checkCGPromosOnSelection(OfferRequestWrapper offerRequestWrapper, CTOffer offer) {
        boolean selectionStatus = false;

        // Check if cart offers in the offer request are not null
        if (offerRequestWrapper.getOfferRequest().getCartOffers() != null) {
            if (offer != null && offer.getAttributes() != null && offer.getAttributes().getOfferChoiceGroup() != null) {
                for (OfferChoiceGroup offerChoiceGroup : offer.getAttributes().getOfferChoiceGroup()) {
                    if (offerChoiceGroup.getOfferCodes() != null && offerChoiceGroup.getGroupSelectionCount() != null) {
                        int groupSelectionCount = Integer.parseInt(offerChoiceGroup.getGroupSelectionCount());
                        int offerCodesCount = offerChoiceGroup.getOfferCodes().size();

                        List<String> cartOfferCodes = offerRequestWrapper.getOfferRequest().getCartOffers().stream()
                                .map(cartOffer -> cartOffer.getOfferCode())
                                .collect(Collectors.toList());

                        if (offerCodesCount >= groupSelectionCount) {
                            long matchingOfferCodesCount = offerChoiceGroup.getOfferCodes().stream()
                                    .filter(cartOfferCodes::contains)
                                    .count();

                            selectionStatus = matchingOfferCodesCount >= groupSelectionCount;
                        } else {
                            selectionStatus = false;
                        }
                    }
                }
            }
        }
        return selectionStatus;
    }

    private static List<Benefit> setBenefitsBasedOnOfferCGPromosOnSelection(CTOffer offer, List<Benefit> benefitList) {
        List<Benefit> finalBenefits = new ArrayList<>();

        if (offer != null && offer.getAttributes() != null && offer.getAttributes().getOfferChoiceGroup() != null) {
            offer.getAttributes().getOfferChoiceGroup().forEach(offerChoiceGroup -> {
                if (offerChoiceGroup.getPromosOnSelection() != null) {
                    offerChoiceGroup.getPromosOnSelection().forEach(promoBenefit -> {
                        benefitList.forEach(benefit -> {
                            if (promoBenefit.equalsIgnoreCase(benefit.getCode())) {
                                finalBenefits.add(benefit);
                            }
                        });
                    });
                }
            });
        }

        offer.getAttributes().setBenefits(finalBenefits);
        return finalBenefits;
    }

    private static List<Benefit> setBenefitsBasedOnOfferCGPromosOnDeSelection(CTOffer offer, List<Benefit> benefitList) {
        List<Benefit> finalBenefits = new ArrayList<>();

        if (offer != null && offer.getAttributes() != null && offer.getAttributes().getOfferChoiceGroup() != null) {
            offer.getAttributes().getOfferChoiceGroup().forEach(offerChoiceGroup -> {
                if (offerChoiceGroup.getPromosOnDeselection() != null) {
                    offerChoiceGroup.getPromosOnDeselection().forEach(promoBenefit -> {
                        benefitList.forEach(benefit -> {
                            if (promoBenefit.equalsIgnoreCase(benefit.getCode())) {
                                finalBenefits.add(benefit);
                            }
                        });
                    });
                }
            });
        }

        offer.getAttributes().setBenefits(finalBenefits);
        return finalBenefits;
    }

    public static List<Benefit> filterDuplicateBenefits(List<Benefit> benefits) {

        List<String> benefit_codes = new ArrayList<>();
        List<Benefit> final_benefits = new ArrayList<>();


        benefits.stream().forEach(benefit -> {
            if (Optional.ofNullable(benefit.getBillingBenefitCode()).isPresent() && !benefit_codes.contains(benefit.getBillingBenefitCode())) {
                benefit_codes.add(benefit.getBillingBenefitCode());
                final_benefits.add(benefit);
            }
        });

        return final_benefits;

    }

    private static Double setBasePriceForProduct(OfferPrice offerPrice, Product product, CTOffer offer, String choiceGrp) {
        if (Optional.ofNullable(product).isPresent() && Optional.ofNullable(product.getObj()).isPresent()) {
            if (Optional.ofNullable(product.getObj().getVariants()).isPresent()
                    && !product.getObj().getVariants().isEmpty()) {
                if (Optional.ofNullable(product.getObj().getVariants().get(0).getPrices()).isPresent()
                        && !product.getObj().getVariants().get(0).getPrices().isEmpty()) {
                    product.getObj().getVariants().get(0).getPrices().stream().filter(Objects::nonNull)
                            .forEach(price -> {
                                Double dollarAmount = price.getValue() != null
                                        && price.getValue().getDollarAmount() != null
                                        ? price.getValue().getDollarAmount()
                                        : 0.0;

                                if (choiceGrp.equalsIgnoreCase("promoOnSelect")) {
                                    offerPrice.setPriceOnCGSelection(dollarAmount);
                                } else if (choiceGrp.equalsIgnoreCase("promoOnDeSelect")) {
                                    offerPrice.setPriceOnCGDeselection(dollarAmount);
                                } else {
                                    offerPrice.setDollarAmount(dollarAmount);
                                }
                            });
                }
            }
        }

        if (choiceGrp.equals("promoOnSelect")) {
            return offerPrice.getPriceOnCGSelection();
        } else if (choiceGrp.equals("promoOnDeSelect")) {
            return offerPrice.getPriceOnCGDeselection();
        } else {
            return offerPrice.getDollarAmount();
        }
    }

    private static Double calculateOfferBestPrice(OfferRequestWrapper offerRequestWrapper, CTOffer offer, OfferPrice offerPrice, List<Benefit> benefits,
                                                  Product product, String offerChoiceGrp) {
        //Call OfferUtils SaveNow method to exclude the benefit from offer price calculation
        benefits = filterSaveNowBenefits(offerRequestWrapper, benefits);
        if (benefits != null && !benefits.isEmpty()) {
            for (Benefit benefit : benefits) {
                if (Optional.ofNullable(benefit).isPresent()) {
                    if ((benefit.getBenefitType().equalsIgnoreCase("free-promo")
                            && product.getProductType().equalsIgnoreCase(Constants.VIDEO_PLAN))
                            && Optional.ofNullable(offer.getAttributes().getEligibility()).isPresent()
                            && !CollectionUtils.isEmpty(offer.getAttributes().getEligibility().getConstraints())
                            && !CollectionUtils.isEmpty(offer.getAttributes().getEligibility().getConstraints().get(0)
                            .getCustomerSegments())
                            && !offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments()
                            .contains(Constants.EMPLOYEE) || benefit.getBenefitType().equalsIgnoreCase("free-trail")) {

                        if (offerChoiceGrp.equals("promoOnSelect")) {
                            offerPrice.setPriceOnCGSelection(offerPrice.getPriceOnCGSelection());
                        } else if (offerChoiceGrp.equals("promoOnDeSelect")) {
                            offerPrice.setPriceOnCGDeselection(offerPrice.getPriceOnCGDeselection());
                        } else {
                            offerPrice.setDollarAmount(offerPrice.getDollarAmount());
                        }
                    } else if ((benefit.getBenefitType().equalsIgnoreCase("free-promo")
                            && product.getProductType().equalsIgnoreCase(Constants.VIDEO_PLAN)
                            && Optional.ofNullable(offer.getAttributes().getEligibility()).isPresent()
                            && !CollectionUtils.isEmpty(offer.getAttributes().getEligibility().getConstraints())
                            && !CollectionUtils.isEmpty(offer.getAttributes().getEligibility().getConstraints().get(0)
                            .getCustomerSegments())
                            && offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments()
                            .contains(Constants.EMPLOYEE))) {

                        if (offerChoiceGrp.equals("promoOnSelect")) {
                            offerPrice.setPriceOnCGSelection(0.0);
                        } else if (offerChoiceGrp.equals("promoOnDeSelect")) {
                            offerPrice.setPriceOnCGDeselection(0.0);
                        } else {
                            offerPrice.setDollarAmount(0);
                        }
                    } else if (benefit.getBenefitType().equalsIgnoreCase("free-trail")) {

                        if (offerChoiceGrp.equals("promoOnSelect")) {
                            offerPrice.setPriceOnCGSelection(offerPrice.getPriceOnCGSelection());
                        } else if (offerChoiceGrp.equals("promoOnDeSelect")) {
                            offerPrice.setPriceOnCGDeselection(offerPrice.getPriceOnCGDeselection());
                        } else {
                            offerPrice.setDollarAmount(offerPrice.getDollarAmount());
                        }
                    } else if (Optional.ofNullable(benefit.getBenefitType()).isPresent()
                            && benefit.getBenefitType().equalsIgnoreCase("free-promo")
                            && !product.getProductType().equalsIgnoreCase(Constants.VIDEO_PLAN)) {

                        if (offerChoiceGrp.equals("promoOnSelect")) {
                            offerPrice.setPriceOnCGSelection(0.0);
                        } else if (offerChoiceGrp.equals("promoOnDeSelect")) {
                            offerPrice.setPriceOnCGDeselection(0.0);
                        } else {
                            offerPrice.setDollarAmount(0);
                        }
                    } else if (Optional.ofNullable(benefit).isPresent()
                            && Optional.ofNullable(benefit.getValue()).isPresent()
                            && Optional.ofNullable(benefit.getBenefitType()).isPresent()
                            && benefit.getBenefitType().equalsIgnoreCase("flat-off")) {

                        if (offerChoiceGrp.equals("promoOnSelect")) {
                            offerPrice.setPriceOnCGSelection(offerPrice.getPriceOnCGSelection() - benefit.getValue().getDollarAmount());
                        } else if (offerChoiceGrp.equals("promoOnDeSelect")) {
                            offerPrice.setPriceOnCGDeselection(offerPrice.getPriceOnCGDeselection() - benefit.getValue().getDollarAmount());
                        } else {
                            offerPrice.setDollarAmount(offerPrice.getDollarAmount() - benefit.getValue().getDollarAmount());
                        }
                    } else if (Optional.ofNullable(benefit).isPresent()
                            && Optional.ofNullable(benefit.getValue()).isPresent()
                            && Optional.ofNullable(benefit.getBenefitType()).isPresent()
                            && benefit.getBenefitType().equalsIgnoreCase("percent-off")
                            && benefit.getValue().getPercentage() != null && benefit.getValue().getPercentage() != 0) {
                        Double percentageOff = new Double(0);

                        if (offerChoiceGrp.equals("promoOnSelect")) {
                            percentageOff = ((offerPrice.getPriceOnCGSelection())
                                    * (Double.valueOf(benefit.getValue().getPercentage()) / 100));
                            offerPrice.setPriceOnCGSelection(offerPrice.getPriceOnCGSelection() - percentageOff);
                        } else if (offerChoiceGrp.equals("promoOnDeSelect")) {
                            percentageOff = ((offerPrice.getPriceOnCGDeselection())
                                    * (Double.valueOf(benefit.getValue().getPercentage()) / 100));
                            offerPrice.setPriceOnCGDeselection(offerPrice.getPriceOnCGDeselection() - percentageOff);
                        } else {
                            percentageOff = ((offerPrice.getDollarAmount())
                                    * (Double.valueOf(benefit.getValue().getPercentage()) / 100));
                            offerPrice.setDollarAmount(offerPrice.getDollarAmount() - percentageOff);

                        }
                    } else if (Optional.ofNullable(benefit).isPresent()
                            && Optional.ofNullable(benefit.getValue()).isPresent()
                            && Optional.ofNullable(benefit.getBenefitType()).isPresent()
                            && benefit.getBenefitType().equalsIgnoreCase("flat-rate")) {

                        if (offerChoiceGrp.equals("promoOnSelect")) {
                            offerPrice.setPriceOnCGSelection(benefit.getValue().getDollarAmount());
                        } else if (offerChoiceGrp.equals("promoOnDeSelect")) {

                            offerPrice.setPriceOnCGDeselection(benefit.getValue().getDollarAmount());
                        } else {
                            offerPrice.setDollarAmount(benefit.getValue().getDollarAmount());
                        }
                    }
                }
            }
        }
        if (offerChoiceGrp.equals("promoOnSelect")) {
            return offerPrice.getPriceOnCGSelection();
        } else if (offerChoiceGrp.equals("promoOnDeSelect")) {
            return offerPrice.getPriceOnCGDeselection();
        } else {
            return offerPrice.getDollarAmount();
        }
    }

    private static void mapProductWithBenefits(CTOffer offer, Map<String, List<Benefit>> benefitData,
                                               Map<String, Product> productData, List<Product> products) {
        for (Product product : products) {
            List<Benefit> benefits = new ArrayList<>();
            offer.getAttributes().getBenefits().stream().filter(Objects::nonNull).forEach(benefit -> {
                if (Optional.ofNullable(benefit).isPresent() && !benefit.getApplicableProducts().isEmpty()) {
                    benefit.getApplicableProducts().stream().filter(Objects::nonNull).forEach(applicableProduct -> {
                        if (!applicableProduct.getProducts().isEmpty()) {
                            List<Product> applicableProducts = applicableProduct.getProducts().stream()
                                    .filter(Objects::nonNull).collect(Collectors.toList());
                            for (Product benefitProd : applicableProducts) {
                                if (null != benefitProd.getKey()
                                        && benefitProd.getKey().equalsIgnoreCase(product.getKey())) {
                                    benefits.add(benefit);
                                    //log.info("API_NAME:EPOCH_GETOFFERS BenefitProduct Key:{} ",benefitProd.getKey());
                                    //log.info("API_NAME:EPOCH_GETOFFERS Product Key:{} ",product.getKey());

                                }
                            }
                        }
                    });
                }
            });

            benefitData.put(product.getKey(), benefits);

        }
    }

    /**
     * @param product           remove expired prices from product price list
     * @param contractIndicator contract indicator to filter prices
     * @return product with valid prices
     */
    public static Product removeExpiredPricesFromList(Product product, String contractIndicator) {

        product.getObj().getVariants().stream().filter(Objects::nonNull).forEach(variant -> {
            List<Price> newList = variant.getPrices().stream().filter(Objects::nonNull)
                    .filter(price -> price != null && price.getEndDate() != null
                            && OffersUtils.validateActiveDates(OffersUtils.getFormattedDate(price.getStartDate()),
                            OffersUtils.getFormattedDate(price.getEndDate()))
                            && price.getContractIndicator().equalsIgnoreCase(contractIndicator))
                    .collect(Collectors.toList());
            variant.setPrices(newList);
        });

        return product;
    }

    /**
     * Convert date string to LocalDate object
     *
     * @param dateStr date string
     * @return LocalDate object
     */
    public static LocalDate convertToLocalDate(String dateStr) {
        LocalDate date = null;
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("[yyyy-MM-dd]")
                .appendPattern("[MM/dd/yyyy]").toFormatter();
        try {
            date = LocalDate.parse(dateStr, formatter);
        } catch (Exception ex) {
            log.error("Unable to parse string {} to LocalDate object", dateStr, ex);
        }
        return date;
    }

    public static Date getServerDateValue(OfferRequestWrapper offerRequestWrapper) {

        Date serverDate = null;
        String serverDateStr = null;
        if (Objects.nonNull(offerRequestWrapper)) {
            serverDateStr = offerRequestWrapper.getOfferRequest().getServerDate();
        }
        Map<String, String> serverDateString = new HashMap<>();
        serverDateString.put(Constants.PROMO_START_DATE, serverDateStr);
        Map<String, Date> serverDateMap = OffersUtils.validateDateFormat(serverDateString);

        serverDate = serverDateMap.get(Constants.PROMO_START_DATE);

        return serverDate;

    }

    public void filterOfferByAttribute(CTOfferResponse ctOfferResponse, PartnerDealerDetails partnerDetails) {
        if (!Objects.isNull(partnerDetails)) {
            List<CTOffer> removeOffers = new ArrayList<>();
            List<CTOffer> offers = ctOfferResponse.getOffers();
            List<GenericTypeIdBase> removableConflictingOffers = new ArrayList<>();
            if (!CollectionUtils.isEmpty(offers)) {
                offers.stream().filter(Objects::nonNull).forEach(offer -> {
                    boolean checkPartnerDealerCode1 = partnerDetails.getPartnerDealerCode1() != null && !CollectionUtils.isEmpty(offer.getAttributes().getPartnerDealerCode1());
                    boolean checkPartnerDealerCode2 = partnerDetails.getPartnerDealerCode2() != null && !CollectionUtils.isEmpty(offer.getAttributes().getPartnerDealerCode2());
                    if ((checkPartnerDealerCode1 && !offer.getAttributes().getPartnerDealerCode1().contains(partnerDetails.getPartnerDealerCode1()))
                            || (checkPartnerDealerCode2 && !offer.getAttributes().getPartnerDealerCode2().contains(partnerDetails.getPartnerDealerCode2()))) {
                        removeOffers.add(offer);
                    } else {
                        if (!CollectionUtils.isEmpty(offer.getAttributes().getConflictingOffers()) &&
                                (checkPartnerDealerCode1 && offer.getAttributes().getPartnerDealerCode1().contains(partnerDetails.getPartnerDealerCode1()))
                                || (checkPartnerDealerCode2 && offer.getAttributes().getPartnerDealerCode2().contains(partnerDetails.getPartnerDealerCode2()))) {
                            removableConflictingOffers.addAll(offer.getAttributes().getConflictingOffers());
                            offer.getAttributes().getConflictingOffers().clear();
                        }
                    }
                });
            }
            removeConflictingOffers(removeOffers, offers, removableConflictingOffers);
            if (!CollectionUtils.isEmpty(removeOffers) && !CollectionUtils.isEmpty(offers)) {
                offers.removeAll(removeOffers);
                ctOfferResponse.setCount(ctOfferResponse.getOffers().size());
                ctOfferResponse.setOffers(offers);
            }
        }
    }

    private void removeConflictingOffers(List<CTOffer> removeOffers, List<CTOffer> offers,
                                         List<GenericTypeIdBase> removableConflictingOffers) {
        if (!CollectionUtils.isEmpty(removableConflictingOffers)) {
            List<String> keys = removableConflictingOffers.stream()
                    .map(GenericTypeIdBase::getKey).collect(Collectors.toList());
            offers.stream().filter(Objects::nonNull).forEach(offer -> {
                if (!CollectionUtils.isEmpty(keys)
                        && keys.contains(offer.getCode())) {
                    removeOffers.add(offer);
                }
            });
        }
    }

    public List<CTOffer> filterDOFeeOffer(CTOfferResponse ctOfferResponse, OfferRequestWrapper offerRequestWrapper) {
        return ctOfferResponse.getOffers().stream().filter(offer ->
                Objects.nonNull(offer.getAttributes())
                        && (

                        (Objects.isNull(offer.getAttributes().getCreditRisk()) ||
                                (
                                        Objects.nonNull(offerRequestWrapper.getOfferRequest().getCreditRisk())
                                                && Objects.nonNull(offer.getAttributes().getCreditRisk())
                                                && (offer.getAttributes().getCreditRisk().contains(offerRequestWrapper.getOfferRequest().getCreditRisk())))
                        )

                                ||

                                (Objects.isNull(offer.getAttributes().getTreatmentCode())) ||
                                (
                                        Objects.nonNull(offerRequestWrapper.getOfferRequest().getTreatmentCode())
                                                && Objects.nonNull(offer.getAttributes().getTreatmentCode())
                                                && (offer.getAttributes().getTreatmentCode().contains(offerRequestWrapper.getOfferRequest().getTreatmentCode()))
                                )

                )
        ).collect(Collectors.toList());
    }

    public List<CTOffer> filterDOFeeOffersBasedOnCustomerType(CTOfferResponse ctOfferResponse,
                                                              OfferRequestWrapper offerRequestWrapper) {
        return null;
    }

    //acquisition flow and not reconnect
    public boolean isCustomerTypeAcquisitionOffers(OfferRequestWrapper offerRequestWrapper, CTOffer offer) {
        log.debug("isCustomerTypeAcquisitionOffers start");
        boolean result = false;
        if (null == offer.getAttributes().getCustomerTypes()
                || offer.getAttributes().getCustomerTypes().stream().anyMatch(type -> type.equalsIgnoreCase(Constants.ACQUISITION))) {
            result = true;
        }
        log.debug("isCustomerTypeAcquisitionOffers end");
        return result;
    }

    // acquisition flow with reconnect < eligible months
    public boolean isCustomerTypeReconnectLessThanEligibleMonthsOffers(OfferRequestWrapper offerRequestWrapper, CTOffer offer) {
        log.debug("isCustomerTypeReconnectLessThanEligibleMonthsOffers start");
        boolean result = false;
        if (null == offer.getAttributes().getCustomerTypes()
                || isCustomerTypeReconnect(offer)) {
            // return offers if ReconnectSubscriberType is empty
            if (Objects.isNull(offer.getAttributes().getReconnectSubscriberType())) {
                result = true;
            } // return offers if ReconnectSubscriberType matches with
            // existingAccountSubscriberType with offer & request and
            // offer CustomerSubtypes is empty
            else if (isValidExistingAccountSubscriberType(offerRequestWrapper, offer)
                    && Objects.isNull(offerRequestWrapper.getOfferRequest().getCustomerSubType())
                    && Objects.isNull(offer.getAttributes().getCustomerSubtypes())) {
                result = true;
            } // return offers if ReconnectSubscriberType matches with
            // existingAccountSubscriberType and
            // CustomerSubtypes matches with customerSubType in offer & request
            else if (isValidExistingAccountSubscriberType(offerRequestWrapper, offer)
                    && isValidCustomerSubtypes(offerRequestWrapper, offer)) {
                result = true;
            }
        }
        log.debug("isCustomerTypeReconnectLessThanEligibleMonthsOffers end");
        return result;
    }

    // filter offers based on offerActionType: Reconnect or reconnect < eligible months
    public void filterOffersBasedOnCustomerTypeReconnectGreaterThanEligibleMonths(CTOfferResponse ctOfferResponse,
                                                                                  OfferRequestWrapper offerRequestWrapper) {
        log.debug("filterOffersBasedOnCustomerTypeReconnectGreaterThanEligibleMonths() start");
        if ((null != ctOfferResponse) && !ctOfferResponse.getOffers().isEmpty()) {
            List<CTOffer> filteredOffers = new ArrayList<>();
            ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(offer -> {
                boolean isValidOffer = isCustomerTypeReconnectGreaterThanEligibleMonthsOffers(offerRequestWrapper, offer);
                if (isValidOffer) {
                    filteredOffers.add(offer);
                }
            });
            ctOfferResponse.setOffers(filteredOffers);
            ctOfferResponse.setCount(filteredOffers.size());
            ctOfferResponse.setTotal(filteredOffers.size());
        }
        log.debug("filterOffersBasedOnCustomerTypeReconnectGreaterThanEligibleMonths() end");
    }

    public boolean isAcquisitionAndNotReconnectFlow(OfferRequestWrapper offerRequestWrapper) {
        log.debug("inside isAcquisitionAndNotReconnectFlow()");
        if (offerRequestWrapper == null || offerRequestWrapper.getOfferRequest() == null) {
            return false;
        }

        OfferRequest offerRequest = offerRequestWrapper.getOfferRequest();
        List<String> offerActionType = Optional.ofNullable(offerRequest.getOfferActionType()).orElse(Collections.emptyList());
        Boolean isReconnectCustomer = Optional.ofNullable(offerRequest.isReconnectCustomer()).orElse(false);

        return (offerActionType.contains(Constants.ACQUISITION) || offerActionType.contains(Constants.UPSELL_ACTION_TYPE))
                && !isReconnectCustomer;
    }

    public boolean isEligibleToReconnectLessThanEligibleMonths(OfferRequestWrapper offerRequestWrapper) {
        log.debug("inside isEligibleToReconnectLessThanEligibleMonths()");
        return isEligibleForReconnectFlow(offerRequestWrapper, true);
    }

    public boolean isEligibleToReconnectGreaterThanEligibleMonths(OfferRequestWrapper offerRequestWrapper) {
        log.debug("inside isEligibleToReconnectGreaterThanEligibleMonths()");
        return isEligibleForReconnectFlow(offerRequestWrapper, false);
    }

    private boolean isEligibleForReconnectFlow(OfferRequestWrapper offerRequestWrapper, boolean isLessThanEligibleMonths) {
        log.debug("isEligibleForReconnectFlow() start");

        if (offerRequestWrapper == null || offerRequestWrapper.getOfferRequest() == null) {
            return false;
        }

        OfferRequest offerRequest = offerRequestWrapper.getOfferRequest();
        List<String> offerActionType = Optional.ofNullable(offerRequest.getOfferActionType()).orElse(Collections.emptyList());
        Boolean isReconnectCustomer = Optional.ofNullable(offerRequest.isReconnectCustomer()).orElse(false);

        if (!isReconnectCustomer || offerActionType.isEmpty() ||
                !(offerActionType.contains(Constants.ACQUISITION) || offerActionType.contains(Constants.UPSELL_ACTION_TYPE))) {
            return false;
        }

        if (Optional.ofNullable(offerRequest.getServiceEndDate()).isEmpty()) {
            return false;
        }

        List<String> offerCodes = Optional.ofNullable(offerRequest.getOfferCodes()).orElse(Collections.emptyList());
        boolean isWithinEligibilityWindow = isDateWithinEligibilityWindow(offerRequest.getServiceEndDate(),
                offerRequest.getSalesChannel(), offerRequest.getOfferProductType(), offerCodes);

        boolean eligible = isLessThanEligibleMonths ? isWithinEligibilityWindow : !isWithinEligibilityWindow;

        log.debug("isEligibleForReconnectFlow() end");
        return eligible;
    }

    //return true is offer & request values matches
    public boolean isValidExistingAccountSubscriberType(OfferRequestWrapper offerRequestWrapper, CTOffer offer) {
        return Objects.nonNull(offer.getAttributes().getReconnectSubscriberType())
                && !offer.getAttributes().getReconnectSubscriberType().isEmpty()
                && Objects.nonNull(offerRequestWrapper.getOfferRequest().getExistingAccountSubscriberType())
                && offer.getAttributes().getReconnectSubscriberType().stream()
                .anyMatch(offerRequestWrapper.getOfferRequest().getExistingAccountSubscriberType()::equalsIgnoreCase);
    }

    //return true is offer & request values matches
    public boolean isValidCustomerSubtypes(OfferRequestWrapper offerRequestWrapper, CTOffer offer) {
        return Objects.nonNull(offer.getAttributes().getCustomerSubtypes())
                && !offer.getAttributes().getCustomerSubtypes().isEmpty()
                && Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerSubType())
                && offer.getAttributes().getCustomerSubtypes().stream()
                .anyMatch(offerRequestWrapper.getOfferRequest().getCustomerSubType()::equalsIgnoreCase);
    }

    // filter offers based on offerActionType: Acquisition or reconnect > 12 months
    public void filterOffersBasedOnCustomerTypeAcquisition(CTOfferResponse ctOfferResponse,
                                                           OfferRequestWrapper offerRequestWrapper) {
        log.debug("filterOffersBasedOnCustomerTypeAcquisition() start");
        if ((null != ctOfferResponse) && !ctOfferResponse.getOffers().isEmpty()) {
            List<CTOffer> filteredOffers = new ArrayList<>();
            ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(offer -> {
                boolean isValidOffer = isCustomerTypeAcquisitionOffers(offerRequestWrapper, offer);
                if (isValidOffer) {
                    filteredOffers.add(offer);
                }
            });
            ctOfferResponse.setOffers(filteredOffers);
            ctOfferResponse.setCount(filteredOffers.size());
            ctOfferResponse.setTotal(filteredOffers.size());
        }
        log.debug("filterOffersBasedOnCustomerTypeAcquisition() end");
    }

    // filter offers based on offerActionType: Reconnect or reconnect < 12 months
    public void filterOffersBasedOnCustomerTypeReconnect(CTOfferResponse ctOfferResponse,
                                                         OfferRequestWrapper offerRequestWrapper) {
        log.debug("filterOffersBasedOnCustomerTypeReconnect() start");
        if ((null != ctOfferResponse) && ctOfferResponse.getOffers().size() > 0) {
            List<CTOffer> filteredOffers = new ArrayList<>();
            ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(offer -> {
                boolean isValidOffer = isCustomerTypeReconnectLessThanEligibleMonthsOffers(offerRequestWrapper, offer);
                if (isValidOffer) {
                    filteredOffers.add(offer);
                }
            });
            ctOfferResponse.setOffers(filteredOffers);
            ctOfferResponse.setCount(filteredOffers.size());
            ctOfferResponse.setTotal(filteredOffers.size());
        }
        log.debug("filterOffersBasedOnCustomerTypeReconnect() end");
    }

    public boolean isEligibleToReconnect(OfferRequestWrapper offerRequestWrapper, List<String> offerProductType) {
        List<String> offerCodes = Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferCodes()).isPresent()
                ? offerRequestWrapper.getOfferRequest().getOfferCodes()
                : new ArrayList<>();
        return isDateWithinEligibilityWindow(offerRequestWrapper.getOfferRequest().getServiceEndDate(),
                offerRequestWrapper.getOfferRequest().getSalesChannel(), offerProductType, offerCodes);
    }

    public boolean isReconnectFlow(OfferRequestWrapper offerRequestWrapper) {
        return (Optional.ofNullable(offerRequestWrapper.getOfferRequest()).isPresent()
                && (containsEDSP(offerRequestWrapper.getOfferRequest())
                || containsTAZ(offerRequestWrapper.getOfferRequest())
                || containsTAZBYOD(offerRequestWrapper.getOfferRequest())
                || containsTAZCONTRACT(offerRequestWrapper.getOfferRequest()))
                && Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferActionType()).isPresent()
                && offerRequestWrapper.getOfferRequest().getOfferActionType().contains(Constants.ACQUISITION)
                && Optional.ofNullable(offerRequestWrapper.getOfferRequest().isReconnectCustomer()).isPresent()
                && offerRequestWrapper.getOfferRequest().isReconnectCustomer() == true) ? true : false;
    }

    public boolean isValidSubscribeType(OfferRequestWrapper offerRequestWrapper) {
        List<String> notValidSubscribeType = Arrays.asList(Constants.BYOD, Constants.DTVN);
        return (!Optional.ofNullable(offerRequestWrapper.getOfferRequest().getExistingAccountSubscriberType())
                .isPresent()
                || (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getExistingAccountSubscriberType())
                .isPresent()
                && !notValidSubscribeType
                .contains(offerRequestWrapper.getOfferRequest().getExistingAccountSubscriberType())));
    }

    /**
     * Method to get the qualifying products from the associated products
     *
     * @param associatedProducts list of associated products
     * @return qualifyingProducts list of products
     */
    public static List<Product> getQualifyingProductsFromAssociatedProducts(List<AssociatedProduct> associatedProducts) {
        List<Product> qualifyingProducts = new ArrayList<>();
        if (Objects.nonNull(associatedProducts) && !associatedProducts.isEmpty()) {
            for (AssociatedProduct associatedProduct : associatedProducts) {
                if (Objects.nonNull(associatedProduct.getQualifyingProducts()) && !associatedProduct.getQualifyingProducts().isEmpty()) {
                    for (ProductWrapper qualifyingProduct : associatedProduct.getQualifyingProducts()) {
                        if (Objects.nonNull(qualifyingProduct.getProducts()) && !qualifyingProduct.getProducts().isEmpty()) {
                            qualifyingProducts.addAll(qualifyingProduct.getProducts());
                        }
                    }
                }
            }
        }
        return qualifyingProducts;
    }

    public List<CTOffer> filterOffersByProductsHavingSubCategoryLocals(List<CTOffer> ctOffers) {
        log.debug("filterOffersByProductsHavingSubCategoryLocals() start");
        List<CTOffer> filteredOffers = new ArrayList<>();
        if ((null != ctOffers) && !ctOffers.isEmpty()) {
            ctOffers.stream().filter(Objects::nonNull).forEach(offer -> {
                boolean isValidLocalOffer = isProductsHavingSubCategoryLocals(offer);
                if (isValidLocalOffer) {
                    filteredOffers.add(offer);
                }
            });
        }
        log.debug("filterOffersByProductsHavingSubCategoryLocals() end");
        return filteredOffers;
    }

    public boolean isProductsHavingSubCategoryLocals(CTOffer offer) {
        log.debug("isProductsHavingSubCategoryLocals:{} method start");
        boolean result = false;
        if (CollectionUtils.isNotEmpty(offer.getAttributes().getAssociatedProducts()) &&
                CollectionUtils.isNotEmpty(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts())
                && CollectionUtils.isNotEmpty(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts())
                && offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts()
                .stream()
                .anyMatch(product ->
                        Objects.nonNull(product.getObj())
                                && CollectionUtils.isNotEmpty(product.getObj().getVariants())
                                && Objects.nonNull(product.getObj().getVariants().get(0).getAttributes())
                                && org.apache.commons.lang3.StringUtils.isNotEmpty(product.getObj().getVariants().get(0).getAttributes().getSubCategory())
                                && product.getObj().getVariants().get(0).getAttributes().getSubCategory().equalsIgnoreCase(Constants.LOCALS))) {
            result = true;
        }
        log.debug("isProductsHavingSubCategoryLocals:{} method end result = ", result);
        return result;
    }

    public static String getModifiedSalesChannel(List<String> salesChannel, List<String> offerActionType) {

        String modifiedSalesChannel = salesChannel.get(0);
        if (null != offerActionType && (offerActionType.contains(Constants.ACQUISITION_ACTION_TYPE) || offerActionType.contains(Constants.CLOSING_ACTION_TYPE) || offerActionType.contains(Constants.SOS_ACTION_TYPE))) {
            modifiedSalesChannel = salesChannel.get(0) + "Sales";
        } else {
            modifiedSalesChannel = salesChannel.get(0) + "Services";
        }
        return modifiedSalesChannel;
    }

    public void processOffers(CTOfferResponse ctOfferResponse, String modifiedSalesChannel) {

        ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {
            if (Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts())) {
                ctOffer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull).forEach(associatedProduct -> {
                    if (Objects.nonNull(associatedProduct.getBundleProducts())) {
                        associatedProduct.getBundleProducts().stream().filter(Objects::nonNull).forEach(bundleProduct -> {
                            if (Objects.nonNull(bundleProduct.getProducts())) {
                                bundleProduct.getProducts().stream().filter(Objects::nonNull).forEach(product -> {
                                    if (Objects.nonNull(product.getObj().getVariants())) {
                                        product.getObj().getVariants().stream().filter(Objects::nonNull).forEach(variant ->
                                        {
                                            if (Objects.nonNull(variant.getAttributes().getSubCategoryByKey())) {
                                                variant.getAttributes().getSubCategoryByKey().stream().filter(Objects::nonNull).forEach(subCategoryByKey ->
                                                {
                                                    if (subCategoryByKey.getKey().equalsIgnoreCase(modifiedSalesChannel)) {
                                                        variant.getAttributes().setSubCategory(subCategoryByKey.getValue());
                                                    }
                                                });
                                            }

                                            if (Optional.ofNullable(ctOffer.getAttributes().getDisplayTypeByKey()).isPresent()) {
                                                ctOffer.getAttributes().getDisplayTypeByKey().stream().filter(Objects::nonNull).forEach(displayType -> {
                                                    if (displayType.getDisplayTypeKey().equalsIgnoreCase(modifiedSalesChannel)) {
                                                        variant.getAttributes().setDisplayType(displayType.getDisplayTypeValue());
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }

        });
    }

    public void processProducts(CTProductResponse ctProductResponse, String modifiedSalesChannel) {

        ctProductResponse.getProducts().stream().filter(Objects::nonNull).forEach(product -> {
            if (Objects.nonNull(product.getVariants())) {
                product.getVariants().stream().filter(Objects::nonNull).forEach(variant -> {
                    if (Objects.nonNull(variant.getAttributes().getSubCategoryByKey())) {
                        variant.getAttributes().getSubCategoryByKey().stream().forEach(subCategoryByKey -> {
                            if (subCategoryByKey.getKey().equalsIgnoreCase(modifiedSalesChannel)) {
                                variant.getAttributes().setSubCategory(subCategoryByKey.getValue());
                            }
                        });
                    }
                });
            }
        });
    }

    public boolean isCustomerTypeReconnect(CTOffer offer) {
        return Optional.ofNullable(offer)
                .map(CTOffer::getAttributes)
                .map(OfferAttributes::getCustomerTypes)
                .filter(customerType -> !customerType.isEmpty())
                .filter(customerType -> customerType.stream().anyMatch(type -> type.equalsIgnoreCase(Constants.RECONNECT)))
                .isPresent();
    }

    public boolean isCustomerTypeReconnectGreaterThanEligibleMonths(CTOffer offer) {
        return Optional.ofNullable(offer)
                .map(CTOffer::getAttributes)
                .map(OfferAttributes::getCustomerTypes)
                .filter(customerType -> !customerType.isEmpty())
                .filter(customerType -> customerType.stream().anyMatch(type -> type.equalsIgnoreCase(Constants.RECONNECT_RETURN)))
                .isPresent();
    }

    // acquisition flow with reconnect > eligible months
    public boolean isCustomerTypeReconnectGreaterThanEligibleMonthsOffers(OfferRequestWrapper offerRequestWrapper, CTOffer offer) {
        log.debug("isCustomerTypeReconnectGreaterThanEligibleMonthsOffers start");
        boolean result = false;
        if (null == offer.getAttributes().getCustomerTypes()
                || isCustomerTypeReconnectGreaterThanEligibleMonths(offer)) {
            // return offers if ReconnectSubscriberType is empty
            if (Objects.isNull(offer.getAttributes().getReconnectSubscriberType())) {
                result = true;
            } // return offers if ReconnectSubscriberType matches with
            // existingAccountSubscriberType with offer & request and
            // offer CustomerSubtypes is empty
            else if (isValidExistingAccountSubscriberType(offerRequestWrapper, offer)
                    && Objects.isNull(offerRequestWrapper.getOfferRequest().getCustomerSubType())
                    && Objects.isNull(offer.getAttributes().getCustomerSubtypes())) {
                result = true;
            } // return offers if ReconnectSubscriberType matches with
            // existingAccountSubscriberType and
            // CustomerSubtypes matches with customerSubType in offer & request
            else if (isValidExistingAccountSubscriberType(offerRequestWrapper, offer)
                    && isValidCustomerSubtypes(offerRequestWrapper, offer)) {
                result = true;
            }
        }
        log.debug("isCustomerTypeReconnectGreaterThanEligibleMonthsOffers end");
        return result;
    }

    public void filterOffersBasedOnCustomerSubType(CTOfferResponse ctOfferResponse,
                                                   String requestCustomerSubType) {
        Optional.ofNullable(ctOfferResponse).ifPresent(response -> {
            Optional.ofNullable(response.getOffers())
                    .filter(offers -> !offers.isEmpty())
                    .ifPresent(offers -> {
                        List<CTOffer> filteredOffers = offers.stream()
                                .filter(Objects::nonNull)
                                .filter(offer -> {
                                    List<String> responseCustomerSubTypes = offer.getAttributes().getCustomerSubtypes();
                                    return (responseCustomerSubTypes != null && requestCustomerSubType != null && responseCustomerSubTypes.stream().anyMatch(subType -> subType.equalsIgnoreCase(requestCustomerSubType)))
                                            || (responseCustomerSubTypes == null || responseCustomerSubTypes.isEmpty());
                                })
                                .collect(Collectors.toList());
                        response.setOffers(filteredOffers);
                        response.setCount(filteredOffers.size());
                        response.setTotal(filteredOffers.size());
                    });
        });
    }

    public static boolean isClosingAndCredit(OfferRequestWrapper offerRequestWrapper) {
        return (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferActionType()).isPresent()
                && offerRequestWrapper.getOfferRequest().getOfferActionType().contains(Constants.CLOSING_ACTION_TYPE)
                && Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferProductType()).isPresent()
                && offerRequestWrapper.getOfferRequest().getOfferProductType().contains(Constants.CREDIT));
    }

    public void filterOfferBasedOnZipOrDMA(List<CTOffer> finalOfferList, OfferRequestWrapper offerRequestWrapper) {
        CustomerEligibility customerEligibility = offerRequestWrapper.getOfferRequest().getCustomerEligibility();
        List<String> defaultZipCode = redisCacheHelper.getValues(Constants.DEFAULT_ZIPCODE, Constants.OTT);
        List<String> defaultDma = redisCacheHelper.getValues(Constants.DEFAULT_DMA, Constants.OTT);
        if (Objects.isNull(customerEligibility) || (Objects.nonNull(customerEligibility) && CollectionUtils.isEmpty(customerEligibility.getZipCode()))) {
            if (Objects.isNull(customerEligibility)) {
                customerEligibility = new CustomerEligibility();
            }
            customerEligibility.setZipCode(defaultZipCode);
            customerEligibility.setDma(defaultDma);
            offerRequestWrapper.getOfferRequest().setCustomerEligibility(customerEligibility);
            filterOfferBasedOnZipOrDMA(finalOfferList, offerRequestWrapper.getOfferRequest());
        } else if (Objects.nonNull(customerEligibility) && CollectionUtils.isNotEmpty(customerEligibility.getZipCode()) && CollectionUtils.isEmpty(customerEligibility.getFipsCode())) {
            customerEligibility.setDma(defaultDma);
            offerRequestWrapper.getOfferRequest().setCustomerEligibility(customerEligibility);
            filterOfferBasedOnZipOrDMA(finalOfferList, offerRequestWrapper.getOfferRequest());
        } else if (Objects.nonNull(customerEligibility) && CollectionUtils.isNotEmpty(customerEligibility.getZipCode()) && CollectionUtils.isNotEmpty(customerEligibility.getFipsCode())) {
            List<String> dmaValueList = dmaLookUpService.getDMAValue(customerEligibility.getZipCode().get(0), customerEligibility.getFipsCode().get(0));
            customerEligibility.setDma(dmaValueList);
            offerRequestWrapper.getOfferRequest().setCustomerEligibility(customerEligibility);
            filterOfferBasedOnZipOrDMA(finalOfferList, offerRequestWrapper.getOfferRequest());
        }
    }

    public void filterOfferBasedOnZipOrDMA(List<CTOffer> ctOffers, OfferRequest offerRequest) {
        List<CTOffer> removeOfferList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(ctOffers)) {
            ctOffers.forEach(ctOffer -> {
                if (Objects.nonNull(ctOffer.getAttributes().getEligibility()) && CollectionUtils.isNotEmpty(ctOffer.getAttributes().getEligibility().getConstraints())) {
                    boolean shouldRemove = ctOffer.getAttributes().getEligibility().getConstraints().stream().anyMatch(constraint -> {
                        boolean zipNotMatch = CollectionUtils.isNotEmpty(constraint.getZip()) &&
                                Objects.nonNull(offerRequest.getCustomerEligibility()) &&
                                CollectionUtils.isNotEmpty(offerRequest.getCustomerEligibility().getZipCode()) &&
                                offerRequest.getCustomerEligibility().getZipCode().stream().noneMatch(constraint.getZip()::contains);
                        boolean dmaNotMatch = CollectionUtils.isNotEmpty(constraint.getDma()) &&
                                Objects.nonNull(offerRequest.getCustomerEligibility()) &&
                                CollectionUtils.isNotEmpty(offerRequest.getCustomerEligibility().getDma()) &&
                                offerRequest.getCustomerEligibility().getDma().stream().noneMatch(constraint.getDma()::contains);
                        boolean invalidDMA = CollectionUtils.isNotEmpty(constraint.getDma()) &&
                                (Objects.isNull(offerRequest.getCustomerEligibility()) || CollectionUtils.isEmpty(offerRequest.getCustomerEligibility().getDma()));
                        boolean invalidZip = CollectionUtils.isNotEmpty(constraint.getZip()) &&
                                (Objects.isNull(offerRequest.getCustomerEligibility()) || CollectionUtils.isEmpty(offerRequest.getCustomerEligibility().getZipCode()));
                        return zipNotMatch || dmaNotMatch || invalidDMA || invalidZip;
                    });
                    if (shouldRemove) {
                        removeOfferList.add(ctOffer);
                    }
                }
            });
        }
        // Remove the offers that should be removed
        ctOffers.removeAll(removeOfferList);
    }

    public void filterAutoRenewMessage(List<CTOffer> ctOffers, OfferRequestWrapper offerRequestWrapper) {
        if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferProductFamily()).isPresent()
                && offerRequestWrapper.getOfferRequest().getOfferProductFamily().stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))
                && offerRequestWrapper.isEmployeeAccount() && CollectionUtils.isNotEmpty(ctOffers)) {
            ctOffers.stream().filter(Objects::nonNull).forEach(offer -> {
                if (offer.getAttributes() != null
                        && CollectionUtils.isNotEmpty(offer.getAttributes().getBenefits())) {
                    offer.getAttributes().getBenefits().stream().filter(Objects::nonNull).forEach(benefitType -> {
                        if (benefitType.getBenefitType() != null && benefitType.getBenefitType().equalsIgnoreCase(Constants.FREE_PROMO)) {
                            if (offer.getAttributes() != null
                                    && CollectionUtils.isNotEmpty(offer.getAttributes().getAssociatedProducts())) {
                                offer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull).forEach(associatedProduct -> {
                                    if (CollectionUtils.isNotEmpty(associatedProduct.getBundleProducts())) {
                                        associatedProduct.getBundleProducts().stream().filter(Objects::nonNull).forEach(bundleProduct -> {
                                            if (CollectionUtils.isNotEmpty(bundleProduct.getProducts())) {
                                                bundleProduct.getProducts().stream().filter(Objects::nonNull).forEach(product -> {
                                                    if (product.getObj() != null
                                                            && CollectionUtils.isNotEmpty(product.getObj().getVariants())) {
                                                        for (Variant variant : product.getObj().getVariants()) {
                                                            if (variant.getAttributes() != null) {
                                                                variant.getAttributes().setAutoRenewMessages(null);
                                                            }
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
                }
            });
        }

    }

    public void filterOffersBasedOnCustomerType(CTOfferResponse offerResponse, CTOfferRequest ctOfferRequest) {
        boolean isAcquisitionAndNotReconnectFlag = Optional.ofNullable(ctOfferRequest)
                .map(this::isAcquisitionAndNotReconnectFlow)
                .orElse(false);
        boolean isReconnectAndLessThanEligibleMonthsFlag = Optional.ofNullable(ctOfferRequest)
                .map(this::isEligibleToReconnectLessThanEligibleMonths)
                .orElse(false);
        boolean isReconnectAndGreaterThanEligibleMonthsFlag = Optional.ofNullable(ctOfferRequest)
                .map(this::isEligibleToReconnectGreaterThanEligibleMonths)
                .orElse(false);
        if (isAcquisitionAndNotReconnectFlag) {
            log.info("Acquisition and not reconnect for " + ctOfferRequest.getOfferProductType().toString());
            filterOffersBasedOnCustomerTypeAcquisition(offerResponse);
        } else if (isReconnectAndGreaterThanEligibleMonthsFlag) {
            log.info("Acquisition and reconnect > eligible months for " + ctOfferRequest.getOfferProductType().toString());
            filterOffersBasedOnCustomerTypeReconnectGreaterThanEligibleMonths(offerResponse, ctOfferRequest);
        } else if (isReconnectAndLessThanEligibleMonthsFlag) {
            log.info("Acquisition and reconnect < eligible months for " + ctOfferRequest.getOfferProductType().toString());
            filterOffersBasedOnCustomerTypeReconnect(offerResponse, ctOfferRequest);
        }
    }

    public boolean isAcquisitionAndNotReconnectFlow(CTOfferRequest ctOfferRequest) {
        log.debug("inside isAcquisitionAndNotReconnectFlow()");
        if (ctOfferRequest == null) {
            return false;
        }
        List<String> offerActionType = Optional.ofNullable(ctOfferRequest.getOfferActionType()).orElse(Collections.emptyList());
        Boolean isReconnectCustomer = Optional.ofNullable(ctOfferRequest.isReconnectCustomer()).orElse(false);
        return (offerActionType.contains(Constants.ACQUISITION) || offerActionType.contains(Constants.UPSELL_ACTION_TYPE))
                && !isReconnectCustomer;
    }

    public boolean isEligibleToReconnectLessThanEligibleMonths(CTOfferRequest ctOfferRequest) {
        log.debug("inside isEligibleToReconnectLessThanEligibleMonths()");
        return isEligibleForReconnectFlow(ctOfferRequest, true);
    }

    public boolean isEligibleToReconnectGreaterThanEligibleMonths(CTOfferRequest ctOfferRequest) {
        log.debug("inside isEligibleToReconnectGreaterThanEligibleMonths()");
        return isEligibleForReconnectFlow(ctOfferRequest, false);
    }

    private boolean isEligibleForReconnectFlow(CTOfferRequest ctOfferRequest, boolean isLessThanEligibleMonths) {
        log.debug("isEligibleForReconnectFlow() start");
        if (ctOfferRequest == null) {
            return false;
        }
        List<String> offerActionType = Optional.ofNullable(ctOfferRequest.getOfferActionType()).orElse(Collections.emptyList());
        Boolean isReconnectCustomer = Optional.ofNullable(ctOfferRequest.isReconnectCustomer()).orElse(false);
        if (!isReconnectCustomer || offerActionType.isEmpty() ||
                !(offerActionType.contains(Constants.ACQUISITION) || offerActionType.contains(Constants.UPSELL_ACTION_TYPE))) {
            return false;
        }
        if (Optional.ofNullable(ctOfferRequest.getServiceEndDate()).isEmpty()) {
            return false;
        }
        List<String> offerCodes = Optional.ofNullable(ctOfferRequest.getOfferCodes()).orElse(Collections.emptyList());
        boolean isWithinEligibilityWindow = isDateWithinEligibilityWindow(ctOfferRequest.getServiceEndDate(),
                ctOfferRequest.getSalesChannel(), ctOfferRequest.getOfferProductType(), offerCodes);
        boolean eligible = isLessThanEligibleMonths ? isWithinEligibilityWindow : !isWithinEligibilityWindow;
        log.debug("isEligibleForReconnectFlow() end");
        return eligible;
    }

    public void filterOffersBasedOnCustomerTypeAcquisition(CTOfferResponse ctOfferResponse) {
        log.debug("filterOffersBasedOnCustomerTypeAcquisition() start");
        if ((null != ctOfferResponse) && !ctOfferResponse.getOffers().isEmpty()) {
            List<CTOffer> filteredOffers = new ArrayList<>();
            ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(offer -> {
                boolean isValidOffer = isCustomerTypeAcquisitionOffers(offer);
                if (isValidOffer) {
                    filteredOffers.add(offer);
                }
            });
            ctOfferResponse.setOffers(filteredOffers);
            ctOfferResponse.setCount(filteredOffers.size());
            ctOfferResponse.setTotal(filteredOffers.size());
        }
        log.debug("filterOffersBasedOnCustomerTypeAcquisition() end");
    }

    public void filterOffersBasedOnCustomerTypeReconnect(CTOfferResponse ctOfferResponse,
                                                         CTOfferRequest ctOfferRequest) {
        log.debug("filterOffersBasedOnCustomerTypeReconnect() start");
        if ((null != ctOfferResponse) && !ctOfferResponse.getOffers().isEmpty()) {
            List<CTOffer> filteredOffers = new ArrayList<>();
            ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(offer -> {
                boolean isValidOffer = isCustomerTypeReconnectLessThanEligibleMonthsOffers(ctOfferRequest, offer);
                if (isValidOffer) {
                    filteredOffers.add(offer);
                }
            });
            ctOfferResponse.setOffers(filteredOffers);
            ctOfferResponse.setCount(filteredOffers.size());
            ctOfferResponse.setTotal(filteredOffers.size());
        }
        log.debug("filterOffersBasedOnCustomerTypeReconnect() end");
    }

    public void filterOffersBasedOnCustomerTypeReconnectGreaterThanEligibleMonths(CTOfferResponse ctOfferResponse,
                                                                                  CTOfferRequest ctOfferRequest) {
        log.debug("filterOffersBasedOnCustomerTypeReconnectGreaterThanEligibleMonths() start");
        if ((null != ctOfferResponse) && ctOfferResponse.getOffers().size() > 0) {
            List<CTOffer> filteredOffers = new ArrayList<>();
            ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(offer -> {
                boolean isValidOffer = isCustomerTypeReconnectGreaterThanEligibleMonthsOffers(ctOfferRequest, offer);
                if (isValidOffer) {
                    filteredOffers.add(offer);
                }
            });
            ctOfferResponse.setOffers(filteredOffers);
            ctOfferResponse.setCount(filteredOffers.size());
            ctOfferResponse.setTotal(filteredOffers.size());
        }
        log.debug("filterOffersBasedOnCustomerTypeReconnectGreaterThanEligibleMonths() end");
    }

    public boolean isCustomerTypeAcquisitionOffers(CTOffer offer) {
        log.debug("isCustomerTypeAcquisitionOffers start");
        boolean result = false;
        if (null == offer.getAttributes().getCustomerTypes()
                || offer.getAttributes().getCustomerTypes().stream().anyMatch(type -> type.equalsIgnoreCase(Constants.ACQUISITION))) {
            result = true;
        }
        log.debug("isCustomerTypeAcquisitionOffers end");
        return result;
    }

    public boolean isCustomerTypeReconnectLessThanEligibleMonthsOffers(CTOfferRequest ctOfferRequest, CTOffer offer) {
        log.debug("isCustomerTypeReconnectLessThanEligibleMonthsOffers start");
        boolean result = false;
        if (null == offer.getAttributes().getCustomerTypes()
                || isCustomerTypeReconnect(offer)) {
            if (Objects.isNull(offer.getAttributes().getReconnectSubscriberType())) {
                result = true;
            } // return offers if ReconnectSubscriberType matches with
            else if (isValidExistingAccountSubscriberType(ctOfferRequest, offer)
                    && CollectionUtils.isEmpty(ctOfferRequest.getCustomerSubType())
                    && CollectionUtils.isEmpty(offer.getAttributes().getCustomerSubtypes())) {
                result = true;
            } // return offers if ReconnectSubscriberType matches with
            else if (isValidExistingAccountSubscriberType(ctOfferRequest, offer)
                    && isValidCustomerSubtypes(ctOfferRequest, offer)) {
                result = true;
            }
        }
        log.debug("isCustomerTypeReconnectLessThanEligibleMonthsOffers end");
        return result;
    }

    public boolean isCustomerTypeReconnectGreaterThanEligibleMonthsOffers(CTOfferRequest ctOfferRequest, CTOffer offer) {
        log.debug("isCustomerTypeReconnectGreaterThanEligibleMonthsOffers start");
        boolean result = false;
        if (null == offer.getAttributes().getCustomerTypes()
                || isCustomerTypeReconnectGreaterThanEligibleMonths(offer)) {
            if (Objects.isNull(offer.getAttributes().getReconnectSubscriberType())) {
                result = true;
            } // return offers if ReconnectSubscriberType matches with
            else if (isValidExistingAccountSubscriberType(ctOfferRequest, offer)
                    && CollectionUtils.isEmpty(ctOfferRequest.getCustomerSubType())
                    && CollectionUtils.isEmpty(offer.getAttributes().getCustomerSubtypes())) {
                result = true;
            } // return offers if ReconnectSubscriberType matches with
            else if (isValidExistingAccountSubscriberType(ctOfferRequest, offer)
                    && isValidCustomerSubtypes(ctOfferRequest, offer)) {
                result = true;
            }
        }
        log.debug("isCustomerTypeReconnectGreaterThanEligibleMonthsOffers end");
        return result;
    }

    public boolean isValidExistingAccountSubscriberType(CTOfferRequest ctOfferRequest, CTOffer offer) {
        return Objects.nonNull(offer.getAttributes().getReconnectSubscriberType())
                && !offer.getAttributes().getReconnectSubscriberType().isEmpty()
                && Objects.nonNull(ctOfferRequest.getExistingAccountSubscriberType())
                && offer.getAttributes().getReconnectSubscriberType().stream()
                .anyMatch(ctOfferRequest.getExistingAccountSubscriberType()::equalsIgnoreCase);
    }

    public boolean isValidCustomerSubtypes(CTOfferRequest ctOfferRequest, CTOffer offer) {
        return CollectionUtils.isNotEmpty(offer.getAttributes().getCustomerSubtypes())
                && CollectionUtils.isNotEmpty(ctOfferRequest.getCustomerSubType())
                && offer.getAttributes().getCustomerSubtypes().stream()
                .anyMatch(subtype -> ctOfferRequest.getCustomerSubType().stream()
                        .anyMatch(subtype::equalsIgnoreCase));
    }

    public List<CTOffer> filterOffersWithOfferProductTypeNotVideoPlan(CTOfferResponse ctOfferResponse) {
        log.debug("filterOffersWithOfferProductTypeNotVideoPlan() start");
        if (ctOfferResponse == null || ctOfferResponse.getOffers().isEmpty()) {
            return Collections.emptyList();
        }
        return ctOfferResponse.getOffers().stream()
                .filter(Objects::nonNull)
                .filter(offer -> Optional.ofNullable(offer.getAttributes())
                        .map(OfferAttributes::getOfferProductTypes)
                        .map(offerProductTypes -> offerProductTypes.stream()
                                .noneMatch(offerProductType -> Constants.VIDEO_PLAN.equalsIgnoreCase(offerProductType)))
                        .orElse(true))
                .collect(Collectors.toList());
    }

    public CTOfferResponse updateCTOfferResponseIndexToVideoPlan(CTOfferResponse ctOfferResponse) {
        log.debug("updateCTOfferResponseIndexToVideoPlan() start");
        if (ctOfferResponse == null || ctOfferResponse.getOffers().isEmpty()) {
            return new CTOfferResponse();
        }
        List<CTOffer> offers = ctOfferResponse.getOffers();
        List<CTOffer> updatedOffers = Stream.concat(
                offers.stream()
                        .filter(offer -> offer != null && Constants.VIDEO_PLAN.equalsIgnoreCase(offer.getAttributes().getOfferProductType())),
                offers.stream()
                        .filter(offer -> offer == null || !Constants.VIDEO_PLAN.equalsIgnoreCase(offer.getAttributes().getOfferProductType()))
        ).collect(Collectors.toList());
        ctOfferResponse.setOffers(updatedOffers);
        return ctOfferResponse;
    }

    public void filterOffersBasedOnSalesChannel(CTOfferResponse ctOfferResponse, OfferRequestWrapper offerRequestWrapper) {
        log.debug("filterOffersBasedOnSalesChannel() start");
        if (ctOfferResponse == null || ctOfferResponse.getOffers().isEmpty()) {
            return;
        }
        List<String> requestSalesChannels = Optional.ofNullable(offerRequestWrapper)
                .map(OfferRequestWrapper::getOfferRequest)
                .map(OfferRequest::getSalesChannel)
                .orElse(Collections.emptyList());
        List<CTOffer> filteredOffers = ctOfferResponse.getOffers().stream()
                .filter(Objects::nonNull)
                .filter(offer -> Optional.ofNullable(offer.getAttributes())
                        .map(OfferAttributes::getEligibility)
                        .map(Eligibility::getConstraints)
                        .flatMap(constraints -> constraints.stream().findFirst())
                        .map(Constraint::getSalesChannel)
                        .map(salesChannels -> salesChannels.stream()
                                .anyMatch(salesChannel -> requestSalesChannels.stream()
                                        .anyMatch(reqChannel -> reqChannel.equalsIgnoreCase(salesChannel))))
                        .orElse(false))
                .collect(Collectors.toList());
        ctOfferResponse.setOffers(filteredOffers);
        ctOfferResponse.setCount(filteredOffers.size());
        ctOfferResponse.setTotal(filteredOffers.size());
    }

    public void filterOffersBasedOnEligibleIapPartners(CTOfferResponse ctOfferResponse, OfferRequestWrapper offerRequestWrapper) {
        if (featureHelper.isEnabled(Constants.FEATURE_ELIGIBLE_IAP_PARTNERS_ENABLED)
                && offerRequestWrapper.getOfferRequest() != null && offerRequestWrapper.getOfferRequest().getContractIndicator() != null && !offerRequestWrapper.getOfferRequest().getContractIndicator().get(0).equalsIgnoreCase(Constants.EDSP_STRING)
                && offerRequestWrapper.getOfferRequest().getSalesChannel() != null && !offerRequestWrapper.getOfferRequest().getSalesChannel().get(0).equalsIgnoreCase(Constants.OEM_IAPFIRETV)) {
            String iapPartnerAccountType = getIapPartnerAccountType(offerRequestWrapper.getOfferRequest());
            Optional.ofNullable(ctOfferResponse).ifPresent(response -> {
                Optional.ofNullable(response.getOffers())
                        .filter(offers -> !offers.isEmpty())
                        .ifPresent(offers -> {
                            List<CTOffer> filteredOffers = offers.stream()
                                    .filter(Objects::nonNull)
                                    .filter(offer -> {
                                        if (iapPartnerAccountType != null && CollectionUtils.isNotEmpty(offer.getAttributes().getEligibleIAPPartners())) {
                                            return offer.getAttributes().getEligibleIAPPartners().stream().anyMatch(partner -> partner.equalsIgnoreCase(iapPartnerAccountType));
                                        } else if (iapPartnerAccountType == null && CollectionUtils.isNotEmpty(offer.getAttributes().getEligibleIAPPartners())) {
                                            return false;
                                        } else {
                                            return true;
                                        }
                                    })
                                    .collect(Collectors.toList());
                            response.setOffers(filteredOffers);
                            response.setCount(filteredOffers.size());
                            response.setTotal(filteredOffers.size());
                        });
            });
        }
    }

    public void filterOfferBasedOnCartContextWithConflictingOffers(List<CTOffer> finalOfferList, OfferRequestWrapper offerRequestWrapper) {
        if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCartContext()).isPresent()) {
            List<String> productTypesNotToCheck = new ArrayList<>(Arrays.asList(Constants.REWARD));
            if (featureHelper.isEnabled(Constants.FEATURE_FLAG_CONFLICTING_OFFER_NEW_LOGIC)) {
                productTypesNotToCheck.add(Constants.VIDEO_ADDON);
            }
            List<CTOffer> removeList = new ArrayList<>();
            List<String> conflictingList = new ArrayList<>();
            finalOfferList.forEach(offer -> {
                if (offer.getAttributes().isSpecialOffer() && !productTypesNotToCheck.contains(offer.getAttributes().getOfferProductType())) {
                    if (Optional.ofNullable(offer.getAttributes().getConflictingOffers()).isPresent() &&
                            !offer.getAttributes().getConflictingOffers().isEmpty()) {
                        offer.getAttributes().getConflictingOffers().forEach(conflictingOffer -> {
                            if (offerRequestWrapper.getFlow().equalsIgnoreCase("validateCart")) {
                                conflictingList.add(conflictingOffer.getId());
                            } else {
                                conflictingList.add(conflictingOffer.getKey());
                            }
                        });
                    }
                    offer.getAttributes().setConflictingOffers(null);
                } else if (CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getCartOffers()) && Constants.REWARD.equalsIgnoreCase(offer.getAttributes().getOfferProductType()) && CollectionUtils.isNotEmpty(offer.getAttributes().getConflictingOffers())) {
                    offer.getAttributes().getConflictingOffers().forEach(conflictingOffer -> {
                        if (offerRequestWrapper.getFlow().equalsIgnoreCase("validateCart")) {
                            conflictingList.add(conflictingOffer.getId());
                        } else {
                            conflictingList.add(conflictingOffer.getKey());
                        }
                    });
                    offer.getAttributes().setConflictingOffers(null);
                }
            });

            if (!conflictingList.isEmpty()) {
                finalOfferList.forEach(offer -> {
                    if (offerRequestWrapper.getFlow().equalsIgnoreCase("validateCart")) {
                        if (conflictingList.contains(offer.getId())) {
                            removeList.add(offer);
                        }
                    } else {
                        if (conflictingList.contains(offer.getCode())) {
                            removeList.add(offer);
                        }
                    }
                });
            }

            if (!removeList.isEmpty()) {
                finalOfferList.removeAll(removeList);
            }
        }
    }

    /**
     * Filters offers using non-stackable rules derived from cart offers in the request.
     * Processing steps:
     * Collect cart offer codes from offerRequestWrapper.getOfferRequest().getCartOffers()
     * Find matching offers in finalOfferList and read their
     * additionalEligibility entries of type NON_STACKABLE
     * Build a target set of conflicting offer codes and remove matching offers from
     * finalOfferList with exception of priority offers.
     * priority offers are not filtered out.
     * They are allowed to be stacked with the cart offer and other offers, even if they are listed as non-stackable.
     * This is to allow certain offers to be exempt from non-stackable restrictions, providing flexibility in offer combinations.
     *
     * @param finalOfferList mutable list of offers to be filtered in-place
     * @param offerRequestWrapper request wrapper containing cart context and flow metadata
     */
    public void filterOffersBasedOnNonStackableCartOffers(List<CTOffer> finalOfferList, OfferRequestWrapper offerRequestWrapper) {
        if (CollectionUtils.isEmpty(finalOfferList)
                || offerRequestWrapper == null
                || offerRequestWrapper.getOfferRequest() == null
                || CollectionUtils.isEmpty(offerRequestWrapper.getOfferRequest().getCartOffers())) {
            return;
        }

        Set<String> cartOfferCodes = offerRequestWrapper.getOfferRequest().getCartOffers().stream()
                .filter(Objects::nonNull)
                .map(cartOffer -> cartOffer.getOfferCode())
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());


        if (CollectionUtils.isEmpty(cartOfferCodes)) {
            return;
        }

        Set<String> nonStackableCodesToFilter = finalOfferList.stream()
                .filter(Objects::nonNull)
                .filter(offer -> offer.getAttributes() != null)
                .filter(offer -> StringUtils.hasText(offer.getCode())
                        && cartOfferCodes.contains(offer.getCode().trim().toUpperCase()))
                .map(offer -> offer.getAttributes().getAdditionalEligibility())
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .filter(additionalEligibility -> org.apache.commons.lang3.StringUtils
                        .equalsIgnoreCase(Constants.NON_STACKABLE, additionalEligibility.getEligibilityType()))
                .filter(additionalEligibility -> !StringUtils.hasText(additionalEligibility.getEligibleProductType())
                        || org.apache.commons.lang3.StringUtils.equalsIgnoreCase(Constants.VIDEO_ADDON, additionalEligibility.getEligibleProductType()))
                .flatMap(additionalEligibility -> {
                    List<String> offerIds = additionalEligibility.getOfferIds();
                    List<String> priorityOffers = additionalEligibility.getPriorityOffers();
                    if (CollectionUtils.isEmpty(offerIds)) {
                        return Stream.empty();
                    }
                    // If priority offers is null, filter all offers; if not null, filter only non-priority offers
                    if (CollectionUtils.isEmpty(priorityOffers)) {
                        return offerIds.stream()
                                .filter(StringUtils::hasText);
                    } else {
                        Set<String> normalizedPriorityOffers = priorityOffers.stream()
                                .filter(StringUtils::hasText)
                                .map(String::trim)
                                .map(String::toUpperCase)
                                .collect(Collectors.toSet());
                        return offerIds.stream()
                                .filter(StringUtils::hasText)
                                .filter(offerId -> !normalizedPriorityOffers.contains(offerId.trim().toUpperCase()));
                    }
                })
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
        // For validateCart flow, also remove offers that are in the cart but conflict with non-stackable rules.
        boolean isValidateCartFlow = "validateCart".equalsIgnoreCase(offerRequestWrapper.getFlow());
        if (CollectionUtils.isNotEmpty(nonStackableCodesToFilter)) {
            finalOfferList.removeIf(offer -> offer != null
                    && StringUtils.hasText(offer.getCode())
                    && (isValidateCartFlow || !cartOfferCodes.contains(offer.getCode().trim().toUpperCase()))
                    && nonStackableCodesToFilter.contains(offer.getCode().trim().toUpperCase()));
        }
    }

    /**
     * Utility method to execute a callable with the provided MDC context.
     *
     * @param mdcContext The MDC context to set during execution.
     * @param callable   The callable to execute.
     * @param <T>        The return type of the callable.
     * @return The result of the callable execution.
     * @throws Exception If the callable throws an exception.
     */
    public static <T> T executeWithMdcContext(Map<String, String> mdcContext, Callable<T> callable) throws Exception {
        if (mdcContext != null) {
            MDC.setContextMap(mdcContext); // Restore MDC context
        }
        try {
            return callable.call();
        } finally {
            MDC.clear(); // Clear MDC context after execution
        }
    }

    /**
     * Gets iap remove products from included products from config.
     *
     * @return the iap remove products from included products from config
     */
    public Map<String, List<String>> getIAPRemoveProductsFromIncludedProductsFromConfig() {
        Map<String, List<String>> productsToRemoveMap = new HashMap<>();
        List<String> iapRemoveProductsFromIncludedProductsFromConfig = Optional.ofNullable(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_REMOVE_PRODUCT_FROM_INCLUDED_PRODUCTS, Constants.OTT))
                .filter(values -> !values.isEmpty() && values.get(0) != null)
                .map(values -> values.get(0).split("\\s*;\\s*"))
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
        if (CollectionUtils.isNotEmpty(iapRemoveProductsFromIncludedProductsFromConfig)) {
            for (String includedProductConfig : iapRemoveProductsFromIncludedProductsFromConfig) {
                String[] includedProductConfigArray = includedProductConfig.split("\\s*:\\s*");
                if (includedProductConfigArray.length == 2) {
                    productsToRemoveMap.put(includedProductConfigArray[0], Arrays.asList(includedProductConfigArray[1].split("\\s*,\\s*")));
                }
            }
        }
        return productsToRemoveMap;
    }

    public List<String> filterVideoDeviceOffer(List<CTOffer> offerList) {
        List<String> allOfferCodes = new ArrayList<>();
        List<String> allDeviceOffers = new ArrayList<>();

        offerList.stream()
                .filter(Objects::nonNull)
                .forEach(offersMaps -> {
                    if (offersMaps.getAttributes() != null && CollectionUtils.isNotEmpty(offersMaps.getAttributes().getOfferChoiceGroup())) {
                        offersMaps.getAttributes().getOfferChoiceGroup().forEach(offerChoiceGroup -> {
                            if (CollectionUtils.isNotEmpty(offerChoiceGroup.getOfferCodes())) {
                                allOfferCodes.addAll(offerChoiceGroup.getOfferCodes());
                            }
                        });
                    }
                });

        if (!allOfferCodes.isEmpty()) {
            List<String> distinctOfferCodes = allOfferCodes.stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            CTOfferRequest ctOfferRequest = new CTOfferRequest();
            ctOfferRequest.setOfferCodes(distinctOfferCodes);

            CTOfferResponse offerResponse = epochClient.getOffersFromCache(ctOfferRequest, List.of("offerProductType"));
            if (offerResponse != null && CollectionUtils.isNotEmpty(offerResponse.getOffers())) {
                offerResponse.getOffers().stream()
                        .filter(Objects::nonNull)
                        .forEach(offer -> {
                            if (offer.getAttributes() != null && Constants.VIDEO_DEVICE.equalsIgnoreCase(offer.getAttributes().getOfferProductType())) {
                                allDeviceOffers.add(offer.getCode());
                            }
                        });
            }
        }

        return allDeviceOffers;
    }

    public boolean isBYODFlow(OfferRequestWrapper offerRequestWrapper) {
        return Optional.ofNullable(offerRequestWrapper)
                .map(OfferRequestWrapper::getOfferRequest)
                .map(OfferRequest::getCustomerEligibility)
                .map(CustomerEligibility::isBYODFlow)
                .orElse(false);
    }

    public void updateBenefitsBasedOnVideoDeviceOfferCodes(CTOffer offer, List<String> filteredGraphQLVideoDeviceOfferCodes) {
        List<OfferChoiceGroup> offerChoiceGroupsToRemove = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(offer.getAttributes().getOfferChoiceGroup())) {
            offer.getAttributes().getOfferChoiceGroup().forEach(offerChoiceGroup -> {
                if (CollectionUtils.isNotEmpty(offerChoiceGroup.getOfferCodes())) {
                    if (offerChoiceGroup.getOfferCodes().stream().filter(Objects::nonNull)
                            .anyMatch(s -> filteredGraphQLVideoDeviceOfferCodes.stream()
                                    .anyMatch(code -> code.equalsIgnoreCase(s)))) {
                        offer.getAttributes().setBenefits(
                                setBenefitsBasedOnOfferCGPromosOnDeSelection(offer, offer.getAttributes().getBenefits()));
                        offerChoiceGroupsToRemove.add(offerChoiceGroup);
                    }
                }
            });
        }
        if (!offerChoiceGroupsToRemove.isEmpty()) {
            offer.getAttributes().getOfferChoiceGroup().removeAll(offerChoiceGroupsToRemove);
        }
    }

    private void removedOffersBasedOnAdditionalEligibilityProductType(List<CTOffer> offerList) {
        List<CTOffer> removeVideoDeviceOffers = new ArrayList<>();
        offerList.stream()
                .filter(Objects::nonNull)
                .forEach(offer -> {
                    if (offer.getAttributes() != null && CollectionUtils.isNotEmpty(offer.getAttributes().getAdditionalEligibility())) {
                        List<AdditionalEligibility> additionalEligibilityForVideoDevice = offer.getAttributes()
                                .getAdditionalEligibility()
                                .stream()
                                .filter(Objects::nonNull)
                                .filter(additionalEligibility -> Constants.VIDEO_DEVICE.equalsIgnoreCase(additionalEligibility.getEligibleProductType()))
                                .collect(Collectors.toList());

                        List<AdditionalEligibility> additionalEligibilityNotVideoDevice = offer.getAttributes()
                                .getAdditionalEligibility()
                                .stream()
                                .filter(Objects::nonNull)
                                .filter(additionalEligibility -> !Constants.VIDEO_DEVICE.equalsIgnoreCase(additionalEligibility.getEligibleProductType()))
                                .collect(Collectors.toList());

                        if (CollectionUtils.isNotEmpty(additionalEligibilityForVideoDevice) && CollectionUtils.isEmpty(additionalEligibilityNotVideoDevice)) {
                            removeVideoDeviceOffers.add(offer);
                        } else if (CollectionUtils.isNotEmpty(additionalEligibilityNotVideoDevice)) {
                            offer.getAttributes().getAdditionalEligibility().removeAll(additionalEligibilityForVideoDevice);
                        }
                    }
                });

        if (CollectionUtils.isNotEmpty(removeVideoDeviceOffers)) {
            offerList.removeAll(removeVideoDeviceOffers);
        }
    }

    public void filterOffersBasedOnEligibleIapPartners(List<CTOffer> getOffer, OfferRequestWrapper offerRequestWrapper) {
        if (featureHelper.isEnabled(Constants.FEATURE_ELIGIBLE_IAP_PARTNERS_ENABLED)
                && offerRequestWrapper.getOfferRequest() != null && offerRequestWrapper.getOfferRequest().getContractIndicator() != null && !offerRequestWrapper.getOfferRequest().getContractIndicator().get(0).equalsIgnoreCase(Constants.EDSP_STRING)
                && offerRequestWrapper.getOfferRequest().getSalesChannel() != null && !offerRequestWrapper.getOfferRequest().getSalesChannel().get(0).equalsIgnoreCase(Constants.OEM_IAPFIRETV)) {
            String iapPartnerAccountType = getIapPartnerAccountType(offerRequestWrapper.getOfferRequest());
            Optional.ofNullable(getOffer)
                    .filter(offers -> !offers.isEmpty())
                    .ifPresent(offers -> {
                        List<CTOffer> filteredOffers = offers.stream()
                                .filter(Objects::nonNull)
                                .filter(offer -> {
                                    if (iapPartnerAccountType != null && CollectionUtils.isNotEmpty(offer.getAttributes().getEligibleIAPPartners())) {
                                        return
                                                offer.getAttributes().getEligibleIAPPartners().stream().anyMatch(partner -> partner.equalsIgnoreCase(iapPartnerAccountType));
                                    } else if (iapPartnerAccountType == null && CollectionUtils.isNotEmpty(offer.getAttributes().getEligibleIAPPartners())) {
                                        return false;
                                    } else {
                                        return true;
                                    }

                                })
                                .collect(Collectors.toList());
                        getOffer.clear();
                        getOffer.addAll(filteredOffers);

                    });

        }
    }

    public List<String> getConfigList(String key) {
        if (CollectionUtils.isNotEmpty(redisCacheHelper.getValues(key, Constants.OTT))) {
            return new ArrayList<>(Arrays.asList(
                    Optional.ofNullable(redisCacheHelper.getValues(key, Constants.OTT).get(0))
                            .orElse("")
                            .split("\\s*,\\s*")));
        }
        return null;
    }

    public void filterMDUIncludedProducts(CTProductResponse baseProductsResponse) {
        List<String> mduIncludedProductExclusions = getConfigList(Constants.MDU_INCLUDED_PRODUCT_EXCLUSIONS);

        Optional.ofNullable(baseProductsResponse)
                .map(CTProductResponse::getProducts)
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .filter(this::isVideoPlanProduct)
                .filter(this::isMDUBusinessSegment)
                .forEach(product -> processIncludedProducts(product, mduIncludedProductExclusions));
    }

    private boolean isVideoPlanProduct(ProductObj product) {
        return Optional.ofNullable(product.getProductType())
                .map(type -> Constants.VIDEO_PLAN.equalsIgnoreCase(type.getKey()))
                .orElse(false);
    }

    private boolean isMDUBusinessSegment(ProductObj product) {
        return Optional.ofNullable(product.getVariants())
                .filter(CollectionUtils::isNotEmpty)
                .map(variants -> Optional.ofNullable(variants.get(0).getAttributes())
                        .map(attributes -> Optional.ofNullable(attributes.getBusinessSegment())
                                .filter(CollectionUtils::isNotEmpty)
                                .map(segment -> segment.stream().anyMatch(Constants.MDU::equalsIgnoreCase))
                                .orElse(false))
                        .orElse(false))
                .orElse(false);
    }

    private void processIncludedProducts(ProductObj product, List<String> mduIncludedProductExclusions) {
        Optional.ofNullable(product.getVariants().get(0).getAttributes())
                .map(Attributes::getIncludedProducts)
                .filter(CollectionUtils::isNotEmpty)
                .ifPresent(includedProducts -> includedProducts.forEach(includedProduct -> {
                    List<IncludedProduct> filteredProducts = filterIncludedProducts(includedProduct, mduIncludedProductExclusions);
                    updateIncludedProducts(includedProduct, filteredProducts);
                    removeEmptyIncludedProducts(product, includedProduct);
                }));
    }

    private List<IncludedProduct> filterIncludedProducts(IncludeProductWrapper includedProduct, List<String> exclusions) {
        return Optional.ofNullable(includedProduct.getProducts())
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .filter(productObj -> Optional.ofNullable(productObj.getKey())
                        .map(key -> !exclusions.contains(key))
                        .orElse(true)) // Keep products with null keys
                .collect(Collectors.toList());
    }

    private void updateIncludedProducts(IncludeProductWrapper includedProduct, List<IncludedProduct> filteredProducts) {
        if (CollectionUtils.isNotEmpty(filteredProducts)) {
            includedProduct.setProducts(filteredProducts);
        } else {
            includedProduct.setProducts(null);
        }
    }

    private void removeEmptyIncludedProducts(ProductObj product, IncludeProductWrapper includedProduct) {
        if (CollectionUtils.isEmpty(includedProduct.getProducts())) {
            product.getVariants().get(0).getAttributes().setIncludedProducts(null);
        }
    }
    
    public Double getTwoDigitRoundOffValue(Double amount) {
        if (Objects.nonNull(amount)) {
            BigDecimal bd = new BigDecimal(Double.toString(amount));
            bd = bd.setScale(2, RoundingMode.HALF_UP);
            return bd.doubleValue();
        }
        return 0.0;
    }

    public Map<String, String> getAllMessagesMap(List<List<MessageEntry>> messageGroups, String salesChannel) {
        Map<String, String> allMessagesMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(messageGroups) && salesChannel != null) {
            for (var messageGroup : messageGroups) {
                var messagesMap = new HashMap<String, String>();
                for (var message : messageGroup) {
                    if (Constants.MESSAGES_BY_KEY_MESSAGE_TYPE.equals(message.getName())) {
                        var value = message.getValue();
                        if (value instanceof Map) {
                            var keyObj = ((Map<?, ?>) value).get(Constants.KEY);
                            if (keyObj != null) messagesMap.put(message.getName(), keyObj.toString());
                        }
                    }
                    if (Constants.MESSAGES_BY_KEY_ALL_MESSAGES.equals(message.getName())) {
                        for (var msgArr : (List<?>) message.getValue()) {
                            for (var entry : (List<?>) msgArr) {
                                var allMessage = MAPPER.convertValue(entry, MessageEntry.class);
                                var value = allMessage.getValue();
                                if (value instanceof Map) {
                                    var keyObj = ((Map<?, ?>) value).get(Constants.KEY);
                                    if (keyObj != null) messagesMap.put(allMessage.getName(), keyObj.toString());
                                } else {
                                    messagesMap.put(allMessage.getName(), value != null ? value.toString() : null);
                                }
                            }
                            allMessagesMap.put(
                                    messagesMap.get(Constants.MESSAGES_BY_KEY_MESSAGE_TYPE)
                                            + messagesMap.get(Constants.ALL_MESSAGES_SALES_CHANNEL)
                                            + messagesMap.get(Constants.ALL_MESSAGES_FLOW_TYPE)
                                            + messagesMap.get(Constants.KEY),
                                    messagesMap.get(Constants.VALUE)
                            );
                        }
                    }
                }
            }
        }
        return allMessagesMap;
    }

    public void updateDelayProvisioningMessages(String messageKey, Attributes variantAttributes,
                                                Map<String, String> globalMessagesMap, String salesChannel,
                                                String flowType) {
        final String shortMsgKey = messageKey + salesChannel + flowType + Constants.SHORT_MESSAGE;
        final String longMsgKey = messageKey + salesChannel + flowType + Constants.LONG_MESSAGE;
        final String shortMsgKeyDefault = messageKey + Constants.DEFAULT + flowType + Constants.SHORT_MESSAGE;
        final String longMsgKeyDefault = messageKey + Constants.DEFAULT + flowType + Constants.LONG_MESSAGE;

        final String shortMsg = globalMessagesMap.getOrDefault(shortMsgKey, globalMessagesMap.get(shortMsgKeyDefault));
        final String longMsg = globalMessagesMap.getOrDefault(longMsgKey, globalMessagesMap.get(longMsgKeyDefault));

        var delayProvisioningMsg = new DelayProvisioningMessagesByKey();
        delayProvisioningMsg.setShortMessage(shortMsg);
        delayProvisioningMsg.setLongMessage(longMsg);
        variantAttributes.setDelayProvisioningMessagesByKey(
                (shortMsg != null || longMsg != null) ? delayProvisioningMsg : null);
    }

    public void processDelayProvisioningProductVariantAttributes(Attributes attributes, String salesChannel, boolean isDelayProvisioningTillTimePresent, String flowType) {
        Map<String, String> globalMessagesMap = getAllMessagesMap(attributes.getGlobalMessagesByKey(), salesChannel);

        if (isDelayProvisioningTillTimePresent && org.apache.commons.collections4.CollectionUtils.isNotEmpty(attributes.getGlobalMessagesByKey())) {
            updateDelayProvisioningMessages(Constants.DELAY_PROVISIONING, attributes, globalMessagesMap, salesChannel, flowType);
            // in getProducts delayProvisioning & delayProvisioningReason is set to null as per requirement
            attributes.setDelayProvisioning(null);
            attributes.setDelayProvisioningReasons(null);
        } else {
            suppressDelayProvisioning(attributes);
        }
    }

    public void suppressDelayProvisioning(Attributes attributes) {
        attributes.setDelayProvisioning(null);
        attributes.setDelayProvisioningReasons(null);
        attributes.setDelayProvisioningMessagesByKey(null);
    }

    public String getProductsFlowType(ProductRequest productRequest) {
        if (productRequest.getCustomerContext() == null) {
            return Constants.SALES;
        }
        return Constants.SERVICES;
    }

    public Map<String, Boolean> getDelayProvisioningProductsFromRequest(ProductRequest productsRequest) {
        Map<String, Boolean> delayProvisioningProductsMap = new HashMap<>();
        if (productsRequest == null ||
                productsRequest.getCustomerContext() == null ||
                productsRequest.getCustomerContext().getOtt() == null ||
                CollectionUtils.isEmpty(productsRequest.getCustomerContext().getOtt().getProducts())) {
            return delayProvisioningProductsMap; // Return empty map if any required field is null or empty
        }
        productsRequest.getCustomerContext().getOtt().getProducts().stream()
                .filter(Objects::nonNull)
                .forEach(product -> delayProvisioningProductsMap.put(
                        product.getProductCode(),
                        product.getDelayProvisioningTillTime() != null
                ));
        return delayProvisioningProductsMap;
    }

    public void processDelayProvisioningProducts(ProductObj productObj, Map<String, Boolean> reqDelayProvisioningProductsMap,
                                                 String salesChannel, ProductRequest productsRequest, OffersUtils offersUtils) {
        boolean isDelayProvisioningTillTimePresent = reqDelayProvisioningProductsMap.getOrDefault(productObj.getCode(), false);
        Optional.ofNullable(productObj.getVariants())
                .filter(org.apache.commons.collections4.CollectionUtils::isNotEmpty)
                .ifPresent(variants -> variants.forEach(variant -> processDelayProvisioningProductVariant(variant, salesChannel, isDelayProvisioningTillTimePresent, productsRequest, offersUtils)));
    }

    public void processDelayProvisioningProductVariant(Variant variant, String salesChannel, boolean isDelayProvisioningTillTimePresent,
                                                       ProductRequest productsRequest, OffersUtils offersUtils) {
        if (variant.getAttributes() != null) {
            if (salesChannel != null) {
                offersUtils.processDelayProvisioningProductVariantAttributes(
                        variant.getAttributes(),
                        salesChannel,
                        isDelayProvisioningTillTimePresent,
                        offersUtils.getProductsFlowType(productsRequest)
                );
            } else {
                offersUtils.suppressDelayProvisioning(variant.getAttributes());
            }
        }
    }

    /**
     * Populates dependent offer objects for each CTOffer in the provided list.
     * This method iterates through each offer in the provided list, checks for additional eligibility criteria,
     * and if the criteria type is "dependentOffer", it creates a new DependentOffer object with the specified
     * attributes (product type, minimum count, maximum count, and offer IDs). These dependent offer objects
     * are then added to their respective CTOffer objects.
     *
     * @param offerList The list of CTOffer objects to be processed for dependent offers.
     */

    public void setDepenentOfferObject(List<CTOffer> offerList) {
        if (CollectionUtils.isNotEmpty(offerList)) {
            offerList.forEach(offer -> {
                List<DependentOffer> dependentOffers = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(offer.getAttributes().getAdditionalEligibility())) {
                    offer.getAttributes().getAdditionalEligibility().forEach(additionalEligibility -> {
                        if (Constants.DEPENDENT_OFFER.equalsIgnoreCase(additionalEligibility.getEligibilityType())) {
                            DependentOffer dependentOffer = new DependentOffer();
                            dependentOffer.setOfferProductType(additionalEligibility.getEligibleProductType());
                            dependentOffer.setMinCount(additionalEligibility.getMinCount());
                            dependentOffer.setMaxCount(additionalEligibility.getMaxCount());
                            dependentOffer.setOfferCode(additionalEligibility.getOfferIds());
                            dependentOffer.setProductCode(additionalEligibility.getProductIds());
                            dependentOffers.add(dependentOffer);
                        }
                    });
                }
                if (dependentOffers.size() > 0) {
                    offer.getAttributes().setDependentOffers(dependentOffers);
                }
            });
        }
    }
    /**
     * Filters and processes conflicting offers when allow-conflicting-offers are present.
     * <p>
     * This method performs the following operations:
     * <ol>
     *   <li>Collects all offer codes that are listed as conflicting offers across all offers in the list.</li>
     *   <li>For each offer, merges the {@code allowConflictingOffers} into the {@code conflictingOffers} list:
     *     <ul>
     *       <li>If both {@code allowConflictingOffers} and {@code conflictingOffers} are non-empty,
     *           the allow-conflicting offers are appended to the existing conflicting offers.</li>
     *       <li>If {@code allowConflictingOffers} is non-empty but {@code conflictingOffers} is empty,
     *           the allow-conflicting offers replace the conflicting offers entirely.</li>
     *     </ul>
     *   </li>
     *   <li>Clears the {@code allowConflictingOffers} field on each offer by setting it to {@code null}.</li>
     *   <li>Removes any offers from the final list whose offer code appears in the collected conflicting list,
     *       effectively filtering out offers that are flagged as conflicting by other offers.</li>
     * </ol>
     *
     * @param finalOfferList the list of {@link CTOffer} objects to process; may be {@code null} or empty
     * @return a filtered list of {@link CTOffer} objects with conflicting offers removed and
     *         allow-conflicting offers merged into conflicting offers; returns the original list
     *         if no conflicting offers are found
     */
    public List<CTOffer> filterConflictingOfferWhenAllowConflictingOffersPresent(List<CTOffer> finalOfferList, OfferRequestWrapper offerRequestWrapper) {
        Set<String> conflictingOfferSet = new HashSet<>();
        if (CollectionUtils.isNotEmpty(finalOfferList)) {
            finalOfferList.forEach(offer -> {
                if (CollectionUtils.isNotEmpty(offer.getAttributes().getConflictingOffers())) {
                    offer.getAttributes().getConflictingOffers().forEach(conflictingOffer -> conflictingOfferSet.add(conflictingOffer.getKey()));
                }
                if (CollectionUtils.isNotEmpty(offer.getAttributes().getAllowConflictingOffers()) && CollectionUtils.isNotEmpty(offer.getAttributes().getConflictingOffers())) {
                    offer.getAttributes().getConflictingOffers().addAll(offer.getAttributes().getAllowConflictingOffers());
                } else if (CollectionUtils.isNotEmpty(offer.getAttributes().getAllowConflictingOffers()) && CollectionUtils.isEmpty(offer.getAttributes().getConflictingOffers())) {
                    offer.getAttributes().setConflictingOffers(offer.getAttributes().getAllowConflictingOffers());
                }
                offer.getAttributes().setAllowConflictingOffers(null);
            });
        }
        if (!conflictingOfferSet.isEmpty() && (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCartContext()).isPresent() || !CollectionUtils.containsAny(Arrays.asList(Constants.ACQUISITION, Constants.UPSELL_ACTION_TYPE), offerRequestWrapper.getOfferRequest().getOfferActionType()))) {
            finalOfferList = finalOfferList.stream().filter(offer -> (!conflictingOfferSet.contains(offer.getCode()))).collect(Collectors.toList());
        }
        return finalOfferList;
    }
    public static boolean isEligibleIncludedProduct(IncludeProductWrapper includeProductWrapper, List<String> salesChannelsList, String iapPartnerAccountType) {
        if (Objects.isNull(includeProductWrapper.getConstraints())) {
            return true;
        }
        Constraints constraints = includeProductWrapper.getConstraints();
        boolean hasEligibleSalesChannels = CollectionUtils.isNotEmpty(constraints.getEligibleSalesChannels());
        boolean hasExcludedPartnersAndHasIAPAccountType = CollectionUtils.isNotEmpty(constraints.getExcludedPartners()) && org.apache.commons.lang3.StringUtils.isNotEmpty(iapPartnerAccountType);

        if (!hasEligibleSalesChannels && !hasExcludedPartnersAndHasIAPAccountType) {
            return true;
        }

        boolean salesChannelMatch = hasEligibleSalesChannels
                && CollectionUtils.isNotEmpty(salesChannelsList)
                && CollectionUtils.containsAny(constraints.getEligibleSalesChannels(), salesChannelsList);

        boolean partnerNotExcluded = hasExcludedPartnersAndHasIAPAccountType
                && !constraints.getExcludedPartners().contains(iapPartnerAccountType);

        if (hasEligibleSalesChannels && hasExcludedPartnersAndHasIAPAccountType) {
            return salesChannelMatch && partnerNotExcluded;
        }
        if (hasExcludedPartnersAndHasIAPAccountType) {
            return partnerNotExcluded;
        }
        return salesChannelMatch;
    }

    public static void filterIncludedProductsBasedOnSalesChannelOrExcludedPartner(CTProductResponse ctProductResponse, List<String> salesChannelsList, String iapPartnerAccountType) {
        if (ctProductResponse == null || CollectionUtils.isEmpty(ctProductResponse.getProducts())) {
            return;
        }
        ctProductResponse.getProducts().stream()
                .filter(productObj -> Objects.nonNull(productObj) && CollectionUtils.isNotEmpty(productObj.getVariants()))
                .flatMap(productObj -> productObj.getVariants().stream())
                .filter(variant -> Objects.nonNull(variant.getAttributes()) && CollectionUtils.isNotEmpty(variant.getAttributes().getIncludedProducts()))
                .forEach(variant -> filterAndSetIncludedProducts(variant, salesChannelsList, iapPartnerAccountType));
    }

    public static void filterAndSetIncludedProducts(Variant variant, List<String> salesChannelsList, String iapPartnerAccountType) {
        List<IncludeProductWrapper> filtered = variant.getAttributes().getIncludedProducts().stream()
                .filter(ipw -> isEligibleIncludedProduct(ipw, salesChannelsList, iapPartnerAccountType))
                .collect(Collectors.toList());
        if (filtered.size() > 1) {
            // If multiple IncludeProductWrappers are eligible, merge their products into a single wrapper
            List<IncludedProduct> allIncludedProducts = filtered.stream()
                    .filter(ipw -> CollectionUtils.isNotEmpty(ipw.getProducts()))
                    .flatMap(ipw -> ipw.getProducts().stream())
                    .distinct()
                    .collect(Collectors.toList());
            IncludeProductWrapper merged = new IncludeProductWrapper();
            merged.setProducts(allIncludedProducts);
            filtered = Stream.of(merged).collect(Collectors.toList());
        }
        variant.getAttributes().setIncludedProducts(CollectionUtils.isNotEmpty(filtered) ? filtered : null);
    }

    public static void filterConflictingProductsBasedOnSalesChannelOrExcludedPartner(CTProductResponse ctProductResponse, List<String> salesChannelsList, String iapPartnerAccountType) {
        if (ctProductResponse == null || CollectionUtils.isEmpty(ctProductResponse.getProducts())) {
            return;
        }
        ctProductResponse.getProducts().stream()
                .filter(productObj -> Objects.nonNull(productObj) && CollectionUtils.isNotEmpty(productObj.getVariants()))
                .flatMap(productObj -> productObj.getVariants().stream())
                .filter(variant -> Objects.nonNull(variant.getAttributes()) && CollectionUtils.isNotEmpty(variant.getAttributes().getConflictingProductsReference()))
                .forEach(variant -> {

                    filterAndSetConflictingProducts(variant,salesChannelsList,iapPartnerAccountType);
                });
    }

    public static void filterAndSetConflictingProducts(Variant variant, List<String> salesChannelsList, String iapPartnerAccountType) {
        List<IncludeProductWrapper> filtered = variant.getAttributes().getConflictingProductsReference().stream()
                .filter(ipw -> OffersUtils.isEligibleIncludedProduct(ipw, salesChannelsList, iapPartnerAccountType))
                .collect(Collectors.toList());
        List<GenericTypeIdBase> flattenedConflictingProducts = filtered
                .stream()
                .filter(wrapper -> wrapper != null && wrapper.getProducts() != null && !wrapper.getProducts().isEmpty())
                .flatMap(wrapper -> wrapper.getProducts().stream()
                        .map(product -> {
                            GenericTypeIdBase genericTypeIdBase = new GenericTypeIdBase();
                            genericTypeIdBase.setId(product.getId());
                            genericTypeIdBase.setTypeId(product.getTypeId());
                            genericTypeIdBase.setKey(product.getKey());
                            return genericTypeIdBase;
                        }))
                .collect(Collectors.toList());
        variant.getAttributes().setConflictingProducts(flattenedConflictingProducts);
        variant.getAttributes().setConflictingProductsReference(filtered);
    }
}
