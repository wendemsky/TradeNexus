package com.marshals.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"classpath:schema.sql", "classpath:data.sql"},
     executionPhase = ExecutionPhase.AFTER_TEST_METHOD) 
class TradeControllerE2ETest {
	@Autowired
	private TestRestTemplate restTemplate; // for executing rest endpoints

	@Autowired
	private JdbcTemplate jdbcTemplate;  // for executing SQL queries
	
}
