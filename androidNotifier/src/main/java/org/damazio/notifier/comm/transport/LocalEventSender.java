package org.damazio.notifier.comm.transport;

import java.util.EnumMap;
import java.util.EnumSet;

import org.damazio.notifier.NotifierService.NotifierServiceModule;
import org.damazio.notifier.comm.transport.ip.IpEventSender;
import org.damazio.notifier.event.EventContext;
import org.damazio.notifier.event.EventListener;
import org.damazio.notifier.prefs.Preferences;
import org.damazio.notifier.prefs.Preferences.PreferenceListener;
import org.damazio.notifier.protocol.Common.Event;
import org.damazio.notifier.util.DeviceUtils;

/**
 * Sends local events to remote devices.
 *
 * @author Rodrigo Damazio
 */
public class LocalEventSender implements EventListener, NotifierServiceModule {
  private final EnumMap<TransportType, BaseEventSender> senders =
      new EnumMap<TransportType, BaseEventSender>(TransportType.class);

  private final PreferenceListener preferenceListener = new PreferenceListener() {
    @Override
    public void onTransportStateChanged(TransportType type, boolean enabled) {
      synchronized (senders) {
        if (enabled) {
          onTransportEnabled(type);
        } else {
          onTransportDisabled(type);
        }
      }
    }
  };

  private final EventContext eventContext;

  public LocalEventSender(EventContext eventContext) {
    this.eventContext = eventContext;
  }

  public void onCreate() {
    // Register for changes on transport types, and initially get a
    // notification about the current types.
    eventContext.getPreferences().registerListener(preferenceListener, true);
  }

  public void onDestroy() {
    eventContext.getPreferences().unregisterListener(preferenceListener);

    // Shutdown all transports.
    synchronized (senders) {
      for (TransportType type : TransportType.values()) {
        onTransportDisabled(type);
      }
    }
  }

  private void onTransportEnabled(TransportType type) {
    BaseEventSender sender = null;
    switch (type) {
      case BLUETOOTH:
        // TODO
        break;
      case CLOUD:
        // TODO
        break;
      case IP:
        sender = new IpEventSender(eventContext);
        break;
      case USB:
        // TODO
        break;
    }

    if (sender != null) {
      sender.start();
      senders.put(type, sender);
    }
  }

  private void onTransportDisabled(TransportType type) {
    BaseEventSender sender = senders.remove(type);
    if (sender != null) {
      sender.shutdown();
    }
  }

  @Override
  public void onNewEvent(EventContext context, long eventId, Event event, boolean isLocal, boolean isCommand) {
    if (!isLocal) {
      // Non-local events should not be sent.
      return;
    }

    // Add source device ID to the event.
    event = event.toBuilder()
        .setSourceDeviceId(DeviceUtils.getDeviceId(context.getAndroidContext()))
        .build();

    Preferences preferences = context.getPreferences();
    EnumSet<TransportType> transportTypes = preferences.getEnabledTransports();
    synchronized (senders) {
      for (TransportType transportType : transportTypes) {
        BaseEventSender sender = senders.get(transportType);
        sender.sendEvent(context, eventId, event);
      }
    }
  }

}
