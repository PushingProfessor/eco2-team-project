package com.jakubhekal.datausage.managers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class WifiUsageManager {

    private final Context context;
    private final NetworkStatsManager networkStatsManager;

    public static final int BYTES_RX = 0;
    public static final int BYTES_TX = 1;
    public static final int BYTES_ALL = 2;

    public WifiUsageManager(Context context) {
        this.context = context;
        this.networkStatsManager = (NetworkStatsManager) this.context.getSystemService(Context.NETWORK_STATS_SERVICE);
    }

    private long getBytes(NetworkStats.Bucket bucket, int byteType) {
        switch (byteType) {
            case BYTES_RX:
                return bucket.getRxBytes();
            case BYTES_TX:
                return bucket.getTxBytes();
            case BYTES_ALL:
                return bucket.getRxBytes() + bucket.getTxBytes();
        }
        return 0;
    }

    public long getAllBytesWifi(long startTime, long endTime, int byteType) {
        try {
            NetworkStats.Bucket bucket;
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, null, startTime, endTime);
            return getBytes(bucket, byteType);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public long getAllBytesWifi(long startTime, long endTime) {
        return getAllBytesWifi(startTime, endTime, BYTES_ALL);
    }

    private String getWifiSSID() {
        // SSID : Wifi SSID 가져옴, 특정 Wifi 구분
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getSSID();
    }

    public long getPackageBytesWifi(int uid, long startTime, long endTime, int byteType) {
        try {
            NetworkStats networkStats;
            networkStats = networkStatsManager.queryDetailsForUid(ConnectivityManager.TYPE_WIFI, getWifiSSID(), startTime, endTime, uid);

            long bytes = 0;
            NetworkStats.Bucket bucket = new NetworkStats.Bucket();
            while (networkStats.hasNextBucket()) {
                networkStats.getNextBucket(bucket);
                bytes += getBytes(bucket, byteType);
            }
            networkStats.close();
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public long getPackageBytesWifi(int uid, long startTime, long endTime) {
        return getPackageBytesWifi(uid, startTime, endTime, BYTES_ALL);
    }
}