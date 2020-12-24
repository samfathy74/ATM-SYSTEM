package com.example.firebasetutorial.admin.admin_activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.firebasetutorial.R;
import com.example.firebasetutorial.activity_login;
import com.example.firebasetutorial.admin.adapters.RecycleVAdapterDepartment;
import com.example.firebasetutorial.classes.Departments;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import static com.example.firebasetutorial.admin.admin_activities.activity_admin_home.prepareDepartment;

public class activity_department extends AppCompatActivity {
    FirebaseAuth auth;
    DatabaseReference reference;

    RecyclerView departsRecycler;
    LinearLayout LinerLayoutAdminDepartment, LinerLayoutAdminDepartmentSearch;
    FirebaseRecyclerOptions<Departments> options;
    RecycleVAdapterDepartment adapter;
    ImageView imgAddDepartment;
    Query query, queries;
    SearchView searcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_department);

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);

        if (auth.getCurrentUser() == null) {
            //home page
            startActivity(new Intent(getApplicationContext(), activity_login.class));
            finish();
        }

        LinerLayoutAdminDepartment = findViewById(R.id.LinerLayoutAdminDepartment);
        LinerLayoutAdminDepartmentSearch = findViewById(R.id.LinerLayoutAdminDepartmentSearch);

        imgAddDepartment = findViewById(R.id.imgAddDepartment);
        imgAddDepartment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(activity_department.this, activity_create_department.class));
            }
        });

        searcher = findViewById(R.id.searcher);
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


        departsRecycler = findViewById(R.id.depRecycler);
        departsRecycler.setLayoutManager(new LinearLayoutManager(this));

        query = reference.child("Departments");
        options = new FirebaseRecyclerOptions.Builder<Departments>()
                .setQuery(query, Departments.class)
                .build();

        adapter = new RecycleVAdapterDepartment(options);
        departsRecycler.setAdapter(adapter);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    LinerLayoutAdminDepartment.setVisibility(View.GONE);
                    adapter.startListening();
                } else {
                    LinerLayoutAdminDepartment.setVisibility(View.VISIBLE);
                    LinerLayoutAdminDepartmentSearch.setVisibility(View.GONE);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(activity_department.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onBackPressing(View view) {
        finish();
    }

    private void searchIntoRecycle(String s) {
        queries = reference.child("Departments").orderByChild("Name").startAt(s).endAt(s + "\uf8ff");
        options = new FirebaseRecyclerOptions.Builder<Departments>()
                .setQuery(queries, Departments.class).build();
        adapter = new RecycleVAdapterDepartment(options);
        departsRecycler.setAdapter(adapter);
        queries.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    adapter.startListening();
                    LinerLayoutAdminDepartmentSearch.setVisibility(View.GONE);
                } else {
                    LinerLayoutAdminDepartmentSearch.setVisibility(View.VISIBLE);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(activity_department.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!isConnected()) {
            showInfoAlert();
        }
        prepareDepartment(getApplicationContext(), reference);
        adapter.startListening();
    }

    private void showInfoAlert() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
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
        if (wifiConn.isConnected() && wifiConn != null && wifiConn.isAvailable() || dataConn != null && dataConn.isConnected() && wifiConn.isAvailable()) {
            return true;
        } else {
            return false;
        }
    }

}