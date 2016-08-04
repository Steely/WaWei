package com.yc.dbsync.dto;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.reflect.TypeToken;
import com.yc.dbsync.model.SyncRow;

public class SyncRowDTO {
	public static Type G_LIST_TYPE =  new TypeToken<List<SyncRow>>() {}.getType(); // 转为gson 的type
	private static Set<String> requiredColumn = new HashSet<>();//String[]{"sync_id","sync_state","sync_create_time","sync_update_time","sync_time"};
	
	static{
		requiredColumn.add("sync_id");
		requiredColumn.add("sync_state");
		requiredColumn.add("sync_create_time");
		requiredColumn.add("sync_update_time");
		requiredColumn.add("sync_time");
	}
	
	public static SyncRow initFromResultSet(ResultSet rst)throws SQLException{
		SyncRow row = new SyncRow();
		//"sync_id","sync_state","sync_create_time","sync_update_time","sync_time"
		row.setSyncId(rst.getString("sync_id"));
		row.setSyncState(rst.getInt("sync_state"));
		row.setSyncCreateTime(rst.getTimestamp("sync_create_time"));
		row.setSyncUpdateTime(rst.getTimestamp("sync_update_time"));
		row.setSyncTime(rst.getTimestamp("sync_time"));
		ResultSetMetaData rsmd = rst.getMetaData();
		
		Map<String, Object> columns = row.getColumns();
		int count=rsmd.getColumnCount();
		for(int i=0;i<count;i++){
			String columnName=rsmd.getColumnName(i+1);
			if(!requiredColumn.contains(columnName)){
				columns.put(columnName, rst.getObject(columnName));
			}
		}
		return row;
	}
}
