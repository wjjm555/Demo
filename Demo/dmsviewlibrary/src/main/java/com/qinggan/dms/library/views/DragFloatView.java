package com.qinggan.dms.library.views;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Color;
import android.view.animation.OvershootInterpolator;

import com.qinggan.dms.library.R;
import com.qinggan.dms.library.model.DragItemInfo;

public class DragFloatView extends DragBaseView {

    private ObjectAnimator animator;

    private boolean showing = false;

    public DragFloatView(Context context) {
        super(context);
        initViews();
    }

    private void initViews() {
        setSize(DragItemInfo.Size.SMALL);
        setCardBackgroundColor(Color.TRANSPARENT);
        setCardElevation(2);
        setRadius(getResources().getDimension(R.dimen.item_border_corners));
    }

    public boolean isShowing() {
        return showing;
    }

    public DragItemInfo getInfo() {
        return info;
    }

    public void setInfo(DragItemInfo info) {
        this.info = info;
        if (contentView != null)
            contentView.setImageResource(info.getSmallSizeImgResId());
    }

    public void displayAnimation() {
        showing = true;
        if (animator != null)
            animator.cancel();
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(SCALEX, MINVALUE, MAXVALUE);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(SCALEY, MINVALUE, MAXVALUE);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(ALPHA, MINVALUE, MAXVALUE);
        animator = ObjectAnimator.ofPropertyValuesHolder(this, scaleX, scaleY, alpha).setDuration(DURATION);
        animator.setInterpolator(new OvershootInterpolator());
        animator.start();
    }

    public void dismissAnimation() {
        showing = false;
        if (animator != null)
            animator.cancel();
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(SCALEX, MAXVALUE, MINVALUE);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(SCALEY, MAXVALUE, MINVALUE);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(ALPHA, MAXVALUE, MINVALUE);
        animator = ObjectAnimator.ofPropertyValuesHolder(this, scaleX, scaleY, alpha).setDuration(DURATION);
        animator.setInterpolator(new OvershootInterpolator());
        animator.start();
    }
}
