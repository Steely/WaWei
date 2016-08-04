package com.yc.dbsync.model;

public class SyncClient {
	private String channel;
	private String syncConfig;
	private String privateKey;
	private String token;
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public String getSyncConfig() {
		return syncConfig;
	}
	public void setSyncConfig(String syncConfig) {
		this.syncConfig = syncConfig;
	}
	public String getPrivateKey() {
		return privateKey;
	}
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	
}
