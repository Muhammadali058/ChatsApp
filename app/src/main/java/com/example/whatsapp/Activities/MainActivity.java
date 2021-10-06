package com.example.whatsapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.whatsapp.Adapters.FragmentsAdapter;
import com.example.whatsapp.HP;
import com.example.whatsapp.R;
import com.example.whatsapp.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseDatabase database;
    FirebaseRemoteConfig mFirebaseRemoteConfig;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        database = FirebaseDatabase.getInstance();

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        mFirebaseRemoteConfig.fetchAndActivate().addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                String toolbarColor = mFirebaseRemoteConfig.getString("toolbarColor");
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(toolbarColor)));
            }
        });

        FragmentsAdapter fragmentsAdapter = new FragmentsAdapter(this);
        binding.viewPager.setAdapter(fragmentsAdapter);

        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(binding.tabLayout, binding.viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position){
                    case 0:
                        tab.setText("CHATS");
                        break;
                    case 1:
                        tab.setText("STATUS");
                        break;
                    case 2:
                        tab.setText("CALLS");
                        break;
                }
            }
        });
        tabLayoutMediator.attach();
    }

    @Override
    protected void onResume() {
        super.onResume();
        database.getReference("presence")
                .child(FirebaseAuth.getInstance().getUid())
                .child("status")
                .setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        database.getReference("presence")
                .child(FirebaseAuth.getInstance().getUid())
                .child("status")
                .setValue("last seen at " + HP.getFormatedTime(new Date().getTime()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, SplashActivity.class);
                startActivity(intent);
                finishAffinity();
                break;
        }
        return true;
    }
}