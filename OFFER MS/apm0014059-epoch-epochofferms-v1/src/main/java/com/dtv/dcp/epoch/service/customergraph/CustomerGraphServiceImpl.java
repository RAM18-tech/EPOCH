package com.dtv.dcp.epoch.service.customergraph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.customergraph.CustomerGraphClient;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.customergraph.response.CGResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@Component(CustomerGraphServiceImpl.COMPONENT_NAME)
public class CustomerGraphServiceImpl implements CustomerGraphService {

	/** The log. */
	private static Logger log = LoggerFactory.getLogger(CustomerGraphServiceImpl.class);

	public static final String COMPONENT_NAME = "CustomerGraph";


	/** The CpopClient. */
	@Autowired
	CustomerGraphClient customerGraphClient;


	@Cacheable(value = Constants.CPOPOFFERSMS_PROFILE_NEAR_CACHE_MAP_NAME, key = "{ 'epochCGProfileProduct', #isCTBackUpenabled, #accountId }" , unless="#result==null", cacheManager="redisCacheManagerFirstHolder")
	@Override
	public CGResponse getActiveSubscriptions(String accountId,String isCTBackUpenabled) throws ServiceException {
		CGResponse response = null;
		JsonNode serviceResponse = null;
		log.info("getActiveSubscriptions");
		try {
			// JsonNode serviceResponse = customerGraphClient.getAccountAndServiceDetails(accountId);
			serviceResponse = customerGraphClient.getAccountById(accountId);
			ObjectMapper mapper = new ObjectMapper();

			// CGResponse content = serviceResponse.at("/content");
			response = mapper.convertValue(serviceResponse.at("/content"), new TypeReference<CGResponse>() {
			});
			return response; 
		} catch (Exception e) {
			//log.info("getActiveSubscriptions - > accountId:: {}", ESAPI.encoder().encodeForHTML(accountId));
			if (serviceResponse != null) {
				//log.info("getActiveSubscriptions - > serviceResponse:: "+ ESAPI.encoder().encodeForHTML(serviceResponse.toString()));
			}
			//log.error("getActiveSubscriptions:: "+e.getMessage());
			ServiceException ex = new ServiceException(ErrorMessages.ACCOUNT_NUMBER_NOT_FOUND, "getActiveSubscriptions: "+e.getMessage());
			ex.addDetail(ErrorMessages.ACCOUNT_NUMBER_NOT_FOUND, "EPOCH", accountId);			
			throw ex;
		}
	}

	@Cacheable(value = Constants.CPOPOFFERSMS_PROFILE_NEAR_CACHE_MAP_NAME, key = "{ 'epochCGProfileProduct', #isCTBackUpenabled, #accountId }" , unless="#result==null" , cacheManager="redisCacheManagerFirstHolder")
	@Override
	public <T> T getUverseAccountProducts(String customerId, String accountId, String accountType,
										  Boolean includeAssignedProductDetails, Class<T> responseClass,String isCTBackUpenabled) throws ServiceException {
		try {

			log.info("getUverseAccountProducts");
			T temp = customerGraphClient.getUverseAccountProducts(customerId, accountId, accountType,
					includeAssignedProductDetails, responseClass);
			log.debug("@@@@End getUverseAccountProducts");
			return temp;
		} catch (Exception e) {
			ServiceException ex = new ServiceException(ErrorMessages.ACCOUNT_NUMBER_NOT_FOUND,
					"getUverseAccountProducts: " + e.getMessage());
			ex.addDetail(ErrorMessages.ACCOUNT_NUMBER_NOT_FOUND, "EPOCH", accountId);
			throw ex;
		}
	}

	@Cacheable(value = Constants.CPOPOFFERSMS_PROFILE_NEAR_CACHE_MAP_NAME, key = "{ 'epochCGCustomerAccountProduct', #isCTBackUpenabled, #accountId }" , unless="#result==null" , cacheManager="redisCacheManagerFirstHolder")
	@Override
	public <T> T getUverseCustomerAccounts(String customerId, String accountId, String accountType,
			Class<T> responseClass, String isCTBackUpenabled) throws ServiceException {
		try {

			log.info("getUverseCustomerAccounts start....");
			T temp = customerGraphClient.getUverseCustomerAccounts(customerId, accountId, accountType, responseClass);
			log.debug("@@@@End getUverseCustomerAccounts");
			return temp;
		} catch (Exception e) {
			ServiceException ex = new ServiceException(ErrorMessages.ACCOUNT_NUMBER_NOT_FOUND,
					"getUverseAccountProducts: " + e.getMessage());
			ex.addDetail(ErrorMessages.ACCOUNT_NUMBER_NOT_FOUND, "EPOCH", accountId);
			throw ex;
		}
	}

	@Override
	public <T> T getUverseCustomerCoupons(String customerId, String accountId, String accountType,
		Class<T> responseClass, String isCTBackUpenabled) throws ServiceException {
		try {

			log.info("getUverseCustomerAccounts start....");
			T temp = customerGraphClient.getUverseCustomerCoupons(customerId, accountId, accountType, responseClass);
			log.debug("@@@@End getUverseCustomerAccounts");
			return temp;
		} catch (Exception e) {
			ServiceException ex = new ServiceException(ErrorMessages.ACCOUNT_NUMBER_NOT_FOUND,
					"getUverseAccountProducts: " + e.getMessage());
			ex.addDetail(ErrorMessages.ACCOUNT_NUMBER_NOT_FOUND, "EPOCH", accountId);
			throw ex;
		}
	}
}