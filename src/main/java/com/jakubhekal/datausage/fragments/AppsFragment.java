// ★★★★★★★★★★★★★★★★★★★★ Apps 목록에서 앱별 탄소발자국 계산 코드★★★★★★★★★★★★★★★★★
// 앱 아이콘, 앱 이름, 사용량 비율, 계산한 탄소발자국 포함
// 수정 작업 :
// 앱 데이터 사용량 → 비율 로 변경
// 계산한 탄소발자국을 Textview _ cfootprint에 나타냄
package com.jakubhekal.datausage.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.jakubhekal.datausage.DateTimeUtils;
import com.jakubhekal.datausage.R;
import com.jakubhekal.datausage.Utils;
import com.jakubhekal.datausage.activities.AppDetailActivity;
import com.jakubhekal.datausage.managers.DataUsageManager;
import com.jakubhekal.datausage.managers.NetworkUsageManager;
import com.jakubhekal.datausage.managers.WifiUsageManager;
import com.jakubhekal.datausage.managers.PreferenceManager;
import com.jakubhekal.datausage.managers.CalculateManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AppsFragment extends Fragment {

    Context context;

    TabLayout tabLayout;
    RecyclerView recyclerView;
    View loadingView;
    View emptyView;

    //Manager 불러오기
    PreferenceManager preferenceManager;
    NetworkUsageManager networkUsageManager;
    WifiUsageManager wifiUsageManager;

    DataUsageManager dataUsageManager;
    CalculateManager calculateManager;

    public AppsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_apps, container, false);
        context = getContext();
        tabLayout = root.findViewById(R.id.tabs);
        recyclerView = root.findViewById(R.id.recycler_view);

        loadingView = root.findViewById(R.id.loading);
        emptyView = root.findViewById(R.id.empty);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        preferenceManager = new PreferenceManager(context);
        dataUsageManager = new DataUsageManager(context);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                initData();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        initData();

        return root;
    }

    private void initData() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        loadingView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        executor.execute(() -> {
            List<Package> packageList = getPackages();
            handler.post(() -> {
                recyclerView.setAdapter(new PackageAdapter(packageList));
                loadingView.setVisibility(View.GONE);
                if(packageList.size() <= 0) {
                    emptyView.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    /*
    PackageManager : 기기에 설치된 어플리케이션 패키지 관련 정보 검색
    PackageInfo :
    TabLayout :
    DataTimeUtils :
    'packageInfoList'를 반복하여 각 패키지에 대한 정보 수집
    각 패키지의 INTERNET 권한 확인 후 없으면 다음 패키지
    선택된 탭에 따라 현재 패키지의 데이터 사용량 검색
    데이터 사용량이 0보다 클 경우 'package' 객체를 만들고 리스트에 추가
    모든 패키지 확인 후 데이터 사용량을 기준으로 내림차순 정렬
    최종적으로 정렬된 패키치 목록 반환
     */
    private List<Package> getPackages() {
        // PackageManager 가져오기 (앱 정보 불러오기)
        PackageManager packageManager = context.getPackageManager();
        // Package 저장할 리스트 생성
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(PackageManager.GET_META_DATA);
        List<Package> packageList = new ArrayList<>(packageInfoList.size());

        long totalDataUsage;
        if (tabLayout.getSelectedTabPosition() == 0) {
            totalDataUsage = dataUsageManager.getAllBytesDataUsage(
                    DateTimeUtils.getDayStartMillis(),
                    DateTimeUtils.getDayEndMillis()
            );
        } else {
            totalDataUsage = dataUsageManager.getAllBytesDataUsage(
                    DateTimeUtils.getPeriodStartMillis(preferenceManager.getPeriodStart()),
                    DateTimeUtils.getPeriodEndMillis(preferenceManager.getPeriodStart())
            );
        }

        //for 문: 설치된 패키지 목록 반복
        for (PackageInfo packageInfo : packageInfoList) {
            //if 문: INTERNET 권한 확인
            if (packageManager.checkPermission(Manifest.permission.INTERNET, packageInfo.packageName) == PackageManager.PERMISSION_DENIED) {
                continue;
            }

            // 현재 패키지의 데이터 사용량 저장 변수 초기화
            Long dataUsage;
            if (tabLayout.getSelectedTabPosition() == 0) {
                dataUsage = dataUsageManager.getPackageBytesDataUsage(packageInfo.applicationInfo.uid,
                        DateTimeUtils.getDayStartMillis(),
                        DateTimeUtils.getDayEndMillis());
            } else {
                dataUsage = dataUsageManager.getPackageBytesDataUsage(packageInfo.applicationInfo.uid,
                        DateTimeUtils.getPeriodStartMillis(preferenceManager.getPeriodStart()),
                        DateTimeUtils.getPeriodEndMillis(preferenceManager.getPeriodStart()));
            }

            // 비율이 0.1% 미만인 경우 패스
            double usageRatio = (totalDataUsage > 0) ? (dataUsage.doubleValue() / totalDataUsage) * 100.0 : 0.0;
            if (usageRatio < 0.1) {
                continue;
            }


            Package packageItem = new Package();
            packageItem.setPackageName(packageInfo.packageName);
            packageItem.setDataUsage(dataUsage);
            packageItem.setUsageRatio(usageRatio);

            packageList.add(packageItem);
            ApplicationInfo ai = null;
            try {
                ai = packageManager.getApplicationInfo(packageInfo.packageName, PackageManager.GET_META_DATA);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (ai == null) {
                continue;
            }
            CharSequence appName = packageManager.getApplicationLabel(ai);
            if (appName != null) {
                packageItem.setName(appName.toString());
            }
        }

        // 데이터 사용량을 내림차순으로 정렬
        // Collections.sort(packageList, (p1, p2) -> (int) ((p2.getDataUsage() - p1.getDataUsage()) / 10));
        Collections.sort(packageList, (p1, p2) -> Long.compare(p2.getDataUsage(), p1.getDataUsage()));

        return packageList;
    }

    // Package 클래스 : 앱 정보 저장 클래스
    public class Package {
        private String name;
        private String packageName;
        private Long dataUsage;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public Long getDataUsage() {
            return dataUsage;
        }

        public void setDataUsage(Long dataUsage) {
            this.dataUsage = dataUsage;
        }

        public double usageRatio;
        public double getUsageRatio() {return usageRatio;}
        public void setUsageRatio(double usageRatio) {this.usageRatio = usageRatio;}


    }

    //PackageAdapter 클래스: RecyclerView에 데이터 제공
    public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.PackageViewHolder> {
        List<Package> mPackageList;

        public PackageAdapter(List<Package> packageList) {
            mPackageList = packageList;
        }

        @NonNull
        @Override
        public PackageAdapter.PackageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
            return new PackageAdapter.PackageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PackageAdapter.PackageViewHolder holder, int position) {
            Package packageItem = mPackageList.get(position);
            holder.name.setText(packageItem.getName());
            //holder.dataUsage.setText(Utils.convertFromBytes(packageItem.getDataUsage()));
            holder.dataUsage.setText(String.format(Locale.getDefault(), "%.1f%%", packageItem.getUsageRatio()));
            try {
                // 앱 아이콘 설정
                holder.icon.setImageDrawable(holder.context.getPackageManager().getApplicationIcon(packageItem.getPackageName()));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            // 항목 클릭 시 앱 세부 정보 화면으로 이동
            holder.itemView.setOnClickListener(view -> {
                Intent i = new Intent(holder.context, AppDetailActivity.class);
                i.putExtra("PACKAGE", packageItem.getPackageName());
                holder.context.startActivity(i);
            });

            // CalculateManager에서 계산한 탄소발자국 값을 cfootprint에 불러오기
            CalculateManager calculateManager = new CalculateManager(holder.context);
            long dataUsageBytes = packageItem.getDataUsage();  // 데이터 사용량 값을 가져옴
            calculateManager.calculateTotalDataUsage(dataUsageBytes, holder.cfootprintTextView);
        }

        @Override
        public int getItemCount() {
            return mPackageList.size();
        }

        public class PackageViewHolder extends RecyclerView.ViewHolder {
            Context context;
            TextView name;                  // 앱 이름
            TextView dataUsage;          // 데이터 사용량 -> 비율로 변경 예정
            ImageView icon;                 // 앱 아이콘 표시
            TextView cfootprintTextView;    // 탄소발자국 표시


            // 이름 지정
            public PackageViewHolder(View itemView) {
                super(itemView);
                context = itemView.getContext();
                name = itemView.findViewById(R.id.text_app_name);
                dataUsage = itemView.findViewById(R.id.text_data_usage);
                icon = itemView.findViewById(R.id.app_icon);
                cfootprintTextView = itemView.findViewById(R.id.text_cfootprint);
            }
        }
    }

    public String getTopPackage() {
        List<Package> packageList = getPackages();

        // 맨 위의 앱 이름 가져오기
        if (!packageList.isEmpty()) {
            return packageList.get(0).getName();
        }

        // 만약 리스트가 비어 있다면 null 또는 원하는 기본값을 반환할 수 있습니다.
        return null;
    }
}

