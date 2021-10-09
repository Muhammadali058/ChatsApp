package com.example.whatsapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsapp.Activities.MainActivity;
import com.example.whatsapp.Models.Status;
import com.example.whatsapp.Models.Stories;
import com.example.whatsapp.R;
import com.example.whatsapp.databinding.StatusHolderBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class StatusesAdapter extends RecyclerView.Adapter<StatusesAdapter.ViewHolder>{

    Context context;
    List<Stories> stories;

    public StatusesAdapter(Context context, List<Stories> stories) {
        this.context = context;
        this.stories = stories;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.status_holder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Stories story = stories.get(position);

        holder.binding.name.setText(story.getName());
        Glide.with(context)
                .load(story.getProfileImage())
                .placeholder(R.drawable.avatar)
                .into(holder.binding.image);
        holder.binding.statusView.setPortionsCount(story.getStatuses().size());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<MyStory> myStories = new ArrayList<>();

                for(Status status : story.getStatuses()) {
                    myStories.add(new MyStory(status.getImageUrl(), new Date(status.getTime())));
                }

                new StoryView.Builder(((MainActivity)context).getSupportFragmentManager())
                        .setStoriesList(myStories) // Required
                        .setStoryDuration(3000) // Default is 2000 Millis (2 Seconds)
                        .setTitleText(story.getName()) // Default is Hidden
//                        .setSubtitleText("") // Default is Hidden
                        .setTitleLogoUrl(story.getProfileImage()) // Default is Hidden
                        .setStoryClickListeners(new StoryClickListeners() {
                            @Override
                            public void onDescriptionClickListener(int position) {
                                //your action
                            }

                            @Override
                            public void onTitleIconClickListener(int position) {
                                //your action
                            }
                        }) // Optional Listeners
                        .build() // Must be called before calling show method
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return stories.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        StatusHolderBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = StatusHolderBinding.bind(itemView);
        }
    }
}
