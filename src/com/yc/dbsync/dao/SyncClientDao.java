package com.yc.dbsync.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.yc.common.CacheManager;
import com.yc.dbsync.dto.SyncClientDTO;
import com.yc.dbsync.model.SyncClient;

public class SyncClientDao {
	static final Logger logger = Logger.getLogger(SyncClientDao.class);
	private final static Lock lock = new ReentrantLock();
	public SyncClient getByChannel(String channel) throws SQLException {
		SyncClient client = CacheManager.getSyncClient(channel);
		if(client==null){
			lock.lock();
			try {
				client = CacheManager.getSyncClient(channel);
				if(client==null){
					client = _getByChannel(channel);
					if(client!=null){
						CacheManager.putSyncClient(channel, client);
					}
				}
			}finally{
				lock.unlock();
			}
		}
		return client;
	}

	private SyncClient _getByChannel(String channel) throws SQLException {
		if (channel == null || "".equals(channel.trim())) {
			logger.warn("invalid parameter");
			return null;
		}
		Connection conn = SyncDataSource.getConfigDataSource().getConnection();
		if (conn == null) {
			logger.error("lose connection");
			return null;
		}
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		SyncClient syncClient = null;
		try {
			String sql = SyncClientDTO.QUERY_BY_CHANNEL_SQL;
			logger.debug(sql);
			logger.debug("param:" + channel);
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, channel);
			rst = pstmt.executeQuery();
			if (rst.next()) {
				logger.debug("have result");
				syncClient = SyncClientDTO.initFromResult(rst);
			} else {
				logger.debug("result is empty");
			}
		} finally {
			SyncDataSource.close(rst, pstmt, conn);
		}
		return syncClient;
	}
}
