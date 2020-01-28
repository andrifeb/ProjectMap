package com.mbul.projectmap.model;

public class SingleDataJasa {

    private String jasa_uid;
    private String jasa_nama_toko;
    private String jasa_no_telp;

    public SingleDataJasa(String jasa_uid, String jasa_nama_toko, String jasa_no_telp) {
        this.jasa_uid = jasa_uid;
        this.jasa_nama_toko = jasa_nama_toko;
        this.jasa_no_telp = jasa_no_telp;
    }

    public String getJasa_uid() { return jasa_uid; }

    public String getJasa_nama_toko() {
        return jasa_nama_toko;
    }

    public String getJasa_no_telp() {
        return jasa_no_telp;
    }

}
