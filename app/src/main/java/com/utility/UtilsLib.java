package com.utility;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.v4.content.FileProvider;
import android.telephony.TelephonyManager;
import android.text.Html;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.utility.others.ResizeHeightAnimation;
import com.utility.others.ResizeWidthAnimation;
import com.utility.others.UnCaughtException;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import Decoder.BASE64Decoder;
import Decoder.BASE64Encoder;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

@SuppressLint({"SimpleDateFormat", "InlinedApi", "DefaultLocale"})
@SuppressWarnings("unused")
public class UtilsLib {
    public static final String FORMAT_DATE_TIME = "yyyy-MM-dd'T'HH:mm'Z'";

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

    public static long getCurrentTimeMiliByTimeZone(int timezone) {
        long result = System.currentTimeMillis() - ((getTimeZoneInLocal() - timezone) * 3600 * 1000);
        return result;
    }

    /**
     * Get DateTime.
     *
     * @param dateTimeInMilliseconds datetime in milliseconds
     * @param format                 like "yyyy/MM/dd HH:mm:ss"
     * @return (String) result allow format
     */
    public static String getDateTime(Object dateTimeInMilliseconds, String format) {
        long value = 0;
        try {
            value = checkLongValue(String.valueOf(dateTimeInMilliseconds));
        } catch (Exception e) {
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(value);
    }

    /**
     * Get DateTime.
     *
     * @param inputTime     datetime in ISO 8601 format like "yyyy-MM-dd'T'HH:mm'Z'"
     * @param outputPattern like "yyyy-MM-dd'T'HH:mm'Z'"
     * @return (String) result allow format
     */
    public static String parseTime(String inputTime, String outputPattern) {
        String time = inputTime;
        try {
            DateTime dateTime = new DateTime(inputTime);
            return dateTime.toString(outputPattern, Locale.getDefault());
        } catch (Exception e) {
            DebugLog.loge(e);
        }
        return time;
    }

    /**
     * Get Time in Milliseconds.
     *
     * @param inputTime datetime in ISO 8601 format like "yyyy-MM-dd'T'HH:mm'Z'"
     * @return (Long) result time in milliseconds
     */
    public static long parseTimeMilliseconds(String inputTime) {
        try {
            if (inputTime.isEmpty()) {
                return 0;
            }
            DateTime dateTime = new DateTime(inputTime);
            return dateTime.toDate().getTime();
        } catch (Exception e) {
            DebugLog.loge(e);
        }
        return 0;
    }

    /**
     * Get Time in Milliseconds.
     *
     * @param dayOfMonth
     * @param month
     * @param year
     * @param hour
     * @param minute
     * @param second
     * @return (Long) result time in milliseconds
     */
    public static long parseTimeToMiliseconds(int dayOfMonth, int month, int year, int hour, int minute, int second) {
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
     *
     * @param context The context to use. Usually your Application or Activity
     *                object.
     * @param message The text to show. Can be formatted text.
     */
    public static void showToast(Context context, String message) {
        if (context == null) {
            return;
        }
        if (!message.isEmpty()) {
            try {
                Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 230);
                toast.show();
            } catch (Exception e) {
                DebugLog.loge(e);
            }
        }
    }

    /**
     * Make a standard toast that just contains a text view in custom location
     * on screen.
     *
     * @param context The context to use. Usually your Application or Activity
     *                object.
     * @param message The text to show. Can be formatted text.
     * @param gravity Set the location at which the notification should appear on
     *                the screen.
     */
    public static void showToast(Context context, String message, int gravity) {
        if (context == null) {
            return;
        }
        if (!message.isEmpty() && context != null) {
            try {
                Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
                toast.setGravity(gravity, 0, 0);
                setToastMessageCenter(toast);
                toast.show();
            } catch (Exception e) {
                DebugLog.loge(e);
            }
        }
    }

    private static Toast setToastMessageCenter(Toast toast) {
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if (v != null) {
            v.setGravity(Gravity.CENTER);
        }
        return toast;
    }

    /**
     * Check Input string is Null or Empty
     *
     * @return (boolean) true/false
     */
    public static boolean isNullOrEmpty(Object obj) {
        String inputString = String.valueOf(obj);
        if (obj == null) {
            return true;
        } else {
            if (inputString.isEmpty()) {
                return true;
            } else {
                if (inputString.equals("null")) {
                    return true;
                }
            }
        }
        return false;
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
                    if (view == null || view.getText().toString().isEmpty() || view.getText().toString().equals("")) {
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
        } catch (Exception e) {
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
    public static void showKeybroad(Context context, EditText editText) {
        if (context == null) {
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

    public static void updateListHeight(ListView listview) {
        if (listview == null) {
            return;
        }
        ListAdapter listAdapter = listview.getAdapter();
        if (listAdapter == null) {
            return;

        }

        int totalHeight = 0;
        int adapterCount = listAdapter.getCount();
        for (int size = 0; size < adapterCount; size++) {
            View listItem = listAdapter.getView(size, null, listview);
            listItem.measure(0, 0);
            totalHeight = totalHeight + listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listview.getLayoutParams();
        params.height = totalHeight + (listview.getDividerHeight() * (adapterCount - 1));
        listview.setLayoutParams(params);
    }

    public static void updateListViewHeight(ListView listview, int tempHeight) {
        if (listview == null) {
            return;
        }
        ListAdapter listAdapter = listview.getAdapter();
        if (listAdapter == null) {
            return;

        }

        int totalHeight = 0;
        int adapterCount = listAdapter.getCount();
        for (int size = 0; size < adapterCount; size++) {
            View listItem = listAdapter.getView(size, null, listview);
            listItem.measure(0, 0);
            // totalHeight += measureHeight(0, listItem);
            totalHeight = totalHeight + listItem.getMeasuredHeight() + tempHeight;
        }

        ViewGroup.LayoutParams params = listview.getLayoutParams();
        params.height = totalHeight + (listview.getDividerHeight() * (adapterCount - 1));
        listview.setLayoutParams(params);
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
        if (pattern.matcher(email).matches() || pattern2.matcher(email).matches()) {
            return true;
        }
        return false;
    }

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
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse(uri));
            context.startActivity(intent);
        } catch (Exception e) {
        }
    }

    /**
     * Call Email Application On Device.
     *
     * @param context  The context to use. Usually your Application or Activity
     *                 object.
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
                // only SMS
                // apps
                // respond
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
            if (Patterns.WEB_URL.matcher(url.trim()).matches()) {
                return true;
            }
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
            if (Patterns.PHONE.matcher(phone_number.trim()).matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check package (an application) exist in device
     *
     * @param context       The context to use. Usually your Application or Activity
     *                      object.
     * @param targetPackage input targetPackage string like: com.application
     * @return (Boolean) exist or not
     */
    public boolean isPackageExisted(Context context, String targetPackage) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            return false;
        }
        return true;
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
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return px;
    }

    public static float convertPixelsToDp(Context context, float px) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
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
        } catch (NoSuchAlgorithmException e) {
        } catch (UnsupportedEncodingException e) {
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

    /**
     * Encrypt AES.
     *
     * @param data input string
     * @return (String) result.
     */

    public static String AESEncrypt(byte[] pass_key, String data) throws Exception {
        if (data.equals("")) {
            return "";
        }
        String valueToEnc = new String(data.getBytes(), "UTF-8");
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.ENCRYPT_MODE, generateKey(pass_key));
        byte[] encValue = c.doFinal(valueToEnc.getBytes());
        String encryptedValue = new BASE64Encoder().encode(encValue);
        return encryptedValue;
    }

    /**
     * Decrypt AES.
     *
     * @param encryptedValue input string
     * @return (String) result.
     */
    public static String AESDecrypt(byte[] pass_key, String encryptedValue) throws Exception {
        if (encryptedValue.equals("")) {
            return "";
        }
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.DECRYPT_MODE, generateKey(pass_key));
        byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedValue);
        byte[] decValue = c.doFinal(decordedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }

    private static final String ALGORITHM = "AES";

    private static Key generateKey(byte[] pass_key) throws Exception {
        Key key = new SecretKeySpec(pass_key, ALGORITHM);
        return key;
    }

    /**
     * Deletes this file. Directories must be empty before they will be deleted.
     *
     * @param path the path or url of file
     */
    public static void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            boolean result = file.delete();
        }
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
            if (e != null)
                DebugLog.loge("Error: " + e.getMessage());
        }
        return keyHash;
    }

    /**
     * Get Day In DatePicker.
     *
     * @param datePicker See DatePicker
     * @return (String) result.
     */
    public static String getDayInDatePicker(DatePicker datePicker) {
        String day = "";
        int dayOfMonth = datePicker.getDayOfMonth();

        if (dayOfMonth >= 10) {
            day = String.valueOf(dayOfMonth);
        } else {
            day = "0" + String.valueOf(dayOfMonth);
        }

        return day;
    }

    /**
     * Get Month In DatePicker.
     *
     * @param datePicker See DatePicker
     * @return (String) result.
     */
    public static String getMonthInDatePicker(DatePicker datePicker) {
        String month = "";
        int mMonth = datePicker.getMonth();
        mMonth++;
        if (mMonth >= 10) {
            month = String.valueOf(mMonth);
        } else {
            month = "0" + String.valueOf(mMonth);
        }

        return month;
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
            long dur = Long.parseLong(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            return dur;
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
        String[] files = null;
        try {
            files = assetManager.list(folderAssets);
        } catch (IOException e) {
            if (e != null)
                DebugLog.loge("Failed to get asset file list: " + e);
            return;
        }
        for (String filename : files) {
            if (file.equals("")) {
                try {
                    File outFile = new File(path, filename);
                    InputStream inputStream = assetManager.open(folderAssets + "/" + filename);
                    OutputStream outputStream = new FileOutputStream(outFile);
                    FileUtils.copyFile(inputStream, outputStream);
                    DebugLog.logd("copy asset file: " + filename);
                } catch (IOException e) {
                    if (e != null)
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
                        if (e != null)
                            DebugLog.loge("Failed to copy asset file: " + filename + "\n" + e);
                    }
                }
            }
        }
    }

    /**
     * Indicates whether network connectivity exists and it is possible to
     * establish connections and pass data
     *
     * @param context The context to use. Usually your Application or Activity
     *                object.
     * @return (boolean) true/false.
     */
    public static boolean isNetworkConnect(Context context) {
        if (context == null) {
            return false;
        }
        final ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return true;
        } else {
            return false;
        }
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
     * get list picture file
     *
     * @param directory get list file follow format: png, jpg, jpeg, bmp.
     * @return (File[]) result.
     */
    public static File[] listValidImageFiles(File directory) {
        try {
            return directory.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String filename) {
                    File file2 = new File(dir, filename);
                    return (filename.contains(".png") || filename.contains(".jpg") || filename.contains(".jpeg") || filename.contains(".bmp") || file2.isDirectory()) && !file2.isHidden()
                            && !filename.startsWith(".");
                }
            });
        } catch (Exception e) {
            DebugLog.loge(e);
        }
        return new File[]{};
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
        Typeface typeFont = null;
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
        } catch (Exception e) {
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
    @SuppressLint("DefaultLocale")
    public static String removeAccents(String input) {
        String output = input;
        try {
            output = Normalizer.normalize(output, Normalizer.Form.NFD);
            output = output.replaceAll("[^\\p{ASCII}]", "");
            output = output.replaceAll("\\p{M}", "");
        } catch (Exception e) {
        }
        return output;
    }

    public static String removeEndWithPattern(String input, String pattern) {
        while (input.endsWith(pattern)) {
            input = input.substring(0, input.length() - 1);
        }
        return input;
    }

    public static int checkIntValue(String data) {
        int result = 0;
        if (!data.equals("")) {
            try {
                result = Integer.parseInt(data);
            } catch (Exception e) {
            }
        }
        return result;
    }

    public static float checkFloatValue(String data) {
        float result = 0;
        if (!data.equals("")) {
            try {
                result = Float.parseFloat(data);
            } catch (Exception e) {
            }
        }
        return result;
    }

    public static long checkLongValue(String data) {
        long result = 0;
        if (!data.equals("")) {
            try {
                result = Long.parseLong(data);
            } catch (Exception e) {
            }
        }
        return result;
    }

    public static double checkDoubleValue(String data) {
        double result = 0;
        if (!data.equals("")) {
            try {
                result = Double.parseDouble(data);
            } catch (Exception e) {
            }
        }
        return result;
    }

    public static boolean checkBooleanValue(String data) {
        boolean result = false;
        if (!data.equals("")) {
            try {
                result = Boolean.parseBoolean(data);
            } catch (Exception e) {
            }
        }
        return result;
    }

    public static void setScrollEditText(Context context, final EditText editText, int maxLines) {
        editText.setScroller(new Scroller(context));
        editText.setMaxLines(maxLines);
        editText.setVerticalScrollBarEnabled(true);
        editText.setMovementMethod(new ScrollingMovementMethod());

        editText.setOnTouchListener(new OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
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
    public static void waveLockScreen(Context context) {
        try {
            // wave lock screen
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            @SuppressWarnings("deprecation")
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
            if (Build.VERSION.SDK_INT >= 8) {
                appInfo.sourceDir = filePath;
                appInfo.publicSourceDir = filePath;
            }
            icon = appInfo.loadIcon(context.getPackageManager());
        }
        return icon;
    }

}
