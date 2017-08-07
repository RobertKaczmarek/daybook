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
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class EventCreateActivity extends AppCompatActivity {
    private APICreateTask mCreateEventTask = null;
    private JSONObject auth_token;

    private static TextView dateView;

    private static String title;
    private static String description;
    private static String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_create);

        try {
            Intent received_intent = getIntent();
            auth_token = new JSONObject(received_intent.getStringExtra("auth_token"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        dateView = (TextView) findViewById(R.id.eventDateView);
    }

    public void createEvent(View view) {
        final EditText eventTitle = (EditText) findViewById(R.id.eventTitle);
        title =  eventTitle.getText().toString();

        final EditText eventDesc = (EditText) findViewById(R.id.eventDescription);
        description = eventDesc.getText().toString();

        mCreateEventTask = new APICreateTask(title, description, date);
        mCreateEventTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public static void setDate(Integer day, Integer month, Integer year) {
        date = year + "-" + month + "-" + day;

        dateView.setText(date);
    }

    public class APICreateTask extends AsyncTask<Void, Void, Boolean> {

        private final String mTitle;
        private final String mDescirption;
        private final String mDate;
        private JSONObject object;

        APICreateTask(String title, String desc, String date) {
            mTitle = title;
            mDescirption = desc;
            mDate = date;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            JSONObject json = new JSONObject();
            try {
                json.put("title", mTitle);
                json.put("description", mDescirption);
                json.put("date", mDate);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            HttpURLConnection httpcon;
            String url = "https://mysterious-dusk-55204.herokuapp.com/events";
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

            setResult(1, intent);
            finish();
        }
    }
}
