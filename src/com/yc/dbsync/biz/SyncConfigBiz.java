package com.yc.dbsync.biz;

import java.util.List;

import com.yc.dbsync.dao.SyncConfigDao;
import com.yc.dbsync.model.SyncConfig;

public class SyncConfigBiz {
	
	/**
	 * 根据客户端传递的渠道号获取同步信息
	 * 同步信息说明了客户端要上报哪些信息给服务端
	 * @param channel
	 * @return
	 */
	public List<SyncConfig> getClientConfigs(String channel){
		return new SyncConfigDao().getConfigsByChannel(channel,true);
	}
	
	/**
	 * 根据客户端传递的渠道号获取同步信息
	 * 同步信息说明了服端要给客户端传哪些数据进行同步
	 * @param channel
	 * @return
	 */
	public List<SyncConfig> getServerConfigs(String channel){
		return new SyncConfigDao().getConfigsByChannel(channel, false);
	}
}
