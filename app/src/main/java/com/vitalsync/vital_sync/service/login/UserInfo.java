package com.vitalsync.vital_sync.service.login;

public class UserInfo {
    private int id;
    private String user_id;
    private String password;

    public UserInfo(int id, String user_id, String password){
        this.id = id;
        this.user_id = user_id;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public String getUserId() {
        return user_id;
    }

    public String getPassword() {
        return password;
    }
}
