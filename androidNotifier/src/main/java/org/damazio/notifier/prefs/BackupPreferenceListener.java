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

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;

public class BackupPreferenceListener {

  /**
   * Real implementation of the listener, which calls the {@link BackupManager}.
   */
  private static class BackupPreferencesListenerImpl implements OnSharedPreferenceChangeListener{
    private final BackupManager backupManager;

    public BackupPreferencesListenerImpl(Context context) {
      this.backupManager = new BackupManager(context);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
      backupManager.dataChanged();
    }
  }

  /**
   * Creates and returns a proper instance of the listener for this device.
   */
  public static OnSharedPreferenceChangeListener create(Context context) {
    if (Build.VERSION.SDK_INT >= 8) {
      return new BackupPreferencesListenerImpl(context);
    } else {
      return null;
    }
  }

  private BackupPreferenceListener() {}
}
