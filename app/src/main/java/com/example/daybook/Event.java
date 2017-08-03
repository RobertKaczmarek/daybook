package com.example.daybook;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Robert Kaczmarek on 26-Jul-17.
 */

public class Event implements Parcelable {
    public Integer id;
    public String title;
    public String description;
    public String date;

    Event() {

    }

    Event(Integer i, String t, String d, String dt) {
        id = i;
        title = t;
        description = d;
        date = dt;
    }

    protected Event(Parcel in) {
        id = in.readInt();
        title = in.readString();
        description = in.readString();
        date = in.readString();
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    @Override
    public String toString() {
        return title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeString(date);
    }
}