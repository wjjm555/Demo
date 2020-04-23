package com.qinggan.cockpit.view;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class BgDrawable extends Drawable {

    private Bitmap mSourceImg;

    public BgDrawable(Bitmap img){
        mSourceImg = img;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawBitmap(mSourceImg,
                new Rect(0, 0, mSourceImg.getWidth(), mSourceImg.getHeight()),
                new Rect(0 ,0, canvas.getWidth(), canvas.getHeight()),
                        null);
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }

}
