/*
 * Copyright (c) 2017. danlu.com Co.Ltd. All rights reserved.
 */

package cn.anaction.picturelib;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * author: wuhaiyang(<a href="mailto:wuhaiyang@danlu.com">wuhaiyang@danlu.com</a>)<br/>
 * version: 1.0.0<br/>
 * since: 2017-05-04 下午10:58<br/>
 * <p>
 * 内容描述区域<br/>
 * </p>
 */
public class Utils {

    public static boolean checkIntentExit(@NonNull Context context, Intent intent) {
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfos.isEmpty()) {
//            Toast.makeText(context,context.getResources().getString(R.string.tip_no_camera),Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public static Uri convertFileUriToFileProviderUri(@NonNull Context context, @NonNull Uri uri) {
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            //兼容7.0 以上版本
            File file = new File(uri.getPath());
            // 文件一定位于xml path 指定目录下的 子目录下面
            return FileProvider.getUriForFile(context, getFileProviderName(context), file);
        }
        return uri;
    }

    public final static String getFileProviderName(Context context) {
        return context.getPackageName() + ".fileprovider";
    }

    public static String getFilePathByUri(Uri uri, Context activity) {
        File file = getFileByUri(uri, activity);
        String path = file == null ? null : file.getPath();
        return path;
    }

    public static File getFileByUri(Uri uri, Context activity) {
        String scheme = uri.getScheme();
        String picturePath = null;
        if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            //从系统表中查询指定Uri对应的照片
            Cursor cursor = activity.getContentResolver().query(uri, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            if (columnIndex >= 0) {
                picturePath = cursor.getString(columnIndex);
            } else if (TextUtils.equals(uri.getAuthority(), getFileProviderName(activity))) {
                picturePath = parseOwnUri(activity, uri);
            }
            cursor.close();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            picturePath = uri.getPath();
        }
        return TextUtils.isEmpty(picturePath) ? null : new File(picturePath);
    }

    public static String getFilePahtByDocumentsUri(Uri uri, Context context) throws Exception {
        // android 7.0 -> content://com.android.providers.media.documents/document/image%3A201923
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) && uri.getPath().contains("document")) {
            File tempFile = getTempFile(context, uri);
            if (null == tempFile) {
                throw new NullPointerException();
            }
            inputStreamToFile(context.getContentResolver().openInputStream(uri), tempFile);
            return tempFile.getPath();
        } else {
            return getFilePathByUri(uri, context);
        }
    }

    /**
     * 获取临时文件
     * @param context
     * @param photoUri
     * @return
     */
    public static File getTempFile(Context context, Uri photoUri) {
        String minType = getMimeType(context, photoUri);
        if (!checkMimeType(context, minType)) {
            return null;
        }
        ;
        File filesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!filesDir.exists()) filesDir.mkdirs();
        File photoFile = new File(filesDir, UUID.randomUUID().toString() + "." + minType);
        return photoFile;
    }

    /**
     * 检查文件类型是否是图片
     *
     * @param minType
     * @return
     */
    public static boolean checkMimeType(Context context, String minType) {
        boolean isPicture = TextUtils.isEmpty(minType) ? false : ".jpg|.gif|.png|.bmp|.jpeg|.webp|".contains(minType.toLowerCase()) ? true : false;
//        if (!isPicture) Toast.makeText(context,context.getResources().getText(R.string.tip_type_not_image),Toast.LENGTH_SHORT).show();
        return isPicture;
    }

    /**
     * To find out the extension of required object in given uri
     * Solution by http://stackoverflow.com/a/36514823/1171484
     */
    public static String getMimeType(Context context, Uri uri) {
        String extension;
        //Check uri format to avoid null
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            //If scheme is a content
            extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(context.getContentResolver().getType(uri));
            if (TextUtils.isEmpty(extension))
                extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
            if (TextUtils.isEmpty(extension))
                extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(context.getContentResolver().getType(uri));
        }
        if (TextUtils.isEmpty(extension)) {
            extension = getMimeTypeByFileName(getFileByUri(uri, context).getName());
        }
        return extension;
    }

    public static String getMimeTypeByFileName(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."), fileName.length());
    }

    /**
     * InputStream 转File
     */
    public static void inputStreamToFile(InputStream is, File file) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024 * 10];
            int i;
            while ((i = is.read(buffer)) != -1) {
                fos.write(buffer, 0, i);
            }
        } catch (IOException e) {
            throw new IOException();
        } finally {
            try {
                fos.flush();
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将TakePhoto 提供的Uri 解析出文件绝对路径
     *
     * @param uri
     * @return
     */
    public static String parseOwnUri(Context context, Uri uri) {
        if (uri == null) return null;
        String path;
        if (TextUtils.equals(uri.getAuthority(), getFileProviderName(context))) {
            path = new File(uri.getPath().replace("camera_photos/", "")).getAbsolutePath();
        } else {
            path = uri.getPath();
        }
        return path;
    }
    /**
     * 将bitmap写入到文件
     *
     * @param bitmap
     */
    public static void writeToFile(Bitmap bitmap, Uri imageUri) {
        if (bitmap == null) return;
        File file = new File(imageUri.getPath());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(bos.toByteArray());
            bos.flush();
            fos.flush();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) try {
                fos.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 判断SD卡是否被挂载
    public static boolean isSDCardMounted() {
        // return Environment.getExternalStorageState().equals("mounted");
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    public static boolean fileIsExists(String path) {
        if (path == null || path.trim().length() <= 0) {
            return false;
        }
        try {
            File f = new File(path);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
