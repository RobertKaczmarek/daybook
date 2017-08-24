package com.example.daybook;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import org.joda.time.DateTime;

// dialog DatePicker do wybierania daty
public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // aktualna data ustawioana jako domyślna
        final DateTime calendar = new DateTime();
        int year = calendar.getYear();
        int month = calendar.getMonthOfYear();
        int day = calendar.getDayOfMonth();

        // tworzymy nową instancję Dialogu i zwracamy ją
        return new DatePickerDialog(getActivity(), this, year, month - 1, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // zwracamy wybraną datę do odpowiedniej funkcji do odpowiedniego activity
        // trzeba odróżnic które activity ją wywołuje, ponieważ inaczej sypie NullPointerami
        if (this.getActivity().getClass() == EventCreateActivity.class) EventCreateActivity.setDate(day, month + 1, year);
        if (this.getActivity().getClass() == EventEditActivity.class) EventEditActivity.setDate(day, month + 1, year);
    }
}