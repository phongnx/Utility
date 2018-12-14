package com.utility.files;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;
import android.widget.Toast;

import com.utility.DebugLog;
import com.utility.R;
import com.utility.SharedPreference;
import com.utility.UtilsLib;

import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@SuppressLint("DefaultLocale")
public class FileUtils {
    private static final String SDCARD_NAME = "SDCARD_NAME";
    private static final String TREE_URI = "TREE_URI";
    public static final int REQUEST_CODE_GRANT_URI_PERMISSION = 113;
    private FileTransferListener mFileTransferListener;
    private long mTotalSize = 0;
    private long mTransferred = 0;
    private long mCurrentProgress = 0;

    private FileChannel inChannel;
    private FileChannel outChannel;

    private volatile boolean cancel = false;

    public void cancelTransfer() {
        cancel = true;
        try {
            if (inChannel != null) {
                inChannel.close();
                inChannel = null;
            }
            if (outChannel != null) {
                outChannel.close();
                outChannel = null;
            }
        } catch (Exception e) {
            DebugLog.loge(e);
        }
    }

    public void setFileTransferListener(FileTransferListener fileTransferListener) {
        mFileTransferListener = fileTransferListener;
        mTotalSize = 0;
        mTransferred = 0;
        mCurrentProgress = 0;
    }

    public static boolean isExistSDCard(Context context) {
        return !TextUtils.isEmpty(getPathSDCard(context));
    }

    public static String getPathSDCard(Context context) {
        List<String> sdCards = SDCardUtils.getSDCardPaths(context, true);
        if (!UtilsLib.isEmptyList(sdCards)) {
            return sdCards.get(0);
        }
        return "";
    }

    public static void openFile(Context context, File file) {
        Intent intent = new Intent();
        Uri uri = UtilsLib.getUriFromPath(context, file.getAbsolutePath());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        if (isVideoFile(file.getName())) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "video/*");
        } else if (isMusicFile(file.getName())) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "audio/*");
        } else if (isImageFile(file.getName())) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "image/*");
            context.startActivity(intent);
        } else if (isDocumentFile(file.getName())) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "text/plain");
        } else if (file.getName().endsWith(".apk")) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        } else if (isZipFile(file.getName())) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/zip");
        } else {
            intent = null;
            Toast.makeText(context, "Can't open file", Toast.LENGTH_LONG).show();
        }
        try {
            if (intent != null) {
                context.startActivity(intent);
            }
        } catch (Exception e) {
            DebugLog.loge(e);
        }
    }

    public static int getTotalFileInFolder(File folder) {
        File[] files = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.canRead() && !file.isHidden();
            }
        });
        if (files != null) {
            return files.length;
        }
        return 0;
    }

    public static Drawable getThumbnailIcon(Context context, String filePath) {
        Drawable icon = null;
        PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
        if (packageInfo != null) {
            ApplicationInfo appInfo = packageInfo.applicationInfo;
            if (Build.VERSION.SDK_INT >= 8) {
                appInfo.sourceDir = filePath;
                appInfo.publicSourceDir = filePath;
            }
            icon = appInfo.loadIcon(context.getPackageManager());
        }
        return icon;
    }

    public static long getTotalMemory(String path) {
        long totalSize = 0;
        try {
            DebugLog.logi("path: " + path);
            File file = new File(path);
            StatFs statFs = new StatFs(path);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                totalSize = file.getTotalSpace();
            } else {
                totalSize = statFs.getTotalBytes();
            }
            return totalSize;
        } catch (Exception e) {
            e.printStackTrace();
            return totalSize;
        }
    }

    public static long getFreeMemory(String path) {
        try {
            File file = new File(path);
            StatFs statFs = new StatFs(path);
            long sdFreeSize;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                sdFreeSize = file.getFreeSpace();
            } else {
                sdFreeSize = statFs.getFreeBytes();
            }
            return sdFreeSize;
        } catch (Exception e) {
            return 0;
        }
    }

    @SuppressLint("SimpleDateFormat")
    public static String getTimeCreateFile(String path, String simpleDateFormat) {
        File file = new File(path);
        Date timeCreate = new Date(file.lastModified());
        SimpleDateFormat format = new SimpleDateFormat(simpleDateFormat);
        return format.format(timeCreate);
    }

    public static String convertSizeFile(long size) {
        String str;
        DecimalFormat dec = new DecimalFormat("0.00");
        if (size >= 1024 * 1024 * 1024) {
            double gb = (((size / 1024.0) / 1024.0) / 1024.0);
            str = dec.format(gb).concat(Size.GB);
        } else if (size >= 1024 * 1024) {
            double mb = ((size / 1024.0) / 1024.0);
            str = dec.format(mb).concat(Size.MB);
        } else if (size > 1024.0) {
            double kb = size / 1024.0;
            str = dec.format(kb).concat(Size.KB);
        } else if (size > 0) {
            str = size + Size.B;
        } else {
            str = 0 + Size.Kb;
        }
        return str;
    }

    public static double getPercentageMemory(double totalSize, double freeSize) {
        return ((freeSize * 100) / totalSize);
    }

    public static long getFolderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                length += file.length();
            } else {
                length += getFolderSize(file);
            }
        }
        return length;
    }

    public static String[] getAllFileNameInPath(String pathFolder) {
        File folder = new File(pathFolder);
        if (folder.exists()) {
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    File file = new File(dir, filename);
                    return file.isFile() && !file.isHidden() && file.canRead();
                }
            };
            return folder.list(filter);
        }
        return null;
    }

    public static boolean isDocumentFile(String name) {
        for (int i = 0; i < FileTypes.documents.length; i++) {
            if (name.endsWith(FileTypes.documents[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean isImageFile(String name) {
        for (int i = 0; i < FileTypes.images.length; i++) {
            if (name.endsWith(FileTypes.images[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean isVideoFile(String name) {
        for (int i = 0; i < FileTypes.videos.length; i++) {
            if (name.endsWith(FileTypes.videos[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMusicFile(String name) {
        for (int i = 0; i < FileTypes.audios.length; i++) {
            if (name.endsWith(FileTypes.audios[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean isZipFile(String name) {
        for (int i = 0; i < FileTypes.zips.length; i++) {
            if (name.endsWith(FileTypes.zips[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean is7ZFile(String name) {
        if (name.endsWith(".7z")) {
            return true;
        }
        return false;
    }

    public static boolean isAPKFile(String name) {
        return name.toLowerCase().endsWith(".apk");
    }

    public static File[] getAllFolderInPath(String pathFolder) {
        File folder = new File(pathFolder);
        if (folder.exists()) {
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    return (sel.isDirectory()) && (!sel.isHidden() && sel.canRead());
                }
            };
            return folder.listFiles(filter);
        }
        return null;
    }

    public static File[] getAllFileInPath(String pathFolder) {
        File folder = new File(pathFolder);
        if (folder.exists()) {
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    return (sel.isFile()) && (!sel.isHidden() && sel.canRead());
                }
            };
            return folder.listFiles(filter);
        }
        return null;
    }

    public static File[] getAllFileInPathByType(String pathFolder, final FileType fileType) {
        File folder = new File(pathFolder);
        if (folder.exists()) {
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    File file = new File(dir, filename);
                    if (file.exists() && file.isFile() && !file.isHidden() && file.canRead()) {
                        if (fileType == FileType.DOCUMENT && isDocumentFile(filename)) {
                            return true;
                        }
                        if (fileType == FileType.IMAGE && isImageFile(filename)) {
                            return true;
                        }
                        if (fileType == FileType.VIDEO && isVideoFile(filename)) {
                            return true;
                        }
                        if (fileType == FileType.MUSIC && isMusicFile(filename)) {
                            return true;
                        }
                        if (fileType == FileType.ZIP && isZipFile(filename)) {
                            return true;
                        }
                        if (fileType == FileType.APK && isAPKFile(filename)) {
                            return true;
                        }
                        if (fileType == FileType.FOLDER) {
                            return true;
                        }
                    }
                    return false;
                }
            };
            return folder.listFiles(filter);
        }
        return null;
    }

    public static long getTotalSize(File[] files) {
        long size = 0;
        if (files != null) {
            for (File file : files) {
                try {
                    size += file.length();
                } catch (Exception e) {
                    DebugLog.loge(e);
                }
            }
        }
        return size;
    }

    public FileUtilsResult copyFilesInFolderByType(Context context, String sourceFolder, String outputFolder, FileType fileType) {
        if (isSDCardPath(context, outputFolder) && Build.VERSION.SDK_INT >= 21) {
            if (!isHavePermissionWithTreeUri(context)) {
                return new FileUtilsResult(false, context.getString(R.string.message_need_sdcard_access_permission));
            }
        }
        FileUtilsResult result;
        cancel = false;
        File[] files = getAllFileInPathByType(sourceFolder, fileType);
        if (files != null && files.length > 0) {
            if (mFileTransferListener != null) {
                mTotalSize = getTotalSize(files);
                mTransferred = 0;
                mCurrentProgress = 0;
            }
            File folderTarget = new File(outputFolder, new File(sourceFolder).getName());
            if (!folderTarget.exists()) {
                result = createFolder(context, outputFolder, folderTarget.getName());
                if (!result.isSuccess())
                    return new FileUtilsResult(false, context.getString(R.string.message_copy_failed));
            }
            for (File file : files) {
                if (!cancel) {
                    result = copyFile(context, file, folderTarget.getPath());
                    if (!result.isSuccess())
                        return new FileUtilsResult(false, context.getString(R.string.message_copy_failed));
                }
            }
        }
        setFileTransferListener(null);
        return new FileUtilsResult(true, context.getString(R.string.message_copy_success));
    }

    public FileUtilsResult moveFilesInFolderByType(Context context, String sourceFolder, String outputFolder, FileType fileType) {
        if ((isSDCardPath(context, outputFolder) || isSDCardPath(context, sourceFolder)) && Build.VERSION.SDK_INT >= 21) {
            if (!isHavePermissionWithTreeUri(context)) {
                return new FileUtilsResult(false, context.getString(R.string.message_need_sdcard_access_permission));
            }
        }

        cancel = false;
        File[] files = getAllFileInPathByType(sourceFolder, fileType);
        if (files != null && files.length > 0) {
            if (mFileTransferListener != null) {
                mTotalSize = getTotalSize(files);
                mTransferred = 0;
                mCurrentProgress = 0;
            }
            File folderTarget = new File(outputFolder, new File(sourceFolder).getName());
            if (!folderTarget.exists()) {
                createFolder(context, outputFolder, folderTarget.getName());
            }
            for (File file : files) {
                if (!cancel) {
                    FileUtilsResult object = copyFile(context, file, folderTarget.getPath());
                    if (object.isSuccess()) {
                        FileUtilsResult object1 = deleteFileOrFolder(context, file);
                        if (!object1.isSuccess()) {
                            return new FileUtilsResult(false, context.getString(R.string.message_move_failed));
                        }
                    } else {
                        return new FileUtilsResult(false, context.getString(R.string.message_move_failed));
                    }
                }
            }
        }
        setFileTransferListener(null);
        return new FileUtilsResult(true, context.getString(R.string.message_move_success));
    }

    public FileUtilsResult deleteFilesInFolderByType(Context context, String sourceFolder, FileType fileType) {
        if (context == null) {
            DebugLog.loge("Context is NULL");
            return new FileUtilsResult(false, "Context is NULL");
        }
        if (isSDCardPath(context, sourceFolder) && Build.VERSION.SDK_INT >= 21) {
            if (!isHavePermissionWithTreeUri(context)) {
                return new FileUtilsResult(false, context.getString(R.string.message_need_sdcard_access_permission));
            }
        }

        File[] files = getAllFileInPathByType(sourceFolder, fileType);
        if (files != null) {
            for (File file : files) {
                if (!deleteFileOrFolder(context, file).isSuccess()) {
                    return new FileUtilsResult(false, context.getString(R.string.message_delete_failed));
                }
            }
            return new FileUtilsResult(true, context.getString(R.string.message_delete_success));
        }
        return new FileUtilsResult(false, context.getString(R.string.message_delete_failed));
    }

    public FileUtilsResult moveFileOrFolder(Context context, File sourceLocation, String targetFolderLocation) {
        FileUtilsResult done;
        try {
            cancel = false;
            FileUtilsResult copy = copyFileOrFolder(context, sourceLocation, targetFolderLocation);

            FileUtilsResult delete = null;
            if (copy.isSuccess() && !cancel) {
                delete = deleteFileOrFolder(context, sourceLocation);
            }

            if (copy.isSuccess() && delete != null && delete.isSuccess()) {
                done = new FileUtilsResult(true, context.getString(R.string.message_move_success));
            } else {
                done = new FileUtilsResult(false, context.getString(R.string.message_move_failed));
            }
        } catch (Exception e) {
            DebugLog.loge(e);
            done = new FileUtilsResult(false, context.getString(R.string.message_move_failed));
        } finally {
            setFileTransferListener(null);
        }
        return done;
    }

    public FileUtilsResult copyFileOrFolder(@NonNull Context context, File sourceLocation, String targetFolderLocation) {
        FileUtilsResult copyFileFolder;
        try {
            if (context == null) {
                return new FileUtilsResult(false, "Context is NULL");
            }
            if (isSDCardPath(context, targetFolderLocation) && Build.VERSION.SDK_INT >= 21) {
                if (!isHavePermissionWithTreeUri(context)) {
                    return new FileUtilsResult(false, context.getString(R.string.message_need_sdcard_access_permission));
                }
            }

            cancel = false;
            if (sourceLocation.isDirectory()) {
                if (mFileTransferListener != null) {
                    mTotalSize = getFolderSize(sourceLocation);
                    mTransferred = 0;
                    mCurrentProgress = 0;
                }
                copyFileFolder = copyFolder(context, sourceLocation, targetFolderLocation);
            } else {
                if (mFileTransferListener != null) {
                    mTotalSize = sourceLocation.length();
                    mTransferred = 0;
                    mCurrentProgress = 0;
                }
                copyFileFolder = copyFile(context, sourceLocation, targetFolderLocation);
            }
            return copyFileFolder;
        } catch (Exception e) {
            DebugLog.loge(e);
            copyFileFolder = new FileUtilsResult(false, context.getString(R.string.message_copy_failed));
        } finally {
            setFileTransferListener(null);
        }
        return copyFileFolder;
    }

    private FileUtilsResult copyFolder(Context context, File sourceLocation, String targetFolderLocation) {
        FileUtilsResult result;
        FileUtilsResult failed = new FileUtilsResult(false, context.getString(R.string.message_copy_failed));
        try {
            if (cancel) {
                return new FileUtilsResult(false, context.getString(R.string.message_action_cancel));
            }
            File outputFolder = new File(targetFolderLocation, sourceLocation.getName());
            if (!outputFolder.exists()) {
                result = createFolder(context, targetFolderLocation, sourceLocation.getName());
                if (!result.isSuccess()) return failed;
            }

            File[] files = getAllFileInPath(sourceLocation.getPath());
            File[] folders = getAllFolderInPath(sourceLocation.getPath());
            if (files != null) {
                for (File file : files) {
                    if (!cancel) {
                        result = copyFile(context, file, outputFolder.getPath());
                        if (!result.isSuccess()) return failed;
                    }
                }
            }
            if (folders != null) {
                for (File folder : folders) {
                    if (!cancel) {
                        result = copyFolder(context, folder, outputFolder.getPath());
                        if (!result.isSuccess()) return failed;
                    }
                }
            }
        } catch (Exception e) {
            DebugLog.loge(e);
            return failed;
        }

        return new FileUtilsResult(true, context.getString(R.string.message_copy_success));
    }

    public FileUtilsResult deleteFileOrFolder(Context context, File fileDelete) {
        if (context == null) {
            return new FileUtilsResult(false, "Context is NULL");
        }
        if (isSDCardPath(context, fileDelete.getPath()) && Build.VERSION.SDK_INT >= 21) {
            if (isHavePermissionWithTreeUri(context)) {
                return deleteFileV21(context, fileDelete);
            } else {
                return new FileUtilsResult(false, context.getString(R.string.message_need_sdcard_access_permission));
            }
        }

        try {
            if (fileDelete.isDirectory()) {
                return deleteFolder(context, fileDelete);
            } else {
                if (fileDelete.delete()) {
                    MediaStoreUtils.addToMediaStore(context, fileDelete.getAbsolutePath());
                    return new FileUtilsResult(true, context.getString(R.string.message_delete_success));
                } else {
                    return new FileUtilsResult(true, context.getString(R.string.message_delete_failed));
                }
            }
        } catch (Exception e) {
            return new FileUtilsResult(true, context.getString(R.string.message_delete_failed));
        }
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);
        try {
            fileOrDirectory.delete();
        } catch (Exception e) {
            DebugLog.loge(e);
        }
    }

    private FileUtilsResult deleteFolder(Context context, File folder) {
        try {
            if (folder.exists()) {
                File[] files = folder.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteFolder(context, files[i]);
                    } else {
                        if (files[i].delete()) {
                            MediaStoreUtils.addToMediaStore(context, files[i].getAbsolutePath());
                        } else {
                            return new FileUtilsResult(false, context.getString(R.string.message_delete_failed));
                        }
                    }
                }
                if (!folder.getPath().equals(Environment.getExternalStorageDirectory() + "/Download")) {
                    if (folder.delete()) {
                        return new FileUtilsResult(true, context.getString(R.string.message_delete_success));
                    } else {
                        return new FileUtilsResult(false, context.getString(R.string.message_delete_failed));
                    }
                } else {
                    return new FileUtilsResult(false, context.getString(R.string.message_delete_folder_dont_delete_this_folder));
                }
            }
        } catch (Exception e) {
            DebugLog.loge(e);

        }
        return new FileUtilsResult(false, context.getString(R.string.message_delete_failed));
    }

    private FileUtilsResult deleteFileV21(Context context, File fileDelete) {
        try {
            DebugLog.loge("Path: " + fileDelete.getPath());
            Uri treeUri = Uri.parse(SharedPreference.getString(context, TREE_URI, ""));
            DocumentFile documentSDCard = DocumentFile.fromTreeUri(context, treeUri);
            String targetSDCard = getTargetSDCard(context, fileDelete.getPath());
            if (!targetSDCard.isEmpty()) {
                String[] parts = targetSDCard.trim().split("\\/");
                for (String part : parts) {
                    if (part.isEmpty()) continue;
                    DocumentFile nextDocument = documentSDCard.findFile(part);
                    if (nextDocument == null) {
                        return new FileUtilsResult(false, context.getString(R.string.message_delete_file_not_exist) + fileDelete.getPath());
                    }
                    documentSDCard = nextDocument;
                }
                DebugLog.loge("Delete: " + parts.length);
            }

            if (documentSDCard != null && documentSDCard.delete()) {
                DebugLog.loge("need Remove file in SDCard : " + documentSDCard.getUri().toString());
                deleteFileInSDCardFromMediaStore(context.getContentResolver(), fileDelete);
                //addToMediaStore(context, documentSDCard.getUri());
                return new FileUtilsResult(true, context.getString(R.string.message_delete_success));
            } else {
                return new FileUtilsResult(false, context.getString(R.string.message_delete_failed));
            }
        } catch (Exception e) {
            DebugLog.loge(e);
            return new FileUtilsResult(false, context.getString(R.string.message_delete_failed));
        }
    }

    public static void deleteFileInSDCardFromMediaStore(final ContentResolver contentResolver, final File file) {
        String canonicalPath;
        try {
            canonicalPath = file.getCanonicalPath();
        } catch (IOException e) {
            canonicalPath = file.getAbsolutePath();
        }
        final Uri uri = MediaStore.Files.getContentUri("external");
        final int result = contentResolver.delete(uri,
                MediaStore.Files.FileColumns.DATA + "=?", new String[]{canonicalPath});
        if (result == 0) {
            final String absolutePath = file.getAbsolutePath();
            if (!absolutePath.equals(canonicalPath)) {
                contentResolver.delete(uri,
                        MediaStore.Files.FileColumns.DATA + "=?", new String[]{absolutePath});
            }
        }
    }

    public static void copyFile(InputStream inputStream, OutputStream outputStream) {
        try {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            DebugLog.loge(e);
        }
    }

    public FileUtilsResult copyFile(@NonNull Context context, final File inputFile, final String outputFolder) {
        try {
            cancel = false;
            if (isSDCardPath(context, outputFolder) && Build.VERSION.SDK_INT >= 21) {
                if (isHavePermissionWithTreeUri(context)) {
                    return copyFileToSDCardV21(context, inputFile, outputFolder);
                } else {
                    return new FileUtilsResult(false, context.getString(R.string.message_need_sdcard_access_permission));
                }
            }

            if (!inputFile.exists()) {
                return new FileUtilsResult(false, context.getString(R.string.message_file_does_not_exist));
            }

            /*
             * Copy and move file in internal memory or SD card (SDK version < 21)
             * */
            File outputFile = new File(outputFolder, inputFile.getName());

            if (!outputFile.exists()) {
                outputFile.createNewFile();
            }


            if (mFileTransferListener == null) {
                inChannel = new FileInputStream(inputFile).getChannel();
                outChannel = new FileOutputStream(outputFile).getChannel();
                try {
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                } finally {
                    if (inChannel != null) {
                        inChannel.close();
                        inChannel = null;
                    }
                    if (outChannel != null) {
                        outChannel.close();
                        outChannel = null;
                    }
                }
            } else {
                FileInputStream fileInputStream = new FileInputStream(inputFile);
                FileOutputStream fileOuInputStream = new FileOutputStream(outputFile);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = fileInputStream.read(buffer)) != -1 && !cancel) {
                    fileOuInputStream.write(buffer, 0, read);
                    mTransferred += read;
                    publishProgress();
                }
                fileInputStream.close();
                fileOuInputStream.flush();
                fileOuInputStream.close();
            }

            MediaStoreUtils.addToMediaStore(context, outputFile.getAbsolutePath());
            //forceTellAndroidAboutNewFile(context, outputFile.getAbsolutePath());
            return new FileUtilsResult(true, context.getString(R.string.message_copy_success));
        } catch (Exception e) {
            return new FileUtilsResult(false, context.getString(R.string.message_copy_failed));
        }
    }

    private FileUtilsResult copyFileToSDCardV21(Context context, final File inputFile, final String outputFolder) {
        try {

            Uri treeUri = Uri.parse(SharedPreference.getString(context, TREE_URI, ""));
            DocumentFile documentOutputFile = DocumentFile.fromTreeUri(context, treeUri);
            String targetSDCard = getTargetSDCard(context, outputFolder);
            if (!targetSDCard.isEmpty()) {
                String[] parts = targetSDCard.split("\\/");
                for (String part : parts) {
                    DocumentFile nextDocument = documentOutputFile.findFile(part);
                    if (nextDocument == null) {
                        DebugLog.logd("createDirectory: " + part);
                        nextDocument = documentOutputFile.createDirectory(part);
                    }
                    documentOutputFile = nextDocument;
                }
            }

            DocumentFile existDocumentFile = documentOutputFile.findFile(inputFile.getName());
            if (existDocumentFile != null) {
                DebugLog.loge("File existed: " + inputFile.getName());
                return new FileUtilsResult(false, context.getString(R.string.message_copy_file_exist_destination) + inputFile.getName());
            }
            DocumentFile mDocumentFile = documentOutputFile.createFile("*/*", inputFile.getName());

            cancel = false;
            if (mFileTransferListener == null) {
                FileDescriptor fileDescriptor = context.getContentResolver()
                        .openFileDescriptor(mDocumentFile.getUri(), "w").getFileDescriptor();
                inChannel = new FileInputStream(inputFile).getChannel();
                outChannel = new FileOutputStream(fileDescriptor).getChannel();
                try {
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                } finally {
                    if (inChannel != null) {
                        inChannel.close();
                        inChannel = null;
                    }
                    if (outChannel != null) {
                        outChannel.close();
                        outChannel = null;
                    }
                }
            } else {
                FileInputStream fileInputStream = new FileInputStream(inputFile);
                OutputStream outputStream = context.getContentResolver().openOutputStream(mDocumentFile.getUri(), "w");
                byte[] buffer = new byte[1024];
                int read;
                while ((read = fileInputStream.read(buffer)) != -1 && !cancel) {
                    outputStream.write(buffer, 0, read);
                    mTransferred += read;
                    publishProgress();
                }
                fileInputStream.close();
                outputStream.flush();
                outputStream.close();
            }
            MediaStoreUtils.addToMediaStore(context, outputFolder + inputFile.getName());
            return new FileUtilsResult(true, context.getString(R.string.message_copy_success));
        } catch (FileNotFoundException e) {
            return new FileUtilsResult(false, context.getString(R.string.message_copy_failed));
        } catch (Exception e) {
            DebugLog.loge(e);
            return new FileUtilsResult(false, context.getString(R.string.message_copy_failed));
        }
    }

    public static FileUtilsResult createFolder(Context context, String parentPath, String name) {
        if (!FileUtils.isValidName(name) && !FileUtils.isValidName(parentPath)) {
            return new FileUtilsResult(false, context.getString(R.string.message_create_failed) + name);
        }
        if (isSDCardPath(context, parentPath) && Build.VERSION.SDK_INT >= 21) {
            if (isHavePermissionWithTreeUri(context)) {
                return createFolderSDCardV21(context, name, parentPath);
            } else {
                return new FileUtilsResult(false, context.getString(R.string.message_need_sdcard_access_permission));
            }
        } else {
            try {
                File file = new File(parentPath, name);
                if (file.exists()) {
                    return new FileUtilsResult(false, context.getString(R.string.message_folder_exist));
                }
                if (file.mkdirs()) {
                    return new FileUtilsResult(true, context.getString(R.string.message_create_folder_success));
                } else {
                    return new FileUtilsResult(false, context.getString(R.string.message_create_folder_failed));
                }
            } catch (Exception e) {
                return new FileUtilsResult(false, context.getString(R.string.message_create_folder_failed));
            }
        }
    }

    private static FileUtilsResult createFolderSDCardV21(Context context, String folderName, String parentPath) {
        try {
            if (isFileExitedInSDCardV21(context, parentPath + "/" + folderName)) {
                return new FileUtilsResult(false, context.getString(R.string.message_folder_exist));
            }
            Uri treeUri = Uri.parse(SharedPreference.getString(context, TREE_URI, ""));
            DocumentFile documentOutputFile = DocumentFile.fromTreeUri(context, treeUri);
            String targetSDCard = getTargetSDCard(context, parentPath + "/" + folderName);
            if (!targetSDCard.isEmpty()) {
                String[] parts = targetSDCard.split("\\/");
                for (String part : parts) {
                    DocumentFile nextDocument = documentOutputFile.findFile(part);
                    if (nextDocument == null) {
                        nextDocument = documentOutputFile.createDirectory(part);
                    }
                    documentOutputFile = nextDocument;
                }
            }
            if (documentOutputFile != null) {
                return new FileUtilsResult(true, context.getString(R.string.message_create_folder_success));
            } else {
                return new FileUtilsResult(false, context.getString(R.string.message_create_folder_failed));
            }
        } catch (Exception e) {
            DebugLog.loge(e);
            return new FileUtilsResult(false, context.getString(R.string.message_create_folder_failed));
        }

    }

    public static FileUtilsResult renameFile(final Context context, String newName, String path) {
        if (isSDCardPath(context, path) && Build.VERSION.SDK_INT >= 21) {
            if (isHavePermissionWithTreeUri(context)) {
                return renameFileSDCardV21(context, newName, path);
            } else {
                return new FileUtilsResult(false, context.getString(R.string.message_need_sdcard_access_permission));
            }
        }

        File from = new File(path);
        if (!FileUtils.isValidName(newName)) {
            DebugLog.loge("invalid name");
            return new FileUtilsResult(false, context.getString(R.string.message_rename_failed_to) + newName);
        }
        if (!from.exists()) {
            DebugLog.loge("File From not exist");
            return new FileUtilsResult(false, context.getString(R.string.message_file_does_not_exist));
        }

        try {
            File to = new File(from.getParent(), newName);
            if (to.exists()) {
                DebugLog.loge("File To exist");
                return new FileUtilsResult(false, context.getString(R.string.message_file_exist));
            }

            List<String> listFileFrom = getAllFileRecusiveInFolder(from.getPath());
            if (from.renameTo(to)) {
                //TODO : Update all Files insize to/from file.
                List<String> listFileTo = getAllFileRecusiveInFolder(to.getPath());
                for (String removePath : listFileFrom)
                    MediaStoreUtils.removeFromMediaStore(context, removePath);
                for (String addPath : listFileTo) MediaStoreUtils.addToMediaStore(context, addPath);

                return new FileUtilsResult(true, context.getString(R.string.message_rename_success));
            } else {
                return new FileUtilsResult(false, context.getString(R.string.message_rename_failed_to) + newName);
            }
        } catch (Exception e) {
            DebugLog.loge(e);
            return new FileUtilsResult(false, context.getString(R.string.message_rename_failed_to) + newName);
        }
    }

    private static FileUtilsResult renameFileSDCardV21(Context context, String newName, String pathFileOrigin) {
        try {
            String targetSDCard = getTargetSDCard(context, pathFileOrigin);
            String parentFolder = new File(pathFileOrigin).getParent();
            File file = new File(parentFolder, newName);
            Uri treeUri = Uri.parse(SharedPreference.getString(context, TREE_URI, ""));
            DocumentFile documentOutputFile = DocumentFile.fromTreeUri(context, treeUri);
            if (!targetSDCard.isEmpty()) {
                String[] parts = targetSDCard.split("\\/");
                for (String part : parts) {
                    DocumentFile nextDocument = documentOutputFile.findFile(part);
                    if (nextDocument == null) {
                        return new FileUtilsResult(false, context.getString(R.string.message_permission_denied));
                    }
                    documentOutputFile = nextDocument;
                }
            }
            if (documentOutputFile.renameTo(newName)) {
                MediaStoreUtils.removeFromMediaStore(context, pathFileOrigin);
                MediaStoreUtils.addToMediaStore(context, file.getPath());
                return new FileUtilsResult(true, context.getString(R.string.message_rename_success));
            } else {
                return new FileUtilsResult(false, context.getString(R.string.message_rename_failed_to) + newName);
            }
        } catch (Exception e) {
            DebugLog.loge(e);
            return new FileUtilsResult(false, context.getString(R.string.message_rename_failed_to) + newName);
        }
    }

    public static boolean isSDCardPath(Context context, String path) {
        try {
            String pathSDCard = getPathSDCard(context);
            if (pathSDCard != null && !pathSDCard.isEmpty() && path.startsWith(pathSDCard)) {
                return true;
            }
        } catch (Exception e) {
            DebugLog.loge(e);
        }
        return false;
    }

    public static boolean isTreeUri(String treeUri) {
        try {
            String[] tree = treeUri.split("\\%3A");
            DebugLog.loge("treeUri: " + treeUri);
            DebugLog.loge("tree: " + tree.length);
            if (tree.length == 1) {
                return true;
            }
        } catch (Exception e) {
            DebugLog.loge(e);
        }
        return false;
    }

    public static boolean isHavePermissionWithTreeUri(Context context) {
        if (context == null) {
            DebugLog.logd("Permission deny");
            return false;
        }
        String tree_uri = SharedPreference.getString(context.getApplicationContext(), TREE_URI, null);
        DebugLog.logd("Root Path : " + tree_uri);
        if (Build.VERSION.SDK_INT >= 21 && (tree_uri == null || tree_uri.isEmpty() /*|| !tree_uri.contains(getSdcardName(context))*/)) {
            DebugLog.logd("Permission deny");
            return false;
        }
        DebugLog.logd("Has Permission");
        return true;
    }

    public static void requestTreeUriPermission(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= 21) {
                ((Activity) context).startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), REQUEST_CODE_GRANT_URI_PERMISSION);
            }
        } catch (Exception e) {
            DebugLog.loge(e);
        }
    }

    public static void precessRequestTreeUriPermissionResult(Context context, int requestCode, int resultCode, Intent resultData) {
        if (Build.VERSION.SDK_INT >= 21) {
            if (requestCode == REQUEST_CODE_GRANT_URI_PERMISSION && resultCode == Activity.RESULT_OK) {
                Uri treeUri = resultData.getData();
                if (isTreeUri(treeUri.toString())) {
                    context.grantUriPermission(context.getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    context.getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    SharedPreference.setString(context, TREE_URI, treeUri.toString());
                }
            }
        }
    }

    public static void resetSDCardPermission(Context context) {
        SharedPreference.setString(context, TREE_URI, "");
    }

    public static String getSdcardName(Context context) {
        String pathSdCard = getPathSDCard(context);
        if (TextUtils.isEmpty(pathSdCard)) {
            return "";
        }
        File file = new File(pathSdCard);
        return file.getName();
    }

    private static boolean isFileExitedInSDCardV21(Context context, String path) {
        try {
            Uri treeUri = Uri.parse(SharedPreference.getString(context, TREE_URI, ""));
            DocumentFile documentOutputFile = DocumentFile.fromTreeUri(context, treeUri);
            String targetSDCard = getTargetSDCard(context, path);
            if (!targetSDCard.isEmpty()) {
                String[] parts = targetSDCard.split("\\/");
                for (String part : parts) {
                    DocumentFile nextDocument = documentOutputFile.findFile(part);
                    if (nextDocument == null) {
                        return false;
                    }
                    documentOutputFile = nextDocument;
                }
            }
        } catch (Exception e) {
            DebugLog.loge(e);
        }
        return true;
    }

    private static String getTargetSDCard(Context context, String outputPath) {
        try {
            String sdcardName = getSdcardName(context);
            int index = outputPath.indexOf(sdcardName);
            String targetSDCard = "";
            try {
                targetSDCard = outputPath.substring(index + sdcardName.length()).trim();
            } catch (Exception e) {
                DebugLog.loge(e);
            }
            if (targetSDCard.length() > 0) {
                targetSDCard = targetSDCard.substring(1).trim();
            }
            return targetSDCard;
        } catch (Exception e) {
            DebugLog.loge(e);
        }
        return "";
    }

    public static Uri getUriFileSDCardV24(Context context, String path) {
        if (!FileUtils.isHavePermissionWithTreeUri(context)) {
            return null;
        } else {
            String targetSDCard = getTargetSDCard(context, path);
            Uri treeUri = Uri.parse(SharedPreference.getString(context, TREE_URI, ""));
            DocumentFile documentOutputFile = DocumentFile.fromTreeUri(context, treeUri);
            if (documentOutputFile == null) return null;
            if (!targetSDCard.isEmpty()) {
                String[] parts = targetSDCard.split("\\/");
                for (int i = 0; i < parts.length; i++) {
                    DocumentFile nextDocument = documentOutputFile.findFile(parts[i]);
                    if (nextDocument == null) {
                        return null;
                    }
                    documentOutputFile = nextDocument;
                }
            }
            return documentOutputFile.getUri();
        }
    }

    /**
     * Kiểm tra xem các thư mục cha có chứa bất kì file .nomedia nào không
     * Nếu tìm thấy thì trả về path của folder chứa file .nomedia
     * Nếu không tìm thấy thì trả về null
     *
     * @param pathFolder
     * @return path/null
     */
    public static String isSuperParentFolderContainNomediaFile(String pathFolder) {
        try {
            if (pathFolder.contains("/.")) {
                DebugLog.loge("NoMediaFile" + pathFolder);
                return pathFolder.substring(0, pathFolder.indexOf("/.") + 1);
            }
            File folder = new File(pathFolder);
            if (containsNomediaFile(folder)) {
                return pathFolder;
            }
            while (folder.getParentFile() != null) {
                folder = folder.getParentFile();
                if (containsNomediaFile(folder)) {
                    return folder.getAbsolutePath();
                }
            }
        } catch (Exception e) {
            DebugLog.loge(e);
        }
        return null;
    }

    public static boolean containsNomediaFile(File folder) {
        return new File(folder.getAbsoluteFile(), ".nomedia").exists();
    }

    public static boolean isNomediaFolder(File folder, List<String> nomediaPaths, List<String> mediaPaths) {
        if (mediaPaths.contains(folder.getAbsolutePath())) {
            return false;
        }
        if (nomediaPaths.contains(folder.getAbsolutePath())) {
            return true;
        }
        String pathNomediaFolder = isSuperParentFolderContainNomediaFile(folder.getAbsolutePath());
        if (pathNomediaFolder != null) {
            nomediaPaths.add(pathNomediaFolder);
            return true;
        }
        mediaPaths.add(folder.getAbsolutePath());
        return false;
    }

    private void publishProgress() {
        if (mFileTransferListener != null) {
            try {
                int progress = (int) ((mTransferred * 100) / mTotalSize);
                if (progress > mCurrentProgress) {
                    mCurrentProgress = progress;
                    mFileTransferListener.onProgress(progress);
                }
            } catch (Exception e) {
                DebugLog.loge(e);
            }
        }
    }

    private interface Size {
        String Kb = "Kb";
        String B = "B";
        String KB = "KB";
        String MB = "MB";
        String GB = "GB";
        String TB = "TB";
    }

    public enum FileType {
        FOLDER,
        DOCUMENT,
        IMAGE,
        VIDEO,
        MUSIC,
        ZIP,
        APK,
    }

    public interface FileTransferListener {
        void onProgress(int progress); // percent (ex: 1/100)
    }

    private static final String ANSI_INVALID_CHARACTERS = "\\:*?\"<>|";

    public static boolean isValidName(String name) {
        for (int i = 0; i < ANSI_INVALID_CHARACTERS.length(); i++) {
            if (name.contains(String.valueOf(ANSI_INVALID_CHARACTERS.charAt(i)))) return false;
        }
        return true;
    }

    public static FileUtilsResult saveTextToFile(Context context, String path, String content) {
        if (FileUtils.isSDCardPath(context, path)) {
            if (!FileUtils.isHavePermissionWithTreeUri(context)) {
                return new FileUtilsResult(false, context.getString(R.string.message_permission_denied));
            } else {
                //TODO : Save file to SDCard path.
                Uri treeUri = Uri.parse(SharedPreference.getString(context, TREE_URI, ""));
                DocumentFile documentOutputFile = DocumentFile.fromTreeUri(context, treeUri);
                String targetSDCard = getTargetSDCard(context, path);
                if (!targetSDCard.isEmpty()) {
                    String[] parts = targetSDCard.split("\\/");
                    for (int i = 0; i < parts.length - 1; i++) {
                        DocumentFile nextDocument = documentOutputFile.findFile(parts[i]);
                        if (nextDocument == null) {
                            DebugLog.logd("createDirectory: " + parts[i]);
                            nextDocument = documentOutputFile.createDirectory(parts[i]);
                        }
                        documentOutputFile = nextDocument;
                    }
                }
                String fileName = getFileNameFromPath(path);
                DebugLog.logd("fileName: " + fileName);

                DocumentFile document = documentOutputFile.findFile(fileName);
                if (document != null && document.exists()) {
                    document.delete();
                }

                DocumentFile mDocumentFile = documentOutputFile.createFile("*/*", fileName);
                DebugLog.logd("saveFile Uri : " + mDocumentFile.getUri());
                if (!mDocumentFile.canWrite()) {
                    return new FileUtilsResult(false, context.getString(R.string.message_save_to_file_failed));
                }
                try {
                    OutputStream outputStream = context.getContentResolver().openOutputStream(mDocumentFile.getUri(), "w");
                    outputStream.write(content.getBytes());
                    outputStream.flush();
                    outputStream.close();
                } catch (Exception e) {
                    DebugLog.loge(e);
                    return new FileUtilsResult(false, context.getString(R.string.message_save_to_file_failed));
                }
            }
        } else {
            try {
                FileOutputStream fileOuInputStream = new FileOutputStream(new File(path), false);
                fileOuInputStream.write(content.getBytes());
                fileOuInputStream.flush();
                fileOuInputStream.close();
            } catch (Exception e) {
                DebugLog.loge(e);
                return new FileUtilsResult(false, context.getString(R.string.message_save_to_file_failed));
            }
        }
        return new FileUtilsResult(true, context.getString(R.string.message_save_file_success));
    }

    public static String getFileNameFromPath(String path) {
        if (!TextUtils.isEmpty(path)) {
            path = path.substring(TextUtils.lastIndexOf(path, '/') + 1);
            return path;
        }
        return path;
    }

    public void zipAtFolder(String srcFolder, String pathZipFile) throws Exception {
        FileOutputStream fileWriter = new FileOutputStream(pathZipFile);
        ZipOutputStream zip = new ZipOutputStream(fileWriter);
        addTreeFileToZip("", srcFolder, zip);
        zip.flush();
        zip.close();
    }

    public void zipMultiFileAndFolder(List<String> listFolder, String pathZipFile) throws Exception {
        FileOutputStream fileWriter = new FileOutputStream(pathZipFile);
        ZipOutputStream zipOutputStream = new ZipOutputStream(fileWriter);
        for (int i = 0; i < listFolder.size(); i++) {
            addTreeFileToZip("", listFolder.get(i), zipOutputStream);
        }
        zipOutputStream.flush();
        zipOutputStream.close();
    }

    private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) throws Exception {
        File folder = new File(srcFolder);
        if (folder.list().length != 0) {
            for (String fileName : folder.list()) {
                if (path.equals("")) {
                    addTreeFileToZip(folder.getName(), srcFolder + "/" + fileName, zip);
                } else {
                    addTreeFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip);
                }
            }
        } else {
            zip.putNextEntry(new ZipEntry(path + "/" + folder.getName() + "/"));
        }
    }

    private void addTreeFileToZip(String path, String srcFile, ZipOutputStream zip) throws Exception {
        File folder = new File(srcFile);
        if (folder.isDirectory()) {
            addFolderToZip(path, srcFile, zip);
        } else {
            byte[] buf = new byte[1024];
            int len;
            FileInputStream in = new FileInputStream(srcFile);
            zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
            while ((len = in.read(buf)) > 0) {
                zip.write(buf, 0, len);
            }
        }
    }

    public FileUtilsResult zipMultiFolderOrFile(Context context, List<String> listPathSource, String pathFileZip) {
        if (isSDCardPath(context, listPathSource.get(0)) && Build.VERSION.SDK_INT >= 21) {
            if (isHavePermissionWithTreeUri(context)) {
                return zipMultiFileToSDCardV21(context, listPathSource, pathFileZip);
            } else {
                return new FileUtilsResult(false, context.getString(R.string.message_need_sdcard_access_permission));
            }
        }
        try {
            zipMultiFileAndFolder(listPathSource, pathFileZip);
            MediaStoreUtils.addToMediaStore(context, pathFileZip);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new FileUtilsResult(true, context.getString(R.string.message_zip_success));
    }

    public FileUtilsResult zipFolderOrFile(Context context, String pathSource, String pathFileZip) {
        if (isSDCardPath(context, pathSource) && Build.VERSION.SDK_INT >= 21) {
            if (isHavePermissionWithTreeUri(context)) {
                return zipFileToSDCardV21(context, pathSource, pathFileZip);
            } else {
                return new FileUtilsResult(false, context.getString(R.string.message_need_sdcard_access_permission));
            }
        }
        try {
            zipAtFolder(pathSource, pathFileZip);
            MediaStoreUtils.addToMediaStore(context, pathFileZip);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new FileUtilsResult(true, context.getString(R.string.message_zip_success));
    }

    public FileUtilsResult zipMultiFileToSDCardV21(Context context, List<String> listPathSource, String pathFileZip) {
        File fileZipTarget = new File(pathFileZip);
        Uri treeUri = Uri.parse(SharedPreference.getString(context, TREE_URI, ""));
        DocumentFile documentOutputFile = DocumentFile.fromTreeUri(context, treeUri);
        String targetSDCard = getTargetSDCard(context, pathFileZip);
        if (!targetSDCard.isEmpty()) {
            String[] parts = targetSDCard.split("\\/");
            for (String part : parts) {
                DocumentFile nextDocument = documentOutputFile.findFile(part);
                if (nextDocument == null) {
                    nextDocument = documentOutputFile.createFile("*/*", part);
                }
                documentOutputFile = nextDocument;
            }
        }
        try {
            OutputStream outputStream = context.getContentResolver().openOutputStream(documentOutputFile.getUri(), "w");
            Closeable res = outputStream;
            try {
                ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
                res = zipOutputStream;
                for (int i = 0; i < listPathSource.size(); i++) {
                    addTreeFileToZip("", listPathSource.get(i), zipOutputStream);
                }
                MediaStoreUtils.addToMediaStore(context, fileZipTarget.getAbsolutePath());
            } finally {
                res.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new FileUtilsResult(true, context.getString(R.string.message_zip_success));
    }


    public FileUtilsResult zipFileToSDCardV21(Context context, String pathSource, String pathFileZip) {
        File fileZipTarget = new File(pathFileZip);
        File inputFile = new File(pathSource);
        Uri treeUri = Uri.parse(SharedPreference.getString(context, TREE_URI, ""));
        DocumentFile documentOutputFile = DocumentFile.fromTreeUri(context, treeUri);
        String targetSDCard = getTargetSDCard(context, pathFileZip);
        if (!targetSDCard.isEmpty()) {
            String[] parts = targetSDCard.split("\\/");
            for (String part : parts) {
                DocumentFile nextDocument = documentOutputFile.findFile(part);
                if (nextDocument == null) {
                    nextDocument = documentOutputFile.createFile("*/*", part);
                }
                documentOutputFile = nextDocument;
            }
        }
        DocumentFile existDocumentFile = documentOutputFile.findFile(inputFile.getName());
        if (existDocumentFile != null) {
            return new FileUtilsResult(false, context.getString(R.string.message_copy_file_exist_destination) + inputFile.getName());
        }
        try {
            OutputStream outputStream = context.getContentResolver().openOutputStream(documentOutputFile.getUri(), "w");
            Closeable res = outputStream;
            try {
                ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
                res = zipOutputStream;
                addTreeFileToZip("", pathSource, zipOutputStream);
                MediaStoreUtils.addToMediaStore(context, fileZipTarget.getAbsolutePath());
            } finally {
                res.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new FileUtilsResult(true, context.getString(R.string.message_zip_success));
    }

    public static void writeFileOutPutStream(File fileChild, ZipOutputStream outputStream) throws IOException {
        InputStream in = new FileInputStream(fileChild);
        try {
            byte[] buffer = new byte[1024];
            while (true) {
                int readCount = in.read(buffer);
                if (readCount < 0) {
                    break;
                }
                outputStream.write(buffer, 0, readCount);
            }
        } finally {
            in.close();
        }
    }

    /**
     * Extract File 7Z
     *
     * @param context
     * @param pathZip
     * @param pathFolder
     * @return
     */
    public static FileUtilsResult extractFile7ZToSdcardV21(Context context, String pathZip, String pathFolder) {
        try {
            Uri treeUri = Uri.parse(SharedPreference.getString(context, TREE_URI, ""));
            DocumentFile documentOutputFile = DocumentFile.fromTreeUri(context, treeUri);
            String targetSDCard = getTargetSDCard(context, pathZip);
            if (!targetSDCard.isEmpty()) {
                String[] parts = targetSDCard.split("\\/");
                for (String part : parts) {
                    DocumentFile nextDocument = documentOutputFile.findFile(part);
                    if (nextDocument == null) {
                        nextDocument = documentOutputFile.createDirectory(part);
                    }
                    documentOutputFile = nextDocument;
                }
            }
            DocumentFile documentFile = documentOutputFile.createFile("*/*", pathZip);
            OutputStream out = context.getContentResolver().openOutputStream(documentFile.getUri(), "w");
            //String cmd = Command.getExtractCmd(pathZip, pathDirectory);
            //P7ZipApi.executeCommand(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new FileUtilsResult(true, context.getString(R.string.message_extract_success));
    }

    public FileUtilsResult extractFileCompress(Context context, String pathZip, String pathDirectory) {
        File fileZip = new File(pathZip);
        File folder = new File(pathDirectory);
        if (isSDCardPath(context, pathZip) && Build.VERSION.SDK_INT >= 21) {
            if (isHavePermissionWithTreeUri(context)) {
                return extractFileToSdcardV21(context, fileZip, folder);
            } else {
                return new FileUtilsResult(false, context.getString(R.string.message_need_sdcard_access_permission));
            }
        }
        if (folder.exists()) {
            return new FileUtilsResult(false, context.getString(R.string.message_file_exist));
        }
        try {
            ZipFile zipFile = new ZipFile(fileZip);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File file = new File(folder, entry.getName());
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    file.getParentFile().mkdirs();
                    InputStream in = zipFile.getInputStream(entry);
                    try {
                        copyExtractFile(in, file);
                    } finally {
                        in.close();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new FileUtilsResult(true, context.getString(R.string.message_extract_success));

    }

    private FileUtilsResult extractFileToSdcardV21(Context context, File fileZip, File folder) {
        try {
            ZipFile zipFile = new ZipFile(fileZip);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            if (folder.exists()) {
                return new FileUtilsResult(false, context.getString(R.string.message_file_exist));
            }
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File file = new File(folder, entry.getName());

                if (entry.isDirectory()) {
                    FileUtils.createFolder(context, folder.getPath(), entry.getName());
                } else {
                    InputStream in = zipFile.getInputStream(entry);
                    try {
                        copyExtractFileToSdcardV21(context, in, entry.getName(), file.getParentFile());
                    } finally {
                        in.close();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new FileUtilsResult(true, context.getString(R.string.message_extract_success));
    }

    /**
     * @param context
     * @param in
     * @param fileName
     * @param folderTarget
     */
    private static void copyExtractFileToSdcardV21(Context context, InputStream in, String fileName, File folderTarget) {
        try {
            Uri treeUri = Uri.parse(SharedPreference.getString(context, TREE_URI, ""));
            DocumentFile documentOutputFile = DocumentFile.fromTreeUri(context, treeUri);
            String targetSDCard = getTargetSDCard(context, folderTarget.getAbsolutePath());
            if (!targetSDCard.isEmpty()) {
                String[] parts = targetSDCard.split("\\/");
                for (String part : parts) {
                    DocumentFile nextDocument = documentOutputFile.findFile(part);
                    if (nextDocument == null) {
                        nextDocument = documentOutputFile.createDirectory(part);
                    }
                    documentOutputFile = nextDocument;
                }
            }
            String[] partFiles = fileName.split("/");
            DocumentFile documentFile = documentOutputFile.createFile("*/*", partFiles[1]);
            OutputStream out = context.getContentResolver().openOutputStream(documentFile.getUri(), "w");
            byte[] buffer = new byte[1024];
            while (true) {
                int readCount = in.read(buffer);
                if (readCount < 0) {
                    break;
                }
                out.write(buffer, 0, readCount);
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyExtractFile(InputStream in, File file) throws IOException {
        OutputStream out = new FileOutputStream(file);
        try {
            byte[] buffer = new byte[1024];
            while (true) {
                int readCount = in.read(buffer);
                if (readCount < 0) {
                    break;
                }
                out.write(buffer, 0, readCount);
            }
        } finally {
            out.close();
        }
    }

    private static List<String> getAllFileRecusiveInFolder(String path) {
        Stack<File> stackFile = new Stack<>();
        List<String> listFile = new ArrayList<>();
        File root = new File(path);
        if (root.isFile()) listFile.add(path);
        else stackFile.push(root);
        while (!stackFile.isEmpty()) {
            File currentFile = stackFile.pop();
            for (File file : currentFile.listFiles()) {
                if (file.isDirectory()) stackFile.push(file);
                else if (file.isFile()) listFile.add(file.getPath());
            }
        }
        return listFile;
    }
}
