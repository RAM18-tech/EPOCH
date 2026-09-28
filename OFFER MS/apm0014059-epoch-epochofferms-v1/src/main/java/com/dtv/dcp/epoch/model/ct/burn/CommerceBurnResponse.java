package com.dtv.dcp.epoch.model.ct.burn;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommerceBurnResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String content;
    private CommerceToolError error;

    public CommerceBurnResponse(String content, CommerceToolError error) {
        this.content = content;
        this.error = error;
    }

    public CommerceBurnResponse() {
        // Intentionally Left Blank For Jackson
    }

    @Override
    public String toString() {
        return "CommerceBurnResponse{" +
                "content='" + content + '\'' +
                ", error=" + error +
                '}';
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public CommerceToolError getError() {
        return error;
    }

    public void setError(CommerceToolError error) {
        this.error = error;
    }
}
