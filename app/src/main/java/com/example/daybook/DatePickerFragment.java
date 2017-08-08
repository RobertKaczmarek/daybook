package com.example.daybook;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.widget.DatePicker;

import org.joda.time.DateTime;

/**
 * Created by Robert Kaczmarek on 04-Aug-17.
 */

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final DateTime calendar = new DateTime();
        int year = calendar.getYear();
        int month = calendar.getMonthOfYear();
        int day = calendar.getDayOfMonth();

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        if (this.getActivity().getClass() == EventCreateActivity.class) EventCreateActivity.setDate(day, month, year);
        if (this.getActivity().getClass() == EventEditActivity.class) EventEditActivity.setDate(day, month, year);
    }
}