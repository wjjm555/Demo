package com.qinggan.dms.library.views;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.qinggan.dms.library.R;
import com.qinggan.dms.library.model.DragItemInfo;

public class DragItemView extends DragBaseView {

    private DragItemInfo tmpInfo;

    private ObjectAnimator contentAnimator, borderAnimator;

    public DragItemView(Context context) {
        super(context, null);
        initViews();
    }

    private void initViews() {
        borderView.setVisibility(View.VISIBLE);
        setCardBackgroundColor(Color.TRANSPARENT);
        setCardElevation(1);
        setRadius(getResources().getDimension(R.dimen.item_border_corners));
        setForeground(getResources().getDrawable(R.drawable.ripple_item));
    }

    public void updateImgResId(DragItemInfo.Size size, int imgResId) {
        if (info != null) {
            if (size == DragItemInfo.Size.LARGE)
                info.setLargeSizeImgResId(imgResId);
            else
                info.setSmallSizeImgResId(imgResId);
        }
    }

    public void updateTmpImgResId(DragItemInfo.Size size, int imgResId) {
        if (tmpInfo != null) {
            if (size == DragItemInfo.Size.LARGE)
                tmpInfo.setLargeSizeImgResId(imgResId);
            else
                tmpInfo.setSmallSizeImgResId(imgResId);
        }
    }

    public DragItemInfo getTmpInfo() {
        return tmpInfo;
    }

    public void setTmpInfo(DragItemInfo tmpInfo) {
        this.tmpInfo = tmpInfo;
    }

    public void replaceTmpInfo() {
        this.info = this.tmpInfo;
        this.tmpInfo = null;
        refreshContent();
    }

    public void refreshTmpContent() {
        refreshContent(tmpInfo);
    }

    public void clearTmpInfo() {
        this.tmpInfo = null;
        refreshContent();
    }

    public void borderSelectedAnimation() {
        if (borderAnimator != null)
            borderAnimator.cancel();
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(ALPHA, BORDERALPHAVALUE, MAXVALUE);
        borderAnimator = ObjectAnimator.ofPropertyValuesHolder(borderView, alpha).setDuration(DURATION);
        borderAnimator.setInterpolator(new DecelerateInterpolator());
        borderAnimator.start();
    }


    public void borderCancelledAnimation() {
        if (borderAnimator != null)
            borderAnimator.cancel();
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(ALPHA, MAXVALUE, BORDERALPHAVALUE);
        borderAnimator = ObjectAnimator.ofPropertyValuesHolder(borderView, alpha).setDuration(DURATION);
        borderAnimator.setInterpolator(new DecelerateInterpolator());
        borderAnimator.start();
    }

    public void contentShrinkAnimation() {
        if (contentAnimator != null)
            contentAnimator.cancel();
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(SCALEX, MAXVALUE, getRealScaleX());
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(SCALEY, MAXVALUE, getRealScaleY());
        contentAnimator = ObjectAnimator.ofPropertyValuesHolder(contentView, scaleX, scaleY).setDuration(DURATION);
        contentAnimator.setInterpolator(new DecelerateInterpolator());
        contentAnimator.start();
    }

    public void contentAmplifyAnimation() {
        if (contentAnimator != null)
            contentAnimator.cancel();
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(SCALEX, getRealScaleX(), MAXVALUE);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(SCALEY, getRealScaleY(), MAXVALUE);
        contentAnimator = ObjectAnimator.ofPropertyValuesHolder(contentView, scaleX, scaleY).setDuration(DURATION);
        contentAnimator.setInterpolator(new DecelerateInterpolator());
        contentAnimator.start();
    }

    public void contentDisplayAnimation() {
        if (contentAnimator != null)
            contentAnimator.cancel();
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(SCALEX, MINVALUE, MAXVALUE);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(SCALEY, MINVALUE, MAXVALUE);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(ALPHA, MINVALUE, MAXVALUE);
        contentAnimator = ObjectAnimator.ofPropertyValuesHolder(contentView, scaleX, scaleY, alpha).setDuration(DURATION);
        contentAnimator.setInterpolator(new OvershootInterpolator());
        contentAnimator.start();
    }

    public void contentDismissAnimation() {
        if (contentAnimator != null)
            contentAnimator.cancel();
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(SCALEX, MAXVALUE, MINVALUE);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(SCALEY, MAXVALUE, MINVALUE);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(ALPHA, MAXVALUE, MINVALUE);
        contentAnimator = ObjectAnimator.ofPropertyValuesHolder(contentView, scaleX, scaleY, alpha).setDuration(DURATION);
        contentAnimator.setInterpolator(new OvershootInterpolator());
        contentAnimator.start();
    }

    public void tmpDisplayAnimation() {
        if (contentAnimator != null)
            contentAnimator.cancel();
        refreshContent(tmpInfo);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(SCALEX, MINVALUE, getRealScaleX());
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(SCALEY, MINVALUE, getRealScaleY());
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(ALPHA, MINVALUE, CONTENTALPHAVALUE);
        contentAnimator = ObjectAnimator.ofPropertyValuesHolder(contentView, scaleX, scaleY, alpha).setDuration(DURATION);
        contentAnimator.setInterpolator(new DecelerateInterpolator());
        contentAnimator.start();

    }

    public void tmpDismissAnimation() {
        if (contentAnimator != null)
            contentAnimator.cancel();
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(SCALEX, getRealScaleX(), MINVALUE);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(SCALEY, getRealScaleY(), MINVALUE);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(ALPHA, CONTENTALPHAVALUE, MINVALUE);
        contentAnimator = ObjectAnimator.ofPropertyValuesHolder(contentView, scaleX, scaleY, alpha).setDuration(DURATION);
        contentAnimator.setInterpolator(new DecelerateInterpolator());
        contentAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                refreshContent();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        contentAnimator.start();
    }

    public void tmpReplaceSuccessAnimation() {
        if (contentAnimator != null)
            contentAnimator.cancel();
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(SCALEX, getRealScaleX(), MAXVALUE);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(SCALEY, getRealScaleY(), MAXVALUE);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(ALPHA, CONTENTALPHAVALUE, MAXVALUE);
        contentAnimator = ObjectAnimator.ofPropertyValuesHolder(contentView, scaleX, scaleY, alpha).setDuration(DURATION);
        contentAnimator.setInterpolator(new DecelerateInterpolator());
        contentAnimator.start();
    }

    public void tmpReplaceDismissAnimation() {
        if (contentAnimator != null)
            contentAnimator.cancel();
        tmpReplaceDismissAnimationStepOne();
    }

    public void tmpReplaceDisplayAnimation() {
        if (contentAnimator != null)
            contentAnimator.cancel();
        tmpReplaceDisplayAnimationStepOne();
    }

    private void tmpReplaceDisplayAnimationStepOne() {
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(SCALEX, MAXVALUE, MINVALUE);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(SCALEY, MAXVALUE, MINVALUE);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(ALPHA, MAXVALUE, MINVALUE);
        contentAnimator = ObjectAnimator.ofPropertyValuesHolder(contentView, scaleX, scaleY, alpha).setDuration(DURATION / 2);
        contentAnimator.setInterpolator(new DecelerateInterpolator());
        contentAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                refreshContent(tmpInfo);
                tmpReplaceDisplayAnimationStepTwo();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        contentAnimator.start();
    }

    private void tmpReplaceDisplayAnimationStepTwo() {
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(SCALEX, MINVALUE, getRealScaleX());
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(SCALEY, MINVALUE, getRealScaleY());
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(ALPHA, MINVALUE, CONTENTALPHAVALUE);
        contentAnimator = ObjectAnimator.ofPropertyValuesHolder(contentView, scaleX, scaleY, alpha).setDuration(DURATION / 2);
        contentAnimator.setInterpolator(new DecelerateInterpolator());
        contentAnimator.start();
    }

    private void tmpReplaceDismissAnimationStepOne() {
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(SCALEX, getRealScaleX(), MINVALUE);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(SCALEY, getRealScaleY(), MINVALUE);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(ALPHA, CONTENTALPHAVALUE, MINVALUE);
        contentAnimator = ObjectAnimator.ofPropertyValuesHolder(contentView, scaleX, scaleY, alpha).setDuration(DURATION / 2);
        contentAnimator.setInterpolator(new DecelerateInterpolator());
        contentAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                refreshContent();
                tmpReplaceDismissAnimationStepTwo();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        contentAnimator.start();
    }

    private void tmpReplaceDismissAnimationStepTwo() {
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(SCALEX, MINVALUE, MAXVALUE);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(SCALEY, MINVALUE, MAXVALUE);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(ALPHA, MINVALUE, MAXVALUE);
        contentAnimator = ObjectAnimator.ofPropertyValuesHolder(contentView, scaleX, scaleY, alpha).setDuration(DURATION / 2);
        contentAnimator.setInterpolator(new DecelerateInterpolator());
        contentAnimator.start();
    }

    private float getRealScaleX() {
        return size == DragItemInfo.Size.LARGE ? LARGECONTENTSCALEVALUE : SMALLCONTENTSCALEXVALUE;
    }

    private float getRealScaleY() {
        return size == DragItemInfo.Size.LARGE ? LARGECONTENTSCALEVALUE : SMALLCONTENTSCALEYVALUE;
    }

}
