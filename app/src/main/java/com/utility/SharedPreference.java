package com.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class SharedPreference {
    private static SharedPreferences sharedPreferences;
    private static String PREFERENCE_MODE = "PREFERENCE_MODE";
    private static PreferenceMode defaultPreferenceMode = PreferenceMode.MODE_PRIVATE;

    public enum PreferenceMode {
        MODE_PRIVATE("MODE_PRIVATE"),
        MODE_DEFAULT("MODE_DEFAULT");

        protected String value;

        PreferenceMode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static void setPreferenceMode(Context context, PreferenceMode mode) {
        if (context == null || mode == null) {
            return;
        }
        SharedPreferences sharedPreferences;
        try {
            sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        } catch (Exception e) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
        Editor editor = sharedPreferences.edit();
        editor.putString(PREFERENCE_MODE, mode.toString());
        editor.commit();
    }

    public static PreferenceMode getCurrentPreferenceMode(Context context) {
        if (context == null) {
            return defaultPreferenceMode;
        }
        SharedPreferences sharedPreferences;
        try {
            sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        } catch (Exception e) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return PreferenceMode.valueOf(sharedPreferences.getString(PREFERENCE_MODE, defaultPreferenceMode.toString()));
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        if (context == null) {
            return null;
        }
        try {
            if (getCurrentPreferenceMode(context) == PreferenceMode.MODE_PRIVATE) {
                sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
            } else {
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            }
        } catch (Exception e) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return sharedPreferences;
    }

    // String
    public static String getString(Context context, Object key, String defaultValue) {
        if (context != null) {
            try {
                return getSharedPreferences(context).getString(String.valueOf(key), defaultValue);
            } catch (Exception e) {
                DebugLog.loge(e);
            }
        }
        return defaultValue;
    }

    public static void setString(Context context, Object key, String data) {
        try {
            if (context != null) {
                Editor editor = getSharedPreferences(context).edit();
                editor.putString(String.valueOf(key), data);
                editor.apply();
            }
        } catch (Exception e) {
            DebugLog.loge(e);
        }
    }

    // Long
    public static Long getLong(Context context, Object key, Long defaultValue) {
        if (context != null) {
            try {
                return getSharedPreferences(context).getLong(String.valueOf(key), defaultValue);
            } catch (Exception e) {
                DebugLog.loge(e);
            }
        }
        return defaultValue;
    }

    public static void setLong(Context context, Object key, Long data) {
        if (context != null) {
            try {
                Editor editor = getSharedPreferences(context).edit();
                editor.putLong(String.valueOf(key), data);
                editor.apply();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Boolean
    public static Boolean getBoolean(Context context, Object key, Boolean defaultValue) {
        if (context != null) {
            try {
                return getSharedPreferences(context).getBoolean(String.valueOf(key), defaultValue);
            } catch (Exception e) {
                DebugLog.loge(e);
            }
        }
        return defaultValue;
    }

    public static void setBoolean(Context context, Object key, Boolean data) {
        if (context != null) {
            try {
                Editor editor = getSharedPreferences(context).edit();
                editor.putBoolean(String.valueOf(key), data);
                editor.apply();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Integer
    public static Integer getInt(Context context, Object key, Integer defaultValue) {
        if (context != null) {
            try {
                return getSharedPreferences(context).getInt(String.valueOf(key), defaultValue);
            } catch (Exception e) {
                DebugLog.loge(e);
            }
        }
        return defaultValue;
    }

    public static void setInt(Context context, Object key, Integer data) {
        try {
            if (context != null) {
                Editor editor = getSharedPreferences(context).edit();
                editor.putInt(String.valueOf(key), data);
                editor.apply();
            }
        } catch (Exception e) {
            DebugLog.loge(e);
        }
    }

    // Float
    public static Float getFloat(Context context, Object key, Float defaultValue) {
        if (context != null) {
            try {
                return getSharedPreferences(context).getFloat(String.valueOf(key), defaultValue);
            } catch (Exception e) {
                DebugLog.loge(e);
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public static void setFloat(Context context, Object key, Float data) {
        try {
            if (context != null) {
                Editor editor = getSharedPreferences(context).edit();
                editor.putFloat(String.valueOf(key), data);
                editor.apply();
            }
        } catch (Exception e) {
            DebugLog.loge(e);
        }
    }

}
