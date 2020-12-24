package com.example.firebasetutorial.employee.adapters;

import android.annotation.SuppressLint;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebasetutorial.R;
import com.example.firebasetutorial.classes.Notification;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class RVAdapterRequestsHistory extends FirebaseRecyclerAdapter<Notification, RVAdapterRequestsHistory.DataHolderView> {

    public RVAdapterRequestsHistory(@NonNull FirebaseRecyclerOptions<Notification> options) {
        super(options);
    }

    @NonNull
    @Override
    public DataHolderView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DataHolderView(LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_rv_request_history, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindViewHolder(@NonNull DataHolderView holder, int position, @NonNull Notification notification) {
        holder.requestTime.setText(notification.getDateTime_Requested());
        holder.requestType.setText(notification.getRequest_Type());
        holder.requestCase.setText(Html.fromHtml("<font color='#F43E31'>Request Case: </font><br/>" + notification.getRequest_Case()));
        holder.requestSignature.setText(Html.fromHtml("<font color='#F43E31'>Signature by: </font>" +notification.getSignature()));
        String requestCode = notification.getRequestCode();
        switch (requestCode) {
            case "0":
                holder.requestImg.setImageResource(R.drawable.ic_waiting);
                holder.requestSignature.setVisibility(View.GONE);
                break;
            case "-1":
                holder.requestImg.setImageResource(R.drawable.unaccetped);
                holder.requestSignature.setVisibility(View.VISIBLE);
                break;
            case "1":
                holder.requestImg.setImageResource(R.drawable.ic_request_accept);
                holder.requestSignature.setVisibility(View.VISIBLE);
                break;
        }
    }

    class DataHolderView extends RecyclerView.ViewHolder {
        TextView requestType, requestTime,requestCase,requestSignature;
        ImageView requestImg;

        public DataHolderView(@NonNull View itemView) {
            super(itemView);
            requestType = itemView.findViewById(R.id.requestType);
            requestCase = itemView.findViewById(R.id.requestCase);
            requestTime = itemView.findViewById(R.id.requestTime);
            requestImg = itemView.findViewById(R.id.requestImg);
            requestSignature = itemView.findViewById(R.id.requestSignature);
        }
    }
}

