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
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebasetutorial.R;
import com.example.firebasetutorial.activity_login;
import com.example.firebasetutorial.manager.adapters.RecycleVAdapterAttendance;
import com.example.firebasetutorial.classes.Attendance;
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
import java.util.Locale;

public class activity_follow_attendance extends AppCompatActivity {
    RecyclerView recycleAllEmpsList;
    RecycleVAdapterAttendance adapter;
    FirebaseAuth auth;
    DatabaseReference reference;
    ImageView imageRecycleNull;
    SharedPreferences prefs;
    SearchView searcher;
    FirebaseRecyclerOptions<Attendance> options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_attendance);

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);

        if (auth.getCurrentUser() == null) {
            //home page
            startActivity(new Intent(getApplicationContext(), activity_login.class));
            finish();
        }

        searcher = findViewById(R.id.searcher);
        imageRecycleNull = findViewById(R.id.imageRecycleNull);
        recycleAllEmpsList = findViewById(R.id.recycleAllEmpsList);
        recycleAllEmpsList.setLayoutManager(new LinearLayoutManager(activity_follow_attendance.this));
    }

    private void getAttendanceData() {
        prefs = getSharedPreferences("mprofileData", MODE_PRIVATE);
        String department = prefs.getString("Department", "");

        String date = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Calendar.getInstance().getTime()).toString();
        if (department.isEmpty()) {
            Toast.makeText(activity_follow_attendance.this, "failed with department is incorrect", Toast.LENGTH_LONG).show();
        } else {

            searcher.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    searchIntoRecycle(s, date, department);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    searchIntoRecycle(s, date, department);
                    return false;
                }
            });

            try {
                Query query = reference.child("Attendees").child(date).child(department);
                options = new FirebaseRecyclerOptions.Builder<Attendance>()
                        .setQuery(query, Attendance.class)
                        .build();
                adapter = new RecycleVAdapterAttendance(options);
                recycleAllEmpsList.setAdapter(adapter);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            adapter.startListening();
                            imageRecycleNull.setVisibility(View.GONE);
                            recycleAllEmpsList.setVisibility(View.VISIBLE);
                        } else {
                            imageRecycleNull.setVisibility(View.VISIBLE);
                            recycleAllEmpsList.setVisibility(View.GONE);
                        }
                        adapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NotNull DatabaseError databaseError) {
                        Toast.makeText(activity_follow_attendance.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                Toast.makeText(activity_follow_attendance.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void searchIntoRecycle(String s, String date, String depart) {
        Query queries = reference.child("Attendees").child(date).child(depart).orderByChild("Email").startAt(s.toLowerCase()).endAt(s.toLowerCase() + "\uf8ff");
        options = new FirebaseRecyclerOptions.Builder<Attendance>()
                .setQuery(queries, Attendance.class).build();

        adapter = new RecycleVAdapterAttendance(options);
        recycleAllEmpsList.setAdapter(adapter);
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
                Toast.makeText(activity_follow_attendance.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public void onStart() {
        if (!isConnected()) {
            showInfoAlert();
        }
        super.onStart();
        getAttendanceData();

    }

    private void showInfoAlert() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(activity_follow_attendance.this, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
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