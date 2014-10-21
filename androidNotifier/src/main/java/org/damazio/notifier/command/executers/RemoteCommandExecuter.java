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
package org.damazio.notifier.command.executers;

import static org.damazio.notifier.Constants.TAG;

import org.damazio.notifier.command.executers.call.CallExecuter;
import org.damazio.notifier.command.executers.call.HangUpExecuter;
import org.damazio.notifier.command.executers.sms.SmsExecuter;
import org.damazio.notifier.event.EventContext;
import org.damazio.notifier.event.EventListener;
import org.damazio.notifier.protocol.Common.Event;

import android.util.Log;

/**
 * Locally executes remote commands.
 *
 * @author Rodrigo Damazio
 */
public class RemoteCommandExecuter implements EventListener {

  public void onNewEvent(EventContext context, long eventId, Event event, boolean isLocal, boolean isCommand) {
    // Ensure it's a command sent from another device.
    if (isLocal || !isCommand) {
      return;
    }

    Log.d(TAG, "Executing command " + event);
    CommandExecuter executer = getExecuterForType(event.getType());
    executer.executeCommand(context.getAndroidContext(), event.getPayload());

    context.getEventManager().markEventProcessed(eventId);
  }

  private CommandExecuter getExecuterForType(Event.Type type) {
    switch (type) {
      case COMMAND_CALL:
        return new CallExecuter();
      case COMMAND_HANGUP:
        return new HangUpExecuter();
      case COMMAND_SMS:
        return new SmsExecuter();
      default:
        throw new IllegalArgumentException("Don't know how to handle type " + type);
    }
  }
}
