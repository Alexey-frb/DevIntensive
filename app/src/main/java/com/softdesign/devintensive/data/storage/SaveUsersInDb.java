package com.softdesign.devintensive.data.storage;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.redmadrobot.chronos.ChronosOperation;
import com.redmadrobot.chronos.ChronosOperationResult;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.res.UserListRes;
import com.softdesign.devintensive.data.network.res.UserModelGetRes;
import com.softdesign.devintensive.data.storage.models.DaoSession;
import com.softdesign.devintensive.data.storage.models.Repository;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.models.UserDao;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

/**
 * Сохранить список пользователей в БД
 */
public class SaveUsersInDb extends ChronosOperation<List<User>> {

    private Response<UserListRes> mResponse;
    private DataManager mDataManager;
    private DaoSession mDaoSession;

    public SaveUsersInDb(Response<UserListRes> response) {
        mResponse = response;
        mDataManager = DataManager.getInstance();
        mDaoSession = mDataManager.getDaoSession();
    }

    @Nullable
    @Override
    public List<User> run() {
        List<Repository> allRepositories = new ArrayList<Repository>();
        List<User> allUsers = new ArrayList<User>();

        long index = 0, number;
        for (UserListRes.UserData userRes : mResponse.body().getData()) {
            index++;
            User user = mDaoSession.queryBuilder(User.class)
                    .where(UserDao.Properties.RemoteId.eq(userRes.getId())).build().unique();
            if (user != null) {
                number = user.getSortId();
            } else {
                number = index;
            }

            allRepositories.addAll(getRepoListFromUserRes(userRes));
            allUsers.add(new User(userRes, number));
        }

        mDaoSession.getRepositoryDao().insertOrReplaceInTx(allRepositories);
        mDaoSession.getUserDao().insertOrReplaceInTx(allUsers);

        return null;
    }

    private List<Repository> getRepoListFromUserRes(UserListRes.UserData userData) {
        final String userId = userData.getId();

        List<Repository> repositories = new ArrayList<>();
        for (UserModelGetRes.Repo repositoryRes : userData.getRepositories().getRepo()) {
            repositories.add(new Repository(repositoryRes, userId));
        }

        return repositories;
    }

    @NonNull
    @Override
    public Class<Result> getResultClass() {
        return Result.class;
    }

    public final static class Result extends ChronosOperationResult<List<User>> {
    }
}
