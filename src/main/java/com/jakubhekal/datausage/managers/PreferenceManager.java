package com.jakubhekal.datausage.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.Gravity;

import androidx.appcompat.app.AppCompatDelegate;

public class PreferenceManager {

    SharedPreferences sharedPreferences;
    Context context;

    private static class PREF_CONSTANTS {

        public static final String LAUNCHED = "launched";
        public static final String NIGHT_MODE = "night.mode";
        public static final String PERIOD_START = "period.start";
        public static final String PERIOD_LIMIT = "period.limit";
        public static final String PERIOD_LIMIT_UNIT = "period.limit.unit";
        public static final String DAILY_LIMIT = "daily.limit";
        public static final String DAILY_LIMIT_UNIT = "daily.limit.unit";
        public static final String DAILY_LIMIT_CUSTOM = "daily.limit.custom";
        public static final String NOTIFICATION_PERMANENT = "notification.permanent";
        public static final String NOTIFICATION_WARNING = "notification.warning";
    }

    public PreferenceManager(Context context){
        this.context = context;
        sharedPreferences = this.context.getSharedPreferences("preference", Context.MODE_PRIVATE);
    }

    public void clearPreferences(){
        sharedPreferences.edit()
                .clear()
                .apply();
    }

    public void reload() {
        sharedPreferences = context.getSharedPreferences("preference", Context.MODE_PRIVATE);
    }

    //On launch
    public void setLaunched(boolean launched){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_CONSTANTS.LAUNCHED, launched);
        editor.apply();
    }
    public boolean getLaunched() {
        return sharedPreferences.getBoolean(PREF_CONSTANTS.LAUNCHED, true);
    }

    //Night mode settings
    public void setNightMode(int mode){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PREF_CONSTANTS.NIGHT_MODE, mode);
        editor.apply();
    }
    public int getNightMode() {
        return sharedPreferences.getInt(PREF_CONSTANTS.NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    //Period start
    public void setPeriodStart(int number){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PREF_CONSTANTS.PERIOD_START, number);
        editor.apply();
    }
    public int getPeriodStart() {
        return sharedPreferences.getInt(PREF_CONSTANTS.PERIOD_START,1);
    }

    // Period limit
    public void setPeriodLimit(Long bytes){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(PREF_CONSTANTS.PERIOD_LIMIT, bytes);
        editor.apply();
    }
    public Long getPeriodLimit() {
        return sharedPreferences.getLong(PREF_CONSTANTS.PERIOD_LIMIT,0);
    }

    // Period limit unit
    public void setPeriodLimitUnit(String unit){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_CONSTANTS.PERIOD_LIMIT_UNIT, unit);
        editor.apply();
    }

    public String getPeriodLimitUnit() {
        return sharedPreferences.getString(PREF_CONSTANTS.PERIOD_LIMIT_UNIT, DialogManager.DATA_SIZE_UNIT_GB);
    }

    // Daily limit
    public void setDailyLimit(Long bytes){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(PREF_CONSTANTS.DAILY_LIMIT, bytes);
        editor.apply();
    }

    public Long getDailyLimit() {
        return sharedPreferences.getLong(PREF_CONSTANTS.DAILY_LIMIT,0);
    }

    // Daily limit unit
    public void setDailyLimitUnit(String unit){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_CONSTANTS.DAILY_LIMIT_UNIT, unit);
        editor.apply();
    }

    public String getDailyLimitUnit() {
        return sharedPreferences.getString(PREF_CONSTANTS.DAILY_LIMIT_UNIT, DialogManager.DATA_SIZE_UNIT_MB);
    }

    // Daily limit custom
    public void setDailyLimitCustom(boolean isCustom){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_CONSTANTS.DAILY_LIMIT_CUSTOM, isCustom);
        editor.apply();
    }

    public boolean getDailyLimitCustom() {
        return sharedPreferences.getBoolean(PREF_CONSTANTS.DAILY_LIMIT_CUSTOM,false);
    }

    // Notification permanent
    public void setNotificationPermanent(boolean show){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_CONSTANTS.NOTIFICATION_PERMANENT, show);
        editor.apply();
    }

    public boolean getNotificationPermanent() {
        return sharedPreferences.getBoolean(PREF_CONSTANTS.NOTIFICATION_PERMANENT,true);
    }

    // Notification warning
    public void setNotificationWarning(boolean show){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_CONSTANTS.NOTIFICATION_WARNING, show);
        editor.apply();
    }

    public boolean getNotificationWarning() {
        return sharedPreferences.getBoolean(PREF_CONSTANTS.NOTIFICATION_WARNING,true);
    }

}