package com.example.firebasetutorial.employee.employee_activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.chaos.view.PinView;
import com.example.firebasetutorial.R;
import com.example.firebasetutorial.activity_login;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class activity_emp_verify_phone extends AppCompatActivity {

    TextView tvNumber, tvBtnResendCode;
    PinView codeVerify;
    String verificationBySystem;
    String number;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    Button btnVerify;
    boolean timeOutCode = false;
    ProgressDialog progressDialog;

    FirebaseAuth auth;
    FirebaseDatabase db;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emp_verify_phone);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseDatabase.getInstance();

        if (auth.getCurrentUser() == null) {
            //home page
            startActivity(new Intent(getApplicationContext(), activity_login.class));
            finish();
        }

        progressDialog = new ProgressDialog(activity_emp_verify_phone.this);
        progressDialog.create();
        progressDialog.show();
        progressDialog.setMessage("Verifying you're not a robot...");
        progressDialog.setCanceledOnTouchOutside(false);

        tvNumber = findViewById(R.id.tvNumber);
        tvBtnResendCode = findViewById(R.id.resendPhoneCode);
        codeVerify = findViewById(R.id.phonePinCode);
        btnVerify = findViewById(R.id.btnVerify);

        number = getIntent().getStringExtra("phone");
        tvNumber.setText(number);
        generateVerificationCode(number);

        tvBtnResendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!timeOutCode) {
                    Snackbar.make(findViewById(android.R.id.content), "Please Wait...", Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(findViewById(android.R.id.content), "Succeeded send code", Snackbar.LENGTH_SHORT).show();
                    timeOutCode = false;
                    resendVerificationCode(number);
                    progressDialog.show();
                }
            }
        });

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Pattern.compile("^[\\w]{6}$").matcher(codeVerify.getText().toString().trim()).matches()) {
                    Snackbar.make(findViewById(android.R.id.content), "enter correct code", Snackbar.LENGTH_LONG).show();
                    codeVerify.requestFocus();
                } else {
                    verifyCode(codeVerify.getText().toString());
                }
            }
        });
    }

    private void generateVerificationCode(String number) {
        Log.d("TAG", "start sent request to otp");
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(number)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(mCallbacks)        // OnVerificationStateChangedCallbacks
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void resendVerificationCode(String number) {
        Log.d("TAG", "start re-sent request otp");
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(number)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                .setForceResendingToken(mResendToken)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
        timeOutCode = false; //temppp
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) { //get code on sent
            super.onCodeSent(s, forceResendingToken);
            Log.d("TAG", "Code Sent");
//            Toast.makeText(activity_emp_verify_phone.this, "Code Sent", Toast.LENGTH_LONG).show();
            verificationBySystem = s;
            mResendToken = forceResendingToken;
            progressDialog.dismiss();
        }

        @Override
        public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
            super.onCodeAutoRetrievalTimeOut(s);
            timeOutCode = true;
            Snackbar.make(findViewById(android.R.id.content), "Time Out of verification code please click Re-send code", Snackbar.LENGTH_LONG).show();
//            Toast.makeText(activity_emp_verify_phone.this, s.toString()+"\nnew=old\n"+mResendToken.toString(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {//get code from sms
            String codeByUser = phoneAuthCredential.getSmsCode();
            if (codeByUser != null) {
                //method
                verifyCode(codeByUser);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) { //get error
            progressDialog.dismiss();
            timeOutCode = true;
            Toast.makeText(activity_emp_verify_phone.this, "An error occurred while getting the code\n" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    private void verifyCode(String codeByUser) { //compare between two codes
        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationBySystem, codeByUser);
            openActivity(credential);
        } catch (Exception e) {
            Toast.makeText(activity_emp_verify_phone.this, "Wrong verification code entered\n" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void openActivity(PhoneAuthCredential credential) {
        auth.getCurrentUser().updatePhoneNumber(credential).addOnCompleteListener(activity_emp_verify_phone.this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    UpdatePhone(number);
                    finish();
                } else {
                    Toast.makeText(activity_emp_verify_phone.this, "An error occurred while updating the phone number due to:\n" + task.getException(), Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(activity_emp_verify_phone.this, "An error occurred while updating the phone number due to:\n" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void UpdatePhone(String phoneNumber) {
        SharedPreferences preferences = getSharedPreferences("eprofileData", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        final String uid = auth.getCurrentUser().getUid();
        auth.updateCurrentUser(auth.getCurrentUser()).addOnCompleteListener(activity_emp_verify_phone.this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    try {
                        db.getReference().child("User").child("Employee").child(uid).child("Phone").setValue(phoneNumber);
                        db.getReference().child("User").child("Employee").child(uid).child("DateTime_of_last_update").setValue(new SimpleDateFormat("dd/MM/yyyy - hh:mm:ss a", Locale.ENGLISH).format(Calendar.getInstance().getTime()).toString());
                        editor.putString("Phone", phoneNumber);
                        editor.apply();
                        Snackbar.make(findViewById(android.R.id.content), "Successfully update phone number", Snackbar.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(activity_emp_verify_phone.this, "Failed update phone number\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    @Override
    public void onStart() {
        if (!isConnected()) {
            showInfoAlert();
        } else {
            super.onStart();
        }
    }

    private void showInfoAlert() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(activity_emp_verify_phone.this, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
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