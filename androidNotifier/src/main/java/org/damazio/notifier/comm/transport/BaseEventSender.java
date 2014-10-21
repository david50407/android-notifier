package org.damazio.notifier.comm.transport;

import static org.damazio.notifier.Constants.TAG;

import org.damazio.notifier.event.EventContext;
import org.damazio.notifier.protocol.Common.Event;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.util.Log;

// TODO: Maybe this is overkill - should we use the DB for retries?
public abstract class BaseEventSender {
  private static final int MSG_EVENT = 42;
  private static final long RETRY_BACKOFF_MS = 200L;
  private static final int MAX_RETRIES = 10;

  private static class EventState {
    public EventContext context;
    public Event event;
    public long eventId;
    public int retries = 0;

    public EventState(EventContext context, long eventId, Event event) {
      this.context = context;
      this.event = event;
      this.eventId = eventId;
    }
  }

  private class EventHandler implements Handler.Callback {
    public boolean handleMessage(Message msg) {
      if (msg.what != MSG_EVENT) {
        Log.e(TAG, "Got bad message type: " + msg.what);
        return true;
      }

      Object msgObj = msg.obj;
      if (!(msgObj instanceof Event)) {
        Log.e(TAG, "Got bad event object: " + msgObj.getClass().getName());
        return true;
      }

      EventState state = (EventState) msgObj;
      boolean handled = handleEvent(state.context, state.eventId, state.event);
      if (handled) {
        state.context.getEventManager().markEventProcessed(state.eventId);
      }
      if (handled || !scheduleRetry(msg, state)) {
        // We're done with this message
        msg.recycle();
      }

      return true;
    }
  }

  private HandlerThread sendThread;
  private Handler sendHandler;

  public void start() {
    sendThread = new HandlerThread(
        "sender-" + getTransportType(),
        Process.THREAD_PRIORITY_BACKGROUND);
    sendThread.start();
    sendHandler = new Handler(sendThread.getLooper(), new EventHandler());
  }

  private boolean scheduleRetry(Message msg, EventState state) {
    if (state.retries >= MAX_RETRIES) {
      Log.w(TAG, "Too many retries, giving up.");
      return false;
    }

    // Schedule a retry
    state.retries++;
    long delayMs = state.retries * RETRY_BACKOFF_MS;
    sendHandler.sendMessageDelayed(msg, delayMs);
    return true;
  }

  public void sendEvent(EventContext context, long eventId, Event event) {
    EventState state = new EventState(context, eventId, event);
    Message message = Message.obtain(sendHandler, MSG_EVENT, state);
    message.sendToTarget();
  }

  public void shutdown() {
    sendThread.quit();

    try {
      sendThread.join();
    } catch (InterruptedException e) {
      Log.w(TAG, "Got exception waiting for send thread for " + getTransportType(), e);
    }
  }

  protected abstract boolean handleEvent(EventContext context, long eventId, Event event);
  protected abstract TransportType getTransportType();
}
