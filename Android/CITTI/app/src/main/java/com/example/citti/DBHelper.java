package com.example.citti;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.androidplot.xy.XYPlot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class DBHelper extends AsyncTask<Void, Void, Integer> {
    //Prevent leak
    private WeakReference<Activity> mWeakActivity;
    private Activity mActivity;
    private Context mContext;
    private String mMode;


    public DBHelper(Activity activity, Context context, String mode) {
        mActivity = activity;
        mWeakActivity = new WeakReference<>(activity);
        mContext = context;
        mMode = mode;
    }

    protected BS getBS(int cellId, int pci, int tac, int mnc, String nt) {
        BS q = CellTable.getInstance(mContext).getBSDao().getBS(cellId,pci,tac,mnc,nt);
        return q;
    }


    @Override
    protected Integer doInBackground(Void... voids) {
        if(mMode=="init"){
            // Create a list of BS object from csv file
            List<BS> bs_list = new ArrayList<BS>();

            try {
                InputStreamReader is = new InputStreamReader(mContext.getAssets().open("celltable.csv"));
                BufferedReader reader = new BufferedReader(is);
                String line;
                while ((line = reader.readLine()) != null) {
                    BS bs = new BS();
                    String[] ss = line.replace("\"", "").split(",");
                    bs.setCell_id(Integer.parseInt(ss[1]));
                    bs.setPci(Integer.parseInt(ss[2]));
                    bs.setTac(Integer.parseInt(ss[3]));
                    bs.setMnc(Integer.parseInt(ss[4]));
                    bs.setNt(ss[5]);
                    bs.setLat(Float.parseFloat(ss[6]));
                    bs.setLng(Float.parseFloat(ss[7]));
                    bs_list.add(bs);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Insert the list into RoomDatabase
            CellTable.getInstance(mContext).getBSDao().insertAll(bs_list);
            Log.d("INIT DB","Done!");

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mActivity.findViewById(R.id.layout_top).setVisibility(View.VISIBLE);
                }
            });

            return null;
        }
        else if(mMode=="query"){
            //print front 5 data from RoomDatabase
            List<BS> topBSs = CellTable.getInstance(mContext).getBSDao().getTopBSs();
            if(topBSs.size()==0){
                Log.d("CellTable", "Contains no data.");
            }

            for (int i = 0 ; i < topBSs.size() ; i++){
                Log.d("CellTable" , String.valueOf(i));
                Log.d("CellTable" , "CellID: "+topBSs.get(i).getCell_id());
                Log.d("CellTable" , "PCI: "+topBSs.get(i).getPci());
                Log.d("CellTable" , "TAC: "+topBSs.get(i).getTac());
                Log.d("CellTable" , "MNC: "+topBSs.get(i).getMnc());
                Log.d("CellTable" , "Type: "+topBSs.get(i).getNt());
                Log.d("CellTable" , "Lat: "+topBSs.get(i).getLat());
                Log.d("CellTable" , "Lng: "+topBSs.get(i).getLng());

            }

            return null;
        }
        else if(mMode=="clear"){
            // Clear entire RoomDatabase
            //CellTable.getInstance(mContext).getBSDao().deleteAll();
            mContext.deleteDatabase("CellTable.db");
            Log.d("CellTable", "Clear all data.");
        }

        return null;

    }

}
