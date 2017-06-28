package com.utility;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.DocumentFile;
import android.widget.Toast;

import com.utility.others.TypesFile;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressLint("DefaultLocale")
public class FileUtils {
    public static final String SDCARD_NAME = "SDCARD_NAME";
    public static final String TREE_URI = "TREE_URI";
    public static final int REQUEST_CODE_GRANT_URI_PERMISSION = 1144;
    private static FileTransferListener fileTransferListener;
    private static long totalSize = 0;
    private static long transferred = 0;
    private static long currentProgress = 0;

    private static FileChannel inChannel;
    private static FileChannel outChannel;

    private static volatile boolean cancel = false;

    public static void cancelTransfer() {
        cancel = true;
        try {
            if (inChannel != null){
                inChannel.close();
                inChannel = null;
            }
            if (outChannel != null){
                outChannel.close();
                outChannel = null;
            }
        } catch (Exception e) {
            DebugLog.loge(e);
        }
    }

    public static void setFileTransferListener(FileTransferListener fileTransferListener) {
        FileUtils.fileTransferListener = fileTransferListener;
        totalSize = 0;
        transferred = 0;
        currentProgress = 0;
    }

    public static boolean isExistSDCard(Context context) {
        return getPathSDCard(context) != null;
    }

    public static String getPathSDCard(Context context) {
        try {
            String popularCase = getPopularCase();
            if (!popularCase.isEmpty()) {
                File file = new File(popularCase);
                if (file.length() != 0) {
                    DebugLog.logi("SD Card Path: " + popularCase);
                    try {
                        String[] names = popularCase.split("\\/");
                        SharedPreference.setString(context, SDCARD_NAME, names[names.length - 1]);
                        DebugLog.logi("SD Card Name: " + names[names.length - 1]);
                    } catch (Exception e) {
                    }
                }
            }

            String type = Environment.MEDIA_MOUNTED;
            File[] paths = ContextCompat.getExternalFilesDirs(context, type);
            for (int i = 0; i < paths.length; i++) {
                DebugLog.logi("path: " + paths[i]);
            }
            if (paths != null && paths.length >= 2) {
                for (int i = 1; i < paths.length; i++) {
                    if (paths[i] != null) {
                        String sdPath = paths[i].getPath();
                        int splitIndex = sdPath.indexOf("/Android/data/");
                        String sdCardPath = sdPath.substring(0, splitIndex);
                        DebugLog.logi("SD Card Path: " + sdCardPath);
                        File file = new File(sdCardPath);
                        if (file.length() != 0) {
                            try {
                                String[] names = sdCardPath.split("\\/");
                                SharedPreference.setString(context, SDCARD_NAME, names[names.length - 1]);
                                DebugLog.logi("SD Card Name: " + names[names.length - 1]);
                            } catch (Exception e) {
                            }
                            return sdPath.substring(0, splitIndex);
                        }
                    }
                }
            }
        } catch (Exception e) {
            DebugLog.loge(e);
        }
        return null;
    }

    @SuppressLint("SdCardPath")
    private static String getPopularCase() {
        File file = new File(System.getenv("SECONDARY_STORAGE") == null ? "" : System.getenv("SECONDARY_STORAGE"));
        if (file.exists()) {
            return file.getAbsolutePath();
        }
        String[] popular = new String[]{
                "/storage/sdcard1", // Motorola Xoom
                "/storage/extsdcard", // Samsung SGS3
                "/storage/sdcard0/external_sdcard", // User request
                "/mnt/extsdcard",
                "/mnt/sdcard/external_sd", // Samsung galaxy // family
                "/mnt/external_sd",
                "/mnt/media_rw/sdcard1", // 4.4.2 on CyanogenMod S3
                "/removable/microsd", // Asus transformer prime
                "/mnt/emmc",
                "/storage/external_SD", // LG
                "/storage/ext_sd", // HTC One Max
                "/storage/removable/sdcard1", // Sony Xperia Z1
                "/data/sdext",
                "/data/sdext2",
                "/data/sdext3",
                "/data/sdext4",
                "/sdcard1", // Sony XperiaZ
                "/sdcard2", // HTC One M8s
                "/storage/microsd" // ASUS ZenFone 2
        };
        for (int i = 0; i < popular.length; i++) {
            try {
                file = new File(popular[i]);
                if (file.exists()) {
                    return file.getAbsolutePath();
                }
            } catch (Exception e) {
                DebugLog.loge(e);
            }
        }
        return "";
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

    public static boolean isDocumentFile(String name) {
        for (int i = 0; i < TypesFile.documents.length; i++) {
            if (name.endsWith(TypesFile.documents[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean isImageFile(String name) {
        for (int i = 0; i < TypesFile.images.length; i++) {
            if (name.endsWith(TypesFile.images[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean isVideoFile(String name) {
        for (int i = 0; i < TypesFile.videos.length; i++) {
            if (name.endsWith(TypesFile.videos[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMusicFile(String name) {
        for (int i = 0; i < TypesFile.audios.length; i++) {
            if (name.endsWith(TypesFile.audios[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean isZipFile(String name) {
        for (int i = 0; i < TypesFile.zips.length; i++) {
            if (name.endsWith(TypesFile.zips[i])) {
                return true;
            }
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
                    if (file.isFile() && !file.isHidden() && file.canRead()) {
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
                    }
                    return false;
                }
            };
            return folder.listFiles(filter);
        }
        return null;
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

    public static long getTotalSize(File[] files) {
        long size = 0;
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                try {
                    size += files[i].length();
                } catch (Exception e) {
                }
            }
        }
        return size;
    }

    public static boolean copyFilesInFolderByType(Context context, String sourceFolder, String outputFolder, FileType fileType) {
        if (isSDCardPath(context, outputFolder) && Build.VERSION.SDK_INT >= 21) {
            if (!isHavePermissionWithTreeUri(context)) {
                UtilsLib.showToast(context, "Do not have permission to operator with SD card");
                return false;
            }
        }

        cancel = false;
        File[] files = getAllFileInPathByType(sourceFolder, fileType);
        if (files != null) {
            if (fileTransferListener != null) {
                totalSize = getTotalSize(files);
                transferred = 0;
                currentProgress = 0;
            }
            for (int i = 0; i < files.length; i++) {
                if (!cancel) {
                    copyFile(context, files[i], outputFolder);
                }
            }
        }
        setFileTransferListener(null);
        return true;
    }

    public static boolean moveFilesInFolderByType(Context context, String sourceFolder, String outputFolder, FileType fileType) {
        if ((isSDCardPath(context, outputFolder) || isSDCardPath(context, sourceFolder)) && Build.VERSION.SDK_INT >= 21) {
            if (!isHavePermissionWithTreeUri(context)) {
                UtilsLib.showToast(context, "Do not have permission to operator with SD card");
                return false;
            }
        }

        cancel = false;
        File[] files = getAllFileInPathByType(sourceFolder, fileType);
        if (files != null) {
            if (fileTransferListener != null) {
                totalSize = getTotalSize(files);
                transferred = 0;
                currentProgress = 0;
            }
            for (int i = 0; i < files.length; i++) {
                if (!cancel) {
                    boolean copy = copyFile(context, files[i], outputFolder);
                    if (copy) {
                        deleteFile(context, files[i]);
                    }
                }
            }
        }
        setFileTransferListener(null);
        return true;
    }

    public static boolean moveFileOrFolder(Context context, File sourceLocation, String targetFolderLocation) {
        try {
            cancel = false;
            boolean copy = copyFileOrFolder(context, sourceLocation, targetFolderLocation);
            boolean delete = false;
            if (copy && !cancel) {
                delete = deleteFile(context, sourceLocation);
                if (!delete) DebugLog.loge("DELETE FAILED");
            } else {
                DebugLog.loge("COPY FAILED");
            }
            return copy && delete;
        } catch (Exception e) {
            DebugLog.loge(e);
        } finally {
            setFileTransferListener(null);
        }
        return false;
    }

    public static boolean copyFileOrFolder(Context context, File sourceLocation, String targetFolderLocation) {
        try {
            if (context == null) {
                DebugLog.loge("Context is NULL");
                return false;
            }
            if (isSDCardPath(context, targetFolderLocation) && Build.VERSION.SDK_INT >= 21) {
                if (!isHavePermissionWithTreeUri(context)) {
                    UtilsLib.showToast(context, "Do not have permission to operator with SD card");
                    return false;
                }
            }

            cancel = false;
            if (sourceLocation.isDirectory()) {
                if (fileTransferListener != null) {
                    totalSize = getFolderSize(sourceLocation);
                    transferred = 0;
                    currentProgress = 0;
                }
                copyFolder(context, sourceLocation, targetFolderLocation);
            } else {
                if (fileTransferListener != null) {
                    totalSize = sourceLocation.length();
                    transferred = 0;
                    currentProgress = 0;
                }
                copyFile(context, sourceLocation, targetFolderLocation);
            }
            return true;
        } catch (Exception e) {
            DebugLog.loge(e);
        } finally {
            setFileTransferListener(null);
        }
        return false;
    }

    private static boolean copyFolder(Context context, File sourceLocation, String targetFolderLocation) {
        try {
            if (cancel) {
                return false;
            }
            File outputFolder = new File(targetFolderLocation, sourceLocation.getName());
            if (!outputFolder.exists()) {
                createFolder(context, targetFolderLocation, sourceLocation.getName());
            }

            File[] files = getAllFileInPath(sourceLocation.getPath());
            File[] folders = getAllFolderInPath(sourceLocation.getPath());
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (!cancel) {
                        copyFile(context, files[i], outputFolder.getPath());
                    }
                }
            }
            if (folders != null) {
                for (int i = 0; i < folders.length; i++) {
                    if (!cancel) {
                        copyFolder(context, folders[i], outputFolder.getPath());
                    }
                }
            }
        } catch (Exception e) {
            DebugLog.loge(e);
        }
        return false;
    }

    public static boolean deleteFile(Context context, File fileDelete) {
        if (context == null) {
            DebugLog.loge("Context is NULL");
            return false;
        }
        if (isSDCardPath(context, fileDelete.getPath()) && Build.VERSION.SDK_INT >= 21) {
            if (isHavePermissionWithTreeUri(context)) {
                return deleteFileV21(context, fileDelete);
            } else {
                UtilsLib.showToast(context, "Do not have permission to operator with SD card");
                return false;
            }
        }

        try {
            return fileDelete.delete();
        } catch (Exception e) {
            DebugLog.loge(e);
        }
        return false;
    }

    private static boolean deleteFileV21(Context context, File fileDelete) {
        try {
            Uri treeUri = Uri.parse(SharedPreference.getString(context, TREE_URI, ""));
            DocumentFile documentSDCard = DocumentFile.fromTreeUri(context, treeUri);
            String targetSDCard = getTargetSDCard(context, fileDelete.getPath());
            if (!targetSDCard.isEmpty()) {
                String[] parts = targetSDCard.split("\\/");
                for (int i = 0; i < parts.length; i++) {
                    DocumentFile nextDocument = documentSDCard.findFile(parts[i]);
                    if (nextDocument == null) {
                        DebugLog.loge("File don't exist: " + fileDelete.getPath());
                        return false;
                    }
                    documentSDCard = nextDocument;
                }
            }
            return documentSDCard.delete();
        } catch (Exception e) {
            DebugLog.loge(e);
        }
        return false;
    }

    public static void copyFile(InputStream inputStream, OutputStream outputStream){
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

    public static boolean copyFile(Context context, final File inputFile, final String outputFolder) {
        try {
            if (context == null) {
                DebugLog.loge("Context is NULL");
                return false;
            }
            cancel = false;
            if (isSDCardPath(context, outputFolder) && Build.VERSION.SDK_INT >= 21) {
                if (isHavePermissionWithTreeUri(context)) {
                    return copyFileToSDCardV21(context, inputFile, outputFolder);
                } else {
                    UtilsLib.showToast(context, "Do not have permission to operator with SD card");
                    return false;
                }
            }

            /*
            * Copy and move file in internal memory or SD card (SDK version < 21)
            * */
            File outputFile = new File(outputFolder, inputFile.getName());
            if (fileTransferListener == null) {
                inChannel = new FileInputStream(inputFile).getChannel();
                outChannel = new FileInputStream(outputFile).getChannel();
                try {
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                } finally {
                    if (inChannel != null){
                        inChannel.close();
                        inChannel = null;
                    }
                    if (outChannel != null){
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
                    transferred += read;
                    publishProgress();
                }
                fileInputStream.close();
                fileOuInputStream.flush();
                fileOuInputStream.close();
            }
            UtilsLib.tellAndroidAboutFile(context, outputFile);
            return true;
        } catch (Exception e) {
            DebugLog.loge(e);
        }
        return false;
    }

    private static boolean copyFileToSDCardV21(Context context, final File inputFile, final String outputFolder) {
        try {
            if (!inputFile.exists()) {
                DebugLog.loge("File don't exist: " + inputFile.getPath());
                return false;
            }

            Uri treeUri = Uri.parse(SharedPreference.getString(context, TREE_URI, ""));
            DocumentFile documentOutputFile = DocumentFile.fromTreeUri(context, treeUri);
            String targetSDCard = getTargetSDCard(context, outputFolder);
            if (!targetSDCard.isEmpty()) {
                String[] parts = targetSDCard.split("\\/");
                for (int i = 0; i < parts.length; i++) {
                    DocumentFile nextDocument = documentOutputFile.findFile(parts[i]);
                    if (nextDocument == null) {
                        DebugLog.logd("createDirectory: " + parts[i]);
                        nextDocument = documentOutputFile.createDirectory(parts[i]);
                    }
                    documentOutputFile = nextDocument;
                }
            }

            DocumentFile existDocumentFile = documentOutputFile.findFile(inputFile.getName());
            if (existDocumentFile != null) {
                DebugLog.loge("File existed: " + inputFile.getName());
                return false;
            }
            DocumentFile mDocumentFile = documentOutputFile.createFile("*/*", inputFile.getName());
            cancel = false;
            if (fileTransferListener == null) {
                FileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(mDocumentFile.getUri(), "w").getFileDescriptor();
                inChannel = new FileInputStream(inputFile).getChannel();
                outChannel = new FileOutputStream(fileDescriptor).getChannel();
                try {
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                } finally {
                    if (inChannel != null){
                        inChannel.close();
                        inChannel = null;
                    }
                    if (outChannel != null){
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
                    transferred += read;
                    publishProgress();
                }
                fileInputStream.close();
                outputStream.flush();
                outputStream.close();
            }
            tellAndroidAboutFile(context, mDocumentFile.getUri());
            return true;
        } catch (FileNotFoundException e) {
            DebugLog.loge(e);
        } catch (Exception e) {
            DebugLog.loge(e);
        }
        return false;
    }

    public static boolean createFolder(Context context, String parentPath, String name) {
        if (isSDCardPath(context, parentPath) && Build.VERSION.SDK_INT >= 21) {
            if (isHavePermissionWithTreeUri(context)) {
                return createFolderSDCardV21(context, name, parentPath);
            } else {
                UtilsLib.showToast(context, "Do not have permission to operator with SD card");
                return false;
            }
        } else {
            try {
                File file = new File(parentPath, name);
                return file.mkdirs();
            } catch (Exception e) {
            }
        }
        return false;
    }

    private static boolean createFolderSDCardV21(Context context, String folderName, String parentPath) {
        try {
            if (isFileExitedInSDCardV21(context, parentPath + "/" + folderName)) {
                DebugLog.loge("File existed");
                return false;
            }
            Uri treeUri = Uri.parse(SharedPreference.getString(context, TREE_URI, ""));
            DocumentFile documentOutputFile = DocumentFile.fromTreeUri(context, treeUri);
            String targetSDCard = getTargetSDCard(context, parentPath + "/" + folderName);
            if (!targetSDCard.isEmpty()) {
                String[] parts = targetSDCard.split("\\/");
                for (int i = 0; i < parts.length; i++) {
                    DocumentFile nextDocument = documentOutputFile.findFile(parts[i]);
                    if (nextDocument == null) {
                        nextDocument = documentOutputFile.createDirectory(parts[i]);
                    }
                    documentOutputFile = nextDocument;
                }
            }
            return documentOutputFile != null;
        } catch (Exception e) {
            DebugLog.loge(e);
        }
        return false;
    }

    public static boolean renameFile(Context context, String newName, String path) {
        if (isSDCardPath(context, path) && Build.VERSION.SDK_INT >= 21) {
            if (isHavePermissionWithTreeUri(context)) {
                return renameFileSDCardV21(context, newName, path);
            } else {
                UtilsLib.showToast(context, "Do not have permission to operator with SD card");
                return false;
            }
        }

        try {
            File from = new File(path);
            File to = new File(from.getParent(), newName);
            return from.renameTo(to);
        } catch (Exception e) {
            DebugLog.loge(e);
        }
        return false;
    }

    private static boolean renameFileSDCardV21(Context context, String newName, String path) {
        try {
            String targetSDCard = getTargetSDCard(context, path);
            if (isFileExitedInSDCardV21(context, targetSDCard)) {
                DebugLog.loge("File existed");
                return false;
            }
            Uri treeUri = Uri.parse(SharedPreference.getString(context, TREE_URI, ""));
            DocumentFile documentOutputFile = DocumentFile.fromTreeUri(context, treeUri);
            if (!targetSDCard.isEmpty()) {
                String[] parts = targetSDCard.split("\\/");
                for (int i = 0; i < parts.length; i++) {
                    DocumentFile nextDocument = documentOutputFile.findFile(parts[i]);
                    if (nextDocument == null) {
                        DebugLog.loge("File don't exist");
                        return false;
                    }
                    documentOutputFile = nextDocument;
                }
            }
            return documentOutputFile.renameTo(newName);
        } catch (Exception e) {
            DebugLog.loge(e);
        }
        return false;
    }

    private static void tellAndroidAboutFile(Context context, Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(uri);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            DebugLog.loge(e);
        }
    }

    private static boolean isSDCardPath(Context context, String path) {
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

    private static boolean isHavePermissionWithTreeUri(Context context) {
        if (context == null) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= 21 && SharedPreference.getString(context, TREE_URI, "").isEmpty()) {
            return false;
        }
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

    public static boolean isTreeUri(String treeUri) {
        try {
            String[] tree = treeUri.split("\\%3A");
            DebugLog.loge("treeUri: " + treeUri);
            DebugLog.loge("tree: " + tree.length);
            if (tree.length == 1) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    private static boolean isFileExitedInSDCardV21(Context context, String path) {
        try {
            Uri treeUri = Uri.parse(SharedPreference.getString(context, TREE_URI, ""));
            DocumentFile documentOutputFile = DocumentFile.fromTreeUri(context, treeUri);
            String targetSDCard = getTargetSDCard(context, path);
            if (!targetSDCard.isEmpty()) {
                String[] parts = targetSDCard.split("\\/");
                for (int i = 0; i < parts.length; i++) {
                    DocumentFile nextDocument = documentOutputFile.findFile(parts[i]);
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
            String sdcardName = SharedPreference.getString(context, SDCARD_NAME, "extSdCard");
            int index = outputPath.indexOf(sdcardName);
            String targetSDCard = outputPath.substring(index + sdcardName.length()).trim();
            if (targetSDCard.length() > 0) {
                targetSDCard = targetSDCard.substring(1).trim();
            }
            return targetSDCard;
        } catch (Exception e) {
            DebugLog.loge(e);
        }
        return "";
    }

    private static void publishProgress() {
        if (fileTransferListener != null) {
            try {
                int progress = (int) ((transferred * 100) / totalSize);
                if (progress > currentProgress) {
                    currentProgress = progress;
                    fileTransferListener.onProgress(progress);
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
        DOCUMENT,
        IMAGE,
        VIDEO,
        MUSIC,
        ZIP,
        APK
    }

    public interface FileTransferListener {
        void onProgress(int progress); // percent (ex: 1/100)
    }
}
