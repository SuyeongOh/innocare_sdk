package com.innopia.vital_sync.service;

public class LoginRequest {
    public String id;
    public String password;

    public LoginRequest(String id, String password){
        this.id = id;
        this.password = password;
    }
}