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
package org.damazio.notifier.event.receivers.sms;

import static org.damazio.notifier.Constants.TAG;

import org.damazio.notifier.event.EventContext;
import org.damazio.notifier.event.receivers.EventBroadcastReceiver;
import org.damazio.notifier.protocol.Common.Event;
import org.damazio.notifier.protocol.Common.Event.Type;
import org.damazio.notifier.protocol.Notifications.SmsNotification;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class SmsReceiver extends EventBroadcastReceiver {
  private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

  @Override
  protected void onReceiveEvent(EventContext context, Intent intent) {
    // Create the notification contents using the SMS contents
    boolean notificationSent = false;
    Bundle bundle = intent.getExtras();
    if (bundle != null) {
      Object[] pdus = (Object[]) bundle.get("pdus");
      SmsDecoder decoder = new SmsDecoder(context.getAndroidContext(), context.getNumberUtils());

      for (int i = 0; i < pdus.length; i++) {
        SmsNotification sms = decoder.decodeSms(pdus[i]);
        if (sms == null) {
          continue;
        }

        handleEvent(sms);
        notificationSent = true;
      }
    }

    if (!notificationSent) {
      // If no notification sent (extra info was not there), send one without info
      Log.w(TAG, "Got SMS but failed to extract details.");
      handleEvent(null);
    }
  }

  @Override
  protected Type getEventType() {
    return Event.Type.NOTIFICATION_SMS;
  }

  @Override
  protected String getExpectedAction() {
    return ACTION;
  }
}
