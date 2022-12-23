package com.ymca.co_shield.ui.vaccineFinder;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.ymca.co_shield.LoginActivity;
import com.ymca.co_shield.R;

import java.util.Calendar;

public class VaccineFinderFragment extends Fragment {

    EditText findByPinCode;
    Button selectDate;
    Button searchButton;
    View root;
    int year,month,date;
    String vaccineDate;
    GoogleSignInClient googleSignInClient;
    ImageView signOutButton3;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.vaccine_finder, container, false);
        signOutButton3 =root.findViewById(R.id.logout3);
        GoogleSignInOptions googleSignInOptions=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.google_key)).requestEmail().build();
        googleSignInClient= GoogleSignIn.getClient(getActivity(),googleSignInOptions);

        signOutButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        signOutButton3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getContext(),"Logout Button",Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        searchButton=root.findViewById(R.id.findVaccineButton);
        findByPinCode=root.findViewById(R.id.findByPincode);
        selectDate=root.findViewById(R.id.selectDate);
        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c=Calendar.getInstance();
                year=c.get(Calendar.YEAR);
                month=c.get(Calendar.MONTH);
                date=c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dpd=new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        vaccineDate= Integer.toString(dayOfMonth)+"-"+Integer.toString(month+1)+"-"+Integer.toString(year);
                        selectDate.setText(vaccineDate);

                    }
                },year,month,date);
                dpd.show();

            }
        });
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(findByPinCode.getText().toString().length()!=6){
                    findByPinCode.setError("Please Enter a valid PinCode");
                }
                else{
                    VaccineFinderFragmentDirections.ActionNavigationVaccineFinderToNavigationVaccineCenters action=VaccineFinderFragmentDirections.actionNavigationVaccineFinderToNavigationVaccineCenters();
                    action.setPinCode(findByPinCode.getText().toString());
                    action.setDate(vaccineDate);
                    NavHostFragment.findNavController(getParentFragment()).navigate(action);
                }
                }

        });
        return root;
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