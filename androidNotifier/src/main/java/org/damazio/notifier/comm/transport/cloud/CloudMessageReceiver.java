package org.damazio.notifier.comm.transport.cloud;

import static org.damazio.notifier.Constants.TAG;

import java.util.Arrays;

import org.damazio.notifier.event.EventManager;
import org.damazio.notifier.protocol.Common.Event;

import com.google.protobuf.InvalidProtocolBufferException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CloudMessageReceiver extends BroadcastReceiver {

  private static final String EVENT_EXTRA = "event";

  @Override
  public void onReceive(Context context, Intent intent) {
    if (!"com.google.android.c2dm.intent.RECEIVE".equals(intent.getAction())) {
      Log.e(TAG, "Bad C2DM intent: " + intent);
      return;
    }

    // TODO: Handle payload > 1024 bytes
    byte[] eventBytes = intent.getExtras().getByteArray(EVENT_EXTRA);
    Event event;
    try {
      event = Event.parseFrom(eventBytes);
    } catch (InvalidProtocolBufferException e) {
      Log.e(TAG, "Got bad payload: " + Arrays.toString(eventBytes));
      return;
    }

    EventManager eventManager = new EventManager(context);
    eventManager.handleRemoteEvent(event);
  }
}
