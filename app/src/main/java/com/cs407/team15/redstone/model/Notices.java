package com.cs407.team15.redstone.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Notices implements Parcelable {
    private String writer;
    private String title;
    private String content;
    private String date;
    private int notice_id;

    public Notices(String writer, String title, String content, String date, int notice_id) {
        this.writer = writer;
        this.title = title;
        this.content = content;
        this.date = date;
        this.notice_id = notice_id;
    }

    public Notices() {}

    protected Notices(Parcel in) {
        writer = in.readString();
        title = in.readString();
        content = in.readString();
        date = in.readString();
        notice_id = in.readInt();
    }

    public static final Creator<Notices> CREATOR = new Creator<Notices>() {
        @Override
        public Notices createFromParcel(Parcel in) {
            return new Notices(in);
        }

        @Override
        public Notices[] newArray(int size) {
            return new Notices[size];
        }
    };

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getNotice_id() {
        return notice_id;
    }

    public void setNotice_id(int notice_id) {
        this.notice_id = notice_id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(writer);
        parcel.writeString(title);
        parcel.writeString(content);
        parcel.writeString(date);
        parcel.writeInt(notice_id);
    }
}

