
package com.jakubhekal.datausage;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.jakubhekal.datausage.activities.MainActivity;
import com.jakubhekal.datausage.managers.DataUsageManager;
import com.jakubhekal.datausage.managers.NetworkUsageManager;
import com.jakubhekal.datausage.managers.PreferenceManager;
import com.jakubhekal.datausage.managers.WifiUsageManager;

/*
 * 백그라운드에서 실행
 * 네트워크 사용량 주기적으로 표시
 * 위젯 설정
 */

public class NotifyService extends Service {

    // 상수
    private static final int NOTIFICATION_PERMANENT_ID = 1;
    private static final String NOTIFICATION_PERMANENT_CHANNEL = "datausage.permanent";
    private static final int NOTIFICATION_USAGE_WARNING_ID = 2;
    private static final String NOTIFICATION_USAGE_WARNING_CHANNEL = "datausage.warning";
    private static final int HANDLER_DELAY = 2000;

    Handler handler;
    NotificationManager notificationManager;
    ConnectivityManager connectivityManager;
    PreferenceManager preferenceManager;
    NetworkUsageManager networkUsageManager;
    WifiUsageManager wifiUsageManager;
    DataUsageManager dataUsageManager;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Manager 초기화
        notificationManager = getSystemService(NotificationManager.class);
        connectivityManager = getSystemService(ConnectivityManager.class);
        handler = new Handler(Looper.getMainLooper());
        preferenceManager = new PreferenceManager(getApplicationContext());
        networkUsageManager = new NetworkUsageManager(getApplicationContext());
        wifiUsageManager = new WifiUsageManager(getApplicationContext());
        dataUsageManager = new DataUsageManager(getApplicationContext());

        //알림 채널 생성
        NotificationChannel permanent_channel = new NotificationChannel(NOTIFICATION_PERMANENT_CHANNEL, getString(R.string.setting_notification_permanent_title), NotificationManager.IMPORTANCE_LOW);
        permanent_channel.setDescription(getString(R.string.setting_notification_permanent_info));
        permanent_channel.setShowBadge(false);
        notificationManager.createNotificationChannel(permanent_channel);

        /*NotificationChannel warning_channel = new NotificationChannel(NOTIFICATION_USAGE_WARNING_CHANNEL, getString(R.string.warning_title), NotificationManager.IMPORTANCE_DEFAULT);
        warning_channel.setDescription(getString(R.string.warning_info));
        warning_channel.setVibrationPattern(new long[] {1000});
        notificationManager.createNotificationChannel(warning_channel);*/

        handler.postDelayed(new Runnable() {
            public void run() {
                // 설정 및 사용량 정보 업데이트
                preferenceManager.reload();
                boolean isMobileDataConnected = connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_MOBILE;
                int daysTillEndOfPeriod = DateTimeUtils.getDaysTillPeriodEnd(preferenceManager);

                long periodUsage = dataUsageManager.getAllBytesDataUsage(
                        DateTimeUtils.getPeriodStartMillis(preferenceManager.getPeriodStart()),
                        DateTimeUtils.getDayEndMillis());
                Long periodLimit = preferenceManager.getPeriodLimit();
                long dailyUsage = dataUsageManager.getAllBytesDataUsage(
                        DateTimeUtils.getDayStartMillis(),
                        DateTimeUtils.getDayEndMillis());
                Long dailyLimit = preferenceManager.getDailyLimitCustom() ? preferenceManager.getDailyLimit() : (periodLimit - (periodUsage-dailyUsage)) / daysTillEndOfPeriod;


                // 알림 표시 갱신
                if(preferenceManager.getNotificationPermanent()
                ) {
                    showPermanentNotification(Utils.convertFromBytes(dailyUsage), Utils.convertFromBytes(dailyLimit));
                } else {
                    killContinuousNotification();
                }

                //handler 주기적 실행
                handler.postDelayed(this, HANDLER_DELAY);
            }
        }, HANDLER_DELAY);

        return START_STICKY;
    }

    private void showPermanentNotification(String usage, String limit) {
        Intent appIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_PERMANENT_CHANNEL)
                .setSmallIcon(R.drawable.icon_data_usage)
                .setContentTitle(String.format(getString(R.string.notification_permanent_title), usage))
                .setContentText(String.format(getString(R.string.notification_permanent_info), limit))
                .setSilent(true)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(NOTIFICATION_PERMANENT_ID, builder.build());
    }

    private void killContinuousNotification() {
        notificationManager.cancel(NOTIFICATION_PERMANENT_ID);
    }

    private void showWarningNotification(int percent) {
        Intent appIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_USAGE_WARNING_CHANNEL)
                .setSmallIcon(R.drawable.icon_warning)
                .setContentTitle(getString(R.string.notification_warning_title))
                .setContentText(String.format(getString(R.string.notification_warning_info), percent))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager.notify(NOTIFICATION_USAGE_WARNING_ID, builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
