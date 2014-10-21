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
package org.damazio.notifier.event.receivers.mms;

import static org.damazio.notifier.Constants.TAG;

import org.damazio.notifier.event.EventContext;
import org.damazio.notifier.event.receivers.EventBroadcastReceiver;
import org.damazio.notifier.event.util.PhoneNumberUtils;
import org.damazio.notifier.protocol.Common.Event;
import org.damazio.notifier.protocol.Common.Event.Type;
import org.damazio.notifier.protocol.Common.PhoneNumber;
import org.damazio.notifier.protocol.Notifications.MmsNotification;

import android.content.Intent;
import android.util.Log;

public class MmsReceiver extends EventBroadcastReceiver {
  private static final String DATA_TYPE = "application/vnd.wap.mms-message";

  @Override
  protected void onReceiveEvent(EventContext context, Intent intent) {
    if (!DATA_TYPE.equals(intent.getType())) {
      Log.e(TAG, "Got wrong data type for MMS: " + intent.getType());
      return;
    }

    // Parse the WAP push contents
    PduParser parser = new PduParser();
    PduHeaders headers = parser.parseHeaders(intent.getByteArrayExtra("data"));
    if (headers == null) {
      Log.e(TAG, "Couldn't parse headers for WAP PUSH.");
      return;
    }

    int messageType = headers.getMessageType();
    Log.d(TAG, "WAP PUSH message type: 0x" + Integer.toHexString(messageType));

    // Check if it's a MMS notification
    if (messageType == PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND) {
      String fromStr = null;
      EncodedStringValue encodedFrom = headers.getFrom();
      if (encodedFrom != null) {
        fromStr = encodedFrom.getString();
      }
      
      PhoneNumberUtils numberUtils = context.getNumberUtils();
      PhoneNumber from = numberUtils.resolvePhoneNumber(fromStr);

      // TODO: Add text/image/etc.
      MmsNotification mms = MmsNotification.newBuilder()
          .setSender(from)
          .build();
      handleEvent(mms);
    }
  }

  @Override
  protected String getExpectedAction() {
    return "android.provider.Telephony.WAP_PUSH_RECEIVED";
  }

  @Override
  protected Type getEventType() {
    return Event.Type.NOTIFICATION_MMS;
  }
}
