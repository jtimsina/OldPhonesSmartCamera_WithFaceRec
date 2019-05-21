package com.hqcd.smartsecuritycamera;

import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private Button registerButton;
    private EditText registerEmail, registerPW, registerDisplayName;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerButton = (Button)findViewById(R.id.register_registerButton);
        registerEmail = (EditText)findViewById(R.id.register_emailET);
        registerPW = (EditText)findViewById(R.id.register_pwET);
        registerDisplayName = (EditText)findViewById(R.id.register_displayNameET);

        mAuth = FirebaseAuth.getInstance();
    }

    public void createAccount(){
        String emailText = registerEmail.getText().toString();
        String pwText = registerPW.getText().toString();
        String displayText = registerDisplayName.getText().toString();

        if((emailText.length() < 1) || (pwText.length() < 1) || (displayText.length() < 1))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Missing Fields");
            builder.setTitle("All Fields Must be Filled In");
            builder.setPositiveButton("Ok", null);

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else if(pwText.length() < 6)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Password must be at least 6 characters long");
            builder.setTitle("Password Requirement Not Met");
            builder.setPositiveButton("Ok", null);

            AlertDialog dialog = builder.create();
            dialog.show();
        }

        else {
            mAuth.createUserWithEmailAndPassword(registerEmail.getText().toString(), registerPW.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(registerDisplayName.getText().toString()).build();
                                user.updateProfile(profileChangeRequest);
                                Toast.makeText(getApplicationContext(), "Authentication succeeded.", Toast.LENGTH_SHORT).show();
                                finish();
                                //updateUI(user);

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                                //updateUI(null);
                            }
                        }
                    });
        }
    }

    public void onClick(View view){
        switch(view.getId())
        {
            case R.id.register_registerButton:
                createAccount();
                break;
        }
    }
}
