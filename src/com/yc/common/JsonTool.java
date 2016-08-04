package com.yc.common;

import java.lang.reflect.Type;
import java.sql.Time;
import java.text.SimpleDateFormat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class JsonTool {
//	private static Gson gson = new Gson();
	private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss")
			.registerTypeAdapter(Time.class, new SQLTimeTypeAdapter()).create();
	
	
	private static class SQLTimeTypeAdapter implements JsonSerializer<Time>{
		private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		@Override
		public JsonElement serialize(Time src, Type typeOfSrc, JsonSerializationContext context) {
			String str = sdf.format(src);
			return new JsonPrimitive(str);
		}
		
	}
	
	public static String toString(Object object){
		return gson.toJson(object);
	}
	
	public static <T> T toBean(String json, Class<T> classOfT){
		return gson.fromJson(json, classOfT);
	}
	
	public static <T> T toBean(String json,Type type){
		return gson.fromJson(json, type);
	}
}
