package com.example.daybook;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.support.design.widget.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements DeleteDialog.NoticeDialogListener {
    private JSONObject auth_token;
    private JSONArray events;
    private JSONArray notes;
    private JSONArray alarms;
    private APISyncTask mSyncTask = null;

    private int listItemPosition = -1;
    private String listIdentifier;

    public static final String eventExtra = "Event";
    public static final String noteExtra = "Note";

    static public ArrayList<Event> myEvents;
    static public ArrayList<Note> myNotes;
    static {
        myEvents = new ArrayList<Event>();
        myNotes = new ArrayList<Note>();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 7) {
            try {
                setDate();

                auth_token = new JSONObject(data.getStringExtra("auth_token"));

                mSyncTask = new APISyncTask("events");
                mSyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
                final EventListFragment eventFr = (EventListFragment) getSupportFragmentManager().findFragmentById(R.id.eventFragment);
                final ArrayAdapter<Event> eventAdapter = (ArrayAdapter<Event>) eventFr.getListAdapter();
                eventFr.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(getApplicationContext(), "Event selected.", Toast.LENGTH_LONG).show();
                        startSecondActivity(parent, position, "event");
                    }
                });
                eventFr.getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(getApplicationContext(), "Event long clicked.", Toast.LENGTH_LONG).show();

                        DialogFragment newFragment = DeleteDialog.newInstance();
                        newFragment.show(getFragmentManager(), "DeleteDialogTag");

                        listItemPosition = position;
                        listIdentifier = "events";

                        return true;
                    }
                });

                mSyncTask = new APISyncTask("notes");
                mSyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
                final NoteListFragment noteFr = (NoteListFragment) getSupportFragmentManager().findFragmentById(R.id.noteFragment);
                final ArrayAdapter<Note> noteAdapter = (ArrayAdapter<Note>) noteFr.getListAdapter();
                noteFr.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(getApplicationContext(), "Note selected.", Toast.LENGTH_LONG).show();
                        startSecondActivity(parent, position, "note");
                    }
                });
                noteFr.getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(getApplicationContext(), "Note long clicked.", Toast.LENGTH_LONG).show();

                        DialogFragment newFragment = DeleteDialog.newInstance();
                        newFragment.show(getFragmentManager(), "DeleteDialogTag");

                        listItemPosition = position;
                        listIdentifier = "notes";

                        return true;
                    }
                });

                mSyncTask = new APISyncTask("alarms");
                mSyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void startSecondActivity(AdapterView<?> parent, int position, String action) {
        Intent intent = null;
        switch (action) {
            case "event": {
                intent = new Intent(this, EventInfoActivity.class);
                Event tmp = (Event) parent.getItemAtPosition(position);

                intent.putExtra(eventExtra, tmp);
                break;
            }
            case "note": {
                intent = new Intent(this, NoteInfoActivity.class);
                Note tmp = (Note) parent.getItemAtPosition(position);

                intent.putExtra(noteExtra, tmp);
            }
        }

        startActivity(intent);
    }

    private void setDate() {
        TextView currentDataTxV = (TextView) findViewById(R.id.dataTxV);
        String dayName= new SimpleDateFormat("EEEE").format(Calendar.getInstance().getTime());
        String day = new SimpleDateFormat("dd").format(Calendar.getInstance().getTime());
        String month = new SimpleDateFormat("MMMM").format(Calendar.getInstance().getTime());

        String content = "Hello! Today is " + dayName + ", the " + day + ". day of " + month + "!";

        currentDataTxV.setText(content);
    }

    private void setEvents() {
        try {
            for (int i = 0; i < events.length(); i++) {
                JSONObject event = events.getJSONObject(i);

                String event_title = event.getString("title");
                String event_desc = event.getString("description");
                String event_date = event.getString("date");

                myEvents.add(new Event(event_title, event_desc, event_date));

                EventListFragment eventFr = (EventListFragment) getSupportFragmentManager().findFragmentById(R.id.eventFragment);
                ArrayAdapter<Event> eventAdapter = (ArrayAdapter<Event>) eventFr.getListAdapter();
                eventAdapter.notifyDataSetChanged();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setNotes() {
        try {
            for (int i = 0; i < notes.length(); i++) {
                JSONObject note = notes.getJSONObject(i);

                String note_desc = note.getString("description");

                myNotes.add(new Note(note_desc));

                NoteListFragment noteFr = (NoteListFragment) getSupportFragmentManager().findFragmentById(R.id.noteFragment);
                ArrayAdapter<Note> noteAdapter = (ArrayAdapter<Note>) noteFr.getListAdapter();
                noteAdapter.notifyDataSetChanged();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setAlarms() {
        try {
            JSONObject alarm = events.getJSONObject(0);
//            alarmsView.setText(alarm.toString());
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDataPositiveClick(DialogFragment dialog) {
        if (listItemPosition != -1) {
            switch (listIdentifier) {
                case "events": {
                    myEvents.remove(listItemPosition);
                    EventListFragment eventFr = (EventListFragment) getSupportFragmentManager().findFragmentById(R.id.eventFragment);
                    ArrayAdapter<Event> eventAdapter = (ArrayAdapter<Event>) eventFr.getListAdapter();
                    eventAdapter.notifyDataSetChanged();
                    break;
                }
                case "notes": {
                    myNotes.remove(listItemPosition);
                    NoteListFragment noteFr = (NoteListFragment) getSupportFragmentManager().findFragmentById(R.id.noteFragment);
                    ArrayAdapter<Note> noteAdapter = (ArrayAdapter<Note>) noteFr.getListAdapter();
                    noteAdapter.notifyDataSetChanged();
                    break;
                }
            }
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        View v = findViewById(R.id.dataTxV);
        Snackbar.make(v, "Delete canceled!", Snackbar.LENGTH_LONG).show();
    }

    public class APISyncTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEndpoint;

        APISyncTask(String endpoint) {
            mEndpoint = endpoint;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpURLConnection httpcon;
            String url = "https://mysterious-dusk-55204.herokuapp.com/" + mEndpoint;
            String result = null;
            int resCode;
            InputStream input;
            try {
                httpcon = (HttpURLConnection) ((new URL(url).openConnection()));
                httpcon.setRequestProperty("Content-Type", "application/json");
                httpcon.setRequestProperty("Authorization", auth_token.get("auth_token").toString());
                httpcon.setRequestMethod("GET");
                httpcon.connect();
                resCode = httpcon.getResponseCode();

                if (resCode == HttpURLConnection.HTTP_OK) {
                    input =  httpcon.getInputStream();

                    BufferedReader br = new BufferedReader(new InputStreamReader(input, "iso-8859-1"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line;

                    while((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    input.close();
                    result = sb.toString();

                    switch (mEndpoint) {
                        case "events": events = new JSONArray(result); break;
                        case "notes": notes = new JSONArray(result); break;
                        case "alarms": alarms = new JSONArray(result); break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return true;
        }

        protected void onPostExecute(Boolean result) {
            switch (mEndpoint) {
                case "events": setEvents(); break;
                case "notes": setNotes(); break;
                case "alarms": setAlarms(); break;
            }
        }
    }
}
