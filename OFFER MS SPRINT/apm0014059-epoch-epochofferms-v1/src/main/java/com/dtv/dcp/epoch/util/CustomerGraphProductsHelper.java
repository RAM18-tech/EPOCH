package com.dtv.dcp.epoch.util;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.model.customergraph.response.BillingOffer;
import com.dtv.dcp.epoch.model.customergraph.response.CGAccountProducts;
import com.dtv.dcp.epoch.model.customergraph.response.CGProduct;
import com.dtv.dcp.epoch.model.customergraph.response.CharacteristicDetail;
import com.dtv.dcp.epoch.model.customergraph.response.ChildPricingSchema;
import com.dtv.dcp.epoch.model.customergraph.response.ComponentDetail;
import com.dtv.dcp.epoch.model.customergraph.response.ContainedProduct;
import com.dtv.dcp.epoch.model.customergraph.response.ParentProductSpecContainment;
import com.dtv.dcp.epoch.model.customergraph.response.Product;
import com.dtv.dcp.epoch.model.customergraph.response.ProductDetails;
import com.dtv.dcp.epoch.model.customergraph.response.ProductSpecificationPricing;
import com.dtv.dcp.epoch.model.customergraph.response.Promotion;
import com.dtv.dcp.epoch.model.customergraph.response.UVAccountProductsResponse;
import com.dtv.dcp.epoch.model.customergraph.response.WirelineAssignedProductDetails;

@Component
public class CustomerGraphProductsHelper {

	private static final String DTVS_LINE_OF_BUSINESS = "DT";
	private static final String VOIPLN="VOIPLN";
	private static final List<String> IPTV_TYPE_CODES = Arrays.asList("VBP", "STB", "GP");
	private static final List<String> HSIA_TYPE_CODES = Arrays.asList("HSIA", "FL");
	private static final List<String> VOIP_TYPE_CODES = Arrays.asList("CVOIP", VOIPLN, "VOIPCP");
	private static final List<String> BASE_PACKAGE_TYPE_CODES = Arrays.asList("HSIA", "VBP", VOIPLN, "ATTDV");
	private static final List<String> ADDON_TYPE_CODES = Arrays.asList("GP", "DTGP", "STB");
	private static final String PICK_SERVICE_TYPE_CODE = "DTPCK";

	private static final Map<String, String> BASE_PKG_PROD_CODE_MAP = Stream.of(
			new AbstractMap.SimpleEntry<>("HSIA", "Speed"), new AbstractMap.SimpleEntry<>("VBP", "RequestedPackage"),
			new AbstractMap.SimpleEntry<>(VOIPLN, "usagePlan"), new AbstractMap.SimpleEntry<>("ATTDV", "BasePkg"))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

	public CGAccountProducts createUVerseAccountProducts(UVAccountProductsResponse accountProductsResponse) {

		CGAccountProducts cgAccountProducts = new CGAccountProducts();
		Map<String, Product> productMap = new HashMap<>();
		Map<String, List<Promotion>> promotionMap = new HashMap<>();

		Optional.ofNullable(accountProductsResponse).map(UVAccountProductsResponse::getProductDetails)
				.map(ProductDetails::getProducts)
				.ifPresent(products -> products
						.forEach(product -> Optional.ofNullable(product.getWirelineAssignedProductDetails())
								.map(WirelineAssignedProductDetails::getComponentDetails)
								.ifPresent(cds -> cds.forEach(cd -> processComponentDetail(product, cd,
										cgAccountProducts, productMap, promotionMap)))));

		return cgAccountProducts;
	}

	private void processComponentDetail(CGProduct product, ComponentDetail componentDetail,
										CGAccountProducts cgAccountProducts, Map<String, Product> productMap,
										Map<String, List<Promotion>> promotionMap) {

		// process all child components
		Optional.ofNullable(componentDetail.getComponentDetails())
				.ifPresent(childComponents -> childComponents.forEach(childComponent -> processComponentDetail(product,
						childComponent, cgAccountProducts, productMap, promotionMap)));

		if (componentDetail.getBillingOffers() != null) {
			// create products and promotions from billing offers for current
			// component
			componentDetail.getBillingOffers().stream()
					.filter(billingOffer -> "AC".equals(billingOffer.getStatusCode()))
					.forEach(billingOffer -> processBillingOffer(billingOffer, product, componentDetail,
							cgAccountProducts, productMap, promotionMap));
		}
	}

	private void processBillingOffer(BillingOffer billingOffer, CGProduct product, ComponentDetail componentDetail,
									 CGAccountProducts cgAccountProducts, Map<String, Product> productMap,
									 Map<String, List<Promotion>> promotionMap) {

		if (billingOffer.getBaseOfferId() != null) {
			Promotion cgPromotion = createPromotion(billingOffer);

			if (productMap.containsKey(billingOffer.getBaseOfferId())) {
				productMap.get(billingOffer.getBaseOfferId()).getPromotions().add(cgPromotion);
			} else {

				List<Promotion> temp = Optional.ofNullable(promotionMap.get(billingOffer.getBaseOfferId()))
						.orElse(new ArrayList<>());

				temp.add(cgPromotion);
				promotionMap.put(billingOffer.getBaseOfferId(), temp);
			}
		} else {
			Product cgProduct = createProduct(billingOffer, componentDetail, product);

			cgAccountProducts.getProducts().add(cgProduct);
			productMap.put(cgProduct.getProductBillingId(), cgProduct);

			if (cgProduct.getProductBillingId() != null && promotionMap.containsKey(cgProduct.getProductBillingId())) {
				cgProduct.getPromotions().addAll(promotionMap.get(cgProduct.getProductBillingId()));
			}

		}
	}

	private Promotion createPromotion(BillingOffer billingOffer) {
		Promotion cgPromotion = new Promotion();

		cgPromotion.setPromotionName(billingOffer.getName());
		cgPromotion.setPromotionBillingID(Optional.ofNullable(billingOffer.getProductSpecificationPricing())
				.map(ProductSpecificationPricing::getChildPricingSchema).map(ChildPricingSchema::getPricePlanCode)
				.orElse(null));
		cgPromotion.setPromotionBillingCode(cgPromotion.getPromotionBillingID());
		cgPromotion.setPromotionType(billingOffer.getPromType());
		cgPromotion.setPromotionEndDate(billingOffer.getPromotionExpireDate());
		cgPromotion.setPromotionStatus(billingOffer.getStatusCode());
		cgPromotion.setPromotionStatusValue(billingOffer.getStatusValue());
		Optional.ofNullable(billingOffer.getPromRank())
				.ifPresent(rank -> cgPromotion.setPromoRank(Integer.valueOf(rank)));
		cgPromotion.setPromotionReason(billingOffer.getPromotionReason());

		Optional.ofNullable(billingOffer.getIntegratedOfferOriginator()).ifPresent(c -> cgPromotion.setIoPromo(true));

		return cgPromotion;
	}

	private Product createProduct(BillingOffer billingOffer, ComponentDetail componentDetail, CGProduct product) {
		Product cgProduct = new Product();

		cgProduct.setProductName(billingOffer.getName());
		cgProduct.setProductBillingId(Optional.ofNullable(billingOffer.getProductSpecificationPricing())
				.map(ProductSpecificationPricing::getChildPricingSchema).map(ChildPricingSchema::getPricePlanCode)
				.orElse(null));
		cgProduct.setProductBillingCode(cgProduct.getProductBillingId());
		cgProduct.setStatus(billingOffer.getStatusCode());
		cgProduct.setLinesOfBusiness(getLosg(product, componentDetail));

		if (isBasePackage(billingOffer, componentDetail)) {
			cgProduct.setProductType(Product.ProductType.BASE_PACKAGE.toString());
			cgProduct.setProductBillingCode(getBasePackageProductCode(componentDetail));
			cgProduct.setProductName(getBasePackageDisplayName(componentDetail));

		} else if (isAddon(billingOffer, componentDetail)) {
			cgProduct.setProductType(Product.ProductType.ADDON.toString());
			cgProduct.setProductBillingCode(
					componentDetail.getParentProductSpecContainment().getContainedProducts().get(0).getCode());
		} else if (isPickService(componentDetail)) {
			cgProduct.setProductType(Product.ProductType.PICK_SERVICES.toString());
			cgProduct.setProductBillingCode(
					componentDetail.getParentProductSpecContainment().getContainedProducts().get(0).getCode());
		}
		cgProduct.setProductDescription(cgProduct.getProductName());

		return cgProduct;
	}

	private String getBasePackageProductCode(ComponentDetail componentDetail) {
		String productCode = null;
		String typeCode = getComponentTypeCode(componentDetail);

		if (componentDetail.getCharacteristicDetails() != null) {
			productCode = componentDetail.getCharacteristicDetails().stream()
					.filter(cd -> cd.getProductSpecificationCharacteristic().getCode()
							.equals(BASE_PKG_PROD_CODE_MAP.get(typeCode)))
					.findFirst().map(CharacteristicDetail::getValue).orElse(null);
		}

		return productCode;
	}

	private String getBasePackageDisplayName(ComponentDetail componentDetail) {
		String productCode = null;
		String typeCode = getComponentTypeCode(componentDetail);

		if (componentDetail.getCharacteristicDetails() != null) {
			productCode = componentDetail.getCharacteristicDetails().stream()
					.filter(cd -> cd.getProductSpecificationCharacteristic().getCode()
							.equals(BASE_PKG_PROD_CODE_MAP.get(typeCode)))
					.findFirst().map(CharacteristicDetail::getValueForDisplay).orElse(null);
		}

		return productCode;
	}

	private boolean isBasePackage(BillingOffer billingOffer, ComponentDetail componentDetail) {
		String typeCode = getComponentTypeCode(componentDetail);
		String code = billingOffer.getProductSpecificationPricing().getChildPricingSchema().getType().getCode();

		return "PR".equals(code) && BASE_PACKAGE_TYPE_CODES.contains(typeCode);
	}

	private boolean isAddon(BillingOffer billingOffer, ComponentDetail componentDetail) {
		String typeCode = getComponentTypeCode(componentDetail);
		String billingTypeCode = getBillingOfferTypeCode(billingOffer);
		return ADDON_TYPE_CODES.contains(typeCode) && "AD".equals(billingTypeCode);
	}

	private boolean isPickService(ComponentDetail componentDetail) {
		String typeCode = getComponentTypeCode(componentDetail);

		return PICK_SERVICE_TYPE_CODE.equals(typeCode);
	}

	private String getLosg(CGProduct product, ComponentDetail componentDetail) {
		String losg = null;

		if (DTVS_LINE_OF_BUSINESS.equalsIgnoreCase(product.getLineOfBusiness().get(0)))
			return LosgType.DTVS.toString();

		String typeCode = getComponentTypeCode(componentDetail);

		if (HSIA_TYPE_CODES.contains(typeCode)) {
			losg = LosgType.HSIA.toString();
		} else if (IPTV_TYPE_CODES.contains(typeCode)) {
			losg = LosgType.IPTV.toString();
		} else if (VOIP_TYPE_CODES.contains(typeCode)) {
			losg = LosgType.VOIP.toString();
		}

		return losg;
	}

	private String getComponentTypeCode(ComponentDetail componentDetail) {
		String typeCode = null;

		List<ContainedProduct> containedProducts = Optional
				.ofNullable(componentDetail.getParentProductSpecContainment())
				.map(ParentProductSpecContainment::getContainedProducts).orElse(null);

		if (containedProducts != null && !containedProducts.isEmpty()) {
			typeCode = containedProducts.get(0).getTypeCode();

			if (typeCode == null && containedProducts.get(0).getContainedProducts() != null
					&& !containedProducts.get(0).getContainedProducts().isEmpty()) {
				typeCode = containedProducts.get(0).getContainedProducts().get(0).getTypeCode();
			}
		}

		return typeCode;
	}

	private String getBillingOfferTypeCode(BillingOffer billingOffer) {
		String typeCode = null;

		ChildPricingSchema childPricingSchema = Optional.ofNullable(billingOffer.getProductSpecificationPricing())
				.map(ProductSpecificationPricing::getChildPricingSchema).orElse(null);

		if (childPricingSchema != null) {
			typeCode = childPricingSchema.getType().getCode();
		}

		return typeCode;
	}

}