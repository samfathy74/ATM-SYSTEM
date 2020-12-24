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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class activity_update_department extends AppCompatActivity {
    FirebaseAuth auth;
    DatabaseReference reference;
    SharedPreferences preferences;
    TextInputLayout txtUpdateDepName, txtUpdateDepDesc;
    ImageButton editDepartManager, editDepartDesc;
    Spinner spinnerMan;
    String newManagerId, oldManagerId,id;
    Button btn_Delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_update_department);

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);

        if (auth.getCurrentUser() == null) {
            //login page
            startActivity(new Intent(activity_update_department.this, activity_login.class));
            finish();
        }

        txtUpdateDepName = findViewById(R.id.txtUpdateDepName);
        txtUpdateDepDesc = findViewById(R.id.txtUpdateDepDesc);

        editDepartDesc = findViewById(R.id.editDepartDesc);
        editDepartManager = findViewById(R.id.editDepartManager);
        btn_Delete = findViewById(R.id.btnDelete_Depart);
        spinnerMan = findViewById(R.id.spinnerMan);

        txtUpdateDepName.setEnabled(false);
        txtUpdateDepDesc.setEnabled(false);
        spinnerMan.setEnabled(false);

        preferences = getSharedPreferences("DepartData", MODE_PRIVATE);

        String departName = preferences.getString("Name", "");
        String departDesc = preferences.getString("Description", "");
        String departManager = preferences.getString("Manager", "");

        txtUpdateDepName.getEditText().setText(departName);
        txtUpdateDepDesc.getEditText().setText(departDesc);

        getEmployeeEmail(reference, departManager, departName);


        editDepartDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String departmentName = txtUpdateDepName.getEditText().getText().toString().trim();
                String departmentDesc = txtUpdateDepDesc.getEditText().getText().toString().trim();
                if (!txtUpdateDepDesc.isEnabled()) {
                    txtUpdateDepDesc.setEnabled(true);
                    editDepartDesc.setImageResource(R.drawable.ic_ok);
                } else {
                    txtUpdateDepName.requestFocus();
                    //code edit
                    if (validateDepartDesc()) {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("Description", departmentDesc).apply();
                        reference.child("Departments").child(departmentName).child("Description").setValue(departmentDesc);
                        txtUpdateDepDesc.setError(null);
                        Snackbar.make(findViewById(android.R.id.content), "Successfully update description", Snackbar.LENGTH_SHORT).show();
                        txtUpdateDepDesc.setEnabled(false);
                        editDepartDesc.setImageResource(R.drawable.ic_edit);
                    }
                }
            }
        });

        editDepartManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newDepartManager = spinnerMan.getSelectedItem().toString().trim();
                if (!spinnerMan.isEnabled()) {
                    spinnerMan.setEnabled(true);
                    editDepartManager.setImageResource(R.drawable.ic_ok);
                } else {
                    SharedPreferences.Editor editor = preferences.edit();

                    if (validateDepartMan()) {
                        try {
                            //remove manager
                            UpgradeRoleToEmployee(departManager
                                    , departName
                                    , editor);

                            //update emploee
                            UpgradeRoleToManager(newDepartManager
                                    , departName
                                    , editor);

                            editor.putString("Manager", newDepartManager).apply();
                        } catch (Exception e) {
                            Toast.makeText(activity_update_department.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        Snackbar.make(findViewById(android.R.id.content), "Successfully update Manager", Snackbar.LENGTH_SHORT).show();
                        spinnerMan.setEnabled(false);
                        editDepartManager.setImageResource(R.drawable.ic_edit);
                    }
                }
            }
        });

        btn_Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(activity_update_department.this, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
                builder1.setMessage(Html.fromHtml("<font color='#FF0000'>Do you want to delete this department ?</font>"));
                builder1.setPositiveButton(Html.fromHtml("Delete"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        preferences = getSharedPreferences("DepartData", MODE_PRIVATE);
                        String Dname = preferences.getString("Name", "");
                        List<String> Depart_item = ShPreferences.readDataInListPreferences(getApplicationContext(), "DepartmentsList", "Departments");
                        SharedPreferences.Editor editor = preferences.edit();
                        //remove manager
                        removeDepartmentWithData(departManager,Depart_item.get(new Random().nextInt(Depart_item.size())));
                        changeDepartmentOfEmployee(departName,Depart_item.get(new Random().nextInt(Depart_item.size())));
                        reference.child("Departments").child(Dname).setValue(null);
                        Snackbar.make(findViewById(android.R.id.content), "Successfully delete department", Snackbar.LENGTH_SHORT).show();
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

    private void getEmployeeEmail(DatabaseReference reference, String departManager, String departName) {
        ArrayList<String> list = new ArrayList<>();
        list.add(0, "Select New Manager");

        Query query = reference.child("User").child("Employee").orderByChild("Department").equalTo(departName);
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
                spinnerMan.setSelection(0, true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Snackbar.make(findViewById(android.R.id.content), "error" + error.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

   private void changeDepartmentOfEmployee(String departName, String newDepart){
        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference();
       reference.child("User").child("Employee").orderByChild("Department").equalTo(departName).addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               Map<String,Object> map = new HashMap<>();
               for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                   id = dataSnapshot.getKey();
                   map.put("Department",newDepart);
                   reference2.child("User").child("Employee").child(id).updateChildren(map);
               }


           }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void UpgradeRoleToManager(String managerEmail, String depName, SharedPreferences.Editor editor) {
        //first ref
        reference.child("User").child("Employee").orderByChild("Email").equalTo(managerEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    newManagerId = dataSnapshot.getKey();
                }
                //second ref
                reference.child("User").child("Employee").child(newManagerId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                        //third ref
                        reference.child("User").child("Manager").child(newManagerId).setValue(dataSnapshot.getValue(), new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError firebaseError, @NotNull DatabaseReference firebase) {
                                if (firebaseError != null) {
                                    Toast.makeText(getApplicationContext(), firebaseError.getMessage(), Toast.LENGTH_LONG).show();
                                } else {
                                    reference.child("User").child("Manager").child(newManagerId).child("Role").setValue("Manager");
                                    reference.child("User").child("Manager").child(newManagerId).child("Department").setValue(depName);
                                    reference.child("Departments").child(depName).child("Manager").setValue(managerEmail);
                                    editor.putString("Manager", spinnerMan.getSelectedItem().toString());
                                    editor.apply();
                                }
                                reference.child("User").child("Employee").child(newManagerId).setValue(null);
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
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void UpgradeRoleToEmployee(String managerEmail, String depName, SharedPreferences.Editor editor) {
        //first ref
        reference.child("User").child("Manager").orderByChild("Email").equalTo(managerEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    oldManagerId = dataSnapshot.getKey();
                }
                //second ref
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
                                    reference.child("Departments").child(depName).child("Manager").setValue(managerEmail);
                                    reference.child("User").child("Employee").child(oldManagerId).child("Role").setValue("Employee");
                                    reference.child("User").child("Employee").child(oldManagerId).child("Department").setValue(depName);
                                    editor.putString("Manager", spinnerMan.getSelectedItem().toString());
                                    editor.apply();
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
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void removeDepartmentWithData(String managerEmail, String depName) {
        //first ref
        reference.child("User").child("Manager").orderByChild("Email").equalTo(managerEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    oldManagerId = dataSnapshot.getKey();
                }
                //second ref
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
                                    Map<String,Object> map = new HashMap<>();
                                    map.put("Role","Employee");
                                    map.put("Department",depName);
                                    reference.child("User").child("Employee").child(oldManagerId).updateChildren(map);
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
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private Boolean validateDepartDesc() {
        preferences = getSharedPreferences("DepartData", MODE_PRIVATE);
        String description = preferences.getString("Description", "");
        String val = txtUpdateDepDesc.getEditText().getText().toString().trim();
        if (val.length() < 4) {
            txtUpdateDepDesc.setError("Department description is too short!");
            return false;
        } else if (val.equals(description)) {
            if (txtUpdateDepDesc.isEnabled()) {
                txtUpdateDepDesc.setEnabled(false);
                editDepartDesc.setImageResource(R.drawable.ic_edit);
            }
            txtUpdateDepDesc.setError("Department description not update!");
            return false;
        } else {
            txtUpdateDepDesc.setError(null);
            return true;
        }
    }

    private Boolean validateDepartMan() {
        preferences = getSharedPreferences("DepartData", MODE_PRIVATE);
        String manager = preferences.getString("Manager", "");
        String val = spinnerMan.getSelectedItem().toString().trim();
        if (spinnerMan.getSelectedItemId() == 0) {
            Toast.makeText(this, "select manager!", Toast.LENGTH_SHORT).show();
            return false;
        } else if (val.equals(manager)) {
            if (spinnerMan.isEnabled()) {
                spinnerMan.setEnabled(false);
                editDepartManager.setImageResource(R.drawable.ic_edit);
            }
            Snackbar.make(findViewById(android.R.id.content), "Department Manager not update!", Snackbar.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
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

    public void onBackPressing(View view) {
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isConnected()) {
            showInfoAlert();
        }
    }

}