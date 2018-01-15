package com.utility;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

import java.util.HashMap;

/**
 * Created by admin on 7/19/2017.
 */

public class KeyboardUtils implements ViewTreeObserver.OnGlobalLayoutListener {
    private final static int MAGIC_NUMBER = 150;

    private SoftKeyboardToggleListener mCallback;
    private View mRootView;
    private float mScreenDensity = 1;
    private static HashMap<SoftKeyboardToggleListener, KeyboardUtils> sListenerMap = new HashMap<>();

    public interface SoftKeyboardToggleListener {
        void onToggleSoftKeyboard(boolean isVisible);
    }


    @Override
    public void onGlobalLayout() {
        Rect r = new Rect();
        mRootView.getWindowVisibleDisplayFrame(r);

        int heightDiff = mRootView.getRootView().getHeight() - (r.bottom - r.top);
        float dp = heightDiff / mScreenDensity;

        if (mCallback != null)
            mCallback.onToggleSoftKeyboard(dp > MAGIC_NUMBER);
    }


    public static void addKeyboardToggleListener(Activity act, SoftKeyboardToggleListener listener) {
        removeKeyboardToggleListener(listener);

        sListenerMap.put(listener, new KeyboardUtils(act, listener));
    }

    public static void removeKeyboardToggleListener(SoftKeyboardToggleListener listener) {
        if (sListenerMap.containsKey(listener)) {
            KeyboardUtils k = sListenerMap.get(listener);
            k.removeListener();

            sListenerMap.remove(listener);
        }
    }

    public static void removeAllKeyboardToggleListeners() {
        for (SoftKeyboardToggleListener l : sListenerMap.keySet())
            sListenerMap.get(l).removeListener();

        sListenerMap.clear();
    }

    public static void toggleKeyboardVisibility(Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    /**
     * Closes the keyboard
     *
     * @param activeView the view with the keyboard focus
     */
    public static void forceCloseKeyboard(View activeView) {
        InputMethodManager inputMethodManager = (InputMethodManager) activeView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activeView.getWindowToken(), 0);
    }

    private void removeListener() {
        mCallback = null;
        mRootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    private KeyboardUtils(Activity act, SoftKeyboardToggleListener listener) {
        mCallback = listener;

        mRootView = ((ViewGroup) act.findViewById(android.R.id.content)).getChildAt(0);
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        mScreenDensity = act.getResources().getDisplayMetrics().density;
    }
}
