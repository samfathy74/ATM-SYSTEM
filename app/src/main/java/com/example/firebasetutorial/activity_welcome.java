package com.example.firebasetutorial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Html;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebasetutorial.admin.admin_activities.activity_admin_home;
import com.example.firebasetutorial.employee.employee_activities.activity_ehome;
import com.example.firebasetutorial.manager.manager_activities.activity_mhome;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class activity_welcome extends AppCompatActivity {
    Animation topanimation, bottomanimation;
    TextView welcomeTextView;
    //    ImageView welcomeImg;
    private FirebaseAuth auth;
    DatabaseReference reference;
    SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        //Animation
        topanimation = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomanimation = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        //get Elements;
        welcomeTextView = findViewById(R.id.textWelcome);

        //run animation
        welcomeTextView.setAnimation(bottomanimation);

        if (auth.getCurrentUser() == null) {
            //login page
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(activity_welcome.this, activity_login.class));
                    finish();
                }
            }, 3500);

        } else {
            //home page
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    usersLoginAs(auth.getCurrentUser().getUid());
                }
            }, 3500);
        }
    }

    private void getManagerBasicData() {
        final String uid = auth.getCurrentUser().getUid();
        reference.child("User").child("Manager").orderByChild(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
//                get data from db
                    String fname = snapshot.child(uid).child("First_Name").getValue(String.class);
                    String lname = snapshot.child(uid).child("Last_Name").getValue(String.class);
                    String department = snapshot.child(uid).child("Department").getValue(String.class);

                    preferences = getSharedPreferences("departmentWithName", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("Department", department);
                    editor.apply();
                    preferences = getSharedPreferences("mprofileData", MODE_PRIVATE);
                    SharedPreferences.Editor editor1 = preferences.edit();
                    editor1.putString("First_Name", fname);
                    editor1.putString("Last_Name", lname);
                    editor1.apply();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(activity_welcome.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getEmployeeBasicData() {
        final String uid = auth.getCurrentUser().getUid();
        reference.child("User").child("Employee").orderByChild(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
//                get data from db
                    String fname = snapshot.child(uid).child("First_Name").getValue(String.class);
                    String lname = snapshot.child(uid).child("Last_Name").getValue(String.class);
                    String department = snapshot.child(uid).child("Department").getValue(String.class);

                    preferences = getSharedPreferences("departmentWithName", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("Department", department);
                    editor.apply();
                    preferences = getSharedPreferences("eprofileData", MODE_PRIVATE);
                    SharedPreferences.Editor editor1 = preferences.edit();
                    editor1.putString("First_Name", fname);
                    editor1.putString("Last_Name", lname);
                    editor1.apply();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(activity_welcome.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void usersLoginAs(String id) {
        reference = FirebaseDatabase.getInstance().getReference();
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("User").child("Manager").child(id).exists()) {
                    getManagerBasicData();
                    startActivity(new Intent(activity_welcome.this, activity_mhome.class));
                    finish();
                } else if (snapshot.child("User").child("Employee").child(id).exists()) {
                    getEmployeeBasicData();
                    startActivity(new Intent(activity_welcome.this, activity_ehome.class));
                    finish();
                } else if (snapshot.child("Admin").child(id).exists()) {
                    startActivity(new Intent(activity_welcome.this, activity_admin_home.class));
                    finish();
                } else {
                    auth.signOut();
                    startActivity(new Intent(activity_welcome.this, activity_login.class));
                    finish();
                    Toast.makeText(getApplicationContext(), "This account does not belong to the system, it may have been deleted by the administrator ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "error in db " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

    public static void getSettings(Context context) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        SharedPreferences preferences = context.getSharedPreferences("Settings", MODE_PRIVATE);
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
                    editor.putString("PM_AMA", PM_AML);
                    editor.apply();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isConnected()) {
            showInfoAlert();
        }
    }
}