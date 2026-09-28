package com.dtv.dcp.epoch.model.common;


import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class Context.
 * @author dr000y
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class PurchaseContext implements Serializable {
	
   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/*
   * Purchase Type
   */
    @JsonProperty("purchaseType")
    private String purchaseType;
    
    /*
     * Purchase Amount
     */
    @JsonProperty("purchaseAmount")
    private String purchaseAmount;
    
    /*
     * Content Category
     */
    @JsonProperty("contentCategory")
    private String contentCategory;
    
    /*
     * Format
     */
    @JsonProperty("format")
    private String format;
    
    /*
     * Title
     */
    @JsonProperty("title")
    private String title;
   
    /*
     * Genre
     */
	@JsonProperty("genre")
    private String genre;
    
	/*
     * Rating
     */
	@JsonProperty("rating")
    private String rating;
	
	/*
     * Studio
     */
    @JsonProperty("studio")
    private String studio;
    
    /*
     * New release
     */
    @JsonProperty("newRelease")
    private Boolean newRelease;
    
    /*
     * Release Date
     */
    @JsonProperty("releaseDate")
    private String releaseDate;
    
    /*
     * TMS Program ID
     */
    @JsonProperty("tmsProgramID")
    private String tmsProgramID;

	/*
	 * Event Code
	 */
	@JsonProperty("eventCode")
	private String eventCode;


   
	public String getPurchaseType() {
		return purchaseType;
	}



	public void setPurchaseType(String purchaseType) {
		this.purchaseType = purchaseType;
	}



	public String getPurchaseAmount() {
		return purchaseAmount;
	}



	public void setPurchaseAmount(String purchaseAmount) {
		this.purchaseAmount = purchaseAmount;
	}



	public String getContentCategory() {
		return contentCategory;
	}



	public void setContentCategory(String contentCategory) {
		this.contentCategory = contentCategory;
	}



	public String getFormat() {
		return format;
	}



	public void setFormat(String format) {
		this.format = format;
	}



	public String getTitle() {
		return title;
	}



	public void setTitle(String title) {
		this.title = title;
	}



	public String getGenre() {
		return genre;
	}



	public void setGenre(String genre) {
		this.genre = genre;
	}



	public String getRating() {
		return rating;
	}



	public void setRating(String rating) {
		this.rating = rating;
	}



	public String getStudio() {
		return studio;
	}



	public void setStudio(String studio) {
		this.studio = studio;
	}



	public Boolean getNewRelease() {
		return newRelease;
	}



	public void setNewRelease(Boolean newRelease) {
		this.newRelease = newRelease;
	}



	public String getReleaseDate() {
		return releaseDate;
	}



	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}



	public String getTmsProgramID() {
		return tmsProgramID;
	}



	public void setTmsProgramID(String tmsProgramID) {
		this.tmsProgramID = tmsProgramID;
	}

	public String getEventCode() {	return eventCode; }

	public void setEventCode(String eventCode) { this.eventCode = eventCode; }

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Context [purchaseType=");
		builder.append(purchaseType);
		builder.append(" ,purchaseAmount=");
		builder.append(purchaseAmount);
		builder.append(" ,contentCategory=");
		builder.append(contentCategory);
		builder.append(" ,format=");
		builder.append(format);
		builder.append(" ,title=");
		builder.append(title);
		builder.append(" ,rating=");
		builder.append(rating);
		builder.append(" ,studio=");
		builder.append(studio);
		builder.append(" ,newRelease=");
		builder.append(newRelease);
		builder.append(" ,releaseDate=");
		builder.append(releaseDate);
		builder.append(" ,tmsProgramID=");
		builder.append(tmsProgramID);
		builder.append(" ,eventCode=");
		builder.append(eventCode);
		builder.append("]");
		return builder.toString();
	}

}
