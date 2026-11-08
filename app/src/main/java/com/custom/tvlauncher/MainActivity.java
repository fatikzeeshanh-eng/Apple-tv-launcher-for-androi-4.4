package com.custom.tvlauncher;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private LinearLayout dockLayout, standbyContainer;
    private ImageView bgImageView;
    private TextView txtClock, txtStandbyClock;
    private PackageManager packageManager;

    private int currentWallpaperIndex = 0;
    private int[] wallpapers = {
        android.R.drawable.screen_background_dark,
        android.R.drawable.star_on,
        android.R.drawable.gallery_thumb
    };

    private boolean isStandbyMode = false;
    private Handler clockHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        packageManager = getPackageManager();

        dockLayout = (LinearLayout) findViewById(R.id.dockLayout);
        standbyContainer = (LinearLayout) findViewById(R.id.standbyContainer);
        bgImageView = (ImageView) findViewById(R.id.bgImageView);
        txtClock = (TextView) findViewById(R.id.txtClock);
        txtStandbyClock = (TextView) findViewById(R.id.txtStandbyClock);

        loadSavedWallpaper();
        populateDockApps();
        startClockTicker();

        findViewById(R.id.btnControlCenter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showControlCenterDialog();
            }
        });

        findViewById(R.id.btnSettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            }
        });
    }

    private void populateDockApps() {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> appList = packageManager.queryIntentActivities(intent, 0);

        for (final ResolveInfo info : appList) {
            View itemView = getLayoutInflater().inflate(R.layout.item_tvos_app, dockLayout, false);

            ImageView icon = (ImageView) itemView.findViewById(R.id.dockAppIcon);
            TextView name = (TextView) itemView.findViewById(R.id.dockAppName);

            icon.setImageDrawable(info.loadIcon(packageManager));
            name.setText(info.loadLabel(packageManager));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent launchIntent = packageManager.getLaunchIntentForPackage(info.activityInfo.packageName);
                    if (launchIntent != null) {
                        startActivity(launchIntent);
                    }
                }
            });

            dockLayout.addView(itemView);
        }
    }

    private void startClockTicker() {
        clockHandler.post(new Runnable() {
            @Override
            public void run() {
                String timeStr = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
                txtClock.setText(timeStr);
                txtStandbyClock.setText(timeStr);
                clockHandler.postDelayed(this, 1000);
            }
        });
    }

    private void showControlCenterDialog() {
        final Dialog dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.setContentView(R.layout.dialog_control_center);

        dialog.findViewById(R.id.ccBtnWallpaper).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateWallpaper();
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.ccBtnStandby).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleStandby();
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.ccBtnSystemSettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void rotateWallpaper() {
        currentWallpaperIndex = (currentWallpaperIndex + 1) % wallpapers.length;
        bgImageView.setImageResource(wallpapers[currentWallpaperIndex]);

        SharedPreferences prefs = getSharedPreferences("tvos_prefs", MODE_PRIVATE);
        prefs.edit().putInt("wallpaper_idx", currentWallpaperIndex).apply();
    }

    private void loadSavedWallpaper() {
        SharedPreferences prefs = getSharedPreferences("tvos_prefs", MODE_PRIVATE);
        currentWallpaperIndex = prefs.getInt("wallpaper_idx", 0);
        if(currentWallpaperIndex < wallpapers.length) {
            bgImageView.setImageResource(wallpapers[currentWallpaperIndex]);
        }
    }

    private void toggleStandby() {
        isStandbyMode = !isStandbyMode;
        if (isStandbyMode) {
            standbyContainer.setVisibility(View.VISIBLE);
            dockLayout.setVisibility(View.GONE);
        } else {
            standbyContainer.setVisibility(View.GONE);
            dockLayout.setVisibility(View.VISIBLE);
        }
    }
}
