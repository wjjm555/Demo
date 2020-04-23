package com.qinggan.cockpit.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.qinggan.cockpit.R;

public class UserHeadView extends View {

    private Bitmap mImage;

    public UserHeadView(Context context) {
        super(context);
    }

    public UserHeadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon_head);
        mImage = createCircleImage(bitmap);
    }

    public void setIcon(Bitmap bitmap){
        mImage = createCircleImage(bitmap);
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(mImage,
                new Rect(0, 0, mImage.getWidth(), mImage.getHeight()),
                new Rect(0, 0, canvas.getWidth(), canvas.getHeight()),
                null);
    }

    private Bitmap createCircleImage(Bitmap source)
    {
        int size = source.getWidth() > source.getHeight()? source.getHeight(): source.getWidth();
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Bitmap target = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(target);
        canvas.drawCircle(size / 2, size / 2, size / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, 0, 0, paint);
        return target;
    }
}
