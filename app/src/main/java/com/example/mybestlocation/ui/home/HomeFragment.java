package com.example.mybestlocation.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybestlocation.Config;
import com.example.mybestlocation.JSONParser;
import com.example.mybestlocation.Position;
import com.example.mybestlocation.R;
import com.example.mybestlocation.databinding.FragmentHomeBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
public class HomeFragment extends Fragment {
    ArrayList<Position> data = new ArrayList<Position>();
    PositionAdapter adapter;
    RecyclerView recyclerView;

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Inflate the binding for the Home Fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize RecyclerView
        recyclerView = binding.recyclerViewLocations;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PositionAdapter(getContext(), data);
        recyclerView.setAdapter(adapter);

        // Add item decoration for spacing
        recyclerView.addItemDecoration(new SpaceItemDecoration(16));

        // Start initial data download
        new Download().execute();

        // Set up the download button's click listener
        binding.downloadBtn.setOnClickListener(v -> {
            new Download().execute();
        });

        // Apply animations to views
        Animation fadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        binding.textHome.startAnimation(fadeIn);
        binding.downloadBtn.startAnimation(fadeIn);
        recyclerView.startAnimation(fadeIn);

        // Request location permissions
        ActivityCompat.requestPermissions(
                requireActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                1
        );

        return root;
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    AlertDialog alert;

    class Download extends AsyncTask {
        @Override
        protected void onPreExecute() {
            // UI Thread
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Download");
            builder.setMessage("Downloading...");
            alert = builder.create();
            alert.show();

        }

        @Override
        protected Object doInBackground(Object[] objects) {
            // Code de thread secondaire (background)
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // problem: pas d'acces a l'interface graphique
            JSONParser parser = new JSONParser();
            JSONObject response = parser.makeRequest(Config.url_getAll);

            try {
                int success = response.getInt("success");
                Log.e("response", "==" + success);
                if(success == 1 ){
                    data.clear();
                    JSONArray positions=response.getJSONArray("positions");
                    Log.e("response", "==" + response);
                    for (int i = 0; i < positions.length(); i++) {
                        JSONObject obj = positions.getJSONObject(i);
                        int id = obj.getInt("id");
                        String pseudo = obj.getString("pseudo");
                        String longitude = obj.getString("longitude");
                        String latitude = obj.getString("latitude");
                        String numero = obj.getString("numero");
                        Position p = new Position(id,pseudo,longitude,latitude,numero);
                        data.add(p);
                    }

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
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if(grantResults.length > 0){
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    Toast.makeText(this.getContext(),"Permission granted",Toast.LENGTH_SHORT).show();
                }
            }
            else {
                // Permission denied
                Toast.makeText(this.getContext(), "Permission denied", Toast.LENGTH_SHORT).show();

            }
        }

    }
}