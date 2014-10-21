package org.damazio.notifier.util;

import static org.damazio.notifier.Constants.TAG;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

public class DeviceUtils {

  public static long getDeviceId(Context context) {
    String deviceIdStr = Settings.Secure.getString(
        context.getContentResolver(), Settings.Secure.ANDROID_ID);
    if (deviceIdStr == null) {
      long rand = Double.doubleToRawLongBits(Math.random() * Long.MAX_VALUE);
      Log.w(TAG, "No device ID found - created random ID " + rand);
      return rand;
    } else {
      return Long.parseLong(deviceIdStr, 16);
    }
  }

  private DeviceUtils() {}
}
