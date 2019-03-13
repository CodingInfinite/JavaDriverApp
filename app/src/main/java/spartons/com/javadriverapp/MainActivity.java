package spartons.com.javadriverapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import spartons.com.javadriverapp.helper.FirebaseHelper;
import spartons.com.javadriverapp.helper.GoogleMapHelper;
import spartons.com.javadriverapp.helper.MarkerAnimationHelper;
import spartons.com.javadriverapp.helper.UiHelper;
import spartons.com.javadriverapp.interfaces.LatLngInterpolator;
import spartons.com.javadriverapp.model.Driver;

import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2161;
    private static final String DRIVER_ID = "0000";   // Id must be unique for every driver.


    private FirebaseHelper firebaseHelper = new FirebaseHelper(DRIVER_ID);
    private GoogleMapHelper googleMapHelper = new GoogleMapHelper();
    private AtomicBoolean driverOnlineFlag = new AtomicBoolean(false);

    private GoogleMap googleMap;
    private Marker currentPositionMarker;
    private FusedLocationProviderClient locationProviderClient;
    private UiHelper uiHelper;
    private LocationRequest locationRequest;

    private TextView driverStatusTextView;

    private boolean locationFlag = true;


    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Location location = locationResult.getLastLocation();
            if (location == null) return;
            if (locationFlag) {
                locationFlag = false;
                animateCamera(location);
            }
            if (driverOnlineFlag.get())
                firebaseHelper.updateDriver(new Driver(location.getLatitude(), location.getLongitude(), DRIVER_ID));
            showOrAnimateMarker(location);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.supportMap);
        assert mapFragment != null;
        uiHelper = new UiHelper(this);
        mapFragment.getMapAsync(googleMap -> this.googleMap = googleMap);
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = uiHelper.getLocationRequest();
        if (!uiHelper.isPlayServicesAvailable()) {
            Toast.makeText(this, "Play Services did not installed!", Toast.LENGTH_SHORT).show();
            finish();
        } else requestLocationUpdates();
        SwitchCompat driverStatusSwitch = findViewById(R.id.driverStatusSwitch);
        driverStatusTextView = findViewById(R.id.driverStatusTextView);
        driverStatusSwitch.setOnCheckedChangeListener((buttonView, b) -> {
            driverOnlineFlag.set(b);
            if (driverOnlineFlag.get())
                driverStatusTextView.setText(getResources().getString(R.string.online));
            else {
                driverStatusTextView.setText(getResources().getString(R.string.offline));
                firebaseHelper.deleteDriver();
            }
        });
    }

    private void animateCamera(Location location) {
        CameraUpdate cameraUpdate = googleMapHelper.buildCameraUpdate(new LatLng(location.getLatitude(), location.getLongitude()));
        googleMap.animateCamera(cameraUpdate);
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUpdates() {
        if (!uiHelper.isHaveLocationPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }
        if (uiHelper.isLocationProviderEnabled())
            uiHelper.showPositiveDialogWithListener(this, getResources().getString(R.string.need_location), getResources().getString(R.string.location_content), () -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)), "Turn On", false);
        locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void showOrAnimateMarker(Location location) {
        if (currentPositionMarker == null)
            currentPositionMarker = googleMap.addMarker(googleMapHelper.getDriverMarkerOptions(location));
        else
            MarkerAnimationHelper.animateMarkerToGB(
                    currentPositionMarker,
                    new LatLng(location.getLatitude(),
                            location.getLongitude()),
                    new LatLngInterpolator.Spherical());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            int value = grantResults[0];
            if (value == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Location Permission denied", Toast.LENGTH_SHORT).show();
                finish();
            } else if (value == PackageManager.PERMISSION_GRANTED) requestLocationUpdates();
        }
    }
}
