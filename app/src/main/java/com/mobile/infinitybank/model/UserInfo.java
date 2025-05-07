package com.mobile.infinitybank.model;

import android.os.Parcel;
import android.os.Parcelable;

public class UserInfo implements Parcelable {
    private String label;
    private String value;

    public UserInfo(String label, String value) {
        this.label = label;
        this.value = value;
    }

    protected UserInfo(Parcel in) {
        label = in.readString();
        value = in.readString();
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(label);
        dest.writeString(value);
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "label='" + label + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
} 