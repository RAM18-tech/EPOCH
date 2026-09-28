package com.dtv.dcp.epoch.service.customergraph;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.model.customergraph.CustomerCoupons;
import com.dtv.dcp.epoch.model.customergraph.CustomerGraphPublisherMessage;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;

@ExtendWith(MockitoExtension.class)
public class CustomerGraphClientServiceTest {

	private static final Logger log = LoggerFactory.getLogger(CustomerGraphClientService.class);
	@InjectMocks
	@Spy
	private CustomerGraphClientService customerGraphClientService;
	
	@Mock
	private KafkaTemplate<String, CustomerGraphPublisherMessage> kafkaTemplateCG;
	
	@Mock
	FeatureManagerHelper featureManagerHelper;
	
	
	
	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	public void testPublishUpdateCouponMessageSU() {
		Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(false);
		CustomerCoupons updateCoupon = new DataReader().readFileToObj("couponRes.json",CustomerCoupons.class);
		customerGraphClientService.publishUpdateCouponMessage(updateCoupon, "12345");
	}
	
	@Test
	public void testPublishUpdateCouponMessageMU1() {
		Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(false);
		CustomerCoupons updateCoupon = new DataReader().readFileToObj("couponResMU1.json",CustomerCoupons.class);
		customerGraphClientService.publishUpdateCouponMessage(updateCoupon, "12345");
	}
	
	@Test
	public void testPublishUpdateCouponMessageTrue() {
		Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(true);
		CustomerCoupons updateCoupon = new DataReader().readFileToObj("couponResMU2.json",CustomerCoupons.class);
		customerGraphClientService.publishUpdateCouponMessage(updateCoupon, "12345");
	}

	@Test
	public void testGetCurrentTime() {
		customerGraphClientService.getCurrentTime();
	}

}
