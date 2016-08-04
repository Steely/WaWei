package com.yc.syncserver.process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yc.dbsync.biz.SyncConfigBiz;
import com.yc.dbsync.model.SyncConfig;
import com.yc.protocal.model.ResponseData;
import com.yc.syncserver.ResponseCode;

import com.yc.common.JsonTool;

public class SyncConfigProcess extends BaseProcess {

	@Override
	public ResponseData process(String jsonStr) throws Exception {
		ResponseData responseData = new ResponseData();
		if(jsonStr==null){
			responseData.setResponseCode(ResponseCode.INVALID_PARAMETER);
			return responseData;
		}
		JSONObject jsonObject = JSON.parseObject(jsonStr);
		String channel = jsonObject.getString("channel");
		if(channel==null){
			responseData.setResponseCode(ResponseCode.INVALID_PARAMETER);
			return responseData;
		}
		List<SyncConfig> syncConfigs = new SyncConfigBiz().getClientConfigs(channel);
		Map<String, Object> result = new HashMap<>();
		result.put("jsonStr", JsonTool.toString(syncConfigs));
		responseData.setData(result);
		responseData.setResponseCode(ResponseCode.SUCCESS);
		return responseData;
	}

}
