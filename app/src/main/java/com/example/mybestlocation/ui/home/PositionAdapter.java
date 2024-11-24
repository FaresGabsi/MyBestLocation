package com.example.mybestlocation.ui.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybestlocation.Config;
import com.example.mybestlocation.JSONParser;
import com.example.mybestlocation.Position;
import com.example.mybestlocation.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class PositionAdapter extends RecyclerView.Adapter<PositionAdapter.PositionViewHolder> {

    private final ArrayList<Position> positionList;
    private final Context context;

    public PositionAdapter(Context context, ArrayList<Position> positionList) {
        this.context = context;
        this.positionList = positionList;
    }

    @NonNull
    @Override
    public PositionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_postion, parent, false);
        return new PositionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PositionViewHolder holder, int position) {
        Position currentPosition = positionList.get(position);
        holder.tvPseudo.setText(currentPosition.getPseudo());
        holder.tvLatitude.setText("Latitude: " + currentPosition.getLatitude());
        holder.tvLongitude.setText("Longitude: " + currentPosition.getLongitude());
        holder.tvNumber.setText("Phone: " + currentPosition.getNumero());

        // Handle "View on Map" click
        holder.ivViewMap.setOnClickListener(v -> {
            String latitude = currentPosition.getLatitude();
            String longitude = currentPosition.getLongitude();
            Uri mapUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
            if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(mapIntent);
            } else {
                Toast.makeText(context, "No Maps app installed!", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle "Delete" click
        holder.ivDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Confirmer la suppression")
                    .setMessage("Êtes-vous sûr de vouloir supprimer cet élément ?")
                    .setPositiveButton("Oui", (dialog, which) -> {
                        // Start async task to delete item
                        new DeletePositionTask(currentPosition, position).execute();
                    })
                    .setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss())
                    .show();
        });
        holder.ivCall.setOnClickListener(v -> {
            String phoneNumber = currentPosition.getNumero();



            // Alternative: Use ACTION_CALL (requires CALL_PHONE permission)
       Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
        context.startActivity(callIntent);
    } else {
        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE}, 1);
    }
        });


        holder.ivSms.setOnClickListener(v -> {
            String phoneNumber = currentPosition.getNumero();
            String message = "Bonjour, voici ma position: Latitude " + currentPosition.getLatitude() +
                    ", Longitude " + currentPosition.getLongitude();

            // Show confirmation dialog
            new AlertDialog.Builder(context)
                    .setTitle("Confirmation")
                    .setMessage("Êtes-vous sûr de vouloir envoyer ce message ? Cela coûte 50 millimes.")
                    .setPositiveButton("Oui", (dialog, which) -> {
                        // Check for SMS permissions
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                            try {
                                SmsManager smsManager = SmsManager.getDefault();
                                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                                Toast.makeText(context, "SMS envoyé avec succès!", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(context, "Échec de l'envoi du SMS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Request permission if not granted
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.SEND_SMS}, 1);
                        }
                    })
                    .setNegativeButton("Annuler", (dialog, which) -> {
                        // Dismiss dialog if user cancels
                        dialog.dismiss();
                    })
                    .show();
        });


    }

    @Override
    public int getItemCount() {
        return positionList.size();
    }

    static class PositionViewHolder extends RecyclerView.ViewHolder {

        TextView tvPseudo, tvLatitude, tvLongitude, tvNumber;
        ImageView ivViewMap, ivDelete, ivCall, ivSms;

        public PositionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPseudo = itemView.findViewById(R.id.tvPseudo);
            tvLatitude = itemView.findViewById(R.id.tvLatitude);
            tvLongitude = itemView.findViewById(R.id.tvLongitude);
            tvNumber = itemView.findViewById(R.id.tvPhoneNumber);
            ivViewMap = itemView.findViewById(R.id.ivViewMap);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            ivCall = itemView.findViewById(R.id.ivCallIcon);
            ivSms = itemView.findViewById(R.id.ivSmsIcon);
        }
    }

    private class DeletePositionTask extends AsyncTask<Void, Void, Boolean> {

        private final Position position;
        private final int adapterPosition;

        public DeletePositionTask(Position position, int adapterPosition) {
            this.position = position;
            this.adapterPosition = adapterPosition;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(context, "Deleting item...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            JSONParser jsonParser = new JSONParser();
            String url = Config.url_delete; // Ensure Config.url_delete points to your backend's delete endpoint

            HashMap<String, String> params = new HashMap<>();
            params.put("id", String.valueOf(position.getId())); // Replace "id" with the actual parameter your backend expects

            // Send POST request
            JSONObject response = jsonParser.makeHttpRequest(url, "POST", params);

            try {
                if (response != null) {
                    int success = response.getInt("success"); // Ensure this matches your backend's response
                    return success == 1; // Assuming 1 indicates success
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                // Remove the item from the RecyclerView if the delete was successful
                positionList.remove(adapterPosition);
                notifyItemRemoved(adapterPosition);
                notifyItemRangeChanged(adapterPosition, positionList.size());
                Toast.makeText(context, "Item deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to delete item. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
