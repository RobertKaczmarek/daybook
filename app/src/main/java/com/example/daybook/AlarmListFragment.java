package com.example.daybook;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.widget.ArrayAdapter;

// lista alarmów
public class AlarmListFragment extends ListFragment {

  public AlarmListFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setListAdapter(new ArrayAdapter<Alarm>(getActivity(), R.layout.alarm_list_layout,
      android.R.id.text1, MainActivity.myAlarms));
  }
}
