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
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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


import java.util.ArrayList;
import java.util.Arrays;

import kth.init.bicyclesthlm.data.Networking;
import kth.init.bicyclesthlm.model.BicycleCollection;
import kth.init.bicyclesthlm.model.FiltersDialogModel;
import kth.init.bicyclesthlm.databinding.ActivityMapsBinding;
import kth.init.bicyclesthlm.model.TrafficFlowCollection;

//Parts of this activity is taken from developer.google.com
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final int REQUEST_ACCESS_LOCATION = 1000;

    private GoogleMap mMap;
    private Location mLastLocation;
    private Marker searchMarker;
    private ActivityMapsBinding binding;
    private boolean locationPermissionGranted;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private FiltersDialogModel filtersDialogModel;
    private BicycleCollection bicycleCollection;
    private BicycleCollection cityBikesCollection;
    private BicycleCollection bikeParkingCollection;
    private TrafficFlowCollection trafficFlowCollection;


    private Networking network;

    private ArrayList<Marker> bicyclePumpMarkers;
    private ArrayList<Marker> cityBikesMarkers;
    private ArrayList<Marker> bikeParkingMarkers;
    private ArrayList<Marker> trafficFlowMarkers;

    private final LatLng defaultLocation = new LatLng(59.334591, 18.063240); //Stockholm

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        filtersDialogModel = new FiltersDialogModel();
        bicycleCollection = new BicycleCollection(this, this);
        cityBikesCollection = new BicycleCollection(this, this);
        bikeParkingCollection = new BicycleCollection(this, this);
        trafficFlowCollection = new TrafficFlowCollection(this,this);

        network = new Networking(this);

        bicyclePumpMarkers = new ArrayList<>();
        cityBikesMarkers = new ArrayList<>();
        bikeParkingMarkers = new ArrayList<>();
        trafficFlowMarkers = new ArrayList<>();

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

        // Set up a PlaceSelectionListener to handle search response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 11));
                if (searchMarker != null)
                    searchMarker.remove();
                searchMarker = mMap.addMarker(new MarkerOptions().position(place.getLatLng()));
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        //Filter button listener
        ImageButton filterButton = findViewById(R.id.layers_button);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMapTypeDialog(mapFragment.getActivity());
            }
        });
    }

    //Gets saved state of filters
    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences preferences = getSharedPreferences("filterState", MODE_PRIVATE);

        filtersDialogModel.setBicyclePaths(preferences.getBoolean("bicyclePaths", false));
        filtersDialogModel.setCityBikes(preferences.getBoolean("cityBikes", false));
        filtersDialogModel.setBicyclePumps(preferences.getBoolean("bicyclePumps", false));
        filtersDialogModel.setBicycleParking(preferences.getBoolean("bicycleParking", false));
        filtersDialogModel.setBicycleTrafficFlow(preferences.getBoolean("bicycleTrafficFlow", false));
        filtersDialogModel.setStandardMap(preferences.getBoolean("standardMap", true));
        filtersDialogModel.setSatelliteMap(preferences.getBoolean("satellite", false));

        initPerms();
    }

    //After onStarts checks which filters are active and adds those to map
    @Override
    protected void onResume() {
        super.onResume();

        if (filtersDialogModel.isBicyclePumps()) {
            network.getBicyclePumps(bicycleCollection); //GET requests

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    addBicyclePumpMarkers(bicycleCollection, bicyclePumpMarkers, "pump");
                }
            }, 1500); //Delay because previous GET request has to be finished
        }
        if (filtersDialogModel.isCityBikes()) {
            network.getCityBikes(cityBikesCollection); //GET requests

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    addBicyclePumpMarkers(cityBikesCollection, cityBikesMarkers, "citybike");
                }
            }, 1500); //Delay because previous GET request has to be finished
        }
        if (filtersDialogModel.isBicycleParking()) {
            network.getBikeParking(bikeParkingCollection); //GET requests

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    addBicyclePumpMarkers(bikeParkingCollection, bikeParkingMarkers, "bikeparking");
                }
            }, 1500); //Delay because previous GET request has to be finished
        }
        if (filtersDialogModel.isBicycleTrafficFlow()) {
            if(trafficFlowCollection.getTrafficFlowObjectArrayList().size() < 20 || trafficFlowCollection.getTrafficFlowObjectArrayList().isEmpty())
                network.getTraficFlowLinkIds(trafficFlowCollection); //GET requests

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    addTrafficFlowMarkers(trafficFlowCollection, trafficFlowMarkers, "trafficflow");
                }
            }, 1500); //Delay because previous GET request has to be finished
        }
    }

    //Saves states of filters
    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences preferences = getSharedPreferences("filterState", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("bicyclePaths", filtersDialogModel.isBicyclePaths());
        editor.putBoolean("cityBikes", filtersDialogModel.isCityBikes());
        editor.putBoolean("bicyclePumps", filtersDialogModel.isBicyclePumps());
        editor.putBoolean("bicycleParking", filtersDialogModel.isBicycleParking());
        editor.putBoolean("bicycleTrafficFlow", filtersDialogModel.isBicycleTrafficFlow());
        editor.putBoolean("standardMap", filtersDialogModel.isStandardMap());
        editor.putBoolean("satellite", filtersDialogModel.isSatelliteMap());

        editor.apply();
    }

    //Asks user for location permissions
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

    //Runs when map has loaded
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        //Checks saved state
        if (filtersDialogModel.isStandardMap())
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        else
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        getDeviceLocation();

        if (mLastLocation != null)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 11));
        else
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 8));

        //If user already has granted location permission on re-open app
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            locationPermissionGranted = true;
            updateLocationUI();
        }
    }

    //When user grants location permission
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

    //Enables or disables user location and find-my-location button
    @SuppressLint("MissingPermission") //No, it does not have missing permissions...
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

    //Opens filters dialog
    public void showMapTypeDialog(Activity activity) {
        String[] filterTypes = {"Bicycle paths", "Citybikes", "Bicycle pumps", "Bicycle parking", "Bicycle traffic flow"};

        AlertDialog dialog = new AlertDialog.Builder(MapsActivity.this)
                .setTitle("Filters")
                .setPositiveButton("Standard", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        filtersDialogModel.setSatelliteMap(false);
                        filtersDialogModel.setStandardMap(true);
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    }
                })
                .setNeutralButton("Satellite", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        filtersDialogModel.setSatelliteMap(true);
                        filtersDialogModel.setStandardMap(false);
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

                    }
                })
                .setMultiChoiceItems(filterTypes, filtersDialogModel.isAllChecked(), new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        if (b) {
                            switch (i) {
                                case 0:
                                    filtersDialogModel.setBicyclePaths(true);
                                    //TODO add bicycle paths layer
                                    System.out.println("Added bicycle paths");
                                    break;
                                case 1:
                                    filtersDialogModel.setCityBikes(true);
                                    network.getCityBikes(cityBikesCollection);
                                    break;
                                case 2:
                                    filtersDialogModel.setBicyclePumps(true);
                                    network.getBicyclePumps(bicycleCollection);
                                    break;
                                case 3:
                                    filtersDialogModel.setBicycleParking(true);
                                    network.getBikeParking(bikeParkingCollection);
                                    break;
                                case 4:
                                    filtersDialogModel.setBicycleTrafficFlow(true);
                                    if(trafficFlowCollection.getTrafficFlowObjectArrayList().size() < 20 || trafficFlowCollection.getTrafficFlowObjectArrayList().isEmpty())
                                        network.getTraficFlowLinkIds(trafficFlowCollection);
                                    break;
                            }
                        } else {
                            switch (i) {
                                case 0:
                                    filtersDialogModel.setBicyclePaths(false);
                                    //TODO remove bicycle paths layer
                                    break;
                                case 1:
                                    filtersDialogModel.setCityBikes(false);

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        cityBikesMarkers.forEach(Marker::remove);
                                        cityBikesMarkers.clear();
                                    }
                                    break;
                                case 2:
                                    filtersDialogModel.setBicyclePumps(false);

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        bicyclePumpMarkers.forEach(Marker::remove);
                                        bicyclePumpMarkers.clear();
                                    }
                                    break;
                                case 3:
                                    filtersDialogModel.setBicycleParking(false);

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        bikeParkingMarkers.forEach(Marker::remove);
                                        bikeParkingMarkers.clear();
                                    }
                                    break;
                                case 4:
                                    filtersDialogModel.setBicycleTrafficFlow(false);

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        trafficFlowMarkers.forEach(Marker::remove);
                                        trafficFlowMarkers.clear();
                                    }
                                    break;
                            }
                        }
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {

                        if (filtersDialogModel.isBicyclePumps()) {
                            addBicyclePumpMarkers(bicycleCollection, bicyclePumpMarkers, "pump");
                        }
                        if (filtersDialogModel.isCityBikes()) {
                            addBicyclePumpMarkers(cityBikesCollection, cityBikesMarkers, "citybike");
                        }
                        if (filtersDialogModel.isBicycleParking()) {
                            addBicyclePumpMarkers(bikeParkingCollection, bikeParkingMarkers, "bikeparking");
                        }
                        if (filtersDialogModel.isBicycleTrafficFlow()) {
                            addTrafficFlowMarkers(trafficFlowCollection, trafficFlowMarkers, "trafficflow");
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
        params.setMargins(5, 5, 5, 5);

        standardMapButton.setLayoutParams(params);
        satelliteMapButton.setLayoutParams(params);

        standardMapButton.setBackground(standardIcon);
        satelliteMapButton.setBackground(satelliteIcon);
    }

    /*
    Creates markers from bicyclepumps latlngs
    then adds them to an ArrayList of Marker
    this allows us to later remove only those specified markers
    */
    private void addBicyclePumpMarkers(BicycleCollection collection, ArrayList<Marker> markers, String image) {
        Bitmap b = BitmapFactory.decodeResource(getResources(), getImage(image));
        Bitmap smallPumpIcon = Bitmap.createScaledBitmap(b, 30, 40, false);

        for (int i = 0; i < collection.getLatLngs().size(); i++) {
            Marker pumpMarker = mMap.addMarker(new MarkerOptions()
                    .position(collection.getLatLngs().get(i))
                    .icon(BitmapDescriptorFactory.fromBitmap(smallPumpIcon)).flat(false));

            markers.add(pumpMarker);
        }
    }

    /*
    Creates markers from trafficFlow
    then adds them to an ArrayList of Marker
    this allows us to later remove only those specified markers
    adds title, snippet and onclick
    */
    private void addTrafficFlowMarkers(TrafficFlowCollection collection, ArrayList<Marker> markers, String image) {
        Bitmap b = BitmapFactory.decodeResource(getResources(), getImage(image));
        Bitmap smallPumpIcon = Bitmap.createScaledBitmap(b, 30, 40, false);

        for (int i = 0; i < collection.getTrafficFlowObjectArrayList().size(); i++) {
            Marker pumpMarker = mMap.addMarker(new MarkerOptions()
                    .position(collection.getTrafficFlowObjectArrayList().get(i).getLatLng())
                    .icon(BitmapDescriptorFactory.fromBitmap(smallPumpIcon)).flat(false));

            pumpMarker.setTag(0);
            pumpMarker.setTitle("Dagligt medelvärde: " + collection.getTrafficFlowObjectArrayList().get(i).getMeanValue());
            pumpMarker.setSnippet("Mätmetod: " + collection.getTrafficFlowObjectArrayList().get(i).getFlowEstimation());
            markers.add(pumpMarker);

            mMap.setOnMarkerClickListener(this);
        }
    }

    @Override
    /** Called when the user clicks a marker. */
    public boolean onMarkerClick(final Marker marker) {
        return false;
    }

    /*
        Gets the marker image
     */
    private int getImage(String imageName){
        int imagenumb = R.drawable.info;

        switch (imageName){
            case "pump":
                imagenumb = R.drawable.pump;
                break;
            case "citybike":
                imagenumb = R.drawable.citybike;
                break;
            case "bikeparking":
                imagenumb = R.drawable.bikeparking;
                break;
            case "trafficflow":
                imagenumb = R.drawable.info;
                break;
        }
        return imagenumb;
    }

}