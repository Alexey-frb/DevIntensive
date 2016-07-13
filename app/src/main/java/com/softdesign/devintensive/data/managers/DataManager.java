package com.softdesign.devintensive.data.managers;

import android.content.Context;

import com.softdesign.devintensive.data.network.RestService;
import com.softdesign.devintensive.data.network.ServiceGenerator;
import com.softdesign.devintensive.data.network.req.UserLoginReq;
import com.softdesign.devintensive.data.network.res.UploadPhotoRes;
import com.softdesign.devintensive.data.network.res.UserModelRes;
import com.softdesign.devintensive.utils.DevIntensiveApplication;

import okhttp3.MultipartBody;
import retrofit2.Call;

public class DataManager {

    private static DataManager INSTANCE = null;

    private PreferencesManager mPreferencesManager;
    private Context mContext;
    private RestService mRestService;

    private DataManager() {
        mPreferencesManager = new PreferencesManager();
        mContext = DevIntensiveApplication.getContext();
        mRestService = ServiceGenerator.createService(RestService.class);
    }

    public static DataManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DataManager();
        }
        return INSTANCE;
    }

    public PreferencesManager getPreferencesManager() {
        return mPreferencesManager;
    }

    public Context getContext() {
        return mContext;
    }

    public RestService getRestService() {
        return mRestService;
    }

    //region Network
    public Call<UserModelRes> loginUser(UserLoginReq userLoginReq) {
        return mRestService.loginUser(userLoginReq);
    }

    public Call<UploadPhotoRes> uploadPhoto(String userId, MultipartBody.Part file) {
        return mRestService.uploadPhoto(userId, file);
    }

    public Call<UploadPhotoRes> uploadAvatar(String userId, MultipartBody.Part file) {
        return mRestService.uploadAvatar(userId, file);
    }
    //endregions
}