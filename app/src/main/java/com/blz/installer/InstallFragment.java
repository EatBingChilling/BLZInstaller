package com.blz.installer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class InstallFragment extends Fragment {
    private static final String TAG = "InstallFragment";
    private static final String APK_FILENAME = "app.apk";
    private static final String TARGET_PACKAGE = "dev.blaze.launcher";

    private TextView installStatusText;
    private View installStatusIndicator;
    private Button installButton;
    private Button refreshStatusButton;
    private Button downloadButton;

    private final Executor backgroundExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // 使用新的权限请求API
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startInstallProcess();
                } else {
                    Toast.makeText(getContext(), "需要存储权限才能安装APK", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_install, container, false);

        // 初始化视图
        installStatusText = view.findViewById(R.id.install_status_text);
        installStatusIndicator = view.findViewById(R.id.install_status_indicator);
        installButton = view.findViewById(R.id.installButton);
        downloadButton = view.findViewById(R.id.dlmc);

        // 添加刷新状态按钮
        refreshStatusButton = view.findViewById(R.id.refreshStatusButton);
        if (refreshStatusButton == null) {
            // 如果布局中没有此按钮，可以动态创建一个
            refreshStatusButton = new Button(getContext());
            refreshStatusButton.setText("刷新安装状态");
            refreshStatusButton.setId(View.generateViewId());
            ((ViewGroup) view).addView(refreshStatusButton);
        }

        refreshStatusButton.setOnClickListener(v -> {
            checkBlazeLauncherInstalled();
        });

        // 检测 Blaze Launcher 是否已安装
        checkBlazeLauncherInstalled();

        // 安装 APK 按钮
        installButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                    ContextCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Android 10 以下版本需要存储权限
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                // 已有权限或不需要权限（Android 10+），开始安装
                startInstallProcess();
            }
        });

        downloadButton.setOnClickListener(v -> {
            // 跳转到 APK 下载页面
            String url = "https://bbk.endyun.ltd/s/1-21-51-02v8";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 每次返回到界面时重新检查安装状态
        checkBlazeLauncherInstalled();
    }

    private void startInstallProcess() {
        // 禁用安装按钮防止重复点击
        installButton.setEnabled(false);

        // 在后台线程处理文件操作
        backgroundExecutor.execute(() -> {
            try {
                File apkFile = extractApkFromAssets();
                if (apkFile != null) {
                    mainHandler.post(() -> {
                        installApk(apkFile);
                    });
                } else {
                    mainHandler.post(() -> {
                        installButton.setEnabled(true);
                    });
                }
            } catch (Exception e) {
                mainHandler.post(() -> {
                    installButton.setEnabled(true);
                });
            }
        });
    }

    private File extractApkFromAssets() {
        Context context = getContext();
        if (context == null) return null;

        File outputFile = new File(context.getExternalFilesDir(null), APK_FILENAME);

        try (InputStream in = context.getAssets().open(APK_FILENAME);
             FileOutputStream out = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[8192]; // 更大的缓冲区提高性能
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
            return outputFile;

        } catch (IOException e) {
            return null;
        }
    }

    private void installApk(File apkFile) {
        try {
            Context context = getContext();
            if (context == null) return;

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            Uri apkUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Android 7.0 及以上版本，使用 FileProvider
                apkUri = FileProvider.getUriForFile(context,
                        context.getPackageName() + ".fileprovider", apkFile);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                // Android 7.0 以下版本
                apkUri = Uri.fromFile(apkFile);
            }

            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            context.startActivity(intent);

        } catch (Exception e) {
            installButton.setEnabled(true);
        }
    }

    private void checkBlazeLauncherInstalled() {
        backgroundExecutor.execute(() -> {
            boolean isInstalled = false;
            Context context = getContext();

            if (context != null) {
                try {
                    // 只检查 dev.blaze.launcher 包名
                    try {
                        context.getPackageManager().getPackageInfo(TARGET_PACKAGE, 0);
                        isInstalled = true;
                    } catch (PackageManager.NameNotFoundException e) {
                        isInstalled = false;
                    }

                } catch (Exception e) {
                }

                final boolean finalIsInstalled = isInstalled;
                mainHandler.post(() -> {
                    if (getContext() != null && installStatusText != null && installStatusIndicator != null) {
                        // 更新UI以反映安装状态
                        installStatusText.setText(finalIsInstalled ? "已安装" : "未安装");

                        // 设置圆形指示器背景
                        if (finalIsInstalled) {
                            installStatusIndicator.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.circle_indicator_installed));
                        } else {
                            installStatusIndicator.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.circle_indicator_not_installed));
                        }

                        // 根据安装状态更新按钮状态
                        installButton.setText(finalIsInstalled ? "已安装" : "安装");
                        installButton.setEnabled(!finalIsInstalled); // 如果已安装则禁用按钮
                    }
                });
            }
        });
    }
}