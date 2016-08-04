package com.yc.dbsync.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.yc.common.TextUtil;
import com.yc.dbsync.dto.SyncDeletedDTO;
import com.yc.dbsync.model.SyncDeleted;
import com.yc.dbsync.model.SyncResult.FailResult;
import com.yc.dbsync.model.SyncResult.SuccessResult;

public class SyncDeletedDao {
	private static Logger logger = Logger.getLogger(SyncDeletedDao.class);
	private final static String QUERY_DELETED_ROWS = "SELECT * FROM `sync_delete_row` WHERE `table_name`=? AND `status`="+SyncDeleted.STATUS_WAIT;
	private final static String EXECUTE_DELETE_FROMAT = "DELETE FROM `%s` WHERE `sync_id`=?";
	private final static String UPDATE_DELETED_STATUS = "UPDATE `sync_delete_row` SET `status`=?,`sync_time`=?,`remark`=? WHERE `sync_id`=?";
	private final static String UPDATE_STATUS_DELETE_BY_SELF = "UPDATE `sync_delete_row` SET `status`="+SyncDeleted.STATUS_SUCCESS+",`sync_time`=NOW(),`remark`='delete by sync program' WHERE `table_name`=? AND `sync_id`=?";

	/**
	 * 获取删除了哪些数据
	 * 
	 * @param fromdb
	 * @param todb
	 * @return
	 */
	public List<SyncDeleted> getSyncDeleted(String fromdb, String todb, String tableName) throws SQLException {
		List<SyncDeleted> result = new ArrayList<>();
		Connection conn = SyncDataSource.getConnection(fromdb);
		if (conn == null) {
			logger.error("lose connection");
			return result;
		}
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		try {
			String sql = QUERY_DELETED_ROWS;
			logger.debug(sql);
			logger.debug("dbname:" + fromdb + " tableName:" + tableName);
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tableName);
			rst = pstmt.executeQuery();
			while (rst.next()) {
				SyncDeleted syncDeleted = SyncDeletedDTO.initFromResultSet(rst);
				syncDeleted.setFromDb(fromdb);
				syncDeleted.setToDb(todb);
				result.add(syncDeleted);
			}
			logger.debug("rsult size:" + result.size());
		} finally {
			SyncDataSource.close(rst, pstmt, conn);
		}
		return result;
	}

	
	/**
	 * 删除需要删除的数据
	 * 
	 * @param deleted
	 * @return
	 * @throws SQLException
	 */
	public boolean delete(SyncDeleted deleted) throws SQLException {
		Connection conn = SyncDataSource.getConnection(deleted.getToDb());
		if (conn == null) {
			logger.error("lose connection");
			return false;
		}
		PreparedStatement pstmt = null;
		try {
			// "DELETE FROM `%s` WHERE sync_id=? and team_id=?";
			String sql = String.format(EXECUTE_DELETE_FROMAT, deleted.getTableName());
			logger.debug(sql);
			logger.debug("sync_id:" + deleted.getSyncId());
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, deleted.getSyncId());
			int result = pstmt.executeUpdate();
			SyncDataSource.close(null, pstmt, null);
			logger.debug("execute result:" + result);
			if(result>0){
				//通过同步删除的数据要在sync_delete_row 更新一下状态，不要更新到别的服务器上去
				sql = UPDATE_STATUS_DELETE_BY_SELF;
				logger.debug(sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, deleted.getTableName());
				pstmt.setString(2, deleted.getSyncId());
				logger.debug("tablename:"+deleted.getTableName()+" syncid:"+deleted.getSyncId());
				int _result = pstmt.executeUpdate();
				logger.debug("execute result:"+_result);
			}
			return result > 0;
		} finally {
			SyncDataSource.close(null, pstmt, conn);
		}
	}

	public void updateSyncFail(String tableName, List<FailResult> results) throws SQLException {
		if (results.isEmpty() || TextUtil.isEmpty(tableName)) {
			logger.warn("invalid parameter");
			return;
		}
		String[] _names = tableName.split("\\.");

		if (_names.length < 2) {
			throw new NullPointerException("dbname can not be null");
		}
		List<DeletedStatus> statusList = new ArrayList<>();
		Timestamp now = new Timestamp(System.currentTimeMillis());
		for (FailResult result : results) {
			DeletedStatus status = new DeletedStatus();
			status.status = SyncDeleted.STATUS_FAIL;
			status.remark = result.getResaon();
			status.syncId = result.getSyncId();
			status.syncTime = now;
			statusList.add(status);
		}
		if (!statusList.isEmpty()) {
			updateStatus(_names[0], statusList);
		}
	}

	public void updateSyncSuccess(String tableName, List<SuccessResult> results) throws SQLException {
		if (results.isEmpty() || TextUtil.isEmpty(tableName)) {
			logger.warn("invalid parameter");
			return;
		}
		String[] _names = tableName.split("\\.");

		if (_names.length < 2) {
			throw new NullPointerException("dbname can not be null");
		}
		List<DeletedStatus> statusList = new ArrayList<>();
		for (SuccessResult result : results) {
			DeletedStatus status = new DeletedStatus();
			status.status = SyncDeleted.STATUS_SUCCESS;
			status.remark = "";
			status.syncId = result.getSyncId();
			status.syncTime = result.getSyncTime();
			statusList.add(status);
		}
		if (!results.isEmpty()) {
			updateStatus(_names[0], statusList);
		}
	}

	private void updateStatus(String dbName, List<DeletedStatus> deletedStatus) throws SQLException {
		// "UPDATE `sync_delete_row` SET `status`=?,`sync_time`=?,`remark`=?
		// WHERE `syncd_id`=?"
		if (TextUtil.isEmpty(dbName)) {
			throw new NullPointerException("dbname can not be null");
		}
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = SyncDataSource.getConnection(dbName);
			if (conn == null) {
				logger.error("lose connection");
				return;
			}
			String sql = UPDATE_DELETED_STATUS;
			logger.debug(sql);
			pstmt = conn.prepareStatement(sql);
			for (DeletedStatus status : deletedStatus) {
				logger.debug("sync_id:" + status.syncId + " remark: " + status.remark + " status:" + status.status + " synctime:" + status.syncTime);
				pstmt.setInt(1, status.status);
				pstmt.setTimestamp(2, status.syncTime);
				pstmt.setString(3, status.remark);
				pstmt.setString(4, status.syncId);
				pstmt.addBatch();
			}
			int[] result = pstmt.executeBatch();
			logger.debug("result size:" + result.length);
		} finally {
			SyncDataSource.close(null, pstmt, conn);
		}
	}

	private static class DeletedStatus {
		public String syncId;
		public Timestamp syncTime;
		public String remark;
		public int status;
	}
}
