package com.yc.dbsync.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.yc.dbsync.SyncContans.FailReason;
import com.yc.dbsync.dto.SyncRowDTO;
import com.yc.dbsync.model.SyncConfig;
import com.yc.dbsync.model.SyncResult.FailResult;
import com.yc.dbsync.model.SyncResult.SuccessResult;
import com.yc.common.JsonTool;

import com.yc.dbsync.model.SyncRow;

public class DBSyncDao {
	private static Logger logger = Logger.getLogger(DBSyncDao.class);
	private static Map<String, Timestamp> lastCheckTimes = new HashMap<>();
	private final static long TIME_INTERVAL = 1000 * 60 * 60 * 12; // 程序新启动要检查12个小时前的所有更新
	private final static String BASE_INSERT_COLUMN = " (`sync_id`,`sync_state`,`sync_create_time`,`sync_time`";
	private final static String BASE_INSERT_COLUMN_VALUE = ") VALUE(?,?,?,?";
	private final static String BASE_UPDATE_COLUMN = " SET `sync_state`=? ,`sync_update_time`=? ,`sync_time`=?";
	// 数据库名.表名 limit 一次不取全部
	private final static String QUERY_SYNCROW_SUFFIX_FORMAT = " ,`sync_id`,`sync_state`,`sync_create_time`,`sync_time`,`sync_update_time` FROM %s.%s WHERE sync_state in (1,3) limit  0,%d";
	private final static String QUERY_SYNCROW_FORMAT = "SELECT * FROM `%s`.`%s` WHERE sync_state in (1,3) limit  0,%d";
	private final static String UPDATE_SYNCROW_SUCESS = "UPDATE `%s` SET `sync_state`=?,`sync_time`=? where `sync_id`=?";
	private final static String UPDATE_SYNCROW_FAIL = "UPDATE `%s` SET `sync_time`=? where `sync_id`=?";
	private final static String UPDATE_SYNCROW_FAIL_DATA_EXISTS = "UPDATE `%s` SET `sync_time`=?,`sync_state`=" + SyncRow.STATE_NORMAL + " where `sync_id`=?";

	private final static String QUERY_UPDATED_TABLES = "SELECT * FROM `sync_table_notic` WHERE update_time > ?";


	/**
	 * 同步插入
	 * 
	 * @param row
	 * @return
	 * @throws SQLException
	 */
	public boolean insert(SyncRow row) throws SQLException {
		Connection conn = SyncDataSource.getConnection(row.getToDb());
		if (conn == null) {
			logger.error("lose connection");
			return false;
		}
		PreparedStatement pstmt = null;
		int result;
		try {
			StringBuffer sql = new StringBuffer("insert into");
			sql.append(" ").append(row.getTableName()).append(BASE_INSERT_COLUMN);
			Map<String, Object> columns = row.getColumns();
			// 同步的例是否包含teamid
			for (String key : columns.keySet()) {
				sql.append(",`").append(key).append("`");
			}
			sql.append(BASE_INSERT_COLUMN_VALUE);
			for (int i = 0; i < columns.size(); i++) {
				sql.append(",?");
			}
			sql.append(")");
			String _sql = sql.toString();
			logger.debug("sql " + _sql);
			logger.debug("param " + JsonTool.toString(columns));
			logger.debug(columns.size());
			pstmt = conn.prepareStatement(_sql);
			// `sync_id`,`sync_state`,`sync_create_time`,`sync_time`
			pstmt.setObject(1, row.getSyncId());
			pstmt.setObject(2, SyncRow.STATE_NORMAL);
			pstmt.setObject(3, row.getSyncCreateTime());
			pstmt.setObject(4, row.getSyncTime());
			int index = 5;
			for (String key : columns.keySet()) {
				pstmt.setObject(index++, columns.get(key));
			}
			result = pstmt.executeUpdate();
			logger.debug("sync_id:" + row.getSyncId() + " result:" + result);
		} finally {
			SyncDataSource.close(null, pstmt, conn);
		}
		return result > 0;
	}

	/**
	 * 同步更新
	 * 
	 * @param row
	 * @return
	 * @throws SQLException
	 */
	public boolean update(SyncRow row) throws SQLException {
		Map<String, Object> columnMap = row.getColumns();
		if (columnMap.isEmpty()) {
			logger.error("execute update but columns is null sync_id is " + row.getSyncId());
			return false;
		}

		Connection conn = SyncDataSource.getConnection(row.getToDb());
		if (conn == null) {
			logger.error("lose connection");
			return false;
		}
		PreparedStatement pstmt = null;
		int result;
		try {
			StringBuffer sql = new StringBuffer("UPDATE `");
			sql.append(row.getTableName()).append("`").append(BASE_UPDATE_COLUMN);

			for (String column : columnMap.keySet()) {
				sql.append(" ,`").append(column).append("`=?");
			}
			sql.append(" where sync_id=?");
			// set `sync_state`=? ,`sync_update_time`=? ,`sync_time`=?
			String _sql = sql.toString();

			logger.debug(_sql);
			logger.debug("param " + JsonTool.toString(columnMap));
			pstmt = conn.prepareStatement(_sql);
			pstmt.setObject(1, SyncRow.STATE_NORMAL);
			pstmt.setObject(2, row.getSyncUpdateTime());
			pstmt.setObject(3, row.getSyncTime());
			int index = 4;
			for (String column : columnMap.keySet()) {
				pstmt.setObject(index++, columnMap.get(column));
			}
			pstmt.setObject(index, row.getSyncId());
			result = pstmt.executeUpdate();
			logger.debug("sync_id:" + row.getSyncId() + " result:" + result);
		} finally {
			SyncDataSource.close(null, pstmt, conn);
		}
		return result > 0;
	}

	/**
	 * 获取同步数据(服务端)
	 * 
	 * @param config
	 * @param teamId
	 *            标识哪个监控中心，当在云端检查更新的时候会用
	 * @return
	 * @throws SQLException
	 */
	public List<SyncRow> getServerSyncRow(SyncConfig config) throws SQLException {
		return getSyncRow(config.getServerDb(), config.getClientDb(), config.getTableName(),config.getColumns(),config.getQueryMaximun());
	}

	/**
	 * 获取同步数据(终端)
	 * 
	 * @param config
	 * @return
	 * @throws SQLException
	 */
	public List<SyncRow> getClientSyncRow(SyncConfig config) throws SQLException {
		return getSyncRow(config.getClientDb(), config.getServerDb(), config.getTableName(),config.getColumns(),config.getQueryMaximun());
	}

	/**
	 * 获取需要同步的数据
	 * @param fromdb		从哪个库获取要同步的数据
	 * @param todb			数据要同步到哪个库（另外一端要用到）
	 * @param tablename		获取同步的表
	 * @param columns		要同步的列
	 * @param queryMaximun 	最多获取多少条数据
	 * @return
	 * @throws SQLException
	 */
	private List<SyncRow> getSyncRow(String fromdb, String todb, String tablename, String columns,int queryMaximun) throws SQLException {
		List<SyncRow> rows = new ArrayList<>();
		Connection conn = SyncDataSource.getConnection(fromdb);
		if (conn == null) {
			logger.error("lose connection");
			return rows;
		}
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		String sql = null;
		if (columns.trim().equals("*")) {
			sql = String.format(QUERY_SYNCROW_FORMAT, fromdb, tablename, queryMaximun);
		} else {
			StringBuffer _sql = new StringBuffer("select ");
			String _from = String.format(QUERY_SYNCROW_SUFFIX_FORMAT, fromdb, tablename, queryMaximun);
			_sql.append(columns).append(_from);
			sql = _sql.toString();
		}
		try {
			logger.debug(sql);
			pstmt = conn.prepareStatement(sql);
			rst = pstmt.executeQuery();
			while (rst.next()) {
				SyncRow row = SyncRowDTO.initFromResultSet(rst);
				row.setTableName(tablename);
				row.setFromDb(fromdb);
				row.setToDb(todb);
				rows.add(row);
			}
			logger.debug("rsult size:" + rows.size());
		} finally {
			SyncDataSource.close(rst, pstmt, conn);
		}
		return rows;
	}

	/**
	 * 检查数据是否存在 注：因为涉及配置的时候服务与客户端服务库名称不一至的问题，所以这个方法只能用来做插入和修改前的检验
	 * 
	 * @param row
	 * @return
	 */
	public SyncRow rowExists(SyncRow row) {
		SyncRow newRow = null;
		String sql = String.format("select * from `%s` where sync_id = ? ", row.getTableName());
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		try {
			conn = SyncDataSource.getConnection(row.getToDb());
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, row.getSyncId());
			logger.debug("sql " + sql);
			logger.debug("param " + row.getSyncId());
			rst = pstmt.executeQuery();
			if (rst.next()) {
				newRow = SyncRowDTO.initFromResultSet(rst);
				newRow.setTableName(row.getTableName());
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			SyncDataSource.close(rst, pstmt, conn);
		}

		return newRow;
	}

	/**
	 * 同步失败后更新本地的同步时间 注意：这个方法不更改同步状态
	 * 
	 * @param tableName
	 * @param results
	 * @throws SQLException
	 */
	public void updateSyncFail(String tableName, List<FailResult> results) throws SQLException {
		if (results.isEmpty() || tableName == null) {
			logger.warn("invalid parameter");
			return;
		}
		String[] _names = tableName.split("\\.");
		Connection conn = null;
		if (_names.length > 1) {
			conn = SyncDataSource.getConnection(_names[0]);
			tableName = _names[1];
		} else {
			throw new NullPointerException("dbname can not be null");
		}
		if (conn == null) {
			logger.error("lose connection");
			return;
		}

		PreparedStatement pstmt = null;
		try {
			// "update `%s` set `sync_time`=? where `sync_id`=?";
			FailResultContainer container = splitFailResults(results);
			if (!container.updates.isEmpty()) {
				String sql = String.format(UPDATE_SYNCROW_FAIL, tableName);
				logger.debug(sql);
				pstmt = conn.prepareStatement(sql);
				for (FailResult result : container.updates) {
					Timestamp curr = new Timestamp(System.currentTimeMillis());
					pstmt.setObject(1, curr);
					pstmt.setObject(2, result.getSyncId());
					pstmt.addBatch();
					logger.debug("batch :" + JsonTool.toString(result));
				}
				int result = pstmt.executeUpdate();
				logger.debug("result " + result);
			}

			if (!container.inserts.isEmpty()) {
				SyncDataSource.close(null, pstmt, null);
				String sql = String.format(UPDATE_SYNCROW_FAIL_DATA_EXISTS, tableName);
				logger.debug(sql);
				pstmt = conn.prepareStatement(sql);
				for (FailResult result : container.inserts) {
					Timestamp curr = new Timestamp(System.currentTimeMillis());
					pstmt.setObject(1, curr);
					pstmt.setObject(2, result.getSyncId());
					pstmt.addBatch();
					logger.debug("batch :" + JsonTool.toString(result));
				}
				int result = pstmt.executeUpdate();
				logger.debug("result " + result);
			}
		} finally {
			SyncDataSource.close(null, pstmt, conn);
		}
	}

	private static class FailResultContainer {
		public List<FailResult> inserts = new ArrayList<>(); // 新增失败
		public List<FailResult> updates = new ArrayList<>(); // 更新失败
	}

	private FailResultContainer splitFailResults(List<FailResult> failResults) {
		FailResultContainer container = new FailResultContainer();
		for (FailResult result : failResults) {
			if (FailReason.DATA_EXISTS.equals(result.getResaon())) {
				container.inserts.add(result);
			} else {
				container.updates.add(result);
			}
		}
		return container;
	}

	/**
	 * 同步成功后更新本地数据状态和同步时间
	 * 
	 * @param tableName
	 * @param results
	 * @throws SQLException
	 */
	public void updateSyncSuccess(String tableName, List<SuccessResult> results) throws SQLException {
		if (results.isEmpty() || tableName == null) {
			logger.warn("invalid parameter");
			return;
		}
		String[] _names = tableName.split("\\.");
		Connection conn = null;
		if (_names.length > 1) {
			conn = SyncDataSource.getConnection(_names[0]);
			tableName = _names[1];
		} else {
			throw new NullPointerException("db name can not be null");
		}
		if (conn == null) {
			logger.error("lose connection");
			return;
		}
		PreparedStatement pstmt = null;
		try {
			// update `%s` set `sync_state`=?,`sync_time`=? where `sync_id`=?
			String sql = String.format(UPDATE_SYNCROW_SUCESS, tableName);
			logger.debug(sql);
			pstmt = conn.prepareStatement(sql);
			for (SuccessResult result : results) {
				pstmt.setObject(1, SyncRow.STATE_NORMAL);
				pstmt.setObject(2, result.getSyncTime());
				pstmt.setObject(3, result.getSyncId());
				pstmt.addBatch();
				logger.debug("batch :" + JsonTool.toString(result));
			}
			int result = pstmt.executeUpdate();
			logger.debug("result " + result);
		} finally {
			SyncDataSource.close(null, pstmt, conn);
		}
	}

	/**
	 * 查询哪些表更新了
	 * 
	 * @param dbname
	 * @return
	 * @throws SQLException
	 */
	public Set<String> getUpdatedTables(String dbname) throws SQLException {
		Set<String> tables = new HashSet<>();
		if (dbname == null || "".equals(dbname.trim())) {
			return tables;
		}

		Connection conn = null;
		try {
			conn = SyncDataSource.getConnection(dbname);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (conn == null) {
			logger.error("lose connection");
			return tables;
		}
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		try {
			String sql = QUERY_UPDATED_TABLES;
			logger.debug(sql);
			pstmt = conn.prepareStatement(sql);
			Timestamp lastCheckTime = lastCheckTimes.get(dbname);
			if (lastCheckTime == null) {
				lastCheckTime = new Timestamp(System.currentTimeMillis() - TIME_INTERVAL);
			}
			logger.debug("param " + lastCheckTime);
			pstmt.setTimestamp(1, lastCheckTime);
			rst = pstmt.executeQuery();
			while (rst.next()) {
				tables.add(rst.getString("table_name"));
				Timestamp updateTime = rst.getTimestamp("update_time");
				if (updateTime.getTime() > lastCheckTime.getTime()) {
					lastCheckTime = updateTime;
				}
			}
			lastCheckTimes.put(dbname, lastCheckTime);
			logger.debug("result size:" + tables.size() + " lastCheckTime:" + lastCheckTime);
		} finally {
			SyncDataSource.close(rst, pstmt, conn);
		}
		return tables;
	}

}
