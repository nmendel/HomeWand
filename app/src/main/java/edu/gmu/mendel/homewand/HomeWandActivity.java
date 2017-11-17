/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.gmu.mendel.homewand;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HomeWandActivity extends Activity {

    public static final String SENSOR_VALS = "sensor_vals";
    public static final String INTENT_DELETE_FLAG = "delete";
    private static final int READ_REQUEST_CODE = 42;
    private static final int DELETE_REQUEST_CODE = 43;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_wand);
        View view = findViewById(R.id.textView);
        view.setBackgroundColor(Color.LTGRAY);
    }

    /** Called when the user taps the Start Capture button */
    public void startCaptureActivity(View view) {
        Log.i("1","Pressed start capture");
        Intent intent = new Intent(this, CaptureActivity.class);
        startActivity(intent);
    }

    /** Called when the user taps the Motion button */
    public void startMotionActivity(View view) {
        Log.i("1","Pressed motion");
        Intent intent = new Intent(this, MotionActivity.class);
        startActivity(intent);
    }

    /** Called when the user taps the View Data button */
    public void openFile(View view) {
        Log.i("1","Pressed Open");
        performFileSearch();
    }

    /** Called when the user taps the Delete Data button */
    public void deleteFile(View view) {
        Log.i("1","Pressed Delete");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        startActivityForResult(intent, DELETE_REQUEST_CODE);
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);


        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("*/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        Uri uri = null;
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            if (resultData != null) {
                uri = resultData.getData();
                Log.i("file_location", "Uri: " + uri.toString());

                String fileData = readTextFromUri(uri);
                Log.i("file_data", fileData);
            }
        } else if (requestCode == DELETE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            if (resultData != null) {
                uri = resultData.getData();
                Log.i("file_location", "Uri: " + uri.toString());

                // Delete file
                try {
                    DocumentsContract.deleteDocument(getContentResolver(), uri);
                } catch (FileNotFoundException e) {
                    Log.e("failed_delete", "Uri: " + uri.toString());
                }
            }
        }
    }

    private String readTextFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }

            reader.close();
            inputStream.close();

            return stringBuilder.toString();
        } catch(IOException e) {
            Log.e("failed", "failed to open file", e);
            return "";
        }
    }
}