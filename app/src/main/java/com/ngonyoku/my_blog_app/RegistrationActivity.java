package com.ngonyoku.my_blog_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;

public class RegistrationActivity extends AppCompatActivity {
    private static final String TAG = "RegistrationActivity";
    //Firebase
    private FirebaseAuth mAuth;

    //Views
    private TextInputLayout mEmail, mPassword, mConfirmPassword;
    private Button mRegisterUserBtn;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();

        mEmail = findViewById(R.id.reg_email);
        mPassword = findViewById(R.id.reg_password);
        mConfirmPassword = findViewById(R.id.reg_confirm_password);
        mRegisterUserBtn = findViewById(R.id.reg_registerUser_btn);
        mProgressBar = findViewById(R.id.reg_progressBar);

        mRegisterUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();
                String confirmPassword = mConfirmPassword.getEditText().getText().toString();
                if (!email.isEmpty()) {
                    mEmail.setError(null);
                    if (!password.isEmpty()) { /*Confirm Email is not empty*/
                        mPassword.setError(null);
                        if (password.length() >= 8) {/* Check password Length*/
                            mPassword.setError(null);
                            if (!confirmPassword.isEmpty()) {
                                mConfirmPassword.setError(null);
                                if (password.equals(confirmPassword)) {
                                    mConfirmPassword.setError(null);

                                    mProgressBar.setVisibility(View.VISIBLE);
                                    registerNewUser(email, password); /*Register User*/
                                } else {
                                    mConfirmPassword.setError("Password don't match");
                                    mConfirmPassword.requestFocus();
                                }
                            } else {
                                mConfirmPassword.setError("Please Confirm your password");
                                mConfirmPassword.requestFocus();
                            }
                        } else {
                            mPassword.setError("Password must have at least 8 characters");
                            mPassword.requestFocus();
                        }
                    } else {
                        mPassword.setError("Enter a password");
                        mPassword.requestFocus();
                    }
                } else {
                    mEmail.setError("Email Required");
                    mEmail.requestFocus();
                }
            }
        });
    }

    private void registerNewUser(String email, String password) {
        /*Register User */
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            startActivity(
                                    new Intent(RegistrationActivity.this, ProfileSetUpActivity.class)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            );
                            finish();

                        } else {
                            mProgressBar.setVisibility(View.GONE);
                            Toast.makeText(RegistrationActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
        ;
    }
}