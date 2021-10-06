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

import com.example.whatsapp.Activities.CountriesActivity;
import com.example.whatsapp.Models.Countries;
import com.example.whatsapp.R;
import com.example.whatsapp.databinding.CountryHolderBinding;

import java.util.ArrayList;
import java.util.List;

public class CountriesAdapter extends RecyclerView.Adapter<CountriesAdapter.ViewHolder> implements Filterable {
    Context context;
    List<Countries> countries;
    List<Countries> countriesFullList;

    public CountriesAdapter(Context context, List<Countries> countries) {
        this.context = context;
        this.countries = countries;
        this.countriesFullList = new ArrayList<>(countries);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.country_holder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final  int i = position;
        Countries country = countries.get(position);

        holder.binding.image.setImageResource(country.getImage());
        holder.binding.name.setText(country.getName());
        holder.binding.code.setText("+" + country.getCode());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("country", countries.get(i));

                ((CountriesActivity)context).setResult(123, intent);
                ((CountriesActivity)context).finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return countries.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        CountryHolderBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = CountryHolderBinding.bind(itemView);
        }
    }

    @Override
    public Filter getFilter() {
        return filteredList;
    }

    Filter filteredList = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Countries> filteredList = new ArrayList<>();

            if(constraint == null && constraint.length() == 0)
                filteredList.addAll(countriesFullList);
            else {
                String filteredPattern = constraint.toString().toLowerCase().trim();
                for(Countries country : countriesFullList){
                    if(country.getName().toLowerCase().contains(filteredPattern)){
                        filteredList.add(country);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            countries.clear();
            countries.addAll((List)results.values);
            notifyDataSetChanged();
        }
    };

}
