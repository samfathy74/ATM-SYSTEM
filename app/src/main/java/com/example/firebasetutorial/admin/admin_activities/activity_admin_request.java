package com.example.firebasetutorial.admin.admin_activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebasetutorial.R;
import com.example.firebasetutorial.activity_login;
import com.example.firebasetutorial.admin.adapters.RecycleVAdapterRequest;
import com.example.firebasetutorial.classes.ShPreferences;
import com.example.firebasetutorial.classes.Notification;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;


public class activity_admin_request extends AppCompatActivity {

    RecyclerView recycleNotifyList;
    RecycleVAdapterRequest adapter;
    DatabaseReference reference;
    FirebaseRecyclerOptions<Notification> options;
    FirebaseAuth auth;
    Spinner spinnerDepart;
    ImageView imgCloseSpinner;
    SearchView searcher;
    LinearLayout imgOpenSpinner, linerLayoutEmptyNotify;
    CardView cardSpinner;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_request);

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);

        if (auth.getCurrentUser() == null) {
            //home page
            startActivity(new Intent(getApplicationContext(), activity_login.class));
            finish();
        }

        linerLayoutEmptyNotify = findViewById(R.id.LinerLayoutEmptyNotify);
        searcher = findViewById(R.id.searcher);
        spinnerDepart = findViewById(R.id.spinnerDepart);
        imgCloseSpinner = findViewById(R.id.imgCloseSpinner);
        imgOpenSpinner = findViewById(R.id.imgOpenSpinner);
        cardSpinner = findViewById(R.id.cardSpinner);
        recycleNotifyList = findViewById(R.id.recycleNotifyList);
        recycleNotifyList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        getDepartmentList();
        collapseAndExpandView();
        changeDepartmentBySpinner();
    }

    private void getDepartmentList() {
        List<String> Depart_item = ShPreferences.readDataInListPreferences(getApplicationContext(), "DepartmentsList", "Departments");
        ArrayAdapter<String> sp_Depart_Aapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_hint_text, Depart_item);
        sp_Depart_Aapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepart.setAdapter(sp_Depart_Aapter);
    }

    private void changeDepartmentBySpinner() {
        spinnerDepart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String depart = parent.getSelectedItem().toString();
                getRequests(depart);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(activity_admin_request.this, "select department to show employee attendance ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void collapseAndExpandView() {
        imgCloseSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Transition transition = new Slide(Gravity.END);
                transition.setDuration(1000);
                transition.addTarget(R.id.cardSpinner);
                TransitionManager.beginDelayedTransition(cardSpinner, transition);
                cardSpinner.setVisibility(View.GONE);
                imgOpenSpinner.setVisibility(View.VISIBLE);
            }
        });

        imgOpenSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Transition transition = new Slide(Gravity.TOP);
                transition.setDuration(1000);
                transition.addTarget(R.id.cardSpinner);
                TransitionManager.beginDelayedTransition(cardSpinner, transition);
                cardSpinner.setVisibility(View.VISIBLE);
                imgOpenSpinner.setVisibility(View.GONE);
            }
        });
    }

    private void getRequests(String depart) {
        try {
            searcher.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    searchIntoRecycle(s,depart );
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    searchIntoRecycle(s, depart);
                    return false;
                }
            });

            Query queries = reference.child("Requests").child(depart);
            options = new FirebaseRecyclerOptions.Builder<Notification>()
                    .setQuery(queries, Notification.class)
                    .build();
            adapter = new RecycleVAdapterRequest(options);
            recycleNotifyList.setAdapter(adapter);
            queries.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        adapter.startListening();
                        linerLayoutEmptyNotify.setVisibility(View.GONE);
                    } else {
                        linerLayoutEmptyNotify.setVisibility(View.VISIBLE);
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

    private void searchIntoRecycle(String s, String depart) {
        Query queries = reference.child("Requests").child(depart).orderByChild("Email").startAt(s.toLowerCase()).endAt(s.toLowerCase() + "\uf8ff");
        options = new FirebaseRecyclerOptions.Builder<Notification>()
                .setQuery(queries, Notification.class).build();

        adapter = new RecycleVAdapterRequest(options);
        recycleNotifyList.setAdapter(adapter);
        queries.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    adapter.startListening();
                    linerLayoutEmptyNotify.setVisibility(View.GONE);
                } else {
                    if (linerLayoutEmptyNotify.getVisibility() == View.VISIBLE) {
                        linerLayoutEmptyNotify.setVisibility(View.GONE);
                    } else {
                        linerLayoutEmptyNotify.setVisibility(View.VISIBLE);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(activity_admin_request.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onStart() {
        if (!isConnected()) {
            showInfoAlert();
        }
        super.onStart();
        getRequests(spinnerDepart.getSelectedItem().toString());
    }

    public void onBackPressing(View view) {
        finish();
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
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo dataConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiConn.isConnected() && wifiConn != null && wifiConn.isAvailable() || dataConn != null && dataConn.isConnected() && dataConn.isAvailable()) {
            return true;
        } else {
            return false;
        }
    }
}