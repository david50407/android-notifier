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
package org.damazio.notifier;

import java.util.List;

import org.damazio.notifier.comm.pairing.DeviceManager;
import org.damazio.notifier.comm.transport.RemoteEventReceiver;
import org.damazio.notifier.comm.transport.LocalEventSender;
import org.damazio.notifier.command.executers.RemoteCommandExecuter;
import org.damazio.notifier.event.EventContext;
import org.damazio.notifier.event.EventManager;
import org.damazio.notifier.event.display.RemoteNotificationDisplayer;
import org.damazio.notifier.event.receivers.LocalEventReceiver;
import org.damazio.notifier.prefs.Preferences;
import org.damazio.notifier.prefs.Preferences.PreferenceListener;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class NotifierService extends Service {
  /** An independent module which has the same lifecycle as the service. */
  public static interface NotifierServiceModule {
    void onCreate();
    void onDestroy();
  }

  private final PreferenceListener preferencesListener = new PreferenceListener() {
    @Override
    public void onStartForegroundChanged(boolean startForeground) {
      updateForegroundState(startForeground);
    }
  };

  private Preferences preferences;

  private DeviceManager deviceManager;

  /** Manages events and the event log. */
  private EventManager eventManager;

  /** Receives events from other devices. */
  private RemoteEventReceiver remoteEventReceiver;

  /** Receives events from the local system. */
  private LocalEventReceiver localEventReceiver;

  /** Sends events to other devices. */
  private LocalEventSender localEventSender;

  /** Executes remote commands locally. */
  private RemoteCommandExecuter commandExecuter;

  /** Displays remote notifications locally. */
  private RemoteNotificationDisplayer notificationDisplayer;

  @Override
  public void onCreate() {
    super.onCreate();

    preferences = new Preferences(this);
    preferences.registerListener(preferencesListener);

    updateForegroundState(preferences.startForeground());

    deviceManager = new DeviceManager();
    eventManager = new EventManager(this, deviceManager, preferences);
    EventContext eventContext = new EventContext(this, eventManager, deviceManager, preferences);

    // Modules (external-event-driven)
    remoteEventReceiver = new RemoteEventReceiver(eventContext);
    localEventReceiver = new LocalEventReceiver(eventContext);
    remoteEventReceiver.onCreate();
    localEventReceiver.onCreate();

    // Event-log-driven
    localEventSender = new LocalEventSender(eventContext);
    localEventSender.onCreate();
    commandExecuter = new RemoteCommandExecuter();
    notificationDisplayer = new RemoteNotificationDisplayer();
    eventManager.registerEventListeners(localEventSender, commandExecuter, notificationDisplayer);
  }

  private void updateForegroundState(boolean startForeground) {
    if (startForeground) {
      startForeground();
    } else {
      stopForeground(true);
    }
  }

  private void startForeground() {
    PendingIntent intent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
    Notification notification = new Notification(R.drawable.icon, null /* ticker */, System.currentTimeMillis());
    notification.setLatestEventInfo(this, getString(R.string.notification_title), getString(R.string.notification_text), intent);
    notification.flags |= Notification.FLAG_NO_CLEAR;
    notification.flags |= Notification.FLAG_ONGOING_EVENT;

    // TODO: Ensure this never collides with event display notification IDs
    startForeground(0x91287346, notification);
  }

  @Override
  public void onStart(Intent intent, int startId) {
    handleCommand(intent);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    handleCommand(intent);
    return START_STICKY;
  }

  private void handleCommand(Intent intent) {
    // TODO: Intents for locale and otherss
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onDestroy() {
    preferences.unregisterListener(preferencesListener);
    eventManager.unregisterEventListeners(localEventSender, commandExecuter, notificationDisplayer);

    localEventReceiver.onDestroy();
    remoteEventReceiver.onDestroy();

    stopForeground(true);

    super.onDestroy();
  }

  @Override
  public void onLowMemory() {
    // TODO: We run in the foreground, so it's probably polite to do something here.
  }

  public static void startIfNotRunning(Context context) {
    if (!isRunning(context)) {
      context.startService(new Intent(context, NotifierService.class));
    }
  }

  /**
   * Uses the given context to determine whether the service is already running.
   */
  public static boolean isRunning(Context context) {
    ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
    List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

    for (RunningServiceInfo serviceInfo : services) {
      ComponentName componentName = serviceInfo.service;
      String serviceName = componentName.getClassName();
      if (serviceName.equals(NotifierService.class.getName())) {
        return true;
      }
    }

    return false;
  }

  protected Preferences getPreferences() {
    return preferences;
  }
}
