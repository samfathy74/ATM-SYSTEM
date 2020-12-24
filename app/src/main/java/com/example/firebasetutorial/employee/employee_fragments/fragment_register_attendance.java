package com.example.firebasetutorial.employee.employee_fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.firebasetutorial.R;
import com.example.firebasetutorial.activity_login;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import pl.droidsonroids.gif.GifImageView;

import static android.content.Context.MODE_PRIVATE;
import static com.example.firebasetutorial.activity_welcome.getSettings;

public class fragment_register_attendance extends Fragment {

    // VARIABLES INITIALIZATION
    TextView showTime;
    Button attendanceBtn;
    TextView showInfo;
    GifImageView waitingImage;
    int TIME_DELAY = 1000;
    int START_TIME_IN_HOURS;
    int START_TIME_IN_MINUTES;
    int END_TIME_IN_HOURS;
    int END_TIME_IN_MINUTES;
    String STATUS;
    Location location;
    LocationManager locationManager;
    double latitude;
    double longitude;
    SharedPreferences preferences;
    FusedLocationProviderClient locationProviderClient;

    FirebaseAuth auth;
    DatabaseReference reference;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_attendance, container, false);
        //ASSIGN VARIABLES TO THEW ITEMS
        assignVaribles(view);

        if (auth.getCurrentUser() == null) {
            //login page
            startActivity(new Intent(getActivity(), activity_login.class));
            getActivity().finish();
        }

        attendanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("Please wait, this process may take a serval minutes");
                progressDialog.create();
                progressDialog.show();
                get_Location(reference, progressDialog);
            }
        });

        preferences = getActivity().getSharedPreferences("Settingss", MODE_PRIVATE);
        START_TIME_IN_HOURS = Integer.parseInt(preferences.getString("StartHourA", ""));
        START_TIME_IN_MINUTES = Integer.parseInt(preferences.getString("StartMinA", ""));
        END_TIME_IN_HOURS = Integer.parseInt(preferences.getString("EndHourA", ""));
        END_TIME_IN_MINUTES = Integer.parseInt(preferences.getString("EndMinA", ""));
        STATUS = preferences.getString("PM_AMA", "AM");
        getUpdates();
        return view;
    }

    private void assignVaribles(View view) {
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);

        showTime = (TextView) view.findViewById(R.id.currentTime);
        attendanceBtn = (Button) view.findViewById(R.id.attendanceBtn);
        showInfo = (TextView) view.findViewById(R.id.currentInfo);
        waitingImage = (GifImageView) view.findViewById(R.id.waitingImage);
        locationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
    }

    //TIME UPDATER
    @SuppressLint("SetTextI18n")
    public void getUpdates() {

        Calendar calendar = Calendar.getInstance();

        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat hoursFormat = new SimpleDateFormat("hh", Locale.ENGLISH);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat minutesFormat = new SimpleDateFormat("mm", Locale.ENGLISH);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dayFormat = new SimpleDateFormat("a", Locale.ENGLISH);
        int hours = Integer.parseInt(hoursFormat.format(calendar.getTime()));
        int minutes = Integer.parseInt(minutesFormat.format(calendar.getTime()));
        String day = dayFormat.format(calendar.getTime());

        if ((hours >= START_TIME_IN_HOURS && minutes >= START_TIME_IN_MINUTES) && (hours <= END_TIME_IN_HOURS && minutes <= END_TIME_IN_MINUTES) && (day.equals(STATUS))) {
            attendanceBtn.setEnabled(true);
            attendanceBtn.setVisibility(View.VISIBLE);
            showInfo.setVisibility(View.GONE);
            waitingImage.setVisibility(View.GONE);

        } else {
            showInfo.setVisibility(View.VISIBLE);
            attendanceBtn.setEnabled(false);
            attendanceBtn.setVisibility(View.GONE);
            waitingImage.setVisibility(View.VISIBLE);
        }

        String currentTime = simpleDateFormat.format(calendar.getTime());
        showTime.setText("Attendance registration will open at the time:\n"+START_TIME_IN_HOURS+":"+END_TIME_IN_MINUTES+" "+STATUS);
        refresh(TIME_DELAY);
    }

    //TO GET REFRESH EVERY 1 SECOND
    private void refresh(int timeDelay) {
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                getUpdates();
            }
        };
        handler.postDelayed(runnable, timeDelay);
    }


    //GET USERS DATA AND INSERT DATA
    private void insertLeavingData(String addressLocation, DatabaseReference reference, ProgressDialog progressDialog) {
        String userId = auth.getCurrentUser().getUid();
        String user_email = auth.getCurrentUser().getEmail();
        String currentDay = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Calendar.getInstance().getTime());
        String currentDateTime = new SimpleDateFormat("(EEEE) dd/MM/yyyy - hh:mm:ss a", Locale.ENGLISH).format(Calendar.getInstance().getTime()).toString();
//        String addressLocation = getActivity().getIntent().getStringExtra("location");

        if (!addressLocation.isEmpty()) {
            reference.child("User").child("Employee").orderByChild(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String fullName = snapshot.child(userId).child("First_Name").getValue(String.class) + " " + snapshot.child(userId).child("Last_Name").getValue(String.class);
                    String Department = snapshot.child(userId).child("Department").getValue(String.class);

                    //DATABASE CODE
                    reference.child("Attendees").child(currentDay).child(Department).child(user_email.replace(".", ",")).child("Email").setValue(user_email);
                    reference.child("Attendees").child(currentDay).child(Department).child(user_email.replace(".", ",")).child("Name").setValue(fullName);
                    reference.child("Attendees").child(currentDay).child(Department).child(user_email.replace(".", ",")).child("Department").setValue(Department);
                    reference.child("Attendees").child(currentDay).child(Department).child(user_email.replace(".", ",")).child("Status").setValue("Available");
                    reference.child("Attendees").child(currentDay).child(Department).child(user_email.replace(".", ",")).child("DateTime_of_Attendance").setValue(currentDateTime);
                    reference.child("Attendees").child(currentDay).child(Department).child(user_email.replace(".", ",")).child("DateTime_Out_Work").setValue("");
                    reference.child("Attendees").child(currentDay).child(Department).child(user_email.replace(".", ",")).child("Leave_Location").setValue("");
                    reference.child("Attendees").child(currentDay).child(Department).child(user_email.replace(".", ",")).child("Attendance_Location").setValue(addressLocation);

                    progressDialog.setMessage("Your attendance has been successfully registered");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                        }
                    }, 1000);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(getActivity(), "Your device may not support GPS or make sure GPS location settings is enabled", Toast.LENGTH_LONG).show();
            get_Location(reference, progressDialog);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // SAVE CURRENT LOCATION
    @SuppressLint("MissingPermission")
    private void get_Location(DatabaseReference reference, ProgressDialog progressDialog) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            //check provider(gps&internet is worked
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Geocoder geocoder = new Geocoder(getActivity(), Locale.ENGLISH);
                        location = task.getResult();
                        //re-get getLocation
                        LocationRequest locationRequest = new LocationRequest();
                        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                .setInterval(10000)
                                .setFastestInterval(1000)
                                .setNumUpdates(1);
                        LocationCallback callback = new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                super.onLocationResult(locationResult);
                                location = locationResult.getLastLocation();
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                String addressLine = null;
                                try {
                                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                                    progressDialog.setMessage("Confirm Request");
                                    addressLine = addresses.get(0).getAddressLine(0);
                                    insertLeavingData(addressLine, reference, progressDialog);
                                } catch (IOException e) {
                                    progressDialog.setMessage("Confirm Request");
                                    addressLine = "Latitude & Longitude " + latitude + "," + longitude;
                                    insertLeavingData(addressLine, reference, progressDialog);
                                }
                            }
                        };
                        //request mean getLocation update
                        locationProviderClient.requestLocationUpdates(locationRequest, callback, Looper.myLooper());
                    }

                });

                //provider is disabled go to enable it
            } else {
                progressDialog.dismiss();
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                } else if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                } else {
                    Toast.makeText(getActivity(), "Sure enable GPS and WI-FI Network", Toast.LENGTH_LONG).show();
                }
            }

        } else {
            progressDialog.dismiss();
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 90);
        }
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