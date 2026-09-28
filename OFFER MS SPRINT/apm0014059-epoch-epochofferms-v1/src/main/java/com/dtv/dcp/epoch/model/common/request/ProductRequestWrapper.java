package com.dtv.dcp.epoch.model.common.request;

import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductRequestWrapper {


	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The logged In Indicator. */
	private String loggedInId;

	/** The direc TV now. */
	private ProductRequest productRequest;

	/** The linked Wireless Account. */
	private String authAccountsWireless;

	/** The Dtvn Account. */
	private String dtvnAccount;

	/** The idpctx session Id. */
	private String sessionId;

	/** The linked UverseAccountNums. */
	private String linkedUverseAccountNums;

	/** The iptvMigration. */
	private boolean iptvMigration;

	/** The iptvMigrationIntent. */
	private boolean iptvMigrationIntent;

	/** The promotional offers. */
	private String promotionalOffers;

	/** The channel. */
	private String channel;

	/**The IOContext Object*/
	//private IOContext ioContext;

	/**
	 * The agentType.
	 */
	private String agentType;


	private CTProductRequest ctProductRequest;

	private boolean isMobility;
	
	/**
	 * The isEmployeeAccount.
	 */
	private boolean isEmployeeAccount;

	public boolean isMobility() {
		return isMobility;
	}

	public void setMobility(boolean mobility) {
		isMobility = mobility;
	}

	public CTProductRequest getCtProductRequest() {
		return ctProductRequest;
	}

	public void setCtProductRequest(CTProductRequest ctProductRequest) {
		this.ctProductRequest = ctProductRequest;
	}

	/**
	 * @return the loggedInId
	 */
	public String getLoggedInId() {
		return loggedInId;
	}

	/**
	 * @param loggedInId the loggedInId to set
	 */
	public void setLoggedInId(String loggedInId) {
		this.loggedInId = loggedInId;
	}

	/**
	 * @return the productRequest
	 */
	public ProductRequest getProductRequest() {
		return productRequest;
	}

	/**
	 * @param productRequest the productRequest to set
	 */
	public void setProductRequest(ProductRequest productRequest) {
		this.productRequest = productRequest;
	}

	/**
	 * @return the authAccountsWireless
	 */
	public String getAuthAccountsWireless() {
		return authAccountsWireless;
	}

	/**
	 * @param authAccountsWireless the authAccountsWireless to set
	 */
	public void setAuthAccountsWireless(String authAccountsWireless) {
		this.authAccountsWireless = authAccountsWireless;
	}

	/**
	 * @return the dtvnAccount
	 */
	public String getDtvnAccount() {
		return dtvnAccount;
	}

	/**
	 * @param dtvnAccount the dtvnAccount to set
	 */
	public void setDtvnAccount(String dtvnAccount) {
		this.dtvnAccount = dtvnAccount;
	}

	/**
	 * @return the sessionId
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * @param sessionId the sessionId to set
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * @return the linkedUverseAccountNums
	 */
	public String getLinkedUverseAccountNums() {
		return linkedUverseAccountNums;
	}

	/**
	 * @param linkedUverseAccountNums the linkedUverseAccountNums to set
	 */
	public void setLinkedUverseAccountNums(String linkedUverseAccountNums) {
		this.linkedUverseAccountNums = linkedUverseAccountNums;
	}

	/**
	 * @return the iptvMigration
	 */
	public boolean isIptvMigration() {
		return iptvMigration;
	}

	/**
	 * @param iptvMigration the iptvMigration to set
	 */
	public void setIptvMigration(boolean iptvMigration) {
		this.iptvMigration = iptvMigration;
	}

	/**
	 * @return the iptvMigrationIntent
	 */
	public boolean isIptvMigrationIntent() {
		return iptvMigrationIntent;
	}

	/**
	 * @param iptvMigrationIntent the iptvMigrationIntent to set
	 */
	public void setIptvMigrationIntent(boolean iptvMigrationIntent) {
		this.iptvMigrationIntent = iptvMigrationIntent;
	}

	/**
	 * @return the promotionalOffers
	 */
	public String getPromotionalOffers() {
		return promotionalOffers;
	}

	/**
	 * @param promotionalOffers the promotionalOffers to set
	 */
	public void setPromotionalOffers(String promotionalOffers) {
		this.promotionalOffers = promotionalOffers;
	}

	/**
	 * @return the channel
	 */
	public String getChannel() {
		return channel;
	}

	/**
	 * @param channel the channel to set
	 */
	public void setChannel(String channel) {
		this.channel = channel;
	}


	public String getAgentType() {
		return agentType;
	}

	public void setAgentType(String agentType) {
		this.agentType = agentType;
	}

	/**
	 * @return the isEmployeeAccount
	 */
	public boolean isEmployeeAccount() {
		return isEmployeeAccount;
	}

	/**
	 * @param isEmployeeAccount the isEmployeeAccount to set
	 */
	public void setEmployeeAccount(boolean isEmployeeAccount) {
		this.isEmployeeAccount = isEmployeeAccount;
	}
}
