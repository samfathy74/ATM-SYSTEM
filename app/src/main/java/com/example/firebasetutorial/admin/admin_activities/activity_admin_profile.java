package com.example.firebasetutorial.admin.admin_activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebasetutorial.R;
import com.example.firebasetutorial.activity_login;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.firebasetutorial.activity_welcome.getSettings;


public class activity_admin_profile extends AppCompatActivity {

    ImageButton editEmail, editPassword, editAttendTime, editLeaveTime;
    Button logoutBtn;
    TextInputLayout emailText, passText;
    NumberPicker editTextAHour, editTextAMin, editTextAPeriod, editTextLHour, editTextLMin, editTextLPeriod;

    SharedPreferences preferences;
    FirebaseAuth auth;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);

        if (auth.getCurrentUser() == null) {
            //login page
            startActivity(new Intent(getApplicationContext(), activity_login.class));
            finish();
        }

        Configuration configuration = getResources().getConfiguration();
        Locale locale = Locale.ENGLISH;
        Locale.setDefault(locale);
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);
        createConfigurationContext(configuration);

        logoutBtn = findViewById(R.id.btnLogout);
        emailText = findViewById(R.id.adminEmail);
        passText = findViewById(R.id.adminPassword);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);

        editAttendTime = findViewById(R.id.editAttendTime);
        editLeaveTime = findViewById(R.id.editLeaveTime);

        editTextAHour = findViewById(R.id.editTextAHour);
        editTextAMin = findViewById(R.id.editTextAMin);
        editTextAPeriod = findViewById(R.id.editTextAPeriod);

        editTextLHour = findViewById(R.id.editTextLHour);
        editTextLMin = findViewById(R.id.editTextLMin);
        editTextLPeriod = findViewById(R.id.editTextLPeriod);

        emailText.setEnabled(false);
        passText.setEnabled(false);
        //Attendance
        editTextAHour.setEnabled(false);
        editTextAHour.setMinValue(1);
        editTextAHour.setMaxValue(12);

        editTextAMin.setEnabled(false);
        editTextAMin.setMinValue(0);
        editTextAMin.setMaxValue(59);

        editTextAPeriod.setEnabled(false);
        editTextAPeriod.setMinValue(1);
        editTextAPeriod.setMinValue(1);
        editTextAPeriod.setMaxValue(1);
        //Leaving
        editTextLHour.setEnabled(false);
        editTextLHour.setValue(1);
        editTextLHour.setMaxValue(12);

        editTextLMin.setEnabled(false);
        editTextLMin.setMinValue(0);
        editTextLMin.setMaxValue(59);

        editTextLPeriod.setEnabled(false);
        editTextLPeriod.setMinValue(1);
        editTextLPeriod.setMinValue(1);
        editTextLPeriod.setMaxValue(1);

        preferences = getSharedPreferences("AdminData", MODE_PRIVATE);
        String adminEmail = preferences.getString("Email", "");
        emailText.getEditText().setText(adminEmail);
        changeTimer();
        updatePassword();
        updateEmail();

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(activity_admin_profile.this, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
                builder1.setMessage(Html.fromHtml("<font color='#FF0000'>Do you want to logout from account ?</font>"));
                builder1.setPositiveButton(Html.fromHtml("Logout"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(activity_admin_profile.this, activity_login.class));
                        auth.signOut();
                        finish();
                    }
                });
                builder1.setNegativeButton(Html.fromHtml("Cancel"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder1.create();
                dialog.show();
            }
        });
    }

    private void updatePassword() {
        editPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferences = getSharedPreferences("AdminData", MODE_PRIVATE);
                String pass = preferences.getString("Password", "");
                String password = passText.getEditText().getText().toString().trim();
                if (!passText.isEnabled()) {
                    passText.setEnabled(true);
                    editPassword.setImageResource(R.drawable.ic_ok);
                } else {
                    if (passText.getEditText().getText().toString().isEmpty() || !passText.getEditText().getText().toString().equals(pass)) {
                        passText.setError("Password entered not match with old password!");
                    } else {
                        passText.setEnabled(false);
                        editPassword.setImageResource(R.drawable.ic_edit);
                        passText.getEditText().setText(null);
                        passText.setError(null);
                        alertDialogUpdatePass();
                    }
                }
            }
        });
    }

    //pass alert
    private void alertDialogUpdatePass() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(activity_admin_profile.this, R.style.CustomAlertDialog).setCancelable(false);
        View view = getLayoutInflater().inflate(R.layout.alert_update_pass, null);
        builder1.setTitle("");
        TextView title = view.findViewById(R.id.alertTitlePass);
        TextView msg = view.findViewById(R.id.alertMsgPass);
        TextInputLayout etxtNewPass = view.findViewById(R.id.etxtnewPass);
        TextInputLayout etxtConfirmPass = view.findViewById(R.id.etxtConfirmPass);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        builder1.setView(view);
        AlertDialog dialog1 = builder1.create();

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String confirmNewPass = etxtConfirmPass.getEditText().getText().toString().trim();
                if (!validatePassword(etxtNewPass)) {
                    //
                } else if (!confirmNewPass.equals(etxtNewPass.getEditText().getText().toString().trim())) {
                    etxtConfirmPass.setError("Confirm password not match with new password!");
                } else {
                    auth.getCurrentUser().updatePassword(confirmNewPass).addOnCompleteListener(activity_admin_profile.this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                updateUsersData("Password", confirmNewPass);
                                dialog1.dismiss();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Password update failed due to:\n " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog1.dismiss();
            }
        });
        dialog1.show();
    }

    private void updateUsersData(String key, String value) {
        preferences = getSharedPreferences("AdminData", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        auth.updateCurrentUser(auth.getCurrentUser()).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    try {
                        reference.child("Admin").child(auth.getCurrentUser().getUid()).child(key).setValue(value);
                        editor.putString(key, value);
                        editor.apply();
                        Snackbar.make(findViewById(android.R.id.content), "successfully update " + key, Snackbar.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed update " + key + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void updateEmail() {
        editEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailText.getEditText().getText().toString().trim();
                if (!emailText.isEnabled()) {
                    emailText.setEnabled(true);
                    editEmail.setImageResource(R.drawable.ic_ok);
                } else {
                    if (validateEmail()) {
                        auth.getCurrentUser().updateEmail(email).addOnCompleteListener(activity_admin_profile.this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    updateUsersData("Email", email);
                                    emailText.setEnabled(false);
                                    editEmail.setImageResource(R.drawable.ic_edit);
                                    emailText.setError(null);
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Password update failed due to:\n " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        });
    }

    private Boolean validatePassword(TextInputLayout etxtNewPass) {
        String val = etxtNewPass.getEditText().getText().toString().trim();
        Pattern pattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#_$!%*+=?&])[A-Za-z\\d@#_$=+!%*?&]{8,}$");
        Matcher matcher = pattern.matcher(val);
        preferences = getSharedPreferences("AdminData", MODE_PRIVATE);
        String password = preferences.getString("Password", "");

        if (val.isEmpty()) {
            etxtNewPass.setError("Password cannot be empty!");
            return false;
        } else if (!matcher.matches()) {
            etxtNewPass.setError("Password is too weak!");
            return false;
        } else if (val.equals(password)) {
            etxtNewPass.setError("Password not updated!");
            return false;
        } else {
            etxtNewPass.setError(null);
            return true;
        }
    }

    private Boolean validateEmail() {
        String val = emailText.getEditText().getText().toString().trim();
        Pattern pattern = Pattern.compile("^([\\w\\-]{5,})+@([\\w-]{3,}\\.)+[\\w]{2,}$");
        Matcher matcher = pattern.matcher(val);
        preferences = getSharedPreferences("AdminData", MODE_PRIVATE);
        String email = preferences.getString("Email", "");

        if (val.isEmpty()) {
            emailText.setError("Email cannot be empty!");
            return false;
        } else if (!matcher.matches()) {
            emailText.setError("Email is not valid or too short!");
            return false;
        } else if (val.equals(email)) {
            emailText.setError("Email not update!");
            if (emailText.isEnabled()) {
                emailText.setEnabled(false);
                editEmail.setImageResource(R.drawable.ic_edit);
            }
            return false;
        } else {
            emailText.setError(null);
            return true;
        }
    }

    private void changeTimer() {
        editAttendTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = editTextAHour.getValue();
                int min = editTextAMin.getValue();

                if (!editTextAHour.isEnabled()) {
                    editTextAHour.setEnabled(true);
                    editTextAMin.setEnabled(true);
                    editAttendTime.setImageResource(R.drawable.ic_ok);
                } else {
                    if (hour < 0 || min < 0) {
                        Toast.makeText(activity_admin_profile.this, "Attend Hour or Min can not be lower than zero!", Toast.LENGTH_SHORT).show();
                    } else {

                        Map<String, Object> map = new HashMap<>();
                        map.put("StartHour", String.valueOf(hour));
                        map.put("EndHour", String.valueOf(hour));
                        map.put("StartMin", "0");
                        map.put("EndMin", String.valueOf(min));
                        map.put("PM_AM", "AM");
                        reference.child("Settings").child("AttendanceSettings").setValue(map);
                        Toast.makeText(activity_admin_profile.this, "Successfully adjust attendance time", Toast.LENGTH_SHORT).show();

                        getSettings(getApplicationContext());
                    }
                    editTextAHour.setEnabled(false);
                    editTextAMin.setEnabled(false);
                    editAttendTime.setImageResource(R.drawable.ic_edit);
                }
            }
        });

        editLeaveTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hourL = editTextLHour.getValue();
                int minL = editTextLMin.getValue();

                if (!editTextLHour.isEnabled()) {
                    editTextLHour.setEnabled(true);
                    editTextLMin.setEnabled(true);
                    editLeaveTime.setImageResource(R.drawable.ic_ok);
                } else {
                    if (hourL < 0 || minL < 0) {
                        Toast.makeText(activity_admin_profile.this, "Leave Hour or Min or period can not be lower than zero!", Toast.LENGTH_SHORT).show();
                    } else {
                            Map<String, Object> map = new HashMap<>();
                            map.put("StartHour", String.valueOf(hourL));
                            map.put("EndHour", String.valueOf(hourL));
                            map.put("StartMin", "0");
                            map.put("EndMin", String.valueOf(minL));
                            map.put("PM_AM", "PM");
                            reference.child("Settings").child("LeavingSettings").setValue(map);

                            Toast.makeText(activity_admin_profile.this, "Successfully adjust Leaving time", Toast.LENGTH_SHORT).show();

                        getSettings(getApplicationContext());
                    }
                    editTextLHour.setEnabled(false);
                    editTextLMin.setEnabled(false);
                    editTextLPeriod.setEnabled(false);
                    editLeaveTime.setImageResource(R.drawable.ic_edit);
                }
            }
        });
    }

    private void displayTimerValue() {
        preferences = getSharedPreferences("Settings", MODE_PRIVATE);
//        attend time
        editTextAHour.setValue(Integer.parseInt(preferences.getString("StartHourA", "")));
        editTextAMin.setValue(Integer.parseInt(preferences.getString("StartMinA", "")));

//        leave time
        editTextLHour.setValue( Integer.parseInt(preferences.getString("StartHourL", "")));
        editTextLMin.setValue(Integer.parseInt(preferences.getString("StartMinL", "")));

    }

    public void onBackPressing(View view) {
        finish();
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

    @Override
    protected void onStart() {
        super.onStart();
        if (!isConnected()) {
            showInfoAlert();
        }
        displayTimerValue();
    }
}