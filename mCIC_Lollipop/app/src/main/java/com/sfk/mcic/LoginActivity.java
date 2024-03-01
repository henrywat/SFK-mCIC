package com.sfk.mcic;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.ConnectionResult;
import com.sfk.mcic.Service.RegistrationIntentService;
import com.sfk.mcic.Util.Params;

import org.apache.http.conn.ConnectTimeoutException;

public class LoginActivity extends AppCompatActivity {

    private TextView tvLoginName, tvLoginPw;
    private Button btnLogin;
    private int uid;
    private String servletIP, servletPort, serviceName, loginName, loginPw;
    //private boolean loggedin;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final String logTag = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.d(logTag, "This is debug message.");
        Log.e(logTag, "This is error message.");
        Log.i(logTag, "This is information message.");
        Log.v(logTag, "This is verbose message.");
        Log.w(logTag, "This is warning message.");

        //loggedin = false;
        tvLoginName = (TextView)this.findViewById(R.id.editTextLoginName);
        tvLoginPw = (TextView)this.findViewById(R.id.editTextLoginPw);
        btnLogin = (Button)this.findViewById(R.id.btnLogin);
        servletIP = getResources().getString(R.string.servletIP);
        servletPort = getResources().getString(R.string.servletPort);
        serviceName = getResources().getString(R.string.serviceName);

        btnLogin.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View arg0) {

                loginName = tvLoginName.getText().toString().trim().toLowerCase();
                loginPw = tvLoginPw.getText().toString().trim();

                if (checkPlayServices() && !loginName.equals("") && !loginPw.equals("")) {
                        String path = "http://"+servletIP+":"+servletPort+"/"+serviceName+"/GetCICUid?";
                        path += "u=" + loginName;
                        path += "&p=" + loginPw;
                        //Log.w(logTag, "Login Path ==> "+path);
                        new AsyncLogin().execute(path);
                }
        }});

        checkExistingUser();
    }

    private void checkExistingUser() {
        SharedPreferences sp1= getSharedPreferences("CIC", MODE_PRIVATE);
        uid = sp1.getInt("uid", -1);

        if (uid>=0) {
            Log.w(logTag, "Existing user found, uid="+uid);
            //loggedin = true;

            // register token
            Intent intent = new Intent(getBaseContext(), RegistrationIntentService.class);
            intent.putExtra("uid", uid);
            Log.w(logTag, "Calling RegistrationIntentService");
            startService(intent);

            Intent i1 = new Intent(getBaseContext(), ListActivity.class);
            startActivity(i1);
        } else {
            Log.w(logTag, "Existing user NOT found");
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.w(logTag, "This device is not supported.");
                Toast.makeText(getBaseContext(), "This device is not supported.", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        checkExistingUser();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        // Dismiss the progress bar when application is closed
        //if (prgDialog != null) {
        //    prgDialog.dismiss();
        //}
    }

    /*
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type: // we set this to 0
                //prgDialog = new ProgressDialog(this);
                //prgDialog.setMessage("Downloading file. Please wait...");
                //prgDialog.setIndeterminate(false);
                //prgDialog.setMax(100);
                //prgDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                //prgDialog.setCancelable(true);
                prgDialog = ProgressDialog.show(this, "Login", "Please Wait ...");
                //prgDialog.show();
                return prgDialog;
            default:
                return null;
        }
    }*/

    class AsyncLogin extends AsyncTask<String, Integer, String> {

        private int progress;
        private ProgressDialog mDialog;


        protected void onPreExecute() {
            super.onPreExecute();
            //showDialog(progress_bar_type);
            mDialog = new ProgressDialog(LoginActivity.this);
            mDialog.setTitle("Logging In");
            mDialog.setMessage("Please Wait ...");
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            //mDialog.setProgress(100);
            mDialog.setIcon(R.drawable.login_icon);
            //mDialog.setMax(100);
            //mDialog.setIndeterminate(false);
            mDialog.setCancelable(false);
            //mDialog.setButton("取消", new DialogInterface.OnClickListener() {
            //    public void onClick(DialogInterface dialog, int i)
            //    {
            //        dialog.cancel();
            //    }
            //});
            mDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {
            // TODO Auto-generated method stub
            mDialog.setProgress(progress[0]);
        }

        protected void onPostExecute(String result) {

            result = result.trim();
            Log.w(logTag, "login servlet result="+result);
            if (!result.startsWith("*")) {
                try {
                    int temp = Integer.parseInt(result);

                    //Log.w(logTag, "temp = "+temp);
                    if (temp>=0) {
                        uid = temp;
                    } else {
                        uid = -1;
                    }
                } catch (Exception e) {
                    uid = -1;
                }

                //Log.w(logTag, "uid after Login ==> "+uid);
                if (uid <0) {
                    Toast.makeText(LoginActivity.this, "Invalid Login", Toast.LENGTH_SHORT).show();
                    Log.w(logTag, "Invalid login with username="+ loginName);
                } else {
                    Log.w(logTag, "login success username=" + loginName + " with uid=" + uid + ", result=" + result);
                    Intent intent = new Intent(getBaseContext(), RegistrationIntentService.class);
                    intent.putExtra("uid", uid);
                    startService(intent);

                    SharedPreferences sp1= getSharedPreferences("CIC", MODE_PRIVATE);
                    SharedPreferences.Editor sp2 = sp1.edit();
                    sp2.putInt("uid", uid);
                    sp2.putString("loginName", loginName);
                    sp2.commit();

                    Intent i1 = new Intent(LoginActivity.this, ListActivity.class);
                    startActivity(i1);
                }

            } else {
                Toast.makeText(LoginActivity.this, result, Toast.LENGTH_SHORT).show();
            }
            if (mDialog!=null) mDialog.dismiss();
        }

        protected String doInBackground(String... path) {
            // TODO Auto-generated method stub

            StringBuffer response = new StringBuffer();
            URL url;
            try {
                url = new URL(path[0]);
                URLConnection con = url.openConnection();
                con.setDoOutput(true);
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
                is.close();
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                Log.w(logTag, "AsyncLogin L1 MalformedURLException, uid="+uid);
                return ("*L1 Invalid URL");
            } catch (UnknownHostException e) {
                Log.w(logTag, "AsyncLogin L2 UnknownHostException, uid="+uid);
                return ("*L2 Server Unreachable");
            } catch (ConnectTimeoutException e) {
                Log.w(logTag, "AsyncLogin L3 ConnectTimeoutException, uid="+uid);
                return ("*L3 Connection Timeout");
            } catch (SocketTimeoutException e) {
                Log.w(logTag, "AsyncLogin L4 SocketTimeoutException, uid="+uid);
                return ("*L4 Bad Internet");
            } catch (ConnectException e) {
                Log.w(logTag, "AsyncLogin L5 ConnectException, uid="+uid);
                return ("*L5 Server down");
            } catch (IOException e) {
                Log.w(logTag, "AsyncLogin L6 IOException, uid="+uid);
                e.printStackTrace();
                return ("*L6 Bad Response");
            }

            return response.toString();
        }
    }
}
