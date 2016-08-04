package com.yc.dbsync.model;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class SyncRow {
	public static final int STATE_NEW = 1;
	public static final int STATE_NORMAL = 2;
	public static final int STATE_MODIFY = 3;
	
	private int syncState;				//同步状态
	private String syncId;				//数据行唯一标识
	private Timestamp syncCreateTime;	//数据创建时间
	private Timestamp syncUpdateTime;	//更新时间
	private Timestamp syncTime;			//同步时间
	private String tableName;			//表名
	private String fromDb;				//发生更改的数据库
	private String toDb;				//同步到的数据库
	private int teamId;					//监控中心的唯一标识
	private Map<String, Object> columns = new HashMap<>();	//要同步的数据例
	
	public int getTeamId() {
		return teamId;
	}
	public void setTeamId(int teamId) {
		this.teamId = teamId;
	}
	public String getFromDb() {
		return fromDb;
	}
	public void setFromDb(String fromDb) {
		this.fromDb = fromDb;
	}
	public String getToDb() {
		return toDb;
	}
	public void setToDb(String toDb) {
		this.toDb = toDb;
	}
	
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public Map<String, Object> getColumns() {
		return columns;
	}
	public void setColumns(Map<String, Object> columns) {
		this.columns = columns;
	}
	public int getSyncState() {
		return syncState;
	}
	public void setSyncState(int syncState) {
		this.syncState = syncState;
	}
	public String getSyncId() {
		return syncId;
	}
	public void setSyncId(String syncId) {
		this.syncId = syncId;
	}
	
	public Timestamp getSyncCreateTime() {
		return syncCreateTime;
	}
	public void setSyncCreateTime(Timestamp syncCreateTime) {
		this.syncCreateTime = syncCreateTime;
	}
	public Timestamp getSyncUpdateTime() {
		return syncUpdateTime;
	}
	public void setSyncUpdateTime(Timestamp syncUpdateTime) {
		this.syncUpdateTime = syncUpdateTime;
	}
	public Timestamp getSyncTime() {
		return syncTime;
	}
	public void setSyncTime(Timestamp syncTime) {
		this.syncTime = syncTime;
	}
//	public JSONObject toJson(){
//		JSONObject jsonObject = new JSONObject();
//		
//		jsonObject.put("sync_id", syncId);
//		jsonObject.put("sync_state", syncState);
//		jsonObject.put("sync_create_time", syncCreateTime);
//		jsonObject.put("sync_update_time", syncUpdateTime);
//		jsonObject.put("sync_time", syncTime);
////		jsonObject.put("columns", syncCreateTime);
//		
//		JSONObject _columns = (JSONObject)JSON.toJSON(columns);
//		jsonObject.put("columns", _columns);
//		
//		return jsonObject;
//	}
}
