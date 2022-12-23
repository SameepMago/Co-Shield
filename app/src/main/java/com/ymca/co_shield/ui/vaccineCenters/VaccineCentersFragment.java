package com.ymca.co_shield.ui.vaccineCenters;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ymca.co_shield.LoginActivity;
import com.ymca.co_shield.R;
import com.ymca.co_shield.VaccineAlertService;
import com.ymca.co_shield.VaccineStatusAdapter;
import com.ymca.co_shield.VaccineStatusModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class VaccineCentersFragment extends Fragment {
    String date;
    String pinCode;
    private RecyclerView vaccineStatusRecyclerView;
    private VaccineStatusAdapter vaccineStatusAdapter;
    private ArrayList<VaccineStatusModel> vaccineStatusModelArrayList;
    View root;
    RequestQueue requestQueue;
    TextView district;
    TextView vaccineCentersHeading;
    TextView alertText;
    TextView alertMessage;
    Button subscribeButton;
    ImageView signOutButton4;
    GoogleSignInClient googleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    String uId;
    private FirebaseFirestore db;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root= inflater.inflate(R.layout.fragment_vaccine_centres, container, false);
        vaccineCentersHeading=root.findViewById(R.id.vaccine_centers_heading);
        district=root.findViewById(R.id.districtName);
        alertText=root.findViewById(R.id.alertText);
        alertMessage=root.findViewById(R.id.alertMessage);
        subscribeButton=root.findViewById(R.id.subscribeButton);
        signOutButton4 =root.findViewById(R.id.logout4);
        mAuth=FirebaseAuth.getInstance();
        db= FirebaseFirestore.getInstance();
        user=mAuth.getCurrentUser();
        uId=user.getUid();
        Log.d("uId",uId);
        GoogleSignInOptions googleSignInOptions=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.google_key)).requestEmail().build();
        googleSignInClient= GoogleSignIn.getClient(getActivity(),googleSignInOptions);

        signOutButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        signOutButton4.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getContext(),"Logout Button",Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        if(getArguments()!=null){
            VaccineCentersFragmentArgs args=VaccineCentersFragmentArgs.fromBundle(getArguments());
            pinCode=args.getPinCode();
            date=args.getDate();
        }
        vaccineStatusRecyclerView=root.findViewById(R.id.vaccineStatusRecyclerView);
        vaccineStatusRecyclerView.setHasFixedSize(true);
        vaccineStatusRecyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        vaccineStatusModelArrayList=new ArrayList<VaccineStatusModel>();
        vaccineStatusAdapter=new VaccineStatusAdapter(getContext(),vaccineStatusModelArrayList);
        vaccineStatusRecyclerView.setAdapter(vaccineStatusAdapter);

        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference docRef = db.collection("User").document(uId);
                Intent serviceIntent=new Intent(getActivity(), VaccineAlertService.class);
                serviceIntent.putExtra("pinCode",pinCode);
                serviceIntent.putExtra("date",date);
                if(subscribeButton.getText()=="SUBSCRIBE"){
                subscribeButton.setText("UNSUBSCRIBE");
                    docRef.update("pincode", pinCode).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("updated", "DocumentSnapshot successfully updated!");
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("error", "Error updating document", e);
                                }
                            });
                    serviceIntent.setAction(VaccineAlertService.ACTION_START_FOREGROUND_SERVICE);
                }
                else{
                    subscribeButton.setText("SUBSCRIBE");
                    docRef.update("pincode", "").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("updated", "DocumentSnapshot successfully updated!");
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("error", "Error updating document", e);
                                }
                            });
                    serviceIntent.setAction(VaccineAlertService.ACTION_STOP_FOREGROUND_SERVICE);
                }

                ContextCompat.startForegroundService(getContext(),serviceIntent);
            }
        });

        requestQueue= Volley.newRequestQueue(getContext());
        Log.i("pincode",pinCode+" " + date);
        showRecyclerView();
        Log.i("done","successful");
        return root;
    }

    public void showRecyclerView(){
        JsonObjectRequest sessionsList=new JsonObjectRequest(Request.Method.GET, getString(R.string.backend_url) + "/v2/appointment/sessions/public/calendarByPin?pincode=" + pinCode + "&date=" + date, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.i("working","Work");
                    JSONArray centerArray=response.getJSONArray("centers");
                    if(centerArray.length()==0){
                        Toast.makeText(getContext(),"No Vaccination Centers Available",Toast.LENGTH_LONG).show();
                        vaccineCentersHeading.setText("Vaccine Availability Alerts");
                        vaccineStatusRecyclerView.setVisibility(View.GONE);
                        district.setVisibility(View.GONE);
                        alertText.setVisibility(View.VISIBLE);
                        alertMessage.setVisibility(View.VISIBLE);
                        subscribeButton.setVisibility(View.VISIBLE);

                    }
                    else{
                        vaccineCentersHeading.setText("Vaccine Availability Status");
                        vaccineStatusRecyclerView.setVisibility(View.VISIBLE);
                        district.setVisibility(View.VISIBLE);
                        alertMessage.setVisibility(View.GONE);
                        alertText.setVisibility(View.GONE);
                        subscribeButton.setVisibility(View.GONE);
                        for(int i =0;i<centerArray.length();i++){
                            JSONObject centerObj=centerArray.getJSONObject(i);
                            String centerName=centerObj.getString("name");
                            Log.i("name",centerName);
                            String centerLocation=centerObj.getString("address");
                            String centerFromTime=centerObj.getString("from");
                            String centerToTime=centerObj.getString("to");
                            String feeType=centerObj.getString("fee_type");
                            JSONObject sessionObj=centerObj.getJSONArray("sessions").getJSONObject(0);
                            Integer availableCapacity=sessionObj.getInt("available_capacity");
                            Integer ageLimit=sessionObj.getInt("min_age_limit");
                            String vaccineName=sessionObj.getString("vaccine");
                            VaccineStatusModel vaccineStatusItem=new VaccineStatusModel();
                            vaccineStatusItem.setCenterName(centerName);
                            vaccineStatusItem.setCenterAddress(centerLocation);
                            vaccineStatusItem.setCenterFromTime(centerFromTime);
                            vaccineStatusItem.setCenterToTime(centerToTime);
                            vaccineStatusItem.setAgeLimit(ageLimit);
                            vaccineStatusItem.setAvailableCapacity(availableCapacity);
                            vaccineStatusItem.setFeeType(feeType);
                            vaccineStatusItem.setVaccineName(vaccineName);
                            vaccineStatusModelArrayList.add(vaccineStatusItem);
                            district.setText(centerObj.getString("block_name")+", "+centerObj.getString("district_name")+", "+centerObj.getString("state_name"));
                        }
                    }
                    vaccineStatusAdapter.notifyDataSetChanged();
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("error",error.toString());
            }
        });

        requestQueue.add(sessionsList);
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