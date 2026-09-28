package com.dtv.dcp.epoch.representation;

import java.io.Serializable;

import com.dtv.dcp.epoch.exception.ServiceError;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Error implements Serializable {
  private static final long serialVersionUID = 1L;
  
  private Links links;
  
  private ServiceError error;
  
  public Error(ServiceError error) {
    setError(error);
  }
  
  private void setError(ServiceError error) {
    this.error = error;
  }
  
  public ServiceError getError() {
    return this.error;
  }
  
  public Links getLinks() {
    return this.links;
  }
  
  public void setLinks(Links links) {
    this.links = links;
  }
}
