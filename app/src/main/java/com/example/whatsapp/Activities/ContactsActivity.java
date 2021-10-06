package com.example.whatsapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.example.whatsapp.Adapters.ContactsAdapter;
import com.example.whatsapp.HP;
import com.example.whatsapp.Models.Contacts;
import com.example.whatsapp.Models.Users;
import com.example.whatsapp.R;
import com.example.whatsapp.databinding.ActivityContactsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {

    ActivityContactsBinding binding;
    FirebaseDatabase database;
    List<Users> users;
    List<Users> usersFullList;
    ContactsAdapter contactsAdapter;
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Select contact");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading contacts...");
        dialog.setCancelable(false);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 123);
            }
        }

        database = FirebaseDatabase.getInstance();
        users = new ArrayList<>();
        usersFullList = new ArrayList<>();

        dialog.show();
        getAuthenticatedUsersList();
        contactsAdapter = new ContactsAdapter(ContactsActivity.this, users, usersFullList);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(ContactsActivity.this));
        binding.recyclerView.setAdapter(contactsAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contacts_menu, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                contactsAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @NonNull
    private void getAuthenticatedUsersList() {
        String ISOPrefix = HP.getCountryISO(this);

        final String[] PROJECTION = new String[]{
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        if (cursor != null) {
            HashSet<String> mobileNoSet = new HashSet<String>();
            try {
                final int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                final int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                String name, phoneNumber;
                while (cursor.moveToNext()) {
                    name = cursor.getString(nameIndex);
                    phoneNumber = cursor.getString(numberIndex);

                    phoneNumber = phoneNumber.replace(" ","");
                    phoneNumber = phoneNumber.replace("-","");
                    phoneNumber = phoneNumber.replace("(","");
                    phoneNumber = phoneNumber.replace(")","");

                    if (String.valueOf(phoneNumber.charAt(0)).equals("0")) {
                        phoneNumber = phoneNumber.substring(1);
                        phoneNumber = ISOPrefix + phoneNumber;
                    }else {
                        if (!String.valueOf(phoneNumber.charAt(0)).equals("+")) {
                            phoneNumber = ISOPrefix + phoneNumber;
                        }
                    }

                    // Not to add duplicate number
                    if (!mobileNoSet.contains(phoneNumber)) {
                        String finalPhoneNumber = phoneNumber;
                        String finalName = name;

                        // checking phoneNumber in database
                        database.getReference("users")
                                .orderByChild("phoneNumber")
                                .equalTo(finalPhoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                        Users user = dataSnapshot.getValue(Users.class);
                                        user.setName(finalName);

                                        // if contacts phoneNumber == online user phoneNumber
                                        if((finalPhoneNumber.equals(user.getPhoneNumber()))){
                                            // not to add own phoneNumber
                                            if(!finalPhoneNumber.equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())){
                                                users.add(user);
                                                usersFullList.add(user);

                                                dialog.dismiss();
                                                getSupportActionBar().setSubtitle(String.valueOf(users.size()) + " contacts");
                                                contactsAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    }
                                }else {
                                    dialog.dismiss();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        mobileNoSet.add(phoneNumber);
                    }
                }
            } finally {
                cursor.close();
            }
        }
    }

    @Override
    public void onBackPressed() {
        goBack();
        super.onBackPressed();
    }

    private void goBack(){
        Intent intent = new Intent();
//        intent.putExtra("user", "null");
        setResult(123, intent);
    }
}