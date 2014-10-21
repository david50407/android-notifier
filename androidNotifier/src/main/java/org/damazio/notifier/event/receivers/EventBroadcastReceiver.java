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

import org.damazio.notifier.NotifierService;
import org.damazio.notifier.comm.pairing.DeviceManager;
import org.damazio.notifier.event.EventContext;
import org.damazio.notifier.event.EventManager;
import org.damazio.notifier.prefs.Preferences;
import org.damazio.notifier.protocol.Common.Event;
import org.damazio.notifier.protocol.Common.Event.Type;

import com.google.protobuf.MessageLite;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Base class for all broadcast receivers which receive events.
 *
 * @author Rodrigo Damazio
 */
public abstract class EventBroadcastReceiver extends BroadcastReceiver {
  private EventManager eventManager;

  @Override
  public final void onReceive(Context context, Intent intent) {
    Type eventType = getEventType();
    if (!intent.getAction().equals(getExpectedAction())) {
      Log.e(TAG, "Wrong intent received by receiver for " + eventType.name() + ": " + intent.getAction());
      return;
    }

    NotifierService.startIfNotRunning(context);

    Preferences preferences = new Preferences(context);
    if (!preferences.isEventTypeEnabled(eventType)) {
      return;
    }

    synchronized (this) {
      DeviceManager deviceManager = new DeviceManager();
      eventManager = new EventManager(context, deviceManager, preferences);

      onReceiveEvent(eventManager.getEventContext(), intent);

      eventManager = null;
    }
  }

  protected void handleEvent(MessageLite notification) {
    eventManager.handleLocalEvent(getEventType(), notification);
  }

  /**
   * Returns the event type being generated.
   */
  protected abstract Event.Type getEventType();

  /**
   * Returns the broadcast action this receiver handles.
   */
  protected abstract String getExpectedAction();

  /**
   * Processes the broadcast intent and calls {@link #handleEvent} with the results.
   */
  protected abstract void onReceiveEvent(EventContext context, Intent intent);
}
