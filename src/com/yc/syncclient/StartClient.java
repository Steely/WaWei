package com.yc.syncclient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.yc.common.Coder;
import com.yc.dbsync.model.SyncClient;
import com.yc.syncclient.network.protocal.ProtocalClientConstant;
import com.yc.syncclient.network.protocal.common.SendThread;
import com.yc.syncclient.task.LoadSyncConfigTask;
import com.yc.syncclient.task.SyncDataTask;

public class StartClient {
	private static Logger logger = null;
	public static void main(String [] args){
		String configPath = System.getProperty("CONFIG_PATH");
		if (configPath == null) {
			configPath = System.getProperty("user.dir")+"/clientetc";
			System.setProperty("CONFIG_PATH", configPath);
		}
		System.out.println("config path "+configPath);
		PropertyConfigurator.configure(configPath + "/conf/log4j.properties");
		logger = Logger.getLogger(StartClient.class);
		loadConfig(configPath+"/conf/config.properties");
		//启动异步请求队列消化线程
		new SendThread().start();
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
		
		TimerTask syncDataTask = new SyncDataTask();
		TimerTask loadConfigTask = new LoadSyncConfigTask();

		//每10分钟获取一次
		scheduler.scheduleAtFixedRate(loadConfigTask, 0, 10, TimeUnit.MINUTES);
		//每5秒钟同步一次
		scheduler.scheduleAtFixedRate(syncDataTask, 5, 5, TimeUnit.SECONDS);

		logger.info("************************");
		logger.info("Client Started!");
		logger.info("************************");
	}
	
	/**
	 * 加载客户端配置
	 */
	private static void loadConfig(String configPath){
		File file = new File(configPath);
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
//			logger.debug("===========load config info==========");
//			for(Object _key:p.keySet()){
//				logger.debug("key:"+_key+" value:"+p.getProperty(_key.toString()));
//			}
//			logger.debug("===========load config info==========");
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
