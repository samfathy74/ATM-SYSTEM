package com.example.firebasetutorial.admin.admin_activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.firebasetutorial.classes.NotificationHelper;
import com.example.firebasetutorial.R;
import com.example.firebasetutorial.activity_login;
import com.example.firebasetutorial.classes.ShPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static com.example.firebasetutorial.activity_welcome.getSettings;

public class activity_admin_home extends AppCompatActivity {

    FirebaseAuth auth;
    DatabaseReference reference;
    SharedPreferences preferences;
    CardView cardDepartment,cardAttendance,cardRequest;
    ImageView profImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);

        if (auth.getCurrentUser() == null) {
            //home page
            startActivity(new Intent(getApplicationContext(), activity_login.class));
            finish();
        }

        cardRequest = findViewById(R.id.cardRequest);
        cardAttendance = findViewById(R.id.cardAttendance);
        cardDepartment = findViewById(R.id.cardDepartment);
        profImg = findViewById(R.id.profImg);

        profImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(activity_admin_home.this,activity_admin_profile.class));
            }
        });

        cardDepartment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(activity_admin_home.this,activity_department.class));
            }
        });

        cardAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(activity_admin_home.this,activity_admin_attendance.class));
            }
        });

        cardRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(activity_admin_home.this,activity_admin_request.class));
            }
        });

        //NOTIFICATIONS
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            @SuppressLint("WrongConstant") NotificationChannel channel =
                    new NotificationChannel(NotificationHelper.CHANNEL_ID, NotificationHelper.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(NotificationHelper.CHANNEL_DEC);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        checkRequestsStatus();
        checkAttendeesStatus();
    }

    private void loadAdminDataToShared(){
        String userId = auth.getCurrentUser().getUid();
        preferences = getSharedPreferences("AdminData", MODE_PRIVATE);

        reference.child("Admin").orderByChild(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userEmail = snapshot.child(userId).child("Email").getValue(String.class);
                String pass = snapshot.child(userId).child("Password").getValue(String.class);

                SharedPreferences.Editor myEditor = preferences.edit();
                myEditor.putString("Email", userEmail);
                myEditor.putString("Password", pass);
                myEditor.apply();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(activity_admin_home.this, "problem"+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void prepareDepartment(Context context,DatabaseReference reference){
        ShPreferences.clearDataInListPreferences(context,"DepartmentsList","Departments");
            ArrayList<String> Depart_items = new ArrayList<>();
            reference.child("Departments").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String key = ds.getKey();
                        Depart_items.add(key);
                    }
                    if(!Depart_items.isEmpty()) {
                        ShPreferences.storeDataInListPreferences(context,Depart_items,"DepartmentsList","Departments");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }

    @Override
    public void onBackPressed() {
           AlertDialog.Builder builder1 = new AlertDialog.Builder(activity_admin_home.this, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
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

    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!isConnected()) {
            showInfoAlert();
        }
        prepareDepartment(getApplicationContext(),reference);
        loadAdminDataToShared();
        getSettings(getApplicationContext());

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

    private void checkRequestsStatus(){
        //CHECK THE REQUESTS ROOT ITSELF
        DatabaseReference checkRequests = FirebaseDatabase.getInstance().getReference();
        checkRequests.child("Requests").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                NotificationHelper.displayNotification(activity_admin_home.this, "ABOUT REQUESTS", "EMPLOYEE REQUEST STATUS CHANGED", activity_admin_request.class);
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void checkAttendeesStatus(){
        String currentDay = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Calendar.getInstance().getTime());

        DatabaseReference checkAttendees = FirebaseDatabase.getInstance().getReference();
        checkAttendees.child("Attendees").child(currentDay).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                NotificationHelper.displayNotification(getApplicationContext(), "EMPLOYEES ACTIVITY", "AN UPDATE IN ATTENDEES ACTIVITY", activity_admin_attendance.class);
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