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
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import java.lang.reflect.Array;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import bolts.Task;

public class LoginActivity extends AppCompatActivity {

    private EditText mEmailText;
    private EditText mNameText;
    private EditText mPasswordText;

    private TextInputLayout mNameLayout;

    private Button mLoginButton;
    private TextView mSignUpLabel;

    private Button mLoginWithFacebookButton;

    private ProgressDialog mProgressDialog;

    private UserLoginTask mAuthTask;

    private boolean mSingUpFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        // Initialize the SDK before executing any other operations,
        // especially, if you're using Facebook UI elements.

        setContentView(R.layout.activity_login);

        mEmailText = (EditText) findViewById(R.id.login_email_input);
        mNameText = (EditText) findViewById(R.id.login_name_input);
        mNameLayout = (TextInputLayout) findViewById(R.id.login_name_layout);
        mPasswordText = (EditText) findViewById(R.id.login_passwd_input);
        mLoginButton = (Button) findViewById(R.id.login_btn);
        mSignUpLabel = (TextView) findViewById(R.id.login_signup_label);
        mLoginWithFacebookButton = (Button) findViewById(R.id.login_facebook_btn);

        mProgressDialog = new ProgressDialog(LoginActivity.this, R.style.AppTheme_Dialog);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(getString(R.string.login_progress));

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
        mSignUpLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSingUpFlag = !mSingUpFlag;
                if (mSingUpFlag) {
                    mSignUpLabel.setText(R.string.signup_link_label);
                    mLoginButton.setText(R.string.sign_up_label);
                    mNameLayout.setVisibility(View.VISIBLE);
                } else {
                    mSignUpLabel.setText(R.string.signin_link_label);
                    mLoginButton.setText(R.string.login_label);
                    mNameLayout.setVisibility(View.GONE);
                }
            }
        });

        mLoginWithFacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] permissions = {"user_photos","user_friends"};
                ParseFacebookUtils.logInWithReadPermissionsInBackground(LoginActivity.this,
                        Arrays.asList(permissions), new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                if (user == null) {
                                    Log.d("CMPE137", "User cancelled login");
                                } else if(user.isNew()) {

                                } else {
                                    openMainActivity();
                                }
                            }
                        });
            }
        });

//        Use this the first time to get your KeyHash for your device.
//        source: http://stackoverflow.com/a/23863110
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.assignment.sjsu.hudoassenco.cmpe137",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
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
        String email = mEmailText.getText().toString();
        String password = mPasswordText.getText().toString();
        String name = mNameText.getText().toString();

        mEmailText.setError(null);
        mPasswordText.setError(null);

        View focusView = null;
        boolean cancel = false;

        Utils.ValidationResult validationResult;

        validationResult = Utils.isPasswordValid(password);
        if(!validationResult.mValid) {
            mPasswordText.setError(getString(validationResult.mMessageRes));
            focusView = mPasswordText;
            cancel = true;
        }

        validationResult = Utils.isEmailValid(email);
        if(!validationResult.mValid) {
            mEmailText.setError(getString(validationResult.mMessageRes));
            focusView = mEmailText;
            cancel = true;
        }

        if(mSingUpFlag) {
            validationResult = Utils.isNameValid(name);
            if(!validationResult.mValid) {
                mNameText.setError(getString(validationResult.mMessageRes));
                focusView = mNameText;
                cancel = true;
            }
        }

        if(cancel) {
            if (focusView != null) {
                focusView.requestFocus();
            }
        } else {
            mProgressDialog.show();
            mAuthTask = new UserLoginTask(email, name, password);
            mAuthTask.execute();
        }

    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mName;
        private final String mPassword;

        UserLoginTask(String email, String name, String password) {
            mEmail = email;
            mName = name;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if(mSingUpFlag) {
                    ParseUser user = new ParseUser();
                    user.setEmail(mEmail);
                    user.setUsername(mEmail);
                    user.setPassword(mPassword);
                    user.put("name", mName);
                    user.signUp();

                    return true;
                } else {
                    ParseUser user = ParseUser.logIn(mEmail, mPassword);
                    if(user != null) {
                        return true;
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            mProgressDialog.dismiss();

            if (success) {
                openMainActivity();
            } else {
                mPasswordText.setError(getString(R.string.error_incorrect_password));
                mPasswordText.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            mProgressDialog.dismiss();
        }
    }
}
