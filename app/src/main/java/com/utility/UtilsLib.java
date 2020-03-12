package com.utility;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Telephony;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.core.content.FileProvider;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.utility.files.FileUtils;
import com.utility.others.RequestCodes;
import com.utility.others.ResizeHeightAnimation;
import com.utility.others.ResizeWidthAnimation;
import com.utility.others.UnCaughtException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

@SuppressLint({"SimpleDateFormat", "InlinedApi", "DefaultLocale"})
@SuppressWarnings("unused")
public class UtilsLib {
    private static final String FORMAT_DATE_TIME = "yyyy-MM-dd'T'HH:mm'Z'";
    private static Toast sToast;

    public static void preventCrashError(Context context) {
        Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(context));
    }

    public static String getCountryCodeDefault(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String country = tm.getSimCountryIso();
        DebugLog.loge("Country (TelephonyManager): " + country);
        if (country.isEmpty()) {
            country = Locale.getDefault().getCountry();
            DebugLog.loge("Country (Locale): " + country);
        }
        return country;
    }

    public static String getDateTimeBySystemFormat(Context context, long time) {
        try {
            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context.getApplicationContext()); // Gets system date format
            DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context.getApplicationContext()); // Gets system time format
            return dateFormat.format(time) + " " + timeFormat.format(time);
        } catch (Exception e) {
            DebugLog.loge(e);
        }
        return UtilsLib.getDateTime(time, FORMAT_DATE_TIME);
    }

    public static String getDateTime(Object dateTimeInMilliseconds, String format) {
        long value;
        try {
            value = Long.parseLong(String.valueOf(dateTimeInMilliseconds));
        } catch (Exception e) {
            DebugLog.loge(e);
            value = System.currentTimeMillis();
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(value);
    }


    public static String getDateTimeByTimezone(long time, String timezone, String pattern) {
        if (timezone == null || timezone.isEmpty()) {
            timezone = TimeZone.getDefault().getID();
        }
        DateTime dateTime = new DateTime(time);
        DateTime dateTimeZone = dateTime.withZone(DateTimeZone.forID(timezone));
        DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern(pattern);
        return dateTimeZone.toString(dateTimeFormat);
    }

    public static String getDateTimeByOffSet(long time, int offSet, String pattern) {
        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(pattern);
            DateTimeZone dateTimeZone = DateTimeZone.forOffsetMillis(offSet);
            DateTime dateTime = new DateTime(time, dateTimeZone);
            return dateTimeFormatter.print(dateTime);
        } catch (Exception e) {
            DebugLog.loge(e);
        }
        return "";
    }

    /**
     * Get Time Zone In Local.
     *
     * @return (int) -12 to 14
     */
    public static int getTimeZoneInLocal() {
        TimeZone timeZone = TimeZone.getDefault();
        int timeZoneOffSet = timeZone.getRawOffset() / 1000 / 3600;
        return timeZoneOffSet;
    }

    /**
     * Get Time in Milliseconds.
     *
     * @return (Long) result time in milliseconds
     */
    public static long parseTimeToMilliseconds(int dayOfMonth, int month, int year, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);

        return calendar.getTimeInMillis();
    }

    /**
     * Make a standard toast that just contains a text view.
     */
    public static void showToast(Context context, String message) {
        if (context == null) {
            return;
        }
        try {
            Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 230);
            toast.show();
        } catch (Exception e) {
            DebugLog.loge(e);
        }
    }

    /**
     * Make a standard toast that just contains a text view.
     */
    private static void dismissLastToast() {
        if (sToast != null) {
            try {
                sToast.cancel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void showToast(Context context, String message, int duration) {
        if (context == null) {
            return;
        }
        dismissLastToast();
        try {
            sToast = Toast.makeText(context, message, duration);
            sToast.setGravity(Gravity.CENTER, 0, 230);
            sToast.show();
        } catch (Exception e) {
            DebugLog.loge(e);
        }
    }

    public static void showToast(Context context, int restId) {
        if (context == null) {
            return;
        }
        dismissLastToast();
        try {
            sToast = Toast.makeText(context, restId, Toast.LENGTH_SHORT);
            sToast.setGravity(Gravity.CENTER, 0, 230);
            sToast.show();
        } catch (Exception e) {
            DebugLog.loge(e);
        }
    }

    public static void showToast(Context context, int restId, int duration) {
        if (context == null) {
            return;
        }
        dismissLastToast();
        try {
            sToast = Toast.makeText(context, restId, duration);
            sToast.setGravity(Gravity.CENTER, 0, 230);
            sToast.show();
        } catch (Exception e) {
            DebugLog.loge(e);
        }
    }

    private static Toast setToastMessageCenter(Toast toast) {
        TextView v = toast.getView().findViewById(android.R.id.message);
        if (v != null) {
            v.setGravity(Gravity.CENTER);
        }
        return toast;
    }


    /**
     * Sets the right-hand compound drawable of the TextView to the "error" icon
     * and sets an error message that will be displayed in a popup when the
     * TextView has focus. The icon and error message will be reset to null when
     * any key events cause changes to the TextView's text. If the error is
     * null, the error message and icon will be cleared. .
     *
     * @param obj          must be TextView or EditText.
     * @param errorMessage error message to show.
     */
    public static void showErrorNullOrEmpty(final Object obj, final String errorMessage) {
        if (obj == null)
            return;
        if (obj instanceof TextView) {
            final TextView view = (TextView) obj;
            view.setError(errorMessage);
            view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View arg0, boolean arg1) {
                    if (view.getText().toString().isEmpty() || view.getText().toString().equals("")) {
                        view.setError(errorMessage);
                    } else {
                        view.setError(null);
                    }
                }
            });
        } else if (obj instanceof EditText) {
            final EditText view = (EditText) obj;
            view.setError(errorMessage);
            view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View arg0, boolean arg1) {
                    if (view == null || view.getText().toString().isEmpty() || view.getText().toString().equals("")) {
                        view.setError(errorMessage);
                    } else {
                        view.setError(null);
                    }
                }
            });
        }
    }

    public static void showErrorNullOrEmptyWithThemeLight(final Object obj, final String errorMessage) {
        if (obj == null)
            return;
        if (obj instanceof TextView) {
            final TextView view = (TextView) obj;
            view.setError(Html.fromHtml("<font color='black'>" + errorMessage + "!</font>"));
            view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View arg0, boolean arg1) {
                    if (view == null || view.getText().toString().isEmpty() || view.getText().toString().equals("")) {
                        view.setError(Html.fromHtml("<font color='black'>" + errorMessage + "!</font>"));
                    } else {
                        view.setError(null);
                    }
                }
            });
        } else if (obj instanceof EditText) {
            final EditText view = (EditText) obj;
            view.setError(Html.fromHtml("<font color='black'>" + errorMessage + "!</font>"));
            view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View arg0, boolean arg1) {
                    if (view == null || view.getText().toString().isEmpty() || view.getText().toString().equals("")) {
                        view.setError(Html.fromHtml("<font color='black'>" + errorMessage + "!</font>"));
                    } else {
                        view.setError(null);
                    }
                }
            });
        }
    }

    public static void hideErrorView(View view) {
        try {
            if (view == null)
                return;
            if (view instanceof TextView) {
                ((TextView) view).setError(null);
            }
            if (view instanceof EditText) {
                ((EditText) view).setError(null);
            }
        } catch (Exception ignored) {
        }
    }

    public static void showOrHideKeyboard(Activity activity, boolean willShow) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null) {
            if (willShow) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            } else {
                imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

    /**
     * Request to show the soft input window from the context of the window that
     * is currently accepting input.
     *
     * @param context  The context to use. Usually your Application or Activity
     *                 object.
     * @param editText must be EditText
     */
    public static void showKeyboard(Context context, EditText editText) {
        if (context == null || editText == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * Request to hide the soft input window from the context of the window that
     * is currently accepting input.
     *
     * @param context The context to use. Usually your Application or Activity
     *                object.
     * @param view    must be EditText
     */

    public static void hideKeyboard(Context context, View view) {
        if (context == null) {
            return;
        }
        if (view != null) {
            try {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            } catch (Exception e) {
            }
        }
    }

    /**
     * Request to hide the soft input window from the context of the window that
     * is currently accepting input.
     *
     * @param context  The context to use. Usually your Application or Activity
     *                 object.
     * @param editText must be EditText
     */
    public static void removeFocusAndHideKeyboard(final Context context, final EditText editText) {
        editText.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus == true) {
                    InputMethodManager inputMgr = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    inputMgr.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });
        editText.requestFocus();
    }

    /**
     * Checking format input email.
     *
     * @param email check email allow format
     *              "[a-zA-Z0-9._-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2}[A-Za-z]*$+" or
     *              "[a-zA-Z0-9._-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2}[A-Za-z]*$+\\.+[a-z]+"
     * @return (boolean) true/false
     */
    public static boolean validateEmail(String email) {
        Pattern pattern = Pattern.compile("[a-zA-Z0-9._-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2}[A-Za-z]*$+");
        Pattern pattern2 = Pattern.compile("[a-zA-Z0-9._-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2}[A-Za-z]*$+\\.+[a-z]+");
        return pattern.matcher(email).matches() || pattern2.matcher(email).matches();
    }

    @RequiresPermission(Manifest.permission.CALL_PHONE)
    public static void callPhone(Context context, String phone) throws SecurityException {
        if (context == null) {
            return;
        }
        DebugLog.logd("phone: " + phone);
        if (phone == null || phone.isEmpty()) {
            showToast(context, "Phone number is empty");
            return;
        }
        try {
            phone.replaceAll("\\-", "").replaceAll(" ", "");
        } catch (Exception e) {
            DebugLog.loge(e);
        }
        String uri = "tel: " + phone;
        try {
            if (RuntimePermissions.checkAccessCallPhonePermission(context)) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse(uri));
                context.startActivity(intent);
            } else {
                DebugLog.loge("Do not have CALL_PHONE permission");
            }
        } catch (Exception e) {
            DebugLog.loge(e);
        }
    }

    /**
     * Call Email Application On Device.
     *
     * @param context  The context to use. Usually your Application or Activity object.
     * @param email    email to send
     * @param subject  subject to send
     * @param bodyText content mail to send
     */
    public static void callEmailApplication(Context context, String email, String subject, String bodyText) {
        if (context == null) {
            return;
        }
        callEmailApplication(context, new String[]{email}, subject, bodyText);
    }

    public static void callEmailApplication(Context context, String[] emails, String subject, String bodyText) {
        final Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/message");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, emails);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, bodyText);
        context.startActivity(Intent.createChooser(emailIntent, "Email Client").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @SuppressLint("NewApi")
    public static void sendSMS(Context context, String content, String phoneNumber, String messageError) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(context);

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("sms:" + phoneNumber)); // This ensures
                intent.putExtra("sms_body", content);

                if (defaultSmsPackageName != null) {
                    intent.setPackage(defaultSmsPackageName);
                } else {
                    String defaultApplication = Settings.Secure.getString(context.getContentResolver(), "sms_default_application");
                    if (defaultApplication != null) {
                        intent.setPackage(defaultApplication);
                    } else {
                        if (messageError.isEmpty()) {
                            showToast(context, messageError);
                        }
                        return;
                    }
                }
                context.startActivity(intent);

            } else {
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse("sms:" + phoneNumber));
                sendIntent.putExtra("sms_body", content);
                context.startActivity(sendIntent);
            }
        } catch (Exception e) {
            DebugLog.loge(e);
            if (messageError.isEmpty()) {
                showToast(context, messageError);
            }
        }
    }

    /**
     * Get Bitmap
     *
     * @param encodedImage input string base 64 image encoded
     * @return (Bitmap) bitmap
     */
    public static Bitmap getBitMapFromBase64(String encodedImage) {
        Bitmap decodedByte = null;
        try {
            byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
            decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        } catch (Exception e) {
            DebugLog.loge(e);
        }
        return decodedByte;
    }

    /**
     * Get String Base 64
     *
     * @param image_url input string image_url of image
     * @return (String) string base 64 was encoded
     */
    public static String encodeImageBase64(String image_url) {
        Bitmap bitmapOrg = BitmapFactory.decodeFile(image_url);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bitmapOrg.compress(Bitmap.CompressFormat.JPEG, 100, bao);
        byte[] ba = bao.toByteArray();
        String result = Base64.encodeToString(ba, Base64.DEFAULT);
        bitmapOrg.recycle();
        return result;
    }

    /**
     * Check url format
     *
     * @param url input url string
     * @return (Boolean) match or not
     */
    public static boolean isUrlFormat(String url) {
        if (!url.trim().isEmpty()) {
            return Patterns.WEB_URL.matcher(url.trim()).matches();
        }
        return false;
    }

    /**
     * Check phone number format
     *
     * @param phone_number input phone_number string
     * @return (Boolean) match or not
     */
    public static boolean isPhoneNumberFormat(String phone_number) {
        if (!phone_number.trim().isEmpty()) {
            return Patterns.PHONE.matcher(phone_number.trim()).matches();
        }
        return false;
    }

    /**
     * Check package (an application) exist in device
     *
     * @param context     The context to use. Usually your Application or Activity object.
     * @param packageName input packageName string like: com.application
     * @return (Boolean) exist or not
     */
    public static boolean isPackageInstalled(Context context, String packageName) {
        try {
            if (context == null) {
                return false;
            }
            PackageManager packageManager = context.getPackageManager();
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Convert DP to Pixel.
     *
     * @param context The context to use. Usually your Application or Activity
     *                object.
     * @param dp      The unit to convert from.
     * @return (int) result.
     */
    public static int convertDPtoPixel(Context context, int dp) {
        Resources r = context.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    public static float convertPixelsToDp(Context context, float px) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return px / (metrics.densityDpi / 160f);
    }

    /**
     * Encrypt SHA1.
     *
     * @param text input string
     * @return (String) result.
     */
    public static String encryptSHA1(String text) {
        String result = "";
        if (text.equals("")) {
            return "";
        }
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
            md.update(text.getBytes("iso-8859-1"), 0, text.length());
            byte[] sha1hash = md.digest();
            result = convertToHex(sha1hash);
        } catch (NoSuchAlgorithmException ignored) {
        } catch (UnsupportedEncodingException ignored) {
        }
        return result;
    }

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    /**
     * Encrypt MD5.
     *
     * @param text input string
     * @return (String) result.
     */
    public static String encryptMD5(String text) {
        if (text.equals("")) {
            return "";
        }
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(text.getBytes());
            byte[] a = digest.digest();
            int len = a.length;
            StringBuilder sb = new StringBuilder(len << 1);
            for (int i = 0; i < len; i++) {
                sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
                sb.append(Character.forDigit(a[i] & 0x0f, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /*
     * Encrypt AES.
     * */
    public static SecretKey generateKey(String password) {
        return new SecretKeySpec(password.getBytes(), "AES");
    }

    @SuppressLint("GetInstance")
    public static byte[] AESEncrypt(String message, SecretKey secret)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        return cipher.doFinal(message.getBytes("UTF-8"));
    }

    @SuppressLint("GetInstance")
    public static String AESDecrypt(byte[] cipherText, SecretKey secret)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret);
        return new String(cipher.doFinal(cipherText), "UTF-8");
    }

    /**
     * Gen KeyHash for facebook or something use.
     * <p>
     * Class for retrieving various kinds of information related to
     * the application packages that are currently installed on the
     * device.
     *
     * @return (String) result.
     */
    @SuppressLint("PackageManagerGetSignatures")
    public static String genKeyHash(Context context) {
        String keyHash = "error";
        PackageManager manager = context.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = manager.getPackageInfo(context.getPackageName(), 0);
            if (packageInfo == null) {
                DebugLog.loge("Error: packageInfo == null");
                return keyHash;
            }
            PackageInfo info = manager.getPackageInfo(packageInfo.packageName, PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                DebugLog.loge("KeyHash:\n" + keyHash);
            }
        } catch (Exception e) {
            DebugLog.loge("Error: " + e.getMessage());
        }
        return keyHash;
    }

    /**
     * Get Duration of AudioFile.
     *
     * @param filePath path or url of file
     * @return (long) result.
     */
    public static long getDurationAudioFile(String filePath) {
        try {
            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            metaRetriever.setDataSource(filePath);
            return Long.parseLong(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        } catch (Exception e) {
            DebugLog.loge(e);
        }
        return 0;
    }

    /**
     * Copy a file or all file in assets folder to SD card or InternalStored
     *
     * @param context      The context to use. Usually your Application or Activity
     *                     object.
     * @param path         the path to the directory where the file to copy.
     * @param folderAssets folder in assets.
     * @param file         the assets file name
     */
    public static void copyAssets(Context context, String path, String folderAssets, String file) {
        AssetManager assetManager = context.getAssets();
        String[] files;
        try {
            files = assetManager.list(folderAssets);
        } catch (IOException e) {
            DebugLog.loge("Failed to get asset file list: " + e);
            return;
        }
        if (files == null) {
            return;
        }
        for (String filename : files) {
            if (file.equals("")) {
                try {
                    File outFile = new File(path, filename);
                    InputStream inputStream = assetManager.open(folderAssets + "/" + filename);
                    OutputStream outputStream = new FileOutputStream(outFile);
                    new FileUtils().copyFile(inputStream, outputStream);
                    DebugLog.logd("copy asset file: " + filename);
                } catch (IOException e) {
                    DebugLog.loge("Failed to copy asset file: " + filename + "\n" + e);
                }
            } else {
                if (filename.contains(file)) {
                    try {
                        File outFile = new File(path, filename);
                        InputStream inputStream = assetManager.open(folderAssets + "/" + filename);
                        OutputStream outputStream = new FileOutputStream(outFile);
                        FileUtils.copyFile(inputStream, outputStream);
                        DebugLog.logd("copy asset file: " + filename);
                    } catch (IOException e) {
                        DebugLog.loge("Failed to copy asset file: " + filename + "\n" + e);
                    }
                }
            }
        }
    }

    public static String readTextFileInAsset(Context context, String place_file_name) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(context.getAssets().open(place_file_name), "UTF-8"));
            StringBuilder returnString = new StringBuilder();
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                returnString.append(mLine);
            }
            return returnString.toString().trim();
        } catch (IOException e) {
            DebugLog.loge(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    DebugLog.loge(e);
                }
            }
        }
        return "";
    }

    /**
     * Indicates whether network connectivity exists and it is possible to
     * establish connections and pass data
     *
     * @param context The context to use. Usually your Application or Activity
     *                object.
     * @return (boolean) true/false.
     */
    @SuppressLint("MissingPermission")
    public static boolean isNetworkConnect(Context context) {
        if (context == null) {
            return false;
        }
        final ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    /*
     *
     * */
    public static void openMediaExplorer(Context context, int REQUEST_TAKE_EXPLORER_FILE) {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        ((Activity) context).startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_TAKE_EXPLORER_FILE);
    }

    /**
     * Returns the value mapped by key, or throws if no such mapping exists.
     *
     * @param jsonObject JSONObject
     * @param key
     * @return (Object) result.
     */
    public static Object getValueInJsonObj(JSONObject jsonObject, String key) {
        if (jsonObject.has(key)) {
            try {
                return jsonObject.get(key);
            } catch (JSONException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Returns the value mapped by key, or throws if no such mapping exists.
     *
     * @param jsonObject JSONObject
     * @param key
     * @return (String) result.
     */
    public static String getStringInJsonObj(JSONObject jsonObject, String key) {
        if (jsonObject.has(key)) {
            try {
                String temp = String.valueOf(jsonObject.get(key));
                if (temp.equalsIgnoreCase("null")) {
                    return "";
                }
                return temp;
            } catch (JSONException e) {
                return "";
            }
        } else {
            return "";
        }
    }

    public static ArrayList<Object> getObjectInJsonArray(JSONArray jsonArray) {
        ArrayList<Object> arrayList = new ArrayList<Object>();
        int length = jsonArray.length();
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                try {
                    arrayList.add(jsonArray.get(i));
                } catch (JSONException e) {
                }
            }
        }

        return arrayList;
    }

    public static ArrayList<String> getStringInJsonArray(JSONArray jsonArray) {
        ArrayList<String> arrayList = new ArrayList<String>();
        int length = jsonArray.length();
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                try {
                    arrayList.add(String.valueOf(jsonArray.get(i)));
                } catch (JSONException e) {
                }
            }
        }

        return arrayList;
    }

    public static String getStringInJsonArray(JSONArray jsonArray, int i) {
        try {
            return String.valueOf(jsonArray.get(i));
        } catch (JSONException e) {
            return "";
        }
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);
        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;
        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;
        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);
        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, null, targetRect, null);
        return dest;
    }

    public static void resizeImage(String oldPathFile, String newPathFile, float maxImageSize, boolean isDeleteOrigin) {
        resizeImage(oldPathFile, newPathFile, maxImageSize, null, isDeleteOrigin);
    }

    private static void resizeImage(String oldPathFile, String newPathFile, float maxImageSize, Matrix matrix, boolean isDeleteOrigin) {
        if (oldPathFile.equals(newPathFile)) {
            isDeleteOrigin = false;
        }
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeFile(oldPathFile, options);
            Bitmap realImage = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            float ratio = Math.min(maxImageSize / realImage.getWidth(), maxImageSize / realImage.getHeight());
            int width = Math.round(ratio * realImage.getWidth());
            int height = Math.round(ratio * realImage.getHeight());

            Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width, height, true);
            File file = new File(newPathFile);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            newBitmap.recycle();
            realImage.recycle();
            if (isDeleteOrigin) {
                new File(oldPathFile).delete();
            }
        } catch (Exception e) {
            DebugLog.loge(e);
        }
    }

    public static void resizeImageAndRotate(String oldPathFile, String newPathFile, int maxImageSize, int quality, boolean isDeleteOrigin) {
        try {
            ExifInterface exif = new ExifInterface(oldPathFile);
            String TAG_ORIENTATION = exif.getAttribute(ExifInterface.TAG_ORIENTATION);

            Matrix matrix = new Matrix();
            if (Integer.parseInt(TAG_ORIENTATION) == ExifInterface.ORIENTATION_ROTATE_90) {
                DebugLog.loge("rotate: 90");
                matrix.postRotate(90);
            } else if (Integer.parseInt(TAG_ORIENTATION) == ExifInterface.ORIENTATION_ROTATE_180) {
                DebugLog.loge("rotate: 180");
                matrix.postRotate(180);
            } else if (Integer.parseInt(TAG_ORIENTATION) == ExifInterface.ORIENTATION_ROTATE_270) {
                DebugLog.loge("rotate: 270");
                matrix.postRotate(270);
            }

            if (TAG_ORIENTATION.equals("0")) {
                resizeImage(oldPathFile, newPathFile, maxImageSize, null, isDeleteOrigin);
            } else {
                resizeImage(oldPathFile, newPathFile, maxImageSize, matrix, isDeleteOrigin);
            }
        } catch (Exception e) {
        }
    }

    public static void rotateImage(String oldPathFile, String newPathFile, boolean isDeleteOrigin) {
        if (oldPathFile.equals(newPathFile)) {
            isDeleteOrigin = false;
        }
        try {
            ExifInterface exif = new ExifInterface(oldPathFile);
            String TAG_ORIENTATION = exif.getAttribute(ExifInterface.TAG_ORIENTATION);

            Matrix matrix = new Matrix();
            if (Integer.parseInt(TAG_ORIENTATION) == ExifInterface.ORIENTATION_ROTATE_90) {
                DebugLog.loge("rotate: 90");
                matrix.postRotate(90);
            } else if (Integer.parseInt(TAG_ORIENTATION) == ExifInterface.ORIENTATION_ROTATE_180) {
                DebugLog.loge("rotate: 180");
                matrix.postRotate(180);
            } else if (Integer.parseInt(TAG_ORIENTATION) == ExifInterface.ORIENTATION_ROTATE_270) {
                DebugLog.loge("rotate: 270");
                matrix.postRotate(270);
            }

            if (!TAG_ORIENTATION.equals("0")) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = false;
                Bitmap bitmap = BitmapFactory.decodeFile(oldPathFile, options);
                Bitmap realImage = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                File file = new File(newPathFile);
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                realImage.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
                realImage.recycle();

                DebugLog.logd("\norigin:\npath --> " + oldPathFile + "\nsize --> " + new File(oldPathFile).length());
                DebugLog.logd("\nresize:\npath --> " + newPathFile + "\nsize --> " + file.length());
            }
            if (isDeleteOrigin) {
                new File(oldPathFile).delete();
            }
        } catch (Exception e) {
            DebugLog.loge(e);
        }
    }

    /**
     * Sets the typeface and style in which the text should be displayed.
     */
    public static void setTypeface(Context context, View view, String fontAssets) {
        Typeface typeFont;
        if (fontAssets.length() > 0) {
            typeFont = Typeface.createFromAsset(context.getAssets(), fontAssets);
        } else {
            typeFont = Typeface.createFromAsset(context.getAssets(), "");
        }
        if (view instanceof TextView) {
            ((TextView) view).setTypeface(typeFont);
        } else if (view instanceof EditText) {
            ((EditText) view).setTypeface(typeFont);
        } else if (view instanceof Button) {
            ((Button) view).setTypeface(typeFont);
        } else if (view instanceof TextInputLayout) {
            ((TextInputLayout) view).setTypeface(typeFont);
        } else if (view instanceof TextInputEditText) {
            ((TextInputEditText) view).setTypeface(typeFont);
        }
    }

    public static void overrideFonts(final Context context, final View view, String fontAssets) {
        try {
            if (view instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) view;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    overrideFonts(context, child, fontAssets);
                }
            } else {
                setTypeface(context, view, fontAssets);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Start Resize Width View Animation
     */
    public static void startResizeWidthViewAnimation(View view, long duration, int width) {
        ResizeWidthAnimation anim = new ResizeWidthAnimation(view, width);
        anim.setDuration(duration);
        view.startAnimation(anim);
    }

    public static void startResizeHeightViewAnimation(View view, long duration, int height) {
        ResizeHeightAnimation anim = new ResizeHeightAnimation(view, height);
        anim.setDuration(duration);
        view.startAnimation(anim);
    }

    /**
     * Start Resize Width View Animation
     */
    public static void startResizeWidthViewAnimation(View view, long duration, int position, int width) {
        animate(view).x(position).setDuration(duration);
        ResizeWidthAnimation anim = new ResizeWidthAnimation(view, width);
        anim.setDuration(duration);
        view.startAnimation(anim);
    }

    public static void startResizeHeightViewAnimation(View view, long duration, int position, int height) {
        animate(view).y(position).setDuration(duration);
        ResizeHeightAnimation anim = new ResizeHeightAnimation(view, height);
        anim.setDuration(duration);
        view.startAnimation(anim);
    }

    /**
     * Start View Alpha Animation
     */
    public static void startViewAlphaAnimation(boolean isShow, final View view, final long duration) {
        if (isShow) {
            view.setVisibility(View.VISIBLE);
            animate(view).alpha(1).setDuration(duration);
        } else {
            animate(view).alpha(0).setDuration(duration);
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    view.setVisibility(View.GONE);
                }
            }, duration);
        }
    }

    /**
     * Start Scale View Animation
     */
    public static void startScaleViewAnimation(final View view, float scale, long duration) {
        animate(view).scaleX(scale).scaleY(scale).setDuration(duration);
    }

    /**
     * Get InfoDevices.
     */
    public static String getInfoDevices(Context context) {
        StringBuilder information = new StringBuilder();
        information.append("Locale: ").append(Locale.getDefault()).append('\n');
        try {
            if (context != null) {
                PackageManager packageManager = context.getPackageManager();
                PackageInfo packageInfo;
                packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
                information.append("Package: ").append(packageInfo.packageName).append('\n');
                information.append("Version: ").append(packageInfo.versionName).append('\n');
                information.append("VersionCode: ").append(packageInfo.versionCode).append('\n');
            } else {
                information.append("Context == null\n");
            }

        } catch (Exception e) {
            DebugLog.loge("Error:\n" + e);
            if (context != null) {
                information.append("Could not get Version information for").append(context.getPackageName()).append('\n');
            }

        }
        information.append("Phone Model:").append(android.os.Build.MODEL).append('\n');
        information.append("Android Version:").append(android.os.Build.VERSION.RELEASE).append('\n');
        information.append("Board: ").append(android.os.Build.BOARD).append('\n');
        information.append("Brand: ").append(android.os.Build.BRAND).append('\n');
        information.append("Device: ").append(android.os.Build.DEVICE).append('\n');
        information.append("Host: ").append(android.os.Build.HOST).append('\n');
        information.append("ID: ").append(android.os.Build.ID).append('\n');
        information.append("Model: ").append(android.os.Build.MODEL).append('\n');
        information.append("Product:").append(android.os.Build.PRODUCT).append('\n');
        information.append("Type: ").append(android.os.Build.TYPE).append('\n');
        return information.toString();
    }

    /**
     * Remove Accents.
     */
    public static String removeAccents(String input) {
        String output = input;
        try {
            output = Normalizer.normalize(output, Normalizer.Form.NFD);
            output = output.replaceAll("đ", "d");
            output = output.replaceAll("Đ", "D");
            output = output.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
            output = output.replaceAll("[^\\p{ASCII}]", "");
            output = output.replaceAll("\\p{M}", "");
        } catch (Exception e) {
            return input;
        }
        if (output.isEmpty()) {
            return input;
        }
        return output;
    }

    public static String removeEndWithPattern(String input, String pattern) {
        while (input.endsWith(pattern)) {
            input = input.substring(0, input.length() - 1);
        }
        return input;
    }

    @SuppressLint("ClickableViewAccessibility")
    public static void setScrollEditText(Context context, final EditText editText, int maxLines) {
        editText.setScroller(new Scroller(context));
        editText.setMaxLines(maxLines);
        editText.setVerticalScrollBarEnabled(true);
        editText.setMovementMethod(new ScrollingMovementMethod());

        editText.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (view.getId() == editText.getId()) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:
                            view.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });
    }

    public static void scrollUpToTop(final ScrollView scrollView, long delayTime) {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                scrollView.smoothScrollTo(0, 0);
            }
        }, delayTime);
    }

    public static void shareFile(Context context, String path) {
        try {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            int index = new File(path).getName().lastIndexOf('.') + 1;
            String ext = new File(path).getName().substring(index).toLowerCase();
            String type = mime.getMimeTypeFromExtension(ext);
            Uri uri = getUriFromPath(context, path);
            Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            } else {
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.setType(type);
            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.message_choose_an_app)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void shareMultiFile(Context context, List<String> listFileShare) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        ArrayList<Uri> files = new ArrayList<>();
        for (String path : listFileShare) {
            Uri uri = getUriFromPath(context, path);
            files.add(uri);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.setType("*/*");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
        context.startActivity(intent);
    }

    @SuppressLint("DefaultLocale")
    public static void shareViaFacebook(Context context, String urlToShare) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, urlToShare);

        // See if official Facebook app is found
        boolean facebookAppFound = false;
        List<ResolveInfo> matches = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo info : matches) {
            if (info.activityInfo.packageName.toLowerCase().startsWith("com.facebook.katana")) {
                intent.setPackage(info.activityInfo.packageName);
                facebookAppFound = true;
                break;
            }
        }

        // As fallback, launch sharer.php in a browser
        if (!facebookAppFound) {
            String sharerUrl = "https://www.facebook.com/sharer/sharer.php?u=" + urlToShare;
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharerUrl));
        }

        context.startActivity(intent);
    }

    public static void gotoStore(Context context, String packageName) {
        Uri uri = Uri.parse("market://details?id=" + packageName);
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW);
        myAppLinkToMarket.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        myAppLinkToMarket.setData(uri);
        try {
            context.startActivity(myAppLinkToMarket);
        } catch (Exception e) {
            myAppLinkToMarket.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
            context.startActivity(myAppLinkToMarket);
        }
    }

    public static void copyTextToCLipBoard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Clipboard", text);
        clipboard.setPrimaryClip(clip);
    }

    public static void addContentMediaForImageFile(Context context, File file, String picture_title, String picture_description) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, picture_title);
        values.put(MediaStore.Images.Media.DESCRIPTION, picture_description);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.ImageColumns.BUCKET_ID, file.toString().toLowerCase(Locale.US).hashCode());
        values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, file.getName().toLowerCase(Locale.US));
        values.put("_data", file.getAbsolutePath());

        ContentResolver cr = context.getContentResolver();
        cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    @SuppressLint("Wakelock")
    public static void wakeLockScreen(Context context) {
        try {
            // wave lock screen
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            @SuppressLint("InvalidWakeLockTag") @SuppressWarnings("deprecation")
            PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "WakeLock");
            wakeLock.acquire();
        } catch (Exception e) {
            DebugLog.loge(e);
        }
    }

    public static Uri getUriFromPath(Context context, String path) {
        if (context == null || path == null || path.isEmpty()) {
            return null;
        }
        try {
            Uri uri;
            if (Build.VERSION.SDK_INT < 24) {
                uri = Uri.fromFile(new File(path));
            } else {
                uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", new File(path));
            }
            return uri;
        } catch (Exception e) {
            DebugLog.loge(e);
        }
        return null;
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public static void tellAndroidAboutFile(Context mContext, File file) {
        try {
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(getUriFromPath(mContext, file.getAbsolutePath()));
            mContext.sendBroadcast(intent);
        } catch (Exception e) {
            DebugLog.loge(e);
        }
    }

    public static Drawable getThumbnailIcon(String filePath, Context context) {
        Drawable icon = null;
        PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
        if (packageInfo != null) {
            ApplicationInfo appInfo = packageInfo.applicationInfo;
            appInfo.sourceDir = filePath;
            appInfo.publicSourceDir = filePath;
            icon = appInfo.loadIcon(context.getPackageManager());
        }
        return icon;
    }

    public static String getDeviceId(Context context) {
        try {
            @SuppressLint("HardwareIds") String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(android_id.getBytes());
            byte messageDigest[] = digest.digest();
            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < messageDigest.length; i++) {
                byte aMessageDigest = messageDigest[i];
                StringBuilder h = new StringBuilder(Integer.toHexString(0xFF & aMessageDigest));
                while (h.length() < 2)
                    h.insert(0, "0");
                hexString.append(h);
            }
            return hexString.toString().toUpperCase();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean isAppRunning(Context context, String packageName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        for (int i = 0; i < procInfos.size(); i++) {
            if (procInfos.get(i).processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isScreenOn(Context context) {
        try {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (Build.VERSION.SDK_INT >= 21) {
                return powerManager.isInteractive();
            } else {
                return powerManager.isScreenOn();
            }
        } catch (Exception e) {
            DebugLog.loge(e);
        }
        return false;
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                DebugLog.logd(service.service.getClassName() + " is running");
                return true;
            }
        }
        return false;
    }

    public static boolean isEllipsized(TextView textView) {
        Layout layout = textView.getLayout();
        if (layout != null) {
            int lines = layout.getLineCount();
            if (lines > 0) {
                int ellipsisCount = layout.getEllipsisCount(lines - 1);
                return ellipsisCount > 0;
            }
        }
        return false;
    }

    public static boolean isLayoutR2L(Context context) {
        Configuration config = context.getResources().getConfiguration();
        return Build.VERSION.SDK_INT >= 17 && config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    public static String formatTwoDigits(long input) {
        String formatted = String.valueOf(input);
        if (input < 10 && input >= 0) {
            formatted = "0" + input;
        }
        return formatted;
    }

    public static void openCalendar(Context context) {
        try {
            Intent calIntent = new Intent(Intent.ACTION_MAIN);
            calIntent.addCategory(Intent.CATEGORY_APP_CALENDAR);
            calIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(calIntent);
        } catch (Exception e) {
            DebugLog.loge(e);
        }
    }

    public static boolean openAlarm(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            Intent alarmClockIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
            alarmClockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Verify clock implementation
            String clockImpls[][] = {
                    {"HTC Alarm Clock", "com.htc.android.worldclock", "com.htc.android.worldclock.WorldClockTabControl"},
                    {"Standar Alarm Clock", "com.android.deskclock", "com.android.deskclock.AlarmClock"},
                    {"Mobiistar Alarm Clock", "com.android.deskclock", "com.android.deskclock.DeskClock"},
                    {"Huawei Alarm Clock", "com.android.deskclock", "com.android.deskclock.AlarmsMainActivity"},
                    {"Vivo Alarm Clock", "com.android.BBKClock", "com.android.BBKClock.Timer"},
                    {"Froyo Nexus Alarm Clock", "com.google.android.deskclock", "com.android.deskclock.DeskClock"},
                    {"Moto Blur Alarm Clock", "com.motorola.blur.alarmclock", "com.motorola.blur.alarmclock.AlarmClock"},
                    {"Samsung Galaxy Clock", "com.sec.android.app.clockpackage", "com.sec.android.app.clockpackage.common.activity.ClockPackage"},
                    {"Samsung Galaxy Clock", "com.sec.android.app.clockpackage", "com.sec.android.app.clockpackage.ClockPackage"},
                    {"Sony Ericsson Xperia Z", "com.sonyericsson.organizer", "com.sonyericsson.organizer.Organizer_WorldClock"},
                    {"ASUS Tablets", "com.asus.deskclock", "com.asus.deskclock.DeskClock"},
                    {"LG Alarm Clock", "com.lge.clock", "com.lge.clock.AlarmClockActivity"},
                    {"LG Alarm Clock", "com.lge.clock", "com.lge.clock.DefaultAlarmClockActivity"}
            };

            boolean foundClockImpl = false;

            for (String[] clockImpl : clockImpls) {
                String vendor = clockImpl[0];
                String packageName = clockImpl[1];
                String className = clockImpl[2];
                try {
                    ComponentName cn = new ComponentName(packageName, className);
                    packageManager.getActivityInfo(cn, PackageManager.GET_META_DATA);
                    alarmClockIntent.setComponent(cn);
                    DebugLog.loge("Found " + vendor + " --> " + packageName + "/" + className);
                    foundClockImpl = true;
                } catch (PackageManager.NameNotFoundException e) {
                    DebugLog.loge(vendor + " does not exists");
                }
            }

            if (foundClockImpl) {
                context.startActivity(alarmClockIntent);
                return true;
            }
        } catch (Exception e) {
            DebugLog.loge(e);
        }
        return false;
    }

    public static void setBackgroundColor(View view, int color) {
        if (view != null) {
            Drawable background = view.getBackground();
            if (background instanceof GradientDrawable) {
                ((GradientDrawable) background).setColor(color);
            }
        }
    }

    @NonNull
    public static String getRandomId() {
        String possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        long currentTime = System.currentTimeMillis();
        StringBuilder builder = new StringBuilder(String.valueOf(currentTime));
        builder.append("_");
        for (int i = 0; i < 20; i++) {
            Random random = new Random();
            builder.append(possible.charAt(random.nextInt(possible.length())));
        }
        return builder.toString();
    }

    public static boolean isLocationServiceEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static void requestEnableLocationService(Context context) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, RequestCodes.REQUEST_ENABLE_GPS_SERVICE);
        } else {
            context.startActivity(intent);
        }
    }

    public static void openMap(@NonNull Context context, double latitude, double longitude) {
        try {
            String uri = String.format(Locale.getDefault(), "geo:%f,%f", latitude, longitude);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            context.startActivity(intent);
        } catch (Exception e) {
            DebugLog.loge(e);
            String uri = "https://www.google.com/maps/@" + latitude + "," + longitude;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            context.startActivity(intent);
        }
    }

    public static boolean isEmptyList(List list) {
        return list == null || list.isEmpty();
    }

    public static String getLanguage(Context context) {
        String defaultLanguage = "en";
        try {
            Configuration config = context.getResources().getConfiguration();
            Locale locale = Build.VERSION.SDK_INT >= 24 ? config.getLocales().get(0) : config.locale;
            defaultLanguage = locale.getLanguage();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultLanguage;
    }

    @SuppressLint("MissingPermission")
    public static void enableWifiConnection(@NonNull Context context) {
        try {
            @SuppressLint("WifiManagerPotentialLeak") WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isWifiEnable(@NonNull Context context) {
        @SuppressLint("WifiManagerPotentialLeak") WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager != null && wifiManager.isWifiEnabled();
    }

    @SuppressLint("MissingPermission")
    public static void addNetwork(@NonNull Context context, @NonNull String ssid, @NonNull String password) {
        try {
            if (!isWifiEnable(context)) {
                enableWifiConnection(context);
            }
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = String.format("\"%s\"", ssid);
            wifiConfig.preSharedKey = String.format("\"%s\"", password);
            @SuppressLint("WifiManagerPotentialLeak") WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            int networkId = wifiManager.getConnectionInfo().getNetworkId();
            wifiManager.removeNetwork(networkId);
            wifiManager.saveConfiguration();
            //remember id
            int netId = wifiManager.addNetwork(wifiConfig);
            wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();

            Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
            context.startActivity(intent);
        } catch (Exception e) {
            DebugLog.loge(e);
        }
    }

    private static int mCountResultScanMedia = 0;
    private static String[] paths;

    public static void scanMediaFile(final Context context, final ScanMediaListener listener) {
        if (context == null) {
            return;
        }

        if (listener != null) {
            listener.scanStarted();
        }

        File internalStorage = Environment.getExternalStorageDirectory();
        String sdCardStorage = FileUtils.getPathSDCard(context);
        if (sdCardStorage != null && !sdCardStorage.isEmpty()) {
            paths = new String[]{internalStorage.getAbsolutePath(), sdCardStorage};
        } else {
            paths = new String[]{internalStorage.getAbsolutePath()};
        }

        MediaScannerConnection.scanFile(context, paths, null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                DebugLog.logd("Scanned " + path);
                mCountResultScanMedia++;
                if (mCountResultScanMedia == paths.length && listener != null) {
                    if (context != null && context instanceof Activity) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.scanCompleted();
                            }
                        });
                    } else {
                        listener.scanCompleted();
                    }
                }
            }
        });
    }

    public interface ScanMediaListener {
        void scanStarted();

        void scanCompleted();
    }

}
