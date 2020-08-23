package com.ngonyoku.my_blog_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {
    private FirebaseFirestore mFirebaseFirestore;
    private StorageReference mPostsStorageRef;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private Toolbar mToolbar;
    private ImageView mPostImage;
    private TextInputLayout mPostDescription;
    private Button mPostBtn;
    private ProgressBar mProgressBar;

    private Uri mPostImageUri;
    private Bitmap mCompressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mPostsStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        mToolbar = findViewById(R.id.newPost_toolbar);
        mPostImage = findViewById(R.id.newPost_image);
        mPostDescription = findViewById(R.id.newPost_description);
        mPostBtn = findViewById(R.id.newPost_post_btn);
        mProgressBar = findViewById(R.id.newPost_progressBar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = mPostDescription.getEditText().getText().toString();

                if (!description.isEmpty() && mPostImageUri != null) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    post(description);
                } else {
                    mPostDescription.requestFocus();
                    mPostDescription.setError("Please add a Description");
                }
            }
        });
    }

    private void post(final String description) {
        String randomName = FieldValue.serverTimestamp().toString();
        final StorageReference imagePath = mPostsStorageRef.child("Post_Images/" + System.currentTimeMillis() + ".jpg");
        UploadTask uploadTask = imagePath.putFile(mPostImageUri);
        Task<Uri> imageUrl = uploadTask
                .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        return imagePath.getDownloadUrl();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            final String downloadUrl = task.getResult().toString();

                            File newImageFile = new File(mPostImageUri.getPath());

                            try {
                                mCompressedImageFile = new Compressor(NewPostActivity.this)
                                        .setMaxHeight(100)
                                        .setMaxWidth(100)
                                        .setQuality(40)
                                        .compressToBitmap(newImageFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            /*Compress to Bitmap*/
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            mCompressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                            byte[] thumbImageData = byteArrayOutputStream.toByteArray();

                            /*Upload the thumbnail*/
                            final StorageReference thumbPath = mPostsStorageRef.child("Post_Images/thumbs/" + System.currentTimeMillis() + ".jpg");
                            UploadTask uploadThumbImage = thumbPath.putBytes(thumbImageData);
                            Task<Uri> thumbUrl = uploadThumbImage
                                    .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                        @Override
                                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                            return thumbPath.getDownloadUrl();
                                        }
                                    })
                                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            if (task.isSuccessful()) {
                                                String thumbnailUrl = task.getResult().toString();
                                                Map<String, Object> postMap = new HashMap<>();
                                                postMap.put("image_url", downloadUrl);
                                                postMap.put("thumbnail_url", thumbnailUrl);
                                                postMap.put("description", description);
                                                postMap.put("user_id", mUser.getUid());
//                                                postMap.put("timestamp", FieldValue.serverTimestamp());
                                                addPostToFireStore(postMap);
                                            } else {
                                                Toast.makeText(NewPostActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                mProgressBar.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                        } else {
                            mProgressBar.setVisibility(View.GONE);
                            Toast.makeText(NewPostActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void addPostToFireStore(Object postMap) {
        mFirebaseFirestore
                .collection(getString(R.string.collection_Posts))
                .add(postMap)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(NewPostActivity.this, "Posted", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(NewPostActivity.this, MainActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                            finish();
                        } else {
                            Toast.makeText(NewPostActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        mProgressBar.setVisibility(View.GONE);
                    }
                })
        ;
    }

    public void loadPostImage(View view) {
        CropImage
                .activity()
                .setMinCropResultSize(512, 512)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(NewPostActivity.this)
        ;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mPostImageUri = result.getUri();
                Picasso.get().load(mPostImageUri).fit().centerCrop().into(mPostImage);
            }

        }
    }
}