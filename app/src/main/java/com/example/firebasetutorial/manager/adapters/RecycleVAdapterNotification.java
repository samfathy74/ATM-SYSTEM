package com.example.firebasetutorial.manager.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebasetutorial.R;
import com.example.firebasetutorial.classes.Notification;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RecycleVAdapterNotification extends FirebaseRecyclerAdapter<Notification, RecycleVAdapterNotification.DataHolderView> {
    Context context;
    private int mExpandedPosition = -1;
    private RecyclerView recyclerView = null;
    DatabaseReference reference;
    private int drawIcon;


    public RecycleVAdapterNotification(@NonNull FirebaseRecyclerOptions<Notification> options) {
        super(options);
    }

    @NonNull
    @Override
    public DataHolderView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DataHolderView(LayoutInflater.from(parent.getContext()).inflate(R.layout.custm_rv_notification, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindViewHolder(@NonNull DataHolderView holder, int position, @NonNull Notification notification) {
        final boolean isExpanded = position == mExpandedPosition;

        holder.txtEmailN.setText(notification.getEmail());
        holder.txtFullnameN.setText(Html.fromHtml("<font color='#F43E31' >Full Name: </font> " + notification.getName()));
        holder.txtRequestTypeN.setText(Html.fromHtml("<font color='#F43E31'>Request Type:</font> " + notification.getRequest_Type()));
        holder.txtRequestTimeN.setText(notification.getDateTime_Requested());

        holder.txtRequestDescripN.setText(Html.fromHtml("<font color='#F43E31'>Subject of Request:</font><br/>" + notification.getRequest_Case()));
        holder.txtLocationN.setText(Html.fromHtml("<font color='#F43E31'>Location of Request: </font><br/>" + notification.getLocation()));
        holder.txtRequestN.setText(Html.fromHtml("<font color='#F43E31'>Signature :</font> " +notification.getSignature()));

        holder.btnRefuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRequestCode("-1", notification.getDepartment(), notification.getEmail(), v);
            }
        });

        holder.btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRequestCode("1", notification.getDepartment(), notification.getEmail(), v);
            }
        });

        //checkRecycleEmptyorNot(holder, position, notification);
        updateDrawIcon(drawIcon, notification, holder);

        holder.cardNotifyN.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.itemView.setActivated(isExpanded);
        holder.parentLayoutN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExpanded) {
                    holder.txtEmailN.setCompoundDrawablesRelativeWithIntrinsicBounds(drawIcon, 0, R.drawable.ic_flat_arrow_down, 0);
                } else {
                    holder.txtEmailN.setCompoundDrawablesRelativeWithIntrinsicBounds(drawIcon, 0, R.drawable.ic_flat_arrow_up, 0);
                }
                mExpandedPosition = isExpanded ? -1 : position;
                TransitionManager.beginDelayedTransition(recyclerView);
                notifyDataSetChanged();
            }
        });

        holder.txtLocationN.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String requestAddress = holder.txtLocationN.getText().toString();
                if(requestAddress.length()>30) {
                    if (requestAddress.contains("&")) {
                        copyToClipboard(requestAddress.substring(21 + 21), view);
                    } else {
                        copyToClipboard(requestAddress.substring(21), view);
                    }
                }else{
                    copyToClipboard("NULL", view);
                }
                return true;
            }
        });
    }

    private void updateRequestCode(String code, String department, String email, View view) {
        reference = FirebaseDatabase.getInstance().getReference();
        reference.child("Requests").child(department).child(email.replace(".", ","))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        HashMap<String, Object> result = new HashMap<>();
                        result.put("RequestCode", code);
                        result.put("Signature","Manager");
                        reference.child("Requests").child(department).child(email.replace(".", ",")).updateChildren(result);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(view.getContext(), "Database Error!!" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateDrawIcon(int drawIcon, Notification notification, DataHolderView holder) {
        if (notification.getRequestCode().equals("1") || notification.getRequestCode().equals("-1")) {
            holder.txtEmailN.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_open_mail, 0, 0, 0);
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnRefuse.setVisibility(View.GONE);
            holder.txtRequestN.setVisibility(View.VISIBLE);
            drawIcon = R.drawable.ic_open_mail;
        } else {
            drawIcon = R.drawable.ic_close_mail;
            holder.txtRequestN.setVisibility(View.GONE);
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnRefuse.setVisibility(View.VISIBLE);
            holder.txtEmailN.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_close_mail, 0, 0, 0);
        }
    }

    class DataHolderView extends RecyclerView.ViewHolder {
        TextView txtRequestTimeN, txtRequestTypeN, txtFullnameN, txtLocationN, txtEmailN, txtRequestDescripN, txtRequestN;
        LinearLayout parentLayoutN, LinerLayoutEmptyNotify;
        CardView cardNotifyN;
        Button btnAccept, btnRefuse;

        public DataHolderView(@NonNull View itemView) {
            super(itemView);
            cardNotifyN = itemView.findViewById(R.id.cardNotifyN);

            txtRequestTimeN = itemView.findViewById(R.id.txtRequestTimeN);
            txtFullnameN = itemView.findViewById(R.id.txtFullnameN);
            txtLocationN = itemView.findViewById(R.id.txtLocationN);
            txtRequestTypeN = itemView.findViewById(R.id.txtRequestTypeN);
            txtEmailN = itemView.findViewById(R.id.txtEmailN);
            txtRequestDescripN = itemView.findViewById(R.id.txtRequestDescripN);
            txtRequestN = itemView.findViewById(R.id.txtRequestN);

            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnRefuse = itemView.findViewById(R.id.btnRefuse);

            parentLayoutN = itemView.findViewById(R.id.parentLayoutN);
            LinerLayoutEmptyNotify = itemView.findViewById(R.id.LinerLayoutEmptyNotify1);
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
}
