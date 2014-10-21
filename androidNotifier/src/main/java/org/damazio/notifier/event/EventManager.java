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

import static org.damazio.notifier.Constants.TAG;

import java.util.HashSet;
import java.util.Set;

import org.damazio.notifier.comm.pairing.DeviceManager;
import org.damazio.notifier.event.log.EventLogColumns;
import org.damazio.notifier.event.log.EventLogHelper;
import org.damazio.notifier.prefs.Preferences;
import org.damazio.notifier.protocol.Common.Event;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.protobuf.MessageLite;

public class EventManager {
  private final EventLogHelper logHelper;
  private final Preferences preferences;
  private final EventContext eventContext;

  private HandlerThread eventThread;
  private final Set<EventListener> listeners = new HashSet<EventListener>();
  private ContentObserver logObserver;

  private long lastEventId;

  public EventManager(Context context) {
    this(context, new DeviceManager(), new Preferences(context));
  }

  public EventManager(Context context, DeviceManager deviceManager, Preferences preferences) {
    this.logHelper = new EventLogHelper(context.getContentResolver());
    this.preferences = preferences;

    // TODO: Cut this circular dependency
    this.eventContext = new EventContext(context, this, deviceManager, preferences);
  }

  public EventContext getEventContext() {
    return eventContext;
  }

  // TODO: How to ensure a wake lock is kept until the event is handled?
  public void handleLocalEvent(Event.Type eventType, MessageLite payload) {
    // Save the event to the event log.
    // Listeners will be notified by the logObserver.
    Log.d(TAG, "Saved event with type=" + eventType + "; payload=" + payload);
    logHelper.insertEvent(eventType, payload.toByteArray(), System.currentTimeMillis(), null);
  }

  public void handleRemoteEvent(Event event) {
    // Save the event to the event log.
    // Listeners will be notified by the logObserver.
    Log.d(TAG, "Received event " + event);
    logHelper.insertEvent(event.getType(), event.getPayload().toByteArray(), event.getTimestamp(), event.getSourceDeviceId());
  }

  public void markEventProcessed(long eventId) {
    if (preferences.shouldPruneLog() && preferences.getPruneLogDays() == 0) {
      // Prune immediately.
      logHelper.deleteEvent(eventId);
    } else {
      logHelper.markEventProcessed(eventId);
    }
  }

  public void registerEventListeners(EventListener... addedListeners) {
    synchronized (listeners) {
      if (listeners.isEmpty()) {
        registerEventLogListener();
      }
      for (EventListener listener : addedListeners) {
        listeners.add(listener);
      }
    }
  }

  public void unregisterEventListeners(EventListener... removedListeners) {
    synchronized (listeners) {
      for (EventListener listener : removedListeners) {
        listeners.remove(listener);
      }
      if (listeners.isEmpty()) {
        unregisterEventLogListener();
      }
    }
  }

  public void forceRetryProcessing() {
    logObserver.dispatchChange(false);
  }

  private void registerEventLogListener() {
    if (eventThread != null) return;

    // Listen for new events in the event log.
    eventThread = new HandlerThread("EventObserverThread");
    eventThread.start();
    Handler handler = new Handler(eventThread.getLooper());
    logObserver = new ContentObserver(handler) {
      @Override
      public void onChange(boolean selfChange) {
        notifyNewEvents();
      }
    };
    logHelper.registerContentObserver(logObserver);

    // Do an initial run to ensure any missed events are delivered.
    logObserver.dispatchChange(false);
  }

  private void unregisterEventLogListener() {
    if (eventThread == null) return;

    logHelper.unregisterContentObserver(logObserver);

    eventThread.quit();
    try {
      eventThread.join(1000);
    } catch (InterruptedException e) {
      Log.e(TAG, "Failed to join event thread", e);
    }

    eventThread = null;
    logObserver = null;
  }

  private void notifyNewEvents() {
    // Get all unprocessed events since the last one.
    // TODO: Also retry older ones, up to a certain deadline, but not too quickly.
    Cursor cursor = logHelper.getUnprocessedEvents(lastEventId + 1);
    synchronized (listeners) {
      while (cursor.moveToNext()) {
        Event event = logHelper.buildEvent(cursor);
        Log.d(TAG, "Notifying about " + event.toString());

        boolean isLocal = !event.hasSourceDeviceId();
        boolean isCommand = ((event.getType().getNumber() & Event.Type.COMMAND_MASK_VALUE) != 0);

        lastEventId = cursor.getLong(cursor.getColumnIndexOrThrow(EventLogColumns._ID));

        for (EventListener listener : listeners) {
          listener.onNewEvent(eventContext, lastEventId, event, isLocal, isCommand);
        }
      }
    }
  }
}
