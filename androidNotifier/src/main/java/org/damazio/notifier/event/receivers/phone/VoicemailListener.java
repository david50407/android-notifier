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
import org.damazio.notifier.protocol.Common.Event;
import org.damazio.notifier.protocol.Notifications.VoicemailNotification;

import android.telephony.PhoneStateListener;

public class VoicemailListener extends PhoneStateListener {
  private final EventContext eventContext;

  public VoicemailListener(EventContext eventContext) {
    this.eventContext = eventContext;
  }

  @Override
  public void onMessageWaitingIndicatorChanged(boolean mwi) {
    if (!eventContext.getPreferences().isEventTypeEnabled(Event.Type.NOTIFICATION_VOICEMAIL)) {
      return;
    }

    VoicemailNotification notification = VoicemailNotification.newBuilder()
        .setHasVoicemail(mwi)
        .build();

    eventContext.getEventManager().handleLocalEvent(Event.Type.NOTIFICATION_VOICEMAIL, notification);
  }
}
