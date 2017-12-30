package ateam.com.aarti;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import me.gujun.android.taggroup.TagGroup;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static int MEDIA_REQUEST_CODE = 1;
    private Button openMedia;
    private Button submitData;
    private TagGroup aartiName;
    private TagGroup devtaName;
    private EditText composerName; // optional
    private EditText mediaName;
    private Spinner spinnerDays;
    private Uri path;

    private Uri uri;
    private ProgressDialog progressBar;
    private DatabaseReference mFirebaseDatabseRef;
    private StorageReference mStorageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        openMedia = findViewById(R.id.findMedia); //button
        submitData = findViewById(R.id.submit);  //button

        aartiName = findViewById(R.id.name_tag);
        devtaName = findViewById(R.id.devta_tag);

        composerName = findViewById(R.id.composer_name);
        mediaName = findViewById(R.id.findMediaText);
        spinnerDays = findViewById(R.id.day_choose);

        openMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Open The media Database", Toast.LENGTH_SHORT).show();

                Intent intent_upload = new Intent();
                intent_upload.setType("audio/*");
                intent_upload.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent_upload, MEDIA_REQUEST_CODE);
            }
        });

        submitData.setOnClickListener(this);

        mFirebaseDatabseRef = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();

    }

    @Override
    public void onClick(View view) {

        final String key = mFirebaseDatabseRef.push().getKey();
        final String aartiNameText = getFullName(aartiName.getTags());
        final String devtaNameText = getFullName(devtaName.getTags());
        final String day = spinnerDays.getSelectedItem().toString();

        progressBar = new ProgressDialog(this);
        progressBar.show();

        StorageReference stref =
                mStorageRef.child(key).child(mediaName.getText().toString().trim());

        stref.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        path = taskSnapshot.getDownloadUrl();
                        Toast.makeText(MainActivity.this, "Path: " + path.getEncodedPath(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "onSuccess: " + String.valueOf(path));

                        Song song = new Song(key, aartiNameText, devtaNameText,
                                day, String.valueOf(path), "");

                        mFirebaseDatabseRef.child(key).setValue(song)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(MainActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, "onSuccess: Database Updated");




                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });

                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        int progress = (int) (taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        progress *= 100;
                        progressBar.setProgress(progress);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: " + e.getMessage());
                        Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.dismiss();

                    }
                });
        progressBar.dismiss();

    }

    private String getFullName(String[] names) {

        String name = "";

        for (int i = 0; i < names.length; i++)
            if (name == "")
                name = names[i];
            else
                name = name + "|" + names[i];

        return name;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == MEDIA_REQUEST_CODE) {
            uri = data.getData();

            Log.e(TAG, "onActivityResult: " + String.valueOf(uri));
            Log.e(TAG, "onActivityResult: " + getFileName(uri));

            mediaName.setText(getFileName(uri));
        } else {
            Toast.makeText(this, "Cant Done this facility", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}
