package com.softdesign.devintensive.data.network;

import com.softdesign.devintensive.data.network.req.UserContactsReq;
import com.softdesign.devintensive.data.network.req.UserLoginReq;
import com.softdesign.devintensive.data.network.res.UploadPhotoRes;
import com.softdesign.devintensive.data.network.res.UserContactsRes;
import com.softdesign.devintensive.data.network.res.UserDataRes;
import com.softdesign.devintensive.data.network.res.UserLikeRes;
import com.softdesign.devintensive.data.network.res.UserListRes;
import com.softdesign.devintensive.data.network.res.UserLoginRes;
import com.softdesign.devintensive.data.network.res.UserUnlikeRes;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface RestService {

    @POST("login")
    Call<UserLoginRes> loginUser(@Body UserLoginReq req);

    @GET("user/{userId}")
    Call<UserDataRes> loginToken(@Path("userId") String userId);

    @Multipart
    @POST("user/{userId}/publicValues/profilePhoto")
    Call<UploadPhotoRes> uploadPhoto(@Path("userId") String userId, @Part MultipartBody.Part file);

    @Multipart
    @POST("user/{userId}/publicValues/profileAvatar")
    Call<UploadPhotoRes> uploadAvatar(@Path("userId") String userId, @Part MultipartBody.Part file);

    @GET("user/list?orderBy=rating")
    Call<UserListRes> getUserList();

    @PUT("user/{userId}/contacts")
    Call<UserContactsRes> changeUserContacts(@Path("userId") String userId, @Body UserContactsReq req);

    @POST("user/{userId}/like")
    Call<UserLikeRes> likeUser(@Path("userId") String userId);

    @POST("user/{userId}/unlike")
    Call<UserUnlikeRes> unlikeUser(@Path("userId") String userId);
}
