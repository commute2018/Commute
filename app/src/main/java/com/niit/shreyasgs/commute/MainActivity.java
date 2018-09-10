package com.niit.shreyasgs.commute;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    //-------PERMISSIONS-----------
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int PERMISSION_CODE = 1234;
    private static final int LOCATION_SETTINGS_REQUEST = 1;

    //-------FIREBASE INSTANCES--------
    private FirebaseAuth CustomerAuth;
    private DatabaseReference customerReference;


    //--------MAP DECLARATION-----
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GeoDataClient geoDataClient;
    private GoogleApiClient googleApiClient;

    //-------LAYOUT INSTANCES------
    private AutoCompleteTextView customerFromInput;
    private AutoCompleteTextView customerToInput;
    private Button customerBookNowButton;
    private Button customerWeeklyBookButton;
    private Button customerMonthlyBookButton;
    private ImageButton searchFrom;
    private ImageButton searchTo;
    private ImageView myLocation;


    //------PAGE VARIABLES------
    String customerFrom, customerTo, currentUser;
    private Address fromAddress,toAddress;
    private Boolean locationPermissionGranted = false;
    private static final float DEFAULT_ZOOM = 15f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40, -168), new LatLng(71,136));
    private PlaceAutocompleteAdapter autocompleteAdapter;
    private placeInfo selectedPlace;

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        googleApiClient = new GoogleApiClient.Builder(this).addApi(Places.GEO_DATA_API).addApi(Places.PLACE_DETECTION_API).enableAutoManage(this,this).build();
        geoDataClient = Places.getGeoDataClient(this, null);
        getLocationPermission();

        //-----FIREBASE DECLARATIONS------
        CustomerAuth = FirebaseAuth.getInstance();
        customerReference = FirebaseDatabase.getInstance().getReference();
        currentUser = CustomerAuth.getCurrentUser().getUid();

        //-----LAYOUT DECLARATIONS TO BE MADE HERE
        customerFromInput = (AutoCompleteTextView) findViewById(R.id.customer_map_activity_from);
        customerToInput = (AutoCompleteTextView) findViewById(R.id.customer_map_activity_to);
        searchFrom = (ImageButton)findViewById(R.id.customer_map_activity_search_from);
        searchTo = (ImageButton)findViewById(R.id.customer_map_activity_search_to);
        myLocation = (ImageView)findViewById(R.id.customer_map_activity_find_my_loc);

        //---references taken from app_bar_main.xml------
        customerBookNowButton = (Button)findViewById(R.id.customer_book_now_toolbar_button);
        customerWeeklyBookButton = (Button)findViewById(R.id.customer_weekly_book_toolbar_button);
        customerMonthlyBookButton = (Button)findViewById(R.id.customer_monthly_book_toolbar_button);
        //------------------------------------------------


        autocompleteAdapter = new PlaceAutocompleteAdapter(this, geoDataClient, LAT_LNG_BOUNDS, null);


        customerFromInput.setOnItemClickListener(suggestionClickListener);
        customerToInput.setOnItemClickListener(suggestionClickListener);


        //-------setting autocomplete for search bar-----------
        customerFromInput.setAdapter(autocompleteAdapter);
        customerToInput.setAdapter(autocompleteAdapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);

        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Opening account settings", Toast.LENGTH_SHORT).show();
                Intent settingsIntent = new Intent(MainActivity.this, AccountSettingsActivity.class);
                startActivity(settingsIntent);
            }
        });

        searchFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customerFrom = customerFromInput.getText().toString();
                Geocoder fromGeocoder = new Geocoder(MainActivity.this);

                List<Address> fromList = new ArrayList<>();
                try {
                    fromList = fromGeocoder.getFromLocationName(customerFrom, 1);
                } catch (IOException e) {

                }

                if(fromList.size() > 0){
                    fromAddress = fromList.get(0);
                    moveCamera(new LatLng(fromAddress.getLatitude(),fromAddress.getLongitude()), DEFAULT_ZOOM);
                    mMap.addMarker(new MarkerOptions().position(new LatLng(fromAddress.getLatitude(),fromAddress.getLongitude())).title("from"));
                    Toast.makeText(MainActivity.this, "Address found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        searchTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customerTo = customerToInput.getText().toString();
                Geocoder toGeocoder = new Geocoder(MainActivity.this);

                List<Address> toList = new ArrayList<>();
                try {
                    toList = toGeocoder.getFromLocationName(customerTo, 1);
                } catch (IOException e) {

                }

                if(toList.size() > 0){
                    toAddress = toList.get(0);
                    moveCamera(new LatLng(toAddress.getLatitude(),toAddress.getLongitude()), DEFAULT_ZOOM);
                    mMap.addMarker(new MarkerOptions().position(new LatLng(toAddress.getLatitude(),toAddress.getLongitude())).title("to"));
                    Toast.makeText(MainActivity.this, "Address found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
            }
        });

        customerBookNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customerBookNowButton.setBackgroundResource(R.drawable.white_border);
                customerBookNowButton.setTextColor(getApplication().getResources().getColor(R.color.darkBlue));
                customerWeeklyBookButton.setBackgroundResource(R.drawable.custom_border);
                customerWeeklyBookButton.setTextColor(getApplication().getResources().getColor(R.color.white));
                customerMonthlyBookButton.setBackgroundResource(R.drawable.custom_border);
                customerMonthlyBookButton.setTextColor(getApplication().getResources().getColor(R.color.white));
            }
        });

        customerWeeklyBookButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                customerWeeklyBookButton.setBackgroundResource(R.drawable.white_border);
                customerWeeklyBookButton.setTextColor(getApplication().getResources().getColor(R.color.darkBlue));
                customerBookNowButton.setBackgroundResource(R.drawable.custom_border);
                customerBookNowButton.setTextColor(getApplication().getResources().getColor(R.color.white));
                customerMonthlyBookButton.setBackgroundResource(R.drawable.custom_border);
                customerMonthlyBookButton.setTextColor(getApplication().getResources().getColor(R.color.white));
                Toast.makeText(MainActivity.this, "Book cabs for a week", Toast.LENGTH_SHORT).show();
            }
        });

        customerMonthlyBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customerMonthlyBookButton.setBackgroundResource(R.drawable.white_border);
                customerMonthlyBookButton.setTextColor(getApplication().getResources().getColor(R.color.darkBlue));
                customerWeeklyBookButton.setBackgroundResource(R.drawable.custom_border);
                customerWeeklyBookButton.setTextColor(getApplication().getResources().getColor(R.color.white));
                customerBookNowButton.setBackgroundResource(R.drawable.custom_border);
                customerBookNowButton.setTextColor(getApplication().getResources().getColor(R.color.white));
                Toast.makeText(MainActivity.this, "Book cabs for a month", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
            Intent startIntent = new Intent(MainActivity.this , StartActivity.class);
            startActivity(startIntent);
            finish();
        }
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
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

    private boolean checkLocationEnabled(){
        LocationManager locationManager = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return gps_enabled;
    }

    private void getDeviceLocationPrompt(){
        LocationRequest mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)
                .setFastestInterval(1 * 1000);

        LocationSettingsRequest.Builder settingsBuilder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        settingsBuilder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this)
                .checkLocationSettings(settingsBuilder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                } catch (ApiException ex) {
                    switch (ex.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) ex;
                                resolvableApiException.startResolutionForResult(MainActivity.this, LOCATION_SETTINGS_REQUEST);
                            } catch (IntentSender.SendIntentException e) {

                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                            break;
                    }
                }
            }
        });
    }

    private void mapInit(){
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void getDeviceLocation(){
        if(checkLocationEnabled()){
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
                                Toast.makeText(MainActivity.this , "location found", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(MainActivity.this, "location not found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }catch (SecurityException e){

            }
        }else{
            getDeviceLocationPrompt();
        }
    }

    private void moveCamera(LatLng latLng, float zoom){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        if (locationPermissionGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }


    //---------Google maps places API--------------------
    private AdapterView.OnItemClickListener suggestionClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Toast.makeText(MainActivity.this, "Item clicked", Toast.LENGTH_SHORT).show();
            final AutocompletePrediction items = autocompleteAdapter.getItem(position);
            final String place_id = items.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(googleApiClient, place_id);
            placeResult.setResultCallback(placeDetailsCallback);
        }
    };

    private ResultCallback<PlaceBuffer> placeDetailsCallback;

    {
        placeDetailsCallback = new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {
                Toast.makeText(MainActivity.this, "Checking the result", Toast.LENGTH_SHORT).show();
                if (!places.getStatus().isSuccess()) {
                    Toast.makeText(MainActivity.this, "No places found", Toast.LENGTH_SHORT).show();
                    places.release();
                    return;
                }
                final Place place = places.get(0);
                try {
                    Toast.makeText(MainActivity.this, "Trying to get the location",Toast.LENGTH_SHORT).show();
                    selectedPlace = new placeInfo();
                    selectedPlace.setName(place.getName().toString());
                    selectedPlace.setAddress(place.getAddress().toString());
                    selectedPlace.setId(place.getId());
                    selectedPlace.setAttributions(place.getAttributions().toString());
                    selectedPlace.setLatLng(place.getLatLng());
                    selectedPlace.setPhoneNumber(place.getPhoneNumber().toString());
                    selectedPlace.setRating(place.getRating());
                    selectedPlace.setWebsiteUri(place.getWebsiteUri());
                } catch (Exception e) {

                }
                moveCamera(new LatLng(place.getViewport().getCenter().latitude, place.getViewport().getCenter().longitude), DEFAULT_ZOOM);
                places.release();
            }
        };
    }

}
