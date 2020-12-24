package com.example.firebasetutorial.manager.manager_fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.firebasetutorial.R;
import com.example.firebasetutorial.activity_login;
import com.example.firebasetutorial.manager.adapters.RecycleVAdapterEmployee;
import com.example.firebasetutorial.classes.Employees;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import static android.content.Context.MODE_PRIVATE;

public class fragment_memployees extends Fragment {

    RecyclerView empsListRecyclerView;
    RecycleVAdapterEmployee adapter;
    DatabaseReference reference;
    FirebaseRecyclerOptions<Employees> options;
    FirebaseAuth auth;
    ImageView imageRecycleEmpNull;
    SharedPreferences prefs;
    SearchView searcher;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);

        if (auth.getCurrentUser() == null) {
            //home page
            startActivity(new Intent(getActivity(), activity_login.class));
            getActivity().finish();
        }

        View view = inflater.inflate(R.layout.fragment_memployees, container, false);

        imageRecycleEmpNull = view.findViewById(R.id.imageRecycleEmpNull);
        searcher = view.findViewById(R.id.searcher);
        empsListRecyclerView = view.findViewById(R.id.recycleEmpsList);
        empsListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        showDepartEmployees();
        return view;
    }

    private void showDepartEmployees() {
        prefs = getActivity().getSharedPreferences("mprofileData", MODE_PRIVATE);
        String department = prefs.getString("Department", "");

        if (department.isEmpty()) {
            Toast.makeText(getActivity(), "failed to get department is null", Toast.LENGTH_LONG).show();
        } else {
            try {

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

                Query queries = reference.child("User").child("Employee").orderByChild("Department").equalTo(department);
                options = new FirebaseRecyclerOptions.Builder<Employees>()
                        .setQuery(queries, Employees.class)
                        .build();
                adapter = new RecycleVAdapterEmployee(options);
                empsListRecyclerView.setAdapter(adapter);
                queries.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            adapter.startListening();
                            imageRecycleEmpNull.setVisibility(View.GONE);
                        } else {
                            imageRecycleEmpNull.setVisibility(View.VISIBLE);
                        }
                        adapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getActivity(), "there is problem00", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Toast.makeText(getActivity(), "problem " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void searchIntoRecycle(String s) {
        Query queries = reference.child("User").child("Employee").orderByChild("Email").startAt(s.toLowerCase()).endAt(s.toLowerCase() + "\uf8ff");
        options = new FirebaseRecyclerOptions.Builder<Employees>()
                .setQuery(queries, Employees.class).build();

        adapter = new RecycleVAdapterEmployee(options);
        empsListRecyclerView.setAdapter(adapter);
        queries.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    adapter.startListening();
                    imageRecycleEmpNull.setVisibility(View.GONE);
                } else {
                        imageRecycleEmpNull.setVisibility(View.VISIBLE);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public void onStart() {
        if (!isConnected()) {
            showInfoAlert();
        }
        super.onStart();
        showDepartEmployees();
    }

    private void showInfoAlert() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity(), R.style.Theme_MaterialComponents_Light_Dialog_Alert).setCancelable(false);
        builder1.setMessage(Html.fromHtml("<font color='#FF0000'>There is a problem, the internet connection is weak or closed!</font>"));
        builder1.setCancelable(false);
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
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo dataConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiConn.isConnected() && wifiConn != null && wifiConn.isAvailable() || dataConn != null && dataConn.isConnected() && wifiConn.isAvailable()) {
            return true;
        } else {
            return false;
        }
    }

}
