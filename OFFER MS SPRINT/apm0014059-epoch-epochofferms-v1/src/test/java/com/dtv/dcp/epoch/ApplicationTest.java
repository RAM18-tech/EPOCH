package com.dtv.dcp.epoch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * The Class ApplicationTest. 
 */
public class ApplicationTest{

	/** The main app. */
	@Mock
	Application mainApp;
	
	/** The builder. */
	@Mock
	private SpringApplicationBuilder builder;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}
	
	/**
	 * Application context loaded.
	 */
	@Test
	public void applicationContextLoaded() {
	}

	/**
	 * Application context test.
	 */
	@Test
	public void applicationContextTest() {
//	    mainApp.main(new String[] {});
	}
}
