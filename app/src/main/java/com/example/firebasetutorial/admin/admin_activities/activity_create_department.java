package com.example.firebasetutorial.admin.admin_activities;

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
import android.provider.Settings;
import android.text.Html;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.firebasetutorial.R;
import com.example.firebasetutorial.activity_login;
import com.example.firebasetutorial.classes.ShPreferences;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.example.firebasetutorial.admin.admin_activities.activity_admin_home.prepareDepartment;

public class activity_create_department extends AppCompatActivity {

    TextInputLayout depNameTxt, depDecTxt;
    DatabaseReference reference;
    FirebaseAuth auth;
    SharedPreferences preferences;
    Button btnCreateDepart;
    Spinner spinnerMan;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_create_department);
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);


        if (auth.getCurrentUser() == null) {
            //login page
            startActivity(new Intent(activity_create_department.this, activity_login.class));
            finish();
        }

        depNameTxt = findViewById(R.id.txtDepName);
        depDecTxt = findViewById(R.id.txtDesc);
        btnCreateDepart = findViewById(R.id.btnCreateDepart);
        spinnerMan = findViewById(R.id.spinnerMan);

        btnCreateDepart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (validateDepartName() & validateDepartDesc() & validateDepartMan()) {
                    // GET STRINGS
                    String depName = depNameTxt.getEditText().getText().toString().trim();
                    String depDesc = depDecTxt.getEditText().getText().toString().trim();
                    String currentDate = new SimpleDateFormat("dd/MM/yyyy - hh:mm:ss a", Locale.ENGLISH).format(Calendar.getInstance().getTime()).toString();
                    HashMap<Object, String> map = new HashMap<>();
                    // INSERT STRINGS
                    map.put("Name", depName.toLowerCase().trim());
                    map.put("Description", depDesc);
                    map.put("DateTime_of_Department_Created", currentDate);
                    map.put("Manager", spinnerMan.getSelectedItem().toString());
                    try {
                        //INSERT DATETIME
                        reference.child("Departments").child(depName.toLowerCase()).setValue(map);
                        Snackbar.make(findViewById(android.R.id.content), "Successfully create new department " + id, Snackbar.LENGTH_SHORT).show();
                        UpgradeRoleForUser(depName);
                        prepareDepartment(getApplicationContext(),reference);
                        depDecTxt.getEditText().setText(null);
                        depNameTxt.getEditText().setText(null);
                    }catch (Exception e){
                        Toast.makeText(activity_create_department.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void getManagerList(DatabaseReference reference) {
        ArrayList<String> list = new ArrayList<>();
        list.add(0,"Select Manager from Employee");

        Query query = reference.child("User").child("Employee");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String key = ds.child("Email").getValue(String.class);
                    list.add(key);
                }
                ArrayAdapter<String> sp_Depart_Aapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_hint_text, list);
                sp_Depart_Aapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerMan.setAdapter(sp_Depart_Aapter);
                spinnerMan.setSelection(0,true);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Snackbar.make(findViewById(android.R.id.content), "error"+error.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void UpgradeRoleForUser(String depName) {
        Query query = reference.child("User").child("Employee").orderByChild("Email").equalTo(spinnerMan.getSelectedItem().toString());
        //first ref
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    id= dataSnapshot.getKey();
                }
                //second ref
                reference.child("User").child("Employee").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                        //third ref
                        reference.child("User").child("Manager").child(id).setValue(dataSnapshot.getValue(), new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError firebaseError, @NotNull DatabaseReference firebase) {
                                if (firebaseError != null) {
                                    Toast.makeText(getApplicationContext(), firebaseError.getMessage(), Toast.LENGTH_LONG).show();
                                } else {
                                    reference.child("User").child("Manager").child(id).child("Role").setValue("Manager");
                                    reference.child("User").child("Manager").child(id).child("Department").setValue(depName);
                                    reference.child("Requests").child(depName).child(spinnerMan.getSelectedItem().toString().replace(".",",")).setValue(null);
                                }
                                reference.child("User").child("Employee").child(id).setValue(null);
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
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(),  error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private Boolean validateDepartName() {
        String val = depNameTxt.getEditText().getText().toString().trim();
        List<String> Depart_item = ShPreferences.readDataInListPreferences(getApplicationContext(), "DepartmentsList", "Departments");

        if (val.length() < 4) {
            depNameTxt.setError("Department name is too short!");
            return false;
        } else if (Depart_item.contains(val)) {
            depNameTxt.setError("Department name is already exist!");
            return false;
        } else {
        depNameTxt.setError(null);
            return true;
        }
    }

    private Boolean validateDepartDesc() {
        String val = depDecTxt.getEditText().getText().toString().trim();

        if (val.length() < 4) {
            depDecTxt.setError("Department description is too short!");
            return false;
        }  else {
            depDecTxt.setError(null);
            return true;
        }
    }

    private Boolean validateDepartMan() {
        if (spinnerMan.getSelectedItemId() == 0) {
            Toast.makeText(this, "select manager!", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!isConnected()) {
            showInfoAlert();
        }
        getManagerList(reference);
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
}