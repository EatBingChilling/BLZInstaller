package com.blz.installer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.blz.installer.R;

public class HelpFragment extends Fragment {

    @Override
       public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help, container, false);

        // 绑定点击事件
        Button videoTextView = view.findViewById(R.id.video1); // 确保布局中有这个ID
        if (videoTextView != null) {
            videoTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = "https://b23.tv/NdJyQQR"; // 去掉HTML标签
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                }
            });
        }

        Button vpnTextView = view.findViewById(R.id.vpn); // 确保布局中有这个ID
        if (vpnTextView != null) {
            vpnTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = "https://www.papansya.com/bsda/android/227110067/letsvpn-2.27.1.apk"; // 去掉HTML标签
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                }
            });
        }

        return view;
    }
}
