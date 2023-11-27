//♥♥♥♥♥♥♥♥♥♥♥ wifi 사용량 + 모바일 데이터 사용량 = DataUsage ♥♥♥♥♥♥♥♥♥♥♥♥♥♥♥

package com.jakubhekal.datausage.managers;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.Manifest;
import android.content.pm.ApplicationInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DataUsageManager {

    private final Context context;
    private final WifiUsageManager wifiUsageManager;
    private final NetworkUsageManager networkUsageManager;

    public static final int BYTES_RX = 0;
    public static final int BYTES_TX = 1;
    public static final int BYTES_ALL = 2;

    // DataUsageManager 클래스의 생성자
    public DataUsageManager(Context context) {
        this.context = context;
        this.wifiUsageManager = new WifiUsageManager(context);
        this.networkUsageManager = new NetworkUsageManager(context);
    }

    // 주어진 기간 동안 WiFi와 모바일 데이터 사용량을 합친 총 데이터 사용량을 반환
    public long getAllBytesDataUsage(long startTime, long endTime, int byteType) {
        long wifiUsage = wifiUsageManager.getAllBytesWifi(startTime, endTime, byteType);
        long mobileUsage = networkUsageManager.getAllBytesMobile(startTime, endTime, byteType);
        return wifiUsage + mobileUsage;
    }

    // 주어진 기간 동안 WiFi와 모바일 데이터 사용량을 합친 총 데이터 사용량을 반환 (기본값 사용)
    public long getAllBytesDataUsage(long startTime, long endTime) {
        long wifiUsage = wifiUsageManager.getAllBytesWifi(startTime, endTime, BYTES_ALL);
        long mobileUsage = networkUsageManager.getAllBytesMobile(startTime, endTime, BYTES_ALL);
        return wifiUsage + mobileUsage;
    }

    // 주어진 UID에 대한 특정 기간 동안 WiFi와 모바일 데이터 사용량을 합친 총 데이터 사용량을 반환
    public long getPackageBytesDataUsage(int uid, long startTime, long endTime, int byteType) {
        long wifiUsage = wifiUsageManager.getPackageBytesWifi(uid, startTime, endTime, byteType);
        long mobileUsage = networkUsageManager.getPackageBytesMobile(uid, startTime, endTime, byteType);
        return wifiUsage + mobileUsage;
    }

    // 주어진 UID에 대한 특정 기간 동안 WiFi와 모바일 데이터 사용량을 합친 총 데이터 사용량을 반환 (기본값 사용)
    public long getPackageBytesDataUsage(int uid, long startTime, long endTime) {
        long wifiUsage = wifiUsageManager.getPackageBytesWifi(uid, startTime, endTime, BYTES_ALL);
        long mobileUsage = networkUsageManager.getPackageBytesMobile(uid, startTime, endTime, BYTES_ALL);
        return wifiUsage + mobileUsage;
    }

    public Map<String, Long> getAllAppsDataUsage(long startTime, long endTime) {
        Map<String, Long> appUsageMap = new HashMap<>();

        // PackageManager 가져오기 (앱 정보 불러오기)
        PackageManager packageManager = context.getPackageManager();
        // Package 저장할 리스트 생성
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(PackageManager.GET_META_DATA);

        //for 문: 설치된 패키지 목록 반복
        for (PackageInfo packageInfo : packageInfoList) {
            //if 문: INTERNET 권한 확인
            if (packageManager.checkPermission(Manifest.permission.INTERNET, packageInfo.packageName) == PackageManager.PERMISSION_DENIED) {
                continue;
            }

            // 현재 패키지의 데이터 사용량 저장 변수 초기화
            Long dataUsage = getPackageBytesDataUsage(packageInfo.applicationInfo.uid, startTime, endTime, BYTES_ALL);

            appUsageMap.put(packageInfo.packageName, dataUsage);
        }

        return appUsageMap;
    }
}