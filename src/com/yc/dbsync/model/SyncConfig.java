package com.yc.dbsync.model;

import java.util.Date;

public class SyncConfig {
	public static final int ROLE_SERVER_TO_CLIENT = 1;  //服务器同步到客户端
	public static final int ROLE_CLIENT_TO_SERVER = 2;	//客户端同步到服务器
	public static final int ROLE_SERVER_EACH_CLIENT = 3;//双向同步
	
	private int id;
	private String channel;
	private String serverDb;
	private String clientDb;
	private String tableName;
	private String columns; 
	private int roles;
	private Date lastSyncTimel;
	private int queryMaximun;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	
	public String getServerDb() {
		return serverDb;
	}
	public void setServerDb(String serverDb) {
		this.serverDb = serverDb;
	}
	public String getClientDb() {
		return clientDb;
	}
	public void setClientDb(String clientDb) {
		this.clientDb = clientDb;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getColumns() {
		return columns;
	}
	public void setColumns(String columns) {
		this.columns = columns;
	}
	public int getRoles() {
		return roles;
	}
	public void setRoles(int roles) {
		this.roles = roles;
	}
	public Date getLastSyncTimel() {
		return lastSyncTimel;
	}
	public void setLastSyncTimel(Date lastSyncTimel) {
		this.lastSyncTimel = lastSyncTimel;
	}
	public int getQueryMaximun() {
		return queryMaximun;
	}
	public void setQueryMaximun(int queryMaximun) {
		this.queryMaximun = queryMaximun;
	}
}
