package com.dtv.dcp.epoch.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import org.springframework.jdbc.core.RowMapper;

import com.dtv.dcp.epoch.model.ct.response.EvergentContract;

public class DmpDataMapper implements RowMapper<EvergentContract>{

	public EvergentContract mapRow(ResultSet rs, int rowNum) throws SQLException {
		EvergentContract data = new EvergentContract();
		data.setBulked_pkg(Objects.nonNull(rs.getString("Bulked_pkg")) ?  rs.getString("Bulked_pkg") :"");
		data.setProg_addons(Objects.nonNull(rs.getString("prog_addons")) ? rs.getString("prog_addons"):"");
		data.setUnlimited_cdvr(rs.getBoolean("unlimited_cdvr"));
		data.setFree_unlimited_cdvr(rs.getBoolean("free_unlimited_cdvr"));
		data.setAddtl_devices(Objects.nonNull(rs.getInt("addtl_devices")) ? rs.getInt("addtl_devices") :0);
		data.setAmt_of_addtl_free_devices(Objects.nonNull(rs.getInt("amt_of_addtl_free_devices")) ?rs.getInt("amt_of_addtl_free_devices") :0);
		data.setTotal_devices_per_courtesy_acct(Objects.nonNull(rs.getString("total_devices_per_courtesy_acct")) ?rs.getInt("total_devices_per_courtesy_acct") :-1);
		data.setTotal_devices_per_bcomp_acct(Objects.nonNull(rs.getString("total_devices_per_bcomp_acct")) ?rs.getInt("total_devices_per_bcomp_acct") :-1);
		data.setTotal_devices_per_showroom_acct(Objects.nonNull(rs.getString("total_devices_per_showroom_acct")) ?rs.getInt("total_devices_per_showroom_acct") :-1);
	    return data;
	   }
	
}
