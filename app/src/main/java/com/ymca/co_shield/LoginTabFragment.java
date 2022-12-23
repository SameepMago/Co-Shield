package com.ymca.co_shield;

import android.content.Intent;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginTabFragment extends Fragment {

    private FirebaseAuth mAuth;
    private Button loginButton;
    private EditText loginEmail;
    private EditText loginPassword;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_login_tab, container, false);
        mAuth = FirebaseAuth.getInstance();
        loginButton = (Button) root.findViewById(R.id.login_button);
        loginEmail = (EditText) root.findViewById(R.id.login_email_text);
        loginPassword = (EditText) root.findViewById(R.id.login_password_text);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(loginEmail.getText().toString().trim().length()==0){
                    loginEmail.setError("Please Enter a Email Id");
                }
                else if(loginPassword.getText().toString().trim().length()==0){
                    loginPassword.setError("Please Enter Password");
                }
                else{
                    loginAccount(loginEmail.getText().toString().trim(), loginPassword.getText().toString().trim());
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
        if (currentUser != null) {
            reload();        }
    }

    private void reload() {
    }

    private void loginAccount(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Login", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("login failed", "signInWithEmail:failure", task.getException());
                            Toast.makeText(getActivity(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(getActivity(), "Login Successful", Toast.LENGTH_LONG).show();
            Intent intent=new Intent(getActivity(),MainActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
    }
}
