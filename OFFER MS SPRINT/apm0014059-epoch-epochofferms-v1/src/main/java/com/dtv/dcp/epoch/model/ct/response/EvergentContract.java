package com.dtv.dcp.epoch.model.ct.response;

public class EvergentContract {

	private String bulked_pkg;
	
	private String prog_addons;
	
	private boolean unlimited_cdvr;
	
	private boolean free_unlimited_cdvr;
	
	private int amt_of_addtl_free_devices;
	
	private int addtl_devices;
	
	private int total_devices_per_courtesy_acct;
	
	private int total_devices_per_bcomp_acct;
	
	private int total_devices_per_showroom_acct;


	public String getBulked_pkg() {
		return bulked_pkg;
	}

	public void setBulked_pkg(String bulked_pkg) {
		this.bulked_pkg = bulked_pkg;
	}

	public String getProg_addons() {
		return prog_addons;
	}

	public void setProg_addons(String prog_addons) {
		this.prog_addons = prog_addons;
	}

	public boolean isUnlimited_cdvr() {
		return unlimited_cdvr;
	}

	public void setUnlimited_cdvr(boolean unlimited_cdvr) {
		this.unlimited_cdvr = unlimited_cdvr;
	}
	
	public boolean isFree_unlimited_cdvr() {
		return free_unlimited_cdvr;
	}

	public void setFree_unlimited_cdvr(boolean free_unlimited_cdvr) {
		this.free_unlimited_cdvr = free_unlimited_cdvr;
	}

	public int getAmt_of_addtl_free_devices() {
		return amt_of_addtl_free_devices;
	}

	public void setAmt_of_addtl_free_devices(int amt_of_addtl_free_devices) {
		this.amt_of_addtl_free_devices = amt_of_addtl_free_devices;
	}

	public int getAddtl_devices() {
		return addtl_devices;
	}

	public void setAddtl_devices(int addtl_devices) {
		this.addtl_devices = addtl_devices;
	}

	public int getTotal_devices_per_courtesy_acct() {
		return total_devices_per_courtesy_acct;
	}

	public void setTotal_devices_per_courtesy_acct(int total_devices_per_courtesy_acct) {
		this.total_devices_per_courtesy_acct = total_devices_per_courtesy_acct;
	}

	public int getTotal_devices_per_bcomp_acct() {
		return total_devices_per_bcomp_acct;
	}

	public void setTotal_devices_per_bcomp_acct(int total_devices_per_bcomp_acct) {
		this.total_devices_per_bcomp_acct = total_devices_per_bcomp_acct;
	}

	public int getTotal_devices_per_showroom_acct() {
		return total_devices_per_showroom_acct;
	}

	public void setTotal_devices_per_showroom_acct(int total_devices_per_showroom_acct) {
		this.total_devices_per_showroom_acct = total_devices_per_showroom_acct;
	}
	

}
