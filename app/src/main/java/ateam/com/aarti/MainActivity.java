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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import me.gujun.android.taggroup.TagGroup;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final String TABLE_AARTI="aarti";

    private static int MEDIA_REQUEST_CODE = 1;

    private TagGroup devtaName;
    private EditText composerName; // optional
    private EditText mediaName;
    private Spinner spinnerDays, spinnerDays2, spinnerAarti;
    private Uri path;
    private Uri uri;
    private ProgressDialog progressDialog;
    private DatabaseReference mFirebaseDatabseRef;
    private StorageReference mStorageRef;
    private ImageButton addNewField;
    private List<AartiBucket> list;
    private ArrayList<String> arrayList;
    private AartiBucket aartiBucket;
    private String m_Text = "";
    private EditText newEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Button openMedia = findViewById(R.id.findMedia);
        Button submitData = findViewById(R.id.submit);


        aartiBucket = new AartiBucket();

        spinnerAarti = findViewById(R.id.spinner_aarti_name);
        devtaName = findViewById(R.id.devta_tag);
        addNewField = findViewById(R.id.imageButtonAddField);

        composerName = findViewById(R.id.composer_name);
        mediaName = findViewById(R.id.findMediaText);
        spinnerDays = findViewById(R.id.day_choose);
        spinnerDays2 = findViewById(R.id.day_choose2);
        newEntry = findViewById(R.id.newentry);

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


        addItems();

        addNewField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addnewFieldName(newEntry.getText().toString());
            }
        });

        mFirebaseDatabseRef = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();

    }

    private void addnewFieldName(final String m_text) {
        FirebaseDatabase.getInstance().getReference("0").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                arrayList = new ArrayList<>();
                list = new ArrayList<>();
                String s= "";
                final int size = (int) dataSnapshot.getChildrenCount();
                Log.e(TAG, "onDataChange: " + size);

                for (DataSnapshot myData :
                        dataSnapshot.getChildren()) {
                    s = myData.getValue(String.class);
                }
                dataSnapshot.getRef().removeValue();
                /*String[] str = s.split("|");

                int len = str.length;

                for (int i=0; i<len; i++){
                       arrayList.add(str[i]);
                }
                arrayList.add(s);*/

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this,
                        android.R.layout.simple_spinner_item, arrayList);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerAarti.setAdapter(arrayAdapter);

                Map<String, Object> map = new HashMap<>();
                map.put("\"name\"",s + "," + m_text);


                /**
                 *
                 * child updating
                 */
                FirebaseDatabase.getInstance().getReference("0").updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.e(TAG, "onSuccessDataHash: ");
                        newEntry.setText("");
                        addItems();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addItems() {
        FirebaseDatabase.getInstance().getReference("0").child("\"name\"").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int size = (int) dataSnapshot.getChildrenCount();
                Log.e(TAG, "onDataChange: " + size);

                String s = null;
                s = dataSnapshot.getValue().toString();

                Log.e(TAG, "onDataSnapshotValue: "+s );

                /*for(int i=0;i < list.size(); i++){
                    Log.e(TAG, "onDataChange: "+String.valueOf(list.get(i).getNum())+ list.get(i).getAartiName());
                    arrayList.add( list.get(i).getAartiName());
                }*/
                String[] elements = s.split(",");
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this,
                        android.R.layout.simple_spinner_item,elements);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerAarti.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View view) {

        if (devtaName.getTags().length == 0 && spinnerDays.getSelectedItemPosition() == 0
                && composerName.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "complete the data first", Toast.LENGTH_SHORT).show();

        } else {
            final String key = mFirebaseDatabseRef.push().getKey();
            final String aartiNameText = spinnerAarti.getSelectedItem().toString();
            final String devtaNameText = getFullName(devtaName.getTags());
            final String aartiText = composerName.getText().toString().trim();
            final String day;
            final String[] dayPreviousValue = new String[1];
            if (spinnerDays2.getSelectedItemPosition() == 0)
                day = spinnerDays.getSelectedItem().toString();
            else
                day = spinnerDays.getSelectedItem().toString() + "," + spinnerDays2.getSelectedItem().toString();

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
                            mFirebaseDatabseRef.child(TABLE_AARTI).child(key).setValue(song)
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
                name = name + "," + names[i];

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
