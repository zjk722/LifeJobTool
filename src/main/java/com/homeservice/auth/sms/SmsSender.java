package com.homeservice.auth.sms;

public interface SmsSender {

    void sendLoginCode(String phone, String code);
}
