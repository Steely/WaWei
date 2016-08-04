package com.yc.syncserver.process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yc.dbsync.biz.DBSyncBiz;
import com.yc.dbsync.biz.SyncConfigBiz;
import com.yc.dbsync.dto.SyncRowDTO;
import com.yc.dbsync.model.SyncConfig;
import com.yc.dbsync.model.SyncResult;
import com.yc.dbsync.model.SyncRow;
import com.yc.protocal.model.ResponseData;
import com.yc.syncserver.ResponseCode;

import com.yc.common.JsonTool;

public class DBSyncProcess extends BaseProcess {
	private DBSyncBiz syncBiz = new DBSyncBiz();

	@Override
	public ResponseData process(String jsonStr) throws Exception {
		ResponseData responseData = new ResponseData();
		if (jsonStr == null || jsonStr.trim().isEmpty()) {
			responseData.setResponseCode(ResponseCode.INVALID_PARAMETER);
			return responseData;
		}
		SyncResult syncResult = null;
		try {
			JSONObject jsonObject = JSON.parseObject(jsonStr);
			List<SyncRow> syncRows = JsonTool.toBean(jsonObject.getString("sync_data"),SyncRowDTO.G_LIST_TYPE);
			syncResult = syncBiz.executeSync(syncRows);
			String channel = jsonObject.getString("channel");
			if (channel != null) {
				SyncConfigBiz configBiz = new SyncConfigBiz();
				List<SyncConfig> syncConfigs = configBiz.getServerConfigs(channel);
				
				/** 
				 * 会存在服务器数据比客户端发过来的同步数据更新的情况
				 * 当上面这种情况发生时会把服务器的数据存入到更新到客户端的列表
				 * 所以这里要处理将数据合并一下
				 **/
				List<SyncRow> syncRowList = syncBiz.getServerSyncData(syncConfigs);
				List<SyncRow> resultRowList = syncResult.getSyncData();
				if(!syncRowList.isEmpty()){
					resultRowList.addAll(syncRowList);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		Map<String, Object> respMap = new HashMap<>();
//		respMap.put("jsonStr", syncResult.toJson().toJSONString());
		respMap.put("jsonStr", JsonTool.toString(syncResult));
		responseData.setData(respMap);
		responseData.setResponseCode(ResponseCode.SUCCESS);
		
		return responseData;
	}
	
}
