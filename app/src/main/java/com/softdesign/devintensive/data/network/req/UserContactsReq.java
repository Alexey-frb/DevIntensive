package com.softdesign.devintensive.data.network.req;

public class UserContactsReq {

    private String email;
    private String phone;
    private String vk;

    public UserContactsReq(String email, String phone, String vk) {
        this.email = email;
        this.phone = phone;
        this.vk = vk;
    }
}
