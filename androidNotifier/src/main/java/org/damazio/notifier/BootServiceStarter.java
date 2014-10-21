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
package org.damazio.notifier;

import static org.damazio.notifier.Constants.TAG;

import org.damazio.notifier.prefs.Preferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootServiceStarter extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent.getAction() != Intent.ACTION_BOOT_COMPLETED) {
      Log.w(TAG, "Received unexpected intent: " + intent);
      return;
    }

    Preferences preferences = new Preferences(context);
    // TODO: Check if start at boot requested.

    context.startService(new Intent(context, NotifierService.class));
  }
}
