package com.example.firebasetutorial.manager.manager_fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.firebasetutorial.R;
import com.example.firebasetutorial.activity_login;
import com.example.firebasetutorial.manager.adapters.RecycleVAdapterNotification;
import com.example.firebasetutorial.classes.Notification;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import static android.content.Context.MODE_PRIVATE;

public class fragment_mnotify extends Fragment {

    RecyclerView recycleNotifyList;
    RecycleVAdapterNotification adapter;
    DatabaseReference reference;
    FirebaseRecyclerOptions<Notification> options;
    FirebaseAuth auth;
    LinearLayout linerLayoutEmptyNotify;
    SharedPreferences prefs;
    SearchView searcher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);

        if (auth.getCurrentUser() == null) {
            //home page
            startActivity(new Intent(getActivity(), activity_login.class));
            getActivity().finish();
        }

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mnotify, container, false);

        searcher = view.findViewById(R.id.searcher);
        linerLayoutEmptyNotify = view.findViewById(R.id.LinerLayoutEmptyNotify);
        recycleNotifyList = view.findViewById(R.id.recycleNotifyList);
        recycleNotifyList.setLayoutManager(new LinearLayoutManager(getActivity()));

        showDepartEmployees();
        return view;
    }

    private void showDepartEmployees() {
        prefs = getActivity().getSharedPreferences("mprofileData", MODE_PRIVATE);
        String department = prefs.getString("Department", "");

        if (department.isEmpty()) {
            Toast.makeText(getActivity(), "failed to get department is null", Toast.LENGTH_LONG).show();
        } else {
            searcher.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    searchIntoRecycle(s, department);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    searchIntoRecycle(s, department);
                    return false;
                }
            });

            try {
                Query queries = reference.child("Requests").child(department);
                options = new FirebaseRecyclerOptions.Builder<Notification>()
                        .setQuery(queries, Notification.class)
                        .build();
                adapter = new RecycleVAdapterNotification(options);
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
                        Toast.makeText(getActivity(), "there is problem00", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Toast.makeText(getActivity(), "problem " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void searchIntoRecycle(String s, String depart) {
        Query queries = reference.child("Requests").child(depart).orderByChild("Email").startAt(s.toLowerCase()).endAt(s.toLowerCase() + "\uf8ff");
        options = new FirebaseRecyclerOptions.Builder<Notification>()
                .setQuery(queries, Notification.class).build();

        adapter = new RecycleVAdapterNotification(options);
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
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity(), R.style.Theme_MaterialComponents_Light_Dialog_Alert);
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
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo dataConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiConn.isConnected() && wifiConn != null && wifiConn.isAvailable() || dataConn != null && dataConn.isConnected() && dataConn.isAvailable()) {
            return true;
        } else {
            return false;
        }
    }
}