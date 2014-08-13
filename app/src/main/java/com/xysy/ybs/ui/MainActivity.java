package com.xysy.ybs.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.xysy.ybs.R;
import com.xysy.ybs.tools.Logger;
import com.xysy.ybs.ui.fragment.DrawerFragment;
import com.xysy.ybs.ui.fragment.JobsFragment;

public class MainActivity extends Activity
        implements DrawerFragment.OnCitySelectedListener{

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private String[] mCities;
    private String mCurrentCity;

    private CharSequence mTitle;
    private CharSequence mDrawerTitle;

    private boolean preferenceChanged = false;

    SharedPreferences.OnSharedPreferenceChangeListener mPreferenceListener;
    SharedPreferences mPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UmengUpdateAgent.update(this);

        mPreference = PreferenceManager.getDefaultSharedPreferences(this);
        mPreferenceListener = new PreferenceChangeListener();
        mPreference.registerOnSharedPreferenceChangeListener(mPreferenceListener);

        mCities = parseCitiesFromPreference(mPreference);

        mTitle = mDrawerTitle = getTitle();
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
        mDrawerLayout.setScrimColor(Color.argb(100, 0, 0, 0));
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer,
                R.string.app_name, R.string.app_name) {
            @Override
            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                getActionBar().setTitle(mTitle);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getFragmentManager().beginTransaction().
                replace(R.id.drawer, DrawerFragment.newInstance(mCities)).commit();

        setCity(mCities[0]);
    }

    @Override
    public void onResume() {
        super.onResume();
        //MobclickAgent.setDebugMode( true );
        MobclickAgent.onResume(this);
        if (preferenceChanged) {
            mCities = parseCitiesFromPreference(mPreference);
            getFragmentManager().beginTransaction().
                    replace(R.id.drawer, DrawerFragment.newInstance(mCities)).commit();
            setCity(mCities[0]);
            preferenceChanged = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mPreference.unregisterOnSharedPreferenceChangeListener(mPreferenceListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
         mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_favorites:
                startActivity(new Intent(this, FavoriteActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setCity(String city) {
        if (city.equals(mCurrentCity)) {
            return;
        }
        Fragment fragment = JobsFragment.newInstance(city);
        FragmentManager manager = getFragmentManager();
        manager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        mCurrentCity = city;
        setTitle(city + getResources().getString(R.string.app_name));
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    @Override
    public void onCitySelected(int position) {
        setCity(mCities[position]);
        mDrawerLayout.closeDrawer(Gravity.START);
    }

    private String[] parseCitiesFromPreference(SharedPreferences sp) {
        String raw = sp.getString("pref_key_cities", null);
        if (raw == null) {
            raw= getResources().getString(R.string.default_cities);
        }
        return raw.trim().split(" ");
    }

    private class PreferenceChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            preferenceChanged = true;
        }
    }
}
