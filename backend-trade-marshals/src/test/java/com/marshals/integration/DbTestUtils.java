/*
 * DbTestUtils.java - utility functions for database integration tests.
 */

package com.marshals.integration;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

public enum DbTestUtils {
	INSTANCE;
	
	private static final String DB_URL = "jdbc:oracle:thin:@//localhost:1521/xepdb1";
	private static final String USER = "MARSH";
	private static final String PASSWORD = "MARSH";
	
	private static final String SQL_SCRIPT = "sql/marshals_schema_creation.sql";

	private Connection connection;

	/**
	 * Re-run the database initialization script.
	 *
	 * Alternatively, for simple schemas, we could drop all rows in the required tables, 
	 * then insert test data:
	 *     Statement stmt = connection.createStatement();
	 *     stmt.executeUpdate("delete from emp");
	 *     stmt.executeUpdate("insert into emp (empno,ename,job,...) values (7369,'SMITH','CLERK',...)");
	 *       ...
	 *     stmt.close();
	 */
	public void initDb() {
		try {
			if (connection == null) {
				connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
			}
			// run DB scripts

			ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
			// Only pure SQL scripts allowed - PL/SQL statements must be removed 
			resourceDatabasePopulator.setContinueOnError(true);
			resourceDatabasePopulator.addScript(new FileSystemResource(SQL_SCRIPT));
			resourceDatabasePopulator.populate(connection);
			Thread.sleep(500);
		} 
		catch (Exception e) {
			close();
			throw new RuntimeException(e);
		}
		// if no errors, keep connection open
	}

	public synchronized JdbcTemplate initJdbcTemplate() {
		try {
			if (connection == null) {
				connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
			}
			
			return new JdbcTemplate(new SingleConnectionDataSource(connection, true));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized void close() {
		if (connection != null) {
			try {
				connection.close();
				connection = null;
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static boolean areEqual(Map<String, Object> first, Map<String, Object> second) {
		if (first.size() != second.size()) {
			throw new SizesAreDifferentException("first map size is " + first.size() + 
												 ", second map size is " + second.size());
		}
		for (String key: first.keySet()) {
			Object firstValue = first.get(key);
			Object secondValue = second.get(key);
			if (firstValue.getClass() != secondValue.getClass()) {
				if (secondValue instanceof BigDecimal) {
					secondValue = ((BigDecimal) secondValue).intValue();
				}
				else {
					throw new TypesAreDifferentException("for column " + key + ", expected " 
						+ firstValue.getClass() + " but was " + secondValue.getClass());
				}
			}
			if (!firstValue.equals(secondValue)) {
				throw new ValuesAreDifferentException("for column " + key + ", expected " 
						+ firstValue + " but was " + secondValue);
			}
		}
		return true;
	} 
	
	public static boolean areEqual(List<Map<String, Object>> firstList, 
								   List<Map<String, Object>> secondList) {
		if (firstList.size() != secondList.size()) {
			throw new SizesAreDifferentException("first list size is " + firstList.size() + 
												 ", second list size is " + secondList.size());
		}
		for (int i = 0; i < firstList.size(); i++) {
			areEqual(firstList.get(i), secondList.get(i)); // throws an exception if maps are different
		}
		return true;
	} 
		
	public static int intValue(Object bigDecimal) {
		return bigDecimal != null ? ((BigDecimal) bigDecimal).intValue() : 0;
	}
	
	public static double doubleValue(Object bigDecimal) {
		return bigDecimal != null ? ((BigDecimal) bigDecimal).doubleValue() : 0;
	}
	
	public static String formatTimestamp(Object timestamp) {
		Date date = new Date(((Timestamp) timestamp).getTime());
		return new SimpleDateFormat("dd-MMM-yyyy").format(date).toUpperCase();		
	}
}

class SizesAreDifferentException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public SizesAreDifferentException(String msg) {
		super(msg);
	}
}

class TypesAreDifferentException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public TypesAreDifferentException(String msg) {
		super(msg);
	}
}

class ValuesAreDifferentException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public ValuesAreDifferentException(String msg) {
		super(msg);
	}
}