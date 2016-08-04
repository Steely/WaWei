package com.yc.syncserver.process;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yc.dbsync.biz.SyncDeletedBiz;
import com.yc.dbsync.model.SyncResult;
import com.yc.protocal.model.ResponseData;
import com.yc.syncserver.ResponseCode;

import com.yc.common.JsonTool;

/**
 * 同步结果上报
 * @author steely
 *
 */
public class SyncDeletedResultProcess extends BaseProcess {
	private SyncDeletedBiz biz = new SyncDeletedBiz();
	@Override
	public ResponseData process(String jsonStr) throws Exception {
		ResponseData responseData = new ResponseData();
		if (jsonStr == null || jsonStr.trim().isEmpty()) {
			responseData.setResponseCode(ResponseCode.INVALID_PARAMETER);
			return responseData;
		}
		JSONObject object = JSON.parseObject(jsonStr);
		if (object == null) {
			responseData.setResponseCode(ResponseCode.INVALID_PARAMETER);
			return responseData;
		}
		SyncResult syncResult = JsonTool.toBean(object.getString("sync_result"),SyncResult.class);
		biz.processResult(syncResult);
		Map<String, Object> result = new HashMap<>();
		result.put("jsonStr", "{}");
		responseData.setData(result);
		responseData.setResponseCode(ResponseCode.SUCCESS);
		return responseData;
	}

}
