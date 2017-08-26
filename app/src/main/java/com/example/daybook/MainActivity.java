package com.example.daybook;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.support.design.widget.Snackbar;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
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
import java.util.Collections;
import java.util.Iterator;

// główne activity aplikacji - dashboard z listą wydarzeń, notatek i alarmów
public class MainActivity extends AppCompatActivity implements DeleteDialog.NoticeDialogListener {
    final MainActivity pointer = this; // wskaźnik na to activity

    private AlarmManager alarmManager; // manager do alarmów

    private JSONObject auth_token; // token autoryzacyjny
    private JSONArray events; // JSON przechowujący wszystkie wydarzenia
    private JSONArray notes; // JSON prezchowujący wszystkie notatki
    private JSONArray alarms; // JSON przechowujący wszystkie alarmy
    private APISyncTask mSyncTask = null; // callback do serwera pobierający dane obiekty
    private APIDeleteTask mDeleteTask = null; // callback do serwera usuwający dany obiekt
    private UserLogoutTask mLogoutTask = null; // callback do serwera wylogowywujący użytkownika

    private int listItemPosition = -1;
    private String listIdentifier;

    public FloatingActionsMenu fab;

    public static final String AUTH_TOKEN = "com.example.daybook.token"; // nazwaklucza do SharedPreferences gdzie przechowywane jest auth_token
    public static final String TOKEN = "token";
    public static final String eventExtra = "Event";
    public static final String noteExtra = "Note";

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

        // jeżeli token nie jest zapsany w SharedPreferences, to wywołujemy activity w celu zalogowania się
        if (!restoreToken()) {
            Intent intent = new Intent(pointer, LoginActivity.class);
            startActivityForResult(intent, 1);
        }

        // jeżeli zmienna jest zainicjowana i posiada token, to zapełniamy aplikację danymi
        if (auth_token != null && auth_token.has("auth_token")) initialize();

        // inicjujemy wszystkie potrzebne rzeczy
        FloatingActionButton addEventButton = (FloatingActionButton) findViewById(R.id.add_event);
        FloatingActionButton addNoteButton = (FloatingActionButton) findViewById(R.id.add_note);
        FloatingActionButton addAlarmButton = (FloatingActionButton) findViewById(R.id.add_alarm);

        fab = (FloatingActionsMenu) findViewById(R.id.fab_menu);

        addEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createEvent = new Intent(pointer, EventCreateActivity.class);
                createEvent.putExtra("auth_token", auth_token.toString());
                startActivityForResult(createEvent, 1);
            }
        });

        addNoteButton.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createNote = new Intent(pointer, NoteCreateActivity.class);
                createNote.putExtra("auth_token", auth_token.toString());
                startActivityForResult(createNote, 1);
            }
        }));

        addAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createAlarm = new Intent(pointer, AlarmCreateActivity.class);
                createAlarm.putExtra("auth_token", auth_token.toString());
                startActivityForResult(createAlarm, 1);
            }
        });

        alarmManager = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logout_button) {
            clearApp();

            mLogoutTask = new UserLogoutTask();
            mLogoutTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);

            Intent intent = new Intent(pointer, LoginActivity.class);
            startActivityForResult(intent, 1);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case 7 :
                // kod 7 - oznacza powrót z Login activity
                try {
                    auth_token = new JSONObject(data.getStringExtra("auth_token"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // zapełniamy całą aplikacje danymi użytkownika
                initialize();
                break;
            case 1 :
                // kod 1 - oznacza powrót z EventCreate activity
                // przechwytujemy stworzony obiekt i dodajemy go do listy
                try {
                    JSONObject event = new JSONObject(data.getStringExtra("object"));

                    Integer event_id = event.getInt("id");
                    String event_title = event.getString("title");
                    String event_desc = event.getString("description");
                    String event_date = event.getString("date");

                    myEvents.add(new Event(event_id, event_title, event_desc, event_date));
                    // po dodaniu sortujemy liste
                    Collections.sort(myEvents,Event.DESCENDING_COMPARATOR);

                    EventListFragment eventFr = (EventListFragment) getSupportFragmentManager().findFragmentById(R.id.eventFragment);
                    ArrayAdapter<Event> eventAdapter = (ArrayAdapter<Event>) eventFr.getListAdapter();
                    eventAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                break;
            case 2 :
                // kod 2 - oznacza powrót z NoteCreate activity
                try {
                    JSONObject note = new JSONObject(data.getStringExtra("object"));

                    Integer note_id = note.getInt("id");
                    String note_desc = note.getString("description");

                    myNotes.add(new Note(note_id, note_desc));

                    NoteListFragment noteFr = (NoteListFragment) getSupportFragmentManager().findFragmentById(R.id.noteFragment);
                    ArrayAdapter<Note> noteAdapter = (ArrayAdapter<Note>) noteFr.getListAdapter();
                    noteAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                break;
            case 3 :
                // kod 3 - oznacza powrót z AlarmCreate activty
                try {
                    JSONObject alarm = new JSONObject(data.getStringExtra("object"));

                    Integer alarm_id = alarm.getInt("id");
                    String alarm_time = alarm.getString("time");

                    myAlarms.add(new Alarm(alarm_id, alarm_time));
                    Collections.sort(myAlarms,Alarm.DESCENDING_COMPARATOR);

                    AlarmListFragment alarmFr = (AlarmListFragment) getSupportFragmentManager().findFragmentById(R.id.alarmFragment);
                    ArrayAdapter<Alarm> alarmAdapter = (ArrayAdapter<Alarm>) alarmFr.getListAdapter();
                    alarmAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    // funkcja odpowiedzialna za inicjacje aplikacji danmi użytkownika
    private void initialize() {
        // czyścimy aplikacje
        clearApp();

        // ustawiamy datę
        setDate();

        // pobieramy każdy tych obiektów
        // ustawiamy onItemClick i onItemLongClick listenery do list
        mSyncTask = new APISyncTask("events");
        mSyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
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
                DialogFragment newFragment = DeleteDialog.newInstance("event");
                newFragment.show(getFragmentManager(), "DeleteDialogTag");

                listItemPosition = position;
                listIdentifier = "events";

                return true;
            }
        });

        mSyncTask = new APISyncTask("notes");
        mSyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
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
                DialogFragment newFragment = DeleteDialog.newInstance("note");
                newFragment.show(getFragmentManager(), "DeleteDialogTag");

                listItemPosition = position;
                listIdentifier = "notes";

                return true;
            }
        });

        mSyncTask = new APISyncTask("alarms");
        mSyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
        final AlarmListFragment alarmFr = (AlarmListFragment) getSupportFragmentManager().findFragmentById(R.id.alarmFragment);
        final ArrayAdapter<Alarm> alarmAdapter = (ArrayAdapter<Alarm>) alarmFr.getListAdapter();
        alarmFr.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                AppCompatTextView alarmView = (AppCompatTextView) view;
                Alarm alarm = (Alarm) alarmFr.getListView().getItemAtPosition(position);

                if (alarm.set) {
                    Toast.makeText(getApplicationContext(), "Alarm cleared.", Toast.LENGTH_SHORT).show();

                    alarmView.setActivated(false);
                    alarm.set = false;
                    alarmManager.cancel(alarm.intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Alarm set.", Toast.LENGTH_SHORT).show();

                    alarmView.setActivated(true);
                    alarm.set = true;
                    alarm.intent = Alarm(new LocalTime(alarm.time));
                }
            }
        });
        alarmFr.getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                DialogFragment newFragment = DeleteDialog.newInstance("alarm");
                newFragment.show(getFragmentManager(), "DeleteDialogTag");

                listItemPosition = position;
                listIdentifier = "alarms";

                return true;
            }
        });
    }


    // uruchamia odpowiednie Info activity w zalezności jaki obiekt został naciśnięty
    private void startSecondActivity(AdapterView<?> parent, int position, String action) {
        Intent intent = null;
        switch (action) {
            case "event": {
                intent = new Intent(this, EventInfoActivity.class);
                Event tmp = (Event) parent.getItemAtPosition(position);

                intent.putExtra(eventExtra, tmp);
                intent.putExtra("auth_token", auth_token.toString());
                intent.putExtra("position", position);
                break;
            }
            case "note": {
                intent = new Intent(this, NoteInfoActivity.class);
                Note tmp = (Note) parent.getItemAtPosition(position);
                intent.putExtra("auth_token", auth_token.toString());
                intent.putExtra("position", position);

                intent.putExtra(noteExtra, tmp);
            }
        }

        startActivity(intent);
    }

    private void setDate() {
        TextView currentDataTxV = (TextView) findViewById(R.id.dataTxV);
        String dayName= new SimpleDateFormat("EEEE").format(Calendar.getInstance().getTime());
        String day = new SimpleDateFormat("dd").format(Calendar.getInstance().getTime());
        if (day.indexOf("0") == 0) day = day.substring(1, 2);
        String month = new SimpleDateFormat("MMMM").format(Calendar.getInstance().getTime());

        String content = "Hello! Today is " + dayName + ",\nthe " + day + ". day of " + month + "!";

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

                myAlarms.add(new Alarm(alarm_id, alarm_time.split("[T.]+")[1].substring(0, 5)));

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

    // funkcja ustawiająca alarm - przyjmuje czas oraz dany alarm
    private PendingIntent Alarm(LocalTime time) {
        // ustawiamy intent oraz pending intent
        Intent intent = new Intent(this, AlarmReceiverActivity.class);
        intent.putExtra(MainActivity.eventExtra, myEvents);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                12345, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // w zależności która godizna została wybrany, tak ustawiany jest alarm
        DateTime date;
        if (new LocalTime().isAfter(time)) {
            date = new DateTime()
                    .withDayOfMonth(new DateTime().getDayOfMonth() + 1)
                    .withHourOfDay(time.getHourOfDay())
                    .withMinuteOfHour(time.getMinuteOfHour());
            alarmManager.set(AlarmManager.RTC_WAKEUP, date.getMillis(), pendingIntent);
        }
        else {
            date = new DateTime()
                    .withHourOfDay(time.getHourOfDay())
                    .withMinuteOfHour(time.getMinuteOfHour());
            alarmManager.set(AlarmManager.RTC_WAKEUP, date.getMillis(), pendingIntent);
        }

        return pendingIntent;
    }

    // czyści aplikacje z danych użytkownika
    private void clearApp() {
        myAlarms.clear();
        alarms = null;
        myEvents.clear();
        events = null;
        myNotes.clear();
        notes = null;
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

    // zamyka FAB przy kliknięciu gdziekolwiek na ekran
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


    @Override
    public void onResume() {
        super.onResume();

        // informujemy adaptery o zmianach który mogły wystąpić wynikające z dodania nowego obiektu
        // w którymś z Create activity
        EventListFragment eventFr = (EventListFragment) getSupportFragmentManager().findFragmentById(R.id.eventFragment);
        ArrayAdapter<Event> eventAdapter = (ArrayAdapter<Event>) eventFr.getListAdapter();
        eventAdapter.notifyDataSetChanged();

        NoteListFragment noteFr = (NoteListFragment) getSupportFragmentManager().findFragmentById(R.id.noteFragment);
        ArrayAdapter<Note> noteAdapter = (ArrayAdapter<Note>) noteFr.getListAdapter();
        noteAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();

        // zapisujemy auth_token w SharedPreferences przy zabiciu aplikacji / przejścia do tła
        if (auth_token != null) {
            SharedPreferences token = getSharedPreferences(AUTH_TOKEN, MODE_PRIVATE);
            SharedPreferences.Editor editor = token.edit();
            editor.clear();

            try {
                editor.putString(TOKEN, auth_token.getString("auth_token"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            editor.commit();
        }
    }

    // funkcja odnawiająca auth_token z SharedPreferences
    private Boolean restoreToken() {
        Boolean result = false;
        SharedPreferences token = getSharedPreferences(AUTH_TOKEN, MODE_PRIVATE);

        if (token.contains(TOKEN)) {
            try {
                auth_token = new JSONObject();

                auth_token.put("auth_token", token.getString(TOKEN, "0"));
                result = true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    // GET request do serwera pobierający wszystkie obiekty z danej kategorii
    public class APISyncTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEndpoint;

        APISyncTask(String endpoint) {
            mEndpoint = endpoint;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpURLConnection httpcon;
            String url = "https://daybook-backend.herokuapp.com/" + mEndpoint;
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

    // DELETE request do serwera usuwający dany obiekt
    public class APIDeleteTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEndpoint;
        private Event mEvent = null;
        private Note mNote = null;
        private Alarm mAlarm = null;
        String url;

        APIDeleteTask(String endpoint, Object object) {
            mEndpoint = endpoint;

            switch (object.getClass().getSimpleName()) {
                case "Event": {
                    mEvent = (Event) object;
                    url = "https://daybook-backend.herokuapp.com/" + mEndpoint + "/" + mEvent.id;
                    break;
                }
                case "Note": {
                    mNote = (Note) object;
                    url = "https://daybook-backend.herokuapp.com/" + mEndpoint + "/" + mNote.id;
                    break;
                }
                case "Alarm": {
                    mAlarm = (Alarm) object;
                    url = "https://daybook-backend.herokuapp.com/" + mEndpoint + "/" + mAlarm.id;
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
                    input = httpcon.getInputStream();

                    BufferedReader br = new BufferedReader(new InputStreamReader(input, "iso-8859-1"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line;

                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    input.close();
                    result = sb.toString();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return true;
        }
    }

    // GET request do serwera wylogowywujący user - niszczony jest auth_token po stronie serwera
    public class UserLogoutTask extends AsyncTask<Void, Void, Boolean> {

        UserLogoutTask() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpURLConnection httpcon;
            String url = "https://daybook-backend.herokuapp.com/auth/logout";
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
                    input = httpcon.getInputStream();

                    BufferedReader br = new BufferedReader(new InputStreamReader(input, "iso-8859-1"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line;

                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    input.close();
                    result = sb.toString();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return true;
        }

        protected void onPostExecute(Boolean result) {
            auth_token = null;
        }
    }
}
