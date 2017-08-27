package com.example.daybook;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.joda.time.LocalTime;
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

// activity do tworzenia nowych alarmów
public class AlarmCreateActivity extends AppCompatActivity {
  private APICreateTask mCreateAlarmTask = null; // callback do serwera, który utworzy na nim alarm
  private String auth_token; // token autoryzacji

  private static TextView timeView; // TextView używane do ustawiania i wyświetlania godziny
  private static String time; // czas

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_alarm_create);

    Intent received_intent = getIntent();

    // pobierany jest auth_token wysłany z MainActivity
    auth_token = received_intent.getStringExtra("auth_token");

    timeView = (TextView) findViewById(R.id.alarmTimeView);
  }

  // funkcja uruchamiająca asynchroniczny task, który jest odwołaniem się do serwera
  public void createAlarm(View view) {
    // zapobiega crashowaniu się aplikacji przy nie wybraniu czasu alarmu
    if (time == null) time = new LocalTime().toString("HH:mm");

    // asynchroniczne zadanie wykonujące request do serwera
    mCreateAlarmTask = new APICreateTask(time);
    mCreateAlarmTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
  }

  // TimePicker wykorzystywany do wybierania czasu
  public void showTimePickerDialog(View v) {
    DialogFragment newFragment = new TimePickerFragment();
    newFragment.show(getFragmentManager(), "timePicker");
  }

  // funkcja ustawiająca TextView jak i zmienna Time na czas dostarczony z TimePickera
  public static void setTime(Integer hours, Integer minutes) {
    String hour = hours.toString();
    String minute = minutes.toString();

    if (hours < 10) hour = "0" + hours;
    if (minutes < 10) minute = "0" + minutes;

    time = hour + ":" + minute;

    timeView.setText(time);
  }


  // POST request do serwera tworzący na nim alarm - potrzebny są zmienny Time i Days (jednak nie wykorzystujemy jej, stąd wysyłamy po prostu 0)
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
        httpcon.setRequestProperty("Authorization", auth_token);
        httpcon.setRequestMethod("POST");

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

    // funkcja wykonująca się po zawartości AsyncTask - ustawia odpowiedni format czasu i przekazuje go w obiekcie do MainActivity
    protected void onPostExecute(Boolean result) {
      try {
        String time = object.getString("time").split("[T.]+")[1].substring(0, 5);
        object.put("time", time);
      } catch (JSONException e) {
        e.printStackTrace();
      }

      Intent intent = new Intent();
      intent.putExtra("object", object.toString());

      setResult(3, intent);
      finish();
    }
  }
}
