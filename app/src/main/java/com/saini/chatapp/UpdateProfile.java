package com.saini.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UpdateProfile extends AppCompatActivity {

    private EditText mnewusername;
    private ImageView mgetnewuserimageinimageview;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    private String ImageURIAccessToken;

    private androidx.appcompat.widget.Toolbar mtoolbarofupdateprofile;
    private ImageView mbackbuttonofupdateprofile;

    ProgressBar updateProfilePB;

    private Uri imagePath;
    Intent intent;

    String newName;
    android.widget.Button updateProfileBtn;
    private static int PICK_IMAGE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        mtoolbarofupdateprofile = findViewById(R.id.toolbarOfUpdateProfile);
        mbackbuttonofupdateprofile = findViewById(R.id.backButtonOfUpdateProfile);
        mgetnewuserimageinimageview = findViewById(R.id.getNewUserimageinimageview);
        updateProfilePB = findViewById(R.id.updateProfilePB);
        mnewusername = findViewById(R.id.getNewUsername);
        updateProfileBtn = findViewById(R.id.updateProfileButton);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        intent = getIntent();

        setSupportActionBar(mtoolbarofupdateprofile);

        mbackbuttonofupdateprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mnewusername.setText(intent.getStringExtra("nameofuser"));

        DatabaseReference databaseReference = firebaseDatabase.getReference(firebaseAuth.getUid());

        updateProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newName = mnewusername.getText().toString();
                if(newName.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Name is Empty", Toast.LENGTH_SHORT).show();
                }
                else if(imagePath != null) {
                    updateProfilePB.setVisibility(view.VISIBLE);
                    UserProfile userProfile = new UserProfile(newName, firebaseAuth.getUid());
                    databaseReference.setValue(userProfile);

                    updateImageToStorage();

                    Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
                    updateProfilePB.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(UpdateProfile.this, ChatActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    updateProfilePB.setVisibility(view.VISIBLE);
                    UserProfile userProfile = new UserProfile(newName, firebaseAuth.getUid());
                    databaseReference.setValue(userProfile);

                    updateNameOnCloudFirestore();

                    Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
                    updateProfilePB.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(UpdateProfile.this, ChatActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        mgetnewuserimageinimageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE);
            }
        });

        storageReference = firebaseStorage.getReference();
        storageReference.child("Images").child(firebaseAuth.getUid()).child("Profile Pic").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                ImageURIAccessToken = uri.toString();
                Picasso.get().load(uri).into(mgetnewuserimageinimageview);
            }
        });

    }

    private void updateNameOnCloudFirestore() {
        DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", newName);
        userData.put("image", ImageURIAccessToken);
        userData.put("uid", firebaseAuth.getUid());
        userData.put("status", "Online");

        documentReference.set(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateImageToStorage() {

        StorageReference imageRef = storageReference.child("Images").child(firebaseAuth.getUid()).child("Profile Pic");

        // Image Compression
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        //putting image to storage

        UploadTask uploadTask = imageRef.putBytes(data);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        ImageURIAccessToken = uri.toString();
                        Toast.makeText(getApplicationContext(), "URI get success", Toast.LENGTH_SHORT).show();
                        updateNameOnCloudFirestore();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "URI get failed", Toast.LENGTH_SHORT).show();
                    }
                });

                Toast.makeText(getApplicationContext(), "Image is updated", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Image not updated", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            imagePath = data.getData();
            mgetnewuserimageinimageview.setImageURI(imagePath);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        documentReference.update("status","Offline").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "Now User is Offline", Toast.LENGTH_SHORT).show();
            }
        });
    }
}