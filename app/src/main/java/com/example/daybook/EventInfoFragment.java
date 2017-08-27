package com.example.daybook;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.joda.time.DateTime;

import static com.example.daybook.MainActivity.myEvents;

// fragment odpowiedzialny za wyświeltanie wybranego wydarzenia
public class EventInfoFragment extends Fragment {
  private Event mEvent; // wybrane wydarzenie
  private String auth_token; // token autoryzacji
  private Integer position; // pozycja na liście,
  // potrzebna podczas, gdy użytkownik będzie chciał zaktualizować wydarzenie z tego panelu

  public EventInfoFragment() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
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

  // funkcja wyświetlająca dane wydarzenia
  public void displayEvent(Event event) {
    ((TextView) getActivity().findViewById(R.id.eventTitleTextView)).setText(event.title);
    ((TextView) getActivity().findViewById(R.id.eventDescTextView)).setText(event.description);
    ((TextView) getActivity().findViewById(R.id.eventDateTextView)).setText(new DateTime(event.date).toString("dd-MM-yyyy"));
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    Intent intent = getActivity().getIntent();

    // pobieramy wybrane wydarzenie, token oraz pozycję na liście
    mEvent = intent.getParcelableExtra(MainActivity.eventExtra);
    auth_token = intent.getStringExtra("auth_token");
    position = intent.getIntExtra("position", 0);
    if (mEvent != null) displayEvent(mEvent);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    // 9 - oznacza powrót z activity, które pozwala na aktualizację wydarzenia
    // trzeba wybrane wydarzenie zaktualizować na głównej liście i wyświetlić je
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
