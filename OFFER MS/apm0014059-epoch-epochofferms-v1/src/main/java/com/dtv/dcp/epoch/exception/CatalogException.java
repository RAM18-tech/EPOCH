/**
 * 
 */
package com.dtv.dcp.epoch.exception;

/**
 * The Class CatalogException.
 *
 * @author sm907k
 */
public class CatalogException extends Exception{
	
	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;
	/** The source. */
	private String source;
	
	/**
	 * Gets the source.
	 *
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Sets the source.
	 *
	 * @param source the new source
	 */
	public void setSource(String source) {
		this.source = source;
	}
}
