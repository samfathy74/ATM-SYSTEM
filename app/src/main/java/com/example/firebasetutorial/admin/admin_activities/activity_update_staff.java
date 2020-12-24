package com.example.firebasetutorial.admin.admin_activities;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.firebasetutorial.R;
import com.example.firebasetutorial.activity_login;
import com.example.firebasetutorial.classes.ShPreferences;
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
import com.hbb20.CountryCodePicker;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class activity_update_staff extends AppCompatActivity {
    TextInputLayout edit_Fname_staff, edit_Lname_staff, edit_Email_staff, edit_Password_staff, edit_Phone_staff;
    ImageButton imageEnabledF, imageEnabledL, imageEnabledPh, imageEnabledPass, imageEnabledDepart;
    FirebaseAuth auth;
    DatabaseReference reference;
    CountryCodePicker keyCountryplus;
    Button btn_Delete;
    Spinner spanner_Depart_Staff;
    SharedPreferences preferences;
    String oldManagerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_update_staff);

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);

        if (auth.getCurrentUser() == null) {
            //login page
            startActivity(new Intent(this, activity_login.class));
            finish();
        }

        DefinitionVariables();
        enabledEditableData();

        btn_Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(activity_update_staff.this, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
                builder1.setMessage(Html.fromHtml("<font color='#FF0000'>Do you want to delete this user ?</font>"));
                builder1.setPositiveButton(Html.fromHtml("Delete"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        preferences = getSharedPreferences("StaffData", MODE_PRIVATE);
                        String id = preferences.getString("ID", "");
                        String role = preferences.getString("Role", "");
                        String email = preferences.getString("Email", "");
                        String password = preferences.getString("Password", "");
                        if(role.equals("Manager")){
                            Toast.makeText(getApplicationContext(), "You cannot delete this manager until you choose another department manager!", Toast.LENGTH_LONG).show();
                        }else {
                            reference.child("User").child(role).child(id).setValue(null);
                            Snackbar.make(findViewById(android.R.id.content), "Successfully delete account", Snackbar.LENGTH_SHORT).show();
                            finish();
                        }
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

    private void getDepartments(DatabaseReference reference, String department) {
        List<String> Depart_item = ShPreferences.readDataInListPreferences(getApplicationContext(), "DepartmentsList", "Departments");
        Depart_item.add(0, "Select Department");

        ArrayAdapter<String> sp_Depart_Aapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_hint_text, Depart_item);
        sp_Depart_Aapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spanner_Depart_Staff.setAdapter(sp_Depart_Aapter);
        spanner_Depart_Staff.setSelection(sp_Depart_Aapter.getPosition(department));
    }

    private void DefinitionVariables() {
        edit_Fname_staff = findViewById(R.id.edit_Fname_Staff);
        edit_Lname_staff = findViewById(R.id.edit_Lname_Staff);
        edit_Email_staff = findViewById(R.id.edit_Email_Staff);
        edit_Phone_staff = findViewById(R.id.edit_Phone_Staff);
        spanner_Depart_Staff = findViewById(R.id.spanner_Depart_Staff);
        edit_Password_staff = findViewById(R.id.edit_Password_Staff);
        keyCountryplus = findViewById(R.id.keyCountryplus);

        imageEnabledF = findViewById(R.id.imageEnabledF);
        imageEnabledL = findViewById(R.id.imageEnabledL);
        imageEnabledPh = findViewById(R.id.imageEnabledPh);
        imageEnabledPass = findViewById(R.id.imageEnabledPass);
        imageEnabledDepart = findViewById(R.id.imageEnabledDepart);

        btn_Delete = findViewById(R.id.btnDelete_Staff);
    }

    //method retrieve data
    private void getUserData() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("StaffData", MODE_PRIVATE);
        String id = prefs.getString("ID", "");
        String fname = prefs.getString("First_Name", "");
        String lname = prefs.getString("Last_Name", "");
        String email = prefs.getString("Email", "");
        String pass = prefs.getString("Password", "");
        String department = prefs.getString("Department", "");
        String phone = prefs.getString("Phone", "");

        //set data in fields
        edit_Fname_staff.getEditText().setText(fname);
        edit_Lname_staff.getEditText().setText(lname);
        edit_Email_staff.getEditText().setText(email);
        edit_Phone_staff.getEditText().setText(phone);

        getDepartments(reference, department);
        edit_Password_staff.getEditText().setText(pass);
    }

    //method update data
    private void updateUsersData(String key, String value) {
//        final String uid = auth.getCurrentUser().getUid();
        preferences = getSharedPreferences("StaffData", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String id = preferences.getString("ID", "");
        String role = preferences.getString("Role", "");

        auth.updateCurrentUser(auth.getCurrentUser()).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    try {
                        reference.child("User").child(role).child(id).child(key).setValue(value);
                        reference.child("User").child(role).child(id).child("DateTime_of_last_update").setValue(new SimpleDateFormat("(EEEE) dd/MM/yyyy - hh:mm:ss a", Locale.ENGLISH).format(Calendar.getInstance().getTime()).toString());
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

    private void enabledEditableData() {
        edit_Fname_staff.setEnabled(false);
        edit_Lname_staff.setEnabled(false);
        edit_Password_staff.setEnabled(false);
        edit_Phone_staff.setEnabled(false);
        edit_Email_staff.setEnabled(false);
        spanner_Depart_Staff.setEnabled(false);
        keyCountryplus.setEnabled(false);

        //fname
        imageEnabledF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstname = edit_Fname_staff.getEditText().getText().toString().trim();
                if (!edit_Fname_staff.isEnabled()) {
                    edit_Fname_staff.setEnabled(true);
                    imageEnabledF.setImageResource(R.drawable.ic_ok);
                } else {
                    edit_Fname_staff.requestFocus();
                    //code edit
                    if (validateFName()) {
                        updateUsersData("First_Name", firstname);
                        edit_Fname_staff.setEnabled(false);
                        imageEnabledF.setImageResource(R.drawable.ic_edit);
                    }
                }
            }
        });
        //lname
        imageEnabledL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lastname = edit_Lname_staff.getEditText().getText().toString().trim();
                if (!edit_Lname_staff.isEnabled()) {
                    edit_Lname_staff.setEnabled(true);
                    imageEnabledL.setImageResource(R.drawable.ic_ok);
                } else {
                    edit_Lname_staff.requestFocus();
                    if (validateLName()) {
                        edit_Lname_staff.setEnabled(false);
                        imageEnabledL.setImageResource(R.drawable.ic_edit);
                        updateUsersData("Last_Name", lastname);
                    }
                }
            }
        });

        imageEnabledPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = edit_Password_staff.getEditText().getText().toString().trim();
                if (!edit_Password_staff.isEnabled()) {
                    edit_Password_staff.setEnabled(true);
                    imageEnabledPass.setImageResource(R.drawable.ic_ok);
                } else {
                    if (validatePassword()) {
                        auth.getCurrentUser().updatePassword(password).addOnCompleteListener(activity_update_staff.this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    updateUsersData("Password", password);
                                    edit_Password_staff.setEnabled(false);
                                    imageEnabledPass.setImageResource(R.drawable.ic_edit);
                                    edit_Password_staff.setError(null);
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
        //phone
        imageEnabledPh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = edit_Phone_staff.getEditText().getText().toString();
                String keyCountry = keyCountryplus.getSelectedCountryCodeWithPlus().toString();

                if (!edit_Phone_staff.isEnabled()) {
                    edit_Phone_staff.setEnabled(true);
                    keyCountryplus.setEnabled(true);
                    imageEnabledPh.setImageResource(R.drawable.ic_ok);
                } else {
                    edit_Phone_staff.requestFocus();
                    if (validatePhone()) {
                        updateUsersData("Phone", keyCountry + phone);
                        edit_Phone_staff.setEnabled(false);
                        keyCountryplus.setEnabled(false);
                        imageEnabledPh.setImageResource(R.drawable.ic_edit);
                    }
                }
            }
        });
        //department
        imageEnabledDepart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferences = getSharedPreferences("StaffData", MODE_PRIVATE);
                String id = preferences.getString("ID", "");
                String role = preferences.getString("Role", "");

                String department = spanner_Depart_Staff.getSelectedItem().toString();
                if (!spanner_Depart_Staff.isEnabled()) {
                    spanner_Depart_Staff.setEnabled(true);
                    imageEnabledDepart.setImageResource(R.drawable.ic_ok);
                } else {
                    spanner_Depart_Staff.requestFocus();
                    if (validateDepartment()) {
                        spanner_Depart_Staff.setEnabled(false);
                        imageEnabledDepart.setImageResource(R.drawable.ic_edit);
                        updateUsersData("Department", department);
                    }
                }
            }
        });
    }

    //Validation methods
    private Boolean validateFName() {
        String val = edit_Fname_staff.getEditText().getText().toString().trim();
        Pattern pattern = Pattern.compile("^[A-Za-zء-ي]{3,}$");
        Matcher matcher = pattern.matcher(val);
        preferences = getSharedPreferences("StaffData", MODE_PRIVATE);
        String fname = preferences.getString("First_Name", "");

        if (!matcher.matches()) {
            edit_Fname_staff.setError("enter valid first name");
            return false;
        } else if (val.length() < 4) {
            edit_Fname_staff.setError("First name is too short!");
            return false;
        } else if (val.equals(fname)) {
            edit_Fname_staff.setError("First name not update!");
            if (edit_Fname_staff.isEnabled()) {
                edit_Fname_staff.setEnabled(false);
                imageEnabledF.setImageResource(R.drawable.ic_edit);
            }
            return false;
        } else {
            edit_Fname_staff.setError(null);
            return true;
        }
    }

    private Boolean validateLName() {
        String val = edit_Lname_staff.getEditText().getText().toString().trim();
        Pattern pattern = Pattern.compile("^[A-Za-zء-ي]{3,}$");
        Matcher matcher = pattern.matcher(val);
        preferences = getSharedPreferences("StaffData", MODE_PRIVATE);
        String lname = preferences.getString("Last_Name", "");

        if (!matcher.matches()) {
            edit_Lname_staff.setError("enter valid last name");
            return false;
        } else if (val.length() < 4) {
            edit_Lname_staff.setError("Last name is too short!");
            return false;
        } else if (val.equals(lname)) {
            edit_Lname_staff.setError("Last name not update!");
            if (edit_Lname_staff.isEnabled()) {
                edit_Lname_staff.setEnabled(false);
                imageEnabledL.setImageResource(R.drawable.ic_edit);
            }
            return false;
        } else {
            edit_Lname_staff.setError(null);
            return true;
        }
    }

    private Boolean validateDepartment() {
        String val = spanner_Depart_Staff.getSelectedItem().toString();
        preferences = getSharedPreferences("StaffData", MODE_PRIVATE);
        String department = preferences.getString("Department", "");
        String role = preferences.getString("Role", "");

        if (spanner_Depart_Staff.getSelectedItemId() == 0) {
            Toast.makeText(getApplicationContext(), "select department", Toast.LENGTH_SHORT).show();
            return false;
        } else if (val.equals(department)) {
            Toast.makeText(getApplicationContext(), "Department not update!", Toast.LENGTH_SHORT).show();
            if (spanner_Depart_Staff.isEnabled()) {
                spanner_Depart_Staff.setEnabled(false);
                imageEnabledDepart.setImageResource(R.drawable.ic_edit);
            }
            return false;
        } else if (role.equals("Manager")) {
            Toast.makeText(getApplicationContext(), "You cannot modify the department for this manager until you choose another department manager!", Toast.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }
    }

    private Boolean validatePassword() {
        String val = edit_Password_staff.getEditText().getText().toString().trim();
        Pattern pattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#_$!%*+=?&])[A-Za-z\\d@#_$=+!%*?&]{8,}$");
        Matcher matcher = pattern.matcher(val);
        preferences = getSharedPreferences("StaffData", MODE_PRIVATE);
        String password = preferences.getString("Password", "");

        if (val.isEmpty()) {
            edit_Password_staff.setError("Password cannot be empty!");
            return false;
        } else if (!matcher.matches()) {
            edit_Password_staff.setError("Password is too weak!");
            return false;
        } else if (val.equals(password)) {
            edit_Password_staff.setError("Password not update!");
            if (edit_Password_staff.isEnabled()) {
                edit_Password_staff.setEnabled(false);
                imageEnabledPass.setImageResource(R.drawable.ic_edit);
            }
            return false;
        } else {
            edit_Password_staff.setError(null);
            return true;
        }
    }

    private Boolean validatePhone() {
        String val = edit_Phone_staff.getEditText().getText().toString().trim();
        Pattern pattern = Pattern.compile("^[\\+0-9]{10}$");
        Matcher matcher = pattern.matcher(val);
        preferences = getSharedPreferences("StaffDate", MODE_PRIVATE);
        String phone = preferences.getString("Phone", "");

        if (val.isEmpty() || keyCountryplus.getSelectedCountryCode().isEmpty()) {
            edit_Phone_staff.setError("Phone cannot be empty!");
            return false;
        } else if (!matcher.matches()) {
            edit_Phone_staff.setError("enter valid number without key as +20");
            return false;
        } else if (val.equals(phone)) {
            edit_Phone_staff.setError("Phone not update!");
            if (edit_Phone_staff.isEnabled()) {
                edit_Phone_staff.setEnabled(false);
                imageEnabledPh.setImageResource(R.drawable.ic_edit);
            }
            return false;
        } else {
            edit_Phone_staff.setError(null);
            return true;
        }
    }

    private void UpgradeRoleToEmployee(String managerEmail, String depName, SharedPreferences.Editor editor) {
        //first ref
        reference.child("User").child("Manager").child(oldManagerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                //third ref
                reference.child("User").child("Employee").child(oldManagerId).setValue(dataSnapshot.getValue(), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError firebaseError, @NotNull DatabaseReference firebase) {
                        if (firebaseError != null) {
                            Toast.makeText(getApplicationContext(), firebaseError.getMessage(), Toast.LENGTH_LONG).show();
                        } else {
                            reference.child("User").child("Employee").child(oldManagerId).child("Role").setValue("Employee");
                            reference.child("User").child("Employee").child(oldManagerId).child("Department").setValue(depName);
                        }
                        reference.child("User").child("Manager").child(oldManagerId).setValue(null);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError e) {
                Toast.makeText(getApplicationContext(), "Failed to" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        getUserData();
        if (!isConnected()) {
            showInfoAlert();
        }
    }

    public void onBackPressing(View view) {
        finish();
    }

    private void showInfoAlert() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(activity_update_staff.this, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
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