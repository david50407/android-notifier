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
import org.damazio.notifier.event.receivers.EventBroadcastReceiver;
import org.damazio.notifier.event.util.PhoneNumberUtils;
import org.damazio.notifier.protocol.Common.Event;
import org.damazio.notifier.protocol.Common.Event.Type;
import org.damazio.notifier.protocol.Common.PhoneNumber;
import org.damazio.notifier.protocol.Notifications.RingNotification;

import android.content.Intent;
import android.telephony.TelephonyManager;

public class RingReceiver extends EventBroadcastReceiver {
  @Override
  protected void onReceiveEvent(EventContext context, Intent intent) {
    String stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
    if (TelephonyManager.EXTRA_STATE_RINGING.equals(stateStr)) {
      PhoneNumberUtils numberUtils = context.getNumberUtils();

      String incomingNumberStr = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
      PhoneNumber incomingNumber = numberUtils.resolvePhoneNumber(incomingNumberStr);

      RingNotification ring = RingNotification.newBuilder()
          .setNumber(incomingNumber)
          .build();
      handleEvent(ring);
    }
  }

  @Override
  protected Type getEventType() {
    return Event.Type.NOTIFICATION_RING;
  }

  @Override
  protected String getExpectedAction() {
    return TelephonyManager.ACTION_PHONE_STATE_CHANGED;
  }
}
