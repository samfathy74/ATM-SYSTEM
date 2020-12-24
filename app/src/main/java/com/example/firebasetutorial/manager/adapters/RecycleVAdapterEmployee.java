package com.example.firebasetutorial.manager.adapters;

import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebasetutorial.R;
import com.example.firebasetutorial.classes.Employees;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class RecycleVAdapterEmployee extends FirebaseRecyclerAdapter<Employees, RecycleVAdapterEmployee.DataHolderView> {
    public RecycleVAdapterEmployee(@NonNull FirebaseRecyclerOptions<Employees> options) {
        super(options);
    }

    @NonNull
    @Override
    public DataHolderView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DataHolderView(LayoutInflater.from(parent.getContext()).inflate(R.layout.custm_rv_emps, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull DataHolderView holder, int position, @NonNull Employees employees) {
        holder.txtFname.setText(employees.getFirst_Name());
        holder.txtLname.setText(employees.getLast_Name());
        holder.txtEmail.setText(Html.fromHtml("<font color='#F43E31'>Email: </font>" + employees.getEmail()));
        holder.txtPhone.setText(Html.fromHtml("<font color='#F43E31'>Phone: </font>" + employees.getPhone()));
        holder.txtRole.setText(Html.fromHtml("<font color='#F43E31'>Role: </font>" + employees.getRole()));
        holder.txtDateTimeCreated.setText(Html.fromHtml("<font color='#F43E31'>Date of Employment: </font><br/>" + employees.getDateTime_of_account_created().substring(0, Math.min(employees.getDateTime_of_account_created().length(), 10))));
        if (employees.getProfile_Image() != null) {
            StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(employees.getProfile_Image());
            gsReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(holder.uploadImg);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    holder.uploadImg.setImageResource(R.drawable.add_photo);
                }
            });
        } else {
            holder.uploadImg.setImageResource(R.drawable.add_photo);
        }
    }

    class DataHolderView extends RecyclerView.ViewHolder {
        TextView txtDateTimeCreated, txtFname, txtLname, txtEmail, txtRole, txtPhone;
        ImageView uploadImg;
        CardView cardView;

        public DataHolderView(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardEmpData);
            txtDateTimeCreated = itemView.findViewById(R.id.txtdateTime);
            txtFname = itemView.findViewById(R.id.txtfname);
            txtLname = itemView.findViewById(R.id.txtlname);
            txtEmail = itemView.findViewById(R.id.txtemail);
            txtPhone = itemView.findViewById(R.id.txtphone);
            txtRole = itemView.findViewById(R.id.txtrole);
            uploadImg = itemView.findViewById(R.id.empProImage);
        }
    }
}
