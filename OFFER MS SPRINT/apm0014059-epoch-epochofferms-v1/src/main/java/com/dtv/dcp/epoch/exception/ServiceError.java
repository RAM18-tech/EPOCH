package com.dtv.dcp.epoch.exception;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceError implements Serializable {
  private static final long serialVersionUID = 1L;
  
  private String errorId;
  
  private String message;
  
  private List<Error> details;
  
  ServiceError() {}
  
  public ServiceError(String errorId, String message) {
    setErrorId(errorId);
    setMessage(message);
  }
  
  void setErrorId(String errorId) {
    this.errorId = errorId;
  }
  
  void setMessage(String message) {
    this.message = message;
  }
  
  void setDetails(List<Error> details) {
    this.details = details;
  }
  
  public static long getSerialversionuid() {
    return 1L;
  }
  
  public String getErrorId() {
    return this.errorId;
  }
  
  public String getMessage() {
    return this.message;
  }
  
  public List<Error> getDetails() {
    return this.details;
  }
  
  void addDetail(Error error) {
    if (this.details == null)
      this.details = new ArrayList<>(); 
    this.details.add(error);
  }
  
  void addDetail(ResolvableErrorEnum detail, String... args) {
    String code = ResourceManager.getIdentifier(detail);
    String msg = ResourceManager.getMessage(detail, args);
    addDetail(new Error(code, msg));
  }
  
  public static class Error implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String code;
    
    private String message;
    
    Error() {}
    
    public Error(String code, String message) {
      setCode(code);
      setMessage(message);
    }
    
    void setCode(String code) {
      this.code = code;
    }
    
    void setMessage(String message) {
      this.message = message;
    }
    
    public String getCode() {
      return this.code;
    }
    
    public String getMessage() {
      return this.message;
    }
  }
}
