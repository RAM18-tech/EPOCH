package com.dtv.dcp.epoch.service;

import java.util.List;

/**
 * The Interface AccountLookupService.
 */
public interface AccountLookupService {

	public List<String> fetchEligibleOffersForOTTAccount(String accountNum, List<String> targetedOffers);

	public Boolean isAccountNumberExistsInBanLookupTable(String accountNumber, String banLookupTableName);


}