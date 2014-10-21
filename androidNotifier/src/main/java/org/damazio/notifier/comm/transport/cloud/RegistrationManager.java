package org.damazio.notifier.comm.transport.cloud;

import org.damazio.notifier.prefs.Preferences;

import android.content.Context;

public class RegistrationManager {

  public void register() {

  }

  public void unregister() {

  }

  public static void handleError(String error) {
    // TODO Auto-generated method stub

  }

  public static void registrationIdReceived(Context context, String registrationId) {
    Preferences preferences = new Preferences(context);
    preferences.setC2dmRegistrationId(registrationId);
    preferences.setC2dmServerRegistered(false);
    // TODO: Start service to notify server
  }

}
