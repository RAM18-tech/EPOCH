package com.dtv.dcp.epoch.common;

import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.Test;


public class CpopConstantsTest {

	@Test
	public void testCpopConstantsAttributes() throws Exception {

		CpopConstants cpopConstants = new CpopConstants();

		assertNotNull(cpopConstants.CPOP_ID);

	}

}
