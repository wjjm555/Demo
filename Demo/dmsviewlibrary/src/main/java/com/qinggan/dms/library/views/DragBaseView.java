package com.qinggan.dms.library.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.qinggan.dms.library.R;
import com.qinggan.dms.library.model.DragItemInfo;

public abstract class DragBaseView extends CardView {

    final int DURATION = 500;

    final float MAXVALUE = 1, MINVALUE = 0, CONTENTALPHAVALUE = 0.4f, BORDERALPHAVALUE = 0.3f,
            LARGECONTENTSCALEVALUE = 0.99f, SMALLCONTENTSCALEXVALUE = 0.98f, SMALLCONTENTSCALEYVALUE = 0.97f;

    final String ALPHA = "alpha", SCALEX = "scaleX", SCALEY = "scaleY";

    public DragItemInfo info;

    public DragItemInfo.Size size = DragItemInfo.Size.NONE;

    public ImageView borderView, contentView;

    public DragBaseView(@NonNull Context context) {
        this(context, null);
    }

    public DragBaseView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragBaseView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(getContext(), R.layout.layout_grag_item, this);
        borderView = findViewById(R.id.borderView);
        contentView = findViewById(R.id.contentView);
    }

    public String getType() {
        if (info != null)
            return info.getType();
        return "";
    }

    public DragItemInfo.Size getSize() {
        return size;
    }

    public DragItemInfo getInfo() {
        return info;
    }

    public void setSize(DragItemInfo.Size size) {
        this.size = size;
        refreshContent();
    }

    public void setInfo(DragItemInfo info) {
        this.info = info;
        refreshContent();
    }

    public void refreshContent() {
        refreshContent(info);
    }

    public void refreshContent(DragItemInfo info) {
        if (contentView != null && info != null)
            if (size == DragItemInfo.Size.LARGE)
                contentView.setImageResource(info.getLargeSizeImgResId());
            else
                contentView.setImageResource(info.getSmallSizeImgResId());
    }
}
