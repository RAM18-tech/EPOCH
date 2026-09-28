package com.dtv.dcp.epoch.model.ct.offer;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FieldDetails {

	private Class<?> type;
	private Object value;
	
	private static final Logger log = LoggerFactory.getLogger(FieldDetails.class);
	
	public FieldDetails(Object target, String fieldName) {
		PropertyUtilsBean pub=new PropertyUtilsBean();
		try {
			if(fieldName!=null && !fieldName.isBlank())
			{
				this.value=pub.getProperty(target, fieldName);
				this.type=pub.getPropertyType(target, fieldName);
			}
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | NestedNullException e) {
			log.error(String.format("getOffersFromSource::: %s", e.getMessage()));
		}
	}
	
	public Class<?> getType(){
		return type;
	}
	
	public Object getValue() {
		return value;
	}
	
}

