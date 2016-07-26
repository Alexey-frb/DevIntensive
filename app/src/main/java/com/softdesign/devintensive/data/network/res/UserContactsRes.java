package com.softdesign.devintensive.data.network.res;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserContactsRes {

    @SerializedName("success")
    @Expose
    public boolean success;
    @SerializedName("data")
    @Expose
    public Data data;

    public class Data {

        @SerializedName("updated")
        @Expose
        public String updated;
        @SerializedName("email")
        @Expose
        public String email;
        @SerializedName("phone")
        @Expose
        public String phone;
        @SerializedName("vk")
        @Expose
        public String vk;
    }
}
