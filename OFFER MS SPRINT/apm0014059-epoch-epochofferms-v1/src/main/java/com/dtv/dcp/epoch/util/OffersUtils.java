package com.dtv.dcp.epoch.util;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.integration.EpochClient;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.*;
import com.dtv.dcp.epoch.model.common.CustomerContext;
import com.dtv.dcp.epoch.model.common.request.*;
import com.dtv.dcp.epoch.model.common.request.CartProduct;
import com.dtv.dcp.epoch.model.common.response.CheckEligibiltyResponse;
import com.dtv.dcp.epoch.model.common.response.SwimlaneEligibilityDetails;
import com.dtv.dcp.epoch.model.ct.benefit.Benefit;
import com.dtv.dcp.epoch.model.ct.eligibility.Constraint;
import com.dtv.dcp.epoch.model.ct.eligibility.Eligibility;
import com.dtv.dcp.epoch.model.ct.generic.GenericByKey;
import com.dtv.dcp.epoch.model.ct.generic.GenericTypeIdBase;
import com.dtv.dcp.epoch.model.ct.offer.*;
import com.dtv.dcp.epoch.model.ct.product.*;
import com.dtv.dcp.epoch.model.ct.product.Variant;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.model.customergraph.response.CGResponse;
import com.dtv.dcp.epoch.model.eligibility.EligibilityErrorMessage;
import com.dtv.dcp.epoch.processor.ott.services.CustomerSubscriptionDetail;
import com.dtv.dcp.epoch.service.DMALookUpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
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

    /**
     * The log.
     */
    private static final Logger log = LoggerFactory.getLogger(OffersUtils.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String ZIP_CODE = "zipCode";

    private static final String DEVICES = "devices";
    
    private static final String EXCEPTION_OCCURED= " Exception Occured :";

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
    
	public boolean isADEFlow(CTOfferRequest ctOfferRequest,String feature) {		
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
            String dtvnMidasRules = enterpriseRule.getDtvnow();
            List<DtvnMidasRule> dtvnMidasRuleInfoList = JsonService
                    .getListObjectFromJsonTreeWithNoRootElement(dtvnMidasRules, DtvnMidasRule.class);
            return dtvnMidasRuleInfoList;

        } catch (ServiceException se) {
            throw se;
        } catch (Exception e) {
            log.error(EXCEPTION_OCCURED, e);
            throw new ServiceException(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR)
                    .addDetail(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR_DETAILS001);
        }

    }
    
    public static boolean needsAttributeRemoval(List<String> offerProductFamilies,List<String> offerTypes, List<String> productTypes) {
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
     * @param primaryKey
     * @return
     */
    public static String getKey(String primaryKey) {
        return primaryKey + "_" + Constants.CACHE_NAME + "_" + Constants.OBJECT_KEY + "_" + Constants.CACHE_VERSION_ID + "_" + Constants.APPLICATION_ID;
    }

    /**
     * @param keyExtractor
     * @param <T>
     * @return
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
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
                	if(isPSTflagEnabled()) {
                        sdfWithTime.setTimeZone(TimeZone.getTimeZone(Constants.PST));
                    }
                    if (v != null) {
                        dateWithTime = sdfWithTime.parse(v);
                    }
                    dateWithTimeList.put(k, dateWithTime);
                } catch (ParseException e) {
                    try {
                    	if(isPSTflagEnabled()) {
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

    /**
     * @param cpopDate
     * @return
     */
    public static String getFormattedDate(String cpopDate) {

        String formattedDate = null;
        if(isPSTflagEnabled()) {
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
            } catch(DateTimeParseException e) {
                log.error("Invalid date present in the request - isActive() ", e);
            }
        }
        return formattedDate;
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
        if(isPSTflagEnabled()) {
            sdf.setTimeZone(TimeZone.getTimeZone(Constants.PST));
        } else {
            sdf.setTimeZone(TimeZone.getTimeZone(Constants.GMT));
        }
        boolean isCompareDatePresent = true; 
        if(compareDate == null) {
        	isCompareDatePresent = false;
        	compareDate = new Date();
        }
        if (startDate != null && endDate != null && startAndEndDate.get(Constants.PROMO_START_DATE) != null
                && startAndEndDate.get(Constants.PROMO_END_DATE) != null) {
            try {
            	if(isPSTflagEnabled()) {
	            	Date currentDate=new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(compareDate));
	                Date start = new SimpleDateFormat(Constants.DATE_TIME_FORMAT)
	                        .parse(sdf.format(startAndEndDate.get(Constants.PROMO_START_DATE)));
	                Date end = new SimpleDateFormat(Constants.DATE_TIME_FORMAT)
	                        .parse(sdf.format(startAndEndDate.get(Constants.PROMO_END_DATE)));
	                if(isCompareDatePresent) {
	                	if ((start.before(currentDate) || start.equals(currentDate)) && (end.after(currentDate) || end.equals(currentDate))) {
	                        result = true;
	                    }
	                } else {
	                	if ((start.before(currentDate)) && (end.after(currentDate) || end.equals(currentDate))) {
	                        result = true;
	                    }
	                }
                } else {
            		Date currentDate=new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(compareDate));
                    Date start = startAndEndDate.get(Constants.PROMO_START_DATE);
                    Date end = startAndEndDate.get(Constants.PROMO_END_DATE);
                    if(isCompareDatePresent) {
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
     * @param startDate
     * @param endDate
     * @return
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
     * @param startDate
     * @param endDate
     * @return
     */
    public static boolean validateActiveDates(String startDate, String endDate) {
    	return validateActiveDates(startDate, endDate, null);
    }
    
    

    public static int returnNumberOfDays(String endDate) {
    	LocalDate localCurrentDate = LocalDate.parse(LocalDate.now().toString(),DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_YYYY_MM_DD));
		LocalDate localEndDate = LocalDate.parse(endDate,DateTimeFormatter.ofPattern(Constants.DTVN_CPC_DATE_FORMAT));
		return Math.toIntExact(ChronoUnit.DAYS.between(localEndDate,localCurrentDate));
	}
	
	

    /**
     *
     * @param startDate
     * @return
     */
    public static boolean isEndDateIsGreater(String startDate) {
        boolean result = false;
        Map<String, String> strtAndEndDateString = new HashMap<>();
        strtAndEndDateString.put(Constants.PROMO_START_DATE, startDate);

        Map<String, Date> startAndEdnDate = validateDateFormat(strtAndEndDateString);
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);
        if(isPSTflagEnabled()) {
            sdf.setTimeZone(TimeZone.getTimeZone(Constants.PST));
        } else {
            sdf.setTimeZone(TimeZone.getTimeZone(Constants.GMT));
        }
        if (startDate != null && startAndEdnDate.get(Constants.PROMO_START_DATE) != null) {
            try {
            	if(isPSTflagEnabled()) {
                    Date start = new SimpleDateFormat(Constants.DATE_TIME_FORMAT)
                            .parse(sdf.format(startAndEdnDate.get(Constants.PROMO_START_DATE)));

                    if (start.after(new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(new Date()))) ) {
                        result = true;
                    }
                } else {
                    Date start = startAndEdnDate.get(Constants.PROMO_START_DATE);
                    if (start.after(new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(new Date()))) ) {
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
    *
    * @param startDate
    * @return
    */
    public static boolean isFutureDate(String startDate) {
    	boolean result = false;
    	try {
    		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_M_D_YYYY);
    		Date currentDate=new SimpleDateFormat(Constants.DATE_FORMAT_M_D_YYYY).parse(sdf.format(new Date()));
    		Date start = sdf.parse(startDate);
    		if(start.after(currentDate)) {
    			result = true;
    		}
    	} catch (ParseException e) {
    		if(startDate.contains("/")) {
    			startDate =	startDate.replaceAll("/", "-");
    		}
    		try {
    			SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_M_D_YYYY);
    			Date currentDate=new SimpleDateFormat(Constants.DATE_FORMAT_M_D_YYYY).parse(sdf.format(new Date()));
    			Date start = sdf.parse(startDate);
    			if(start.after(currentDate)) {
    				result = true;  
    			}
    		} catch (ParseException ex) {
    			log.error(String.format("Invalid date2 present in the request - validateDateFormat() %s", ex)); 
    		}
    	}
    	return result;
    }
    
    /**
    *
    * @param startDate,endDate
    * @return
    */
   public static boolean isDateAfter(String startDate, String endDate) {
       boolean result = false;
       Map<String, String> strtAndEndDateString = new HashMap<>();
       strtAndEndDateString.put(Constants.PROMO_START_DATE, startDate);
       strtAndEndDateString.put(Constants.PROMO_END_DATE, endDate);

       Map<String, Date> startAndEdnDate = validateDateFormat(strtAndEndDateString);
       SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);
       if(isPSTflagEnabled()) {
           sdf.setTimeZone(TimeZone.getTimeZone(Constants.PST));
       } else {
           sdf.setTimeZone(TimeZone.getTimeZone(Constants.GMT));
       }
       if (startDate != null && startAndEdnDate.get(Constants.PROMO_START_DATE) != null) {
           try {
           	if(isPSTflagEnabled()) {
                   Date start = new SimpleDateFormat(Constants.DATE_TIME_FORMAT)
                           .parse(sdf.format(startAndEdnDate.get(Constants.PROMO_START_DATE)));
                   Date end = new SimpleDateFormat(Constants.DATE_TIME_FORMAT)
                           .parse(sdf.format(startAndEdnDate.get(Constants.PROMO_END_DATE)));

                   if (start.after(end) ) {
                       result = true;
                   }
               } else {
                   Date start = startAndEdnDate.get(Constants.PROMO_START_DATE);
                   Date end = startAndEdnDate.get(Constants.PROMO_END_DATE);
                   if (start.after(end) ) {
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
   *
   * @param startDate,endDate
   * @return
   */
  public static boolean isDateSameOrAfter(String startDate, String endDate) {
      boolean result = false;
      Map<String, String> strtAndEndDateString = new HashMap<>();
      strtAndEndDateString.put(Constants.PROMO_START_DATE, startDate);
      strtAndEndDateString.put(Constants.NBCD_DATE, endDate);

      Map<String, Date> startAndEdnDate = validateDateFormat(strtAndEndDateString);
      SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);
      if(isPSTflagEnabled()) {
          sdf.setTimeZone(TimeZone.getTimeZone(Constants.PST));
      } else {
          sdf.setTimeZone(TimeZone.getTimeZone(Constants.GMT));
      }
      if (startDate != null && startAndEdnDate.get(Constants.PROMO_START_DATE) != null) {
          try {
          	if(isPSTflagEnabled()) {
                  Date start = new SimpleDateFormat(Constants.DATE_TIME_FORMAT)
                          .parse(sdf.format(startAndEdnDate.get(Constants.PROMO_START_DATE)));
                  Date end = new SimpleDateFormat(Constants.DATE_TIME_FORMAT)
                          .parse(sdf.format(startAndEdnDate.get(Constants.NBCD_DATE)));

                  if (start.equals(end)|| start.after(end)) {
                      result = true;
                  }
              } else {
                  Date start = startAndEdnDate.get(Constants.PROMO_START_DATE);
                  Date end = startAndEdnDate.get(Constants.NBCD_DATE);
                  if (start.equals(end)|| start.after(end)) {
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
     *
     * @param startDate
     * @return
     */
    public static boolean isDateActive(String startDate) {
        boolean result = false;
        Map<String, String> strtAndEndDateString = new HashMap<>();
        strtAndEndDateString.put(Constants.PROMO_START_DATE, startDate);

        Map<String, Date> startAndEdnDate = validateDateFormat(strtAndEndDateString);
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);
        if(isPSTflagEnabled()) {
            sdf.setTimeZone(TimeZone.getTimeZone(Constants.PST));
        } else {
            sdf.setTimeZone(TimeZone.getTimeZone(Constants.GMT));
        }
        if (startDate != null && startAndEdnDate.get(Constants.PROMO_START_DATE) != null) {
            try {
            	if(isPSTflagEnabled()) {
                    Date start = new SimpleDateFormat(Constants.DATE_TIME_FORMAT)
                            .parse(sdf.format(startAndEdnDate.get(Constants.PROMO_START_DATE)));

                    if (start.before(new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(new Date()))) ) {
                        result = true;
                    }
                } else {
                    Date start = startAndEdnDate.get(Constants.PROMO_START_DATE);
                    if (start.before(new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(new Date()))) ) {
                        result = true;
                    }
                }
            } catch (ParseException e) {
                log.error("Invalid date present in the request - isActive() ", e);
            }

        }
        return result;
    }
    
    public boolean isDateWithinEligibilityWindow(String serviceEndDate, List<String> salesChannel, List<String> offerProductType,List<String> offerCodes) {

		log.info("offerRequestWrapper.getOfferRequest().getServiceEndDate() {}",sanitizeData(serviceEndDate));
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
			
			if(Optional.ofNullable(offerCodes).isPresent() && offerCodes.size()>0
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
	 * @param existingGroup
	 * @return boolean
	 * Method to check whether the given String can be parsed to Long or not
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
		if ((offerRequest.getSalesChannel().contains(Constants.OPUS) || offerRequest.getSalesChannel().contains(Constants.DTV360) || offerRequest.getSalesChannel().contains(Constants.UVC))
				&& (Objects.nonNull(offerRequest.getContractIndicator()) 
				&& !offerRequest.getContractIndicator().contains(Constants.EDSP_STRING))) {
			if (offerRequest.isReconnectCustomer() 
					&& !isDateWithinEligibilityWindow(offerRequest.getServiceEndDate(),offerRequest.getSalesChannel(),offerRequest.getOfferProductType(),offerRequest.getOfferCodes())){
				offerRequest.setContractIndicator(Arrays.asList((Constants.TAZ_STRING)));
			}
		}
		if ((offerRequest.getSalesChannel().contains(Constants.OPUS) || offerRequest.getSalesChannel().contains(Constants.DTV360) || offerRequest.getSalesChannel().contains(Constants.UVC))
				&& Objects.isNull(offerRequest.getContractIndicator())) {
			offerRequest.setContractIndicator(Arrays.asList((Constants.TAZ_STRING)));
		}
		if (offerRequest.getSalesChannel().contains(Constants.INDIRECT_PARTNER)
				&& Objects.isNull(offerRequest.getContractIndicator())) {
			offerRequest.setContractIndicator(Arrays.asList((Constants.TAZ_STRING)));
		}

		if (offerRequest.getSalesChannel().contains(Constants.DIRECTV_ONLINE)
				&& !Objects.nonNull(offerRequest.getContractIndicator())) {
			offerRequest.setContractIndicator(Arrays.asList((Constants.TAZ_STRING)));
		}
		
		if (offerRequest.getSalesChannel().contains(Constants.DIRECTV_STREAM_ONLINE)
				&& !Objects.nonNull(offerRequest.getContractIndicator())) {
			offerRequest.setContractIndicator(Arrays.asList((Constants.TAZBYOD_STRING)));
		}		
	}
	

	/**
	 * @param dtvnRule
	 * @return
	 */
	public static boolean getContractIntentForContract(DtvnMidasRule dtvnRule) {
		return Optional.ofNullable(dtvnRule.getContractIntent()).isPresent()
				&& ("Contract".equalsIgnoreCase(dtvnRule.getContractIntent()));
	}

	/**
	 * @param dtvnRule
	 * @return
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
		return types != null ? types.stream().anyMatch(type -> type.equalsIgnoreCase(matchType)): false;
	}
	
	public static boolean isPSTflagEnabled() {
		// Commenting this out as this will always result in false
		/*FeatureManagerHelper featureManagerHelper = new FeatureManagerHelper();
		try {
			if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_PST_TIME_LOGIC_ENABLED)) {
				return true;
			}
		} catch(NullPointerException e ) {
	      log.error("featureManagerHelper.isEnabled catch block  - isPSTflagEnabled()");
		}*/
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
	 * Filter other offers by fee type.
	 *
	 * @param ctOffers the ct offers
	 * @param feeType the fee type
	 * @return the list
	 */
	public List<CTOffer> filterOtherFeeOffersByFeeType(List<CTOffer> ctOffers, String feeType) {
		List<CTOffer> otherFeeOffers = null;
        if (Optional.ofNullable(ctOffers).isPresent() ) {
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
	 * Filter fee offers by fee type.
	 *
	 * @param ctOffers the ct offers
	 * @param feeType the fee type
	 * @return the list
	 */
	public List<CTOffer> filterFeeOffersByFeeType(List<CTOffer> ctOffers, String feeType) {
		List<CTOffer> otherFeeOffers = null;
        if (Optional.ofNullable(ctOffers).isPresent() ) {
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
	 * This method is to filter video device Offers based on device type
	 * @param ctOffers
	 * @param deviceType
	 * @return
	 */
	public List<CTOffer> filterDeviceOffersByDeviceType(List<CTOffer> ctOffers, String deviceType) {
		List<CTOffer> deviceOffers = null;
        if (Optional.ofNullable(ctOffers).isPresent() ) {
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
	
	public List<CTOffer> filterStandAloneOffersByAddOnTypePlanSubType(List<CTOffer> ctOffers, String addOnType, String planSubType,String accountType) {
		List<CTOffer> standAloneOffers = null;
        if (Optional.ofNullable(ctOffers).isPresent() ) {
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
						offer.getAttributes().getEligibility().getConstraints()!= null &&
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
		if(Optional.ofNullable(cgResponse).isPresent() 
				&& Optional.ofNullable(cgResponse.getAccountInfo()).isPresent()) {
			return cgResponse.getAccountInfo().getAccountType();
		}
		return null;
	}
	public static String formatTwoDigits(int dayOfMonth) {
		String date = Integer.toString(dayOfMonth);
		if (dayOfMonth < 10) {
		    NumberFormat f = new DecimalFormat("00");
		    date = String.valueOf(f.format(dayOfMonth));
		}
		return date;
	}
    public List<String> fetchCTZipcodes() {
        try {
            String zipcode = enterpriseRule.getCtZipCodes();
            List<String> zipcodeList = JsonService.getListObjectFromJsonTree(zipcode, "zipCode", String.class);
            return zipcodeList;

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
		if( inputList != null && !inputList.isEmpty()) {
			return HtmlUtils.htmlEscape(inputList.toString());
		} else {
		return null;
		}
	}
	
	public static String sanitizeData(String input) {
		if( input != null ) {
			return HtmlUtils.htmlEscape(input);
		} else {
			return null;
		}
	}

	public boolean containsEDSP (OfferRequest offerRequest) {
		return (!CollectionUtils.isEmpty(offerRequest.getContractIndicator())
				&& offerRequest.getContractIndicator().contains("EDSP"));
	}
	
	public boolean containsTAZ (OfferRequest offerRequest) {
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

	public boolean isActiveOffer(CTOffer ctOffer,Date serverDate) {
		return ctOffer != null 
				&& ctOffer.getStartDate() != null 
				&& ctOffer.getEndDate() != null
                && OffersUtils.validateActiveDates(
                	OffersUtils.getFormattedDate(ctOffer.getStartDate()), OffersUtils.getFormattedDate(ctOffer.getEndDate()),serverDate);
	}

	public String getIgnoreAttributes() {
		return ignoreAttributes;
	}

	public List<CTOffer> filterOffersByProductType(List<CTOffer> ctOffers, String productType) {
		List<CTOffer> deviceOffers = null;
        if (Optional.ofNullable(ctOffers).isPresent() ) {
        	deviceOffers = ctOffers.stream().filter(Objects::nonNull).filter(offer -> 
						offer.getAttributes() != null && offer.getAttributes().getOfferProductType() != null &&  
						productType.equalsIgnoreCase(offer.getAttributes().getOfferProductType())) 
						.collect(Collectors.toList());
        }
        return deviceOffers;
	}
	
	public List<CTOffer> filterOneTimePaymentOffers(List<CTOffer> ctOffers) {
		List<CTOffer> oneTimePaymentOffers = null;
		List<CTOffer> offersWithBillingId = null;
		if (Optional.ofNullable(ctOffers).isPresent() ) {
			//Get the billingID from offer
			//Go to the list of variants in all the associated products and select the price with the matching billing id
			//Check if 'NumberOfPayments' attribute is configured to '1' 
			offersWithBillingId = ctOffers.stream().filter(Objects::nonNull).filter(offer ->
				Objects.nonNull(offer.getAttributes())
				&& !StringUtils.isEmpty(offer.getAttributes().getBillingId())
				&& !StringUtils.isEmpty(offer.getAttributes().getBillingCode())
			).collect(Collectors.toList());
			
			if(Objects.nonNull(offersWithBillingId) && !offersWithBillingId.isEmpty()) {
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
	 * @param ctOffer
	 * @return
	 */
	public Boolean hasLocalChannels(CTOffer ctOffer) {
		Boolean hasLocalChannels = false;
		if(Objects.nonNull(ctOffer) && Objects.nonNull(ctOffer.getAttributes()) &&
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
	 * @param ctOffer
	 * @return
	 */
	public Boolean isEligibleForServedMarket(CTOffer ctOffer) {
		Boolean isEligibleForServedMarket = false;
		if(Objects.nonNull(ctOffer.getAttributes()) && Objects.nonNull(ctOffer.getAttributes().getEligibleForServedMarket())) {				
			isEligibleForServedMarket = ctOffer.getAttributes().getEligibleForServedMarket();
		}									
		return isEligibleForServedMarket;
	}

	/**
	 * This method is used to check whether the BP is Roadrunner or not
	 *
	 * @param ctOffer
	 * @return
	 */
	public Boolean isRoadrunner(CTOffer ctOffer) {
		Boolean isRoadrunner = false;

		ProductObj productObj = getProductObjFromOffer(ctOffer);

		if(Objects.nonNull(productObj)
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
	 * @param ctOffer
	 * @return
	 */
	public Boolean isLocalsBolton(CTOffer ctOffer) {
		Boolean isLocalsBolton = false;

		ProductObj productObj = getProductObjFromOffer(ctOffer);

		if(Objects.nonNull(productObj)
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
	 * @param ctOffer
	 * @return
	 */
	public Boolean isLocalChannelVideoAddon(CTOffer ctOffer) {
		Boolean isLocalChannelVideoAddon = null;
		ProductObj productObj = getProductObjFromOffer(ctOffer);

		if(Objects.nonNull(productObj)
				&& !CollectionUtils.isEmpty(productObj.getVariants())
				&& Objects.nonNull(productObj.getVariants().get(0))
				&& Objects.nonNull(productObj.getVariants().get(0).getAttributes())) {
			isLocalChannelVideoAddon = productObj.getVariants().get(0).getAttributes().getHasLocalChannels();
		}

		return isLocalChannelVideoAddon;
	}

    /**
     * This method is used to get the product id from offer
     * @param ctOffer
     * @return
     */
    public String getProductIdFromOffer(CTOffer ctOffer) {
    	if(Objects.nonNull(ctOffer) && Objects.nonNull(ctOffer.getAttributes()) && !CollectionUtils.isEmpty(ctOffer.getAttributes().getAssociatedProducts())
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
	 * @param ctOffer
	 * @return
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
    	
    	if ((null != processctOfferResponse) && processctOfferResponse.getOffers().size() > 0) {
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
    	
    	if ((null != processctOfferResponse) && processctOfferResponse.getOffers().size() > 0) {
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
    	
    	if ((null != processctOfferResponse) && processctOfferResponse.getOffers().size() > 0) {
    		List<CTOffer> filteredOffers = new ArrayList<>();
    		processctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(offer -> {
    			//For Free trial offers Setting conflicting offers as null. For the other offers returning the response as is
				if (!Optional.ofNullable(offer.getAttributes().getOfferType()).isPresent() ||
						(Optional.ofNullable(offer.getAttributes().getOfferType()).isPresent() && offer.getAttributes().getOfferType().equalsIgnoreCase(Constants.FREETRIALWITHHYPHEN))) {
					offer.getAttributes().setConflictingOffers(null);
					filteredOffers.add(offer);
				} else if(Optional.ofNullable(offer.getAttributes().getEligibility()).isPresent()
						&& Optional.ofNullable(offer.getAttributes().getEligibility().getConstraints()).isPresent()
						&& !CollectionUtils.isEmpty(offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments())
						&& offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments().contains(Constants.EMPLOYEE)){
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
        
        if(!offerRequestWrapper.getOfferRequest().getOfferActionType().contains(Constants.ACQUISITION)) {
        	if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext())) {
            	offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite().getProducts().forEach(product-> {
        			if(org.apache.commons.lang.StringUtils.isNotBlank(product.getBillingProductCode())) {
        				billingProductCodes.add(product.getBillingProductCode());
        			}
        		});
            }
        }
        if(offerRequestWrapper.getOfferRequest().getOfferActionType().contains(Constants.ACQUISITION) 
        		&& !StringUtils.isEmpty(offerRequestWrapper.getOfferRequest().getBillingSystem())
        		&& Constants.STMS.equalsIgnoreCase(offerRequestWrapper.getOfferRequest().getBillingSystem())) {
        	isSTMSRequest = true;
        }
        if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getIsMigrationRequired())
        		&& Boolean.TRUE.equals(offerRequestWrapper.getOfferRequest().getIsMigrationRequired())) {
        	isSTMSRequest = true;
        }
        
        if(CollectionUtils.isNotEmpty(billingProductCodes)) {
        	isSTMSRequest = true;
		}
        return isSTMSRequest;
    }
    
    public Boolean isNotValidBasedOnBillingSystem(CTOffer ctOffer, OfferRequestWrapper offerRequestWrapper) {
		boolean isSTMSRequest = isSTMSRequest(offerRequestWrapper);
		if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getIsMigrationRequired()) 
				&& offerRequestWrapper.getOfferRequest().getIsMigrationRequired().equals(Boolean.TRUE)) {	
			if(!Constants.FEE.equalsIgnoreCase(ctOffer.getAttributes().getOfferProductType()) 
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
	 * @param ctOffer
	 * @param offerRequestWrapper
	 * @return
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
	 * @param ctOffer
	 * @param selectedOffers
	 * @param offerRequestWrapper
	 * @return
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
	 * @param ctOffer
	 * @param offerRequestWrapper
	 * @return
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
		|| salesChannel.equalsIgnoreCase(Constants.OEM_IAPFIRETV) ||salesChannel.equalsIgnoreCase(Constants.OEM_IAPROKUTV) || salesChannel.equalsIgnoreCase(Constants.EVERGENT_CRM) || salesChannel.equalsIgnoreCase(Constants.OEM_IAP_GOOGLE)
                || salesChannel.equalsIgnoreCase(Constants.DIRECT_INTEGRATION_PARTNER) || salesChannel.equalsIgnoreCase(Constants.OSPREY) || salesChannel.equalsIgnoreCase(Constants.DIRECTV_STREAM_ONLINE) || salesChannel.equalsIgnoreCase(Constants.ISUROKUTV)
                || salesChannel.equalsIgnoreCase(Constants.ASSISTED_SALES));
	}
    
    public static boolean checkAgentIndirectChannels(String salesChannel, List<String> productFamily) {
    	return !CollectionUtils.isEmpty(productFamily) && productFamily.contains(Constants.SATELLITE_PRODUCT_FAMILY) 
    			&& (salesChannel.equalsIgnoreCase(Constants.SALES_CRM) || salesChannel.equalsIgnoreCase(Constants.CCAP) || salesChannel.equalsIgnoreCase(Constants.DPP) 
                || salesChannel.equalsIgnoreCase(Constants.DIRECT_INTEGRATION_PARTNER) || salesChannel.equalsIgnoreCase(Constants.ASSISTED_SALES));
	}
    
    public boolean checkIsOemChannel(OfferRequest offerRequest) {
    	return  Optional.ofNullable(offerRequest).isPresent()
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



	public void setMaxOccurance(List<String> customerSegments,Benefit benefit, CTOffer offer, OfferRequestWrapper offerRequestWrapper, int freeDeviceCount) {
    	if(Objects.nonNull(customerSegments) && Objects.nonNull(benefit)){
			if(customerSegments.contains(Constants.DEMO)){				
				benefit.setMaxOccurrence(Optional.ofNullable(benefit.getMaxOccurrence_demo()).isPresent()  ? benefit.getMaxOccurrence_demo(): 0 );	
			}else if(customerSegments.contains(Constants.DECA)){		
				benefit.setMaxOccurrence(Optional.ofNullable(benefit.getMaxOccurrence_deca()).isPresent()  ? benefit.getMaxOccurrence_deca(): 0 );	
			}else if(customerSegments.contains(Constants.SHOWROOM)){    
				benefit.setMaxOccurrence(Optional.ofNullable(benefit.getMaxOccurrence_showroom()).isPresent()  ? benefit.getMaxOccurrence_showroom(): 0 );
			}else if(customerSegments.contains(Constants.BCOMP)){		
				benefit.setMaxOccurrence(Optional.ofNullable(benefit.getMaxOccurrence_bcomp()).isPresent()  ? benefit.getMaxOccurrence_bcomp(): 0 );
			}else if(customerSegments.contains(Constants.COURTESY)){	
				benefit.setMaxOccurrence(Optional.ofNullable(benefit.getMaxOccurrence_courtesy()).isPresent()  ? benefit.getMaxOccurrence_courtesy(): 0 );	
			}
    	}
		if( Objects.nonNull(offer.getAttributes().getAssociatedProducts()) 
    			&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts())
				&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts())
				&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0))
				&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj())
				&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants())
				&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0))
				&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes())
				&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().getMinMaxQuantity())) {
			
			if((offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.MDUTENANT) || offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.SHOWROOM)
					||offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.COURTESY) || offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.BCOMP))
				&& (Objects.nonNull(freeDeviceCount) && freeDeviceCount!=0 && Objects.nonNull(benefit))) {
				offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().setFreeDeviceCount(freeDeviceCount);
				if(Optional.ofNullable(offer.getAttributes().getBenefits()).isPresent() && !offer.getAttributes().getBenefits().isEmpty()){
					offer.getAttributes().getBenefits().forEach(benefitobj -> benefitobj.setMaxOccurrence(freeDeviceCount));
				}
			}
			if((offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.DEMO) || offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.DECA)) 
					&& Objects.nonNull(benefit)) {
				offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().setFreeDeviceCount(benefit.getMaxOccurrence());
			}
			if(Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerSegments()).isPresent()
				    &&
				    	(	
				    		offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.DEMO)
				    			||
				    		(offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.MDUTENANT)&&offer.getAttributes().isBulkOffer())
				    	)
            ) {
				List<MinMaxQuantity> minmax=offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().getMinMaxQuantity();
				minmax.forEach(obj ->{
					if(Objects.nonNull(obj.getContractApplicable()) 
							&&((Objects.nonNull(offerRequestWrapper.getOfferRequest().getContractIndicator())
									&& isMatchFound(offerRequestWrapper.getOfferRequest().getContractIndicator(),obj.getContractApplicable().get(0)))
								||
								obj.getContractApplicable().contains(offer.getAttributes().getContractIndicator())
							) 
						) {
						if(Optional.ofNullable(offer.getAttributes().getBenefits()).isPresent() && !offer.getAttributes().getBenefits().isEmpty()){
						obj.setMaxQuantity(String.valueOf(offer.getAttributes().getBenefits().get(0).getMaxOccurrence()));
						}else if(offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.MDUTENANT)) {
							obj.setMaxQuantity(String.valueOf(freeDeviceCount));
						}
						
					}
				});
			}
			
		}
		
    }
    
    public List<CTOffer> removeBulkOffer(List<CTOffer> offers , String offerProductType, int maxOccurance) {
    	
    	List<CTOffer> finalOffers= new ArrayList<CTOffer>();
    	
    	log.info("maxoccurance-------->>>>>"+String.valueOf(maxOccurance));
    	
    	offers.forEach(offer ->{
    		log.info("offer-------->>>>>"+String.valueOf(offer.getCode()));
    		if(Optional.ofNullable(offer).isPresent() && Optional.ofNullable(offer.getAttributes()).isPresent()
					&& Optional.ofNullable(offer.getAttributes().getOfferProductType()).isPresent()
					&& offer.getAttributes().getOfferProductType().equalsIgnoreCase(Constants.VIDEO_DEVICE)
					&& Optional.ofNullable(offer.getAttributes().getEligibility()).isPresent()
					&& Optional.ofNullable(offer.getAttributes().getEligibility().getConstraints()).isPresent()
					&& Optional.ofNullable(offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments()).isPresent()
					&& offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments().contains(Constants.MDUTENANT)) {
    			
    			log.info("logic-------->>>>> inside");
    				if(maxOccurance>0) {
	    				if(Optional.ofNullable(offer.getAttributes().getBenefits()).isPresent() && !offer.getAttributes().getBenefits().isEmpty()
	    						&& offer.getAttributes().getBenefits().get(0).getMaxOccurrence()>0 && Objects.nonNull(offer.getAttributes().isBulkOffer()) && offer.getAttributes().isBulkOffer() ) {
	    					finalOffers.add(offer);
	    				}
    				}else if(Objects.isNull(offer.getAttributes().isBulkOffer()) || !offer.getAttributes().isBulkOffer()){
	    					finalOffers.add(offer);
	    			}
    				
            } else {
    			log.info("logic-------->>>>> outside");
    			finalOffers.add(offer);
    		}
    	});
    	return finalOffers;
    	
    }
    public static void calculateBestPriceForCredit(CTOffer offer,List<String> choiceGrpOnDeselection, List<String> choiceGrpOnSelection,OfferRequestWrapper offerRequestWrapper,boolean choiceGrpSalesChannelFlag) {
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
            if(offerRequestWrapper.getOfferRequest().getCartOffers() == null && benefit.getBenefitType().equalsIgnoreCase("flat-off") && choiceGrpOnSelection.contains(benefit.getCode()))
            {
                Double benefitPrice = benefit.getValue().getDollarAmount();
                totalChoiceOnSelectionPrice = totalChoiceOnSelectionPrice + benefitPrice;
            }
            if(offerRequestWrapper.getOfferRequest().getCartOffers() == null && benefit.getBenefitType().equalsIgnoreCase("flat-off") && choiceGrpOnDeselection.contains(benefit.getCode()))
            {
                Double benefitPrice = benefit.getValue().getDollarAmount();
                totalChoiceOnDeSelectionPrice = totalChoiceOnDeSelectionPrice + benefitPrice;
            }
        }
        BigDecimal bd = new BigDecimal(Double.toString(totalOfferPrice));
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        offerPrice.setDollarAmount(bd.doubleValue());
        if( offerRequestWrapper.getOfferRequest().getCartOffers() == null && choiceGrpSalesChannelFlag
                && (!choiceGrpOnDeselection.isEmpty() || !choiceGrpOnSelection.isEmpty() ))
        {
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

	public void calculateBestPrice(OfferRequestWrapper offerRequestWrapper, List<CTOffer> offerList) {
        boolean isBYODFlowStatus = Optional.ofNullable(offerRequestWrapper)
                .map(OfferRequestWrapper::getOfferRequest)
                .map(OfferRequest::getCustomerEligibility)
                .map(CustomerEligibility::isBYODFlow)
                .orElse(false);
        offerList.stream().filter(Objects::nonNull).forEach(offer -> {
            // SLS-IXP-FLAG changes: use OR so this runs exactly once even if both flags are enabled
            if(isSlsGetOfferSalesEnabled() || isSlsGetOfferServicesEnabled()) {
                setBasePriceBasedOnAttributePricingCriteria(offerRequestWrapper, offer);
            }
            setBasePriceBasedOnPriceTier(offer);
            if (!(offerRequestWrapper.isNoDeviceFrameworkEnabled() && isBYODFlowStatus)
                    && offerRequestWrapper.getOfferRequest().getCartOffers() != null
                    && checkOfferCodeInChoiceGrp(offer.getAttributes().getOfferChoiceGroup(), offerRequestWrapper)) {
                updateBenefitsBasedOnSelection(offerRequestWrapper, offer);
            }
                    AtomicReference<List<String>> choiceGrpOnSelection = new AtomicReference<>(new ArrayList<>());
                    AtomicReference<List<String>> choiceGrpOnDeselection = new AtomicReference<>(new ArrayList<>());
            AtomicBoolean isChoicGrpSalesChannelPresentInRequest = new AtomicBoolean(false);
                    if (Optional.ofNullable(offer.getAttributes().getOfferChoiceGroup()).isPresent()) {
                        if (Optional.ofNullable(offer.getAttributes().getOfferChoiceGroup()).isPresent() && !offer.getAttributes().getOfferChoiceGroup().isEmpty()) {
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
                calculateBestPriceForCredit(offer, choiceGrpOnSelection.get(), choiceGrpOnDeselection.get(), offerRequestWrapper, isChoicGrpSalesChannelPresentInRequest.get());
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
    public static void setBasePriceBasedOnPriceTier(CTOffer offer) {
        if (Optional.ofNullable(offer.getAttributes().getAssociatedProducts()).isPresent()) {
            AssociatedProduct associatedProduct = offer.getAttributes().getAssociatedProducts().get(0);
            if (Optional.ofNullable(associatedProduct.getBundleProducts()).isPresent()) {
                ProductWrapper bundleProduct = associatedProduct.getBundleProducts().get(0);
                if (Optional.ofNullable(bundleProduct).isPresent()) {
                    List<Product> products = bundleProduct.getProducts();
                    setBasePriceTierPriceForQualifyingOrBundledProducts(offer, products);
                }
            }
            if (Optional.ofNullable(associatedProduct.getQualifyingProducts()).isPresent()) {
                ProductWrapper bundleProduct = associatedProduct.getQualifyingProducts().get(0);
                if (Optional.ofNullable(bundleProduct).isPresent()) {
                    List<Product> products = bundleProduct.getProducts();
                    setBasePriceTierPriceForQualifyingOrBundledProducts(offer, products);
                }
            }
        }
    }

    private static void setBasePriceTierPriceForQualifyingOrBundledProducts(CTOffer offer, List<Product> products) {
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
                            if (priceWithPriceTier.size() > 0 && priceWithPriceTier.stream().anyMatch(price -> price.getPriceTier().contains(priceTier))) {
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
        if (offerRequestWrapper.getOfferRequest().getCartOffers() == null && !choiceGrpOnDeselection.get().isEmpty() && choiceGrpOnSelection.get().isEmpty())
        {

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

        if (offerRequestWrapper.getOfferRequest().getCartOffers() == null && choiceGrpOnDeselection.get().isEmpty() && !choiceGrpOnSelection.get().isEmpty())
        {

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
                                        && benefitProd.getKey().equalsIgnoreCase(product.getKey()) ) {
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
				}else {
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

                            if (matchingOfferCodesCount >= groupSelectionCount) {
                                selectionStatus = true;
                            } else {
                                selectionStatus = false;
                            }
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
		
		
		benefits.stream().forEach(benefit ->{
			if(Optional.ofNullable(benefit.getBillingBenefitCode()).isPresent() && !benefit_codes.contains(benefit.getBillingBenefitCode())) {
				benefit_codes.add(benefit.getBillingBenefitCode());
				final_benefits.add(benefit);
			}
		});
		
		return final_benefits;
		
	}

    private static Double setBasePriceForProduct(OfferPrice offerPrice, Product product, CTOffer offer,String choiceGrp) {
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

                                if(choiceGrp.equalsIgnoreCase("promoOnSelect")) {
                                    offerPrice.setPriceOnCGSelection(dollarAmount);
                                }
                                else if(choiceGrp.equalsIgnoreCase("promoOnDeSelect")){
                                    offerPrice.setPriceOnCGDeselection(dollarAmount);
                                }else {
								offerPrice.setDollarAmount(dollarAmount);
                                }
							});
				}
			}
		}

        if(choiceGrp.equals("promoOnSelect")) {
            return offerPrice.getPriceOnCGSelection();
        }else if(choiceGrp.equals("promoOnDeSelect")){
            return offerPrice.getPriceOnCGDeselection();
        }
        else {
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
                    }else if ((benefit.getBenefitType().equalsIgnoreCase("free-promo")
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
     * @param product
     * @param contractIndicator
     * @return
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
	 * @param dateStr
	 * @return
	 */
	public static LocalDate convertToLocalDate(String dateStr) {
		LocalDate date = null;
		DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("[yyyy-MM-dd]")
				.appendPattern("[MM/dd/yyyy]").toFormatter();
		try {
			date = LocalDate.parse(dateStr, formatter);
		} catch (Exception ex) {
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
	
	public void filterOfferByAttribute(CTOfferResponse ctOfferResponse,PartnerDealerDetails partnerDetails) {		
		if(!Objects.isNull(partnerDetails)) {			
			List<CTOffer> removeOffers = new ArrayList<>();
			List<CTOffer> offers = ctOfferResponse.getOffers();
			List<GenericTypeIdBase> removableConflictingOffers = new ArrayList<>();		
			if(!CollectionUtils.isEmpty(offers)) {
				offers.stream().filter(Objects::nonNull).forEach(offer -> {
					boolean checkPartnerDealerCode1 = partnerDetails.getPartnerDealerCode1()!=null && !CollectionUtils.isEmpty(offer.getAttributes().getPartnerDealerCode1());
					boolean checkPartnerDealerCode2 = partnerDetails.getPartnerDealerCode2()!=null && !CollectionUtils.isEmpty(offer.getAttributes().getPartnerDealerCode2());
					if ((checkPartnerDealerCode1 && !offer.getAttributes().getPartnerDealerCode1().contains(partnerDetails.getPartnerDealerCode1()))
							|| (checkPartnerDealerCode2 && !offer.getAttributes().getPartnerDealerCode2().contains(partnerDetails.getPartnerDealerCode2()))) {								
						removeOffers.add(offer);
					}else {
						if(!CollectionUtils.isEmpty(offer.getAttributes().getConflictingOffers()) && 
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
	public List<CTOffer> filterDOFeeOffer(CTOfferResponse ctOfferResponse, OfferRequestWrapper offerRequestWrapper){
		return ctOfferResponse.getOffers().stream().filter(offer -> 
		Objects.nonNull(offer.getAttributes())
		&&(
				
			(   Objects.isNull(offer.getAttributes().getCreditRisk())  || 
					( 
					Objects.nonNull(offerRequestWrapper.getOfferRequest().getCreditRisk()) 
					&& Objects.nonNull(offer.getAttributes().getCreditRisk()) 
					&& (offer.getAttributes().getCreditRisk().contains(offerRequestWrapper.getOfferRequest().getCreditRisk()))) 
					)	
				
				||
				
			(   Objects.isNull(offer.getAttributes().getTreatmentCode()))  ||
					( 
					Objects.nonNull(offerRequestWrapper.getOfferRequest().getTreatmentCode()) 
					&& Objects.nonNull(offer.getAttributes().getTreatmentCode()) 
					&& (offer.getAttributes().getTreatmentCode().contains(offerRequestWrapper.getOfferRequest().getTreatmentCode()))
					) 	

		  )
		).collect(Collectors.toList());
	}
	
	//customerType will be NULL/No Value for sales channel directvOnline
	//customerType will be Acquisition for sales channel opus
	/*
	public List<CTOffer> filterDOFeeOffersBasedOnCustomerType(CTOfferResponse ctOfferResponse) {
		return ctOfferResponse.getOffers().stream().filter(offer -> Objects.nonNull(offer.getAttributes()))
				.filter(offer -> (null == offer.getAttributes().getCustomerType()
						|| Constants.ACQUISITION.equalsIgnoreCase(offer.getAttributes().getCustomerType())))
				.collect(Collectors.toList());
	}
	*/

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
        if ((null != ctOfferResponse) && ctOfferResponse.getOffers().size() > 0) {
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
		if ((null != ctOfferResponse) && ctOfferResponse.getOffers().size() > 0) {
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
		boolean eligible = false;
		List<String> offerCodes = Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferCodes()).isPresent()
				? offerRequestWrapper.getOfferRequest().getOfferCodes()
				: new ArrayList<String>();
		eligible = isDateWithinEligibilityWindow(offerRequestWrapper.getOfferRequest().getServiceEndDate(),
				offerRequestWrapper.getOfferRequest().getSalesChannel(), offerProductType, offerCodes);
		log.info("==================eligibleToReconnect {}", eligible);
		return eligible;
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
     * @param associatedProducts
     *
     * @return qualifyingProducts
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
        if (null!=offerActionType && (offerActionType.contains(Constants.ACQUISITION_ACTION_TYPE) || offerActionType.contains(Constants.CLOSING_ACTION_TYPE) || offerActionType.contains(Constants.SOS_ACTION_TYPE))) {
            modifiedSalesChannel = salesChannel.get(0) + "Sales";
        } else {
            modifiedSalesChannel = salesChannel.get(0) + "Services";
        }
        return modifiedSalesChannel;
    }

    public void processOffers(CTOfferResponse ctOfferResponse, String modifiedSalesChannel) {

        ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer ->{
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
                                            
                                            /*if(Optional.ofNullable(ctOffer.getAttributes().getDisplayTypeByKey()).isPresent()) {
                                            	ctOffer.getAttributes().getDisplayTypeByKey().stream().filter(Objects::nonNull).forEach(displayType -> {
                                					if (displayType.getDisplayTypeKey().equalsIgnoreCase(modifiedSalesChannel)) {
                                						variant.getAttributes().setDisplayType(displayType.getDisplayTypeValue());
                                					}
                                				});
                                            }*/
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
    public void filterOfferBasedOnZipOrDMA(List<CTOffer> finalOfferList , OfferRequestWrapper offerRequestWrapper){
        CustomerEligibility customerEligibility = offerRequestWrapper.getOfferRequest().getCustomerEligibility();
        List<String> defaultZipCode = redisCacheHelper.getValues(Constants.DEFAULT_ZIPCODE, Constants.OTT);
        List<String> defaultDma = redisCacheHelper.getValues(Constants.DEFAULT_DMA, Constants.OTT);
        if (Objects.isNull(customerEligibility) || (Objects.nonNull(customerEligibility) && CollectionUtils.isEmpty(customerEligibility.getZipCode()))) {
            if(Objects.isNull(customerEligibility)){
                customerEligibility = new CustomerEligibility();
            }
            customerEligibility.setZipCode(defaultZipCode);
            customerEligibility.setDma(defaultDma);
            offerRequestWrapper.getOfferRequest().setCustomerEligibility(customerEligibility);
            filterOfferBasedOnZipOrDMA(finalOfferList, offerRequestWrapper.getOfferRequest());
        } else if (Objects.nonNull(customerEligibility)  && CollectionUtils.isNotEmpty(customerEligibility.getZipCode()) && CollectionUtils.isEmpty(customerEligibility.getFipsCode())) {
            customerEligibility.setDma(defaultDma);
            offerRequestWrapper.getOfferRequest().setCustomerEligibility(customerEligibility);
            filterOfferBasedOnZipOrDMA(finalOfferList, offerRequestWrapper.getOfferRequest());
        } else if (Objects.nonNull(customerEligibility)  && CollectionUtils.isNotEmpty(customerEligibility.getZipCode()) && CollectionUtils.isNotEmpty(customerEligibility.getFipsCode())) {
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

    // BYODOPC-5029: Reconnect offer  is applicable to the both sale(Acquisition) and Services(others)
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
            log.info("Acquisition/others and not reconnect for " + ctOfferRequest.getOfferProductType().toString());
            filterOffersBasedOnCustomerTypeAcquisition(offerResponse);
        } else if (isReconnectAndGreaterThanEligibleMonthsFlag) {
            log.info("Acquisition/others and reconnect > eligible months for " + ctOfferRequest.getOfferProductType().toString());
            filterOffersBasedOnCustomerTypeReconnectGreaterThanEligibleMonths(offerResponse, ctOfferRequest);
        } else if (isReconnectAndLessThanEligibleMonthsFlag) {
            log.info("Acquisition/others and reconnect < eligible months for " + ctOfferRequest.getOfferProductType().toString());
            filterOffersBasedOnCustomerTypeReconnect(offerResponse, ctOfferRequest);
        }
    }

    // BYODOPC-5029: Reconnect offer is applicable to the both sale(Acquisition) and Services(others)
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

    // BYODOPC-5029: Reconnect offer  is applicable to the both sale(Acquisition) and Services(others)
    private boolean isEligibleForReconnectFlow(CTOfferRequest ctOfferRequest, boolean isLessThanEligibleMonths) {
        log.debug("isEligibleForReconnectFlow() start");
        if (ctOfferRequest == null) {
            return false;
        }
        List<String> offerActionType = Optional.ofNullable(ctOfferRequest.getOfferActionType()).orElse(Collections.emptyList());
        Boolean isReconnectCustomer = Optional.ofNullable(ctOfferRequest.isReconnectCustomer()).orElse(false);
        if (!isReconnectCustomer || offerActionType.isEmpty() ||
                !(offerActionType.contains(Constants.ACQUISITION) || offerActionType.contains(Constants.UPSELL_ACTION_TYPE)
                        || offerActionType.contains(Constants.OTHER_ACTION_TYPE))) {
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
        if ((null != ctOfferResponse) && ctOfferResponse.getOffers().size() > 0) {
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

    /**
     *  BYODOPC-5029: Reconnect offer  is applicable to the both sale(Acquisition) and Services(others)
     * Filters the offers in the given {@code CTOfferResponse} based on whether the customer is in a reconnect flow.
     *
     * @param ctOfferResponse the response object whose offer list is filtered in-place
     * @param reconnectOrNot  {@code true} if the customer is in a reconnect flow within the eligibility window;
     *                        {@code false} for a regular (non-reconnect) services flow
     */
    public void checkCustomerTypeReconnectOrNot(CTOfferResponse ctOfferResponse, boolean reconnectOrNot) {
        log.debug("checkCustomerTypeReconnectOrNot() start");
        if ((null != ctOfferResponse) && !ctOfferResponse.getOffers().isEmpty()) {
            List<CTOffer> filteredOffers = new ArrayList<>();
            ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(offer -> {
                if (reconnectOrNot) {
                    // Reconnect flow: include only offers explicitly tagged for reconnect customers
                    if (null != offer.getAttributes().getCustomerTypes() && offer.getAttributes().getCustomerTypes().contains(Constants.RECONNECT)) {
                        filteredOffers.add(offer);
                    }
                } else {
                    // Non-reconnect services flow: include only offers with no customer type restriction (null = applicable to all)
                    if (null == offer.getAttributes().getCustomerTypes()) {
                        filteredOffers.add(offer);
                    }
                }
            });
            ctOfferResponse.setOffers(filteredOffers);
            ctOfferResponse.setCount(filteredOffers.size());
            ctOfferResponse.setTotal(filteredOffers.size());
        }
        log.debug("checkCustomerTypeReconnectOrNot() end");
    }

    public void filterOffersBasedOnCustomerTypeReconnect(CTOfferResponse ctOfferResponse,
                                                         CTOfferRequest ctOfferRequest) {
        log.debug("filterOffersBasedOnCustomerTypeReconnect() start");
        if ((null != ctOfferResponse) && ctOfferResponse.getOffers().size() > 0) {
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

    public void filterOffersBasedOnEligibleIapPartners(CTOfferResponse ctOfferResponse,OfferRequestWrapper offerRequestWrapper) {
        if(featureHelper.isEnabled(Constants.FEATURE_ELIGIBLE_IAP_PARTNERS_ENABLED)
                && offerRequestWrapper.getOfferRequest()!=null && offerRequestWrapper.getOfferRequest().getContractIndicator()!=null && !offerRequestWrapper.getOfferRequest().getContractIndicator().get(0).equalsIgnoreCase(Constants.EDSP_STRING)
                && offerRequestWrapper.getOfferRequest().getSalesChannel()!=null && !offerRequestWrapper.getOfferRequest().getSalesChannel().get(0).equalsIgnoreCase(Constants.OEM_IAPFIRETV))
        {
            String iapPartnerAccountType=  getIapPartnerAccountType(offerRequestWrapper.getOfferRequest());
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

            CTOfferResponse offerResponse = epochClient.getOffersFromCache(ctOfferRequest, Arrays.asList("offerProductType"));
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

    public  void filterOffersBasedOnEligibleIapPartners(List<CTOffer> getOffer, OfferRequestWrapper offerRequestWrapper) {
        if(featureHelper.isEnabled(Constants.FEATURE_ELIGIBLE_IAP_PARTNERS_ENABLED)
                && offerRequestWrapper.getOfferRequest()!=null && offerRequestWrapper.getOfferRequest().getContractIndicator()!=null && !offerRequestWrapper.getOfferRequest().getContractIndicator().get(0).equalsIgnoreCase(Constants.EDSP_STRING)
                && offerRequestWrapper.getOfferRequest().getSalesChannel()!=null && !offerRequestWrapper.getOfferRequest().getSalesChannel().get(0).equalsIgnoreCase(Constants.OEM_IAPFIRETV))
        {
            String iapPartnerAccountType=  getIapPartnerAccountType(offerRequestWrapper.getOfferRequest());
            Optional.ofNullable(getOffer)
                    .filter(offers -> !offers.isEmpty())
                    .ifPresent(offers -> {
                        List<CTOffer> filteredOffers = offers.stream()
                                .filter(Objects::nonNull)
                                .filter(offer -> {
                                    if (iapPartnerAccountType != null && CollectionUtils.isNotEmpty(offer.getAttributes().getEligibleIAPPartners())) {
                                        return
                                                offer.getAttributes().getEligibleIAPPartners().stream().anyMatch(partner -> partner.equalsIgnoreCase(iapPartnerAccountType));
                                    }
                                    else if (iapPartnerAccountType == null && CollectionUtils.isNotEmpty(offer.getAttributes().getEligibleIAPPartners())) {
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

    /**
     *
     * @param key
     * @return
     */
    public List<String> getConfigListValues(String key) {
        List<String> configValues = redisCacheHelper.getValues(key, Constants.OTT);
        if (CollectionUtils.isNotEmpty(configValues)) {
            List<String> parsedValues = configValues.stream()
                    .filter(Objects::nonNull)
                    .flatMap(value -> Arrays.stream(value.split("\\s*,\\s*")))
                    .map(String::trim)
                    .map(value -> value.replace("[", "").replace("]", "").replace("\"", ""))
                    .map(String::trim)
                    .filter(org.springframework.util.StringUtils::hasText)
                    .collect(Collectors.toList());
            return parsedValues;
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
                // First pass: extract messageType regardless of its position in the group.
                String messageType = null;
                for (var message : messageGroup) {
                    if (Constants.MESSAGES_BY_KEY_MESSAGE_TYPE.equals(message.getName())) {
                        var value = message.getValue();
                        if (value instanceof Map) {
                            var keyObj = ((Map<?, ?>) value).get(Constants.KEY);
                            if (keyObj != null) messageType = keyObj.toString();
                        }
                        break;
                    }
                }
                // Second pass: process allMessages using the extracted messageType.
                for (var message : messageGroup) {
                    if (Constants.MESSAGES_BY_KEY_ALL_MESSAGES.equals(message.getName())) {
                        for (var msgArr : (List<?>) message.getValue()) {
                            var messagesMap = new HashMap<String, String>();
                            messagesMap.put(Constants.MESSAGES_BY_KEY_MESSAGE_TYPE, messageType);
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
     * Evaluate All Rules for SwimLane defined in CT against CheckEligibilityRequest.
     * @param req
     * @param eligList
     * @param errorMsgs
     * @return
     */ 
    public CheckEligibiltyResponse evaluateEligibility(CheckEligibilityRequest req, List<com.dtv.dcp.epoch.model.eligibility.Eligibility> eligList, List<EligibilityErrorMessage> errorMsgs) {
        CheckEligibiltyResponse resp = new CheckEligibiltyResponse();
        log.info("evaluateEligibility() Method start");

        SwimlaneEligibilityDetails swimlaneDetails = new SwimlaneEligibilityDetails();
        //If Swimlane Eligibility is not defined in CT, return true
        if (eligList == null || eligList.isEmpty()) {
        	swimlaneDetails = new SwimlaneEligibilityDetails();
            swimlaneDetails.setSwimlaneSwitchEligible(true);
            resp.setSwimlaneEligibilityDetails(swimlaneDetails);
            return resp;
        }
        ExistingProductFamily existingProductFamily= Optional.ofNullable(req.getCustomerContext())
                .map(CustomerContext::getExistingProductFamily)
                .filter(familyList -> !familyList.isEmpty())
                .map(familyList -> familyList.get(0))
                .orElse(null);
        VolPauseDetails volPauseDetails =null;
        String gracePeriodEndDate=null;
        AdditionalAccountDetails additionalAccountDetails = Optional.ofNullable(req.getCustomerContext().getAdditionalAccountDetails())
                .orElseGet(AdditionalAccountDetails::new);
        req.getCustomerContext().setAdditionalAccountDetails(additionalAccountDetails);
        if(existingProductFamily!=null && additionalAccountDetails!=null  && additionalAccountDetails.getStatus()==null) {
             gracePeriodEndDate= existingProductFamily.getGracePeriodEndDate();
             if(gracePeriodEndDate!=null) {
                 additionalAccountDetails.setStatus(Constants.IN_ELIGIBLEACCOUNT_GRACE_PERIOD);
             }
            if(Optional.ofNullable(existingProductFamily.getVolPauseDetails()).isPresent())
            {
                 volPauseDetails = existingProductFamily.getVolPauseDetails().get(0);
                if (volPauseDetails!=null  ) {
                    additionalAccountDetails.setStatus(volPauseDetails.getStatus());
                }
            }
            if(existingProductFamily.getProjectedBillDate())
            {
                additionalAccountDetails.setStatus(Constants.BRE);
            }
           else if( existingProductFamily.getPendingSwimlaneSwitch())
            {
                additionalAccountDetails.setStatus(Constants.PENDING_SWIMLANE);
            }
            if(existingProductFamily.getTenureType()!=null && existingProductFamily.getTenureType().equalsIgnoreCase(Constants.IN_ELIGIBLEACCOUNT_PENDING_GOODBYE))
            {
                additionalAccountDetails.setStatus(Constants.IN_ELIGIBLEACCOUNT_PENDING_DISCONNECT);
            }
            if ( CollectionUtils.isNotEmpty(existingProductFamily.getAdditionalDetails()) && additionalAccountDetails!=null && additionalAccountDetails.getStatus()==null) {
                for (AdditionalDetails details : existingProductFamily.getAdditionalDetails()) {
                    if ((details.getAdditionalInfoName().equalsIgnoreCase(Constants.SERVICE_SUSPENDED_INFO)
                            || details.getAdditionalInfoName().equalsIgnoreCase(Constants.SERVICE_SUSPENDED_INFO_UPPERCASE))
                            && CollectionUtils.isNotEmpty(details.getParams())
                            && (details.getParams().stream().filter(Objects::nonNull)
                            .anyMatch(d -> d.getParamValue().equalsIgnoreCase(Constants.SUSPEND))
                            || details.getParams().stream().filter(Objects::nonNull)
                            .anyMatch(d -> d.getParamValue().equalsIgnoreCase(Constants.PENDING_SUSPEND)))) {
                        additionalAccountDetails.setStatus(Constants.IN_ELIGIBLEACCOUNT_SUSPEND);
                       break;
                    }
                    else if ((details.getAdditionalInfoName().equalsIgnoreCase(Constants.FREE_TRIAL_DETAILS)
                            || details.getAdditionalInfoName().equalsIgnoreCase(Constants.FREE_TRIAL_DETAILS_UPPERCASE)
                            && CollectionUtils.isNotEmpty(details.getParams())
                            && (details.getParams().stream().filter(Objects::nonNull)
                            .anyMatch(d -> (d.getParamValue().equalsIgnoreCase(Constants.ACTIVE))
                                    && (d.getParamName().equalsIgnoreCase("status")))))) {
                        additionalAccountDetails.setStatus(Constants.FREE_TRIAL);
                       break;
                    }

                }
            }

        }
        //If isPendingSwimlaneSwitch is sent as True in Request update the status to Pending Swimlane inside Additional Account details
       if(req.getCustomerContext()!=null && req.getCustomerContext().getOtt() !=null
                && req.getCustomerContext().getOtt().getIsProjectedBillDate()!=null && req.getCustomerContext().getOtt().getIsProjectedBillDate())
       {
           additionalAccountDetails.setStatus(Constants.BRE);
       }
       else  if (req.getCustomerContext()!=null && req.getCustomerContext().getOtt() !=null
              && req.getCustomerContext().getOtt().getPendingSwimlaneSwitch()!=null &&
               req.getCustomerContext().getOtt().getPendingSwimlaneSwitch()) {
           additionalAccountDetails.setStatus(Constants.PENDING_SWIMLANE);
        }
        if(req.getCustomerContext()!=null && req.getCustomerContext().getOtt() !=null
                && req.getCustomerContext().getOtt().getTenureType()!=null && req.getCustomerContext().getOtt().getTenureType().equalsIgnoreCase(Constants.IN_ELIGIBLEACCOUNT_PENDING_GOODBYE))
        {
            additionalAccountDetails.setStatus(Constants.IN_ELIGIBLEACCOUNT_PENDING_DISCONNECT);
        }
        if (Objects.nonNull(req.getCustomerContext())
                && Objects.nonNull(req.getCustomerContext().getAdditionalAccountDetails())
                && CollectionUtils.isNotEmpty(req.getCustomerContext().getAdditionalAccountDetails().getAdditonalInfo())) {
            for (AdditionalDetails details : req.getCustomerContext().getAdditionalAccountDetails().getAdditonalInfo()) {
                // Check if the additional details match the criteria for a free trial
                if ((details.getName().equalsIgnoreCase(Constants.FREE_TRIAL_DETAILS)
                        || details.getName().equalsIgnoreCase(Constants.FREE_TRIAL_DETAILS_UPPERCASE))
                        && CollectionUtils.isNotEmpty(details.getParams())
                        && (details.getParams().stream().filter(Objects::nonNull)
                        .anyMatch(d -> (d.getParamValue().equalsIgnoreCase(Constants.ACTIVE))
                                && (d.getParamName().equalsIgnoreCase("status")))
                )) {
                    additionalAccountDetails.setStatus(Constants.FREE_TRIAL);
                    break;
                }

            }
        }

            //Initialize Swimlane Switch Eligibility to false. It will be set to true if one of the rule matches
        swimlaneDetails.setSwimlaneSwitchEligible(false);
        // This List will hold the error messages for the failed rules
        List<Map<String, List<String>>> errorRules = new ArrayList<>();
 
        for (com.dtv.dcp.epoch.model.eligibility.Eligibility eligibility : eligList) {
        	// This List will hold the error messages for the failed rules
        	List<Map<String, List<String>>> failedErrorRules = new ArrayList<>();
            boolean keyEligibility = evaluateEligibilityKey(req, eligibility, failedErrorRules, errorMsgs);
            boolean customerEligibility =evaluateCustomerEligibility(keyEligibility, req, eligibility, failedErrorRules, errorMsgs);
            boolean channelEligible = evaluateChannelEligibility(keyEligibility, req, eligibility, failedErrorRules, errorMsgs);
            boolean agentEligibility =evaluateAgentEligibility(channelEligible, req, eligibility, failedErrorRules, errorMsgs);
            // Call populateDerivedFrom to check for the rules which are derived based on other rules
            populateDerivedFrom(errorMsgs, failedErrorRules);
            // Add the local failed to the final list of Errors
            errorRules.addAll(failedErrorRules);
            // If all conditions of a Rule matches, break the loop
            if(keyEligibility && customerEligibility && channelEligible && agentEligibility) {
            	errorRules = new ArrayList<>();
            	break;
            }
        }
 
        if (errorRules.isEmpty()) {
            swimlaneDetails = new SwimlaneEligibilityDetails();
            swimlaneDetails.setSwimlaneSwitchEligible(true);
        } else {
        	
            setErrorMessageBasedonCriticality(errorRules, swimlaneDetails);
        }
 
        resp.setSwimlaneEligibilityDetails(swimlaneDetails);
        log.info("evaluateEligibility() Method End");
        return resp;
    }
 
    /**
     * Retrieves the error message based on the "criticality" of the error rules. Initially the maxCriticality is set to Integer.MIN_VALUE.
     * It iterates through the error rules, finds the one with the "highest criticality", and sets the error message and code in the SwimlaneEligibilityDetails.
     * @param errorRules
     * @param swimlaneDetails
     */
    private void setErrorMessageBasedonCriticality(List<Map<String, List<String>>> errorRules, SwimlaneEligibilityDetails swimlaneDetails) {
        Map<String, List<String>> highestCriticalityRule = null;
        int maxCriticality = Integer.MIN_VALUE;
 
        for (Map<String, List<String>> errorRule : errorRules) {
            int criticality = errorRule.getOrDefault(Constants.CRITICALITY, Collections.emptyList()).stream()
                    .findFirst()
                    .map(Integer::parseInt)
                    .orElse(Integer.MIN_VALUE);
 
            if (criticality > maxCriticality) {
                maxCriticality = criticality;
                highestCriticalityRule = errorRule;
            }
        }
 
        if (highestCriticalityRule != null) {
            swimlaneDetails.setSlsIneligibleReasonMsg(highestCriticalityRule.get(Constants.ERROR_MESSAGE).get(0));
            swimlaneDetails.setSlsIneligibleReasonCode(highestCriticalityRule.get(Constants.ERROR_CODE).get(0));
        }
    }
 
    /**
     * There are specific rules which will not have attributeList defined as they are not based on specific rule attributes 
     * instead are derived based on other rules defined in derivedFrom attribute.
     * @param errorMsgs
     * @param errorRules
     */
    private void populateDerivedFrom(List<EligibilityErrorMessage> errorMsgs, List<Map<String, List<String>>> errorRules) {
        List<Map<String, List<String>>> matchingMaps = errorMsgs.stream()
                .filter(msg -> msg.getEligibilityErrorMessage() != null)
                .flatMap(msg -> msg.getEligibilityErrorMessage().stream())
                .filter(map -> map.containsKey(Constants.DERIVED_FROM) && map.get(Constants.DERIVED_FROM) != null && !map.get(Constants.DERIVED_FROM).isEmpty())
                .collect(Collectors.toList());
 
        for (Map<String, List<String>> matchingMap : matchingMaps) {
            List<String> derivedAttrs = matchingMap.get(Constants.DERIVED_FROM);
            List<String> errorCodes = errorRules.stream()
                    .filter(rule -> rule.get(Constants.ERROR_CODE) != null)
                    .flatMap(rule -> rule.get(Constants.ERROR_CODE).stream())
                    .collect(Collectors.toList());
 
            if (derivedAttrs != null && errorCodes.containsAll(derivedAttrs)) {
            	errorRules.clear();
                errorRules.add(matchingMap);
            }
        }
    }
 
    /**
     * Retrieves the error message for a specific attribute from the list of error messages.
     * @param failedErrorMessages
     * @param errorMsgs
     * @param attribute
     */
    private void addErrorMessage(List<Map<String, List<String>>> failedErrorMessages, List<EligibilityErrorMessage> errorMsgs, String attribute) {
        errorMsgs.stream()
                .filter(msg -> msg.getEligibilityErrorMessage() != null)
                .flatMap(msg -> msg.getEligibilityErrorMessage().stream())
                .filter(map -> map.getOrDefault(Constants.ATTRIBUTE_LIST, Collections.emptyList()).contains(attribute))
                .findFirst()
                .ifPresent(failedErrorMessages::add);
    }
 
private boolean evaluateEligibilityKey(CheckEligibilityRequest req, com.dtv.dcp.epoch.model.eligibility.Eligibility eligibility, List<Map<String, List<String>>> failedErrorMessages, List<EligibilityErrorMessage> errorMsgs) {
    log.info("evaluateEligibilityKey() Method start");
        // Key Eligibility from CT, if nothing is defined return true
        Map<String, List<String>> keys = eligibility.getKeyEvaluation();
        if (keys == null) return true;
 
        // Sales Channel from Request
        String salesChannel = req.getSalesChannel();
        boolean isSalesChannelValid = Optional.ofNullable(keys.get(Constants.ELIGIBILITY_SALES_CHANNEL))
                .map(list -> list.contains(salesChannel))
                .orElse(false);
 
        // If Device Eligibility is required by the keys, evaluate device eligibility; otherwise ignore device check
        boolean deviceEligibility = false;
        if (keys.containsKey(Constants.DEVICE_ELIGIBILITY)  ) {
            CustomerContext ctx = req.getCustomerContext();
            if (ctx != null && ctx.getOtt() != null && ctx.getOtt().getProducts() != null) {

                deviceEligibility = ctx.getOtt().getProducts().stream()
                        .filter(Objects::nonNull)
                        .anyMatch(p -> Constants.VIDEO_DEVICE.equalsIgnoreCase(p.getProductType()));
                boolean containsNodevice = Optional.ofNullable(keys.get(Constants.DEVICE_ELIGIBILITY))
                        .map(list -> list.stream().anyMatch(value -> value.equalsIgnoreCase(Constants.DEVICE_ELIGIBILITY_NODEVICE)))
                        .orElse(false);
                deviceEligibility=(deviceEligibility && !containsNodevice) || (!deviceEligibility && containsNodevice);

            } else {
                deviceEligibility = true;
            }
        }
        else {
            deviceEligibility=true;
        }

        // Determine final result:
        // - if DEVICE_ELIGIBILITY is present, require both sales channel and device eligibility
        // - otherwise, require only sales channel validity
        boolean result =
               isSalesChannelValid && deviceEligibility;

 
        if (!result) {
            addErrorMessage(failedErrorMessages, errorMsgs, Constants.ELIGIBILITY_KEY_EVALUATION);
        }
    log.info("evaluateEligibilityKey() Method End");
        return result;
    }
 
    /**
     * Evaluate Customer Eligibility
     * @param keyEligibility
     * @param req
     * @param eligibility
     * @param failedErrorMessages
     * @param errorMsgs
     * @return
     */
    private boolean evaluateCustomerEligibility(boolean keyEligibility, CheckEligibilityRequest req, com.dtv.dcp.epoch.model.eligibility.Eligibility eligibility, List<Map<String, List<String>>> failedErrorMessages, List<EligibilityErrorMessage> errorMsgs) {
        log.info("evaluateCustomerEligibility() Method start");
        //Key Eligibility is false, no need to check Customer Eligibility
    	if (!keyEligibility) return false;
 
        //Customer Eligibility from CT, If nothing is defined return true
        Map<String, List<String>> custElig = eligibility.getCustomerEligibility();
        if (custElig == null || custElig.isEmpty()) return true;
 
        boolean hasRelevantKeys = Stream.of(Constants.ELIGIBILITY_PACKAGES, Constants.CUSTOMER_SUBSCRIPTION_TYPE,Constants.CONTRACTINDICATOR, Constants.ELIGIBILITY_DEVICES, Constants.IN_ELIGIBILE_ACCOUNT_STATUS)
                .anyMatch(custElig::containsKey);
        if (!hasRelevantKeys) return true;
 
        //Customer Eligibility from Request/ If no CustomerSubscriptionType is present, default to "DEFAULT"
        String subType = Optional.ofNullable(req.getCustomerSubscriptionType()).orElse(Constants.SLS_DEFAULT);
        String contractIndicator = Optional.ofNullable(req.getContractIndicator()).map(l -> l.get(0)).orElse("");
        List<ProductInfo> products = Optional.ofNullable(req.getCustomerContext()).map(CustomerContext::getOtt).map(CartProduct::getProducts).orElse(Collections.emptyList());
        AdditionalAccountDetails details = Optional.ofNullable(req.getCustomerContext()).map(CustomerContext::getAdditionalAccountDetails).orElse(new AdditionalAccountDetails());

        Set<String> eligiblePackages = new HashSet<>(custElig.getOrDefault(Constants.ELIGIBILITY_PACKAGES, Collections.emptyList()));
        List<String> videoDeviceCodes = products.stream()
                .filter(p -> Constants.VIDEO_DEVICE.equalsIgnoreCase(p.getProductType()))
                .map(ProductInfo::getProductCode)
                .collect(Collectors.toList());

       //Check Customer Eligibility for Contract Indicator, Subscription Type, Eligible Packages, and Account Status
        boolean isEligible = (!custElig.containsKey(Constants.CONTRACTINDICATOR) || custElig.get(Constants.CONTRACTINDICATOR).contains(contractIndicator)) &&
                (!custElig.containsKey(Constants.CUSTOMER_SUBSCRIPTION_TYPE) || custElig.get(Constants.CUSTOMER_SUBSCRIPTION_TYPE).contains(subType)) &&
                (!custElig.containsKey(Constants.ELIGIBILITY_PACKAGES) || eligiblePackages.isEmpty() ||
                        products.stream().map(ProductInfo::getProductCode).anyMatch(eligiblePackages::contains)) &&
                (!custElig.containsKey(Constants.IN_ELIGIBILE_ACCOUNT_STATUS) || (!custElig.get(Constants.IN_ELIGIBILE_ACCOUNT_STATUS).stream()
                        .anyMatch(status -> status.equalsIgnoreCase(details.getStatus()))));

       //Check if the customer is eligible for video devices
        if (isEligible && custElig.containsKey(Constants.ELIGIBILITY_DEVICES)) {
            isEligible = videoDeviceCodes.stream().anyMatch(custElig.get(Constants.ELIGIBILITY_DEVICES)::contains);
        }

        if (!isEligible) {
            addErrorMessage(failedErrorMessages, errorMsgs, Constants.CUSTOMER_ELIGIBILITY);
        }
        log.info("evaluateCustomerEligibility() Method End");
        return isEligible;
    }
    
    /**
     * Evaluate Channel Eligibilty
     * @param keyEligibility
     * @param req
     * @param eligibility
     * @param failedErrorMessages
     * @param errorMsgs
     * @return
     */
    private boolean evaluateChannelEligibility(boolean keyEligibility, CheckEligibilityRequest req, com.dtv.dcp.epoch.model.eligibility.Eligibility eligibility, List<Map<String, List<String>>> failedErrorMessages, List<EligibilityErrorMessage> errorMsgs) {
        log.info("evaluateChannelEligibility() Method start");
        //Key Eligibility is false, no need to check Channel Eligibility
    	if (!keyEligibility) return false;
 
    	//Channel Eligibility from CT, If nothing is defined return true
        Map<String, List<String>> channelDetails = eligibility.getChannelDetails();
        if (channelDetails == null || channelDetails.isEmpty() || req.getChannelEligibility() == null) return true;
 
        //Channel Eligibility from Request
        ChannelEligibility ce = req.getChannelEligibility();
        boolean isEligible = (channelDetails.containsKey(Constants.ELIGIBILITY_CHANNEL) && channelDetails.get(Constants.ELIGIBILITY_CHANNEL).contains(ce.getSalesChannel())) ||
                (channelDetails.containsKey(Constants.ELIGIBILITY_SUB_CHANNEL) && channelDetails.get(Constants.ELIGIBILITY_SUB_CHANNEL).contains(ce.getSalesSubChannel())) ||
                (channelDetails.containsKey(Constants.ELIGIBILITY_SITEIDS) && channelDetails.get(Constants.ELIGIBILITY_SITEIDS).contains(ce.getSalesSiteId()));
 
        if (!isEligible) {
            addErrorMessage(failedErrorMessages, errorMsgs, Constants.CHANNEL_ELIGIBILITY_DETAILS);
        }
        log.info("evaluateChannelEligibility() Method End");
        return isEligible;
    }
 
    /**
     * Evaluate Agent Eligibility
     * @param channelEligible
     * @param req
     * @param eligibility
     * @param failedErrorMessages
     * @param errorMsgs
     * @return
     */
    private boolean evaluateAgentEligibility(boolean channelEligible, CheckEligibilityRequest req, com.dtv.dcp.epoch.model.eligibility.Eligibility  eligibility, List<Map<String, List<String>>> failedErrorMessages, List<EligibilityErrorMessage> errorMsgs) {
        log.info("evaluateAgentEligibility() Method start");
        //Channel Eligibility is false, no need to check Agent Eligibility
    	if (!channelEligible) return false;
 
    	//Agent Eligibility from CT, If nothing is defined return true
        List<Map<String, List<String>>> agentElig = eligibility.getAgentEligibility();
        if (agentElig == null || agentElig.isEmpty()) return true;
 
        //Agent Eligibility from Request
        AgentDealerDetails agent = req.getAgentDealerDetails();
        if (agent == null) return false;
 
        //Agent Eligibility check for agentRole or agentId
        String role = Optional.ofNullable(agent.getAgentRole()).orElse("");
        String id = Optional.ofNullable(agent.getAgentId()).orElse("");
        String dealerCode = Optional.ofNullable(agent.getDealerCode()).orElse("");
        boolean isEligible = agentElig.stream()
                .anyMatch(map -> (!map.containsKey(Constants.AGENT_ROLE) || map.get(Constants.AGENT_ROLE).contains(role))
                        && (!map.containsKey(Constants.AGENT_ID) || map.get(Constants.AGENT_ID).contains(id))
                        && (!map.containsKey(Constants.AGENT_DEALERCODE) || map.get(Constants.AGENT_DEALERCODE).contains(dealerCode)));
 
        if (!isEligible) {
            addErrorMessage(failedErrorMessages, errorMsgs, Constants.AGENT_ELIGIBILITY);
        }
        log.info("evaluateAgentEligibility() Method End");
        return isEligible;
    }

    public boolean skipProductsOnAccountTSuppressForSLS(CTOfferRequest ctOfferRequest) {
        // SLS-IXP-FLAG changes
        return  (isSlsGetOfferServicesEnabled() || isVCSLSServicesFlowEnabled())
                && isServiceCT(ctOfferRequest)
                && ctOfferRequest.getContractIndicator() != null
                && isUniversalCohort(ctOfferRequest.getContractIndicator())
                && ctOfferRequest.getOfferProductType().stream()
                .anyMatch(type -> type.equalsIgnoreCase(Constants.VIDEO_ADDON)
                        || type.equalsIgnoreCase(Constants.VIDEO_DEVICE)
                        || type.equalsIgnoreCase(Constants.PROTECTION_PLAN));
    }

    public CTOfferResponse filterProductsOnAccountToSuppressSLSOffer(
            OfferRequestWrapper offerRequestWrapper,
            CTOfferRequest ctOfferRequest,
            CTOfferResponse ctOfferResponse) {

        // Set default flow intents based on keys in flowIntentSubscriptionMap
        Map<String, List<String>> flowIntentSubscriptionMap = offerRequestWrapper.getSlsCombinationMap() != null ? offerRequestWrapper.getSlsCombinationMap() : new HashMap<>();

        // IMPORTANT:
        // We intentionally keep allOffers immutable in this method.
        // Earlier logic removed flow intents/subscription types directly on source offers,
        // which caused cross-flow side effects (offer suppressed in one pass but re-picked later).
        List<CTOffer> allOffers = new ArrayList<>(ctOfferResponse.getOffers());

        // Accumulator keyed by offer id/code.
        // Only offers that are INCLUDED for at least one (flowIntent, subscriptionType)
        // are inserted here. If never inserted, the offer is completely filtered out.
        Map<String, CTOffer> reducedOffersById = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> flowIntentEntry : flowIntentSubscriptionMap.entrySet()) {
            String flowIntent = flowIntentEntry.getKey();
            List<String> subscriptionTypes = flowIntentEntry.getValue();

            //log.info("Filtering offers for flowIntent: {} & subscriptionTypes: {} & salesChannel: {} & contractIndicator: {}",
            //        flowIntent, subscriptionTypes, ctOfferRequest.getSalesChannel(), ctOfferRequest.getContractIndicator());

            // Filter eligible offers based on flow intent and subscription type
            List<CTOffer> eligibleOffers = allOffers.stream()
                    .filter(offer -> checkSLSOfferInlaneSwimlaneEligbility(offer, flowIntent, subscriptionTypes))
                    .collect(Collectors.toList());

            for (CTOffer eligibleOffer : eligibleOffers) {
                // Gate decision only (no source-offer mutation).
                if (processSLSOffersBasedOnProductOnAccountToSuppress(
                        ctOfferRequest, eligibleOffer, flowIntent, subscriptionTypes)) {
                    List<String> matchedSubscriptionTypes = getMatchedSubscriptionTypesForFlow(eligibleOffer, subscriptionTypes);
                    if (CollectionUtils.isNotEmpty(matchedSubscriptionTypes)) {
                        // Build reduced final copy with only allowed flow intents/subscription types.
                        accumulateReducedSLSOffer(reducedOffersById, eligibleOffer, flowIntent, matchedSubscriptionTypes);
                    }
                }
            }
        }

        // Final response is strictly derived from accumulated reduced copies.
        // Any source offer absent in this map is considered fully suppressed/filtered out.
        List<CTOffer> filteredOffers = new ArrayList<>(reducedOffersById.values());

        ctOfferResponse.setOffers(filteredOffers);
        ctOfferResponse.setCount(filteredOffers.size());
        ctOfferResponse.setTotal(filteredOffers.size());
        return ctOfferResponse;
    }

    // NOTE: Empty flowIntents is treated as applicable for both INLANE and SWIMLANE by design.
    private boolean checkSLSOfferInlaneSwimlaneEligbility(CTOffer offer, String flowIntent, List<String> subscriptionTypes) {
        return Objects.nonNull(offer.getAttributes())
                && (CollectionUtils.isEmpty(offer.getAttributes().getFlowIntents())
                || offer.getAttributes().getFlowIntents().stream()
                .anyMatch(intent -> intent.equalsIgnoreCase(flowIntent)))
                && CollectionUtils.isNotEmpty(offer.getAttributes().getServiceSubscriptionType())
                && CollectionUtils.isNotEmpty(subscriptionTypes)
                && offer.getAttributes().getServiceSubscriptionType().stream()
                        .anyMatch(type -> subscriptionTypes.stream().anyMatch(st -> st.equalsIgnoreCase(type)));
    }

    // IMPORTANT:
    // This method is intentionally a pure include/exclude decision.
    // It must not mutate offer.attributes.flowIntents/serviceSubscriptionType.
    private boolean processSLSOffersBasedOnProductOnAccountToSuppress(
            CTOfferRequest ctOfferRequest,
            CTOffer offer,
            String flowIntent,
            List<String> subscriptionTypes) {

        boolean skipSuppress = Boolean.TRUE.equals(offer.getAttributes().getSkipProductsOnAccountToSuppress());
        boolean isInlane = flowIntent.equalsIgnoreCase(Constants.INLANE);
        boolean isSwimlane = flowIntent.equalsIgnoreCase(Constants.SWIMLANE);
        boolean hasSuppressionList = offer.getAttributes().getProductsOnAccountToSuppressOffer() != null;
        boolean isSuppressed = hasSuppressionList && isProductOnAccountToSuppress(ctOfferRequest, offer);

        if (skipSuppress) {
            if (isInlane) {
                return true;
            } else if (isSwimlane) {
                if(!hasSuppressionList || !isSuppressed){
                    return true;
                }else{
                    return false;
                }

            }
        } else {
            if (isInlane ) {
                if(!hasSuppressionList || !isSuppressed){
                    return true;
                }else{
                    return false;
                }

            } else if (isSwimlane) {
                return true;
            }
        }

        return false;
    }

    private List<String> getMatchedSubscriptionTypesForFlow(CTOffer offer, List<String> subscriptionTypes) {
        if (Objects.isNull(offer) || Objects.isNull(offer.getAttributes())
                || CollectionUtils.isEmpty(offer.getAttributes().getServiceSubscriptionType())
                || CollectionUtils.isEmpty(subscriptionTypes)) {
            return Collections.emptyList();
        }

        List<String> matchedTypes = new ArrayList<>();
        for (String offerSubscriptionType : offer.getAttributes().getServiceSubscriptionType()) {
            if (subscriptionTypes.stream().anyMatch(st -> st.equalsIgnoreCase(offerSubscriptionType))
                    && matchedTypes.stream().noneMatch(type -> type.equalsIgnoreCase(offerSubscriptionType))) {
                matchedTypes.add(offerSubscriptionType);
            }
        }
        return matchedTypes;
    }

    // We accumulate onto a copied offer instance to avoid mutating shared source offers
    // that are re-evaluated across multiple flow-intent passes.
    private void accumulateReducedSLSOffer(Map<String, CTOffer> reducedOffersById,
                                           CTOffer sourceOffer,
                                           String flowIntent,
                                           List<String> matchedSubscriptionTypes) {

        String offerAccumulatorKey = getOfferAccumulatorKey(sourceOffer);
        CTOffer reducedOffer = reducedOffersById.computeIfAbsent(offerAccumulatorKey, key -> createReducedSLSOfferCopy(sourceOffer));

        if (Objects.isNull(reducedOffer.getAttributes())) {
            return;
        }
        List<String> reducedFlowIntents = Optional.ofNullable(reducedOffer.getAttributes().getFlowIntents())
                .orElseGet(ArrayList::new);
        if (reducedFlowIntents.stream().noneMatch(intent -> intent.equalsIgnoreCase(flowIntent))) {
            reducedFlowIntents.add(flowIntent);
        }
        reducedOffer.getAttributes().setFlowIntents(reducedFlowIntents);

        List<String> reducedSubscriptionTypes = Optional.ofNullable(reducedOffer.getAttributes().getServiceSubscriptionType())
                .orElseGet(ArrayList::new);
        for (String matchedSubscriptionType : matchedSubscriptionTypes) {
            if (reducedSubscriptionTypes.stream().noneMatch(type -> type.equalsIgnoreCase(matchedSubscriptionType))) {
                reducedSubscriptionTypes.add(matchedSubscriptionType);
            }
        }
        reducedOffer.getAttributes().setServiceSubscriptionType(reducedSubscriptionTypes);
    }

    private String getOfferAccumulatorKey(CTOffer offer) {
        if (Objects.nonNull(offer)) {
            if (!StringUtils.isEmpty(offer.getId())) {
                return offer.getId();
            }
            if (!StringUtils.isEmpty(offer.getCode())) {
                return offer.getCode();
            }
        }
        return String.valueOf(System.identityHashCode(offer));
    }

    // Deep copy via ObjectMapper to keep non-SLS offer attributes intact,
    // then initialize only reduced SLS dimensions for accumulation.
    private CTOffer createReducedSLSOfferCopy(CTOffer sourceOffer) {
        CTOffer copiedOffer = MAPPER.convertValue(sourceOffer, CTOffer.class);
        if (Objects.nonNull(copiedOffer) && Objects.nonNull(copiedOffer.getAttributes())) {
            copiedOffer.getAttributes().setFlowIntents(new ArrayList<>());
            copiedOffer.getAttributes().setServiceSubscriptionType(new ArrayList<>());
        }
        return copiedOffer;
    }

    private boolean isProductOnAccountToSuppress(CTOfferRequest ctOfferRequest, CTOffer offer) {
        Set<String> inputSuppressProductCodes = ctOfferRequest != null && ctOfferRequest.getProductsOnAccountToSuppressOffer() != null
                ? new HashSet<>(ctOfferRequest.getProductsOnAccountToSuppressOffer())
                : Collections.emptySet();
        Set<String> outputSuppressCodes = offer != null && offer.getAttributes() != null && offer.getAttributes().getProductsOnAccountToSuppressOffer() != null
                ? new HashSet<>(offer.getAttributes().getProductsOnAccountToSuppressOffer())
                : Collections.emptySet();
        for (String code : inputSuppressProductCodes) {
            if (outputSuppressCodes.contains(code)) {
                return true;
            }
        }
        return false;
    }

    public static void removeServiceSubscriptionTypeDEFAULT(List<CTOffer> offers) {
        if (CollectionUtils.isNotEmpty(offers)) {
            offers.stream().filter(Objects::nonNull)
                    .filter(offer -> ObjectUtils.allNotNull(offer.getAttributes()))
                    .filter(offer -> CollectionUtils.isNotEmpty(offer.getAttributes().getServiceSubscriptionType()))
                    .forEach(offer -> {
                        List<String> types = offer.getAttributes().getServiceSubscriptionType();
                        if (CollectionUtils.isNotEmpty(types)) {
                            List<String> filtered = types.stream()
                                    .filter(type -> !Constants.SLS_DEFAULT.equalsIgnoreCase(type))
                                    .collect(Collectors.toList());
                            offer.getAttributes().setServiceSubscriptionType(filtered.isEmpty() ? null : filtered);
                        }
                    });
        }
    }

    public boolean isSwimlaneOffer(CTOffer offer) {
        if (offer == null || offer.getAttributes() == null) {
            return false;
        }
        OfferAttributes attributes = offer.getAttributes();
        String contractIndicator = attributes.getContractIndicator();
        return redisCacheHelper.getSwimlaneRules(Constants.UNIVERSAL_COHORTS, Constants.OTT_PRODUCT_FAMILY).stream()
                .filter(Objects::nonNull)
                .anyMatch(value -> value.equalsIgnoreCase(contractIndicator))
                && CollectionUtils.isNotEmpty(attributes.getFlowIntents())
                && attributes.getFlowIntents().stream()
                .anyMatch(flow -> Constants.SWIMLANE.equalsIgnoreCase(flow));
    }

    public void suppressSLSInlaneOffersServiceSubscriptionType(OfferRequestWrapper offerRequestWrapper, List<CTOffer> offers) {
        if (offerRequestWrapper != null
                && offerRequestWrapper.getOfferRequest() != null
                && CollectionUtils.isNotEmpty(offers)) {

            OfferRequest offerRequest = offerRequestWrapper.getOfferRequest();
            if (StringUtils.isEmpty(offerRequest.getCustomerSubscriptionType())) {
                for (CTOffer offer : offers) {
                    if (offer != null && offer.getAttributes() != null) {
                        List<String> flowIntents = offer.getAttributes().getFlowIntents();

                        if (CollectionUtils.isNotEmpty(flowIntents)) {
                            long inlaneCount = flowIntents.stream()
                                    .filter(intent -> Constants.INLANE.equalsIgnoreCase(intent))
                                    .count();

                            if (inlaneCount > 0) {
                                if (flowIntents.size() == inlaneCount) {
                                    // Only INLANE present
                                    offer.getAttributes().setServiceSubscriptionType(null);
                                } else {
                                    // Remove only INLANE
                                    flowIntents.removeIf(intent -> Constants.INLANE.equalsIgnoreCase(intent));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isSlsGetOfferSalesEnabled() {
        return featureHelper.isEnabled(Constants.FEATURE_FLAG_SLS_GETOFFERS_SALES_ENABLED);
    }

    public boolean isSlsGetOfferServicesEnabled() {
        return featureHelper.isEnabled(Constants.FEATURE_FLAG_SLS_GETOFFERS_SERVICES_ENABLED);
    }

    public boolean isSlsGetProductServicesEnabled() {
        return featureHelper.isEnabled(Constants.FEATURE_FLAG_SLS_GETPRODUCTS_SERVICES_ENABLED);
    }

    public boolean isSlsGetProductSalesEnabled() {
        return featureHelper.isEnabled(Constants.FEATURE_FLAG_SLS_GETPRODUCTS_SALES_ENABLED);
    }

    public boolean isVCSLSSalesFlowEnabled() {
        return featureHelper.isEnabled(Constants.FEATURE_FLAG_SLS_VC_SALES_ENABLED);
    }

    public boolean isVCSLSServicesFlowEnabled() {
        return featureHelper.isEnabled(Constants.FEATURE_FLAG_SLS_VC_SERVICES_ENABLED);
    }

    /**
     * Builds the pricing criteria context map for the getProducts flow.
     * <ul>
     *   <li>{@code serviceSubscriptionType} – from {@code request.customerSubscriptionType}</li>
     *   <li>{@code treatmentCode}           – from {@code request.treatmentCode}</li>
     *   <li>{@code creditRisk}              – from {@code request.creditRisk}</li>
     *   <li>{@code iapPartnerType}          – from {@code request.customerContext.OTT.iapPartnerAccountType}
     *       if present; otherwise derived from salesChannel:
     *       oemIAPROKUTV to ROKUTV, oemIAPFIRETV to FIRETV, oemIAPGOOGLE to GOOGLE</li>
     * </ul>
     */
    public Map<String, String> buildProductPricingCriteriaContext(ProductRequest productRequest) {
        Map<String, String> criteriaContext = new HashMap<>();
        if (productRequest == null) return criteriaContext;

        criteriaContext.put(Constants.ELIGIBILITY_CRITERIA_KEY_SERVICE_SUBSCRIPTION_TYPE,
                productRequest.getCustomerSubscriptionType());
        criteriaContext.put(Constants.ELIGIBILITY_CRITERIA_KEY_TREATMENT_CODE,
                productRequest.getTreatmentCode());
        criteriaContext.put(Constants.ELIGIBILITY_CRITERIA_KEY_CREDIT_RISK,
                productRequest.getCreditRisk());
        criteriaContext.put(Constants.ELIGIBILITY_CRITERIA_KEY_IAP_PARTNER_TYPE,
                resolveIapPartnerTypeFromProductRequest(productRequest));

        return criteriaContext;
    }

    /**
     * Resolves effective {@code iapPartnerType} for the getProducts flow.
     * Priority: {@code customerContext.OTT.iapPartnerAccountType} (if non-blank),
     * then derived from salesChannel (oemIAPROKUTV to ROKUTV, oemIAPFIRETV to FIRETV, oemIAPGOOGLE to GOOGLE).
     */
    public String resolveIapPartnerTypeFromProductRequest(ProductRequest productRequest) {
        if (productRequest == null) return null;

        // Priority 1: explicit iapPartnerAccountType from OTT context
        if (Objects.nonNull(productRequest.getCustomerContext())
                && Objects.nonNull(productRequest.getCustomerContext().getOtt())
                && Objects.nonNull(productRequest.getCustomerContext().getOtt().getIapPartnerAccountType())
                && !productRequest.getCustomerContext().getOtt().getIapPartnerAccountType().trim().isEmpty()) {
            return productRequest.getCustomerContext().getOtt().getIapPartnerAccountType();
        }

        // Priority 2: derive from salesChannel
        if (CollectionUtils.isNotEmpty(productRequest.getSalesChannel())) {
            String salesChannel = productRequest.getSalesChannel().get(0);
            if (Constants.OEM_IAPROKUTV.equalsIgnoreCase(salesChannel)) {
                return Constants.ROKUTV;
            } else if (Constants.OEM_IAPFIRETV.equalsIgnoreCase(salesChannel)) {
                return Constants.FIRETV;
            } else if (Constants.OEM_IAP_GOOGLE.equalsIgnoreCase(salesChannel)) {
                return Constants.GOOGLE;
            }
        }
        return null;
    }

    public boolean isUniversalCohort(List<String> contractIndicators) {
        return CollectionUtils.isNotEmpty(contractIndicators)
                && redisCacheHelper.getSwimlaneRules(Constants.UNIVERSAL_COHORTS, Constants.OTT_PRODUCT_FAMILY).stream().filter(Objects::nonNull).anyMatch(rule -> contractIndicators.stream().anyMatch(ci -> ci.equalsIgnoreCase(rule)));
    }

    public static boolean isOTTServiceFlow(OfferRequest offerRequest) {
        return offerRequest.getOfferProductFamily().contains(Constants.OTT_PRODUCT_FAMILY) &&
                Optional.ofNullable(offerRequest.getOfferActionType()).isPresent()
                && (offerRequest.getOfferActionType().contains(Constants.UPGRADE_ACTION_TYPE)
                || offerRequest.getOfferActionType().contains(Constants.DOWNGRADE_ACTION_TYPE)
                || offerRequest.getOfferActionType().contains(Constants.CROSS_SELL_ACTION_TYPE)
                || offerRequest.getOfferActionType().contains(Constants.OTHER_ACTION_TYPE)
                || offerRequest.getOfferActionType().contains(Constants.RETENTION_ACTION_TYPE));
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
        Map<String, List<String>> flowIntentMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(finalOfferList)) {
            finalOfferList.forEach(offer -> {
                        if (offer.getAttributes().getFlowIntents() != null && CollectionUtils.isNotEmpty(offer.getAttributes().getFlowIntents())) {
                            flowIntentMap.put(offer.getCode(), offer.getAttributes().getFlowIntents());
                        }
                    });
            finalOfferList.forEach(offer -> {
                if (Objects.nonNull(offer.getAttributes()) && CollectionUtils.isNotEmpty(offer.getAttributes().getOfferProductTypes()) &&
                        offer.getAttributes().getOfferProductTypes().stream().anyMatch(Constants.VIDEO_ADDON::equalsIgnoreCase)) {
                    if (CollectionUtils.isNotEmpty(offer.getAttributes().getConflictingOffers())) {
                        offer.getAttributes().getConflictingOffers().forEach(conflictingOffer -> {
                            if (isSlsGetOfferServicesEnabled()
                                    && isService(offerRequestWrapper.getOfferRequest())
                                    && isUniversalCohort(offerRequestWrapper.getOfferRequest().getContractIndicator())) {
                                List<String> flowIntents = flowIntentMap.get(conflictingOffer.getKey());
                                if (flowIntents != null && CollectionUtils.isNotEmpty(flowIntents) && offer.getAttributes().getFlowIntents() != null && CollectionUtils.isNotEmpty(offer.getAttributes().getFlowIntents())) {
                                    if (offer.getAttributes().getFlowIntents().containsAll(flowIntents)) {
                                        conflictingOfferSet.add(conflictingOffer.getKey());
                                    }
                                }
                            } else {
                                conflictingOfferSet.add(conflictingOffer.getKey());
                            }

                        });
                    }
                    if (CollectionUtils.isNotEmpty(offer.getAttributes().getAllowConflictingOffers()) && CollectionUtils.isNotEmpty(offer.getAttributes().getConflictingOffers())) {
                        offer.getAttributes().getConflictingOffers().addAll(offer.getAttributes().getAllowConflictingOffers());
                    } else if (CollectionUtils.isNotEmpty(offer.getAttributes().getAllowConflictingOffers()) && CollectionUtils.isEmpty(offer.getAttributes().getConflictingOffers())) {
                        offer.getAttributes().setConflictingOffers(offer.getAttributes().getAllowConflictingOffers());
                    }
                    offer.getAttributes().setAllowConflictingOffers(null);
                }
            });
        }
        if (!conflictingOfferSet.isEmpty() && (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCartContext()).isPresent() ||
                (CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getOfferActionType()) && !CollectionUtils.containsAny(Arrays.asList(Constants.ACQUISITION, Constants.UPSELL_ACTION_TYPE), offerRequestWrapper.getOfferRequest().getOfferActionType()))
                || (CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getCustomerSegments()) && CollectionUtils.containsAny(Arrays.asList(Constants.MDUTENANT), offerRequestWrapper.getOfferRequest().getCustomerSegments())))) {
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
                .forEach(variant -> {
                    List<IncludeProductWrapper> filtered = variant.getAttributes().getIncludedProducts().stream()
                            .filter(ipw -> OffersUtils.isEligibleIncludedProduct(ipw, salesChannelsList, iapPartnerAccountType))
                            .collect(Collectors.toList());
                    if (filtered.size() > 1) {
                        // If multiple IncludeProductWrappers are eligible, we need to merge their products into a single wrapper
                        List<IncludedProduct> allIncludedProducts = filtered.stream().filter(includeProductWrapper -> CollectionUtils.isNotEmpty(includeProductWrapper.getProducts()))
                                .flatMap(includeProductWrapper -> includeProductWrapper.getProducts().stream())
                                .distinct()
                                .collect(Collectors.toList());
                        IncludeProductWrapper includeProductWrapper = new IncludeProductWrapper();
                        includeProductWrapper.setProducts(allIncludedProducts);
                        filtered = Stream.of(includeProductWrapper).collect(Collectors.toList());
                    }
                    variant.getAttributes().setIncludedProducts(CollectionUtils.isNotEmpty(filtered) ? filtered : null);
                });
    }

    // PriceTier Framework – Attribute Pricing & Conflicting Tier Resolution
    /**
     * Entry point called (statically) from {@link #calculateBestPrice} for each offer.
     *
     * <p><b>Step 0</b>: if {@code offer.attributes.priceTier} is set the immediately
     * following call to {@link #setBasePriceBasedOnPriceTier(CTOffer)} handles it; returns early.<br>
     * <b>Steps 1-3</b>: BAU price list then attribute criteria evaluation then
     * conflicting-tier suppression, applied to every variant inside the offer's
     * bundle/qualifying products.
     *
     * <p>In the static (calculateBestPrice) pipeline {@code {GlobalConfigKey}} tokens inside
     * {@code attributePricingCriteria} are skipped (treated as "no restriction") because Redis
     * is unavailable in a static context.  The instance method
     */
    private void setBasePriceBasedOnAttributePricingCriteria(
            OfferRequestWrapper offerRequestWrapper, CTOffer offer) {

        if (offer.getAttributes() == null) return;

        List<String> contractIndicators = Optional.ofNullable(offerRequestWrapper)
                .map(OfferRequestWrapper::getOfferRequest)
                .map(OfferRequest::getContractIndicator)
                .orElse(Collections.emptyList());
        boolean slsEnabled = featureHelper.isEnabled(Constants.FEATURE_FLAG_SLS_GETOFFERS_SERVICES_ENABLED)
                || featureHelper.isEnabled(Constants.FEATURE_FLAG_SLS_GETOFFERS_SALES_ENABLED);
        if (!slsEnabled) {
            log.debug("PriceTier Framework: skipping attribute pricing for offer [{}] – neither SLS getOffer flag enabled",
                    offer.getCode());
            return;
        }

        // Step 0: explicit priceTier – handled by setBasePriceBasedOnPriceTier(offer) below
        if (offer.getAttributes().getPriceTier() != null && !offer.getAttributes().getPriceTier().trim().isEmpty()) {
            log.debug("PriceTier Framework: offer [{}] has explicit priceTier [{}]; deferring to priceTier lookup.",
                    offer.getCode(), offer.getAttributes().getPriceTier());
            return;
        }

        if (CollectionUtils.isEmpty(offer.getAttributes().getAssociatedProducts())) return;

        // Build context once per offer – same for all products/variants in this offer
        Map<String, String> criteriaContext = buildPricingCriteriaContext(offer, offerRequestWrapper);

        AssociatedProduct associatedProduct = offer.getAttributes().getAssociatedProducts().get(0);
        applyAttributePricingToProductWrappers(associatedProduct.getBundleProducts(), criteriaContext, offer.getCode());
        applyAttributePricingToProductWrappers(associatedProduct.getQualifyingProducts(), criteriaContext, offer.getCode());
    }

    /**
     * Applies Steps 1-3 to all variants in every product of the given wrapper list.
     *
     * @param productWrappers   bundle or qualifying product wrappers
     * @param criteriaContext   pricing context (serviceSubscriptionType, iapPartnerType, …)
     * @param offerCode       for debug logging only
     */
    private void applyAttributePricingToProductWrappers(
            List<ProductWrapper> productWrappers,
            Map<String, String> criteriaContext,
            String offerCode) {

        if (CollectionUtils.isEmpty(productWrappers)) return;

        productWrappers.stream()
                .filter(Objects::nonNull)
                .filter(productWrapper -> CollectionUtils.isNotEmpty(productWrapper.getProducts()))
                .flatMap(productWrapper -> productWrapper.getProducts().stream())
                .filter(Objects::nonNull)
                .filter(product -> product.getObj() != null && CollectionUtils.isNotEmpty(product.getObj().getVariants()))
                .forEach(product -> product.getObj().getVariants().stream()
                        .filter(Objects::nonNull)
                        .forEach(variant -> {
                            List<Price> prices = variant.getPrices();
                            if (CollectionUtils.isEmpty(prices)) return;

                            List<Price> selected = evaluateAndSelectPrices(prices, criteriaContext);

                            log.debug("PriceTier Framework: offer [{}] product [{}] prices before={} after={}",
                                    offerCode, product.getKey(), prices.size(), selected != null ? selected.size() : null);
                            variant.setPrices(selected);
                        }));
    }

    // Core evaluator – shared by Sales and Services flows
    /**
     * Selects the correct price record(s) for a variant.
     *
     * <ol>
     *   <li><b>Step 0</b> – explicit tier: if {@code requestedPriceTier} is non-blank,
     *       return only prices whose {@code priceTier} contains that value.</li>
     *   <li><b>Step 1</b> – BAU: work with the full (already date/contract-filtered) list.</li>
     *   <li><b>Step 2</b> – Attribute criteria: filter by {@code attributePricingCriteria};
     *       a price with no criteria is eligible for all; fallback to BAU if none pass.</li>
     *   <li><b>Step 3</b> – Conflict resolution: suppress prices whose {@code priceTier}
     *       appears in another surviving price's {@code conflictingPriceTiers}.</li>
     * </ol>
     *
     * @param prices             candidate prices for a variant
     * @param criteriaContext                keythenvalue map (serviceSubscriptionType, iapPartnerType, …)
     * @return final eligible price list (never {@code null})
     */
    public List<Price> evaluateAndSelectPrices(
            List<Price> prices,
            Map<String, String> criteriaContext) {

        if (CollectionUtils.isEmpty(prices)) return Collections.emptyList();

        // Step 1 – BAU
        List<Price> bau = prices.stream().filter(Objects::nonNull).collect(Collectors.toList());

        // Step 2 – Attribute criteria evaluation
        List<Price> filtered = bau.stream()
                .filter(p -> isPriceEligibleByCriteria(p, criteriaContext))
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            log.debug("PriceTier Framework: no prices passed attribute criteria; reverting to BAU.");
            filtered = bau;
        }

        // Step 3 – Conflicting priceTier resolution
        return resolveConflictingPriceTiers(filtered);
    }

    // Criteria Context building
    /**
     * Builds the pricing context map for a given offer and request.
     * <ul>
     *   <li>{@code serviceSubscriptionType} then {@code offer.serviceSubscriptionTypes[0]}</li>
     *   <li>{@code treatmentCode}           then {@code offer.treatmentCodes[0]}</li>
     *   <li>{@code creditRisk}              then {@code offer.eligibilityCreditRisks[0]}</li>
     *   <li>{@code iapPartnerType}          then {@code OTT.iapPartnerAccountType}
     *       or derived from {@code salesChannel} (ROKU / FIRETV / GOOGLE)</li>
     * </ul>
     */
    public  Map<String, String> buildPricingCriteriaContext(CTOffer offer,
                                                                  OfferRequestWrapper offerRequestWrapper) {
        OfferRequest req = offerRequestWrapper != null ? offerRequestWrapper.getOfferRequest() : null;
        Map<String, String> criteriaContext = new HashMap<>();
        if (offer != null && offer.getAttributes() != null) {
            criteriaContext.put(Constants.ELIGIBILITY_CRITERIA_KEY_SERVICE_SUBSCRIPTION_TYPE,
                    getFirstElement(offer.getAttributes().getServiceSubscriptionType()));
            criteriaContext.put(Constants.ELIGIBILITY_CRITERIA_KEY_TREATMENT_CODE,
                    getFirstElement(offer.getAttributes().getTreatmentCode()));
            criteriaContext.put(Constants.ELIGIBILITY_CRITERIA_KEY_CREDIT_RISK,
                    getFirstElement(offer.getAttributes().getCreditRisk()));
        }
        criteriaContext.put(Constants.ELIGIBILITY_CRITERIA_KEY_IAP_PARTNER_TYPE, resolveIapPartnerType(req));
        return criteriaContext;
    }

    /**
     * Resolves the effective {@code iapPartnerType} for the request.
     * Priority: {@code OTT.iapPartnerAccountType} then derived from {@code salesChannel}.
     * {@code static} so it is callable from both static and instance pipelines.
     */
    public  String resolveIapPartnerType(OfferRequest offerRequest) {
        if (offerRequest == null) return null;
        String fromOtt = getIapPartnerAccountType(offerRequest);
        if (fromOtt != null && !fromOtt.isEmpty()) return fromOtt;
        List<String> iapparternerTypeSLSList = redisCacheHelper
                .getSwimlaneRules(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT);
        if (CollectionUtils.isNotEmpty(iapparternerTypeSLSList) && CollectionUtils.isNotEmpty(offerRequest.getSalesChannel())) {
            String salesChannel = offerRequest.getSalesChannel().get(0);
            if (salesChannel != null) {
                return iapparternerTypeSLSList.stream()
                        .map(entry -> entry.split("\\s*:\\s*"))
                        .filter(kvPair -> kvPair.length == 2 && salesChannel.equalsIgnoreCase(kvPair[0]))
                        .map(kvPair -> kvPair[1])
                        .findFirst()
                        .orElse(null);
            }
        }
        return null;
    }

    // Attribute criteria evaluation
    /**
     * Returns {@code true} when {@code price} passes all {@code attributePricingCriteria} checks.
     * <ul>
     *   <li>No criteria then eligible for all.</li>
     *   <li>Multiple values for the same key then OR match (pipe-separated).</li>
     *   <li>Different keys then AND match.</li>
     *   <li>{@code {GlobalConfigKey}} tokens then resolved via {@code configResolver};
     *       if the resolver returns empty the token is ignored (price stays eligible).</li>
     *   <li>Comparison is case-insensitive; whitespace is trimmed.</li>
     * </ul>
     */
     boolean isPriceEligibleByCriteria(Price price,
                                             Map<String, String> criteriaContext) {
        if (CollectionUtils.isEmpty(price.getAttributePricingCriteria())) return true;

        // Group raw criteria entries by key: "key=rawValue"
        Map<String, List<String>> byKey = new LinkedHashMap<>();
        for (String entry : price.getAttributePricingCriteria()) {
            if (entry == null || !entry.contains("=")) continue;
            String[] parts = entry.split("=", 2);
            byKey.computeIfAbsent(parts[0].trim(), k -> new ArrayList<>()).add(parts[1].trim());
        }
        if (byKey.isEmpty()) return true;

        // AND across keys; OR within a key (after pipe/config expansion)
        for (Map.Entry<String, List<String>> e : byKey.entrySet()) {
            String contextValue = criteriaContext.get(e.getKey());
            if (!isCriteriaValueMatched(e.getValue(), contextValue)) {
                log.debug("Price [{}] failed criteria key [{}]: contextValue=[{}]",
                        price.getId(), e.getKey(), contextValue);
                return false;
            }
        }
        return true;
    }

    /**
     * OR-match: at least one expanded value equals {@code contextValue} (case-insensitive).
     * If all tokens were {@code {GlobalConfigKey}} and none resolved then "no restriction" (true).
     */
    private boolean isCriteriaValueMatched(
            List<String> rawValues,
            String contextValue) {

        if (contextValue == null || contextValue.trim().isEmpty()) return false;

        List<String> expanded = expandCriteriaValues(rawValues);
        // Empty after expansion (e.g. all were unresolved GlobalConfigKeys) then no restriction
        if (expanded.isEmpty()) return true;
        return expanded.stream().anyMatch(v -> v.equalsIgnoreCase(contextValue.trim()));
    }

    /**
     * Expands raw criteria value tokens:
     * <ul>
     *   <li>{@code v1|v2|v3}         then split on {@code |}}</li>
     *   <li>{@code {GlobalConfigKey}} then resolved via {@code configResolver};
     *       each resolved value is comma-split</li>
     * </ul>
     */
    private List<String> expandCriteriaValues(
            List<String> rawValues) {

        List<String> result = new ArrayList<>();
        for (String raw : rawValues) {
            if (raw == null) continue;
            String trimmed = raw.trim();
            Arrays.stream(trimmed.split("\\s*\\|\\s*"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(result::add);
        }
        return result;
    }

    // Conflicting priceTier resolution (Step 3)
    /**
     * Suppresses price records whose own {@code priceTier} appears in the
     * {@code conflictingPriceTiers} declared by any other surviving price.
     *
     * <ol>
     *   <li>Collect all tier values declared as conflicting across all prices.</li>
     *   <li>Remove prices whose {@code priceTier} intersects that set.</li>
     *   <li>Safety guard: if suppression would remove every price, return the original list.</li>
     * </ol>
     */
    static List<Price> resolveConflictingPriceTiers(List<Price> prices) {
        if (CollectionUtils.isEmpty(prices)) return prices;

        Set<String> suppressed = prices.stream()
                .filter(Objects::nonNull)
                .filter(price -> CollectionUtils.isNotEmpty(price.getConflictingPriceTiers()))
                .flatMap(price -> price.getConflictingPriceTiers().stream())
                .filter(Objects::nonNull)
                .map(t -> t.trim().toLowerCase())
                .collect(Collectors.toSet());

        if (suppressed.isEmpty()) return prices;

        List<Price> resolved = prices.stream()
                .filter(Objects::nonNull)
                .filter(price -> !isPriceTierSuppressed(price, suppressed))
                .collect(Collectors.toList());

/*        if (resolved.isEmpty()) {
            log.debug("PriceTier Framework: conflict resolution would remove all prices; returning original.");
            return prices;
        }*/
        log.debug("PriceTier Framework: suppressed tiers={}, before={}, after={}",
                suppressed, prices.size(), resolved != null ? resolved.size() : null);
        return resolved;
    }

    /** Returns {@code true} if any of the price's own tiers are in the suppressed set. */
    private static boolean isPriceTierSuppressed(Price price, Set<String> suppressed) {
        return CollectionUtils.isNotEmpty(price.getPriceTier())
                && price.getPriceTier().stream()
                        .filter(Objects::nonNull)
                        .map(t -> t.trim().toLowerCase())
                        .anyMatch(suppressed::contains);
    }

    /** Safely returns the first element of a list, or {@code null} if absent. */
    private static String getFirstElement(List<String> list) {
        return CollectionUtils.isNotEmpty(list) ? list.get(0) : null;
    }
    public Map<String, String> getOfferFlowByOfferRequest(OfferRequestWrapper offerRequestWrapper) {
        Map<String, String> OfferRequestFlowMap = new LinkedHashMap<>();

        if (offerRequestWrapper == null) {
            log.debug("getOfferFlowByOfferRequest: offerRequestWrapper is null, returning empty map.");
            return OfferRequestFlowMap;
        }

        OfferRequest offerRequest = offerRequestWrapper.getOfferRequest();

        if (offerRequest != null) {

            if(offerRequest.getCustomerSubscriptionType() != null){
                OfferRequestFlowMap.put(Constants.CUSTOMER_SUBSCRIPTION_TYPE,
                        "true");
            }else{
                OfferRequestFlowMap.put(Constants.CUSTOMER_SUBSCRIPTION_TYPE,
                        "false");
            }

            if (CollectionUtils.isNotEmpty(offerRequest.getOfferCodes())) {
                OfferRequestFlowMap.put(Constants.OFFER_FLOW, Constants.OFFER_CODE_CALL);
            } else if (offerRequest.getCartContext() != null && CollectionUtils.isNotEmpty(offerRequest.getCartContext().getCpopOfferCodes())) {
                OfferRequestFlowMap.put(Constants.OFFER_FLOW, Constants.CART_MODE_CALL);
            } else {
                OfferRequestFlowMap.put(Constants.OFFER_FLOW, Constants.BASE_CALL);
            }

        }

        log.debug("getOfferFlowByOfferRequest: resolved flowMap={}", OfferRequestFlowMap);
        return OfferRequestFlowMap;
    }


    public List<CTOffer> filterSalesOffersBasedOnCustomerSubscriptionType(List<CTOffer> finalOfferList,
                                                   String requestCustomerSubType) {
        Optional.ofNullable(finalOfferList)
                    .filter(offers -> !offers.isEmpty())
                    .ifPresent(offers -> {
                        List<CTOffer> filteredOffers = offers.stream()
                                .filter(Objects::nonNull)
                                .filter(offer -> {
                                    List<String> responseServiceSubTypes = offer.getAttributes().getServiceSubscriptionType();
                                    return (CollectionUtils.isNotEmpty(responseServiceSubTypes) && requestCustomerSubType != null && responseServiceSubTypes.stream().anyMatch(subType -> subType.equalsIgnoreCase(requestCustomerSubType)));
                                })
                                .collect(Collectors.toList());
                        offers.clear();
                        offers.addAll(filteredOffers);
                    });
        return finalOfferList;
    }

    public boolean isSalesCT(CTOfferRequest offerRequest) {
        return Optional.ofNullable(offerRequest.getOfferActionType()).isPresent()
                && (offerRequest.getOfferActionType().contains(Constants.ACQUISITION_ACTION_TYPE)
                || offerRequest.getOfferActionType().contains(Constants.UPSELL_ACTION_TYPE));
    }

    public boolean isOTTSales(OfferRequest offerRequest) {
        return offerRequest.getOfferProductFamily().contains(Constants.OTT_PRODUCT_FAMILY) &&
                Optional.ofNullable(offerRequest.getOfferActionType()).isPresent()
                && (offerRequest.getOfferActionType().contains(Constants.ACQUISITION_ACTION_TYPE)
                || offerRequest.getOfferActionType().contains(Constants.UPSELL_ACTION_TYPE));
    }

    public boolean isOTTOfferCodeCall(OfferRequest offerRequest) {
        return CollectionUtils.isNotEmpty(offerRequest.getOfferCodes()) && !(Optional.ofNullable(offerRequest.getOfferActionType()).isPresent());
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
    public void removeServiceSubTypeAndPriceTierForOpus(CTOfferResponse ctResponse, OfferRequest ctOfferRequest) {

        List<String> salesChannels = ctOfferRequest.getSalesChannel();
        if(CollectionUtils.isNotEmpty(salesChannels)) {
            String value = eligibleSalesChannelForCustSubsTypeCheck(salesChannels);
                if ("false".equalsIgnoreCase(value)) {
                    clearServiceSubTypeAndFlowIntents(ctResponse);
                    clearVariantPriceTier(ctResponse);
                    clearVariantPriceTierQualifyingProduct(ctResponse);
                }
        }
    }

    private void clearServiceSubTypeAndFlowIntents(CTOfferResponse ctResponse) {
        Optional.ofNullable(ctResponse.getOffers())
                .filter(CollectionUtils::isNotEmpty)
                .ifPresent(offers -> offers.stream()
                        .filter(Objects::nonNull)
                        .map(CTOffer::getAttributes)
                        .filter(Objects::nonNull)
                        .filter(this::hasConstraintWithSalesChannel)
                        .forEach(attributes -> {
                            attributes.setServiceSubscriptionType(null);
                            attributes.setFlowIntents(null);
                        }));
    }

    private boolean hasConstraintWithSalesChannel(OfferAttributes attributes) {
        return Objects.nonNull(attributes.getEligibility())
                && CollectionUtils.isNotEmpty(attributes.getEligibility().getConstraints())
                && attributes.getEligibility().getConstraints().stream()
                        .filter(Objects::nonNull)
                        .anyMatch(constraint -> CollectionUtils.isNotEmpty(constraint.getSalesChannel()));
    }

    private void clearVariantPriceTier(CTOfferResponse ctResponse) {
        if (CollectionUtils.isEmpty(ctResponse.getOffers())) {
            return;
        }
        ctResponse.getOffers().stream()
                .filter(Objects::nonNull)
                .filter(offer -> offer.getAttributes() != null)
                .filter(offer -> CollectionUtils.isNotEmpty(offer.getAttributes().getAssociatedProducts()))
                .flatMap(offer -> offer.getAttributes().getAssociatedProducts().stream())
                .filter(Objects::nonNull)
                .filter(ap -> CollectionUtils.isNotEmpty(ap.getBundleProducts()))
                .flatMap(ap -> ap.getBundleProducts().stream())
                .filter(Objects::nonNull)
                .filter(bp -> CollectionUtils.isNotEmpty(bp.getProducts()))
                .flatMap(bp -> bp.getProducts().stream())
                .filter(Objects::nonNull)
                .filter(product -> product.getObj() != null && CollectionUtils.isNotEmpty(product.getObj().getVariants()))
                .flatMap(product -> product.getObj().getVariants().stream())
                .filter(Objects::nonNull)
                .forEach(variant -> {
                    if (variant.getAttributes() != null && CollectionUtils.isNotEmpty(variant.getPrices())) {
                        variant.getPrices().get(0).setPriceTier(null);
                    }
                });
    }

    private void clearVariantPriceTierQualifyingProduct(CTOfferResponse ctResponse) {
        if (CollectionUtils.isEmpty(ctResponse.getOffers())) {
            return;
        }
        ctResponse.getOffers().stream()
                .filter(Objects::nonNull)
                .filter(offer -> offer.getAttributes() != null)
                .filter(offer -> CollectionUtils.isNotEmpty(offer.getAttributes().getAssociatedProducts()))
                .flatMap(offer -> offer.getAttributes().getAssociatedProducts().stream())
                .filter(Objects::nonNull)
                .filter(ap -> CollectionUtils.isNotEmpty(ap.getQualifyingProducts()))
                .flatMap(ap -> ap.getQualifyingProducts().stream())
                .filter(Objects::nonNull)
                .filter(qp -> CollectionUtils.isNotEmpty(qp.getProducts()))
                .flatMap(qp -> qp.getProducts().stream())
                .filter(Objects::nonNull)
                .filter(product -> product.getObj() != null && CollectionUtils.isNotEmpty(product.getObj().getVariants()))
                .flatMap(product -> product.getObj().getVariants().stream())
                .filter(Objects::nonNull)
                .forEach(variant -> {
                    if (variant.getAttributes() != null && CollectionUtils.isNotEmpty(variant.getPrices())) {
                        variant.getPrices().get(0).setPriceTier(null);
                    }
                });
    }

    public boolean isOpusChannel(OfferRequest offerRequest) {
        List<String> salesChannels = offerRequest.getSalesChannel();
        return CollectionUtils.isNotEmpty(salesChannels)
                && salesChannels.stream().anyMatch(channel -> channel.equalsIgnoreCase(Constants.OPUS) || channel.equalsIgnoreCase(Constants.DTV360) || channel.equalsIgnoreCase(Constants.UVC));
    }


    public  String eligibleSalesChannelForCustSubsTypeCheck(List<String> salesChannels) {
        if (salesChannels == null) return null;
        List<String> salesChannelSLSList = redisCacheHelper
                .getSwimlaneRules(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_ELIGIBLE_SALES_CHANNEL_COMBINATIONS_SLS, Constants.OTT);
        if (CollectionUtils.isNotEmpty(salesChannelSLSList) && CollectionUtils.isNotEmpty(salesChannels)) {
            String salesChannel = salesChannels.get(0);
            if (salesChannel != null) {
                boolean salesChannelExists = salesChannelSLSList.stream()
                        .map(entry -> entry.split("\\s*:\\s*"))
                        .filter(kvPair -> kvPair.length == 2)
                        .anyMatch(kvPair -> salesChannel.equalsIgnoreCase(kvPair[0]));
                if (salesChannelExists) {
                    String value = salesChannelSLSList.stream()
                            .map(entry -> entry.split("\\s*:\\s*"))
                            .filter(kvPair -> kvPair.length == 2 && salesChannel.equalsIgnoreCase(kvPair[0]))
                            .map(kvPair -> kvPair[1])
                            .findFirst()
                            .orElse(null);
                    if("Y".equalsIgnoreCase(value)){
                        return "true";
                    }else{
                        return "false";
                    }
                }else{
                    return "true";
                }
            }
        }
        return null;
    }

    public void suppressSLSInlaneOffersProductPriceTier(OfferRequestWrapper offerRequestWrapper, List<CTOffer> offers) {
        if (!isValidRequestForPriceTierSuppression(offerRequestWrapper, offers)) {
            return;
        }
        List<CTOffer> nonSwimlaneOffers = filterNonSwimlaneOffersWithAssociatedProducts(offers);
        clearBundleProductVariantPriceTier(nonSwimlaneOffers);
        clearQualifyingProductVariantPriceTier(nonSwimlaneOffers);
    }

    private boolean isValidRequestForPriceTierSuppression(OfferRequestWrapper offerRequestWrapper, List<CTOffer> offers) {
        return offerRequestWrapper != null
                && offerRequestWrapper.getOfferRequest() != null
                && CollectionUtils.isNotEmpty(offers)
                && (offerRequestWrapper.getOfferRequest().getCustomerSubscriptionType() == null
                        || offerRequestWrapper.getOfferRequest().getCustomerSubscriptionType().isEmpty());
    }

    private boolean isNonSwimlaneOffer(CTOffer offer) {
        return offer != null
                && offer.getAttributes() != null
                && CollectionUtils.isNotEmpty(offer.getAttributes().getFlowIntents())
                && offer.getAttributes().getFlowIntents().stream()
                        .noneMatch(flowIntent -> flowIntent.equalsIgnoreCase(Constants.SWIMLANE))
                && CollectionUtils.isNotEmpty(offer.getAttributes().getAssociatedProducts());
    }

    private List<CTOffer> filterNonSwimlaneOffersWithAssociatedProducts(List<CTOffer> offers) {
        return offers.stream()
                .filter(this::isNonSwimlaneOffer)
                .collect(Collectors.toList());
    }

    private void clearBundleProductVariantPriceTier(List<CTOffer> offers) {
        offers.stream()
                .flatMap(offer -> offer.getAttributes().getAssociatedProducts().stream())
                .filter(Objects::nonNull)
                .filter(ap -> CollectionUtils.isNotEmpty(ap.getBundleProducts()))
                .flatMap(ap -> ap.getBundleProducts().stream())
                .filter(Objects::nonNull)
                .filter(bp -> CollectionUtils.isNotEmpty(bp.getProducts()))
                .flatMap(bp -> bp.getProducts().stream())
                .filter(Objects::nonNull)
                .filter(product -> product.getObj() != null && CollectionUtils.isNotEmpty(product.getObj().getVariants()))
                .flatMap(product -> product.getObj().getVariants().stream())
                .filter(Objects::nonNull)
                .forEach(this::clearFirstPriceTier);
    }

    private void clearQualifyingProductVariantPriceTier(List<CTOffer> offers) {
        offers.stream()
                .flatMap(offer -> offer.getAttributes().getAssociatedProducts().stream())
                .filter(Objects::nonNull)
                .filter(ap -> CollectionUtils.isNotEmpty(ap.getQualifyingProducts()))
                .flatMap(ap -> ap.getQualifyingProducts().stream())
                .filter(Objects::nonNull)
                .filter(qp -> CollectionUtils.isNotEmpty(qp.getProducts()))
                .flatMap(qp -> qp.getProducts().stream())
                .filter(Objects::nonNull)
                .filter(product -> product.getObj() != null && CollectionUtils.isNotEmpty(product.getObj().getVariants()))
                .flatMap(product -> product.getObj().getVariants().stream())
                .filter(Objects::nonNull)
                .forEach(this::clearFirstPriceTier);
    }

    private void clearFirstPriceTier(Variant variant) {
        if (variant.getAttributes() != null && CollectionUtils.isNotEmpty(variant.getPrices())) {
            variant.getPrices().get(0).setPriceTier(null);
        }
    }

    public  String getIapSalesChannels(OfferRequest offerRequest) {
        if (offerRequest == null) return "N";
        List<String> iapparternerTypeSLSList = redisCacheHelper
                .getSwimlaneRules(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT);
        if (CollectionUtils.isNotEmpty(iapparternerTypeSLSList) && CollectionUtils.isNotEmpty(offerRequest.getSalesChannel())) {
            String salesChannel = offerRequest.getSalesChannel().get(0);
            List<String> iapChannels = iapparternerTypeSLSList.stream()
                    .filter(Objects::nonNull)
                    .map(entry -> entry.split("\\s*:\\s*"))  // Split "key:value"
                    .filter(kvPair -> kvPair.length == 2)     // Only valid pairs
                    .map(kvPair -> kvPair[0])                 // Extract keys only
                    .distinct()                               // Remove duplicates
                    .collect(Collectors.toList());
            if (salesChannel != null && iapChannels != null) {
               if(iapChannels.contains(salesChannel)){
                   return "Y";
               }else{
                   return "N";
               }
            }
        }
        return "N";
    }

    public  String getIapSalesChannelsVC(String salesChannel) {
        if (salesChannel == null) return "N";
        List<String> iapparternerTypeSLSList = redisCacheHelper
                .getSwimlaneRules(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT);
        if (CollectionUtils.isNotEmpty(iapparternerTypeSLSList) && salesChannel != null && !salesChannel.isEmpty()) {

            List<String> iapChannels = iapparternerTypeSLSList.stream()
                    .filter(Objects::nonNull)
                    .map(entry -> entry.split("\\s*:\\s*"))  // Split "key:value"
                    .filter(kvPair -> kvPair.length == 2)     // Only valid pairs
                    .map(kvPair -> kvPair[0])                 // Extract keys only
                    .distinct()                               // Remove duplicates
                    .collect(Collectors.toList());
            if (salesChannel != null && iapChannels != null) {
                if(iapChannels.contains(salesChannel)){
                    return "Y";
                }else{
                    return "N";
                }
            }
        }
        return "N";
    }


    public void updateIapSalesChannelResponses(OfferRequest offerRequest, CTOfferResponse ctResponse) {
        Optional.ofNullable(ctResponse.getOffers())
                .filter(CollectionUtils::isNotEmpty)
                .ifPresent(offers -> offers.stream()
                        .filter(Objects::nonNull)
                        .map(CTOffer::getAttributes)
                        .filter(Objects::nonNull)
                        .filter(this::hasConstraintWithSalesChannel)
                        .forEach(attributes -> {
                            attributes.setServiceSubscriptionType(null);
                            attributes.setFlowIntents(null);
                            attributes.setContractIndicator(offerRequest.getOriginalContractIndicator());
                        }));
    }

    /**
     * Parses {@code globalMessagesByKey} from CT and returns a flat lookup map.
     * <p>Key format (all lowercase): {@code messageType + salesChannel + flowType + priceTier + msgKey}.
     * Only Disclosure, Description and DisplayName messageTypes are included.
     * Entries with no priceTier use {@code ""} as the priceTier segment (catch-all).
     * Used by both getOffers and getProducts 3D GlobalMessages flows.
     */
    public Map<String, String> getGlobalMessages3DMap(List<List<MessageEntry>> messageGroups) {
        Map<String, String> map3D = new HashMap<>();
        if (CollectionUtils.isEmpty(messageGroups)) {
            return map3D;
        }
        for (var messageGroup : messageGroups) {
            // First pass: extract messageType regardless of its position in the group.
            String messageType = null;
            for (var message : messageGroup) {
                if (Constants.MESSAGES_BY_KEY_MESSAGE_TYPE.equals(message.getName())) {
                    var value = message.getValue();
                    if (value instanceof Map) {
                        var keyObj = ((Map<?, ?>) value).get(Constants.KEY);
                        if (keyObj != null) messageType = keyObj.toString();
                    }
                    break;
                }
            }
            if (!is3DMessageType(messageType)) {
                continue;
            }
            // Second pass: process allMessages using the extracted messageType.
            final String resolvedMessageType = messageType;
            for (var message : messageGroup) {
                if (Constants.MESSAGES_BY_KEY_ALL_MESSAGES.equals(message.getName())) {
                    for (var msgArr : (List<?>) message.getValue()) {
                        // Fresh map per entry so priceTier/salesChannel/etc. from one entry
                        // don't bleed into the next (e.g. a catch-all entry with no priceTier
                        // following a PT100 entry would otherwise inherit PT100).
                        var entryMap = new HashMap<String, String>();
                        for (var entry : (List<?>) msgArr) {
                            var allMessage = MAPPER.convertValue(entry, MessageEntry.class);
                            var value = allMessage.getValue();
                            if (value instanceof Map) {
                                var keyObj = ((Map<?, ?>) value).get(Constants.KEY);
                                if (keyObj != null) entryMap.put(allMessage.getName(), keyObj.toString());
                            } else {
                                entryMap.put(allMessage.getName(), value != null ? value.toString() : null);
                            }
                        }
                        String priceTier = entryMap.getOrDefault(Constants.ALL_MESSAGES_PRICE_TIER, "");
                        if (priceTier == null) priceTier = "";
                        // Normalize to lowercase so lookups are case-insensitive across CT keys and request values.
                        String mapKey = (resolvedMessageType
                                + entryMap.get(Constants.ALL_MESSAGES_SALES_CHANNEL)
                                + entryMap.get(Constants.ALL_MESSAGES_FLOW_TYPE)
                                + priceTier
                                + entryMap.get(Constants.KEY)).toLowerCase();
                        map3D.put(mapKey, entryMap.get(Constants.VALUE));
                    }
                }
            }
        }
        return map3D;
    }

    /**
     * GET /offers – applies 3D GlobalMessages (Description, DisplayName, Disclosure) to each bundle
     * product variant with the customer-specific priceTier.
     *
     * <p>Must be called AFTER {@code calculateBestPrice} so {@code setBasePriceBasedOnPriceTier} has
     * already filtered variant prices: {@code prices.get(0).getPriceTier()} gives the resolved priceTier.
     *
     * <p>{@code globalMessagesByKey} is still populated at this point because
     * {@code BuildMiddlewareCTFilters.updateMessagesByKey} only removes the
     * {@code delayProvisioning} messageType, leaving all other types intact.
     *
     * <p>Offer-level 3D map overrides variant-level on duplicate keys.
     * IXP flag guard is the caller's responsibility.
     * {@code salesChannel} must not be null; pass {@code Constants.DEFAULT} when absent in the request.
     */
    public void applyGlobalMessages3DForOffers(CTOfferResponse response, String salesChannel, String flowType) {
        if (response == null || CollectionUtils.isEmpty(response.getOffers())) {
            return;
        }
        response.getOffers().stream()
                .filter(Objects::nonNull)
                .filter(offer -> offer.getAttributes() != null)
                .filter(offer -> CollectionUtils.isNotEmpty(offer.getAttributes().getAssociatedProducts()))
                .forEach(offer -> {
                    // Build offer-level 3D map once per offer.
                    Map<String, String> offerLevel3DMap =
                            getGlobalMessages3DMap(offer.getAttributes().getGlobalMessagesByKey());
                    offer.getAttributes().getAssociatedProducts().stream()
                            .filter(Objects::nonNull)
                            .filter(ap -> CollectionUtils.isNotEmpty(ap.getBundleProducts()))
                            .forEach(ap -> ap.getBundleProducts().stream()
                                    .filter(Objects::nonNull)
                                    .filter(bp -> CollectionUtils.isNotEmpty(bp.getProducts())
                                            && bp.getProducts().get(0) != null)
                                    .forEach(bp -> {
                                        Product product = bp.getProducts().get(0);
                                        if (product.getObj() == null
                                                || CollectionUtils.isEmpty(product.getObj().getVariants())) {
                                            return;
                                        }
                                        // priceTier from prices[0] – correctly filtered by
                                        // setBasePriceBasedOnPriceTier which ran inside calculateBestPrice.
                                        String priceTier = null;
                                        Variant firstVariant = product.getObj().getVariants().get(0);
                                        if (CollectionUtils.isNotEmpty(firstVariant.getPrices())) {
                                            List<String> tiers = firstVariant.getPrices().get(0).getPriceTier();
                                            priceTier = CollectionUtils.isNotEmpty(tiers) ? tiers.get(0) : null;
                                        }
                                        final String resolvedPriceTier = priceTier;
                                        product.getObj().getVariants().stream()
                                                .filter(Objects::nonNull)
                                                .filter(variant -> variant.getAttributes() != null)
                                                .forEach(variant -> {
                                                    // Build variant-level 3D map; merge with offer-level (offer overrides variant).
                                                    Map<String, String> variantLevel3DMap =
                                                            getGlobalMessages3DMap(variant.getAttributes().getGlobalMessagesByKey());
                                                    if (variantLevel3DMap.isEmpty() && offerLevel3DMap.isEmpty()) {
                                                        if(salesChannel.equalsIgnoreCase(Constants.DTV360) || salesChannel.equalsIgnoreCase(Constants.UVC)){
                                                            variant.getAttributes().setDisclosureMessagesByKey(null);
                                                            variant.getAttributes().setDescriptionsByKey(null);
                                                            variant.getAttributes().setDisplayNamesByKey(null);
                                                        }
                                                        return;
                                                    }
                                                    Map<String, String> map3D = new HashMap<>(variantLevel3DMap);
                                                    map3D.putAll(offerLevel3DMap); // offer overrides variant
                                                    if (hasGlobalMessages3DForType(map3D, Constants.MESSAGES_BY_KEY_TYPE_DESCRIPTION)) {
                                                        applyGlobalMessages3DToDescription(variant, map3D, salesChannel, flowType, resolvedPriceTier);
                                                    }else if(salesChannel.equalsIgnoreCase(Constants.DTV360) || salesChannel.equalsIgnoreCase(Constants.UVC)){
                                                        variant.getAttributes().setDescriptionsByKey(null);
                                                    }
                                                    if (hasGlobalMessages3DForType(map3D, Constants.MESSAGES_BY_KEY_TYPE_DISPLAY_NAME)) {
                                                        applyGlobalMessages3DToDisplayName(variant, map3D, salesChannel, flowType, resolvedPriceTier);
                                                    }else if(salesChannel.equalsIgnoreCase(Constants.DTV360) || salesChannel.equalsIgnoreCase(Constants.UVC)){
                                                        variant.getAttributes().setDisplayNamesByKey(null);
                                                    }
                                                    if (hasGlobalMessages3DForType(map3D, Constants.MESSAGES_BY_KEY_TYPE_DISCLOSURE)) {
                                                        applyGlobalMessages3DToDisclosure(variant, map3D, salesChannel, flowType, resolvedPriceTier);
                                                    }else if(salesChannel.equalsIgnoreCase(Constants.DTV360) || salesChannel.equalsIgnoreCase(Constants.UVC)){
                                                        variant.getAttributes().setDisclosureMessagesByKey(null);
                                                    }
                                                });
                                    }));
                });
    }

    /**
     * Returns true if messageType is one of the 3D types: Disclosure, Description or DisplayName.
     */
    public boolean is3DMessageType(String messageType) {
        return messageType != null && (
                messageType.equalsIgnoreCase(Constants.MESSAGES_BY_KEY_TYPE_DISCLOSURE)
                        || messageType.equalsIgnoreCase(Constants.MESSAGES_BY_KEY_TYPE_DESCRIPTION)
                        || messageType.equalsIgnoreCase(Constants.MESSAGES_BY_KEY_TYPE_DISPLAY_NAME)
        );
    }

    /**
     * Returns true if the 3D map contains at least one entry for the given messageType prefix.
     */
    public boolean hasGlobalMessages3DForType(Map<String, String> map3D, String messageType) {
        if (map3D == null || map3D.isEmpty() || messageType == null) return false;
        String prefix = messageType.toLowerCase();
        return map3D.keySet().stream().anyMatch(k -> k.startsWith(prefix));
    }

    /**
     * Resolves a 3D value from the map using priceTier priority:
     * <ol>
     *   <li>Exact priceTier match (e.g. PT100, PT101) when priceTier is non-null.</li>
     *   <li>Catch-all (blank priceTier) entry as fallback.</li>
     * </ol>
     * Returns null if no match – leaving the variant field unchanged.
     */
     public String resolve3DValue(Map<String, String> map3D, String type, String salesChannel,
                                  String flowType, String priceTier, String msgKey) {
        if (priceTier != null && !priceTier.isEmpty()) {
            String specificVal = map3D.get((type + salesChannel + flowType + priceTier + msgKey).toLowerCase());
            if (specificVal != null) return specificVal;
        }
        return map3D.get((type + salesChannel + flowType + "" + msgKey).toLowerCase());
    }

    /**
     * Overrides {@code disclosure} and {@code shortDisclosure} on the variant's
     * {@code DisclosureMessagesByKey} with 3D values resolved from the map.
     * Called AFTER BAU disclosure so BAU-only fields (optIn, optOut, autoRenew, selectAllDisclosure) are preserved.
     * No-op if neither long nor short value resolves from the map.
     */
    public void applyGlobalMessages3DToDisclosure(Variant variant, Map<String, String> map3D,
                                                  String salesChannel, String flowType, String priceTier) {
        String longDisclosure = resolve3DValue(map3D, Constants.MESSAGES_BY_KEY_TYPE_DISCLOSURE,
                salesChannel, flowType, priceTier, Constants.LONG_MESSAGE);
        String shortDisclosure = resolve3DValue(map3D, Constants.MESSAGES_BY_KEY_TYPE_DISCLOSURE,
                salesChannel, flowType, priceTier, Constants.SHORT_MESSAGE);
        if (longDisclosure != null || shortDisclosure != null) {
            if (variant.getAttributes().getDisclosureMessagesByKey() == null || (salesChannel.equalsIgnoreCase(Constants.DTV360) || salesChannel.equalsIgnoreCase(Constants.UVC))) {
                variant.getAttributes().setDisclosureMessagesByKey(new DisclosureMessagesByKey());
            }
            if (longDisclosure != null) {
                variant.getAttributes().getDisclosureMessagesByKey().setDisclosure(longDisclosure);
            }
            if (shortDisclosure != null) {
                variant.getAttributes().getDisclosureMessagesByKey().setShortDisclosure(shortDisclosure);
            }
        }else if(salesChannel.equalsIgnoreCase(Constants.DTV360) || salesChannel.equalsIgnoreCase(Constants.UVC)){
            variant.getAttributes().setDisclosureMessagesByKey(null);
        }
    }

    /**
     * Overrides {@code longDesc} and {@code shortDesc} on the variant's
     * {@code ProductDescriptionsByKey} with 3D values resolved from the map.
     * No-op if neither long nor short value resolves from the map.
     */
    public void applyGlobalMessages3DToDescription(Variant variant, Map<String, String> map3D,
                                                   String salesChannel, String flowType, String priceTier) {
        String longDesc = resolve3DValue(map3D, Constants.MESSAGES_BY_KEY_TYPE_DESCRIPTION,
                salesChannel, flowType, priceTier, Constants.LONG_MESSAGE);
        String shortDesc = resolve3DValue(map3D, Constants.MESSAGES_BY_KEY_TYPE_DESCRIPTION,
                salesChannel, flowType, priceTier, Constants.SHORT_MESSAGE);
        if (longDesc != null || shortDesc != null) {
            if (variant.getAttributes().getDescriptionsByKey() == null || (salesChannel.equalsIgnoreCase(Constants.DTV360) || salesChannel.equalsIgnoreCase(Constants.UVC))) {
                variant.getAttributes().setDescriptionsByKey(new ProductDescriptionsByKey());
            }
            if (longDesc != null) {
                variant.getAttributes().getDescriptionsByKey().setLongDesc(longDesc);
            }
            if (shortDesc != null) {
                variant.getAttributes().getDescriptionsByKey().setShortDesc(shortDesc);
            }
        }else if(salesChannel.equalsIgnoreCase(Constants.DTV360) || salesChannel.equalsIgnoreCase(Constants.UVC)){
            variant.getAttributes().setDescriptionsByKey(null);
        }
    }

    /**
     * Overrides {@code longDisplayName} and {@code shortDisplayName} on the variant's
     * {@code GenericByKey} with 3D values resolved from the map.
     * No-op if neither long nor short value resolves from the map.
     */
    public void applyGlobalMessages3DToDisplayName(Variant variant, Map<String, String> map3D,
                                                   String salesChannel, String flowType, String priceTier) {
        String longDisplayName = resolve3DValue(map3D, Constants.MESSAGES_BY_KEY_TYPE_DISPLAY_NAME,
                salesChannel, flowType, priceTier, Constants.LONG_MESSAGE);
        String shortDisplayName = resolve3DValue(map3D, Constants.MESSAGES_BY_KEY_TYPE_DISPLAY_NAME,
                salesChannel, flowType, priceTier, Constants.SHORT_MESSAGE);
        if (longDisplayName != null || shortDisplayName != null) {
            if (variant.getAttributes().getDisplayNamesByKey() == null || (salesChannel.equalsIgnoreCase(Constants.DTV360) || salesChannel.equalsIgnoreCase(Constants.UVC))) {
                variant.getAttributes().setDisplayNamesByKey(new GenericByKey());
            }
            if (longDisplayName != null) {
                variant.getAttributes().getDisplayNamesByKey().setLongDisplayName(longDisplayName);
            }
            if (shortDisplayName != null) {
                variant.getAttributes().getDisplayNamesByKey().setShortDisplayName(shortDisplayName);
            }
        }else if(salesChannel.equalsIgnoreCase(Constants.DTV360) || salesChannel.equalsIgnoreCase(Constants.UVC)){
            variant.getAttributes().setDisplayNamesByKey(null);
        }
    }

    /**
     * GET /products – applies 3D GlobalMessages (Description, DisplayName, Disclosure) to one variant.
     * Reads {@code globalMessagesByKey} directly from the variant (no offer-level override).
     * {@code priceTier} comes from {@code customerContext.ott.products[i].priceTier};
     * pass null to match only catch-all (blank priceTier) CT entries.
     * No-op when variant has no globalMessagesByKey or the parsed map is empty.
     */
    public void processGlobalMessages3DForProductVariant(Variant variant, String salesChannel, String flowType, String priceTier) {
        if (variant == null || variant.getAttributes() == null
                || CollectionUtils.isEmpty(variant.getAttributes().getGlobalMessagesByKey())) {
            if(salesChannel.equalsIgnoreCase(Constants.DTV360) || salesChannel.equalsIgnoreCase(Constants.UVC)){
                variant.getAttributes().setDisclosureMessagesByKey(null);
                variant.getAttributes().setDescriptionsByKey(null);
                variant.getAttributes().setDisplayNamesByKey(null);
            }
            return;
        }
        Map<String, String> map3D = getGlobalMessages3DMap(variant.getAttributes().getGlobalMessagesByKey());
        if (map3D.isEmpty()) {
            return;
        }
        if (hasGlobalMessages3DForType(map3D, Constants.MESSAGES_BY_KEY_TYPE_DISCLOSURE)) {
            applyGlobalMessages3DToDisclosure(variant, map3D, salesChannel, flowType, priceTier);
        }else if(salesChannel.equalsIgnoreCase(Constants.DTV360) || salesChannel.equalsIgnoreCase(Constants.UVC)){
            variant.getAttributes().setDisclosureMessagesByKey(null);
        }
        if (hasGlobalMessages3DForType(map3D, Constants.MESSAGES_BY_KEY_TYPE_DESCRIPTION)) {
            applyGlobalMessages3DToDescription(variant, map3D, salesChannel, flowType, priceTier);
        }else if(salesChannel.equalsIgnoreCase(Constants.DTV360) || salesChannel.equalsIgnoreCase(Constants.UVC)){
            variant.getAttributes().setDescriptionsByKey(null);
        }
        if (hasGlobalMessages3DForType(map3D, Constants.MESSAGES_BY_KEY_TYPE_DISPLAY_NAME)) {
            applyGlobalMessages3DToDisplayName(variant, map3D, salesChannel, flowType, priceTier);
        }else if(salesChannel.equalsIgnoreCase(Constants.DTV360) || salesChannel.equalsIgnoreCase(Constants.UVC)){
            variant.getAttributes().setDisplayNamesByKey(null);
        }
    }

    /**
     * GET /products – applies 3D GlobalMessages to all variants of a single product.
     * Delegates to {@link #processGlobalMessages3DForProductVariant} for each variant.
     */
    public void processGlobalMessages3DForProducts(ProductObj productObj, String salesChannel, String flowType, String priceTier) {
        Optional.ofNullable(productObj.getVariants())
                .filter(CollectionUtils::isNotEmpty)
                .ifPresent(variants -> variants.stream()
                        .filter(Objects::nonNull)
                        .forEach(variant -> processGlobalMessages3DForProductVariant(variant, salesChannel, flowType, priceTier)));
    }

    /**
     * GET /products – applies 3D GlobalMessages (Description, DisplayName, Disclosure) to every product variant.
     * Must be called AFTER {@code filterPricesByPriceTier} so priceTier is already resolved.
     * {@code salesChannel} must not be null; pass {@code Constants.DEFAULT} when absent in the request.
     * IXP flag guard is the caller's responsibility.
     */
    public void applyGlobalMessages3DForProducts(CTProductResponse response, String salesChannel, String flowType) {
        if (response == null || CollectionUtils.isEmpty(response.getProducts())) {
            return;
        }
        response.getProducts().stream()
                .filter(Objects::nonNull)
                .filter(productObj -> CollectionUtils.isNotEmpty(productObj.getVariants()))
                .forEach(productObj -> {
                    String priceTier = null;
                    Variant firstVariant = productObj.getVariants().get(0);
                    if (firstVariant != null && CollectionUtils.isNotEmpty(firstVariant.getPrices())) {
                        List<String> priceTiers = firstVariant.getPrices().get(0).getPriceTier();
                        priceTier = CollectionUtils.isNotEmpty(priceTiers) ? priceTiers.get(0) : null;
                    }
                    processGlobalMessages3DForProducts(productObj, salesChannel, flowType, priceTier);
                });
    }

    /**
     * Determines if the request's price protection window matches any of the configured HOG windows.
     * @param priceProtection
     */
    public boolean isConfiguredHogPriceProtectionWindow(com.dtv.dcp.epoch.model.ct.request.PriceProtection priceProtection) {
        if (Objects.isNull(priceProtection)
                || !org.springframework.util.StringUtils.hasText(priceProtection.getStartDate())
                || !org.springframework.util.StringUtils.hasText(priceProtection.getEndDate())) {
            return false;
        }

        if (!usesConfiguredHogWindow(priceProtection)) {
            return true;
        }

        String hogDatesConfigKey = "HOG-DATES";
        List<String> hogDateRanges = getConfigListValues(hogDatesConfigKey);

        boolean isMatch = CollectionUtils.isNotEmpty(hogDateRanges)
                && hogDateRanges.stream()
                .filter(org.springframework.util.StringUtils::hasText)
                .map(range -> range.split(":"))
                .anyMatch(parts -> parts.length == 2
                        && Objects.equals(priceProtection.getStartDate(), parts[0])
                        && Objects.equals(priceProtection.getEndDate(), parts[1]));


        return isMatch;
    }

    /**
     * Determines if the price protection's contract indicators include any of the configured HOG indicators.
     * @param priceProtection
     * @return
     */
    public boolean usesConfiguredHogWindow(com.dtv.dcp.epoch.model.ct.request.PriceProtection priceProtection) {
        return Optional.ofNullable(priceProtection.getContractIndicator())
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .anyMatch(indicator -> Constants.TAZCONTRACT_STRING.equalsIgnoreCase(indicator)
                        || Constants.ROAD_RUNNER.equalsIgnoreCase(indicator));
    }

    /**
     * Checks if a product should be excluded from delay provisioning.
     * A product is excluded if its code is already in the customerContext, or if its
     * productGroup matches the productGroup of any product on the account (skip associated tiers).
     *
     * @param productCode                the product code to evaluate
     * @param productGroup               the productGroup of the product to evaluate (from CT variant attributes)
     * @param customerContextProductCodes set of product codes from request.customerContext
     * @param accountProductGroups     set of productGroups resolved from CT response for customerContext products
     * @return true if the product should be excluded from delay provisioning
     */
    public boolean isProductActiveOrSameGroup(String productCode, String productGroup,
                                              Set<String> customerContextProductCodes, Set<String> accountProductGroups) {
        if (customerContextProductCodes.contains(productCode)) {
            return true;
        }
        return productGroup != null && accountProductGroups.contains(productGroup);
    }

    /**
     * Extracts product codes from customerContext.ott.products.
     *
     * @param ottProducts list of products from request.customerContext.ott.products
     * @return set of product codes from customerContext
     */
    public Set<String> collectCustomerContextProductCodes(List<ProductInfo> ottProducts) {
        if (CollectionUtils.isEmpty(ottProducts)) {
            return Collections.emptySet();
        }
        return ottProducts.stream()
                .filter(Objects::nonNull)
                .map(ProductInfo::getProductCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }


    /**
     * Resolves productGroup values for customerContext products by looking up their productGroup
     * from the CT product response (variant.attributes.productGroup).
     *
     * @param customerContextProductCodes product codes from request.customerContext.ott.products
     * @param responseProducts                 all CT products from the product API response
     * @return set of productGroup values for customerContext products
     */
    public Set<String> resolveProductGroupsFromCTResponse(Set<String> customerContextProductCodes, List<ProductObj> responseProducts) {
        if (customerContextProductCodes.isEmpty() || CollectionUtils.isEmpty(responseProducts)) {
            return Collections.emptySet();
        }
        return responseProducts.stream()
                .filter(Objects::nonNull)
                .filter(p -> customerContextProductCodes.contains(p.getCode()))
                .flatMap(p -> Optional.ofNullable(p.getVariants()).orElse(Collections.emptyList()).stream())
                .filter(Objects::nonNull)
                .map(Variant::getAttributes)
                .filter(Objects::nonNull)
                .map(Attributes::getProductGroup)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

}

