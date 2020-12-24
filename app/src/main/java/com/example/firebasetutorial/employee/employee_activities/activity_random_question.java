package com.example.firebasetutorial.employee.employee_activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.firebasetutorial.R;
import com.example.firebasetutorial.activity_login;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class activity_random_question extends Activity {

    FirebaseAuth auth;
    DatabaseReference reference;
    SharedPreferences preferences;
    String status, question, uAnswer, correctAnswer;
    String currentDateTime = new SimpleDateFormat("(EEEE) dd/MM/yyyy - hh:mm:ss a", Locale.ENGLISH).format(Calendar.getInstance().getTime()).toString();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_question);
        //DATABASE INITIALIZE
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);

        if (auth.getCurrentUser() == null) {
            //home page
            startActivity(new Intent(getApplicationContext(), activity_login.class));
            finish();
        }
        onOpenActivity();
    }

    @SuppressLint("SetTextI18n")
    private void onOpenActivity() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(activity_random_question.this, R.style.CustomAlertDialog).setCancelable(false);
        View view = getLayoutInflater().inflate(R.layout.alert_questions, null);
        builder1.setTitle("");

        TextView title = view.findViewById(R.id.alertTitle);
        TextView msg = view.findViewById(R.id.alertMsg);
        TextView txtQuestion = view.findViewById(R.id.txtQuestion);
        TextView txtCounter = view.findViewById(R.id.txtCountDown);
        EditText etxtAnswer = view.findViewById(R.id.etxtAnswer);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);
        builder1.setView(view);

        Random r = new Random();
        int x = r.nextInt(10);
        int y = r.nextInt(100);
        int z = r.nextInt(50);
        int sumxyz = x + y + z;
        txtQuestion.setText("( " + x + " + " + y + " + " + z + " )");


        CountDownTimer timer = new CountDownTimer(5 * 1000 * 60, 1000) {
            @SuppressLint("DefaultLocale")
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                int minutes = (int) ((millisUntilFinished / (1000 * 60)) % 60);
                int hours = (int) ((millisUntilFinished / (1000 * 60 * 60)) % 24);
                txtCounter.setText(String.format("%d:%d:%d", hours, minutes, seconds));
            }

            public void onFinish() {
                if (String.valueOf(sumxyz).equals(etxtAnswer.getText().toString().trim())) {
                    status = "attentive";
                } else {
                    status = "Not attentive";
                }
                question = "( " + x + " + " + y + " + " + z + " )";
                correctAnswer = String.valueOf(sumxyz);
                uAnswer = etxtAnswer.getText().toString().trim();
                setQuestionDataToDatabase(question, correctAnswer, uAnswer, status, currentDateTime);
                Toast.makeText(activity_random_question.this, "thank you", Toast.LENGTH_SHORT).show();
                finish();
            }
        };
        timer.start();

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.cancel();
                if (etxtAnswer.getText().toString().isEmpty()) {
                    etxtAnswer.setError("can not leave uAnswer null!");
                } else {
                    if (String.valueOf(sumxyz).equals(etxtAnswer.getText().toString().trim())) {
                        status = "attentive";
                    } else {
                        status = "Not attentive";
                        System.out.print("false");
                    }
                    question = String.valueOf("( " + x + " + " + y + " + " + z + " )");
                    correctAnswer = String.valueOf(sumxyz);
                    uAnswer = etxtAnswer.getText().toString().trim();
                    setQuestionDataToDatabase(question, correctAnswer, uAnswer, status, currentDateTime);
                    Toast.makeText(activity_random_question.this, "thank you", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

        AlertDialog dialog1 = builder1.create();
        dialog1.show();
    }


    private void setQuestionDataToDatabase(String question, String correctAnswer, String uAnswer, String status, String currentDateTime) {
        preferences = getSharedPreferences("eprofileData", MODE_PRIVATE);
        String department = preferences.getString("Department", "");
        String email = auth.getCurrentUser().getEmail().replace(".", ",");

        reference.child("RandomTest").child(department).child(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> map = new HashMap<>();
                map.put("Email", email.replace(",", "."));
                map.put("Question", question);
                map.put("UserAnswer", uAnswer);
                map.put("Answer", correctAnswer);
                map.put("Status", status);
                map.put("DateTime", currentDateTime);
                reference.child("RandomTest").child(department).child(email).setValue(map);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

}
