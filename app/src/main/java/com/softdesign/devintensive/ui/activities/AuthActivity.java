package com.softdesign.devintensive.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.redmadrobot.chronos.ChronosConnector;
import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.req.UserLoginReq;
import com.softdesign.devintensive.data.network.res.UserListRes;
import com.softdesign.devintensive.data.network.res.UserModelGetRes;
import com.softdesign.devintensive.data.network.res.UserModelPostRes;
import com.softdesign.devintensive.data.storage.SaveUsersInDb;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.NetworkStatusChecker;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Авторизация пользователя
 */
public class AuthActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = ConstantManager.TAG_PREFIX + "AuthActivity";

    @BindView(R.id.main_coordinator_container)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.authorization_box)
    CardView mAuthorizationBox;
    @BindView(R.id.login_btn)
    Button mSignIn;
    @BindView(R.id.remember_txt)
    TextView mRememberPassword;
    @BindView(R.id.login_email_et)
    EditText mLogin;
    @BindView(R.id.login_password_et)
    EditText mPassword;

    private ChronosConnector mConnector;
    private Bus mBus;

    private DataManager mDataManager;

    /**
     * метод вызывается при создании активити (после изменения конфигурации/возврата к текущей
     * активности после его уничтожения.
     *
     * @param savedInstanceState - объект со значениями, сохраненными в Bundle - состояние UI
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        Log.d(TAG, "onCreate");

        mBus = new Bus();
        mBus.register(AuthActivity.this);

        mConnector = new ChronosConnector();
        mConnector.onCreate(this, savedInstanceState);

        mDataManager = DataManager.getInstance();

        signInByToken();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mConnector.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        mConnector.onPause();
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mConnector.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");
    }

    @Override
    @OnClick({R.id.login_btn, R.id.remember_txt})
    public void onClick(View v) {
        Log.d(TAG, "onClick");

        switch (v.getId()) {
            case R.id.login_btn:
                signIn();
                break;
            case R.id.remember_txt:
                rememberPassword();
                break;
        }
    }

    /**
     * Отобразить снекбар с сообщением
     *
     * @param message - сообщение
     */
    private void showSnackbar(String message) {
        Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Открыть сайт с формой запроса нового пароля
     */
    private void rememberPassword() {
        Intent rememberIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.login_frame_url_forgot_pass)));
        startActivity(rememberIntent);
    }

    /**
     * Вход на сайт с логином и паролем, запрос информации о пользователе
     */
    private void signIn() {
        Log.d(TAG, "signIn");

        showProgress();

        if (NetworkStatusChecker.isNetworkAvailable(this)) {
            Call<UserModelPostRes> call = mDataManager.loginUser(new UserLoginReq(mLogin.getText().toString(), mPassword.getText().toString()));

            call.enqueue(new Callback<UserModelPostRes>() {
                @Override
                public void onResponse(Call<UserModelPostRes> call, Response<UserModelPostRes> response) {
                    try {
                        if (response.code() == 200) {
                            mDataManager.getPreferencesManager().saveAuthToken(response.body().getData().getToken());
                            mDataManager.getPreferencesManager().saveUserId(response.body().getData().getUser().getId());
                            loginSuccess(response.body().getData().getUser());
                        } else if (response.code() == 404) {
                            mBus.post(getString(R.string.error_invalid_login_or_password));
                        } else {
                            mBus.post(getString(R.string.error_not_response_from_server));
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        Log.e(TAG, "onResponseSignIn: " + e.toString());
                        mBus.post(e.toString());
                    }
                }

                @Override
                public void onFailure(Call<UserModelPostRes> call, Throwable t) {
                    Log.e(TAG, "onFailureSignIn:" + t.getMessage());
                    mBus.post(t.getMessage());
                }
            });
        } else {
            hideProgress();
            mBus.post(getString(R.string.error_network_not_available));
        }
    }

    /**
     * Вход на сайт по токену, запрос информации о пользователе
     */
    private void signInByToken() {
        Log.d(TAG, "signInByToken");

        showProgress();

        if (!mDataManager.getPreferencesManager().getAuthToken().equals("null") && !mDataManager.getPreferencesManager().getUserId().equals("null")) {
            if (NetworkStatusChecker.isNetworkAvailable(this)) {
                Call<UserModelGetRes> call = mDataManager.loginToken(mDataManager.getPreferencesManager().getUserId());

                call.enqueue(new Callback<UserModelGetRes>() {
                    @Override
                    public void onResponse(Call<UserModelGetRes> call, Response<UserModelGetRes> response) {
                        try {
                            if (response.code() == 200) {
                                loginSuccess(response.body().getData());
                            } else if (response.code() == 401) {
                                mBus.post(getString(R.string.error_incorrect_token));
                            } else {
                                mBus.post(getString(R.string.error_not_response_from_server));
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            Log.e(TAG, "onResponseSignInByToken: " + e.toString());
                            mBus.post(e.toString());
                        }
                    }

                    @Override
                    public void onFailure(Call<UserModelGetRes> call, Throwable t) {
                        Log.e(TAG, "onFailureSignInByToken:" + t.getMessage());
                        mBus.post(t.getMessage());
                    }
                });
            } else {
                mBus.post(getString(R.string.error_network_not_available));
            }
        } else {
            mBus.post("");
        }
    }

    /**
     * Сохранить данные о пользователе в SharedPreferences и в локальную базу данных
     *
     * @param userModel - данные пользователя
     */
    private void loginSuccess(UserModelGetRes.Data userModel) {
        Log.d(TAG, "loginSuccess");

        // Сохранение в SharedPreferences
        saveUserProfileData(userModel);
        saveUserInfoData(userModel);
        saveUserFullName(userModel);

        mDataManager.getPreferencesManager().saveUserPhoto(Uri.parse(userModel.getPublicInfo().getPhoto()));
        mDataManager.getPreferencesManager().saveUserAvatar(Uri.parse(userModel.getPublicInfo().getAvatar()));

        // Сохрание в локальную базу данных
        saveUserInDb();
    }

    /**
     * Сохранить в SharedPreferences данные о рейтинге, кол-ве проектов и строках кода
     *
     * @param userModel - данные пользователя
     */
    private void saveUserProfileData(UserModelGetRes.Data userModel) {
        int[] userProfile = {
                userModel.getProfileValues().getRating(),
                userModel.getProfileValues().getLinesCode(),
                userModel.getProfileValues().getProjects()
        };

        mDataManager.getPreferencesManager().saveUserProfileData(userProfile);
    }

    /**
     * Сохранить в SharedPreferences данные о пользователе: телефон, е-mail, vk, repository
     *
     * @param userModel - данные о пользователе
     */
    private void saveUserInfoData(UserModelGetRes.Data userModel) {
        List<String> userInfo = new ArrayList<>();
        userInfo.add(userModel.getContacts().getPhone());
        userInfo.add(userModel.getContacts().getEmail());
        userInfo.add(userModel.getContacts().getVk());
        userInfo.add(userModel.getRepositories().getRepo().get(0).getGit());
        userInfo.add(userModel.getPublicInfo().getBio());

        mDataManager.getPreferencesManager().saveUserInfoData(userInfo);
    }

    /**
     * Сохранить в SharedPreferences полное имя пользователя
     *
     * @param userModel - данные о пользователе
     */
    private void saveUserFullName(UserModelGetRes.Data userModel) {
        List<String> userFullName = new ArrayList<>();
        userFullName.add(userModel.getFirstName());
        userFullName.add(userModel.getSecondName());

        mDataManager.getPreferencesManager().saveUserFullName(userFullName);
    }

    /**
     * Сохранить данные о пользователе в локальную базу данных
     */
    private void saveUserInDb() {
        Log.d(TAG, "saveUserInDb");

        if (NetworkStatusChecker.isNetworkAvailable(this)) {
            Call<UserListRes> call = mDataManager.getUserListFromNetwork();

            call.enqueue(new Callback<UserListRes>() {
                @Override
                public void onResponse(Call<UserListRes> call, Response<UserListRes> response) {
                    try {
                        if (response.code() == 200) {
                            mConnector.runOperation(new SaveUsersInDb(response), false);
                        } else {
                            mBus.post(getString(R.string.error_user_list_not_available));
                            Log.e(TAG, "onResponseSaveUserInDb: " + String.valueOf(response.errorBody().source()));
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        Log.e(TAG, "onResponseSaveUserInDb: " + e.toString());
                        mBus.post(e.toString());
                    }
                }

                @Override
                public void onFailure(Call<UserListRes> call, Throwable t) {
                    Log.e(TAG, "onFailureSaveUserInDb: " + t.getMessage());
                    mBus.post(t.getMessage());
                }
            });
        } else {
            mBus.post(getString(R.string.error_network_not_available));
        }
    }

    /**
     * Вызывается по завершению сохранения в локальную базу данных
     *
     * @param result - результат
     */
    @SuppressWarnings("unused")
    public void onOperationFinished(final SaveUsersInDb.Result result) {
        hideProgress();

        if (result.isSuccessful()) {
            Log.d(TAG, "onOperationFinished: successful!");

            Intent loginIntent = new Intent(AuthActivity.this, MainActivity.class);
            finish();
            startActivity(loginIntent);
        } else {
            Log.e(TAG, "onOperationFinished: " + result.getErrorMessage());
        }
    }

    /**
     * Вызывается при вызове mBus.post (в случае ошибки при запросе на сервер)
     *
     * @param error - текст ошибки
     */
    @Subscribe
    @SuppressWarnings("unused")
    public void answerAvailable(String error) {
        hideProgress();

        ButterKnife.bind(this);

        mAuthorizationBox.setVisibility(View.VISIBLE);

        if (!error.isEmpty()) {
            showSnackbar(error);
        }
    }
}