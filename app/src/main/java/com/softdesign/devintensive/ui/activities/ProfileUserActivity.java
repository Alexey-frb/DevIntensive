package com.softdesign.devintensive.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.res.UserLikeRes;
import com.softdesign.devintensive.data.network.res.UserUnlikeRes;
import com.softdesign.devintensive.data.storage.models.DaoSession;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.models.UserDTO;
import com.softdesign.devintensive.data.storage.models.UserDao;
import com.softdesign.devintensive.ui.adapters.RepositoriesAdapter;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.NetworkStatusChecker;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;

public class ProfileUserActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.user_photo_img)
    ImageView mProfileImage;
    @BindView(R.id.bio_et)
    EditText mUserBio;
    @BindView(R.id.rating_txt)
    TextView mUserRating;
    @BindView(R.id.code_lines_txt)
    TextView mUserCodeLines;
    @BindView(R.id.projects_txt)
    TextView mUserProjects;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.main_coordinator_container)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.likes_fab)
    FloatingActionButton mLikes;

    @BindView(R.id.repositories_list)
    ListView mRepoListView;

    private DataManager mDataManager;
    private DaoSession mDaoSession;

    private String mUserId;

    /**
     * Установить максимальную высоту списка
     *
     * @param listView - список
     */
    public static void setMaxHeightOfListView(ListView listView) {
        ListAdapter adapter = listView.getAdapter();

        View view = adapter.getView(0, null, listView);
        view.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );

        int totalHeight = view.getMeasuredHeight() * adapter.getCount();

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + listView.getDividerHeight() * (adapter.getCount() - 1);
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_user);

        ButterKnife.bind(this);

        mDataManager = DataManager.getInstance();
        mDaoSession = mDataManager.getDaoSession();

        setupToolbar();
        initProfileData();
    }

    /**
     * Инициализировать тулбар
     */
    private void setupToolbar() {
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
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

    /**
     * Загрузить данные о пользователе
     */
    private void initProfileData() {
        UserDTO userDTO = getIntent().getParcelableExtra(ConstantManager.PARCELABLE_KEY);

        final List<String> repositories = userDTO.getRepositories();
        final RepositoriesAdapter repositoriesAdapter = new RepositoriesAdapter(this, repositories);

        mRepoListView.setAdapter(repositoriesAdapter);

        mRepoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openLinkWeb(repositories.get(position));
            }
        });

        mUserBio.setText(userDTO.getBio());
        mUserRating.setText(userDTO.getRating());
        mUserCodeLines.setText(userDTO.getCodeLines());
        mUserProjects.setText(userDTO.getProjects());
        mUserId = userDTO.getUserId();

        mCollapsingToolbarLayout.setTitle(userDTO.getFullName());

        DataManager.getInstance().getPicasso()
                .load(userDTO.getPhoto())
                .placeholder(R.drawable.user_bg)
                .error(R.drawable.user_bg)
                .fit()
                .centerCrop()
                .into(mProfileImage);

        setMaxHeightOfListView(mRepoListView);
    }

    /**
     * Запустить веб-браузер для просмотра ссылки
     *
     * @param link - ссылка
     */
    private void openLinkWeb(String link) {
        if (link.toLowerCase().contains("http://")) {
            link = link.replaceAll("http://", "");
        } else if (link.toLowerCase().contains("http://")) {
            link = link.replaceAll("http://", "");
        }

        if (!link.equals("")) {
            Intent openLinkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + link));
            startActivity(openLinkIntent);
        }
    }

    @Override
    @OnClick(R.id.likes_fab)
    public void onClick(View v) {
        unlikeUser();
    }

    private void likeUser() {
        Log.d(TAG, "likeUser");

        if (NetworkStatusChecker.isNetworkAvailable(this)) {
            Call<UserLikeRes> call = mDataManager.likeUser(mUserId);

            call.enqueue(new retrofit2.Callback<UserLikeRes>() {
                @Override
                public void onResponse(Call<UserLikeRes> call, Response<UserLikeRes> response) {
                    try {
                        if (response.code() == 200) {
                            updateLikeProfile(response.body());
                        } else if (response.code() == 401) {
                            showSnackbar(getString(R.string.error_incorrect_token));
                        } else {
                            showSnackbar(getString(R.string.error_not_response_from_server));
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        Log.e(TAG, "onResponseLikeUser: " + e.toString());
                        showSnackbar(e.toString());
                    }
                }

                @Override
                public void onFailure(Call<UserLikeRes> call, Throwable t) {
                    Log.e(TAG, "onFailureLikeUser:" + t.getMessage());
                    showSnackbar(t.getMessage());
                }
            });
        } else {
            showSnackbar(getString(R.string.error_network_not_available));
        }
    }

    private void unlikeUser() {
        Log.d(TAG, "unlikeUser");

        if (NetworkStatusChecker.isNetworkAvailable(this)) {
            Call<UserUnlikeRes> call = mDataManager.unlikeUser(mUserId);

            call.enqueue(new retrofit2.Callback<UserUnlikeRes>() {
                @Override
                public void onResponse(Call<UserUnlikeRes> call, Response<UserUnlikeRes> response) {
                    try {
                        if (response.code() == 200) {
                            updateUnlikeProfile(response.body());
                        } else if (response.code() == 401) {
                            showSnackbar(getString(R.string.error_incorrect_token));
                        } else {
                            showSnackbar(getString(R.string.error_not_response_from_server));
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        Log.e(TAG, "onResponseUnlikeUser: " + e.toString());
                        showSnackbar(e.toString());
                    }
                }

                @Override
                public void onFailure(Call<UserUnlikeRes> call, Throwable t) {
                    Log.e(TAG, "onFailureUnlikeUser:" + t.getMessage());
                    showSnackbar(t.getMessage());
                }
            });
        } else {
            showSnackbar(getString(R.string.error_network_not_available));
        }
    }

    private void updateLikeProfile(UserLikeRes userData) {
        User user = mDaoSession.queryBuilder(User.class)
                .where(UserDao.Properties.RemoteId.eq(mUserId)).build().unique();

        user.setRating(userData.getData().getRating());
        user.update();

        mUserRating.setText(String.valueOf(userData.getData().getRating()));
    }

    private void updateUnlikeProfile(UserUnlikeRes userData) {
        User user = mDaoSession.queryBuilder(User.class)
                .where(UserDao.Properties.RemoteId.eq(mUserId)).build().unique();

        user.setRating(userData.getData().getRating());
        user.update();

        mUserRating.setText(String.valueOf(userData.getData().getRating()));
    }
}
