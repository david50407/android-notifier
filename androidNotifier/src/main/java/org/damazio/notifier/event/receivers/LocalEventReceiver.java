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
package org.damazio.notifier.event.receivers;

import static org.damazio.notifier.Constants.TAG;

import org.damazio.notifier.NotifierService.NotifierServiceModule;
import org.damazio.notifier.event.EventContext;
import org.damazio.notifier.event.receivers.battery.BatteryEventReceiver;
import org.damazio.notifier.event.receivers.phone.MissedCallListener;
import org.damazio.notifier.event.receivers.phone.VoicemailListener;
import org.damazio.notifier.prefs.Preferences.PreferenceListener;
import org.damazio.notifier.protocol.Common.Event.Type;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Module which manages local event receivers.
 * (except those driven by broadcasts, which are registered directly in the Manifest)
 *
 * @author Rodrigo Damazio
 */
public class LocalEventReceiver implements NotifierServiceModule {
  private final EventContext eventContext;

  private final PreferenceListener preferenceListener = new PreferenceListener() {
    @Override
    public synchronized void onSendEventStateChanged(Type type, boolean enabled) {
      switch (type) {
        case NOTIFICATION_MISSED_CALL:
          onMissedCallStateChanged(enabled);
          break;
        case NOTIFICATION_BATTERY:
          onBatteryStateChanged(enabled);
          break;
        case NOTIFICATION_VOICEMAIL:
          onVoicemailStateChanged(enabled);
          break;
        case NOTIFICATION_RING:
        case NOTIFICATION_SMS:
        case NOTIFICATION_MMS:
        case NOTIFICATION_THIRD_PARTY:
          // These come in via global broadcast receivers, nothing to do.
          break;
        default:
          Log.w(TAG, "Unknown event type's state changed: " + type);
      }
    }
  };

  private VoicemailListener voicemailListener;
  private BatteryEventReceiver batteryReceiver;
  private MissedCallListener missedCallListener;

  public LocalEventReceiver(EventContext eventContext) {
    this.eventContext = eventContext;
  }

  public void onCreate() {
    if (voicemailListener != null) {
      throw new IllegalStateException("Already started");
    }

    // Register for preference changes, and also do an initial notification of
    // the current values.
    eventContext.getPreferences().registerListener(preferenceListener, true);
  }

  public void onDestroy() {
    eventContext.getPreferences().unregisterListener(preferenceListener);

    // Disable all events.
    onMissedCallStateChanged(false);
    onBatteryStateChanged(false);
    onVoicemailStateChanged(false);
  }

  private synchronized void onVoicemailStateChanged(boolean enabled) {
    if (voicemailListener != null ^ !enabled) {
      Log.d(TAG, "Voicemail state didn't change");
      return;
    }

    TelephonyManager telephonyManager = (TelephonyManager) eventContext.getAndroidContext().getSystemService(Context.TELEPHONY_SERVICE);
    if (enabled) {
      voicemailListener = new VoicemailListener(eventContext);
      telephonyManager.listen(voicemailListener, PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR);
    } else {
      telephonyManager.listen(voicemailListener, PhoneStateListener.LISTEN_NONE);
      voicemailListener = null;
    }
  }

  private void onBatteryStateChanged(boolean enabled) {
    if (batteryReceiver != null ^ !enabled) {
      Log.d(TAG, "Battery state didn't change");
      return;
    }

    if (enabled) {
      // Register the battery receiver
      // (can't be registered in the manifest for some reason)
      batteryReceiver = new BatteryEventReceiver();
      eventContext.getAndroidContext().registerReceiver(
          batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    } else {
      eventContext.getAndroidContext().unregisterReceiver(batteryReceiver);
    }
  }

  private synchronized void onMissedCallStateChanged(boolean enabled) {
    if (missedCallListener != null ^ !enabled) {
      Log.d(TAG, "Missed call state didn't change");
      return;
    }

    if (enabled) {
      missedCallListener = new MissedCallListener(eventContext);
      missedCallListener.onCreate();
    } else {
      missedCallListener.onDestroy();
      missedCallListener = null;
    }
  }
}
