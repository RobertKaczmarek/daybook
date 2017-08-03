package com.example.daybook;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.v4.app.FragmentActivity;


/**
 * A simple {@link Fragment} subclass.
 */
public class EventInfoFragment extends Fragment {


    public EventInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_info, container, false);
    }

    public void displayEvent(Event event) {
        ((TextView) getActivity().findViewById(R.id.eventTitleTextView)).setText(event.title);
        ((TextView) getActivity().findViewById(R.id.eventDescTextView)).setText(event.description);
        ((TextView) getActivity().findViewById(R.id.eventDateTextView)).setText(event.date);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // perform actions when the parent Activity is created
        super.onActivityCreated(savedInstanceState);

        Intent intent = getActivity().getIntent();

        // display the task details
        Event receivedTask = intent.getParcelableExtra(MainActivity.eventExtra);
        if(receivedTask != null) displayEvent(receivedTask);
    }

}
