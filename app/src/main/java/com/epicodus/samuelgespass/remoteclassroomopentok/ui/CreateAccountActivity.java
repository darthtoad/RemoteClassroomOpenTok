package com.epicodus.samuelgespass.remoteclassroomopentok.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.epicodus.samuelgespass.remoteclassroomopentok.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;


public class CreateAccountActivity extends AppCompatActivity implements View.OnClickListener {

    Button signInButton;
    Button signUpButton;
    EditText nameEditText;
    EditText emailEditText;
    EditText passwordEditText;
    EditText confirmPassword;


    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private String name;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        nameEditText = (EditText) findViewById(R.id.nameEditText);
        emailEditText = (EditText) findViewById(R.id.emailEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        confirmPassword = (EditText) findViewById(R.id.confirmPasswordEditText);
        signInButton = (Button) findViewById(R.id.signInButton);
        signUpButton = (Button) findViewById(R.id.signUpButton);
        signInButton.setOnClickListener(this);
        signUpButton.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        createAuthStateListener();
        createAuthProgressDialog();
    }

    @Override
    public void onClick(View view) {

        if (view == signInButton) {
            Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        if (view == signUpButton) {
            createNewUser();
        }

    }

    private void createAuthProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading...");
        progressDialog.setMessage("Please wait... \\uD83D\\uDD52");
        progressDialog.setCancelable(false);
    }


    private void createNewUser() {
        name = nameEditText.getText().toString().trim();
        final String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPasswordString = confirmPassword.getText().toString().trim();

        boolean validEmail = isValidEmail(email);
        boolean validName = isValidName(name);
        boolean validPassword = isValidPassword(password, confirmPasswordString);

        if (!validEmail || !validName || !validPassword) return;

        progressDialog.show();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        progressDialog.dismiss();

                        if (task.isSuccessful()) {
                            createFirebaseUserProfile(task.getResult().getUser());
                        } else {
                            Toast.makeText(CreateAccountActivity.this, "Cannot sign up ❌", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void createFirebaseUserProfile(final FirebaseUser user) {
        UserProfileChangeRequest addProfileName = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        user.updateProfile(addProfileName)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("name", user.getDisplayName());
                        }
                    }
                });
    }

    private void createAuthStateListener() {
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    Intent intent = new Intent(CreateAccountActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        };
    }

    private boolean isValidEmail(String email) {
        boolean isGood = (email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches());
        if (!isGood) {
            emailEditText.setError("USE A REAL EMAIL ADDRESS FOR CRYIN' OUT LOUD!");
            return false;
        }
        return isGood;
    }

    private boolean isValidName(String name) {
        if (name.equals("")) {
            nameEditText.setError("YOU HAVE NOT ENTERED A NAME, YOU IMBECILE!");
            return false;
        }
        return true;
    }

    private boolean isValidPassword(String password, String confirmPassword) {
        if (password.length() < 6) {
            passwordEditText.setError("I'm sorry, I think you password might be a little too short. You can try making it longer than six characters.");
            return false;
        } else if (!password.equals(confirmPassword)) {
            passwordEditText.setError("Excuse me, I hope this doesn't bother you too much, but I believe the passwords that you have entered do not match. Please try again. Thank you!");
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        auth.removeAuthStateListener(authListener);
    }

}
