package com.jakubhekal.datausage;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.jakubhekal.datausage.R;
import com.jakubhekal.datausage.managers.PreferenceManager;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Calendar;
import java.util.Locale;

public class Utils {

    //byte 문자열로 변환
    public static String convertFromBytes(long bytes) {
        // bytes가 0 미만일 때 0 B로 표시
        if(bytes < 0) {
            return "0 B";
        }

        // 1000 미만일 때 단위 B
        if (bytes < 1000) {
            return bytes + " B";
        }

        /*
         CharacterIterator : 반복 함수 정의
         문자열 k - M - G - T - P - E 반복
        while 문 : bytes가 999,950보다 크거나 같을 때, ci 반복 (1000단위로)
         */
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format(Locale.getDefault(), "%.1f %cB", bytes / 1000.0, ci.current());
        // %cB에서 "c"는 ci "B"는 Byte(고정)
    }

    // 남은 날짜 정보에 대한 format 매서드
    public static String formatDaysReaming(Context context, int days, String date) {
        if(days > 5 || days == 0) {
            return String.format(context.getString(R.string.period_info_main_3), days, date);
        } else if (days > 2) {
            return String.format(context.getString(R.string.period_info_main_2), days, date);
        } else {
            return String.format(context.getString(R.string.period_info_main_1), days, date);
        }
    }

    // 테마 (Dark, Light) 가져오기
    public static String getThemeName(Context context){
        switch (AppCompatDelegate.getDefaultNightMode()) {
            case AppCompatDelegate.MODE_NIGHT_YES:
                return context.getString(R.string.dialog_theme_dark);

            case AppCompatDelegate.MODE_NIGHT_NO:
                return context.getString(R.string.dialog_theme_light);

            default:
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
            case AppCompatDelegate.MODE_NIGHT_UNSPECIFIED:
                return context.getString(R.string.dialog_theme_system);

        }
    }

    // 테마 설정 후 재시작
    public static void setTheme(Context context, int mode){
        AppCompatDelegate.setDefaultNightMode(mode);
        Bundle bundle = ActivityOptions.makeCustomAnimation(context, android.R.anim.fade_in,android.R.anim.fade_out).toBundle();
        Intent intent = new Intent(context, context.getClass());
        ((Activity) context).finish();
        context.startActivity(intent, bundle);
    }

    public static int getAttrColor(Context context, int attr){
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        return ContextCompat.getColor(context, typedValue.resourceId);
    }

}
