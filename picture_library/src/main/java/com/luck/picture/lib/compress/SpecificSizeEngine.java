package com.luck.picture.lib.compress;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;

import com.luck.picture.lib.config.PictureSelectionConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * @classDescription: 描述：
 * @author: LiuChaoya
 * @createTime: 2019/5/10 10:55.
 * @email: 1090969255@qq.com
 */
public class SpecificSizeEngine extends AbsEngine {

    private final PictureSelectionConfig config;

    public SpecificSizeEngine() throws IOException {
        this(null, null);
    }

    public SpecificSizeEngine(String srcImg, File tagImg) throws IOException {
        super(srcImg, tagImg);
        config = PictureSelectionConfig.getInstance();
    }

    @Override
    protected File compress() throws IOException {
        if (srcWidth > srcHeight) {
            if (config.zoomWidth > srcHeight || config.zoomHeight > srcWidth) {
                return new File(srcImg);
            }
        } else {
            if (config.zoomHeight > srcHeight || config.zoomWidth > srcWidth) {
                return new File(srcImg);
            }
        }
        if ((config.zoomWidth > 0 || config.zoomHeight > 0)) {
            //压缩尺寸和大小
            return compressZoom();
        } else {
            //压缩大小
            String sourcePath = srcImg;
            String outPath = tagImg.getParent();
            String outFileName = tagImg.getName();
            return compressToTargetSize(sourcePath, outPath, outFileName);
        }
    }

    /**
     * 缩放压缩至用户指定的宽高，但会比用户设置的略低，因为
     */
    private File compressZoom() throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;
        BitmapFactory.decodeFile(srcImg, options);
        options.inJustDecodeBounds = false;
        options.inSampleSize = getSize();
        //图片缩小一部分，避免Bitmap加载OOM
        Bitmap bitmap = BitmapFactory.decodeFile(srcImg, options);
        //设置缩放比，将缩小的部分算进来，为了使创建的Bitmap尺符合开发者指定的期望压缩的尺寸
        double radio = div(getRadio(), options.inSampleSize);
        Bitmap result = Bitmap.createBitmap(
                (int) (div(bitmap.getWidth(), radio)),
                (int) (div(bitmap.getHeight(), radio)),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        RectF rectF = new RectF(0, 0, (float) (bitmap.getWidth() / radio), (float) (bitmap.getHeight() / radio));
        //将原图画在缩放之后的矩形上
        canvas.drawBitmap(bitmap, null, rectF, null);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        result.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        FileOutputStream fos = new FileOutputStream(tagImg);
        fos.write(bos.toByteArray());
        fos.flush();
        fos.close();

        //指定了需要压缩到的目标大小
        if (config.pictureSize > 0) {

            String sourcePath = tagImg.getAbsolutePath();
            String outPath = tagImg.getParent();
            String outFileName = tagImg.getName();

            File resultFile = compressToTargetSize(sourcePath, outPath, outFileName);
            // 同时在尺寸和大小上进行压缩的图片将只保留压缩大小之后的图片，而作为仅仅调整尺寸的中间产物(图片)，
            // 这里将对之进行删除
            tagImg.delete();
            return resultFile;
        } else {
            return tagImg;
        }

    }

    private int getSize() {
        if (srcWidth > srcHeight) {
            return (int) Math.max(div(srcWidth, config.zoomHeight), div(srcHeight, config.zoomWidth));
        } else {
            return (int) Math.max(div(srcHeight, config.zoomHeight), div(srcWidth, config.zoomWidth));
        }
    }

    /**
     * 缩放宽高均大于0，取最大缩放比
     * 高大于0，宽等于0，取高的缩放比
     * 其余情况，取宽的缩放比
     */
    private double getRadio() {
        if (config.zoomWidth > 0 && config.zoomHeight > 0) {
            //横屏照片
            if (srcWidth > srcHeight) {
                return Math.max(div(srcHeight, config.zoomWidth), div(srcWidth, config.zoomHeight));
            } else {//正常竖屏
                return Math.max(div(srcWidth, config.zoomWidth), div(srcHeight, config.zoomHeight));
            }
        } else if (config.zoomWidth == 0 && config.zoomHeight > 0) {
            if (srcWidth > srcHeight) {
                return div(srcWidth, config.zoomHeight);
            } else {//正常竖屏
                return div(srcHeight, config.zoomHeight);
            }
        } else {
            if (srcWidth > srcHeight) {
                return div(srcHeight, config.zoomWidth);
            } else {//正常竖屏
                return div(srcWidth, config.zoomWidth);
            }
        }
    }

    /**
     * 压缩至指定的图片大小
     */
    private File compressToTargetSize(String sourcePath, String outPath, String outFileName) throws IOException {
        String outFileParent = outPath + "/compressSecond/";
        File outDir = new File(outFileParent);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        File compressedFile = new File(sourcePath);
        File outFile = null;
        int quality = 100;
        int dec = 8;
        while (Checker.isNeedCompress(config.pictureSize, compressedFile.getAbsolutePath()) && quality > dec) {
            outFile = compressedFile = new File(outDir, outFileName);
            Bitmap tagBitmap = BitmapFactory.decodeFile(sourcePath);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            quality -= dec;
            tagBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
            tagBitmap.recycle();
            FileOutputStream fos = new FileOutputStream(outFile);
            fos.write(stream.toByteArray());
            fos.flush();
            fos.close();
            stream.close();
        }
        if (outFile == null) {
            return compressedFile;
        } else {
            return outFile;
        }
    }

    private double div(double v1, double v2) {
        BigDecimal bv1 = new BigDecimal(v1);
        BigDecimal bv2 = new BigDecimal(v2);
        return bv1.divide(bv2, 20, BigDecimal.ROUND_UP).doubleValue();
    }
}
