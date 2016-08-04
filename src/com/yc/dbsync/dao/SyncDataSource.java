package com.yc.dbsync.dao;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.DataSource;

import com.alibaba.druid.pool.DruidDataSourceFactory;

public class SyncDataSource {
//	 private static final Logger logger =Logger.getLogger(SyncDataSource.class);
	private static final Lock dsLock = new ReentrantLock();
	private static final Lock cdsLock = new ReentrantLock();

	private static DataSource dataSource;
	private static DataSource configDataSource;

	public static DataSource getConfigDataSource() {
		cdsLock.lock();
		try {
			if (configDataSource == null) {
				String configFile = System.getProperty("CONFIG_PATH") + "/conf/configdruid.properties";
				configDataSource = getDataSource(configFile);
			}
		} finally {
			cdsLock.unlock();
		}

		return configDataSource;

	}

	public static DataSource getDataSource() {
		dsLock.lock();
		try {
			if (dataSource == null) {
				String configFile = System.getProperty("CONFIG_PATH") + "/conf/druid.properties";
				dataSource = getDataSource(configFile);
			}
		} finally {
			dsLock.unlock();
		}
		return dataSource;
	}

	private static DataSource getDataSource(String configFile) {
		DataSource _dataSource = null;
		Properties properties = new Properties();
		File file = new File(configFile);
		InputStream inputStream = null;
		try {
			inputStream = new BufferedInputStream(new FileInputStream(file));
			properties.load(inputStream);
			_dataSource = DruidDataSourceFactory.createDataSource(properties);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return _dataSource;
	}

	public static Connection getConnection(String dbname) throws SQLException {
		Connection conn = getDataSource().getConnection();
		conn.setCatalog(dbname);
		// logger.debug(defaultDB+" 打开一次连接: "+conn);
		return conn;
	}

	public static void close(ResultSet rst, Statement stmt, Connection conn) {
		// logger.debug("关闭一次连接:"+conn);
		if (rst != null) {
			try {
				rst.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		DataSource ds = getDataSource();
		System.out.println(ds.getConnection().toString());
	}

}
