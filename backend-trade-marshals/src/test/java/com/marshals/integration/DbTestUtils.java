package com.marshals.integration;
/*
 * DbTestUtils.java - utility functions for database integration tests.
 */

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;


public class DbTestUtils
{

	public static int countRowsInTable(Connection connection, String table) throws SQLException {
		int count = 0;
		String sql = "Select Count(0) from " + table;
		ResultSetHandler<BigDecimal> rsHandler = new ScalarHandler<>("count(0)");
		QueryRunner queryRunner = new QueryRunner();
		BigDecimal dcount = queryRunner.query(connection, sql, rsHandler);
		count = dcount.intValue();
		
		return count;
	}
	
	public static int countRowsInTableWhere(Connection connection, String table, String where) throws SQLException {
		int count = 0;
		String sql = "Select Count(0) from " + table + " where " + where;
		ResultSetHandler<BigDecimal> rsHandler = new ScalarHandler<>("count(0)");
		QueryRunner queryRunner = new QueryRunner();
		BigDecimal dcount = queryRunner.query(connection, sql, rsHandler);
		count = dcount.intValue();
		
		return count;
	}			

//	/**
//	 * Implements some basic sanity checking for an Employee
//	 * 
//	 * @param employee
//	 * @return true if all conditions are met
//	 */
//
//	public static boolean validateEmployee(Employee employee) {
//		boolean valid = 
//				employee.getEmpNumber() > 0
//			 && Objects.nonNull(employee.getEmpName())
//			 && Objects.nonNull(employee.getJob())
//			 && employee.getMgrNumber() >= 0
//			 && Objects.nonNull(employee.getHireDate())
//			 && (Objects.nonNull(employee.getSalary()) && employee.getSalary().compareTo(BigDecimal.ZERO) > 0)
//			 && (employee.getComm() == null || employee.getComm().compareTo(BigDecimal.ZERO) > 0)
//			 && employee.getDeptNumber() > 0;
//		
//		return valid;
//	}
	
}
