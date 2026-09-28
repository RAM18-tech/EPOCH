package com.dtv.dcp.epoch.processor.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.CpopConstants;
import com.dtv.dcp.epoch.model.ct.benefit.Benefit;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;

/**
 * The Class DTVNowCPOPAdditionalOfferHelper.
 *
 * @author ag5791
 */
@Component
public class CPOPAdditionalOfferHelper {

	@Autowired
	private FeatureManagerHelper featureManagerHelper;

	@Value("${dcp.config.env:env}")
	private String idpConfigEnv;

	/**
	 * @param channel
	 * @return
	 */
	public boolean isRewardCapabilityEnabled(String channel) {
		return Optional.ofNullable(channel).isPresent()
				&& featureManagerHelper.isEnabled(CpopConstants.EPOCH_REWARDS_ENABLED);
	}

	public boolean isRewardCapabilityEnabled() {
		return featureManagerHelper.isEnabled(CpopConstants.EPOCH_REWARDS_ENABLED);
	}

	/**
	 *
	 * @param ctOffersResponse
	 * @return
	 */
	public Map<String, Boolean> createQuotaBaseBenefitMap(CTOfferResponse ctOffersResponse) {
		Map<String, Boolean> quotaBaseBenefitMap = new HashMap<>();

		if (Optional.ofNullable(ctOffersResponse).isPresent()) {
			List<CTOffer> ctOffers = ctOffersResponse.getOffers();

			ctOffers.stream().filter(Objects::nonNull).forEach(offer -> {
				if (Optional.ofNullable(offer.getAttributes().getBenefits()).isPresent()) {
					List<Benefit> benefits = offer.getAttributes().getBenefits();
					benefits.stream().forEach(benefit -> {
						if (benefit.isQuotaBased()) {
							quotaBaseBenefitMap.put(benefit.getBillingBenefitCode(), benefit.isQuotaBased());
						}
					});
				}

			});
		}
		return quotaBaseBenefitMap;
	}

	/**
	 * @param sessionId
	 * @return
	 */
	@Cacheable(value = Constants.CPOPOFFERSMS_MAIN_CACHE_MAP_NAME, key = "{ 'epochQuotabasedOfferMap', #isCTBackUpenabled, #sessionId, #idpConfigEnv,'epochOffers' }", unless = "#result==null")
	public Map<String, Boolean> getQuotaBaseOfferMapFromCache(String sessionId,String isCTBackUpenabled) {

		return null;
	}

	/**
	 *
	 * @param sessionId
	 * @param quotaBaseBenefitMap
	 * @return
	 */
	@CachePut(value = Constants.CPOPOFFERSMS_MAIN_CACHE_MAP_NAME, key = "{ 'epochQuotabasedOfferMap', #isCTBackUpenabled, #sessionId, #idpConfigEnv, 'epochOffers' }", unless = "#result==null", cacheManager="redisCacheManagerFirstHolder")
	public Map<String, Boolean> putQuotaBaseOfferMapInCache(String sessionId,
			Map<String, Boolean> quotaBaseBenefitMap,String isCTBackUpenabled) {
		return quotaBaseBenefitMap;
	}

	public String getConfiguredEnvrironment() {
		return idpConfigEnv;
	}
}