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

import java.io.InputStream;
import java.util.Scanner;

import org.damazio.notifier.R;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LogDbHelper extends SQLiteOpenHelper {

  private static final int CURRENT_DB_VERSION = 1;
  private static final String DB_NAME = "eventlog";
  private final Context context;

  public LogDbHelper(Context context) {
    super(context, DB_NAME, null, CURRENT_DB_VERSION);

    this.context = context;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    InputStream schemaStream = context.getResources().openRawResource(R.raw.logdb_schema);
    Scanner schemaScanner = new Scanner(schemaStream, "UTF-8");
    schemaScanner.useDelimiter(";");

    Log.i(TAG, "Creating database");
    try {
      db.beginTransaction();
      while (schemaScanner.hasNext()) {
        String statement = schemaScanner.next();
        Log.d(TAG, "Creating database: " + statement);
        db.execSQL(statement);
      }
      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // No upgrade for now.
  }
}
