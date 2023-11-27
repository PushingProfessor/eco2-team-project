package com.jakubhekal.datausage.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jakubhekal.datausage.BottomNavigationPager;
import com.jakubhekal.datausage.NotifyService;
import com.jakubhekal.datausage.fragments.AppsFragment;
import com.jakubhekal.datausage.fragments.OverviewFragment;
import com.jakubhekal.datausage.R;
import com.jakubhekal.datausage.managers.PermissionManager;

public class MainActivity extends AppCompatActivity {
    // 앱 상단과 하단 view 를 담을 변수 선언
    AppBarLayout appBarLayout; // activity_main.xml 에서 top_app_bar
    BottomNavigationView bottomNavigationView; // activity_main.xml 에서 bottom_navigation_bar
    // BottomNavigationPager 객체를 변수 선언
    BottomNavigationPager bottomNavigationPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        super
        : 부모 클래스 지칭
        상위 클래스 함수에 우리가 현재 작업 중인 클래스 정보 전달
         */
        setContentView(R.layout.activity_main);

        // toolbar 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.main_app_bar_menu);

        appBarLayout = findViewById(R.id.top_app_bar);
        bottomNavigationView = findViewById(R.id.bottom_navigation_bar);

    }

    @Override
    protected void onResume() {
        super.onResume();

        bottomNavigationPager = new BottomNavigationPager(this, bottomNavigationView, R.id.content);
        bottomNavigationPager.bindFragment(R.id.menu_item_overview, new OverviewFragment());
        bottomNavigationPager.bindFragment(R.id.apps, new AppsFragment());
        bottomNavigationPager.enable();

        if (PermissionManager.hasPermissions(this)) {
            startService(new Intent(this, NotifyService.class));
        } else {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Missing permissions")
                    .setMessage("Give this app some permissions to work properly")
                    .setNegativeButton("Leave", ((dialog, which) -> finish()))
                    .setPositiveButton("OK", (dialog, which) -> {
                        dialog.dismiss();
                        PermissionManager.requestPermissions(this);
                    })
                    .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}