package com.yc.dbsync.model;

import java.sql.Timestamp;

public class SyncDeleted {
	public static final int STATUS_WAIT = 1;	//等待同步
	public static final int STATUS_SUCCESS = 2;	//同步成功
	public static final int STATUS_FAIL = 3;	//同步失败
	
	
	private String tableName;		//删除的表名
	private String syncId;			//删除的唯一id
	private Timestamp deleteTime;	//删除的时间
	private int status;				//同步状态1、待同步 2、同步成功 3、同步失败
	private Timestamp syncTime;		//删除时间
	
	private String fromDb;	//从哪个库同步
	private String toDb;	//同步到哪个库
	private String remark;	//备注，通常会放同步失败的原因
	
	
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getSyncId() {
		return syncId;
	}
	public void setSyncId(String syncId) {
		this.syncId = syncId;
	}
	public Timestamp getDeleteTime() {
		return deleteTime;
	}
	public void setDeleteTime(Timestamp deleteTime) {
		this.deleteTime = deleteTime;
	}
	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Timestamp getSyncTime() {
		return syncTime;
	}
	public void setSyncTime(Timestamp syncTime) {
		this.syncTime = syncTime;
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
	
}
