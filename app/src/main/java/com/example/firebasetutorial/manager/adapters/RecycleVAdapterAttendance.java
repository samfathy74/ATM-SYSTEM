package com.example.firebasetutorial.manager.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebasetutorial.R;
import com.example.firebasetutorial.classes.Attendance;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RecycleVAdapterAttendance extends FirebaseRecyclerAdapter<Attendance, RecycleVAdapterAttendance.DataHolderView> {
    private int mExpandedPosition = -1;
    private RecyclerView recyclerView = null;
    private int drawIcon = R.drawable.ic_flat_arrow_down;

    public RecycleVAdapterAttendance(@NonNull FirebaseRecyclerOptions<Attendance> options) {
        super(options);
    }

    @NonNull
    @Override
    public DataHolderView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DataHolderView(LayoutInflater.from(parent.getContext()).inflate(R.layout.custm_rv_follow_attendance, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindViewHolder(@NonNull DataHolderView holder, int position, @NonNull Attendance employees) {
        final boolean isExpanded = position == mExpandedPosition;

        holder.txtParentEmail.setText(employees.getEmail());
        holder.txtFullname.setText(Html.fromHtml("<font color='#F43E31' >Full Name: </font>" + employees.getName()));
        holder.txtAddress.setText(Html.fromHtml("<font color='#F43E31'>Location of Attendance: </font><br/>" + employees.getAttendance_Location()));
        holder.txtDateTimeAttendance.setText(Html.fromHtml("<font color='#F43E31'>Date and Time of Attendance:</font><br/>" + employees.getDateTime_of_Attendance()));
        holder.txtStatus.setText(Html.fromHtml("<font color='#F43E31'>Status: </font>" + employees.getStatus()));
        holder.txtLeaveLocation.setText(Html.fromHtml("<font color='#F43E31'>Location of Leaving:</font><br/>" + employees.getLeave_Location()));
        holder.txtdateTimeLeaving.setText(Html.fromHtml("<font color='#F43E31'>Date and Time of Leaving:</font><br/>" + employees.getDateTime_Out_Work()));

        if (!holder.txtStatus.getText().toString().contains("Not Available")) {
            holder.txtParentEmail.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_online, 0, drawIcon, 0);
            holder.txtStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_online, 0, 0, 0);
        } else {
            holder.txtParentEmail.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_offline, 0, drawIcon, 0);
            holder.txtStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_offline, 0, 0, 0);
        }

        holder.cardView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.itemView.setActivated(isExpanded);
        holder.txtParentEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExpanded) {
                    drawIcon = R.drawable.ic_flat_arrow_down;
                    holder.txtParentEmail.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, drawIcon, 0);
                } else {
                    drawIcon = R.drawable.ic_flat_arrow_up;
                    holder.txtParentEmail.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, drawIcon, 0);
                }
                mExpandedPosition = isExpanded ? -1 : position;
                TransitionManager.beginDelayedTransition(recyclerView);
                notifyDataSetChanged();
            }
        });

        holder.txtAddress.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String attendAddress = holder.txtAddress.getText().toString().trim();
                if (attendAddress.length() > 30) {
                    if (attendAddress.contains("&")) {
                        copyToClipboard(attendAddress.substring(24 + 21), view);
                    } else {
                        copyToClipboard(attendAddress.substring(24), view);
                    }
                } else {
                    copyToClipboard("NULL", view);
                }
                return true;
            }
        });

        holder.txtLeaveLocation.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String leaveAddress = holder.txtLeaveLocation.getText().toString().trim();
                if (leaveAddress.length() > 30) {
                    if (leaveAddress.contains("&")) {
                        copyToClipboard(leaveAddress.substring(21 + 21), view);
                    } else {
                        copyToClipboard(leaveAddress.substring(21), view);
                    }
                } else {
                    copyToClipboard("NULL", view);
                }
                return true;
            }
        });

        getMonthlySummaryAttendance(holder,employees);
    }


    class DataHolderView extends RecyclerView.ViewHolder {
        TextView txtParentEmail, txtDateTimeAttendance, txtFullname, txtAddress, txtStatus, txtdateTimeLeaving, txtLeaveLocation;
        CardView cardView;

        public DataHolderView(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardEmpInData);
            txtParentEmail = itemView.findViewById(R.id.txtParentEmail);
            txtFullname = itemView.findViewById(R.id.txtFullname);
            txtAddress = itemView.findViewById(R.id.txtAttendanceLocation);
            txtDateTimeAttendance = itemView.findViewById(R.id.txtDateTimeAttendance);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtdateTimeLeaving = itemView.findViewById(R.id.txtdateTimeLeaving);
            txtLeaveLocation = itemView.findViewById(R.id.txtLeaveLocation);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    //method to copy text of any element
    public void copyToClipboard(String copyText, View view) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager)
                    view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(copyText);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                    view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData
                    .newPlainText("Your ", copyText);
            clipboard.setPrimaryClip(clip);
        }
        Toast.makeText(view.getContext(), copyText + " is copied", Toast.LENGTH_SHORT).show();
    }

    private void getMonthlySummaryAttendance(DataHolderView holder, Attendance employees){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("Attendees").limitToFirst(30).orderByChild("Email").equalTo(employees.getEmail()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()){
                    long  count=  snapshot.getChildrenCount();
                    holder.txtStatus.setText(String.valueOf(count));
                }
                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    long count = snapshot.getChildrenCount();
                    holder.txtStatus.setText(String.valueOf(count));
                }
                notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    long  count=  snapshot.getChildrenCount();
                    holder.txtStatus.setText(String.valueOf(count));
                }
                notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()){
                    long  count=  snapshot.getChildrenCount();
                    holder.txtStatus.setText(String.valueOf(count));
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}