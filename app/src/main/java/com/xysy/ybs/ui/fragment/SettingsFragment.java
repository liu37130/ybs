package com.xysy.ybs.ui.fragment;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import com.umeng.update.UmengUpdateAgent;
import com.xysy.ybs.R;
import com.xysy.ybs.data.DataProvider;
import com.xysy.ybs.tools.FileUtils;
import com.xysy.ybs.tools.Logger;
import com.xysy.ybs.tools.RequestCenter;

import java.io.File;

public class SettingsFragment extends PreferenceFragment{

    SharedPreferences pref;
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);

        pref = getPreferenceScreen().getSharedPreferences();
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                showCurrentPreference();
            }
        };
        showCurrentPreference();
    }

    @Override
    public void onResume() {
        super.onResume();
        pref.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onPause() {
        super.onPause();
        pref.unregisterOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == (Preference)findPreference("pref_key_clear_cache")) {
            clearCache();
            return true;
        } else if (preference == (Preference)findPreference("pref_key_version")){
            UmengUpdateAgent.update(getActivity());
            return true;
        } else {
            return true;
        }
    }

    private void showCurrentPreference() {
        EditTextPreference jobSetting = (EditTextPreference)findPreference("pref_key_job_keywords");
        jobSetting.setSummary(pref.getString("pref_key_job_keywords", "").trim());

        EditTextPreference citiesSetting = (EditTextPreference)findPreference("pref_key_cities");
        citiesSetting.setSummary(pref.getString("pref_key_cities", "").trim());

        Preference cacheClear = (Preference)findPreference("pref_key_clear_cache");
        cacheClear.setSummary(String.valueOf(getCacheSize()) + "M");

        Preference version = (Preference)findPreference("pref_key_version");
        version.setSummary(getVersion());
    }

    private long getCacheSize() {
        File cache = getActivity().getCacheDir();
        return FileUtils.getFolderSize(cache) / 1048576;
    }

    private String getVersion() {
        PackageInfo packageInfo;
        try {
            packageInfo = getActivity().getPackageManager().getPackageInfo(
                    getActivity().getPackageName(), PackageManager.GET_CONFIGURATIONS);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void clearCache() {
        new ClearCacheTask().execute();
    }

    private class ClearCacheTask extends AsyncTask<Void, Void, Void> {
        //private ProgressDialog dialog = new ProgressDialog(getActivity());

        @Override
        protected void onPreExecute() {
            //dialog.setMessage(getResources().getString(R.string.clearing_cache));
            //dialog.show();
        }
        @Override
        protected Void doInBackground(Void... args) {
            FileUtils.deleteFolderFile(getActivity().getCacheDir().getAbsolutePath(), false);
            RequestCenter.getCenter().clearCache();
            getActivity().getContentResolver().delete(DataProvider.JOBS_URI, null, null);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            showCurrentPreference();
            //dialog.dismiss();
            Toast.makeText(getActivity(), getResources().getString(R.string.cleared_cache),
                    Toast.LENGTH_SHORT).show();

        }
    }


}
