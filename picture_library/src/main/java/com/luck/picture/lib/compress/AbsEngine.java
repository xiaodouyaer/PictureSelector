package com.luck.picture.lib.compress;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * @classDescription: 描述：抽象引擎，旨在能让开发者自定义自己的压缩引擎，
 * 满足对图片压缩的各样需求你
 * abstract engine,for developer custom self's engine
 * @author: LiuChaoya
 * @createTime: 2019/5/10 10:25.
 * @email: 1090969255@qq.com
 */
public abstract class AbsEngine implements Serializable {
    protected ExifInterface srcExif;
    protected String srcImg;
    protected File tagImg;
    protected int srcWidth;
    protected int srcHeight;

    public AbsEngine setSrcImg(String srcImg) {
        this.srcImg = srcImg;
        return this;
    }

    public AbsEngine setTagImg(File tagImg) {
        this.tagImg = tagImg;
        return this;
    }

    public AbsEngine() throws IOException {
        this(null, null);
    }

    public AbsEngine(String srcImg, File tagImg) throws IOException {
        if (Checker.isJPG(srcImg)) {
            this.srcExif = new ExifInterface(srcImg);
        }
        this.tagImg = tagImg;
        this.srcImg = srcImg;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;

        BitmapFactory.decodeFile(srcImg, options);
        this.srcWidth = options.outWidth;
        this.srcHeight = options.outHeight;
    }

    protected Bitmap rotatingImage(Bitmap bitmap) {
        if (srcExif == null) return bitmap;

        Matrix matrix = new Matrix();
        int angle = 0;
        int orientation = srcExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                angle = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                angle = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                angle = 270;
                break;
        }

        matrix.postRotate(angle);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * 开发者可以实现自己的压缩方式
     * developer can implement self's compress way
     *
     * @return 压缩后的文件
     * @throws IOException 可能的异常
     */
    protected abstract File compress() throws IOException;

}
