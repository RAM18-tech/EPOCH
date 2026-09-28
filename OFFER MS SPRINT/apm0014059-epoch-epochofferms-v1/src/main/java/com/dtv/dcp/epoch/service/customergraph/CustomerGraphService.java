package com.dtv.dcp.epoch.service.customergraph;


import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.model.customergraph.response.CGResponse;


public interface CustomerGraphService {

CGResponse getActiveSubscriptions(String accountId,String isCTBackUpenabled) throws ServiceException;
	
	public <T> T getUverseAccountProducts(String customerId, String accountId, String accountType,
			Boolean includeAssignedProductDetails, Class<T> responseClass,String isCTBackUpenabled) throws ServiceException;
	
	public <T> T getUverseCustomerAccounts(String customerId, String accountId, String accountType, Class<T> responseClass,String isCTBackUpenabled) throws ServiceException;
	
	public <T> T getUverseCustomerCoupons(String customerId, String accountId, String accountType, Class<T> responseClass,String isCTBackUpenabled) throws ServiceException;
}
