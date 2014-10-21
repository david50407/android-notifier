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
package org.damazio.notifier.comm.transport;

import org.damazio.notifier.NotifierService.NotifierServiceModule;
import org.damazio.notifier.event.EventContext;
import org.damazio.notifier.prefs.Preferences.PreferenceListener;

/**
 * Listens to and receives remote events.
 *
 * @author Rodrigo Damazio
 */
public class RemoteEventReceiver implements NotifierServiceModule {

  private final EventContext eventContext;
  private final PreferenceListener preferenceListener = new PreferenceListener() {
    // TODO
  };

  public RemoteEventReceiver(EventContext eventContext) {
    this.eventContext = eventContext;
  }

  public void onCreate() {
    // Register to learn about changes in transport methods.
    // This will also trigger the initial starting of the enabled ones.
    eventContext.getPreferences().registerListener(preferenceListener, true);
  }

  public void onDestroy() {
    eventContext.getPreferences().unregisterListener(preferenceListener);
  }
}
