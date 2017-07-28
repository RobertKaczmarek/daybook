package com.example.daybook;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class NoteInfoFragment extends Fragment {


    public NoteInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_note_info, container, false);
    }

    public void displayNote(Note note) {
        ((TextView) getActivity().findViewById(R.id.noteDescTextView)).setText(note.description);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Intent intent = getActivity().getIntent();

        Note receivedNote = intent.getParcelableExtra(MainActivity.noteExtra);
        if (receivedNote != null) displayNote(receivedNote);
    }
}
