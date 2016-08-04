package com.yc.dbsync.biz;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.yc.dbsync.SyncContans.FailReason;
import com.yc.dbsync.dao.DBSyncDao;
import com.yc.dbsync.model.SyncConfig;
import com.yc.dbsync.model.SyncResult;
import com.yc.dbsync.model.SyncResult.FailResult;
import com.yc.dbsync.model.SyncResult.SuccessResult;
import com.yc.dbsync.model.SyncRow;

public class DBSyncBiz {
	private static Logger logger = Logger.getLogger(DBSyncBiz.class);
	private DBSyncDao dao = new DBSyncDao();

	/**
	 * 执行数据同步
	 * 
	 * @param syncRows
	 * @return
	 */
	public SyncResult executeSync(List<SyncRow> syncRows) {
		SyncResult result = new SyncResult();
		Map<String, List<FailResult>> failMap = result.getFailResult();
		Map<String, List<SuccessResult>> successMap = result.getSuccessResult();
		for (SyncRow row : syncRows) {
			try {
				switch (row.getSyncState()) {
				case SyncRow.STATE_NEW:
					if (dao.rowExists(row) != null) {
						addFail(row, failMap, FailReason.DATA_EXISTS);
					} else {
						row.setSyncTime(new Timestamp(System.currentTimeMillis()));
						if (dao.insert(row)) {
							addSuccess(row, successMap);
						} else {
							addFail(row, failMap, FailReason.EXECUTE_NOT_RESULT);
						}
					}
					break;
				case SyncRow.STATE_MODIFY:
					SyncRow localRow = dao.rowExists(row);
					if (localRow != null) {
						Timestamp localRowTime = localRow.getSyncUpdateTime();
						// 本地数据比要同步的数据更新
						if (localRowTime != null && localRowTime.getTime() > row.getSyncUpdateTime().getTime()) {
							localRow.setFromDb(row.getToDb());
							localRow.setToDb(row.getFromDb());
							addFail(row, failMap, FailReason.DATA_NEWER);
							List<SyncRow> _syncRows = result.getSyncData();
							_syncRows.add(localRow);
						} else {
							row.setSyncTime(new Timestamp(System.currentTimeMillis()));
							dao.update(row);
							addSuccess(row, successMap);
							// 这里注意，因为更新的配置里还涉及到只有一些列，所在更新未必会成功，但不能标记为失败
							// 如果更新数据里有
							// name:'myname',pass:'mypass',other_column:"string"
							// 本地数据
							// name:'myname',pass:'mypass',other_column:"yykkddss"
							// 配置文件里只需要更新 name 和 pass ，那么调用execute
							// update的时间为会返回false
							// if (dao.update(row)) {
							// addSuccess(row, successMap);
							// } else {
							// addFail(row, failMap,
							// FailReason.EXECUTE_NOT_RESULT);
							// }
						}
					} else {
						row.setSyncTime(new Timestamp(System.currentTimeMillis()));
						logger.info("sync update by data not exists ");
						if (dao.insert(row)) {
							addSuccess(row, successMap);
						} else {
							addFail(row, failMap, FailReason.EXECUTE_NOT_RESULT);
						}
					}
					break;
				case SyncRow.STATE_NORMAL:
					addFail(row, failMap, FailReason.INVALID_PARAMETER);
					break;
				}
			} catch (SQLException e) {
				addFail(row, failMap, FailReason.SERVER_EXCEPTION);
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 获取要同步的数据(服务端)
	 * 
	 * @param configList
	 * @param teamId
	 *            监控中心id
	 * @return
	 */
	public List<SyncRow> getServerSyncData(List<SyncConfig> configList) {
		List<SyncRow> result = new ArrayList<>();
		// Map<dbname,Set<tableName>> updateMap
		Map<String, Set<String>> updatedMap = new HashMap<>();
		for (SyncConfig config : configList) {
			String dbname = config.getServerDb();
			Set<String> updatedSet = updatedMap.get(dbname);
			if(updatedSet==null){
				updatedSet = getUpdateTables(dbname);
				if(updatedSet!=null && !updatedSet.isEmpty()){
					updatedMap.put(dbname, updatedSet);
				}
			}
			// 只有当sync_table_notic 里记录了有更新才去访问数据库的更新
			if (updatedSet != null && updatedSet.contains(config.getTableName())) {
				try {
					List<SyncRow> rows = dao.getServerSyncRow(config);
					if (rows != null && !rows.isEmpty()) {
						result.addAll(rows);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	/**
	 * 获取要同步的数据（终端）
	 * 
	 * @param configList
	 * @return
	 */
	public List<SyncRow> getClientSyncData(List<SyncConfig> configList) {
		List<SyncRow> result = new ArrayList<>();
		// Map<dbname,Set<tableName>> updateMap
		Map<String, Set<String>> updatedMap = new HashMap<>();
		for (SyncConfig config : configList) {
			String dbname = config.getClientDb();
			Set<String> updatedSet = updatedMap.get(dbname);
			if (updatedSet == null) {
				updatedSet = getUpdateTables(dbname);
				if (updatedSet!=null && !updatedSet.isEmpty()) {
					updatedMap.put(dbname, updatedSet);
				}
			}
			// 只有当sync_table_notic 里记录了有更新才去访问数据库的更新
			if (updatedSet != null && updatedSet.contains(config.getTableName())) {
				try {
					List<SyncRow> rows = dao.getClientSyncRow(config);
					if (rows != null && !rows.isEmpty()) {
						result.addAll(rows);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	
	
	private Set<String> getUpdateTables(String dbname) {
		Set<String> tables = null;
		try {
			tables = dao.getUpdatedTables(dbname);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tables;
	}

	/**
	 * 将同步结果进行处理 因为从服务器返回的同步结果有新的同步信息，所以在这里也要执行服务器数据同步到本地的行为
	 * 
	 * @param result
	 * @return
	 */
	public SyncResult processResult(SyncResult result) {
		Map<String, List<SuccessResult>> successMap = result.getSuccessResult();
		for (String tableName : successMap.keySet()) {
			try {
				dao.updateSyncSuccess(tableName, successMap.get(tableName));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		Map<String, List<FailResult>> failMap = result.getFailResult();
		for (String tableName : failMap.keySet()) {
			try {
				dao.updateSyncFail(tableName, failMap.get(tableName));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		SyncResult syncResult = this.executeSync(result.getSyncData());
		return syncResult;
	}

	private void addFail(SyncRow row, Map<String, List<FailResult>> failMap, String reason) {
		String tableName = row.getFromDb() + "." + row.getTableName();
		List<FailResult> failList = failMap.get(tableName);
		if (failList == null) {
			failList = new ArrayList<>();
			failMap.put(tableName, failList);
		}
		failList.add(new FailResult(row.getSyncId(), reason));
	}

	private void addSuccess(SyncRow row, Map<String, List<SuccessResult>> successMap) {
		String tableName = row.getFromDb() + "." + row.getTableName();
		List<SuccessResult> successList = successMap.get(tableName);
		if (successList == null) {
			successList = new ArrayList<>();
			successMap.put(tableName, successList);
		}
		successList.add(new SuccessResult(row.getSyncId(), row.getSyncTime()));
	}

}
