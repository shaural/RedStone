package com.cs407.team15.redstone.ui.post;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.cs407.team15.redstone.ui.adminpage.adminActivity;
import com.cs407.team15.redstone.ui.publicboard.PublicBoardActivity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

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

    private String path, location;

    private String category;

    private Context mContext;

    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mContext = PostActivity.this;

        close = findViewById(R.id.close);
        image_added = findViewById(R.id.image_added);
        image_add = findViewById(R.id.image_add);
        post = findViewById(R.id.post);
        description = findViewById(R.id.description);
        image_add_layout = findViewById(R.id.image_add_layout);

        Intent intent = getIntent();
        path = intent.getStringExtra("path");
        location = intent.getStringExtra("area"); // Location
        
        if (path.equals("admin")) {
            reference = FirebaseDatabase.getInstance().getReference(path);
            storageRef = FirebaseStorage.getInstance().getReference(path);
        } else if (path.equals("public")) {
            reference = FirebaseDatabase.getInstance().getReference(path).child(location);
            storageRef = FirebaseStorage.getInstance().getReference(path).child(location);
        }


        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
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

        if (path.equals("admin")) {
            list.add("Features");
            list.add("Ads");
            list.add("Other");
        } else if (path.equals("public")){
            list.add("Post");
            list.add("Ads");
        }

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

                        goBack();

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

            goBack();
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

            goBack();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goBack();
    }

    private void goBack() {
        Intent intent;
        if (path.equals("admin")) {
            intent = new Intent (mContext, adminActivity.class);
            intent.putExtra("area", location);
            intent.putExtra("path", "admin");
            startActivity(intent);
        } else if (path.equals("public")) {
            intent = new Intent (mContext, PublicBoardActivity.class);
            intent.putExtra("area", location);
            intent.putExtra("path", "public");
            startActivity(intent);
        }
        finish();
    }
}
