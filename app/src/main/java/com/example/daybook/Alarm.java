package com.example.daybook;

import android.app.PendingIntent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.joda.time.LocalTime;

import java.util.Comparator;

/**
 * Created by Robert Kaczmarek on 31-Jul-17.
 */

public class Alarm implements Parcelable, Comparable<Alarm> {
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


    public static final Comparator<Alarm> DESCENDING_COMPARATOR = new Comparator<Alarm>() {
        public int compare(Alarm l, Alarm r) {
            return new LocalTime(l.time).compareTo(new LocalTime(r.time));
        }
    };

    @Override
    public int compareTo(@NonNull Alarm alarm) {
        return new LocalTime(this.time).compareTo(new LocalTime(alarm.time));
    }
}
