package com.example.daybook;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

import butterknife.ButterKnife;
import butterknife.InjectView;

// activity odpowiedzialne za logowanie użytkownika
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    private UserLoginTask mAuthTask = null; // callback do serwera tworzący użytkownika
    private String result = null; // odpowiedź z serwera

    @InjectView(R.id.input_email) EditText _emailText;
    @InjectView(R.id.input_password) EditText _passwordText;
    @InjectView(R.id.btn_login) Button _loginButton;
    @InjectView(R.id.link_signup) TextView _signupLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
    }

    // funckcja odpowiedzialna za logowanie
    public void login() {
        Log.d(TAG, "Login");

        // sprawdzamy, czy pola zostały poprawnie wypełnione
        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        // ProcessDialog informujący, że coś się dzieje w aplikacji
        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        // zapytanie do serwera logujące użytkownika
        mAuthTask = new UserLoginTask(email, password, progressDialog);
        mAuthTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    // funkcja wywołująca się po wróceniu z Signup activity - przekazywany z niej auth_token
    // jest przekazywany dalej do MainActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                try {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("auth_token", data.getStringExtra("auth_token"));
                    setResult(7, intent);

                    finish();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // zapobiega wróceniu do MainActivty za pomocą przycisku back
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    // funkcja wywołująca się po zakończeniu połączenia z serwerem
    public void onLogin() {
        if (result != null) onLoginSuccess();
        else onLoginFailed();
    }

    // poprawne logowanie - dane istnieją na serwerze
    public void onLoginSuccess() {
        _loginButton.setEnabled(true);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("auth_token", result);
        setResult(7, intent);

        finish();
    }

    // niepoprawne logowanie - użytkownik nie istnieje / błędne dane
    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Invalid credentials.", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    // funkcja sprawdzająca poprawność wypełnenia pól
    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }


    // POST request do serwera logujący użytkownika - zwraca auth_token używany
    // przy każdym innym requeście do autoryzacji użytkownika
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private final ProgressDialog mProgressDialog;

        UserLoginTask(String email, String password, ProgressDialog dialog) {
            mEmail = email;
            mPassword = password;
            mProgressDialog = dialog;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            JSONObject json = new JSONObject();
            try {
                json.put("email", mEmail);
                json.put("password", mPassword);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            HttpURLConnection httpcon;
            String url = "https://daybook-backend.herokuapp.com/auth/login";
            try {
                httpcon = (HttpURLConnection) ((new URL(url).openConnection()));
                httpcon.setDoOutput(true);
                httpcon.setRequestProperty("Content-Type", "application/json");
                httpcon.setRequestProperty("Accept", "application/json");
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
                result = sb.toString();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        }

        protected void onPostExecute(Boolean result) {
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            onLogin();
                            mProgressDialog.dismiss();
                        }
                    }, 3000);

        }
    }
}
