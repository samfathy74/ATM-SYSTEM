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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebasetutorial.R;
import com.example.firebasetutorial.classes.Attentive;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class RecycleVAdapterAttentive extends FirebaseRecyclerAdapter<Attentive, RecycleVAdapterAttentive.DataHolderView> {
    private int mExpandedPosition = -1;
    private RecyclerView recyclerView = null;
    private int drawIcon = R.drawable.ic_flat_arrow_down;

    public RecycleVAdapterAttentive(@NonNull FirebaseRecyclerOptions<Attentive> options) {
        super(options);
    }

    @NonNull
    @Override
    public DataHolderView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DataHolderView(LayoutInflater.from(parent.getContext()).inflate(R.layout.custm_rv_follow_attendance, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindViewHolder(@NonNull DataHolderView holder, int position, @NonNull Attentive Attentive) {
        final boolean isExpanded = position == mExpandedPosition;
        holder.txtStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
        holder.txtParentEmail.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, drawIcon, 0);
        holder.txtAnswer.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
        holder.fullname.setVisibility(View.GONE);

        holder.txtParentEmail.setText(Attentive.getEmail());
        holder.txtAnswer.setText(Html.fromHtml("<font color='#F43E31'>Correct Answer: </font><br/>" + Attentive.getAnswer()));
        holder.txtDateTime.setText(Html.fromHtml("<font color='#F43E31'>Date and Time of Request:</font><br/>" + Attentive.getDateTime()));
        holder.txtStatus.setText(Html.fromHtml("<font color='#F43E31'>Status: </font>" + Attentive.getStatus()));
        holder.txtQuestion.setText(Html.fromHtml("<font color='#F43E31'>Question:</font><br/>" + Attentive.getQuestion()));
        holder.txtUserAnswer.setText(Html.fromHtml("<font color='#F43E31'>Employee Answer:</font><br/>" + Attentive.getUserAnswer()));

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

    }


    class DataHolderView extends RecyclerView.ViewHolder {
        TextView txtParentEmail, txtDateTime, txtAnswer, txtStatus, txtUserAnswer, txtQuestion,fullname;
        CardView cardView;

        public DataHolderView(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardEmpInData);
            txtParentEmail = itemView.findViewById(R.id.txtParentEmail);
            txtQuestion = itemView.findViewById(R.id.txtAttendanceLocation);
            txtUserAnswer = itemView.findViewById(R.id.txtDateTimeAttendance);
            txtAnswer = itemView.findViewById(R.id.txtStatus);
            txtStatus = itemView.findViewById(R.id.txtdateTimeLeaving);
            txtDateTime = itemView.findViewById(R.id.txtLeaveLocation);
            fullname = itemView.findViewById(R.id.txtFullname);

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