package test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yc.dbsync.dao.SyncDataSource;
import com.yc.dbsync.model.SyncResult;
import com.yc.dbsync.model.SyncResult.FailResult;
import com.yc.dbsync.model.SyncResult.SuccessResult;

public class JustTest {

	public static void main(String [] args)throws Exception{
//		String configPath = System.getProperty("CONFIG_PATH");
//		if (configPath == null) {
//			configPath = System.getProperty("user.dir")+"/etc";
//			System.setProperty("CONFIG_PATH", configPath);
//		}
//		System.out.println("config path "+configPath);
//		PropertyConfigurator.configure(configPath + "/conf/log4j.properties");
//		DataSource ds = SyncDataSource.getDataSource();
//		Connection conn = ds.getConnection();
//		String sql = String.format("DELETE FROM `%s` WHERE `sync_id`=? AND `team_id`=?","t2");
//		System.out.println("sql:"+sql);
//		PreparedStatement pstmt = conn.prepareStatement(sql);
//		pstmt.setString(1, "02e76433-4fe0-11e6-ac25-b083fec16f68");
//		pstmt.setInt(2, 9999);
//		System.out.println("update :"+pstmt.executeUpdate());
//		dbCharsetTest();
		dbBooleanTest();
	}
	
	
	private static void dbBooleanTest() throws SQLException{
		String configPath = System.getProperty("CONFIG_PATH");
		if (configPath == null) {
			configPath = System.getProperty("user.dir")+"/etc";
			System.setProperty("CONFIG_PATH", configPath);
		}
		System.out.println("config path "+configPath);
		PropertyConfigurator.configure(configPath + "/conf/log4j.properties");
		DataSource ds = SyncDataSource.getDataSource();
		Connection conn = ds.getConnection();
		PreparedStatement pstmt = conn.prepareStatement("select * from t10");
		ResultSet rst = pstmt.executeQuery();
		while (rst.next()) {
			System.out.println("id:"+rst.getString("Id")+"\tname:"+rst.getString("name")+"\tisHome:"+rst.getBoolean("isHome"));
		}
	}
	
	protected static void dbCharsetTest()throws SQLException{
		String configPath = System.getProperty("CONFIG_PATH");
		if (configPath == null) {
			configPath = System.getProperty("user.dir")+"/etc";
			System.setProperty("CONFIG_PATH", configPath);
		}
		System.out.println("config path "+configPath);
		PropertyConfigurator.configure(configPath + "/conf/log4j.properties");
		DataSource ds = SyncDataSource.getDataSource();
		Connection conn = ds.getConnection();
		PreparedStatement pstmt = conn.prepareStatement("select plate_cn from t_parkcarenter order by enter_time desc limit 1,20");
		ResultSet rst = pstmt.executeQuery();
		while (rst.next()) {
			System.out.println("value:"+rst.getString(1));
			
		}
	}
	
	@Test
	public void StringFormat() {
		System.out.printf("sdjf%skkk\r\n", "我是字符串阿");
		System.out.printf("%dkkk\r\n", System.currentTimeMillis());
		System.out.println(System.currentTimeMillis());
		JSONObject jsonObject = JSON.parseObject("{\"currTime\":1464083645279}");
		Timestamp timestamp = jsonObject.getTimestamp("currTime");
		System.out.println(timestamp);
	}

	@Test
	public void printUuid() {
		System.out.println(UUID.randomUUID());
		System.out.println((System.currentTimeMillis() + "").length());
	}

	@Test
	public void jsonTest() {

		SyncResult syncResult = new SyncResult();
		Map<String, List<SuccessResult>> successMap = syncResult.getSuccessResult();
		List<SuccessResult> successList = new ArrayList<>();
		successList.add(
				new SuccessResult("6d150858-218c-11e6-b67b-9e71128cae77", new Timestamp(System.currentTimeMillis())));
		successList.add(
				new SuccessResult("6d15085a-218c-11e6-b67b-9e71128cae77", new Timestamp(System.currentTimeMillis())));
		successMap.put("yc_sync_test.t1", successList);

		Map<String, List<FailResult>> failMap = syncResult.getFailResult();
		List<FailResult> failList = new ArrayList<>();
		failList.add(new FailResult("25156571-2226-11e6-bad7-b870f409a570", "no reason"));
		failList.add(new FailResult("9e3bf6b9-2223-11e6-bad7-b870f409a570", "no reason"));
		failMap.put("yc_sync_test.t1", failList);

		String syncStr = new Gson().toJson(syncResult);

		System.out.println(syncStr);
		SyncResult result = new Gson().fromJson(syncStr, SyncResult.class);
		System.out.println(result.getSuccessResult().get("yc_sync_test.t1").get(0).getSyncId() + " \t "
				+ result.getSuccessResult().get("yc_sync_test.t1").get(0).getSyncTime());
		JSONObject object = JSON.parseObject(syncStr);
		System.out.println(object.toString());
		String str = "";
		System.out.println(new Gson().fromJson(str, SyncResult.class));
		str = null;
		System.out.println(new Gson().fromJson(str, SyncResult.class));
		str = " ";
		System.out.println(new Gson().fromJson(str, SyncResult.class));
		List<SyncResult> syncResults = new ArrayList<>();
		syncResults.add(syncResult);
		str = new Gson().toJson(syncResults);
		System.out.println("str : " + str);
		syncResults = new Gson().fromJson(str, new TypeToken<List<SyncResult>>() {
		}.getType());
		System.out.println(syncResults.size());
		System.out.println(syncResults.get(0).getClass().getTypeName());
		System.out.println(syncResults.get(0).getSuccessResult().get("yc_sync_test.t1").get(0).getSyncId() + " kk \t kk"
				+ syncResults.get(0).getSuccessResult().get("yc_sync_test.t1").get(0).getSyncTime());
	}

	@Test
	public void cacheTest() {
		Map<String, String> maps = new HashMap<>();
		LoadingCache<String, Object> cache = CacheBuilder.newBuilder().concurrencyLevel(8)
				.expireAfterWrite(20, TimeUnit.SECONDS).initialCapacity(20).maximumSize(500).recordStats()
				.build(new CacheLoader<String, Object>() {
					@Override
					public Object load(String key) throws Exception {
						System.out.println("come in load" + key);
						Object value = maps.get(key);
						if(value==null){
							throw new ExecutionException(null);
						}
						return value;
					}
				});
		maps.put("a", "1");
		maps.put("c", "2");
		maps.put("d", "3");
		maps.put("e", "4");
		
		cache.put("a", "1");
		cache.put("c", "2");
		cache.put("d", "3");
		cache.put("e", "4");

		Set<String> keys = new HashSet<>();
		keys.add("a");
		keys.add("e");
		keys.add("b");
		keys.add("c");
		keys.add("d");
		keys.add("f");
		
		String _key=null;
		for (String key : keys) {
			_key = key;
			try {
				System.out.println(cache.get(key));
			} catch (ExecutionException e) {
//				e.printStackTrace();
				System.err.println("key empty "+key);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			System.out.println(cache.get(_key));
		} catch (ExecutionException e) {
			System.err.println("key empty "+_key);
		}
	}
}
