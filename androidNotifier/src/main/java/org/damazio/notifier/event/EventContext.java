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
package org.damazio.notifier.event;

import org.damazio.notifier.comm.pairing.DeviceManager;
import org.damazio.notifier.event.util.PhoneNumberUtils;
import org.damazio.notifier.prefs.Preferences;

import android.content.Context;

public class EventContext {
  private final Context context;
  private final EventManager eventManager;
  private final DeviceManager deviceManager;
  private final Preferences preferences;
  private PhoneNumberUtils numberUtils;

  public EventContext(Context context, EventManager eventManager,
      DeviceManager deviceManager, Preferences preferences) {
    this.context = context;
    this.eventManager = eventManager;
    this.preferences = preferences;
    this.deviceManager = deviceManager;
  }

  public synchronized PhoneNumberUtils getNumberUtils() {
    if (numberUtils == null) {
      numberUtils = new PhoneNumberUtils(context);
    }
    return numberUtils;
  }

  public Context getAndroidContext() {
    return context;
  }

  public Preferences getPreferences() {
    return preferences;
  }

  public EventManager getEventManager() {
    return eventManager;
  }

  public DeviceManager getDeviceManager() {
    return deviceManager;
  }
}
