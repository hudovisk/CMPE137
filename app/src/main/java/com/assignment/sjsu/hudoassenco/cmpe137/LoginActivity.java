package com.assignment.sjsu.hudoassenco.cmpe137;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

public class LoginActivity extends AppCompatActivity {

    private EditText _emailText;
    private EditText _passwordText;
    private Button _loginButton;
    private TextView _signUpLabel;
    //Facebook stuff
    private LoginButton _facebookButton;
    private CallbackManager _facebookCallbackMgr;

    private ProgressDialog _progressDialog;

    private UserLoginTask _authTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        // Initialize the SDK before executing any other operations,
        // especially, if you're using Facebook UI elements.

        setContentView(R.layout.activity_login);

        _emailText = (EditText) findViewById(R.id.login_email_input);
        _passwordText = (EditText) findViewById(R.id.login_passwd_input);
        _loginButton = (Button) findViewById(R.id.login_btn);
        _signUpLabel = (TextView) findViewById(R.id.login_signup_label);

        _progressDialog = new ProgressDialog(LoginActivity.this, R.style.AppTheme_Dialog);
        _progressDialog.setIndeterminate(true);
        _progressDialog.setMessage(getString(R.string.login_progress));

        _loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
        _signUpLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Call sign up activity here!
            }
        });

        //Facebook stuff initialization
        _facebookButton = (LoginButton) findViewById(R.id.login_facebook_btn);
        _facebookCallbackMgr = CallbackManager.Factory.create();

        //TODO: Check other possible permissions.
        _facebookButton.setReadPermissions("user_friends");

        _facebookButton.registerCallback(_facebookCallbackMgr, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //TODO: Get all user information (Back-end guy)
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                Log.e(getString(R.string.log_tag), exception.toString());
                exception.printStackTrace();
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
