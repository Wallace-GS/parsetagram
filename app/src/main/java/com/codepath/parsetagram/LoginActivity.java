package com.codepath.parsetagram;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";
    public static final int INVALID_LOGIN = 101;

    private TextInputLayout etUsername;
    private TextInputLayout etPassword;
    private Button btnLogin;
    private Button btnSignup;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (ParseUser.getCurrentUser() != null) {
            goMainActivity();
        }

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);
        progressBar = findViewById(R.id.progressBar);

        Objects.requireNonNull(etUsername.getEditText()).addTextChangedListener(createTextWatcher(etUsername));
        Objects.requireNonNull(etPassword.getEditText()).addTextChangedListener(createTextWatcher(etPassword));

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = etUsername.getEditText().getText().toString();
                String password = etPassword.getEditText().getText().toString();
                signupUser(username, password);
            }
        });
    }

    private TextWatcher createTextWatcher(final TextInputLayout text) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // not needed
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                text.setError(null);
                btnLogin.setEnabled(true);
                btnSignup.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
    }

    private void signupUser(String username, String password) {
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e != null) {
                    String title = "Registration Failed";
                    String message = e.getLocalizedMessage();
                    showErrorDialog(title, message);
                } else {
                    showProgress();
                    goMainActivity();
                }
            }
        });
    }

    private void loginUser() {
        String username = Objects.requireNonNull(etUsername.getEditText()).getText().toString();
        String password = Objects.requireNonNull(etPassword.getEditText()).getText().toString();
        validateUserInput(username, password);

        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    showProgress();
                    goMainActivity();
                } else {
                    String title = "Login Failed";
                    String message = e.getLocalizedMessage();
                    showErrorDialog(title, message);
                }
            }
        });


    }

    private void goMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void validateUserInput(String username, String password) {
        if (username.isEmpty()) {
            etUsername.setError("Username cannot be empty");
            btnLogin.setEnabled(false);
            btnSignup.setEnabled(false);
        } else if (password.isEmpty()) {
            etPassword.setError("Password cannot be empty");
            btnLogin.setEnabled(false);
            btnSignup.setEnabled(false);
        }
    }

    private void showProgress() {
        etUsername.setEnabled(false);
        etPassword.setEnabled(false);
        btnLogin.setVisibility(View.INVISIBLE);
        btnSignup.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}