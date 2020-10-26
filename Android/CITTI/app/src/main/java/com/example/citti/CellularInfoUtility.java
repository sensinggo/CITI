package com.example.citti;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.telephony.TelephonyManager;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;

import androidx.core.app.ActivityCompat;

public class CellularInfoUtility {

    public class Container extends ArrayList<CellularInfo> {
        private int maxSize;

        public Container(int size) {
            this.maxSize = size;
        }

        public boolean add(CellularInfo info) {
            boolean r = super.add(info);
            if (size() > maxSize) {
                removeRange(0, size() - maxSize);
            }
            return r;
        }

        public void print() {
            for (int i = 0; i < size(); i++) {
                this.get(i).print();
            }
            Log.d("CONTAINER", "-----------------------");
        }
    }

    public class Segment {
        public Container container;
        public double[] gis_info;
    }

    public Segment segment;

    // For real cellular data from SIM
    private Context mContext;
    private Activity mActivity;
    private TelephonyManager mTM;


    // For file simulation
    private int mFileCounter = 0;
    private ArrayList<CellularInfo> mData;

    public CellularInfoUtility(int segment_size, Context context, Activity activity) {
        segment = new Segment();
        segment.container = new Container(segment_size);

        mContext = context;
        mActivity = activity;
        mTM = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);

        mData = new ArrayList<>();
        initData();
    }

    private void initData() {
        try {
            Log.d("initData", "Initializing data!");
            InputStreamReader is = new InputStreamReader(mContext.getAssets().open("test_data.csv"));
            BufferedReader reader = new BufferedReader(is);
            reader.readLine();  // skip title
            String line;
            while ((line = reader.readLine()) != null) {
                String[] ss = line.split(",");    //CellID,PCI,TAC,NT,MNC,label
                CellularInfo info = new CellularInfo(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]), Integer.parseInt(ss[2]), Integer.parseInt(ss[4]), ss[3], mContext, mActivity);
                mData.add(info);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fromFile() {
        segment.container.add(mData.get(mFileCounter));
        mFileCounter++;
        if (mFileCounter > mData.size() - 1) {
            mFileCounter = 0;
        }
    }

    public void fromSim() {
        try {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("CELLINF", "Permission not granted");
                return;
            }
            else if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("CELLINF", "Permission not granted");
                return;
            }
            else{

                mTM = (TelephonyManager) mContext.
                        getSystemService(Context.TELEPHONY_SERVICE);
                List<CellInfo> cellInfoList = mTM.getAllCellInfo();

                for(CellInfo cellInfo : cellInfoList){
                    String nt="";
                    int cellid=-1;
                    int pci=-1;
                    int tac=-1;
                    int mnc=-1;
                    if(cellInfo.isRegistered()){        // Primary cell
                        if(cellInfo instanceof CellInfoLte){
                            nt = "LTE";
                            CellIdentityLte cellIdentity = ((CellInfoLte) cellInfo).getCellIdentity();
                            cellid = cellIdentity.getCi();
                            pci = cellIdentity.getPci();
                            tac = cellIdentity.getTac();
                            mnc = cellIdentity.getMnc();
                        }
                        else if(cellInfo instanceof CellInfoWcdma){
                            nt = "Wcdma";
                            CellIdentityWcdma cellIdentity = ((CellInfoWcdma) cellInfo).getCellIdentity();
                            cellid = cellIdentity.getCid();
                            pci = cellIdentity.getPsc();
                            tac = cellIdentity.getLac();
                            mnc = cellIdentity.getMnc();
                        }
                        CellularInfo info = new CellularInfo(cellid, pci, tac, mnc, nt, mContext, mActivity);
                        info.print();
                        segment.container.add(info);
                        return;
                    }
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
