package com.example.firebasetutorial.employee.employee_activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.firebasetutorial.R;
import com.example.firebasetutorial.activity_login;
import com.example.firebasetutorial.employee.adapters.RVAdapterRequestsHistory;
import com.example.firebasetutorial.classes.Notification;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class activity_request_history extends AppCompatActivity {
    RecyclerView recyclerView;
    DatabaseReference reference;
    FirebaseAuth auth;
    SharedPreferences myShared;
    RVAdapterRequestsHistory adapter;
    FirebaseRecyclerOptions<Notification> options;
    LinearLayout linerLayoutEmptyRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_history);
        //DATABASE INITIALIZE
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);

        if (auth.getCurrentUser() == null) {
            //home page
            startActivity(new Intent(getApplicationContext(), activity_login.class));
            finish();
        }

        linerLayoutEmptyRequest = findViewById(R.id.LinerLayoutEmptyRequestts);
        recyclerView = findViewById(R.id.recyclerviewRequest);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity_request_history.this));
    }

    private void showRequestHistory() {
        myShared = getSharedPreferences("eprofileData", MODE_PRIVATE);
        String department = myShared.getString("Department", "");
        String email = auth.getCurrentUser().getEmail().replace(".", ",");

        if (department.isEmpty()) {
            Toast.makeText(getApplicationContext(), "failed to get department is null", Toast.LENGTH_LONG).show();
        } else {
            try {
                Query queries = reference.child("Requests").child(department).orderByChild("Email").equalTo(auth.getCurrentUser().getEmail());
                options = new FirebaseRecyclerOptions.Builder<Notification>()
                        .setQuery(queries, Notification.class)
                        .build();
                adapter = new RVAdapterRequestsHistory(options);
                recyclerView.setAdapter(adapter);
                queries.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            adapter.startListening();
                            linerLayoutEmptyRequest.setVisibility(View.GONE);
                        } else {
                            linerLayoutEmptyRequest.setVisibility(View.VISIBLE);
                        }
                        adapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), "there is problem00", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "problem " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onStart() {
        if (!isConnected()) {
            showInfoAlert();
        }
        super.onStart();
        showRequestHistory();
    }

    private void showInfoAlert() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getApplicationContext(), R.style.Theme_MaterialComponents_Light_Dialog_Alert);
        builder1.setCancelable(false);
        builder1.setMessage(Html.fromHtml("<font color='#FF0000'>There is a problem, the internet connection is weak or closed!</font>"));
        builder1.setPositiveButton(Html.fromHtml("Settings"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });
        builder1.setNegativeButton(Html.fromHtml("Retry"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!isConnected()) {
                    showInfoAlert();
                } else {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog dialog = builder1.create();
        dialog.show();
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo dataConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiConn.isConnected() && wifiConn != null && wifiConn.isAvailable() || dataConn != null && dataConn.isConnected() && dataConn.isAvailable()) {
            return true;
        } else {
            return false;
        }
    }

    public void onBackPressing(View view) {
        finish();
    }
}