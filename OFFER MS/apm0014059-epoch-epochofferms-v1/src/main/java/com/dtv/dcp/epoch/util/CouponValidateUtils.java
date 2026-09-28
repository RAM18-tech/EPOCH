package com.dtv.dcp.epoch.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dtv.dcp.epoch.common.Constants;

public class CouponValidateUtils {
	
    private static final Logger log = LoggerFactory.getLogger(CouponValidateUtils.class);
    /**
     * @param cpopDate
     * @return
     */
    public static String getFormattedDate(String cpopDate) {

        String formattedDate = null;
        if (Optional.ofNullable(cpopDate).isPresent()) {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(Constants.CPOP_DATE_FORMATE);
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT);
            LocalDate date = LocalDate.parse(cpopDate, inputFormatter);
            formattedDate = outputFormatter.format(date);
        }
        return formattedDate;
    }
    
    /**
     * @param startDate
     * @param endDate
     * @return
     */
    public static boolean validateActiveDates(String startDate, String endDate) {
        boolean result = false;
        Map<String, String> strtAndEndDateString = new HashMap<>();
        strtAndEndDateString.put(Constants.CAMPAIGN_END_DATE, endDate);
        strtAndEndDateString.put(Constants.CAMPAIGN_START_DATE, startDate);
        Map<String, Date> startAndEdnDate = validateDateFormat(strtAndEndDateString);
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone(Constants.PST));
        
    	log.info("strtAndEndDateString-hashmap");
    	System.out.println(Arrays.asList(strtAndEndDateString));

    	log.info("CAMPAIGN_START_DATE");
		System.out.print(startAndEdnDate.get(Constants.CAMPAIGN_START_DATE));
		
		log.info("CAMPAIGN_END_DATE");
		System.out.print(startAndEdnDate.get(Constants.CAMPAIGN_END_DATE));
		
		log.info("start Date");
		System.out.print(startDate);
		
		log.info("end Date");
		System.out.print(endDate);
		
        if (startDate != null && endDate != null && startAndEdnDate.get(Constants.CAMPAIGN_START_DATE) != null
                && startAndEdnDate.get(Constants.CAMPAIGN_END_DATE) != null) {
        	log.info("Date comparison inside 001");
            try {
                Date start = new SimpleDateFormat(Constants.DATE_TIME_FORMAT)
                        .parse(sdf.format(startAndEdnDate.get(Constants.CAMPAIGN_START_DATE)));
                Date end = new SimpleDateFormat(Constants.DATE_TIME_FORMAT)
                        .parse(sdf.format(startAndEdnDate.get(Constants.CAMPAIGN_END_DATE)));
            	log.info("Date comparison inside 002");

                if (start.before(new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(new Date()))) && (end
                        .after(new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(new Date())))
                        || (end.equals(
                        new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(new Date())))))) {
                    result = true;
                }
            } catch (ParseException e) {
                log.error("Invalid date present in the request - isActive() ", e);
            }

		}
		return result;
	}
    
    /**
     * @param startDate
     * @return
     */
    public static boolean validateActiveDate(String startDate) {
        boolean result = true;
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.CPOP_DATE_FORMATE);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        if (startDate != null) {
            try {
                Date start = new SimpleDateFormat(Constants.CPOP_DATE_FORMATE).parse(startDate);
                log.info("Current date ={} ",new SimpleDateFormat(Constants.CPOP_DATE_FORMATE).parse(sdf.format(new Date())));
                if ((start.before(new SimpleDateFormat(Constants.CPOP_DATE_FORMATE).parse(sdf.format(new Date()))))) {
                    result = false;
                }
            } catch (ParseException e) {
                log.error("Invalid date present in the request - isActive() ", e);
            }

		}
		return result;
	}
    
    /**
     * @param strtAndEndDate
     * @return
     */
    public static Map<String, Date> validateDateFormat(Map<String, String> strtAndEndDate) {
        Map<String, Date> dateWithTimeList = new HashMap<>();
        SimpleDateFormat sdfWithTime = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);
        SimpleDateFormat sdfWithDate = new SimpleDateFormat(Constants.DATE_FORMAT);
        if (!MapUtils.isEmpty(strtAndEndDate)) {
            strtAndEndDate.forEach((k, v) -> {
                Date dateWithTime = null;
                try {
                    sdfWithTime.setTimeZone(TimeZone.getTimeZone(Constants.PST));
                    if (v != null) {
                        dateWithTime = sdfWithTime.parse(v);
                    }
                    dateWithTimeList.put(k, dateWithTime);
                } catch (ParseException e) {
                    try {
                        sdfWithDate.setTimeZone(TimeZone.getTimeZone(Constants.PST));
                        if (v != null) {
                            Calendar cal = Calendar.getInstance();
                            if (k.equals(Constants.CAMPAIGN_START_DATE)) {
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
     * @param sdfWithDate
     * @param v
     * @return
     * @throws ParseException
     */
    private static Calendar setTimeInDate(SimpleDateFormat sdfWithDate, String v) throws ParseException {
        Date date = sdfWithDate.parse(v);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }



}
