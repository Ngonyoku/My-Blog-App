package com.ngonyoku.my_blog_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileSetUpActivity extends AppCompatActivity {
    public static final int READ_EXTERNAL_STORAGE_PERMISSION = 15;

    private StorageReference mProfileStorageRef;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore mFirebaseFirestore;

    private CircleImageView mProfileImage;
    private TextInputLayout mProfileUsername;

    private String mUser_name;
    private ProgressBar mProgressBar;
    private Button mSaveProfileBtn;

    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_set_up);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mProfileStorageRef = FirebaseStorage.getInstance().getReference();
        mFirebaseFirestore = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.profile_toolbar);
        mProfileImage = findViewById(R.id.profile_image);
        mProfileUsername = findViewById(R.id.profile_username);
        mProgressBar = findViewById(R.id.profile_progressBar);
        mSaveProfileBtn = findViewById(R.id.profile_save_btn);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.profile_set_up);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.profile_image_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(ProfileSetUpActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        getCroppedImageUri();
                    } else {
                        ActivityCompat.requestPermissions(ProfileSetUpActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION);
                        Toast.makeText(ProfileSetUpActivity.this, "Please Grant permission", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    getCroppedImageUri();
                }
            }
        });

        mSaveProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserProfileInfo();
            }
        });

        loadUserData();
    }

    private void loadUserData() {
        mSaveProfileBtn.setEnabled(false);
        mProgressBar.setVisibility(View.VISIBLE);

        /*Retrieve User Information from the database*/
        mFirebaseFirestore
                .collection(getString(R.string.collection_Users))
                .document(mUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                String username = task.getResult().getString(getString(R.string.field_username));
                                final String image = task.getResult().getString(getString(R.string.field_profile_image_url));

                                mProfileUsername.getEditText().setText(username);
                                Picasso
                                        .get()
                                        .load(image)
                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                        .fit()
                                        .centerCrop()
                                        .into(mProfileImage, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError(Exception e) {
                                                Picasso.get().load(image).fit().centerCrop().into(mProfileImage);
                                            }
                                        })
                                ;
                            }
                            mSaveProfileBtn.setEnabled(true);
                            mProgressBar.setVisibility(View.GONE);
                        } else {
                            mProgressBar.setVisibility(View.GONE);
                            mSaveProfileBtn.setEnabled(true);
                            Toast.makeText(ProfileSetUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
        ;
    }

    /*We get the Cropped Image*/
    private void getCroppedImageUri() {
        CropImage
                .activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(ProfileSetUpActivity.this)
        ;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                Picasso.get().load(mImageUri).into(mProfileImage);
            }

        }
    }

    private void saveUserProfileInfo() {
        mUser_name = mProfileUsername.getEditText().getText().toString().trim();
        Uri imageUrl = (mImageUri != null) ? mImageUri : Uri.parse("");
        if (!mUser_name.isEmpty()) {
            mProfileUsername.setError(null);
            mProgressBar.setVisibility(View.VISIBLE);
            final HashMap<String, String> user_map = new HashMap<>();
            user_map.put(getString(R.string.field_username), mUser_name);
            if (imageUrl != Uri.parse("")) {
                /*Add Image to Storage and add the data to FireStore*/
                final StorageReference imagePath = mProfileStorageRef.child(getString(R.string.storage_node_profile_images) + "/" + mUser.getUid() + ".jpg");
                UploadTask uploadTask = imagePath.putFile(imageUrl);
                Task<Uri> urlTask = uploadTask
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
                                    user_map.put(getString(R.string.field_profile_image_url), task.getResult().toString());
                                    saveProfileToFireStore(user_map); /*Add Image to FireStore*/
                                } else {
                                    mProgressBar.setVisibility(View.GONE);
                                    Toast.makeText(ProfileSetUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                saveProfileToFireStore(user_map);
            }
        } else {
            mProgressBar.setVisibility(View.GONE);
            mProfileUsername.setError("Username is Required");
            mProfileUsername.requestFocus();
        }
    }

    /*Add Data to FireStore*/
    private void saveProfileToFireStore(Object user_map) {
        mFirebaseFirestore
                .collection(getString(R.string.collection_Users))
                .document(mUser.getUid())
                .set(user_map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mProgressBar.setVisibility(View.GONE);
                            Toast.makeText(ProfileSetUpActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(ProfileSetUpActivity.this, MainActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            finish();
                        } else {
                            mProgressBar.setVisibility(View.GONE);
                            Toast.makeText(ProfileSetUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
        ;
    }
}
