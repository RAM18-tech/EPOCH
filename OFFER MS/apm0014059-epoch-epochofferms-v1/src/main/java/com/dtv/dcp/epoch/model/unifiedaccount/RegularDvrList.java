package com.dtv.dcp.epoch.model.unifiedaccount;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The class RegularDvrList
 * @author vd7621
 */
public class RegularDvrList  implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The equipmentModel. */
	@JsonProperty("equipmentModel")
	private String equipmentModel;

	/** The equipmentDescription. */
	@JsonProperty("equipmentDescription")
	private String equipmentDescription;

	/** The manufacturer. */
	@JsonProperty("manufacturer")
	private String manufacturer;

	/**
	 * @return the equipmentModel
	 */
	public String getEquipmentModel() {
		return equipmentModel;
	}

	/**
	 * @param equipmentModel the equipmentModel to set
	 */
	public void setEquipmentModel(String equipmentModel) {
		this.equipmentModel = equipmentModel;
	}

	/**
	 * @return the equipmentDescription
	 */
	public String getEquipmentDescription() {
		return equipmentDescription;
	}

	/**
	 * @param equipmentDescription the equipmentDescription to set
	 */
	public void setEquipmentDescription(String equipmentDescription) {
		this.equipmentDescription = equipmentDescription;
	}

	/**
	 * @return the manufacturer
	 */
	public String getManufacturer() {
		return manufacturer;
	}

	/**
	 * @param manufacturer the manufacturer to set
	 */
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RegularDvrList [equipmentModel=" + equipmentModel + ", equipmentDescription=" + equipmentDescription
				+ ", manufacturer=" + manufacturer + "]";
	}

}
