package com.dtv.dcp.epoch.model;

import static org.junit.Assert.assertNotNull;

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.dtv.dcp.epoch.processor.ott.services.model.BestPrice;
import com.dtv.dcp.epoch.processor.ott.services.model.CtProductInfo;
import com.dtv.dcp.epoch.processor.ott.services.model.CustomerServiceDetail;
import com.dtv.dcp.epoch.util.ClassFinder;
import com.dtv.dcp.epoch.util.JavaBeanExtendedTester;



public class BeansTest { 
	private static List<String> beanPackage=new ArrayList<>();
	static {
		beanPackage.add("com.att.idp.video.epoch.model");
		
	}
	@Test
	public void testBeans() throws Exception {
		List<Class<?>> classesToSkipped=new ArrayList<>();
		executeBeanTest(classesToSkipped);
		executeCustomBeanTest();
	}
	

	private void executeCustomBeanTest() {
		BestPrice bestPrice = new BestPrice();
		bestPrice.setBasePrice(10.5);
		bestPrice.setBestPrice(11.8);
	    assertNotNull( bestPrice.getBasePrice().doubleValue());
	    assertNotNull( bestPrice.getBestPrice().doubleValue());
	    CtProductInfo ctProductInfo = new CtProductInfo();
	    ctProductInfo.setBasePrice("10.5");
	    ctProductInfo.setStatus("status");
	    ctProductInfo.setContractPrice("contractPrice");
	    ctProductInfo.setPackageType("packageType");
	    assertNotNull(ctProductInfo.getBasePrice());
	    assertNotNull(ctProductInfo.getStatus());
	    assertNotNull(ctProductInfo.getContractPrice());
	    assertNotNull(ctProductInfo.getPackageType());
	    
	    CustomerServiceDetail customerServiceDetail = new CustomerServiceDetail();
	    customerServiceDetail.setTypeOfPlan("typePlan");
	    customerServiceDetail.setAccountPrice(23344.5);
	    List<String> promosId = new ArrayList<>();
	    promosId.add("promosId");
	    customerServiceDetail.setPromosId(promosId);
	    
	    assertNotNull(customerServiceDetail.getTypeOfPlan());
	    assertNotNull(customerServiceDetail.getAccountPrice()); 
	    assertNotNull(customerServiceDetail.getPromosId());
	    assertNotNull(ctProductInfo.getBasePrice());
	    
	    
	   
		
	}

	private void executeBeanTest(List<Class<?>> classesToSkipped)throws Exception {
		
		for(String pkg:beanPackage) {
			List<Class<?>> setOfClass=ClassFinder.getClasses(pkg);
			setOfClass.forEach(clazz->{
				try {
					if(!(classesToSkipped.contains(clazz)||clazz.isInterface())) {
						JavaBeanExtendedTester.test(clazz);
					} 
				} catch (IntrospectionException e) {
					e.printStackTrace();
				}
				
			});
		}


	}
	
	
}