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

public class TwistedSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
        
        private static final String KEY_BITSYKO_LAYERS = "bitsyko_layers";
        private static final String KEY_EQUALIZER_SETTINGS = "equalizer_settings";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.twisted_settings);
        
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

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }
}