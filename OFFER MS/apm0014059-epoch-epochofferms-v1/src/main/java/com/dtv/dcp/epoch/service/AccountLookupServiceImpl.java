package com.dtv.dcp.epoch.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dtv.dcp.epoch.repository.AccountDataRepository;

@Service
public class AccountLookupServiceImpl implements AccountLookupService {
	
	/** The Constant log. */
	private static final Logger log = LoggerFactory.getLogger(AccountLookupServiceImpl.class);

	@Autowired
	private AccountDataRepository accountDataRespository;
	
	@Override
	public List<String> fetchEligibleOffersForOTTAccount(String accountNum, List<String> targetedOffers) {
		log.debug("Inside AccountLookupServiceImpl.fetchEligibleOffersForOTTAccount() method..");

		return accountDataRespository.fetchEligibleOTTOffersForAccount(accountNum, targetedOffers);

	}

	@Override
	public Boolean isAccountNumberExistsInBanLookupTable(String accountNumber, String banLookupTableName) {
		return accountDataRespository.isAccountNumberExistsInBanLookupTable(accountNumber, banLookupTableName);
	}

}