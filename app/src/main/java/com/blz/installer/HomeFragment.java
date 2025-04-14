package com.blz.installer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.blz.installer.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class HomeFragment extends Fragment {
    
    private static final String TAG = "HomeFragment";
    private static final String MINECRAFT_PACKAGE = "com.mojang.minecraftpe";
    private static final String BLAZE_LAUNCHER_PACKAGE = "dev.blaze.launcher";
    private static final String BLAZE_LAUNCHER_ACTIVITY = "dev.blaze.launcher.GameActivity";
    
    // 权限请求码
    private static final int REQUEST_PACKAGE_USAGE_STATS = 123;
    
    // Activity Result API 权限请求
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 初始化权限请求
        requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                boolean allGranted = true;
                for (Boolean granted : permissions.values()) {
                    if (!granted) {
                        allGranted = false;
                        break;
                    }
                }
                
                if (allGranted) {
                    Log.d(TAG, "All permissions granted");
                    updateUI();
                } else {
                    Log.d(TAG, "Some permissions denied");
                    showPermissionExplanationDialog();
                }
            }
        );
      }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 加载布局
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 检查并请求必要权限
        checkAndRequestPermissions();
        
        // 启动按钮
        Button startBLZButton = view.findViewById(R.id.startBLZ);
        startBLZButton.setOnClickListener(v -> {
            if (hasRequiredPermissions()) {
                launchBlazeApp();
            } else {
                checkAndRequestPermissions();
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // 每次恢复时重新检查权限和状态
        updateUI();
    }
    
    /**
     * 检查并请求所需权限
     */
    private void checkAndRequestPermissions() {
        if (!hasRequiredPermissions()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+: 需要特殊处理 QUERY_ALL_PACKAGES
                openAppInfoSettings();
            } else {
                // Android 10及以下：请求常规权限
                requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.QUERY_ALL_PACKAGES
                });
            }
        } else {
            updateUI();
        }
    }
    
    /**
     * 检查是否有所需权限
     */
    private boolean hasRequiredPermissions() {
        Context context = getContext();
        if (context == null) return false;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+: 检查QUERY_ALL_PACKAGES权限
            return context.getPackageManager().canRequestPackageInstalls() || 
                   isPackageQueryAllowed();
        } else {
            // Android 10及以下
            return true; // 旧版本Android不严格限制查询应用
        }
    }
    
    /**
     * 检查是否允许查询所有包 (Android 11+)
     */
    private boolean isPackageQueryAllowed() {
        try {
            return isAppInstalled(requireContext(), MINECRAFT_PACKAGE);
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception when querying packages", e);
            return false;
        }
    }
    
    /**
     * 打开应用信息设置页面请求权限
     */
    private void openAppInfoSettings() {
        Context context = getContext();
        if (context == null) return;
        
        new MaterialAlertDialogBuilder(context)
            .setTitle("需要权限")
            .setMessage("为了检测已安装的应用，请在设置中允许查询所有应用权限")
            .setPositiveButton("去设置", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, REQUEST_PACKAGE_USAGE_STATS);
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    /**
     * 显示权限解释对话框
     */
    private void showPermissionExplanationDialog() {
        Context context = getContext();
        if (context == null) return;
        
        new MaterialAlertDialogBuilder(context)
            .setTitle("权限被拒绝")
            .setMessage("没有必要的权限，应用无法检测 Minecraft 是否已安装。请在设置中手动授予权限。")
            .setPositiveButton("去设置", (dialog, which) -> openAppInfoSettings())
            .setNegativeButton("取消", null)
            .show();
    }
    
    /**
     * 更新UI显示
     */
    private void updateUI() {
        View view = getView();
        Context context = getContext();
        if (view == null || context == null) return;
        
        try {
            updateMinecraftStatus(view, context);
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception when updating UI", e);
            checkAndRequestPermissions();
        }
    }
    
    /**
     * 更新Minecraft状态显示
     */
    private void updateMinecraftStatus(View view, Context context) {
        boolean isMinecraftInstalled = false;
        String versionName = "";
        
        try {
            isMinecraftInstalled = isAppInstalled(context, MINECRAFT_PACKAGE);
            if (isMinecraftInstalled) {
                versionName = getAppVersionName(context, MINECRAFT_PACKAGE);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception when checking Minecraft", e);
            checkAndRequestPermissions();
            return;
        }
        
        String minecraftStatus = isMinecraftInstalled ? "已安装" : "未安装";
        
        // 显示 Minecraft PE 的状态
        TextView minecraftTitle = view.findViewById(R.id.minecraftTitle);
        TextView minecraftSubTitle = view.findViewById(R.id.minecraftSubTitle);
        View installStatusIndicator = view.findViewById(R.id.install_status_indicator);
        
        // 设置标题和副标题
        minecraftTitle.setText("Minecraft PE");
        minecraftSubTitle.setText(minecraftStatus + (isMinecraftInstalled ? ("，版本：" + versionName) : ""));
        
        // 设置圆形指示器的颜色
        int indicatorDrawable = isMinecraftInstalled ? 
                R.drawable.circle_indicator_installed : 
                R.drawable.circle_indicator_not_installed;
        
        installStatusIndicator.setBackground(ContextCompat.getDrawable(context, indicatorDrawable));
        
        // 记录检测结果
        Log.d(TAG, "Minecraft installation status: " + minecraftStatus + 
                (isMinecraftInstalled ? ", version: " + versionName : ""));
    }
    
    /**
     * 启动BLZ应用
     */
    private void launchBlazeApp() {
        Context context = getContext();
        if (context == null) return;
        
        try {
            if (!isAppInstalled(context, BLAZE_LAUNCHER_PACKAGE)) {
                Toast.makeText(context, "Blaze Launcher 未安装", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Attempted to launch Blaze Launcher but it's not installed");
                return;
            }
            
            Intent intent = new Intent();
            intent.setClassName(BLAZE_LAUNCHER_PACKAGE, BLAZE_LAUNCHER_ACTIVITY);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            Log.d(TAG, "Successfully launched Blaze Launcher");
        } catch (Exception e) {
            Toast.makeText(context, "无法启动 Blaze Launcher: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to launch Blaze Launcher", e);
        }
    }
    
    /**
     * 检测应用是否安装
     */
    private boolean isAppInstalled(Context context, String packageName) {
        if (context == null || packageName == null || packageName.isEmpty()) {
            Log.e(TAG, "Invalid context or package name for app installation check");
            return false;
        }
        
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "App not installed: " + packageName);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if app is installed: " + packageName, e);
            return false;
        }
    }
    
    /**
     * 获取应用版本名称
     */
    private String getAppVersionName(Context context, String packageName) {
        if (context == null || packageName == null || packageName.isEmpty()) {
            return "";
        }
        
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getPackageInfo(packageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "Cannot get version for app (not found): " + packageName);
            return "";
        } catch (Exception e) {
            Log.e(TAG, "Error getting app version: " + packageName, e);
            return "";
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PACKAGE_USAGE_STATS) {
            // 从应用设置返回后，尝试更新UI
            updateUI();
        }
    }
}
