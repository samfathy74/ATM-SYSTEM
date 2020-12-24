package com.example.firebasetutorial.employee.employee_fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebasetutorial.R;
import com.example.firebasetutorial.activity_login;
import com.example.firebasetutorial.employee.employee_activities.activity_random_question;
import com.example.firebasetutorial.employee.employee_activities.activity_request_history;
import com.example.firebasetutorial.employee.employee_activities.activity_request;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.content.Context.MODE_PRIVATE;

public class fragment_ehome extends Fragment {
    CardView cardReqests, cardReqestHistory;
    TextView txtWelcomeName;
    FirebaseAuth auth;
    DatabaseReference reference;
    SharedPreferences prefs;
    String fname, lname;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ehome, container, false);

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);

        if (auth.getCurrentUser() == null) {
            //login page
            startActivity(new Intent(getActivity(), activity_login.class));
            getActivity().finish();
        }

        cardReqests = view.findViewById(R.id.cardReqests);
        cardReqestHistory = view.findViewById(R.id.cardReqestHistory);
        txtWelcomeName = view.findViewById(R.id.tvWelcomeName);

        prefs = getActivity().getSharedPreferences("eprofileData", MODE_PRIVATE);
        fname = prefs.getString("First_Name", ""); //"No name defined" is the default value.
        lname = prefs.getString("Last_Name", ""); //"No name defined" is the default value.
        txtWelcomeName.setText(fname + " " + lname);

        cardReqests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    prefs = getActivity().getSharedPreferences("eprofileData", MODE_PRIVATE);
                    fname = prefs.getString("First_Name", ""); //"No name defined" is the default value.
                    lname = prefs.getString("Last_Name", ""); //"No name defined" is the default value.
                    txtWelcomeName.setText(fname + " " + lname);

                    startActivity(new Intent(getActivity(), activity_request.class));
                } catch (Exception e) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        cardReqestHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    prefs = getActivity().getSharedPreferences("eprofileData", MODE_PRIVATE);
                    fname = prefs.getString("First_Name", ""); //"No name defined" is the default value.
                    lname = prefs.getString("Last_Name", ""); //"No name defined" is the default value.
                    txtWelcomeName.setText(fname + " " + lname);

                    startActivity(new Intent(getActivity(), activity_request_history.class));
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "problem when start history " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isConnected()) {
            showInfoAlert();
        }
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
        if (wifiConn.isConnected() && wifiConn != null && wifiConn.isAvailable() || dataConn != null && dataConn.isConnected() && wifiConn.isAvailable()) {
            return true;
        } else {
            return false;
        }
    }

}

