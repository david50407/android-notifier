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
package org.damazio.notifier.command.executers.sms;

import static org.damazio.notifier.Constants.TAG;

import java.util.ArrayList;

import org.damazio.notifier.command.executers.CommandExecuter;
import org.damazio.notifier.protocol.Commands.SmsCommand;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import android.content.Context;
import android.telephony.SmsManager;
import android.util.Log;

public class SmsExecuter implements CommandExecuter {

  public void executeCommand(Context context, ByteString payload) {
    SmsCommand command;
    try {
      command = SmsCommand.parseFrom(payload);
    } catch (InvalidProtocolBufferException e) {
      Log.e(TAG, "Failed to parse SMS command", e);
      return;
    }

    String text = command.getText();
    String number = command.getToNumber();

    SmsManager smsManager = SmsManager.getDefault();
    ArrayList<String> textParts = smsManager.divideMessage(text);
    // TODO: Some day, notify back about sent and delivered.
    smsManager.sendMultipartTextMessage(number, null, textParts, null, null);
  }

}
