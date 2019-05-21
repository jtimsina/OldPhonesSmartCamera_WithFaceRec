package com.hqcd.smartsecuritycamera;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;

public class LogInActivity extends AppCompatActivity {

    private EditText emailField, passwordField;
    private Button toRegisterActivity, loginButton;
    private FirebaseAuth mAuth;
    private static final String TAG = "LogInActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        emailField = (EditText)findViewById(R.id.login_emailET);
        passwordField = (EditText)findViewById(R.id.login_pwET);
        toRegisterActivity = (Button)findViewById(R.id.login_toRegister);
        loginButton = (Button)findViewById(R.id.login_loginButton);

        mAuth = FirebaseAuth.getInstance();


    }



    public void signIn(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    public void onClick(View view)
    {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        switch (view.getId())
        {
            case R.id.login_toRegister:
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
                break;
            case R.id.login_loginButton:
                signIn(email, password);
                break;
        }
    }
}
