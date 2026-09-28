package com.dtv.dcp.epoch.model.common.response;

import java.io.Serializable;

import com.dtv.dcp.epoch.model.ct.coupon.Content;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *This class model class is for response structure for bundle offer
 *
 */

/**
 * Created by nk3077 on 05/08/2018.
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CouponValidationResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** The content. */
	@JsonProperty(value = "content", required = false)
	private Content content;

	/**
	 * @return
	 */
	public Content getContent() {
		return content;
	}

	/**
	 * @param content
	 */
	public void setContent(Content content) {
		this.content = content;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OfferBurnResponse [content=");
		builder.append(content);
		builder.append("]");
		return builder.toString();
	}

}