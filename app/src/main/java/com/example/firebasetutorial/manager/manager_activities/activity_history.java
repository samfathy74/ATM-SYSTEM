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

public class activity_history extends AppCompatActivity {
    RecyclerView recycleHistory;
    DatePicker datePickerHistory;
    RecycleVAdapterAttendance adapter;
    FirebaseAuth auth;
    DatabaseReference reference;
    ImageView imageRecycleNull;
    TextView txtViewHistory;
    FirebaseRecyclerOptions<Attendance> options;
    SharedPreferences preferences;
    SearchView searcher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_history);

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);

        if (auth.getCurrentUser() == null) {
            //home page
            startActivity(new Intent(getApplicationContext(), activity_login.class));
            finish();
        }

        txtViewHistory = findViewById(R.id.txtViewHistory);
        imageRecycleNull = findViewById(R.id.imageRecycleNull);
        datePickerHistory = findViewById(R.id.datePickerHistory);
        recycleHistory = findViewById(R.id.recycleHistory);
        searcher = findViewById(R.id.searcher);
        recycleHistory.setLayoutManager(new LinearLayoutManager(activity_history.this));

        String date = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Calendar.getInstance().getTime()).toString();
        getHistoryData(date);
        changeDateByDatePicker();

        txtViewHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (datePickerHistory.getVisibility() == View.GONE) {
                    datePickerHistory.setVisibility(View.VISIBLE);
                    TransitionManager.beginDelayedTransition(datePickerHistory);
                    txtViewHistory.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_flat_arrow_up, 0);
                } else {
                    datePickerHistory.setVisibility(View.GONE);
                    TransitionManager.beginDelayedTransition(datePickerHistory);
                    txtViewHistory.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_flat_arrow_down, 0);
                }
            }
        });
    }

    private void getHistoryData(String date) {
        if (date.isEmpty()) {
            Toast.makeText(activity_history.this, "failed date null", Toast.LENGTH_LONG).show();
        } else {
            searcher.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    searchIntoRecycle(s, date);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    searchIntoRecycle(s, date);
                    return false;
                }
            });

            try {
                preferences = getSharedPreferences("mprofileData", MODE_PRIVATE);
                String department = preferences.getString("Department", "");
                Query query = reference.child("Attendees").child(date).child(department);
                options = new FirebaseRecyclerOptions.Builder<Attendance>()
                        .setQuery(query, Attendance.class)
                        .build();
                adapter = new RecycleVAdapterAttendance(options);
                recycleHistory.setAdapter(adapter);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                        datePickerHistory.setVisibility(View.GONE);
                        if (dataSnapshot.exists()) {
                            adapter.startListening();
                            imageRecycleNull.setVisibility(View.GONE);
                            recycleHistory.setVisibility(View.VISIBLE);
                        } else {
                            imageRecycleNull.setVisibility(View.VISIBLE);
                            recycleHistory.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NotNull DatabaseError databaseError) {
                        Toast.makeText(activity_history.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                Toast.makeText(activity_history.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void changeDateByDatePicker() {
        Calendar calendar = Calendar.getInstance();
        Calendar.getInstance().setTimeInMillis(System.currentTimeMillis());
        datePickerHistory.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int dayOfMonth) {
                String date2 = datePickerHistory.getDayOfMonth() + "-" + (1 + datePickerHistory.getMonth()) + "-" + datePickerHistory.getYear();
                getHistoryData(date2);
                txtViewHistory.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_flat_arrow_down, 0);
            }
        });
    }

    private void searchIntoRecycle(String s, String date) {
        preferences = getSharedPreferences("mprofileData", MODE_PRIVATE);
        String department = preferences.getString("Department", "");
        Query queries = reference.child("Attendees").child(date).child(department).orderByChild("Email").startAt(s.toLowerCase()).endAt(s.toLowerCase() + "\uf8ff");
        options = new FirebaseRecyclerOptions.Builder<Attendance>()
                .setQuery(queries, Attendance.class).build();

        adapter = new RecycleVAdapterAttendance(options);
        recycleHistory.setAdapter(adapter);
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
                Toast.makeText(activity_history.this, error.getMessage(), Toast.LENGTH_LONG).show();
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
        AlertDialog.Builder builder1 = new AlertDialog.Builder(activity_history.this, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
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