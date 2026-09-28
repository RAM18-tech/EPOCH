package com.dtv.dcp.epoch.exception;

import java.util.Arrays;

public class ExternalServiceException extends ServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ExternalServiceException(ResolvableErrorEnum errorResource, Throwable cause, String... args) {
		super(errorResource, cause);
		ServiceError.Error error = new ServiceError.Error(cause.getClass().getSimpleName(), cause.getMessage());
		addDetails(Arrays.asList(error));
	}

	public ExternalServiceException(ResolvableErrorEnum errorResource) {
		super(errorResource);
	}

}
