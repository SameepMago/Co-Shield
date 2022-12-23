package com.ymca.co_shield;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VaccineStatusAdapter extends RecyclerView.Adapter<VaccineStatusAdapter.ViewHolder> {

    private Context context;
    private List<VaccineStatusModel> vaccineStatusList;

    public VaccineStatusAdapter(Context context, List<VaccineStatusModel> vaccineStatusList) {
        this.context = context;
        this.vaccineStatusList = vaccineStatusList;
    }

    @NonNull
    @Override
    public VaccineStatusAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.vaccine_finder_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VaccineStatusAdapter.ViewHolder holder, int position) {
        VaccineStatusModel vaccineItem=vaccineStatusList.get(position);
        holder.vaccineCenterName.setText(vaccineItem.getCenterName());
        holder.vaccineCenterLocation.setText(vaccineItem.getCenterAddress());
        holder.vaccineAvailability.setText("Availability: "+vaccineItem.getAvailableCapacity().toString());
        holder.vaccineAgeLimit.setText("Age Limit: " +vaccineItem.getAgeLimit().toString());
        holder.vaccineFees.setText(vaccineItem.getFeeType());
        holder.vaccineName.setText(vaccineItem.getVaccineName());
        holder.vaccineTimings.setText("From: "+vaccineItem.getCenterFromTime()+" To: "+vaccineItem.getCenterToTime());

    }

    @Override
    public int getItemCount() {
        return vaccineStatusList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView vaccineCenterName;
        TextView vaccineCenterLocation;
        TextView vaccineTimings;
        TextView vaccineName;
        TextView vaccineFees;
        TextView vaccineAgeLimit;
        TextView vaccineAvailability;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            vaccineCenterName=itemView.findViewById(R.id.vaccineCenterName);
            vaccineCenterLocation=itemView.findViewById(R.id.vaccineCenterLocation);
            vaccineTimings=itemView.findViewById(R.id.vaccineCenterTimings);
            vaccineName=itemView.findViewById(R.id.vaccineName);
            vaccineFees=itemView.findViewById(R.id.vaccineFees);
            vaccineAgeLimit=itemView.findViewById(R.id.vaccineAgeLimit);
            vaccineAvailability=itemView.findViewById(R.id.vaccineAvailability);
        }
    }
}
