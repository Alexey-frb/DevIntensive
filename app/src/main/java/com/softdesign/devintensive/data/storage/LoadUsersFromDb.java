package com.softdesign.devintensive.data.storage;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.redmadrobot.chronos.ChronosOperation;
import com.redmadrobot.chronos.ChronosOperationResult;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.models.UserDao;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.DevIntensiveApplication;

import org.greenrobot.greendao.Property;

import java.util.ArrayList;
import java.util.List;

/**
 * Загрузка списка пользователей из БД
 */
public class LoadUsersFromDb extends ChronosOperation<List<User>> {
    public static final String TAG = ConstantManager.TAG_PREFIX + "LoadUsersFromDb";

    private Property mOrderProperty;
    private String mAscOrDesc;

    public LoadUsersFromDb(String orderProperty, String ascOrDesc) {
        switch (orderProperty) {
            case "manual":
                mOrderProperty = UserDao.Properties.SortId;
                break;
            case "rating":
                mOrderProperty = UserDao.Properties.Rating;
                break;
            case "code_lines":
                mOrderProperty = UserDao.Properties.CodeLines;
                break;
            default:
                mOrderProperty = UserDao.Properties.SortId;
        }

        mAscOrDesc = ascOrDesc;

        DataManager.getInstance().getPreferencesManager().saveOrderDb(orderProperty, ascOrDesc);
    }

    @Nullable
    @Override
    public List<User> run() {
        List<User> userList = new ArrayList<>();

        try {
            userList = DevIntensiveApplication.getDaoSession().queryBuilder(User.class)
                    .where(UserDao.Properties.CodeLines.gt(0))
                    .orderCustom(mOrderProperty, mAscOrDesc)
                    .build()
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "run: " + e.getMessage());
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
