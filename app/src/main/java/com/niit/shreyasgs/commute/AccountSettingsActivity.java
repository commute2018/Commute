package com.niit.shreyasgs.commute;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AccountSettingsActivity extends AppCompatActivity {

    private FirebaseAuth userAuth;
    private DatabaseReference userReference;

    private Toolbar settingsToolbar;

    private EditText userNameText;
    private EditText emailIDText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        settingsToolbar = (Toolbar)findViewById(R.id.Settings_tool_bar);
        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        userNameText = (EditText)findViewById(R.id.account_settings_username);
        emailIDText = (EditText)findViewById(R.id.account_settings_emailID);

        userAuth = FirebaseAuth.getInstance();
        String current_user = userAuth.getCurrentUser().getUid();
        userReference = FirebaseDatabase.getInstance().getReference().child(current_user);


        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String email_id = dataSnapshot.child("emailID").getValue().toString();

                userNameText.setText(name);
                emailIDText.setText(email_id);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.settings_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.settigns_logout_button){
            userAuth.signOut();
            Intent startIntent = new Intent(AccountSettingsActivity.this, StartActivity.class);
            startActivity(startIntent);
            finish();
        }

        if(item.getItemId() == R.id.settings_save_button){
            String updatedName = userNameText.getText().toString();
            String updatedEmailID = emailIDText.getText().toString();

            Map updatedMap = new HashMap();
            updatedMap.put("name", updatedName);
            updatedMap.put("emailID", updatedEmailID);

            userReference.updateChildren(updatedMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(AccountSettingsActivity.this, "Updated name and email", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            Intent MainIntent = new Intent(AccountSettingsActivity.this, MainActivity.class);
            startActivity(MainIntent);
            finish();
        }
        return true;
    }
}
