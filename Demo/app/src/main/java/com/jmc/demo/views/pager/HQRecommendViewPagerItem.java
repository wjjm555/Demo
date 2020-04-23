package com.jmc.demo.views.pager;

public class HQRecommendViewPagerItem {
    private String title;
    private String describe;
    private String url;
    private String id;

    private int resID;
    private int source;

    public HQRecommendViewPagerItem() {

    }

    public HQRecommendViewPagerItem(String title, int resID, int source) {
        this.title = title;
        this.resID = resID;
        this.source = source;
    }

    public HQRecommendViewPagerItem(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getResID() {
        return resID;
    }

    public void setResID(int resID) {
        this.resID = resID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }
}
