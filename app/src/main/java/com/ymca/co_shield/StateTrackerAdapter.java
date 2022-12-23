package com.ymca.co_shield;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StateTrackerAdapter extends RecyclerView.Adapter<StateTrackerAdapter.ViewHolder> {

    private Context context;
    private List<StateTrackerModel> stateTrackerList;

    public StateTrackerAdapter(Context context, List<StateTrackerModel> stateTrackerList) {
        this.context = context;
        this.stateTrackerList = stateTrackerList;
    }

    @NonNull
    @Override
    public StateTrackerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.covid_state_tracker_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StateTrackerAdapter.ViewHolder holder, int position) {
        StateTrackerModel stateItem= stateTrackerList.get(position);
        holder.stateName.setText(stateItem.getState());
        holder.numberStateRecovered.setText(stateItem.getRecovered().toString());
        holder.numberStateCase.setText(stateItem.getCases().toString());
        holder.numberStateDeath.setText(stateItem.getDeaths().toString());
        holder.numberStateActive.setText("Active Cases - "+stateItem.getActive().toString());
        holder.numberStateNewActive.setText(stateItem.getNewActive().toString());
        holder.numberStateNewRecovered.setText(stateItem.getNewRecovered().toString());
        holder.numberStateNewDeath.setText(stateItem.getNewDeath().toString());
    }

    @Override
    public int getItemCount() {
        return stateTrackerList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView stateName;
        TextView numberStateCase;
        TextView numberStateRecovered;
        TextView numberStateDeath;
        TextView numberStateActive;
        TextView numberStateNewActive;
        TextView numberStateNewRecovered;
        TextView numberStateNewDeath;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            stateName=itemView.findViewById(R.id.stateName);
            numberStateCase=itemView.findViewById(R.id.numberStateCase);
            numberStateDeath=itemView.findViewById(R.id.numberStateDeath);
            numberStateRecovered=itemView.findViewById(R.id.numberStateRecovered);
            numberStateActive=itemView.findViewById(R.id.numberStateActive);
            numberStateNewActive=itemView.findViewById(R.id.numberStateNewActive);
            numberStateNewRecovered=itemView.findViewById(R.id.numberStateNewRecovered);
            numberStateNewDeath=itemView.findViewById(R.id.numberStateNewDeath);
        }
    }
}
