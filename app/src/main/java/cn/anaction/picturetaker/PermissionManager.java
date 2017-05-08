/*
 * Copyright (c) 2017. danlu.com Co.Ltd. All rights reserved.
 */

package cn.anaction.picturetaker;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * author: wuhaiyang(<a href="mailto:wuhaiyang@danlu.com">wuhaiyang@danlu.com</a>)<br/>
 * version: 1.0.0<br/>
 * since: 2017-05-06 下午9:29<br/>
 * <p>
 * </p>
 */
public class PermissionManager {

    public static final int RC_PERMISSION_GROUP = 0X8001;
    public static final int RC_PERMISSION_SINGLE = 0X8002;

    private OnPermissionResult listener;

    public void performRequestPermissions(Activity context, @NonNull List<String> permissions, OnPermissionResult listener) {
        if (permissions.isEmpty()) return;
        this.listener = listener;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            List<String> unGrantedPermissions = getUnGrantedPermissions(context, permissions);
            if (!unGrantedPermissions.isEmpty()) {
                if (isExistRationaleOfPermission(context, unGrantedPermissions)) {
                    pollingRationaleDialg(context, unGrantedPermissions);
                } else {
                    ActivityCompat.requestPermissions(context, unGrantedPermissions.toArray(new String[unGrantedPermissions.size()]), RC_PERMISSION_GROUP);
                }
            } else {
                // granted
                listener.onPermissionsGranted(permissions);
            }
        } else {
            //granted
            listener.onPermissionsGranted(permissions);
        }
    }

    @Deprecated
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void pollingRationaleDialg(final Activity activity, final List<String> permissions) {
        if (permissions.isEmpty()) return;
        final String lastPermission = permissions.remove(permissions.size() - 1);
        if (activity.shouldShowRequestPermissionRationale(lastPermission)) {
            new AlertDialog.Builder(activity)
                    .setTitle("提示")
                    .setMessage("需要申请" + lastPermission + "权限")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            List<String> singlePermissions = new ArrayList<>();
                            singlePermissions.add(lastPermission);
                            ActivityCompat.requestPermissions(activity, singlePermissions.toArray(new String[1]), RC_PERMISSION_SINGLE);
                            pollingRationaleDialg(activity, permissions);
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    pollingRationaleDialg(activity, permissions);
                }
            }).show();
        } else {
            pollingRationaleDialg(activity, permissions);
        }
    }

    public boolean isExistRationaleOfPermission(Activity activity, @NonNull List<String> permissions) {
        for (String permission : permissions) {
            // 权限第一次申请时返回FALSE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (activity.shouldShowRequestPermissionRationale(permission)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<String> getUnGrantedPermissions(Context context, @NonNull List<String> permissions) {
        List<String> unGrantedPermissions = new ArrayList<>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                unGrantedPermissions.add(permission);
            }
        }

        return unGrantedPermissions;
    }

    public boolean checkEachPermissionIsGranted(Context context, @NonNull String[] permissions) {
        List<String> unGrantedPermissions = getUnGrantedPermissions(context, Arrays.asList(permissions));
        return unGrantedPermissions.isEmpty();
    }

    public void onRequestPermissionsResult(Activity context, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RC_PERMISSION_GROUP:
            case RC_PERMISSION_SINGLE:
                if (checkEachPermissionIsGranted(context, permissions)) {
                    listener.onPermissionsGranted(Arrays.asList(permissions));
                } else {
                    List<String> unGrantedPermissions = getUnGrantedPermissions(context, Arrays.asList(permissions));
                    List<String> neverShowAgainPermissions = new ArrayList<>();
                    for (String permission : unGrantedPermissions) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (!context.shouldShowRequestPermissionRationale(permission)) {
                                neverShowAgainPermissions.add(permission);
                            }
                        }
                    }
                    if (!neverShowAgainPermissions.isEmpty()) {
                        listener.onPermissionCompleteDenied(neverShowAgainPermissions);
                    }
                    listener.onPermissionDenied(unGrantedPermissions);
//                    if (isExistRationaleOfPermission(context,unGrantedPermissions)) {
//                        //
//                    } else {
//                        // TODO: 2017/5/6 提示用户去设置界面开启权限
//                    }
                }
                break;
        }
    }

    public interface OnPermissionResult {
        void onPermissionsGranted(List<String> permissions);

        void onPermissionDenied(List<String> permissions);

        /**
         * 提示用户去设置界面打开权限
         *
         * @param permissions
         */
        void onPermissionCompleteDenied(List<String> permissions);
    }


    //shouldShowRequestPermissionRationale true 只有一种情况 首次请求权限时拒绝了，下次调用该方法 返回true,其余情况均返回FALSE


}
