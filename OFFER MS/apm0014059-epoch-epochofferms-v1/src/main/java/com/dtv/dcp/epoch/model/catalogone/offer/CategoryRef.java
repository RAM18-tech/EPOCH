package com.dtv.dcp.epoch.model.catalogone.offer;

import java.io.Serializable;

import com.dtv.dcp.epoch.model.ct.generic.GenericLocaleBase;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author dr000y
 *
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryRef implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String id;

	private GenericLocaleBase name;

	private String code;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public GenericLocaleBase getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(GenericLocaleBase name) {
		this.name = name;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return "CategoryRef [id=" + id + ", name=" + name + ", code=" + code + "]";
	}

}
