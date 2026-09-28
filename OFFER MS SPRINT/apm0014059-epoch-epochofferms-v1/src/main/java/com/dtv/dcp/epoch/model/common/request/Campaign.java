package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.ct.generic.GenericLocaleBase;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Campaign implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;

	private String code;

	private GenericLocaleBase name;

	private GenericLocaleBase description;

	private String status;

	private String version;

	private String createdAt;

	private String lastModifiedAt;

	private List<String> categories;

	private List<Variant> variants;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code
	 *            the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return the name
	 */
	public GenericLocaleBase getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(GenericLocaleBase name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public GenericLocaleBase getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(GenericLocaleBase description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the categories
	 */
	public List<String> getCategories() {
		return categories;
	}

	/**
	 * @param categories
	 *            the categories to set
	 */
	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

	/**
	 * @return the variants
	 */
	public List<Variant> getVariants() {
		return variants;
	}

	/**
	 * @param variants
	 *            the variants to set
	 */
	public void setVariants(List<Variant> variants) {
		this.variants = variants;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getLastModifiedAt() {
		return lastModifiedAt;
	}

	public void setLastModifiedAt(String lastModifiedAt) {
		this.lastModifiedAt = lastModifiedAt;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Campaign [id=");
		builder.append(id);
		builder.append(", code=");
		builder.append(code);
		builder.append(", name=");
		builder.append(name);
		builder.append(", description=");
		builder.append(description);
		builder.append(", status=");
		builder.append(status);
		builder.append(", version=");
		builder.append(version);
		builder.append(", createdAt=");
		builder.append(createdAt);
		builder.append(", lastModifiedAt=");
		builder.append(lastModifiedAt);
		builder.append(", categories=");
		builder.append(categories);
		builder.append(", variants=");
		builder.append(variants);
		builder.append("]");
		return builder.toString();
	}

}
