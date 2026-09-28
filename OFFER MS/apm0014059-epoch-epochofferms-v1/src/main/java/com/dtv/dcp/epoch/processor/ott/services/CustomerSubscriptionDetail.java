package com.dtv.dcp.epoch.processor.ott.services;

import java.util.Date;
import java.util.List;

import com.dtv.dcp.epoch.processor.ott.services.model.CustomerServiceDetail;

public class CustomerSubscriptionDetail {	
	
	private Date promoEndDate; 
	private Date nextBillingDate;
	private String userType;
	private String agentType;
	private String contractIndicator;
	private String freeTrialEligble;
	private String isDefaultPromo;	
	
	private List<String> basePackageServiceIdList;
	
	private List<String> addOnServiceIdList;
	
	private List<String> deviceServiceIdList;
	
	private List<CustomerServiceDetail> listBaseBackageServiceDetail;
	
	private List<CustomerServiceDetail> listAddOnServiceDetail;
	
	private List<CustomerServiceDetail> listDeviceServiceDetail;

	public String videoPlanPackageCode;

	public String getVideoPlanPackageCode() {
		return videoPlanPackageCode;
	}

	public void setVideoPlanPackageCode(String videoPlanPackageCode) {
		this.videoPlanPackageCode = videoPlanPackageCode;
	}

	public String videoPlanProductStatus;

	public String getVideoPlanProductStatus() {
		return videoPlanProductStatus;
	}

	public void setVideoPlanProductStatus(String videoPlanProductStatus) {
		this.videoPlanProductStatus = videoPlanProductStatus;
	}
	private String accountType;
	
	public String getUserType() {
		return userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}
	public String getAgentType() {
		return agentType;
	}
	public void setAgentType(String agentType) {
		this.agentType = agentType;
	}
	public String getContractIndicator() {
		return contractIndicator;
	}
	public void setContractIndicator(String contractIndicator) {
		this.contractIndicator = contractIndicator;
	}
	     
	public Date getNextBillingDate() {
		return nextBillingDate;
	}
	public void setNextBillingDate(Date nextBillingDate) {
		this.nextBillingDate = nextBillingDate;
	}

	public Date getPromoEndDate() {
		return promoEndDate;
	}
	public void setPromoEndDate(Date promoEndDate) {
		this.promoEndDate = promoEndDate;
	}

	/**
	 * @return the accountType
	 */
	public String getAccountType() {
		return accountType;
	}
	/**
	 * @param accountType the accountType to set
	 */
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
	public boolean isAgent() {
		return false;
	}
	/**
	 * @return the listBaseBackageServiceDetail
	 */
	public List<CustomerServiceDetail> getListBaseBackageServiceDetail() {
		return listBaseBackageServiceDetail;
	}
	/**
	 * @param listBaseBackageServiceDetail the listBaseBackageServiceDetail to set
	 */
	public void setListBaseBackageServiceDetail(List<CustomerServiceDetail> listBaseBackageServiceDetail) {
		this.listBaseBackageServiceDetail = listBaseBackageServiceDetail;
	}
	/**
	 * @return the listAddOnServiceDetail
	 */
	public List<CustomerServiceDetail> getListAddOnServiceDetail() {
		return listAddOnServiceDetail;
	}
	/**
	 * @param listAddOnServiceDetail the listAddOnServiceDetail to set
	 */
	public void setListAddOnServiceDetail(List<CustomerServiceDetail> listAddOnServiceDetail) {
		this.listAddOnServiceDetail = listAddOnServiceDetail;
	}
	/**
	 * @return the basePackageServiceIdList
	 */
	public List<String> getBasePackageServiceIdList() {
		return basePackageServiceIdList;
	}
	/**
	 * @param basePackageServiceIdList the basePackageServiceIdList to set
	 */
	public void setBasePackageServiceIdList(List<String> basePackageServiceIdList) {
		this.basePackageServiceIdList = basePackageServiceIdList;
	}
	/**
	 * @return the addOnServiceIdList
	 */
	public List<String> getAddOnServiceIdList() {
		return addOnServiceIdList;
	}
	/**
	 * @param addOnServiceIdList the addOnServiceIdList to set
	 */
	public void setAddOnServiceIdList(List<String> addOnServiceIdList) {
		this.addOnServiceIdList = addOnServiceIdList;
	}
	/**
	 * @return the freeTrialEligble
	 */
	public String getFreeTrialEligble() {
		return freeTrialEligble;
	}
	/**
	 * @param freeTrialEligble the freeTrialEligble to set
	 */
	public void setFreeTrialEligble(String freeTrialEligble) {
		this.freeTrialEligble = freeTrialEligble;
	}	
	

	/**
	 * @return the isDefaultPromo
	 */
	public String getIsDefaultPromo() {
		return isDefaultPromo;
	}
	/**
	 * @param isDefaultPromo the isDefaultPromo to set
	 */
	public void setIsDefaultPromo(String isDefaultPromo) {
		this.isDefaultPromo = isDefaultPromo;
	}
	public List<String> getDeviceServiceIdList() {
		return deviceServiceIdList;
	}
	public void setDeviceServiceIdList(List<String> deviceServiceIdList) {
		this.deviceServiceIdList = deviceServiceIdList;
	}
	public List<CustomerServiceDetail> getListDeviceServiceDetail() {
		return listDeviceServiceDetail;
	}
	public void setListDeviceServiceDetail(List<CustomerServiceDetail> listDeviceServiceDetail) {
		this.listDeviceServiceDetail = listDeviceServiceDetail;
	}
	
}
