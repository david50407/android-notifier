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
package org.damazio.notifier.command.executers.call;

import static org.damazio.notifier.Constants.TAG;

import org.damazio.notifier.command.executers.CommandExecuter;
import org.damazio.notifier.protocol.Commands.CallCommand;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

public class CallExecuter implements CommandExecuter {
  public void executeCommand(Context context, ByteString payload) {
    CallCommand cmd;
    try {
       cmd = CallCommand.parseFrom(payload);
    } catch (InvalidProtocolBufferException e) {
      throw new IllegalArgumentException("Unable to parse call command: " + payload, e);
    }

    Log.i(TAG, "Calling " + cmd.getNumber());

    Uri numberUri = Uri.fromParts("tel", cmd.getNumber(), null);
    Intent intent = new Intent(Intent.ACTION_CALL);
    intent.setData(numberUri);
    context.startActivity(intent);
  }
}
