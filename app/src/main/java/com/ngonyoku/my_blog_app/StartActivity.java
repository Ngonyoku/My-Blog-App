package com.ngonyoku.my_blog_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }

    public void openLogInActivity(View view) {
        startActivity(new Intent(this, LogInActivity.class));
    }

    public void openRegistrationActivity(View view) {
        startActivity(new Intent(this, RegistrationActivity.class));
    }
}