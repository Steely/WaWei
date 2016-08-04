package test;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yc.common.JsonTool;
import com.yc.dbsync.dao.SyncDataSource;


public class SqlTypeTest {
	private static Logger logger = Logger.getLogger(SqlTypeTest.class);;
	public static void main(String[]args)throws Exception{
		String configPath = System.getProperty("CONFIG_PATH");
		if (configPath == null) {
			configPath = System.getProperty("user.dir")+"/etc";
			System.setProperty("CONFIG_PATH", configPath);
		}
		System.out.println("config path "+configPath);
		PropertyConfigurator.configure(configPath + "/conf/log4j.properties");
		DataSource ds = SyncDataSource.getDataSource();
		Connection conn = ds.getConnection();
		Statement statement = conn.createStatement();
		ResultSet rst = statement.executeQuery("select * from type_test");
		List<Map<String, Object>> values = new ArrayList<>();
		while(rst.next()){
			ResultSetMetaData rsmd = rst.getMetaData();
			int count=rsmd.getColumnCount();
			Map<String, Object> map = new HashMap<>();
			for(int i=0;i<count;i++){
				String columnName=rsmd.getColumnName(i+1);
				String columnTypeName = rsmd.getColumnTypeName(i+1);
				int columnType = rsmd.getColumnType(i+1);
				String className  =rsmd.getColumnClassName(i+1);
				logger.debug("cName:"+columnName);
				logger.debug("ctName:"+columnTypeName);
				logger.debug("ctype:"+columnType);
				logger.debug("clzName:"+className);
				logger.debug("value:"+rst.getObject(i+1));
				logger.debug("value_type:"+rst.getObject(i+1).getClass().getName());
				if(columnType==Types.TIMESTAMP){
					logger.debug("getTimestamp:"+rst.getTimestamp(i+1));
				}
				map.put(columnName, rst.getObject(i+1));
				logger.debug("=====================");
			}
			values.add(map);
			logger.debug("-------------------------------------");
		}
		
		System.out.println(JsonTool.toString(values));
		values = JsonTool.toBean(JsonTool.toString(values), values.getClass());
		for(Map<String, Object> value:values){
			for(String key:value.keySet()){
				logger.debug("key:"+key+" value:"+value.get(key));
			}
			logger.debug("=====================");
		}
	}
}
