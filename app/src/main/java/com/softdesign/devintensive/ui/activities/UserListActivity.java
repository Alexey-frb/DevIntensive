package com.softdesign.devintensive.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.redmadrobot.chronos.ChronosConnector;
import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.storage.LoadUsersFromDb;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.models.UserDTO;
import com.softdesign.devintensive.ui.adapters.UserAdapter;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.helper.OnStartDragListener;
import com.softdesign.devintensive.utils.helper.SimpleItemTouchHelperCallback;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserListActivity extends BaseActivity implements OnStartDragListener {

    private static final String TAG = ConstantManager.TAG_PREFIX + "UserListActivity";

    @BindView(R.id.main_coordinator_container)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.navigation_drawer)
    DrawerLayout mNavigationDrawer;
    @BindView(R.id.user_list)
    RecyclerView mRecyclerView;

    private DataManager mDataManager;
    private UserAdapter mUserAdapter;
    private List<User> mUsers;

    private String mQuery;

    private Handler mHandler;

    private ItemTouchHelper mItemTouchHelper;

    private ChronosConnector mConnector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConnector = new ChronosConnector();
        mConnector.onCreate(this, savedInstanceState);
        setContentView(R.layout.activity_user_list);

        ButterKnife.bind(this);

        mDataManager = DataManager.getInstance();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mHandler = new Handler();

        setupToolbar();
        setupDrawer();

        mConnector.runOperation(new LoadUsersFromDb(), false);
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
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        mConnector.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mNavigationDrawer.openDrawer(GravityCompat.START);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Кнопка Назад - закрыть меню, если открыто
     */
    @Override
    public void onBackPressed() {
        if (mNavigationDrawer != null && mNavigationDrawer.isDrawerOpen(GravityCompat.START)) {
            mNavigationDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Переопределяет меню пользователя - добавление кнопки Поиск
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                showUserByQuery(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    /**
     * Отобразить снекбар с сообщением
     *
     * @param message
     */
    private void showSnackbar(String message) {
        Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Инициализировать боковое меню
     */
    private void setupDrawer() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        assert navigationView != null;
        navigationView.setCheckedItem(R.id.team_menu);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                item.setChecked(true);

                switch (item.getItemId()) {
                    case R.id.user_profile_menu:
                        Intent intentActivity = new Intent(UserListActivity.this, MainActivity.class);
                        finish();
                        startActivity(intentActivity);
                        break;
                    case R.id.team_menu:
                        break;
                    case R.id.exit_menu:
                        mDataManager.getPreferencesManager().saveAuthToken("");
                        mDataManager.getPreferencesManager().saveUserId("");
                        Intent authActivity = new Intent(UserListActivity.this, AuthActivity.class);
                        finish();
                        startActivity(authActivity);
                        break;
                }

                mNavigationDrawer.closeDrawer(GravityCompat.START);
                return false;
            }
        });
    }

    /**
     * Инициализировать тулбар
     */
    private void setupToolbar() {
        mToolbar.setTitle(R.string.menu_team);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Отобразить список пользователей
     *
     * @param users - список пользовалей
     */
    private void showUsers(final List<User> users) {
        mUsers = users;
        mUserAdapter = new UserAdapter(mUsers, new UserAdapter.UserViewHolder.CustomClickListener() {
            @Override
            public void onUserItemClickListener(int position) {
                UserDTO userDTO = new UserDTO(mUsers.get(position));
                Intent profileIntent = new Intent(UserListActivity.this, ProfileUserActivity.class);
                profileIntent.putExtra(ConstantManager.PARCELABLE_KEY, userDTO);

                startActivity(profileIntent);
            }
        }, this);

        mRecyclerView.swapAdapter(mUserAdapter, false);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mUserAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    /**
     * Отобразить список пользователей в соответствии с поиисковым запросом
     *
     * @param query - поисковый запрос
     */
    private void showUserByQuery(String query) {
        mQuery = query;

        if (!query.equals("")) {
            Runnable searchUsers = new Runnable() {
                @Override
                public void run() {
                    showUsers(mDataManager.getUserListByName(mQuery));
                }
            };

            mHandler.removeCallbacks(searchUsers);
            mHandler.postDelayed(searchUsers, ConstantManager.SEARCH_DELAY);
        } else {
            mConnector.runOperation(new LoadUsersFromDb(), false);
        }
    }

    /**
     * Вызывается по завершению сохранения в локальную базу данных
     *
     * @param result - результат
     */
    public void onOperationFinished(final LoadUsersFromDb.Result result) {
        if (result.isSuccessful()) {
            Log.d(TAG, "onOperationFinished: successful!");

            if (result.getOutput().size() == 0) {
                showSnackbar(getString(R.string.error_user_list_not_loaded));
            } else {
                showUsers(result.getOutput());
            }
        } else {
            Log.e(TAG, "onOperationFinished: error " + result.getErrorMessage());
        }
    }
}
