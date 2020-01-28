package com.mbul.projectmap.model;

public class SingleDataUser {

    private String user_uid;
    private String user_nama;
    private String user_no_telp;
    private String status;

    public SingleDataUser(String user_uid, String user_nama, String user_no_telp, String status) {
        this.user_uid = user_uid;
        this.user_nama = user_nama;
        this.user_no_telp = user_no_telp;
        this.status = status;
    }

    public String getUser_uid() {
        return user_uid;
    }

    public String getUser_nama() {
        return user_nama;
    }

    public String getUser_no_telp() {
        return user_no_telp;
    }

    public String getStatus() {
        return status;
    }
}
