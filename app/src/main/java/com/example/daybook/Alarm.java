package com.example.daybook;

import android.app.PendingIntent;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by Robert Kaczmarek on 31-Jul-17.
 */

public class Alarm implements Parcelable {
    public Integer id;
    public String time;
    public PendingIntent intent;
    boolean set;

    Alarm() {

    }

    Alarm(Integer i, String t) {
        id = i;
        time = t;
        set = false;
    }

    protected Alarm(Parcel in) {
    }

    public static final Creator<Alarm> CREATOR = new Creator<Alarm>() {
        @Override
        public Alarm createFromParcel(Parcel in) {
            return new Alarm(in);
        }

        @Override
        public Alarm[] newArray(int size) {
            return new Alarm[size];
        }
    };

    @Override
    public String toString() {
        return time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }
}
