package com.sfk.mcic;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.sfk.mcic.Util.Params;

import org.apache.http.conn.ConnectTimeoutException;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

//import android.os.Bundle;

public class ShowsinglephotoActivity extends AppCompatActivity {

    private TouchImageView image;
    private Bitmap bmp;
    public static final int progress_bar_type = 0;
    private String emmsIP, BRPhotoID, ARPhotoID,csid;
    private final String logTag = "SinglePhoto";
    private AsyncTask asyncDownloadImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showsinglephoto);

        emmsIP = getResources().getString(R.string.emmsIP);

        image = (TouchImageView) findViewById(R.id.img);

        Intent i1 = getIntent();
        BRPhotoID = i1.getStringExtra("BRPhotoID");
        ARPhotoID = i1.getStringExtra("ARPhotoID");
        csid = i1.getStringExtra("csid");
        String path;
        if (ARPhotoID == null)
            path = "http://"+emmsIP+"/emmsweb/openFile?type=beforeRectificationPhoto&id="+BRPhotoID;
        else
            path = "http://"+emmsIP+"/emmsweb/openFile?type=afterRectificationPhoto&id="+ARPhotoID;

        Log.d(this.getClass().getName(), "GetBeforePhotos - "+path);
        asyncDownloadImage = new AsyncDownloadImage(this).execute(path);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        // Dismiss the progress bar when application is closed
    }

    class AsyncDownloadImage extends AsyncTask<String, Integer, String> {

        private int progress;
        private ProgressDialog mDialog;
        Context mContext;

        public AsyncDownloadImage(Context mContext) {
            this.mContext = mContext;
        }
        protected void onPreExecute() {
            super.onPreExecute();

            mDialog = new ProgressDialog(mContext);
            mDialog.setTitle("Please Wait ...");
            mDialog.setMessage("Please Wait ...");
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mDialog.setProgress(0);
            mDialog.setMax(100);
            mDialog.setIndeterminate(false);
            mDialog.setIcon(R.drawable.download_icon);
            mDialog.setCancelable(false);
            /*mDialog.setButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int i)
                {
                    dialog.cancel();
                    Intent i1 = new Intent(ShowsinglephotoActivity.this, DetailActivity.class);
                    i1.putExtra("csid", csid);
                    startActivity(i1);
                }
            });*/
            mDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {
            mDialog.setProgress(progress[0]);
            if (progress[1]==1) mDialog.setTitle("Requesting Photos");
            else if (progress[1]==2) {mDialog.setTitle("Downloading Photos");}
        }

        protected void onPostExecute(String result) {
            if (!result.startsWith("*")) {
                image.setImageBitmap(bmp);
            } else {
                Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
            }
            if (mDialog!=null) mDialog.dismiss();
        }

        protected String doInBackground(String... path) {
            publishProgress(progress, 1);
            URL url;
            try {
                url = new URL(path[0]);
                Log.w(logTag, "AsyncDownloadImage - " + url);
                URLConnection con = url.openConnection();
                con.setConnectTimeout(Params.connectiontimeout);
                con.setReadTimeout(Params.conreadtimeout);
                InputStream is = con.getInputStream();
                publishProgress(progress, 2);
                bmp = BitmapFactory.decodeStream(is);
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                Log.e(logTag, "AsyncDownloadImage S1 MalformedURLException, before id="+BRPhotoID+", after id="+ARPhotoID);
                return"*S1 Invalid URL";
            } catch (UnknownHostException e) {
                Log.e(logTag, "AsyncDownloadImage S2 UnknownHostException, before id="+BRPhotoID+", after id="+ARPhotoID);
                return"*S2 Server Unreachable";
            } catch (ConnectTimeoutException e) {
                Log.e(logTag, "AsyncDownloadImage S3 ConnectTimeoutException, before id="+BRPhotoID+", after id="+ARPhotoID);
                return"*S3 Connection Timeout";
            } catch (SocketTimeoutException e) {
                Log.e(logTag, "AsyncDownloadImage S4 SocketTimeoutException, before id="+BRPhotoID+", after id="+ARPhotoID);
                return"*S4 Bad Internet";
            } catch (ConnectException e) {
                Log.w(logTag, "AsyncCaseList S5 ConnectException, before id="+BRPhotoID+", after id="+ARPhotoID);
                return ("*S5 Server down");
            } catch (IOException e) {
                Log.e(logTag, "AsyncDownloadImage S6 W5 IO Exception, before id="+BRPhotoID+", after id="+ARPhotoID);
                return"*S6 Bad Response";
            }
            return "ok";
        }
    }
}
