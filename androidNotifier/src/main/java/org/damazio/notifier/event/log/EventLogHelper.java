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
package org.damazio.notifier.event.log;

import static org.damazio.notifier.Constants.TAG;

import org.damazio.notifier.protocol.Common.Event;
import org.damazio.notifier.protocol.Common.Event.Builder;

import com.google.protobuf.ByteString;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class EventLogHelper {
  private final ContentResolver contentResolver; 

  public EventLogHelper(ContentResolver contentResolver) {
    this.contentResolver = contentResolver;
  }
  
  /**
   * 
   * @param type
   * @param payload
   * @param timestamp
   * @param sourceDeviceId the ID of the device that sent the notification, or null if it was generated locally
   */
  public void insertEvent(Event.Type type, byte[] payload, long timestamp, Long sourceDeviceId) {
    ContentValues values = new ContentValues(5);
    values.put(EventLogColumns.COLUMN_EVENT_TYPE, type.getNumber());
    values.put(EventLogColumns.COLUMN_SOURCE_DEVICE_ID, sourceDeviceId);
    values.put(EventLogColumns.COLUMN_TIMESTAMP, timestamp);
    values.put(EventLogColumns.COLUMN_PAYLOAD, payload);
    Uri inserted = contentResolver.insert(EventLogColumns.URI, values);
    Log.d(TAG, "Created event row " + inserted);
  }

  public void registerContentObserver(ContentObserver observer) {
    contentResolver.registerContentObserver(
        EventLogColumns.URI, true, observer);
  }

  public void unregisterContentObserver(ContentObserver observber) {
    contentResolver.unregisterContentObserver(observber);
  }

  public Cursor getUnprocessedEvents(long minEventId) {
    return contentResolver.query(EventLogColumns.URI, null,
        EventLogColumns.COLUMN_PROCESSED + " = FALSE AND" +
        EventLogColumns._ID + " >= ?", new String[] { Long.toString(minEventId) },
        null);
  }

  public Event buildEvent(Cursor cursor) {
    int typeIdx = cursor.getColumnIndexOrThrow(EventLogColumns.COLUMN_EVENT_TYPE);
    int sourceDeviceIdx = cursor.getColumnIndex(EventLogColumns.COLUMN_SOURCE_DEVICE_ID);
    int timestampIdx = cursor.getColumnIndexOrThrow(EventLogColumns.COLUMN_TIMESTAMP);
    int payloadIdx = cursor.getColumnIndex(EventLogColumns.COLUMN_PAYLOAD);

    Builder eventBuilder = Event.newBuilder();
    eventBuilder.setTimestamp(cursor.getLong(timestampIdx));
    eventBuilder.setType(Event.Type.valueOf(cursor.getInt(typeIdx)));
    if (payloadIdx != -1 && !cursor.isNull(payloadIdx)) {
      eventBuilder.setPayload(ByteString.copyFrom(cursor.getBlob(payloadIdx)));
    }
    if (sourceDeviceIdx != -1 && !cursor.isNull(sourceDeviceIdx)) {
      eventBuilder.setSourceDeviceId(cursor.getLong(sourceDeviceIdx));
    }

    return eventBuilder.build();
  }

  public void markEventProcessed(long eventId) {
    Uri uri = getEventUri(eventId);
    ContentValues values = new ContentValues(1);
    values.put(EventLogColumns.COLUMN_PROCESSED, true);
    contentResolver.update(uri, values , null, null);
    Log.d(TAG, "Marked event " + eventId + " as processed");
  }

  public void deleteEvent(long eventId) {
    Uri uri = getEventUri(eventId);
    contentResolver.delete(uri, null, null);
    Log.d(TAG, "Deleted event" + eventId);
  }

  private Uri getEventUri(long eventId) {
    return Uri.withAppendedPath(EventLogColumns.URI, Long.toString(eventId));
  }
}
