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
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebasetutorial.R;
import com.example.firebasetutorial.activity_login;
import com.example.firebasetutorial.admin.adapters.RecycleVAdapterStaff;
import com.example.firebasetutorial.classes.Employees;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class activity_employee extends AppCompatActivity {

    RecyclerView recycleEmployeeList;
    LinearLayout LinerLayoutAdminEmpSearch,LinerLayoutAdminEmp;
    RecycleVAdapterStaff adapter;
    FirebaseAuth auth;
    DatabaseReference reference;
    ImageView imageAddStaff;
    FirebaseRecyclerOptions<Employees> options;
    SharedPreferences preferences;
    Query queries;
    SearchView searcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_employee);

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);

        if (auth.getCurrentUser() == null) {
            //login page
            startActivity(new Intent(activity_employee.this, activity_login.class));
            finish();
        }

        LinerLayoutAdminEmp = findViewById(R.id.LinerLayoutAdminEmp);
        LinerLayoutAdminEmpSearch = findViewById(R.id.LinerLayoutAdminEmpSearch);

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

        imageAddStaff = findViewById(R.id.imageAddStaff);
        recycleEmployeeList = findViewById(R.id.recycleEmployeeList);

        recycleEmployeeList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        imageAddStaff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(activity_employee.this, activity_create_accounts.class));
            }
        });

    }

    private void showDepartEmployees() {
        try {
            preferences = getSharedPreferences("DepartData", Context.MODE_PRIVATE);
            String department = preferences.getString("Name","");

            queries = reference.child("User").child("Employee").orderByChild("Department").equalTo(department);
            options = new FirebaseRecyclerOptions.Builder<Employees>()
                    .setQuery(queries, Employees.class).build();

            adapter = new RecycleVAdapterStaff(options);
            recycleEmployeeList.setAdapter(adapter);

            queries.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        adapter.startListening();
                        LinerLayoutAdminEmp.setVisibility(View.GONE);
                    } else {
                        LinerLayoutAdminEmp.setVisibility(View.VISIBLE);
                        LinerLayoutAdminEmpSearch.setVisibility(View.GONE);
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(activity_employee.this, error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "problem " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void searchIntoRecycle(String s){
        queries = reference.child("User").child("Employee").orderByChild("Email").startAt(s.toLowerCase()).endAt(s.toLowerCase()+"\uf8ff");
        options = new FirebaseRecyclerOptions.Builder<Employees>()
                .setQuery(queries, Employees.class).build();

        adapter = new RecycleVAdapterStaff(options);
        recycleEmployeeList.setAdapter(adapter);
        queries.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    adapter.startListening();
                    LinerLayoutAdminEmpSearch.setVisibility(View.GONE);
                } else {
                    if(LinerLayoutAdminEmp.getVisibility()==View.VISIBLE){
                        LinerLayoutAdminEmpSearch.setVisibility(View.GONE);
                    }else{
                        LinerLayoutAdminEmpSearch.setVisibility(View.VISIBLE);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(activity_employee.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isConnected()) {
            showInfoAlert();
        }
        showDepartEmployees();
    }


    private void showInfoAlert() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(activity_employee.this, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
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