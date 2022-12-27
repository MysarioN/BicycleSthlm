package kth.init.bicyclesthlm;

import static android.content.ContentValues.TAG;

import static kth.init.bicyclesthlm.BuildConfig.MAPS_API_KEY;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;

import kth.init.bicyclesthlm.Model.FiltersDialogModel;
import kth.init.bicyclesthlm.databinding.ActivityMapsBinding;

//Parts of this class is taken from developer.google.com
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int REQUEST_ACCESS_LOCATION = 1000;

    private GoogleMap mMap;
    private Location mLastLocation;
    private Marker searchMarker;
    private ActivityMapsBinding binding;
    private boolean locationPermissionGranted;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private FiltersDialogModel filtersDialogModel;

    private final LatLng defaultLocation = new LatLng(59.334591, 18.063240); //Stockholm

    //Test
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        filtersDialogModel = new FiltersDialogModel();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);

        Places.initialize(getApplicationContext(), MAPS_API_KEY);
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG));

        autocompleteFragment.setCountry("SE");

        autocompleteFragment.setLocationBias(RectangularBounds.newInstance(
                new LatLng(59.229790, 18.206432),
                new LatLng(59.427347, 17.733876)));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 11));
                if(searchMarker != null)
                    searchMarker.remove();
                searchMarker = mMap.addMarker(new MarkerOptions().position(place.getLatLng()));
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        ImageButton button = findViewById(R.id.layers_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMapTypeDialog();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        initPerms();
    }

    private void initPerms() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_ACCESS_LOCATION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        getDeviceLocation();

        if (mLastLocation != null)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 11));
        else
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 8));
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_ACCESS_LOCATION) {
            // if request is cancelled, the results array is empty
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // stop this activity
                this.finish();
            } else {
                locationPermissionGranted = true;
            }
        } else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        updateLocationUI();
    }

    @SuppressLint("MissingPermission")
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastLocation = null;
                initPerms();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /*
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
    private void getDeviceLocation() {

        try {
            if (locationPermissionGranted) {
                @SuppressLint("MissingPermission") Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastLocation = task.getResult();
                            if (mLastLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastLocation.getLatitude(),
                                                mLastLocation.getLongitude()), 11));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, 11));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    public void showMapTypeDialog() {
        String[] filterTypes = {"Bicycle paths", "Citybikes", "Bicycle pumps", "Bicycle parking", "Bicycle traffic flow"};

        AlertDialog dialog = new AlertDialog.Builder(MapsActivity.this)
                .setTitle("Filters")
                .setPositiveButton("Standard", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    }
                })
                .setNeutralButton("Satellite", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    }
                })
                .setMultiChoiceItems(filterTypes, filtersDialogModel.isAllChecked(), new DialogInterface.OnMultiChoiceClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        if(b){
                            switch (i){
                                case 0:
                                    filtersDialogModel.setBicyclePaths(true);
                                    //TODO add bicycle paths layer
                                    System.out.println("Added bicycle paths");
                                    break;
                                case 1:
                                    filtersDialogModel.setCityBikes(true);
                                    //TODO add citybike filter
                                    System.out.println("Added citybikes");
                                    break;
                                case 2:
                                    filtersDialogModel.setBicyclePumps(true);
                                    //TODO add bicycle pump filter
                                    System.out.println("Added bicycle pump");
                                    break;
                                case 3:
                                    filtersDialogModel.setBicycleParking(true);
                                    //TODO add bicycle parking filter
                                    System.out.println("Added bicycle parking");
                                    break;
                                case 4:
                                    filtersDialogModel.setBicycleTrafficFlow(true);
                                    //TODO add bicycle traffic flow filter
                                    System.out.println("Added bicycle traffic flow");
                                    break;
                            }
                        } else {
                            switch (i){
                                case 0:
                                    filtersDialogModel.setBicyclePaths(false);
                                    //TODO remove bicycle paths layer
                                    System.out.println("Removed bicycle paths");
                                    break;
                                case 1:
                                    filtersDialogModel.setCityBikes(false);
                                    //TODO remove citybike filter
                                    System.out.println("Removed citybikes");
                                    break;
                                case 2:
                                    filtersDialogModel.setBicyclePumps(false);
                                    //TODO remove bicycle pump filter
                                    System.out.println("Removed bicycle pump");
                                    break;
                                case 3:
                                    filtersDialogModel.setBicycleParking(false);
                                    //TODO remove bicycle parking filter
                                    System.out.println("Removed bicycle parking");
                                    break;
                                case 4:
                                    filtersDialogModel.setBicycleTrafficFlow(false);
                                    //TODO remove bicycle traffic flow filter
                                    System.out.println("Removed bicycle traffic flow");
                                    break;
                            }
                        }
                    }
                })
                .setCancelable(true)
                .show();

        Button standardMapButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button satelliteMapButton = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);

        Drawable standardIcon = AppCompatResources.getDrawable(MapsActivity.this, R.drawable.standard_map);
        Drawable satelliteIcon = AppCompatResources.getDrawable(MapsActivity.this, R.drawable.satellite_map);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(5,5,5,5);

        standardMapButton.setLayoutParams(params);
        satelliteMapButton.setLayoutParams(params);

        standardMapButton.setBackground(standardIcon);
        satelliteMapButton.setBackground(satelliteIcon);
    }
}