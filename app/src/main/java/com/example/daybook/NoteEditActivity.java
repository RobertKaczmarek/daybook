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

// activity odpowiedzialne za edycje notatki
public class NoteEditActivity extends AppCompatActivity {
    private static Note note;

    private JSONObject auth_token;

    private APIUpdateTask mUpdateNoteTask = null; // callback do serwera który zaktualizuje notatke

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        try {
            Intent received_intent = getIntent();

            // odbieramy auth_token i wybraną notatke z MainActivity
            auth_token = new JSONObject(received_intent.getStringExtra("auth_token"));
            note = received_intent.getParcelableExtra(MainActivity.noteExtra);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        EditText descriptionView = (EditText) findViewById(R.id.noteEditDescription);
        descriptionView.setText(note.description);
    }

    // funkcja odpowiedzialna za aktualizację notatki
    public void updateNote(View view) {
        // na podstawie wypełnionych pół podejmujemy decyzje o aktualizacji notatki
        if (validate()) {
            final EditText noteDesc = (EditText) findViewById(R.id.noteEditDescription);
            note.description = noteDesc.getText().toString();

            mUpdateNoteTask = new APIUpdateTask(note.id, note.description);
            mUpdateNoteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
        }
    }

    // metoda walidująca wypełnione pola
    public boolean validate() {
        boolean valid = true;

        final EditText noteDesc = (EditText) findViewById(R.id.noteEditDescription);
        String description = noteDesc.getText().toString();

        if (description.isEmpty()) {
            noteDesc.setError("description cannot be blank!");
            valid = false;
        } else {
            noteDesc.setError(null);
        }

        return valid;
    }


    // PUT request do serwera aktualizujący na nim dane
    public class APIUpdateTask extends AsyncTask<Void, Void, Boolean> {

        private final Integer mId;
        private final String mDescirption;

        APIUpdateTask(Integer id, String desc) {
            mId = id;
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
            String url = "https://daybook-backend.herokuapp.com/notes/" + mId;
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
            intent.putExtra(MainActivity.noteExtra, note);

            setResult(8, intent);
            finish();
        }
    }
}
