package com.ngonyoku.my_blog_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

public class LogInActivity extends AppCompatActivity {
    private static final String TAG = "LogInActivity";
    //Firebase
    private FirebaseAuth mAuth;
    //Views
    private TextInputLayout mEmail, mPassword;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        mAuth = FirebaseAuth.getInstance();

        mEmail = findViewById(R.id.login_email);
        mPassword = findViewById(R.id.login_password);
        mProgressBar = findViewById(R.id.login_progressBar);
    }

    private void logInUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mProgressBar.setVisibility(View.GONE);
                            startActivity(
                                    new Intent(LogInActivity.this, MainActivity.class)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            );
                            Toast.makeText(LogInActivity.this, "Welcome Back", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            mProgressBar.setVisibility(View.GONE);
                            Toast.makeText(LogInActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
        ;
    }

    public void signInUser(View view) {
        String email = mEmail.getEditText().getText().toString();
        String password = mPassword.getEditText().getText().toString();
        if (!email.isEmpty()) {
            mEmail.setError(null);
            if (!password.isEmpty()) {
                mPassword.setError(null);
                mProgressBar.setVisibility(View.VISIBLE);
                logInUser(email, password);
            } else {
                mPassword.requestFocus();
                mPassword.setError("Please enter your Password");
            }
        } else {
            mEmail.requestFocus();
            mEmail.setError("Please enter your Email Address");
        }
    }
}