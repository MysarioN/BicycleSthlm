package kth.init.bicyclesthlm;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import kth.init.bicyclesthlm.databinding.ActivityMapsBinding;

//Parts of this class is taken from developer.google.com
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int REQUEST_ACCESS_LOCATION = 1000;

    private GoogleMap mMap;
    Location mLastLocation;
    private ActivityMapsBinding binding;
    private boolean locationPermissionGranted;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final LatLng defaultLocation = new LatLng(59.334591, 18.063240); //Stockholm

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        getDeviceLocation();

        if (mLastLocation != null)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 5));
        else
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 5));
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
        String[] filterTypes = {"Citybikes", "Bicycle pumps", "Bicycle parking", "Bicycle traffic flow"};
        boolean[] checked = {false, false, false, false};

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
                .setMultiChoiceItems(filterTypes, checked, new DialogInterface.OnMultiChoiceClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        if(b){
                            switch (i){
                                case 0:
                                    //TODO add citybike filter
                                    System.out.println("Added citybikes");
                                    break;
                                case 1:
                                    //TODO add bicycle pump filter
                                    System.out.println("Added bicycle pump");
                                    break;
                                case 2:
                                    //TODO add bicycle parking filter
                                    System.out.println("Added bicycle parking");
                                    break;
                                case 3:
                                    //TODO add bicycle traffic flow filter
                                    System.out.println("Added bicycle traffic flow");
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