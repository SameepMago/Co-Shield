package com.ymca.co_shield;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    TabLayout loginTabLayout;
    ViewPager2 loginViewPager;
    LoginAdapter loginAdapter;
    FloatingActionButton googleLogin,twitterLogin,facebookLogin;
    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    FirebaseAuth.AuthStateListener authStateListener;




    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 1);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginTabLayout=findViewById(R.id.loginTabLayout);
        loginViewPager=findViewById(R.id.loginViewPager);
        googleLogin=findViewById(R.id.googleLogin);
        loginTabLayout.addTab(loginTabLayout.newTab().setText("Login"));
        loginTabLayout.addTab(loginTabLayout.newTab().setText("SignUp"));
        loginTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        FragmentManager fm=getSupportFragmentManager();
        loginAdapter=new LoginAdapter(fm,getLifecycle());
        loginViewPager.setAdapter(loginAdapter);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();



        loginTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
             loginViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        loginViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                loginTabLayout.selectTab(loginTabLayout.getTabAt(position));
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.google_key)).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        authStateListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // Get signedIn user
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //if user is signed in, we call a helper method to save the user details to Firebase
                if (user != null) {
                    // User is signed in
                    // you could place other firebase code
                    //logic to save the user details to Firebase
                    Log.d("authChanged", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("Sign out", "onAuthStateChanged:signed_out");
                }
            }
        };
        googleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

    }

    public void setCurrentTab(int position){

        loginViewPager.setCurrentItem(position); //your viewpager object

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (authStateListener != null){
           mAuth.signOut();
            Log.d("signout","SIgn out");
        }
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null){
            mAuth.removeAuthStateListener(authStateListener);
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("sign In", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            String uId=user.getUid();
                            addDataToFirestore(account.getEmail(),account.getDisplayName(),uId);
                            updateUI(user);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("SignIn error", "signInWithCredential:failure", task.getException());
                            updateUI(null);
                        }
                    }
                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 1) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            Log.d("task",task.toString());
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("authentication", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("failure", "Google sign in failed", e);
            }
        }
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(this, "Login Successful", Toast.LENGTH_LONG).show();
            Intent intent=new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void addDataToFirestore(String email,String name,String uId){
        CollectionReference dbProfile = db.collection("User");
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("name", name);
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