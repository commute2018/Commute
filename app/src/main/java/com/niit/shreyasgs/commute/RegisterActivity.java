package com.niit.shreyasgs.commute;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    //----ALL FIREBASE INSTANCES HERE------
    private FirebaseAuth registerAuth;

    //----ALL LAYOUT INSTANCES HERE--------
    private EditText userNameInput;
    private EditText emailIDInput;
    private EditText passwordInput;
    private Button registerButton;

    //-----ALL VARIABLE DECLARATIONS HERE------
    String userName,emailID,password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //------ALL FIREBASE INITIALIZATIONS HERE-------
        registerAuth = FirebaseAuth.getInstance();

        //------ALL INITIALIZATIONS HERE---------
        userNameInput = (EditText)findViewById(R.id.register_activity_username);
        emailIDInput = (EditText)findViewById(R.id.register_activity_emailid);
        passwordInput = (EditText)findViewById(R.id.register_activity_password);
        registerButton = (Button)findViewById(R.id.register_activity_register_button);

        //-------REGISTER BUTTON ON CLICK--------
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                userName = userNameInput.getText().toString();
                emailID = emailIDInput.getText().toString();
                password = passwordInput.getText().toString();

                registerAuth.createUserWithEmailAndPassword(emailID , password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(RegisterActivity.this , "Sign up successful" , Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(RegisterActivity.this, "Error signing up",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
