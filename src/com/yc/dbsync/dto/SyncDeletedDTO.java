package com.yc.dbsync.dto;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.yc.dbsync.model.SyncDeleted;

public class SyncDeletedDTO {
	public static Type G_LIST_TYPE =  new TypeToken<List<SyncDeleted>>() {}.getType();
	
	public static SyncDeleted initFromResultSet(ResultSet rst)throws SQLException{
		SyncDeleted sd = new SyncDeleted();
		sd.setDeleteTime(rst.getTimestamp("delete_time"));
		sd.setStatus(rst.getInt("status"));
		sd.setSyncId(rst.getString("sync_id"));
		sd.setSyncTime(rst.getTimestamp("sync_time"));
		sd.setTableName(rst.getString("table_name"));
		return sd;
		
	}
}
