package com.example.firebasetutorial.employee.employee_fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.firebasetutorial.R;
import com.example.firebasetutorial.activity_login;
import com.example.firebasetutorial.manager.manager_activities.activity_mng_verify_phone;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class fragment_emprofile extends Fragment {

    TextInputLayout editFnamepro, editLnamepro, editOrigEmailpro, editPasswordp, editPhonepro, editRolepro, editDepartpro;
    ImageButton imageEnabledF, imageEnabledL, imageEnabledPh, imageEnabledPass;
    ImageView uploadImg;
    private static final int IMAGE_REQUEST_CODE = 2;
    StorageReference mStorageRef;
    FirebaseAuth auth;
    FirebaseDatabase db;
    CountryCodePicker codePicker;
    Button btn_Logout;
    SharedPreferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_emprofile, container, false);

        DefinitionVariables(view);

        if (auth.getCurrentUser() == null) {
            //login page
            startActivity(new Intent(getActivity(), activity_login.class));
            getActivity().finish();
        }

        btn_Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity(), R.style.Theme_MaterialComponents_Light_Dialog_Alert);
                builder1.setMessage(Html.fromHtml("<font color='#FF0000'>Do you want to logout from account ?</font>"));
                builder1.setPositiveButton(Html.fromHtml("Logout"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getActivity(), activity_login.class));
                        auth.signOut();
                        getActivity().finish();
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

        uploadImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE_REQUEST_CODE);
            }
        });

//        getUsersDatafromDataBase();
        LoadProfilePicture();
        enabledEditableData();

        return view;
    }

    private void DefinitionVariables(View view) {
        auth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseDatabase.getInstance();

        editFnamepro = view.findViewById(R.id.editFnamep);
        editLnamepro = view.findViewById(R.id.editLnamep);
        editOrigEmailpro = view.findViewById(R.id.editOriginalEmailp);
        editPhonepro = view.findViewById(R.id.editPhonep);
        editRolepro = view.findViewById(R.id.editRolep);
        editDepartpro = view.findViewById(R.id.editDepartp);
        editPasswordp = view.findViewById(R.id.editPasswordp);
        codePicker = view.findViewById(R.id.keyCountry);

        uploadImg = view.findViewById(R.id.profileImage);
        imageEnabledF = view.findViewById(R.id.imageEnabledF);
        imageEnabledL = view.findViewById(R.id.imageEnabledL);
        imageEnabledPh = view.findViewById(R.id.imageEnabledPh);
        imageEnabledPass = view.findViewById(R.id.imageEnabledPass);
        btn_Logout = view.findViewById(R.id.btnLogout);
    }

    private void LoadProfilePicture() {
        StorageReference Sreference = mStorageRef.child("Profile_Images/" + auth.getCurrentUser().getEmail() + "/Avatar.jpg");
        Sreference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                try {
                    if (uri == null) {
                        uploadImg.setImageResource(R.drawable.add_photo);
                    } else {
                        Picasso.get().load(uri).into(uploadImg);
                    }
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Failed" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                Uri imageUri = data.getData();
                UploadToFirebase(imageUri);
                uploadImg.setImageURI(imageUri);
            } catch (Exception e) {
                Toast.makeText(getActivity(), "There was occurred problem\n" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void UploadToFirebase(Uri imageUri) {
        ProgressDialog progressBar = new ProgressDialog(getActivity());
        progressBar.show();
        StorageReference SReference = mStorageRef.child("Profile_Images").child(auth.getCurrentUser().getEmail()).child("Avatar.jpg");
        SReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressBar.dismiss();
                updateUsersData("Profile_Image", taskSnapshot.getStorage().getRoot() + taskSnapshot.getStorage().getPath());
                Snackbar.make(getActivity().findViewById(android.R.id.content), "Succeed upload image", Snackbar.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Failed upload image\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.dismiss();
            }
        });
    }

    private void getUserData(){
        SharedPreferences prefs = getActivity().getSharedPreferences("eprofileData", MODE_PRIVATE);
        String fname = prefs.getString("First_Name", "");
        String lname = prefs.getString("Last_Name", "");
        String email = prefs.getString("Email","");
        String pass = prefs.getString("Password","");
        String role = prefs.getString("Role","");
        String department = prefs.getString("Department","");
        String phone = prefs.getString("Phone","");

        //set data in fields
        editFnamepro.getEditText().setText(fname);
        editLnamepro.getEditText().setText(lname);
        editOrigEmailpro.getEditText().setText(email);
        editPhonepro.getEditText().setText(phone);
        editRolepro.getEditText().setText(role);
        editDepartpro.getEditText().setText(department);

    }

    //method update data
    private void updateUsersData(String key, String value) {
        final String uid = auth.getCurrentUser().getUid();
        preferences = getActivity().getSharedPreferences("eprofileData", MODE_PRIVATE);
        auth.updateCurrentUser(auth.getCurrentUser()).addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    try {
                        db.getReference().child("User").child("Employee").child(uid).child(key).setValue(value);
                        db.getReference().child("User").child("Employee").child(uid).child("DateTime_of_last_update").setValue(new SimpleDateFormat("(EEEE) dd/MM/yyyy - hh:mm:ss a", Locale.ENGLISH).format(Calendar.getInstance().getTime()).toString());
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(key, value);
                        editor.apply();
                        Snackbar.make(getActivity().findViewById(android.R.id.content), "successfully update " + key, Snackbar.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "Failed update " + key + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void enabledEditableData() {
        editFnamepro.setEnabled(false);
        editLnamepro.setEnabled(false);
        editPasswordp.setEnabled(false);
        editPhonepro.setEnabled(false);
        editOrigEmailpro.setEnabled(false);
        editDepartpro.setEnabled(false);
        editRolepro.setEnabled(false);
        codePicker.setEnabled(false);

        //fname
        imageEnabledF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstname = editFnamepro.getEditText().getText().toString().trim();
                if (!editFnamepro.isEnabled()) {
                    editFnamepro.setEnabled(true);
                    imageEnabledF.setImageResource(R.drawable.ic_ok);
                } else {
                    editFnamepro.requestFocus();
                    //code edit
                    if (validateFName()) {
                        try {
                            updateUsersData("First_Name", firstname);
                            editFnamepro.setEnabled(false);
                            imageEnabledF.setImageResource(R.drawable.ic_edit);
                        } catch (Exception e) {
                            Toast.makeText(getActivity(), "failed update fname" + e, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
        //lname
        imageEnabledL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lastname = editLnamepro.getEditText().getText().toString().trim();
                if (!editLnamepro.isEnabled()) {
                    editLnamepro.setEnabled(true);
                    imageEnabledL.setImageResource(R.drawable.ic_ok);
                } else {
                    editLnamepro.requestFocus();
                    if (validateLName()) {
                        try {
                            editLnamepro.setEnabled(false);
                            imageEnabledL.setImageResource(R.drawable.ic_edit);
                            updateUsersData("Last_Name", lastname);
                        } catch (Exception e) {
                            Toast.makeText(getActivity(), "failed update last name " + e, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
        //password
        imageEnabledPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferences = getActivity().getSharedPreferences("eprofileData", MODE_PRIVATE);
                String pass = preferences.getString("Password","");

                if (!editPasswordp.isEnabled()) {
                    customAlertDialog("Warning, Are you trying to change password?", "If you want to change the password, " +
                            "press (Continue) and enter the old password to complete change password. Otherwise, click (Cancel)", editPasswordp, imageEnabledPass);
                } else {
                    if (editPasswordp.getEditText().getText().toString().isEmpty() || !editPasswordp.getEditText().getText().toString().equals(pass)) {
                        editPasswordp.setError("Password entered not match with old password!");
                    } else {
                        try {
                            editPasswordp.setEnabled(false);
                            imageEnabledPass.setImageResource(R.drawable.ic_edit);
                            editPasswordp.getEditText().setText(null);
                            editPasswordp.setError(null);
                            alertDialogUpdatePass();
                        } catch (Exception e) {
                            Toast.makeText(getActivity(), "failed update password " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });
        //phone
        imageEnabledPh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = editPhonepro.getEditText().getText().toString();
                String keyCountry = codePicker.getSelectedCountryCodeWithPlus().toString();

                if (!editPhonepro.isEnabled()) {
                    customAlertDialog("Warning, Are you trying to update phone?", "If you want to update phone number, " +
                            "press click (Continue) and choose country key with enter number phone without country key again. Otherwise, click (Cancel)", editPhonepro, imageEnabledPh);
                } else {
                    editPhonepro.requestFocus();
                    if (validatePhone()) {
                        //code here......
                        getActivity().getIntent().putExtra("phone", keyCountry + phone);
                        alertDialogUpdatePhone(editPhonepro);
                    }
                }
            }
        });
    }

    public void customAlertDialog(String title, String message, TextInputLayout editText, ImageButton imageEnabled) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog).setCancelable(false);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editText.setEnabled(true);
                imageEnabled.setImageResource(R.drawable.ic_ok);
                editText.requestFocus();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                editText.setEnabled(false);
                imageEnabled.setImageResource(R.drawable.ic_edit);
                editText.setError(null);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //pass alert
    private void alertDialogUpdatePass() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog).setCancelable(false);
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
                    //etxtNewPass.setError("else, Enter strong new password");
                } else if (!confirmNewPass.equals(etxtNewPass.getEditText().getText().toString().trim())) {
                    etxtConfirmPass.setError("Confirm password not match with new password!");
                } else {
                    auth.getCurrentUser().updatePassword(confirmNewPass).addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
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
                            Toast.makeText(getActivity(), "Password update failed due to:\n" + e.getMessage(), Toast.LENGTH_LONG).show();
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

    //phone alert
    private void alertDialogUpdatePhone(TextInputLayout editPhonepro) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog).setCancelable(false);
        View view = getLayoutInflater().inflate(R.layout.alert_phone, null);
        builder1.setTitle("");
        TextView msg = view.findViewById(R.id.alertMsgPhone);
        TextView title = view.findViewById(R.id.alertTitlePhone);
        title.setText("Verify Phone Number");
        msg.setText("To confirm adding this phone number\n" + editPhonepro.getEditText().getText().toString() + "\nto your account, please click (Verify now) or to confirm at a later time click (Verify later).");
        Button btnConfirm = view.findViewById(R.id.btnConfirm);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        builder1.setView(view);
        AlertDialog dialog1 = builder1.create();

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), activity_mng_verify_phone.class);
                intent.putExtra("phone", getActivity().getIntent().getStringExtra("phone"));
                startActivity(intent);
                editPhonepro.setEnabled(false);
                codePicker.setEnabled(false);
                imageEnabledPh.setImageResource(R.drawable.ic_edit);
                dialog1.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog1.dismiss();
                editPhonepro.setEnabled(false);
                codePicker.setEnabled(false);
                imageEnabledPh.setImageResource(R.drawable.ic_edit);
            }
        });
        dialog1.show();
    }


    //Validation methods
    private Boolean validateFName() {
        String val = editFnamepro.getEditText().getText().toString().trim();
        Pattern pattern = Pattern.compile("^[A-Za-zء-ي]{3,}$");
        Matcher matcher = pattern.matcher(val);
        preferences = getActivity().getSharedPreferences("eprofileData", MODE_PRIVATE);
        String fname = preferences.getString("First_Name","");

        if (!matcher.matches()) {
            editFnamepro.setError("enter valid first name");
            return false;
        } else if (val.length() < 4) {
            editFnamepro.setError("First name is too short!");
            return false;
        } else if (val.equals(fname)) {
            editFnamepro.setError("First name not update!");
            if (editFnamepro.isEnabled()) {
                editFnamepro.setEnabled(false);
                imageEnabledF.setImageResource(R.drawable.ic_edit);
            }
            return false;
        } else {
            editFnamepro.setError(null);
            return true;
        }
    }

    private Boolean validateLName() {
        String val = editLnamepro.getEditText().getText().toString().trim();
        Pattern pattern = Pattern.compile("^[A-Za-zء-ي]{3,}$");
        Matcher matcher = pattern.matcher(val);
        preferences = getActivity().getSharedPreferences("eprofileData", MODE_PRIVATE);
        String lname = preferences.getString("Last_Name","");

        if (!matcher.matches()) {
            editLnamepro.setError("enter valid last name");
            return false;
        } else if (val.length() < 4) {
            editLnamepro.setError("Last name is too short!");
            return false;
        } else if (val.equals(lname)) {
            editLnamepro.setError("Last name not update!");
            if (editLnamepro.isEnabled()) {
                editLnamepro.setEnabled(false);
                imageEnabledL.setImageResource(R.drawable.ic_edit);
            }
            return false;
        } else {
            editLnamepro.setError(null);
            return true;
        }
    }

    private Boolean validatePassword(TextInputLayout newPassword) {
        String val = newPassword.getEditText().getText().toString().trim();
        Pattern pattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#_$!%*+=?&])[A-Za-z\\d@#_$=+!%*?&]{8,}$");
        Matcher matcher = pattern.matcher(val);
        preferences = getActivity().getSharedPreferences("eprofileData", MODE_PRIVATE);
        String password = preferences.getString("Password","");

        if (val.isEmpty()) {
            newPassword.setError("Password cannot be empty!");
            return false;
        } else if (!matcher.matches()) {
            newPassword.setError("Password is too weak!");
            return false;
        } else if (val.equals(password)) {
            newPassword.setError("Password not updates!");
            return false;
        } else {
            //Alert dialog change pass
            newPassword.setError(null);
            return true;
        }
    }

    private Boolean validatePhone() {
        String val = editPhonepro.getEditText().getText().toString().trim();
        Pattern pattern = Pattern.compile("^[\\+0-9]{10}$");
        Matcher matcher = pattern.matcher(val);

        if (val.isEmpty() || codePicker.getSelectedCountryCode().isEmpty()) {
            editPhonepro.setError("Phone cannot be empty!");
            return false;
        } else if (!matcher.matches()) {
            editPhonepro.setError("enter valid number without key as +20");
            return false;
        } else {
            //OTP
            editPhonepro.setError(null);
            return true;
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

    @Override
    public void onStart() {
        super.onStart();
        getUserData();
        if (!isConnected()) {
            showInfoAlert();
        }
    }

}