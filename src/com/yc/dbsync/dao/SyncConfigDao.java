package com.yc.dbsync.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.yc.common.CacheManager;
import com.yc.dbsync.dto.SyncConfigDTO;
import com.yc.dbsync.model.SyncConfig;

public class SyncConfigDao {
	static final Logger logger = Logger.getLogger(DBSyncDao.class);

	public List<SyncConfig> getConfigsByChannel(String channel, boolean fromClient) {
		String key = channel + "_" + fromClient;
		List<SyncConfig> configs = CacheManager.getSyncConfigs(key);
		if (configs == null) {
			synchronized (SyncConfigDao.class) {
				configs = CacheManager.getSyncConfigs(key);
				if (configs == null) {
					configs =_getConfigsByChannel(channel, fromClient);
					if(configs!=null){
						CacheManager.putSyncConfigs(key, configs);
					}
				}
			}
		}
		return configs;
	}

	public List<SyncConfig> _getConfigsByChannel(String channel, boolean fromClient) {
		List<SyncConfig> result = new ArrayList<>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		try {
			conn = SyncDataSource.getConfigDataSource().getConnection();
			String sql = "select * from sync_config where channel = ?";
			if (fromClient) {
				sql += " and roles !=1";
			} else {
				sql += " and roles !=2";
			}

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, channel);
			logger.debug(sql);
			rst = pstmt.executeQuery();
			while (rst.next()) {
				SyncConfig config = SyncConfigDTO.initFormResultSet(rst);
				result.add(config);
			}
			logger.debug("channel " + channel + " result size " + result.size());

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			SyncDataSource.close(rst, pstmt, conn);
		}
		return result;
	}
}
