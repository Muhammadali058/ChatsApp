package com.example.whatsapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Toast;

import com.example.whatsapp.HP;
import com.example.whatsapp.databinding.ActivityVerificationBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mukesh.OnOtpCompletionListener;

import java.util.concurrent.TimeUnit;

public class VerificationActivity extends AppCompatActivity {

    ActivityVerificationBinding binding;
    FirebaseAuth auth;
    String verificationId;
    ProgressDialog dialog;
    String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        sendCode();
    }

    private void init(){
        auth = FirebaseAuth.getInstance();

        phoneNumber = getIntent().getStringExtra("phoneNumber");
        binding.verifyPhoneNumber.setText("Verify " + phoneNumber);
        binding.phoneNumber.setText(phoneNumber);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending code...");
        dialog.setCancelable(false);

        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(binding.otpView.getText().length() > 0) {
                    String code = binding.otpView.getText().toString();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                    auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Intent intent = new Intent(VerificationActivity.this, ProfileInfoActivity.class);
                                intent.putExtra("phoneNumber", phoneNumber);
                                startActivity(intent);
                                finishAffinity();
                            }else {
                                Toast.makeText(VerificationActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        binding.resendSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(binding.timer.getText().equals("0:00"))
                    sendCode();
            }
        });
    }

    private void sendCode(){
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(120L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                dialog.dismiss();
                                finish();
                            }

                            @Override
                            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(s, forceResendingToken);
                                dialog.dismiss();
                                new CountDownTimer(120000,1000){

                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        long seconds = millisUntilFinished/1000;

                                        int min = (int) Math.floor((seconds / 60));
                                        int sec = (int) (seconds % 60);

                                        String minute = "";
                                        String second = "";

                                        if(min == 0)
                                            minute = "00";
                                        else
                                            minute = "0" + String.valueOf(min);

                                        if(sec < 10)
                                            second = "0" + String.valueOf(sec);
                                        else
                                            second = String.valueOf(sec);

                                        String time = minute + ":" + second;

                                        binding.timer.setText(time);
                                    }

                                    @Override
                                    public void onFinish() {
                                        binding.timer.setText("0:00");
                                    }
                                }.start();

                                verificationId = s;
                            }
                        })
                        .build();

        dialog.show();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

}