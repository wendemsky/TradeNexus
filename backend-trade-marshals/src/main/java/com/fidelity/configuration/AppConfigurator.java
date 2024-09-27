package com.fidelity.configuration;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * AppConfigurator is a custom bean factory.
 * 
 * @author ROI Instructor Team
 */
@Configuration
public class AppConfigurator {
	/**
	 * This method creates a Logger that can be autowired in other classes:{@code
	 *    @Autowired 
	 *    private Logger logger;
	 *    ...
	 *    logger.debug("arg = " + arg);
	 }*/
	@Bean
	@Scope("prototype")
	public Logger createLogger(InjectionPoint ip) {
	    Class<?> classThatWantsALogger = ip.getField().getDeclaringClass();
	    return LoggerFactory.getLogger(classThatWantsALogger);
	}
	
	/**
	 * This method creates a JdbcTemplate that can be autowired in other classes
	 * (in this application, it's required only by unit tests):{@code
	 *    @Autowired 
	 *    private JdbcTemplate jdbcTemplate;
	 *    ...
	 *    var query = "select max(deptno) from dept";
	 *    var maxDeptId = jdbcTemplate.queryForObject(query, Integer.class);
	 }*/
	@Bean
	public JdbcTemplate createJdbcTemplate(@Qualifier("datasource") DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}
}
