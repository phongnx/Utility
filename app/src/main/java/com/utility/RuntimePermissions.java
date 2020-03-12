package com.utility;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;


/**
 * Created by Phong on 12/13/2016.
 */

public class RuntimePermissions {

    public interface RequestCodePermission {
        int REQUEST_CODE_GRANT_WRITE_CONTACT_PERMISSIONS = 1000;
        int REQUEST_CODE_GRANT_READ_CONTACT_PERMISSIONS = 1001;
        int REQUEST_CODE_GRANT_OVERLAY_PERMISSIONS = 1002;
        int REQUEST_CODE_GRANT_STORAGE_PERMISSIONS = 1003;
        int REQUEST_CODE_GRANT_LOCATION_PERMISSIONS = 1004;
        int REQUEST_CODE_SETTINGS_LOCATION = 1005;
        int REQUEST_CODE_GRANT_BLUETOOTH_PERMISSIONS = 1006;
        int REQUEST_CODE_GRANT_CAMERA_PERMISSIONS = 1007;
        int REQUEST_CODE_GRANT_MICRO_PERMISSIONS = 1008;
        int REQUEST_CODE_ENABLE_NOTIFICATION = 1009;
        int REQUEST_CODE_GRANT_PHONE_STATE_PERMISSIONS = 1010;
        int REQUEST_CODE_GRANT_GET_ACCOUNTS = 1011;
        int REQUEST_CODE_GRANT_SEND_SMS = 1012;
        int REQUEST_CODE_GRANT_RECEIVE_SMS = 1013;
        int REQUEST_CODE_GRANT_READ_SMS = 1014;
        int REQUEST_CODE_GRANT_RECEIVE_MMS = 1015;
        int REQUEST_CODE_GRANT_READ_CALENDAR = 1016;
        int REQUEST_CODE_GRANT_WRITE_CALENDAR = 1017;
        int REQUEST_CODE_GRANT_CALL_PHONE = 1018;
    }

    /*
     * Check write calendar permission
     *
     * <uses-permission android:name="android.permission.WRITE_CALENDAR" />
     * */
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean checkWriteCalendarPermission(@NonNull Context context) {
        int hasAccessPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR);
        return hasAccessPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestWriteCalendarPermission(@NonNull Context context) {
        if (context instanceof Activity) {
            ActivityCompat.requestPermissions(((Activity) context), new String[]{Manifest.permission.WRITE_CALENDAR}, RequestCodePermission.REQUEST_CODE_GRANT_WRITE_CALENDAR);
        }
    }

    /*
     * Check read calendar permission
     *
     * <uses-permission android:name="android.permission.READ_CALENDAR" />
     * */
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean checkReadCalendarPermission(@NonNull Context context) {
        int hasAccessPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR);
        return hasAccessPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestReadCalendarPermission(@NonNull Context context) {
        if (context instanceof Activity) {
            ActivityCompat.requestPermissions(((Activity) context), new String[]{Manifest.permission.READ_CALENDAR}, RequestCodePermission.REQUEST_CODE_GRANT_READ_CALENDAR);
        }
    }

    /*
     * Check receive mms permission
     *
     * <uses-permission android:name="android.permission.RECEIVE_MMS" />
     * */
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean checkReceiveMmsPermission(@NonNull Context context) {
        int hasAccessPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_MMS);
        return hasAccessPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestReceiveMmsPermission(@NonNull Context context) {
        if (context instanceof Activity) {
            ActivityCompat.requestPermissions(((Activity) context), new String[]{Manifest.permission.RECEIVE_MMS}, RequestCodePermission.REQUEST_CODE_GRANT_RECEIVE_MMS);
        }
    }

    /*
     * Check read sms permission
     *
     * <uses-permission android:name="android.permission.READ_SMS" />
     * */
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean checkReadSmsPermission(@NonNull Context context) {
        int hasAccessPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS);
        return hasAccessPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestReadSmsPermission(@NonNull Context context) {
        if (context instanceof Activity) {
            ActivityCompat.requestPermissions(((Activity) context), new String[]{Manifest.permission.READ_SMS}, RequestCodePermission.REQUEST_CODE_GRANT_READ_SMS);
        }
    }

    /*
     * Check receive sms permission
     *
     * <uses-permission android:name="android.permission.RECEIVE_SMS" />
     * */
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean checkReceiveSmsPermission(@NonNull Context context) {
        int hasAccessPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS);
        return hasAccessPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestReceiveSmsPermission(@NonNull Context context) {
        if (context instanceof Activity) {
            ActivityCompat.requestPermissions(((Activity) context), new String[]{Manifest.permission.RECEIVE_SMS}, RequestCodePermission.REQUEST_CODE_GRANT_RECEIVE_SMS);
        }
    }

    /*
     * Check send sms permission
     *
     * <uses-permission android:name="android.permission.SEND_SMS" />
     * */
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean checkSendSmsPermission(@NonNull Context context) {
        int hasAccessPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS);
        return hasAccessPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestSendSmsPermission(@NonNull Context context) {
        if (context instanceof Activity) {
            ActivityCompat.requestPermissions(((Activity) context), new String[]{Manifest.permission.SEND_SMS}, RequestCodePermission.REQUEST_CODE_GRANT_SEND_SMS);
        }
    }

    /*
     * Check get accounts permission
     *
     * <uses-permission android:name="android.permission.GET_ACCOUNTS" />
     * */
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean checkGetAccountsPermission(@NonNull Context context) {
        int hasAccessPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.GET_ACCOUNTS);
        return hasAccessPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestGetAccountsPermission(@NonNull Context context) {
        if (context instanceof Activity) {
            ActivityCompat.requestPermissions(((Activity) context), new String[]{Manifest.permission.GET_ACCOUNTS}, RequestCodePermission.REQUEST_CODE_GRANT_GET_ACCOUNTS);
        }
    }

    /*
     * Check read contacts permission
     *
     * <uses-permission android:name="android.permission.READ_CONTACTS" />
     * */
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean checkReadContactPermission(@NonNull Context context) {
        int hasAccessPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS);
        return hasAccessPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestReadContactPermission(@NonNull Context context) {
        if (context instanceof Activity) {
            ActivityCompat.requestPermissions(((Activity) context), new String[]{Manifest.permission.READ_CONTACTS}, RequestCodePermission.REQUEST_CODE_GRANT_READ_CONTACT_PERMISSIONS);
        }
    }

    /*
     * Check write contacts permission
     *
     * <uses-permission android:name="android.permission.WRITE_CONTACTS" />
     * */
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean checkWriteContactPermission(@NonNull Context context) {
        int hasAccessPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CONTACTS);
        return hasAccessPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestWriteContactPermission(@NonNull Context context) {
        if (context instanceof Activity) {
            ActivityCompat.requestPermissions(((Activity) context), new String[]{Manifest.permission.WRITE_CONTACTS}, RequestCodePermission.REQUEST_CODE_GRANT_WRITE_CONTACT_PERMISSIONS);
        }
    }

    /*
     * Check and Request storage permission
     *
     * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
     * <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
     * */
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean checkAccessStoragePermission(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        int hasAccessWriteStoragePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int hasAccessReadStoragePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        return hasAccessWriteStoragePermission == PackageManager.PERMISSION_GRANTED && hasAccessReadStoragePermission == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestStoragePermission(@NonNull Context context) {
        if (context instanceof Activity) {
            ActivityCompat.requestPermissions(((Activity) context), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, RequestCodePermission.REQUEST_CODE_GRANT_STORAGE_PERMISSIONS);
        }
    }

    /*
     * Check and Request read phone state permission
     *
     * <uses-permission android:name="android.permission.READ_PHONE_STATE" />
     * */
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean checkAccessPhoneStatePermission(@NonNull Context context) {
        int hasAccessPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE);
        return hasAccessPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestAccessPhoneStatePermission(@NonNull Context context) {
        if (context instanceof Activity) {
            ActivityCompat.requestPermissions(((Activity) context), new String[]{Manifest.permission.READ_PHONE_STATE}, RequestCodePermission.REQUEST_CODE_GRANT_PHONE_STATE_PERMISSIONS);
        }
    }

    /*
     * Check and Request call phone permission
     *
     * <uses-permission android:name="android.permission.CALL_PHONE" />
     * */
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean checkAccessCallPhonePermission(@NonNull Context context) {
        int hasAccessPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE);
        return hasAccessPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestAccessCallPhonePermission(@NonNull Context context) {
        if (context instanceof Activity) {
            ActivityCompat.requestPermissions(((Activity) context), new String[]{Manifest.permission.CALL_PHONE}, RequestCodePermission.REQUEST_CODE_GRANT_CALL_PHONE);
        }
    }

    /*
     * Check and Request location permission
     *
     * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
     * <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
     * */
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean checkAccessLocationPermission(@NonNull Context context) {
        int hasAccessFineLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasAccessCoarseLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        return hasAccessFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasAccessCoarseLocationPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestLocationPermission(@NonNull Context context) {
        if (context instanceof Activity) {
            ActivityCompat.requestPermissions(((Activity) context), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, RequestCodePermission.REQUEST_CODE_GRANT_LOCATION_PERMISSIONS);
        }
    }

    /*
     * Request location service on/off
     * */
    public static void requestLocationTurnOn(@NonNull Context context) {
        try {
            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            if (context instanceof Activity) {
                ((Activity) context).startActivityForResult(myIntent, RequestCodePermission.REQUEST_CODE_SETTINGS_LOCATION);
            } else {
                context.startActivity(myIntent);
            }
        } catch (Exception ignored) {
        }
    }

    /*
     * Check and Request bluetooth permission
     *
     * <uses-permission android:name="android.permission.BLUETOOTH" />
     * <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
     * */
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean checkAccessBluetoothPermission(@NonNull Context context) {
        int hasAccessBluetoothPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH);
        int hasAccessBluetoothAdminPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN);
        return hasAccessBluetoothPermission == PackageManager.PERMISSION_GRANTED && hasAccessBluetoothAdminPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestAccessBluetoothPermission(@NonNull Context context) {
        if (context instanceof Activity) {
            ActivityCompat.requestPermissions(((Activity) context), new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN}, RequestCodePermission.REQUEST_CODE_GRANT_BLUETOOTH_PERMISSIONS);
        }
    }

    /*
     * Check and request camera permission
     *
     * <uses-permission android:name="android.permission.CAMERA" />
     * */
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean checkAccessCameraPermission(@NonNull Context context) {
        int hasAccessPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        return hasAccessPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestCameraPermission(@NonNull Context context) {
        if (context instanceof Activity) {
            ActivityCompat.requestPermissions(((Activity) context), new String[]{Manifest.permission.CAMERA}, RequestCodePermission.REQUEST_CODE_GRANT_CAMERA_PERMISSIONS);
        }
    }

    /*
     * Check and request micro permission
     *
     * <uses-permission android:name="android.permission.RECORD_AUDIO" />
     * */
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean checkAccessMicroPermission(@NonNull Context context) {
        int hasAccessPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO);
        return hasAccessPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestMicroPermission(@NonNull Context context) {
        if (context instanceof Activity) {
            ActivityCompat.requestPermissions(((Activity) context), new String[]{Manifest.permission.RECORD_AUDIO}, RequestCodePermission.REQUEST_CODE_GRANT_MICRO_PERMISSIONS);
        }
    }

    /*
     * Check push notification enable or disable
     * */
    public static boolean checkEnablePushNotification(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= 19) {
            return NotificationManagerCompat.from(context).areNotificationsEnabled();
        }
        return true;
    }

    /*
     * Request push notification on/off
     * */
    public static void requestPushNotificationOn(@NonNull Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (context instanceof Activity) {
                ((Activity) context).startActivityForResult(intent, RequestCodePermission.REQUEST_CODE_ENABLE_NOTIFICATION);
            } else {
                context.startActivity(intent);
            }
        } catch (Exception e) {
            DebugLog.loge(e);
        }
    }

    /*
     * Check overlay permission
     *
     * <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
     * */
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean checkOverlayPermission(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }

    @SuppressLint("InlinedApi")
    public static void requestOverlayPermission(@NonNull Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
            if (context instanceof Activity) {
                ((Activity) context).startActivityForResult(intent, RequestCodePermission.REQUEST_CODE_GRANT_OVERLAY_PERMISSIONS);
            } else {
                context.startActivity(intent);
            }
        } catch (Exception ignored) {
        }
    }
}
