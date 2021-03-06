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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    //----ALL FIREBASE INSTANCES HERE------
    private FirebaseAuth registerAuth;
    private DatabaseReference userReference;

    //-----PROGRESS BAR-----
    private ProgressDialog regProgressDialog;

    //----ALL LAYOUT INSTANCES HERE--------
    private EditText userNameInput;
    private EditText emailIDInput;
    private EditText passwordInput;
    private EditText repPasswordInput;
    private Button registerButton;
    private Toolbar regiterPageToolbar;

    //-----ALL VARIABLE DECLARATIONS HERE------
    String userName,emailID,password,repPassword;
    String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //------ALL FIREBASE INITIALIZATIONS HERE-------
        registerAuth = FirebaseAuth.getInstance();
        userReference = FirebaseDatabase.getInstance().getReference();

        //------ALL INITIALIZATIONS HERE---------
        userNameInput = (EditText)findViewById(R.id.register_activity_username);
        emailIDInput = (EditText)findViewById(R.id.register_activity_emailid);
        passwordInput = (EditText)findViewById(R.id.register_activity_password);
        repPasswordInput = (EditText)findViewById(R.id.register_activity_repeat_password);
        registerButton = (Button)findViewById(R.id.register_activity_register_button);
        regiterPageToolbar = (Toolbar)findViewById(R.id.register_activity_toolbar);
        regProgressDialog = new ProgressDialog(this);

        //-------PROGRESS DIALOG CHANGES--------
        regProgressDialog.setTitle("Registering user");
        regProgressDialog.setMessage("Please wait while we create your account");
        regProgressDialog.setCanceledOnTouchOutside(false);

        //-------Toolbar changes to be made here-------
        setSupportActionBar(regiterPageToolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //-------REGISTER BUTTON ON CLICK--------
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                regProgressDialog.show();


                userName = userNameInput.getText().toString();
                emailID = emailIDInput.getText().toString();
                password = passwordInput.getText().toString();
                repPassword = repPasswordInput.getText().toString();

                if(!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(emailID) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(repPassword)){
                    if(password.equals(repPassword)){
                        registerAuth.createUserWithEmailAndPassword(emailID , password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){

                                    currentUser = registerAuth.getCurrentUser().getUid();

                                    Map userDetailsMap = new HashMap();
                                    userDetailsMap.put("name", userName);
                                    userDetailsMap.put("emailID", emailID);

                                    userReference.child(currentUser).updateChildren(userDetailsMap, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                        }
                                    });

                                    Intent mapIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                    startActivity(mapIntent);
                                    regProgressDialog.dismiss();
                                    finish();
                                    Toast.makeText(RegisterActivity.this , "Sign up successful" , Toast.LENGTH_SHORT).show();
                                }else{
                                    regProgressDialog.dismiss();
                                    Toast.makeText(RegisterActivity.this, "Error signing up",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }else{
                        regProgressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Re-enter the password again",Toast.LENGTH_SHORT).show();
                        passwordInput.setText("");
                        repPasswordInput.setText("");
                    }
                }else{
                    regProgressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "Please fill in the details correctly",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
