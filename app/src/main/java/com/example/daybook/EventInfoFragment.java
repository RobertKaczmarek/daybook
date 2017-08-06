package com.example.daybook;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import static com.example.daybook.MainActivity.myEvents;


/**
 * A simple {@link Fragment} subclass.
 */
public class EventInfoFragment extends Fragment {
    private Event mEvent;
    private String auth_token;
    private Integer position;

    public EventInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event_info, container, false);

        view.findViewById(R.id.eventEditButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editEvent = new Intent(getActivity(), EventEditActivity.class);
                editEvent.putExtra(MainActivity.eventExtra, mEvent);
                editEvent.putExtra("auth_token", auth_token);
                startActivityForResult(editEvent, 1);
            }
        });

        return view;
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
        mEvent = intent.getParcelableExtra(MainActivity.eventExtra);
        auth_token = intent.getStringExtra("auth_token");
        position = intent.getIntExtra("position", 0);
        if(mEvent != null) displayEvent(mEvent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 9) {
            try {
                Event event = data.getParcelableExtra(MainActivity.eventExtra);

                if (mEvent.id == event.id) {
                    mEvent.title = event.title;
                    mEvent.description = event.description;
                    mEvent.date = event.date;

                    myEvents.remove(position);
                    myEvents.set(position, mEvent);

                    displayEvent(mEvent);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }
}
