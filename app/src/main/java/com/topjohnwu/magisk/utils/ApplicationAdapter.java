package com.topjohnwu.magisk.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.topjohnwu.magisk.R;
import com.topjohnwu.magisk.module.App;

import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.VH> implements FastScrollRecyclerView.SectionedAdapter {

    private final Utils.ItemClickListener clickListener;
    private SharedPreferences prefs;
    private List<App> appsList = null;
    private Context context;

    public ApplicationAdapter(Activity context, List<App> appsList, Utils.ItemClickListener clickListener) {
        this.context = context;
        this.appsList = appsList;
        this.clickListener = clickListener;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_app, parent, false);

        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        App app = appsList.get(position);
        if (app == null) return;

        holder.itemView.setOnClickListener(v -> clickListener.onItemClick(v, holder.getAdapterPosition()));

        holder.appName.setText(app.label);
        holder.packageName.setText(app.packageName);
        holder.iconView.setImageDrawable(app.icon);
        holder.statusView.setChecked(CheckApp(app.packageName));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return appsList.size();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return String.valueOf(appsList.get(position).label.charAt(0));
    }

    public void UpdateRootStatusView(int position, View convertView) {
        View view = convertView;
        if (null == view) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.list_item_app, null);
        }
        App app = appsList.get(position);
        if (null != app) {
            CheckBox statusview = (CheckBox) view.findViewById(R.id.checkbox);
            if (CheckApp(app.packageName)) {
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

    public static class VH extends RecyclerView.ViewHolder {
        @BindView(R.id.app_name) TextView appName;
        @BindView(R.id.app_paackage) TextView packageName;
        @BindView(R.id.app_icon) ImageView iconView;
        @BindView(R.id.checkbox) CheckBox statusView;

        public VH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}