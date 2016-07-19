package com.softdesign.devintensive.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
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
import com.softdesign.devintensive.data.network.res.UserModelRes;
import com.softdesign.devintensive.data.storage.models.Repository;
import com.softdesign.devintensive.data.storage.models.RepositoryDao;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.models.UserDao;
import com.softdesign.devintensive.utils.AppConfig;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.NetworkStatusChecker;

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

    private DataManager mDataManager;
    private RepositoryDao mRepositoryDao;
    private UserDao mUserDao;

    private boolean isLoginTokenSuccess;

    /**
     * метод вызывается при создании активити (после изменения конфигурации/возврата к текущей
     * активности после его уничтожения.
     *
     * @param savedInstanceState - объект со значениями, сохраненными в Bundle - состояние UI
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConnector = new ChronosConnector();
        mConnector.onCreate(this, savedInstanceState);
        setContentView(R.layout.activity_auth);
        Log.d(TAG, "onCreate");

        ButterKnife.bind(this);

        //mAuthorizationBox.setVisibility(View.GONE);

        mDataManager = DataManager.getInstance();

        mUserDao = mDataManager.getDaoSession().getUserDao();
        mRepositoryDao = mDataManager.getDaoSession().getRepositoryDao();

        //signInByToken();

        //if (!isLoginTokenSuccess) {
        //    mAuthorizationBox.setVisibility(View.VISIBLE);
        //}
    }

    @Override
    protected void onResume() {
        super.onResume();
        mConnector.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        mConnector.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");
    }

    @Override
    protected void onPause() {
        mConnector.onPause();
        super.onPause();
        Log.d(TAG, "onPause");
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
     * Отобразить снекбар
     *
     * @param message - сообщение
     */

    private void showSnackbar(String message) {
        Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    private void rememberPassword() {
        Intent rememberIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.frame_login_url_forgot_pass)));
        startActivity(rememberIntent);
    }

    private void loginSuccess(UserModelRes userModel) {
        mDataManager.getPreferencesManager().saveAuthToken(userModel.getData().getToken());
        mDataManager.getPreferencesManager().saveUserId(userModel.getData().getUser().getId());

        saveUserValues(userModel);
        saveUserFields(userModel);
        saveUserData(userModel);

        saveUserInDb();

        mDataManager.getPreferencesManager().saveUserPhoto(Uri.parse(userModel.getData().getUser().getPublicInfo().getPhoto()));
        mDataManager.getPreferencesManager().saveUserAvatar(Uri.parse(userModel.getData().getUser().getPublicInfo().getAvatar()));

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent loginIntent = new Intent(AuthActivity.this, MainActivity.class);
                startActivity(loginIntent);
            }
        }, AppConfig.START_DELAY);
    }

    private void signIn() {
        if (NetworkStatusChecker.isNetworkAvailable(this)) {
            //showProgress();
            Call<UserModelRes> call = mDataManager.loginUser(new UserLoginReq(mLogin.getText().toString(), mPassword.getText().toString()));
            call.enqueue(new Callback<UserModelRes>() {
                @Override
                public void onResponse(Call<UserModelRes> call, Response<UserModelRes> response) {
                    if (response.code() == 200) {
                        loginSuccess(response.body());
                    } else if (response.code() == 404) {
                        showSnackbar("Неверный логин или пароль!");
                    } else {
                        showSnackbar("Нет ответа от сервера!");
                    }
                }

                @Override
                public void onFailure(Call<UserModelRes> call, Throwable t) {
                    // TODO: 12.07.2016 обработать ошибки ретрофита
                    Log.e(TAG, "onFailure:");
                }
            });
        } else {
            showSnackbar("Сеть на данный момент не доступна, попробуйте позже!");
        }
    }

    private void signInByToken() {
        isLoginTokenSuccess = false;
        //if (!mDataManager.getPreferencesManager().getAuthToken().equals("null")) {
        if (NetworkStatusChecker.isNetworkAvailable(this)) {
            Call<UserModelRes> call = mDataManager.loginToken(mDataManager.getPreferencesManager().getUserId());
            call.enqueue(new Callback<UserModelRes>() {
                @Override
                public void onResponse(Call<UserModelRes> call, Response<UserModelRes> response) {
                    if (response.code() == 200) {
                        loginSuccess(response.body());
                        isLoginTokenSuccess = true;
                    } else if (response.code() == 401) {
                        showSnackbar("Невалидный токен!");
                    } else {
                        showSnackbar("Нет ответа от сервера!");
                    }
                }

                @Override
                public void onFailure(Call<UserModelRes> call, Throwable t) {
                    showSnackbar("Нет ответа от сервера!");
                }
            });
        } else {
            showSnackbar("Сеть на данный момент не доступна, попробуйте позже!");
        }
        //}
    }

    private void saveUserValues(UserModelRes userModel) {
        int[] userValues = {
                userModel.getData().getUser().getProfileValues().getRating(),
                userModel.getData().getUser().getProfileValues().getLinesCode(),
                userModel.getData().getUser().getProfileValues().getProjects()
        };

        mDataManager.getPreferencesManager().saveUserProfileValues(userValues);
    }

    private void saveUserFields(UserModelRes userModel) {
        List<String> userData = new ArrayList<>();
        userData.add(userModel.getData().getUser().getContacts().getPhone());
        userData.add(userModel.getData().getUser().getContacts().getEmail());
        userData.add(userModel.getData().getUser().getContacts().getVk());
        userData.add(userModel.getData().getUser().getRepositories().getRepo().get(0).getGit());
        userData.add(userModel.getData().getUser().getPublicInfo().getBio());

        mDataManager.getPreferencesManager().saveUserProfileData(userData);
    }

    private void saveUserData(UserModelRes userModel) {
        List<String> userData = new ArrayList<>();
        userData.add(userModel.getData().getUser().getFirstName());
        userData.add(userModel.getData().getUser().getSecondName());

        mDataManager.getPreferencesManager().saveUserData(userData);
    }

    private void saveUserInDb() {
        Call<UserListRes> call = mDataManager.getUserListFromNetwork();

        call.enqueue(new Callback<UserListRes>() {
            @Override
            public void onResponse(Call<UserListRes> call, Response<UserListRes> response) {
                try {
                    if (response.code() == 200) {
                        List<Repository> allRepositories = new ArrayList<Repository>();
                        List<User> allUsers = new ArrayList<User>();

                        for (UserListRes.UserData userRes : response.body().getData()) {
                            allRepositories.addAll(getRepoListFromUserRes(userRes));
                            allUsers.add(new User(userRes));
                        }

                        mRepositoryDao.insertOrReplaceInTx(allRepositories);
                        mUserDao.insertOrReplaceInTx(allUsers);

                    } else {
                        showSnackbar("Список пользователей не может быть получен");
                        Log.e(TAG, "onResponse: " + String.valueOf(response.errorBody().source()));
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.toString());
                    showSnackbar("Что то пошло не так. :(");
                }
            }

            @Override
            public void onFailure(Call<UserListRes> call, Throwable t) {
                // TODO: 15.07.2016 обработать ошибки
            }
        });
    }

    private List<Repository> getRepoListFromUserRes(UserListRes.UserData userData) {
        final String userId = userData.getId();

        List<Repository> repositories = new ArrayList<>();
        for (UserModelRes.Repo repositoryRes : userData.getRepositories().getRepo()) {
            repositories.add(new Repository(repositoryRes, userId));
        }

        return repositories;
    }
}
