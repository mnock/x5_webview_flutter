package com.cjx.x5_webview;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Utils {
    static public boolean isTranslucentOrFloating(Context context) {
        boolean isTranslucentOrFloating = false;
        try {
            int[] styleableRes = (int[]) Class.forName("com.android.internal.R$styleable").getField("Window").get(null);
            final TypedArray ta = context.obtainStyledAttributes(styleableRes);
            Method m = ActivityInfo.class.getMethod("isTranslucentOrFloating", TypedArray.class);
            m.setAccessible(true);
            isTranslucentOrFloating = (boolean) m.invoke(null, ta);
            m.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isTranslucentOrFloating;
    }

    static public boolean fixOrientation(Context context) {
        try {
            Field field = Activity.class.getDeclaredField("mActivityInfo");
            field.setAccessible(true);
            ActivityInfo o = (ActivityInfo) field.get(context);
            o.screenOrientation = -1;
            field.setAccessible(false);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
