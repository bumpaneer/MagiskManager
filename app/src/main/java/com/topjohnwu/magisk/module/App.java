package com.topjohnwu.magisk.module;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

public class App {

    public final String label;
    public final String packageName;
    public final Drawable icon;

    public App(ApplicationInfo info, PackageManager pm) {
        this.packageName = info.packageName;
        this.label = info.loadLabel(pm).toString();
        this.icon = info.loadIcon(pm);
    }
}
