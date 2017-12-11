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
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_wand);
        View view = findViewById(R.id.textView);
        view.setBackgroundColor(Color.LTGRAY);

        excludedFolders.add(SIGNATURE_FILE);
        excludedFolders.add("instant-run");
        excludedFolders.add("abc");
    }

    /**
     * Start Capture button, run CaptureActivity
     */
    public void startCaptureActivity(View view) {
        Log.i("1","Pressed start capture");
        Intent intent = new Intent(this, CaptureActivity.class);
        startActivity(intent);
    }

    /**
     * Motion button, run MotionActivity
     */
    public void startMotionActivity(View view) {
        Log.i("1","Pressed motion");
        Intent intent = new Intent(this, MotionActivity.class);
        startActivity(intent);
    }

    /**
     * View Data button, run FileActivity
     */
    public void openFile(View view) {
        Log.i("1","Pressed Open");
        performFileSearch();
    }

    /**
     * Delete Data button, show file explorer and delete the selected file
     */
    public void deleteFile(View view) {
        Log.i("1","Pressed Delete");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        startActivityForResult(intent, DELETE_REQUEST_CODE);
    }

    /**
     * Calculate Motions button, if user is sure, call doCalculateMotions
     */
    public void calculateMotions(View view) {
        Log.i("1","Pressed Calculate Motions");

        // Are you sure popup
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Recalculate Motions")
                .setMessage("Are you sure you want to recalculate HomeWand motions?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doCalculateMotions();
                    }

                })
                .setNegativeButton("No", null)
                .show();

    }

    /**
     * Loop through all sensor activity files in all motion folders
     * Create motion objects for each accelerometer/gyroscope file pair
     * Write an ARFF file for the dataset with each motion object as a row
     */
    public void doCalculateMotions() {
        Log.i("2","Calculate Motions");
        List<String> motionNames = new ArrayList<String>();
        List<Motion> allMotions = new ArrayList<Motion>();
        File[] files = getFilesDir().listFiles();

        // Loop through all non-excluded motion directories
        Log.i("Files", "Size: "+ files.length);
        for (int i = 0; i < files.length; i++)
        {
            if(!excludedFolders.contains(files[i].getName())) {
                // add all motion objects created from the files in the folder
                allMotions.addAll(handleMotion(files[i]));
                motionNames.add(files[i].getName());
            }
        }

        // Create the dataset
        Instances dataset = Motion.getDataset("HomeWand", motionNames);

        // Add all instances to the dataset
        for(Motion motion : allMotions) {
            Instance inst = motion.getInstance();
            dataset.add(inst);
        }

        //Log.d("file_output", motions.toString());

        // Write the ARFF file to disk
        Log.d("ARFF", dataset.toString());
        writeSignatureFile(getFilesDir(), dataset);
    }


    /**
     * Loop through the files in the folder for one motion
     * Create Motion objects for each accelerometer/gyroscope file pair
     */
    public Collection<Motion> handleMotion(File dir) {
        Map<String, Motion> motions = new HashMap<String, Motion>();
        File[] motionFiles = dir.listFiles();


        for (int i = 0; i < motionFiles.length; i++) {
            // Get file timestamp
            String[] fileParts = motionFiles[i].getName().replace(".csv", "").split("-");
            String fileTime = fileParts[0];
            String fileType = fileParts[1];
            Log.i("Files", "FileTime:" + fileTime + ", fileType: " + fileType);

            // See if we've already created a Motion for this timestamp (group file pairs)
            Motion motion = motions.get(fileTime);

            // Read in the file
            Uri uri = Uri.fromFile(motionFiles[i]);
            String fileData = readTextFromUri(uri);

            // calculate features for each file
            if(motion == null) {
                motion = new Motion(dir.getName(), fileTime);
            }
            if(fileData != null && fileData.length() > 0) {
                motion.addData(fileType, fileData);
            }
            motions.put(fileTime, motion);
        }

        return motions.values();
    }

    /**
     * Write the dataset to disk as an ARFF file
     */
    public void writeSignatureFile(File dir, Instances dataset) {
        BufferedWriter signatureFileStream = null;
        File signatureFile = new File(dir, SIGNATURE_FILE);

        try {
            signatureFileStream = new BufferedWriter(new FileWriter(signatureFile));
            signatureFileStream.write(dataset.toString());
            signatureFileStream.close();
        } catch (IOException e) {
            Log.e("signature_file_error", "Failed to write signature file" + signatureFile.getName(), e);
        }
    }


    // Document Provider code below adapted from:
    // https://developer.android.com/guide/topics/providers/document-provider.html
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