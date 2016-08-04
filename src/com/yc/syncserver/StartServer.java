package com.yc.syncserver;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.yc.protocal.server.Server;

public class StartServer {
	static Logger logger = Logger.getLogger(StartServer.class);

	public static void main(String[] args) {
		// 初始化配置文件目录，ProtocalConfig.xml 必须放在这个目录的conf目录下
		if (System.getProperty("CONFIG_PATH") == null) {
			System.setProperty("CONFIG_PATH", "D:/work/javaspace/ParkDBSync/etc");
		}
		PropertyConfigurator.configure(System.getProperty("CONFIG_PATH") + "/conf/log4j.properties");
		System.setProperty("java.net.preferIPv4Stack", "true"); // Disable IPv6

		// 启动API服务
		Server.main(null);

//		new Timer().schedule(new TimerTask() {
//			public void run() {
//				// doNothing
//			}
//		}, 60000, 60000);

		logger.info("************************");
		logger.info("Server Started!");
		logger.info("************************");
	}
}
