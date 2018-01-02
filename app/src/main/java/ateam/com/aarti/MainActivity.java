package ateam.com.aarti;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;

import me.gujun.android.taggroup.TagGroup;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static int MEDIA_REQUEST_CODE = 1;
    private TagGroup aartiName;
    private TagGroup devtaName;
    private EditText composerName; // optional
    private EditText mediaName;
    private Spinner spinnerDays, spinnerDays2;
    private Uri path;
    private Uri uri;
    private ProgressDialog progressDialog;
    private DatabaseReference mFirebaseDatabseRef;
    private StorageReference mStorageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Button openMedia = findViewById(R.id.findMedia);
        Button submitData = findViewById(R.id.submit);

        aartiName = findViewById(R.id.name_tag);
        devtaName = findViewById(R.id.devta_tag);

        composerName = findViewById(R.id.composer_name);
        mediaName = findViewById(R.id.findMediaText);
        spinnerDays = findViewById(R.id.day_choose);
        spinnerDays2 = findViewById(R.id.day_choose2);

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

        if (aartiName.getTags().length == 0 && devtaName.getTags().length == 0 && spinnerDays.getSelectedItemPosition() == 0
                && composerName.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "complete the data first", Toast.LENGTH_SHORT).show();

        } else {
            final String key = mFirebaseDatabseRef.push().getKey();
            final String aartiNameText = getFullName(aartiName.getTags());
            final String devtaNameText = getFullName(devtaName.getTags());
            final String aartiText = composerName.getText().toString().trim();
            final String day;
            final String[] dayPreviousValue = new String[1];
            if (spinnerDays2.getSelectedItemPosition() == 0)
                day = spinnerDays.getSelectedItem().toString();
            else
                day = spinnerDays.getSelectedItem().toString() + "|" + spinnerDays2.getSelectedItem().toString();

            progressDialog = new ProgressDialog(this);
            progressDialog.show();

            StorageReference stref =
                    mStorageRef.child(key).child(mediaName.getText().toString().trim());

            /**
             * First File is uploaded
             * in the Firebase Storage
             */
            stref.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                            path = taskSnapshot.getDownloadUrl();
                            Toast.makeText(MainActivity.this, "Path: " + path.getEncodedPath(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "onSuccess: " + String.valueOf(path));

                            Song song = new Song(key, aartiNameText, devtaNameText,
                                    day, String.valueOf(path), aartiText);

                            /**
                             *
                             * @mFirebaseDatabe is creating a new entry to the corresponding song uploaded
                             */
                            mFirebaseDatabseRef.child(key).setValue(song)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(MainActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                                            Log.e(TAG, "onSuccess: Database Updated");

                                            progressDialog.dismiss();

                                            //  30/12/17 add the code for the name --> key for reverse searching

                                            if (spinnerDays.getSelectedItemPosition() == 0)
                                                Toast.makeText(MainActivity.this, "Choose Day", Toast.LENGTH_SHORT).show();
                                            else
                                                mFirebaseDatabseRef.child(spinnerDays.getSelectedItem().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                        HashMap<String, Object> map = new HashMap<>();
                                                        map.put(key, devtaNameText);

                                                        dataSnapshot.getRef().updateChildren(map).
                                                                addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Toast.makeText(MainActivity.this, "Values Updated", Toast.LENGTH_SHORT).show();
                                                                        Log.e(TAG, "onSuccess: Map Updated");

                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(MainActivity.this, "Map Failed", Toast.LENGTH_SHORT).show();
                                                                Log.e(TAG, "onFailure: Failed : " + e.getMessage(), new Throwable("Error"));
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });


                                            //call for the spinnerDays2 also
                                            if (spinnerDays2.getSelectedItemPosition() != 0)
                                                mFirebaseDatabseRef.child(spinnerDays2.getSelectedItem().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                        HashMap<String, Object> map = new HashMap<>();
                                                        map.put(key, devtaNameText);

                                                        dataSnapshot.getRef().updateChildren(map).
                                                                addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Toast.makeText(MainActivity.this, "Values Updated", Toast.LENGTH_SHORT).show();
                                                                        Log.e(TAG, "onSuccess: Map Updated");

                                                                        aartiName.setTags("");
                                                                        devtaName.setTags("");
                                                                        composerName.setText("");


                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(MainActivity.this, "Map Failed", Toast.LENGTH_SHORT).show();
                                                                Log.e(TAG, "onFailure: Failed : " + e.getMessage(), new Throwable("Error"));
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double per = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    progressDialog.setMessage((int) per + " % completed...");
                }
            });


        }

    }

    private String getFullName(String[] names) {

        String name = "";

        for (int i = 0; i < names.length; i++)
            if (Objects.equals(name, ""))
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
