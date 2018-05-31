package com.demo.widget.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * desc:
 * author: wens
 * date: 2018/5/30.
 */
public class SmallVideoBean implements Parcelable {
    public String cover_url;
    public String title;
    public String video_url;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.cover_url);
        dest.writeString(this.title);
        dest.writeString(this.video_url);
    }

    public SmallVideoBean() {
    }

    protected SmallVideoBean(Parcel in) {
        this.cover_url = in.readString();
        this.title = in.readString();
        this.video_url = in.readString();
    }

    public static final Parcelable.Creator<SmallVideoBean> CREATOR = new Parcelable.Creator<SmallVideoBean>() {
        @Override
        public SmallVideoBean createFromParcel(Parcel source) {
            return new SmallVideoBean(source);
        }

        @Override
        public SmallVideoBean[] newArray(int size) {
            return new SmallVideoBean[size];
        }
    };
}
