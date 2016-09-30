package com.topjohnwu.magisk;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.topjohnwu.magisk.module.App;
import com.topjohnwu.magisk.utils.ApplicationAdapter;
import com.topjohnwu.magisk.utils.Logger;
import com.topjohnwu.magisk.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AutoRootFragment extends Fragment implements Utils.ItemClickListener {

    public FastScrollRecyclerView rcView;
    public SharedPreferences prefs;

    private PackageManager packageManager = null;
    private List<App> appList = null;
    private ApplicationAdapter listAdapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.auto_root_fragment, container, false);

        appList = new ArrayList<>();
        listAdapter = new ApplicationAdapter(getActivity(), appList, this);

        rcView = (FastScrollRecyclerView) v.findViewById(R.id.recyclerView);
        rcView.setLayoutManager(new LinearLayoutManager(getActivity()));
        rcView.setHasFixedSize(true);
        rcView.setAdapter(listAdapter);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        packageManager = getActivity().getPackageManager();

        int fastScrollColor = getActivity().getResources().getColor(R.color.accent);

        if (prefs.getString("theme", "").equals("Dark")) {
            fastScrollColor = getActivity().getResources().getColor(R.color.dh_accent);
        }

        rcView.setPopupBgColor(fastScrollColor);
        rcView.setThumbColor(fastScrollColor);
    }

    @Override
    public void onResume() {
        super.onResume();

        new LoadApplications().execute();
    }

    @Override
    public void onItemClick(View view, int position) {
        Logger.dev("Click");

        App app = appList.get(position);
        ToggleApp(app.packageName, position, view);
    }

    private void ToggleApp(String appToCheck, int position, View v) {
        Logger.dev("Magisk", "AutoRootFragment: ToggleApp called for " + appToCheck);
        Set<String> blackListSet = prefs.getStringSet("auto_blacklist", null);
        assert blackListSet != null;
        List<String> arrayBlackList = new ArrayList<>(blackListSet);

        if (!arrayBlackList.contains(appToCheck)) {
            arrayBlackList.add(appToCheck);
        } else {
            for (int i = 0; i < arrayBlackList.size(); i++) {
                if (appToCheck.equals(arrayBlackList.get(i))) {
                    arrayBlackList.remove(i);
                }
            }
        }

        Logger.dev("Committing set, value is: " + arrayBlackList.toString());

        prefs.edit().putStringSet("auto_blacklist", new HashSet<>(arrayBlackList)).apply();

        listAdapter.UpdateRootStatusView(position, v);

    }

    private void checkForLaunchIntent(List<ApplicationInfo> list) {
        appList.clear();
        for (ApplicationInfo info : list) {
            try {
                if (null != packageManager.getLaunchIntentForPackage(info.packageName)) {
                    if (!info.packageName.equals("com.topjohnwu.magisk")) {
                        appList.add(new App(info, packageManager));
                    }
                }
            } catch (Exception e) {
                Logger.dev("AutoRootFragment ->" + e.getMessage());
            }
        }
        Collections.sort(appList, new CustomComparator());
    }

    public class CustomComparator implements Comparator<App> {
        @Override
        public int compare(App a1, App a2) {
            return a1.label.compareToIgnoreCase(a2.label);
        }
    }

    private class LoadApplications extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progress = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progress = ProgressDialog.show(getActivity(), null, "Loading application info...");
        }


        @Override
        protected Void doInBackground(Void... params) {
            checkForLaunchIntent(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            listAdapter.notifyDataSetChanged();
            progress.dismiss();
        }

    }
}