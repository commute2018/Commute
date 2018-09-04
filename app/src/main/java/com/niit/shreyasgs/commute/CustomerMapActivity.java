package com.niit.shreyasgs.commute;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback {

    //-------FIREBASE INSTANCES--------
    private FirebaseAuth CustomerAuth;


    //--------MAP DECLARATION-----
    private GoogleMap mMap;

    //-------LAYOUT INSTANCES------
    private Toolbar customerMapToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);

        //-----FIREBASE DECLARATIONS------
        CustomerAuth = FirebaseAuth.getInstance();
        customerMapToolbar = (Toolbar)findViewById(R.id.customer_map_activity_toolbar);

        //------toolbar changes to be made here--------
        setActionBar(customerMapToolbar);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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
}
