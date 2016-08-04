package com.yc.dbsync.dto;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.yc.dbsync.model.SyncConfig;

public class SyncConfigDTO {
	public static final Type G_LIST_TYPE = new TypeToken<List<SyncConfig>>() {}.getType(); // 转为gson 的type

	public static SyncConfig initFormResultSet(ResultSet rst) {
		SyncConfig syncConfig = new SyncConfig();
		try {
			syncConfig.setId(rst.getInt("id"));
			syncConfig.setChannel(rst.getString("channel"));
			syncConfig.setServerDb(rst.getString("server_db"));
			syncConfig.setClientDb(rst.getString("client_db"));
			syncConfig.setTableName(rst.getString("table_name"));
			syncConfig.setColumns(rst.getString("columns"));
			syncConfig.setRoles(rst.getInt("roles"));
			syncConfig.setLastSyncTimel(rst.getDate("last_sync_time"));
			syncConfig.setQueryMaximun(rst.getInt("query_maximum"));
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return syncConfig;
	}

}
