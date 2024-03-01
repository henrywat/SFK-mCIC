/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sfk.mcic.Service;

//import android.app.IntentService;
import android.app.IntentService;
import android.content.Intent;
//import android.content.SharedPreferences;
import android.os.AsyncTask;
//import android.os.Bundle;
//import android.preference.PreferenceManager;
//import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;
//import android.widget.Toast;

//import com.google.android.gms.gcm.GcmPubSub;
//import com.google.android.gms.gcm.GoogleCloudMessaging;
//import com.google.android.gms.iid.InstanceID;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.sfk.mcic.R;
import com.sfk.mcic.Util.Params;

import org.apache.http.conn.ConnectTimeoutException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

public class RegistrationIntentService extends IntentService {

    private static final String logTag = "RegistrationIntentService";
    //private static final String[] TOPICS = {"global"};
    private String servletIP, servletPort, serviceName;
    private int uid;


    public RegistrationIntentService() { super(logTag); }

    @Override
    public void onCreate() {
        super.onCreate();
        //Log.w(logTag, "onCreate");
    }

    @Override
    public void onDestroy() {
        //Log.w(logTag, "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Log.w(logTag, "onHandleIntent");
        servletIP = getResources().getString(R.string.servletIP);
        servletPort = getResources().getString(R.string.servletPort);
        serviceName = getResources().getString(R.string.serviceName);
        uid = intent.getIntExtra("uid", 0);
        Log.w(logTag, "Incoming uid=" + uid);

        try {
            synchronized (logTag) {

                //向GCM註冊(產生Token)
                //InstanceID instanceID = InstanceID.getInstance(this);
                //String token = instanceID.getToken("613185692296", GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

                //向Firebase註冊(產生Token)
                //String token = FirebaseInstanceId.getInstance().getToken();
                FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                if (!task.isSuccessful()) {
                                    Log.w(logTag, "getInstanceId failed", task.getException());
                                    return;
                                }

                                // Get new Instance ID token
                                String token = task.getResult().getToken();

                                // Log and toast
                                //String msg = getString(R.string.msg_token_fmt, token);
                                //Log.w(logTag, "GCM Registration Token: " + token.substring(0,10));
                                //Log.w(logTag, msg);

                                //App Server註冊(回傳Token)
                                Log.w(logTag, "sendRegistrationToServer -> "+ token);
                                sendRegistrationToServer(token);
                                //訂閱發怖主題
                                //subscribeTopics(token);
                            }
                        });



            }
        } catch (Exception e) {
            Log.w(logTag, "Failed to complete token refresh", e);
        }
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
        String path = "http://"+servletIP+":"+servletPort+"/"+serviceName+"/SendGCMToken?";
        path += "uid=" + uid;
        path += "&token=" + token;
        Log.w(logTag, "Send Firebase token ==> "+path);
        new AsyncGCMToken().execute(path);
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    /*
    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
    */
    // [END subscribe_topics]
    class AsyncGCMToken extends AsyncTask<String, Integer, String> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected void onProgressUpdate(Integer... progress) {
            // TODO Auto-generated method stub
        }

        protected void onPostExecute(String result) {
            Log.w(logTag, "Device Registration uid="+uid+" result="+result+".");
        }

        protected String doInBackground(String... path) {
            // TODO Auto-generated method stub

            StringBuffer response = new StringBuffer();
            URL url;
            try {
                url = new URL(path[0]);
                URLConnection con = url.openConnection();
                con.setConnectTimeout(Params.connectiontimeout);
                con.setReadTimeout(Params.conreadtimeout);
                InputStream is = con.getInputStream();
                InputStreamReader reader = new InputStreamReader(is);
                BufferedReader breader = new BufferedReader(reader);
                String line = "";
                while((line=breader.readLine()) != null) {
                    response.append(line + "\n");
                }
                breader.close();
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                Log.e(logTag, "AsyncTeamList MalformedURLException, uid="+uid);
                return ("*Invalid URL");
            } catch (UnknownHostException e) {
                Log.e(logTag, "AsyncTeamList UnknownHostException, uid="+uid);
                return ("*Server Unreachable");
            } catch (ConnectTimeoutException e) {
                Log.e(logTag, "AsyncTeamList ConnectTimeoutException, uid="+uid);
                return ("*Connection Timeout");
            } catch (SocketTimeoutException e) {
                Log.e(logTag, "AsyncTeamList SocketTimeoutException, uid="+uid);
                return ("*Bad Internet");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return response.toString();
        }
    }
}
