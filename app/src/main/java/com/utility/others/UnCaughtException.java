package com.utility.others;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.utility.DebugLog;
import com.utility.RuntimePermissions;
import com.utility.UtilsLib;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UnCaughtException implements UncaughtExceptionHandler {
    private String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "data0" + File.separator + "log_";
    private String TAG;
    private Context context;

    public UnCaughtException(Context ctx) {
        context = ctx;
        TAG = context.getPackageName();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        try {
            StringBuilder report = new StringBuilder();
            Date curDate = new Date();
            report.append("Error Report collected on : ").append(curDate.toString()).append('\n').append('\n');
            report.append("Information :").append('\n');
            report.append(UtilsLib.getInfoDevices(context));
            report.append('\n').append('\n');
            report.append("Stack:\n");
            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            e.printStackTrace(printWriter);
            report.append(result.toString());
            printWriter.close();
            report.append('\n');
            report.append("**** End of current Report ***");
            Log.e(UnCaughtException.class.getName(), ":\n" + report);
            recordErrorLog(report.toString());
        } catch (Throwable throwable) {
            Log.e(UnCaughtException.class.getName(), "Error while recordErrorLog: ", throwable);
        }
    }

    @SuppressLint("SimpleDateFormat")
    private void recordErrorLog(String content) {
        try {
            if (RuntimePermissions.checkAccessStoragePermission(context)) {
                File logFile = new File(path + "/" + TAG + "_error.log");
                if (!logFile.exists()) {
                    logFile.createNewFile();
                }
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");

                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                buf.append(sdf.format(new Date(System.currentTimeMillis()))).append("_").append(content);
                buf.newLine();
                buf.close();
            }
        } catch (Exception e) {
            DebugLog.loge(e);
        }
        System.exit(1);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}