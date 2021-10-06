package com.example.whatsapp.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.example.whatsapp.Adapters.CountriesAdapter;
import com.example.whatsapp.HP;
import com.example.whatsapp.Models.Countries;
import com.example.whatsapp.R;
import com.example.whatsapp.databinding.ActivityPhoneNumberBinding;

import java.util.List;

public class PhoneNumberActivity extends AppCompatActivity {

    ActivityPhoneNumberBinding binding;
    List<Countries> countries;
    CountriesAdapter countriesAdapter;
    Dialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.confirm_number_layout);
        TextView editBtn = dialog.findViewById(R.id.editBtn);
        TextView okBtn = dialog.findViewById(R.id.okBtn);
        TextView phoneNumber = dialog.findViewById(R.id.name);

        binding.phoneNumber.requestFocus();

        String number = "<a href='http://www.google.com'>What's my number?</a>";
        binding.number.setText(HP.removeUnderline(number));

        binding.country.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PhoneNumberActivity.this, CountriesActivity.class);
                startActivityForResult(intent, 123);
            }
        });

        binding.nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = binding.code.getText().toString() + binding.phoneNumber.getText().toString();
                phoneNumber.setText(number);
                dialog.show();
            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                binding.phoneNumber.requestFocus();
                /* hide keyboard */
//                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);

                /* show keyboard */
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(PhoneNumberActivity.this, VerificationActivity.class);
                String phoneNumber = "+" + binding.code.getText().toString() + binding.phoneNumber.getText().toString();
                intent.putExtra("phoneNumber", phoneNumber);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 123){
            Countries country = (Countries) data.getSerializableExtra("country");
            binding.country.setText(country.getName());
            binding.code.setText(country.getCode());
        }
    }
}