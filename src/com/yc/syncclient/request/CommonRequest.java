package com.yc.syncclient.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.yc.dbsync.dto.SyncConfigDTO;
import com.yc.dbsync.model.SyncConfig;
import com.yc.dbsync.model.SyncResult;
import com.yc.syncclient.Contants;
import com.yc.syncclient.network.protocal.common.impl.AsyncRequest;
import com.yc.syncclient.network.protocal.common.impl.SyncRequest;
import com.yc.common.JsonTool;

public class CommonRequest {
	private static Logger logger = Logger.getLogger(CommonRequest.class);

	/**
	 * 从服务器上获取同步配置信息
	 * 
	 * @param channel
	 * @return
	 */
	public static List<SyncConfig> getSyncConfig() {
		Map<String, Object> reqData = new HashMap<>();
		String configStr = new SyncRequest(Contants.CommandCode.GET_SYNC_CONFIG, reqData).request();
		logger.debug(configStr);
		List<SyncConfig> configs = JsonTool.toBean(configStr, SyncConfigDTO.G_LIST_TYPE);
		// 有可能后台配置删除了所有的配置，不能根据array 的size 来判断要不要更新配置信息
		if (configs == null) {
			configs = new ArrayList<>();
		}
		return configs;
	}

	public static void reportSyncResult(SyncResult syncResult) {
		Map<String, Object> reqData = new HashMap<>();
		reqData.put("sync_result", syncResult);
		reqData.put("channel", Contants.syncClient.getChannel());
		logger.debug("request:" + JsonTool.toString(reqData));
		new AsyncRequest(Contants.CommandCode.REPORT_SYNC_RESULT, reqData) {

			@Override
			public void onReceive(String data) {
				logger.debug("report sync result success " + data);
			}
			@Override
			public void onError(int code, String reason) {
				logger.error("report sync result eror code:" + code + " reason:" + reason);
			}
		}.request();
	}
	
	public static void reportSyncDeletedResult(SyncResult syncResult){
		Map<String, Object> reqData = new HashMap<>();
		reqData.put("sync_result", syncResult);
		reqData.put("channel", Contants.syncClient.getChannel());
		logger.debug("request:" + JsonTool.toString(reqData));
		new AsyncRequest(Contants.CommandCode.REPORT_SYNC_DELETED_RESULT, reqData) {

			@Override
			public void onReceive(String data) {
				logger.debug("report sync result success " + data);
			}
			@Override
			public void onError(int code, String reason) {
				logger.error("report sync result eror code:" + code + " reason:" + reason);
			}
		}.request();
	}

}
