package com.example.student11.pinotver2;

import android.content.ClipData;

/**
 * Created by student11 on 2016/11/21.
 */
public class Item extends ClipData.Item {
    // 記事のタイトル
    private CharSequence mTitle;
    // 記事の更新日
    private CharSequence mDate;
    // 記事のリンク
    private CharSequence mLink;

    /*public Item() {
        mTitle = "";
        mDate = "";
        mLink = "";
    }*/

    public Item(CharSequence text) {
        super(text);
        mTitle = "";
        mDate = "";
        mLink = "";
    }

    public CharSequence getTitle() {
        return mTitle;
    }

    public void setTitle(CharSequence title) {
        mTitle = title;
    }

    public CharSequence getDate() {
        return mDate;
    }

    public void setDate(CharSequence date) {
        mDate = date;
    }

    public CharSequence getLink() {
        return mLink;
    }

    public void setLink(CharSequence link) {
        mLink = link;
    }
}
