package com.blz.installer;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.graphics.Color;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;
    private ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 启用边缘到边缘显示
        enableEdgeToEdge();
        
        setContentView(R.layout.activity_main);
        
        viewPager = findViewById(R.id.view_pager);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        
        // 设置ViewPager适配器
        adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);
        
        // 处理系统导航栏和状态栏的内边距
        setupWindowInsets();
        
        // 设置 BottomNavigationView 的点击事件
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                viewPager.setCurrentItem(0);
            } else if (itemId == R.id.navigation_download) {
                viewPager.setCurrentItem(1);
            } else if (itemId == R.id.navigation_help) {
                viewPager.setCurrentItem(2);
            } else if (itemId == R.id.navigation_about) {
                viewPager.setCurrentItem(3);
            }
            return true;
        });
        
        // 设置 ViewPager 页面切换监听，同步底部导航栏选中状态
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.navigation_download);
                        break;
                    case 2:
                        bottomNavigationView.setSelectedItemId(R.id.navigation_help);
                        break;
                    case 3:
                        bottomNavigationView.setSelectedItemId(R.id.navigation_about);
                        break;
                }
            }
        });
        
        // 设置默认选中的导航项
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
    }
    
    /**
     * 启用边缘到边缘显示
     */
    private void enableEdgeToEdge() {
        // 告诉系统我们将处理窗口插入
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        // 设置状态栏为透明
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        
        // 设置导航栏为透明或半透明（根据需求调整）
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        
        // 如果需要，可以设置状态栏和导航栏文字颜色（深色或浅色）
        View decorView = getWindow().getDecorView();
        WindowInsetsControllerCompat insetsController = 
                WindowCompat.getInsetsController(getWindow(), decorView);
        
        // 根据背景色选择状态栏图标颜色，true为深色图标，false为浅色图标
        // 根据您的UI设计，您可能需要调整这个
        insetsController.setAppearanceLightStatusBars(false);  
        insetsController.setAppearanceLightNavigationBars(false);
    }
    
    /**
     * 处理窗口插入，确保内容不被系统UI覆盖
     */
    private void setupWindowInsets() {
        // 为ViewPager和BottomNavigationView设置适当的内边距
        ViewCompat.setOnApplyWindowInsetsListener(viewPager, (v, insets) -> {
            // 获取状态栏高度
            int statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            
            // 为ViewPager设置顶部内边距，避免内容被状态栏遮挡
            v.setPadding(0, statusBarInsets, 0, 0);
            
            // 返回原始insets，允许它们在视图层次结构中继续分发
            return insets;
        });
        
        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigationView, (v, insets) -> {
            // 获取导航栏高度
            int navigationBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            
            // 为底部导航栏设置底部内边距，确保它不会被系统导航栏遮挡
            v.setPadding(0, 0, 0, navigationBarInsets);
            
            // 返回原始insets，允许它们在视图层次结构中继续分发
            return insets;
        });
    }
}