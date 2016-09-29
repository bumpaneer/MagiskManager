package com.topjohnwu.magisk.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.topjohnwu.magisk.R;

import java.util.List;
import java.util.Set;

public class ApplicationAdapter extends ArrayAdapter<ApplicationInfo> {
    public SharedPreferences prefs;
    private List<ApplicationInfo> appsList = null;
    private Context context;
    private PackageManager packageManager;

    public ApplicationAdapter(Context context, int textViewResourceId, List<ApplicationInfo> appsList) {
        super(context, textViewResourceId, appsList);
        this.context = context;
        this.appsList = appsList;
        packageManager = context.getPackageManager();
    }

    @Override
    public int getCount() {
        return ((null != appsList) ? appsList.size() : 0);
    }

    @Override
    public ApplicationInfo getItem(int position) {
        return ((null != appsList) ? appsList.get(position) : null);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        VH holder;
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_app, null);

            holder = new VH();
            holder.appName = (TextView) view.findViewById(R.id.app_name);
            holder.packageName = (TextView) view.findViewById(R.id.app_paackage);
            holder.iconView = (ImageView) view.findViewById(R.id.app_icon);
            holder.statusView = (CheckBox) view.findViewById(R.id.checkbox);

            view.setTag(holder);
        } else {
            holder = (VH) view.getTag();
        }

        ApplicationInfo applicationInfo = appsList.get(position);
        if (applicationInfo == null) return view;

        holder.appName.setText(applicationInfo.loadLabel(packageManager));
        holder.packageName.setText(applicationInfo.packageName);
        holder.iconView.setImageDrawable(applicationInfo.loadIcon(packageManager));
        holder.statusView.setChecked(CheckApp(applicationInfo.packageName));

        return view;
    }

    public void UpdateRootStatusView(int position, View convertView) {
        View view = convertView;
        if (null == view) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.list_item_app, null);
        }
        ApplicationInfo applicationInfo = appsList.get(position);
        if (null != applicationInfo) {
            CheckBox statusview = (CheckBox) view.findViewById(R.id.checkbox);
            if (CheckApp(applicationInfo.packageName)) {
                statusview.setChecked(true);
            } else {
                statusview.setChecked(false);
            }
        }

    }

    private boolean CheckApp(String appToCheck) {
        Set<String> set = prefs.getStringSet("auto_blacklist", null);
        if (set != null) {
            for (String string : set) {
                if (string.equals(appToCheck)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static class VH {
        TextView appName, packageName;
        ImageView iconView;
        CheckBox statusView;
    }

}