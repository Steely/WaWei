package com.yc.dbsync.biz;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.yc.dbsync.dao.SyncDeletedDao;
import com.yc.dbsync.model.SyncConfig;
import com.yc.dbsync.model.SyncDeleted;
import com.yc.dbsync.model.SyncResult;
import com.yc.dbsync.model.SyncResult.FailResult;
import com.yc.dbsync.model.SyncResult.SuccessResult;

public class SyncDeletedBiz {
	SyncDeletedDao dao = new SyncDeletedDao();

	/**
	 * 获取删除的同步数据(客户端)
	 * 
	 * @param configs
	 * @return
	 */
	public List<SyncDeleted> getClientSyncDeleted(List<SyncConfig> configs) {
		List<SyncDeleted> result = new ArrayList<>();
		for (SyncConfig config : configs) {
			try {
				result.addAll(dao.getSyncDeleted(config.getClientDb(), config.getServerDb(),config.getTableName()));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	/**
	 * 获取删除的同步数据（服务端）
	 * @param configs
	 * @return
	 */
	public List<SyncDeleted> getServerSyncDeleted(List<SyncConfig> configs){
		List<SyncDeleted> result = new ArrayList<>();
		for (SyncConfig config : configs) {
			try {
				result.addAll(dao.getSyncDeleted(config.getServerDb(), config.getClientDb(),config.getTableName()));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 执行同步删除
	 * 
	 * @param deleteds
	 * @return
	 */
	public SyncResult executeSyncDelete(List<SyncDeleted> deleteds) {
		SyncResult syncResult = new SyncResult();
		for (SyncDeleted deleted : deleteds) {
			String key = deleted.getFromDb() + "." + deleted.getTableName();
			String syncId = deleted.getSyncId();
			Timestamp curTime = new Timestamp(System.currentTimeMillis());
			try {
				if (dao.delete(deleted)) {
					List<SuccessResult> successList = syncResult.getSuccessResult().get(key);
					if (successList == null) {
						successList = new ArrayList<>();
						syncResult.getSuccessResult().put(key, successList);
					}
					successList.add(new SuccessResult(syncId, curTime));
				} else {
					List<FailResult> failList = syncResult.getFailResult().get(key);
					if (failList == null) {
						failList = new ArrayList<>();
						syncResult.getFailResult().put(key, failList);
					}
					failList.add(new FailResult(syncId, "execute delete db return empty"));
				}
			} catch (SQLException se) {
				se.printStackTrace();
				List<FailResult> failList = syncResult.getFailResult().get(key);
				if (failList == null) {
					failList = new ArrayList<>();
					syncResult.getFailResult().put(key, failList);
				}
				failList.add(new FailResult(syncId, "execute delete get exception:" + se.getMessage()));
			}
		}
		
		return syncResult;
	}

	/**
	 * 将执行结果更新到数据库
	 * @param syncResult
	 * @return
	 */
	public SyncResult processResult(SyncResult syncResult) {
		Map<String, List<SuccessResult>> successResult = syncResult.getSuccessResult();
		for (String key : successResult.keySet()) {
			try {
				dao.updateSyncSuccess(key, successResult.get(key));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		Map<String, List<FailResult>> failResult = syncResult.getFailResult();
		for (String key : failResult.keySet()) {
			try {
				dao.updateSyncFail(key, failResult.get(key));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return executeSyncDelete(syncResult.getDeletedData());
	}

}
