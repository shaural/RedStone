package com.cs407.team15.redstone.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Notices implements Parcelable {
    private String writer;
    private String title;
    private String content;
    private String date;
    private String notice_id;

    public Notices(String writer, String title, String content, String date, String notice_id) {
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
        notice_id = in.readString();
    }

    public static void submitNotice(String writer, String title, String content) {
        Notices notice = new Notices(writer, title, content,
                new SimpleDateFormat("yyyy-MM-dd").format(new Date()),
                UUID.randomUUID().toString());
        FirebaseFirestore.getInstance().collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                .collection("notices").add(notice);
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

    public String getNotice_id() {
        return notice_id;
    }

    public void setNotice_id(String notice_id) {
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
        parcel.writeString(notice_id);
    }
}

