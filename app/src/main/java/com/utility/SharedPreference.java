package com.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class SharedPreference {
    private static SharedPreferences sharedPreferences;

    // String
    public static String getString(Context context, Object key, String defaultValue) {
        if (context != null) {
            try {
                sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
            } catch (Exception e) {
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            }
        }
        try {
            return sharedPreferences.getString(String.valueOf(key), defaultValue);
        } catch (Exception e) {
            DebugLog.loge(e);
            return defaultValue;
        }
    }

    public static void setString(Context context, Object key, String data) {
        try {
            if (context != null) {
                try {
                    sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
                } catch (Exception e) {
                    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                }
            }
            Editor editor = sharedPreferences.edit();
            editor.putString(String.valueOf(key), data);
            editor.commit();
        } catch (Exception e) {
            DebugLog.loge(e);
        }
    }

    // Long
    public static Long getLong(Context context, Object key, Long defaultValue) {
        if (context != null) {
            try {
                sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
            } catch (Exception e) {
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            }
        }
        try {
            return sharedPreferences.getLong(String.valueOf(key), defaultValue);
        } catch (Exception e) {
            DebugLog.loge(e);
            return defaultValue;
        }
    }

    public static void setLong(Context context, Object key, Long data) {
        try {
            if (context != null) {
                try {
                    sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
                } catch (Exception e) {
                    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                }
            }
            Editor editor = sharedPreferences.edit();
            editor.putLong(String.valueOf(key), data);
            editor.commit();
        } catch (Exception e) {
            DebugLog.loge(e);
        }
    }

    // Boolean
    public static Boolean getBoolean(Context context, Object key, Boolean defaultValue) {
        if (context != null) {
            try {
                sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
            } catch (Exception e) {
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            }
        }
        try {
            return sharedPreferences.getBoolean(String.valueOf(key), defaultValue);
        } catch (Exception e) {
            DebugLog.loge(e);
            return defaultValue;
        }
    }

    public static void setBoolean(Context context, Object key, Boolean data) {
        try {
            if (context != null) {
                try {
                    sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
                } catch (Exception e) {
                    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                }
            }
            Editor editor = sharedPreferences.edit();
            editor.putBoolean(String.valueOf(key), data);
            editor.commit();
        } catch (Exception e) {
            DebugLog.loge(e);
        }
    }

    // Integer
    public static Integer getInt(Context context, Object key, Integer defaultValue) {
        if (context != null) {
            try {
                sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
            } catch (Exception e) {
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            }
        }
        try {
            return sharedPreferences.getInt(String.valueOf(key), defaultValue);
        } catch (Exception e) {
            DebugLog.loge(e);
            return defaultValue;
        }
    }

    public static void setInt(Context context, Object key, Integer data) {
        try {
            if (context != null) {
                try {
                    sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
                } catch (Exception e) {
                    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                }
            }
            Editor editor = sharedPreferences.edit();
            editor.putInt(String.valueOf(key), data);
            editor.commit();
        } catch (Exception e) {
            DebugLog.loge(e);
        }
    }

    // Float
    public static Float getFloat(Context context, Object key, Float defaultValue) {
        if (context != null) {
            try {
                sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
            } catch (Exception e) {
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            }
        }
        try {
            return sharedPreferences.getFloat(String.valueOf(key), defaultValue);
        } catch (Exception e) {
            DebugLog.loge(e);
            return defaultValue;
        }
    }

    public static void setFloat(Context context, Object key, Float data) {
        try {
            if (context != null) {
                try {
                    sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
                } catch (Exception e) {
                    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                }
            }
            Editor editor = sharedPreferences.edit();
            editor.putFloat(String.valueOf(key), data);
            editor.commit();
        } catch (Exception e) {
            DebugLog.loge(e);
        }
    }

}
