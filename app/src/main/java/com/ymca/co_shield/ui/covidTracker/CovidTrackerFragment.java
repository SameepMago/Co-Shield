package com.ymca.co_shield.ui.covidTracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.ymca.co_shield.LoginActivity;
import com.ymca.co_shield.R;
import com.ymca.co_shield.StateTrackerAdapter;
import com.ymca.co_shield.StateTrackerModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CovidTrackerFragment extends Fragment {

    TextView numberWorldCase;
    TextView numberWorldRecovered;
    TextView numberWorldDeath;
    TextView numberIndiaCase;
    TextView numberIndiaRecovered;
    TextView numberIndiaDeath;
    TextView numberIndiaActive;
    TextView numberIndiaNewActive;
    TextView numberIndiaNewRecovered;
    TextView numberIndiaNewDeath;

    View root;
    private RecyclerView stateTrackerRecyclerView;
    private StateTrackerAdapter stateTrackerAdapter;
    private ArrayList<StateTrackerModel> stateTrackerModelArrayList;
    ImageView signOutButton1;
    GoogleSignInClient googleSignInClient;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_covid_tracker, container, false);
        numberWorldCase=root.findViewById(R.id.numberWorldCase);
        numberWorldRecovered=root.findViewById(R.id.numberWorldRecovered);
        numberWorldDeath=root.findViewById(R.id.numberWorldDeath);
        numberIndiaCase=root.findViewById(R.id.numberIndiaCase);
        numberIndiaRecovered=root.findViewById(R.id.numberIndiaRecovered);
        numberIndiaDeath=root.findViewById(R.id.numberIndiaDeath);
        numberIndiaActive=root.findViewById(R.id.numberindiaActive);
        numberIndiaNewActive=root.findViewById(R.id.numberIndiaNewActive);
        numberIndiaNewRecovered=root.findViewById(R.id.numberIndiaNewRecovered);
        numberIndiaNewDeath=root.findViewById(R.id.numberIndiaNewDeath);
        signOutButton1 =root.findViewById(R.id.logout1);
        GoogleSignInOptions googleSignInOptions=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.google_key)).requestEmail().build();
        googleSignInClient= GoogleSignIn.getClient(getActivity(),googleSignInOptions);

        signOutButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        signOutButton1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getContext(),"Logout Button",Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        stateTrackerRecyclerView=root.findViewById(R.id.covidStatesRecyclerView);
        stateTrackerRecyclerView.setHasFixedSize(true);
        stateTrackerRecyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        stateTrackerModelArrayList=new ArrayList<StateTrackerModel>();
        stateTrackerAdapter=new StateTrackerAdapter(getContext(),stateTrackerModelArrayList);
        stateTrackerRecyclerView.setAdapter(stateTrackerAdapter);

        getWorldData();
        getStateData();

        return root;
    }

    private void getStateData(){
        String stateUrl= "https://api.apify.com/v2/key-value-stores/toDWvRj1JpTXiM8FF/records/LATEST?disableRedirect=true";
        RequestQueue stateRequestQueue=Volley.newRequestQueue(getContext());
            JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, stateUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Integer indiaCases=response.getInt("totalCases");
                    Integer indiaRecovered=response.getInt("recovered");
                    Integer indiaDeath=response.getInt("deaths");
                    Integer indiaActive=response.getInt("activeCases");
                    Integer indiaNewActive=response.getInt("activeCasesNew");
                    Integer indiaNewRecovered=response.getInt("recoveredNew");
                    Integer indiaNewDeaths=response.getInt("deathsNew");
                    numberIndiaCase.setText(indiaCases.toString());
                    numberIndiaRecovered.setText(indiaRecovered.toString());
                    numberIndiaDeath.setText(indiaDeath.toString());
                    numberIndiaActive.setText("Active Cases - "+ indiaActive.toString());
                    numberIndiaNewActive.setText(indiaNewActive.toString());
                    numberIndiaNewRecovered.setText(indiaNewRecovered.toString());
                    numberIndiaNewDeath.setText(indiaNewDeaths.toString());
                    JSONArray regionArray=response.getJSONArray("regionData");
                    for(int i=0;i<regionArray.length();i++){
                            JSONObject regionObject=regionArray.getJSONObject(i);
                            String stateName=regionObject.getString("region");
                            Integer stateCase=regionObject.getInt("totalInfected");
                            Integer stateRecovered=regionObject.getInt("recovered");
                            Integer stateDeath=regionObject.getInt("deceased");
                            Integer stateActive=regionObject.getInt("activeCases");
                            Integer stateNewActive=regionObject.getInt("newInfected");
                            Integer stateNewRecovered=regionObject.getInt("newRecovered");
                            Integer stateNewDeath=regionObject.getInt("newDeceased");


                            StateTrackerModel stateTracker=new StateTrackerModel();
                            stateTracker.setCases(stateCase);
                            stateTracker.setState(stateName);
                            stateTracker.setDeaths(stateDeath);
                            stateTracker.setRecovered(stateRecovered);
                            stateTracker.setActive(stateActive);
                            stateTracker.setNewActive(stateNewActive);
                            stateTracker.setNewRecovered(stateNewRecovered);
                            stateTracker.setNewDeath(stateNewDeath);
                            stateTrackerModelArrayList.add(stateTracker);
                    }

                    stateTrackerAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("error",error.toString());
            }
        });

        stateRequestQueue.add(jsonObjectRequest);

    }

    private void getWorldData(){

        String url="https://corona.lmao.ninja/v3/covid-19/all";
        RequestQueue requestQueue= Volley.newRequestQueue(getContext());
        JsonObjectRequest worldData=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Integer worldCases=response.getInt("cases");
                    Integer worldRecovered=response.getInt("recovered");
                    Integer worldDeaths=response.getInt("deaths");
                    numberWorldCase.setText(worldCases.toString());
                    numberWorldRecovered.setText(worldRecovered.toString());
                    numberWorldDeath.setText(worldDeaths.toString());


                } catch (JSONException e) {
                    e.printStackTrace();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("error",error.toString());
            }
        });

        requestQueue.add(worldData);

    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();

        googleSignInClient.signOut()
                .addOnCompleteListener(getActivity() , new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent signOutIntent=new Intent(getActivity(), LoginActivity.class);
                        signOutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        signOutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        signOutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(signOutIntent);
                        getActivity().finish();
                    }
                });
    }
}