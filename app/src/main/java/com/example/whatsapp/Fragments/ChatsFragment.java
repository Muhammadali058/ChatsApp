package com.example.whatsapp.Fragments;

import static android.content.Context.TELEPHONY_SERVICE;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.whatsapp.Activities.ChatsActivity;
import com.example.whatsapp.Activities.ContactsActivity;
import com.example.whatsapp.Adapters.FriendsAdapter;
import com.example.whatsapp.HP;
import com.example.whatsapp.Models.Contacts;
import com.example.whatsapp.Models.Users;
import com.example.whatsapp.R;
import com.example.whatsapp.databinding.FragmentChatsBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ChatsFragment extends Fragment {

    FragmentChatsBinding binding;
    FirebaseDatabase database;
    FirebaseMessaging messaging;
    FirebaseAuth auth;
    List<Users> users;
    List<Contacts> contacts;
    FriendsAdapter friendsAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChatsBinding.bind(inflater.inflate(R.layout.fragment_chats, container, false));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 123);
            }
        }

        init();
        setFriendsAdapter();

        loadContacts();
        syncContacts();

        return binding.getRoot();
    }

    private void init(){
        database = FirebaseDatabase.getInstance();
        messaging = FirebaseMessaging.getInstance();
        auth = FirebaseAuth.getInstance();
        users = new ArrayList<>();
        contacts = new ArrayList<>();

        messaging.getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                Map<String, Object> map = new HashMap<>();
                map.put("token", s);
                database.getReference("users")
                        .child(auth.getUid())
                        .updateChildren(map);
            }
        });

        binding.createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ContactsActivity.class);
                startActivityForResult(intent, 123);
            }
        });
    }

    private void setFriendsAdapter(){
        friendsAdapter = new FriendsAdapter(getActivity(), users);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.recyclerView.setAdapter(friendsAdapter);

        database.getReference("friends")
                .child(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    users.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Users user = dataSnapshot.getValue(Users.class);
                        users.add(user);
                    }
                    Collections.reverse(users);
                    friendsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadContacts() {
        String ISOPrefix = HP.getCountryISO(getActivity());

        final String[] PROJECTION = new String[]{
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        Cursor cursor = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
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
                        Contacts contact = new Contacts(phoneNumber, name);
                        contacts.add(contact);

                        mobileNoSet.add(phoneNumber);
                    }
                }
            } finally {
                cursor.close();
            }
        }
    }

    public void syncContacts(){
        database.getReference("friends")
                .child(auth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            String friendId = dataSnapshot.getKey();
                            String phoneNumber = dataSnapshot.child("phoneNumber").getValue(String.class);

                            // loop through all contacts
                            boolean isExists = false;
                            String name = null;
                            for(Contacts contact : contacts){
                                String number = contact.getPhoneNumber();
                                if(phoneNumber.contains(number)){
                                    isExists = true;
                                    name = contact.getName();
                                    break;
                                }
                            }

                            Map<String, Object> map = new HashMap<>();
                            if(isExists){
                                map.put("name", name);
                            }else {
                                map.put("name", phoneNumber);
                            }

                            database.getReference("friends")
                                    .child(auth.getUid())
                                    .child(friendId)
                                    .updateChildren(map);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123){
            if(data.getSerializableExtra("user") != null){
                Users user = (Users) data.getSerializableExtra("user");
                Intent intent = new Intent(getActivity(), ChatsActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
            }
        }

    }
}