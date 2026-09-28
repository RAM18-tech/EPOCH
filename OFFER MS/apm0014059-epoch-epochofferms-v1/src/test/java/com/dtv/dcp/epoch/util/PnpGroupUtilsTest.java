/**
 * 
 */
package com.dtv.dcp.epoch.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit4.SpringRunner;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.model.ct.request.PriceProtection;;

public class PnpGroupUtilsTest {

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}
	
	@InjectMocks
	PnpGroupUtils pnpGroupUtils;
	
	@Mock
	RedisCacheHelper redisCacheHelper;
	
	/**
	 * Scenario GF– Sign up in 2023 before 2023 PI. 
	 */
	@Test
	public void testPnpGroupGrandFatheredScenario() {
		PriceProtection priceProtection = new PriceProtection();
		priceProtection.setStartDate("08/07/2023");
		priceProtection.setEndDate("11/06/2026");
		
		List<String> pnpGrandFatherConfigValue = Arrays.asList("11/04/2023");
		List<String> pnpStartYearConfigValue = Arrays.asList("11/04/2025");
		
		Mockito.when(redisCacheHelper.getValues(anyString(), anyString())).thenReturn(pnpGrandFatherConfigValue).thenReturn(pnpStartYearConfigValue);
		
		String pnpGroup = pnpGroupUtils.getPnpGroup(priceProtection,"08/07/2023",Constants.OTT);
		
		assertNotNull(pnpGroup);
		assertEquals("PP-TAZCONTRACT-GRANDFATHERED", pnpGroup);
	}
	
	/**
	 * Scenario 1 – Sign up in 2023 after 2023 PI. 
	 */
	@Test
	public void testPnpGroupScenario1() {
		PriceProtection priceProtection = new PriceProtection();
		priceProtection.setStartDate("11/07/2023");
		priceProtection.setEndDate("11/06/2026");
		
		List<String> pnpGrandFatherConfigValue = Arrays.asList("11/04/2023");
		List<String> pnpStartYearConfigValue = Arrays.asList("11/05/2023");
		
		Mockito.when(redisCacheHelper.getValues(anyString(), anyString())).thenReturn(pnpGrandFatherConfigValue).thenReturn(pnpStartYearConfigValue);
		
		String pnpGroup = pnpGroupUtils.getPnpGroup(priceProtection,"11/07/2023",Constants.OTT);
		
		assertNotNull(pnpGroup);
		assertEquals("2023-PNP-1", pnpGroup);
	}
	
	/**
	 * Scenario 2 – Sign up in 2024 before 2024 first PI. 
	 */
	@Test
	public void testPnpGroupScenario2() {
		PriceProtection priceProtection = new PriceProtection();
		priceProtection.setStartDate("01/07/2024");
		priceProtection.setEndDate("11/06/2026");
		
		List<String> pnpGrandFatherConfigValue = Arrays.asList("11/04/2023");
		List<String> pnpStartYearConfigValue = Arrays.asList("02/01/2024|06/01/2024|11/05/2024");
		
		List<String> pnpConfigValue2 = Arrays.asList("11/05/2023");
		
		Mockito.when(redisCacheHelper.getValues(anyString(), anyString())).thenReturn(pnpGrandFatherConfigValue).thenReturn(pnpStartYearConfigValue).thenReturn(pnpConfigValue2);
		
		String pnpGroup = pnpGroupUtils.getPnpGroup(priceProtection,"01/07/2024",Constants.OTT);
		
		assertNotNull(pnpGroup);
		assertEquals("2023-PNP-1", pnpGroup);
	}
	
	/**
	 * Scenario 3 – Sign up in 2024 between 1st PI and 2nd PI. 
	 */
	@Test
	public void testPnpGroupScenario3() {
		PriceProtection priceProtection = new PriceProtection();
		priceProtection.setStartDate("05/07/2024");
		priceProtection.setEndDate("11/06/2026");
		
		List<String> pnpGrandFatherConfigValue = Arrays.asList("11/04/2023");
		List<String> pnpStartYearConfigValue = Arrays.asList("02/01/2024|06/01/2024|11/05/2024");
		
		List<String> pnpConfigValue2 = Arrays.asList("11/05/2023");
		
		Mockito.when(redisCacheHelper.getValues(anyString(), anyString())).thenReturn(pnpGrandFatherConfigValue).thenReturn(pnpStartYearConfigValue).thenReturn(pnpConfigValue2);
		
		String pnpGroup = pnpGroupUtils.getPnpGroup(priceProtection,"05/07/2024",Constants.OTT);
		
		assertNotNull(pnpGroup);
		assertEquals("2024-PNP-1", pnpGroup);
	}
	
	/**
	 * Scenario 3.1 – Sign up in 2024 before 2024 between 2nd PI and 3rd PI. 
	 */
	@Test
	public void testPnpGroupScenario3dot1() {
		PriceProtection priceProtection = new PriceProtection();
		priceProtection.setStartDate("07/07/2024");
		priceProtection.setEndDate("11/06/2026");
		
		List<String> pnpGrandFatherConfigValue = Arrays.asList("11/04/2023");
		List<String> pnpStartYearConfigValue = Arrays.asList("02/01/2024|06/01/2024|11/05/2024");
		
		List<String> pnpConfigValue2 = Arrays.asList("11/05/2023");
		
		Mockito.when(redisCacheHelper.getValues(anyString(), anyString())).thenReturn(pnpGrandFatherConfigValue).thenReturn(pnpStartYearConfigValue).thenReturn(pnpConfigValue2);
		
		String pnpGroup = pnpGroupUtils.getPnpGroup(priceProtection,"07/07/2024",Constants.OTT);
		
		assertNotNull(pnpGroup);
		assertEquals("2024-PNP-2", pnpGroup);
	}
	
	/**
	 * Scenario 3.2 – Sign up in 2024 after 3rd PI. 
	 */
	@Test
	public void testPnpGroupScenario3dot2() {
		PriceProtection priceProtection = new PriceProtection();
		priceProtection.setStartDate("11/07/2024");
		priceProtection.setEndDate("11/06/2026");
		
		List<String> pnpGrandFatherConfigValue = Arrays.asList("11/04/2023");
		List<String> pnpStartYearConfigValue = Arrays.asList("02/01/2024|06/01/2024|11/05/2024");
		
		List<String> pnpConfigValue2 = Arrays.asList("11/05/2023");
		
		Mockito.when(redisCacheHelper.getValues(anyString(), anyString())).thenReturn(pnpGrandFatherConfigValue).thenReturn(pnpStartYearConfigValue).thenReturn(pnpConfigValue2);
		
		String pnpGroup = pnpGroupUtils.getPnpGroup(priceProtection,"11/07/2024",Constants.OTT);
		
		assertNotNull(pnpGroup);
		assertEquals("2024-PNP-3", pnpGroup);
	}
	
	/**
	 * Scenario 4 – – Sign up in 2024 with no PI in 2024.  (use same date as 2023 PI)
	 */
	@Test
	public void testPnpGroupScenario4() {
		PriceProtection priceProtection = new PriceProtection();
		priceProtection.setStartDate("07/07/2024");
		priceProtection.setEndDate("11/06/2026");
		
		List<String> pnpGrandFatherConfigValue = Arrays.asList("11/04/2023");
		//(use same date as 2023 PI)
		List<String> pnpStartYearConfigValue = Arrays.asList("11/05/2023");
		
		List<String> pnpConfigValue2 = Arrays.asList("11/05/2023");
		
		Mockito.when(redisCacheHelper.getValues(anyString(), anyString())).thenReturn(pnpGrandFatherConfigValue).thenReturn(pnpStartYearConfigValue).thenReturn(pnpConfigValue2);
		
		String pnpGroup = pnpGroupUtils.getPnpGroup(priceProtection,"07/07/2024",Constants.OTT);
		
		assertNotNull(pnpGroup);
		assertEquals("2024-PNP-1", pnpGroup);
	}
	
	/**
	 * Scenario 5 – Sign up in 2025 before 2025 PI. Also, no PI in 2024
	 */
	@Test
	public void testPnpGroupScenario5() {
		PriceProtection priceProtection = new PriceProtection();
		priceProtection.setStartDate("04/07/2025");
		priceProtection.setEndDate("11/06/2026");
		
		List<String> pnpGrandFatherConfigValue = Arrays.asList("11/04/2023");
		List<String> pnpStartYearConfigValue = Arrays.asList("11/05/2025");
		
		//(use same date as 2023 PI)
		List<String> pnpConfigValue2 = Arrays.asList("11/05/2023");
		
		Mockito.when(redisCacheHelper.getValues(anyString(), anyString())).thenReturn(pnpGrandFatherConfigValue).thenReturn(pnpStartYearConfigValue).thenReturn(pnpConfigValue2);
		
		String pnpGroup = pnpGroupUtils.getPnpGroup(priceProtection,"04/07/2025",Constants.OTT);
		
		assertNotNull(pnpGroup);
		assertEquals("2024-PNP-1", pnpGroup);
	}
	
	/**
	 * Scenario 6 – Sign up in 2024 between 1st PI and 2nd PI. 
	 * Also startDate is same as first date of the dates in PI
	 */
	@Test
	public void testPnpGroupScenario6() {
		PriceProtection priceProtection = new PriceProtection();
		priceProtection.setStartDate("02/01/2024");
		priceProtection.setEndDate("11/06/2026");
		
		List<String> pnpGrandFatherConfigValue = Arrays.asList("11/04/2023");
		List<String> pnpStartYearConfigValue = Arrays.asList("02/01/2024|06/01/2024|11/05/2024");
		
		List<String> pnpConfigValue2 = Arrays.asList("11/05/2023");
		
		Mockito.when(redisCacheHelper.getValues(anyString(), anyString())).thenReturn(pnpGrandFatherConfigValue).thenReturn(pnpStartYearConfigValue).thenReturn(pnpConfigValue2);
		
		String pnpGroup = pnpGroupUtils.getPnpGroup(priceProtection,"02/01/2024",Constants.OTT);
		
		assertNotNull(pnpGroup);
		assertEquals("2024-PNP-1", pnpGroup);
	}
	
	/**
	 * Scenario 7 – Sign up in 2024 between 1st PI and 2nd PI. 
	 * Also startDate is same as second date of the dates in PI
	 */
	@Test
	public void testPnpGroupScenario7() {
		PriceProtection priceProtection = new PriceProtection();
		priceProtection.setStartDate("06/01/2024");
		priceProtection.setEndDate("11/06/2026");
		
		List<String> pnpGrandFatherConfigValue = Arrays.asList("11/04/2023");
		List<String> pnpStartYearConfigValue = Arrays.asList("02/01/2024|06/01/2024|11/05/2024");
		
		List<String> pnpConfigValue2 = Arrays.asList("11/05/2023");
		
		Mockito.when(redisCacheHelper.getValues(anyString(), anyString())).thenReturn(pnpGrandFatherConfigValue).thenReturn(pnpStartYearConfigValue).thenReturn(pnpConfigValue2);
		
		String pnpGroup = pnpGroupUtils.getPnpGroup(priceProtection,"06/01/2024",Constants.OTT);
		
		assertNotNull(pnpGroup);
		assertEquals("2024-PNP-2", pnpGroup);
	}
	
	
	/**
	 * Scenario 1 – Sign up in 2022 before 2022 PI. 
	 */
	@Test
	public void testPnpGroupSatelliteScenario1() {
		PriceProtection priceProtection = new PriceProtection();
		priceProtection.setStartDate("01/20/2022");
		priceProtection.setEndDate("01/19/2023");
		
		String pnpGroup = pnpGroupUtils.getPnpGroup(priceProtection, "11/20/2023", Constants.SATELLITE_PRODUCT_FAMILY);
		
		assertNull(pnpGroup);
	}
	
	/**
	 * Scenario 2 – Sign up in 2022 between 2022 PI and 2023 PI. 
	 */
	@Test
	public void testPnpGroupSatelliteScenario2() {
		PriceProtection priceProtection = new PriceProtection();
		priceProtection.setStartDate("12/25/2022");
		priceProtection.setEndDate("12/24/2024");
		
		Mockito.when(redisCacheHelper.getValues("PP-TAZCONTRACT-GRANDFATHERED", Constants.SATELLITE_PRODUCT_FAMILY)).thenReturn(null);
		Mockito.when(redisCacheHelper.getValues("2022-PNP", Constants.SATELLITE_PRODUCT_FAMILY)).thenReturn(Arrays.asList("01/23/2022"));
		
		String pnpGroup = pnpGroupUtils.getPnpGroup(priceProtection, "11/20/2023", Constants.SATELLITE_PRODUCT_FAMILY);
		
		assertNotNull(pnpGroup);
		assertEquals("2022-PNP-1", pnpGroup);
	}
	
	/**
	 * Scenario 3 – Sign up in 2023 between 2022 PI and 2023 PI. 
	 */
	@Test
	public void testPnpGroupSatelliteScenario3() {
		PriceProtection priceProtection = new PriceProtection();
		priceProtection.setStartDate("01/15/2023");
		priceProtection.setEndDate("01/14/2025");
		
		Mockito.when(redisCacheHelper.getValues("PP-TAZCONTRACT-GRANDFATHERED", Constants.SATELLITE_PRODUCT_FAMILY)).thenReturn(null);
		Mockito.when(redisCacheHelper.getValues("2022-PNP", Constants.SATELLITE_PRODUCT_FAMILY)).thenReturn(Arrays.asList("01/23/2022"));
		Mockito.when(redisCacheHelper.getValues("2023-PNP", Constants.SATELLITE_PRODUCT_FAMILY)).thenReturn(Arrays.asList("01/22/2023|11/05/2023"));
		
		String pnpGroup = pnpGroupUtils.getPnpGroup(priceProtection, "11/20/2023", Constants.SATELLITE_PRODUCT_FAMILY);
		
		assertNotNull(pnpGroup);
		assertEquals("2022-PNP-1", pnpGroup);
	}
	
	/**
	 * Scenario 4 – Sign up in 2023 between 1st and 2nd 2023 PIs.
	 */
	@Test
	public void testPnpGroupSatelliteScenario4() {
		PriceProtection priceProtection = new PriceProtection();
		priceProtection.setStartDate("03/25/2023");
		priceProtection.setEndDate("03/24/2025");
		
		Mockito.when(redisCacheHelper.getValues("PP-TAZCONTRACT-GRANDFATHERED", Constants.SATELLITE_PRODUCT_FAMILY)).thenReturn(null);
		Mockito.when(redisCacheHelper.getValues("2023-PNP", Constants.SATELLITE_PRODUCT_FAMILY)).thenReturn(Arrays.asList("01/22/2023|11/05/2023"));
		
		String pnpGroup = pnpGroupUtils.getPnpGroup(priceProtection, "11/20/2023", Constants.SATELLITE_PRODUCT_FAMILY);
		
		assertNotNull(pnpGroup);
		assertEquals("2023-PNP-1", pnpGroup);
	}
	
	/**
	 * Scenario 5 – Sign up in 2023 after 2nd 2023 PI. 
	 */
	@Test
	public void testPnpGroupSatelliteScenario5() {
		PriceProtection priceProtection = new PriceProtection();
		priceProtection.setStartDate("11/25/2023");
		priceProtection.setEndDate("11/24/2025");
		
		Mockito.when(redisCacheHelper.getValues("PP-TAZCONTRACT-GRANDFATHERED", Constants.SATELLITE_PRODUCT_FAMILY)).thenReturn(null);
		Mockito.when(redisCacheHelper.getValues("2023-PNP", Constants.SATELLITE_PRODUCT_FAMILY)).thenReturn(Arrays.asList("01/22/2023|11/05/2023"));
		
		String pnpGroup = pnpGroupUtils.getPnpGroup(priceProtection, "12/25/2023", Constants.SATELLITE_PRODUCT_FAMILY);
		
		assertNotNull(pnpGroup);
		assertEquals("2023-PNP-2", pnpGroup);
	}
	
	/**
	 * Scenario 6 – Sign up in 2023 after 2nd 2023 PI. 
	 */
	@Test
	public void testPnpGroupSatelliteScenario6() {
		PriceProtection priceProtection = new PriceProtection();
		priceProtection.setStartDate("11/25/2023");
		priceProtection.setEndDate("11/24/2025");
		
		Mockito.when(redisCacheHelper.getValues("PP-TAZCONTRACT-GRANDFATHERED", Constants.SATELLITE_PRODUCT_FAMILY)).thenReturn(null);
		Mockito.when(redisCacheHelper.getValues("2023-PNP", Constants.SATELLITE_PRODUCT_FAMILY)).thenReturn(Arrays.asList("01/22/2023|11/05/2023"));
		
		String pnpGroup = pnpGroupUtils.getPnpGroup(priceProtection, "02/25/2024", Constants.SATELLITE_PRODUCT_FAMILY);
		
		assertNotNull(pnpGroup);
		assertEquals("2023-PNP-2", pnpGroup);
	}
	
	/**
	 * Scenario 7 – Sign up in 2024, no PI in 2024. (Use same date as 2023 PI) 
	 */
	@Test
	public void testPnpGroupSatelliteScenario7() {
		PriceProtection priceProtection = new PriceProtection();
		priceProtection.setStartDate("01/25/2024");
		priceProtection.setEndDate("01/24/2026");
		
		Mockito.when(redisCacheHelper.getValues("PP-TAZCONTRACT-GRANDFATHERED", Constants.SATELLITE_PRODUCT_FAMILY)).thenReturn(null);
		Mockito.when(redisCacheHelper.getValues("2024-PNP", Constants.SATELLITE_PRODUCT_FAMILY)).thenReturn(Arrays.asList("11/05/2023"));
		
		String pnpGroup = pnpGroupUtils.getPnpGroup(priceProtection, "02/25/2024", Constants.SATELLITE_PRODUCT_FAMILY);
		
		assertNotNull(pnpGroup);
		assertEquals("2024-PNP-1", pnpGroup);
	}
	
	
	/**
	 * Scenario 8 – Sign up in 2024, before 1st PI in 2024. 
	 */
	@Test
	public void testPnpGroupSatelliteScenario8() {
		PriceProtection priceProtection = new PriceProtection();
		priceProtection.setStartDate("01/25/2024");
		priceProtection.setEndDate("01/24/2026");
		
		Mockito.when(redisCacheHelper.getValues("PP-TAZCONTRACT-GRANDFATHERED", Constants.SATELLITE_PRODUCT_FAMILY)).thenReturn(null);
		Mockito.when(redisCacheHelper.getValues("2023-PNP", Constants.SATELLITE_PRODUCT_FAMILY)).thenReturn(Arrays.asList("01/22/2023|11/05/2023"));
		Mockito.when(redisCacheHelper.getValues("2024-PNP", Constants.SATELLITE_PRODUCT_FAMILY)).thenReturn(Arrays.asList("10/06/2024"));
		
		String pnpGroup = pnpGroupUtils.getPnpGroup(priceProtection, "02/25/2024", Constants.SATELLITE_PRODUCT_FAMILY);
		
		assertNotNull(pnpGroup);
		assertEquals("2023-PNP-2", pnpGroup);
	}
	
	/**
	 * Scenario 9 – Sign up in 2024, after 1st PI in 2024. 
	 */
	@Test
	public void testPnpGroupSatelliteScenario9() {
		PriceProtection priceProtection = new PriceProtection();
		priceProtection.setStartDate("11/25/2024");
		priceProtection.setEndDate("11/24/2026");
		
		Mockito.when(redisCacheHelper.getValues("PP-TAZCONTRACT-GRANDFATHERED", Constants.SATELLITE_PRODUCT_FAMILY)).thenReturn(null);
		Mockito.when(redisCacheHelper.getValues("2024-PNP", Constants.SATELLITE_PRODUCT_FAMILY)).thenReturn(Arrays.asList("10/06/2024"));
		
		String pnpGroup = pnpGroupUtils.getPnpGroup(priceProtection, "12/25/2024", Constants.SATELLITE_PRODUCT_FAMILY);
		
		assertNotNull(pnpGroup);
		assertEquals("2024-PNP-1", pnpGroup);
	}
	
	/**
	 * Scenario 10 – Sign up in 2025, no PI in 2025.  (Use same date as 2024 PI) 
	 */
	@Test
	public void testPnpGroupSatelliteScenario10() {
		PriceProtection priceProtection = new PriceProtection();
		priceProtection.setStartDate("01/25/2025");
		priceProtection.setEndDate("01/24/2027");
		
		Mockito.when(redisCacheHelper.getValues("PP-TAZCONTRACT-GRANDFATHERED", Constants.SATELLITE_PRODUCT_FAMILY)).thenReturn(null);
		Mockito.when(redisCacheHelper.getValues("2025-PNP", Constants.SATELLITE_PRODUCT_FAMILY)).thenReturn(Arrays.asList("10/06/2024"));
		
		String pnpGroup = pnpGroupUtils.getPnpGroup(priceProtection, "02/25/2025", Constants.SATELLITE_PRODUCT_FAMILY);
		
		assertNotNull(pnpGroup);
		assertEquals("2025-PNP-1", pnpGroup);
	}
}
