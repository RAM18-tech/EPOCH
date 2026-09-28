package com.dtv.dcp.epoch.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.model.ct.request.PriceProtection;

@Component
public class PnpGroupUtils {

	private static final Logger log = LoggerFactory.getLogger(PnpGroupUtils.class);

	@Autowired
	RedisCacheHelper redisCacheHelper;
	
	public static final String PNPTAZCONTRACTGRANDFATHERED = "PP-TAZCONTRACT-GRANDFATHERED";
	
	public static final String DASHPNP = "-PNP";

	public String getPnpGroup(PriceProtection priceProtection, String nbcd, String productFamily) {
		String pnpGroup = null;
		try {
			log.debug("start of getPnpGroup method");
			LocalDate ppStartDate = LocalDate.parse(priceProtection.getStartDate(),
					DateTimeFormatter.ofPattern(Constants.DTVN_CPC_DATE_FORMAT));
			
			LocalDate ppEndDate = LocalDate.parse(priceProtection.getEndDate(),
					DateTimeFormatter.ofPattern(Constants.DTVN_CPC_DATE_FORMAT));

			LocalDate nextBillingDate = LocalDate.parse(nbcd,
					DateTimeFormatter.ofPattern(Constants.DTVN_CPC_DATE_FORMAT));
			
			// if the nextBillingDate is not between the start date and end date return null as pnp group
			
			if(ppStartDate.isAfter(nextBillingDate) || ppEndDate.isBefore(nextBillingDate)) {
				return pnpGroup;
			}
			
			Integer ppStartYear = ppStartDate.getYear();
			String ppStartYearConcat = ppStartYear.toString() + DASHPNP;
			String previousYearConcat = null;

			List<String> pnpGrandFatherConfigValue = redisCacheHelper
					.getValues(PNPTAZCONTRACTGRANDFATHERED, productFamily);
			List<String> pnpStartYearConfigValue = redisCacheHelper.getValues(ppStartYearConcat, productFamily);
			List<String> pnpStartYearConfigValues = new ArrayList<String>();

			pnpStartYearConfigValues.addAll(pnpStartYearConfigValue != null
					? Arrays.asList(pnpStartYearConfigValue.get(0).split(Pattern.quote("|")))
					: null);

			LocalDate pnpGrandFatherDate = null;
			if(Constants.OTT_PRODUCT_FAMILY.equalsIgnoreCase(productFamily)) {
				pnpGrandFatherDate = LocalDate.parse(pnpGrandFatherConfigValue.get(0),
						DateTimeFormatter.ofPattern(Constants.DTVN_CPC_DATE_FORMAT));
			}

			LocalDate pnpConfigValueDate = LocalDate.parse(pnpStartYearConfigValues.get(0),
					DateTimeFormatter.ofPattern(Constants.DTVN_CPC_DATE_FORMAT));

			if (Constants.OTT_PRODUCT_FAMILY.equalsIgnoreCase(productFamily) 
					&& (ppStartDate.isBefore(pnpGrandFatherDate) || ppStartDate.isEqual(pnpGrandFatherDate))) {
				pnpGroup = PNPTAZCONTRACTGRANDFATHERED;
			} else if (ppStartDate.isBefore(pnpConfigValueDate)) {
				Integer previousYear = ppStartYear - 1;
				previousYearConcat = previousYear.toString() + DASHPNP;
				List<String> pnpConfigValue2 = redisCacheHelper.getValues(previousYearConcat, productFamily);
				List<String> pnpConfigValues2 = new ArrayList<String>();
				
				pnpConfigValues2.addAll(pnpConfigValue2 != null
						? Arrays.asList(pnpConfigValue2.get(0).split(Pattern.quote("|")))
						: null);
				
				pnpGroup = Optional.ofNullable(pnpConfigValues2).isPresent()
						? previousYearConcat + "-" + pnpConfigValues2.size()
						: null;
			} else {
				if (pnpStartYearConfigValues.size() == 1) {
					pnpGroup = ppStartYearConcat + "-" + pnpStartYearConfigValue.size();
				} else {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DTVN_CPC_DATE_FORMAT);

					pnpStartYearConfigValues.add(priceProtection.getStartDate());
					pnpStartYearConfigValues.sort(Comparator.comparing(str -> LocalDate.parse(str, formatter)));

					List<String> listWithoutDuplicates = pnpStartYearConfigValues.stream().distinct()
							.collect(Collectors.toList());
					Integer findIndex = listWithoutDuplicates.indexOf(priceProtection.getStartDate());
					// Below If block is to handle scenario if pp startdate is same as one of the PP dates from global config
					if (pnpStartYearConfigValues.size() != listWithoutDuplicates.size()) {
						listWithoutDuplicates.add("DUMMY DATE");
						listWithoutDuplicates.add(0, listWithoutDuplicates.remove(listWithoutDuplicates.size() - 1));
						findIndex = listWithoutDuplicates.indexOf(priceProtection.getStartDate());
					}
					pnpGroup = ppStartYearConcat + "-" + findIndex;
				}
			}

		} catch (Exception e) {
			log.error("Exception in PnpGroupUtils.getPnpGroup :{}", e);
		}
		return pnpGroup;
	}

}
