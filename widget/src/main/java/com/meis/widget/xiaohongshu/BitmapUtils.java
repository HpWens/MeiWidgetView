package com.meis.widget.xiaohongshu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

/**
 * Created by wenshi on 2019/3/6.
 * Description
 */
public class BitmapUtils {

    public static Bitmap getCompressBitmap(Context context, String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        // 不加载到内存中
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        // 判定是否是横竖图
        boolean verEnable = options.outWidth < options.outHeight;
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        options.inSampleSize = BitmapUtils.calculateInSampleSize(options, verEnable ? screenWidth : screenHeight, verEnable ? screenHeight : screenWidth);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int maxWidth, int maxHeight) {
        int outHeight = options.outHeight;
        int outWidth = options.outWidth;
        int simple = 1;
        if (outHeight > maxHeight || outWidth > maxWidth) {
            int tempHeght = outHeight / 2;
            for (int tempWidth = outWidth / 2; tempHeght / simple > maxHeight && tempWidth / simple > maxWidth; simple *= 2) {
            }
        }
        return simple;
    }


    /**
     * 可以根据图片的平移缩放获取裁剪后的bitmap
     *
     * @param context
     * @param path
     * @param verticalCrop
     * @return
     */
    public static Bitmap getFixedBitmap(Context context, String path, boolean verticalCrop) {
        // 获取控件的宽度和高度
        int viewWidth = 0;
        int viewHeight = 0;

        if (verticalCrop) {
            viewHeight = context.getResources().getDisplayMetrics().widthPixels;
            viewWidth = (int) (viewHeight * 0.75F);
        } else {
            viewWidth = context.getResources().getDisplayMetrics().heightPixels;
            viewHeight = (int) (viewWidth * 0.75F);
        }

        Bitmap originBitmap = getCompressBitmap(context, path);
        if (originBitmap == null) {
            return Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
        }

        // 图片的固定宽度  高度
        int drawableWidth = originBitmap.getWidth();
        int drawableHeight = originBitmap.getHeight();
        // 将图片移动到屏幕的中点位置
        float dx = (viewWidth - drawableWidth) / 2;
        float dy = (viewHeight - drawableHeight) / 2;

        Matrix bitmapMatrix = new Matrix();
        bitmapMatrix.postTranslate(dx, dy);

        float scaleSize = 1.0F;
        if (drawableWidth >= viewWidth && drawableHeight >= viewHeight) {
            scaleSize = Math.max(viewHeight * 1.0f / drawableHeight, viewWidth * 1.0f / drawableWidth);
        } else if (drawableWidth > viewWidth && drawableHeight < viewHeight) {
            scaleSize = viewHeight * 1.0f / drawableHeight;
        } else if (drawableWidth < viewWidth && drawableHeight > viewHeight) {
            scaleSize = viewWidth * 1.0f / drawableWidth;
        } else {
            float sw = viewWidth * 1.0f / drawableWidth;
            float sh = viewHeight * 1.0f / drawableHeight;
            scaleSize = Math.max(sw, sh);
        }

        bitmapMatrix.postScale(scaleSize, scaleSize, viewWidth / 2, viewHeight / 2);

        Bitmap rectBitmap = Bitmap.createBitmap(originBitmap, 0, 0, drawableWidth, drawableHeight, bitmapMatrix, false);

        int rectWidth = rectBitmap.getWidth();
        int rectHeight = rectBitmap.getHeight();

        int x = 0;
        int y = 0;

        if (rectWidth > viewWidth) {
            x = (rectWidth - viewWidth) / 2;
        } else if (rectHeight > viewHeight) {
            y = (rectHeight - viewHeight) / 2;
        }

        return Bitmap.createBitmap(rectBitmap, x, y, rectWidth - 2 * x, rectHeight - 2 * y);
    }

}
