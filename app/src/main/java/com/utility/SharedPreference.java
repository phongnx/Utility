package com.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class SharedPreference {
	private static SharedPreferences sharedPreferences;

	// String
	public static String getString(Context context, Object key, String defaultValue) {
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		try {
			return sharedPreferences.getString(String.valueOf(key), defaultValue);
		} catch (Exception e) {
			DebugLog.loge(e);
			return defaultValue;
		}
	}

	public static void setString(Context context, Object key, String data) {
		try {
			sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			Editor editor = sharedPreferences.edit();
			editor.putString(String.valueOf(key), data);
			editor.commit();
		} catch (Exception e) {
			DebugLog.loge(e);
		}
	}

	// Long
	public static Long getLong(Context context, Object key, Long defaultValue) {
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		try {
			return sharedPreferences.getLong(String.valueOf(key), defaultValue);
		} catch (Exception e) {
			DebugLog.loge(e);
			return defaultValue;
		}
	}

	public static void setLong(Context context, Object key, Long data) {
		try {
			sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			Editor editor = sharedPreferences.edit();
			editor.putLong(String.valueOf(key), data);
			editor.commit();
		} catch (Exception e) {
			DebugLog.loge(e);
		}
	}

	// Boolean
	public static Boolean getBoolean(Context context, Object key, Boolean defaultValue) {
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		try {
			return sharedPreferences.getBoolean(String.valueOf(key), defaultValue);
		} catch (Exception e) {
			DebugLog.loge(e);
			return defaultValue;
		}
	}

	public static void setBoolean(Context context, Object key, Boolean data) {
		try {
			sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			Editor editor = sharedPreferences.edit();
			editor.putBoolean(String.valueOf(key), data);
			editor.commit();
		} catch (Exception e) {
			DebugLog.loge(e);
		}
	}

	// Integer
	public static Integer getInt(Context context, Object key, Integer defaultValue) {
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		try {
			return sharedPreferences.getInt(String.valueOf(key), defaultValue);
		} catch (Exception e) {
			DebugLog.loge(e);
			return defaultValue;
		}
	}

	public static void setInt(Context context, Object key, Integer data) {
		try {
			sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			Editor editor = sharedPreferences.edit();
			editor.putInt(String.valueOf(key), data);
			editor.commit();
		} catch (Exception e) {
			DebugLog.loge(e);
		}
	}

	// Float
	public static Float getFloat(Context context, Object key, Float defaultValue) {
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		try {
			return sharedPreferences.getFloat(String.valueOf(key), defaultValue);
		} catch (Exception e) {
			DebugLog.loge(e);
			return defaultValue;
		}
	}

	public static void setFloat(Context context, Object key, Float data) {
		try {
			sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			Editor editor = sharedPreferences.edit();
			editor.putFloat(String.valueOf(key), data);
			editor.commit();
		} catch (Exception e) {
			DebugLog.loge(e);
		}
	}

}
