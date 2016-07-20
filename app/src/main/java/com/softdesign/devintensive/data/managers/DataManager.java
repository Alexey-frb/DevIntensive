package com.softdesign.devintensive.data.managers;

import android.content.Context;
import android.util.Log;

import com.softdesign.devintensive.data.network.PicassoCache;
import com.softdesign.devintensive.data.network.RestService;
import com.softdesign.devintensive.data.network.ServiceGenerator;
import com.softdesign.devintensive.data.network.req.UserLoginReq;
import com.softdesign.devintensive.data.network.res.UploadPhotoRes;
import com.softdesign.devintensive.data.network.res.UserListRes;
import com.softdesign.devintensive.data.network.res.UserModelGetRes;
import com.softdesign.devintensive.data.network.res.UserModelPostRes;
import com.softdesign.devintensive.data.storage.models.DaoSession;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.models.UserDao;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.DevIntensiveApplication;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;

public class DataManager {
    public static final String TAG = ConstantManager.TAG_PREFIX + "DataManager";

    private static DataManager INSTANCE = null;

    private PreferencesManager mPreferencesManager;
    private Context mContext;

    private RestService mRestService;
    private Picasso mPicasso;
    private DaoSession mDaoSession;

    private DataManager() {
        mPreferencesManager = new PreferencesManager();
        mContext = DevIntensiveApplication.getContext();

        mRestService = ServiceGenerator.createService(RestService.class);
        mPicasso = new PicassoCache(mContext).getPicassoInstance();
        mDaoSession = DevIntensiveApplication.getDaoSession();
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

    public Picasso getPicasso() {
        return mPicasso;
    }

    public RestService getRestService() {
        return mRestService;
    }

    //region --- Network ---
    public Call<UserModelPostRes> loginUser(UserLoginReq userLoginReq) {
        return mRestService.loginUser(userLoginReq);
    }

    public Call<UserModelGetRes> loginToken(String userId) {
        return mRestService.loginToken(userId);
    }

    public Call<UploadPhotoRes> uploadPhoto(String userId, MultipartBody.Part file) {
        return mRestService.uploadPhoto(userId, file);
    }

    public Call<UploadPhotoRes> uploadAvatar(String userId, MultipartBody.Part file) {
        return mRestService.uploadAvatar(userId, file);
    }

    public Call<UserListRes> getUserListFromNetwork() {
        return mRestService.getUserList();
    }
    //endregion

    //region --- Database ---
    public DaoSession getDaoSession() {
        return mDaoSession;
    }

    public List<User> getUserListByName(String query) {
        List<User> userList = new ArrayList<>();

        try {
            userList = mDaoSession.queryBuilder(User.class)
                    .where(UserDao.Properties.Rating.gt(0), UserDao.Properties.SearchName.like("%" + query.toUpperCase() + "%"))
                    .orderDesc(UserDao.Properties.Rating)
                    .build()
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "getUserListByName: " + e.getMessage());
        }

        return userList;
    }
    //endregion
}