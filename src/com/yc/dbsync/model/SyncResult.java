package com.yc.dbsync.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncResult {

	//Map<dbName.tableName,List<SuccessResult>>
	private Map<String, List<SuccessResult>> successResult = new HashMap<>();
	//Map<dbName.tableName, List<FailResult>>
	private Map<String, List<FailResult>> failResult = new HashMap<>();
	private List<SyncRow> syncData = new ArrayList<>();
	private List<SyncDeleted> deletedData = new ArrayList<>();

	public boolean isEmpty(){
		if(successResult.isEmpty() && failResult.isEmpty() && syncData.isEmpty())
			return true;
		return false;
	}
	
	public boolean isEmptyDeleted(){
		if(successResult.isEmpty() && failResult.isEmpty() && deletedData.isEmpty())
			return true;
		return false;
	}
	
	public List<SyncDeleted> getDeletedData() {
		return deletedData;
	}

	public Map<String, List<SuccessResult>> getSuccessResult() {
		return successResult;
	}

	public Map<String, List<FailResult>> getFailResult() {
		return failResult;
	}

	public List<SyncRow> getSyncData() {
		return syncData;
	}


	/**
	 * 记录成功的记录
	 * 
	 * @author steely
	 *
	 */
	public static class SuccessResult {
		private String syncId;
		private Timestamp syncTime;

		public SuccessResult() {
		}

		public SuccessResult(String syncId, Timestamp syncTime) {
			this.syncId = syncId;
			this.syncTime = syncTime;
		}

		public String getSyncId() {
			return syncId;
		}

		public void setSyncId(String syncId) {
			this.syncId = syncId;
		}

		public Timestamp getSyncTime() {
			return syncTime;
		}

		public void setSyncTime(Timestamp syncTime) {
			this.syncTime = syncTime;
		}

	}

	/**
	 * 记录失败的记录
	 * 
	 * @author steely
	 *
	 */
	public static class FailResult {
		private String syncId;
		private String resaon;

		public FailResult() {
		}

		public FailResult(String syncId, String resaon) {
			this.syncId = syncId;
			this.resaon = resaon;
		}

//		public JSONObject toJson() {
//			JSONObject jsonObject = new JSONObject();
//			jsonObject.put("sync_id", syncId);
//			jsonObject.put("reason", resaon);
//			return jsonObject;
//		}

		public String getSyncId() {
			return syncId;
		}

		public void setSyncId(String syncId) {
			this.syncId = syncId;
		}

		public String getResaon() {
			return resaon;
		}

		public void setResaon(String resaon) {
			this.resaon = resaon;
		}
	}

}
