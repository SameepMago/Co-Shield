package com.ymca.co_shield;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class SignUpFragment extends Fragment {
    private FirebaseAuth mAuth;
    private Button signUpButton;
    private EditText signUpEmail;
    private EditText signUpPassword;
    private EditText signUpName;
    private EditText signUpConfirmPassword;
    private FirebaseFirestore db;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup root=(ViewGroup) inflater.inflate(R.layout.fragment_sign_up,container,false);
        mAuth = FirebaseAuth.getInstance();
        signUpButton=(Button) root.findViewById(R.id.signup_button);
        signUpEmail =(EditText) root.findViewById(R.id.signup_email_text);
        signUpPassword=(EditText) root.findViewById(R.id.signup_password_text);
        signUpName =(EditText) root.findViewById(R.id.signup_name_text);
        signUpConfirmPassword=(EditText) root.findViewById(R.id.signup_confirm_password_text);
        db = FirebaseFirestore.getInstance();
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(signUpEmail.getText().toString().trim().length()==0){
                    signUpEmail.setError("Please Enter a Email Id");
                }
                else if(!(signUpEmail.getText().toString().trim().matches("^[A-Za-z0-9+_.-]\\S+@gmail\\.com$"))){
                    signUpEmail.setError("Please Enter a Valid Gmail Id");
                }
                else if(signUpName.getText().toString().trim().length()==0){
                    signUpName.setError("Please Enter your Name");
                }
                else if(signUpPassword.getText().toString().trim().length()==0){
                    signUpPassword.setError("Please Enter a Password");
                }
                else if(signUpConfirmPassword.getText().toString().trim().length()==0){
                    signUpConfirmPassword.setError("Please confirm your Password");
                }
                else if(!(signUpPassword.getText().toString().trim().equals(signUpConfirmPassword.getText().toString().trim()))){
                    signUpConfirmPassword.setError("Please enter the correct Password");
                }
                else {
                    createAccount(signUpEmail.getText().toString().trim(), signUpPassword.getText().toString().trim(), signUpName.getText().toString().trim());
                }
            }
        });
        return root;
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            reload();
        }
    }
    private void reload() { }
    private void createAccount(String email, String password,String name) {
        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("SignUp", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            String uId=user.getUid();
                            addDataToFirestore(email,name,uId);
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("SignUp", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getActivity(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
        // [END create_user_with_email]
    }
    private void updateUI(FirebaseUser user) {
        if(user!=null) {
            Toast.makeText(getActivity(), "Sign Up Successful", Toast.LENGTH_LONG).show();
            ((LoginActivity) getActivity()).setCurrentTab(0);
            signUpEmail.setText("");
            signUpName.setText("");
            signUpPassword.setText("");
            signUpConfirmPassword.setText("");
        }
    }

    private void addDataToFirestore(String email,String name,String uId){
        CollectionReference dbProfile = db.collection("User");
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("name", name);
        user.put("pincode","");
        dbProfile.document(uId)
                .set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void avoid) {
                Log.d("Document", "DocumentSnapshot successfully written!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("error", "Error adding document", e);
            }
        });

    }
}