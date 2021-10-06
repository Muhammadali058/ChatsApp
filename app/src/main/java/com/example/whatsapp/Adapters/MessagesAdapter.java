package com.example.whatsapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsapp.HP;
import com.example.whatsapp.Models.Messages;
import com.example.whatsapp.R;
import com.example.whatsapp.databinding.ChatItemReceiveBinding;
import com.example.whatsapp.databinding.ChatItemSendBinding;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter{

    Context context;
    List<Messages> messages;

    private final int ITEM_SEND = 1;
    private final int ITEM_RECEIVE = 2;
    FirebaseDatabase database;
    FirebaseStorage storage;
    String senderId, receiverId;
    String senderRoom, receiverRoom;
    private int[] reactions = new int[]{
            R.drawable.ic_fb_like,
            R.drawable.ic_fb_love,
            R.drawable.ic_fb_laugh,
            R.drawable.ic_fb_wow,
            R.drawable.ic_fb_sad,
            R.drawable.ic_fb_angry
    };

    public MessagesAdapter(Context context, List<Messages> messages, String receiverId) {
        this.context = context;
        this.messages = messages;
        this.receiverId = receiverId;
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        senderId = FirebaseAuth.getInstance().getUid();

        senderRoom = senderId + receiverId;
        receiverRoom = receiverId + senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ITEM_SEND){
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_send, parent, false);
            return new ViewHolderSend(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_receive, parent, false);
            return new ViewHolderReceive(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Messages message = messages.get(position);

        if(holder instanceof ViewHolderSend){
            bindSenderMessages(holder, message);
        }
        else {
            bindReceiverMessages(holder, message);
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Delete Message")
                        .setMessage("Are you sure to delete this message?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                database.getReference("chats")
                                        .child(senderRoom)
                                        .child("messages")
                                        .child(message.getMessageId())
                                        .removeValue();

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

    private void bindSenderMessages(RecyclerView.ViewHolder holder, Messages message){
        ViewHolderSend viewHolder = (ViewHolderSend)holder;
        // When Photo
        if(message.getMessage().equals("photo")){
            viewHolder.binding.textLayout.setVisibility(View.GONE);
            viewHolder.binding.imageLayout.setVisibility(View.VISIBLE);
            viewHolder.binding.videoLayout.setVisibility(View.GONE);

            viewHolder.binding.imageTime.setText(HP.getFormatedTime(message.getTime()));
            viewHolder.binding.image.setImageURI(Uri.fromFile(new File(message.getImageUrl())));
        }

        // When Video
        else if(message.getMessage().equals("video")){
            viewHolder.binding.textLayout.setVisibility(View.GONE);
            viewHolder.binding.imageLayout.setVisibility(View.GONE);
            viewHolder.binding.videoLayout.setVisibility(View.VISIBLE);

            viewHolder.binding.videoTime.setText(HP.getFormatedTime(message.getTime()));
            viewHolder.binding.downloadLayout.setVisibility(View.GONE);

            viewHolder.binding.video.setVideoPath(message.getImageUrl());
            MediaController mediaController = new MediaController(context);
            mediaController.setAnchorView(viewHolder.binding.video);
            viewHolder.binding.video.setMediaController(mediaController);
            viewHolder.binding.video.seekTo(1);
        }

        // When Text
        else {
            viewHolder.binding.textLayout.setVisibility(View.VISIBLE);
            viewHolder.binding.imageLayout.setVisibility(View.GONE);
            viewHolder.binding.videoLayout.setVisibility(View.GONE);

            String[] dividedMessage = HP.getDividedMessage(message.getMessage());
            if(dividedMessage[0] != null){
                viewHolder.binding.topMessage.setVisibility(View.VISIBLE);
                viewHolder.binding.belowMessage.setVisibility(View.VISIBLE);

                viewHolder.binding.topMessage.setText(dividedMessage[0]);
                viewHolder.binding.belowMessage.setText(dividedMessage[1]);
            }else {
                viewHolder.binding.topMessage.setVisibility(View.GONE);
                viewHolder.binding.belowMessage.setVisibility(View.VISIBLE);

                viewHolder.binding.topMessage.setVisibility(View.GONE);
                viewHolder.binding.belowMessage.setText(dividedMessage[1]);
            }

            viewHolder.binding.textTime.setText(HP.getFormatedTime(message.getTime()));
        }

        if(message.getReaction() >= 0) {
            viewHolder.binding.reaction.setImageResource(reactions[message.getReaction()]);
            viewHolder.binding.reaction.setVisibility(View.VISIBLE);
        } else {
            viewHolder.binding.reaction.setVisibility(View.GONE);
        }
    }
    private void bindReceiverMessages(RecyclerView.ViewHolder holder, Messages message){
        ViewHolderReceive viewHolder = (ViewHolderReceive)holder;
        // When Photo
        if(message.getMessage().equals("photo")){
            viewHolder.binding.textLayout.setVisibility(View.GONE);
            viewHolder.binding.imageLayout.setVisibility(View.VISIBLE);
            viewHolder.binding.videoLayout.setVisibility(View.GONE);

            viewHolder.binding.imageTime.setText(HP.getFormatedTime(message.getTime()));

            Glide.with(context).load(message.getImageUrl())
                    .placeholder(R.drawable.avatar)
                    .into(viewHolder.binding.image);

//            viewHolder.binding.image.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    StorageReference myStorage = storage.getReferenceFromUrl(message.getImageUrl());
//                    final File rootPath = new File(context.getExternalFilesDir(""), "Whatsapp Images");
//                    if (!rootPath.exists()) {
//                        rootPath.mkdirs();
//                    }
//
//                    final File localFile = new File(rootPath, Calendar.getInstance().getTimeInMillis() + ".jpg");
//                    myStorage.getFile(localFile);
//                    return true;
//                }
//            });
        }

        // When Video
        else if(message.getMessage().equals("video")){
            viewHolder.binding.textLayout.setVisibility(View.GONE);
            viewHolder.binding.imageLayout.setVisibility(View.GONE);
            viewHolder.binding.videoLayout.setVisibility(View.VISIBLE);

            viewHolder.binding.videoTime.setText(HP.getFormatedTime(message.getTime()));

            final String[] fileName = new String[1];
            StorageReference myStorage = storage.getReferenceFromUrl(message.getImageUrl());
            myStorage.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                // When Successfully get metadata
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    fileName[0] = storageMetadata.getName();
                    double sizeD = storageMetadata.getSizeBytes();
                    double size = sizeD  / (1024 * 1024);
                    viewHolder.binding.videoSize.setText(String.format("%.2f", size) + " MB");

                    File rootPath = new File(context.getExternalFilesDir(""), "Whatsapp Videos");
                    final File videoFile = new File(rootPath, fileName[0] + ".mp4");
                    if(videoFile.exists()){
                        viewHolder.binding.downloadLayout.setVisibility(View.GONE);

                        viewHolder.binding.video.setVideoPath(videoFile.getPath());
                        MediaController mediaController = new MediaController(context);
                        mediaController.setAnchorView(viewHolder.binding.video);
                        viewHolder.binding.video.setMediaController(mediaController);
                        viewHolder.binding.video.seekTo(1);
                    }
                    else {
                        viewHolder.binding.downloadLayout.setVisibility(View.VISIBLE);

                        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                        mediaMetadataRetriever.setDataSource(message.getImageUrl());
                        Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime(1);
                        viewHolder.binding.video.setBackgroundDrawable(new BitmapDrawable(bitmap));

                        viewHolder.binding.downloadBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                viewHolder.binding.downloadBtn.setVisibility(View.GONE);
                                viewHolder.binding.videoProgress.setVisibility(View.VISIBLE);

                                if (!rootPath.exists()) {
                                    rootPath.mkdirs();
                                }
                                myStorage.getFile(videoFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        viewHolder.binding.downloadLayout.setVisibility(View.GONE);
                                        viewHolder.binding.video.setBackgroundDrawable(null);

                                        viewHolder.binding.video.setVideoPath(videoFile.getPath());
                                        MediaController mediaController = new MediaController(context);
                                        mediaController.setAnchorView(viewHolder.binding.video);
                                        viewHolder.binding.video.setMediaController(mediaController);
                                        viewHolder.binding.video.seekTo(1);

                                    }
                                });
                            }
                        });
                    }  // End of if video file exists

                }
            }); // End of onSuccess listener

        }

        // When Text
        else {
            viewHolder.binding.textLayout.setVisibility(View.VISIBLE);
            viewHolder.binding.imageLayout.setVisibility(View.GONE);
            viewHolder.binding.videoLayout.setVisibility(View.GONE);

            String[] dividedMessage = HP.getDividedMessage(message.getMessage());
            if(dividedMessage[0] != null){
                viewHolder.binding.topMessage.setVisibility(View.VISIBLE);
                viewHolder.binding.belowMessage.setVisibility(View.VISIBLE);

                viewHolder.binding.topMessage.setText(dividedMessage[0]);
                viewHolder.binding.belowMessage.setText(dividedMessage[1]);
            }else {
                viewHolder.binding.topMessage.setVisibility(View.GONE);
                viewHolder.binding.belowMessage.setVisibility(View.VISIBLE);

                viewHolder.binding.topMessage.setVisibility(View.GONE);
                viewHolder.binding.belowMessage.setText(dividedMessage[1]);
            }

            viewHolder.binding.textTime.setText(HP.getFormatedTime(message.getTime()));

            ReactionsConfig config = new ReactionsConfigBuilder(context)
                    .withReactions(reactions).build();

            ReactionPopup popup = new ReactionPopup(context, config, (position) -> {
                if(position >= 0){
                    message.setReaction(position);

                    database.getReference("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(message.getMessageId())
                            .setValue(message);

                    database.getReference("chats")
                            .child(receiverRoom)
                            .child("messages")
                            .child(message.getMessageId())
                            .setValue(message);

                }
                return true; // true is closing popup, false is requesting a new selection
            });

            viewHolder.binding.textLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });
        } // End of if condition

        if(message.getReaction() >= 0) {
            viewHolder.binding.reaction.setImageResource(reactions[message.getReaction()]);
            viewHolder.binding.reaction.setVisibility(View.VISIBLE);
        } else {
            viewHolder.binding.reaction.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(messages.get(position).getSenderId().equals(FirebaseAuth.getInstance().getUid())){
            return ITEM_SEND;
        }else {
            return ITEM_RECEIVE;
        }
    }

    public class ViewHolderSend extends RecyclerView.ViewHolder{
        ChatItemSendBinding binding;
        public ViewHolderSend(@NonNull View itemView) {
            super(itemView);
            binding = ChatItemSendBinding.bind(itemView);
        }
    }

    public class ViewHolderReceive extends RecyclerView.ViewHolder{
        ChatItemReceiveBinding binding;
        public ViewHolderReceive(@NonNull View itemView) {
            super(itemView);
            binding = ChatItemReceiveBinding.bind(itemView);
        }
    }
}
