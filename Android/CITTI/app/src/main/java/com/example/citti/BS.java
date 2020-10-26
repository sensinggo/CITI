package com.example.citti;


import android.util.Log;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "BS")
public class BS {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int cell_id;
    private int pci;
    private int tac;
    private int mnc;
    private String nt;
    private float lat;
    private float lng;

    public int getId(){
        return id;
    }

    public int getCell_id(){
        return cell_id;
    }

    public int getPci(){
        return pci;
    }

    public int getTac(){
        return tac;
    }

    public int getMnc(){
        return mnc;
    }

    public String getNt(){
        return nt;
    }

    public float getLat(){
        return lat;
    }

    public float getLng() {
        return lng;
    }

    public void setId(int id){
        this.id = id;
    }

    public void setCell_id(int cell_id){
        this.cell_id = cell_id;
    }

    public void setPci(int pci){
        this.pci = pci;
    }

    public void setTac(int tac){
        this.tac = tac;
    }

    public void setMnc(int mnc){
        this.mnc = mnc;
    }

    public void setNt(String nt){
        this.nt = nt;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public void setLng(float lng) {
        this.lng = lng;
    }

    public void print(){
        Log.d("BS", "Cellid: " + this.cell_id + ", PCI: " + this.pci + ", TAC: " + this.tac + ", MNC: " + this.mnc + ", NT: " + this.nt);
    }
}
