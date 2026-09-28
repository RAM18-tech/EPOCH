package com.dtv.dcp.epoch.model.common;

import java.io.Serializable;

public class CampaignEligibility implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8521274846614988529L;
	
	private String purchaseType;
	private String contentCategory;
	private String genre;
	private String rating;
	private String format;
	private String title;

	private Boolean newRelease;
	private String releaseDate;
	
	public String getPurchaseType() {
		return purchaseType;
	}
	public void setPurchaseType(String purchaseType) {
		this.purchaseType = purchaseType;
	}
	public String getContentCategory() {
		return contentCategory;
	}
	public void setContentCategory(String contentCategory) {
		this.contentCategory = contentCategory;
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
	@Override
	public String toString() {
		
		return "CampaignEligibility [purchaseType=" + purchaseType + ", contentCategory=" + contentCategory + ", genre="
				+ genre + ", rating=" + rating + ", format=" + format + ", title=" + title + ", newRelease="
				+ newRelease + ", releaseDate=" + releaseDate + "]";
	}
	
	
}
