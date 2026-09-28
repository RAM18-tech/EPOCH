package com.dtv.dcp.epoch.message;

import com.dtv.dcp.epoch.exception.ResolvableErrorEnum;
import com.dtv.dcp.epoch.exception.ResourceManager;

/**
 * This enum consists of Log messages for all resolvable EELF error codes
 *  
 */
public enum LogMessages implements ResolvableErrorEnum {

	RESTSERVICE_HELLO, RESTSERVICE_HELLO_NAME, SPRINSERVICE_HELLO, SPRINSERVICE_HELLO_NAME, 
	SPRINSERVICE_HELLO_MESSAGE, SPRINSERVICE_HELLO_MESSAGE_NAME, RESTSERVICE_SAY_HELLO;

	static {

		ResourceManager.loadMessageBundle("logmessages");

	}

}