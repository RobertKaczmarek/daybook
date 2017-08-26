package com.example.daybook;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

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

// activity odpowiedzialne za tworzenie notatki
public class NoteCreateActivity extends AppCompatActivity {
    private APICreateTask mCreateNoteTask = null; // callback do serwera w celu utworzenia nowego wydarzenia
    private String auth_token; // token autoryzacji

    private static String description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_create);

        Intent received_intent = getIntent();

        // przechwytujemy auth_token z MainActivity
        auth_token = received_intent.getStringExtra("auth_token");
    }

    // funkcja odpowiedzialna za tworzenie notatek na serwerze
    public void createNote(View view) {
        // na podstawie wypełnionych pół podejmujemy decyzje o tworzeniu notatki
        if (validate()) {
            final EditText noteDesc = (EditText) findViewById(R.id.noteDescription);
            description = noteDesc.getText().toString();

            mCreateNoteTask = new APICreateTask(description);
            mCreateNoteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
        }
    }

    // metoda walidująca wypełnione pola
    public boolean validate() {
        boolean valid = true;

        final EditText noteDesc = (EditText) findViewById(R.id.noteDescription);
        String description = noteDesc.getText().toString();

        if (description.isEmpty()) {
            noteDesc.setError("description cannot be blank!");
            valid = false;
        } else {
            noteDesc.setError(null);
        }

        return valid;
    }


    // POST request do serwera tworzący wydarzenie
    public class APICreateTask extends AsyncTask<Void, Void, Boolean> {

        private final String mDescirption;
        private String object;

        APICreateTask(String desc) {
            mDescirption = desc;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            JSONObject json = new JSONObject();
            try {
                json.put("description", mDescirption);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            HttpURLConnection httpcon;
            String url = "https://daybook-backend.herokuapp.com/notes";
            try {
                httpcon = (HttpURLConnection) ((new URL(url).openConnection()));
                httpcon.setDoOutput(true);
                httpcon.setRequestProperty("Content-Type", "application/json");
                httpcon.setRequestProperty("Authorization", auth_token);
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
                object = sb.toString();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        }

        // funkcja wykonująca się po zawartości AsyncTask - przekazuje stworzony obiekt do MainActivity
        protected void onPostExecute(Boolean result) {
            Intent intent = new Intent();
            intent.putExtra("object", object);

            setResult(2, intent);
            finish();
        }
    }
}
