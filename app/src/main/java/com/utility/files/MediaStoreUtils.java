package com.utility.files;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.utility.DebugLog;
import com.utility.UtilsLib;

import java.io.File;

/**
 * Created by Phong on 1/15/2018.
 */

public class MediaStoreUtils {

    public static void removeFromMediaStore(Context context, String path) {
        DebugLog.logd("Remove from store : " + path);
        ContentResolver resolver = context.getContentResolver();
        if (FileUtils.isImageFile(path)) {
            resolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Images.Media.DATA + "=?", new String[]{path});
        } else if (FileUtils.isMusicFile(path)) {
            resolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Audio.Media.DATA + "=?", new String[]{path});
        } else if (FileUtils.isVideoFile(path)) {
            resolver.delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Video.Media.DATA + "=?", new String[]{path});
        } else if (FileUtils.isDocumentFile(path)) {
            resolver.delete(MediaStore.Files.getContentUri("external"),
                    MediaStore.MediaColumns.DATA + "=?", new String[]{path});
        }
    }

    public static void addToMediaStore(final Context context, String filePath) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Uri uri = UtilsLib.getUriFromPath(context, filePath);
            DebugLog.logd("Add to store : " + filePath + "\n uri: " + uri.toString());
            MediaStoreUtils.addToMediaStore(context, uri);
        } else {
            MediaStoreUtils.addToMediaStoreV24(context, filePath);
        }
    }

    public static void addToMediaStore(Context context, Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(uri);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            DebugLog.loge(e);
        }
    }

    private static void addToMediaStoreV24(Context context, String filePath) {
        File file = new File(filePath);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, file.getName());
        values.put(MediaStore.Images.Media.DISPLAY_NAME, file.getName());
        values.put(MediaStore.Images.Media.MIME_TYPE, UtilsLib.getMimeType(filePath));
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATE_MODIFIED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATA, filePath);
        Uri contentUri;

        if (FileUtils.isImageFile(filePath)) {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if (FileUtils.isMusicFile(filePath)) {
            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        } else if (FileUtils.isVideoFile(filePath)) {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else {
            contentUri = MediaStore.Files.getContentUri("external");
        }
        context.getContentResolver().insert(contentUri, values);
    }
}
