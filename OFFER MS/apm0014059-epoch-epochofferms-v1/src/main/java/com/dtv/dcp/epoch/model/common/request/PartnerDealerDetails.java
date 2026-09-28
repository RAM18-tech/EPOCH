package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;

public class PartnerDealerDetails  implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3939406263458334340L;
	// new addition
	private String partnerDealerCode1;
	private String partnerDealerCode2;
	private String partnerLocationId;

	public String getPartnerDealerCode1() {
		return partnerDealerCode1;
	}

	public void setPartnerDealerCode1(String partnerDealerCode1) {
		this.partnerDealerCode1 = partnerDealerCode1;
	}

	public String getPartnerDealerCode2() {
		return partnerDealerCode2;
	}

	public void setPartnerDealerCode2(String partnerDealerCode2) {
		this.partnerDealerCode2 = partnerDealerCode2;
	}

	public String getPartnerLocationId() {
		return partnerLocationId;
	}

	public void setPartnerLocationId(String partnerLocationId) {
		this.partnerLocationId = partnerLocationId;
	}

}
