package com.example.firebasetutorial.admin.admin_activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Html;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebasetutorial.R;
import com.example.firebasetutorial.activity_login;
import com.example.firebasetutorial.classes.ShPreferences;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class activity_create_accounts extends AppCompatActivity {
    TextInputLayout etxtFname, etxtLname, etxtEmail, etxtPass, editFixedEmail;
    Button btnSignup, btnBulkSignup;
    Spinner spinnerDepart;
    private FirebaseAuth auth, auth1;
    private DatabaseReference reference;
    ArrayAdapter<String> sp_Depart_Aapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_create_accounts);
        auth = FirebaseAuth.getInstance();

        FirebaseOptions firebaseOptions = new FirebaseOptions.Builder()
                .setDatabaseUrl("https://fire-tutorial-bc8e4.firebaseio.com/")
                .setApiKey("AIzaSyB4yB4TJwqNgd2yqNZSy0BIpNxME8S1eFo")
                .setApplicationId("fire-tutorial-bc8e4").build();

        try {
            FirebaseApp myApp = FirebaseApp.initializeApp(getApplicationContext(), firebaseOptions, "AnyAppName");
            auth1 = FirebaseAuth.getInstance(myApp);
        } catch (IllegalStateException e) {
            auth1 = FirebaseAuth.getInstance(FirebaseApp.getInstance("AnyAppName"));
        }

        reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);

        if (auth.getCurrentUser() == null) {
            //login page
            startActivity(new Intent(getApplicationContext(), activity_login.class));
            finish();
        }

        DefinationVariables();

        List<String> Depart_item = ShPreferences.readDataInListPreferences(getApplicationContext(), "DepartmentsList", "Departments");
        Depart_item.add(0, "Select Department");
        sp_Depart_Aapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_hint_text, Depart_item);
        sp_Depart_Aapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepart.setAdapter(sp_Depart_Aapter);
        spinnerDepart.setSelection(0, true);


        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressDialog progressDialog = new ProgressDialog(activity_create_accounts.this);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setMessage("please wait...");

                final String email = etxtEmail.getEditText().getText().toString().trim() + editFixedEmail.getEditText().getText().toString().trim();
                final String pass = etxtPass.getEditText().getText().toString().trim();

                if (validateFName() && validateLName() && validateEmail() && validatePassword() && spinnerDepart.getSelectedItemId() != 0) {
                    progressDialog.show();
                    Registration(email, pass, progressDialog);
                }
            }
        });

        btnBulkSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder1 = new AlertDialog.Builder(activity_create_accounts.this, R.style.CustomAlertDialog).setCancelable(false);
                View view = getLayoutInflater().inflate(R.layout.alert_create_group_accounts, null);
                builder1.setTitle("");

                TextView title = view.findViewById(R.id.alertTitle);
                TextView msg = view.findViewById(R.id.alertMsg);
                EditText etxtCount = view.findViewById(R.id.etxtCount);
                Button btnConfirm = view.findViewById(R.id.btnConfirm);
                Button btnCancel = view.findViewById(R.id.btnCancel);
                Spinner spinnerD = view.findViewById(R.id.spinnerD);
                spinnerD.setAdapter(sp_Depart_Aapter);
                builder1.setView(view);

                ProgressDialog progressDialog = new ProgressDialog(activity_create_accounts.this);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setMessage("please wait...");

                btnConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (etxtCount.getText().toString().isEmpty() || Integer.parseInt(etxtCount.getText().toString()) < 1 || Integer.parseInt(etxtCount.getText().toString()) >= 30) {
                            etxtCount.setError("Enter a number between 1 and 30 to complete process");
                        } else if (spinnerD.getSelectedItemId() == 0) {
                            Toast.makeText(getApplicationContext(), "determine Department", Toast.LENGTH_SHORT).show();
                        } else {
                            progressDialog.show();
                            for (int i = 1; i <= Integer.parseInt(etxtCount.getText().toString().trim()); i++) {
                                new Handler().postDelayed(new Runnable() {
                                    public void run() {
                                        try {
                                            randomRegistration(spinnerD, progressDialog, etxtCount);
                                        } catch (Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(getApplicationContext(), "Refused " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }, 5000);
                            }
                        }
                    }
                });

                AlertDialog dialog1 = builder1.create();
                dialog1.show();

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog1.dismiss();
                    }
                });
            }
        });
    }


    private void DefinationVariables() {
        etxtFname = findViewById(R.id.editFname);
        etxtLname = findViewById(R.id.editLname);
        etxtEmail = findViewById(R.id.editEmail);
        editFixedEmail = findViewById(R.id.editFixedEmail);
        etxtPass = findViewById(R.id.editPass);

        btnSignup = findViewById(R.id.btnSignUp);
        btnBulkSignup = findViewById(R.id.btnBulkSignUp);

        spinnerDepart = findViewById(R.id.spinnerDepart);
    }

    private void Registration(String email, String pass, ProgressDialog progressDialog) {
        auth1.createUserWithEmailAndPassword(email.toLowerCase(), pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                String UserID = auth1.getUid();
                HashMap<String, Object> mapValues = new HashMap<>();
                mapValues.put("ID", UserID);
                mapValues.put("First_Name", etxtFname.getEditText().getText().toString());
                mapValues.put("Last_Name", etxtLname.getEditText().getText().toString());
                mapValues.put("Email", email.toLowerCase());
                mapValues.put("Password", pass);
                mapValues.put("Profile_Image", "gs://fire-tutorial-bc8e4.appspot.com/Profile_Images/null/Avatar.jpg");
                mapValues.put("Phone", "+20");
                mapValues.put("Department", spinnerDepart.getSelectedItem().toString());
                mapValues.put("Role", "Employee");
                mapValues.put("DateTime_of_last_update", "Data not updated until this moment");
                mapValues.put("DateTime_of_account_created", new SimpleDateFormat("dd/MM/yyyy - hh:mm:ss a", Locale.ENGLISH).format(Calendar.getInstance().getTime()).toString());
                if (task.isSuccessful()) {
                    try {
                        reference.child("User").child("Employee").child(UserID).setValue(mapValues);
                        auth1.signOut();
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "successfully to registration", Toast.LENGTH_SHORT).show();
                        ClearFields();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Failed to create account reason to\n " + task.getException(), Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void randomRegistration(Spinner spinnerD, ProgressDialog progressDialog, EditText etxtCount) {
        String AlphaNumericString = "0123456789abcdefghijklmnopqrstuvxyz";
        StringBuilder sb1 = new StringBuilder(10);
        for (int i = 0; i < 6; i++) {
            int index = (int) (AlphaNumericString.length() * Math.random());
            sb1.append(AlphaNumericString.charAt(index));
        }

        auth1.createUserWithEmailAndPassword(sb1.toString().toLowerCase() + "@firemail.com", "Fire@" + sb1.toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                String UserID = auth1.getUid();
                HashMap<String, Object> mapValues = new HashMap<>();
                mapValues.put("ID", UserID);
                mapValues.put("First_Name", "NULL");
                mapValues.put("Last_Name", "NULL");
                mapValues.put("Email", sb1.toString().toLowerCase() + "@firemail.com");
                mapValues.put("Password", "Fire@" + sb1.toString());
                mapValues.put("Profile_Image", "gs://fire-tutorial-bc8e4.appspot.com/Profile_Images/null/Avatar.jpg");
                mapValues.put("Phone", "+20");
                mapValues.put("Role", "Employee");
                mapValues.put("Department", spinnerD.getSelectedItem());
                mapValues.put("DateTime_of_last_update", "Data not updated until this moment");
                mapValues.put("DateTime_of_account_created", new SimpleDateFormat("dd/MM/yyyy - hh:mm:ss a", Locale.ENGLISH).format(Calendar.getInstance().getTime()).toString());
                if (task.isSuccessful()) {
                    try {
                        reference.child("User").child("Employee").child(UserID).setValue(mapValues);
                        auth1.signOut();
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "successfully to registration", Toast.LENGTH_SHORT).show();
                        etxtCount.setText(null);
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Failed to create account reason to\n " + task.getException(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void ClearFields() {
        etxtFname.getEditText().setText(null);
        etxtLname.getEditText().setText(null);
        etxtEmail.getEditText().setText(null);
        etxtPass.getEditText().setText(null);
    }

    private void getDepartments(DatabaseReference reference) {

    }


    //Validation method
    private Boolean validateFName() {
        String val = etxtFname.getEditText().getText().toString().trim();
        Pattern pattern = Pattern.compile("^[A-Za-zء-ي]{2,}$");
        Matcher matcher = pattern.matcher(val);

        if (val.isEmpty()) {
            etxtFname.setError("First name cannot be empty!");
            return false;
        } else if (!matcher.matches()) {
            etxtFname.setError("enter valid first name");
            return false;
        } else if (val.length() < 3) {
            etxtFname.setError("First name is too short!");
            return false;
        } else {
            etxtFname.setError(null);
            return true;
        }
    }

    private Boolean validateLName() {
        String val = etxtLname.getEditText().getText().toString().trim();
        Pattern pattern = Pattern.compile("^[A-Za-zء-ي]{2,}$");
        Matcher matcher = pattern.matcher(val);
        if (val.isEmpty()) {
            etxtLname.setError("Last name cannot be empty!");
            return false;
        } else if (!matcher.matches()) {
            etxtFname.setError("enter valid last name");
            return false;
        } else if (val.length() < 3) {
            etxtLname.setError("Last name is too short!");
            return false;
        } else {
            etxtLname.setError(null);
            return true;
        }
    }

    private Boolean validateEmail() {
        String val = etxtEmail.getEditText().getText().toString().trim(); //+ editFixedEmail.getEditText().getText().toString().trim();
        Pattern pattern = Pattern.compile("^[\\w]{5,}$");
        Matcher matcher = pattern.matcher(val);

        if (val.isEmpty()) {
            etxtEmail.setError("Email cannot be empty!");
            return false;
        } else if (val.length() < 6) {
            etxtEmail.setError("Username is too short!");
            return false;
        } else if (!matcher.matches()) {
            etxtEmail.setError("Please enter valid username!");
            return false;
        } else {
            etxtEmail.setError(null);
            return true;
        }
    }

    private Boolean validatePassword() {
        String val = etxtPass.getEditText().getText().toString().trim();
        Pattern pattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#_$!%*+=?&])[A-Za-z\\d@#_$=+!%*?&]{8,}$");
        Matcher matcher = pattern.matcher(val);

        if (val.isEmpty()) {
            etxtPass.setError("Password cannot be empty!");
            return false;
        } else if (!matcher.matches()) {
            etxtPass.setError("Password is too weak!");
            return false;
        } else {
            //Alert dialog change pass
            etxtPass.setError(null);
            return true;
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
        AlertDialog.Builder builder1 = new AlertDialog.Builder(activity_create_accounts.this, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
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

    public void onBackPressing(View view) {
        finish();
    }
}