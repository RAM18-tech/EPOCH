package com.dtv.dcp.epoch.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class Constants.
 */

public final class Constants {

	public static final String AGENT = "agent";
	public static final String CUSTOMER = "customer";

	public static final String FEATURE_SVC_EPOCH_DCP_BILLINGMS_ENABLED = "svc-epoch-dcp-billingms-enabled";
	public static final String FEATURE_SVC_EPOCH_DCP_OFFERMS_ENABLED ="svc-epoch-dcp-offerms-enabled";
	/** The Constant FEATURE_IDP_FEATURES_SVC_PVTENABLED. */
	public static final String FEATURE_IDP_FEATURES_SVC_PVTENABLED = "svc-epoch-pvtenabled";
	public static final String FEATURE_SVC_MIDDLEWARECACHE_ENABLED = "svc-middlewarecache-enabled";

	/** The Constant FEATURE_TOGGLE_DTVN_CPOPENABLED_BACKUP. */
	public static final String FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP = "svc-epochofferv2enabled-ctenv-backup";

	public static final String FEATURE_TOGGLE_PST_TIME_LOGIC_ENABLED = "svc-epoch-pst-time-logic-enabled";

	public static final String BILLING_REFERENEID_PREFIX = "^0+";
    public static final String DEFAULT_ZIPCODE = "defaultZipCode" ;
	public static final String DEFAULT_DMA = "defaultDMA";
	public static final String SERVED_MARKET = "isServedMarket";
    public static final String FEATURE_FLAG_CONFLICTING_OFFER_NEW_LOGIC = "svc-epoch-conflicting-offer-new-logic";
    public static final String FEATURE_TOGGLE_IXP_FS_ISSUE = "svc-epoch-ixp-fs-issue-flag-enabled";
    public static final String FEATURE_SVC_EPOCH_PERF_OPT_ENABLED  = "svc-epoch-perf-optimization-enabled";
    public static final String FEATURE_FLAG_PROMO_SUPPRESSION_IN_VC = "svc-epoch-promo-suppression-in-vc";
    /**
	 * Instantiates a new constants.
	 */
	private Constants() {
	}

	/** The Constant OTT_PRODUCT_FAMILY. */
	public static final String OTT_PRODUCT_FAMILY = "OTT";

	/** The Constant SATELLITE_PRODUCT_FAMILY. */
	public static final String WATCHTV_PRODUCT_FAMILY = "watchtv";

	/** The Constant SATELLITE_PRODUCT_FAMILY. */
	public static final String SATELLITE_PRODUCT_FAMILY = "satellite";

	/** The Constant LEGACY_SATELLITE_PRODUCT_FAMILY. */
	public static final String LEGACY_SATELLITE_PRODUCT_FAMILY = "legacySatellite";

	/** The Constant IPTV2021. */
	public static final String IPTV_2021 = "IPTV2021";

	/** The Constant IPTV2022. */
	public static final String IPTV_2022 = "IPTV2022";

	/** The Constant IPTV_PRODUCT_FAMILY. */
	public static final String IPTV_PRODUCT_FAMILY = "IPTV";

	/** The Constant BB_PRODUCT_FAMILY. */
	public static final String BB_PRODUCT_FAMILY = "broadband";

	/** The Constant DTVS_PRODUCT_FAMILY. */
	public static final String DTVS_PRODUCT_FAMILY = "satellite";

	/** The Constant DTVS_PRODUCT_FAMILY. */
	public static final String WIRELESS_PRODUCT_FAMILY = "wireless";

	/** The Constant REWARD_OFFER_TYPE. */
	public static final String REWARD_OFFER_TYPE = "reward";

	/** The Constant ACQUISITION_ACTION_TYPE. */
	public static final String ACQUISITION_ACTION_TYPE = "Acquisition";

	/** The Constant RETENTION_ACTION_TYPE. */
	public static final String RETENTION_ACTION_TYPE = "Retention";
	
	/** The Constant CLOSING_ACTION_TYPE. */
	public static final String CLOSING_ACTION_TYPE = "Closing";
	
	/** The Constant SOS_ACTION_TYPE. */
	public static final String SOS_ACTION_TYPE = "SOS";

	/** The Constant REPLACEMENT_CLASSIFICATION_TYPE. */
	public static final String REPLACEMENT_CLASSIFICATION_TYPE = "replacement-device";

	public static final String UPGRADE_ACTION_TYPE = "Upgrade";
	public static final String DOWNGRADE_ACTION_TYPE = "Downgrade";
	public static final String CROSS_SELL_ACTION_TYPE = "Cross-sell";
	public static final String OTHER_ACTION_TYPE = "Other";

	public static final String BYOD = "BYOD";
	public static final String DTVN = "DTVN";

	/** */
	public static final String ACTIVE = "Active";

	/** Constants for RetentionOfferSubType */
	public static final String RETENTION_OFFER_SUBTYPE_PREMIUM = "PremiumSaveOffer";
	public static final String RETENTION_OFFER_SUBTYPE_MVP = "MVPOffer";
	public static final String RETENTION_OFFER_SUBTYPE_PROGRAMMING_DISPUTE = "ProgrammingDispute";

	/** The Constant VIDEO_PLAN. */
	public static final String VIDEO_PLAN = "video-plan";

	/** The Constant VIDEO_PLAN. */
	public static final String BASE = "base";

	/** The Constant VIDEO_PLAN. */
	public static final String STANDALONE = "standalone";

	/** The Constant VIDEO_PLAN. */
	public static final String VIDEO_ADDON = "video-addon";

	/** The Constant ADDON. */
	public static final String ADDON = "addon";

	/** The Constant INSURANCE. */
	public static final String INSURANCE = "insurance";

	/** The Constant ADDON. */
	public static final String EQUIPMENT = "equipment";

	/** The Constant VIDEO_PLAN. */
	public static final String VIDEO_DEVICE = "video-device";

	/** The Constant VIDEO_PLAN. */
	public static final String VIDEO_ACCESSORY = "video-accessory";

	/** The Constant FEE. */
	public static final String FEE = "fee";
	
	/** The Constant PROTECTION PLAN. */
	public static final String PROTECTION_PLAN = "protection-plan";
	
	public static final String CONDITION="condition";

	/** The Constant SUCCESS. */
	public static final String SUCCESS = "success";

	/** The Constant IDPCTX_AGENT_TYPE. */
	public static final String IDPCTX_AGENT_TYPE = "idpctx-rep-dtvnowaccesslevel";

	/** The Constant IDPCTX_AGENT_TYPE. */
	public static final String IDPCTX_PARTNER_TYPE = "idpctx-appname";

	/** The Constant IDPCTX_LOGGEDINID. */
	public static final String IDPCTX_LOGGEDINID = "idpctx-loggedInId";

	/** The Constant IDPCTX_AUTHORIZEDACCOUNTS_WIRELESS. */
	public static final String IDPCTX_AUTHORIZEDACCOUNTS_WIRELESS = "idpctx-linkedWirelessAccNums";

	/** The Constant IDPCTX_DTVN_ACCOUNT. */
	public static final String IDPCTX_DTVN_ACCOUNT = "idpctx-linkedDirectvNowAccNums";

	/** The Constant IDPCTX_SESSION_ID. */
	public static final String IDPCTX_SESSION_ID = "idpctx-session-id";

	/** The Constant IDPCTX_LINKEDUVERSEACCNUMS. */
	public static final String IDPCTX_LINKEDUVERSEACCNUMS = "Idpctx-linkeduverseaccnums";

	/** The Constant BAN_NOT_FOUND. */
	public static final String BAN_NOT_FOUND = "Mobility account not available";

	/** The Constant GOT_EXCEPTION. */
	public static final String GOT_EXCEPTION = "Got Exception : {}";

	/** The Constant GOT_CLIENT_EXCEPTION. */
	public static final String GOT_CLIENT_EXCEPTION = "Got ClientException... ";

	/** The Constant WIRELESS_CLIENT. */
	public static final String WIRELESS_CLIENT = "WIRELESS_CLIENT";

	/** The Constant ALERT_CODE. */
	public static final String ALERT_CODE = "CATLG_ALERT_0001";

	/** The Constant SUSPENDED_ACCOUNT. */
	public static final String SUSPENDED_ACCOUNT = "Suspended Account";
	
	public static final String SERVICE_SUSPENDED_INFO = "serviceSuspendInfo";
	
	public static final String SERVICE_SUSPENDED_INFO_UPPERCASE = "SERVICE_SUSPEND_INFO";
	public static final String FREE_TRIAL_DETAILS_UPPERCASE = "FREE_TRIAL_DETAILS";
	public static final String FREE_TRIAL_DETAILS = "freetraildetails";


	public static final String IAP_INFO = "IAP_INFO";
	
	public static final String IAPINFO = "iapInfo";

	/** The Constant DIRECTTV_NOW_ARRAY. */
	public static final String[] DIRECTTV_NOW_ARRAY = { "DirecTVNow" };

	/** The Constant ALERT_DESCRIPTION. */
	public static final String ALERT_DESCRIPTION = "Wireless Account validation not successful. Customer may qualify additional Benefits";

	/**
	 * **************************************************************************************.
	 */
	/** Catalog Cache Maps Constants - Start */
	/*****************************************************************************************/
	/** The Constant OFFERSMS_NEAR_CACHE_MAP_NAME. */
	public static final String OFFERSMS_NEAR_CACHE_MAP_NAME = "offersMsNearCache";

	/** The Constant OFFERSMS_MAIN_CACHE_MAP_NAME. */
	public static final String OFFERSMS_MAIN_CACHE_MAP_NAME = "offersMsMainCache";

	public static final String CPOPOFFERSMS_MAIN_CACHE_MAP_NAME = "cpopOffersMsMainCache";

	/** The Constant OFFERSMS_SERVICES_CACHE_MAP_NAME. */
	public static final String OFFERSMS_SERVICES_CACHE_MAP_NAME = "offersMsServicesNearCache";

	/** The Constant OFFERSMS_NEAR_CACHE_AEM_MAP_NAME. */
	public static final String OFFERSMS_NEAR_CACHE_AEM_MAP_NAME = "offersMsNearAemCache";
	// Note: Please add cache map names in this CATALOG_ALL_CACHE_MAP_NAMES_LIST, so
	// that Cache APIs
	/** The Constant CATALOG_ALL_CACHE_MAP_NAMES_LIST. */
	// can use them for eviction, display of keys and associated objects
	public static final String CATALOG_ALL_CACHE_MAP_NAMES_LIST = "offersMsNearCache,offersMsMainCache,offersMsServicesNearCache,offersMsCpopOTTNearCache";
	/** The Constant OFFERSMS_SERVICES_CACHE_MAP_NAME. */

	public static final String OFFERSMS_CPOP_NEAR_CACHE_MAP_NAME = "offersMsCpopOTTNearCache";

	public static final String CPOPOFFERSMS_PROFILE_NEAR_CACHE_MAP_NAME = "cpopOfferMsProfileNearCache";

	// COSC Cache constants
	/** The cache name. */
	public static final String CACHE_NAME = "idseMsOffers";

	/** The object key. */
	public static final String OBJECT_KEY = "session_data";

	/** The application id. */
	public static final String APPLICATION_ID = "msOffersAppId";

	/** The cache version id. */
	public static final String CACHE_VERSION_ID = "v1";

	/** The Constant ELIGIBILITIES_KEY_PREFIX. */
	public static final String ELIGIBILITIES_KEY_PREFIX = "wireless-";

	/** The Constant ELIGIBILE_SOC_CODES_KEY_PREFIX. */
	public static final String ELIGIBILE_SOC_CODES_KEY_PREFIX = "wireless-socs-";

	public static final String ONLINE = "online";
	public static final String INDIRECT_PARTNER = "partner";
	public static final String OEM_IAPFIRETV = "oemIAPFIRETV";
	public static final String OEM_IAPROKUTV = "oemIAPROKUTV";
	// OEM_APPLE constant for Apple OEM Sales Channel
	public static final String OEM_APPLE = "oemAPPLE";
	// OEM_TIZEN constant for Tizen OEM Sales Channel
	public static final String OEM_TIZEN = "oemTIZEN";
	// OEM_GOOGLE constant for Google OEM Sales Channel
	public static final String OEM_GOOGLE = "oemGOOGLE";
	public static final String ROKUTV = "ROKUTV";
	public static final String DIRECTV_ONLINE = "directvOnline";
	public static final String OEM_IAP_GOOGLE = "oemIAPGOOGLE";
	public static final String ISUROKUTV = "isuROKUTV";
	public static final String EVERGENT_CRM = "evCRM";
	public static final String DIRECTV = "DIRECTV";
	public static final String ALL = "ALL";
	public static final String OPUS = "opus";
	public static final String IVR = "ivr";
	public static final String TACTICAL = "tactical";
	public static final String OTT = "OTT";
	public static final String SELF_SERVICE_NEW = "selfServiceNew";
	public static final String ACQUISITION = "Acquisition";
	public static final String UPSELL_ACTION_TYPE = "Upsell";
	public static final String AGENT_NEW = "agentNew";
	public static final String AGENT_EXISTING = "agentExisting";
	public static final String SELF_SERVICE_EXISTING = "selfServiceExisting";
	public static final String CONTRACT = "contract";
	public static final String NONCONTRACT = "non-contract";

	public static final String PROMO_START_DATE = "promoStartDate";

	public static final String PROMO_END_DATE = "promoEndDate";

	public static final String CT_PROMO_START_DATE = "startDateStr";

	public static final String CT_PROMO_END_DATE = "endDateStr";

	public static final String CT_PROMO_ID = "promoId";

	public static final String NBCD_DATE = "nextBillingDate";

	public static final String PST = "PST";

	public static final String GMT = "GMT";

	public static final String STATUS = "productStatus";
	public static final String GRANDFATHER = "grandFather";
	public static final String DATE_TIME_FORMAT = "MM/dd/yyyy HH:mm:ss";

	/** The Constant DTVNOW_ACCOUNT_TYPE. */
	public static final String DTVNOW_ACCOUNT_TYPE = "DTVNOW";

	/** The Constant CTLG_E500005. */
	public static final String CTLG_E500005 = "CTLG-E500005";

	/** The Constant CATLG_EXT_ERROR_50001. */
	public static final String CATLG_EXT_ERROR_50001 = "CATLG-EXT-ERROR-50001";
	/** The Constant CART_VALIDATION_URI. */
	public static final String CART_VALIDATION_URI = "Offer validation :: URI  = ";
	/** The Constant CART_VALIDATION_URI. */
	public static final String CART_VALIDATION = "Offer validation";
	/** The Constant V2. */
	public static final String V2 = "V2";
	/** The Constant SELF. */
	public static final String SELF = "self";
	/** The Constant APPLICATION_JSON. */
	public static final String APPLICATION_JSON = "application/json";
	/** The Constant VALIDATION_ERROR_000010. */
	public static final String ZERO_SUCCESS = "00000";
	/** The Constant WARNING. */
	public static final String WARNING = "warning";
	/** The Constant for promotion key. */
	public static final String PROMOTION = "promotion";
	/** The Constant IDPCTX_ACCTINFOCUS. */
	public static final String IDPCTX_ACCTINFOCUS = "idpctx-acctinfocus";
	/** The Constant IDPXTX_ACCTINFOCUSTYPE. */
	public static final String IDPXTX_ACCTINFOCUSTYPE = "idpctx-acctinfocustype";
	/** The Constant IDPCTX_LINKED_WIRELESS_ACCT_NUMS. */
	public static final String IDPCTX_LINKED_WIRELESS_ACCT_NUMS = "idpctx-linkedWirelessAccNums";
	/** The Constant IDPCTX_UUID. */
	public static final String IDPCTX_UUID = "idpctx-uuid";

	/** Reward and Coupon constants **/
	public static final String REWARD = "reward";

	public static final String COUPON = "coupon";

	public static final String FAIL = "failure";

	public static final String REWARD_VALIDATION_SUCCESS_MESSAGE = "Offer Validation Success";

	public static final String CT_ERROR_CODE_EV8349 = "eV8349";

	public static final String CT_ERROR_CODE_EV8350 = "eV8350";

	public static final String CT_ERROR_CODE_EV8351 = "eV8351";

	public static final String CT_ERROR_CODE_EV8355 = "eV8355";

	/** The Constant RESIDENTIAL_ACCOUNT_TYPE. */
	public static final String RESIDENTIAL_ACCOUNT_TYPE = "Residential";

	public static final String CPOP_DATE_FORMATE = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	/** The Constant DATE_FORMAT. */
	public static final String DATE_FORMAT = "MM/dd/yyyy";

	public static final String IS_DEFAULT_PROMO = "isDefaultPromo";

	public static final String IS_FREE_TRIAL_ELIGIBLE = "isFreeTrialEligible";

	public static final String SPECIAL = "special";

	public static final String LEAD = "lead";

	public static final String AVAILABE_PACKAGE_GROUP = "available";

	public static final String PACKAGE = "packageGroup";

	public static final String EMPLOYEE = "Employee";

	public static final String PIPE = "\\|";
	public static final String SALES_CHANNEL_ALL = "All";

	public static final String RESIDENTIAL = "Residential";

	public static final String MOBILITY = "Mobility";
	public static final String MDUTENANT = "MDUTenant";

	public static final String BCOMP = "BCOMP";
	
	public static final String MDU = "MDU";
	public static final String CONS = "CONS";

	public static final String SHOWROOM = "Showroom";
	public static final String COURTESY = "Courtesy";
	public static final String DEMO = "DEMO";
	public static final String DECA = "DECA";

	public static final String BULK_PRICE = "bulk-price";

	public static final String RECONNECT = "reconnect";
	public static final String RECONNECT_RETURN = "reconnectReturn";

	/** The Constant VIDEO_BASE. */
	public static final String VIDEO_BASE = "video-base";

	/** The Constant SUBSCRIPTION. */
	public static final String SUBSCRIPTION = "Subscription";

	/** The Constant PROGRAMMING_BOLTON. */
	public static final String PROGRAMMING_BOLTON = "Programming-Bolt-on";

	/** The Constant RSN_CAPS. */
	public static final String RSN_CAPS = "RSN";
	/** The Constant RSNFEE. */
	public static final String RSNFEE = "RSNFee";

	public static final String TIVO = "Tivo";

	/** The Constant Activation Fee. */
	public static final String ACTIVATION_FEE = "ActivationFee";
	
	public static final String DO_FEE = "doFee";
	/** The Constant BOLTON. */
	public static final String BOLTON = "Bolt-on";
	/** The Constant RSN_CAPS. */
	public static final String HARDWARE = "Hardware";

	/** The Constant cDVR. */
	public static final String CDVR = "cDVR";

	/** The Constant DTVNSREVICE_ADDON_CONCURRENT_STREAMS. */
	public static final String DTVNSREVICE_ADDON_CONCURRENT_STREAMS = "Concurrent Streams";

	/** The Constant DTVNSREVICE_ADDON_DVR. */
	public static final String DTVNSREVICE_ADDON_DVR = "DVR";

	/** The Constant STANDALONE. */
	public static final String STANDALONE_WITH_CAPITAL_S = "Standalone";

	/** The Constant Streams. */
	public static final String STREAMS = "Streams";

	public static final String FREETRIALWITHHYPHEN = "free-trial";
	public static final String FREE_TRIAL = "freeTrial";

	public static final String PERCENT_OFF = "percent-off";

	public static final String FLATOFF = "flat-off";

	public static final String FREE_PROMO = "free-promo";

	public static final String FREE = "Free";

	public static final String ACCOUNT = "account";

	public static final String PERCENTAGE_OFF = "Percentage Off";

	public static final String PRODUCT = "product";

	public static final String PERCENTAGE = "Percentage";

	public static final String LIFETIME = "Lifetime";

	public static final String FLAT = "flat";

	public static final String FLAT_OFF = "Flat Off";

	public static final String DTV_NOW_ADDON = "DTV Now add on";

	public static final String RETAIL_PRICE = "Retail Price";

	public static final String MOBILITY_WITH_CAPITAL_M = "Mobility";

	public static final String DEPENDENT = "Dependent";

	public static final String DTVN_CPC_DATE_FORMAT = "M/d/yyyy";

	public static final String NEW_CUSTOMER = "NewCustomer";

	public static final String UPSELL = "Upsell";

	public static final String BOGO = "bogo";

	public static final String Upsell = "upsell";

	public static final String EXISTING_CUSTOMER = "ExistingCustomer";

	public static final String PREPAY = "Prepay";

	public static final String MYATT = "myatt";

	public static final String AGENT_RETENTION = "AgentRetention";

	public static final String SELF_CARE_RETENTION = "SelfcareRetention";

	public static final String AGENT_SPCE_RETENTION = "Agent Retention";

	public static final String SELF_CARE_SPACE_RETENTION = "Selfcare Retention";

	public static final String INTERNATIONAL = "International";

	public static final String ENGLISH = "English";
	
	public static final String SPANISH = "Spanish";

	public static final String CROSS_PRODUCT_MOBILITY = "Cross-Product-Mobility";

	public static final String CROSS_PRODUCT_WATCHTV = "CrossProduct-WatchTV";

	public static final String CROSS_PRODUCT = "Cross-product";

	public static final String RSN = "rsn";

	public static final String BASEPACKAGE = "basePackage";

	public static final String ALL_CONTRACT = "All,Contract";

	public static final String ALL_RETAIL = "All,Retail";

	public static final String OFFER_PROMOTIONS = "offerPromotions";

	public static final String CROSS_PRODUCT_WATCH_TV = "CrossProduct-WatchTV";

	public static final String C_DVR = "cDVR";

	public static final String BOLT_ON = "Bolt-on";

	public static final String PROGRAMMING_BOLT_ON = "Programming-Bolt-on";

	public static final String STANDALONE_CAPS = "Standalone";

	public static final int ONE = 1;
	
	public static final int TWO = 2;

	public static final String MIGRATION = "migration";

	public static final String ZIPCODE = "zipCode";

	public static final String DEVICES = "devices";

	public static final String CPOPOFFERIDS = "cpopOfferIds";

	public static final String CPOPOFFERCODES = "cpopOfferCodes";

	public static final String ATT_TVNOW_GENERIC_QUERY = "dtvNowGenericQuery";

	public static final String QUERY_DTVN_SERVICES = "dtvNowServiceInfo";

	/********************** TOGGLE *****************************/
	public static final String TRUE = "T";

	public static final String LOW_CREDIT_RISK = "Low";

	public static final String HIGH_CREDIT_RISK = "High";

	public static final String UNKNOWN_CREDIT_RISK = "Unknown";

	public static final String TRACE_ID = "idp-trace-id";

	public static final String EPOCHOFFERSMS_CT_PRIMARY_CACHE_MAP_NAME = "epochOffersMsCTCache1";

	public static final String EPOCHOFFERSMS_CT_BACKUP_CACHE_MAP_NAME = "epochOffersMsCTCache2";

	public static final String EPOCHOFFERSMS_CT_PVT_CACHE_MAP_NAME = "epochOffersMsCTCache3";
	
	public static final String EPOCHOFFERSMS_CT_PRODUCTS_PRIMARY_CACHE_MAP_NAME = "epochOffersMsProductsCache1";

	public static final String EPOCHOFFERSMS_CT_PRODUCTS_BACKUP_CACHE_MAP_NAME = "epochOffersMsProductsCache2";

	public static final String EPOCHOFFERSMS_CT_PRODUCTS_PVT_CACHE_MAP_NAME = "epochOffersMsProductsCache3";
	
	public static final String SVC_PRODUCTS_CACHE_FLAG = "svc-epoch-enable-products-cache";
	
	public static final String SVC_BENEFITS_CACHE_FLAG = "svc-epoch-enable-benefits-cache";
	
	public static final String EPOCHOFFERSMS_CT_BENEFITS_PRIMARY_CACHE_MAP_NAME = "epochOffersMsBenefitsCache1";

	public static final String EPOCHOFFERSMS_CT_BENEFITS_BACKUP_CACHE_MAP_NAME = "epochOffersMsBenefitsCache2";

	public static final String EPOCHOFFERSMS_CT_BENEFITS_PVT_CACHE_MAP_NAME = "epochOffersMsBenefitsCache3";

	public static final String CPOPOFFERSMS_CT_PRIMARY_WIRELESS_CACHE_MAP_NAME = "cpopOffersMsWirelessPrimaryCache";

	public static final String CPOPOFFERSMS_CT_BACKUP_WIRELESS_CACHE_MAP_NAME = "cpopOffersMsWirelessBackupCache";
	
	public static final String EPOCHOFFERSMS_IXP_PRIMARY_CACHE_MAP_NAME = "epochOffersMsIXPCache1"; 

	/** The Constant epoch internal flag. */
	public static final String SVC_EPOCH_INTERNAL = "svc-epoch-internal";
	/** The Constant cpop internal flag. */
	public static final String SVC_EPOCH_OPUS_INTERNAL = "svc-epoch-opus-internal";
	/** The Constant cpop internal flag. */
	public static final String SVC_EPOCH_SERVICES_INTERNAL = "svc-epoch-services-internal";

	public static final String BENEFITCODE = "benefitCode";
	
	public static final String BENEFITCODES = "benefitCodes";
	public static final String COOLOFFPERIOD = "coolOffPeriod";

	public static final String STARTDATE = "startDate";

	public static final String ENDDATE = "endDate";
	public static final String SMART_ATTRIBUTES = "get-only-smart-attributes";

	public static final String CAMPAIGN_START_DATE = "campaign-start-date";

	public static final String CAMPAIGN_END_DATE = "campaign-end-date";

	public static final String AVAILABLE = "A";

	public static final String EXPIRED = "E";

	public static final String SINGLE_USE_COUPON = "singleUse";

	public static final String COUPON_NOT_EXISTS = "Coupon does not exists";
	
	public static final String SYSTEM_ERROR = "System Error";

	public static final String COUPON_ALREADY_REDEEMED = "Coupon was already redeemed";
	
	public static final String COUPON_INELGIBLE_ERROR = "Coupon is not valid for the cart items provided in the request";

	public static final String COUPON_INVALID_PURCHASE = "Coupon is not valid for the purchase";

	public static final String COUPON_ACCOUNT_INELIGIBLE = "Account is not eligible for the coupon";

	public static final String COUPON_EXPIRED = "Coupon has Expired";

	/** UCC enabled flag. */
	public static final String SVC_EPOCH_UCC_ENABLED = "svc-epoch-ucc-enabled";

	/** UCC b pid enabled flag. */
	public static final String SVC_EPOCH_UCC_B_ENABLED = "svc-epoch-ucc-b-enabled";

	public static final String UVERSE_CG_ACCOUNT_TYPE = "uversedtv";

	public static final String WIRELESS_DEVICE = "wireless-device";

	public static final String WIRELESS_SDG = "wireless-sdg";

	public static final String WIRELESS_SDF = "wireless-sdf";

	public static final String WIRELESS_ADDON = "wireless-addon";

	public static final String WIRELESS_PLAN = "wireless-plan";

	public static final String WIRELESS_INSTALLMENT = "wireless-instalment";

	public static final String WIRELESS_ACCESSORY = "wireless-accessory";

	public static final String WIRELESS_COMMITMENT_TERM = "wireless-commitment-term";

	public static final String CAMPAIGN = "campaign";

	public static final String USED_COUPON = "used";

	public static final String AVAILABLE_COUPON = "Available";

	public static final String REDEEMED_COUPON = "Redeemed";

	public static final String EXPIRED_COUPON = "Expired";

	public static final List<String> COUPON_STATUS_LIST = new ArrayList<>(
			Arrays.asList(USED_COUPON, REDEEMED_COUPON, EXPIRED_COUPON, AVAILABLE_COUPON));

	public static final String BILLING_BENEFIT_ID = "billingBenefitId";

	public static final String BILLING_BENEFIT_CODE = "billingBenefitCode";

	public static final String PROMOTION_TYPE = "promotionType";

	public static final String DATE_FORMAT_CG = "yyyy-MM-dd HH:mm:ss.SSSZ";

	public static final int ONE_THUSAND = 1000;
	// benefit types

	public static final String BENEFIT_PERCENT_OFFER = "percent-off";

	public static final String BENEFIT_FREE_PROMO = "free-promo";

	public static final String BENEFIT_FLAT_OFF = "flat-off";

	public static final String BENEFIT_FLAT_RATE = "flat-rate";

	public static final String DTV_RIO_SERVICE = "dtvRioService";

	public static final String PURCHASE_TYPE_BOTH = "Both";

	public static final String PURCHASE_TYPE_SEPARATOR = "\\|";

	public static final String FEATURE_TOGGLE_ZIP_SALES_OFFER = "svc-sales-zip-offer";

	public static final String FEATURE_TOGGLE_ZIP_CACHING_ENABLED = "svc-zip-offer-cache";

	public static final String MIG_OFFERS_R001 = "CTLG_E400038";

	public static final String MIG_OFFERS_R002 = "INVALID-MIGRATION-REQUEST-10002";

	public static final String MIGRATION_CT_VALIDATION_TEXT = "migration-validation-error";

	public static final String SVC_EPOCH_MOBILITY_REWARD = "svc-epoch-mobility-reward";

	public static final String CONTENT_CATEGORY_YES = "yes";

	public static final String CONTENT_CATEGORY_NO = "no";

	public static final String CONTENT_CATEGORY_NONADULT = "NonAdult";

	public static final String CONTENT_CATEGORY_ADULT = "Adult,NonAdult";

	public static final String MULTI_USE_TYPE_2_COUPON = "multi-use2";

	public static final String EDSP_CART_CONTEXT_VALIDATION = "edsp-validation-error";

	public static final String EDSP_OFFER_CT_ERROR_CODE = "CTLG_E500038";

	public static final String EDSP_STRING = "EDSP";
	
	public static final String TAZ_STRING = "TAZ";
	
	public static final String TAZBYOD_STRING = "TAZBYOD";
	
	public static final String TAZCONTRACT_STRING = "TAZCONTRACT";

	public static final String BBTV_STRING = "BBTV";
	
	// Represents the Contract Indicator value for "FRONTPORCH"
	public static final String FRONTPORCH_STRING = "FRONTPORCH";
	public static final String ROAD_RUNNER = "RR";
	public static final String GENRE = "GENRE";

	public static final String INVALID_STATUS = "Invalid";

	public static final String QUOTA_REACHED_COUPON = "quotaReached";

	public static final String COUPON_INACTIVE = "This coupon code is not yet active";

	// CatalogOne Changes
	public static final String CPOP_CATALOB_ONE_CACHE_MAP_NAME = "CpopCatalogOneProductOfferings";
	public static final String CATALOG_ONE_ONLINE_ADD_ON_MAP = "c1Addons";
	public static final String CATALOG_ONE_ONLINE_PRODUCT_OFFERING_KEY = "CatalogOne_ProductOfferings_Plan";
	public static final String CATALOG_ONE_ONLINE_ADD_ON_KEY = "CatalogOne_AddOn_PlanId";
	public static final String CATALOG_ONE_OFFER_RELATION_ID = "CatalogOne_RelationId";
	public static final String C1_KEY_DIS_SERVICE_ABT_CHECK = "disableServiceabilityCheck=";
	public static final String C1_KEY_INCLUDE_ALL_NODES = "includeAllNodes=";
	public static final String C1_KEY_RELATION_TYPE = "relationType=";
	public static final String C1_KEY_RELATION_ENT_ID = "relatedEntityId=";
	public static final String C1_KEY_RELATION_ROLE = "relationRole=";
	public static final String C1_AMPERSAND = "&";
	public static final String C1_UNDERSCORE = "_";
	public static final String C1_TRUE = "true";
	public static final String C1_FALSE = "false";
	public static final String C1_KEY_PROD_OFFER_TYPE_GRP = "productOfferingTypeGroup=";
	public static final String C1_VAL_MULTI_MOB_OFFER = "multilineMobileOffer";
	public static final String C1_VAL_COMMERCIAL_REL = "commercialRelation";
	public static final String C1_KEY_SALES_CHANNEL = "salesChannel=";
	public static final String CHANNEL = "CHANNEL";
	public static final String RELROLE = "RELROLE";
	public static final String RELTYPE = "RELTYPE";
	public static final String RELENTITY = "RELENTITY";
	public static final String C1_SALES_CHANNEL = "SelfService";

	public static final String PRODUCTS = "products";
	public static final String CURRENT_DATE = "currentDate";
	public static final String HD_ACCESS_FEE = "HDAccessFee";
	public static final String GENIE_HD_DVR = "genieHDDVR";
	public static final String GENIE_MINI = "genieMini";
	public static final String GENIE_SERVER = "genieServer";
	public static final String GENIE_MINI_WIRELESS = "genieMiniWireless";
	public static final String GEMINI_WIRELESS = "geminiWireless";
	public static final String INCLUDED_WITH_SERVER = "includedWithServer";
	public static final String INCLUDED = "included";
	public static final String CHARGEABLE = "chargeable";
	public static final String DISCOUNTED = "discounted";

	public static final String ACCOUNT_TYPE_RSN = "I";
	public static final String SUB_ACCOUNT_TYPE_RSN = "R";
	public static final String MSG_SEQUENCE_NUM = "1";
	public static final String MSG_TOTAL_SEQUENCE_NUM = "1";

	public static final String ADD = "add";
	public static final String PRESERVE = "preserve";
	public static final String NOOFMONTHS = "NoOfMonths";
	public static final String TYPE_AGREEMENT = "agreement";
	public static final String TYPE_ADJUSTMENT = "adjustment";
	public static final String TYPE_RECEIVER = "receiver";
	public static final String TYPE_ACCESSORY = "accessory";
	public static final String TYPE_OFFERDETAIL = "offerDetail";
	public static final String CREDIT = "credit";
	public static final String DEVICE = "video device";
	public static final String ENABLER_PPC = "88830604";
	public static final String FEE_TYPE = "feeType";
	public static final String FTV_COUNT = "ftvCount";
	public static final String REMOTE = "Remote";
	public static final String ENABLER = "enabler";
	public static final String STMS = "stms";
	public static final String VIRTUAL_TYPE = "virtual";
	public static final String NA = "NA";
	public static final String FLAT_RATE = "FLATRATE";
	public static final String TYPE_SPORTS = "sports";

	public static final String PICK_CODE = "PickCode";

	public static final String COMMA = ",";

	public static final String BOTH = "both";
	public static final String ADDITIONAL = "additional";
	public static final List<String> LINE_TYPES = new ArrayList<>(Arrays.asList(BOTH, ADDITIONAL));
	public static final String CALL_TO_ORDER = "callToOrder";
	public static final String STB_NAME = "STBName";
	public static final String SUPPRESS_UMLIMITED_OFFER = "suppresss-unlimited-offer";
	public static final String SUPPRESS_UMLIMITED_RACKRATE_OFFER = "suppresss-unlimited-rackrate-offer";

	public static final String DVANA1 = "DVANA1";
	public static final String DVANA2 = "DVANA2";

	public static final String EVENT_CHANGE = "CHANGE";
	public static final String CLIENT_APPLICATION = "SERVICES";
	public static final String ACTIONTYPE_NOCHANGE = "NOCHANGE";
	public static final String SUSPEND = "SUSPEND";
	public static final String PENDING_SUSPEND = "PENDING SUSPEND";

	public static final String TIVO_FEE = "tivoServiceFeeDTV";
	public static final String BUSINESS_SEG = "REG";
	public static final String BUSINESS_SEGMENT_PTR = "PTR";

	public static final String SLASH = "/";
	public static final String EMPTYSTRING = "";

	// Added for the US# 158528 (Longhorn Long Term) - started
	/** The Constant IDPCTX_DEALER_CODE_1. */
	public static final String IDPCTX_PARTNER_DEALER_CODE1 = "idpctx-dealer-code1";
	/** The Constant IDPCTX_DEALER_CODE_2. */
	public static final String IDPCTX_PARTNER_DEALER_CODE2 = "idpctx-dealer-code2";
	/** The Constant IDPCTX_LOCATION_ID. */
	public static final String IDPCTX_PARTNER_LOCATION_ID = "idpctx-locationId";
	// Added for the US# 158528 - End

	public static final String FIRETV = "FIRETV";

	public static final String EQUIPMENT_REBATE = "equipment-rebate";

	public static final String EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CACHE_MAP_NAME = "epochOfferMsGlobalConfigurations";

	public static final String EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CACHE_MAP_NAME_BACKUP = "epochOfferMsGlobalConfigurationsBackUp";

	public static final String EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CACHE_MAP_NAME_PVT = "epochOfferMsGlobalConfigurationsPvt";

	public static final String EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_RECONNECT_ELIGIBILITY_WINDOW = "ReconnectEligibilityWindow";
	
	public static final String EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_ELIGIBLITY_CHECK_MONTHS = "PauseEligiblityMonths";

	public static final String EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_MAX_PAUSE_MONTHS = "MaxPauseMonths";
	
	public static final String EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_ADDI_INFO_CHECK = "AdditionalInfoCheck";

	public static final String EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_CUST_SEG = "CustomerSegment";
	
	public static final String EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_CONTRACT_INDICATOR = "ContractIndicator";
	
	public static final String EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_NOT_ALLOWED_PROVIDER = "ProviderNotAllowed";

	public static final String EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_SALES_CHANNELS = "checkEligibilitySalesChannels";
	
	public static final String GLOBAL_CONFIGURATIONS_KEY = "globalConfigKey";

	public static final String GLOBAL_CONFIGURATIONS_VALUE = "globalConfigValue";

	public static final String SERVICE_CODE = "serviceCode";
	public static final String GENIE_2_COUNT = "Genie2Count";

	public static final String SEVERITY_ERROR = "error";
	public static final String SEGMENT = "segment";
	
	public static final String SALES_CRM = "salesCRM";
	public static final String CCAP = "ccap";
	public static final String DPP = "dpp";
	public static final String DIRECT_INTEGRATION_PARTNER = "directIntegrationPartner";
	public static final String OSPREY = "osprey";
	public static final String IPOR="ipor";
	
	public static final String ABP="autobillPay";
	public static final String PB="paperlessBill";
	public static final String DATE_FORMAT_MM_DD_YYYY_SLASH = "MM/dd/yyyy";
	public static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";
	public static final String DATE_FORMAT_M_D_YYYY = "M-d-yyyy";
	
	public static final String ELIGIBILITYMONTHS = "eligibilityMonths";
	public static final String MAXPAUSEMONTHS = "maxpauseMonths";
	
	public static final String ADDITIONALINFOCHECK = "additionalInfoCheck";
	public static final String CUSTOMERSEGMENT = "customerSegment";
	
	public static final String CONTRACTINDICATOR = "contractIndicator";
	public static final String PROVIDERNOTALLOWED = "providerNotAllowed";
	
	public static final String DISPLAY_PRICE = "DISPLAYPRICE";
    
    public static final List<String> IGNORE_TAXLINEITEMDATA=Arrays.asList("36053");
    public static final String DIRECTV_STREAM_ONLINE = "directvStreamOnline";
    
    public static final String 	BPC_4K_SERVICE = "B103750";
    public static final String 	BPC_4K_VOD = "B103133";
	
    public static final Map<String,Integer> PREMIUM_PICK5_MASKS = new HashMap<>();
    static {
    	PREMIUM_PICK5_MASKS.put("P2142",4);
    	PREMIUM_PICK5_MASKS.put("P2162",22);
    	PREMIUM_PICK5_MASKS.put("P2141",2);
    	PREMIUM_PICK5_MASKS.put("P2164",28);
    	PREMIUM_PICK5_MASKS.put("P2148",17);
    	PREMIUM_PICK5_MASKS.put("P2156",11);
    	PREMIUM_PICK5_MASKS.put("P2155",7);
    	PREMIUM_PICK5_MASKS.put("P2161",14);
    	PREMIUM_PICK5_MASKS.put("P2169",30);
    	PREMIUM_PICK5_MASKS.put("P2152",12);
    	PREMIUM_PICK5_MASKS.put("P2167",27);
    	PREMIUM_PICK5_MASKS.put("P2163",26);
    	PREMIUM_PICK5_MASKS.put("P2145",3);
    	PREMIUM_PICK5_MASKS.put("P2150",10);
    	PREMIUM_PICK5_MASKS.put("P2165",15);
    	PREMIUM_PICK5_MASKS.put("P2168",29);
    	PREMIUM_PICK5_MASKS.put("P2147",9);
    	PREMIUM_PICK5_MASKS.put("P2140",1);
    	PREMIUM_PICK5_MASKS.put("P2166",23);
    	PREMIUM_PICK5_MASKS.put("P2146",5);
    	PREMIUM_PICK5_MASKS.put("P2158",13);
    	PREMIUM_PICK5_MASKS.put("P2151",18);
    	PREMIUM_PICK5_MASKS.put("P2157",19);
    	PREMIUM_PICK5_MASKS.put("P2143",8);
    	PREMIUM_PICK5_MASKS.put("P2144",16);
    	PREMIUM_PICK5_MASKS.put("P2149",6);
    	PREMIUM_PICK5_MASKS.put("P2170",31);
    	PREMIUM_PICK5_MASKS.put("P2159",21);
    	PREMIUM_PICK5_MASKS.put("P2153",20);
    	PREMIUM_PICK5_MASKS.put("P2160",25);
    	PREMIUM_PICK5_MASKS.put("P2154",24);
	}
    
    public static final Map<Integer,String> PREMIUM_PICKS = MapUtils.invertMap(PREMIUM_PICK5_MASKS);
    
    public static final Map<String,Integer> INDIVIDUAL_PREMIUM_PICKS = new HashMap<>();
    static {    	
    	INDIVIDUAL_PREMIUM_PICKS.put("P2140",1);
    	INDIVIDUAL_PREMIUM_PICKS.put("P2141",2);
    	INDIVIDUAL_PREMIUM_PICKS.put("P2142",4);
    	INDIVIDUAL_PREMIUM_PICKS.put("P2143",8);
    	INDIVIDUAL_PREMIUM_PICKS.put("P2144",16);
    }
    
    public static final String SPORTS_PACK_SC1="P120570";
    public static final String SPORTS_PACK_SC2="P120572";
    public static final String BOLTON_NO_LOCALS_SAVINGS="B120668";
    public static final String TRACKING_CODE_12M="B101214";
    public static final String TRACKING_CODE_24M="B5712";
    public static final String TRACKING_CODE_M2M="B5714";    
    
    public static final String CUSTOM_DATA_EQUIPMENT_TYPE="customData.equipmentType";
	public static final String CUSTOM_DATA_INSTALL_ACTION_REQUIRED="customData.installActionRequired";
    public static final String DIRECTV_ONLINE_SERVICES="directvOnlineServices";
    public static final String DIRECTV_ONLINE_SALES="directvOnlineSales";
    
    public static final String FEATURE_TOGGLE_PERFORMANCE_UPDATES_ENABLED = "svc-epoch-performance-updates";
    public static final String SAVE_NOW = "SaveNow";
    
    public static final String FEATURE_TOGGLE_REPLACEMENT_DEVICE_BASED_ON_PP = "svc-epoch-replacementdevice-based-on-protectionplan";
    public static final String FEATURE_TOGGLE_REPLACEMENT_DEVICE_BASED_ON_PP_SECOND = "svc-epoch-replacementdevice-based-on-protectionplan-second";
    public static final String SERVER_DATE = "serverDate";
    public static final String FEATURE_TOGGLE_DIP_OFFER_PRICE_ENABLED = "svc-epoch-dip-offer-price-flag-enabled";
    public static 	final ObjectMapper JSON_MAPPER = new ObjectMapper();
    
    public static final String DIRECTV_CENTERS="directvCenters";
    
    public static final Double ZERO_DOLLAR=0.0;
    
    public static final String[] VALID_ACCOUNT_STATUS = {"ACTV", "PSUS", "PDIS"};
    public static final String[] OFFER_ACTION_TYPES = {"UPGRADE", "DOWNGRADE", "RETENTION", "OTHER"};
    public static final String VDAS_SLIMLINE_OFFER = "OF_ACCESSORY-DIRECTV-SLIMLINE_satellite";
    public static final String VDAS_SLIMLINE_TOKENS = "ACCESSORY-DIRECTV-SLIMLINE";
    public static final String ODU = "ODU";
	public static final List<String> LCC_BCODE = new ArrayList<>(Arrays.asList("B121312"));
	public static final String FEATURE_SPORTS_BETA_ZIPCODE_GATING = "svc-epoch-sports-beta-zipcode-gating";
	public static final String EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_COMPATIBLESPK_SKU1 = "CompatibleSportSpack1Pkg";
	
	public static final String VIDEO_BRIDGE_OFFER = "OF_DEVICE-WIRELESS-VIDEO-BRIDGE-OWNED_satellite";
	public static final String VIDEO_BRIDGE = "wirelessVideoBridge";
	public static final String WIRELESS_VIDEO_BRIDGE = "Wireless Video Bridge";
	
	public static final Integer SPORTS_MVP_RANK = 3;
	public static final String FEATURE_FLAG_REWARD_CARD_GEO_LOCATION_ENABLED = "svc-epoch-reward-card-geo-location-enabled";
	public static final String FEATURE_FLAG_ACAPS_ENABLED = "svc-epoch-acaps-enabled";
	public static final String FEATURE_GEM_WHOLE_HOME_ENABLED = "svc-epoch-gem-whole-home-enabled";

	public static final String DEPENDENT_OFFER = "dependentOffer";
	public static final String NON_STACKABLE = "nonStackable";
	public static final String ADD_ACAPS = "Add";
	public static final String REMOVE_RCAPS = "Remove";
	public static final String REDIS_CACHE_SALESCHANNEL = "salesChannel";
	public static final String FEATURE_FLAG_TRUTV_TCM_ANE_ENABLED = "svc-epoch-trutv-tcm-ane-flag-enabled";

	public static final String STB = "stb";
	public static final String ASSISTED_SALES = "assistedSales";
	public static final String ASSISTED_SALES_SALES = "assistedSalesSales";
	public static final String FEATURE_FLAG_RETENTION_ENABLED = "svc-epoch-retention-enabled";
	public static final String GOOGLE = "GOOGLE";
	public static final String REDIS_CACHE_SERVED_MARKET_DEFAULT_ZIP_CODE = "ServedMarketDefaultZipCode";
	public static final String REDIS_CACHE_SERVED_MARKET_DEFAULT_FIPS_CODE = "ServedMarketDefaultFipsCode";
	public static final String LOCALS = "locals";
	public static final String AUTO_RENEWAL_INDICATOR = "autoRenewalInd";
	public static final String RTC_INDICATOR= "RTC";
	public static final String EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_ELIGIBLITY_IAP_PLATFORM_NOTALLOWED = "IapPlatformNotAllowed";
	public static final String GENREBASE= "GenreBase";
	public static final String REDIS_CACHE_LOCALS_ENABLED_FLAG = "localsEnabledFlag";
	public static final String ROADRUNNER = "NFTV2025";
	public static final String DTV_SWIMLANE = "DTVSWIMLANE";
	public static final String FEATURE_FLAG_LOCALS_VALIDATE_CART_ENABLED_FLAG = "svc-epoch-locals-validate-cart-enabled";
	public static final String FEATURE_FLAG_VC_PERFORMANCE_ISSUE_FLAG = "svc-epoch-vc-performance-issue";
	public static final String REDIS_CACHE_VALIDATE_CART_OFFER_PRODUCT_TYPES = "validateCartOfferProductTypes";
	public static final String REDIS_CACHE_LOCALS_ZERO_DOLLAR_BOLT_ON = "localsZeroDollarBoltOn";
	public static final String GLOBAL_ELIGIBILITYRULE_PREDICATEEVAL = "predicateEval";
	public static final String GLOBAL_ELIGIBILITYRULE_PREDICATES = "predicates";
	public static final String GLOBAL_ELIGIBILITYRULE_ATTRIBUTE_REQUESTPATH = "pathVariable";
	public static final String GLOBAL_ELIGIBILITYRULE_ATTRIBUTE_NAME = "attributeName";
	public static final String GLOBAL_ELIGIBILITYRULE_ATTRIBUTE_RESPONSEPATH = "responseAttributePath";
	public static final String GLOBAL_ELIGIBILITYRULE_ATTRIBUTE_STATICVALUE = "staticValue";
	public static final String EPOCHOFFERMS_GLOBAL_ELIGIBILITYRULES_CACHE_MAP_NAME= "epochOfferMsGlobalEligibilityRules";
	public static final String FEATURE_TOGGLE_DTVN_GLOBALELIGIBLITYRULES_ENABLED = "svc-epoch-globaleligibilityrules-enabled";
	public static final String EPOCH_GLOBAL_CONFIG = "global-epoch-configurations";
	public static final String REDIS_CACHE_SERVED_MARKET_FLAG = "isServedMarket";
	public static final String MANDATORY = "mandatory";
	public static final String ARS_FEE = "ARSFee";
	public static final String REDISCACHE_GLOBAL_CONFIG_PARTNERCATALOG_EXCLUSIVE_SALES_CHANNEL = "partnerCatalogExclusiveSalesChannels";
	public static final String REDISCACHE_GLOBAL_CONFIG_PARTNERCATALOG_IAP_PARTNER_TYPE = "partnerCatalogExclusiveIapPartnerType";
	public static final String SPORTS_PACK="SPORTS_PACK";
	public static final String GENRE_TAZBYOD="GENRE_TAZBYOD";
	public static final String FEATURE_ROADRUNNER_ENABLED = "svc-epoch-roadrunner-enabled";
	public static final String LOCALS_BOLTON_PRICE = "localsBoltonPrice";
	public static final String LOCALS_SUBCATEGORY = "Locals";
	public static final String FEATURE_MDU_DTH_ENABLED = "svc-epoch-mdu-dth-enabled";	
	public static final String MDU_DTH = "MDU-DTH";
	public static final String FEATURE_STACKABILITY_RULE_FOR_VIDEO_DEVICES_ENABLED = "svc-epoch-stackability-rule-for-video-device-enabled";
	public static final String FEATURE_NON_STACKABLE_RULE_ENABLED = "svc-epoch-nonstackable-rule-enabled";
	public static final String EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_GENRE_PRODUCTCONDITIONS = "GenreProductConditions";
	public static final String EPOCH_PRODUCT_TYPES_ATTR_PRIMARY_CACHE_MAP_NAME = "epochproducttypesattrsprimary";
	public static final String EPOCH_PRODUCT_TYPES_ATTR_BACKUP_CACHE_MAP_NAME = "epochproducttypesattrsbackup";
	public static final String EPOCH_PRODUCT_TYPES_ATTR_PVT_CACHE_MAP_NAME = "epochproducttypesattrspvt";
	public static final String FEATURE_TOGGLE_VALIDATE_GRAPHQL = "svc-epoch-validate-graphql";
	public static final String FEATURE_FTC_SALES_CHANNEL = "svc-ftc-sales-channel";
	public static final String FLOW_TYPE_GET_OFFERS = "getOffers";
	
	public static final String REDIS_CACHE_GENRE_VALIDATE_CART_OFFER_PRODUCT_TYPES = "genreVCOfferProductTypes";
	public static final String REDIS_CACHE_TAZBYOD_VALIDATE_CART_OFFER_PRODUCT_TYPES = "tazByodVCOfferProductTypes";
	public static final String FEATURE_RR_FEE_FILTER_ENABLED = "svc-epoch-rr-fee-filter-enabled";
	
	public static final String ATTDTV = "ATTDTV";
	public static final String LOCAL_CHANNELS = "LocalChannels";
	public static final String NO = "No";
	
	public static final String MDUDTH_PILOT_DEALERCODES = "MDUDTH-PILOT-DEALERCODES";
	public static final String INDIRECT_SALES_CHANNEL="indirect";
	public static final String FEATURE_ELIGIBLE_IAP_PARTNERS_ENABLED = "svc-epoch-eligible-iap-partner-enabled";
	
	public static final String EXCLUSION_DIRECTINTEGRATIONPARTNER_DEALERCODE="exclusion-directIntegrationPartner-dealerCode";
	public static final String EXCLUSION_DIRECTINTEGRATIONPARTNER_SALESCHANNEL="exclusion-directIntegrationPartner-salesChannel";
	public static final String EXCLUSION_DIRECTINTEGRATIONPARTNER_SALESSUBCHANNEL="exclusion-directIntegrationPartner-salesSubChannel";
	public static final String FEATURE_IAP_REMOVE_INCLUDED_PRODUCTS_ENABLED = "svc-epoch-iap-remove-included-products-enabled";
	public static final String EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_REMOVE_PRODUCT_FROM_INCLUDED_PRODUCTS = "iapRemoveProductFromIncludedProducts";
	public static final String REDIS_CACHE_COHORTS_FOR_VC_COMMON_RULES = "eligibleCohortForValidateCartCommonRules";
	public static final String REDIS_CACHE_COHORTS_FOR_VC_LOCAL_RULES = "eligibleCohortForValidateCartLocalRules";
	public static final String REDIS_CACHE_COHORTS_FOR_VC_GENRE_RULES = "eligibleCohortForValidateCartGenreRules";
	public static final String EMBEDDED_SVOD = "EmbeddedSVOD";
	public static final String FEATURE_VALIDATECART_GENERIC_FLOW = "svc-epoch-validatecart-generic-flow";
	public static final String EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_DEVICE_PRODUCTCONDITIONS = "DeviceDependentProductConditions";
	public static final String REDIS_CACHE_COHORTS_FOR_VC_DEVICE_RULES = "eligibleCohortForValidateCartDeviceRules";
	public static final String FEATURE_FLAG_NO_DEVICE_FRAMEWORK_ENABLED = "svc-epoch-nodevice-framework-enabled";
	public static final String MDU_INCLUDED_PRODUCT_EXCLUSIONS = "MDUIncludedProductExclusions";
	public static final String FEATURE_TOGGLE_MDU_INCLUDED_PRODUCT_EXCLUSIONS = "svc-epoch-mdu-included-product-exclusions";
	public static final String FEATURE_EPOCH_SERVICE_ACTIVATION_FEE_ENABLED="svc-epoch-service-activation-fee-enabled";
	public static final String FEATURE_FLAG_VALIDATE_CART_PRODUCT_GRP = "svc-epoch-product-group-vc-scenario-enabled";	
	public static final String FEATURE_FLAG_OFFER_CODE_VALID_SUBSCRIBER_ENABLED = "svc-epoch-offer-code-valid-subscriber-enabled";
	public static final String EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_MVP_PEACOCK_PRODUCTCONDITIONS = "MVPPeakcockCodesConditions";
	public static final String MEP_PEACOCK_BUNDLE_OFFER = "OF_BOLTON-MEP-PEACOCK-BUNDLE_satellite";
	public static final String EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_MVPX_SKIP_COMBINATION = "MVPXSkipCombinations";
	public static final String MEP_PRODUCT_CODE = "P6688";
	public static final String MEP_SAVE_PRODUCT_CODE = "P121606";
	public static final String PEACOCK_PRODUCT_CODE = "R120484";
	
	public static final String DISPLAY_TYPE_IN_CART = "displayTypeInCart";
	public static final String FEATURE_FLAG_OPTIMOMAS_REPACKAGING_ENABLED = "svc-epoch-optimomas-repackaging-enabled";

	public static final String FEATURE_FLAG_GLOBAL_VALIDATE_CART_EVALUATOR = "svc-epoch-global-vc-rule-evaluator";

	public static final String  DISCOUNTRULES ="DISCOUNTRULES";
	public static final String  DELAYPROVISIONINGBENEFITEXCEPTIONS ="DELAYPROVISIONINGBENEFITEXCEPTIONS";

	public static final String FEATURE_FLAG_DELAY_PROVISIONING_ENABLED = "svc-epoch-delay-provisioning-enabled";
	public static final String DELAY_PROVISIONING = "delayProvisioning";
	public static final String SALES = "sales";
	public static final String SERVICES = "services";
	public static final String MESSAGES_BY_KEY_MESSAGE_TYPE = "messageType";
	public static final String MESSAGES_BY_KEY_ALL_MESSAGES = "allMessages";
	public static final String KEY = "key";
	public static final String VALUE = "value";
	public static final String ALL_MESSAGES_SALES_CHANNEL = "salesChannelForMessages";
	public static final String ALL_MESSAGES_FLOW_TYPE = "flowType";
	public static final String SHORT_MESSAGE = "ShortMessage";
	public static final String LONG_MESSAGE = "LongMessage";
	public static final String DEFAULT = "default";
	public static final String EXT_PROMO_TYPE = "extPromoType";
	public static final String EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_SALES_CHANNEL_TO_SUPPRESS_NON_BP = "salesChToSuppressNonBPOffInBaseCall";
	
	public static final String FEATURE_QUALIFIER_INCOMP_FILTER_ENABLED = "svc-epoch-qualifier-incomp-filter-enabled";
	public static final String FEATURE_INCL_PRD_PRICE_FILTER_ENABLED = "svc-epoch-incl-prd-price-filter-enabled";
	public static final String FEATURE_FLAG_PEACOCK_VALIDATE_CART_RULES = "svc-epoch-peacock-validate-cart-logic-flag-enabled";
	public static final String TRACKING_CODE_MEP="B100625";
	public static final String FEATURE_FLAG_QUALIFYINGPRODUCT_CHECKON_PRODUCTCODES = "svc-epoch-qualifyingproduct-checkon-productcodes-enabled";
	public static final String FEATURE_FLAG_INCLUDE_PRODUCT_FILTERS = "svc-epoch-included-products-filters-logic";
	public static final String FEATURE_FLAG_CONFLICTING_PRODUCT_FILTERS = "svc-epoch-conflictingproducts-filter-bysaleschannel";
	public static final String EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_INCOMPATIBLE_SKU_RULES = "IncompatibleSKUResolutionRules";
    public static final String FEATURE_FLAG_CONFLICT_PRODUCT_RULE_VC = "svc-epoch-conflicting-product-rule-vc";
    public static final String FEATURE_FLAG_COMPLIANCE_RANK_BILLING_PROD_GRP_VC = "svc-epoch-comp-rank-billing-prod-vc";
    
    public static final String FEATURE_SVC_EPOCH_ASYNC_CALLS_ENABLED = "svc-epoch-async-calls-enabled";
    public static final String FEATURE_SVC_EPOCH_VALIDATECART_SEPARATE_FLOW = "svc-epoch-vc-separate-flow-enabled";
    public static final String EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_FREEDEVICEPROMOS = "freeDevicePromos";
}
