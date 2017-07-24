package com.example.daybook;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity {
    private JSONObject auth_token;
    private JSONArray events;
    private APISyncTask mSyncTask = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, 1);

        setDate();
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
                String token = data.getStringExtra("json");
                auth_token = new JSONObject(token);

                setEvents();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
        TextView eventsView = (TextView) findViewById(R.id.eventTxV);

        mSyncTask = new APISyncTask("events");
        mSyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);

       eventsView.setText(events.toString());
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
                    events = new JSONArray(result);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return true;
        }
    }
}
