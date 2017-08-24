package com.example.daybook;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.joda.time.DateTime;

import java.util.Comparator;

// model wydarzenia
public class Event implements Parcelable, Comparable<Event> {
    public Integer id; // do odwołań się na serwerze
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

    // własny komparator wykorzystywany do sortowania elementów na liście
    public static final Comparator<Event> DESCENDING_COMPARATOR = new Comparator<Event>() {
        public int compare(Event l, Event r) {
            return new DateTime(l.date).compareTo(new DateTime(r.date));
        }
    };

    @Override
    public int compareTo(@NonNull Event event) {
        return new DateTime(this.date).compareTo(new DateTime(event.date));
    }
}