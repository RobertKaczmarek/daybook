package com.example.daybook;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Robert Kaczmarek on 28-Jul-17.
 */

public class Note implements Parcelable {
    public Integer id;
    public String description;

    Note() {

    }

    Note(Integer i, String desc) {
        id = i;
        description = desc;
    }

    protected Note(Parcel in) {
        id = in.readInt();
        description = in.readString();
    }

    public static final Creator<Note> CREATOR = new Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    public String toString() {
        if (description.length() > 35) {
            String temp = description.substring(0, 35) + "...";
            return temp;
        }
        else {
            return description;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(description);
    }
}
