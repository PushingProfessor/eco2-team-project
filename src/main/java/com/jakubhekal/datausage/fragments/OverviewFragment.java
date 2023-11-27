/*
 계산한 일일, 기간별 탄소발자국 화면 상 표시
 메인화면
 */

package com.jakubhekal.datausage.fragments;

import android.content.Context;
import android.os.Bundle;

import android.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import android.text.style.AbsoluteSizeSpan;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.TextUtils;

import com.jakubhekal.datausage.DateTimeUtils;
import com.jakubhekal.datausage.R;
import com.jakubhekal.datausage.managers.CalculateManager;
import com.jakubhekal.datausage.managers.DataUsageManager;
import com.jakubhekal.datausage.managers.NetworkUsageManager;
import com.jakubhekal.datausage.managers.PreferenceManager;
import com.jakubhekal.datausage.managers.WifiUsageManager;
import com.jakubhekal.datausage.views.UsageBarView;


public class OverviewFragment extends Fragment {

    private Context context;
    private PreferenceManager preferenceManager;
    private DataUsageManager dataUsageManager;
    private TextView dataUsageTextView;
    private TextView totalDataUsageTextView;
    private TextView mostUsedAppTextView;


    UsageBarView dailyUsageBarView;
    UsageBarView periodUsageBarView;

    public OverviewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    // 레이아웃 설정
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_overview, container, false);
        context = getContext();
        dailyUsageBarView = root.findViewById(R.id.daily_usage_bar);
        periodUsageBarView = root.findViewById(R.id.period_usage_bar);

        //dataUsageTextView = root.findViewById(R.id.datausage);
        totalDataUsageTextView = root.findViewById(R.id.totalDataUsageTextView);

        mostUsedAppTextView = root.findViewById(R.id.most_used_app);

        // String topPackage = getTopPackage();
        //초기화
        preferenceManager = new PreferenceManager(context);
        dataUsageManager = new DataUsageManager(context);

        calculateOverview();

        updateMostUsedApp();

        return root;
    }

    //★★★기존 데이터 사용량으로 나타내는 코드 ★★★
    private void calculateOverview() {
        int daysTillEndOfPeriod = DateTimeUtils.getDaysTillPeriodEnd(preferenceManager);

        Long periodUsage = dataUsageManager.getAllBytesDataUsage(DateTimeUtils.getPeriodStartMillis(preferenceManager.getPeriodStart()), DateTimeUtils.getDayEndMillis());
        Long periodLimit = preferenceManager.getPeriodLimit();

        periodUsageBarView.setData(periodUsage, periodLimit);

        Long dailyUsage = dataUsageManager.getAllBytesDataUsage(DateTimeUtils.getDayStartMillis(), DateTimeUtils.getDayEndMillis());
        Long dailyLimit = preferenceManager.getDailyLimitCustom() ? preferenceManager.getDailyLimit() : (periodLimit - (periodUsage - dailyUsage)) / daysTillEndOfPeriod;

        dailyUsageBarView.setData(dailyUsage, dailyLimit);

        Long totalDataUsage = getTotalDataUsage();
        updateTotalDataUsage(totalDataUsage);
    }

    private long getTotalDataUsage() {
        return dataUsageManager.getAllBytesDataUsage(
                DateTimeUtils.getPeriodStartMillis(preferenceManager.getPeriodStart()),
                DateTimeUtils.getDayEndMillis()
        );
    }

    private void updateTotalDataUsage(long totalDataUsageBytes) {
        double totalDataUsageGB = convertBytesToGigabytes(totalDataUsageBytes);
        double result = totalDataUsageGB * 188;

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        String formattedValue = decimalFormat.format(result);

        // Create a SpannableString for the space with a smaller size
        SpannableString space = new SpannableString(" ");
        space.setSpan(new RelativeSizeSpan(0.4f), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Create a SpannableString for the formattedValue with the desired size
        SpannableString formattedValueSpannable = new SpannableString(formattedValue);
        formattedValueSpannable.setSpan(new RelativeSizeSpan(1.3f), 0, formattedValue.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Create a SpannableString for "gCO₂eq" with a smaller size
        SpannableString unit = new SpannableString(" gCO₂eq");
        unit.setSpan(new RelativeSizeSpan(0.4f), 0, unit.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Concatenate the SpannableStrings
        CharSequence resultWithDifferentSizes = TextUtils.concat(space, formattedValueSpannable, unit);

        // Update the TextView with the calculated result
        totalDataUsageTextView.setText(resultWithDifferentSizes);
    }

    private double convertBytesToGigabytes(long bytes) {
        // 바이트를 기가바이트로 변환 (1 GB = 1024 * 1024 * 1024 bytes)
        return bytes / (1024.0 * 1024.0 * 1024.0);
    }


    private void updateMostUsedApp() {
        // DataUsageManager를 통해 앱 목록과 가장 많은 데이터를 사용한 앱의 이름을 가져오기
        Map<String, Long> appUsageMap = dataUsageManager.getAllAppsDataUsage(
                DateTimeUtils.getPeriodStartMillis(preferenceManager.getPeriodStart()),
                DateTimeUtils.getDayEndMillis()
        );

        // 앱 목록이 비어있지 않으면 가장 많은 데이터를 사용한 앱의 이름을 가져오기
        String mostUsedAppName = getMostUsedAppName(appUsageMap);
            // 가져온 앱 이름을 mostUsedAppTextView에 설정
        if (mostUsedAppTextView != null) {
            mostUsedAppTextView.setText(mostUsedAppName != null ? mostUsedAppName : "No data available");
        }
    }

    // 추가: 앱 목록 중 가장 많은 데이터를 사용한 앱의 이름을 가져오는 메서드
    private String getMostUsedAppName(Map<String, Long> appUsageMap) {
        return appUsageMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey() + "(으)로 인한 배출량이 가장 많아요!")
                .orElse(null);
    }
}
