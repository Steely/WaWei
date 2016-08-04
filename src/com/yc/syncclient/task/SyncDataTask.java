package com.yc.syncclient.task;

import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.yc.common.JsonTool;
import com.yc.dbsync.biz.DBSyncBiz;
import com.yc.dbsync.biz.SyncDeletedBiz;
import com.yc.dbsync.model.SyncDeleted;
import com.yc.dbsync.model.SyncResult;
import com.yc.dbsync.model.SyncRow;
import com.yc.syncclient.Contants;
import com.yc.syncclient.request.CommonRequest;
import com.yc.syncclient.request.SyncDeletedRequest;
import com.yc.syncclient.request.SyncRowRequest;

/**
 * 更新数据上传任务
 * 
 * @author steely
 *
 */
public class SyncDataTask extends TimerTask {
	private Logger logger = Logger.getLogger(SyncDataTask.class);
	DBSyncBiz syncBiz = new DBSyncBiz();
	SyncDeletedBiz deletedBiz = new SyncDeletedBiz();

	@Override
	public void run() {
		try {
			logger.debug("start sync data task");
			if (Contants.syncConfigList.isEmpty()) {
				logger.warn("sync config list is empty");
				return;
			}
			// ================同步新增和更新==================
			List<SyncRow> syncRows = syncBiz.getClientSyncData(Contants.syncConfigList);
			String data = new SyncRowRequest(syncRows).request();
			logger.debug("receive data " + data);
			SyncResult syncResult = JsonTool.toBean(data, SyncResult.class);
			if (syncResult == null || syncResult.isEmpty()) {
				logger.warn("receive data is empty " + data);
			} else {
				SyncResult syncResult2 = syncBiz.processResult(syncResult);
				// 执行了同步之后要上报同步结果
				if (syncResult2 == null || syncResult2.isEmpty()) {
					logger.debug("syncresult is empty, not need to report server");
				} else {
					CommonRequest.reportSyncResult(syncResult2);
				}
			}

			// ================同步删除==================
			List<SyncDeleted> deleteds = deletedBiz.getClientSyncDeleted(Contants.syncConfigList);
			data = new SyncDeletedRequest(deleteds).request();
			logger.debug("deleted request receive data " + data);
			SyncResult syncResult3 = JsonTool.toBean(data, SyncResult.class);
			if (syncResult3 == null || syncResult3.isEmptyDeleted()) {
				logger.warn("deleted request receive data is empty " + data);
			} else {
				SyncResult syncResult4 = deletedBiz.processResult(syncResult3);
				if (syncResult4.isEmptyDeleted()) {
					logger.warn("deleted process result is empty, not need to report server");
				}else{
					CommonRequest.reportSyncDeletedResult(syncResult4);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
