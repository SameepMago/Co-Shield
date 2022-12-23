package com.ymca.co_shield;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.Message;
import javax.sql.DataSource;

public class VaccineAlertService extends Service {

    String pinCode;
    String date;
    RequestQueue requestQueue;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    String uId;
    private FirebaseFirestore db;
    String toMail;
    String name;
    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";

    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";
    public VaccineAlertService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final String ChannelId="Foreground Service Id";
        final String username = "vaccineavailability2@gmail.com";
        final String password = "Sameep123@";
        requestQueue= Volley.newRequestQueue(this);
        NotificationChannel notificationChannel;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(ChannelId,ChannelId, NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(notificationChannel);
            Notification.Builder notification=new Notification.Builder(this,ChannelId).setContentTitle("Vaccine Alert Service").setContentText("Vaccine Alert Service is running in background").setSmallIcon(R.drawable.vaccine);
            startForeground(1001,notification.build());
        }

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                JsonObjectRequest alertRequest=new JsonObjectRequest(Request.Method.GET, getString(R.string.backend_url) + "/v2/appointment/sessions/public/calendarByPin?pincode=" + pinCode + "&date=" + date, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray centerArray=response.getJSONArray("centers");
                            if(centerArray.length()==0){
                                Log.i("vaccine","no available center right now");
                            }
                            else{
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
                                    String title= "VACCINE AVAILABLE IN "+centerName.toUpperCase();
                                    String content="Dear "+name+","+"\n\nAccording to the data of Govt of India, there is vaccine availability at "+centerLocation +" from "+ centerFromTime+" to "+centerToTime+".\n\n"+"Vaccine Name - "+vaccineName+"\n"+"Available Doses - "+availableCapacity+"\n"+"Fee Type - "+feeType+"\n"+"Age Limit - "+ageLimit;
                                    Thread thread = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try  {
                                                sendEmail(username,password,title,content);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    thread.start();
                                }
                            }

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

                requestQueue.add(alertRequest);
            }
        },1000,10000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if(intent!=null){
            String action = intent.getAction();
            switch (action) {
                case ACTION_START_FOREGROUND_SERVICE:
                    pinCode=intent.getStringExtra("pinCode");
                    date=intent.getStringExtra("date");
                    final String username = "vaccineavailability2@gmail.com";
                    final String password = "Sameep123@";
                    mAuth=FirebaseAuth.getInstance();
                    db=FirebaseFirestore.getInstance();
                    user=mAuth.getCurrentUser();
                    uId=user.getUid();
                    Log.d("uId",uId);
                    DocumentReference docRef = db.collection("User").document(uId);
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.d("getSuccessful", "DocumentSnapshot data: " + document.getData());
                                    toMail=document.getString("email");
                                    Log.d("email",toMail);
                                    name=document.getString("name");
                                    String title="SUBSCRIBED FOR VACCINE AVAILABILITY ALERT";
                                    String message="Dear "+name+"," + "\n\nYou have successfully subscribed for the alerts of vaccine availability in the area having picode= "+pinCode+"."+"\n\nYou will receive time to time Emails whenever there will be a vaccination center available in that area.";
                                    Thread thread = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try  {
                                                sendEmail(username,password,title,message);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    thread.start();
                                } else {
                                    Log.d("No Document", "No such document");
                                }
                            } else {
                                Log.d("get Failed", "get failed with ", task.getException());
                            }
                        }
                    });
                    break;
                case ACTION_STOP_FOREGROUND_SERVICE:
                    stopForegroundService();
                    break;
            }
        }

        return START_STICKY;
    }

    void sendEmail(String username,String password,String title,String content){

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator(){
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                }
        );
        try {
            javax.mail.Message message=new MimeMessage(session);
            message.setFrom(new InternetAddress("vaccineavailability2@gmail.com"));
            message.setRecipients(Message.RecipientType.TO,
                   InternetAddress.parse(toMail));
            message.setSubject(title);
            message.setText(content);
            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }

    private void stopForegroundService() {

        // Stop foreground service and remove the notification.
        stopForeground(true);

        // Stop the foreground service.
        stopSelf();
    }

}
