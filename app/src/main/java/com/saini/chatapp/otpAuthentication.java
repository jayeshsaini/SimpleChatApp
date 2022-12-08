package com.saini.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class otpAuthentication extends AppCompatActivity {

    TextView tvChangeNumber;
    EditText etGetOtp;
    android.widget.Button btnVerifyOtp;
    String enteredOtp;
    FirebaseAuth firebaseAuth;
    ProgressBar progressBarOtpAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_authentication);

        tvChangeNumber = findViewById(R.id.changenumber);
        btnVerifyOtp = findViewById(R.id.verifyotp);
        etGetOtp = findViewById(R.id.getotp);
        progressBarOtpAuth = findViewById(R.id.progressbarofotpauth);

        firebaseAuth = FirebaseAuth.getInstance();

        tvChangeNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(otpAuthentication.this, MainActivity.class);
                startActivity(intent);
            }
        });

        btnVerifyOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enteredOtp = etGetOtp.getText().toString();

                if(enteredOtp.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Enter your OTP First", Toast.LENGTH_SHORT).show();
                }
                else {
                    progressBarOtpAuth.setVisibility(View.VISIBLE);
                    String codeReceived = getIntent().getStringExtra("otp");

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeReceived, enteredOtp);
                    signInWithPhoneAuthCredential(credential);
                }

            }
        });

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    progressBarOtpAuth.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Login Success", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(otpAuthentication.this, SetProfile.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    if(task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        progressBarOtpAuth.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}