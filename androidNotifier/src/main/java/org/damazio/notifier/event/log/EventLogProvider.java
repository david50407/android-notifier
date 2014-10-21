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

import java.util.Arrays;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * Content provider for the event log.
 *
 * @author Rodrigo Damazio
 */
public class EventLogProvider extends ContentProvider {

  /** If at least this many rows are deleted, we'll vacuum the database. */
  private static final int VACUUM_DELETION_TRESHOLD = 100;
  
  private static final int MATCH_DIR = 1;
  private static final int MATCH_ITEM = 2;

  private LogDbHelper dbHelper;
  private SQLiteDatabase db;

  private UriMatcher uriMatcher;

  private ContentResolver contentResolver;

  public EventLogProvider() {
    this.uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    uriMatcher.addURI(EventLogColumns.URI_AUTHORITY, "events", MATCH_DIR);
    uriMatcher.addURI(EventLogColumns.URI_AUTHORITY, "events/#", MATCH_ITEM);
  }

  @Override
  public boolean onCreate() {
    this.contentResolver = getContext().getContentResolver();
    this.dbHelper = new LogDbHelper(getContext());
    this.db = dbHelper.getWritableDatabase();
    return db != null;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    // TODO: Extract
    if (matchUriOrThrow(uri) == MATCH_ITEM) {
      String id = uri.getLastPathSegment();
      if (selection != null) {
        selection = "(" + selection + ") AND _id = ?";
      } else {
        selection = "_id = ?";
      }
      if (selectionArgs != null) {
        selectionArgs = Arrays.copyOf(selectionArgs, selectionArgs.length + 1);
      } else {
        selectionArgs = new String[1];
      }
      selectionArgs[selectionArgs.length - 1] = id;
    }

    int rows = db.delete(EventLogColumns.TABLE_NAME, selection, selectionArgs);

    contentResolver.notifyChange(uri, null);

    if (rows > VACUUM_DELETION_TRESHOLD) {
      Log.i(TAG, "Vacuuming the database");
      db.execSQL("VACUUM");
    }

    return rows;
  }
  @Override
  public String getType(Uri uri) {
    switch (uriMatcher.match(uri)) {
      case MATCH_DIR:
        return EventLogColumns.TABLE_TYPE;
      case MATCH_ITEM:
        return EventLogColumns.ITEM_TYPE;
      default:
        throw new IllegalArgumentException("Invalid URI type for '" + uri + "'.");
    }
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    if (matchUriOrThrow(uri) != MATCH_DIR) {
      throw new IllegalStateException("Cannot insert inside an item");
    }

    long rowId = db.insertOrThrow(EventLogColumns.TABLE_NAME, null, values);
    Uri insertedUri = ContentUris.withAppendedId(uri, rowId);
    contentResolver.notifyChange(insertedUri, null);
    return insertedUri;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
      String sortOrder) {
    if (matchUriOrThrow(uri) == MATCH_ITEM) {
      String id = uri.getLastPathSegment();
      if (selection != null) {
        selection = "(" + selection + ") AND _id = ?";
      } else {
        selection = "_id = ?";
      }
      if (selectionArgs != null) {
        selectionArgs = Arrays.copyOf(selectionArgs, selectionArgs.length + 1);
      } else {
        selectionArgs = new String[1];
      }
      selectionArgs[selectionArgs.length - 1] = id;
    }

    Cursor cursor = db.query(EventLogColumns.TABLE_NAME, projection, selection, selectionArgs,
        null, null, sortOrder);
    cursor.setNotificationUri(contentResolver, uri);
    return cursor;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    if (matchUriOrThrow(uri) == MATCH_ITEM) {
      String id = uri.getLastPathSegment();
      if (selection != null) {
        selection = "(" + selection + ") AND _id = ?";
      } else {
        selection = "_id = ?";
      }
      if (selectionArgs != null) {
        selectionArgs = Arrays.copyOf(selectionArgs, selectionArgs.length + 1);
      } else {
        selectionArgs = new String[1];
      }
      selectionArgs[selectionArgs.length - 1] = id;
    }

    int numRows = db.update(EventLogColumns.TABLE_NAME, values, selection, selectionArgs);

    contentResolver.notifyChange(uri, null);

    return numRows;
  }


  private int matchUriOrThrow(Uri uri) {
    int match = uriMatcher.match(uri);
    if (match == UriMatcher.NO_MATCH) {
      throw new IllegalArgumentException("Invalid URI type for '" + uri + "'.");
    }
    return match;
  }
}
