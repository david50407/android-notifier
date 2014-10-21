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
package org.damazio.notifier.event.display;

import org.damazio.notifier.R;
import org.damazio.notifier.event.EventContext;
import org.damazio.notifier.event.EventListener;
import org.damazio.notifier.prefs.Preferences;
import org.damazio.notifier.protocol.Common.Event;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Locally displays remote notifications.
 *
 * @author Rodrigo Damazio
 */
public class RemoteNotificationDisplayer implements EventListener {

  public void onNewEvent(EventContext context, long eventId, Event event, boolean isLocal, boolean isCommand) {
    // Only care about remote notifications.
    if (isLocal || isCommand) {
      return;
    }

    // TODO: Proper text
    String shortText = event.toString();

    Preferences preferences = context.getPreferences();
    Context androidContext = context.getAndroidContext();
    if (preferences.isSystemDisplayEnabled()) {
      Notification notification = new Notification(R.drawable.icon, shortText, System.currentTimeMillis());
      // TODO: Intent = event log
      // TODO: Configurable defaults
      notification.defaults = Notification.DEFAULT_ALL;
      notification.flags |= Notification.FLAG_AUTO_CANCEL;

      NotificationManager notificationManager =
          (NotificationManager) androidContext.getSystemService(Context.NOTIFICATION_SERVICE);
      notificationManager.notify(
          "display",
          (int) (event.getTimestamp() & Integer.MAX_VALUE),
          notification);
    }

    if (preferences.isToastDisplayEnabled()) {
      Toast.makeText(androidContext, shortText, Toast.LENGTH_LONG).show();
    }

    if (preferences.isPopupDisplayEnabled()) {
      Intent intent = new Intent(androidContext, PopupDisplayActivity.class);
      intent.putExtra(PopupDisplayActivity.EXTRA_POPUP_TEXT, shortText);
    }

    context.getEventManager().markEventProcessed(eventId);
  }
}
