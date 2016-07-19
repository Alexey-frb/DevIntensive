package com.softdesign.devintensive.data.storage;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.redmadrobot.chronos.ChronosOperation;
import com.redmadrobot.chronos.ChronosOperationResult;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.models.UserDao;
import com.softdesign.devintensive.utils.DevIntensiveApplication;

import java.util.ArrayList;
import java.util.List;

public class LoadUsersFromDb extends ChronosOperation<List<User>> {
    @Nullable
    @Override
    public List<User> run() {
        List<User> userList = new ArrayList<>();

        try {
            userList = DevIntensiveApplication.getDaoSession().queryBuilder(User.class)
                    .where(UserDao.Properties.Rating.gt(0))
                    .orderDesc(UserDao.Properties.Rating)
                    .build()
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return userList;
    }

    @NonNull
    @Override
    public Class<Result> getResultClass() {
        return Result.class;
    }

    public final static class Result extends ChronosOperationResult<List<User>> {
    }
}
