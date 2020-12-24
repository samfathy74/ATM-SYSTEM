package com.example.firebasetutorial.admin.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebasetutorial.R;
import com.example.firebasetutorial.admin.admin_activities.activity_update_department;
import com.example.firebasetutorial.admin.admin_activities.activity_employee;
import com.example.firebasetutorial.admin.admin_activities.activity_manager;
import com.example.firebasetutorial.classes.Departments;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RecycleVAdapterDepartment extends FirebaseRecyclerAdapter<Departments, RecycleVAdapterDepartment.DataHolderView> {
    SharedPreferences preferences;

    public RecycleVAdapterDepartment(@NonNull FirebaseRecyclerOptions<Departments> options) {
        super(options);
    }

    @NonNull
    @Override
    public DataHolderView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DataHolderView(LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_rv_department, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindViewHolder(@NonNull DataHolderView holder, int position, @NonNull Departments departments) {

        holder.depName.setText(departments.getName());
        holder.depDesc.setText(Html.fromHtml("<font color='#F43E31' >Department Description: </font><br/>" + departments.getDescription()));
        holder.createdTimeDate.setText(Html.fromHtml("<font color='#F43E31'>Date and Time of Department Created: </font><br/>" + departments.getDateTime_of_Department_Created()));
        holder.managerDepart.setText(Html.fromHtml("<font color='#F43E31'>Manager: </font><br/>" + departments.getManager()));

        getEmployeeCount(holder, departments);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferences = v.getContext().getSharedPreferences("DepartData", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("Name", departments.getName());
                editor.putString("Description", departments.getDescription());
                editor.putString("Manager", departments.getManager());
                editor.putString("DateTime_of_Department_Created", departments.getDateTime_of_Department_Created());
                editor.apply();

                v.getContext().startActivity(new Intent(v.getContext(), activity_update_department.class));
                notifyDataSetChanged();
            }
        });

        //manager
        holder.btn_manager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferences = v.getContext().getSharedPreferences("DepartData", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("Name", departments.getName());
                editor.apply();
                v.getContext().startActivity(new Intent(v.getContext(), activity_manager.class));
                notifyDataSetChanged();
            }
        });

        //employee
        holder.btn_employee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferences = v.getContext().getSharedPreferences("DepartData", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("Name", departments.getName());
                editor.apply();
                v.getContext().startActivity(new Intent(v.getContext(), activity_employee.class));
                notifyDataSetChanged();
            }
        });

    }

    private void getEmployeeCount(DataHolderView holder, Departments departments) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("User").child("Employee").orderByChild("Department").equalTo(departments.getName().toString())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int employeeCounter = (int) snapshot.getChildrenCount();
                        holder.empCount.setText(Html.fromHtml("<font color='#F43E31'>Number of Employees in this Department: </font><br/>" + employeeCounter));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }


    class DataHolderView extends RecyclerView.ViewHolder {
        TextView depName, empCount, createdTimeDate, depDesc, managerDepart;
        CardView cardView;
        Button btn_manager, btn_employee;

        public DataHolderView(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardEmpInData);
            depName = itemView.findViewById(R.id.depName);
            depDesc = itemView.findViewById(R.id.depDesc);
            createdTimeDate = itemView.findViewById(R.id.createdTimeDate);
            managerDepart = itemView.findViewById(R.id.managerDepart);
            empCount = itemView.findViewById(R.id.empCount);

            btn_employee = itemView.findViewById(R.id.btn_employee);
            btn_manager = itemView.findViewById(R.id.btn_manager);

        }
    }

}
