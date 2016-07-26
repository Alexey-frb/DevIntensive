package com.softdesign.devintensive.ui.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.req.UserContactsReq;
import com.softdesign.devintensive.data.network.res.UploadPhotoRes;
import com.softdesign.devintensive.data.network.res.UserContactsRes;
import com.softdesign.devintensive.utils.CircleTransform;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.EditTextWatcher;
import com.softdesign.devintensive.utils.NetworkStatusChecker;
import com.squareup.picasso.MemoryPolicy;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Основная логика приложения
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = ConstantManager.TAG_PREFIX + "MainActivity";

    static final ButterKnife.Action<View> ENABLED = new ButterKnife.Action<View>() {
        @Override
        public void apply(View view, int index) {
            view.setEnabled(true);
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
        }
    };

    static final ButterKnife.Action<View> DISABLE = new ButterKnife.Action<View>() {
        @Override
        public void apply(View view, int index) {
            view.setEnabled(false);
            view.setFocusable(false);
            view.setFocusableInTouchMode(false);
        }
    };

    @BindView(R.id.main_coordinator_container)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.navigation_drawer)
    DrawerLayout mNavigationDrawer;
    @BindView(R.id.fab)
    FloatingActionButton mFab;
    @BindView(R.id.profile_placeholder)
    RelativeLayout mProfilePlaceholder;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbar;
    @BindView(R.id.appbar_layout)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.user_photo_img)
    ImageView mProfileImage;
    @BindView(R.id.phone_et)
    EditText mUserPhone;
    @BindView(R.id.email_et)
    EditText mUserMail;
    @BindView(R.id.vk_et)
    EditText mUserVk;
    @BindView(R.id.git_et)
    EditText mUserGit;
    @BindView(R.id.bio_et)
    EditText mUserBio;
    @BindView(R.id.call_phone_iv)
    ImageView mCallPhone;
    @BindView(R.id.send_email_iv)
    ImageView mSendMail;
    @BindView(R.id.open_vk_iv)
    ImageView mOpenVk;
    @BindView(R.id.open_git_iv)
    ImageView mOpenGit;

    @BindViews({R.id.phone_et, R.id.email_et, R.id.vk_et, R.id.git_et, R.id.bio_et})
    List<EditText> mUserInfoViews;
    @BindViews({R.id.call_phone_iv, R.id.send_email_iv, R.id.open_vk_iv, R.id.open_git_iv})
    List<ImageView> mUserActions;

    @BindView(R.id.rating_txt)
    TextView mUserValueRating;
    @BindView(R.id.code_lines_txt)
    TextView mUserValueCodeLines;
    @BindView(R.id.projects_txt)
    TextView mUserValueProjects;

    @BindViews({R.id.rating_txt, R.id.code_lines_txt, R.id.projects_txt})
    List<TextView> mUserValueViews;

    private int mCurrentEditMode = 0;
    private DataManager mDataManager;
    private AppBarLayout.LayoutParams mAppBarParams = null;
    private File mPhotoFile = null;
    private Uri mSelectedImage = null;

    private EditTextWatcher mUserPhoneWatcher;
    private EditTextWatcher mUserMailWatcher;
    private EditTextWatcher mUserVkWatcher;
    private EditTextWatcher mUserGitWatcher;

    /**
     * метод вызывается при создании активити (после изменения конфигурации/возврата к текущей
     * активности после его уничтожения.
     * <p>
     * в данном методе инициализируется/производится:
     * - UI пользовательский интерфейс (статика)
     * - инициализация статических данных активити
     * - связь данных со списками (инициализация адаптеров)
     * <p>
     * Не запускать длительные операции по работе с данными в onCreate() !!!
     *
     * @param savedInstanceState - объект со значениями, сохраненными в Bundle - состояние UI
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");

        ButterKnife.bind(this);

        mDataManager = DataManager.getInstance();

        setupToolbar();
        setupDrawer();

        initUserInfoData();

        mUserPhoneWatcher = new EditTextWatcher(this, mUserPhone);
        mUserMailWatcher = new EditTextWatcher(this, mUserMail);
        mUserVkWatcher = new EditTextWatcher(this, mUserVk);
        mUserGitWatcher = new EditTextWatcher(this, mUserGit);

        if (savedInstanceState == null) {
            // активити запускается впервые
        } else {
            // активити уже создавалось
            mCurrentEditMode = savedInstanceState.getInt(ConstantManager.EDIT_MODE_KEY, 0);
            changeEditMode(mCurrentEditMode);
        }
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

    /**
     * Обрабатывает пользовательские нажатия
     *
     * @param v
     */
    @Override
    @SuppressWarnings("deprecation")
    @OnClick({R.id.fab, R.id.profile_placeholder, R.id.call_phone_iv, R.id.send_email_iv, R.id.open_vk_iv, R.id.open_git_iv})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                if (mCurrentEditMode == 0) {
                    changeEditMode(1);
                    mCurrentEditMode = 1;
                } else {
                    changeEditMode(0);
                    mCurrentEditMode = 0;
                }
                break;

            case R.id.profile_placeholder:
                showDialog(ConstantManager.LOAD_PROFILE_PHOTO);
                break;

            case R.id.call_phone_iv:
                dialPhone(mUserPhone.getText().toString());
                break;

            case R.id.send_email_iv:
                sendMail(mUserMail.getText().toString());
                break;

            case R.id.open_vk_iv:
                openLinkWeb(mUserVk.getText().toString());
                break;

            case R.id.open_git_iv:
                openLinkWeb(mUserGit.getText().toString());
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ConstantManager.EDIT_MODE_KEY, mCurrentEditMode);
    }

    /**
     * Кнопка Назад - закрыть меню, если открыто
     */
    @Override
    public void onBackPressed() {
        if (mNavigationDrawer != null && mNavigationDrawer.isDrawerOpen(GravityCompat.START)) {
            mNavigationDrawer.closeDrawer(GravityCompat.START);
        } else if (mCurrentEditMode == 1) {
            changeEditMode(0);
            mCurrentEditMode = 0;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mNavigationDrawer.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Получение результата из другой Activity (фото из камеры или галлерии)
     *
     * @param requestCode - код запроса
     * @param resultCode  - код результата
     * @param data        - данные
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ConstantManager.REQUEST_GALLERY_PICTURE:
                if (resultCode == RESULT_OK && data != null) {
                    mSelectedImage = data.getData();

                    insertProfileImage(mSelectedImage);
                    uploadPhoto(getFileFromUri(mSelectedImage));
                }
                break;

            case ConstantManager.REQUEST_CAMERA_PICTURE:
                if (resultCode == RESULT_OK && mPhotoFile != null) {
                    mSelectedImage = Uri.fromFile(mPhotoFile);

                    insertProfileImage(mSelectedImage);
                    uploadPhoto(mPhotoFile);
                }
                break;
        }
    }

    /**
     * Обработка полученных разрешений
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ConstantManager.REQUEST_PERMISSION_CAMERA_CODE:
                if (grantResults.length == 2 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: permissions camera granted");
                    loadPhotoFromCamera();
                }
                break;
            case ConstantManager.REQUEST_PERMISSIONS_READ_SDCARD_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: permissions read sdcard granted");
                    loadPhotoFromGallery();
                }
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

    /**
     * Выполнить настройки тулбара
     */
    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();

        mAppBarParams = (AppBarLayout.LayoutParams) mCollapsingToolbar.getLayoutParams();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Выполнить настройки меню
     */
    private void setupDrawer() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (navigationView != null) {
            navigationView.setCheckedItem(R.id.user_profile_menu);

            // Инициализация меню navigation view
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem item) {
                    item.setChecked(true);

                    switch (item.getItemId()) {
                        case R.id.user_profile_menu:
                            break;

                        case R.id.team_menu:
                            Intent teamActivity = new Intent(MainActivity.this, UserListActivity.class);
                            finish();
                            startActivity(teamActivity);
                            break;

                        case R.id.exit_menu:
                            mDataManager.getPreferencesManager().saveAuthToken("");
                            mDataManager.getPreferencesManager().saveUserId("");
                            Intent authActivity = new Intent(MainActivity.this, AuthActivity.class);
                            finish();
                            startActivity(authActivity);
                            break;
                    }
                    mNavigationDrawer.closeDrawer(GravityCompat.START);
                    return false;
                }
            });

            // Загружаем имя пользователя, емейл
            TextView userEmail = (TextView) navigationView.getHeaderView(0).findViewById(R.id.user_email_txt);
            TextView userName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.user_name_txt);
            userName.setText(mDataManager.getPreferencesManager().getFullName());
            userEmail.setText(mDataManager.getPreferencesManager().loadUserInfoData().get(1));

            // Загружаем фото аватар, для скругления используем трансформацию
            ImageView userAvatar = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.user_avatar_img);
            DataManager.getInstance().getPicasso()
                    .load(mDataManager.getPreferencesManager().loadUserAvatar())
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_account)
                    .transform(new CircleTransform())
                    .into(userAvatar);
        }
    }

    /**
     * Переключает режим редактирования
     *
     * @param mode 1-режим редактирования, 0-режим просмотра
     */
    @SuppressWarnings("deprecation")
    private void changeEditMode(int mode) {
        if (mode == 1) {
            mFab.setImageResource(R.drawable.ic_done);

            ButterKnife.apply(mUserInfoViews, ENABLED);
            ButterKnife.apply(mUserActions, DISABLE);

            mUserPhone.addTextChangedListener(mUserPhoneWatcher);
            mUserMail.addTextChangedListener(mUserMailWatcher);
            mUserVk.addTextChangedListener(mUserVkWatcher);
            mUserGit.addTextChangedListener(mUserGitWatcher);

            showProfilePlaceholder();
            lockToolbar();

            mCollapsingToolbar.setExpandedTitleColor(Color.TRANSPARENT);

            mUserPhone.requestFocus();
        } else {
            mFab.setImageResource(R.drawable.ic_mode_edit);

            ButterKnife.apply(mUserInfoViews, DISABLE);
            ButterKnife.apply(mUserActions, ENABLED);

            mUserPhone.removeTextChangedListener(mUserPhoneWatcher);
            mUserMail.removeTextChangedListener(mUserMailWatcher);
            mUserVk.removeTextChangedListener(mUserVkWatcher);
            mUserGit.removeTextChangedListener(mUserGitWatcher);

            hideProfilePlaceholder();
            unlockToolbar();

            mCollapsingToolbar.setExpandedTitleColor(getResources().getColor(R.color.white));

            saveUserInfoData();

            sendUserContactsInServer();
        }
    }

    /**
     * Загрузить пользовательские данные
     */
    private void initUserInfoData() {
        // Загрузить данные пользователя: ФИО, телефон, емейл, ВК, github
        List<String> userInfo = mDataManager.getPreferencesManager().loadUserInfoData();
        for (int i = 0; i < userInfo.size(); i++) {
            mUserInfoViews.get(i).setText(userInfo.get(i));
        }
        // Загрузить данные профиля: рейтинг, количество проектов и строк кода
        List<String> userProfile = mDataManager.getPreferencesManager().loadUserProfileData();
        for (int i = 0; i < userProfile.size(); i++) {
            mUserValueViews.get(i).setText(userProfile.get(i));
        }

        // Загрузить фото пользователя
        DataManager.getInstance().getPicasso()
                .load(mDataManager.getPreferencesManager().loadUserPhoto())
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .fit()
                .centerCrop()
                .placeholder(R.drawable.user_bg)
                .into(mProfileImage);
    }

    /**
     * Сохранить пользовательские данные после редактирования
     */
    private void saveUserInfoData() {
        List<String> userInfo = new ArrayList<>();
        for (EditText userFieldView : mUserInfoViews) {
            userInfo.add(userFieldView.getText().toString());
        }
        mDataManager.getPreferencesManager().saveUserInfoData(userInfo);
    }

    /**
     * Запустить приложение для звонка
     *
     * @param number - номер для звонка
     */
    private void dialPhone(String number) {
        Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
        startActivity(dialIntent);
    }

    /**
     * Запустить приложение для отправки почты
     *
     * @param mail - электронный адрес
     */
    private void sendMail(String mail) {
        Intent sendMailIntent = new Intent(Intent.ACTION_SEND);
        sendMailIntent.setType("text/html");
        sendMailIntent.putExtra(Intent.EXTRA_EMAIL, mail);
        sendMailIntent.putExtra(Intent.EXTRA_SUBJECT, "Content subject");
        sendMailIntent.putExtra(Intent.EXTRA_TEXT, "Content text");

        try {
            startActivity(Intent.createChooser(sendMailIntent, "Send Email"));
        } catch (android.content.ActivityNotFoundException ex) {
            showSnackbar(getString(R.string.error_app_email_not_available));
        }
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

    /**
     * Загрузить фото из галерии
     */
    private void loadPhotoFromGallery() {
        // Проверка наличия разрешения на чтение с карты памяти
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent takeGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            takeGalleryIntent.setType("image/*");
            startActivityForResult(Intent.createChooser(takeGalleryIntent, getString(R.string.user_profile_dialog_choose_message)), ConstantManager.REQUEST_GALLERY_PICTURE);
        } else {
            // Запрос необходимых разрешений у пользователя
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, ConstantManager.REQUEST_PERMISSIONS_READ_SDCARD_CODE);
            Snackbar.make(mCoordinatorLayout, R.string.info_give_permission, Snackbar.LENGTH_LONG)
                    .setAction(R.string.info_action_granted, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openApplicationSettings();
                        }
                    }).show();
        }
    }

    /**
     * Загрузить фото с камеры (сделать снимок)
     */
    private void loadPhotoFromCamera() {
        // Проверка наличия разрешений на использование камеры и запись на карту памяти
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            Intent takeCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            try {
                mPhotoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "loadPhotoFromCamera: " + e.getMessage());
            }

            if (mPhotoFile != null) {
                // передача фотофайла в интент
                takeCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFile));
                startActivityForResult(takeCaptureIntent, ConstantManager.REQUEST_CAMERA_PICTURE);
            }
        } else {
            // Запрос необходимых разрешений у пользователя
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, ConstantManager.REQUEST_PERMISSION_CAMERA_CODE);

            Snackbar.make(mCoordinatorLayout, R.string.info_give_permission, Snackbar.LENGTH_LONG)
                    .setAction(R.string.info_action_granted, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openApplicationSettings();
                        }
                    }).show();
        }
    }

    /**
     * Спрятать фото профиля
     */
    private void hideProfilePlaceholder() {
        mProfilePlaceholder.setVisibility(View.GONE);
    }

    /**
     * Показать фото профиля
     */
    private void showProfilePlaceholder() {
        mProfilePlaceholder.setVisibility(View.VISIBLE);
    }

    /**
     * Заблокировать тулбар
     */
    private void lockToolbar() {
        mAppBarLayout.setExpanded(true, true);
        mAppBarParams.setScrollFlags(0);
        mCollapsingToolbar.setLayoutParams(mAppBarParams);
    }

    /**
     * Разблокировать тулбар
     */
    private void unlockToolbar() {
        mAppBarParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
        mCollapsingToolbar.setLayoutParams(mAppBarParams);
    }

    /**
     * Создать диалог выбора откуда брать фото
     *
     * @param id
     * @return
     */
    @Override
    @SuppressWarnings("deprecation")
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case ConstantManager.LOAD_PROFILE_PHOTO:
                String[] selectItems = {getString(R.string.user_profile_dialog_gallery),
                        getString(R.string.user_profile_dialog_camera),
                        getString(R.string.user_profile_dialog_cancel)};

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.user_profile_dialog_title));
                builder.setItems(selectItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int choiceItem) {
                        switch (choiceItem) {
                            case 0:
                                loadPhotoFromGallery();
                                break;
                            case 1:
                                loadPhotoFromCamera();
                                break;
                            case 2:
                                dialog.cancel();
                                break;
                        }
                    }
                });
                return builder.create();

            default:
                return null;
        }
    }

    /**
     * Создать файл для сохранения снимка с камеры
     *
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "images/jpeg");
        values.put(MediaStore.Images.Media.DATA, image.getAbsolutePath());

        this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        return image;
    }

    /**
     * Вставить фото в профиль
     *
     * @param selectedImage - фото
     */
    private void insertProfileImage(Uri selectedImage) {
        DataManager.getInstance().getPicasso()
                .load(selectedImage)
                .resize(getResources().getDimensionPixelSize(R.dimen.profile_image_size), getResources().getDimensionPixelSize(R.dimen.profile_image_size))
                .centerInside()
                .placeholder(R.drawable.user_bg)
                .into(mProfileImage);

        mDataManager.getPreferencesManager().saveUserPhoto(selectedImage);
    }

    /**
     * Открыть настройки приложения
     */
    public void openApplicationSettings() {
        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));

        startActivityForResult(appSettingsIntent, ConstantManager.REQUEST_PERMISSION_SETTINGS_CODE);
    }

    /**
     * Загрузить фото на сервер
     *
     * @param file - фото
     */
    private void uploadPhoto(File file) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("photo", file.getName(), requestFile);

        if (NetworkStatusChecker.isNetworkAvailable(this)) {
            Call<UploadPhotoRes> call = mDataManager.getRestService().uploadPhoto(
                    mDataManager.getPreferencesManager().getUserId(), body);
            call.enqueue(new Callback<UploadPhotoRes>() {
                @Override
                public void onResponse(Call<UploadPhotoRes> call, Response<UploadPhotoRes> response) {
                    try {
                        if (response.code() == 200) {
                            showSnackbar(getString(R.string.user_profile_photo_upload_ok));
                        } else {
                            showSnackbar(getString(R.string.user_profile_photo_upload_error));
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        Log.e(TAG, "onResponseUploadPhoto: " + e.toString());
                        showSnackbar(e.toString());
                    }
                }

                @Override
                public void onFailure(Call<UploadPhotoRes> call, Throwable t) {
                    Log.e(TAG, "onFailureUploadPhoto:" + t.getMessage());
                    showSnackbar("t.getMessage()");
                }
            });
        } else {
            showSnackbar(getString(R.string.error_network_not_available));
        }
    }

    /**
     * Загрузить аватар на сервер
     *
     * @param file - фото
     */
    private void uploadAvatar(File file) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("avatar", file.getName(), requestFile);

        if (NetworkStatusChecker.isNetworkAvailable(this)) {
            Call<UploadPhotoRes> call = mDataManager.getRestService().uploadPhoto(
                    mDataManager.getPreferencesManager().getUserId(), body);
            call.enqueue(new Callback<UploadPhotoRes>() {
                @Override
                public void onResponse(Call<UploadPhotoRes> call, Response<UploadPhotoRes> response) {
                    try {
                        if (response.code() == 200) {
                            showSnackbar(getString(R.string.user_profile_photo_upload_ok));
                        } else {
                            showSnackbar(getString(R.string.user_profile_photo_upload_error));
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        Log.e(TAG, "onResponseSignIn: " + e.toString());
                        showSnackbar(e.toString());
                    }
                }

                @Override
                public void onFailure(Call<UploadPhotoRes> call, Throwable t) {
                    Log.e(TAG, "onFailureUploadPhoto:" + t.getMessage());
                    showSnackbar("t.getMessage()");
                }
            });
        } else {
            showSnackbar(getString(R.string.error_network_not_available));
        }
    }

    private File getFileFromUri(Uri uri) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = this.getContentResolver().query(uri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();
        return new File(filePath);
    }

    /**
     * Отправить изменения пользовательских контактов на сервер
     */
    private void sendUserContactsInServer() {
        Log.d(TAG, "sendUserContactsInServer");

        if (NetworkStatusChecker.isNetworkAvailable(this)) {
            Call<UserContactsRes> call = mDataManager.changeUserContacts(mDataManager.getPreferencesManager().getUserId(),
                    new UserContactsReq(
                            mUserMail.getText().toString(),
                            mUserPhone.getText().toString(),
                            mUserVk.getText().toString()));

            call.enqueue(new Callback<UserContactsRes>() {
                @Override
                public void onResponse(Call<UserContactsRes> call, Response<UserContactsRes> response) {
                    try {
                        if (response.code() == 200) {
                            showSnackbar(getString(R.string.info_data_change_in_server));
                        } else if (response.code() == 401) {
                            showSnackbar(getString(R.string.error_incorrect_token));
                        } else {
                            showSnackbar(getString(R.string.error_not_response_from_server));
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        Log.e(TAG, "onResponseSendUserContactsInServer: " + e.toString());
                        showSnackbar(e.toString());
                    }
                }

                @Override
                public void onFailure(Call<UserContactsRes> call, Throwable t) {
                    Log.e(TAG, "onFailureSendUserContactsInServer:" + t.getMessage());
                    showSnackbar(t.getMessage());
                }
            });
        } else {
            showSnackbar(getString(R.string.error_network_not_available));
        }
    }
}