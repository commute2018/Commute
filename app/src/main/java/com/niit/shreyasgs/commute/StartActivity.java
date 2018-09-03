package com.niit.shreyasgs.commute;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    //----ALL LAYOUT INSTANCES HERE------
    private Button createNewAccount;
    private Button loginToYourAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

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

            }
        });
    }
}
