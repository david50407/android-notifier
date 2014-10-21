package org.damazio.notifier.comm.transport.ip;

import static org.damazio.notifier.Constants.TAG;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

import org.damazio.notifier.comm.pairing.DeviceManager;
import org.damazio.notifier.comm.pairing.DeviceManager.Device;
import org.damazio.notifier.comm.transport.BaseEventSender;
import org.damazio.notifier.comm.transport.TransportType;
import org.damazio.notifier.event.EventContext;
import org.damazio.notifier.prefs.Preferences;
import org.damazio.notifier.protocol.Common.Event;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.util.Log;

public class IpEventSender extends BaseEventSender {
  private static final int DEFAULT_PORT = 10600;

  private static final int TCP_CONNECT_TIMEOUT_MS = 10 * 1000;

  private final EventContext context;
  private final WifiManager wifi;
  private final ConnectivityManager connectivity;

  public IpEventSender(EventContext context) {
    this.context = context;

    Context androidContext = context.getAndroidContext();
    this.wifi = (WifiManager) androidContext.getSystemService(Context.WIFI_SERVICE);
    this.connectivity =
        (ConnectivityManager) androidContext.getSystemService(Context.CONNECTIVITY_SERVICE);
  }

  @Override
  protected boolean handleEvent(EventContext context, long eventId, Event event) {
    byte[] payload = event.toByteArray();
    Preferences preferences = context.getPreferences();

    // TODO: This belongs somewhere common to all transports
    DeviceManager deviceManager = context.getDeviceManager();
    Collection<Device> targetDevices;
    if (event.getTargetDeviceIdCount() > 0) {
      targetDevices = new ArrayList<DeviceManager.Device>(event.getTargetDeviceIdCount());
      for (long targetDeviceId : event.getTargetDeviceIdList()) {
        Device device = deviceManager.getDeviceForId(targetDeviceId);
        if (device != null) {
          targetDevices.add(device);
        }
      }
    } else {
      targetDevices = deviceManager.getAllDevices();
    }

    // TODO: Enabling wifi, wifi lock, send over 3G, check background data.

    for (Device device : targetDevices) {
      if (preferences.isIpOverTcp()) {
        // Try sending over TCP.
        if (trySendOverTcp(context, payload, device)) {
          // Stop when the first delivery succeeds.
          return true;
        }
      }
    }

    return false;
  }

  private boolean trySendOverTcp(EventContext context, byte[] payload, Device device) {
    Log.d(TAG, "Sending over TCP");

    try {
      InetAddress address = InetAddress.getByName(device.ipAddress);

      Socket socket = new Socket();
      // TODO: Custom port
      SocketAddress remoteAddr = new InetSocketAddress(address, DEFAULT_PORT);
      socket.connect(remoteAddr, TCP_CONNECT_TIMEOUT_MS);
      socket.setSendBufferSize(payload.length * 2);
      OutputStream stream = socket.getOutputStream();
      stream.write(payload);
      stream.flush();
      socket.close();
      Log.d(TAG, "Sent over TCP");
      return true;
    } catch (UnknownHostException e) {
      Log.w(TAG, "Failed to send over TCP", e);
      return false;
    } catch (IOException e) {
      Log.w(TAG, "Failed to send over TCP", e);
      return false;
    }
  }

  private boolean trySendOverUdp(EventContext context, byte[] payload) {
    return false;
  }

  @Override
  protected TransportType getTransportType() {
    return TransportType.IP;
  }

}
