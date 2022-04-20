package com.example.whatsapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.UrlQuerySanitizer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.whatsapp.Adapters.MessagesAdapter;
import com.example.whatsapp.HP;
import com.example.whatsapp.Models.Messages;
import com.example.whatsapp.Models.Users;
import com.example.whatsapp.R;
import com.example.whatsapp.databinding.ActivityChatsBinding;
import com.google.android.gms.common.internal.IGmsCallbacks;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatsActivity extends AppCompatActivity {

    ActivityChatsBinding binding;
    FirebaseDatabase database;
    FirebaseStorage storage;
    FirebaseAuth auth;
    Users user;
    String senderId, receiverId;
    String senderRoom, receiverRoom;
    List<Messages> messages;
    MessagesAdapter messagesAdapter;
    PopupWindow popupWindow;
    View popupView;
    ProgressDialog dialog;
    final int IMAGE_INTENT = 1;
    final int VIDEO_INTENT = 2;
    final int AUDIO_INTENT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        initButtons();
        initAttatchmentButtons();

        setMessagesAdapter();
    }

    private void init(){
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("");

        user = (Users) getIntent().getSerializableExtra("user");

        popupView = LayoutInflater.from(this).inflate(R.layout.attatchment_layout, null);
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        popupWindow = new PopupWindow(popupView, width, height, true);

        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Sending...");

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        senderId = auth.getUid();
        receiverId = user.getuId();

        senderRoom = senderId + receiverId;
        receiverRoom = receiverId + senderId;

        /* Settings Toolbar Details */
        binding.name.setText(user.getName());
        Glide.with(this).load(user.getImageUrl()).placeholder(R.drawable.avatar).into(binding.image);
            // Getting user status
            database.getReference("presence")
                    .child(receiverId)
                    .child("status").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        String status = snapshot.getValue(String.class);
                        binding.status.setText(status);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        binding.message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                database.getReference("presence")
                        .child(senderId)
                        .child("status")
                        .setValue("typing...");

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        database.getReference("presence")
                                .child(senderId)
                                .child("status")
                                .setValue("Online");
                    }
                }, 3000);

            }
        });
    }

    private void initButtons(){
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.attatchmentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int[] location = new int[2];
                view.getLocationOnScreen(location);
                popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, location[0] -600, location[1] - 600);
            }
        });

        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTextMessage();
            }
        });
    }

    private void initAttatchmentButtons(){
        View attatchments = popupWindow.getContentView();
        FloatingActionButton imageBtn = attatchments.findViewById(R.id.imageBtn);
        FloatingActionButton videoBtn = attatchments.findViewById(R.id.videoBtn);
        FloatingActionButton audioBtn = attatchments.findViewById(R.id.audioBtn);
        FloatingActionButton documentBtn = attatchments.findViewById(R.id.documentBtn);
        FloatingActionButton locationBtn = attatchments.findViewById(R.id.locationBtn);
        FloatingActionButton contactBtn = attatchments.findViewById(R.id.contactBtn);

        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(intent, IMAGE_INTENT);
                popupWindow.dismiss();
            }
        });

        videoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(intent, VIDEO_INTENT);
                popupWindow.dismiss();
            }
        });

        audioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatsActivity.this, "Images", Toast.LENGTH_SHORT).show();
            }
        });

        documentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatsActivity.this, "Images", Toast.LENGTH_SHORT).show();
            }
        });

        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatsActivity.this, "Images", Toast.LENGTH_SHORT).show();
            }
        });

        contactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatsActivity.this, "Images", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setMessagesAdapter(){
        messages = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(this, messages, receiverId);
        LinearLayoutManager linearLayoutManager =new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true); // used to show from last
        binding.recyclerView.setLayoutManager(linearLayoutManager);
        binding.recyclerView.setAdapter(messagesAdapter);

        database.getReference("chats")
                .child(senderRoom)
                .child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Messages message = dataSnapshot.getValue(Messages.class);
                    messages.add(message);
                }
                messagesAdapter.notifyDataSetChanged();
                binding.recyclerView.scrollToPosition(messages.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IMAGE_INTENT){
            if(data.getClipData() != null){ // When select Multiple Images
                int totalImages = data.getClipData().getItemCount();
                for(int i = 0; i<totalImages; i++){
                    Uri imageUrl = data.getClipData().getItemAt(i).getUri();
                    sendImageMessage(imageUrl);
                }
            }else if(data != null){
                if(data.getData() != null){
                    Uri imageUrl = data.getData();
                    sendImageMessage(imageUrl);
                }
            }
        }else if(requestCode == VIDEO_INTENT){
            if(data.getClipData() != null){ // When select Multiple Videos
                int totalVideos = data.getClipData().getItemCount();
                for(int i = 0; i<totalVideos; i++){
                    Uri videoUrl = data.getClipData().getItemAt(i).getUri();
                    sendVideoMessage(videoUrl);
                }
            }else if(data != null){
                if(data.getData() != null){
                    Uri videoUrl = data.getData();
                    sendVideoMessage(videoUrl);
                }
            }
        }
    }

    private void sendTextMessage(){
        String messageTxt = binding.message.getText().toString();
        long time = new Date().getTime();
        if(messageTxt.isEmpty()){
            binding.message.setError("Enter your message");
            return;
        }

        Messages message = new Messages(senderId, messageTxt, time);

        sendMessage(message);
    }

    private void sendImageMessage(Uri imageUrl){
        dialog.show();
        Messages message = new Messages(senderId, "photo", new Date().getTime());

        Calendar calendar = Calendar.getInstance();
        StorageReference reference = storage.getReference("chats").child(calendar.getTimeInMillis() + "");
        reference.putFile(imageUrl).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            message.setImageUrl(uri.toString());
                            sendMessage(message);
                            dialog.dismiss();
                        }
                    });
                }
            }
        });
    }

    private void sendVideoMessage(Uri imageUrl){
        dialog.show();
        Messages message = new Messages(senderId, "video", new Date().getTime());

        Calendar calendar = Calendar.getInstance();
        StorageReference reference = storage.getReference("chats").child(calendar.getTimeInMillis() + "");
        reference.putFile(imageUrl).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            message.setImageUrl(uri.toString());
                            sendMessage(message);
                            dialog.dismiss();
                        }
                    });
                }
            }
        });
    }

    private void sendMessage(Messages message) {
        // If receiver is not our friend, add it to our friend list
        checkIsFriend();

        /* Updating last message */
        Map<String, Object> map = new HashMap<>();
        map.put("lastMsg", message.getMessage());
        map.put("lastMsgTime", message.getTime());

        database.getReference("chats")
                .child(senderRoom)
                .updateChildren(map);

        database.getReference("chats")
                .child(receiverRoom)
                .updateChildren(map);


        String messageId = database.getReference().push().getKey();
        message.setMessageId(messageId);

        database.getReference("chats")
                .child(receiverRoom)
                .child("messages")
                .child(messageId)
                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
//                message.setImageUrl(localPath);
                database.getReference("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(messageId)
                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        binding.message.setText("");
                        sendNotification(auth.getCurrentUser().getDisplayName(), message.getMessage(), user.getToken());
                    }
                });
            }
        });
    }

    private void sendNotification(String title, String body, String token){
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JSONObject jsonObject = new JSONObject();
        try {
            JSONObject data = new JSONObject();
            data.put("title", title);
            data.put("body", body);

            jsonObject.put("notification", data);
            jsonObject.put("to", token);
        }catch (Exception ex){
            Toast.makeText(ChatsActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String key = "Key=AAAABm4zeWk:APA91bGUif1bBFhe91OezMatLKf0ffbKqEwsKgRlPIYfCLVd6ZCwQsG3dQBKdiS0h9pUe569-ZVqzouwwKxjr3whT-SFgoXDWCNndEruMzshFLbKKyBpWKwt-IAP_otrGQ8nr4Y_dWtg";

                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", key);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }

    private void checkIsFriend(){
        if(messages.size() <= 0){
            // inserting data in senderId
            Map<String, Object> map = new HashMap<>();
            map.put("name", user.getName());
            map.put("phoneNumber", user.getPhoneNumber());
            map.put("uId", receiverId);

            database.getReference("friends")
                    .child(senderId)
                    .child(receiverId)
                    .setValue(map);

            // inserting data in receiverId
            map.clear();
            map.put("name", auth.getCurrentUser().getPhoneNumber());
            map.put("phoneNumber", auth.getCurrentUser().getPhoneNumber());
            map.put("uId", senderId);

            database.getReference("friends")
                    .child(receiverId)
                    .child(senderId)
                    .setValue(map);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        database.getReference("presence")
                .child(senderId)
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
        getMenuInflater().inflate(R.menu.chats_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

}