package com.dtv.dcp.epoch.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.base.Preconditions;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DataSourceConfig {

	@Value("${postgres.url}")
	private String url;

	@Value("${postgres.userName}")
	private String userName;

	@Value("${postgres.driverClassName}")
	private String driverClassName;

	@Value("${spring.datasource.hikari.minimumIdle}")
	private Integer minIdle;

	@Value("${spring.datasource.hikari.maximum-pool-size}")
	private Integer maxPoolSize;

	@Value("${spring.datasource.hikari.connection-timeout}")
	private Integer connectionTimeout;

	@Value("${spring.datasource.hikari.max-lifetime}")
	private Integer maxConnectionLifeTime;
	
	/** The proxy auth host. */
	@Value("${idp.com.att.proxyAuthHost}")
	private String proxyHost;
	
	/** The proxy port. */
	@Value("${idp.com.att.proxyAuthPort}")
	private int proxyPort;
	
	/** The proxy port. */
	@Value("${dcp.database.proxyEnabled:false}")
	private String isProxyRequired;
	
	@Autowired
	SecretManagementClient secretManagementClient;

	@Bean
	public DataSource getDataSource() {
		final HikariDataSource hikariDataSource = new HikariDataSource();
		Properties props = new Properties();
		hikariDataSource.setDriverClassName(Preconditions.checkNotNull(driverClassName));
		hikariDataSource.setJdbcUrl(Preconditions.checkNotNull(url));
		hikariDataSource.setUsername(Preconditions.checkNotNull(userName));
		hikariDataSource.setPassword(Preconditions.checkNotNull(secretManagementClient.retrieveCredentials().get("EPOCH_POSTGRES_PASSWORD")));
		props.setProperty("sendStringParametersAsUnicode", "false");
		hikariDataSource.setDataSourceProperties(props);
		hikariDataSource.setMinimumIdle(minIdle);
		hikariDataSource.setMaximumPoolSize(maxPoolSize);
		hikariDataSource.setConnectionTimeout(connectionTimeout);
		hikariDataSource.setMaxLifetime(maxConnectionLifeTime);
		hikariDataSource.setAutoCommit(true);

		// Proxy settings
        Properties dataSourceProperties = new Properties();
        if("enabled".equalsIgnoreCase(isProxyRequired)) {
            dataSourceProperties.setProperty("socksProxyHost",proxyHost );
            dataSourceProperties.setProperty("socksProxyPort",String.valueOf(proxyPort));
  		}
        hikariDataSource.setDataSourceProperties(dataSourceProperties);
        
		return hikariDataSource;
	}

	@Bean
	public JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}
}