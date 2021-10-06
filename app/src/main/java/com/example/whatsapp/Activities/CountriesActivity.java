package com.example.whatsapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.whatsapp.Adapters.CountriesAdapter;
import com.example.whatsapp.Models.Countries;
import com.example.whatsapp.R;
import com.example.whatsapp.databinding.ActivityCountriesBinding;

import java.util.ArrayList;
import java.util.List;

public class CountriesActivity extends AppCompatActivity {
    ActivityCountriesBinding binding;
    List<Countries> countries;
    CountriesAdapter countriesAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCountriesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        countries = new ArrayList<>();
        countries.add(new Countries(R.drawable.pakistan, "Pakistan", "92"));
        countries.add(new Countries(R.drawable.india, "India", "91"));
        countries.add(new Countries(R.drawable.china, "China", "86"));
        countries.add(new Countries(R.drawable.america, "America", "1"));
        countries.add(new Countries(R.drawable.spain, "Spain", "34"));

        countriesAdapter = new CountriesAdapter(this, countries);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(countriesAdapter);

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                countriesAdapter.getFilter().filter(newText);
                return false;
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivity();
            }
        });

    }

    @Override
    public void onBackPressed() {
        finishActivity();
        super.onBackPressed();
    }

    private void finishActivity(){
        Intent intent = new Intent();
        intent.putExtra("country", countries.get(0));

        setResult(123, intent);
        finish();
    }
}