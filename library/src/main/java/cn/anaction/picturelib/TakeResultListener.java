/*
 * Copyright (c) 2017. danlu.com Co.Ltd. All rights reserved.
 */

package cn.anaction.picturelib;

/**
 * author: wuhaiyang(<a href="mailto:wuhaiyang@danlu.com">wuhaiyang@danlu.com</a>)<br/>
 * version: 1.0.0<br/>
 * since: 2017-05-05 上午12:25<br/>
 * <p>
 * </p>
 */
public interface TakeResultListener {
    void takeError();
    void takeCancel();
    void takeSuccess(String path);
}
