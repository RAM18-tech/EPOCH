package com.dtv.dcp.epoch.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.dtv.dcp.epoch.model.ct.response.EvergentContract;

@Repository
public class AccountDataRepositoryImpl implements AccountDataRepository {
	
	private static final Logger log = LoggerFactory.getLogger(AccountDataRepositoryImpl.class);
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Value("${postgres.accountdatatablename-geminiairpilot_contract}")
	private String accountDataTableNamegeminiairpilotContract;
	
	@Value("${postgres.accountdatatablename-geminiairpilot_edsp}")
	private String accountDataTableNamegeminiairpilotEdsp;
	
	public List<String> fetchEligibleOTTOffersForAccount(String accountNum, List<String> targetedOffers) {
		log.info("Start of AccountDataRepositoryImpl.fetchEligibleOTTOffersForAccount() method..");
		List<String> eligibleOffers = new ArrayList<>();
		try {
			AtomicReference<Boolean> dbCallRequired = new AtomicReference<>();
			StringBuilder query = new StringBuilder();
			List<Object> accountNums = new ArrayList<>();
			targetedOffers.stream().filter(Objects::nonNull).forEach(offerCode -> {
				if (query.length() > 0) {
					query.append(" UNION ");
				}
				if ("OF_GEMINIAIRPILOT_Contract_1".equalsIgnoreCase(offerCode)) {
					dbCallRequired.set(true);
					query.append(prepareQuery(accountDataTableNamegeminiairpilotContract));
					accountNums.add(accountNum);
				}
				if ("OF_GEMINIAIRPILOT_EDSP_1".equalsIgnoreCase(offerCode)) {
					dbCallRequired.set(true);
					query.append(prepareQuery(accountDataTableNamegeminiairpilotEdsp));
					accountNums.add(accountNum);
				}
			});
			query.append(";");
			log.info("fetchEligibleOTTOffersForAccount SQL QUERY:::{}", query);
			if(Boolean.TRUE.equals(dbCallRequired.get()))
			{
				eligibleOffers = jdbcTemplate.queryForList(query.toString(), accountNums.toArray(), String.class);
			}
		} catch (Exception e) {
			log.error("Exception occred while fetchEligibleOTTOffersForAccount", e);
		}
		log.info("End of AccountDataRepositoryImpl.fetchEligibleOTTOffersForAccount() method.." + eligibleOffers);
		return eligibleOffers;
	}
	
	private String prepareQuery(String tableName) {
		StringBuilder query = new StringBuilder("SELECT OFFER_ID FROM ");
		query.append(tableName);
		query.append(" WHERE ban = ?");
		return query.toString();
	}

	@Override
	public
	List<EvergentContract> fetchTenantData(String evergentPropertyID) {
		StringBuilder query = new StringBuilder(
				"SELECT * FROM ");
		query.append("epochoff_app.dmp_contract_details");
		query.append(" WHERE evergent_property_id=?");
		query.append(";");
		//log.info("EvergentPropertyID :: {}", ESAPI.encoder().encodeForHTML(evergentPropertyID));
		log.info("fetchEvergentPropertyIDData SQL QUERY:::{}", query);
		long startTimeMillis = System.currentTimeMillis();
		List<EvergentContract> propertyData =  jdbcTemplate.query(query.toString(), new DmpDataMapper(),evergentPropertyID);
		log.info("TOTAL_TIME_TAKEN_FROM_TABLE_TO_FETCH-[{}]", System.currentTimeMillis()-startTimeMillis);
		return propertyData;
	}

	@Override
	public Boolean isAccountNumberExistsInBanLookupTable(String accountNumber, String banLookupTableName) {

		Boolean isAccountNumberExists = false;
		try{
			StringBuilder query = new StringBuilder("SELECT EXISTS(SELECT * FROM epochoff_app.");
			query.append(banLookupTableName);
			query.append(" WHERE ban = ?);");
			log.info("isAccountNumberExistsInBanLookupTable SQL QUERY:::{}", query);
			isAccountNumberExists = jdbcTemplate.queryForObject(query.toString(), new Object[] { accountNumber }, Boolean.class);
		}catch (Exception e) {
			log.error("Exception occurred in method isAccountNumberExistsInBanLookupTable ", e);
		}
		return isAccountNumberExists;
	}
}
