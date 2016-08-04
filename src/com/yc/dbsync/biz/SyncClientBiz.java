package com.yc.dbsync.biz;

import java.sql.SQLException;
import org.apache.log4j.Logger;

import com.yc.common.Coder;
import com.yc.dbsync.dao.SyncClientDao;
import com.yc.dbsync.model.SyncClient;

public class SyncClientBiz {
	private static Logger logger = Logger.getLogger(SyncClientBiz.class);

	public SyncClient authentic(String channel, String token) {
		SyncClient client = getByChannel(channel);
		if (client == null) {
			logger.warn("syncclient not exists channel is " + channel);
		} else {
			try {
				String localToken = Coder.encryptHMAC(client.getToken(), client.getPrivateKey());
				if (!token.equals(localToken)) {
					client = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
				client = null;
			}
		}
		return client;
	}

	private SyncClient getByChannel(String channel) {
		try {
			return new SyncClientDao().getByChannel(channel);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
