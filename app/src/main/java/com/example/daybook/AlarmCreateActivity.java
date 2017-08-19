package com.example.daybook;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class AlarmCreateActivity extends AppCompatActivity {
    private APICreateTask mCreateAlarmTask = null;
    private JSONObject auth_token;

    private static TextView timeView;


    private static String time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_create);

        try {
            Intent received_intent = getIntent();
            auth_token = new JSONObject(received_intent.getStringExtra("auth_token"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        timeView = (TextView) findViewById(R.id.alarmTimeView);

    }

    public void createAlarm(View view) {
        mCreateAlarmTask = new APICreateTask(time);
        mCreateAlarmTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    public static void setTime(Integer hours, Integer minutes) {
        time = hours + ":" + minutes;

        timeView.setText(time);
    }

    public class APICreateTask extends AsyncTask<Void, Void, Boolean> {

        private final String mTime;
        private JSONObject object;

        APICreateTask(String time) {
            mTime = time;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            JSONObject json = new JSONObject();
            try {
                json.put("time", mTime);
                json.put("days", 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            HttpURLConnection httpcon;
            String url = "https://daybook-backend.herokuapp.com/alarms";
            try {
                httpcon = (HttpURLConnection) ((new URL(url).openConnection()));
                httpcon.setDoOutput(true);
                httpcon.setRequestProperty("Content-Type", "application/json");
                httpcon.setRequestProperty("Authorization", auth_token.get("auth_token").toString());
                httpcon.setRequestMethod("POST");
//                httpcon.connect();

                OutputStream os = httpcon.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(json.toString());
                writer.close();
                os.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(httpcon.getInputStream(), "UTF-8"));

                String line = null;
                StringBuilder sb = new StringBuilder();

                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                br.close();
                object = new JSONObject(sb.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return true;
        }

        protected void onPostExecute(Boolean result) {
            Intent intent = new Intent();
            intent.putExtra("object", object.toString());

            setResult(3, intent);
            finish();
        }
    }
}
