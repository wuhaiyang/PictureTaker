/*
 * Copyright (c) 2017. danlu.com Co.Ltd. All rights reserved.
 */

package cn.anaction.picturelib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.TextUtils;

/**
 * author: wuhaiyang(<a href="mailto:wuhaiyang@danlu.com">wuhaiyang@danlu.com</a>)<br/>
 * version: 1.0.0<br/>
 * since: 2017-05-04 下午10:49<br/>
 * <p>
 * <p>
 * 内容描述区域<br/>
 * </p>
 */
public class PictureTaker {

    private static final int RC_TAKE_PICTURE_FROM_CAMERA = 0x9001;
    private static final int RC_TAKE_PICTURE_FROM_GALLERY = 0X9002;
    private static final int RC_TAKE_PICTURE_FROM_DOCUMENT = 0X9003;
    private static final int RC_TAKE_PICTURE_FROM_CROP = 0X9004;

    // 如果后期参数过多 可以参照系统AlertControllr的做法
    private Uri outPutUri;
    private Uri tmpUri;
    private TakeResultListener listener;
    private boolean isDevelopMode = true;
    private CropOptions cropOptions;
    private Activity context;

    private String getExceptionMsg(String detailMsg) {
        return isDevelopMode ? detailMsg : "";
    }

    public void onCreate(Bundle savedInstanceState) {
        if (null != savedInstanceState) {
            onRestoreInstanceState(savedInstanceState);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("cropOptions", cropOptions);
        outState.putParcelable("outPutUri", outPutUri);
        outState.putParcelable("tmpUri", tmpUri);
        outState.putBoolean("isDevelopMode", isDevelopMode);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        cropOptions = (CropOptions) savedInstanceState.getSerializable("cropOptions");
        outPutUri = savedInstanceState.getParcelable("outPutUri");
        tmpUri = savedInstanceState.getParcelable("tmpUri");
        isDevelopMode = savedInstanceState.getBoolean("isDevelopMode");
    }

    public PictureTaker(TakeResultListener listener, Activity context) {
        this.listener = listener;
        this.context = context;
    }

    private void setCropOptions(CropOptions cropOptions) {
        this.cropOptions = cropOptions;
    }

    public void setContext(Activity context) {
        this.context = context;
    }

    public void setDevelopMode(boolean developMode) {
        isDevelopMode = developMode;
    }

    public void setListener(TakeResultListener listener) {
        this.listener = listener;
    }


    /**
     * 调用系统相机进行拍照,调用之前请先检查权限相关
     */
    public void takePicFromCamera(@NonNull Uri outPutUri) {
        // 先检查相机应用是否存在
        Intent cameraIntent = getCameraIntent(outPutUri);
        if (!Utils.checkIntentExit(context, cameraIntent)) {
            // 无相机应用
            return;
        }
        ;
        if (Build.VERSION.SDK_INT >= 23) {
            this.tmpUri = Utils.convertFileUriToFileProviderUri(context, outPutUri);
        } else {
            this.tmpUri = outPutUri;
        }
        this.cropOptions = null;
        this.outPutUri = outPutUri;
        context.startActivityForResult(getCameraIntent(tmpUri), RC_TAKE_PICTURE_FROM_CAMERA);
    }

    public void takePicFromCamera(@NonNull Uri outPutUri, CropOptions options) {
        takePicFromCamera(outPutUri);
        this.outPutUri = outPutUri;
        this.cropOptions = options;
    }

    public void takePicFromGallery() {
        Intent intent = getPickIntentWithGallery();
        if (!Utils.checkIntentExit(context, intent)) return;
        this.cropOptions = null;
        this.outPutUri = null;
        context.startActivityForResult(intent, RC_TAKE_PICTURE_FROM_GALLERY);
    }

    public void takePicFromGallery(Uri outPutUri, CropOptions options) {
        takePicFromGallery();
        this.outPutUri = outPutUri;
        this.cropOptions = options;
    }

    public void takePicFromDocument() {
        // 检查权限
        Intent intent = getPickIntentWithDocuments();
        if (!Utils.checkIntentExit(context, intent)) return;
        this.cropOptions = null;
        this.outPutUri = null;
        context.startActivityForResult(intent, RC_TAKE_PICTURE_FROM_DOCUMENT);
    }

    public void takePicFromDocument(Uri outPutUri, CropOptions options) {
        takePicFromDocument();
        this.cropOptions = options;
        this.outPutUri = outPutUri;
    }

    public void cropPic(Uri targetUri, Uri outPutUri) {
        // 检查权限
        Intent cropIntent = getCropIntent(targetUri, outPutUri, cropOptions);
        if (!Utils.checkIntentExit(context, cropIntent)) {
            // 无裁剪应用
            listener.takeError();
        }
        context.startActivityForResult(cropIntent, RC_TAKE_PICTURE_FROM_CROP);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_TAKE_PICTURE_FROM_GALLERY:
                proccessTakePicFromFile(data, context, resultCode, true);
                break;
            case RC_TAKE_PICTURE_FROM_CAMERA:
                if (resultCode == Activity.RESULT_OK) {
                    if (null != tmpUri) {
                        try {
                            ImageRotateCorrector.of().correctImage(context, tmpUri);
                            if (null != cropOptions) {
                                cropPic(tmpUri, outPutUri);
                            } else {
                                String pathByUri = Utils.getFilePathByUri(tmpUri, context);
                                if (!TextUtils.isEmpty(pathByUri)) {
                                    listener.takeSuccess(pathByUri);
                                } else {
                                    // 根据uri 获取文件真实路径错误
                                    listener.takeError();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            // 拍照后 图片旋转角度纠正过程出现异常
                            listener.takeError();
                        }
                    } else {
                        // 拍照后图片输出路径为null
                        listener.takeError();
                    }
                } else {
                    listener.takeCancel();
                }
                break;
            case RC_TAKE_PICTURE_FROM_DOCUMENT:
                proccessTakePicFromFile(data, context, resultCode, false);
                break;
            case RC_TAKE_PICTURE_FROM_CROP:
                if (resultCode == Activity.RESULT_OK) {
                    if (null != outPutUri) {
                        String path = Utils.getFilePathByUri(outPutUri, context);
                        if (!TextUtils.isEmpty(path)) {
                            listener.takeSuccess(path);
                        } else {
                            // 获取裁剪后的路径失败
                            listener.takeError();
                        }
                    } else {
                        // 获取裁剪后的路径失败
                        listener.takeError();
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    if (null != data) {
                        Bitmap bitmpa = data.getParcelableExtra("data");// 获取裁剪的结果数据
                        Utils.writeToFile(bitmpa, outPutUri);
                        listener.takeSuccess(outPutUri.getPath());
                    } else {
                        listener.takeCancel();
                    }
                } else {
                    listener.takeCancel();
                }
                break;
        }
    }

    /**
     * 处理从相册 || 文件中 选择后的结果
     *
     * @param data
     * @param context
     * @param resultCode
     * @param isFromGallery
     */
    private void proccessTakePicFromFile(Intent data, Context context, int resultCode, boolean isFromGallery) {
        if (resultCode == Activity.RESULT_OK) {
            if (null == data){
                listener.takeError();
                return;
            }
            Uri outPutUri = data.getData();
            if (null == outPutUri) {
                // 选取相册 || 文件  获取的Uri为null
                listener.takeError();
                return;
            }
            if (null != cropOptions) {
                cropPic(outPutUri, this.outPutUri);
                return;
            }
            String path = null;
            if (isFromGallery) {
                path = Utils.getFilePathByUri(outPutUri, context);
            } else {
                try {
                    path = Utils.getFilePahtByDocumentsUri(outPutUri, context);
                } catch (Exception e) {
                    e.printStackTrace();
                    // 从文件中获取图片，uri转文件路径 异常
                    listener.takeError();
                }
            }
            if (TextUtils.isEmpty(path)) {
                // 选取图库 || 文件 后，uri 转文件路径异常
                listener.takeError();
                return;
            } else {
                listener.takeSuccess(path);
            }
        } else {
            // cance
            listener.takeCancel();
        }
    }


    private Intent getCameraIntent(Uri uri) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE); //
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        return intent;
    }

    /**
     * 获取选择照片Intent
     *
     * @return
     */
    private Intent getPickIntentWithGallery() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");//从所有图片中进行选择
        return intent;
    }

    /**
     * 获取从文件中选择照片Intent
     *
     * @return
     */
    private Intent getPickIntentWithDocuments() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        return intent;
    }

    /**
     * 获取裁剪照片的Intent
     *
     * @param targetUri 要裁剪的照片
     * @param outPutUri 裁剪完成的照片
     * @param options   裁剪配置
     * @return
     */
    public static Intent getCropIntent(Uri targetUri, Uri outPutUri, CropOptions options) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(targetUri, "image/*");
        intent.putExtra("crop", "true");
        if (options.getAspectX() * options.getAspectY() > 0) {
            intent.putExtra("aspectX", options.getAspectX());
            intent.putExtra("aspectY", options.getAspectY());
        }
        if (options.getOutputX() * options.getOutputY() > 0) {
            intent.putExtra("outputX", options.getOutputX());
            intent.putExtra("outputY", options.getOutputY());
        }
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outPutUri);
        intent.putExtra("return-data", true);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        return intent;
    }


}
