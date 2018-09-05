package com.niit.shreyasgs.commute;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback {

    //-------PERMISSIONS-----------
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int PERMISSION_CODE = 1234;

    //-------FIREBASE INSTANCES--------
    private FirebaseAuth CustomerAuth;
    private DatabaseReference customerReference;


    //--------MAP DECLARATION-----
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;

    //-------LAYOUT INSTANCES------
    private Toolbar customerMapToolbar;
    private EditText customerFromInput;
    private EditText customerToInput;
    private Button customerRideNowButton;

    //------PAGE VARIABLES------
    String customerFrom, customerTo, currentUser;
    private Boolean locationPermissionGranted = false;
    private static final float DEFAULT_ZOOM = 15f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);
        getLocationPermission();

        //-----FIREBASE DECLARATIONS------
        CustomerAuth = FirebaseAuth.getInstance();
        customerReference = FirebaseDatabase.getInstance().getReference();
        currentUser = CustomerAuth.getCurrentUser().getUid();

        //-----LAYOUT DECLARATIONS TO BE MADE HERE
        customerMapToolbar = (Toolbar) findViewById(R.id.customer_map_activity_toolbar);
        customerFromInput = (EditText) findViewById(R.id.customer_map_activity_from);
        customerToInput = (EditText) findViewById(R.id.customer_map_activity_to);
        customerRideNowButton = (Button) findViewById(R.id.customer_map_activity_ride_now_button);

        //------toolbar changes to be made here--------
        setActionBar(customerMapToolbar);


        //------RIDE NOW ON CLICK--------
        customerRideNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customerFrom = customerFromInput.getText().toString();
                customerTo = customerToInput.getText().toString();

                Map fromToMap = new HashMap();
                fromToMap.put("from", customerFrom);
                fromToMap.put("to", customerTo);

                customerReference.child(currentUser).updateChildren(fromToMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    }
                });

            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney , 10));
        if (locationPermissionGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu , menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.main_menu_logout_button){
            FirebaseAuth.getInstance().signOut();
            Intent startIntent = new Intent(CustomerMapActivity.this , StartActivity.class);
            startActivity(startIntent);
            finish();
        }
        return true;
    }

    private void getLocationPermission(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationPermissionGranted = true;
                mapInit();
            }else{
                ActivityCompat.requestPermissions(this , permissions , PERMISSION_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this , permissions , PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermissionGranted = false;

        switch (requestCode){
            case PERMISSION_CODE:
                if(grantResults.length > 0 ){
                    for(int i = 0 ; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            locationPermissionGranted = false;
                            return;
                        }
                    }
                    locationPermissionGranted = true;
                    mapInit();
                }
        }
    }

    private void mapInit(){
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void getDeviceLocation(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try{
            if(locationPermissionGranted){
                Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                            Toast.makeText(CustomerMapActivity.this , "location found", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(CustomerMapActivity.this, "location not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        }catch (SecurityException e){

        }
    }

    private void moveCamera(LatLng latLng, float zoom){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

}
