package com.assignment.sjsu.hudoassenco.cmpe137;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity {

    private EditText _emailText;
    private EditText _nameText;
    private EditText _passwordText;

    private TextInputLayout _nameLayout;

    private Button _loginButton;
    private TextView _signUpLabel;
    //Facebook stuff
    private LoginButton _facebookButton;
    private CallbackManager _facebookCallbackMgr;

    private ProgressDialog _progressDialog;

    private UserLoginTask _authTask;

    private boolean _singUpFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        // Initialize the SDK before executing any other operations,
        // especially, if you're using Facebook UI elements.

        setContentView(R.layout.activity_login);

        _emailText = (EditText) findViewById(R.id.login_email_input);
        _nameText = (EditText) findViewById(R.id.login_name_input);
        _nameLayout = (TextInputLayout) findViewById(R.id.login_name_layout);
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
                _singUpFlag = !_singUpFlag;
                if(_singUpFlag) {
                    _signUpLabel.setText(R.string.signup_link_label);
                    _loginButton.setText(R.string.sign_up_label);
                    _nameLayout.setVisibility(View.VISIBLE);
                } else {
                    _signUpLabel.setText(R.string.signin_link_label);
                    _loginButton.setText(R.string.login_label);
                    _nameLayout.setVisibility(View.GONE);
                }
            }
        });

//        Use this the first time to get your KeyHash for your device.
//        source: http://stackoverflow.com/a/23863110
//        try {
//            PackageInfo info = getPackageManager().getPackageInfo(
//                    "com.example.packagename",
//                    PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//
//        } catch (NoSuchAlgorithmException e) {
//
//        }
        //Facebook stuff initialization
        _facebookButton = (LoginButton) findViewById(R.id.login_facebook_btn);
        _facebookCallbackMgr = CallbackManager.Factory.create();

        //TODO: Check other possible permissions.
        _facebookButton.setReadPermissions("user_friends");

        _facebookButton.registerCallback(_facebookCallbackMgr, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //TODO: Get all user information (Back-end guy)
                openMainActivity();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        _facebookCallbackMgr.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(isLoggedInFacebook()) {
            //TODO: Get all user information (Back-end guy)
            openMainActivity();
        }
    }

    private boolean isLoggedInFacebook() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    private void openMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void attemptLogin() {
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String name = _nameText.getText().toString();

        _emailText.setError(null);
        _passwordText.setError(null);

        View focusView = null;
        boolean cancel = false;

        Utils.ValidationResult validationResult;

        validationResult = Utils.isPasswordValid(password);
        if(!validationResult.valid) {
            _passwordText.setError(getString(validationResult.messageRes));
            focusView = _passwordText;
            cancel = true;
        }

        validationResult = Utils.isEmailValid(email);
        if(!validationResult.valid) {
            _emailText.setError(getString(validationResult.messageRes));
            focusView = _emailText;
            cancel = true;
        }

        if(_singUpFlag) {
            validationResult = Utils.isNameValid(name);
            if(!validationResult.valid) {
                _nameText.setError(getString(validationResult.messageRes));
                focusView = _nameText;
                cancel = true;
            }
        }

        if(cancel) {
            if (focusView != null) {
                focusView.requestFocus();
            }
        } else {
            _progressDialog.show();
            _authTask = new UserLoginTask(email, name, password);
            _authTask.execute();
        }

    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String _email;
        private final String _name;
        private final String _password;

        UserLoginTask(String email, String name, String password) {
            _email = email;
            _name = name;
            _password = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if(_singUpFlag) {
                //TODO: Sign up back end
            } else {
                //TODO: attempt authentication against a network service.
            }
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
                openMainActivity();
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
