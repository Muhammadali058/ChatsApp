package com.example.whatsapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsapp.Activities.ChatsActivity;
import com.example.whatsapp.HP;
import com.example.whatsapp.Models.Users;
import com.example.whatsapp.R;
import com.example.whatsapp.databinding.FriendsHolderBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder>{

    Context context;
    List<Users> users;
    FirebaseDatabase database;
    String senderId;
    public FriendsAdapter(Context context, List<Users> users) {
        this.context = context;
        this.users = users;
        database = FirebaseDatabase.getInstance();

        senderId = FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friends_holder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users user = users.get(position);

        holder.binding.name.setText(user.getName());

        // Getting user image
        database.getReference("users")
                .child(user.getuId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                        String token = snapshot.child("token").getValue(String.class);
                        user.setImageUrl(imageUrl);
                        user.setToken(token);

                        Glide.with(context).load(user.getImageUrl())
                                .placeholder(R.drawable.avatar)
                                .into(holder.binding.image);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        // Setting lastMsg and lastMsgTime
        String senderRoom = senderId + user.getuId();
        database.getReference("chats")
                .child(senderRoom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    String lastMsg = snapshot.child("lastMsg").getValue(String.class);
                    long lastMsgTime = snapshot.child("lastMsgTime").getValue(Long.class);

                    String time = HP.getFormatedTime(lastMsgTime);

                    holder.binding.lastMsg.setText(lastMsg);
                    holder.binding.lastMsgTime.setText(time);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // OnClick to open chat activity
        final int finalPosition = position;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Users user = users.get(finalPosition);
                Intent intent = new Intent(context, ChatsActivity.class);
                intent.putExtra("user", user);
                context.startActivity(intent);
            }
        });


        // OnLongClick to delete friend & messages
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Delete")
                        .setMessage("Are you sure to delete all messages?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                database.getReference("friends")
                                        .child(senderId)
                                        .child(user.getuId())
                                        .removeValue();

                                database.getReference("chats")
                                        .child(senderRoom)
                                        .removeValue();

                                users.remove(user);
                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        FriendsHolderBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = FriendsHolderBinding.bind(itemView);
        }
    }
}
