package com.assignment.sjsu.hudoassenco.cmpe137;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    private EditText _emailText;
    private EditText _passwordText;
    private Button _loginButton;
    private ProgressDialog _progressDialog;

    private UserLoginTask _authTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        _emailText = (EditText) findViewById(R.id.input_email);
        _passwordText = (EditText) findViewById(R.id.input_password);
        _loginButton = (Button) findViewById(R.id.btn_login);

        _progressDialog = new ProgressDialog(LoginActivity.this, R.style.AppTheme_Dialog);
        _progressDialog.setIndeterminate(true);
        _progressDialog.setMessage(getString(R.string.login_progress));

        _loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
    }

    private void attemptLogin() {
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        _emailText.setError(null);
        _passwordText.setError(null);

        View focusView = null;
        boolean cancel = false;

        if(!isPasswordValid(password)) {
            _passwordText.setError(getString(R.string.error_invalid_password));
            focusView = _passwordText;
            cancel = true;
        }

        if(!isEmailValid(email)) {
            _emailText.setError(getString(R.string.error_invalid_email));
            focusView = _emailText;
            cancel = true;
        }

        if(cancel) {
            if (focusView != null) {
                focusView.requestFocus();
            }
        } else {
            _progressDialog.show();
            _authTask = new UserLoginTask(email, password);
            _authTask.execute();
        }

    }

    private boolean isPasswordValid(String password) {
        //TODO: Proper password validation logic.
        return !password.isEmpty();
    }

    private boolean isEmailValid(String email) {
        //TODO: Proper email validation logic.
        return !email.isEmpty();
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String _email;
        private final String _password;

        UserLoginTask(String email, String password) {
            _email = email;
            _password = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            _authTask = null;
            _progressDialog.dismiss();

            if (success) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            } else {
                _passwordText.setError(getString(R.string.error_incorrect_password));
                _passwordText.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            _authTask = null;
            _progressDialog.dismiss();
        }
    }
}
