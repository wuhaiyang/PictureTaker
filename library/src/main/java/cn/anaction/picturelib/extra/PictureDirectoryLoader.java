/*
 * Copyright (c) 2017. danlu.com Co.Ltd. All rights reserved.
 */

package cn.anaction.picturelib.extra;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.support.v4.content.CursorLoader;

import static android.provider.MediaStore.MediaColumns.MIME_TYPE;

/**
 * author: wuhaiyang(<a href="mailto:wuhaiyang@danlu.com">wuhaiyang@danlu.com</a>)<br/>
 * version: 1.0.0<br/>
 * since: 2017-05-07 下午9:30<br/>
 * <p>
 * </p>
 */
public class PictureDirectoryLoader extends CursorLoader {

    final String[] IMAGE_PROJECTION = {
            Media._ID,
            MediaStore.Images.Media.DATA,
            Media.BUCKET_ID,
            Media.BUCKET_DISPLAY_NAME,
            Media.DATE_ADDED,
            Media.SIZE
    };

    public PictureDirectoryLoader(Context context, boolean showGif) {
        super(context);
        setProjection(IMAGE_PROJECTION);
        setUri(Media.EXTERNAL_CONTENT_URI);
        setSortOrder(Media.DATE_ADDED + " DESC");

        setSelection(
                MIME_TYPE + "=? or " + MIME_TYPE + "=? or " + MIME_TYPE + "=? " + (showGif ? ("or " + MIME_TYPE + "=?") : ""));
        String[] selectionArgs;
        if (showGif) {
            selectionArgs = new String[]{"image/jpeg", "image/png", "image/jpg", "image/gif"};
        } else {
            selectionArgs = new String[]{"image/jpeg", "image/png", "image/jpg"};
        }
        setSelectionArgs(selectionArgs);
    }

    public PictureDirectoryLoader(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);
    }


}
