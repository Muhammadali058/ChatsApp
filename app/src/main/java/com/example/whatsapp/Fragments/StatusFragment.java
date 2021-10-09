package com.example.whatsapp.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.whatsapp.Activities.ContactsActivity;
import com.example.whatsapp.Adapters.StatusesAdapter;
import com.example.whatsapp.Models.Status;
import com.example.whatsapp.Models.Stories;
import com.example.whatsapp.Models.Users;
import com.example.whatsapp.R;
import com.example.whatsapp.databinding.FragmentChatsBinding;
import com.example.whatsapp.databinding.FragmentStatusBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatusFragment extends Fragment {

    FragmentStatusBinding binding;
    FirebaseDatabase database;
    FirebaseStorage storage;
    FirebaseAuth auth;
    StatusesAdapter statusesAdapter;
    List<Stories> stories;
    ProgressDialog dialog;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStatusBinding.bind(inflater.inflate(R.layout.fragment_status, container, false));

        init();
        setStatusAdapter();

        return binding.getRoot();
    }

    private void init(){
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        binding.createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 123);
            }
        });

        dialog = new ProgressDialog(getActivity());
        dialog.setCancelable(false);
        dialog.setMessage("Updating status...");
    }

    private void setStatusAdapter(){
        stories = new ArrayList<>();
        statusesAdapter = new StatusesAdapter(getActivity(), stories);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.recyclerView.setAdapter(statusesAdapter);

        database.getReference("friends")
                .child(auth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot friendsSnapshot) {
                        if(friendsSnapshot.exists()){
                            // Loop through all friends
                            for(DataSnapshot friendSnapshot : friendsSnapshot.getChildren()){
                                String uId = friendSnapshot.getKey();

                                database.getReference("statuses")
                                        .child(uId)
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot statusesSnapshot) {
                                                if(statusesSnapshot.exists()){
                                                    database.getReference("users")
                                                            .child(uId)
                                                            .addValueEventListener(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot usersSnapshot) {
                                                                    Users user = usersSnapshot.getValue(Users.class);

                                                                    Stories story = new Stories();
                                                                    story.setuId(user.getuId());
                                                                    story.setName(user.getName());
                                                                    story.setProfileImage(user.getImageUrl());

                                                                    List<Status> statuses = new ArrayList<>();
                                                                    for (DataSnapshot statusSnapshot : statusesSnapshot.getChildren()){
                                                                        Status status = statusSnapshot.getValue(Status.class);
                                                                        statuses.add(status);
                                                                    }
                                                                    story.setStatuses(statuses);

                                                                    stories.add(story);
                                                                    statusesAdapter.notifyDataSetChanged();
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {

                                                                }
                                                            });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                            }
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
        if(requestCode == 123){
            if(data != null){
                if(data.getData() != null){
                    dialog.show();
                    Uri imageUri = data.getData();
                    StorageReference reference = storage.getReference("statuses")
                            .child(auth.getUid())
                            .child(Calendar.getInstance().getTimeInMillis() + "");

                    reference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Status status = new Status(
                                                uri.toString(),
                                                new Date().getTime()
                                        );

                                        database.getReference("statuses")
                                            .child(auth.getUid())
                                            .push()
                                            .setValue(status);

                                        dialog.dismiss();
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
    }
}