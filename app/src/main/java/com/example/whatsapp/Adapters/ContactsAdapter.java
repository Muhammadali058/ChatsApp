package com.example.whatsapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsapp.Activities.ContactsActivity;
import com.example.whatsapp.Activities.CountriesActivity;
import com.example.whatsapp.Models.Users;
import com.example.whatsapp.Models.Countries;
import com.example.whatsapp.Models.Users;
import com.example.whatsapp.R;
import com.example.whatsapp.databinding.ContactHolderBinding;
import com.example.whatsapp.databinding.CountryHolderBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> implements Filterable{
    Context context;
    List<Users> users;
    List<Users> usersFullList;
    FirebaseDatabase database;
    public ContactsAdapter(Context context, List<Users> users, List<Users> usersFullList) {
        this.context = context;
        this.users = users;
        this.usersFullList = usersFullList;
        database = FirebaseDatabase.getInstance();
    }

    @NonNull
    @Override
    public ContactsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_holder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsAdapter.ViewHolder holder, int position) {
        Users user = users.get(position);
        holder.binding.name.setText(user.getName());
        Glide.with(context)
                .load(user.getImageUrl())
                .placeholder(R.drawable.avatar)
                .into(holder.binding.image);

        database.getReference("users")
                .child(user.getuId())
                .child("status").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String status = snapshot.getValue(String.class);
                    holder.binding.status.setText(status);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContactsActivity contactsActivity =(ContactsActivity) context;
                Intent intent = new Intent();
                intent.putExtra("user", user);
                contactsActivity.setResult(123, intent);
                contactsActivity.finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ContactHolderBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ContactHolderBinding.bind(itemView);
        }
    }



    @Override
    public Filter getFilter() {
        return filteredList;
    }

    Filter filteredList = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Users> filteredList = new ArrayList<>();

            if(constraint == null && constraint.length() == 0)
                filteredList.addAll(usersFullList);
            else {
                String filteredPattern = constraint.toString().toLowerCase().trim();
                for(Users user : usersFullList){
                    if(user.getName().toLowerCase().contains(filteredPattern)){
                        filteredList.add(user);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            users.clear();
            users.addAll((List)results.values);
            notifyDataSetChanged();
        }
    };
}
