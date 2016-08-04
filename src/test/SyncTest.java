package test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.yc.common.Coder;
import com.yc.dbsync.model.SyncClient;
import com.yc.dbsync.model.SyncResult;
import com.yc.dbsync.model.SyncRow;
import com.yc.dbsync.model.SyncResult.FailResult;
import com.yc.dbsync.model.SyncResult.SuccessResult;
import com.yc.syncclient.Contants;
import com.yc.syncclient.network.protocal.ProtocalClientConstant;
import com.yc.syncclient.network.protocal.common.SendThread;
import com.yc.syncclient.network.protocal.common.impl.AsyncComporessRequest;
import com.yc.syncclient.network.protocal.common.impl.AsyncRequest;
import com.yc.syncclient.network.protocal.common.impl.SyncComporessRequest;
import com.yc.syncclient.network.protocal.common.impl.SyncRequest;
import com.yc.syncclient.request.CommonRequest;

public class SyncTest {
	private static Logger logger;

	@BeforeClass
	public static void beforeClass() {
		org.apache.log4j.PropertyConfigurator.configure("D:/work/javaspace/DBSyncService/etc/conf/log4j.properties");
		logger = Logger.getLogger(SyncTest.class);
		loadConfig("D:/work/javaspace/DBSyncService/etc/conf/clientconfig.properties");
		new SendThread().start();
		
	}
	
	@AfterClass
	public static void AfterClass(){
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void systemTimeTest() {
		SyncRequest request = new SyncRequest(Contants.CommandCode.GET_SYSTEM_TIME, null);
		String result = request.request();
		logger.debug("result " + result);
	}

	@Test
	public void getConfig() {
		Map<String, Object> param = new HashMap<>();
//		param.put("channel", "test_channel");
		String result = new AsyncRequest(Contants.CommandCode.GET_SYNC_CONFIG, param){

			@Override
			public void onError(int code, String reason) {
				logger.error("request get error "+code);
			}

			@Override
			public void onReceive(String data) {
				logger.debug("receiver data "+data);
			}
			
		}.request();
		logger.debug("config result:" + result);
	}
	
	@Test
	public void dbSyncInsert(){
		Map<String, Object> param = new HashMap<>();
		param.put("channel", "test_channel");
		
		List<SyncRow> rows = new ArrayList<>();
		SyncRow sr = new SyncRow();
		sr.setFromDb("sync_test");
		sr.setToDb("yc_sync_test");
		sr.setTableName("t1");
		sr.setSyncState(SyncRow.STATE_NEW);
		sr.setSyncId("6d150c68-218c-11e6-b67b-9e71128cae77");
		sr.setSyncCreateTime(new Timestamp(System.currentTimeMillis()));
		Map<String, Object> column = new HashMap<>();
		column.put("name", "name001");
		column.put("pass", "pass001");
		sr.setColumns(column);
		rows.add(sr);
		
		sr = new SyncRow();
		sr.setFromDb("sync_test");
		sr.setToDb("yc_sync_test");
		sr.setTableName("t1");
		sr.setSyncState(SyncRow.STATE_NEW);
		sr.setSyncId("6d150d44-218c-11e6-b67b-9e71128cae77");
		sr.setSyncCreateTime(new Timestamp(System.currentTimeMillis()));
		column = new HashMap<>();
		column.put("name", "name003");
		column.put("pass", "pass003");
		sr.setColumns(column);
		rows.add(sr);
		
		sr = new SyncRow();
		sr.setFromDb("sync_d2");
		sr.setToDb("sync_test2");
		sr.setTableName("t2");
		sr.setSyncState(SyncRow.STATE_NEW);
		sr.setSyncId("6d150c68-218c-11e6-b67b-9e71128cae77");
		sr.setSyncCreateTime(new Timestamp(System.currentTimeMillis()));
		column = new HashMap<>();
		column.put("ip", "192.168.1.101");
		column.put("port", "6401");
		sr.setColumns(column);
		rows.add(sr);
		
		sr = new SyncRow();
		sr.setFromDb("sync_d2");
		sr.setToDb("sync_test2");
		sr.setTableName("t2");
		sr.setSyncState(SyncRow.STATE_NEW);
		sr.setSyncId("6d150d44-218c-11e6-b67b-9e71128cae77");
		sr.setSyncCreateTime(new Timestamp(System.currentTimeMillis()));
		column = new HashMap<>();
		column.put("ip", "192.168.1.102");
		column.put("port", "6402");
		sr.setColumns(column);
		rows.add(sr);
		
//		JSONObject jsonObject = new JSONObject();
//		for(String key:rows.keySet()){
//			List<SyncRow> rowList = rows.get(key);
//			JSONArray array = new JSONArray();
//			for(SyncRow row:rowList){
//				array.add(row.toJson());
//			}
//			jsonObject.put(key, array);
//		}
		param.put("sync_data", rows);
//		logger.debug(JSON.toJSONString(param));
		new AsyncComporessRequest(11, param){

			@Override
			public void onError(int code, String reason) {
				logger.warn("error "+code);
			}

			@Override
			public void onReceive(String data) {
				logger.debug("receive "+data);
			}
			
		}.request();
//		
	}
	
	
	@Test
	public void dbSyncUpdate(){
		List<SyncRow> rows = new ArrayList<>();
		SyncRow sr = new SyncRow();
		sr.setFromDb("sync_test");
		sr.setToDb("yc_sync_test");
		sr.setTableName("t1");
		sr.setSyncState(SyncRow.STATE_MODIFY);
		sr.setSyncId("6d150c68-218c-11e6-b67b-9e71128cae77");
		sr.setSyncCreateTime(new Timestamp(System.currentTimeMillis()));
		sr.setSyncUpdateTime(new Timestamp(System.currentTimeMillis()+1000));
		Map<String, Object> column = new HashMap<>();
		column.put("name", "name02");
		column.put("pass", "pass02");
		sr.setColumns(column);
		rows.add(sr);
		
		sr = new SyncRow();
		sr.setFromDb("sync_test");
		sr.setToDb("yc_sync_test");
		sr.setTableName("t1");
		sr.setSyncState(SyncRow.STATE_MODIFY);
		sr.setSyncId("6d150d44-218c-11e6-b67b-9e71128cae77");
		sr.setSyncCreateTime(new Timestamp(System.currentTimeMillis()));
		sr.setSyncUpdateTime(new Timestamp(System.currentTimeMillis()+1000));
		column = new HashMap<>();
		column.put("name", "name04");
		column.put("pass", "pass04");
		sr.setColumns(column);
		rows.add(sr);
		
		
		sr = new SyncRow();
		sr.setFromDb("sync_d2");
		sr.setToDb("sync_test2");
		sr.setTableName("t2");
		sr.setSyncState(SyncRow.STATE_MODIFY);
		sr.setSyncId("6d150c68-218c-11e6-b67b-9e71128cae77");
		sr.setSyncCreateTime(new Timestamp(System.currentTimeMillis()));
		sr.setSyncUpdateTime(new Timestamp(System.currentTimeMillis()+1000));
		column = new HashMap<>();
		column.put("ip", "192.168.20.101");
		column.put("port", "6421");
		sr.setColumns(column);
		rows.add(sr);
		
		sr = new SyncRow();
		sr.setFromDb("sync_d2");
		sr.setToDb("sync_test2");
		sr.setTableName("t2");
		sr.setSyncState(SyncRow.STATE_MODIFY);
		sr.setSyncId("6d150d44-218c-11e6-b67b-9e71128cae77");
		sr.setSyncCreateTime(new Timestamp(System.currentTimeMillis()));
		sr.setSyncUpdateTime(new Timestamp(System.currentTimeMillis()+1000));
		column = new HashMap<>();
		column.put("ip", "192.168.20.102");
		column.put("port", "6422");
		sr.setColumns(column);
		rows.add(sr);
		
		Map<String, Object> param = new HashMap<>();
		param.put("channel", "test_channel");
		param.put("sync_data", rows);
		String request = new SyncComporessRequest(11, param).request();
		logger.debug(request);
	}
	
	@Test
	public void syncResultReport(){
		SyncResult syncResult = new SyncResult();
		Map<String, List<SuccessResult>> successMap = syncResult.getSuccessResult();
		List<SuccessResult> successList = new ArrayList<>();
		successList.add(new SuccessResult("6d150858-218c-11e6-b67b-9e71128cae77", new Timestamp(System.currentTimeMillis())));
		successList.add(new SuccessResult("6d15085a-218c-11e6-b67b-9e71128cae77",new Timestamp(System.currentTimeMillis())));
		successMap.put("yc_sync_test.t1", successList);
		
		Map<String,List<FailResult>> failMap = syncResult.getFailResult();
		List<FailResult> failList = new ArrayList<>();
		failList.add(new FailResult("25156571-2226-11e6-bad7-b870f409a570", "no reason"));
		failList.add(new FailResult("9e3bf6b9-2223-11e6-bad7-b870f409a570", "no reason"));
		failMap.put("yc_sync_test.t1", failList);
		
		CommonRequest.reportSyncResult(syncResult);
	}
	
	private static void loadConfig(String configFile){
		File file = new File(configFile);
		InputStream is = null;
		try {
			is = new BufferedInputStream(new FileInputStream(file));
			Properties p = new Properties();
			p.load(is);
			ProtocalClientConstant.SERVER_DOMAIN = p.getProperty("serverDomain");
			String portStr = (String)p.get("serverPort");
			ProtocalClientConstant.SERVER_PORT = Integer.parseInt(portStr);
			SyncClient sc = new SyncClient();
			sc.setChannel(p.getProperty("authenticChannel"));
			String token = p.getProperty("authenticToken");
			String key = p.getProperty("authenticKey");
			if(token!=null && key !=null){
				try {
					token = Coder.encryptHMAC(token, key);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			sc.setPrivateKey(key);
			sc.setToken(token);
			Contants.syncClient = sc;
			logger.debug("===========load config info==========");
			for(Object _key:p.keySet()){
				logger.debug("key:"+_key+" value:"+p.getProperty(_key.toString()));
			}
			logger.debug("===========load config info==========");
		}catch(IOException ie){
			ie.printStackTrace();
		}finally {
			if(is!=null){
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
