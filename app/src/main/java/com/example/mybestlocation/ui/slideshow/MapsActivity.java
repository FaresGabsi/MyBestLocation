package com.example.mybestlocation.ui.slideshow;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.mybestlocation.R;
import com.example.mybestlocation.databinding.ActivityMapsBinding;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {
    private GoogleMap googleMap;
    private ActivityMapsBinding binding;
    private double longitude, latitude;
    private Marker marker;
    private void setUpMapTypeSelector() {
        findViewById(R.id.btn_normal).setOnClickListener(v -> googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL));
        findViewById(R.id.btn_satellite).setOnClickListener(v -> googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE));
        findViewById(R.id.btn_hybrid).setOnClickListener(v -> googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID));
        findViewById(R.id.btn_terrain).setOnClickListener(v -> googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN));
    }
    private void searchLocation(String location) {
        Geocoder geocoder = new Geocoder(this);
        try {
            // Get the location from user input
            List<Address> addresses = geocoder.getFromLocationName(location, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                // Update the marker position and move the camera
                marker.setPosition(latLng);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
            } else {
                Log.e("MapsActivity", "Location not found!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        longitude = Double.parseDouble(getIntent().getStringExtra("longitude"));
        latitude = Double.parseDouble(getIntent().getStringExtra("latitude"));

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Handle search button click
        Button btnSearch = findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(v -> {
            EditText etSearch = findViewById(R.id.et_search);
            String location = etSearch.getText().toString();
            if (!location.isEmpty()) {
                searchLocation(location);
            }
        });
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setOnMapClickListener(this);
        LatLng position = new LatLng(latitude, longitude);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(position));
        MarkerOptions options = new MarkerOptions().position(position).title("Save");
        // enable zoom in and zoom out
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        // set the color of the marker
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        marker = googleMap.addMarker(options);
        googleMap.setOnMarkerClickListener(this);
        setUpMapTypeSelector();

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        // when clicking on a place in the map add a new marker
        marker.setPosition(latLng);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));

    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        LatLng position = marker.getPosition();

        // Create a dialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setCancelable(true);

        // Inflate the custom layout
        View popupView = getLayoutInflater().inflate(R.layout.marker_popup, null);
        builder.setView(popupView);

        // Set the position in the popup
        TextView tvPosition = popupView.findViewById(R.id.tv_position);
        tvPosition.setText("Lat: " + position.latitude + ", Lng: " + position.longitude);

        // Handle "Add" button click
        Button btnAdd = popupView.findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(v -> {
            // Pass the data back to the SlideshowFragment
            Intent intent = new Intent();
            intent.putExtra("latitude", String.valueOf(position.latitude));
            intent.putExtra("longitude", String.valueOf(position.longitude));
            setResult(RESULT_OK, intent);
            finish();
        });

        // Show the dialog
        android.app.AlertDialog dialog = builder.create();
        dialog.show();

        return true;
    }



}