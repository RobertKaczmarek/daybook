package com.example.daybook;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.joda.time.DateTime;
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

// activity odpowiedzialne za edycję wydarzeń
public class EventEditActivity extends AppCompatActivity {
    private static Event event; // wybrane wydarzenie
    private JSONObject auth_token; // token autoryzacji

    private static TextView dateView;

    private APIUpdateTask mUpdateEventTask = null;  // callback do serwera który zaktualizuje wydarzenie

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);

        try {
            Intent received_intent = getIntent();

            // odbieramy auth_token i wybrane wydarzenie z MainActivity
            auth_token = new JSONObject(received_intent.getStringExtra("auth_token"));
            event = received_intent.getParcelableExtra(MainActivity.eventExtra);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        EditText titleView = (EditText) findViewById(R.id.eventEditTitle);
        titleView.setText(event.title);

        EditText descriptionView = (EditText) findViewById(R.id.eventEditDescription);
        descriptionView.setText(event.description);

        //
        dateView = (TextView) findViewById(R.id.eventDateEditView);
        dateView.setText(new DateTime(event.date).toString("dd-MM-yyyy"));
    }

    // funkcja aktualizująca wydarzenie danymi wprowadzonymi przez użytkownika
    public void updateEvent(View view) {
        // walidacja pól dostępnych w activity
        if (validate()) {
            final EditText eventTitle = (EditText) findViewById(R.id.eventEditTitle);
            event.title =  eventTitle.getText().toString();

            final EditText eventDesc = (EditText) findViewById(R.id.eventEditDescription);
            event.description = eventDesc.getText().toString();

            mUpdateEventTask = new APIUpdateTask(event.id, event.title, event.description, event.date);
            mUpdateEventTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
        }
    }

    // DatePicker do wybrania daty
    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    // funkcja aktualizująca datę w modelu
    public static void setDate(Integer day, Integer month, Integer year) {
        event.date = day + "-" + month + "-" + year;

        dateView.setText(event.date);
    }

    // funkcja walidująca pola
    public boolean validate() {
        boolean valid = true;

        final EditText eventTitle = (EditText) findViewById(R.id.eventEditTitle);
        String title = eventTitle.getText().toString();

        if (title.isEmpty()) {
            eventTitle.setError("title cannot be blank!");
            valid = false;
        }
        else if (title.length() >= 30) {
            eventTitle.setError("title must be under 30 characters!");
            valid = false;
        }
        else if (title.startsWith(" ")) {
            eventTitle.setError("title cannot start with space!!");
            valid = false;
        }
        else eventTitle.setError(null);

        return valid;
    }


    // PUT request do serwera aktualizujący na nim dane
    public class APIUpdateTask extends AsyncTask<Void, Void, Boolean> {

        private final Integer mId;
        private final String mTitle;
        private final String mDescirption;
        private final String mDate;

        APIUpdateTask(Integer id, String title, String desc, String date) {
            mId = id;
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
            String url = "https://daybook-backend.herokuapp.com/events/" + mId;
            try {
                httpcon = (HttpURLConnection) ((new URL(url).openConnection()));
                httpcon.setDoOutput(true);
                httpcon.setRequestProperty("Content-Type", "application/json");
                httpcon.setRequestProperty("Authorization", auth_token.get("auth_token").toString());
                httpcon.setRequestMethod("PUT");

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
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return true;
        }

        // funkcja wykonująca się po zawartości AsyncTask - przekazuje zaktualiony obiekt do MainActivity
        // przekazanie obiektu a nie ponowne pobranie go pozwala zwiększyć wydajność aplikacji
        // tym samym minimalizujemy niepotrzebne odniesienia do serwera
        protected void onPostExecute(Boolean result) {
            Intent intent = new Intent();
            intent.putExtra(MainActivity.eventExtra, event);

            setResult(9, intent);
            finish();
        }
    }
}
