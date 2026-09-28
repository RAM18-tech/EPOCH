package com.dtv.dcp.epoch.repository;

import com.dtv.dcp.epoch.model.customergraph.CustomerCoupons;
import com.dtv.dcp.epoch.model.customergraph.CustomerCouponsResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerCouponRepositoryImpl implements CustomerCouponRepository {

	private static final Logger log = LoggerFactory.getLogger(CustomerCouponRepositoryImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Value("${postgres.campaign_coupon_ban_data}")
	private String campaign_coupon_ban_data;

	@Override
	public <T> T getCustomerCoupons(String accountNum) {
		log.info("Start of CustomerCouponRepositoryImpl.getCustomerCoupons() method..");
		StringBuilder query = new StringBuilder("SELECT * FROM ");
		query.append(campaign_coupon_ban_data);
		query.append(" WHERE ban = ?");
		query.append(";");
		log.info("check coupons SQL QUERY:::{}", query);
		T customerCouponsResults = (T) jdbcTemplate.query(query.toString(), new Object[] { accountNum },
				new BeanPropertyRowMapper<>(CustomerCoupons.class));

		log.info("End of CustomerCouponRepositoryImpl.getCustomerCoupons() method..");
		return customerCouponsResults;
	}
}