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
package org.damazio.notifier.event.receivers.battery;

import static org.damazio.notifier.Constants.TAG;

import java.util.HashMap;
import java.util.Map;

import org.damazio.notifier.event.EventContext;
import org.damazio.notifier.event.receivers.EventBroadcastReceiver;
import org.damazio.notifier.prefs.Preferences;
import org.damazio.notifier.protocol.Common.Event;
import org.damazio.notifier.protocol.Common.Event.Type;
import org.damazio.notifier.protocol.Notifications.BatteryNotification;
import org.damazio.notifier.protocol.Notifications.BatteryNotification.State;

import android.content.Intent;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;

public class BatteryEventReceiver extends EventBroadcastReceiver {
  private static final Map<Integer, State> STATE_MAP = new HashMap<Integer, State>();
  static {
    STATE_MAP.put(BatteryManager.BATTERY_STATUS_CHARGING, State.CHARGING);
    STATE_MAP.put(BatteryManager.BATTERY_STATUS_DISCHARGING, State.DISCHARGING);
    STATE_MAP.put(BatteryManager.BATTERY_STATUS_FULL, State.FULL);
    STATE_MAP.put(BatteryManager.BATTERY_STATUS_NOT_CHARGING, State.NOT_CHARGING);
    STATE_MAP.put(BatteryManager.BATTERY_STATUS_UNKNOWN, null);
  }

  private int lastLevelPercentage;
  private State lastState;

  @Override
  protected void onReceiveEvent(EventContext context, Intent intent) {
    // Try to read extras from intent
    Bundle extras = intent.getExtras();
    if (extras == null) {
      return;
    }

    int level = extras.getInt(BatteryManager.EXTRA_LEVEL, -1);
    int maxLevel = extras.getInt(BatteryManager.EXTRA_SCALE, -1);
    int status = extras.getInt(BatteryManager.EXTRA_STATUS, -1);

    BatteryNotification.Builder notificationBuilder = BatteryNotification.newBuilder();

    State state = STATE_MAP.get(status);
    if (state != null) {
      notificationBuilder.setState(state);
    }

    int levelPercentage = -1;
    if (level != -1 && maxLevel != -1) {
      levelPercentage = 100 * level / maxLevel;
      notificationBuilder.setChargePercentage(levelPercentage);
    }

    Log.d(TAG, "Got battery level=" + levelPercentage + "; state=" + state);

    if (!shouldReport(state, levelPercentage, context.getPreferences())) {
      Log.d(TAG, "Not reporting battery state");
      return;
    }

    lastLevelPercentage = levelPercentage;
    lastState = state;

    handleEvent(notificationBuilder.build());
  }

  private boolean shouldReport(State state, int levelPercentage, Preferences preferences) {
    if (state != lastState) {
      // State changed
      return true;
    }

    int maxPercentage = preferences.getMaxBatteryLevel();
    int minPercentage = preferences.getMinBatteryLevel();
    if (levelPercentage > maxPercentage ||
        levelPercentage < minPercentage) {
      // Outside the range
      return false;
    }

    if (levelPercentage == maxPercentage ||
        levelPercentage == minPercentage) {
      // About to go in or out of range, always make a first/last report
      return true;
    }

    int percentageChange = Math.abs(levelPercentage - lastLevelPercentage);
    if (percentageChange < preferences.getMinBatteryLevelChange()) {
      // Too small change
      return false;
    }

    // With range and above or equal to min change
    return true;
  }

  @Override
  protected Type getEventType() {
    return Event.Type.NOTIFICATION_BATTERY;
  }

  @Override
  protected String getExpectedAction() {
    return Intent.ACTION_BATTERY_CHANGED;
  }
}
