package org.damazio.notifier.comm.transport.cloud;

import static org.damazio.notifier.Constants.TAG;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RegistrationReceiver extends BroadcastReceiver {

  private static final String REGISTRATION_ACTION = "com.google.android.c2dm.intent.REGISTRATION";

  @Override
  public void onReceive(Context context, Intent intent) {
    if (!intent.getAction().equals(REGISTRATION_ACTION)) {
      Log.e(TAG, "Got bad intent at C2DM registration receiver: " + intent);
      return;
    }

    String error = intent.getStringExtra("error");
    if (error != null) {
      RegistrationManager.handleError(error);
    }

    String registrationId = intent.getStringExtra("registration_id");
    RegistrationManager.registrationIdReceived(context, registrationId);
  }

}
