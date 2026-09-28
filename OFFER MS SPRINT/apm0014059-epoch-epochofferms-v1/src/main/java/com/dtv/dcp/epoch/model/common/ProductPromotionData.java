package com.dtv.dcp.epoch.model.common;

public class ProductPromotionData {

	/** The dependentPromoCode. */
	private String dependentPromoCode;
	
	/** The dependentBillerPromoId. */
	private String dependentBillerPromoId;
	
	/** The dependentPromoStartDate. */
	private String dependentPromoStartDate;
	
	/** The dependentPromoName  . */
	private String dependentPromoName  ;
	
	/** The dependentPromoDescription. */
	private String dependentPromoDescription;
	
	/** The dependentPromoDisplayName. */
	private String dependentPromoDisplayName;
	
	/** The dependentPromoIsFreeTrail . */
	private boolean dependentPromoIsFreeTrail ;

	private  String promotionCategory;
	
	/**
	 * @return the dependentPromoCode
	 */
	public String getDependentPromoCode() {
		return dependentPromoCode;
	}

	/**
	 * @param dependentPromoCode the dependentPromoCode to set
	 */
	public void setDependentPromoCode(String dependentPromoCode) {
		this.dependentPromoCode = dependentPromoCode;
	}

	/**
	 * @return the dependentBillerPromoId
	 */
	public String getDependentBillerPromoId() {
		return dependentBillerPromoId;
	}

	/**
	 * @param dependentBillerPromoId the dependentBillerPromoId to set
	 */
	public void setDependentBillerPromoId(String dependentBillerPromoId) {
		this.dependentBillerPromoId = dependentBillerPromoId;
	}

	/**
	 * @return the dependentPromoStartDate
	 */
	public String getDependentPromoStartDate() {
		return dependentPromoStartDate;
	}

	/**
	 * @param dependentPromoStartDate the dependentPromoStartDate to set
	 */
	public void setDependentPromoStartDate(String dependentPromoStartDate) {
		this.dependentPromoStartDate = dependentPromoStartDate;
	}

	/**
	 * @return the dependentPromoName
	 */
	public String getDependentPromoName() {
		return dependentPromoName;
	}

	/**
	 * @param dependentPromoName the dependentPromoName to set
	 */
	public void setDependentPromoName(String dependentPromoName) {
		this.dependentPromoName = dependentPromoName;
	}

	/**
	 * @return the dependentPromoDescription
	 */
	public String getDependentPromoDescription() {
		return dependentPromoDescription;
	}

	/**
	 * @param dependentPromoDescription the dependentPromoDescription to set
	 */
	public void setDependentPromoDescription(String dependentPromoDescription) {
		this.dependentPromoDescription = dependentPromoDescription;
	}

	/**
	 * @return the dependentPromoDisplayName
	 */
	public String getDependentPromoDisplayName() {
		return dependentPromoDisplayName;
	}

	/**
	 * @param dependentPromoDisplayName the dependentPromoDisplayName to set
	 */
	public void setDependentPromoDisplayName(String dependentPromoDisplayName) {
		this.dependentPromoDisplayName = dependentPromoDisplayName;
	}

	/**
	 * @return the dependentPromoIsFreeTrail
	 */
	public boolean isDependentPromoIsFreeTrail() {
		return dependentPromoIsFreeTrail;
	}

	/**
	 * @param dependentPromoIsFreeTrail the dependentPromoIsFreeTrail to set
	 */
	public void setDependentPromoIsFreeTrail(boolean dependentPromoIsFreeTrail) {
		this.dependentPromoIsFreeTrail = dependentPromoIsFreeTrail;
	}

	public String getPromotionCategory() {
		
		return promotionCategory;
	}

	/**
	 * @param promotionCategory the promotionCategory to set
	 */
	public void setPromotionCategory(String promotionCategory) {
		this.promotionCategory = promotionCategory;
	}  

}
