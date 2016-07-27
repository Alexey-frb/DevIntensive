package com.softdesign.devintensive.data.network.res;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserLoginRes {
    @SerializedName("success")
    @Expose
    private boolean success;
    @SerializedName("data")
    @Expose
    private Data data;

    public Data getData() {
        return data;
    }

    public class Data {
        @SerializedName("user")
        @Expose
        private UserDataRes.Data user;
        @SerializedName("token")
        @Expose
        private String token;

        public String getToken() {
            return token;
        }

        public UserDataRes.Data getUser() {
            return user;
        }
    }
}