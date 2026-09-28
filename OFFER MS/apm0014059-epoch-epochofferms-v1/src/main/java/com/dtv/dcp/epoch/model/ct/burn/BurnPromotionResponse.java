package com.dtv.dcp.epoch.model.ct.burn;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BurnPromotionResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private String code;
	private BurnResult result;

	public BurnPromotionResponse(String id, String code, BurnResult result) {
		this.id = id;
		this.code = code;
		this.result = result;
	}

	public BurnPromotionResponse() {
		// Intentionally Left Blank For Jackson
	}

	@Override
	public String toString() {
		return "BurnPromotionResponse{" + "id='" + id + '\'' + ", code='" + code + '\'' + ", result=" + result + '}';
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public BurnResult getResult() {
		return result;
	}

	public void setResult(BurnResult result) {
		this.result = result;
	}
}