package com.fidelity.integration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionManager {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private DataSource dataSource;
	
	public TransactionManager(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void startTransaction() {
		try {
			Connection connection = dataSource.getConnection();
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			throw new DatabaseException("Unable to begin a transaction", e);
		}
		
	}

	public void rollbackTransaction() {
		Connection connection;
		try {
			connection = dataSource.getConnection();
			connection.rollback();
		} catch (SQLException e) {
			throw new DatabaseException("Unable to rollback transaction", e);
		}
		finally {
			shutdown();
		}
	}

	public void commitTransaction() {
		Connection connection;
		try {
			connection = dataSource.getConnection();
			connection.commit();
		} catch (SQLException e) {
			throw new DatabaseException("Unable to rollback transaction", e);
		}
		finally {
			shutdown();
		}
	}
	
	/**
	 * The shutdown() method should be called to insure that the database Connection
	 * is closed.
	 * Calling close() will return the Connection to its Connection pool.
	 * @throws  
	 */
	public void shutdown() {
		try {
			Connection connection = dataSource.getConnection();
			if (!Objects.isNull(connection)) {
				connection.close();
			}
		} catch (SQLException e) {
			logger.error("Error closing database connection", e);
		}
	}
}
