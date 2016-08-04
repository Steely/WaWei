package com.yc.syncclient.request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yc.dbsync.model.SyncRow;
import com.yc.syncclient.Contants.CommandCode;
import com.yc.syncclient.network.protocal.common.impl.SyncComporessRequest;

/**
 * 获取需要同步的数据
 * 
 * @author steely
 *
 */
public class SyncRowRequest extends SyncComporessRequest {
//	private Logger logger = Logger.getLogger(SyncRowRequest.class);
	private List<SyncRow> rows;

	public SyncRowRequest(List<SyncRow> rows) {
		this.rows = rows;
		super.command = CommandCode.DB_SYNC;
		super.isCompress = true;
		super.isSync = true;
	}

	@Override
	public String request() {
		Map<String, Object> param = new HashMap<>();
		param.put("sync_data", rows);
		super.requestData = param;
		return super.request();
	}
}
