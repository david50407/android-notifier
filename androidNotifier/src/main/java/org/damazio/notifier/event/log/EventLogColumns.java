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

import android.net.Uri;
import android.provider.BaseColumns;

public interface EventLogColumns extends BaseColumns {

  public static final Uri URI = Uri.parse("content://org.damazio.notifier.eventlog/events");
  public static final String URI_AUTHORITY = "org.damazio.notifier.eventlog";
  public static final String TABLE_TYPE = "vnd.android.cursor.dir/vnd.damazio.event";
  public static final String ITEM_TYPE = "vnd.android.cursor.item/vnd.damazio.event";
  public static final String TABLE_NAME = "events";

  public static final String COLUMN_TIMESTAMP = "timestamp";
  public static final String COLUMN_SOURCE_DEVICE_ID = "source_device_id";
  public static final String COLUMN_PROCESSED = "processed";
  public static final String COLUMN_EVENT_TYPE = "event_type";
  public static final String COLUMN_PAYLOAD = "payload";

}
