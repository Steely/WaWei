package com.yc.syncclient;

import java.util.ArrayList;
import java.util.List;

import com.yc.dbsync.model.SyncClient;
import com.yc.dbsync.model.SyncConfig;

public class Contants {

	public static SyncClient syncClient = null;

	// 从服务器加载来的同步配置信息
	public static List<SyncConfig> syncConfigList = new ArrayList<>();

	public interface CommandCode {
		public int GET_SYSTEM_TIME = 5; 	//获取系统时间
		public int GET_SYNC_CONFIG = 10; 	//获取同步配置
		public int DB_SYNC = 11; 			//上传同步
		public int REPORT_SYNC_RESULT = 12; //上报同步结果
		public int DB_SYNC_DELETED = 13; 	//同步删除的数据
		public int REPORT_SYNC_DELETED_RESULT = 14; //上报同步删除的结果
	}
}
