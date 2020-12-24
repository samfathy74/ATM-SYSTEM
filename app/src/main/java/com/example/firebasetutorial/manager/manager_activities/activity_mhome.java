package com.example.firebasetutorial.manager.manager_activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;

import com.example.firebasetutorial.classes.NotificationHelper;
import com.example.firebasetutorial.R;
import com.example.firebasetutorial.activity_login;
import com.example.firebasetutorial.manager.manager_fragments.fragment_memployees;
import com.example.firebasetutorial.manager.manager_fragments.fragment_mhome;
import com.example.firebasetutorial.manager.manager_fragments.fragment_mnotify;
import com.example.firebasetutorial.manager.manager_fragments.fragment_mprofile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class activity_mhome extends AppCompatActivity {

    ChipNavigationBar chipNavigationBar;
    FirebaseAuth auth;
    DatabaseReference reference;

    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mhome);

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);

        if (auth.getCurrentUser() == null) {
            //login page
            startActivity(new Intent(activity_mhome.this, activity_login.class));
            finish();
        }

        chipNavigationBar = findViewById(R.id.mainBottomNav);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new fragment_mhome()).commit();
        chipNavigationBar.setItemSelected(R.id.mHome, true);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            @SuppressLint("WrongConstant") NotificationChannel channel =
                    new NotificationChannel(NotificationHelper.CHANNEL_ID, NotificationHelper.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(NotificationHelper.CHANNEL_DEC);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        checkOldRequests();
        checkRequestsStatus();
        checkAttendeesStatus();
        checkRandomTest();

        chipNavigationBarContainer();
        getRequestCount();

    }

    private void chipNavigationBarContainer() {
        chipNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                Fragment fragment = null;
                switch (i) {
                    case R.id.mHome:
                        fragment = new fragment_mhome();
                        break;

                    case R.id.mEmployees:
                        fragment = new fragment_memployees();
                        break;

                    case R.id.mNotify:
                        fragment = new fragment_mnotify();
                        break;

                    case R.id.mProfile:
                        fragment = new fragment_mprofile();
                        break;
                    default:
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
            }
        });
    }

    private void getRequestCount(){
        SharedPreferences preferences = getSharedPreferences("mprofileData", MODE_PRIVATE);
        String department = preferences.getString("Department","");
        if(!department.isEmpty()) {
            reference.child("Requests").child(department).orderByChild("RequestCode").equalTo("0").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int requestCount = (int) snapshot.getChildrenCount();
                    if(requestCount<=0){
                        chipNavigationBar.dismissBadge(R.id.mNotify);
                    }else{
                        chipNavigationBar.showBadge(R.id.mNotify,requestCount);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }



    //method retrieve data
    private void getUsersDatafromDataBase() {
        final String uid = auth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference("User");
        reference.child("Manager").orderByChild(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get data from db
                String fname = snapshot.child(uid).child("First_Name").getValue(String.class);
                String lname = snapshot.child(uid).child("Last_Name").getValue(String.class);
                String email = snapshot.child(uid).child("Email").getValue(String.class);
                String pass = snapshot.child(uid).child("Password").getValue(String.class);
                String role = snapshot.child(uid).child("Role").getValue(String.class);
                String department = snapshot.child(uid).child("Department").getValue(String.class);
                String phone = snapshot.child(uid).child("Phone").getValue(String.class);

                //save data in temp vars
                SharedPreferences preferences = getSharedPreferences("mprofileData", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("First_Name", fname);
                editor.putString("Last_Name", lname);
                editor.putString("Email", email);
                editor.putString("Password", pass);
                editor.putString("Role", role);
                editor.putString("Department", department);
                editor.putString("Phone", phone);

                editor.apply();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    protected void onStart() {
        if (!isConnected()) {
            showInfoAlert();
        }
        super.onStart();
        getUsersDatafromDataBase();
    }

    @Override
    public void onBackPressed() {
        if (chipNavigationBar.getSelectedItemId() == R.id.mHome) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(activity_mhome.this, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
            builder1.setMessage(Html.fromHtml("<font color='#FF0000'>Do you want to exit ?</font>"));
            builder1.setPositiveButton(Html.fromHtml("Exit"), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder1.setNegativeButton(Html.fromHtml("No"), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder1.create();
            dialog.show();
        } else {
            chipNavigationBar.setItemSelected(R.id.mHome, true);
        }
    }
    
    private void showInfoAlert() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(activity_mhome.this, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
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
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo dataConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiConn.isConnected() && wifiConn != null && wifiConn.isAvailable() || dataConn != null && dataConn.isConnected() && wifiConn.isAvailable()) {
            return true;
        } else {
            return false;
        }
    }

    private void checkRequestsStatus(){
        //GET BASIC DATA
        SharedPreferences preferences = getSharedPreferences("mprofileData", MODE_PRIVATE);
        String userDepartment = preferences.getString("Department", "");

        DatabaseReference checkRequests = FirebaseDatabase.getInstance().getReference();
        checkRequests.child("Requests").child(userDepartment).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                NotificationHelper.displayNotification(getApplicationContext(), "NEW REQUESTS", "AN EMPLOYEE REQUEST UPDATED .. CHECK IT NOW", activity_mhome.class);
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                NotificationHelper.displayNotification(getApplicationContext(), "NEW REQUESTS", "AN EMPLOYEE REQUEST UPDATED .. CHECK IT NOW", activity_mhome.class);
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) { }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
    private void checkOldRequests(){
        SharedPreferences preferences = getSharedPreferences("mprofileData", MODE_PRIVATE);
        String department = preferences.getString("Department","");
        if(!department.isEmpty()) {
            reference.child("Requests").child(department).orderByChild("RequestCode").equalTo("0").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int requestCount = (int) snapshot.getChildrenCount();
                    if(requestCount<=0) {
                        chipNavigationBar.dismissBadge(R.id.mNotify);
                    }
                    else {
                        chipNavigationBar.showBadge(R.id.mNotify,requestCount);
                        NotificationHelper.displayNotification(getApplicationContext(), "ABOUT REQUESTS", "YOU MAY HAVE AN OLD REQUEST NEEDS RESPONSE", activity_mhome.class);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void checkAttendeesStatus(){
        SharedPreferences preferences = getSharedPreferences("mprofileData", MODE_PRIVATE);
        String userDepartment = preferences.getString("Department", "");
        String currentDay = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Calendar.getInstance().getTime());

        DatabaseReference checkAttendees = FirebaseDatabase.getInstance().getReference();
        checkAttendees.child("Attendees").child(currentDay).child(userDepartment).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                NotificationHelper.displayNotification(getApplicationContext(), "EMPLOYEES ACTIVITY", "AN UPDATE IN ATTENDEES ACTIVITY", activity_follow_attendance.class);
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void checkRandomTest(){
        SharedPreferences preferences = getSharedPreferences("mprofileData", MODE_PRIVATE);
        String userDepartment = preferences.getString("Department", "");

        DatabaseReference checkTest = FirebaseDatabase.getInstance().getReference();
        checkTest.child("RandomTest").child(userDepartment).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                NotificationHelper.displayNotification(getApplicationContext(), "ABOUT RANDOM TEST", "AN EMPLOYEE RESULT SUBMITTED", activity_follow_attentive.class);
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                NotificationHelper.displayNotification(getApplicationContext(), "ABOUT RANDOM TEST", "AN EMPLOYEE RESULT SUBMITTED", activity_follow_attentive.class);
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}