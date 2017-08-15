package com.example.daybook;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * This activity will be called when the alarm is triggered.
 *
 * @author Michael Irwin
 */

public class AlarmReceiverActivity extends Activity {
    private MediaPlayer mMediaPlayer;
    private ExpandableListView listView;
    private ExpandableListAdapter listAdapter;
    private List<String> mListEvents;
    private ArrayList<Event> mEvents = new ArrayList<Event>();
    private HashMap<String, List<String>> listHash;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.alarm);

        Intent received_intent = getIntent();
            mEvents = (ArrayList<Event>) received_intent.getSerializableExtra((MainActivity.eventExtra));

//            if (events.length() != 0) {
//                for (int i = 0; i < events.length(); i++) {
//                    JSONObject event = events.getJSONObject(i);
//
//                    Integer event_id = event.getInt("id");
//                    String event_title = event.getString("title");
//                    String event_desc = event.getString("description");
//                    String event_date = event.getString("date");
//
//                    mEvents.add(new Event(event_id, event_title, event_desc, event_date));
//                }
//            }

        if (mEvents.isEmpty()) {
            TextView info = (TextView) findViewById(R.id.dayInfo);
            info.setText("You have no events scheduled for today! You can enjoy your day!");
        }
        else {
            TextView info = (TextView) findViewById(R.id.dayInfo);
            info.setText("Today will be fun! You have these events planned:");

            listView = (ExpandableListView) findViewById(R.id.eventExpandable);
            initData();
            listAdapter = new ExpandableListAdapter(this, mListEvents, listHash);
            listView.setAdapter(listAdapter);
        }


        Button stopAlarm = (Button) findViewById(R.id.stopAlarm);
        stopAlarm.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                mMediaPlayer.stop();
                finish();
                return false;
            }
        });

        playSound(this, getAlarmUri());
    }

    private void initData() {
        mListEvents = new ArrayList<String>();
        listHash = new HashMap<>();

        for (int i = 0; i < mEvents.size(); i++) {
            Event event = mEvents.get(i);
            mListEvents.add(event.title);

            ArrayList<String> eventList = new ArrayList<>();
            if (!event.description.isEmpty()) {
                eventList.add("Additional information: " + event.description);
            }
            listHash.put(mListEvents.get(i), eventList);
        }
    }

    private void playSound(Context context, Uri alert) {
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(context, alert);
            final AudioManager audioManager = (AudioManager) context
                    .getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }
        } catch (IOException e) {
            System.out.println("OOPS");
        }
    }

    //Get an alarm sound. Try for an alarm. If none set, try notification,
    //Otherwise, ringtone.
    private Uri getAlarmUri() {
        Uri alert = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alert == null) {
            alert = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (alert == null) {
                alert = RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }
        return alert;
    }
}