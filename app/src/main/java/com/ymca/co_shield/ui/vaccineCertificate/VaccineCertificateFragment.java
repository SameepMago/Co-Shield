package com.ymca.co_shield.ui.vaccineCertificate;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.ymca.co_shield.InputStreamVolleyRequest;
import com.ymca.co_shield.LoginActivity;
import com.ymca.co_shield.R;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.lang.System.out;

public class VaccineCertificateFragment extends Fragment {

    EditText certiMobile;
    Button getOtpButton;
    RequestQueue requestQueue;
    String mobileNumber;
    String txnId;
    EditText enterOtp;
    Button confirmOtpButton;
    String otp;
    String token;
    EditText certiBeneficiary;
    Button downloadCerti;
    TextView enterMobileInstruc;
    TextView enterBeneficiaryInstruc;
    String beneficiaryId;
    Button backButton;
    GoogleSignInClient googleSignInClient;
    ImageView signOutButton2;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.vaccine_certificate, container, false);
        certiMobile = root.findViewById(R.id.certiMobile);
        getOtpButton =root.findViewById(R.id.otpButton);
        certiBeneficiary=root.findViewById(R.id.certiBeneficiary);
        backButton=root.findViewById(R.id.backButton);
        downloadCerti=root.findViewById(R.id.downloadCerti);
        enterMobileInstruc=root.findViewById(R.id.enterMobileInstruc);
        enterBeneficiaryInstruc=root.findViewById(R.id.enterBeneficiaryInstruc);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        signOutButton2 =root.findViewById(R.id.logout2);
        GoogleSignInOptions googleSignInOptions=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.google_key)).requestEmail().build();
        googleSignInClient= GoogleSignIn.getClient(getActivity(),googleSignInOptions);

        signOutButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        signOutButton2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getContext(),"Logout Button",Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        requestQueue= Volley.newRequestQueue(getContext());
        getOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mobileNumber=certiMobile.getText().toString().trim();
                Dialog otpDialog=new Dialog(getContext());
                otpDialog.setContentView(R.layout.confirm_otp);
                otpDialog.setCancelable(true);
                otpDialog.show();
                enterOtp=(EditText) otpDialog.findViewById(R.id.enterOtp);
                confirmOtpButton=(Button) otpDialog.findViewById(R.id.confirmOtpButton);
                Window window = otpDialog.getWindow();
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                JSONObject mobileObject=new JSONObject();
                try {
                    mobileObject.put("mobile",mobileNumber);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonObjectRequest getOtpRequest=new JsonObjectRequest(Request.Method.POST, getString(R.string.backend_url) + "/v2/auth/public/generateOTP", mobileObject, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            txnId=response.getString("txnId");
                            Log.d("txnId",txnId);
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
                requestQueue.add(getOtpRequest);

                confirmOtpButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        otp=enterOtp.getText().toString().trim();
                        if(otp.length()<6){
                            enterOtp.setError("Enter a Valid Otp");
                        }
                        else{
                            try {
                                String sha256=toHexString(getSHA(otp));
                                JSONObject confirmObject=new JSONObject();
                                confirmObject.put("otp",sha256);
                                confirmObject.put("txnId",txnId);
                                JsonObjectRequest confirmOtpRequest=new JsonObjectRequest(Request.Method.POST, getString(R.string.backend_url) + "/v2/auth/public/confirmOTP", confirmObject, new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            token=response.getString("token");
                                            Log.d("token",token);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.d("error",error.toString());
                                    }
                                });
                                requestQueue.add(confirmOtpRequest);
                                otpDialog.dismiss();
                                certiMobile.setVisibility(View.GONE);
                                getOtpButton.setVisibility(View.GONE);
                                downloadCerti.setVisibility(View.VISIBLE);
                                backButton.setVisibility(View.VISIBLE);
                                certiBeneficiary.setVisibility(View.VISIBLE);
                                enterMobileInstruc.setVisibility(View.GONE);
                                enterBeneficiaryInstruc.setVisibility(View.VISIBLE);
                            } catch (NoSuchAlgorithmException | JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                });
            }
        });

        downloadCerti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beneficiaryId=certiBeneficiary.getText().toString().trim();
                if(beneficiaryId.length()<13){
                    certiBeneficiary.setError("Please enter a valid Beneficiary Id");
                }
                else{
                        InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, getString(R.string.backend_url) + "/v2/registration/certificate/public/download?beneficiary_reference_id=" + beneficiaryId, new Response.Listener<byte[]>() {
                        @Override
                        public void onResponse(byte[] response) {
                            if (response!=null) {
                                try {
                                    UUID uuid = UUID.randomUUID();
                                    String randomUUIDString = uuid.toString();
                                    FileOutputStream outputStream;
                                    String name="certificate"+"_"+randomUUIDString+".pdf";
                                    File outFile = new File(Environment
                                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                            + "/" + name);
                                    outputStream = new FileOutputStream(outFile);
                                    outputStream.write(response);
                                    outputStream.close();
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    Uri uri = Uri.fromFile(outFile);
                                    intent.setDataAndType(uri, "application/pdf");
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    startActivity(intent);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                Toast.makeText(getContext(), "Download complete.", Toast.LENGTH_LONG).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("error",error.toString());
                            Log.d("error","Unable to Download the file");
                        }
                    },null) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> headers = new HashMap<String, String>();
                            headers.put("Content-Type", "application/pdf");
                            headers.put("Authorization", "Bearer " + token);
                            return headers;
                        }
                    };
                    RequestQueue mRequestQueue = Volley.newRequestQueue(getContext());
                    mRequestQueue.add(request);
                }

            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                certiMobile.setVisibility(View.VISIBLE);
                getOtpButton.setVisibility(View.VISIBLE);
                downloadCerti.setVisibility(View.GONE);
                backButton.setVisibility(View.GONE);
                certiBeneficiary.setVisibility(View.GONE);
                enterMobileInstruc.setVisibility(View.VISIBLE);
                enterBeneficiaryInstruc.setVisibility(View.GONE);
            }
        });


        return root;
    }
    public static byte[] getSHA(String input) throws NoSuchAlgorithmException
    {
        // Static getInstance method is called with hashing SHA
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        // digest() method called
        // to calculate message digest of an input
        // and return array of byte
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String toHexString(byte[] hash)
    {
        // Convert byte array into signum representation
        BigInteger number = new BigInteger(1, hash);

        // Convert message digest into hex value
        StringBuilder hexString = new StringBuilder(number.toString(16));

        // Pad with leading zeros
        while (hexString.length() < 32)
        {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }
    public void saveInternalFolder(String contents, String filename) {
        try
        {
            // Creates a trace file in the primary external storage space of the
            // current application.
            // If the file does not exists, it is created.
            File traceFile = new File(((getContext())).getExternalFilesDir(null), filename);
            if (!traceFile.exists())
                traceFile.createNewFile();
            // Adds a line to the trace file
            BufferedWriter writer = new BufferedWriter(new FileWriter(traceFile, true /*append*/));
            writer.write(contents);
            writer.close();
            // Refresh the data so it can seen when the device is plugged in a
            // computer. You may have to unplug and replug the device to see the
            // latest changes. This is not necessary if the user should not modify
            // the files.
            MediaScannerConnection.scanFile((getContext()),
                    new String[] { traceFile.toString() },
                    null,
                    null);

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
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