package com.blz.installer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;

import com.blz.installer.R;

public class AboutFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        // 第三个卡片：网站链接
        TextView phoen1xWebsiteTextView = view.findViewById(R.id.phoen1x_website);
        TextView minecraftGroupTextView = view.findViewById(R.id.minecraft_group);
        TextView bilibiliAccountTextView = view.findViewById(R.id.bilibili_account);

        // 设置点击事件，打开浏览器
        phoen1xWebsiteTextView.setOnClickListener(v -> openUrl("http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=vNrcjNbwAF6Kx35hR5VDKrODgHyaiO0I&authKey=iqwo3ZzfSwNTmnwF1lx73I5tSmQMoJDCqlsH1YBnzzIe3qODPhNO3nqbGk2SALlW&noverify=0&group_code=689672711"));
        minecraftGroupTextView.setOnClickListener(v -> openUrl("http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=b00_8Y4fRZgqBaSlCTI92ALKER12mb4Z&authKey=fw6N4dgHuy78xkrDvv4Fdh%2BBlyyWCikaAb0vX3gYZXgE7XfujW5dnrnLVZXTGf2f&noverify=0&group_code=2163019717"));
        bilibiliAccountTextView.setOnClickListener(v -> openUrl("https://space.bilibili.com/2129606465"));

        return view;
    }

    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
}
