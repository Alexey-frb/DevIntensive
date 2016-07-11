package com.softdesign.devintensive.ui.activities;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.utils.ConstantManager;

/**
 * Базовый класс для других классов
 */
public class BaseActivity extends AppCompatActivity {
    static final String TAG = ConstantManager.TAG_PREFIX + "BaseActivity";

    protected ProgressDialog mProgressDialog;

    /**
     * Отобразить прогресс-бар
     */
    public void showProgress() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this, R.style.custom_dialog);
            mProgressDialog.setCancelable(false);
            mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        mProgressDialog.show();
        mProgressDialog.setContentView(R.layout.progress_splash);
    }

    /**
     * Скрыть прогресс-бар
     */
    public void hideProgress() {
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.hide();
            }
        }
    }

    /**
     * Отобразить всплывающее сообщение об ошибке
     *
     * @param message - сообщение
     * @param error   - ошибка
     */
    public void showError(String message, Exception error) {
        showToast(message);
        Log.e(TAG, String.valueOf(error));
    }

    /**
     * Отобразить всплывающее сообщение
     *
     * @param message - сообщение
     */
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Скрыть прогресс-бар с задержкой 5 секунд
     */
    private void runWithDelay() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideProgress();
            }
        }, 5000);
    }
}
