package com.niit.shreyasgs.commute;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    //----ALL LAYOUT INSTANCES HERE------
    private Button createNewAccount;
    private Button loginToYourAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mAuth = FirebaseAuth.getInstance();

        //------ALL INITIALIZATIONS HERE------
        createNewAccount = (Button)findViewById(R.id.start_activity_create_new_account_button);
        loginToYourAccount = (Button)findViewById(R.id.start_activity_login_button);

        //------ON CLICK FOR CREATE NEW ACCOUNT BUTTON-----
        createNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(StartActivity.this , RegisterActivity.class);
                startActivity(registerIntent);
            }
        });

        //------ON CLICK FOR LOGIN TO YOUR ACCOUNT-----
        loginToYourAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(StartActivity.this , LoginActivity.class);
                startActivity(loginIntent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent mapIntent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(mapIntent);
            finish();
        }
    }
}
