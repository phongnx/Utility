package com.utility.others;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.utility.DebugLog;

public class Data {
	private String key;
	private Object value;

	public Data(String key, Object value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public static Map<String, Object> parseToHashMap(ArrayList<Data> data) {
		Map<String, Object> params = new HashMap<String, Object>();
		for (Data obj : data) {
			params.put(obj.getKey(), obj.getValue());
		}
		return params;
	}

	public static ArrayList<Data> reverseFromHashMap(Map<String, String> data) {
		ArrayList<Data> params = new ArrayList<Data>();
		for (Map.Entry<String, String> entry : data.entrySet()) {
			params.add(new Data(entry.getKey(), entry.getValue()));
		}

		return params;
	}

	public static byte[] multipartBody(ArrayList<Data> data, ArrayList<Data> files) {
		byte[] multipartBody = null;

		boundary = "apiclient-" + System.currentTimeMillis();
		mimeType = "multipart/form-data;boundary=" + boundary;

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		try {
			if (data.size() > 0) {
				for (int i = 0; i < data.size(); i++) {
					buildTextPart(dos, data.get(i).getKey(), String.valueOf(data.get(i).getValue()));
					DebugLog.logn(data.get(i).getKey() + ": " + data.get(i).getValue());
				}
			}
			if (files.size() > 0) {
				for (int i = 0; i < files.size(); i++) {
					File file = new File(String.valueOf(files.get(i).value));
					buildPart(dos, files.get(i).getKey(), file.getName(), readFile(file));
					DebugLog.logn(files.get(i).getKey() + ": " + files.get(i).getValue());
				}
			}
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
			multipartBody = bos.toByteArray();
		} catch (Exception e) {
			DebugLog.loge(e);
		}

		return multipartBody;
	}

	static byte[] readFile(File file) {
		int size = (int) file.length();
		byte[] bytes = new byte[size];

		try {
			BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
			buf.read(bytes, 0, bytes.length);
			buf.close();
		} catch (Exception e) {
			DebugLog.loge(e);
		}
		return bytes;
	}

	static final String twoHyphens = "--";
	static final String lineEnd = "\r\n";
	static String boundary = "apiclient-" + System.currentTimeMillis();
	public static String mimeType = "multipart/form-data;boundary=" + boundary;

	static void buildTextPart(DataOutputStream dataOutputStream, String parameterName, String parameterValue) throws IOException {
		dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
		dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + parameterName + "\"" + lineEnd);
		dataOutputStream.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
		dataOutputStream.writeBytes(lineEnd);
		dataOutputStream.write(String.valueOf(parameterValue + lineEnd).getBytes("UTF-8"));
	}

	static void buildPart(DataOutputStream dataOutputStream, String parameterName, String fileName, byte[] fileData) throws IOException {
		dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
		dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + parameterName + "\"; filename=\"" + fileName + "\"" + lineEnd);
		dataOutputStream.writeBytes(lineEnd);

		ByteArrayInputStream fileInputStream = new ByteArrayInputStream(fileData);
		int bytesAvailable = fileInputStream.available();

		int maxBufferSize = 1 * 1024;
		int bufferSize = Math.min(bytesAvailable, maxBufferSize);
		byte[] buffer = new byte[bufferSize];

		// read file and write it into form...
		int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
		DebugLog.loge("File length: " + (bytesAvailable / (1024 * 1024)) + "M");
		try {
			while (bytesRead > 0) {
				try {
					dataOutputStream.write(buffer, 0, bufferSize);
				} catch (OutOfMemoryError e) {
					DebugLog.loge(e);
				}
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}
		} catch (Exception e) {
			DebugLog.loge(e);
		}
		dataOutputStream.writeBytes(lineEnd);
	}
}
