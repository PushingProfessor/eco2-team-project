// ★★★★★★★★★★★★★★★★★★★★[wifi 사용량 + data 사용량 = TotalUsage]★★★★★★★★★★★★★★★★★

package com.jakubhekal.datausage.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakubhekal.datausage.DateTimeUtils;
import com.jakubhekal.datausage.R;
import com.jakubhekal.datausage.Utils;
import com.jakubhekal.datausage.managers.DataUsageManager;
import com.jakubhekal.datausage.managers.NetworkUsageManager;
import com.jakubhekal.datausage.managers.PreferenceManager;
import com.jakubhekal.datausage.managers.WifiUsageManager;
import com.jakubhekal.datausage.views.LineView;

public class AppDetailActivity extends AppCompatActivity {

    ImageView iconView;
    TextView nameView;
    TextView packageView;

    LineView totalTodayView;
    LineView receivedTodayView;
    LineView transmittedTodayView;

    LineView totalPeriodView;
    LineView receivedPeriodView;
    LineView transmittedPeriodView;

    LineView appInfoView;

    NetworkUsageManager networkUsageManager;
    WifiUsageManager wifiUsageManager;
    DataUsageManager dataUsageManager;
    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_detail);

        dataUsageManager = new DataUsageManager(this);
        preferenceManager = new PreferenceManager(this);
        PackageManager packageManager = getPackageManager();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_detail_title);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.icon_arrow_back);
        toolbar.setNavigationOnClickListener(v -> {
            setResult(RESULT_CANCELED,null);
            finish();
        });

        iconView = findViewById(R.id.app_detail_icon);
        nameView = findViewById(R.id.app_detail_name);
        packageView = findViewById(R.id.app_detail_package);

        totalTodayView = findViewById(R.id.data_today_total);
        receivedTodayView = findViewById(R.id.data_today_received);
        transmittedTodayView = findViewById(R.id.data_today_transmitted);

        totalPeriodView = findViewById(R.id.data_period_total);
        receivedPeriodView = findViewById(R.id.data_period_received);
        transmittedPeriodView = findViewById(R.id.data_period_transmitted);

        appInfoView = findViewById(R.id.app_detail_info);

        String packageName = getIntent().getStringExtra("PACKAGE");

        appInfoView.setOnClickListener(view -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:"+ packageName));
            startActivity(intent);
        });

        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);

            iconView.setImageDrawable(packageManager.getApplicationIcon(packageName));
            nameView.setText(packageManager.getApplicationLabel(applicationInfo));
            packageView.setText(packageName);

            /*
            // 모바일 데이터 사용량
            long mobileTotalToday = networkUsageManager.getPackageBytesMobile(applicationInfo.uid, DateTimeUtils.getDayStartMillis(), DateTimeUtils.getDayEndMillis());
            long mobileReceivedToday = networkUsageManager.getPackageBytesMobile(applicationInfo.uid, DateTimeUtils.getDayStartMillis(), DateTimeUtils.getDayEndMillis(), NetworkUsageManager.BYTES_RX);
            long mobileTransmittedToday = networkUsageManager.getPackageBytesMobile(applicationInfo.uid, DateTimeUtils.getDayStartMillis(), DateTimeUtils.getDayEndMillis(), NetworkUsageManager.BYTES_TX);

            long mobileTotalPeriod = networkUsageManager.getPackageBytesMobile(applicationInfo.uid, DateTimeUtils.getPeriodStartMillis(preferenceManager.getPeriodStart()), DateTimeUtils.getPeriodEndMillis(preferenceManager.getPeriodStart()));
            long mobileReceivedPeriod = networkUsageManager.getPackageBytesMobile(applicationInfo.uid, DateTimeUtils.getPeriodStartMillis(preferenceManager.getPeriodStart()), DateTimeUtils.getPeriodEndMillis(preferenceManager.getPeriodStart()), NetworkUsageManager.BYTES_RX);
            long mobileTransmittedPeriod = networkUsageManager.getPackageBytesMobile(applicationInfo.uid, DateTimeUtils.getPeriodStartMillis(preferenceManager.getPeriodStart()), DateTimeUtils.getPeriodEndMillis(preferenceManager.getPeriodStart()), NetworkUsageManager.BYTES_TX);
             */

            // 합산된 데이터 사용량을 뷰에 표시
            totalTodayView.setInfo(Utils.convertFromBytes(dataUsageManager.getPackageBytesDataUsage(applicationInfo.uid, DateTimeUtils.getDayStartMillis(),DateTimeUtils.getDayEndMillis())));
            receivedTodayView.setInfo(Utils.convertFromBytes(dataUsageManager.getPackageBytesDataUsage(applicationInfo.uid, DateTimeUtils.getDayStartMillis(), DateTimeUtils.getDayEndMillis(), DataUsageManager.BYTES_RX)));
            transmittedTodayView.setInfo(Utils.convertFromBytes(dataUsageManager.getPackageBytesDataUsage(applicationInfo.uid, DateTimeUtils.getDayStartMillis(), DateTimeUtils.getDayEndMillis(), DataUsageManager.BYTES_TX)));

            totalPeriodView.setInfo(Utils.convertFromBytes(dataUsageManager.getPackageBytesDataUsage(applicationInfo.uid, DateTimeUtils.getPeriodStartMillis(preferenceManager.getPeriodStart()),DateTimeUtils.getPeriodEndMillis(preferenceManager.getPeriodStart()))));
            receivedPeriodView.setInfo(Utils.convertFromBytes(dataUsageManager.getPackageBytesDataUsage(applicationInfo.uid, DateTimeUtils.getPeriodStartMillis(preferenceManager.getPeriodStart()),DateTimeUtils.getPeriodEndMillis(preferenceManager.getPeriodStart()),dataUsageManager.BYTES_RX)));
            transmittedPeriodView.setInfo(Utils.convertFromBytes(dataUsageManager.getPackageBytesDataUsage(applicationInfo.uid, DateTimeUtils.getPeriodStartMillis(preferenceManager.getPeriodStart()),DateTimeUtils.getPeriodEndMillis(preferenceManager.getPeriodStart()),dataUsageManager.BYTES_TX)));

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /*
    // 나눠진 모바일과 와이파이 사용량 합치기
    private long getTotalUsage(long mobileUsage, long wifiUsage) {
        return mobileUsage + wifiUsage;
    }

     */
}

