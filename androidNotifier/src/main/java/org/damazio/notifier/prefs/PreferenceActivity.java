/*
 * Copyright 2011 Rodrigo Damazio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.damazio.notifier.prefs;

import org.damazio.notifier.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class PreferenceActivity extends android.preference.PreferenceActivity {

  private SharedPreferences preferences;
  private OnSharedPreferenceChangeListener backupListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    preferences = PreferenceManager.getDefaultSharedPreferences(this);
    backupListener = BackupPreferenceListener.create(this);

    addPreferencesFromResource(R.xml.prefs);
  }

  @Override
  protected void onStart() {
    super.onStart();

    preferences.registerOnSharedPreferenceChangeListener(backupListener);
  }

  @Override
  protected void onStop() {
    preferences.unregisterOnSharedPreferenceChangeListener(backupListener);

    super.onStop();
  }
}
