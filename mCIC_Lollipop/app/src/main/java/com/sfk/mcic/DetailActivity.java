package com.sfk.mcic;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.DatePicker;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.sfk.mcic.Util.Params;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

//import com.google.android.gms.appindexing.Action;
//import com.google.android.gms.appindexing.AppIndex;
//import com.google.android.gms.appindexing.Thing;
//import com.google.firebase.appindexing.Action;
//import com.google.firebase.appindexing.FirebaseAppIndex;
//import com.google.firebase.appindexing.FirebaseUserActions;
//import com.google.firebase.appindexing.Indexable;
//import com.google.firebase.appindexing.builders.Actions;
//import android.os.Bundle;

public class DetailActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private ImageButton ibtnBeforeCam, ibtnAfterCam, ibtnBeforeFolder, ibtnAfterFolder, ibtnBeforeDel, ibtnAfterDel;
    private TextView tvCaseno, tvDistrictSt, tvDetail, tvCaseDate, tvInspPhotoCounter, tvCompPhotoCounter, tvACompDate;
    private Gallery gaInspPhoto, gaCompPhoto;
    private ImageView leftArrowImageView, rightArrowImageView, leftArrowImageView2, rightArrowImageView2, iv1;
    private int selectedImagePosition = 0, selectedImagePosition2 = 0, inspphoto, compphoto;
    private List<Drawable> drawables, drawables2;
    private List<String> drawablesID1, drawablesID2;
    private GalleryImageAdapter galImageAdapter;
    private String servletIP, servletPort, serviceName, emmsIP;
    private String csid;
    private String mCurrentPhotoPath;

    public static final int UPLOAD_BEFORE_PHOTO = 100;
    public static final int UPLOAD_AFTER_PHOTO = 101;
    public static final int TAKE_BEFORE_PHOTO = 102;
    public static final int TAKE_AFTER_PHOTO = 103;
    private int jpg_compress_ratio;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String ACCESS_CAMERA = Manifest.permission.CAMERA;
    private final String logTag = "DetailActivity";

    int aCompDay, aCompMonth, aCompYear, aCompHour, aCompMinute;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    //private GoogleApiClient client;

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        aCompDay = dayOfMonth;
        aCompMonth = month;
        aCompYear = year;
        TimePickerDialog timePickerDialog = new TimePickerDialog(DetailActivity.this, DetailActivity.this, aCompHour, aCompMinute, true);
        timePickerDialog.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        aCompHour = hourOfDay;
        aCompMinute = minute;
        Calendar myCal = Calendar.getInstance();
        myCal.set(Calendar.YEAR, aCompYear);
        myCal.set(Calendar.MONTH, aCompMonth);
        myCal.set(Calendar.DAY_OF_MONTH, aCompDay);
        myCal.set(Calendar.HOUR_OF_DAY, aCompHour);
        myCal.set(Calendar.MINUTE, aCompMinute);
        Date date = myCal.getTime();
        //Toast.makeText(DetailActivity.this, ""+aCompDay+"-"+aCompMonth+"-"+aCompYear+" "+aCompHour+":"+aCompMinute, Toast.LENGTH_LONG).show();
        SimpleDateFormat sdfDestination = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.ENGLISH);
        tvACompDate.setTextColor(getResources().getColor(R.color.black));
        tvACompDate.setText(sdfDestination.format(date));
        new AsyncUpdateAcompDate().execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        servletIP = getResources().getString(R.string.servletIP);
        servletPort = getResources().getString(R.string.servletPort);
        serviceName = getResources().getString(R.string.serviceName);
        emmsIP = getResources().getString(R.string.emmsIP);
        try {
            jpg_compress_ratio = Integer.parseInt(getResources().getString(R.string.jpg_compress_ratio));
        } catch ( Exception e) {
            jpg_compress_ratio = 0;
        }
        ibtnBeforeCam = (ImageButton) this.findViewById(R.id.ibtnBeforeCam);
        ibtnAfterCam = (ImageButton) this.findViewById(R.id.ibtnAfterCam);
        tvCaseno = (TextView) this.findViewById(R.id.tvCaseno);
        tvDistrictSt = (TextView) this.findViewById(R.id.tvDistrictStreet);
        tvDetail = (TextView) this.findViewById(R.id.tvDetail);
        tvCaseDate = (TextView) this.findViewById(R.id.tvCaseDate);
        tvACompDate = (TextView) this.findViewById(R.id.tvACompDate);

        gaInspPhoto = (Gallery) this.findViewById(R.id.gaInspPhoto);
        gaCompPhoto = (Gallery) this.findViewById(R.id.gaCompPhoto);
        leftArrowImageView = (ImageView) findViewById(R.id.left_arrow_imageview);
        rightArrowImageView = (ImageView) findViewById(R.id.right_arrow_imageview);
        leftArrowImageView2 = (ImageView) findViewById(R.id.left_arrow_imageview2);
        rightArrowImageView2 = (ImageView) findViewById(R.id.right_arrow_imageview2);
        ibtnBeforeFolder = (ImageButton) this.findViewById(R.id.ibtnBeforeFolder);
        ibtnAfterFolder = (ImageButton) this.findViewById(R.id.ibtnAfterFolder);
        iv1 = (ImageView) this.findViewById(R.id.imageView11);
        ibtnBeforeDel = (ImageButton) this.findViewById(R.id.ibtnBeforeDel);
        ibtnAfterDel = (ImageButton) this.findViewById(R.id.ibtnAfterDel);
        tvInspPhotoCounter = (TextView) this.findViewById(R.id.tvInspPhotoCounter);
        tvCompPhotoCounter = (TextView) this.findViewById(R.id.tvCompPhotoCounter);

        Intent i1 = getIntent();
        csid = i1.getStringExtra("csid").toString();
        //tvCaseno.setText(i1.getStringExtra("caseno"));
        //tvDistrictSt.setText(i1.getStringExtra("district") + " - " + i1.getStringExtra("street"));
        //tvDetail.setText(i1.getStringExtra("detail"));
        //tvCaseDate.setText(i1.getStringExtra("casedate"));
        //inspphoto = Integer.parseInt(i1.getStringExtra("inspphoto"));
        //compphoto = Integer.parseInt(i1.getStringExtra("compphoto"));
        String path = "http://"+servletIP+":"+servletPort+"/"+serviceName+"/GetCICCase?id="+csid;
        Log.w(logTag, path);

        tvACompDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int day, month, year, hour, minute;
                //Calendar calendar = Calendar.getInstance();
                //year = calendar.get(Calendar.YEAR);
                //month = calendar.get(Calendar.MONTH);
                //day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(DetailActivity.this, DetailActivity.this, aCompYear, aCompMonth, aCompDay);
                datePickerDialog.show();
            }
        });

        ibtnBeforeCam.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
                //startActivityForResult(takePictureIntent, take_before_photo);

                requestCameraPermission();
                //Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                //    startActivityForResult(takePictureIntent, TAKE_BEFORE_PHOTO);
                // }

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(DetailActivity.super.getApplicationContext(),
                                "fileprovider_auth_string",
                                photoFile);
                        Log.w(logTag, "photoURI = "+photoURI);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, TAKE_BEFORE_PHOTO);
                    }
                }
            }
        });


        ibtnAfterCam.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                requestCameraPermission();
                //Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //startActivityForResult(takePictureIntent, TAKE_AFTER_PHOTO);

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(DetailActivity.super.getApplicationContext(),
                                "fileprovider_auth_string",
                                photoFile);
                        Log.w(logTag, "photoURI = "+photoURI);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, TAKE_AFTER_PHOTO);
                    }
                }
            }
        });

        ibtnBeforeFolder.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //Intent intent = new Intent();
                //intent.setType("image/*");
                //intent.setAction(Intent.ACTION_GET_CONTENT);
                //startActivityForResult(Intent.createChooser(intent, "Select Before Rect. Photo"), take_before_photo);

                requestExternalPermission();
                Intent i = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        i.setType("image/*");
                //startActivityForResult(Intent.createChooser(i, "Select Before Photo"), UPLOAD_BEFORE_PHOTO);
                startActivityForResult(i, UPLOAD_BEFORE_PHOTO);
            }
        });

        ibtnAfterFolder.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //Intent intent = new Intent();
                //intent.setType("image/*");
                //intent.setAction(Intent.ACTION_GET_CONTENT);
                //startActivityForResult(Intent.createChooser(intent, "Select After Rect. Photo"), take_after_photo);

                requestExternalPermission();
                Intent i = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        i.setType("image/*");
                //startActivityForResult(Intent.createChooser(i, "Select After Photo"), UPLOAD_AFTER_PHOTO);
                startActivityForResult(i, UPLOAD_AFTER_PHOTO);
            }
        });

        leftArrowImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedImagePosition > 0) --selectedImagePosition;
                gaInspPhoto.setSelection(selectedImagePosition, false);
                tvInspPhotoCounter.setText(""+(selectedImagePosition+1)+"/"+drawablesID1.size());
            }
        });

        rightArrowImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedImagePosition < drawables.size() - 1) ++selectedImagePosition;
                gaInspPhoto.setSelection(selectedImagePosition, false);
                tvInspPhotoCounter.setText(""+(selectedImagePosition+1)+"/"+drawablesID1.size());
            }
        });

        leftArrowImageView2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedImagePosition2 > 0) --selectedImagePosition2;
                gaCompPhoto.setSelection(selectedImagePosition2, false);
                tvCompPhotoCounter.setText(""+(selectedImagePosition2+1)+"/"+drawablesID2.size());
            }
        });

        rightArrowImageView2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedImagePosition2 < drawables2.size() - 1) ++selectedImagePosition2;
                gaCompPhoto.setSelection(selectedImagePosition2, false);
                tvCompPhotoCounter.setText(""+(selectedImagePosition2+1)+"/"+drawablesID2.size());
            }
        });

        gaInspPhoto.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                selectedImagePosition = arg2;

                if (selectedImagePosition > 0 && selectedImagePosition < drawables.size() - 1) {
                    leftArrowImageView.setImageDrawable(getResources().getDrawable(R.drawable.arrow_left_enabled));
                    rightArrowImageView.setImageDrawable(getResources().getDrawable(R.drawable.arrow_right_enabled));
                } else if (selectedImagePosition == 0) {
                    leftArrowImageView.setImageDrawable(getResources().getDrawable(R.drawable.arrow_left_disabled));
                    if (drawables.size() > 1)
                        rightArrowImageView.setImageDrawable(getResources().getDrawable(R.drawable.arrow_right_enabled));
                } else if (selectedImagePosition == drawables.size() - 1) {
                    rightArrowImageView.setImageDrawable(getResources().getDrawable(R.drawable.arrow_right_disabled));
                    if (drawables.size() > 1)
                        leftArrowImageView.setImageDrawable(getResources().getDrawable(R.drawable.arrow_left_enabled));
                }

                changeBorderForSelectedImage(selectedImagePosition);
                tvInspPhotoCounter.setText(""+(selectedImagePosition+1)+"/"+drawablesID1.size());
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

        gaInspPhoto.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if (drawablesID1.size() > 0) {
                    Intent i1 = new Intent(getBaseContext(), ShowsinglephotoActivity.class);
                    i1.putExtra("BRPhotoID", drawablesID1.get(arg2));
                    i1.putExtra("csid", csid);
                    startActivity(i1);
                }
            }
        });

        gaCompPhoto.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                selectedImagePosition2 = arg2;

                if (selectedImagePosition2 > 0 && selectedImagePosition2 < drawables2.size() - 1) {
                    leftArrowImageView2.setImageDrawable(getResources().getDrawable(R.drawable.arrow_left_enabled));
                    rightArrowImageView2.setImageDrawable(getResources().getDrawable(R.drawable.arrow_right_enabled));
                } else if (selectedImagePosition2 == 0) {
                    leftArrowImageView2.setImageDrawable(getResources().getDrawable(R.drawable.arrow_left_disabled));
                    if (drawables2.size() > 1)
                        rightArrowImageView2.setImageDrawable(getResources().getDrawable(R.drawable.arrow_right_enabled));
                } else if (selectedImagePosition2 == drawables2.size() - 1) {
                    rightArrowImageView2.setImageDrawable(getResources().getDrawable(R.drawable.arrow_right_disabled));
                    if (drawables2.size() > 1)
                        leftArrowImageView2.setImageDrawable(getResources().getDrawable(R.drawable.arrow_left_enabled));
                }

                changeBorderForSelectedImage2(selectedImagePosition2);
                tvCompPhotoCounter.setText(""+(selectedImagePosition2+1)+"/"+drawablesID2.size());
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        gaCompPhoto.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if (drawablesID2.size() > 0) {
                    Intent i1 = new Intent(getBaseContext(), ShowsinglephotoActivity.class);
                    i1.putExtra("ARPhotoID", drawablesID2.get(arg2));
                    i1.putExtra("csid", csid);
                    startActivity(i1);
                }
            }
        });

        ibtnBeforeDel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Builder builder = new Builder(DetailActivity.this);
                builder.setTitle("Before Rectification Photo");
                builder.setMessage("Delete this photo?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new AsyncDeletePhoto(-1, drawablesID1.get(selectedImagePosition)).execute();
                        drawables.remove(selectedImagePosition);
                        drawablesID1.remove(selectedImagePosition);
                        selectedImagePosition = 0;
                        if(drawables.size() == 0) drawables.add(getResources().getDrawable(R.drawable.no_before_photo));
                        gaInspPhoto.refreshDrawableState();
                        new AsyncDownloadDrawables(-1).onPostExecute("");
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(DetailActivity.this, "Cancel Deletion", Toast.LENGTH_SHORT).show();
                    }
                });
                AlertDialog alertDialog = builder.create();

                if (drawablesID1.size() > 0)
                    alertDialog.show();
            }
        });

        ibtnAfterDel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Builder builder = new Builder(DetailActivity.this);
                builder.setTitle("After Rectification Photo");
                builder.setMessage("Delete this photo?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new AsyncDeletePhoto(1, drawablesID2.get(selectedImagePosition2)).execute();
                        drawables2.remove(selectedImagePosition2);
                        drawablesID2.remove(selectedImagePosition2);
                        selectedImagePosition2 = 0;
                        if(drawables2.size() == 0) drawables2.add(getResources().getDrawable(R.drawable.no_after_photo));
                        gaCompPhoto.refreshDrawableState();
                        new AsyncDownloadDrawables(1).onPostExecute("");
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(DetailActivity.this, "Cancel Deletion", Toast.LENGTH_SHORT).show();
                    }
                });
                AlertDialog alertDialog = builder.create();

                if (drawablesID2.size() > 0)
                    alertDialog.show();
            }
        });

        new AsyncDownloadCase().execute(path);
        new AsyncDownloadDrawables(0).execute();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        String urlString = "http://" + servletIP + ":" + servletPort + "/" + serviceName + "/ReceiveCICPhoto";

        if ((requestCode == UPLOAD_BEFORE_PHOTO || requestCode == UPLOAD_AFTER_PHOTO
                || requestCode == TAKE_BEFORE_PHOTO || requestCode == TAKE_AFTER_PHOTO)
                && resultCode == Activity.RESULT_OK) {

            String selectedPath = "";

            if (requestCode == UPLOAD_BEFORE_PHOTO || requestCode == UPLOAD_AFTER_PHOTO) {
                Uri selectedImageUri = data.getData();
                String[] projection = {MediaStore.Images.Media.DATA};

                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImageUri,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                selectedPath = cursor.getString(columnIndex);
                cursor.close();

                /*Cursor cursor = getContentResolver().query(selectedImageUri, projection, null, null, null);
                if(cursor.moveToFirst()){;
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                selectedPath = cursor.getString(column_index);
                }*/

                /*
                Cursor cursor = managedQuery(selectedImageUri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                String selectedPath = cursor.getString(column_index);
                */
            } else if (requestCode == TAKE_BEFORE_PHOTO){
                selectedPath = mCurrentPhotoPath;
                requestCode = UPLOAD_BEFORE_PHOTO;
            } else {
                selectedPath = mCurrentPhotoPath;
                requestCode = UPLOAD_AFTER_PHOTO;
            }

            Log.w(logTag, "selectedPath = "+selectedPath);
            Log.w(logTag, "urlString = "+urlString);
            Log.w(logTag, "csid = "+csid);
            Log.w(logTag, "requestCode = "+requestCode);
            new AsyncUploadImage().execute(selectedPath, urlString, csid, "" + requestCode, "" + jpg_compress_ratio);

            if (requestCode == UPLOAD_BEFORE_PHOTO)
                new AsyncDownloadDrawables(-1).execute();
            else
                new AsyncDownloadDrawables(1).execute();

        } else {
                Toast.makeText(this, "No Photo is Taken", Toast.LENGTH_SHORT).show();
        }
    }

    private void changeBorderForSelectedImage(int selectedItemPos) {

        int count = gaInspPhoto.getChildCount();
        for (int i = 0; i < count; i++) {
            ImageView imageView = (ImageView) gaInspPhoto.getChildAt(i);
            imageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.image_border));
            imageView.setPadding(3, 3, 3, 3);
        }
        ImageView imageView = (ImageView) gaInspPhoto.getSelectedView();
        imageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.selected_image_border));
        imageView.setPadding(3, 3, 3, 3);
    }

    private void changeBorderForSelectedImage2(int selectedItemPos) {

        int count = gaCompPhoto.getChildCount();
        for (int i = 0; i < count; i++) {
            ImageView imageView = (ImageView) gaCompPhoto.getChildAt(i);
            imageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.image_border));
            imageView.setPadding(3, 3, 3, 3);
        }
        ImageView imageView = (ImageView) gaCompPhoto.getSelectedView();
        imageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.selected_image_border));
        imageView.setPadding(3, 3, 3, 3);
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        // Dismiss the progress bar when application is closed
        //if (mDialog != null) {
        //    mDialog.dismiss();
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
                mDialog.setMessage("...");
                mDialog.show();
                return mDialog;
            default:
                return null;
        }
    }*/

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    /*public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Detail Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }*/

    /*public Action getAction() {
        //return Actions.newView(mText, mUrl);
        return Actions.newView("Detail Page", Uri.parse("http://[ENTER-YOUR-URL-HERE]"));
    }*/

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client.connect();
        //AppIndex.AppIndexApi.start(client, getIndexApiAction());
        /* If you’re logging an action on an item that has already been added to the index,
   you don’t have to add the following update line. See
   https://firebase.google.com/docs/app-indexing/android/personal-content#update-the-index for
   adding content to the index */

        //FirebaseAppIndex.getInstance().update(getIndexable());
        //FirebaseUserActions.getInstance().start(getAction());
    }

    @Override
    public void onStop() {
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //AppIndex.AppIndexApi.end(client, getIndexApiAction());
        //client.disconnect();

        //FirebaseUserActions.getInstance().end(getAction());

        super.onStop();
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.w(logTag, "mCurrentPhotoPath = "+image.getAbsolutePath());
        return image;
    }

    private boolean checkPermission() {

        return ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, ACCESS_CAMERA) != PackageManager.PERMISSION_GRANTED
                ;
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_CAMERA) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(DetailActivity.this, new String[]{ACCESS_CAMERA, WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    private void requestExternalPermission() {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(DetailActivity.this, new String[]{WRITE_EXTERNAL_STORAGE,
                    READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }


    private void requestPermissionAndContinue() {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, ACCESS_CAMERA) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_CAMERA)) {
                Builder alertBuilder = new Builder(this);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle(getString(R.string.permission_necessary));
                alertBuilder.setMessage(R.string.storage_permission_is_encessary_to_wrote_event);
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(DetailActivity.this, new String[]{WRITE_EXTERNAL_STORAGE
                                , READ_EXTERNAL_STORAGE, ACCESS_CAMERA}, PERMISSION_REQUEST_CODE);
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();
                Log.e("", "permission denied, show dialog");
            } else {
                ActivityCompat.requestPermissions(DetailActivity.this, new String[]{WRITE_EXTERNAL_STORAGE,
                        READ_EXTERNAL_STORAGE, ACCESS_CAMERA}, PERMISSION_REQUEST_CODE);
            }
        } else {
            //openActivity();
        }
    }

    class AsyncDownloadDrawables extends AsyncTask<String, Integer, String> {

        private int type; // 0=both, -1=before, 1=after
        private int progress;
        private ProgressDialog mDialog;

        public AsyncDownloadDrawables(int type) {
            this.type = type;
        }

        protected void onPreExecute() {
            super.onPreExecute();

            if (type <= 0) {
                // BEFORE_RECTIFICATION_PHOTO
                drawables = new ArrayList<Drawable>();
                drawablesID1 = new ArrayList<String>();
            }

            if (type >= 0) {
                // AFTER_RECTIFICATION_PHOTO
                drawables2 = new ArrayList<Drawable>();
                drawablesID2 = new ArrayList<String>();
            }

            mDialog = new ProgressDialog(DetailActivity.this);
            mDialog.setTitle("Uploading Photo");
            mDialog.setMessage("Please Wait ...");
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mDialog.setProgress(0);
            mDialog.setMax(100);
            mDialog.setIcon(R.drawable.download_icon);
            mDialog.setIndeterminate(false);
            mDialog.setCancelable(false);
            /*mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    // TODO Auto-generated method stub
                    Log.d("GettingCancelled","onCancel(DialogInterface dialog)");
                    asyncDownloadDrables.cancel(true);
                    DetailActivity.this.finish();
                }
            });*/
            /*mDialog.setButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i)
                    {
                        dialog.cancel();
                        Intent i1 = new Intent(DetailActivity.this, ListActivity.class);
                        startActivity(i1);
                    }
                });*/
            mDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {
            mDialog.setProgress(progress[0]);
            if (progress[1]==1) mDialog.setTitle("Requesting Before Photos");
            else if (progress[1]==2) {mDialog.setTitle("Downloading Before Photos");}
            else if (progress[1]==3) {mDialog.setTitle("Requesting After Photos");}
            else if (progress[1]==4) {mDialog.setTitle("Downloading Before Photos");}
        }

        protected void onPostExecute(String result) {
            if (!result.startsWith("*")) {
                if (type <= 0) {
                    // BEFORE_RECTIFICATION_PHOTO
                    galImageAdapter = new GalleryImageAdapter(getBaseContext(), drawables);
                    gaInspPhoto.setAdapter(galImageAdapter);
                    if (drawables.size() > 0) {
                        gaInspPhoto.setSelection(selectedImagePosition, false);
                        tvInspPhotoCounter.setText("" + (selectedImagePosition + 1) + "/" + drawablesID1.size());
                    }
                    if (drawables.size() == 1)
                        rightArrowImageView.setImageDrawable(getResources().getDrawable(R.drawable.arrow_right_disabled));
                }
                if (type >= 0) {
                    // AFTER_RECTIFICATION_PHOTO
                    galImageAdapter = new GalleryImageAdapter(getBaseContext(), drawables2);
                    gaCompPhoto.setAdapter(galImageAdapter);
                    if (drawables2.size() > 0) {
                        gaCompPhoto.setSelection(selectedImagePosition2, false);
                        tvCompPhotoCounter.setText("" + (selectedImagePosition2 + 1) + "/" + drawablesID2.size());
                    }
                    if (drawables2.size() == 1)
                        rightArrowImageView2.setImageDrawable(getResources().getDrawable(R.drawable.arrow_right_disabled));
                }
            } else {
                Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
            }
            if (mDialog != null) mDialog.dismiss();
        }

        protected String doInBackground(String... path) {
            try {
                String uri;
                URL url;
                URLConnection con;
                InputStream is;
                InputStreamReader reader;
                BufferedReader breader;
                StringBuffer response;
                String line;
                JSONObject jobj;
                JSONArray jarray;
                Bitmap bmp;

                if (type <= 0) {
                    // BEFORE_RECTIFICATION_PHOTO
                    publishProgress(progress+=2, 1);
                    uri = "http://" + servletIP + ":" + servletPort + "/" + serviceName + "/GetCICPhotoList?type=insp&id=" + csid;
                    Log.w(logTag, "GetCICPhotoList(Before) - " + uri);
                    url = new URL(uri);
                    con = url.openConnection();
                    con.setConnectTimeout(Params.connectiontimeout);
                    con.setReadTimeout(Params.conreadtimeout);
                    is = con.getInputStream();
                    reader = new InputStreamReader(is);
                    breader = new BufferedReader(reader);
                    response = new StringBuffer();
                    line = "";
                    while ((line = breader.readLine()) != null) {
                        publishProgress(progress+=2, 1);
                        response.append(line + "\n");
                    }
                    breader.close();
                    is.close();
                    jarray = new JSONArray(response.toString());

                    if (jarray.length() == 0) {
                        drawables.add(getResources().getDrawable(R.drawable.no_before_photo));
                    } else {
                        for (int i = 0; i < jarray.length(); i++) {
                            jobj = jarray.getJSONObject(i);
                            uri = "http://" + emmsIP + "/emmsweb/openFile?type=beforeRectificationPhoto&width=300&id=" + jobj.get("photoId");
                            Log.w(logTag, "GetBeforePhotos - " + uri);
                            url = new URL(uri);
                            con = url.openConnection();
                            is = con.getInputStream();
                            bmp = BitmapFactory.decodeStream(is);
                            publishProgress(progress+=2, 2);
                            if (bmp != null) {
                                drawables.add(new BitmapDrawable(getResources(), bmp));
                                drawablesID1.add(jobj.get("photoId").toString());
                            }
                        }
                    }
                }

                if (type >= 0) {
                    // AFTER_RECTIFICATION_PHOTO
                    publishProgress(progress+=2, 3);
                    uri = "http://" + servletIP + ":" + servletPort + "/" + serviceName + "/GetCICPhotoList?type=comp&id=" + csid;
                    Log.w(logTag, "GetCICPhotoList(After) - " + uri);
                    url = new URL(uri);
                    con = url.openConnection();
                    is = con.getInputStream();
                    con.setConnectTimeout(Params.connectiontimeout);
                    con.setReadTimeout(Params.conreadtimeout);
                    reader = new InputStreamReader(is);
                    breader = new BufferedReader(reader);
                    response = new StringBuffer();
                    line = "";
                    while ((line = breader.readLine()) != null) {
                        publishProgress(progress+=2, 3);
                        response.append(line + "\n");
                    }
                    breader.close();
                    is.close();
                    jarray = new JSONArray(response.toString());

                    if (jarray.length() == 0) {
                        drawables2.add(getResources().getDrawable(R.drawable.no_after_photo));
                    } else {
                        for (int i = 0; i < jarray.length(); i++) {
                            jobj = jarray.getJSONObject(i);
                            uri = "http://" + emmsIP + "/emmsweb/openFile?type=afterRectificationPhoto&width=300&id=" + jobj.get("photoId");
                            Log.w(logTag, "GetAfterPhotos - " + uri);
                            url = new URL(uri);
                            con = url.openConnection();
                            is = con.getInputStream();
                            bmp = BitmapFactory.decodeStream(is);
                            publishProgress(progress+=2, 4);
                            if (bmp != null) {
                                drawables2.add(new BitmapDrawable(getResources(), bmp));
                                drawablesID2.add(jobj.get("photoId").toString());
                            }
                        }
                    }
                }
            } catch (MalformedURLException e) {
                Log.e(logTag, "AsyncDownloadDrawables W1 MalformedURLException, csid="+csid);
                return"*W1 Invalid URL";
            } catch (UnknownHostException e) {
                Log.e(logTag, "AsyncDownloadDrawables W2 UnknownHostException, csid="+csid);
                return"*W2 Server Unreachable";
            } catch (ConnectTimeoutException e) {
                Log.e(logTag, "AsyncDownloadDrawables W3 ConnectTimeoutException, csid="+csid);
                return"*W3 Connection Timeout";
            } catch (SocketTimeoutException e) {
                Log.e(logTag, "AsyncDownloadDrawables W4 SocketTimeoutException, csid="+csid);
                return"*W4 Bad Internet";
            } catch (ConnectException e) {
                Log.w(logTag, "AsyncDownloadDrawables W5 ConnectException, csid="+csid);
                return"*W5 Server down";
            } catch (IOException e) {
                Log.e(logTag, "AsyncDownloadDrawables W6 IOException, csid=" + csid);
                return"*W6 Bad Response";
            } catch (JSONException e) {
                Log.e(logTag, "AsyncDownloadDrawables W6 JSON Exception, csid="+csid);
                return"*W7 Bad Response";
            }
            return "ok";
        }
    }

    class AsyncDeletePhoto extends AsyncTask<String, Integer, String> {

        private String drawableID;
        private int type; // 0=both, -1=before, 1=after
        private String deleteType;
        private String uri;
        private ProgressDialog mDialog;

        public AsyncDeletePhoto(int type, String drawableID) {
            this.type = type;
            this.drawableID = drawableID;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            //showDialog(progress_bar_deletephoto);

            deleteType = type == -1 ? "before" : "after";
            uri = "http://" + servletIP + ":" + servletPort + "/" + serviceName + "/DeleteCICPhoto?type="+deleteType+"&photoid=" + drawableID;
            Log.w(logTag, uri);

            /*mDialog = new ProgressDialog(DetailActivity.this);
            mDialog.setTitle("Deleting Photo");
            mDialog.setMessage("Please Wait ...");
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mDialog.setIcon(R.drawable.file_delete);
            //mDialog.setProgress(0);
            //mDialog.setMax(100);
            mDialog.setIndeterminate(false);
            mDialog.setCancelable(false);
            mDialog.show();*/
        }

        protected void onProgressUpdate(Integer... progress) {
            // TODO Auto-generated method stub
            //mDialog.setProgress(progress[0]);
        }

        protected void onPostExecute(String result) {
            if (!result.startsWith("*")) {
            }
            Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
            //if (mDialog !=null ) mDialog.dismiss();
        }

        protected String doInBackground(String... path) {
            // TODO Auto-generated method stub

            StringBuffer response = new StringBuffer();
            URL url;
            try {
                url = new URL(uri);
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
                Log.e(logTag, "AsyncCaseList Z1 MalformedURLException, type="+deleteType+"&photoid=" + drawableID);
                return ("*Z1 Invalid URL");
            } catch (UnknownHostException e) {
                Log.e(logTag, "AsyncCaseList Z2 UnknownHostException, type="+deleteType+"&photoid=" + drawableID);
                return ("*Z2 Server Unreachable");
            } catch (ConnectTimeoutException e) {
                Log.e(logTag, "AsyncCaseList Z3 ConnectTimeoutException, type="+deleteType+"&photoid=" + drawableID);
                return ("*Z3 Connection Timeout");
            } catch (SocketTimeoutException e) {
                Log.e(logTag, "AsyncCaseList Z4 SocketTimeoutException, type="+deleteType+"&photoid=" + drawableID);
                return ("*Z4 Bad Internet");
            } catch (ConnectException e) {
                Log.w(logTag, "AsyncCaseList Z5 ConnectException, type="+deleteType+"&photoid=" + drawableID);
                return ("*Z5 Server down");
            } catch (IOException e) {
                Log.e(logTag, "AsyncCaseList Z6 IOException, type="+deleteType+"&photoid=" + drawableID);
                return ("*Z6 Bad Response");
            }

            return response.toString();
        }
    }

    class AsyncUploadImage extends AsyncTask<String, Integer, String> {

        private String srcPath, svrPath, id, requestCode;
        private int compressRatio;
        private InputStream inputStream;
        private ProgressDialog uDialog;
        private int progress = 0;

        protected void onPreExecute() {
            super.onPreExecute();
            //showDialog(progress_bar_uploadphoto);
            uDialog = new ProgressDialog(DetailActivity.this);
            uDialog.setTitle("Uploading Photo");
            uDialog.setMessage("Please Wait ...");
            uDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            uDialog.setProgress(0);
            uDialog.setMax(100);
            uDialog.setIndeterminate(false);
            uDialog.setCancelable(false);
            uDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {
            uDialog.setProgress(progress[0]);
            if(progress[1]>0) uDialog.setTitle("Uploading Photo - "+progress[1]+"kb");

        }

        protected void onPostExecute(String result) {
            if (!result.startsWith("*")) {
                Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
            }
            if(uDialog!=null) uDialog.dismiss();

            //new AsyncDownloadDrawables(1).onPostExecute("Server: upload success");
        }

        protected String doInBackground(String... path) {

            this.srcPath = path[0];
            this.svrPath = path[1];
            this.id = path[2];
            this.requestCode = path[3];
            this.compressRatio = Integer.parseInt(path[4]);
            String the_string_response="";

            Bitmap bitmap;
            try {
                File file = new File(srcPath);
                bitmap = setOrientation(srcPath);
                //int bitmapByteCount= BitmapCompat.getAllocationByteCount(bitmap);
                //Bitmap.createScaleBitmap(Bitmap src, int destWidth, int destHeight, boolean filter)
                //String image_str = getStringImage(bitmap);


                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, compressRatio, baos);
                byte[] imageBytes = baos.toByteArray();
                Log.w(logTag, "image size: "+imageBytes.length/1024+"(k)");
                int imgSize = imageBytes.length/1024;
                //if (imgSize>2048) {
                //    Log.e(logTag, "AsyncUploadImage U6 ImageSizeTooLarge, csid="+csid+", size="+imgSize+"kb");
                //    return ("*U6 "+imgSize+"kb size too large");
                //}
                publishProgress(progress, imgSize);
                String image_str = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                //Log.w(logTag, ""+image_str.length());
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("image", image_str));
                nameValuePairs.add(new BasicNameValuePair("id", id));
                nameValuePairs.add(new BasicNameValuePair("requestCode", requestCode));

                HttpParams httpParameters = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParameters, Params.connectiontimeout);
                HttpConnectionParams.setSoTimeout(httpParameters, Params.sockettimeout);
                DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);
                HttpPost httppost = new HttpPost(svrPath);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                the_string_response = convertResponseToString(response);
                Log.w(logTag, "Response " + the_string_response);

            } catch (MalformedURLException e) {
                Log.e(logTag, "AsyncUploadImage U1 MalformedURLException, csid="+csid);
                return ("*U1 Invalid URL");
            } catch (UnknownHostException e) {
                Log.e(logTag, "AsyncUploadImage U2 UnknownHostException, csid="+csid);
                return ("*U2 Server Unreachable");
            } catch (ConnectTimeoutException e) {
                Log.e(logTag, "AsyncUploadImage U3 ConnectTimeoutException, csid="+csid);
                return ("*U3 Connection Timeout");
            } catch (SocketTimeoutException e) {
                Log.e(logTag, "AsyncUploadImage U4 SocketTimeoutException, csid="+csid);
                return ("*U4 Bad Internet");
            } catch (IOException e) {
                Log.e(logTag, "AsyncUploadImage U5 IOException, csid="+csid);
                return ("*U5 Bad Response");
            }
            return the_string_response;
        }

        public String getStringImage(Bitmap bmp) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, compressRatio, baos);
            byte[] imageBytes = baos.toByteArray();
            Log.w(logTag, "image size: "+imageBytes.length/1024+"(k)");
            String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            return encodedImage;
        }

        public String convertResponseToString(HttpResponse response) throws IllegalStateException, IOException {

            String res = "";
            StringBuffer buffer = new StringBuffer();
            inputStream = response.getEntity().getContent();
            int contentLength = (int) response.getEntity().getContentLength(); //getting content length…..

            Log.w(logTag, "contentLength : " + contentLength);

            if (contentLength < 0) {
            } else {
                byte[] data = new byte[512];
                int len = 0;
                try {
                    while (-1 != (len = inputStream.read(data))) {
                        buffer.append(new String(data, 0, len)); //converting to string and appending  to stringbuffer…..
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    inputStream.close(); // closing the stream…..
                } catch (IOException e) {
                    e.printStackTrace();
                }
                res = buffer.toString();     // converting stringbuffer to string…..

                //Toast.makeText(ctx, "Result : " + res, Toast.LENGTH_LONG).show();
            }
            return res;
        }

        public Bitmap setOrientation(String file) throws IOException {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file, opts);

            opts.inJustDecodeBounds = false;
            //opts.inSampleSize = getFitInSampleSize(1024, 800, opts);
            Bitmap bm = BitmapFactory.decodeFile(file, opts);
            ExifInterface exif = new ExifInterface(file);
            String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;

            int rotationAngle = 0;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;

            //int oldwidth = bm.getWidth();
            //int oldheight = bm.getHeight();

            Matrix matrix = new Matrix();
            //matrix.setRotate(rotationAngle, oldwidth / 2, oldheight / 2);
            matrix.preRotate(rotationAngle);

            //float scaleWidth = (float) oldwidth;
            //float scaleHeight = (float) oldheight;

            //if (oldwidth > oldheight) {
            //    scaleWidth = 1024f / oldwidth;
            //    scaleHeight = 800f / oldheight;
            //} else {
            //    scaleWidth = 800f / oldwidth;
            //    scaleHeight = 1024f / oldheight;
            //}
            //Log.w(logTag, ""+oldwidth+"/"+oldheight+"/"+scaleWidth+"/"+scaleHeight+"/"+(int)scaleWidth+"/"+(int)scaleHeight);
            //matrix.postScale(scaleWidth, scaleHeight);
            //Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
            //Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
            //rotatedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, 1024, 800, true);

            //return rotatedBitmap;
            return bm;
        }

        public int getFitInSampleSize(int reqWidth, int reqHeight, BitmapFactory.Options options) {
            int inSampleSize = 1;
            if (options.outWidth > reqWidth || options.outHeight > reqHeight) {
                int widthRatio = Math.round((float) options.outWidth / (float) reqWidth);
                int heightRatio = Math.round((float) options.outHeight / (float) reqHeight);
                inSampleSize = Math.min(widthRatio, heightRatio);
            }
            return inSampleSize;
        }

    }

    class AsyncDownloadCase extends AsyncTask<String, Integer, String> {

        private int progress;

        
        protected void onPreExecute() {
            super.onPreExecute();
            //showDialog(0);
            //mDialog.setMessage("Downloading Case ...");
        }

        protected void onProgressUpdate(Integer... progress) {
            // TODO Auto-generated method stub
            //mDialog.setProgress(progress[0]);
        }

        protected void onPostExecute(String result) {
            if (!result.startsWith("*")) {
                try {
                    JSONArray jarray = new JSONArray(result);
                    //Log.w(logTag, "refreshCaseList array.length() = "+jarray.length());
                    JSONObject jobj;
                    HashMap<String, Object> item;
                    //for (int i=0; i<jarray.length(); i++) {
                    jobj = jarray.getJSONObject(0);
                    //    item = new HashMap<String, Object>();
                    //    item.put("id", jobj.get("id"));
                    //    item.put("caseno", jobj.get("caseno"));
                    //    item.put("casedate", jobj.get("casedate"));
                    //    item.put("district",jobj.get("district"));
                    //    item.put("street", jobj.get("street"));
                    //    item.put("detail", jobj.get("detail"));
                    //    item.put("inspphoto", jobj.get("inspphoto"));
                    //    item.put("compphoto", jobj.get("compphoto"));
                    //    item.put("casetype", jobj.get("casetype"));
                    //    item.put("colorflag", jobj.get("colorflag"));
                    tvCaseno.setText(jobj.get("casetype").toString() + " " + jobj.get("caseno").toString());
                    tvDistrictSt.setText(jobj.get("district").toString() + " - " + jobj.get("street").toString());
                    tvDetail.setText(jobj.get("detail").toString());

                    try {
                        SimpleDateFormat sdfSource = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
                        Date date = sdfSource.parse(jobj.get("casedate").toString());

                        SimpleDateFormat sdfDestination = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.ENGLISH);
                        tvCaseDate.setText(sdfDestination.format(date));
                    } catch (Exception e) {
                        tvCaseDate.setText("invalid date");
                    }

                    try {
                        SimpleDateFormat sdfSource = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
                        Date date = sdfSource.parse(jobj.get("acompdate").toString());
                        SimpleDateFormat sdfDestination = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.ENGLISH);
                        tvACompDate.setText(sdfDestination.format(date));

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        aCompDay = calendar.get(Calendar.DAY_OF_MONTH);
                        aCompMonth = calendar.get(Calendar.MONTH);
                        aCompYear = calendar.get(Calendar.YEAR);
                        aCompHour = calendar.get(Calendar.HOUR_OF_DAY);
                        aCompMinute = calendar.get(Calendar.MINUTE);
                    } catch (Exception e) {
                        tvACompDate.setTextColor(getResources().getColor(R.color.colorAccent));
                        tvACompDate.setText("click & select");
                        Calendar calendar = Calendar.getInstance();
                        aCompDay = calendar.get(Calendar.DAY_OF_MONTH);
                        aCompMonth = calendar.get(Calendar.MONTH);
                        aCompYear = calendar.get(Calendar.YEAR);
                        aCompHour = calendar.get(Calendar.HOUR_OF_DAY);
                        aCompMinute = calendar.get(Calendar.MINUTE);
                    }

                    //tvCaseDate.setText(jobj.get("casedate").toString());
                    inspphoto = Integer.parseInt(jobj.get("inspphoto").toString());
                    compphoto = Integer.parseInt(jobj.get("compphoto").toString());
                    //}
                } catch (JSONException e) {
                    Log.e(logTag, "AsyncDownloadCase C7 JSON Exception, csid="+csid);
                    Toast.makeText(getBaseContext(), "*C7 Bad JSON", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
            }
            //if(mDialog!=null) mDialog.dismiss();
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
                Log.e(logTag, "AsyncDownloadCase C1 MalformedURLException, csid="+csid);
                return ("*C1 Invalid URL");
            } catch (UnknownHostException e) {
                Log.e(logTag, "AsyncDownloadCase C2 UnknownHostException, csid="+csid);
                return ("*C2 Server Unreachable");
            } catch (ConnectTimeoutException e) {
                Log.e(logTag, "AsyncDownloadCase C3 ConnectTimeoutException, csid="+csid);
                return ("*C3 Connection Timeout");
            } catch (SocketTimeoutException e) {
                Log.e(logTag, "AsyncDownloadCase C4 SocketTimeoutException, csid="+csid);
                return ("*C4 Bad Internet");
            } catch (ConnectException e) {
                Log.w(logTag, "AsyncDownloadCase C5 ConnectException, csid="+csid);
                return ("*C5 Server down");
            } catch (IOException e) {
                Log.e(logTag, "AsyncDownloadCase C6 IOException, csid=" + csid);
                return ("*C6 Bad Response");
            }

            return response.toString();
        }
    }

    class AsyncUpdateAcompDate extends AsyncTask<String, Integer, String> {

        public AsyncUpdateAcompDate() {

        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected void onProgressUpdate(Integer... progress) {
            // TODO Auto-generated method stub
        }

        protected void onPostExecute(String result) {

            Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
            /*if (!result.startsWith("*")) {
                Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
            }*/
        }

        protected String doInBackground(String... path) {
            // TODO Auto-generated method stub

            StringBuffer response = new StringBuffer();
            URL url;
            String uri;
            try {
                uri = "http://"+servletIP+":"+servletPort+"/"+serviceName+"/UpdateAcompDate?id="+csid+"&yr="+aCompYear
                        +"&mth="+aCompMonth
                        +"&day="+aCompDay
                        +"&hr="+aCompHour
                        +"&min="+aCompMinute;
                url = new URL(uri);
                Log.w(logTag, "GetCICPhotoList(AsyncUpdateAcompDate) - " + uri);

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
                Log.e(logTag, "AsyncUpdateAcompDate T1 MalformedURLException, csid="+csid);
                return ("*T1 Invalid URL");
            } catch (UnknownHostException e) {
                Log.e(logTag, "AsyncUpdateAcompDate T2 UnknownHostException, csid="+csid);
                return ("*T2 Server Unreachable");
            } catch (ConnectTimeoutException e) {
                Log.e(logTag, "AsyncUpdateAcompDate T3 ConnectTimeoutException, csid="+csid);
                return ("*T3 Connection Timeout");
            } catch (SocketTimeoutException e) {
                Log.e(logTag, "AsyncUpdateAcompDate T4 SocketTimeoutException, csid="+csid);
                return ("*T4 Bad Internet");
            } catch (ConnectException e) {
                Log.w(logTag, "AsyncUpdateAcompDate T5 ConnectExcpetion, csid="+csid);
                return ("*T5 Server down");
            } catch (IOException e) {
                Log.e(logTag, "AsyncUpdateAcompDate T6 IOException, csid="+csid);
                return ("*T6 Bad Response");
            }

            return response.toString();
        }
    }
}