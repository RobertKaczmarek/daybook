package com.example.daybook;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.widget.ArrayAdapter;

public class EventListFragment extends ListFragment {

    public EventListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setListAdapter(new ArrayAdapter<Event>(getActivity(), R.layout.list_layout,
                android.R.id.text1, MainActivity.myEvents));
    }
}
