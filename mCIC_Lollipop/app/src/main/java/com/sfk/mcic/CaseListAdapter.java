package com.sfk.mcic;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.TimeUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CaseListAdapter extends BaseAdapter {

    private LayoutInflater myInflater;
    private ArrayList<HashMap> list;
    private String logTag;

    public CaseListAdapter(Context context, ArrayList<HashMap> list) {
        // TODO Auto-generated constructor stub
        myInflater = LayoutInflater.from(context);
        this.list = list;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return list.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return Long.valueOf(list.get(arg0).get("id").toString());
    }

    @Override
    public View getView(int arg0, View arg1, ViewGroup arg2) {
        // TODO Auto-generated method stub
        ViewTag viewTag;

        logTag = this.getClass().getName();

        if(arg1 == null){
            arg1 = myInflater.inflate(R.layout.caselistlayout, null);

            viewTag = new ViewTag(
                    (LinearLayout) arg1.findViewById(R.id.layout0),
                    (TextView) arg1.findViewById(R.id.caseno),
                    (ImageView) arg1.findViewById(R.id.distDot),
                    (TextView) arg1.findViewById(R.id.diststreet),
                    (TextView) arg1.findViewById(R.id.casedate),
                    (TextView) arg1.findViewById(R.id.detail)
            );

            arg1.setTag(viewTag);
        } else {
            viewTag = (ViewTag) arg1.getTag();
        }

        String colorFlag = list.get(arg0).get("colorflag").toString();
        if (colorFlag.equals("0")) {
            if ((arg0 % 2) == 0)
                viewTag.lLayout.setBackgroundColor(Color.parseColor("#E5FFCC"));
            else
                viewTag.lLayout.setBackgroundColor(Color.parseColor("#FFFFCC"));
        } else
            viewTag.lLayout.setBackgroundColor(Color.parseColor("#FF99C2"));

        viewTag.detailTV.setText(list.get(arg0).get("detail").toString());

        try {
            SimpleDateFormat sdfSource = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
            Date date = sdfSource.parse(list.get(arg0).get("casedate").toString());

            SimpleDateFormat sdfDestination = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.ENGLISH);
            viewTag.casedateTV.setText(sdfDestination.format(date));
        } catch (Exception e) {
            viewTag.casedateTV.setText("invalid date");
        }
        //viewTag.casedateTV.setText(list.get(arg0).get("casedate").toString());

        String dist2 = list.get(arg0).get("district").toString();
        String street2 = list.get(arg0).get("street").toString();
        switch(dist2) {
            case "KT": viewTag.streetTV.setText("KT - "+street2); break;
            case "KB": viewTag.streetTV.setText("KB - "+street2); break;
            case "WTS": viewTag.streetTV.setText("WTS - "+street2); break;
            case "HH": viewTag.streetTV.setText("HH - "+street2); break;
            case "KC": viewTag.streetTV.setText("KC - "+street2); break;
            case "StrKC": viewTag.streetTV.setText("StrKC - "+street2); break;
            case "StrKE": viewTag.streetTV.setText("StrKE - "+street2); break;
            case "StrHN": viewTag.streetTV.setText("StrHN - "+street2); break;
            case "CHT": viewTag.streetTV.setText("CHT - "+street2); break;
            case "EHC": viewTag.streetTV.setText("EHC - "+street2); break;
            case "SLPKG1": viewTag.streetTV.setText("SLPKG1 - "+street2); break;
            case "TP": viewTag.streetTV.setText("TP - "+street2); break;
            default: viewTag.streetTV.setText("- "+street2);
        }

        int inspphoto = Integer.parseInt(list.get(arg0).get("inspphoto").toString());
        int compphoto = Integer.parseInt(list.get(arg0).get("compphoto").toString());
        if (inspphoto < 1  && compphoto < 1)
            //viewTag.distIV.setImageResource(R.drawable.dot_red);
            viewTag.distIV.setBackgroundColor(Color.RED);
        else if (inspphoto < 1)
            //viewTag.distIV.setImageResource(R.drawable.dot_orange);
            viewTag.distIV.setBackgroundColor(Color.rgb(255, 165, 0));
        else if (compphoto < 1)
            //viewTag.distIV.setImageResource(R.drawable.dot_green);
            viewTag.distIV.setBackgroundColor(Color.rgb(60, 179, 131));
        else
            //viewTag.distIV.setImageResource(R.drawable.tick);
            viewTag.distIV.setBackgroundColor(Color.BLACK);

        String caseDate = list.get(arg0).get("casedate").toString();
        String caseType = list.get(arg0).get("casetype").toString();

        if (street2.equals("")) viewTag.distIV.setVisibility(ImageView.INVISIBLE);
        viewTag.casenoTV.setText(caseType+" "+list.get(arg0).get("caseno").toString()+" ("+inspphoto+"/"+compphoto+")");

        return arg1;
    }

    public class ViewTag{
        TextView casenoTV;
        TextView streetTV;
        TextView casedateTV;
        TextView detailTV;
        ImageView distIV;
        LinearLayout lLayout;

        public ViewTag(LinearLayout lLayout, TextView casenoTV, ImageView distIV, TextView streetTV, TextView casedateTV, TextView detailTV){
            this.lLayout = lLayout;
            this.casenoTV = casenoTV;
            this.streetTV = streetTV;
            this.detailTV = detailTV;
            this.distIV = distIV;
            this.casedateTV = casedateTV;
        }
    }

}

