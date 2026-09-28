package com.dtv.dcp.epoch.integration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;

@Component
public class OfferFilterServiceUtil {
	
	private static final Logger log = LoggerFactory.getLogger(OfferFilterServiceUtil.class);
	
	public int compareDates(String dateString, String compareToDateString) throws ParseException {
		Date date=dateString==null?null:formatDate(dateString);
		Date compareToDate=compareToDateString==null?null:formatDate(compareToDateString);
		if(date!=null && compareToDate!=null)
		{
			return date.compareTo(compareToDate);
		}
		else if(date==null) {
			return -1;
		}
		else if(compareToDate==null) {
			return 1;
		}
		return -1;
	}
	
	public int compareToCurrentDate(String dateString) throws ParseException {
		
		SimpleDateFormat sdf=new SimpleDateFormat(Constants.DATE_FORMAT);
		Date date=dateString==null?null:formatDate(dateString);
		
		if(date!=null)
		{
			return date.compareTo(sdf.parse(sdf.format(new Date())));
		}
		return -1;
	}
	
	private Date formatDate(String dateString)
	{
		List<String> formatStrings = Arrays.asList("MM-dd-yyyy","MM/dd/yyyy");
		
		for(String formatString: formatStrings) {
			try {
				return (new SimpleDateFormat(formatString)).parse(dateString);
			}
			catch(ParseException ex) {}
		}
		return null;
	}

}
