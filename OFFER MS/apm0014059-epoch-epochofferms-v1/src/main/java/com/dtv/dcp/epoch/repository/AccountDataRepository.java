package com.dtv.dcp.epoch.repository;

import java.util.List;

import com.dtv.dcp.epoch.model.ct.response.EvergentContract;

public interface AccountDataRepository {
	
	List<EvergentContract> fetchTenantData(String evergentPropertyID);

	List<String> fetchEligibleOTTOffersForAccount(String accountNum, List<String> targetedOffers);

	Boolean isAccountNumberExistsInBanLookupTable(String accountNumber, String banLookupTableName);
	
}
