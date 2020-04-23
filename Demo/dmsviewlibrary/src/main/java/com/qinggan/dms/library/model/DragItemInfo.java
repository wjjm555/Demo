package com.qinggan.dms.library.model;

public class DragItemInfo {

    private String type;

    private int largeSizeImgResId, smallSizeImgResId;

    public enum Size {
        LARGE, SMALL, NONE
    }

    public DragItemInfo(String type, int largeSizeImgResId, int smallSizeImgResId) {
        this.type = type;
        this.largeSizeImgResId = largeSizeImgResId;
        this.smallSizeImgResId = smallSizeImgResId;
    }

    public String getType() {
        if (type != null)
            return type;
        return "";
    }

    public int getLargeSizeImgResId() {
        return largeSizeImgResId;
    }

    public int getSmallSizeImgResId() {
        return smallSizeImgResId;
    }

    public DragItemInfo setSmallSizeImgResId(int smallSizeImgResId) {
        this.smallSizeImgResId = smallSizeImgResId;
        return this;
    }

    public DragItemInfo setLargeSizeImgResId(int largeSizeImgResId) {
        this.largeSizeImgResId = largeSizeImgResId;
        return this;
    }
}
