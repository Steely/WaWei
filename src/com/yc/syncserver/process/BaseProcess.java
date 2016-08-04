package com.yc.syncserver.process;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yc.dbsync.SyncContans;
import com.yc.dbsync.biz.SyncClientBiz;
import com.yc.dbsync.model.SyncClient;
import com.yc.protocal.Protocalable;
import com.yc.protocal.model.RequestData;
import com.yc.protocal.model.ResponseData;
import com.yc.syncserver.ResponseCode;

public abstract class BaseProcess implements Protocalable {
	private static Logger logger = Logger.getLogger(BaseProcess.class);
	private SyncClientBiz biz = new SyncClientBiz();
	/**
	 * 所有的请求都要求认证
	 */
	@Override
	public ResponseData processRequest(RequestData requestData) throws Exception {
		Map<String, Object> reqMap = requestData.getData();
		String jsonStr = (String) reqMap.get("jsonStr");
		if(jsonStr==null || "".equals(jsonStr.trim())){
			logger.warn("Invalid request jsonStr is empty");
			ResponseData responseData = new ResponseData();
			responseData.setResponseCode(ResponseCode.AUTHENTIC_FAIL);
			responseData.setData(new HashMap<String, Object>());
			return responseData;
		}
		JSONObject json = JSON.parseObject(jsonStr);
		String token = (String) json.get("token");
		String channel = (String) json.get("channel");
		// ======认证判断======
		if (token == null || channel == null) {
			logger.warn("Invalid request token or channel empty ");
			ResponseData responseData = new ResponseData();
			responseData.setResponseCode(ResponseCode.AUTHENTIC_FAIL);
			responseData.setData(new HashMap<String, Object>());
			return responseData;
		}
		SyncClient client = biz.authentic(channel, token);
		if (client == null) {
			logger.warn("authentic fail {channel:"+channel+",token:"+token+"}");
			ResponseData responseData = new ResponseData();
			responseData.setResponseCode(ResponseCode.AUTHENTIC_FAIL);
			responseData.setData(new HashMap<String, Object>());
			return responseData;
		}
		//认证成功后将认证信息存入参数里传递
		json.put(SyncContans.DataKey.UCLIENT, client);
		// ==================
		return process(json.toJSONString());
	}

	public abstract ResponseData process(String jsonStr) throws Exception;

}
