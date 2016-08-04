package com.yc.syncclient.task;

import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.yc.dbsync.model.SyncConfig;
import com.yc.syncclient.Contants;
import com.yc.syncclient.request.CommonRequest;

public class LoadSyncConfigTask extends TimerTask {
	private Logger logger = Logger.getLogger(LoadSyncConfigTask.class);
	@Override
	public void run() {
		logger.debug("start load config task");
		List<SyncConfig> configs = CommonRequest.getSyncConfig();
		if(configs!=null){
			Contants.syncConfigList = configs;
			logger.debug("config size "+configs.size());
		}
	}

}
