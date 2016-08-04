package com.yc.dbsync.dto;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.yc.dbsync.model.SyncClient;

public class SyncClientDTO {
	public static final String QUERY_BY_CHANNEL_SQL = "select * from sync_client where channel = ?";
	
	public static SyncClient initFromResult(ResultSet rst){
		SyncClient syncClient = new SyncClient();
		try {
			syncClient.setChannel(rst.getString("channel"));
			syncClient.setPrivateKey(rst.getString("private_key"));
			syncClient.setToken(rst.getString("token"));
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		
		return syncClient;
	}
}
