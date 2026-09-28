package com.dtv.dcp.epoch.model.common;

import java.io.Serializable;
import java.util.List;

/**
 * The Class EligibilitiesInputBean.
 */
public class EligibilitiesInputBean implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The offer id. */
	private String offerId;
	
	/** The session id. */
	private String sessionId;
	
	/** The qualified ban. */
	private String qualifiedBan;
	
	/** The ban in focus. */
	private String banInFocus;
	
	/** The ban in focus list. */
	private List<String> banInFocusList;
	
	/** The primary CTN. */
	private String primaryCTN;
	
	/** The sdg ids. */
	private String groupId;
	
	/** The is eligibilities available in session. */
	private boolean isEligibilitiesAvailableInSession;
	
	/** The uuid. */
	private String uuid;
	
	/** The wireless accounts. */
	private List<String> wirelessAccounts;
	
	/** The ban account type. */
	private String banAccountType;

	/** The alert. */
	private Alert alert;
	
	/**
	 * Instantiates a new eligibilities input bean.
	 */
	public EligibilitiesInputBean() {
		super();
	}

	/**
	 * Gets the offer id.
	 *
	 * @return the offer id
	 */
	public String getOfferId() {
		return offerId;
	}

	/**
	 * Sets the offer id.
	 *
	 * @param offerId the new offer id
	 */
	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}

	/**
	 * Gets the session id.
	 *
	 * @return the session id
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * Sets the session id.
	 *
	 * @param sessionId the new session id
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * Gets the qualified ban.
	 *
	 * @return the qualified ban
	 */
	public String getQualifiedBan() {
		return qualifiedBan;
	}

	/**
	 * Sets the qualified ban.
	 *
	 * @param qualifiedBan the new qualified ban
	 */
	public void setQualifiedBan(String qualifiedBan) {
		this.qualifiedBan = qualifiedBan;
	}

	/**
	 * Gets the ban in focus.
	 *
	 * @return the ban in focus
	 */
	public String getBanInFocus() {
		return banInFocus;
	}

	/**
	 * Sets the ban in focus.
	 *
	 * @param banInFocus the new ban in focus
	 */
	public void setBanInFocus(String banInFocus) {
		this.banInFocus = banInFocus;
	}

	/**
	 * Gets the ban in focus list.
	 *
	 * @return the ban in focus list
	 */
	public List<String> getBanInFocusList() {
		return banInFocusList;
	}

	/**
	 * Sets the ban in focus list.
	 *
	 * @param banInFocusList the new ban in focus list
	 */
	public void setBanInFocusList(List<String> banInFocusList) {
		this.banInFocusList = banInFocusList;
	}

	/**
	 * Gets the primary CTN.
	 *
	 * @return the primary CTN
	 */
	public String getPrimaryCTN() {
		return primaryCTN;
	}

	/**
	 * Sets the primary CTN.
	 *
	 * @param primaryCTN the new primary CTN
	 */
	public void setPrimaryCTN(String primaryCTN) {
		this.primaryCTN = primaryCTN;
	}

	/**
	 * Gets the group id.
	 *
	 * @return the group id
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * Sets the group id.
	 *
	 * @param groupId the new group id
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * Checks if is eligibilities available in session.
	 *
	 * @return true, if is eligibilities available in session
	 */
	public boolean isEligibilitiesAvailableInSession() {
		return isEligibilitiesAvailableInSession;
	}

	/**
	 * Sets the eligibilities available in session.
	 *
	 * @param isEligibilitiesAvailableInSession the new eligibilities available in session
	 */
	public void setEligibilitiesAvailableInSession(boolean isEligibilitiesAvailableInSession) {
		this.isEligibilitiesAvailableInSession = isEligibilitiesAvailableInSession;
	}

	/**
	 * Gets the uuid.
	 *
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * Sets the uuid.
	 *
	 * @param uuid the new uuid
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * Gets the wireless accounts.
	 *
	 * @return the wireless accounts
	 */
	public List<String> getWirelessAccounts() {
		return wirelessAccounts;
	}

	/**
	 * Sets the wireless accounts.
	 *
	 * @param wirelessAccounts the new wireless accounts
	 */
	public void setWirelessAccounts(List<String> wirelessAccounts) {
		this.wirelessAccounts = wirelessAccounts;
	}

	/**
	 * Gets the ban account type.
	 *
	 * @return the ban account type
	 */
	public String getBanAccountType() {
		return banAccountType;
	}

	/**
	 * Sets the ban account type.
	 *
	 * @param banAccountType the new ban account type
	 */
	public void setBanAccountType(String banAccountType) {
		this.banAccountType = banAccountType;
	}

	/**
	 * Gets the alert.
	 *
	 * @return the alert
	 */
	public Alert getAlert() {
		return alert;
	}

	/**
	 * Sets the alert.
	 *
	 * @param alert the new alert
	 */
	public void setAlert(Alert alert) {
		this.alert = alert;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EligibilitiesInputBean [offerId=");
		builder.append(offerId);
		builder.append(", sessionId=");
		builder.append(sessionId);
		builder.append(", qualifiedBan=");
		builder.append(qualifiedBan);
		builder.append(", banInFocus=");
		builder.append(banInFocus);
		builder.append(", primaryCTN=");
		builder.append(primaryCTN);
		builder.append(", groupId=");
		builder.append(groupId);
		builder.append(", isEligibilitiesAvailableInSession=");
		builder.append(isEligibilitiesAvailableInSession);
		builder.append(", uuid=");
		builder.append(uuid);
		builder.append(", wirelessAccounts=");
		builder.append(wirelessAccounts);
		builder.append(", banAccountType=");
		builder.append(banAccountType);
		builder.append(", alert=");
		builder.append(alert);
		builder.append("]");
		return builder.toString();
	}


}
