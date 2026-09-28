package com.dtv.dcp.epoch.message;

import com.dtv.dcp.epoch.exception.ResolvableErrorEnum;
import com.dtv.dcp.epoch.exception.ResourceManager;

public enum TestMessages implements ResolvableErrorEnum{
	TEST_MESSAGE,
	 /**
     * Application initialization started at {0}
     */
	MESSAGE_CONFIGURATION_STARTED,

    /**
     * Prior configuration has been cleared
     */
	MESSAGE_CONFIGURATION_CLEARED,
   

    /**
     * User {0} not found.
     */
  
    ERROR_USER_NOT_FOUND,

	/**
     * Service {0} is not running
     */
    ERROR_SERVICE_NOT_RUNNING,
    
    /**
     * Sample warn msg
     */
    WARN_MSG,
	
	
	/**
     * Sample debug msg
     */
    DEBUG_MSG,
    
    /**
     * Sample trace msg
     */
    TRACE_MSG,
    
    
	
	/**
     * Sample error exception
     */
    MY_SAMPLE_EXCEPTION;
	
	static {
		ResourceManager.loadMessageBundle("testmessages");
	}
}

