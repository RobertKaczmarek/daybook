package com.example.daybook;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static com.example.daybook.MainActivity.myNotes;


/**
 * A simple {@link Fragment} subclass.
 */
public class NoteInfoFragment extends Fragment {
    private Note mNote;
    private String auth_token;
    private Integer position;

    public NoteInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_note_info, container, false);

        view.findViewById(R.id.noteEditButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent noteEvent = new Intent(getActivity(), NoteEditActivity.class);
                noteEvent.putExtra(MainActivity.noteExtra, mNote);
                noteEvent.putExtra("auth_token", auth_token);
                startActivityForResult(noteEvent, 1);
            }
        });

        return view;
    }

    public void displayNote(Note note) {
        ((TextView) getActivity().findViewById(R.id.noteDescTextView)).setText(note.description);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Intent intent = getActivity().getIntent();

        // display the task details
        mNote = intent.getParcelableExtra(MainActivity.noteExtra);
        auth_token = intent.getStringExtra("auth_token");
        position = intent.getIntExtra("position", 0);
        if(mNote != null) displayNote(mNote);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 8) {
            try {
                Note note = data.getParcelableExtra(MainActivity.noteExtra);

                if (mNote.id == note.id) {
                    mNote.description = note.description;

                    myNotes.remove(position);
                    myNotes.set(position, mNote);

                    displayNote(mNote);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }
}
