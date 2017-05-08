/*
 * Copyright (c) 2017. danlu.com Co.Ltd. All rights reserved.
 */

package cn.anaction.picturelib.extra;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import java.util.ArrayList;
import java.util.List;

import cn.anaction.picturelib.R;
import cn.anaction.picturelib.extra.model.PhotoDirectory;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME;
import static android.provider.MediaStore.Images.ImageColumns.BUCKET_ID;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;
import static android.provider.MediaStore.MediaColumns.SIZE;

/**
 * author: wuhaiyang(<a href="mailto:wuhaiyang@danlu.com">wuhaiyang@danlu.com</a>)<br/>
 * version: 1.0.0<br/>
 * since: 2017-05-07 下午6:32<br/>
 * <p>
 * </p>
 */
public class MediaStoreHelper {

    public static final String KEY_GIF = "KEY_SHOWGIF";

    private static final int LOADER_ID = 582;

    public static void getPictureDir(@NonNull FragmentActivity activity, PictureResultCallback callback,Bundle args) {
        activity.getSupportLoaderManager()
                .initLoader(LOADER_ID, args, new PictureDirLoaderCallbacks(activity,callback));
    }

    private static class PictureDirLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        private Context context;
        private PictureResultCallback callback;

        public PictureDirLoaderCallbacks(Context context, PictureResultCallback callback) {
            this.context = context;
            this.callback = callback;
        }
        /**
         * 实例化并返回一个新创建给定ID的Loader对象；
         * @param id
         * @param args
         * @return
         */
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new PictureDirectoryLoader(context,args.getBoolean(KEY_GIF,false));
        }
        /**
         * 当创建好的Loader 完成了数据的load之后回调此方法
         * @param loader
         * @param data
         */
        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (null == data) return;


            List<PhotoDirectory> directories = new ArrayList<>();

            PhotoDirectory photoDirectoryAll = new PhotoDirectory();
            photoDirectoryAll.setName(context.getString(R.string.__picker_all_image));
            photoDirectoryAll.setId("ALL");


            while (data.moveToNext()){
                int imageId  = data.getInt(data.getColumnIndexOrThrow(_ID));
                String bucketId = data.getString(data.getColumnIndexOrThrow(BUCKET_ID));
                String name = data.getString(data.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME));
                String path = data.getString(data.getColumnIndexOrThrow(DATA));
                long size = data.getInt(data.getColumnIndexOrThrow(SIZE));
                if (size < 1) continue;

                PhotoDirectory photoDirectory = new PhotoDirectory();
                photoDirectory.setId(bucketId);
                photoDirectory.setName(name);


                if (!directories.contains(photoDirectory)){
                    photoDirectory.setCoverPath(path);
                    photoDirectory.addPhoto(imageId,path);
                    photoDirectory.setDateAdded(data.getLong(data.getColumnIndexOrThrow(DATE_ADDED)));
                    directories.add(photoDirectory);
                } else {
                    directories.get(directories.indexOf(photoDirectory)).addPhoto(imageId,path);
                }

                photoDirectoryAll.addPhoto(imageId,path);
            }
            if (photoDirectoryAll.getPhotoPaths().size() > 0){
                photoDirectoryAll.setCoverPath(photoDirectoryAll.getPhotoPaths().get(0));
            }
            directories.add(0,photoDirectoryAll);
            if (null != callback){
                callback.onResultCallback(directories);
            }
        }
        /**
         * 当创建好的Loader 被reset时调用此方法，这样保证它的数据无效
         * @param loader
         */
        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

    public interface PictureResultCallback {
        void onResultCallback(List<PhotoDirectory> directories);
    }

}
