package com.example.daybook;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.ButterKnife;
import butterknife.InjectView;

// activity odpowiedzialne za rejestrację
public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";

    private UserSignupTask mSignupTask = null; // callback do serwera tworzący użytkownika
    private String auth_token = null; // odpowiedź z serwera

    @InjectView(R.id.input_name) EditText _nameText;
    @InjectView(R.id.input_email) EditText _emailText;
    @InjectView(R.id.input_password) EditText _passwordText;
    @InjectView(R.id.input_password_confirmation) EditText _passwordConfirmationText;
    @InjectView(R.id.btn_signup) Button _signupButton;
    @InjectView(R.id.link_login) TextView _loginLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.inject(this);

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // funkcja rejestrująca
    public void signup() {
        Log.d(TAG, "Signup");

        // walidacja poprawności wypełnienia pól
        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String password_confirmation = _passwordConfirmationText.getText().toString();

        // rozpoczęcie requestu do serwera
        mSignupTask = new SignupActivity.UserSignupTask(name, email, password, password_confirmation, progressDialog);
        mSignupTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    // przy udanej rejestracji
    public void onSignupSuccess() {
        _signupButton.setEnabled(true);

        // przekazujemy auth_token do MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("auth_token", auth_token);

        setResult(RESULT_OK, intent);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Signup failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    // walidacja pól Signup activity
    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String password_confirmation = _passwordConfirmationText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

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

        if (password_confirmation.isEmpty() || password_confirmation.length() < 4 || password_confirmation.length() > 10) {
            _passwordConfirmationText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else if (!password.equals(password_confirmation)) {
            _passwordConfirmationText.setError("must be the same as password");
            valid = false;
        } else {
            _passwordConfirmationText.setError(null);
        }

        return valid;
    }


    // POST request do serwera tworzący nowego użytkownika
    public class UserSignupTask extends AsyncTask<Void, Void, Boolean> {

        private final String mName;
        private final String mEmail;
        private final String mPassword;
        private final String mPasswordConfirmation;
        private final ProgressDialog mProgressDialog;

        UserSignupTask(String name, String email, String password, String password_confirmation, ProgressDialog progressDialog) {
            mName = name;
            mEmail = email;
            mPassword = password;
            mPasswordConfirmation = password_confirmation;
            mProgressDialog = progressDialog;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            JSONObject json = new JSONObject();
            try {
                json.put("full_name", mName);
                json.put("email", mEmail);
                json.put("password", mPassword);
                json.put("password_confirmation", mPasswordConfirmation);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            HttpURLConnection httpcon;
            String url = "https://daybook-backend.herokuapp.com/signup";
            try {
                httpcon = (HttpURLConnection) ((new URL(url).openConnection()));
                httpcon.setDoOutput(true);
                httpcon.setRequestProperty("Content-Type", "application/json");
                httpcon.setRequestMethod("POST");

                OutputStream os = httpcon.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(json.toString());
                writer.close();
                os.close();

                int status = httpcon.getResponseCode();
                InputStream error = httpcon.getErrorStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(httpcon.getInputStream(), "UTF-8"));

                String line = null;
                StringBuilder sb = new StringBuilder();

                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                br.close();
                auth_token = sb.toString();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        }

        // funkcja wykonująca się po AsyncTasku
        protected void onPostExecute(Boolean result) {
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            onSignupSuccess();
                            mProgressDialog.dismiss();
                        }
                    }, 3000);
        }
    }
}
