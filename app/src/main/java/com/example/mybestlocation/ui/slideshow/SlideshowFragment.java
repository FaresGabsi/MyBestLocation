package com.example.mybestlocation.ui.slideshow;
import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.mybestlocation.Config;
import com.example.mybestlocation.JSONParser;
import com.example.mybestlocation.R;
import com.example.mybestlocation.databinding.FragmentSlideshowBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SlideshowFragment extends Fragment implements LocationListener {

    FusedLocationProviderClient fusedLocationProviderClient;
    private FragmentSlideshowBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Button add = binding.addBtn;
        Button map = binding.mapBtn;
        Button back = binding.backBtn;

        showLocation();

        // Handle "Show Location" button click
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.nav_home);

            }
        });

        // Handle "Save Position" button click
        // Handle "Save Position" button click
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAdded()) { // Check if the fragment is currently added to its activity

                    // Validate that all fields are filled
                    String longitude = binding.textLongitude.getText().toString().trim();
                    String latitude = binding.textLatitude.getText().toString().trim();
                    String numero = binding.textNumero.getText().toString().trim();
                    String pseudo = binding.textPseudo.getText().toString().trim();

                    if (longitude.isEmpty() || latitude.isEmpty() || numero.isEmpty() || pseudo.isEmpty()) {
                        // Show an alert dialog if any field is empty
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Champs manquants")
                                .setMessage("Tous les champs doivent être remplis avant de continuer.")
                                .setPositiveButton("OK", null)
                                .show();
                    } else {
                        // Create the confirmation dialog
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Confirmer l'ajout")
                                .setMessage("Êtes-vous sûr de vouloir ajouter cette position ?")
                                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Proceed with adding the position after confirmation
                                        HashMap<String, String> params = new HashMap<>();
                                        params.put("longitude", longitude);
                                        params.put("latitude", latitude);
                                        params.put("numero", numero);
                                        params.put("pseudo", pseudo);
                                        Log.e("params", "==" + params);

                                        // Execute the Upload task
                                        Upload u = new Upload(params);
                                        u.execute();

                                        // Clear input fields
                                        binding.textLongitude.setText("");
                                        binding.textLatitude.setText("");
                                        binding.textNumero.setText("");
                                        binding.textPseudo.setText("");

                                        // Clear back stack and navigate to HomeFragment
                                        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                                        navController.navigate(R.id.nav_home);
                                    }
                                })
                                .setNegativeButton("Non", null) // Do nothing if 'Non' is pressed
                                .show(); // Show the dialog
                    }
                }
            }
        });



        // Handle "Back to Map" button click
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent maps = new Intent(getActivity(), MapsActivity.class);
                maps.putExtra("longitude", binding.textLongitude.getText().toString());
                maps.putExtra("latitude", binding.textLatitude.getText().toString());
                startActivityForResult(maps, 1); // Request code 1
            }
        });


        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            // Retrieve the latitude and longitude from the returned intent
            String latitude = data.getStringExtra("latitude");
            String longitude = data.getStringExtra("longitude");

            // Update the UI with the retrieved values
            binding.textLatitude.setText(latitude);
            binding.textLongitude.setText(longitude);

            Log.e("SlideshowFragment", "Updated Lat: " + latitude + ", Lng: " + longitude);
        }
    }



    // show realtime location
    @SuppressLint("MissingPermission")
    public void showLocation() {

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.e("location", "gps is enabled");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, this);

        } else {
            Toast.makeText(getContext(), "Please turn on your GPS location", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    private void getLastLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        Log.e("location", "location: " + location.getLatitude() + " " + location.getLongitude());
                        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            Log.e("location address", "address:" + addresses.get(0).getAddressLine(0));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        binding.textLatitude.setText(String.valueOf(location.getLatitude()));
                        binding.textLongitude.setText(String.valueOf(location.getLongitude()));
                    }

                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Unregister the location listener
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        binding = null; // Set binding to null to avoid memory leaks
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.e("Location change:", location.toString());
        if (binding != null) { // Check if binding is not null
            binding.textLatitude.setText(String.valueOf(location.getLatitude()));
            binding.textLongitude.setText(String.valueOf(location.getLongitude()));
        } else {
            Log.e("SlideshowFragment", "Binding is null, cannot update UI.");
        }
    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);
    }

    @Override
    public void onFlushComplete(int requestCode) {
        LocationListener.super.onFlushComplete(requestCode);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }

    AlertDialog alert;

    // saving the informations
    class Upload extends AsyncTask {
        HashMap<String, String> params;

        public Upload(HashMap<String, String> params) {
            this.params = params;
        }

        @Override
        protected void onPreExecute() {
            // UI Thread
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Upload");
            builder.setMessage("Uploading...");
            alert = builder.create();
            alert.show();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            // Code de thread secondaire (background)
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // problem: pas d'acces a l'interface graphique
            JSONParser parser = new JSONParser();
            JSONObject response = parser.makeHttpRequest(Config.url_add,
                    "POST",
                    params);

            try {
                int success = response.getInt("success");
                Log.e("response", "==" + success);
                if (success == 1) {
                    Log.e("response", "===" + response);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            // UI Thread (Thread principal)
            super.onPostExecute(o);
            alert.dismiss();
        }
    }
}