package com.example.citti;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.util.concurrent.ExecutionException;

public class CellularInfo{
    public int cellId;
    public int pci;
    public int tac;
    public int mnc;
    public String nt;
    public float cLat;
    public float cLon;

    public CellularInfo(int cellId, int pci, int tac, int mnc, String nt, Context context, Activity activity){
        this.cellId = cellId;
        this.pci = pci;
        this.tac = tac;
        this.mnc = mnc;
        this.nt = nt;

        queryCellLocation(context, activity);
    }

    private void queryCellLocation(Context context, Activity activity) {
        BS q = new DBHelper(activity, context, "queryCellLoc").getBS(this.cellId, this.pci, this.tac, this.mnc, this.nt);
        this.cLat = q.getLat();
        this.cLon = q.getLng();
//        this.cLat = 24;
//        this.cLon = 123;
    }

    public void print(){
        Log.d("CELLINFO1", "Cellid: " + this.cellId + ", PCI: " + this.pci + ", TAC: " + this.tac + ", MNC: " + this.mnc + ", NT: " + this.nt);
        Log.d("CELLINFO2", "cLat: " + this.cLat + ", cLon: " + cLon);
    }
}
