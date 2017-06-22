package com.utility;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.util.Log;

public class DebugLog {
	private static final String TAG = "DebugLog";
	public static boolean DEBUG = true;

	public static void logd(Object obj) {
		if (obj == null || !DEBUG)
			return;
		String message = String.valueOf(obj);
		String fullClassName = Thread.currentThread().getStackTrace()[3].getClassName();
		String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
		if (className.contains("$")) {
			className = className.substring(0, className.lastIndexOf("$"));
		}
		String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
		int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();

		Log.d(TAG, "at (" + className + ".java:" + lineNumber + ") " + "[" + methodName + "]" + message);
	}

	public static void logn(Object obj) {
		if (obj == null || !DEBUG)
			return;
		String message = String.valueOf(obj);
		String fullClassName = Thread.currentThread().getStackTrace()[3].getClassName();
		String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
		if (className.contains("$")) {
			className = className.substring(0, className.lastIndexOf("$"));
		}
		String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
		int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();

		Log.i(TAG, "at (" + className + ".java:" + lineNumber + ") " + "[" + methodName + "]" + message);
	}

	public static void loge(Object obj) {
		if (obj == null || !DEBUG)
			return;
		String message = String.valueOf(obj);
		String fullClassName = Thread.currentThread().getStackTrace()[3].getClassName();
		String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
		if (className.contains("$")) {
			className = className.substring(0, className.lastIndexOf("$"));
		}
		String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
		int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();

		Log.e(TAG, "at (" + className + ".java:" + lineNumber + ") " + "[" + methodName + "]" + message);
	}

	public static void loge(Exception e) {
		if (e == null || !DEBUG)
			return;
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));

		String message = errors.toString();

		String fullClassName = Thread.currentThread().getStackTrace()[3].getClassName();
		String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
		if (className.contains("$")) {
			className = className.substring(0, className.lastIndexOf("$"));
		}
		String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
		int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();

		Log.e(TAG, "at (" + className + ".java:" + lineNumber + ") " + "[" + methodName + "]" + message);
	}

	public static void logi(Object obj) {
		if (obj == null || !DEBUG)
			return;
		String message = String.valueOf(obj);
		String fullClassName = Thread.currentThread().getStackTrace()[3].getClassName();
		String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
		if (className.contains("$")) {
			className = className.substring(0, className.lastIndexOf("$"));
		}
		String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
		int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();

		Log.i(TAG, "at (" + className + ".java:" + lineNumber + ") " + "[" + methodName + "]" + message);
	}

}
