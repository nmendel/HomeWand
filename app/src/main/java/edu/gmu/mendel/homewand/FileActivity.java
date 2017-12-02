package edu.gmu.mendel.homewand;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

// View a file on screen
public class FileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("3","start_activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);

        TextView mTextField = findViewById(R.id.fileView);
        String fileData = getIntent().getExtras().getString(HomeWandActivity.FILE_DATA_EXTRA);
        mTextField.setText(fileData);
    }


}
