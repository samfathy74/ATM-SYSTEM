package com.example.firebasetutorial.admin.admin_activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebasetutorial.R;
import com.example.firebasetutorial.activity_login;
import com.example.firebasetutorial.classes.ShPreferences;
import com.example.firebasetutorial.manager.adapters.RecycleVAdapterAttendance;
import com.example.firebasetutorial.classes.Attendance;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class activity_admin_attendance extends AppCompatActivity {
    RecyclerView recycleHistory;
    DatePicker datePickerHistory;
    RecycleVAdapterAttendance adapter;
    FirebaseAuth auth;
    DatabaseReference reference;
    ImageView imgCloseDateHistory, imgCloseSpinner;
    TextView txtViewHistory, txtViewManager, txtViewCountAttendEmp, txtViewCountAllEmp;
    FirebaseRecyclerOptions<Attendance> options;
    Spinner spinnerDepart;
    SearchView searcher;
    LinearLayout imgOpenSpinner, imgOpenDateHistory, LinerLayoutAdminEmpSearch, imageRecycleNull;
    CardView cardEmpInData, cardSpinner, cardBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_attendance);

        definitionVariables();

        String date = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Calendar.getInstance().getTime()).toString();
        getIntent().putExtra("changeDate", date);
        getDepartmentList();
        getAttendanceHistory(date, spinnerDepart.getSelectedItem().toString());

        changeDateByDatePicker();
        collapseAndExpandView();
        changeDepartmentBySpinner();


    }

    private void definitionVariables() {
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);

        if (auth.getCurrentUser() == null) {
            //home page
            startActivity(new Intent(getApplicationContext(), activity_login.class));
            finish();
        }

        searcher = findViewById(R.id.searcher);
        spinnerDepart = findViewById(R.id.spinnerDepart);
        txtViewHistory = findViewById(R.id.txtViewHistory);
        imageRecycleNull = findViewById(R.id.imageRecycleNull);
        LinerLayoutAdminEmpSearch = findViewById(R.id.LinerLayoutAdminEmpSearch);
        datePickerHistory = findViewById(R.id.datePickerHistory);
        recycleHistory = findViewById(R.id.recycleHistory);

        txtViewManager = findViewById(R.id.txtViewManager);
        txtViewCountAttendEmp = findViewById(R.id.txtViewCountAttendEmp);
        txtViewCountAllEmp = findViewById(R.id.txtViewCountAllEmp);

        imgCloseDateHistory = findViewById(R.id.imgCloseDateHistory);
        imgCloseSpinner = findViewById(R.id.imgCloseSpinner);
        imgOpenDateHistory = findViewById(R.id.imgOpenDateHistory);
        imgOpenSpinner = findViewById(R.id.imgOpenSpinner);
        cardEmpInData = findViewById(R.id.cardEmpInData);
        cardSpinner = findViewById(R.id.cardSpinner);
        cardBanner = findViewById(R.id.cardBanner);

        recycleHistory.setLayoutManager(new LinearLayoutManager(activity_admin_attendance.this));
    }

    private void getSpecificData(String date, String depart) {
        reference.child("Departments").child(depart).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String manager = snapshot.child("Manager").getValue(String.class);
                    txtViewManager.setText("M: " + manager + " || ");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        reference.child("Attendees").child(date).child(depart).orderByChild("Department").equalTo(depart).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChildren()) {
                    String attenderCount = String.valueOf(snapshot.getChildrenCount());
                    txtViewCountAttendEmp.setText("Attendance (" + attenderCount);
                } else {
                    txtViewCountAttendEmp.setText("Attendance (0");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        reference.child("User").child("Employee").orderByChild("Department").equalTo(depart).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String empCount = String.valueOf(snapshot.getChildrenCount());
                    txtViewCountAllEmp.setText("\\" + empCount + ")");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void collapseAndExpandView() {
        imgCloseDateHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Transition transition = new Slide(Gravity.END);
                transition.setDuration(700);
                transition.addTarget(R.id.cardEmpInData);
                TransitionManager.beginDelayedTransition(cardEmpInData, transition);
                cardEmpInData.setVisibility(View.GONE);
                imgOpenDateHistory.setVisibility(View.VISIBLE);
            }
        });
        imgCloseSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Transition transition = new Slide(Gravity.END);
                transition.setDuration(1000);
                transition.addTarget(R.id.cardSpinner);
                TransitionManager.beginDelayedTransition(cardSpinner, transition);
                cardSpinner.setVisibility(View.GONE);
                imgOpenSpinner.setVisibility(View.VISIBLE);
            }
        });

        imgOpenDateHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Transition transition = new Slide(Gravity.TOP);
                transition.setDuration(1000);
                transition.addTarget(R.id.cardSpinner);
                TransitionManager.beginDelayedTransition(cardEmpInData, transition);
                cardEmpInData.setVisibility(View.VISIBLE);
                imgOpenDateHistory.setVisibility(View.GONE);
            }
        });
        imgOpenSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Transition transition = new Slide(Gravity.TOP);
                transition.setDuration(1000);
                transition.addTarget(R.id.cardSpinner);
                TransitionManager.beginDelayedTransition(cardSpinner, transition);
                cardSpinner.setVisibility(View.VISIBLE);
                imgOpenSpinner.setVisibility(View.GONE);
            }
        });


        txtViewHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (datePickerHistory.getVisibility() == View.GONE) {
                    datePickerHistory.setVisibility(View.VISIBLE);
                    TransitionManager.beginDelayedTransition(datePickerHistory);
                    txtViewHistory.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_flat_arrow_up, 0);
                } else {
                    datePickerHistory.setVisibility(View.GONE);
                    TransitionManager.beginDelayedTransition(datePickerHistory);
                    txtViewHistory.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_flat_arrow_down, 0);
                }
            }
        });

    }

    private void changeDepartmentBySpinner() {
        spinnerDepart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String depart = parent.getSelectedItem().toString();
                String date2 = getIntent().getStringExtra("changeDate");
                getAttendanceHistory(date2, depart);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(activity_admin_attendance.this, "select department to show employee attendance ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changeDateByDatePicker() {
        Calendar calendar = Calendar.getInstance();
        Calendar.getInstance().setTimeInMillis(System.currentTimeMillis());
        datePickerHistory.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int dayOfMonth) {
                String date2 = datePickerHistory.getDayOfMonth() + "-" + (1 + datePickerHistory.getMonth()) + "-" + datePickerHistory.getYear();
                getAttendanceHistory(date2, spinnerDepart.getSelectedItem().toString());
                getIntent().putExtra("changeDate", date2);
                txtViewHistory.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_flat_arrow_down, 0);
            }
        });
    }

    private void getDepartmentList() {
        List<String> Depart_item = ShPreferences.readDataInListPreferences(getApplicationContext(), "DepartmentsList", "Departments");
        ArrayAdapter<String> sp_Depart_Aapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_hint_text, Depart_item);
        sp_Depart_Aapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepart.setAdapter(sp_Depart_Aapter);
    }

    private void getAttendanceHistory(String date, String depart) {
        if (date.isEmpty() && depart.isEmpty()) {
            Toast.makeText(activity_admin_attendance.this, "department or date is null", Toast.LENGTH_LONG).show();
        } else {
            getSpecificData(date, depart);
            searcher.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    searchIntoRecycle(s, date, depart);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    searchIntoRecycle(s, date, depart);
                    return false;
                }
            });

            Query query = reference.child("Attendees").child(date).child(depart);
            options = new FirebaseRecyclerOptions.Builder<Attendance>()
                    .setQuery(query, Attendance.class).build();

            adapter = new RecycleVAdapterAttendance(options);
            recycleHistory.setAdapter(adapter);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                    datePickerHistory.setVisibility(View.GONE);
                    LinerLayoutAdminEmpSearch.setVisibility(View.GONE);
                    if (dataSnapshot.exists()) {
                        adapter.startListening();
                        adapter.notifyDataSetChanged();
                        imageRecycleNull.setVisibility(View.GONE);
                        recycleHistory.setVisibility(View.VISIBLE);
                    } else {
                        imageRecycleNull.setVisibility(View.VISIBLE);
                        recycleHistory.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(activity_admin_attendance.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void searchIntoRecycle(String s, String date, String depart) {
        Query queries = reference.child("Attendees").child(date).child(depart).orderByChild("Email").startAt(s.toLowerCase()).endAt(s.toLowerCase() + "\uf8ff");
        options = new FirebaseRecyclerOptions.Builder<Attendance>()
                .setQuery(queries, Attendance.class).build();

        adapter = new RecycleVAdapterAttendance(options);
        recycleHistory.setAdapter(adapter);
        queries.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    adapter.startListening();
                    LinerLayoutAdminEmpSearch.setVisibility(View.GONE);
                } else {
                    if (imageRecycleNull.getVisibility() == View.VISIBLE) {
                        LinerLayoutAdminEmpSearch.setVisibility(View.GONE);
                    } else {
                        LinerLayoutAdminEmpSearch.setVisibility(View.VISIBLE);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(activity_admin_attendance.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        if (!isConnected()) {
            showInfoAlert();
        }
    }

    private void showInfoAlert() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(activity_admin_attendance.this, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
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