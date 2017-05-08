package cn.anaction.picturetaker;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.anaction.picturelib.CropOptions;
import cn.anaction.picturelib.PictureTaker;
import cn.anaction.picturelib.TakeResultListener;
import cn.anaction.picturelib.extra.MediaStoreHelper;
import cn.anaction.picturelib.extra.model.PhotoDirectory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TakeResultListener {

    private PictureTaker taker;
    private PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_camera).setOnClickListener(this);
        findViewById(R.id.btn_document).setOnClickListener(this);
        findViewById(R.id.btn_gallery).setOnClickListener(this);
        findViewById(R.id.btn_getpicture).setOnClickListener(this);
        initPictureTaker(savedInstanceState);


        permissionManager = new PermissionManager();
    }

    private void initPictureTaker(Bundle savedInstanceState) {
        if (null == taker) {
            taker = new PictureTaker(this, this);
        }
        taker.onCreate(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        taker.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        taker.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        taker.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        File file = new File(getExternalFilesDir("picture_taker"), System.currentTimeMillis() + ".jpg");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        final Uri outPutUri = Uri.fromFile(file);
        AppCompatCheckBox acb = (AppCompatCheckBox) findViewById(R.id.accb_iscrop);
        final boolean isChecked = acb.isChecked();
        final CropOptions options = new CropOptions.Builder()
                .setAspectX(1)
                .setAspectY(1)
                .setOutputX(300)
                .setOutputY(300).create();
        if (v.getId() == R.id.btn_camera) {
            List<String> permissions = new ArrayList<>();
            permissions.add(Manifest.permission.CAMERA);
            permissionManager.performRequestPermissions(this, permissions, new PermissionManager.OnPermissionResult() {
                @Override
                public void onPermissionsGranted(List<String> permissions) {
                    if (isChecked)
                        taker.takePicFromCamera(outPutUri, options);
                    else
                        taker.takePicFromCamera(outPutUri);
                }

                @Override
                public void onPermissionDenied(List<String> permissions) {
                    logOut(permissions, "permissionDenied");
                }

                @Override
                public void onPermissionCompleteDenied(List<String> permissions) {
                    logOut(permissions, "permissionCompleteDenied");
                }
            });

        } else if (v.getId() == R.id.btn_document) {
            if (isChecked) {
                taker.takePicFromDocument(outPutUri, options);
            } else {
                taker.takePicFromDocument();
            }
        } else if (v.getId() == R.id.btn_gallery) {
            if (isChecked) {
                taker.takePicFromGallery(outPutUri, options);
            } else {
                taker.takePicFromGallery();
            }
        } else if (v.getId() == R.id.btn_getpicture){
            getPicture();
        }
    }

    private void getPicture() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(MediaStoreHelper.KEY_GIF,false);
        final ProgressDialog dialog = ProgressDialog.show(this, null, "加载中");
        MediaStoreHelper.getPictureDir(this, new MediaStoreHelper.PictureResultCallback() {
            @Override
            public void onResultCallback(List<PhotoDirectory> directories) {
                dialog.dismiss();
                for (PhotoDirectory directory : directories){
                    Log.w("@@@@ L130", "MainActivity:onResultCallback() -> " + directory);
                }
            }
        },bundle);
    }

    @Override
    public void takeError() {
        Toast.makeText(this, "错误", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void takeCancel() {
        Toast.makeText(this, "取消", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void takeSuccess(String path) {
        Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionManager.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    private void logOut(List<String> permissions, String type) {
        for (String permission : permissions) {
            Log.w("@@@@ L139", "MainActivity:logOut() -> " + type + ": " + permission);
        }
    }


}
