package com.dtv.dcp.epoch.model;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.dtv.dcp.epoch.processor.ott.services.model.BestPrice;
import com.dtv.dcp.epoch.processor.ott.services.model.CtProductInfo;
import com.dtv.dcp.epoch.processor.ott.services.model.CustomerServiceDetail;

public class ModelTest {
	private static List<String> beanPackage = new ArrayList<>();
	static {
		beanPackage.add("com.att.idp.video.epoch.model");

	}

	@Test
	public void testWatchTVModel() {
		BestPrice bestwatchPrice = new BestPrice();
		bestwatchPrice.setBasePrice(10.5);
		bestwatchPrice.setBestPrice(11.8);
		assertNotNull( bestwatchPrice.getBasePrice().doubleValue());
		
		    CtProductInfo ctProductWatchInfo = new CtProductInfo();
		    ctProductWatchInfo.setBasePrice("10.5");
		    ctProductWatchInfo.setStatus("status");
		    ctProductWatchInfo.setContractPrice("contractPrice");
		    ctProductWatchInfo.setPackageType("packageType");
		    assertNotNull(ctProductWatchInfo.getBasePrice());
		    assertNotNull(ctProductWatchInfo.getStatus());
		    assertNotNull(ctProductWatchInfo.getContractPrice());
		    assertNotNull(ctProductWatchInfo.getPackageType());
		    
		    
		    CustomerServiceDetail customerServiceDetail = new CustomerServiceDetail();
		    customerServiceDetail.setTypeOfPlan("typePlan");
		    customerServiceDetail.setAccountPrice(23344.5);
		    List<String> promosId = new ArrayList<>();
		    promosId.add("promosId");
		    customerServiceDetail.setPromosId(promosId);
		    
		    assertNotNull(customerServiceDetail.getTypeOfPlan());
		    assertNotNull(customerServiceDetail.getAccountPrice()); 
		    assertNotNull(customerServiceDetail.getPromosId());
		   // assertNotNull(ctProductInfo.getBasePrice());

	}

}