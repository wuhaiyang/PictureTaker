/*
 * Copyright (c) 2017. danlu.com Co.Ltd. All rights reserved.
 */

package cn.anaction.picturelib.extra.model;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import cn.anaction.picturelib.Utils;


public class PhotoDirectory {

  private String id;
  private String coverPath;
  private String name;
  private long   dateAdded;
  private List<Photo> photos = new ArrayList<>();

  /**
   *  name && id 均相等
   * @param o
   * @return
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PhotoDirectory)) return false;

    PhotoDirectory directory = (PhotoDirectory) o;

    boolean hasId = !TextUtils.isEmpty(id);
    boolean otherHasId = !TextUtils.isEmpty(directory.id);

    if (hasId && otherHasId) {
      if (!TextUtils.equals(id, directory.id)) {
        return false;
      }

      return TextUtils.equals(name, directory.name);
    }

    return false;
  }

  @Override
  public String toString() {
    return "PhotoDirectory{" +
            "id='" + id + '\'' +
            ", coverPath='" + coverPath + '\'' +
            ", name='" + name + '\'' +
            ", dateAdded=" + dateAdded +
            ", photos=" + photos +
            '}';
  }

  @Override public int hashCode() {
    if (TextUtils.isEmpty(id)) {
      if (TextUtils.isEmpty(name)) {
        return 0;
      }

      return name.hashCode();
    }

    int result = id.hashCode();

    if (TextUtils.isEmpty(name)) {
      return result;
    }

    result = 31 * result + name.hashCode();
    return result;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getCoverPath() {
    return coverPath;
  }

  public void setCoverPath(String coverPath) {
    this.coverPath = coverPath;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getDateAdded() {
    return dateAdded;
  }

  public void setDateAdded(long dateAdded) {
    this.dateAdded = dateAdded;
  }

  public List<Photo> getPhotos() {
    return photos;
  }

  public void setPhotos(List<Photo> photos) {
    if (photos == null) return;
    // 过滤无效文件
    for (int i = 0, j = 0, num = photos.size(); i < num; i++) {
        Photo p = photos.get(j);
        if (p == null || !Utils.fileIsExists(p.getPath())) {
            photos.remove(j);
        } else {
            j++;
        }
    }
    this.photos = photos;
  }

  /**
   * 获取文件夹下的所有文件路径
   * @return
   */
  public List<String> getPhotoPaths() {
    List<String> paths = new ArrayList<>(photos.size());
    for (Photo photo : photos) {
      paths.add(photo.getPath());
    }
    return paths;
  }

  public void addPhoto(int id, String path) {
    if (Utils.fileIsExists(path)) {
      photos.add(new Photo(id, path));
    }
  }

}
