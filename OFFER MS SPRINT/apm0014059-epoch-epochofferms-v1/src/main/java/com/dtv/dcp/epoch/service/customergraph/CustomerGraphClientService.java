package com.dtv.dcp.epoch.service.customergraph;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.model.customergraph.CustomerCoupons;
import com.dtv.dcp.epoch.model.customergraph.CustomerGraphPublisherMessage;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.JsonService;


@Service("CustomerGraphService")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CustomerGraphClientService extends CustomerGraphClient {
	
	private static final Logger log = LoggerFactory.getLogger(CustomerGraphClientService.class);
	
	@Value("${apiclient.rest.cg.baseUrl}")
	private String baseUrl;
	@Value("${apiclient.rest.cg.path}")
	private String path;
	
	private static final String apiKey = "customergraph";
	private static final String customerGraphTopicKey = "coupon";
	
	@Value("${spring.kafka.cg.template.cpn-topic}")
	private String customerGraphTopic;
	
	@Value("EPOCH")
	private String SOR;


	@Autowired
	private CustomerGraphPublisherMessage customerGraphMessage;
	@Autowired
	private KafkaTemplate<String, CustomerGraphPublisherMessage> kafkaTemplateCG;
	@Autowired
	private FeatureManagerHelper featureManagerHelper;
	
	public void publishUpdateCouponMessage(CustomerCoupons updateCoupon, String accountId) {
		customerGraphMessage = new CustomerGraphPublisherMessage();
		
		customerGraphMessage.setSOR(SOR);
		customerGraphMessage.setSORDataUpdatedTimeStamp(getCurrentTime());
		customerGraphMessage.setSORNotificationCreationTimestamp(getCurrentTime());
		customerGraphMessage.setCouponCode(updateCoupon.getCouponCode());
		customerGraphMessage.setCustomerID(accountId);
		customerGraphMessage.setCustomerBillingSystem(updateCoupon.getBillingSystem());
		if (featureManagerHelper.isEnabled(Constants.SVC_EPOCH_UCC_B_ENABLED) &&
				updateCoupon.getCouponType() != null && 
				(updateCoupon.getCouponType()).equalsIgnoreCase(Constants.MULTI_USE_TYPE_2_COUPON)) {
			customerGraphMessage.setCouponType(updateCoupon.getCouponType());
			customerGraphMessage.setCouponStatus(updateCoupon.getStatusDescription());
			customerGraphMessage.setLockDuration(updateCoupon.getLockDuration());
			customerGraphMessage.setUsedDate(updateCoupon.getUsedDate());
			customerGraphMessage.setTitlePurchased(updateCoupon.getTitlePurchased());
			customerGraphMessage.setRetailPrice(updateCoupon.getRetailPrice());
			customerGraphMessage.setDiscountAmount(updateCoupon.getDiscountAmount());
			customerGraphMessage.setNetAmount(updateCoupon.getNetAmount());
			customerGraphMessage.setTmsProgramID(updateCoupon.getTmsProgramId());
			customerGraphMessage.setTransactionStatus(Constants.INVALID_STATUS);
		} else {
			customerGraphMessage.setCouponStatus(Constants.AVAILABLE_COUPON);
		}
		
		log.info("EPOCH_CG_COUPON_UPDATE_REQUEST-[{},{}]", customerGraphTopic, JsonService.getJsonFromObject(customerGraphMessage));
		
		kafkaTemplateCG.send(customerGraphTopic, customerGraphTopicKey, customerGraphMessage);
		
		log.info("Sent update coupon message to Customer Graph topic... ");
	}
	
	@Override
	protected String getApiKey() {
		return apiKey;
	}
	
	public String getCurrentTime() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		// Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_CG);
        return dateFormat.format(calendar.getTime());
	}

}
