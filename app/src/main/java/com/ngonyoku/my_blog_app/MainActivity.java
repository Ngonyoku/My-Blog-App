package com.ngonyoku.my_blog_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ngonyoku.my_blog_app.Fragments.AccountFragment;
import com.ngonyoku.my_blog_app.Fragments.HomeFragment;
import com.ngonyoku.my_blog_app.Fragments.NotificationsFragment;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore mFirebaseFirestore;

    private Toolbar mToolbar;
    private FloatingActionButton mPostFab;
    private BottomNavigationView mBottomNavigationView;

    private HomeFragment mHomeFragment;
    private AccountFragment mAccountFragment;
    private NotificationsFragment mNotificationsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mFirebaseFirestore = FirebaseFirestore.getInstance();

        if (mUser != null) {

            mToolbar = findViewById(R.id.main_toolbar);
            mPostFab = findViewById(R.id.main_post_fab);
            mBottomNavigationView = findViewById(R.id.main_bottonNavigationBar);

            mHomeFragment = new HomeFragment();
            mAccountFragment = new AccountFragment();
            mNotificationsFragment = new NotificationsFragment();

            replaceFragment(mHomeFragment);

            setSupportActionBar(mToolbar);
            mPostFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, NewPostActivity.class));
                }
            });

            mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.bottomNav_home:
                            replaceFragment(mHomeFragment);
                            return true;
                        case R.id.bottomNav_notifications:
                            replaceFragment(mNotificationsFragment);
                            return true;
                        case R.id.bottomNav_account:
                            replaceFragment(mAccountFragment);
                            return true;
                        default:
                            return false;
                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mUser == null) {
            goToStart();
        } else {
            mFirebaseFirestore
                    .collection(getString(R.string.collection_Users))
                    .document(mUser.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                if (!task.getResult().exists()) {
                                    startActivity(new Intent(MainActivity.this, ProfileSetUpActivity.class));
                                    Toast.makeText(MainActivity.this, "Please Set Up Your Account", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
            ;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_menu_logOut:
                mAuth.signOut();
                goToStart();
                return true;
            case R.id.main_menu_profile_set_up:
                startActivity(new Intent(this, ProfileSetUpActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*Redirects to the StartActivity*/
    private void goToStart() {
        startActivity(
                new Intent(MainActivity.this, StartActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
        );
        finish();
    }

    /*Changing the Fragments on the Main Activity*/
    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();
    }

}