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
package org.damazio.notifier.event.util;

import static org.damazio.notifier.Constants.TAG;

import org.damazio.notifier.protocol.Common.PhoneNumber;
import org.damazio.notifier.protocol.Common.PhoneNumber.Builder;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;

public class PhoneNumberUtils {
  private final Context context;

  public PhoneNumberUtils(Context context) {
    this.context = context;
  }

  public PhoneNumber resolvePhoneNumber(String number) {
    Builder numberBuilder = PhoneNumber.newBuilder();
    if (number == null) {
      return numberBuilder.build();
    }

    numberBuilder.setNumber(number);

    // Do the contact lookup by number
    Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
    Cursor cursor = null;
    try {
      cursor = context.getContentResolver().query(uri,
          new String[] { PhoneLookup.DISPLAY_NAME, PhoneLookup.TYPE, PhoneLookup.LABEL },
          null, null, null);
    } catch (IllegalArgumentException e) {
      Log.w(TAG, "Unable to look up caller ID", e);
    }

    // Take the first match only
    if (cursor != null && cursor.moveToFirst()) {
      int nameIndex = cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME);
      int typeIndex = cursor.getColumnIndex(PhoneLookup.TYPE);
      int labelIndex = cursor.getColumnIndex(PhoneLookup.LABEL);

      if (nameIndex != -1) {
        numberBuilder.setName(cursor.getString(nameIndex));

        // Get the phone type if possible
        if (typeIndex != -1) {
          int numberType = cursor.getInt(typeIndex);
          String label = "";
          if (labelIndex != -1) {
            label = cursor.getString(labelIndex);
          }

          numberBuilder.setNumberType(
              Phone.getTypeLabel(context.getResources(), numberType, label).toString());
        }
      }
    }

    return numberBuilder.build();
  }
}
