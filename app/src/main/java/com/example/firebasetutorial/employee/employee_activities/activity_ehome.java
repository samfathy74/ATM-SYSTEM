package com.example.firebasetutorial.employee.employee_activities;

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
import android.os.Handler;
import android.provider.Settings;
import android.text.Html;

import com.example.firebasetutorial.classes.NotificationHelper;
import com.example.firebasetutorial.R;
import com.example.firebasetutorial.activity_login;
import com.example.firebasetutorial.employee.employee_fragments.fragment_register_attendance;
import com.example.firebasetutorial.employee.employee_fragments.fragment_ehome;
import com.example.firebasetutorial.employee.employee_fragments.fragment_register_leave;
import com.example.firebasetutorial.employee.employee_fragments.fragment_emprofile;
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
import java.util.GregorianCalendar;
import java.util.Locale;

public class activity_ehome extends AppCompatActivity {

    ChipNavigationBar chipNavigationBar;
    FirebaseAuth auth;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ehome);
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);

        if (auth.getCurrentUser() == null) {
            //login page
            startActivity(new Intent(this, activity_login.class));
            finish();
        }

        chipNavigationBar = findViewById(R.id.navigationMenu);
        chipNavigationBar.setItemSelected(R.id.mainScreen, true);


        getSupportFragmentManager().beginTransaction().replace(R.id.container, new fragment_ehome()).commit();
        menuButton();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel channel =
                    new NotificationChannel(NotificationHelper.CHANNEL_ID, NotificationHelper.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(NotificationHelper.CHANNEL_DEC);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        checkRequestsStatus();
        getRandomQestion();
    }

    private void menuButton() {
        chipNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onItemSelected(int i) {
                Fragment fragment = null;
                switch (i) {
                    case R.id.mainScreen:
                        fragment = new fragment_ehome();
                        break;
                    case R.id.attend:
                        fragment = new fragment_register_attendance();
                        break;
                    case R.id.leave:
                        fragment = new fragment_register_leave();
                        break;
                    case R.id.profile:
                        fragment = new fragment_emprofile();
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
            }
        });
    }

    //method retrieve data
    private void getUsersDatafromDataBase() {
        final String uid = auth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference("User");
        reference.child("Employee").orderByChild(uid).addListenerForSingleValueEvent(new ValueEventListener() {
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
                SharedPreferences preferences = getSharedPreferences("eprofileData", MODE_PRIVATE);
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
    public void onBackPressed() {
        if (chipNavigationBar.getSelectedItemId() == R.id.mainScreen) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(activity_ehome.this, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
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
            chipNavigationBar.setItemSelected(R.id.mainScreen, true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        getSettingss();
        getUsersDatafromDataBase();
        if (!isConnected()) {
            showInfoAlert();
        }
    }

    public void getSettingss() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        SharedPreferences preferences = getSharedPreferences("Settingss", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        reference.child("Settings").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("AttendanceSettings").exists()) {
                    String StartHourA = snapshot.child("AttendanceSettings").child("StartHour").getValue(String.class);
                    String EndHourA = snapshot.child("AttendanceSettings").child("EndHour").getValue(String.class);
                    String StartMinA = snapshot.child("AttendanceSettings").child("StartMin").getValue(String.class);
                    String EndMinA = snapshot.child("AttendanceSettings").child("EndMin").getValue(String.class);
                    String PM_AMA = snapshot.child("AttendanceSettings").child("PM_AM").getValue(String.class);
                    //
                    editor.putString("StartHourA", StartHourA);
                    editor.putString("EndHourA", EndHourA);
                    editor.putString("StartMinA", StartMinA);
                    editor.putString("EndMinA", EndMinA);
                    editor.putString("PM_AMA", PM_AMA);
                    editor.apply();
                }
                if (snapshot.child("LeavingSettings").exists()) {
                    //leave time
                    String StartHourL = snapshot.child("LeavingSettings").child("StartHour").getValue(String.class);
                    String EndHourL = snapshot.child("LeavingSettings").child("EndHour").getValue(String.class);
                    String StartMinL = snapshot.child("LeavingSettings").child("StartMin").getValue(String.class);
                    String EndMinL = snapshot.child("LeavingSettings").child("EndMin").getValue(String.class);
                    String PM_AML = snapshot.child("LeavingSettings").child("PM_AM").getValue(String.class);
                    //
                    editor.putString("StartHourL", StartHourL);
                    editor.putString("EndHourL", EndHourL);
                    editor.putString("StartMinL", StartMinL);
                    editor.putString("EndMinL", EndMinL);
                    editor.putString("PM_AML", PM_AML);
                    editor.apply();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showInfoAlert() {
        android.app.AlertDialog.Builder builder1 = new android.app.AlertDialog.Builder(this, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
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
        android.app.AlertDialog dialog = builder1.create();
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

    void checkRequestsStatus() {
        //GET BASIC DATA
        SharedPreferences preferences = getSharedPreferences("eprofileData", MODE_PRIVATE);
        String userDepartment = preferences.getString("Department", "");
        String usEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        DatabaseReference checkRequests = FirebaseDatabase.getInstance().getReference();
        assert usEmail != null;
        checkRequests.child("Requests").child(userDepartment).child(usEmail.replace(".", ",")).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                NotificationHelper.displayNotification(activity_ehome.this, "ABOUT REQUEST", "THERE IS AN UPDATE CHECK IT NOW", activity_request_history.class);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void getRandomQestion() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dfDateTime = new SimpleDateFormat("hh:mm", Locale.ENGLISH);
        int hour = activity_ehome.randBetween(11, 11); //Hours will be displayed in between 9 to 22
        int min = activity_ehome.randBetween(0, 50);

        GregorianCalendar gc = new GregorianCalendar(0, 0, 1);
        gc.set(0, 0, 0, hour, min);
        String randTime = dfDateTime.format(gc.getTime());
        String x = dfDateTime.format(calendar.getTime());
        if (x.equals(randTime)) {
            NotificationHelper.displayNotification(activity_ehome.this, "QUESTION", "⚠Attention,\nYOU HAVE A NEW QUESTION TO LOOK AT", activity_random_question.class);
        } else {
            System.out.println("no thing : " + randTime + "--" + x);
        }
        refresh();
    }

    public static int randBetween(int start, int end) {
        return start + (int) Math.round(Math.random() * (end - start));
    }

    private void refresh() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getRandomQestion();
            }
        }, 1000);
    }


}