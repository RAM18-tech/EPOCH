package com.dtv.dcp.epoch.exception;

/**
 * The Class ClientException.
 */
public class ClientException extends CatalogException{

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -7198333146940410445L;
	
	/** The error id. */
	private String errorId;  
	
	/** The message. */
	private String message;
	
	/** The http code. */
	private int httpCode;
	
	/**
	 * Instantiates a new client exception.
	 *
	 * @param errorId the error id
	 * @param message the message
	 * @param httpCode the http code
	 */
	public ClientException(String errorId, String message, int httpCode) {
		super();
		this.errorId = errorId;
		this.message = message;
		this.httpCode = httpCode;
	}

	/**
	 * Gets the error id.
	 *
	 * @return the error id
	 */
	public String getErrorId() {
		return errorId;
	}
	
	/**
	 * Sets the error id.
	 *
	 * @param errorId the new error id
	 */
	public void setErrorId(String errorId) {
		this.errorId = errorId;
	}
	
	/**
	 * Gets the http code.
	 *
	 * @return the http code
	 */
	public int getHttpCode() {
		return httpCode;
	}
	
	/**
	 * Sets the http code.
	 *
	 * @param httpCode the new http code
	 */
	public void setHttpCode(int httpCode) {
		this.httpCode = httpCode;
	}

	/* (non-Javadoc)
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		return message;
	}
	
	/**
	 * Sets the message.
	 *
	 * @param message the new message
	 */
	public void setMessage(String message) {
		this.message = message;
	}
}
