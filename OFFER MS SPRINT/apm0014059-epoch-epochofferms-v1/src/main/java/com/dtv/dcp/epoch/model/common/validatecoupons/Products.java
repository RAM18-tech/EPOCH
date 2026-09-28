package com.dtv.dcp.epoch.model.common.validatecoupons;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.ct.generic.GenericLocaleBase;
import com.dtv.dcp.epoch.model.ct.generic.GenericTypeIdBase;

public class Products implements Serializable {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;

		/** The id. */
		private String  id;
		
		/** The code. */
		private String code;
		
		private String benefitContinuation;

		/** The productType. */
		private GenericTypeIdBase productType ;
		
		/** The typeId. */
		private String typeId ;
		
		/** The productType. */
		private GenericLocaleBase name ;
		
		/** The productType. */
		private GenericLocaleBase description ;
		
		/** The categories. */
		private List<GenericTypeIdBase> categories;
		
		/** The variants. */
		private List<Variant> variants;

		/** The lead offer. */
		private Boolean leadOffer;
		
		/** The status. */
		//private String status ;
		
		public String getBenefitContinuation() {
			return benefitContinuation;
		}

		public void setBenefitContinuation(String benefitContinuation) {
			this.benefitContinuation = benefitContinuation;
		}
		
		/**
		 * @return the id
		 */
		public String getId() {
			return id;
		}

		/**
		 * @param id the id to set
		 */
		public void setId(String id) {
			this.id = id;
		}

		/**
		 * @return the code
		 */
		public String getCode() {
			return code;
		}

		/**
		 * @param code the code to set
		 */
		public void setCode(String code) {
			this.code = code;
		}

	/*	*//**
		 * @return the status
		 *//*
		public String getStatus() {
			return status;
		}

		*//**
		 * @param status the status to set
		 *//*
		public void setStatus(String status) {
			this.status = status;
		}*/

		/**
		 * @return the productType
		 */
		public GenericTypeIdBase getProductType() {
			return productType;
		}

		/**
		 * @param productType the productType to set
		 */
		public void setProductType(GenericTypeIdBase productType) {
			this.productType = productType;
		}

		/**
		 * @return the name
		 */
		public GenericLocaleBase getName() {
			return name;
		}

		/**
		 * @param name the name to set
		 */
		public void setName(GenericLocaleBase name) {
			this.name = name;
		}

		/**
		 * @return the description
		 */
		public GenericLocaleBase getDescription() {
			return description;
		}

		/**
		 * @param description the description to set
		 */
		public void setDescription(GenericLocaleBase description) {
			this.description = description;
		}

		/**
		 * @return the categories
		 */
		public List<GenericTypeIdBase> getCategories() {
			return categories;
		}

		/**
		 * @param categories the categories to set
		 */
		public void setCategories(List<GenericTypeIdBase> categories) {
			this.categories = categories;
		}

		/**
		 * @return the variants
		 */
		public List<Variant> getVariants() {
			return variants;
		}

		/**
		 * @param variants the variants to set
		 */
		public void setVariants(List<Variant> variants) {
			this.variants = variants;
		}
		

		/**
		 * Gets the leadOffer.
		 *
		 * @return boolean
		 */
		public Boolean isLeadOffer() {
			return leadOffer;
		}

		/**
		 * Sets the leadOffer.
		 *
		 * @param leadOffer the new lead offer
		 */
		public void setLeadOffer(Boolean leadOffer) {
			this.leadOffer = leadOffer;
		}


		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("ProductObj [id=");
			builder.append(id);
			builder.append(", code=");
			builder.append(code);
	/*		builder.append(", status=");
			builder.append(status);*/
			builder.append(", productType=");
			builder.append(productType);
			builder.append(", name=");
			builder.append(name);
			builder.append(", description=");
			builder.append(description);
			builder.append(", categories=");
			builder.append(categories);
			builder.append(", variants=");
			builder.append(variants);
			builder.append("]");
			return builder.toString();
		}

		public String getTypeId() {
			return typeId;
		}

		public void setTypeId(String typeId) {
			this.typeId = typeId;
		}
	}

