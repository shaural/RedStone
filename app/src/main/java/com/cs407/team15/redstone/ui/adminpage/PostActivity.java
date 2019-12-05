package com.cs407.team15.redstone.ui.adminpage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cs407.team15.redstone.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private Spinner spinner;
    private ArrayAdapter spinnerAdapter;

    private Uri mImageUri;
    String miUrlOk = "";
    private StorageTask uploadTask;
    StorageReference storageRef;

    private ImageView close, image_added, image_add;
    private TextView post;
    private EditText description;
    private RelativeLayout image_add_layout;

    private String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        close = findViewById(R.id.close);
        image_added = findViewById(R.id.image_added);
        image_add = findViewById(R.id.image_add);
        post = findViewById(R.id.post);
        description = findViewById(R.id.description);
        image_add_layout = findViewById(R.id.image_add_layout);


        storageRef = FirebaseStorage.getInstance().getReference("posts");

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PostActivity.this, adminActivity.class));
                finish();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadPost();
            }
        });

        image_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .start(PostActivity.this);
            }
        });

        spinner = (Spinner)findViewById(R.id.category_spinner);

        final ArrayList<String> list = new ArrayList<>();
        list.add("Feature");
        list.add("Ads");
        list.add("Other");

        //using ArrayAdapter
        spinnerAdapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, list);
        spinner.setAdapter(spinnerAdapter);

        //event listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(),"Clicked : "+spinner.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
                category = spinner.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadPost(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Posting");
        pd.show();
        if (mImageUri != null) {
            final StorageReference fileReference = storageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));

            uploadTask = fileReference.putFile(mImageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        miUrlOk = downloadUri.toString();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("AdminPost");

                        String postid = reference.push().getKey();
                        Long tsLong = System.currentTimeMillis()/1000; // Timestamp
                        String ts = tsLong.toString();

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("postid", postid);
                        hashMap.put("postimage", miUrlOk);
                        hashMap.put("description", description.getText().toString());
                        hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        hashMap.put("publisherid", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                        hashMap.put("category", category);
                        hashMap.put("timestamp", ts);

                        reference.child(postid).setValue(hashMap);

                        pd.dismiss();

                        startActivity(new Intent(PostActivity.this, adminActivity.class));
                        finish();

                    } else {
                        Toast.makeText(PostActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(PostActivity.this, "No image selected", Toast.LENGTH_SHORT).show();

            if (category.equals("Notification")) {


            } else {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("AdminPost");

                String postid = reference.push().getKey();
                Long tsLong = System.currentTimeMillis()/1000; // Timestamp
                String ts = tsLong.toString();

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("postid", postid);
                hashMap.put("postimage", "");
                hashMap.put("description", description.getText().toString());
                hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());
                hashMap.put("publisherid", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                hashMap.put("category", category);
                hashMap.put("timestamp", ts);

                reference.child(postid).setValue(hashMap);
            }

            pd.dismiss();

            startActivity(new Intent(PostActivity.this, adminActivity.class));
            finish();
        }
    }

    private void addNotification(String userid, String postid, String text){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);

        Long tsLong = System.currentTimeMillis()/1000; // Timestamp
        String ts = tsLong.toString();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("postid", postid);
        hashMap.put("postimage", "");
        hashMap.put("description", text);
        hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());
        hashMap.put("publisherid", FirebaseAuth.getInstance().getCurrentUser().getEmail());
        hashMap.put("category", "Notification");
        hashMap.put("timestamp", ts);

        reference.push().setValue(hashMap);
    }

//    private void deleteNotifications(final String postid, String userid){
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);
//        reference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
//                    if (snapshot.child("postid").getValue().equals(postid)){
//                        snapshot.getRef().removeValue()
//                                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        Toast.makeText(mContext, "Deleted!", Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            mImageUri = result.getUri();

            image_added.setImageURI(mImageUri);
            image_add_layout.setVisibility(View.GONE);

        } else {
            Toast.makeText(this, "Something gone wrong!", Toast.LENGTH_SHORT).show();
            image_add_layout.setVisibility(View.VISIBLE);
            startActivity(new Intent(PostActivity.this, adminActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(PostActivity.this, adminActivity.class));
        finish();
    }
}
