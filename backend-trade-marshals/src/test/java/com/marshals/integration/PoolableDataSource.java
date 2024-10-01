package com.marshals.integration;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Objects;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * This simple DataSource uses the Connection pooling functionality defined by
 * the Apache dbcp2 project.
 * The Connection pool properties are defined in cp.properties.
 * 
 * The database properties are defined in db.properties.
 * 
 * The PoolableDataSource stores the PoolableConnections in a ThreadLocal.
 * This insures that all getConnection requests made from methods in a thread 
 * will receive the same Connection.
 * This insures that the same connection is used throughout a transaction.
 * This is essential for transaction support.
 * 
 * @author ROI Instructor
 *
 */
public class PoolableDataSource implements DataSource {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private PoolingDataSource<PoolableConnection> poolingDataSource;
	private ThreadLocal<Connection> threadLocal;
	
	public PoolableDataSource() throws IOException {
		threadLocal = new ThreadLocal<>();	
		poolingDataSource = createPoolingDataSource();
	}

	/**
	 * The client will call this method to obtain a database Connection.
	 * The Connections are stored in a ThreadLocal.
	 * This insures that all getConnection requests made from a thread 
	 * will receive the same Connection.
	 * This is essential for transaction support.
	 * 
	 * @throws SQLException 
	 */
	@Override
	public Connection getConnection() throws SQLException {
		// Retrieve the Connection stored in threadLocal if it is there
		Connection connection = threadLocal.get();
		
		// If there is no Connection in threadLocal, get one from the pool
		if (Objects.isNull(connection)) {
			// Get a Connection from the pool
			connection = poolingDataSource.getConnection();
			
			// Store the Connection in threadLocal
			threadLocal.set(connection);
		}
		// If the Connection in threadLocal is closed, get a new one
		else if (connection.isClosed()) {
			// Remove the old Connection from threadLocal
			threadLocal.remove();
			
			// Get a Connection from the pool
			connection = poolingDataSource.getConnection();
			
			// Store the Connection in threadLocal
			threadLocal.set(connection);			
		}
		
		return connection;
	}
	

	/**
	 * The shutdown() method should be called to insure that the database Connection
	 * is closed.
	 */
	public void shutdown() {
		try {
			// Clean up threadLocal
			Connection connection = threadLocal.get();
			if (!Objects.isNull(connection)) {
				connection.close();
				threadLocal.remove();
			}
			
			// Close the poolingDataSource
			poolingDataSource.close();
		} catch (SQLException e) {
			logger.error("Error closing database connection", e);
		}
	}

	/**
	 * This method is based on sample code from Apache Commons
	 * @param dburl			the database url
	 * @param dbUserName	the database user name
	 * @param dbPassword	the database user password
	 * @return				the PoolingDataSource
	 * @throws IOException 
	 */
	private PoolingDataSource<PoolableConnection> createPoolingDataSource() throws IOException {
		PoolingDataSource<PoolableConnection> dataSource = null;
		 
		Properties props  = readPropertiesFile("db.properties");
		String dbUrl      = props.getProperty("db.url");
		String dbUserName = props.getProperty("db.username");
		String dbPassword = props.getProperty("db.password");

		Properties cpProps = readPropertiesFile("cp.properties");
		String minIdle     = cpProps.getProperty("cp.minIdle");
		String maxIdle     = cpProps.getProperty("cp.maxIdle");
		String maxTotal    = cpProps.getProperty("cp.maxTotal");
		
        GenericObjectPoolConfig<PoolableConnection> config = 
        		new GenericObjectPoolConfig<>();
        config.setMaxIdle(Integer.parseInt(maxIdle));
		config.setMinIdle(Integer.parseInt(minIdle));
		config.setMaxTotal(Integer.parseInt(maxTotal));	
		
        // Create a ConnectionFactory used to create Connections.
        // We'll use the DriverManagerConnectionFactory
        ConnectionFactory connectionFactory =
        	new DriverManagerConnectionFactory(dbUrl, dbUserName, dbPassword);

        // Create the PoolableConnectionFactory, which wraps
        // the "real" Connections created by the ConnectionFactory with
        // the classes that implement the pooling functionality.
        PoolableConnectionFactory poolableConnectionFactory =
            new PoolableConnectionFactory(connectionFactory, null);
        
        // The ObjectPool serves as the actual pool of connections.
        // We'll use a GenericObjectPool instance, although
        // any ObjectPool implementation will suffice.
        ObjectPool<PoolableConnection> connectionPool =
                new GenericObjectPool<>(poolableConnectionFactory, config);
        
        // Set the factory's pool property to the owning pool
        poolableConnectionFactory.setPool(connectionPool);
        
        // Finally, create the PoolingDataSource itself,
        // passing in the object pool we created.
        dataSource = new PoolingDataSource<>(connectionPool);
        
		return dataSource;
	}
	
	/****** DataSource methods that we are not using *****/
	@Override
	public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return null;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return null;
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return null;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {

	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {

	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return 0;
	}

	/****** Utility Methods *****/
	private Properties readPropertiesFile(String file) throws IOException {
		Properties properties = new Properties();
		properties.load(this.getClass()
				  .getClassLoader()
				  .getResourceAsStream(file));

		return properties;
	}
}

