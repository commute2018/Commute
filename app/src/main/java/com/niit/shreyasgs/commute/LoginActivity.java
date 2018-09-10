package com.niit.shreyasgs.commute;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    //-----ALL FIREBASE INSTANCES HERE-------
    private FirebaseAuth loginAuth;

    //------ALL LAYOUT INSTANCES HERE--------
    private EditText emailIDInput;
    private EditText passwordInput;
    private Button loginButton;
    private Toolbar loginPageToolbar;
    private ProgressDialog loginDialog;

    //-------ALL VARIABLES HERE---------
    String emailID, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //------ALL FIREBASE DECLARATIONS HERE--------
        loginAuth = FirebaseAuth.getInstance();

        //------ ALL LAYOUT DECLARATIONS HERE--------
        emailIDInput = (EditText)findViewById(R.id.login_activity_emailid);
        passwordInput = (EditText)findViewById(R.id.login_activity_password);
        loginButton = (Button)findViewById(R.id.login_activity_login_button);
        loginPageToolbar = (Toolbar)findViewById(R.id.login_activity_toolbar);
        loginDialog = new ProgressDialog(this);

        //-------Toolbar changes to be made here--------
        setSupportActionBar(loginPageToolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //-------Progress dialog changes-----------
        loginDialog.setTitle("Logging in ");
        loginDialog.setMessage("Please wait while we check your credentials");
        loginDialog.setCanceledOnTouchOutside(false);

        //-------LOGIN BUTTON ON CLICK-----------
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginDialog.show();
                emailID = emailIDInput.getText().toString();
                password = passwordInput.getText().toString();

                if(!TextUtils.isEmpty(emailID) && !TextUtils.isEmpty(password)){
                    loginAuth.signInWithEmailAndPassword(emailID, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                loginDialog.dismiss();
                                Intent mainIntent = new Intent(LoginActivity.this , MainActivity.class);
                                startActivity(mainIntent);
                                Toast.makeText(LoginActivity.this , "Logged in successfully ", Toast.LENGTH_SHORT).show();
                                finish();
                            }else{
                                loginDialog.dismiss();
                                Toast.makeText(LoginActivity.this , "Error logging in try again after sometime", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }else{
                    loginDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Please fill in the details",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
