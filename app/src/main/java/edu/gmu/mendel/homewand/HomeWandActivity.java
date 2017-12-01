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
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weka.core.Instance;
import weka.core.Instances;

public class HomeWandActivity extends Activity {

    public static final String FILE_DATA_EXTRA = "file_data";
    public static final String SIGNATURE_FILE = "signature.arff";
    private static final int READ_REQUEST_CODE = 42;
    private static final int DELETE_REQUEST_CODE = 43;

    private List<String> excludedFolders = new ArrayList<String>();
    public Map<String, List<Float>> signatures = new HashMap<String, List<Float>>();
    public FilenameFilter signatureFileFilter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return name.equals(SIGNATURE_FILE);
        }
    };

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

        excludedFolders.add("instant-run");

        // prepare the signatures variable from pre-calculated signature files
        //doCalculateMotions(false);
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

    public void calculateMotions(View view) {
        Log.i("1","Pressed Calculate Motions");

        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Recalculate Motions")
                .setMessage("Are you sure you want to recalculate HomeWand motions?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doCalculateMotions(true);
                    }

                })
                .setNegativeButton("No", null)
                .show();

    }

    public void doCalculateMotions(boolean overwriteExisting) {
        Log.i("2","Calculate Motions");
        File[] files = getFilesDir().listFiles();

        // Loop through all non-excluded motion directories
        Log.i("Files", "Size: "+ files.length);
        for (int i = 0; i < files.length; i++)
        {
            if(!excludedFolders.contains(files[i].getName())) {
                handleMotion(files[i], overwriteExisting);
            }

        }
    }




    public void handleMotion(File dir, boolean overwriteExisting) {
        //TODO: temporary
        overwriteExisting = true;
        List<Float> motionSignature = null;

        // check for existing signature file in each motion directory
        File[] existingSignatureFile = dir.listFiles(signatureFileFilter);

        Log.i("Files", "Directory Name: " + dir.getName() + ", Number of files: " + existingSignatureFile.length);
        if(existingSignatureFile.length > 0) {
            if(!overwriteExisting) {
                // If no overwriting, read the signature file into memory
                motionSignature = readSignatureFile(existingSignatureFile[0]);

            } else {
                // otherwise, delete and recalculate the motion signature
                existingSignatureFile[0].delete();
                motionSignature = calculateDirectorySignature(dir);
            }
        } else {
            motionSignature = calculateDirectorySignature(dir);
        }

        signatures.put(dir.getName(), motionSignature);
    }

    public List<Float> readSignatureFile(File signatureFile) {
        List<Float> motionSignature = new ArrayList<Float>();
        Uri uri = Uri.fromFile(signatureFile);
        String[] splitVals = readTextFromUri(uri).split(",");

        // read existing signature file into motionSignature variable
        for(int i = 0; i < splitVals.length; i++) {
            motionSignature.add(new Float(splitVals[i]));
        }

        return motionSignature;
    }

    public List<Float> calculateDirectorySignature(File dir) {
        List<Float> motionSignature = new ArrayList<Float>();
        Map<String, Motion> motions = new HashMap<String, Motion>();
        File[] motionFiles = dir.listFiles();
        Instances dataset = Motion.getDataset(dir.getName());


        // TODO: group gyro and accel files for each signature
        for (int i = 0; i < motionFiles.length; i++) {
            String[] fileParts = motionFiles[i].getName().replace(".csv", "").split("-");
            String fileTime = fileParts[0];
            String fileType = fileParts[1];
            Log.i("Files", "FileTime:" + fileTime + ", fileType: " + fileType);

            // calculate signature for each file
            Motion motion = motions.get(fileTime);
            Uri uri = Uri.fromFile(motionFiles[i]);
            String fileData = readTextFromUri(uri);

            if(motion == null) {
                motion = new Motion(fileTime);
            }
            if(fileData != null && fileData.length() > 0) {
                motion.addData(fileType, fileData);
            }
            motions.put(fileTime, motion);
        }

        for(Motion motion : motions.values()) {
            Instance inst = motion.getInstance();
            dataset.add(inst);
            //dataset.attribute(0).addStringValue(motionFiles[i].getName());
        }

        //Log.d("file_output", motions.toString());

        Log.d("ARFF", dataset.toString());
        writeSignatureFile(dir, dataset);

        return motionSignature;
    }

    public void writeSignatureFile(File dir, Instances dataset) {
        BufferedWriter signatureFileStream = null;
        StringBuilder builder = new StringBuilder();
        File signatureFile = new File(dir, SIGNATURE_FILE);

        try {
            signatureFileStream = new BufferedWriter(new FileWriter(signatureFile));
            builder.append(dataset.toString());
            signatureFileStream.write(builder.toString());
            signatureFileStream.close();
        } catch (IOException e) {
            Log.e("signature_file_error", "Failed to write signature file" + signatureFile.getName(), e);
        }
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
                Intent intent = new Intent(this, FileActivity.class);
                intent.putExtra(FILE_DATA_EXTRA, fileData);
                startActivity(intent);
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
                stringBuilder.append("\n");
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