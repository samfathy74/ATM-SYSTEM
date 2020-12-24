package com.example.firebasetutorial.manager.manager_activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebasetutorial.R;
import com.example.firebasetutorial.activity_login;
import com.example.firebasetutorial.admin.adapters.RecycleVAdapterDepartment;
import com.example.firebasetutorial.admin.admin_activities.activity_department;
import com.example.firebasetutorial.classes.Attendance;
import com.example.firebasetutorial.classes.Attentive;
import com.example.firebasetutorial.classes.Departments;
import com.example.firebasetutorial.manager.adapters.RecycleVAdapterAttendance;
import com.example.firebasetutorial.manager.adapters.RecycleVAdapterAttentive;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class activity_follow_attentive extends AppCompatActivity {
    RecyclerView recycleAllAttentive;
    RecycleVAdapterAttentive adapter;
    FirebaseAuth auth;
    DatabaseReference reference;
    ImageView imageRecycleNull;
    FirebaseRecyclerOptions<Attentive> options;
    SharedPreferences preferences;
    SearchView searcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_attentive);

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);

        if (auth.getCurrentUser() == null) {
            //home page
            startActivity(new Intent(getApplicationContext(), activity_login.class));
            finish();
        }

        imageRecycleNull = findViewById(R.id.imageRecycleNull);
        recycleAllAttentive = findViewById(R.id.recycleAllAttentive);
        searcher = findViewById(R.id.searcher);
        recycleAllAttentive.setLayoutManager(new LinearLayoutManager(activity_follow_attentive.this));

        getHistoryData();
    }

    private void getHistoryData() {
        searcher.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchIntoRecycle(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchIntoRecycle(s);
                return false;
            }
        });

        try {
            preferences = getSharedPreferences("mprofileData", MODE_PRIVATE);
            String department = preferences.getString("Department", "");
            if (department != null) {

                Query query = reference.child("RandomTest").child(department).orderByChild("Email");
                options = new FirebaseRecyclerOptions.Builder<Attentive>()
                        .setQuery(query, Attentive.class)
                        .build();
                adapter = new RecycleVAdapterAttentive(options);
                recycleAllAttentive.setAdapter(adapter);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            adapter.startListening();
                            imageRecycleNull.setVisibility(View.GONE);
                            recycleAllAttentive.setVisibility(View.VISIBLE);
                        } else {
                            imageRecycleNull.setVisibility(View.VISIBLE);
                            recycleAllAttentive.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NotNull DatabaseError databaseError) {
                        Toast.makeText(activity_follow_attentive.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(activity_follow_attentive.this, " e.getMessage()", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
        }
    }

    private void searchIntoRecycle(String s) {
        preferences = getSharedPreferences("mprofileData", MODE_PRIVATE);
        String department = preferences.getString("Department", "");
        Query queries = reference.child("RandomTest").child(department).orderByChild("Email").startAt(s.toLowerCase()).endAt(s.toLowerCase() + "\uf8ff");
        options = new FirebaseRecyclerOptions.Builder<Attentive>()
                .setQuery(queries, Attentive.class).build();

        adapter = new RecycleVAdapterAttentive(options);
        recycleAllAttentive.setAdapter(adapter);
        queries.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    adapter.startListening();
                    imageRecycleNull.setVisibility(View.GONE);
                } else {
                    imageRecycleNull.setVisibility(View.VISIBLE);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(activity_follow_attentive.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isConnected()) {
            showInfoAlert();
        }
    }

    private void showInfoAlert() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(activity_follow_attentive.this, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
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