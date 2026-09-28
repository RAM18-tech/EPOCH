package com.dtv.dcp.epoch.exception;

import java.util.List;

import org.springframework.web.client.RestClientException;



public class ServiceException extends RuntimeException {
  private static final long serialVersionUID = -4106321769337595782L;
  
  private final int httpCode;
    
  private final ServiceError error;
  

  public ServiceException(ResolvableErrorEnum errorResource, String... args) {
    super(ResourceManager.formatMessage(errorResource, null, null, args));
    this.httpCode = ResourceManager.getHttpCode(errorResource);
    this.error = createServiceError(errorResource, args);
  }
  
  public ServiceException(ResolvableErrorEnum errorResource, Throwable cause, String... args) {
    super(ResourceManager.formatMessage(errorResource, cause, null, args), cause);
    this.httpCode = ResourceManager.getHttpCode(errorResource);
    this.error = createServiceError(errorResource, args);
  }
  
  public ServiceException(ResolvableErrorEnum errorResource, String message, Throwable cause, String... args) {
    super(ResourceManager.formatMessage(errorResource, cause, message, args), cause);
    this.httpCode = ResourceManager.getHttpCode(errorResource);
    this.error = createServiceError(errorResource, args);
  }
  
  private ServiceError createServiceError(ResolvableErrorEnum errorResource, String... args) {
    String errorId = ResourceManager.getIdentifier(errorResource);
    String message = ResourceManager.getMessage(errorResource, args);
    return new ServiceError(errorId, message);
  }
  
  public ServiceException(ResolvableErrorEnum errorResource, RestClientException re) {
	    super(ResourceManager.formatMessage(errorResource, null, null, re.getMessage()));
	    this.httpCode = ResourceManager.getHttpCode(errorResource);
	    this.error = createServiceError(errorResource, re.getMessage());
	  }
  
  
  public static long getSerialversionuid() {
    return -4106321769337595782L;
  }
  
  public int getHttpCode() {
    return this.httpCode;
  }
  
  public ServiceError getError() {
    return this.error;
  }
  
  public ServiceException addDetail(ResolvableErrorEnum detail, String... args) {
    getError().addDetail(detail, args);
    return this;
  }
  
  public ServiceException addDetails(List<ServiceError.Error> details) {
    if (details != null)
      details.forEach(errorDetail -> getError().addDetail(errorDetail)); 
    return this;
  }
}
