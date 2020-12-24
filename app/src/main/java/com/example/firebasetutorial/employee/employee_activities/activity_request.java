package com.example.firebasetutorial.employee.employee_activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.Html;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class activity_request extends AppCompatActivity {
    TextInputLayout casesText;
    Button sendRequestInfo;
    Spinner spinnerRequests;
    Location location;
    LocationManager locationManager;
    double latitude;
    double longitude;
    FusedLocationProviderClient locationProviderClient;
    DatabaseReference reference;
    DatabaseReference reference2,reference3;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emp_request);
        //ASSIGN VARIABLES TO THEW ITEMS
        assignVaribles();

        if (auth.getCurrentUser() == null) {
            //home page
            startActivity(new Intent(getApplicationContext(), activity_login.class));
            finish();
        }

        String[] spinnerArr = {"Select Request Type", "Late Arrival", "Leave Early", "Sick Leave", "Vacation", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity_request.this, android.R.layout.simple_spinner_item, spinnerArr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRequests.setAdapter(adapter);


        sendRequestInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkHasAlreadyRequest();
            }
        });

        closeKeyboard();
    }

    private void assignVaribles() {
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        reference2 = FirebaseDatabase.getInstance().getReference();
        reference3 = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);

        casesText = (TextInputLayout) findViewById(R.id.casesText);
        sendRequestInfo = (Button) findViewById(R.id.sendRequestBtn);
        spinnerRequests = (Spinner) findViewById(R.id.requestSpinner);
        locationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // GET CURRENT LOCATION
    @SuppressLint("MissingPermission")
    private void get_Location() {
        if (ActivityCompat.checkSelfPermission(activity_request.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity_request.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            //check provider(gps&internet is worked
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.ENGLISH);
                        location = task.getResult();
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
                                    addressLine = addresses.get(0).getAddressLine(0);
                                    getIntent().putExtra("location", addressLine);
                                } catch (IOException e) {
                                    addressLine = "Latitude & Longitude " + latitude + "," + longitude;
                                    getIntent().putExtra("location", addressLine);
                                }
                            }
                        };
                        //request mean getLocation update
                        locationProviderClient.requestLocationUpdates(locationRequest, callback, Looper.myLooper());
                    }
//                    }
                });
                //provider is disabled go to enable it
            } else {
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                } else if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                } else {
                    Toast.makeText(getApplicationContext(), "Sure enable GPS and WI-FI Network", Toast.LENGTH_LONG).show();
                }
            }

        } else {
            ActivityCompat.requestPermissions(activity_request.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION}, 90);
        }
    }


    //GET USERS DATA AND INSERT DATA
    private void get_and_set_CurrentUserData(DatabaseReference reference, String requestCase, String requestType) {
        String userId = auth.getCurrentUser().getUid();
        String user_email = auth.getCurrentUser().getEmail();
        String addressLocation = getIntent().getStringExtra("location");

        if (!addressLocation.equals("null")) {
            if (spinnerRequests.getSelectedItemId() == 0) {
                Toast.makeText(getApplicationContext(), "Please Select Request Type", Toast.LENGTH_LONG).show();
            } else if (casesText.getEditText().getText().length() <= 5) {
                casesText.setError("Request case is too short");
            } else {
                reference3.child("User").child("Employee").orderByChild(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String fullName = snapshot.child(userId).child("First_Name").getValue(String.class) + " " + snapshot.child(userId).child("Last_Name").getValue(String.class);
                        String userDepartment = snapshot.child(userId).child("Department").getValue(String.class);
                        String currentDateTime = new SimpleDateFormat("(EEEE) dd/MM/yyyy - hh:mm:ss a", Locale.ENGLISH).format(Calendar.getInstance().getTime()).toString();

                        //REQUEST BASIC INFO
                        reference.child("Requests").child(userDepartment).child(user_email.replace(".", ",")).child("Request_Type").setValue(requestType);
                        reference.child("Requests").child(userDepartment).child(user_email.replace(".", ",")).child("Request_Case").setValue(requestCase);
                        reference.child("Requests").child(userDepartment).child(user_email.replace(".", ",")).child("RequestCode").setValue("0");
                        reference.child("Requests").child(userDepartment).child(user_email.replace(".", ",")).child("Location").setValue(addressLocation);
                        //DATABASE CODE
                        reference.child("Requests").child(userDepartment).child(user_email.replace(".", ",")).child("Email").setValue(user_email);
                        reference.child("Requests").child(userDepartment).child(user_email.replace(".", ",")).child("Name").setValue(fullName);
                        reference.child("Requests").child(userDepartment).child(user_email.replace(".", ",")).child("Department").setValue(userDepartment);
                        reference.child("Requests").child(userDepartment).child(user_email.replace(".", ",")).child("Signature").setValue("NULL");
                        reference.child("Requests").child(userDepartment).child(user_email.replace(".", ",")).child("DateTime_Requested").setValue(currentDateTime);

                        casesText.getEditText().setText("");
                        spinnerRequests.setSelection(0, true);//setSelection(0);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

        } else {
            get_Location();
        }
    }

    private void checkHasAlreadyRequest() {
        SharedPreferences preferences = getSharedPreferences("eprofileData", MODE_PRIVATE);
        String department = preferences.getString("Department", "");

        String email = auth.getCurrentUser().getEmail().replace(".", ",");

        if (!department.isEmpty()) {
            reference2.child("Requests").child(department).child(email).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String requestCode = snapshot.child("RequestCode").getValue(String.class);

                        assert requestCode != null;
                        if (requestCode.equals("0")) {
                            Snackbar.make(findViewById(android.R.id.content), "Your request has't been answered, if you want to delete and add new request", Snackbar.LENGTH_LONG)
                                    .setActionTextColor(Color.RED)
                                    .setAction("Del", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            reference2.child("Requests").child(department).child(email).setValue(null);
                                        }
                                    }).show();
                        } else {
                            reference2.child("Requests").child(department).child(email).setValue(null);
                            String requestCase = casesText.getEditText().getText().toString();
                            String requestType = spinnerRequests.getSelectedItem().toString();
                            get_and_set_CurrentUserData(reference, requestCase, requestType);
                        }
                    } else {
                        String requestCase = casesText.getEditText().getText().toString();
                        String requestType = spinnerRequests.getSelectedItem().toString();
                        get_and_set_CurrentUserData(reference, requestCase, requestType);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isConnected()) {
            showInfoAlert();
        }
            get_Location();
            getIntent().putExtra("location", "null");
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

    private void closeKeyboard()
    {
        View view = this.getCurrentFocus();

        if (view != null) {

            InputMethodManager manager
                    = (InputMethodManager)
                    getSystemService(
                            Context.INPUT_METHOD_SERVICE);
            manager
                    .hideSoftInputFromWindow(
                            view.getWindowToken(), 0);
        }
    }


    public void onBackPressing(View view) {
        finish();
    }
}