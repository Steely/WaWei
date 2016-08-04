package com.yc.syncclient.request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yc.dbsync.model.SyncDeleted;
import com.yc.syncclient.Contants.CommandCode;
import com.yc.syncclient.network.protocal.common.impl.SyncComporessRequest;

public class SyncDeletedRequest extends SyncComporessRequest{
	private List<SyncDeleted> deleteds;
	public SyncDeletedRequest(List<SyncDeleted> deleteds){
		this.deleteds = deleteds;
		super.command = CommandCode.DB_SYNC_DELETED;
		super.isCompress = true;
		super.isSync = true;
	}
	
	@Override
	public String request() {
		Map<String, Object> param = new HashMap<>();
		param.put("sync_deleted", deleteds);
		super.requestData = param;
		return super.request();
	}
	
}
