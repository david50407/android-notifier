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
package org.damazio.notifier.event.receivers.phone;

import org.damazio.notifier.event.EventContext;
import org.damazio.notifier.event.EventManager;
import org.damazio.notifier.event.util.PhoneNumberUtils;
import org.damazio.notifier.protocol.Common.Event;
import org.damazio.notifier.protocol.Common.PhoneNumber;
import org.damazio.notifier.protocol.Notifications.MissedCallNotification;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.provider.CallLog;

public class MissedCallListener {
  private class CallLogObserver extends ContentObserver {
    private CallLogObserver(Handler handler) {
      super(handler);
    }

    public void onChange(boolean selfChange) {
      super.onChange(selfChange);

      onCallLogChanged();
    }
  }

  private final EventContext eventContext;
  private final ContentResolver resolver;
  private final CallLogObserver observer;

  private long lastMissedCall;

  public MissedCallListener(EventContext eventContext) {
    this.eventContext = eventContext;
    this.resolver = eventContext.getAndroidContext().getContentResolver();
    this.observer = new CallLogObserver(new Handler());
  }

  public void onCreate() {
    this.lastMissedCall = System.currentTimeMillis();
    resolver.registerContentObserver(CallLog.Calls.CONTENT_URI, false, observer);
  }

  public void onDestroy() {
    resolver.unregisterContentObserver(observer);
  }

  private void onCallLogChanged() {
    Cursor cursor = resolver.query(
        CallLog.Calls.CONTENT_URI,
        new String[] { CallLog.Calls.NUMBER, CallLog.Calls.DATE },
        CallLog.Calls.DATE + " > ? AND " + CallLog.Calls.TYPE + " = ?",
        new String[] { Long.toString(lastMissedCall),
                       Integer.toString(CallLog.Calls.MISSED_TYPE) },
        CallLog.Calls.DATE + " DESC");

    EventManager eventManager = eventContext.getEventManager();
    PhoneNumberUtils numberUtils = eventContext.getNumberUtils();

    while (cursor.moveToNext()) {
      int numberIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER);
      int dateIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.DATE);
      if (cursor.isNull(numberIdx) || cursor.isNull(dateIdx)) {
        eventManager.handleLocalEvent(Event.Type.NOTIFICATION_MISSED_CALL, null);
        continue;
      }

      String number = cursor.getString(numberIdx);
      long callDate = cursor.getLong(dateIdx);

      PhoneNumber phoneNumber = numberUtils.resolvePhoneNumber(number);
      MissedCallNotification notification = MissedCallNotification.newBuilder()
          .setTimestamp((int) (callDate / 1000L))
          .setNumber(phoneNumber)
          .build();
      eventManager.handleLocalEvent(Event.Type.NOTIFICATION_MISSED_CALL, notification);

      lastMissedCall = callDate;
    }
  }
}
