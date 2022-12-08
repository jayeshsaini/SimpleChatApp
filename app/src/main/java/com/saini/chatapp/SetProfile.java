package com.saini.chatapp;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SetProfile extends AppCompatActivity {

    private CardView cvGetUserImage;
    private ImageView ivGetUserImage;
    private static int PICK_IMAGE = 100;
    private Uri imagePath;

    private EditText etGetUserName;
    private android.widget.Button btnSaveProfile;

    private FirebaseAuth firebaseAuth;
    private String name;

    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    private FirebaseFirestore firebaseFirestore;

    private String ImageUriAccessToken;
    ProgressBar progressBarSetProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        etGetUserName = findViewById(R.id.getusername);
        cvGetUserImage = findViewById(R.id.getuserimage);
        ivGetUserImage = findViewById(R.id.getuserimageinimageview);
        btnSaveProfile = findViewById(R.id.saveProfile);
        progressBarSetProfile = findViewById(R.id.progressbarofsetProfile);

        cvGetUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE);
            }
        });

        btnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = etGetUserName.getText().toString();
                if(name.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Name is Empty", Toast.LENGTH_SHORT).show();
                }
                else if(imagePath == null) {
                    Toast.makeText(getApplicationContext(), "Image is Empty", Toast.LENGTH_SHORT).show();
                }
                else {
                    progressBarSetProfile.setVisibility(View.VISIBLE);
                    sendDataForNewUser();
                    progressBarSetProfile.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(SetProfile.this, ChatActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });


    }

    private void sendDataForNewUser() {
        sendDataToRealTimeDatabase();
    }

    private void sendDataToRealTimeDatabase() {
        name = etGetUserName.getText().toString().trim();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference(firebaseAuth.getUid());

        UserProfile userProfile = new UserProfile(name, firebaseAuth.getUid());
        databaseReference.setValue(userProfile);
        Toast.makeText(getApplicationContext(), "User Profile Added Successfully", Toast.LENGTH_SHORT).show();
        sendImagetoStorage();
    }

    private void sendImagetoStorage() {
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
                        ImageUriAccessToken = uri.toString();
                        Toast.makeText(getApplicationContext(), "URI get success", Toast.LENGTH_SHORT).show();
                        sendDataToCloudFirestore();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "URI get failed", Toast.LENGTH_SHORT).show();
                    }
                });

                Toast.makeText(getApplicationContext(), "Image is uploaded", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Image not uploaded", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void sendDataToCloudFirestore() {
        DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("image", ImageUriAccessToken);
        userData.put("uid", firebaseAuth.getUid());
        userData.put("status", "Online");

        documentReference.set(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "Data on Cloud Firestore send success", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            imagePath = data.getData();
            ivGetUserImage.setImageURI(imagePath);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


}