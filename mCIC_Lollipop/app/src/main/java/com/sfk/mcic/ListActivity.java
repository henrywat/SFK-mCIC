package com.sfk.mcic;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sfk.mcic.Service.RegistrationIntentService;
import com.sfk.mcic.Util.Params;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ListActivity extends AppCompatActivity {

    private TextView tvLastUpdate, tvLoginName;
    private Button btnFilter;
    private Spinner spPhoto, spDay, spDistrict;
    private int filterDistrictIdx, filterPhotoIdx, filterDayIdx, uid;
    private ListView caseLV;
    private String servletIP, servletPort, serviceName, loginName;
    //private ProgressDialog mDialog;
    private ArrayList<HashMap> castList;
    private JSONArray jarray;
    private ArrayAdapter<String> spDistrictAdapter;
    private List<String> filterDistrict;
    private final String logTag = "ListActivity";
    private AsyncTask asyncDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        tvLastUpdate = (TextView)this.findViewById(R.id.textLastDateTime);
        tvLoginName = (TextView)this.findViewById(R.id.textLoginName);
        btnFilter = (Button)this.findViewById(R.id.btnFilter);
        spDistrict= (Spinner)this.findViewById(R.id.spinnerDistrict);
        spPhoto = (Spinner)this.findViewById(R.id.spinnerPhoto);
        spDay = (Spinner)this.findViewById(R.id.spinnerDay);
        btnFilter = (Button)this.findViewById(R.id.btnFilter);
        caseLV = (ListView)this.findViewById(R.id.listViewCase);
        servletIP = getResources().getString(R.string.servletIP);
        servletPort = getResources().getString(R.string.servletPort);
        serviceName = getResources().getString(R.string.serviceName);

        // load shared preferences
        SharedPreferences sp1= getSharedPreferences("CIC", MODE_PRIVATE);
        uid = sp1.getInt("uid", 0);
        filterDistrictIdx = sp1.getInt("filterDistrictIdx", 0);
        filterPhotoIdx = sp1.getInt("filterPhotoIdx", 0);
        filterDayIdx = sp1.getInt("filterDayIdx", 0);
        loginName = sp1.getString("loginName", "n/a");
        tvLoginName.setText("Welcome, "+loginName);

        filterDistrict = new ArrayList<String>();
        spDistrictAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, filterDistrict);

        // setup District Filter Spinner
        //List<String> filterDistrict = new ArrayList<String>();
        //filterDistrict.add("ALL");
        /*filterDistrict.add("KT");
        filterDistrict.add("KB");
        filterDistrict.add("WTS");
        filterDistrict.add("HH");
        filterDistrict.add("KC");
        filterDistrict.add("StrKC");
        filterDistrict.add("StrKE");
        filterDistrict.add("StrHN");
        filterDistrict.add("CHT");
        filterDistrict.add("EHC");
        filterDistrict.add("SLPKG1");*/

        //if (filterDistrict.size()<1)
        //    getTeamList();

        //ArrayAdapter<String> spDistrictAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, filterDistrict);
        //spDistrictAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spDistrict.setAdapter(spDistrictAdapter);
        //if (filterDistrictIdx > 0) spDistrict.setSelection(filterDistrictIdx);

        // setup Photo Filter Spinner
        List<String> filterPhoto = new ArrayList<String>();
        //filterPhoto.add("ALL");
        //filterPhoto.add("No Before");
        //filterPhoto.add("No After");
        //filterPhoto.add("No Photo");
        //filterPhoto.add("Completed");
        filterPhoto.add("Pending");
        filterPhoto.add("Overdue");
        filterPhoto.add("Completed");
        filterPhoto.add("All");

        ArrayAdapter<String> spPhotoAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, filterPhoto);
        spPhotoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPhoto.setAdapter(spPhotoAdapter);
        if (filterPhotoIdx > 0) spPhoto.setSelection(filterPhotoIdx);

        // setup Day Filter Spinner
        List<String> filterDay = new ArrayList<String>();
        filterDay.add("7 ");
        filterDay.add("30");
        filterDay.add("60");
        ArrayAdapter<String> spDayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, filterDay);
        spDayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDay.setAdapter(spDayAdapter);
        if (filterDayIdx > 0) spDay.setSelection(filterDayIdx);

        // download case list
        refreshCaseList();



        // when a case is selected
        caseLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub

                HashMap item = (HashMap) arg0.getItemAtPosition(arg2);
                Intent i1 = new Intent(getBaseContext(), DetailActivity.class);
                i1.putExtra("csid", item.get("id").toString());
                //i1.putExtra("caseno", item.get("caseno").toString());
                //i1.putExtra("district", item.get("district").toString());
                //i1.putExtra("street", item.get("street").toString());
                //i1.putExtra("detail", item.get("detail").toString());
                //i1.putExtra("casedate", item.get("casedate").toString());
                //i1.putExtra("inspphoto", item.get("inspphoto").toString());
                //i1.putExtra("compphoto", item.get("compphoto").toString());

                startActivity(i1);
            }});

        // when Filter Go Button is clicked
        btnFilter.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View arg0) {
                if (filterDistrict.size()<1)
                    getTeamList();
                else {
                    SharedPreferences sp1 = getSharedPreferences("CIC", MODE_PRIVATE);
                    SharedPreferences.Editor sp2 = sp1.edit();

                    filterDistrictIdx = spDistrict.getSelectedItemPosition();
                    sp2.putInt("filterDistrictIdx", filterDistrictIdx);

                    filterPhotoIdx = spPhoto.getSelectedItemPosition();
                    sp2.putInt("filterPhotoIdx", filterPhotoIdx);

                    filterDayIdx = spDay.getSelectedItemPosition();
                    sp2.putInt("filterDayIdx", filterDayIdx);

                    sp2.commit();

                    refreshCaseList();
                }
            }});
    }

    public void refreshCaseList()  {

        Log.w(logTag, "Refreshing Case List with uid="+uid+", district Idx="+filterDistrictIdx+".");

        String path = "http://"+servletIP+":"+servletPort+"/"+serviceName+"/GetCICCaseList?";
        path += "team="+filterDistrictIdx;
        /*switch(filterDistrictIdx) {
            case 0: path += "ALL"; break;
            case 1: path += "KT"; break;
            case 2: path += "KB"; break;
            case 3: path += "WTS"; break;
            case 4: path += "HH"; break;
            case 5: path += "KC"; break;
            case 6: path += "StrKC"; break;
            case 7: path += "StrKE"; break;
            case 8: path += "StrHN"; break;
            case 9: path += "CHT"; break;
            case 10: path += "EHC"; break;
            case 11: path += "SLPKG1"; break;
        }*/

        path += "&uid="+uid;
        path += "&fphoto="+filterPhotoIdx;
        path += "&fday="+filterDayIdx;

        Log.w(logTag, path);
        asyncDownload = new AsyncCaseList().execute(path);

        Date toDay = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM hh:mm", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        tvLastUpdate.setText(formatter.format(toDay));
    }

    private void getTeamList() {
        Log.w(logTag, "Refreshing Team List with uid="+uid+".");

        String path = "http://"+servletIP+":"+servletPort+"/"+serviceName+"/GetCICTeamList?";
        path += "uid="+uid;

        //Log.w(logTag, path);
        new AsyncTeamList().execute(path);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        //Log.w(logTag, "onResume");
        spDistrict.setSelection(filterDistrictIdx);
        spPhoto.setSelection(filterPhotoIdx);
        spDay.setSelection(filterDayIdx);
        //refreshCaseList();
        if (asyncDownload.isCancelled()) asyncDownload.cancel(true);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (asyncDownload.isCancelled()) asyncDownload.cancel(true);
        // Dismiss the progress bar when application is closed
        //if (prgDialog != null) {
        //    prgDialog.dismiss();
        //}
    }

    /*
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 0: //we set this to 0
                mDialog = new ProgressDialog(this);
                mDialog.setIndeterminate(false);
                mDialog.setMax(100);
                mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mDialog.setCancelable(true);
                mDialog.setMessage("Checking Team List ...");
                mDialog.show();
                return mDialog;
            default:
                return null;
        }
    }*/

    class AsyncCaseList extends AsyncTask<String, Integer, String> {

        private int progress;
        private ProgressDialog mDialog;


        protected void onPreExecute() {
            super.onPreExecute();
            //showDialog(0);

            mDialog = new ProgressDialog(ListActivity.this);
            mDialog.setTitle("Please Wait ...");
            mDialog.setMessage("Please Wait ...");
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mDialog.setIcon(R.drawable.caselistdownload_icon);
            //mDialog.setProgress(0);
            //mDialog.setMax(100);
            mDialog.setIndeterminate(false);
            mDialog.setCancelable(false);
            mDialog.show();

            publishProgress(progress, 1);
            if (filterDistrict.size()<1)
                getTeamList();

        }

        protected void onProgressUpdate(Integer... progress) {
            mDialog.setProgress(progress[0]);
            if (progress[1]==1) mDialog.setTitle("Checking Team List");
            else if (progress[1]==2) {mDialog.setTitle("Requesting Case List");}
            else if (progress[1]==3) {mDialog.setTitle("Downloading Case List");}
            else if (progress[1]==4) {mDialog.setTitle("Preparing Case List");}
        }

        protected void onPostExecute(String result) {
            if (!result.startsWith("*")) {
                castList = new ArrayList<HashMap>();
                try {
                    jarray = new JSONArray(result);
                    //Log.w(logTag, "refreshCaseList array.length() = "+jarray.length());
                    Toast.makeText(getBaseContext(), "Total : " + jarray.length(), Toast.LENGTH_SHORT).show();
                    JSONObject jobj;
                    HashMap<String, Object> item;
                    for (int i = 0; i < jarray.length(); i++) {
                        jobj = jarray.getJSONObject(i);
                        item = new HashMap<String, Object>();
                        item.put("id", jobj.get("id"));
                        item.put("caseno", jobj.get("caseno"));
                        item.put("casedate", jobj.get("casedate"));
                        item.put("district", jobj.get("district"));
                        item.put("street", jobj.get("street"));
                        item.put("detail", jobj.get("detail"));
                        item.put("inspphoto", jobj.get("inspphoto"));
                        item.put("compphoto", jobj.get("compphoto"));
                        item.put("casetype", jobj.get("casetype"));
                        item.put("colorflag", jobj.get("colorflag"));
                        castList.add(item);
                        publishProgress(progress+=3, 4);
                    }
                } catch (JSONException e) {
                    Log.e(logTag, "AsyncCaseList D7 JSON Exception, uid="+uid);
                    Toast.makeText(getBaseContext(), "*D7 Bad JSON", Toast.LENGTH_SHORT).show();
                }
                caseLV.setAdapter(new CaseListAdapter(getBaseContext(), castList));
            } else {
                Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
            }
            if(mDialog!=null) mDialog.dismiss();
        }

        protected String doInBackground(String... path) {

            publishProgress(progress+=5, 2);
            StringBuffer response = new StringBuffer();
            try {
                URL url = new URL(path[0]);
                URLConnection con = url.openConnection();
                con.setConnectTimeout(Params.connectiontimeout);
                con.setReadTimeout(Params.conreadtimeout);
                con.connect();
                InputStream is = con.getInputStream();

                /*Reader reader = new BufferedReader(new InputStreamReader
                        (is, Charset.forName(StandardCharsets.UTF_8.name())));
                    int c = 0;
                    while ((c = reader.read()) != -1) {
                        response.append((char) c);
                        publishProgress(progress+=3, 3);
                    }

                is.close();*/


                //publishProgress(progress += 10); // 20
                InputStreamReader reader = new InputStreamReader(is);
                //publishProgress(progress += 10); // 30
                BufferedReader breader = new BufferedReader(reader);
                String line = "";
                while((line=breader.readLine()) != null) {
                    response.append(line + "\n");
                    //publishProgress(progress += 5);
                }
                breader.close();
                is.close();

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                Log.e(logTag, "AsyncCaseList D1 MalformedURLException, uid="+uid);
                return ("*D1 Invalid URL");
            } catch (UnknownHostException e) {
                Log.e(logTag, "AsyncCaseList D2 UnknownHostException, uid="+uid);
                return ("*D2 Server Unreachable");
            } catch (ConnectTimeoutException e) {
                Log.e(logTag, "AsyncCaseList D3 ConnectTimeoutException, uid="+uid);
                return ("*D3 Connection Timeout");
            } catch (SocketTimeoutException e) {
                Log.e(logTag, "AsyncCaseList D4 SocketTimeoutException, uid="+uid);
                return ("*D4 Bad Internet");
            } catch (ConnectException e) {
                Log.w(logTag, "AsyncCaseList D5 ConnectException, uid="+uid);
                return ("*D5 Server down");
            } catch (IOException e) {
                Log.e(logTag, "AsyncCaseList D6 IOException, uid=" + uid);
                return ("*D6 Bad Response");
            }

            return response.toString();
        }
    }

    class AsyncTeamList extends AsyncTask<String, Integer, String> {
        protected void onPreExecute() {
            super.onPreExecute();
            //showDialog(progress_bar_type);
        }

        protected void onProgressUpdate(Integer... progress) {
            // TODO Auto-generated method stub
            //prgDialog.setProgress(progress[0]);
        }

        protected void onPostExecute(String result) {

            if (!result.startsWith("*")) {
                try {
                    filterDistrict.add("ALL");
                    jarray = new JSONArray(result);
                    //Log.w(logTag, "teamList array.length() = "+jarray.length());
                    //Toast.makeText(getBaseContext(), "Total : "+jarray.length(), Toast.LENGTH_SHORT).show();
                    JSONObject jobj;
                    for (int i = 0; i < jarray.length(); i++) {
                        jobj = jarray.getJSONObject(i);
                        switch (jobj.get("team").toString()) {
                            case "11":
                                filterDistrict.add("KT");
                                break;
                            case "12":
                                filterDistrict.add("KB");
                                break;
                            case "13":
                                filterDistrict.add("WTS");
                                break;
                            case "14":
                                filterDistrict.add("HH");
                                break;
                            case "15":
                                filterDistrict.add("KC");
                                break;
                            case "35":
                                filterDistrict.add("Str KC");
                                break;
                            case "36":
                                filterDistrict.add("Str KE");
                                break;
                            case "38":
                                filterDistrict.add("Str HN");
                                break;
                            case "43":
                                filterDistrict.add("SLPKG1");
                                break;
                            case "66":
                                filterDistrict.add("CHT");
                                break;
                            case "67":
                                filterDistrict.add("EHC");
                                break;
                            case "46":
                                filterDistrict.add("TP");
                                break;
                            case "70":
                                filterDistrict.add("STR KE1");
                                break;
                            case "71":
                                filterDistrict.add("STR KE2");
                                break;
                            case "72":
                                filterDistrict.add("STR KW1");
                                break;
                            case "73":
                                filterDistrict.add("STR KW2");
                                break;
                        }
                        //Log.w(logTag, "asyncTeam idx="+jobj.get("idx")+", team="+jobj.get("team"));
                    }

                    spDistrictAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spDistrict.setAdapter(spDistrictAdapter);
                    if (filterDistrictIdx > 0) spDistrict.setSelection(filterDistrictIdx);
                } catch (JSONException e) {
                    Log.e(logTag, "AsyncTeamList T7 JSON Exception, uid="+uid);
                    Toast.makeText(getBaseContext(), "*T7 Bad JSON", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
            }
            //caseLV.setAdapter(new CaseListAdapter(getBaseContext(), castList));
            //prgDialog.dismiss();
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
                is.close();
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                Log.e(logTag, "AsyncTeamList T1 MalformedURLException, uid="+uid);
                return ("*T1 Invalid URL");
            } catch (UnknownHostException e) {
                Log.e(logTag, "AsyncTeamList T2 UnknownHostException, uid="+uid);
                return ("*T2 Server Unreachable");
            } catch (ConnectTimeoutException e) {
                Log.e(logTag, "AsyncTeamList T3 ConnectTimeoutException, uid="+uid);
                return ("*T3 Connection Timeout");
            } catch (SocketTimeoutException e) {
                Log.e(logTag, "AsyncTeamList T4 SocketTimeoutException, uid="+uid);
                return ("*T4 Bad Internet");
            } catch (ConnectException e) {
                Log.w(logTag, "AsyncTeamList T5 ConnectExcpetion, uid="+uid);
                return ("*T5 Server down");
            } catch (IOException e) {
                Log.e(logTag, "AsyncTeamList T6 IOException, uid="+uid);
                return ("*T6 Bad Response");
            }

            return response.toString();
        }
    }
}