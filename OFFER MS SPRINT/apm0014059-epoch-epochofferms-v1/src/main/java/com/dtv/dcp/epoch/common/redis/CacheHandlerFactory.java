package com.dtv.dcp.epoch.common.redis;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;

/**
 * A factory for creating CacheHandler objects.
 */
@Component
public class CacheHandlerFactory {

	/** The aem cache handler. */
	@Autowired
	private EpochOffersMsIXPCache1 epochOffersMsIXPCache1;
	
	/** The aem cache handler. */
	@Autowired
	private EpochOffersMsCTCache1 epochOffersMsCTCache1;

	/** The generic cache handler. */
	@Autowired
	private EpochOffersMsCTCache2 epochOffersMsCTCache2;
	/** The generic cache handler. */
	@Autowired
	private EpochOffersMsCTCache3 epochOffersMsCTCache3;
	
	@Autowired
	private EpochOffersMsProductsCTCache1 epochOffersMsProductsCTCache1;
	
	@Autowired
	private EpochOffersMsProductsCTCache2 epochOffersMsProductsCTCache2;
	
	@Autowired
	private EpochOffersMsProductsCTCache3 epochOffersMsProductsCTCache3;
	
	@Autowired
	private EpochOffersMsBenefitsCache1 epochOffersMsBenefitsCache1;
	
	@Autowired
	private EpochOffersMsBenefitsCache2 epochOffersMsBenefitsCache2;	
	
	@Autowired
	private EpochOffersMsBenefitsCache3 epochOffersMsBenefitsCache3;
	
	@Autowired
	private CpopOffersMsWirelessPrimaryCTCache wirelessPrimaryCache;

	@Autowired
	private CpopOffersMsWirelessBackupCTCache wirelessBackupCache;

	@Autowired
	CpopOffersMsCatlogOneCache cpopOffersMsCatlogOneCache;
	
	@Autowired
	private EpochOfferMsGlobalConfigurationsCTCache epochOfferMsGlobalConfigurationsCTCache;

	@Autowired
	private EpochOfferMsGlobalConfigurationsCTCacheBackUp epochOfferMsGlobalConfigurationsCTCacheBackUp;

	@Autowired
	private EpochOfferMsGlobalConfigurationsCTCachePvt epochOfferMsGlobalConfigurationsCTCachePvt;

	@Autowired
	private EpochOfferMsGlobalEligibilityConfigCTCache epochOfferMsGlobalEligibilityConfigCTCache;

	@Autowired
	private EpochOfferMsGlobalEligibilityConfigCTCacheBackUp epochOfferMsGlobalEligibilityConfigCTCacheBackUp;

	@Autowired
	private EpochOfferMsGlobalEligibilityConfigCTCachePvt epochOfferMsGlobalEligibilityConfigCTCachePvt;

	@Autowired
	private EpochProductTypesAttrPrimaryCache epochProductTypesAttrPrimaryCache;

	@Autowired
	private EpochProductTypesAttsBackUpCache epochProductTypesAttsBackUpCache;

	@Autowired
	private EpochProductTypesAttsPvtCache epochProductTypesAttsPvtCache;

	/**
	 * Gets the single instance of CacheHandlerFactory.
	 *
	 * @param mapName the map name
	 * @return single instance of CacheHandlerFactory
	 */
	public ICacheHandler getInstance(String mapName) {

		ICacheHandler instance = null;

		if (Constants.EPOCHOFFERSMS_CT_PRIMARY_CACHE_MAP_NAME.equalsIgnoreCase(mapName)) {
			instance = epochOffersMsCTCache1;
		}

		if (Constants.EPOCHOFFERSMS_CT_BACKUP_CACHE_MAP_NAME.equalsIgnoreCase(mapName)) {
			instance = epochOffersMsCTCache2;
		}

		if (Constants.EPOCHOFFERSMS_CT_PVT_CACHE_MAP_NAME.equalsIgnoreCase(mapName)) {
			instance = epochOffersMsCTCache3;
		}
		
		if (Constants.EPOCHOFFERSMS_CT_PRODUCTS_PRIMARY_CACHE_MAP_NAME.equalsIgnoreCase(mapName)) {
			instance = epochOffersMsProductsCTCache1;
		}

		if (Constants.EPOCHOFFERSMS_CT_PRODUCTS_BACKUP_CACHE_MAP_NAME.equalsIgnoreCase(mapName)) {
			instance = epochOffersMsProductsCTCache2;
		}

		if (Constants.EPOCHOFFERSMS_CT_PRODUCTS_PVT_CACHE_MAP_NAME.equalsIgnoreCase(mapName)) {
			instance = epochOffersMsProductsCTCache3;
		}
		
		if (Constants.EPOCHOFFERSMS_CT_BENEFITS_PRIMARY_CACHE_MAP_NAME.equalsIgnoreCase(mapName)) {
			instance = epochOffersMsBenefitsCache1;
		}

		if (Constants.EPOCHOFFERSMS_CT_BENEFITS_BACKUP_CACHE_MAP_NAME.equalsIgnoreCase(mapName)) {
			instance = epochOffersMsBenefitsCache2;
		}

		if (Constants.EPOCHOFFERSMS_CT_BENEFITS_PVT_CACHE_MAP_NAME.equalsIgnoreCase(mapName)) {
			instance = epochOffersMsBenefitsCache3;
		}

		if (Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CACHE_MAP_NAME.equalsIgnoreCase(mapName)) {
			instance = epochOfferMsGlobalConfigurationsCTCache;
		}

		if (Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CACHE_MAP_NAME_BACKUP.equalsIgnoreCase(mapName)) {
			instance = epochOfferMsGlobalConfigurationsCTCacheBackUp;
		}

		if (Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CACHE_MAP_NAME_PVT.equalsIgnoreCase(mapName)) {
			instance = epochOfferMsGlobalConfigurationsCTCachePvt;
		}

		if (Constants.EPOCHOFFERSMS_GLOBAL_ELIGIBILITY_CONFIGURATIONS_CACHE_MAP_NAME.equalsIgnoreCase(mapName)) {
			instance = epochOfferMsGlobalEligibilityConfigCTCache;
		}

		if (Constants.EPOCHOFFERSMS_GLOBAL_ELIGIBILITY_CONFIGURATIONS_CACHE_MAP_NAME_BACKUP.equalsIgnoreCase(mapName)) {
			instance = epochOfferMsGlobalEligibilityConfigCTCacheBackUp;
		}

		if (Constants.EPOCHOFFERSMS_GLOBAL_ELIGIBILITY_CONFIGURATIONS_CACHE_MAP_NAME_PVT.equalsIgnoreCase(mapName)) {
			instance = epochOfferMsGlobalEligibilityConfigCTCachePvt;
		}
		
		if (Constants.CPOPOFFERSMS_CT_PRIMARY_WIRELESS_CACHE_MAP_NAME.equalsIgnoreCase(mapName)) {
			instance = wirelessPrimaryCache;
		} else if (Constants.CPOPOFFERSMS_CT_BACKUP_WIRELESS_CACHE_MAP_NAME.equalsIgnoreCase(mapName)) {
			instance = wirelessBackupCache;
		}
		if (StringUtils.equalsIgnoreCase(Constants.CPOP_CATALOB_ONE_CACHE_MAP_NAME, mapName)) {
			return cpopOffersMsCatlogOneCache;
		}
		if (Constants.EPOCHOFFERSMS_IXP_PRIMARY_CACHE_MAP_NAME.equalsIgnoreCase(mapName)) {
			instance = epochOffersMsIXPCache1;
		}
		if (Constants.EPOCH_PRODUCT_TYPES_ATTR_PRIMARY_CACHE_MAP_NAME.equalsIgnoreCase(mapName)) {
			instance = epochProductTypesAttrPrimaryCache;
		}
		if (Constants.EPOCH_PRODUCT_TYPES_ATTR_BACKUP_CACHE_MAP_NAME.equalsIgnoreCase(mapName)) {
			instance = epochProductTypesAttsBackUpCache;
		}
		if (Constants.EPOCH_PRODUCT_TYPES_ATTR_PVT_CACHE_MAP_NAME.equalsIgnoreCase(mapName)) {
			instance = epochProductTypesAttsPvtCache;
		}

		return instance;
	}
}