package com.yc.syncserver.process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.yc.common.JsonTool;
import com.yc.dbsync.biz.SyncConfigBiz;
import com.yc.dbsync.biz.SyncDeletedBiz;
import com.yc.dbsync.dto.SyncDeletedDTO;
import com.yc.dbsync.model.SyncConfig;
import com.yc.dbsync.model.SyncDeleted;
import com.yc.dbsync.model.SyncResult;
import com.yc.protocal.model.ResponseData;
import com.yc.syncserver.ResponseCode;

/**
 * 同步删除的记录
 * @author steely
 *
 */
public class SyncDeletedProcess extends BaseProcess {
	SyncDeletedBiz biz = new SyncDeletedBiz();

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
			List<SyncDeleted> syncDeleteds = JsonTool.toBean(jsonObject.getString("sync_deleted"),SyncDeletedDTO.G_LIST_TYPE);
			syncResult = biz.executeSyncDelete(syncDeleteds);
			
			String channel = jsonObject.getString("channel");
			if (channel != null) {
				SyncConfigBiz configBiz = new SyncConfigBiz();
				List<SyncConfig> syncConfigs = configBiz.getServerConfigs(channel);
				List<SyncDeleted> deletedList = biz.getServerSyncDeleted(syncConfigs);
				syncResult.getDeletedData().addAll(deletedList);
			}
			
		}catch(JSONException je){
			je.printStackTrace();
		}
		Map<String, Object> respMap = new HashMap<>();
		respMap.put("jsonStr", JsonTool.toString(syncResult));
		responseData.setData(respMap);
		return responseData;
	}

}
