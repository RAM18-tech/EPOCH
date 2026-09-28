package com.dtv.dcp.epoch.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * This service will load DMA lookup data during application startup.
 * @author pp0344
 *
 */
@Service
public class DMALookUpService {

	private static final Logger logger = LoggerFactory.getLogger(DMALookUpService.class);

	private Map<String, String> lookupMap = new HashMap<>();
	private Map<String, List<String>> dmaMap = new HashMap<>();
	
	private List<String> lccTrialDmaList = new ArrayList<>();

	@PostConstruct
	public void initDMALookUpService() {
		
		try {

			List<String> dmaLines = new ArrayList<>();

			try(InputStream inputStream1 = getClass().getClassLoader().getResourceAsStream("DMA_Lookup.txt");
					InputStreamReader inputStreamReader1 = new InputStreamReader(inputStream1, StandardCharsets.UTF_8);
					BufferedReader reader1 = new BufferedReader(inputStreamReader1)){

				String line1;
				while ((line1 = reader1.readLine()) != null) {
					dmaLines.add(line1);
				}
			}
			try(InputStream inputStream2 = getClass().getClassLoader().getResourceAsStream("LCC_TrialDMA.txt");
					InputStreamReader inputStreamReader2 = new InputStreamReader(inputStream2, StandardCharsets.UTF_8);
					BufferedReader reader2 = new BufferedReader(inputStreamReader2)){

				String line2;
				while ((line2 = reader2.readLine()) != null) {
					lccTrialDmaList.add(line2);
				}
			}
			if (CollectionUtils.isNotEmpty(lccTrialDmaList)) {
				lccTrialDmaList.stream().filter(Objects::nonNull);
			}

			dmaLines.stream().filter(Objects::nonNull).forEach(dmaLine -> {
				String[] lineItems = dmaLine.split(",");

				lookupMap.put(lineItems[0].trim() + "_" + lineItems[1].trim(), lineItems[2].trim());

				List<String> dmaList = new ArrayList<>();
				if (CollectionUtils.isNotEmpty(dmaMap.get(lineItems[0].trim() + "_" + lineItems[1].trim()))) {
					dmaList = dmaMap.get(lineItems[0].trim() + "_" + lineItems[1].trim());
				}
				dmaList.add(lineItems[3].trim());
				dmaMap.put(lineItems[0].trim() + "_" + lineItems[1].trim(), dmaList);
			});

			logger.info("DMALookUpService - data load completed");

		} catch (Exception e) {
			logger.error("DMALookUpService - Error in loading DMA lookup data :" + e.getMessage(), e);
		}
	}
	
	
	/**
	 * This is public method will be called from other services to get value of hasLocalChannels based on input parameter zipCode and countyCode.
	 * @param zipCode
	 * @param countyCode
	 * @return Boolean
	 */
	public Boolean hasLocalChannels(String zipCode, String countyCode) {
		Boolean hasLocalChannel = null;
		if (null != lookupMap && !lookupMap.isEmpty()) {
			String lookUpValue = lookupMap.get(zipCode + "_" + countyCode);
			if(StringUtils.isNotBlank(lookUpValue)) {
				if(lookUpValue.equals("1")) {
					hasLocalChannel = true;
				}else {
					hasLocalChannel = false;
				}
			}
		}
		return hasLocalChannel;
	}
	
	/**
	 * This public method will be called from other services to get dma value based on input parameter zipCode and countyCode.
	 * @param zipCode
	 * @param countyCode
	 * @return String
	 */
	public List<String> getDMAValue(String zipCode, String countyCode) {
		return dmaMap.getOrDefault(zipCode + "_" + countyCode, Collections.emptyList());
	}
	
	/**
	 * This is public method will be called from other services to get value of isLCCTrialMarketDMA
	 *  based on input parameter zipCode and countyCode.
	 * @param zipCode
	 * @param countyCode
	 * @return Boolean
	 */
	public Boolean isLCCTrialMarketDMA(String zipCode, String countyCode) {
		Boolean isLCCTrialMarketDMA = null;
		List<String> dmaValue = getDMAValue(zipCode, countyCode);
		if (CollectionUtils.isNotEmpty(lccTrialDmaList) && lccTrialDmaList.stream().anyMatch(dmaValue::contains)) {
			isLCCTrialMarketDMA = true;
			logger.debug("LCCTrialMarketDMA: ", isLCCTrialMarketDMA);
		}else {
			isLCCTrialMarketDMA = false;
		}
		return isLCCTrialMarketDMA;
	}
}
