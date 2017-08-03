package com.example.daybook;

import android.content.Intent;
import android.graphics.Rect;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.DialogFragment;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.design.widget.Snackbar;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import static java.time.LocalDateTime.parse;


public class MainActivity extends AppCompatActivity implements DeleteDialog.NoticeDialogListener {
    private JSONObject auth_token;
    private JSONArray events;
    private JSONArray notes;
    private JSONArray alarms;
    private APISyncTask mSyncTask = null;
    private APIDeleteTask mDeleteTask = null;

    private int listItemPosition = -1;
    private String listIdentifier;

    public FloatingActionsMenu fab;

    public static final String eventExtra = "Event";
    public static final String noteExtra = "Note";
    public static final String alarmExtra = "Alarm";

    static public ArrayList<Event> myEvents;
    static public ArrayList<Note> myNotes;
    static public ArrayList<Alarm> myAlarms;
    static {
        myEvents = new ArrayList<Event>();
        myNotes = new ArrayList<Note>();
        myAlarms = new ArrayList<Alarm>();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, 1);

        FloatingActionButton addEventButton = new FloatingActionButton(getBaseContext());
        FloatingActionButton addNoteButton = new FloatingActionButton(getBaseContext());
        FloatingActionButton addAlarmButton = new FloatingActionButton(getBaseContext());

        fab = (FloatingActionsMenu) findViewById(R.id.fab_menu);

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
                final AlarmListFragment alarmFr = (AlarmListFragment) getSupportFragmentManager().findFragmentById(R.id.alarmFragment);
                final ArrayAdapter<Alarm> alarmAdapter = (ArrayAdapter<Alarm>) alarmFr.getListAdapter();
                alarmFr.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        AppCompatTextView alarmView = (AppCompatTextView ) view;
                        if (alarmView.isActivated()) {
                            Toast.makeText(getApplicationContext(), "Alarm cleared.", Toast.LENGTH_SHORT).show();

                            alarmView.setActivated(false);

                            Alarm alarm = (Alarm) alarmFr.getListView().getItemAtPosition(position);
                            alarm.checked = false;
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Alarm set.", Toast.LENGTH_SHORT).show();

                            alarmView.setActivated(true);

                            Alarm alarm = (Alarm) alarmFr.getListView().getItemAtPosition(position);
                            alarm.checked = true;
                        }
                    }
                });
                alarmFr.getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(getApplicationContext(), "Alarm long clicked.", Toast.LENGTH_LONG).show();

                        DialogFragment newFragment = DeleteDialog.newInstance();
                        newFragment.show(getFragmentManager(), "DeleteDialogTag");

                        listItemPosition = position;
                        listIdentifier = "alarms";

                        return true;
                    }
                });

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

                Integer event_id = event.getInt("id");
                String event_title = event.getString("title");
                String event_desc = event.getString("description");
                String event_date = event.getString("date");

                myEvents.add(new Event(event_id, event_title, event_desc, event_date));

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

                Integer note_id = note.getInt("id");
                String note_desc = note.getString("description");

                myNotes.add(new Note(note_id, note_desc));

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
            for (int i = 0; i < alarms.length(); i++) {
                JSONObject alarm = alarms.getJSONObject(i);

                Integer alarm_id = alarm.getInt("id");
                String alarm_time = alarm.getString("time");

                myAlarms.add(new Alarm(alarm_id, alarm_time));

                AlarmListFragment alarmFr = (AlarmListFragment) getSupportFragmentManager().findFragmentById(R.id.alarmFragment);
                ArrayAdapter<Alarm> alarmAdapter = (ArrayAdapter<Alarm>) alarmFr.getListAdapter();
                alarmAdapter.notifyDataSetChanged();
            }
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
                    mDeleteTask = new APIDeleteTask("events", myEvents.get(listItemPosition));
                    mDeleteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);

                    myEvents.remove(listItemPosition);
                    EventListFragment eventFr = (EventListFragment) getSupportFragmentManager().findFragmentById(R.id.eventFragment);
                    ArrayAdapter<Event> eventAdapter = (ArrayAdapter<Event>) eventFr.getListAdapter();
                    eventAdapter.notifyDataSetChanged();
                    break;
                }
                case "notes": {
                    mDeleteTask = new APIDeleteTask("notes", myNotes.get(listItemPosition));
                    mDeleteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);

                    myNotes.remove(listItemPosition);
                    NoteListFragment noteFr = (NoteListFragment) getSupportFragmentManager().findFragmentById(R.id.noteFragment);
                    ArrayAdapter<Note> noteAdapter = (ArrayAdapter<Note>) noteFr.getListAdapter();
                    noteAdapter.notifyDataSetChanged();
                    break;
                }
                case "alarms": {
                    mDeleteTask = new APIDeleteTask("alarms", myAlarms.get(listItemPosition));
                    mDeleteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);

                    myAlarms.remove(listItemPosition);
                    AlarmListFragment alarmFr = (AlarmListFragment) getSupportFragmentManager().findFragmentById(R.id.alarmFragment);
                    ArrayAdapter<Alarm> alarmAdapter = (ArrayAdapter<Alarm>) alarmFr.getListAdapter();
                    alarmAdapter.notifyDataSetChanged();
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (fab.isExpanded()) {

                Rect outRect = new Rect();
                fab.getGlobalVisibleRect(outRect);

                if(!outRect.contains((int)event.getRawX(), (int)event.getRawY()))
                    fab.collapse();
            }
        }

        return super.dispatchTouchEvent(event);
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

    public class APIDeleteTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEndpoint;
        private Event mEvent = null;
        private Note mNote = null;
        String url;

        APIDeleteTask(String endpoint, Object object) {
            mEndpoint = endpoint;

            switch (object.getClass().getSimpleName()) {
                case "Event": {
                    mEvent = (Event) object;
                    url = "https://mysterious-dusk-55204.herokuapp.com/" + mEndpoint + "/" + mEvent.id;
                    break;
                }
                case "Note": {
                    mNote = (Note) object;
                    url = "https://mysterious-dusk-55204.herokuapp.com/" + mEndpoint + "/" + mNote.id;
                    break;
                }
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpURLConnection httpcon;
            String result = null;
            int resCode;
            InputStream input;
            try {
                httpcon = (HttpURLConnection) ((new URL(url).openConnection()));
                httpcon.setRequestProperty("Content-Type", "application/json");
                httpcon.setRequestProperty("Authorization", auth_token.get("auth_token").toString());
                httpcon.setRequestMethod("DELETE");
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
