package com.softdesign.devintensive.data.managers;

import android.content.SharedPreferences;
import android.net.Uri;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.DevIntensiveApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Сохранение/загрузка пользовательских данных, используя shared preferences
 */
public class PreferencesManager {

    private static final String[] USER_DATA = {
            ConstantManager.FIRST_NAME,
            ConstantManager.SECOND_NAME};
    private static final String[] USER_INFO = {
            ConstantManager.USER_PHONE_KEY,
            ConstantManager.USER_MAIL_KEY,
            ConstantManager.USER_VK_KEY,
            ConstantManager.USER_GIT_KEY,
            ConstantManager.USER_BIO_KEY};
    private static final String[] USER_PROFILE = {
            ConstantManager.USER_RATING_VALUE,
            ConstantManager.USER_CODE_LINES_VALUE,
            ConstantManager.USER_PROJECT_VALUE};
    private static final int[] USER_INFO_DEFAULT_VALUES = {
            R.string.user_profile_phone_et_default_value,
            R.string.user_profile_email_et_default_value,
            R.string.user_profile_vk_et_default_value,
            R.string.user_profile_git_et_default_value,
            R.string.user_profile_bio_et_default_value};
    private SharedPreferences mSharedPreferences;

    public PreferencesManager() {
        this.mSharedPreferences = DevIntensiveApplication.getSharedPreferences();
    }

    /**
     * Сохранить пользовательское имя в shared preferences
     *
     * @param userData
     */
    public void saveUserFullName(List<String> userData) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        for (int i = 0; i < USER_DATA.length; i++) {
            editor.putString(USER_DATA[i], userData.get(i));
        }
        editor.apply();
    }

    /**
     * Восстановить пользовательское имя из shared preferences
     *
     * @return
     */
    public String getFullName() {
        return mSharedPreferences.getString(ConstantManager.SECOND_NAME, "null") + " " +
                mSharedPreferences.getString(ConstantManager.FIRST_NAME, "null");
    }

    /**
     * Сохранить пользовательские данные в shared preferences
     *
     * @param userInfo
     */
    public void saveUserInfoData(List<String> userInfo) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        for (int i = 0; i < USER_INFO.length; i++) {
            editor.putString(USER_INFO[i], userInfo.get(i));
        }
        editor.apply();
    }

    /**
     * Восстанавить пользовательские данные из shared preferences
     *
     * @return
     */
    public List<String> loadUserInfoData() {
        List<String> userInfo = new ArrayList<>();
        for (int i = 0; i < USER_INFO.length; i++) {
            userInfo.add(mSharedPreferences.getString(USER_INFO[i],
                    DevIntensiveApplication.getContext().getString(USER_INFO_DEFAULT_VALUES[i])));
        }
        return userInfo;
    }

    /**
     * Сохранить данные профиля в shared preferences
     *
     * @param userProfile
     */
    public void saveUserProfileData(int[] userProfile) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        for (int i = 0; i < USER_PROFILE.length; i++) {
            editor.putString(USER_PROFILE[i], String.valueOf(userProfile[i]));
        }
        editor.apply();
    }

    /**
     * Восстановить данные профиля из shared preferences
     *
     * @return
     */
    public List<String> loadUserProfileData() {
        List<String> userProfile = new ArrayList<>();
        for (int i = 0; i < USER_PROFILE.length; i++) {
            userProfile.add(mSharedPreferences.getString(USER_PROFILE[i], "0"));
        }
        return userProfile;
    }

    /**
     * Сохранить пользовательское фото в shared preferences
     *
     * @param uri
     */
    public void saveUserPhoto(Uri uri) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(ConstantManager.USER_PHOTO_KEY, uri.toString());
        editor.apply();
    }

    /**
     * Загрузить пользовательское фото из shared preferences
     *
     * @return
     */
    public Uri loadUserPhoto() {
        return Uri.parse(mSharedPreferences.getString(ConstantManager.USER_PHOTO_KEY, "android.resource://com.softdesign.devintensive/drawable/user_bg"));
    }

    /**
     * Сохранить пользовательский аватар в shared preferences
     *
     * @param uri
     */
    public void saveUserAvatar(Uri uri) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(ConstantManager.USER_AVATAR_KEY, uri.toString());
        editor.apply();
    }

    /**
     * Загрузить пользовательский аватар из shared preferences
     *
     * @return
     */
    public Uri loadUserAvatar() {
        return Uri.parse(mSharedPreferences.getString(ConstantManager.USER_AVATAR_KEY, "android.resource://com.softdesign.devintensive/drawable/ic_account"));
    }

    /**
     * Сохранить токен авторизации в shared preferences
     *
     * @param authToken
     */
    public void saveAuthToken(String authToken) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(ConstantManager.AUTH_TOKEN_KEY, authToken);
        editor.apply();
    }

    /**
     * Получить токен авторизации из shared preferences
     *
     * @return
     */
    public String getAuthToken() {
        return mSharedPreferences.getString(ConstantManager.AUTH_TOKEN_KEY, "null");
    }

    /**
     * Сохранить идентификатор пользователя в shared preferences
     *
     * @param userId
     */
    public void saveUserId(String userId) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(ConstantManager.USER_ID_KEY, userId);
        editor.apply();
    }

    /**
     * Получить идентификатор пользователя из shared preferences
     *
     * @return
     */
    public String getUserId() {
        return mSharedPreferences.getString(ConstantManager.USER_ID_KEY, "null");
    }
}
