package com.jakubhekal.datausage.managers;

import android.content.Context;
import android.widget.TextView;

import java.text.DecimalFormat;

public class CalculateManager {

    private Context context;
    private DataUsageManager dataUsageManager;

    public CalculateManager(Context context) {
        this.context = context;
        this.dataUsageManager = new DataUsageManager(context);
    }

    /*
    convertBytesToGigabytes에서 바이트 단위의 값을 기가 바이트로 환산
    기가바이트 환산 값을 totalDataUsageGB 로 설정
    totalBytes * 188로 탄소발자국 계산
    계산값 소수점 아래 한 자리까지 나타내고 단위 설정
    (아래 코드 먼저 참고)
     */
    public double calculateTotalDataUsage(long dataUsageBytes, TextView cfootprintTextView) {
        double totalDataUsageGB = convertBytesToGigabytes(dataUsageBytes);
        double result = totalDataUsageGB * 188;

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        String formattedVale = decimalFormat.format(result);

        String resultWithUnit = formattedVale + " gCO₂eq";
        // 188을 곱한 값을 cfootprintTextView에 설정
        cfootprintTextView.setText(resultWithUnit);

        return totalDataUsageGB;
    }

    private double convertBytesToGigabytes(long bytes) {
        // 바이트를 기가바이트로 변환 (1 GB = 1024 * 1024 * 1024 bytes)
        return bytes / (1024.0 * 1024.0 * 1024.0);
    }

}