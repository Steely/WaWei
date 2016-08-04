package com.yc.syncserver.process;

import java.util.HashMap;
import java.util.Map;

import com.yc.protocal.model.ResponseData;
import com.yc.syncserver.ResponseCode;

public class SystemTimeProcess extends BaseProcess {

	@Override
	public ResponseData process(String jsonStr) throws Exception {
		ResponseData responseData = new ResponseData();
		Map<String, Object> result = new HashMap<>();
		result.put("jsonStr", String.format("{\"currTime\":\"%d\"}", System.currentTimeMillis()));
		
		responseData.setData(result);
		responseData.setResponseCode(ResponseCode.SUCCESS);
		return responseData;
	}

}
