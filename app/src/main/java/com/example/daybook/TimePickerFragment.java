package com.example.daybook;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import org.joda.time.LocalTime;

// dialog TimePicker do wybierania czasu
public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // aktualny czas ustawiony jako domyślny
        final LocalTime c = new LocalTime();
        int hour = c.getHourOfDay();
        int minute = c.getMinuteOfHour();

        // tworzy nową instancję dialogu i jązwraca
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // wywołujemy odpowiednią funkcję żeby ustawić wybrany czas
        AlarmCreateActivity.setTime(hourOfDay, minute);
    }
}