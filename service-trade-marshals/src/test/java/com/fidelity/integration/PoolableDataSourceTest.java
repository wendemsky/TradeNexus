package com.fidelity.integration;

import java.sql.Connection;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class PoolableDataSourceTest {
	static PoolableDataSource dataSource;
	Connection connection;
	
	@BeforeAll
	static void init() throws Exception {
		dataSource = new PoolableDataSource();
	}

	@AfterAll
	static void cleanup() throws Exception {
		dataSource.shutdown();
	}

	@BeforeEach
	void setUp() throws Exception {
		connection = dataSource.getConnection();
	}

	@AfterEach
	void tearDown() throws Exception {
		connection.close();
	}
	
	@Test
	void shouldCreateDataSourceObject() {
		assertNotNull(dataSource);
	}

	@Test
	void shouldCreateConnectionObject() {
		assertNotNull(connection);
	}
}
