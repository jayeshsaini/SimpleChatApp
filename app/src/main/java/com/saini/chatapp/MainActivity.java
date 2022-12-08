package com.saini.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    EditText etGetPhoneNumber;
    android.widget.Button btnSendOTP;
    CountryCodePicker cCodePicker;
    String countryCode;
    String phoneNumber;

    FirebaseAuth firebaseAuth;
    ProgressBar progressBarMain;


    PhoneAuthProvider.OnVerificationStateChangedCallbacks papCallbacks;
    String codeSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cCodePicker = findViewById(R.id.countrycodepicker);
        btnSendOTP = findViewById(R.id.sendotpbutton);
        etGetPhoneNumber = findViewById(R.id.getphonenumber);
        progressBarMain = findViewById(R.id.progressbarofmain);

        firebaseAuth = FirebaseAuth.getInstance();

        countryCode = cCodePicker.getSelectedCountryCodeWithPlus();

        cCodePicker.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                countryCode = cCodePicker.getSelectedCountryCodeWithPlus();
            }
        });

        btnSendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number;
                number = etGetPhoneNumber.getText().toString();

                if(number.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter your number", Toast.LENGTH_SHORT).show();
                }
                else if(number.length()<10) {
                    Toast.makeText(getApplicationContext(), "Please enter correct number", Toast.LENGTH_SHORT).show();
                } else {
                    progressBarMain.setVisibility((View.VISIBLE));
                    phoneNumber = countryCode + number;

                    PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                            .setPhoneNumber(phoneNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(MainActivity.this)
                            .setCallbacks(papCallbacks)
                            .build();

                    PhoneAuthProvider.verifyPhoneNumber(options);

                }

            }
        });

        papCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                // how to automatically fetch code here
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                Toast.makeText(getApplicationContext(), "OTP is sent", Toast.LENGTH_SHORT).show();
                progressBarMain.setVisibility(View.INVISIBLE);
                codeSent = s;
                Intent intent = new Intent(MainActivity.this, otpAuthentication.class);
                intent.putExtra("otp", codeSent);
                startActivity(intent);
            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}