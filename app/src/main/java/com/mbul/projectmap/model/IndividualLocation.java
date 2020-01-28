package com.mbul.projectmap.model;

import com.mapbox.mapboxsdk.geometry.LatLng;

public class IndividualLocation {

    private String uid;
    private String nama_toko;
    private String alamat;
    private String no_telp;
    private LatLng location;

    public IndividualLocation(String uid, String nama_toko, String alamat, String no_telp, LatLng location) {
        this.uid = uid;
        this.nama_toko = nama_toko;
        this.alamat = alamat;
        this.no_telp = no_telp;
        this.location = location;
    }

    public String getUid() { return uid; }

    public String getNama_toko() {
        return nama_toko;
    }

    public void setNama_toko(String nama_toko) {
        this.nama_toko = nama_toko;
    }

    public String getAlamat() {
        return alamat;
    }

    public String getNo_telp() {
        return no_telp;
    }

    public LatLng getLocation() {
        return location;
    }
}
