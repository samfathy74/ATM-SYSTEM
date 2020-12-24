package com.example.firebasetutorial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.Button;

import android.widget.TextView;
import android.widget.Toast;


import com.example.firebasetutorial.admin.admin_activities.activity_create_accounts;
import com.example.firebasetutorial.manager.manager_activities.activity_mhome;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class activity_login extends AppCompatActivity {
    private TextInputLayout etxtEmail, etxtPass, editOtherPartEmail;
    private FirebaseAuth auth;
    SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etxtEmail = findViewById(R.id.editEmail);
        editOtherPartEmail = findViewById(R.id.editOtherPartEmail);
        etxtPass = findViewById(R.id.editPass);
        Button btnLogin = findViewById(R.id.btnLogin);


        auth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email;
                if (etxtEmail.getEditText().getText().toString().contains("@firemail.com")) {
                    email = etxtEmail.getEditText().getText().toString();
                } else {
                    email = etxtEmail.getEditText().getText().toString() + editOtherPartEmail.getEditText().getText().toString();
                }
                final String password = etxtPass.getEditText().getText().toString();
                if (etxtEmail.getEditText().getText().toString().isEmpty()) {
                    etxtEmail.setError("enter correct username of email to login");
                } else if (password.isEmpty()) {
                    etxtPass.setError("enter correct password to login");
                } else {
                    etxtEmail.setError(null);
                    etxtPass.setError(null);
                    LoginUser(email, password);
                }
            }
        });
    }

    //login method
    private void LoginUser(String email, String password) {
        preferences = getSharedPreferences("eprofileData", MODE_PRIVATE) ;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("First_Name", null);
        editor.putString("Last_Name", null);

        preferences = getSharedPreferences("mprofileData", MODE_PRIVATE);
        SharedPreferences.Editor editor1 = preferences.edit();
        editor1.putString("First_Name", null);
        editor1.putString("Last_Name", null);

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    editor.apply();
                    editor1.apply();
                    startActivity(new Intent(activity_login.this, activity_welcome.class));
                    finish();
                } else {
                    Toast.makeText(activity_login.this, "Email or Password not correct\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
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
        AlertDialog.Builder builder1 = new AlertDialog.Builder(activity_login.this, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
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
}