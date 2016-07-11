package com.softdesign.devintensive.ui.activities;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.utils.ConstantManager;

/**
 * Авторизация пользователя
 */
public class LoginActivity extends BaseActivity {
    public static final String TAG = ConstantManager.TAG_PREFIX + "LoginActivity";

    /**
     * метод вызывается при создании активити (после изменения конфигурации/возврата к текущей
     * активности после его уничтожения.
     *
     * @param savedInstanceState - объект со значениями, сохраненными в Bundle - состояние UI
     */
    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "onCreate");
    }

    /**
     * метод вызывается при старте активити перед моментом того как UI станет доступен пользователю.
     * как правило, в данном методе происходит регистрация подписки на события, остановка которых
     * была произведена в методе onStop()
     */
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    /**
     * метод вызывается когда активити становится доступна пользователю для взаимодействия.
     * в данном методе, как правило происходит запуск анимаций/аудио/видео/запуск BroadcastReceiver,
     * необходимых для реализации UI логики/запуска выполнения потоков и т.п.
     * метод должен быть максимально легковесным для максимальной отзывчивости UI
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    /**
     * метод вызывается когда текущая активити теряет фокус, но остается видимой (всплытие диалогового
     * окна/частичное перекрытие другой активити и т.д.)
     * в данном методе реализуется сохранение легковесных UI данных/анимаций/аудио/видео и т.л.)
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    /**
     * метод вызывается когда активити становится невидимым для пользователя.
     * в данном методе происходит отписка от событий, остановка сложных анимаций, сложные операции
     * по сохранению данных/прерывание запущенных потоков и т.п.
     */
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    /**
     * метод вызывается при окончании работы активити (когда это происходит системно или после
     * вызова метода finish()
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    /**
     * метод вызывается при рестарте активити/возобновлении работы после вызоыва метода onStop()
     * в данном методе реализуется специфическая бизнес-логика, которая должна быть реализована именно
     * при рестарте активности - например, запрос к серверу, который необходимо вызывать при
     * возращении из другой активности (обновление данных, подписка на определенное событие
     * проинициализированное на другом экране/специфическая бизнес-логика, завязанная именно
     * на перезапуск активити
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }
}
