/**
 * 
 */
package com.dtv.dcp.epoch.common;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;


public class ConstantsTest {
	
	   /**
		 * Private construtor test.
		 *
		 * @throws InstantiationException the instantiation exception
		 * @throws IllegalAccessException the illegal access exception
		 * @throws IllegalArgumentException the illegal argument exception
		 * @throws InvocationTargetException the invocation target exception
		 * @throws NoSuchMethodException the no such method exception
		 * @throws SecurityException the security exception
		 */
		@Test
		public void privateConstrutorTest() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
			Constructor<Constants> cu = Constants.class.getDeclaredConstructor();
			cu.setAccessible(true);
			cu.newInstance();
		}
		
		@Test
	    public void testConstantsAttributes() throws Exception {		   

	        assertNotNull(Constants.DIRECTTV_NOW_ARRAY);
	        assertNotNull(Constants.CATLG_EXT_ERROR_50001);
	    }

}
