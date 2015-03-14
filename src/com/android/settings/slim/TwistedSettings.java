/*
 * Copyright (C) 2013 SlimRoms
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.slim;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SwitchPreference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.util.List;

import android.app.IActivityManager;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.ServiceManager;
import android.util.DisplayMetrics;
import android.preference.ListPreference;
import android.os.SystemProperties;
import android.preference.Preference.OnPreferenceClickListener;
import com.android.settings.search.Indexable;
import android.provider.SearchIndexableResource;
import android.util.Log;
import android.app.ActivityManagerNative;
import android.os.RemoteException;

public class TwistedSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
        
        private static final String KEY_BITSYKO_LAYERS = "bitsyko_layers";
        private static final String KEY_EQUALIZER_SETTINGS = "equalizer_settings";
        private static final String KEY_LCD_DENSITY = "lcd_density";

    private static final String TAG = "DisplaySettings";
    private ListPreference mLcdDensityPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.twisted_settings);

        mLcdDensityPreference = (ListPreference) findPreference(KEY_LCD_DENSITY);
        int defaultDensity = DisplayMetrics.DENSITY_DEVICE;
        int currentDensity = DisplayMetrics.DENSITY_CURRENT;
        int currentIndex = -1;
        String[] densityEntries = new String[8];
        for (int idx = 0; idx < 8; ++idx) {
            int pct = (75 + idx*5);
            int val = defaultDensity * pct / 100;
            densityEntries[idx] = Integer.toString(val);
            if (pct == 100) {
                densityEntries[idx] += " ("
                        + getResources().getString(R.string.lcd_density_default) + ")";
            }
            if (currentDensity == val) {
                currentIndex = idx;
            }
        }
        mLcdDensityPreference.setEntries(densityEntries);
        mLcdDensityPreference.setEntryValues(densityEntries);
        if (currentIndex != -1) {
            mLcdDensityPreference.setValueIndex(currentIndex);
        }
        mLcdDensityPreference.setOnPreferenceChangeListener(this);
        updateLcdDensityPreferenceDescription(currentDensity);
        
	    if (!isPackageInstalled("org.bitsyko.overlaymanager")) {
                PreferenceScreen screen = getPreferenceScreen();
                Preference pref = getPreferenceManager().findPreference(KEY_BITSYKO_LAYERS);
                screen.removePreference(pref);
            }
            if (!isPackageInstalled("com.vipercn.viper4android_v2")) {
	      PreferenceScreen screen = getPreferenceScreen();
	      Preference pref = getPreferenceManager().findPreference(KEY_EQUALIZER_SETTINGS);
	      screen.removePreference(pref);
	    }
    }

    private void updateLcdDensityPreferenceDescription(int currentDensity) {
        int defaultDensity = DisplayMetrics.DENSITY_DEVICE;
        ListPreference preference = mLcdDensityPreference;
        String summary;
        if (currentDensity < 10 || currentDensity >= 1000) {
            // Unsupported value
            summary = getResources().getString(R.string.lcd_density_unsupported);
        }
        else {
            summary = String.format(getResources().getString(R.string.lcd_density_summary),
                    currentDensity);
            if (currentDensity == defaultDensity) {
                summary += " (" + getResources().getString(R.string.lcd_density_default) + ")";
            }
        }
        preference.setSummary(summary);
    }

        public void writeLcdDensityPreference(final Context context, int value) {
        try {
            SystemProperties.set("persist.sys.lcd_density", Integer.toString(value));
        }
        catch (Exception e) {
            Log.e(TAG, "Unable to save LCD density");
            return;
        }
        final IActivityManager am =
                ActivityManagerNative.asInterface(ServiceManager.checkService("activity"));
        if (am != null) {
            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                @Override
                protected void onPreExecute() {
                    ProgressDialog dialog = new ProgressDialog(context);
                    dialog.setMessage(getResources().getString(R.string.restarting_ui));
                    dialog.setCancelable(false);
                    dialog.setIndeterminate(true);
                    dialog.show();
                }
                @Override
                protected Void doInBackground(Void... arg0) {
                    // Give the user a second to see the dialog
                    try {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e) {
                        // Ignore
                    }
                    // Restart the UI
                    try {
                        am.restart();
                    }
                    catch (RemoteException e) {
                        Log.e(TAG, "Failed to restart");
                    }
                    return null;
                }
            };
            task.execute((Void[])null);
        }
    }

    private boolean isPackageInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        boolean installed = false;
        try {
           pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
           installed = true;
        } catch (PackageManager.NameNotFoundException e) {
           installed = false;
        }
        return installed;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

  @Override
   public boolean onPreferenceChange(Preference preference, Object objValue) {
       final String key = preference.getKey();
       if (KEY_LCD_DENSITY.equals(key)) {
           try {
               // The value must begin with a decimal number.  It may
               // optionally be follewed by a space and arbitrary text.
               String strValue = (String) objValue;
               int idx = strValue.indexOf(' ');
               if (idx > 0) {
                   strValue = strValue.substring(0, idx);
               }
               int value = Integer.parseInt(strValue);
               writeLcdDensityPreference(preference.getContext(), value);
               updateLcdDensityPreferenceDescription(value);
           }
           catch (NumberFormatException e) {
               Log.e(TAG, "could not persist display density setting", e);
           }
            return true;
      }
      return false;
   }
}