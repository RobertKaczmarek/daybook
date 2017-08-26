package com.example.daybook;

import android.os.Parcel;
import android.os.Parcelable;

// model notatki
public class Note implements Parcelable {
    public Integer id; // do odwołań się na serwerze
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

    // metoda drukująca - w zaależności od treści opisu wyświetlamy inaczej an liście w MainActivity
    public String toString() {
        String desc;
        if (description.length() < 35) {
            desc = description;
            if (desc.contains("\n")) {
                Integer position = desc.indexOf("\n");
                desc = desc.substring(0, position);
            }
        }
        else {
            if (description.contains("\n")) {
                Integer position = description.indexOf("\n");
                if (position > 35) desc = description.substring(0, 35) + "...";
                else desc = description.substring(0, position);
            }
            else desc = description.substring(0, 35) + "...";
        }
        return desc;
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
