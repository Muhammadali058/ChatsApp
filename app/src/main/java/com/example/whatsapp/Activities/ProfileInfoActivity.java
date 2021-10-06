package com.example.whatsapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.whatsapp.Models.Users;
import com.example.whatsapp.databinding.ActivityProfileInfoBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ProfileInfoActivity extends AppCompatActivity {

    ActivityProfileInfoBinding binding;
    FirebaseDatabase database;
    FirebaseStorage storage;
    FirebaseAuth auth;
    Uri selectedImage;
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Creating user...");
        dialog.setCancelable(false);

        binding.name.requestFocus();
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);

        String phoneNumber = getIntent().getStringExtra("phoneNumber");
        binding.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 123);
            }
        });

        binding.nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.name.getText().toString().isEmpty()){
                    binding.name.setError("Please enter name");
                    return;
                }

                if(selectedImage != null){
                    dialog.show();
                    StorageReference reference=storage.getReference("users").child(auth.getUid());
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String imageUrl = uri.toString();
                                        createUser(imageUrl);
                                    }
                                });
                            }
                        }
                    });

                }else {
                    createUser("no image");
                }
            }
        });
    }

    private void createUser(String imageUrl){
        Users user = new Users(
                auth.getUid(),
                binding.name.getText().toString(),
                auth.getCurrentUser().getPhoneNumber(),
                imageUrl
        );
        user.setStatus("Hey there! I am using WhatsApp");

        database.getReference("users")
                .child(auth.getUid())
                .setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        dialog.dismiss();
                        Intent intent = new Intent(ProfileInfoActivity.this, MainActivity.class);
                        startActivity(intent);
                        finishAffinity();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null){
            if(data.getData() != null){
                selectedImage = data.getData();
                binding.image.setImageURI(selectedImage);
            }
        }
    }
}