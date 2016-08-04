package com.yc.common;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.yc.dbsync.model.SyncClient;
import com.yc.dbsync.model.SyncConfig;

public class CacheManager {
	static final Logger logger = Logger.getLogger(CacheManager.class);

	public static SyncClient getSyncClient(String key) {
		try {
			return syncCleintCache.get(key);
		} catch (ExecutionException e) {
			logger.warn("get syncclient from cache empty ! key:" + key);
		}
		return null;
	}

	public static void putSyncClient(String key, SyncClient syncClient) {
		syncCleintCache.put(key, syncClient);
	}

	public static List<SyncConfig> getSyncConfigs(String key) {
		try {
			return syncConfigCache.get(key);
		} catch (ExecutionException e) {
			logger.warn("get syncconfig from cache empty ! key:" + key);
		}
		return null;
	}

	public static void putSyncConfigs(String key, List<SyncConfig> configList) {
		syncConfigCache.put(key, configList);
	}

	public static Object get(Object key) {
		try {
			cache.get(key);
		} catch (ExecutionException e) {
			logger.warn("get from cache empty ! key:" + key);
		}
		return null;
	}

	public static void put(Object key, Object value) {
		cache.put(key, value);
	}

	// 设置并发级别为8，并发级别是指可以同时写缓存的线程数
	// .concurrencyLevel(8)
	// 设置写缓存后过期时间
	// .expireAfterWrite(30, TimeUnit.SECONDS)
	// 设置缓存容器的初始容量
	// .initialCapacity(20)
	// 设置缓存最大容量，超过后就会按照LRU最近虽少使用算法来移除缓存项
	// .maximumSize(500)
	// 设置要统计缓存的命中率
	// .recordStats()
	// .removalListener()
	// 在缓存不存在时通过CacheLoader的实现自动加载缓存
	// .build();

	private static LoadingCache<Object, Object> cache = CacheBuilder.newBuilder().concurrencyLevel(8)
			.expireAfterWrite(60, TimeUnit.SECONDS).initialCapacity(20).maximumSize(500).recordStats()
			.build(new CacheLoader<Object, Object>() {
				ExecutionException e = new ExecutionException(null);

				@Override
				public Object load(Object key) throws Exception {
					throw e;
				}
			});

	private static LoadingCache<String, SyncClient> syncCleintCache = CacheBuilder.newBuilder().concurrencyLevel(8)
			.expireAfterWrite(60, TimeUnit.SECONDS).initialCapacity(20).maximumSize(500).recordStats()
			.build(new CacheLoader<String, SyncClient>() {
				ExecutionException e = new ExecutionException(null);

				@Override
				public SyncClient load(String key) throws Exception {
					throw e;
				}
			});

	private static LoadingCache<String, List<SyncConfig>> syncConfigCache = CacheBuilder.newBuilder().concurrencyLevel(8)
			.expireAfterWrite(60, TimeUnit.SECONDS).initialCapacity(20).maximumSize(500).recordStats()
			.build(new CacheLoader<String, List<SyncConfig>>() {
				ExecutionException e = new ExecutionException(null);

				@Override
				public List<SyncConfig> load(String key) throws Exception {
					throw e;
				}
			});

}
