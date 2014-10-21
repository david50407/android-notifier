package org.damazio.notifier.comm.pairing;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DeviceManager {

  public static class Device {
    public final long deviceId;
    public final String ipAddress;
    public final String bluetoothAddress;

    public Device(long deviceId, String ipAddress, String bluetoothAddress) {
      this.deviceId = deviceId;
      this.ipAddress = ipAddress;
      this.bluetoothAddress = bluetoothAddress;
    }
  }

  private final Map<Long, Device> devicesById =
      new HashMap<Long, DeviceManager.Device>();

  public Device getDeviceForId(long deviceId) {
    return devicesById.get(deviceId);
  }

  public Collection<Device> getAllDevices() {
    return devicesById.values();
  }
}
