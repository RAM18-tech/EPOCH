package com.dtv.dcp.epoch.model.common.request;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.model.ct.coupon.PurchaseDetails;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OfferRequestWrapper {


    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The logged In Indicator.
     */
    private String loggedInId;

    /**
     * The direc TV now.
     */
    private OfferRequest offerRequest;

    /**
     * The linked Wireless Account.
     */
    private String authAccountsWireless;

    /**
     * The Dtvn Account.
     */
    private String dtvnAccount;

    private String flow;

    /**
     * The idpctx session Id.
     */
    private String sessionId;

    /**
     * The linked UverseAccountNums.
     */
    private String linkedUverseAccountNums;

    /**
     * The iptvMigration.
     */
    private boolean iptvMigration;

    /**
     * The iptvMigrationIntent.
     */
    private boolean iptvMigrationIntent;

    /**
     * The promotional offers.
     */
    private String promotionalOffers;

    /**
     * The channel.
     */
    private String channel;

    private boolean isMobility;

    private boolean isPartner;
    
    private boolean isDirectvOnline;
    private boolean isChannelEligiblity;

    public boolean isDirectvOnline() {
		return isDirectvOnline;
	}

	public void setDirectvOnline(boolean isDirectvOnline) {
		this.isDirectvOnline = isDirectvOnline;
	}

    private CTOfferRequest ctOfferRequest;
    
    Map<String, PurchaseDetails>  purchaseDetailsList;

    private String partnerType;

    private String currentDate;

    private Map<String, List<String>> slsCombinationMap;

    public String getPartnerType() {
		return partnerType;
	}

	public void setPartnerType(String partnerType) {
		this.partnerType = partnerType;
	}

    public String getFlow() {
        return flow;
    }

    public void setFlow(String flow) {
        this.flow = flow;
    }

    /**
     * The isEmployeeAccount.
     */
    private boolean isEmployeeAccount;


    public Map<String, PurchaseDetails> getPurchaseDetailsList() {
		return purchaseDetailsList;
	}

	public void setPurchaseDetailsList(Map<String, PurchaseDetails> purchaseDetailsList) {
		this.purchaseDetailsList = purchaseDetailsList;
	}

	/**
     * The agentType.
     */
    private String agentType;
    
    private boolean cartModeMobility;

    /**
	 * @return the cartModeMobility
	 */
	public boolean isCartModeMobility() {
		return cartModeMobility;
	}

	/**
	 * @param cartModeMobility the cartModeMobility to set
	 */
	public void setCartModeMobility(boolean cartModeMobility) {
		this.cartModeMobility = cartModeMobility;
	}

	public String getAgentType() {
        return agentType;
    }

    public void setAgentType(String agentType) {
        this.agentType = agentType;
    }

    public String getLoggedInId() {
        return loggedInId;
    }

    public void setLoggedInId(String loggedInId) {
        this.loggedInId = loggedInId;
    }

    public OfferRequest getOfferRequest() {
        return offerRequest;
    }

    public void setOfferRequest(OfferRequest offerRequest) {
        this.offerRequest = offerRequest;
    }

    public String getAuthAccountsWireless() {
        return authAccountsWireless;
    }

    public void setAuthAccountsWireless(String authAccountsWireless) {
        this.authAccountsWireless = authAccountsWireless;
    }

    public String getDtvnAccount() {
        return dtvnAccount;
    }

    public void setDtvnAccount(String dtvnAccount) {
        this.dtvnAccount = dtvnAccount;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getLinkedUverseAccountNums() {
        return linkedUverseAccountNums;
    }

    public void setLinkedUverseAccountNums(String linkedUverseAccountNums) {
        this.linkedUverseAccountNums = linkedUverseAccountNums;
    }

    public boolean isIptvMigration() {
        return iptvMigration;
    }

    public void setIptvMigration(boolean iptvMigration) {
        this.iptvMigration = iptvMigration;
    }

    public boolean isIptvMigrationIntent() {
        return iptvMigrationIntent;
    }

    public void setIptvMigrationIntent(boolean iptvMigrationIntent) {
        this.iptvMigrationIntent = iptvMigrationIntent;
    }

    public String getPromotionalOffers() {
        return promotionalOffers;
    }

    public void setPromotionalOffers(String promotionalOffers) {
        this.promotionalOffers = promotionalOffers;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public boolean isMobility() {
        return isMobility;
    }

    public void setMobility(boolean mobility) {
        isMobility = mobility;
    }

    public CTOfferRequest getCtOfferRequest() {
        return ctOfferRequest;
    }

    public void setCtOfferRequest(CTOfferRequest ctOfferRequest) {
        this.ctOfferRequest = ctOfferRequest;
    }

    public boolean isPartner() {
        return isPartner;
    }

    public void setPartner(boolean partner) {
        isPartner = partner;
    }

    public String getCurrentDate() {return currentDate;}

    public void setCurrentDate( String currentDate) {this.currentDate = currentDate;}

	/**
	 * @return the isEmployeeAccount
	 */
	public boolean isEmployeeAccount() {
		if(Optional.ofNullable(this.offerRequest).isPresent() 
				&& Optional.ofNullable(this.offerRequest.getCustomerSegments()).isPresent()
				&& this.offerRequest.getCustomerSegments().contains(Constants.EMPLOYEE)) {
			return true;
		} else {
		return isEmployeeAccount;
		}
	}

	/**
	 * @param isEmployeeAccount the isEmployeeAccount to set
	 */
	public void setEmployeeAccount(boolean isEmployeeAccount) {
			this.isEmployeeAccount = isEmployeeAccount;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OfferRequestWrapper [loggedInId=");
		builder.append(loggedInId);
		builder.append(", offerRequest=");
		builder.append(offerRequest);
		builder.append(", authAccountsWireless=");
		builder.append(authAccountsWireless);
		builder.append(", dtvnAccount=");
		builder.append(dtvnAccount);
		builder.append(", sessionId=");
		builder.append(sessionId);
		builder.append(", linkedUverseAccountNums=");
		builder.append(linkedUverseAccountNums);
		builder.append(", iptvMigration=");
		builder.append(iptvMigration);
		builder.append(", iptvMigrationIntent=");
		builder.append(iptvMigrationIntent);
		builder.append(", promotionalOffers=");
		builder.append(promotionalOffers);
		builder.append(", channel=");
		builder.append(channel);
		builder.append(", isMobility=");
		builder.append(isMobility);
		builder.append(", isPartner=");
		builder.append(isPartner);
		builder.append(", isDirectvOnline=");
		builder.append(isDirectvOnline);
		builder.append(", ctOfferRequest=");
		builder.append(ctOfferRequest);
		builder.append(", purchaseDetailsList=");
		builder.append(purchaseDetailsList);
		builder.append(", partnerType=");
		builder.append(partnerType);
		builder.append(", isEmployeeAccount=");
		builder.append(isEmployeeAccount);
		builder.append(", agentType=");
		builder.append(agentType);
		builder.append(", cartModeMobility=");
		builder.append(cartModeMobility);
		builder.append("]");
		return builder.toString();
	}

	public boolean isChannelEligiblity() {
		return isChannelEligiblity;
	}

	public void setChannelEligiblity(boolean isChannelEligiblity) {
		this.isChannelEligiblity = isChannelEligiblity;
	}

    private boolean isNoDeviceFrameworkEnabled;

    public boolean isNoDeviceFrameworkEnabled() {
        return isNoDeviceFrameworkEnabled;
    }

    public void setNoDeviceFrameworkEnabled(boolean noDeviceFrameworkEnabled) {
        isNoDeviceFrameworkEnabled = noDeviceFrameworkEnabled;
    }

    private Map<String, String> swimlaneSubscriptionTypeMap;

    public Map<String, String> getSwimlaneSubscriptionTypeMap() {
        return swimlaneSubscriptionTypeMap;
    }

    public void setSwimlaneSubscriptionTypeMap(Map<String, String> swimlaneSubscriptionTypeMap) {
        this.swimlaneSubscriptionTypeMap = swimlaneSubscriptionTypeMap;
    }

	public Map<String, List<String>> getSlsCombinationMap() {
		return slsCombinationMap;
	}

	public void setSlsCombinationMap(Map<String, List<String>> slsCombinationMap) {
		this.slsCombinationMap = slsCombinationMap;
	}

}
