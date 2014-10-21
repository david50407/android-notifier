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
package org.damazio.notifier.event.display;

import org.damazio.notifier.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

/**
 * Simple activity which displays a pop-up.
 *
 * @author Rodrigo Damazio
 */
public class PopupDisplayActivity extends Activity {
  
  public static final String EXTRA_POPUP_TEXT = "popup_text";

  @Override
  public void onCreate(Bundle savedState) {
    Intent intent = getIntent();
    String text = intent.getStringExtra(EXTRA_POPUP_TEXT);

    AlertDialog popup = new AlertDialog.Builder(this)
        .setCancelable(true)
        .setTitle(R.string.popup_display_title)
        .setMessage(text)
        .setNeutralButton(android.R.string.ok, null)
        .create();
    popup.show();
  }
}
