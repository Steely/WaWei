package com.yc.dbsync;

public class SyncContans {
	
	public interface FailReason{
		public String DATA_EXISTS = "data exists";
		public String DATA_NOT_EXISTS = "data not exists";
		public String DATA_NEWER = "local data is newer";
		public String EXECUTE_NOT_RESULT = "execute not result";
		public String INVALID_PARAMETER = "invalid parameter";
		public String SERVER_EXCEPTION = "server exception";
	}
	
	public interface DataKey{
		public String SUCCESS = "success";
		public String FAIL = "fail";
		public String SYNCDATA = "syncdata";
		
		public String CHANNEL = "channel";
		public String TOKEN = "token";
		
		public String UCLIENT = "uclient";
	}
}
