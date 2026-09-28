package com.dtv.dcp.epoch.integration.rsn;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;


public class BasePackageTest {
	

	@Test
	public void testRSNObject() {
		BasePackage basePackage = new BasePackage();
		basePackage.setSku("sku");
		basePackage.setContracted(true);
		assertNotNull(basePackage.getSku());
		assertNotNull(basePackage.isContracted());
		String toStringPromotionBasePackage = basePackage.toString();
		assertNotNull(toStringPromotionBasePackage);

		Promotion promotionObj = new Promotion();
		promotionObj.setAgentOffer("agentOffer");
		promotionObj.setAmount(23242.56);
		promotionObj.setBillingProductCode("P0021");
		promotionObj.setBillingProductId("billingProductId");
		promotionObj.setContractIndicator(false);
		promotionObj.setContractVersion("v2");
		promotionObj.setDescription("setDescription");
		promotionObj.setDescriptionForSales("descriptionForSales");
		promotionObj.setDescriptionForServices("descriptionForServices");
		promotionObj.setDisplayName("displayName");
		promotionObj.setDisplayNameForSales("displayNameForSales");
		promotionObj.setDisplayNameForServices("displayNameForServices");
		promotionObj.setDisplayOrder(4);
		promotionObj.setDuration("duration");
		promotionObj.setDurationPeriod("durationPeriod");
		promotionObj.setEnableServiceDisconnect(false);
		promotionObj.setExpDate("expDate");
		promotionObj.setExtPromoType("extPromoType");
		promotionObj.setFreeTrialPeriod(4);
		promotionObj.setIoOffer(true);
		promotionObj.setLongDescriptionForSales("longDescriptionForSales");
		promotionObj.setLongDescriptionForServices("longDescriptionForServices");
		promotionObj.setMaxOccurrence(3);
		promotionObj.setMyATTDisclosureMsg("myATTDisclosureMsg");
		promotionObj.setName("name");
		promotionObj.setoPUSDisclosureMsg("oPUSDisclosureMsg");
		promotionObj.setValidityEndDate("validityEndDate");
		promotionObj.setParentPromotionId("parentPromotionId");
		promotionObj.setPercentage(5.3);
		promotionObj.setPrepayDuration("prepayDuration");
		promotionObj.setPrepayPeriod("prepayPeriod");
		promotionObj.setPromoPriority("promoPriority");
		promotionObj.setPromotionCategory("promotionCategory");
		promotionObj.setPromotionCode("promotionCode");
		promotionObj.setProvisioningCode("provisioningCode");
		promotionObj.setSegmentDescription("segmentDescription");

		List<ShortDescription> shortDescriptionForSales = new ArrayList<>();
		ShortDescription newShortDescription = new ShortDescription();
		shortDescriptionForSales.add(newShortDescription);
		promotionObj.setShortDescriptionForSales(shortDescriptionForSales);

		promotionObj.setShortDescriptionForServices("shortDescriptionForServices");
		promotionObj.setValidityEndDate("validityEndDate");
		promotionObj.setShortDescriptionForSales(shortDescriptionForSales);
		promotionObj.setPromoType("promoType");
		promotionObj.setFreeTrialPromo(true);
		promotionObj.setShortDescriptionForSales(shortDescriptionForSales);
		String toStringPromotionObj = promotionObj.toString();
		assertNotNull(promotionObj.getShortDescriptionForSales());
		assertNotNull(promotionObj.getAgentOffer());
		assertNotNull(toStringPromotionObj);
		assertNotNull(promotionObj.getAgentOffer());
		assertNotNull(promotionObj.isFreeTrialPromo());
		assertNotNull(promotionObj.getPromoType());
		assertNotNull(promotionObj.getAmount());
		assertNotNull(promotionObj.getBillingProductCode());
		assertNotNull(promotionObj.getBillingProductId());
		assertNotNull(promotionObj.isContractIndicator());
		assertNotNull(promotionObj.getContractVersion());
		assertNotNull(promotionObj.getDescription());
		assertNotNull(promotionObj.getDescriptionForSales());
		assertNotNull(promotionObj.getDescriptionForServices());
		assertNotNull(promotionObj.getDisplayName());
		assertNotNull(promotionObj.getDisplayNameForSales());
		assertNotNull(promotionObj.getDisplayNameForServices());
		assertNotNull(promotionObj.getDisplayOrder());
		assertNotNull(promotionObj.getDuration());

		assertNotNull(promotionObj.getDurationPeriod());
		assertNotNull(promotionObj.isEnableServiceDisconnect());
		assertNotNull(promotionObj.getExpDate());
		assertNotNull(promotionObj.getExtPromoType());
		assertNotNull(promotionObj.getFreeTrialPeriod());
		assertNotNull(promotionObj.getIoOffer());
		assertNotNull(promotionObj.getLongDescriptionForSales());

		assertNotNull(promotionObj.getParentPromotionId());
		assertNotNull(promotionObj.getValidityEndDate());
		assertNotNull(promotionObj.getoPUSDisclosureMsg());
		assertNotNull(promotionObj.getName());
		assertNotNull(promotionObj.getMyATTDisclosureMsg());
		assertNotNull(promotionObj.getMaxOccurrence());
		assertNotNull(promotionObj.getLongDescriptionForServices());
		assertNotNull(promotionObj.getPercentage());
		assertNotNull(promotionObj.getPrepayDuration());
		assertNotNull(promotionObj.getPrepayPeriod());
		assertNotNull(promotionObj.getPromoPriority());
		assertNotNull(promotionObj.getPromotionCategory());
		assertNotNull(promotionObj.getPromotionCode());
		assertNotNull(promotionObj.getProvisioningCode());
		assertNotNull(promotionObj.getSegmentDescription());
		assertNotNull(promotionObj.getShortDescriptionForServices());

	}

	@Test
	public void testRSNFeeObject() {
		RSNFee rSNFeeObj = new RSNFee();
		rSNFeeObj.setDescriptionforSales("descriptionforSales");
		rSNFeeObj.setAmount(45.5);
		rSNFeeObj.setDescription("description");
		rSNFeeObj.setDescriptionforSales("descriptionforSales");
		rSNFeeObj.setDescriptionforServices("descriptionforServices");
		rSNFeeObj.setDisplayName("displayName");
		rSNFeeObj.setDisplayNameforSales("displayNameforSales");
		rSNFeeObj.setDisplayNameforServices("displayNameforServices");
		rSNFeeObj.setName("name");
		rSNFeeObj.setLongDescriptionforSales("setLongDescriptionforSales");
		assertNotNull(rSNFeeObj.getLongDescriptionforSales());

		rSNFeeObj.setLongDescription("setLongDescription");
		assertNotNull(rSNFeeObj.getLongDescription());
		assertNotNull(rSNFeeObj.getName());

		rSNFeeObj.setLongDescriptionforServices("setLongDescriptionforServices");
		assertNotNull(rSNFeeObj.getLongDescriptionforServices());

		List<Promotion> promotions = new ArrayList<>();
		Promotion promotion = new Promotion();
		promotions.add(promotion);
		rSNFeeObj.setPromotions(promotions);

		List<ShortDescription> ShortDescriptions = new ArrayList<>();
		ShortDescription newShortDescription = new ShortDescription();
		ShortDescriptions.add(newShortDescription);
		rSNFeeObj.setShortDescriptionforSales(ShortDescriptions);

		rSNFeeObj.setShortDescriptionforServices("shortDescriptionforServices");
		rSNFeeObj.setSku("sku");
		rSNFeeObj.setType("type");
		rSNFeeObj.setVisible(true);

		assertNotNull(rSNFeeObj.getDescriptionforSales());
		assertNotNull(rSNFeeObj.getAmount());

		assertNotNull(rSNFeeObj.getDescription());
		assertNotNull(rSNFeeObj.getDescriptionforSales());
		assertNotNull(rSNFeeObj.getDescriptionforServices());
		assertNotNull(rSNFeeObj.getDisplayName());
		assertNotNull(rSNFeeObj.getDisplayNameforSales());
		assertNotNull(rSNFeeObj.getPromotions());
		assertNotNull(rSNFeeObj.getShortDescriptionforSales());
		assertNotNull(rSNFeeObj.getType());
		assertNotNull(rSNFeeObj.getVisible());
		assertNotNull(rSNFeeObj.getSku());
		assertNotNull(rSNFeeObj.getSku());

		assertNotNull(rSNFeeObj.getDisplayNameforServices());
		assertNotNull(rSNFeeObj.getShortDescriptionforServices());

		String toStringrSNFeeObj = rSNFeeObj.toString();
		assertNotNull(toStringrSNFeeObj);
	}

	@Test
	public void testRSNResponseObj() {
		RSNResponse rSNResponseObj = new RSNResponse();
		RSNFee rSNFeeObj = new RSNFee();
		rSNFeeObj.setDescriptionforSales("descriptionforSales");
		rSNFeeObj.setAmount(45.5);
		rSNFeeObj.setDescription("description");
		rSNFeeObj.setDescriptionforSales("descriptionforSales");
		rSNFeeObj.setDescriptionforServices("descriptionforServices");
		rSNFeeObj.setDisplayName("displayName");
		rSNFeeObj.setDisplayNameforSales("displayNameforSales");
		rSNFeeObj.setDisplayNameforServices("displayNameforServices");
		rSNFeeObj.setName("name");
		rSNResponseObj.setFee(rSNFeeObj);
		rSNResponseObj.setMessage("message");
		assertNotNull(rSNResponseObj.getFee());
		assertNotNull(rSNResponseObj.getMessage());
		String toStringrRSNResponseObj = rSNResponseObj.toString();
		assertNotNull(toStringrRSNResponseObj);
	}

	@Test
	public void testShortDescriptionObject() {
		ShortDescription shortDescriptionObj = new ShortDescription();
		shortDescriptionObj.setShortDesc("shortDesc");
		shortDescriptionObj.setSystem("system");
		assertNotNull(shortDescriptionObj.getShortDesc());
		assertNotNull(shortDescriptionObj.getSystem());
		String toStringShortDescriptionObject = shortDescriptionObj.toString();
		assertNotNull(toStringShortDescriptionObject);
	}

	@Test
	public void testRSNRequestObject() {
		RSNRequest RSNRequestObj = new RSNRequest();
		BasePackage basePackage = new BasePackage();
		basePackage.setSku("sku");
		basePackage.setContracted(true);
		RSNRequestObj.setBasePackage(basePackage);
		RSNRequestObj.setZipCode("zipCode");
		RSNRequestObj.setAccountType("accountType");

		assertNotNull(RSNRequestObj.getBasePackage());
		assertNotNull(RSNRequestObj.getZipCode());
		assertNotNull(RSNRequestObj.getAccountType());

		String toStringRSNRequestObj = RSNRequestObj.toString();
		assertNotNull(toStringRSNRequestObj);
	}
	
	@Test
	public void testGetRSNInfo() {
		RSNRequest RSNRequestObj = new RSNRequest();
		BasePackage basePackage = new BasePackage();
		basePackage.setSku("sku");
		basePackage.setContracted(true);
		RSNRequestObj.setBasePackage(basePackage);
		RSNRequestObj.setZipCode("zipCode");
		RSNRequestObj.setAccountType("accountType");

		
	}
	
	@Test
	public void testPromotion() {
		Promotion promotionObj = new Promotion();	
		assertNotNull(promotionObj.equals(new Object()));
		String toStringPromotionObj = promotionObj.toString();
		assertNotNull(toStringPromotionObj);

		
	}
	
	

}
